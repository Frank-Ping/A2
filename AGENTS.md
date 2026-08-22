Work directly in the currently opened Wear OS Android Studio project.

Do not open, read, quote, summarize, or reproduce any assignment document. Work only from my prompts and the existing project files.

Preserve the namespace and applicationId `com.example.a2`. Use Kotlin and Jetpack Compose for Wear OS. `MainActivity` must remain the only launcher activity.

## Staged production-code structure

- Part A: use `MainActivity.kt` and `SensorActivity.kt` only.
- Part B: implement the first raw sensor only inside `SensorActivity.kt`.
- Part C: refactor the raw-sensor logic into a small reusable sensor data layer.
- Part D: add a `HealthServicesRepository`.
- Part E: add a Tile service, complication service, and shared latest-reading storage.

Test files under `src/test` and `src/androidTest` do not count as production architecture files and may be added when needed.

Do not introduce a phone module, Navigation Compose, dependency injection framework, database, background service, animation, or unrelated feature.

## Before editing

1. Inspect the current source files, version catalog, app build file, manifest, resources, and tests.
2. Give me a short implementation plan.
3. Then edit the project directly.

## After editing

1. List every changed or created file.
2. Explain the lifecycle and data flow in plain language.
3. Run the relevant Gradle build, unit-test, lint, and instrumentation-compilation tasks.
4. Treat `NO-SOURCE` as missing tests, not as passed tests.
5. Clearly separate compilation, automated tests, and Wear OS emulator verification.
6. Run `git status --short` when the project is a Git repository.

## Git tracking rules

- Track source files under `app/src/main`, `app/src/test`, and `app/src/androidTest`.
- Track Gradle build files, the version catalog, Gradle wrapper files, `AndroidManifest.xml`, source resources, `AGENTS.md`, `.gitignore`, `README.md`, and intentional documentation.
- Add clearly generated, temporary, IDE-specific, local, or secret files to `.gitignore` when they appear.
- Never ignore Kotlin, XML, Gradle, TOML, image-resource, test, or intentional documentation files.
- Do not delete, untrack, commit, or push anything unless the user explicitly requests it.
- After changing `.gitignore`, show the added rules and verify them with `git status --short` and `git check-ignore -v` when Git is available.
