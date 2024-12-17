package io.atak.generator;

import com.squareup.javapoet.*;
import io.atak.processor.EntityModel;

import javax.lang.model.element.Modifier;
import java.time.Instant;
import java.util.List;

/**
 * Generates {@code Abstract<Entity>Service} (generated package) and the concrete
 * {@code <Entity>Service} stub (user's package).
 *
 * <p>Conditional generation:
 * <ul>
 *   <li>{@code @AtakFilter} → adds {@code findAll(filter, pageable)} via JPA Specification</li>
 *   <li>{@code @AtakAudit}  → sets {@code createdAt}/{@code updatedAt} on save</li>
 *   <li>{@code @AtakSecurity} → adds four {@code atakCheck*} hooks called before each operation</li>
 *   <li>{@code @AtakTenant} → scopes all queries/writes to {@code TenantContext.current()}</li>
 * </ul>
 */
public final class ServiceGenerator {

    private static final ClassName SERVICE        = ClassName.get("org.springframework.stereotype", "Service");
    private static final ClassName TRANSACTIONAL  = ClassName.get("org.springframework.transaction.annotation", "Transactional");
    private static final ClassName AUTOWIRED      = ClassName.get("org.springframework.beans.factory.annotation", "Autowired");
    private static final ClassName RUNTIME_EX     = ClassName.get("java.lang", "RuntimeException");
    private static final ClassName ACCESS_DENIED  = ClassName.get("org.springframework.security.access", "AccessDeniedException");
    private static final ClassName AUTHENTICATION = ClassName.get("org.springframework.security.core", "Authentication");
    private static final ClassName SEC_CONTEXT    = ClassName.get("org.springframework.security.core.context", "SecurityContextHolder");
    private static final ClassName LIST_TYPE      = ClassName.get("java.util", "List");
    private static final ClassName PAGE           = ClassName.get("org.springframework.data.domain", "Page");
    private static final ClassName PAGEABLE       = ClassName.get("org.springframework.data.domain", "Pageable");
    private static final ClassName AUDITABLE      = ClassName.get("io.atak.audit", "AtakAuditable");
    private static final ClassName TENANT_CTX     = ClassName.get("io.atak.tenant", "TenantContext");
    private static final ClassName INSTANT        = ClassName.get(Instant.class);
    private static final ClassName SPECIFICATION  = ClassName.get("org.springframework.data.jpa.domain", "Specification");

    private ServiceGenerator() {}

    public static List<JavaFile> generate(EntityModel model) {
        return List.of(buildAbstractService(model), buildConcreteService(model));
    }

    // -----------------------------------------------------------------------

    private static JavaFile buildAbstractService(EntityModel model) {
        String    pkg     = model.generatedPackage("service");
        ClassName entity  = ClassName.get(model.packageName(), model.simpleName());
        ClassName reqDto  = ClassName.get(model.generatedPackage("dto"), model.requestDtoName());
        ClassName respDto = ClassName.get(model.generatedPackage("dto"), model.responseDtoName());
        ClassName mapper  = ClassName.get(model.generatedPackage("mapper"), model.mapperName());
        ClassName repo    = ClassName.get(model.generatedPackage("repository"), model.repositoryName());
        TypeName  idType  = resolveId(model);

        TypeSpec.Builder cls = TypeSpec.classBuilder(model.abstractServiceName())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(TRANSACTIONAL);

        cls.addField(FieldSpec.builder(repo, "repository", Modifier.PROTECTED).addAnnotation(AUTOWIRED).build());
        cls.addField(FieldSpec.builder(mapper, "mapper", Modifier.PROTECTED).addAnnotation(AUTOWIRED).build());

        // ---- create ----
        MethodSpec.Builder create = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .returns(respDto)
                .addParameter(reqDto, "dto");

        if (model.hasSecurity()) {
            create.addStatement("$T auth = $T.getContext().getAuthentication()", AUTHENTICATION, SEC_CONTEXT);
            create.addStatement("atakCheckCreate(auth)");
        }
        create.addStatement("atakBeforeCreate(dto)");
        create.addStatement("$T entity = mapper.toEntity(dto)", entity);
        if (model.hasAudit()) {
            create.beginControlFlow("if (entity instanceof $T auditable)", AUDITABLE)
                  .addStatement("auditable.setCreatedAt($T.now())", INSTANT)
                  .addStatement("auditable.setUpdatedAt($T.now())", INSTANT)
                  .endControlFlow();
        }
        create.addStatement("repository.save(entity)");
        create.addStatement("atakAfterCreate(entity)");
        create.addStatement("return mapper.toResponse(entity)");
        cls.addMethod(create.build());

        // ---- findById ----
        MethodSpec.Builder findById = MethodSpec.methodBuilder("findById")
                .addModifiers(Modifier.PUBLIC)
                .returns(respDto)
                .addParameter(idType, "id");

        if (model.hasSecurity()) {
            findById.addStatement("$T auth = $T.getContext().getAuthentication()", AUTHENTICATION, SEC_CONTEXT);
            findById.addStatement("atakCheckRead(auth)");
        }

        if (model.hasTenant()) {
            findById.addStatement(
                    "$T entity = repository.findById(id)\n    .filter(e -> $T.current().equals(e.get$L()))\n    .orElseThrow(() -> new $T($S + id))",
                    entity, TENANT_CTX,
                    DtoGenerator.capitalize(model.tenantFieldName()),
                    RUNTIME_EX, model.simpleName() + " not found: ");
            findById.addStatement("return mapper.toResponse(entity)");
        } else {
            findById.addStatement(
                    "return repository.findById(id)\n    .map(mapper::toResponse)\n    .orElseThrow(() -> new $T($S + id))",
                    RUNTIME_EX, model.simpleName() + " not found: ");
        }
        cls.addMethod(findById.build());

        // ---- findAll(Pageable) ----
        MethodSpec.Builder findAllPage = MethodSpec.methodBuilder("findAll")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(PAGE, respDto))
                .addParameter(PAGEABLE, "pageable");

