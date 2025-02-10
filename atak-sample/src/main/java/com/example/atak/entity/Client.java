package com.example.atak.entity;

import io.atak.annotation.*;
import jakarta.persistence.*;

/**
 * Demo entity showcasing @AtakAudit and @AtakSecurity.
 *
 * Generated at compile time:
 *  - ClientRequestDto / ClientResponseDto (+ createdAt, updatedAt, createdBy, updatedBy)
 *  - ClientMapper
 *  - ClientRepository (JpaRepository)
 *  - AbstractClientService (with atakCheck* security hooks + audit auto-fill)
 *  - ClientService stub (override hooks here)
 *  - ClientController  → GET/POST/PUT/DELETE /api/clients
 *  - ClientAuditBase   → this entity must extend it
 */
@AtakAudit
@AtakSecurity
@AtakEntity(path = "/api/clients", openApi = true)
@Entity
@Table(name = "clients")
public class Client extends atak.generated.audit.ClientAuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @AtakField(required = true, description = "Full name of the client", example = "Alice Martin")
    @Column(nullable = false)
    private String name;

    @AtakField(required = true, description = "Client email address", example = "alice@example.com")
    @Column(nullable = false, unique = true)
    private String email;

    @AtakField(description = "Client phone number", example = "+33 6 12 34 56 78")
    private String phone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
