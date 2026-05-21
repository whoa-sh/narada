# AGENTS.md

This repository is a Kotlin Spring Boot enterprise service (`narada`).

## Core Architecture

- **Language:** Kotlin 2.3.21, JDK 21, Spring Boot 4.0.6.
- **Persistence:** PostgreSQL with pure Hibernate auditing and UUIDv7.
- **Reliability:** Built-in Docker smoke tests and Node 24 CI/CD.

## Module Map

- `sh.whoa.narada.core`: Shared base entities and JPA optimizations.
- `sh.whoa.narada.util`: High-performance internal utilities (UUIDv7, etc).
- `src/main/resources/db/migration`: Flyway database migrations (minimal baseline standard).

## Agent Skills

Use specialized skills for detailed command and routing guidance:

- `.agents/skills/gradle-build/SKILL.md`
- `.agents/skills/service-architecture/SKILL.md`
- `.agents/skills/deployment/SKILL.md`

## Agent Navigation

To keep context lean, technical logs and status are delegated to:

- `.agents/tracker.md`: Implementation status and decision log.
- `.agents/tasks.yml`: Progress tracking registry.
- `README.md`: Human-centric overview and setup.

## Security Guardrails

The following sensitive files are gitignored and MUST NOT be committed:

- `.env`
- `build/`
- `.gradle/`
