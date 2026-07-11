# Contributing

SoftConfig accepts focused fixes, compatibility work, tests, documentation, and carefully scoped features.

## Development setup

- Install JDK 25.
- Use the checked-in Gradle wrapper rather than a system Gradle installation.
- Run commands from the repository root.

On Linux or macOS:

```bash
./gradlew check
./gradlew test assemble
```

On Windows:

```powershell
.\gradlew.bat check
.\gradlew.bat test assemble
```

Run the relevant active platform build while working on platform-specific behavior, for example `./gradlew :modern:modern-26.2:assemble`.

## Pull requests

- Explain the problem and why the change is needed.
- Keep changes small and avoid unrelated refactors.
- Add or update tests for behavioral changes.
- Preserve SoftConfig 4.x public binary compatibility.
- Do not commit generated files, build output, credentials, or local environment files.
- Ensure `check` and the relevant active-version builds pass before requesting review.

Breaking public API changes belong in a future major version and require migration documentation.

## Releases

Releases are created by maintainers from exact version tags after all required checks pass. Published Maven coordinates are immutable and must never be overwritten.
