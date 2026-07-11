repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.akinsoft.softconfig:modern-26.2:4.8.0")
}

tasks.shadowJar {
    relocate("io.github.notenoughupdates.moulconfig", "example.mod.deps.moulconfig")
}
