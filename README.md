# SoftConfig

SoftConfig is an independently maintained, compatibility-focused fork of MoulConfig. It preserves MoulConfig's existing packages, identifiers, and configuration formats so mods can migrate without rewriting their configuration code.

SoftConfig is not affiliated with or endorsed by NotEnoughUpdates or the original MoulConfig maintainers.

## Compatibility

SoftConfig 4.x is intended to remain compatible with MoulConfig 4.x. Existing package names, the `moulconfig` mod ID, XML namespaces, assets, and saved configuration formats remain unchanged. Public APIs will not be removed or changed incompatibly until a new major version.

## Supported versions

| Minecraft version | Support |
| --- | --- |
| 1.21.11 | Active |
| 26.1 | Active |
| 26.2 | Active |
| Other published targets | Best effort |

SoftConfig follows Hypixel SkyBlock's update rhythm and normally supports the latest two relevant Minecraft versions. The three active targets above cover the initial transition.

## Installation

Stable releases are published to Maven Central under `io.github.akinsoft.softconfig`. Keep the artifact name matching your Minecraft version:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.akinsoft.softconfig:modern-26.2:4.8.0")
}
```

Continue using your existing `include`, shadow, relocation, and access widener setup.

## Migrating

Migration from MoulConfig is normally a group and version change. See [MIGRATING.md](MIGRATING.md).

## Documentation

Documentation and API references are published at <https://akinsoft.github.io/SoftConfig/>.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the development setup and pull request requirements.

## License

SoftConfig is distributed under LGPL-3.0-or-later. See [LICENSE](LICENSE), [COPYING](COPYING), and [NOTICE](NOTICE).

## Upstream

SoftConfig is derived from [NotEnoughUpdates/MoulConfig](https://github.com/NotEnoughUpdates/MoulConfig). The original repository and contributor history remain linked through GitHub's fork relationship and the retained Git history.
