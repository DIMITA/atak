package io.atak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables multi-tenancy on an {@link AtakEntity}-annotated class.
 *
 * <p>When present, ATAK automatically isolates data per tenant by:
 * <ul>
 *   <li>Setting the tenant field on entity creation from {@code TenantContext.current()}</li>
 *   <li>Adding a tenant predicate to all {@code findAll} queries</li>
 *   <li>Verifying the tenant on {@code findById}, {@code update}, and {@code delete}</li>
 * </ul>
 *
 * <h3>Setup</h3>
 * <ol>
 *   <li>Annotate the entity field that holds the tenant identifier:
 *   <pre>{@code
 *   @AtakTenant
 *   @Column(nullable = false)
 *   private String tenantId;
 *   }</pre></li>
 *   <li>Populate the tenant context in a request filter or interceptor:
 *   <pre>{@code
 *   TenantContext.set(resolveTenantFromJwt(request));
 *   }</pre></li>
 *   <li>Clear it after the request:
 *   <pre>{@code
 *   TenantContext.clear();
 *   }</pre></li>
 * </ol>
 *
 * <p>{@code TenantContext} is available from {@code io.atak.tenant.TenantContext}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface AtakTenant {
}
