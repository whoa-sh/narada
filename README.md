# Narada

`narada` is a Kotlin + Spring Boot service using JPA, Flyway migrations, Actuator, and PostgreSQL.

## Stack

- Kotlin 2.2
- Java 21
- Spring Boot 4.0
- Spring Data JPA
- Flyway
- PostgreSQL
- Gradle wrapper
- Docker / Docker Compose

## Architecture Notes

- **Primary Keys:** Uses `UUIDv7` for lexicographically sortable, time-based primary keys.
- **Auditing:** Uses UTC `java.time.Instant` for `createdAt` and `updatedAt` to ensure safe timezone handling in distributed systems.
- **Performance:** `BaseEntity` implements Spring Data's `Persistable<UUID>` to bypass unnecessary `SELECT` queries on `save()` inserts.

## Repository Layout

```text
.
|- src/main/kotlin/sh/whoa/narada/
|- src/main/resources/
|  `- db/migration/
|- src/test/kotlin/sh/whoa/narada/
|- Dockerfile
|- docker-compose.yml
|- docker-compose.dev.yml
|- .env.example
|- Makefile
|- scripts/dev.ps1
`- .github/workflows/
```

## Fast Start (Docker Only)

1. Clone the repo and `cd` into it.
2. Run: `docker compose up --build`
3. Open:
   - App: `http://localhost:7621`
   - Health: `http://localhost:7621/actuator/health`

This path is ready out-of-the-box with local-safe defaults.
If you want overrides, create `.env` from `.env.example`.

Stop:

- `docker compose down`
- `docker compose down -v` (also removes database volume)

## IDE Development (Dependencies Only)

Start only PostgreSQL:

```bash
docker compose -f docker-compose.dev.yml up -d
```

Stop dependencies:

```bash
docker compose -f docker-compose.dev.yml down -v
```

Run app from IDE with:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}`
- `SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}`
- `SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}`

Default local values in `.env.example`:

- `APP_PORT=7621`
- `POSTGRES_PORT=7622`
- Port series convention for service `#2`: `7621` (app), `7622` (db).
  For the next services, continue the series (`7621/7622`, `7631/7632`, ...).

## Migrations (Flyway)

- Migrations live in `src/main/resources/db/migration`.
- Naming convention: `V<version>__<description>.sql` (example: `V2__add_accounts_table.sql`).
- On startup, Flyway runs before JPA initialization.
- Current baseline: `V1__init.sql`.
- Tests keep `contextLoads` and run with Flyway enabled against H2 in PostgreSQL mode.
- Applied migrations are immutable except for repository-wide license header updates. Do not edit an already-applied `V*__*.sql` for schema or data changes; create a new migration file instead.

If you modify an already-applied migration locally, reset the local Postgres volume before restart:

- `docker compose down -v`
- `docker compose up --build`

This volume reset guidance is for local development recovery only.

## CI/CD Policy

### CI (`.github/workflows/ci.yml`)

Runs on:

- Pull requests to `master`
- Pushes to `master`

Jobs:

- `Lint (ktlint + license headers)` -> `ktlintCheck spotlessCheck`
- `Build & Test (Gradle)` -> `clean test build` (runs after lint)
- Uploads `gradle-test-report` artifact on every run

### Docker Smoke (`.github/workflows/docker-smoke.yml`)

Runs on:

- Successful completion of `CI` workflow

Flow:

1. Build app image with GHA cache
2. Compose start and health wait
3. Upload `docker-smoke-summary` artifact (`compose ps`, health payload, container logs)
4. Teardown

PR behavior:

- Checks only
- No image publishing

### Container Release (`.github/workflows/container.yml`)

Runs on:

- Push to `master`
- Manual `workflow_dispatch`

Flow:

1. Build local scan image
2. Generate SBOM (CycloneDX JSON)
3. Vulnerability scan gate (fail at `high` and above)
4. Publish to GHCR only after scan passes
5. Emit build provenance attestation

## Container Hardening

- Multi-stage Docker build
- Non-root runtime user
- Read-only root filesystem for app container
- `tmpfs` mount for `/tmp`
- `cap_drop: [ALL]`
- `no-new-privileges:true`
- Explicit CPU/memory/pid limits and memory reservations for app and PostgreSQL
- Healthchecks for app and database

## Environment Variables

Defined in `.env.example`:

- `APP_PORT`
- `SPRING_PROFILES_ACTIVE`
- `JAVA_OPTS`
- `APP_TMPFS_SIZE`
- `APP_PIDS_LIMIT`
- `APP_MEM_LIMIT`
- `APP_MEM_RESERVATION`
- `APP_CPUS_LIMIT`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_PORT`
- `POSTGRES_PIDS_LIMIT`
- `POSTGRES_MEM_LIMIT`
- `POSTGRES_MEM_RESERVATION`
- `POSTGRES_CPUS_LIMIT`

## Build and Test Commands

Gradle (Unix-like):

```bash
./gradlew --no-daemon test
./gradlew --no-daemon build
./gradlew --no-daemon ktlintCheck
./gradlew --no-daemon spotlessCheck
./gradlew --no-daemon spotlessApply
./gradlew --no-daemon clean
```

Gradle (Windows PowerShell):

```powershell
.\gradlew.bat --no-daemon test
.\gradlew.bat --no-daemon build
.\gradlew.bat --no-daemon ktlintCheck
.\gradlew.bat --no-daemon spotlessCheck
.\gradlew.bat --no-daemon spotlessApply
.\gradlew.bat --no-daemon clean
```

License headers are enforced by Spotless. Run `spotlessApply` before committing when adding Kotlin, Gradle Kotlin DSL, YAML, PowerShell, properties, or Flyway SQL migration files.

Helper scripts:

```bash
make test
make lint
make compose-up
make compose-down-v
```

```powershell
.\scripts\dev.ps1 test
.\scripts\dev.ps1 lint
.\scripts\dev.ps1 compose-up
.\scripts\dev.ps1 compose-down-v
```

## Quick Links

- [Dockerfile](./Dockerfile)
- [Compose (full app)](./docker-compose.yml)
- [Compose (dependencies only)](./docker-compose.dev.yml)
- [Environment template](./.env.example)
- [Make helper targets](./Makefile)
- [PowerShell helper script](./scripts/dev.ps1)
- [CI workflow](./.github/workflows/ci.yml)
- [Docker smoke workflow](./.github/workflows/docker-smoke.yml)
- [Container release workflow](./.github/workflows/container.yml)

On Windows, use `.\gradlew.bat ...`.

## License

Narada source code is available under a choice of AGPL-3.0-only, SSPL-1.0, or Elastic-2.0. The AGPL option is open source under the OSI definition; SSPL and Elastic License 2.0 are source-available options with additional restrictions.

See [LICENSE.txt](./LICENSE.txt), [NOTICE](./NOTICE), and the full texts in [licenses](./licenses).

Source headers are applied and checked with Spotless via `spotlessApply` and `spotlessCheck`.

## Production Notes

Before production:

1. Replace all local default credentials.
2. Restrict database port exposure.
3. Tune JVM and container limits for workload.
4. Pin base image digests and patch regularly.
5. Keep branch protection requiring CI checks on `master`.

## Branching Policy

- Do all new work on a feature branch, not `master`.
- Open pull requests into `master` after checks pass.
