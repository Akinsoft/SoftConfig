plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

val detektVersion = providers.gradleProperty("detekt.version").get()

dependencies {
    compileOnly("dev.detekt:detekt-api:$detektVersion")
    testImplementation(kotlin("test"))
    testImplementation("dev.detekt:detekt-api:$detektVersion")
    testImplementation("dev.detekt:detekt-test:$detektVersion")
}

tasks.test {
    useJUnitPlatform()
}
