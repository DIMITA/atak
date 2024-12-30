package io.atak.tenant;

/**
 * Thread-local holder for the current tenant identifier.
 *
 * <p>Populate this in a Servlet filter or Spring interceptor at the start of each request,
 * and clear it in a {@code finally} block (or use {@code try-with-resources}).
 *
 * <h3>Example filter</h3>
 * <pre>{@code
 * @Component
 * @Order(Ordered.HIGHEST_PRECEDENCE)
 * public class TenantFilter extends OncePerRequestFilter {
 *
 *     @Override
 *     protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
 *                                     FilterChain chain) throws IOException, ServletException {
 *         String tenantId = req.getHeader("X-Tenant-Id");
 *         TenantContext.set(tenantId != null ? tenantId : "default");
 *         try {
 *             chain.doFilter(req, res);
 *         } finally {
 *             TenantContext.clear();
 *         }
 *     }
 * }
 * }</pre>
 */
public final class TenantContext {

    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

    private TenantContext() {}

    /** Sets the current tenant for this thread. */
    public static void set(String tenantId) {
        TENANT.set(tenantId);
    }

    /**
     * Returns the current tenant, or {@code "default"} if none was set.
     * Generated services call this method — it never returns {@code null}.
     */
    public static String current() {
        String t = TENANT.get();
        return t != null ? t : "default";
    }

    /** Clears the current tenant. Always call this in a {@code finally} block. */
    public static void clear() {
        TENANT.remove();
    }
}
