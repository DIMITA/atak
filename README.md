# ATAK

[![CI](https://github.com/dimita/atak/actions/workflows/ci.yml/badge.svg)](https://github.com/dimita/atak/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.atak/atak-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.atak%22)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://openjdk.org/projects/jdk/17/)

> **From entity to production-ready API in seconds.**  
> Kill your Spring boilerplate — one annotation, full CRUD, zero runtime magic.

```java
@AtakEntity
@Entity
public class Client {
    @Id @GeneratedValue private Long id;
    private String name;
    private String email;
}
```

`mvn compile` → RequestDto · ResponseDto · Mapper · Repository · Service · Controller · Swagger UI — all generated at **compile time**.

---

## Table of Contents

- [Why ATAK](#why-atak)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Annotation Reference](#annotation-reference)
  - [@AtakEntity](#atakentity)
  - [@AtakDto](#atakdto)
  - [@AtakField](#atakfield)
  - [@AtakIgnore](#atakignore)
  - [@AtakMapper](#atakmapper)
  - [@AtakFilter](#atakfilter)
  - [@AtakAudit](#atakaudit)
  - [@AtakSecurity](#ataksecurity)
  - [@AtakTenant](#ataktenant)
  - [@AtakHook](#atakhook)
- [Generated Artifacts](#generated-artifacts)
- [Lifecycle Hooks](#lifecycle-hooks)
- [Security Hooks](#security-hooks)
- [Dynamic Filters](#dynamic-filters)
- [Audit Trail](#audit-trail)
- [Multi-Tenancy](#multi-tenancy)
- [OpenAPI / Swagger](#openapi--swagger)
- [Maven Plugin](#maven-plugin)
  - [atak:scaffold](#atakscaffold)
  - [atak:eject](#atakeject)
  - [atak:sync](#ataksync)
- [Configuration Reference](#configuration-reference)
- [Extending Generated Code](#extending-generated-code)
- [Full Example](#full-example)
- [Compatibility](#compatibility)
- [Roadmap](#roadmap)

---

## Why ATAK

Every entity in a standard Spring Boot project forces you to write the same six files:

| File | Lines |
|---|---|
| Entity | ~30 |
| RequestDto / ResponseDto | ~60 |
| Mapper | ~40 |
| Repository | ~10 |
| Service | ~80 |
| Controller | ~80 |
| **Total** | **~300** |

90 % of that code is identical across entities. ATAK generates it all at compile time from a single annotation, leaving you to write only the business logic that actually matters.

**Key properties:**
- ✅ 100 % compile-time — no reflection, no proxies, no runtime bytecode manipulation
- ✅ Fully readable generated code — debug it, step through it, read it
- ✅ Progressive — add `@AtakFilter`, `@AtakAudit`, `@AtakSecurity`, `@AtakTenant` only when you need them
- ✅ Eject at any time — `mvn atak:eject` copies generated files to your source tree

---

## Quick Start

### 1. Add dependencies

```xml
<!-- Annotation processor (compile-time only) -->
<dependency>
    <groupId>io.atak</groupId>
    <artifactId>atak-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>

<!-- Spring Boot integration (auto-configuration, exception handler) -->
<dependency>
    <groupId>io.atak</groupId>
    <artifactId>atak-spring</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 2. Wire the processor

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.atak</groupId>
                <artifactId>atak-core</artifactId>
                <version>0.1.0-SNAPSHOT</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### 3. Annotate your entity

```java
@AtakEntity
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @AtakField(required = true, description = "Full name", example = "Alice Martin")
    private String name;

    @AtakField(required = true, description = "Email address", example = "alice@example.com")
    private String email;

    // standard getters/setters
}
```

### 4. Run

```bash
mvn compile
mvn spring-boot:run
```

Your API is live:

```
POST   /clients        → create
GET    /clients/{id}   → find by id
GET    /clients        → list (pageable)
PUT    /clients/{id}   → update
DELETE /clients/{id}   → delete

GET    /swagger-ui.html → Swagger UI (full docs, zero manual annotations)
```

---

## Installation

### Maven

Replace `LATEST_VERSION` with the version shown in the Maven Central badge above.

```xml
<dependencies>
    <!-- ATAK annotation processor (compile-time only) -->
    <dependency>
        <groupId>io.atak</groupId>
        <artifactId>atak-core</artifactId>
        <version>LATEST_VERSION</version>
    </dependency>

    <!-- ATAK Spring Boot auto-configuration + exception handler -->
    <dependency>
        <groupId>io.atak</groupId>
        <artifactId>atak-spring</artifactId>
        <version>LATEST_VERSION</version>
    </dependency>

    <!-- Required Spring Boot starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- OpenAPI / Swagger UI (optional but recommended) -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.5.0</version>
    </dependency>
</dependencies>
```

Wire the annotation processor (required so Maven calls ATAK at compile time):

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.atak</groupId>
                        <artifactId>atak-core</artifactId>
                        <version>LATEST_VERSION</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Maven plugin (optional)

```xml
<plugin>
    <groupId>io.atak</groupId>
    <artifactId>atak-maven-plugin</artifactId>
    <version>LATEST_VERSION</version>
</plugin>
```

To use the short `atak:` prefix, add to `~/.m2/settings.xml`:

```xml
<pluginGroups>
    <pluginGroup>io.atak</pluginGroup>
</pluginGroups>
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    annotationProcessor("io.atak:atak-core:LATEST_VERSION")
    implementation("io.atak:atak-core:LATEST_VERSION")
    implementation("io.atak:atak-spring:LATEST_VERSION")
}
```

### Build from source

```bash
git clone https://github.com/dimita/atak.git
cd atak
mvn install -DskipTests -pl '!atak-sample'
```

---

## Annotation Reference

### @AtakEntity

**Target:** class  
**Triggers the full generation pipeline.**

```java
@AtakEntity(
    path              = "/api/clients",   // REST base path. Default: /<lowercase-entity-name>s
    generateController = true,            // Set false to skip controller generation
    openApi            = true             // Set false to skip @Operation/@Schema injection
)
```

| Attribute | Type | Default | Description |
|---|---|---|---|
| `path` | `String` | `"/<name>s"` | Base path of the generated REST controller |
| `generateController` | `boolean` | `true` | Whether to generate the REST controller |
| `openApi` | `boolean` | `true` | Whether to inject OpenAPI annotations |

---

### @AtakDto

**Target:** class or field  
**Fine-tunes DTO generation.**

```java
// On a field: control per-field inclusion
@AtakDto(inRequest = false)           // exclude from RequestDto (e.g. computed fields)
@AtakDto(inResponse = false)          // exclude from ResponseDto (e.g. write-only fields)
@AtakDto(description = "Client ref")  // OpenAPI description

// On the class: override defaults for all fields
@AtakDto
@AtakEntity
public class Client { ... }
```

| Attribute | Type | Default | Description |
|---|---|---|---|
| `description` | `String` | `""` | OpenAPI `@Schema(description)` value |
| `inRequest` | `boolean` | `true` | Include this field in `<Entity>RequestDto` |
| `inResponse` | `boolean` | `true` | Include this field in `<Entity>ResponseDto` |

---

### @AtakField

**Target:** field  
**Maximum control over a single field.**

```java
@AtakField(
    required    = true,
    description = "Client email address",
    example     = "alice@example.com",
    jsonName    = "email_address"      // overrides JSON property name
)
private String email;
```

| Attribute | Type | Default | Description |
|---|---|---|---|
| `required` | `boolean` | `false` | Adds `@NotNull` / `@NotBlank` to RequestDto |
| `description` | `String` | `""` | OpenAPI field description |
| `example` | `String` | `""` | OpenAPI example value |
| `jsonName` | `String` | field name | JSON property name override (`@JsonProperty`) |

---

### @AtakIgnore

**Target:** field  
**Excludes a field from all generated artifacts** — DTO, mapper, filter, everything.

```java
@AtakIgnore
private Double internalCostPrice;  // never exposed, never mapped
```

---

### @AtakMapper

**Target:** class  
**Controls mapper generation.** Place alongside `@AtakEntity`.

```java
@AtakMapper(springComponent = false)   // generate static utility class instead of @Component
@AtakEntity
public class Client { ... }
```

| Attribute | Type | Default | Description |
|---|---|---|---|
| `springComponent` | `boolean` | `true` | Annotate mapper with `@Component` (Spring-managed) |

---

### @AtakFilter

**Target:** field  
**Marks a field as a queryable filter.** Triggers generation of `<Entity>Filter` DTO and `<Entity>Specification`.

```java
@AtakFilter                                    // AUTO: LIKE for String, EQUALS otherwise
@AtakFilter(AtakFilter.FilterType.LIKE)        // case-insensitive substring match
@AtakFilter(AtakFilter.FilterType.EQUALS)      // exact match
@AtakFilter(AtakFilter.FilterType.RANGE)       // generates minField + maxField params
@AtakFilter(AtakFilter.FilterType.IN)          // generates fieldIn: List<T> param
```

| Filter type | Generated params | SQL behaviour |
|---|---|---|
| `LIKE` | `name` | `LOWER(name) LIKE %value%` |
| `EQUALS` | `status` | `status = value` |
| `RANGE` | `minPrice`, `maxPrice` | `price >= min AND price <= max` |
| `IN` | `statusIn` | `status IN (v1, v2, ...)` |

**Example:**

```java
@AtakFilter                                    // LIKE on name
private String name;

@AtakFilter(AtakFilter.FilterType.RANGE)       // minPrice + maxPrice
private Double price;

@AtakFilter(AtakFilter.FilterType.IN)          // statusIn=ACTIVE,PENDING
private String status;
```

**Query usage:**

```
GET /products?name=laptop&minPrice=500&maxPrice=2000&statusIn=ACTIVE,NEW&page=0&size=20
```

See [Dynamic Filters](#dynamic-filters) for the full generated code.

---

### @AtakAudit

**Target:** class  
**Adds a full audit trail** (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`).

```java
@AtakAudit                          // track timestamps + user
@AtakAudit(trackUser = false)       // timestamps only, no @CreatedBy / @LastModifiedBy
```

| Attribute | Type | Default | Description |
|---|---|---|---|
| `trackUser` | `boolean` | `true` | Whether to include `createdBy` / `updatedBy` fields |

**Requirements:**

1. Your entity must extend the generated `<Entity>AuditBase`:
```java
@AtakAudit
@AtakEntity
@Entity
public class Order extends OrderAuditBase {  // generated → atak.generated.audit.OrderAuditBase
    ...
}
```

2. Add `@EnableJpaAuditing` to your application class:
```java
@SpringBootApplication
@EnableJpaAuditing
public class Application { ... }
```

3. Provide an `AuditorAware<String>` bean (for `createdBy` / `updatedBy`):
```java
@Bean
public AuditorAware<String> auditorProvider() {
    return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                         .map(Authentication::getName);
}
```

See [Audit Trail](#audit-trail) for the generated ResponseDto fields.

---

### @AtakSecurity

**Target:** class  
**Enables four security check hooks** that are called before each CRUD operation.

```java
@AtakSecurity
@AtakEntity
public class Order { ... }
```

Generates in `AbstractOrderService`:

```java
protected void atakCheckCreate(Authentication auth) {}
protected void atakCheckRead(Authentication auth) {}
protected void atakCheckUpdate(Authentication auth, Order entity) {}
protected void atakCheckDelete(Authentication auth, Order entity) {}
```

Override in your service to enforce access control. Throw `AccessDeniedException` to block.

Requires `spring-boot-starter-security` on the classpath.

See [Security Hooks](#security-hooks) for override examples.

---

### @AtakTenant

**Target:** field  
**Marks the tenant discriminator field.** Enables automatic data isolation per tenant.

```java
@AtakTenant
@Column(nullable = false)
private String tenantId;
```

The annotated field is **excluded from DTOs** and handled entirely by ATAK:
- **Create** → sets the field from `TenantContext.current()`
- **FindAll** → adds `WHERE tenantId = TenantContext.current()` predicate
- **FindById / Update / Delete** → verifies tenant ownership

See [Multi-Tenancy](#multi-tenancy) for setup.

---

### @AtakHook

**Target:** method  
**Documents a lifecycle hook override** in your service. Informational only — used by tooling.

```java
@Override
@AtakHook(AtakHook.Phase.BEFORE_CREATE)
protected void atakBeforeCreate(ClientRequestDto dto) {
    dto.setEmail(dto.getEmail().toLowerCase());
}
```

Available phases:

| Phase constant | Called when |
|---|---|
| `BEFORE_CREATE` | Before entity is mapped and saved |
| `AFTER_CREATE` | After entity is saved, before response is returned |
| `BEFORE_UPDATE` | Before entity is updated |
| `AFTER_UPDATE` | After entity is saved after update |
| `BEFORE_DELETE` | Before entity is deleted |
| `AFTER_DELETE` | After entity is deleted (receives the id) |

---

## Generated Artifacts

For a class `com.example.entity.Invoice` annotated with `@AtakEntity`:

```
atak.generated.dto
  └── InvoiceRequestDto        ← validated request payload
  └── InvoiceResponseDto       ← API response payload

atak.generated.mapper
  └── InvoiceMapper            ← toEntity(), toResponse(), updateEntity()

atak.generated.repository
  └── InvoiceRepository        ← extends JpaRepository<Invoice, Long>
                                  (+ JpaSpecificationExecutor when @AtakFilter / @AtakTenant)

atak.generated.service
  └── AbstractInvoiceService   ← full CRUD + hooks

com.example.entity.service     ← your package
  └── InvoiceService           ← stub: extend and override hooks

atak.generated.controller
  └── InvoiceController        ← @RestController with OpenAPI annotations

atak.generated.filter          ← only when @AtakFilter fields exist
  └── InvoiceFilter
  └── InvoiceSpecification

atak.generated.audit           ← only when @AtakAudit is present
  └── InvoiceAuditBase
```

All generated files include:
- `// Generated by ATAK — do not edit manually` header
- Full OpenAPI annotations when `openApi = true`
- Jakarta validation constraints from `@AtakField(required = true)`

---

## Lifecycle Hooks

Every generated `Abstract<Entity>Service` provides six protected no-op hooks.  
Override any of them in your `<Entity>Service` to inject business logic.

```
                    ┌─────────────────────────────────────────────┐
                    │           Abstract<Entity>Service            │
                    └─────────────────────────────────────────────┘

  create(dto)
    │
    ├── atakBeforeCreate(dto)       ← override: validate, transform, enrich dto
    ├── mapper.toEntity(dto)
    ├── [audit] set createdAt / updatedAt
    ├── repository.save(entity)
    ├── atakAfterCreate(entity)     ← override: publish event, send email, etc.
    └── return mapper.toResponse(entity)

  update(id, dto)
    │
    ├── repository.findById(id)
    ├── [tenant] verify tenantId
    ├── atakBeforeUpdate(dto, entity)  ← override: business validation
    ├── mapper.updateEntity(entity, dto)
    ├── [audit] set updatedAt
    ├── repository.save(entity)
    ├── atakAfterUpdate(entity)        ← override: invalidate cache, etc.
    └── return mapper.toResponse(entity)

  delete(id)
    │
    ├── repository.findById(id)
    ├── [tenant] verify tenantId
    ├── atakBeforeDelete(entity)    ← override: check business rules before delete
    ├── repository.delete(entity)
    └── atakAfterDelete(id)         ← override: cleanup, cascade, publish event
```

**Example:**

```java
@Service
public class InvoiceService extends AbstractInvoiceService {

    @Override
    @AtakHook(AtakHook.Phase.BEFORE_CREATE)
    protected void atakBeforeCreate(InvoiceRequestDto dto) {
        dto.setNumber(generateInvoiceNumber());  // auto-assign reference
    }

    @Override
    @AtakHook(AtakHook.Phase.AFTER_CREATE)
    protected void atakAfterCreate(Invoice entity) {
        eventPublisher.publish(new InvoiceCreatedEvent(entity.getId()));
    }

    @Override
    @AtakHook(AtakHook.Phase.BEFORE_DELETE)
    protected void atakBeforeDelete(Invoice entity) {
        if (!entity.getStatus().equals("DRAFT")) {
            throw new IllegalStateException("Only DRAFT invoices can be deleted");
        }
    }
}
```

---

## Security Hooks

Add `@AtakSecurity` to your entity to enable four additional hooks in the generated service.

```java
@AtakSecurity
@AtakEntity
@Entity
public class Order { ... }
```

The generated `AbstractOrderService` will:
1. Retrieve `Authentication` from `SecurityContextHolder`
2. Call the corresponding `atakCheck*` hook **before** each operation
3. Propagate any `AccessDeniedException` thrown inside the hook

```java
@Service
public class OrderService extends AbstractOrderService {

    @Override
    protected void atakCheckCreate(Authentication auth) {
        if (!hasRole(auth, "ROLE_SALES")) {
            throw new AccessDeniedException("Only SALES role can create orders");
        }
    }

    @Override
    protected void atakCheckRead(Authentication auth) {
        // public read — no restriction
    }

    @Override
    protected void atakCheckUpdate(Authentication auth, Order entity) {
        // only the owner or an admin can update
        if (!entity.getOwnerUsername().equals(auth.getName()) && !hasRole(auth, "ROLE_ADMIN")) {
            throw new AccessDeniedException("You do not own this order");
        }
    }

    @Override
    protected void atakCheckDelete(Authentication auth, Order entity) {
        requireRole(auth, "ROLE_ADMIN");
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth != null && auth.getAuthorities()
                .contains(new SimpleGrantedAuthority(role));
    }

    private void requireRole(Authentication auth, String role) {
        if (!hasRole(auth, role)) throw new AccessDeniedException("Required role: " + role);
    }
}
```

---

## Dynamic Filters

Add `@AtakFilter` to any entity field to generate a type-safe JPA Specification query.

```java
@AtakEntity
@Entity
public class Product {

    @AtakFilter                                  // LIKE on name
    private String name;

    @AtakFilter(AtakFilter.FilterType.RANGE)     // minPrice / maxPrice
    private Double price;

    @AtakFilter(AtakFilter.FilterType.IN)        // priceIn list
    private String category;
}
```

**What gets generated:**

```java
// atak.generated.filter.ProductFilter
public class ProductFilter {
    private String name;       // LIKE %name%
    private Double minPrice;   // price >= minPrice
    private Double maxPrice;   // price <= maxPrice
    private List<String> categoryIn;  // category IN (...)
    // getters / setters
}

// atak.generated.filter.ProductSpecification
public class ProductSpecification implements Specification<Product> {
    private final ProductFilter filter;

    @Override
    public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> q, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (filter.getName() != null)
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        if (filter.getMinPrice() != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
        if (filter.getMaxPrice() != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
        if (filter.getCategoryIn() != null && !filter.getCategoryIn().isEmpty())
            predicates.add(root.get("category").in(filter.getCategoryIn()));
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
```

**Controller endpoint (generated):**

```
GET /products?name=laptop&minPrice=500&maxPrice=2000&categoryIn=ELECTRONICS,GAMING&page=0&size=20&sort=price,asc
```

**Service method (generated):**

```java
public Page<ProductResponseDto> findAll(ProductFilter filter, Pageable pageable) {
    return repository.findAll(new ProductSpecification(filter), pageable)
                     .map(mapper::toResponse);
}
```

---

## Audit Trail

Add `@AtakAudit` to automatically track creation and modification metadata.

```java
@AtakAudit
@AtakEntity
@Entity
public class Contract extends ContractAuditBase {   // extend generated base class
    ...
}
```

**Generated `ContractAuditBase`** (in `atak.generated.audit`):

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class ContractAuditBase implements AtakAuditable {

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    // getters / setters (implements AtakAuditable)
}
```

**Generated `ContractResponseDto`** automatically includes:

```json
{
  "id": 1,
  "title": "Service Agreement",
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-03-22T14:00:00Z",
  "createdBy": "alice",
  "updatedBy": "bob"
}
```

**Required setup in your application class:**

```java
@SpringBootApplication
@EnableJpaAuditing
public class Application {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                             .map(Authentication::getName);
    }
}
```

---

## Multi-Tenancy

Add `@AtakTenant` to the field that identifies the tenant. ATAK automatically scopes every query and write to the current tenant, fetched from `TenantContext`.

```java
@AtakEntity
@Entity
public class Invoice {

    @AtakTenant                         // marks this as the tenant discriminator
    @Column(nullable = false)
    private String tenantId;            // excluded from DTOs automatically

    private String number;
    private Double amount;
    // ...
}
```

**What ATAK injects into `AbstractInvoiceService`:**

| Operation | Injected behaviour |
|---|---|
| `create` | `entity.setTenantId(TenantContext.current())` |
| `findAll(pageable)` | `WHERE tenant_id = TenantContext.current()` |
| `findAll(filter, pageable)` | filter spec AND-ed with tenant predicate |
| `findById` | result filtered to current tenant (404 if wrong tenant) |
| `update` | verifies tenant before updating |
| `delete` | verifies tenant before deleting |

**Populate the context** — create a Servlet filter:

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String tenantId = req.getHeader("X-Tenant-Id");   // or parse from JWT
        TenantContext.set(tenantId != null ? tenantId : "default");
        try {
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();   // always clean up
        }
    }
}
```

**`TenantContext` API:**

```java
TenantContext.set("acme-corp");    // set for current thread
TenantContext.current();           // returns current tenant (never null, defaults to "default")
TenantContext.clear();             // remove — call in finally block
```

---

## OpenAPI / Swagger

ATAK injects OpenAPI 3 annotations into every generated artifact when `openApi = true` (default).

**No manual annotation required.** After `mvn compile` + `spring-boot:run`:

- **Swagger UI** → `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON** → `http://localhost:8080/v3/api-docs`

**What gets annotated automatically:**

```java
// Controller class
@Tag(name = "Client", description = "CRUD operations for Client")

// Each endpoint
@Operation(summary = "Create Client")
@ApiResponse(responseCode = "201")

// RequestDto / ResponseDto
@Schema(description = "Client request payload")

// Each DTO field
@Schema(description = "Full name of the client", example = "Alice Martin", required = true)
```

**Springdoc configuration** (`application.properties`):

```properties
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.operationsSorter=method
```

---

## Maven Plugin

ATAK ships a Maven plugin with three goals for developer productivity.

### Setup

```xml
<plugin>
    <groupId>io.atak</groupId>
    <artifactId>atak-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</plugin>
```

Add `io.atak` to `pluginGroups` in `~/.m2/settings.xml` to use the `atak:` short prefix:

```xml
<settings>
    <pluginGroups>
        <pluginGroup>io.atak</pluginGroup>
    </pluginGroups>
</settings>
```

---

### atak:scaffold

Generates a ready-to-use entity `.java` file annotated with `@AtakEntity`.

```bash
mvn atak:scaffold \
  -Datak.entity=Invoice \
  -Datak.package=com.example.entity \
  -Datak.fields="number:String,amount:Double,status:String,dueDate:java.time.LocalDate"
```

With all features enabled:

```bash
mvn atak:scaffold \
  -Datak.entity=Invoice \
  -Datak.package=com.example.entity \
  -Datak.fields="number:String,amount:Double" \
  -Datak.audit=true \
  -Datak.security=true \
  -Datak.tenant=true
```

**Parameters:**

| Parameter | Default | Description |
|---|---|---|
| `atak.entity` | *(required)* | Entity class name in PascalCase |
| `atak.package` | `com.example.entity` | Target Java package |
| `atak.fields` | *(empty)* | Comma-separated `name:Type` pairs |
| `atak.outputDir` | `src/main/java` | Source root |
| `atak.audit` | `false` | Add `@AtakAudit` |
| `atak.security` | `false` | Add `@AtakSecurity` |
| `atak.tenant` | `false` | Add `tenantId` field with `@AtakTenant` |

**Output example** for `atak.entity=Invoice -Datak.fields="number:String,amount:Double"`:

```java
@AtakEntity
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @AtakField(description = "number")
    private String number;

    @AtakField(description = "amount")
    private Double amount;

    // getters / setters
}
```

Then run `mvn compile` and the full API layer is generated.

---

### atak:eject

Copies all ATAK-generated source files from `target/generated-sources/annotations/` to `src/main/java/`.

After ejection, the generated code becomes regular source files that you own and can modify freely. The ATAK processor dependency can then be removed.

```bash
# Compile first to ensure generated sources are up to date
mvn compile

# Eject (preview what will be copied)
mvn atak:eject -Datak.dryRun=true

# Eject for real
mvn atak:eject

# Eject and overwrite existing files
mvn atak:eject -Datak.overwrite=true
```

**Parameters:**

| Parameter | Default | Description |
|---|---|---|
| `atak.generatedSourcesDir` | `target/generated-sources/annotations` | Source of generated files |
| `atak.outputDir` | `src/main/java` | Destination |
| `atak.overwrite` | `false` | Overwrite existing files |
| `atak.dryRun` | `false` | Print what would be copied without writing |

> ⚠️ **Ejection is permanent.** Commit your work before running. Once ejected, changes you make to the source files will not be reflected by re-running the processor.

---

### atak:sync

Detects drift between the processor output and previously ejected source files.

```bash
mvn compile atak:sync
```

Sample output when drift is detected:

```
[ATAK] ⚠ Drift detected between generated sources and source tree:
  NEW (in generated, not ejected yet):
    + atak/generated/filter/InvoiceFilter.java
    + atak/generated/filter/InvoiceSpecification.java
  CHANGED (processor output differs from ejected source):
    ~ atak/generated/dto/InvoiceResponseDto.java
[ATAK] Run `mvn compile atak:eject -Datak.overwrite=true` to re-sync.
```

**Parameters:**

| Parameter | Default | Description |
|---|---|---|
| `atak.generatedSourcesDir` | `target/generated-sources/annotations` | Processor output |
| `atak.sourceDir` | `src/main/java` | Directory to compare against |
| `atak.failOnDrift` | `false` | Fail the build when drift is detected |

Use `failOnDrift=true` in CI to enforce that ejected files are always up to date:

```xml
<plugin>
    <groupId>io.atak</groupId>
    <artifactId>atak-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>check-sync</id>
            <phase>verify</phase>
            <goals><goal>sync</goal></goals>
            <configuration>
                <failOnDrift>true</failOnDrift>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## Configuration Reference

`application.properties` / `application.yml`:

| Property | Default | Description |
|---|---|---|
| `atak.exception-handler-enabled` | `true` | Register the global exception handler |
| `atak.open-api-customiser-enabled` | `true` | Register the OpenAPI customiser |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Swagger UI path |
| `springdoc.api-docs.path` | `/v3/api-docs` | OpenAPI JSON path |

**Global exception handler** (registered automatically by `atak-spring`):

| Scenario | HTTP status | Triggered by |
|---|---|---|
| Entity not found | `404 Not Found` | `RuntimeException` with "not found" in message |
| Validation failure | `400 Bad Request` | `MethodArgumentNotValidException` |
| Any other exception | `500 Internal Server Error` | `Exception` |

**Error response shape:**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Client not found: 42",
  "path": "/api/clients/42",
  "timestamp": "2025-05-01T10:00:00Z"
}
```

To disable the exception handler and use your own:

```properties
atak.exception-handler-enabled=false
```

---

## Extending Generated Code

### Override service methods entirely

```java
@Service
public class InvoiceService extends AbstractInvoiceService {

    private final PdfService pdfService;

    public InvoiceService(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @Override
    public InvoiceResponseDto create(InvoiceRequestDto dto) {
        // completely replace the generated create logic
        InvoiceResponseDto response = super.create(dto);
        pdfService.generateAsync(response.getId());
        return response;
    }
}
```

### Add custom endpoints alongside generated ones

The generated controller handles the five standard endpoints. Add your own controller for domain-specific operations:

```java
@RestController
@RequestMapping("/api/invoices")
public class InvoiceExtraController {

    private final InvoiceService invoiceService;

    @PostMapping("/{id}/send")
    public ResponseEntity<Void> sendToClient(@PathVariable Long id) {
        invoiceService.send(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csv = invoiceService.exportCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=invoices.csv")
                .body(csv);
    }
}
```

### Disable controller generation and write your own

```java
@AtakEntity(generateController = false)  // no controller generated
@Entity
public class Invoice { ... }
```

Then write a controller that injects the generated service directly.

---

## Full Example

An entity using every ATAK feature simultaneously:

```java
package com.example.entity;

import io.atak.annotation.*;
import jakarta.persistence.*;

@AtakAudit                                           // → audit fields + AuditBase
@AtakSecurity                                        // → security check hooks
@AtakEntity(path = "/api/orders", openApi = true)   // → full API layer
@Entity
@Table(name = "orders")
public class Order extends OrderAuditBase {          // extend generated audit base

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @AtakFilter                                      // → LIKE filter on reference
    @AtakField(required = true, description = "Order reference", example = "ORD-2025-001")
    private String reference;

    @AtakFilter(AtakFilter.FilterType.EQUALS)        // → exact match on status
    @AtakField(description = "Order status", example = "PENDING")
    private String status;

    @AtakFilter(AtakFilter.FilterType.RANGE)         // → minAmount / maxAmount
    @AtakField(required = true, description = "Total amount", example = "1500.00")
    private Double amount;

    @AtakTenant                                      // → multi-tenant isolation
    @Column(nullable = false)
    private String tenantId;

    @AtakIgnore                                      // → never exposed
    private String internalNotes;

    // getters / setters ...
}
```

```java
package com.example.service;

@Service
public class OrderService extends AbstractOrderService {

    // Lifecycle
    @Override
    protected void atakBeforeCreate(OrderRequestDto dto) {
        dto.setReference(generateOrderRef());
        dto.setStatus("PENDING");
    }

    @Override
    protected void atakAfterCreate(Order entity) {
        notificationService.notifyNewOrder(entity);
    }

    // Security
    @Override
    protected void atakCheckDelete(Authentication auth, Order entity) {
        if (!"DRAFT".equals(entity.getStatus()))
            throw new AccessDeniedException("Only DRAFT orders can be deleted");
    }
}
```

**Result:** `GET /api/orders?reference=ORD&minAmount=100&status=PENDING&page=0&size=10`  
→ filtered, tenant-scoped, paginated, fully documented in Swagger UI.

---

## Compatibility

| Requirement | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 3.0+ |
| Jakarta EE | 9+ (jakarta.* namespace) |
| Maven | 3.6+ |

ATAK uses standard `javax.annotation.processing` APIs and JavaPoet for code generation. It has no runtime dependencies beyond what Spring Boot already provides.

---

## Roadmap

| Version | Feature |
|---|---|
| **0.1 (current)** | Core CRUD · DTOs · Mapper · Repository · Service · Controller |
| **0.1 (current)** | Pagination · Dynamic filters · JPA Specification |
| **0.1 (current)** | OpenAPI / Swagger auto-injection |
| **0.1 (current)** | Audit trail (`@AtakAudit`) |
| **0.1 (current)** | Security hooks (`@AtakSecurity`) |
| **0.1 (current)** | Multi-tenancy (`@AtakTenant`) |
| **0.1 (current)** | Maven plugin (scaffold / eject / sync) |
| **0.2** | Gradle plugin |
| **0.2** | `@AtakDto` class-level request / response split |
| **0.3** | Event-driven hooks (Spring ApplicationEvent) |
| **0.3** | Soft delete (`@AtakSoftDelete`) |
| **0.4** | Projection support (lightweight response shapes) |
| **0.4** | Bulk operations |
| **1.0** | Stable API · full test suite · Maven Central release |

---

## License

Apache 2.0 — see [LICENSE](LICENSE).
