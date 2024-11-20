package io.atak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables security permission hooks on an {@link AtakEntity}-annotated class.
 *
 * <p>When present, the generated {@code Abstract<Entity>Service} will call four
 * security check hooks before each CRUD operation. Each hook receives the current
 * Spring Security {@code Authentication}. Throw {@code AccessDeniedException} to block.
 *
 * <h3>Generated hooks</h3>
 * <pre>{@code
 * protected void atakCheckCreate(Authentication auth) {}
 * protected void atakCheckRead(Authentication auth) {}
 * protected void atakCheckUpdate(Authentication auth, Entity entity) {}
 * protected void atakCheckDelete(Authentication auth, Entity entity) {}
 * }</pre>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @Service
 * public class OrderService extends AbstractOrderService {
 *
 *     @Override
 *     protected void atakCheckCreate(Authentication auth) {
 *         if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
 *             throw new AccessDeniedException("Only admins can create orders");
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>Requires {@code spring-boot-starter-security} on the classpath.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AtakSecurity {
}
