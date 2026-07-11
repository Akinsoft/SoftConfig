# SoftConfig

SoftConfig is an independently maintained, compatibility-focused fork of MoulConfig. It preserves MoulConfig's packages, identifiers, and configuration formats so existing mods can migrate without rewriting their configuration code.

SoftConfig is not affiliated with or endorsed by NotEnoughUpdates or the original MoulConfig maintainers.

## Installation

Stable releases are published to Maven Central under `io.github.akinsoft.softconfig`. SoftConfig is split into a shared `common` artifact and platform artifacts named `modern-<minecraftVersion>` or `legacy`.

### Modern

Use the platform artifact matching your Minecraft version. Keep your existing shadow, relocation, and access widener configuration.

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    shadowModImpl("io.github.akinsoft.softconfig:modern-26.2:4.8.0")
}

tasks.shadowJar {
    relocate("io.github.notenoughupdates.moulconfig", "my.mod.deps.moulconfig")
}
```

### Legacy

Legacy 1.8.9 builds remain available on a best-effort basis:

```kotlin
dependencies {
    shadowModImpl("io.github.akinsoft.softconfig:legacy:4.8.0")
}
```

The development-only `io.github.notenoughupdates.moulconfig.tweaker.DevelopmentResourceTweaker` remains available for legacy development environments.

## Migrating

Existing MoulConfig 4.x consumers normally change only the Maven group and version. See the [migration guide](https://github.com/Akinsoft/SoftConfig/blob/main/MIGRATING.md).

## Usage

See the configuration documentation and generated API reference for annotations, processors, managed configuration, GUI components, and XML layouts.

## License and upstream

SoftConfig is LGPL-3.0-or-later and is derived from [NotEnoughUpdates/MoulConfig](https://github.com/NotEnoughUpdates/MoulConfig). Original attribution and Git history are preserved.
