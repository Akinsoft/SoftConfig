import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.plugins.quality.Checkstyle
import java.security.MessageDigest
import java.util.zip.ZipFile

plugins {
	base
	id("moulconfig.base")
	alias(libs.plugins.mkdocs)
	id("moulconfig.dokka.base")
	id("dev.detekt") version "2.0.0-alpha.2"
	checkstyle
}

mkdocs {
	python {
		pip("mkdocs-zettelkasten:0.1.9")
	}
	publish {
		docPath = ""
	}
	strict = true
}

val compileAllDocs = tasks.register("compileAllDocs", Copy::class) {
    dependsOn(tasks.mkdocsBuild)
    destinationDir = layout.buildDirectory.dir("allDocs").get().asFile
    from(tasks.mkdocsBuild)
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory }) {
        into("javadocs")
    }
}
val docJar = tasks.register("docJar", Zip::class) {
    from(compileAllDocs)
    archiveClassifier.set("javadoc")
}
val docConfig = configurations.create("documentation")
artifacts.add(docConfig.name, docJar)
tasks.assemble { dependsOn(docJar) }

dependencies {
    dokka(project(":common"))
    dokka(project(":legacy"))
    dokka(project(":modern"))
    detektPlugins("dev.detekt:detekt-rules-ktlint-wrapper:${providers.gradleProperty("detekt.version").get()}")
    detektPlugins(project(":detekt-rules"))
}

extensions.configure<DetektExtension> {
    buildUponDefaultConfig = true
    config.setFrom(layout.projectDirectory.file("detekt/detekt.yml"))
    source.setFrom(
        files(
            "common/src/main/java",
            "legacy/src/main/java",
            "modern/templates/java",
        ),
    )
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "25"
    reports {
        html.required.set(true)
        sarif.required.set(true)
    }
}

val prepareModernCheckstyleSource by tasks.registering(Sync::class) {
    dependsOn(":modern:modern-26.2:compileJava")
    from("modern/26.2/build/manifoldSourceDump")
    into(layout.buildDirectory.dir("checkstyle-modern-source"))
    filter { line: String -> line.trimEnd() }
}

val checkstyleAuthored by tasks.registering(Checkstyle::class) {
    description = "Runs Checkstyle against authored Java sources."
    dependsOn(prepareModernCheckstyleSource)
    source(
        fileTree("common/src/main/java") { include("**/*.java") },
        fileTree("legacy/src/main/java") { include("**/*.java") },
        fileTree(layout.buildDirectory.dir("checkstyle-modern-source")) { include("**/*.java") },
    )
    classpath = files()
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
    configFile = layout.projectDirectory.file("config/checkstyle/checkstyle.xml").asFile
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

checkstyle {
    toolVersion = providers.gradleProperty("checkstyle.version").get()
}

val japicmp by configurations.creating {
    isTransitive = false
}
val compatibilityBaseline by configurations.creating {
    isTransitive = false
}

dependencies {
    japicmp("com.github.siom79.japicmp:japicmp:0.23.1:jar-with-dependencies")
    compatibilityBaseline(
        "${providers.gradleProperty("softconfig.compatibilityBaselineGroup").get()}:common:" +
            providers.gradleProperty("softconfig.compatibilityBaseline").get(),
    )
}

val checkBinaryCompatibility by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Rejects binary-incompatible changes to the public common API."
    dependsOn(":common:jar")
    classpath = japicmp
    mainClass.set("japicmp.JApiCmp")
    doFirst {
        val currentJar = project(":common").tasks.named<Jar>("jar").get().archiveFile.get().asFile
        args(
            "--old", compatibilityBaseline.singleFile.absolutePath,
            "--new", currentJar.absolutePath,
            "--only-modified",
            "--ignore-missing-classes",
            "--error-on-binary-incompatibility",
        )
    }
}

val activeMinecraftVersions = listOf("1.21.11", "26.1", "26.2")
val retainedProjects = listOf(
    ":legacy",
    ":modern:modern-1.21.4",
    ":modern:modern-1.21.5",
    ":modern:modern-1.21.7",
    ":modern:modern-1.21.10",
    ":modern:modern-1.21.11",
    ":modern:modern-26.1",
    ":modern:modern-26.2",
)

val retainedBuild by tasks.registering {
    group = "verification"
    description = "Builds legacy and every retained modern target."
    dependsOn(retainedProjects.map { "$it:build" })
}

