---
name: gradle-build
description: Use Gradle commands for Spring Boot build, test, and linting operations on Windows.
---
# Skill: Gradle Build Commands

Use these commands for all build, test, and linting operations.

## Core Commands

| Action | Command |
|--------|---------|
| **Linting** | `./gradlew.bat --no-daemon ktlintCheck` |
| **Formatting** | `./gradlew.bat --no-daemon ktlintFormat` |
| **Testing** | `./gradlew.bat --no-daemon clean test` |
| **Full Build** | `./gradlew.bat --no-daemon clean build` |
| **Run Dev** | `./gradlew.bat --no-daemon bootRun` |
| **Package** | `./gradlew.bat --no-daemon bootJar` |
| **Lock Deps** | `./gradlew.bat --no-daemon --write-locks` |

## Guidance

- **Always** use the `--no-daemon` flag in CI-like or resource-constrained environments.
- Always run `ktlintCheck` before proposing a commit.
- Prefer `./gradlew.bat` as the environment is Windows PowerShell.
- Use `clean` before `build` or `test` to ensure a consistent state.
