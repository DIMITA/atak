package io.atak.processor;

import java.util.List;

/** Immutable snapshot of an @AtakEntity class used by all generators. */
record EntityModel(
        String packageName,
        String simpleName,
        String idType,
        boolean generateController,
        boolean openApi,
        String controllerPath,
        List<FieldModel> fields,
        // V2 — dynamic filters
        List<FieldModel> filterableFields,
        // V3 — audit
        boolean hasAudit,
        boolean auditTrackUser,
        // V3 — security hooks
        boolean hasSecurity,
        // V4 — multi-tenant
        String tenantFieldName   // null when not multi-tenant
) {

    record FieldModel(
            String name,
            String typeName,
            boolean inRequest,
            boolean inResponse,
            boolean required,
            String description,
            String example,
            String jsonName,
            // V2 filter
            FilterType filterType  // null when not filterable
    ) {}

    enum FilterType { LIKE, EQUALS, RANGE, IN }

    // --- convenience ---
    boolean hasFilters()  { return filterableFields != null && !filterableFields.isEmpty(); }
    boolean hasTenant()   { return tenantFieldName != null; }
    boolean needsSpecificationExecutor() { return hasFilters() || hasTenant(); }

    String requestDtoName()      { return simpleName + "RequestDto"; }
    String responseDtoName()     { return simpleName + "ResponseDto"; }
    String mapperName()          { return simpleName + "Mapper"; }
    String repositoryName()      { return simpleName + "Repository"; }
    String abstractServiceName() { return "Abstract" + simpleName + "Service"; }
    String serviceName()         { return simpleName + "Service"; }
    String controllerName()      { return simpleName + "Controller"; }
    String filterDtoName()       { return simpleName + "Filter"; }
    String specificationName()   { return simpleName + "Specification"; }
    String auditBaseName()       { return simpleName + "AuditBase"; }

    String generatedPackage(String sub) {
        return "atak.generated." + sub;
    }
}
