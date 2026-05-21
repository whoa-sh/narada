# Repository Guidelines

## Project Structure & Module Organization

This is a Kotlin Spring Boot service named `narada`.

- `src/main/kotlin/sh/whoa/narada/` contains application source code. The current entry point is `NaradaApplication.kt`.
- `src/main/resources/` contains runtime configuration, including `application.properties`.
- `src/test/kotlin/sh/whoa/narada/` contains JUnit 5/Spring Boot tests.
- `build.gradle.kts`, `settings.gradle.kts`, and `gradle/wrapper/` define the Gradle build. Use the checked-in wrapper instead of a system Gradle install.

Keep package paths aligned with `sh.whoa.narada`. Add new code under feature-oriented packages as the service grows, such as `controller`, `service`, `repository`, or domain-specific names.

## Build, Test, and Development Commands

Run commands from the repository root.

- `.\gradlew.bat bootRun` starts the Spring Boot app locally.
- `.\gradlew.bat test` runs the JUnit 5 test suite.
- `.\gradlew.bat build` compiles, tests, and packages the project.
- `.\gradlew.bat clean` removes generated build output.

On Unix-like shells, use `./gradlew` instead of `.\gradlew.bat`.

## Coding Style & Naming Conventions

Use Kotlin idioms and keep code concise. Existing files use tabs for indentation; match that style unless the project adopts a formatter. Class names use `PascalCase`, functions and properties use `camelCase`, and test classes should end with `Tests`.

The build enables strict nullability handling with `-Xjsr305=strict`. Prefer explicit nullability, constructor injection for Spring components, immutable `val` properties, and small functions with clear names. JPA entities are opened through the Kotlin JPA plugin, so avoid manual `open` modifiers unless a framework requires them.

## Testing Guidelines

Tests use Spring Boot test support, Kotlin test, and JUnit 5 via `useJUnitPlatform()`. Put tests under `src/test/kotlin` with package names matching production code.

Name integration-style Spring tests `*Tests` and focused unit tests after the class or behavior under test, for example `LedgerServiceTests`. Add tests for new controllers, repositories, and service logic.

## Commit & Pull Request Guidelines

The current Git history only contains `Initial Commit`, so there is no established convention yet. Use short, imperative commit subjects such as `Add account repository` or `Validate ledger entries`.

Pull requests should include a concise description, the reason for the change, test results, and any configuration or database assumptions. Link related issues when available. For API changes, include example requests and responses.

## Security & Configuration Tips

Do not commit secrets, database passwords, or local environment files. Keep environment-specific settings outside source control and document required properties in `application.properties` or project docs. PostgreSQL is a runtime dependency, so note any local database setup required by new features.
