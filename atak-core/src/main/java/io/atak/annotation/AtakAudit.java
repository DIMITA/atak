package io.atak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables automatic audit-trail generation for an {@link AtakEntity}-annotated class.
 *
 * <p>When present, ATAK generates:
 * <ul>
 *   <li>{@code <Entity>AuditBase} — abstract JPA base class with four audit fields:
 *       {@code createdAt}, {@code updatedAt}, {@code createdBy}, {@code updatedBy}</li>
 *   <li>Audit fields added to {@code <Entity>ResponseDto}</li>
 *   <li>Mapper methods that copy audit fields to the DTO</li>
 *   <li>Service logic that sets {@code createdAt}/{@code updatedAt} automatically</li>
 * </ul>
 *
 * <h3>Requirements</h3>
 * <p>Your entity <strong>must</strong> extend the generated {@code <Entity>AuditBase}:
 * <pre>{@code
 * @AtakAudit
 * @AtakEntity
 * @Entity
 * public class Order extends OrderAuditBase { ... }
 * }</pre>
 *
 * <p>You must also add {@code @EnableJpaAuditing} to your Spring Boot application class
 * and provide an {@code AuditorAware<String>} bean to populate {@code createdBy}/{@code updatedBy}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AtakAudit {

    /** Include {@code createdBy} / {@code updatedBy} fields (requires an {@code AuditorAware} bean). */
    boolean trackUser() default true;
}