        if (model.hasSecurity()) {
            findAllPage.addStatement("$T auth = $T.getContext().getAuthentication()", AUTHENTICATION, SEC_CONTEXT);
            findAllPage.addStatement("atakCheckRead(auth)");
        }

        if (model.hasTenant()) {
            ParameterizedTypeName specType = ParameterizedTypeName.get(SPECIFICATION, entity);
            findAllPage.addStatement(
                    "$T tenantSpec = (root, q, cb) -> cb.equal(root.get($S), $T.current())",
                    specType, model.tenantFieldName(), TENANT_CTX);
            findAllPage.addStatement("return repository.findAll(tenantSpec, pageable).map(mapper::toResponse)");
        } else {
            findAllPage.addStatement("return repository.findAll(pageable).map(mapper::toResponse)");
        }
        cls.addMethod(findAllPage.build());

        // ---- findAll(List) ----
        cls.addMethod(MethodSpec.methodBuilder("findAll")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(LIST_TYPE, respDto))
                .addStatement("return repository.findAll().stream().map(mapper::toResponse).toList()")
                .build());

        // ---- findAll(Filter, Pageable) — V2 ----
        if (model.hasFilters()) {
            ClassName filterDto = ClassName.get(model.generatedPackage("filter"), model.filterDtoName());
            ClassName specCls   = ClassName.get(model.generatedPackage("filter"), model.specificationName());

            MethodSpec.Builder findFiltered = MethodSpec.methodBuilder("findAll")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ParameterizedTypeName.get(PAGE, respDto))
                    .addParameter(filterDto, "filter")
                    .addParameter(PAGEABLE, "pageable");

            if (model.hasSecurity()) {
                findFiltered.addStatement("$T auth = $T.getContext().getAuthentication()", AUTHENTICATION, SEC_CONTEXT);
                findFiltered.addStatement("atakCheckRead(auth)");
            }

            if (model.hasTenant()) {
                ParameterizedTypeName specType = ParameterizedTypeName.get(SPECIFICATION, entity);
                findFiltered.addStatement(
                        "$T tenantSpec = (root, q, cb) -> cb.equal(root.get($S), $T.current())",
                        specType, model.tenantFieldName(), TENANT_CTX);
                findFiltered.addStatement(
                        "return repository.findAll(tenantSpec.and(new $T(filter)), pageable).map(mapper::toResponse)",
                        specCls);
            } else {
                findFiltered.addStatement(
                        "return repository.findAll(new $T(filter), pageable).map(mapper::toResponse)",
                        specCls);
            }
            cls.addMethod(findFiltered.build());
        }

        // ---- update ----
        MethodSpec.Builder update = MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .returns(respDto)
                .addParameter(idType, "id")
                .addParameter(reqDto, "dto");

        update.addStatement("$T entity = repository.findById(id)\n    .orElseThrow(() -> new $T($S + id))",
                entity, RUNTIME_EX, model.simpleName() + " not found: ");

        if (model.hasTenant()) {
            update.beginControlFlow("if (!$T.current().equals(entity.get$L()))",
                            TENANT_CTX, DtoGenerator.capitalize(model.tenantFieldName()))
                  .addStatement("throw new $T($S)", RUNTIME_EX, model.simpleName() + " not found: " + id)
                  .endControlFlow();
        }

        if (model.hasSecurity()) {
            update.addStatement("$T auth = $T.getContext().getAuthentication()", AUTHENTICATION, SEC_CONTEXT);
            update.addStatement("atakCheckUpdate(auth, entity)");
        }
        update.addStatement("atakBeforeUpdate(dto, entity)");
        update.addStatement("mapper.updateEntity(entity, dto)");
        if (model.hasAudit()) {
            update.beginControlFlow("if (entity instanceof $T auditable)", AUDITABLE)
                  .addStatement("auditable.setUpdatedAt($T.now())", INSTANT)
                  .endControlFlow();
        }
        update.addStatement("repository.save(entity)");
        update.addStatement("atakAfterUpdate(entity)");
        update.addStatement("return mapper.toResponse(entity)");
        cls.addMethod(update.build());

        // ---- delete ----
        MethodSpec.Builder delete = MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(idType, "id");

        delete.addStatement("$T entity = repository.findById(id)\n    .orElseThrow(() -> new $T($S + id))",
                entity, RUNTIME_EX, model.simpleName() + " not found: ");

        if (model.hasTenant()) {
            delete.beginControlFlow("if (!$T.current().equals(entity.get$L()))",
                            TENANT_CTX, DtoGenerator.capitalize(model.tenantFieldName()))
                  .addStatement("throw new $T($S)", RUNTIME_EX, model.simpleName() + " not found: ")
                  .endControlFlow();
        }

        if (model.hasSecurity()) {
            delete.addStatement("$T auth = $T.getContext().getAuthentication()", AUTHENTICATION, SEC_CONTEXT);
            delete.addStatement("atakCheckDelete(auth, entity)");
        }
        delete.addStatement("atakBeforeDelete(entity)");
        delete.addStatement("repository.delete(entity)");
        delete.addStatement("atakAfterDelete(id)");
        cls.addMethod(delete.build());

        // ---- lifecycle hooks ----
        addHook(cls, "atakBeforeCreate", reqDto, "dto");
        addHook(cls, "atakAfterCreate",  entity, "entity");
        addHook2(cls, "atakBeforeUpdate", reqDto, "dto", entity, "entity");
        addHook(cls, "atakAfterUpdate",  entity, "entity");
        addHook(cls, "atakBeforeDelete", entity, "entity");
        cls.addMethod(MethodSpec.methodBuilder("atakAfterDelete")
                .addModifiers(Modifier.PROTECTED).addParameter(idType, "id").build());

        // ---- security hooks (V3) ----
        if (model.hasSecurity()) {
            cls.addMethod(MethodSpec.methodBuilder("atakCheckCreate")
                    .addModifiers(Modifier.PROTECTED).addParameter(AUTHENTICATION, "auth").build());
            cls.addMethod(MethodSpec.methodBuilder("atakCheckRead")
                    .addModifiers(Modifier.PROTECTED).addParameter(AUTHENTICATION, "auth").build());
            cls.addMethod(MethodSpec.methodBuilder("atakCheckUpdate")
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(AUTHENTICATION, "auth")
                    .addParameter(entity, "entity").build());
            cls.addMethod(MethodSpec.methodBuilder("atakCheckDelete")
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(AUTHENTICATION, "auth")
                    .addParameter(entity, "entity").build());
        }

        return JavaFile.builder(pkg, cls.build())
                .addFileComment("Generated by ATAK — do not edit manually")
                .build();
    }

    private static JavaFile buildConcreteService(EntityModel model) {
        String pkg = model.packageName() + ".service";
        ClassName abstractSvc = ClassName.get(model.generatedPackage("service"), model.abstractServiceName());

        TypeSpec svc = TypeSpec.classBuilder(model.serviceName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(SERVICE)
                .superclass(abstractSvc)
                .addJavadoc("Override atakBefore*/atakAfter* (and atakCheck* when @AtakSecurity is used) to add business logic.")
                .build();

        return JavaFile.builder(pkg, svc)
                .addFileComment("Generated by ATAK — customise freely")
                .build();
    }

    private static void addHook(TypeSpec.Builder cls, String name, TypeName p1, String n1) {
        cls.addMethod(MethodSpec.methodBuilder(name).addModifiers(Modifier.PROTECTED)
                .addParameter(p1, n1).build());
    }

    private static void addHook2(TypeSpec.Builder cls, String name,
                                 TypeName p1, String n1, TypeName p2, String n2) {
        cls.addMethod(MethodSpec.methodBuilder(name).addModifiers(Modifier.PROTECTED)
                .addParameter(p1, n1).addParameter(p2, n2).build());
    }

    private static TypeName resolveId(EntityModel model) {
        try { return ClassName.bestGuess(model.idType()); }
        catch (IllegalArgumentException e) { return ClassName.get(Long.class); }
    }
}