val verifyArtifacts by tasks.registering {
    group = "verification"
    description = "Verifies published artifact metadata, licenses, sources, Javadocs, and POMs."
    dependsOn(":common:jar", ":common:sourcesJar", ":common:javadocJar")
    activeMinecraftVersions.forEach { minecraftVersion ->
        dependsOn(":modern:modern-$minecraftVersion:remapJar")
        dependsOn(":modern:modern-$minecraftVersion:sourcesJar")
        dependsOn(":modern:modern-$minecraftVersion:javadocJar")
    }
    doLast {
        val requiredEntries = setOf(
            "META-INF/licenses/SoftConfig-LGPL-3.0.txt",
            "META-INF/licenses/GNU-GPL-3.0.txt",
            "META-INF/licenses/libninepatch-MPL-2.0.txt",
            "META-INF/NOTICE-SoftConfig",
        )
        activeMinecraftVersions.forEach { minecraftVersion ->
            val projectName = "modern-$minecraftVersion"
            val libsDirectory = project(":modern:$projectName").layout.buildDirectory.dir("libs").get().asFile
            val jar = libsDirectory.resolve("$projectName-${project.version}.jar")
            check(jar.isFile) { "Missing active artifact: $jar" }
            ZipFile(jar).use { zip ->
                val entries = zip.entries().asSequence().map { it.name }.toSet()
                val entryCount = zip.entries().asSequence().count()
                check(entryCount == entries.size) { "Duplicate ZIP entries in ${jar.name}" }
                check(requiredEntries.all(entries::contains)) { "Missing license or notice entries in ${jar.name}" }
                check("LICENSE" !in entries) { "Unexpected ambiguous root LICENSE in ${jar.name}" }
                check("fabric.mod.json" in entries) { "Missing fabric.mod.json in ${jar.name}" }
                check("moulconfig.accesswidener" in entries) { "Missing access widener in ${jar.name}" }
                val metadata = zip.getInputStream(zip.getEntry("fabric.mod.json")).bufferedReader().use { it.readText() }
                check(Regex("\"id\"\\s*:\\s*\"moulconfig\"").containsMatchIn(metadata)) {
                    "Incorrect Fabric mod id in ${jar.name}"
                }
                check(Regex("\"name\"\\s*:\\s*\"SoftConfig\"").containsMatchIn(metadata)) {
                    "Incorrect Fabric mod name in ${jar.name}"
                }
                check(metadata.contains(project.version.toString())) { "Incorrect Fabric version in ${jar.name}" }
            }
            check(libsDirectory.resolve("$projectName-${project.version}-sources.jar").isFile) {
                "Missing sources artifact for $projectName"
            }
            check(libsDirectory.resolve("$projectName-${project.version}-javadoc.jar").isFile) {
                "Missing Javadoc artifact for $projectName"
            }
            val pom = project(":modern:$projectName").layout.buildDirectory
                .file("publications/maven/pom-default.xml").get().asFile
            check(pom.isFile && pom.readText().contains("<name>SoftConfig</name>")) {
                "Missing or incorrect POM for $projectName"
            }
        }

        val commonLibs = project(":common").layout.buildDirectory.dir("libs").get().asFile
        listOf("", "-sources", "-javadoc").forEach { classifier ->
            check(commonLibs.resolve("common-${project.version}$classifier.jar").isFile) {
                "Missing common$classifier artifact"
            }
        }
    }
}

val centralBundle by tasks.registering(Zip::class) {
    group = "publishing"
    description = "Builds the signed Maven Central upload bundle."
    val stagingDirectory = layout.buildDirectory.dir("central-staging")
    from(stagingDirectory)
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    archiveFileName.set("softconfig-${project.version}-central.zip")
    doFirst {
        check(providers.gradleProperty("softconfig.stagingDirectory").isPresent) {
            "Pass -Psoftconfig.stagingDirectory=${stagingDirectory.get().asFile.absolutePath}"
        }
        check(providers.gradleProperty("softconfig.signingKey").isPresent) {
            "Pass the in-memory signing key with -Psoftconfig.signingKey"
        }
        stagingDirectory.get().asFile.walkTopDown()
            .filter { it.isFile && it.extension !in setOf("md5", "sha1") }
            .toList()
            .forEach { artifact ->
                mapOf("md5" to "MD5", "sha1" to "SHA-1").forEach { (extension, algorithm) ->
                    val digest = MessageDigest.getInstance(algorithm).digest(artifact.readBytes())
                    artifact.parentFile.resolve("${artifact.name}.$extension")
                        .writeText(digest.joinToString("") { "%02x".format(it) })
                }
            }
    }
}

val verifyMigrationFixtures by tasks.registering {
    group = "verification"
    description = "Checks the documented Groovy, Kotlin DSL, catalog, common, and shaded migration examples."
    doLast {
        val directFixtures = listOf(
            "migration-fixtures/groovy/build.gradle",
            "migration-fixtures/kotlin-shadow/build.gradle.kts",
            "migration-fixtures/version-catalog/gradle/libs.versions.toml",
            "migration-fixtures/common/build.gradle.kts",
        )
        directFixtures.forEach { fixture ->
            val contents = file(fixture).readText()
            check("io.github.akinsoft.softconfig" in contents) { "SoftConfig coordinates missing from $fixture" }
            check("org.notenoughupdates.moulconfig:" !in contents) { "Old MoulConfig coordinates remain in $fixture" }
        }
        check(file("migration-fixtures/shaded-transitive/README.md").isFile) {
            "Missing shaded/transitive migration fixture"
        }
    }
}

gradle.projectsEvaluated {
    verifyArtifacts.configure {
        dependsOn(allprojects.flatMap { candidate ->
            candidate.tasks.matching { it.name == "generatePomFileForMavenPublication" }
        })
    }
    centralBundle.configure {
        dependsOn(allprojects.flatMap { candidate ->
            candidate.tasks.matching { it.name == "publishAllPublicationsToCentralStagingRepository" }
        })
    }
}

tasks.named("check") {
    dependsOn(
        tasks.named("detekt"),
        checkstyleAuthored,
        ":detekt-rules:test",
        checkBinaryCompatibility,
        verifyMigrationFixtures,
    )
}
