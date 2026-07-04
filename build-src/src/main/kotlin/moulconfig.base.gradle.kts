import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.charset.StandardCharsets

repositories {
	mavenLocal()
	mavenCentral()
	maven("https://repo.nea.moe/releases")
	maven("https://repo.spongepowered.org/maven/")
	maven("https://maven.neoforged.net/releases")
}


group = providers.gradleProperty("moulconfig.publishGroup")
	.orElse("org.notenoughupdates.moulconfig")
	.get()
version = providers.gradleProperty("moulconfig.publishVersion")
	.orElse(if (Version.isSnapshot) "9999.9999.9999" else Version.tag!!)
	.get()

tasks.withType(JavaCompile::class) {
	options.encoding = StandardCharsets.UTF_8.name()
}

tasks.withType(ShadowJar::class).configureEach {
	relocate("juuxel.libninepatch", "io.github.notenoughupdates.moulconfig.deps.libninepatch")
}
afterEvaluate {
	extensions.findByType<PublishingExtension>()?.apply {
		repositories {
			if (project.hasProperty("moulconfigPassword") && !Version.isSnapshot) {
				maven {
					url = uri("https://maven.notenoughupdates.org/releases")
					name = "moulconfig"
					credentials(PasswordCredentials::class)
					authentication {
						create<BasicAuthentication>("basic")
					}
				}
			}
		}
		publications.filterIsInstance<MavenPublication>().forEach {
			it.pom {
				licenses {
					license {
						name.set("LGPL-3.0 or later")
						url.set("https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/HEAD/COPYING.LESSER")
					}
				}
				developers {
					developer {
						name.set("NotEnoughUpdates contributors")
					}
					developer {
						name.set("Linnea Gräf")
					}
				}
				scm {
					url.set(
						providers.gradleProperty("moulconfig.scmUrl")
							.orElse("https://github.com/NotEnoughUpdates/MoulConfig")
					)
				}
			}
		}
	}
}
