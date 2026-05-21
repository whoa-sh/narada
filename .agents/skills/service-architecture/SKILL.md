---
name: service-architecture
description: High-performance architectural standards for Kotlin Spring Boot services.
---
# Skill: Service Architecture Standards

Use these standards for all core domain, entity, and utility development.

## ID Generation (UUIDv7)

- **Implementation:** `sh.whoa.narada.util.UUIDv7`
- **Standard:** Monotonic, high-performance bitwise construction from `Long` primitives.
- **Optimization:** Zero heap allocations (no `ByteArray` or `ByteBuffer`) during ID generation.
- **Usage:** Set as the default ID generation strategy in `BaseEntity`.

## Persistence & Auditing

- **Base Entity:** `sh.whoa.narada.core.BaseEntity`
- **Auditing:** Use Hibernate-native `@CreationTimestamp` and `@UpdateTimestamp` exclusively.
- **Encapsulation:** Auditing fields MUST use `protected set` to satisfy both Kotlin `allOpen` and Hibernate reflection requirements while preventing external modification.
- **JPA Performance:** Implements `Persistable<UUID>` with a `@Transient` `isNewEntity` flag. This bypasses JPA's standard `merge()` "SELECT-before-INSERT" behavior for fresh entities.

## Kotlin Standards

- **Version:** Kotlin 2.3.21.
- **Principles:**
  - Prefer **Composition** over Inheritance.
  - Use **Sealed Classes** to represent exhaustive domain states.
  - Use **Result** or `runCatching` for idiomatic error handling in business logic.
  - Apply **Implicit Type Saftey** by avoiding unnecessary casts.
