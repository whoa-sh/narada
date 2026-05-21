# Implementation Plan: Enterprise Refinements Phase

## Objective
Apply "Enterprise Grade" refinements based on Gemini Code Assist feedback to both `chitragupta` and `narada` repositories. This will improve type safety in tests, unify auditing logic, simplify utility code, and establish a professional database baseline.

## Scope
- `chitragupta` and `narada` repositories.

## Implementation Steps (To be applied to BOTH repos)

### 1. Fix TestContainer Generic Type (High)
- **Target:** `src/test/kotlin/sh/whoa/.../...ApplicationTests.kt`
- **Change:** Change `val postgres = PostgreSQLContainer<Nothing>("postgres:17-alpine")` to `val postgres = PostgreSQLContainer("postgres:17-alpine")`.
- **Reason:** `Nothing` cannot satisfy the recursive generic bound of `PostgreSQLContainer`, leading to potential compilation or type resolution issues.

### 2. Unify Auditing Logic & Encapsulate (Medium)
- **Target:** `src/main/kotlin/sh/whoa/.../core/BaseEntity.kt`
- **Change:**
  - Remove `@CreatedDate` (Spring Data annotation).
  - Use `@org.hibernate.annotations.CreationTimestamp` for `createdAt`.
  - Use `@org.hibernate.annotations.UpdateTimestamp` for `updatedAt`.
  - Add `@jakarta.persistence.Column(updatable = false, nullable = false)` to `createdAt`.
  - Add `@jakarta.persistence.Column(nullable = false)` to `updatedAt`.
  - Change `val createdAt` to `var createdAt: Instant? = null` and add `private set`.
  - Add `private set` to `updatedAt`.
- **Reason:** Hibernate annotations are more performant. Adding `private set` ensures data integrity by preventing accidental manual modification of system-managed fields.

### 3. High-Performance UUIDv7 (Medium)
- **Target:** `src/main/kotlin/sh/whoa/.../util/UUIDv7.kt`
- **Change:** 
  - Refactor `randomUUID()` to construct MSB and LSB directly from `Long` values using bitwise operations.
  - Remove `randomBytesMonotonic()` and `toLongBE()` to eliminate `ByteArray` and `ByteBuffer` allocations.
- **Reason:** Direct bitwise construction on primitives is significantly faster and allocation-free, which is critical for a high-throughput ID generator.

## Verification
- Run `.\gradlew.bat --no-daemon ktlintFormat` and `.\gradlew.bat --no-daemon ktlintCheck`.
- Run `.\gradlew.bat --no-daemon clean build` to verify tests and build pass.
- Ensure `ApplicationTests` still passes (verifying TestContainers fix).

## Commit and Push
- Commit message: `refactor: apply enterprise refinements for auditing, tests, and utility code`
- Push to respective branches (`master` for `narada`, `feature/enterprise-followups` for `chitragupta`).
