package com.example.atak.entity;

import io.atak.annotation.*;
import jakarta.persistence.*;

/**
 * Demo entity showcasing @AtakFilter and @AtakTenant.
 *
 * Generated at compile time:
 *  - ProductRequestDto / ProductResponseDto
 *  - ProductMapper        (sets tenantId from TenantContext on create)
 *  - ProductRepository    (JpaRepository + JpaSpecificationExecutor)
 *  - AbstractProductService
 *      ↳ findAll(ProductFilter, Pageable) via JPA Specification
 *      ↳ all queries/writes scoped to TenantContext.current()
 *  - ProductService stub
 *  - ProductController  → GET/POST/PUT/DELETE /api/products
 *  - ProductFilter      → query DTO (name:LIKE, priceMin/priceMax:RANGE)
 *  - ProductSpecification → JPA Specification implementation
 */
@AtakEntity(path = "/api/products", openApi = true)
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @AtakFilter                  // LIKE by default for String
    @AtakField(required = true, description = "Product name", example = "Laptop Pro 15")
    @Column(nullable = false)
    private String name;

    @AtakField(description = "Product description", example = "High-performance laptop")
    private String description;

    @AtakFilter(AtakFilter.FilterType.RANGE)   // generates minPrice + maxPrice filter params
    @AtakField(required = true, description = "Price in EUR", example = "1299.99")
    @Column(nullable = false)
    private Double price;

    @AtakTenant
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @AtakIgnore
    @Column(name = "internal_cost")
    private Double internalCost;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Double getInternalCost() { return internalCost; }
    public void setInternalCost(Double internalCost) { this.internalCost = internalCost; }
}
