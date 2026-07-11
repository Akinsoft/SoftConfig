# Migrating From MoulConfig

SoftConfig 4.x keeps MoulConfig's packages and APIs, so no source-code or config-file migration is normally required.

1. Ensure your repositories include `mavenCentral()`.
2. Replace `org.notenoughupdates.moulconfig` with `io.github.akinsoft.softconfig`.
3. Change the version to `4.8.1` or newer.

```diff
- implementation("org.notenoughupdates.moulconfig:modern-26.2:4.7.2")
+ implementation("io.github.akinsoft.softconfig:modern-26.2:4.8.1")
```

For a version catalog:

```diff
- moulconfig = { module = "org.notenoughupdates.moulconfig:modern-26.2", version = "4.7.2" }
+ moulconfig = { module = "io.github.akinsoft.softconfig:modern-26.2", version = "4.8.1" }
```

Update every MoulConfig dependency declaration your build uses, including `implementation`, `include`, shadow configurations, and version catalogs. Keep the same artifact name for your Minecraft version. Do not change imports, relocation rules, XML namespaces, assets, or saved configuration files.

Remove the NotEnoughUpdates Maven repository only if no other dependency needs it.

If another dependency shades MoulConfig, update that dependency after its maintainer adopts SoftConfig. Do not bundle SoftConfig beside its existing MoulConfig copy.
