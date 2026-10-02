# Minecraft 26.3 source target

This target was ported from Akinsoft/SoftConfig commit
`f83ff6aaefb09860775b9b05e5f2d1bdb930af50`, alongside the retained
1.21.11, 26.1 and 26.2 targets. The existing packages, Fabric ID, configuration
formats and common behavior remain compatible with SoftConfig 4.x.

## Toolchains and build

Run the checked-in wrapper with JDK 25. Common Java and Kotlin output retains
the Java 8 ABI; its Manifold/Lombok compilation needs a real JDK 8 toolchain.
The 26.1, 26.2 and 26.3 platform projects select JDK 25. The retained 1.21.11
project selects JDK 21, which is also needed when building every target.
Gradle can discover these installations, or callers can pass their paths with
`org.gradle.java.installations.paths`.

From the repository root, a bounded 26.3 build is:

```sh
./gradlew :modern:modern-26.3:remapJar :common:test \
  :modern:modern-26.3:test :modern:modern-26.3:generatePomFileForMavenPublication \
  checkstyleAuthored --no-daemon --max-workers=4 \
  -Psoftconfig.activeMinecraftVersions=26.3 \
  -Psoftconfig.releaseVersion=4.8.5 \
  -Dsoftconfig.sourceRevision=f83ff6aaefb09860775b9b05e5f2d1bdb930af50 \
  -Porg.gradle.java.installations.paths="$JDK8_PORT_HOME,$JDK25_PORT_HOME"
```

Set `JDK8_PORT_HOME` and `JDK25_PORT_HOME` to the actual installations when
using the last option; omit it when discovery already works. The source
revision property supplies archive provenance when Git metadata is absent.
The release-version property overrides the existing SNAPSHOT default for this
local compatibility build; it does not publish or replace an upstream release.

To check the retained target matrix, run all four `remapJar` tasks plus
`:common:test`, `:modern:modern-26.3:test` and `checkstyleAuthored`, without the
single-target property. Include the JDK 21 installation in the toolchain path
list if Gradle does not discover it.

## Caller-selected local Maven repository

The coordinate consumed by Skysoft is
`io.github.akinsoft.softconfig:modern-26.3:4.8.5`. Build this genuine source
target first, then use a new local repository directory selected by the caller:

```sh
./gradlew :modern:modern-26.3:publishMavenPublicationToCentralStagingRepository \
  --no-daemon --max-workers=4 \
  -Psoftconfig.activeMinecraftVersions=26.3 \
  -Psoftconfig.releaseVersion=4.8.5 \
  -Dsoftconfig.sourceRevision=f83ff6aaefb09860775b9b05e5f2d1bdb930af50 \
  -Porg.gradle.java.installations.paths="$JDK8_PORT_HOME,$JDK25_PORT_HOME" \
  -Psoftconfig.stagingDirectory="$SOFTCONFIG_PORT_REPO"
```

Set `SOFTCONFIG_PORT_REPO` to an absolute local filesystem directory. The
`centralStaging` name is the existing Gradle file-repository name; this command
writes that selected directory. It does not upload to Maven Central. The
publication includes the production JAR, sources, Javadocs and POM. Keep
published Maven coordinates immutable and follow the maintainer's release
process for any eventual public release.

Dependency verification stays enabled. The validated production JAR SHA-256 is
`2e1f24801068c472c91e24ebacc144cf544eb3bb9ba95893e8443bd088f2496e`.
Skysoft pins this exact genuine local dependency in its verification metadata.
If a reviewed source change produces another artifact, verify its provenance
and deliberately update only that selected dependency's accepted checksum.
Do not disable verification or accept an unexplained mismatch.

## Native input and validation

Saved keyboard integers retain their GLFW meanings; mouse integers retain the
logical left/right/middle order 0/1/2. The platform translates SDL scancodes and
native mouse values at capture, polling, display and event boundaries. For
example, saved A remains 65 while native A is SDL scancode 4. Native mouse
left/right/middle values 1/3/2 become logical 0/1/2. Saved F25 remains intact,
but SDL has no corresponding physical scancode to poll.

After dispatching the legacy keyboard event, the 26.3 screen checks the native
Escape value 41 rather than comparing it with the converted legacy value 256.
Earlier targets keep their original comparison. Consumed events and the
wrapper context's focus-clear-before-close behavior are preserved. The
existing config editor has a separate internal focus context, so its focused
search field closes on the first Escape; this port does not change that common
architecture.

All four production target remaps and authored checkstyle passed. The 24
test cases report zero failures and one original chroma skip. The 26.3 test set includes
two native input bridge cases. Common behavioral sources and original resources
remain unchanged. An actual screen-handler unit fixture could not compile
because the existing test classpath excludes Minecraft screen classes; no
duplicated-condition test or dependency workaround was added.

The consuming Skysoft JAR passed isolated cold startup, full required mixin
audit, actual config GUI, native X11/SDL Escape, mouse toggle/restore and a
visible fresh offline world using Minecraft's native screenshot function.
The exact consumer also passed the corrected combined-pack cold/post-world
audits, actual config GUI, native Escape and world checks with zero cumulative
helper failures. Its final checkpoint recorded 12,779 frames in each shared
Skyblocker and NoFrills GPU fixture; all 24 farm-key assertions passed. The
preceding combined run's unrelated unsupported operator-command failure remains
documented in its original evidence. OpenGL software rendering was used. Authenticated Hypixel
gameplay, Vulkan/hardware GPU, other consumers, keybind-editor process-restart
reload and native file-dialog behavior are not established by these checks.
