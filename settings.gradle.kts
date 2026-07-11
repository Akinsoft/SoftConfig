pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://oss.sonatype.org/content/repositories/snapshots")
		maven("https://maven.architectury.dev/")
		maven("https://maven.fabricmc.net")
		maven("https://maven.wagyourtail.xyz/releases")
		maven("https://maven.wagyourtail.xyz/snapshots")
		maven("https://maven.neoforged.net/releases")
		maven("https://maven.minecraftforge.net/")
		maven("https://repo.spongepowered.org/maven/")
        maven {
            url = uri("https://repo.sk1er.club/repository/maven-releases/")
            content {
                excludeGroup("xyz.wagyourtail.unimined.mapping")
            }
        }
	}
	resolutionStrategy {
		eachPlugin {
			when (requested.id.id) {
				"gg.essential.loom" -> useModule("gg.essential:architectury-loom:${requested.version}")
			}
		}
	}
}

rootProject.name = "SoftConfig"

include("common")
include("modern")
include("detekt-rules")
providers.gradleProperty("softconfig.activeMinecraftVersions").get()
	.split(",")
	.map(String::trim)
	.filter(String::isNotEmpty)
	.forEach { version ->
	val modPath = "modern:$version"
	include(modPath)
	project(":$modPath").name = "modern-$version"
}
includeBuild("build-src")
