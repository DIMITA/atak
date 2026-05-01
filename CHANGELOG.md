# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2025-05-08

### Added
- Parent multi-module Maven configuration for unified build lifecycle
- `.gitignore` tailored for Java/Maven artifacts
- GitHub Actions CI workflow for automated build and test on every push
- Maven Central publish workflow for automated artifact deployment
- Comprehensive project README with usage guide and architecture overview
- Maven Central publishing guide with GPG signing and Sonatype setup instructions

### Changed
- Project promoted to stable — all modules are production-ready

---

## [0.6.0] - 2025-03-30

### Added
- `atak-spring` module: Spring Boot auto-configuration support
- `AtakAutoConfiguration` — registers all ATAK beans automatically
- `AtakProperties` — externalized configuration via `application.properties`
- `AtakErrorResponse` — standardized error response structure
- `AtakGlobalExceptionHandler` — centralized exception handling via `@ControllerAdvice`
- Auto-configuration entry registered in `spring.factories` / `AutoConfiguration.imports`

---

## [0.5.0] - 2025-02-26

### Added
- `atak-sample` module: reference Spring Boot application demonstrating ATAK usage
- Sample `Client` entity with `@AtakAudit` and `@AtakSecurity` annotations
- Sample `Product` entity with `@AtakFilter` and `@AtakTenant` annotations
- `ClientService` hook implementing custom business logic overrides
- `TenantRequestFilter` wiring `TenantContext` from incoming HTTP headers
- `application.properties` with ATAK-specific configuration entries

---

## [0.4.0] - 2025-01-26

### Added
- `atak-maven-plugin` module: Maven plugin for project lifecycle management
- `ScaffoldMojo` — scaffolds a new ATAK-enabled module from scratch (`atak:scaffold`)
- `SyncMojo` — re-synchronizes generated sources with entity model changes (`atak:sync`)
- `EjectMojo` — ejects generated sources into the project for manual customization (`atak:eject`)

---

## [0.3.0] - 2025-01-08

### Added
- `AtakProcessor` — main `javax.annotation.processing.Processor` entry point
- `EntityModel` — internal representation of an annotated entity
- `EntityModelFactory` — builds `EntityModel` instances from `TypeElement`
- `TenantContext` — thread-local utility for managing current tenant identity
- Registration of `AtakProcessor` as a Java annotation processor service (`META-INF/services`)
- Unit tests covering end-to-end code generation from annotated entity classes

---

## [0.2.0] - 2024-12-17

### Added
- `AuditGenerator` — generates audit fields and listeners
- `ControllerGenerator` — generates REST controller with CRUD endpoints
- `DtoGenerator` — generates request/response DTO classes
- `FilterGenerator` — generates JPA specification filter for dynamic queries
- `MapperGenerator` — generates MapStruct mapper interface
- `RepositoryGenerator` — generates Spring Data JPA repository
- `ServiceGenerator` — generates service layer with transactional methods

---

## [0.1.0] - 2024-11-25

### Added
- Initial `atak-core` module setup
- `@AtakEntity` — marks a JPA entity for ATAK processing
- `@AtakAudit` — enables audit field generation (`createdAt`, `updatedAt`, `createdBy`)
- `@AtakDto` — controls DTO generation options per entity
- `@AtakField` — fine-grained field-level generation control
- `@AtakFilter` — enables dynamic JPA Specification filter generation
- `@AtakHook` — marks a service class as a hook for generated service methods
- `@AtakIgnore` — excludes a field from all generation
- `@AtakMapper` — customizes MapStruct mapper generation
- `@AtakSecurity` — attaches role-based access rules to generated endpoints
- `@AtakTenant` — marks the field carrying the tenant identifier
- `AtakAuditable` — interface contract for auditable entities

[1.0.0]: https://github.com/DIMITA/atak/compare/v0.6.0...v1.0.0
[0.6.0]: https://github.com/DIMITA/atak/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/DIMITA/atak/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/DIMITA/atak/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/DIMITA/atak/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/DIMITA/atak/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/DIMITA/atak/releases/tag/v0.1.0
