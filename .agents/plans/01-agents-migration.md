# Implementation Plan: .agents Documentation Migration (Refined)

## Objective
Migrate the documentation of `chitragupta` and `narada` to a modernized `.agents/` structure, strictly following the pattern and philosophy of the "TwoFac" project. This involves progressive disclosure, skill-based guidance, and a clear architectural map.

## Scope
- `chitragupta` and `narada` repositories.

## Target Structure (Per Project)
- `AGENTS.md` (Root): High-level map, router, and platform/security guardrails.
- `.agents/`
    - `plans/`: Directory for implementation plans (moving relevant ones here).
    - `skills/`
        - `gradle-build/SKILL.md`: Metadata-driven Gradle commands for Spring Boot.
        - `service-architecture/SKILL.md`: Architectural standards (UUIDv7, Auditing, JPA).
        - `deployment/SKILL.md`: Docker, Compose, and GHA standards.
    - `tracker.md`: Project status and decision log (migrated from `docs/implementation-tracker.md`).
    - `tasks.yml`: Progress tracking registry.

## Implementation Steps (To be applied to BOTH repos)

### 1. Initialize .agents Structure
- Create `.agents/` with subdirectories: `skills/gradle-build/`, `skills/service-architecture/`, `skills/deployment/`, `plans/`, `reports/`, `agents/`.
- Create an empty `.agents/tasks.yml`.

### 2. Migrate Documentation
- Move `docs/implementation-tracker.md` to `.agents/tracker.md`.
- Move the recently approved plans to `.agents/plans/` (using a numbered naming convention if appropriate).

### 3. Draft High-Fidelity SKILL.md Files (Following TwoFac Style)

#### **`.agents/skills/gradle-build/SKILL.md`**
- Use frontmatter `name` and `description`.
- Use tables for command summaries.
- Add specific guidance for Spring Boot (no-daemon, devtools).

#### **`.agents/skills/service-architecture/SKILL.md`**
- Document UUIDv7, `BaseEntity` with `protected set`, and Hibernate auditing.
- Use the "Standards" and "Usage" pattern from the example.

#### **`.agents/skills/deployment/SKILL.md`**
- Document Docker healthchecks, security profiles, and GHA Node 24 standards.

### 4. Create Root AGENTS.md
- Include "Core Architecture", "Module Map", "Agent Skills" (links), "Agent Navigation", and "Security Guardrails" (gitignored files).

### 5. Cleanup
- Remove the legacy `docs/` folder.

## Verification
- Verify that I can successfully read and navigate the new structure.
- Run `ktlintCheck` to ensure no formatting regressions.

## Commit and Push
- Commit message: `docs: migrate documentation to modernized .agents structure per TwoFac pattern`
- Push to respective branches (`master` for `narada`, `feature/enterprise-followups` for `chitragupta`).
