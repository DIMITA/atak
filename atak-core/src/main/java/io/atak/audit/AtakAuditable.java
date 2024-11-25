package io.atak.audit;

import java.time.Instant;

/**
 * Marker interface implemented by all ATAK-generated {@code <Entity>AuditBase} classes.
 *
 * <p>The generated {@code Abstract<Entity>Service} performs a safe {@code instanceof} check
 * and calls these setters automatically when {@code @AtakAudit} is present on the entity.
 *
 * <p>Your entity must extend the generated {@code <Entity>AuditBase} to satisfy this contract.
 */
public interface AtakAuditable {

    void setCreatedAt(Instant createdAt);
    Instant getCreatedAt();

    void setUpdatedAt(Instant updatedAt);
    Instant getUpdatedAt();

    void setCreatedBy(String createdBy);
    String getCreatedBy();

    void setUpdatedBy(String updatedBy);
    String getUpdatedBy();
}
