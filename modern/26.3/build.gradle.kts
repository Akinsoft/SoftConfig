plugins {
    id("moulconfig.fabric")
    id("moulconfig.test")
}
fabricDeps {
    impl("fabric-command-api-v2")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))
dependencies {
    testImplementation("org.lwjgl:lwjgl-sdl:3.4.3")
}
