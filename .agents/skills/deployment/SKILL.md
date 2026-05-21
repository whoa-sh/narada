---
name: deployment
description: Standards for Docker, Compose, and GitHub Actions CI/CD workflows.
---
# Skill: Deployment & CI/CD

Use these standards for all infrastructure and pipeline modifications.

## Docker Standards

- **Healthchecks:** Defined in both `Dockerfile` and `docker-compose.yml`. Use `wget -qO- http://127.0.0.1:8080/actuator/health` and verify `"status":"UP"`.
- **Security:**
  - `read_only: true` (Root filesystem is immutable).
  - `tmpfs: [/tmp]` (Writable scratch space).
  - `cap_drop: [ALL]` (No Linux capabilities).
  - `no-new-privileges: true`.

## GitHub Actions Standards

- **Runtime:** Every JavaScript-based action MUST run on the **Node.js 24** runtime.
- **Action Versions (2026 Standards):**

| Category | Action | Version |
|----------|--------|---------|
| Core | `actions/checkout` | `@v6` |
| Artifacts | `actions/upload-artifact` | `@v7` |
| Java | `actions/setup-java` | `@v5` |
| Gradle | `gradle/actions/setup-gradle` | `@v6` |
| Docker | `docker/login-action` | `@v4` |
| Security | `actions/attest` | `@v1` |
| Scanning | `anchore/scan-action` | `@v7` |
| Quality | `github/codeql-action/upload-sarif` | `@v4` |
