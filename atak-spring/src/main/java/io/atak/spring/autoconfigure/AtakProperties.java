package io.atak.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for ATAK Spring Boot integration. */
@ConfigurationProperties(prefix = "atak")
public class AtakProperties {

    /** Whether ATAK global exception handler is enabled. */
    private boolean exceptionHandlerEnabled = true;

    /** Whether to register the OpenAPI customiser that adds ATAK branding to docs. */
    private boolean openApiCustomiserEnabled = true;

    public boolean isExceptionHandlerEnabled() { return exceptionHandlerEnabled; }
    public void setExceptionHandlerEnabled(boolean v) { this.exceptionHandlerEnabled = v; }

    public boolean isOpenApiCustomiserEnabled() { return openApiCustomiserEnabled; }
    public void setOpenApiCustomiserEnabled(boolean v) { this.openApiCustomiserEnabled = v; }
}
