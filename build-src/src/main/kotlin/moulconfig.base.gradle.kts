import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.charset.StandardCharsets

plugins {
    signing
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.nea.moe/releases")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.notenoughupdates.org/releases") {
        content {
            includeGroup("org.notenoughupdates.moulconfig")
        }
    }
}

group = providers.gradleProperty("softconfig.group").get()
version = providers.gradleProperty("softconfig.releaseVersion")
    .orElse(providers.gradleProperty("softconfig.version"))
    .get()

tasks.withType<JavaCompile>().configureEach {
    options.encoding = StandardCharsets.UTF_8.name()
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(rootProject.file("LICENSE")) {
        into("META-INF/licenses")
        rename { "SoftConfig-LGPL-3.0.txt" }
    }
    from(rootProject.file("COPYING")) {
        into("META-INF/licenses")
        rename { "GNU-GPL-3.0.txt" }
    }
    from(rootProject.file("NOTICE")) {
        into("META-INF")
        rename { "NOTICE-SoftConfig" }
    }
    from(rootProject.file("third-party/libninepatch-MPL-2.0.txt")) {
        into("META-INF/licenses")
    }
}

tasks.withType<ShadowJar>().configureEach {
    relocate("juuxel.libninepatch", "io.github.notenoughupdates.moulconfig.deps.libninepatch")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("LICENSE")
}

afterEvaluate {
    extensions.findByType<PublishingExtension>()?.apply {
        repositories {
            providers.gradleProperty("softconfig.stagingDirectory").orNull?.let { stagingDirectory ->
                maven {
                    name = "centralStaging"
                    url = uri(stagingDirectory)
                }
            }
        }
        publications.filterIsInstance<MavenPublication>().forEach { publication ->
            publication.pom {
                name.set("SoftConfig")
                description.set("A compatibility-focused configuration library for Minecraft mods")
                url.set("https://github.com/Akinsoft/SoftConfig")
                licenses {
                    license {
                        name.set("GNU Lesser General Public License v3.0 or later")
                        url.set("https://github.com/Akinsoft/SoftConfig/blob/main/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("NotEnoughUpdates contributors")
                    }
                    developer {
                        name.set("SoftConfig contributors")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Akinsoft/SoftConfig.git")
                    developerConnection.set("scm:git:ssh://git@github.com/Akinsoft/SoftConfig.git")
                    url.set("https://github.com/Akinsoft/SoftConfig")
                }
            }
        }
    }

    extensions.findByType<SigningExtension>()?.apply {
        val signingKey = providers.gradleProperty("softconfig.signingKey").orNull
        val signingPassword = providers.gradleProperty("softconfig.signingPassword").orNull
        if (signingKey != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(extensions.getByType<PublishingExtension>().publications)
        }
    }
}
