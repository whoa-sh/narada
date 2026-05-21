# Implementation Plan: Repository Skeleton Synchronization

## Objective
Synchronize `chitragupta` and `narada` to ensure they are identical architectural clones, differing only in project-specific names. This resolves drifts in Gradle versions, wrapper scripts, and Docker configurations.

## Scope
- `chitragupta` and `narada` repositories.

## Identified Drifts & Improvement Opportunities

### 1. Gradle Version Drift
- **Status:** `chitragupta` is on Gradle 9.5.1, while `narada` is on 9.4.1.
- **Action:** Upgrade `narada` to Gradle 9.5.1 for parity.

### 2. Gradle Wrapper Script Drift
- **Status:** The `gradlew.bat` scripts are significantly different in structure and content.
- **Action:** Synchronize `gradlew.bat` across both projects using the version from `chitragupta` (which appears newer/more standard for 9.5.1).

### 3. Docker Compose Redundancy
- **Status:** Both projects have a `docker-compose.yml` (production/smoke) and `docker-compose.dev.yml` (development). 
- **Improvement:** In `docker-compose.yml`, the `postgres` healthcheck command uses `${POSTGRES_USER:-chitragupta}`. We should ensure these are perfectly mirrored with their respective project names.

### 4. GitIgnore Synchronization
- **Action:** Perform a final check/sync of `.gitignore` to ensure both projects ignore the same build/IDE/local artifacts.

## Implementation Steps

### Step 1: Upgrade Narada Gradle Version
- Update `narada/gradle/wrapper/gradle-wrapper.properties` to use 9.5.1.
- Add `networkTimeout=10000`, `retries=0`, `retryBackOffMs=500` to `narada` for parity with `chitragupta`.

### Step 2: Synchronize Gradle Wrapper Scripts
- Copy `chitragupta/gradlew` to `narada/gradlew`.
- Copy `chitragupta/gradlew.bat` to `narada/gradlew.bat`.

### Step 3: Mirror Docker Configurations
- Ensure `narada/docker-compose.yml` healthchecks use `narada` defaults consistently.

### Step 4: Parity Check for .gitignore and .dockerignore
- Audit and sync the content of these files across both projects.

## Verification
- Run `.\gradlew.bat --version` in both repos to confirm 9.5.1.
- Run `.\gradlew.bat clean` in both repos to ensure wrapper scripts work correctly.
- Verify `docker compose config` for both projects.

## Commit and Push
- Commit message: `refactor: synchronize repository skeletons and upgrade gradle`
- Push to respective branches (`master` for `narada`, `feature/enterprise-followups` for `chitragupta`).
