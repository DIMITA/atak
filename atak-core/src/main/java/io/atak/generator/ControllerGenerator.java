package io.atak.generator;

import com.squareup.javapoet.*;
import io.atak.processor.EntityModel;

import javax.lang.model.element.Modifier;

/**
 * Generates a Spring MVC REST controller with optional OpenAPI annotations.
 *
 * <p>When {@code @AtakFilter} fields exist, an additional
 * {@code GET /?field=value} search endpoint is generated that delegates to
 * the filter-aware service method.
 */
public final class ControllerGenerator {

    // Spring Web
    private static final ClassName REST_CONTROLLER = ClassName.get("org.springframework.web.bind.annotation", "RestController");
    private static final ClassName REQUEST_MAPPING  = ClassName.get("org.springframework.web.bind.annotation", "RequestMapping");
    private static final ClassName GET_MAPPING      = ClassName.get("org.springframework.web.bind.annotation", "GetMapping");
    private static final ClassName POST_MAPPING     = ClassName.get("org.springframework.web.bind.annotation", "PostMapping");
    private static final ClassName PUT_MAPPING      = ClassName.get("org.springframework.web.bind.annotation", "PutMapping");
    private static final ClassName DELETE_MAPPING   = ClassName.get("org.springframework.web.bind.annotation", "DeleteMapping");
    private static final ClassName PATH_VARIABLE    = ClassName.get("org.springframework.web.bind.annotation", "PathVariable");
    private static final ClassName REQUEST_BODY     = ClassName.get("org.springframework.web.bind.annotation", "RequestBody");
    private static final ClassName MODEL_ATTRIBUTE  = ClassName.get("org.springframework.web.bind.annotation", "ModelAttribute");
    private static final ClassName RESPONSE_ENTITY  = ClassName.get("org.springframework.http", "ResponseEntity");
    private static final ClassName HTTP_STATUS      = ClassName.get("org.springframework.http", "HttpStatus");
    private static final ClassName VALID            = ClassName.get("jakarta.validation", "Valid");

    // Pageable
    private static final ClassName PAGEABLE = ClassName.get("org.springframework.data.domain", "Pageable");

    // OpenAPI
    private static final ClassName OPERATION    = ClassName.get("io.swagger.v3.oas.annotations", "Operation");
    private static final ClassName API_RESPONSE  = ClassName.get("io.swagger.v3.oas.annotations.responses", "ApiResponse");
    private static final ClassName TAG          = ClassName.get("io.swagger.v3.oas.annotations", "tags", "Tag");

    private ControllerGenerator() {}

    public static JavaFile generate(EntityModel model) {
        String    pkg     = model.generatedPackage("controller");
        ClassName reqDto  = ClassName.get(model.generatedPackage("dto"), model.requestDtoName());
        ClassName respDto = ClassName.get(model.generatedPackage("dto"), model.responseDtoName());
        ClassName svc     = ClassName.get(model.packageName() + ".service", model.serviceName());
        TypeName  idType  = resolveId(model);

        TypeSpec.Builder cls = TypeSpec.classBuilder(model.controllerName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(REST_CONTROLLER)
                .addAnnotation(AnnotationSpec.builder(REQUEST_MAPPING)
                        .addMember("value", "$S", model.controllerPath()).build());

        if (model.openApi()) {
            cls.addAnnotation(AnnotationSpec.builder(TAG)
                    .addMember("name", "$S", model.simpleName())
                    .addMember("description", "$S", "CRUD operations for " + model.simpleName())
                    .build());
        }

        // constructor injection
        cls.addField(FieldSpec.builder(svc, "service", Modifier.PRIVATE, Modifier.FINAL).build());
        cls.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(svc, "service")
                .addStatement("this.service = service")
                .build());

        // POST /
        MethodSpec.Builder create = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(RESPONSE_ENTITY, respDto))
                .addAnnotation(POST_MAPPING)
                .addParameter(ParameterSpec.builder(reqDto, "dto")
                        .addAnnotation(VALID).addAnnotation(REQUEST_BODY).build())
                .addStatement("return $T.status($T.CREATED).body(service.create(dto))", RESPONSE_ENTITY, HTTP_STATUS);
        if (model.openApi()) openApi(create, "Create " + model.simpleName(), "201");
        cls.addMethod(create.build());

        // GET /{id}
        MethodSpec.Builder findById = MethodSpec.methodBuilder("findById")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(RESPONSE_ENTITY, respDto))
                .addAnnotation(AnnotationSpec.builder(GET_MAPPING).addMember("value", "$S", "/{id}").build())
                .addParameter(ParameterSpec.builder(idType, "id")
                        .addAnnotation(AnnotationSpec.builder(PATH_VARIABLE).addMember("value", "$S", "id").build()).build())
                .addStatement("return $T.ok(service.findById(id))", RESPONSE_ENTITY);
        if (model.openApi()) openApi(findById, "Get " + model.simpleName() + " by ID", "200");
        cls.addMethod(findById.build());

        // GET / (pageable — plain list when no filters)
        if (!model.hasFilters()) {
            MethodSpec.Builder findAll = MethodSpec.methodBuilder("findAll")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(RESPONSE_ENTITY)
                    .addAnnotation(GET_MAPPING)
                    .addParameter(PAGEABLE, "pageable")
                    .addStatement("return $T.ok(service.findAll(pageable))", RESPONSE_ENTITY);
            if (model.openApi()) openApi(findAll, "List all " + model.simpleName() + "s", "200");
            cls.addMethod(findAll.build());
        } else {
            // GET / with filter params  (V2)
            ClassName filterDto = ClassName.get(model.generatedPackage("filter"), model.filterDtoName());

            MethodSpec.Builder search = MethodSpec.methodBuilder("search")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(RESPONSE_ENTITY)
                    .addAnnotation(GET_MAPPING)
                    .addParameter(ParameterSpec.builder(filterDto, "filter")
                            .addAnnotation(MODEL_ATTRIBUTE).build())
                    .addParameter(PAGEABLE, "pageable")
                    .addStatement("return $T.ok(service.findAll(filter, pageable))", RESPONSE_ENTITY);
            if (model.openApi()) openApi(search, "Search " + model.simpleName() + "s with filters", "200");
            cls.addMethod(search.build());
        }

        // PUT /{id}
        MethodSpec.Builder update = MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(RESPONSE_ENTITY, respDto))
                .addAnnotation(AnnotationSpec.builder(PUT_MAPPING).addMember("value", "$S", "/{id}").build())
                .addParameter(ParameterSpec.builder(idType, "id")
                        .addAnnotation(AnnotationSpec.builder(PATH_VARIABLE).addMember("value", "$S", "id").build()).build())
                .addParameter(ParameterSpec.builder(reqDto, "dto")
                        .addAnnotation(VALID).addAnnotation(REQUEST_BODY).build())
                .addStatement("return $T.ok(service.update(id, dto))", RESPONSE_ENTITY);
        if (model.openApi()) openApi(update, "Update " + model.simpleName(), "200");
        cls.addMethod(update.build());

        // DELETE /{id}
        MethodSpec.Builder delete = MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(RESPONSE_ENTITY, ClassName.get(Void.class)))
                .addAnnotation(AnnotationSpec.builder(DELETE_MAPPING).addMember("value", "$S", "/{id}").build())
                .addParameter(ParameterSpec.builder(idType, "id")
                        .addAnnotation(AnnotationSpec.builder(PATH_VARIABLE).addMember("value", "$S", "id").build()).build())
                .addStatement("service.delete(id)")
                .addStatement("return $T.noContent().build()", RESPONSE_ENTITY);
        if (model.openApi()) openApi(delete, "Delete " + model.simpleName(), "204");
        cls.addMethod(delete.build());

        return JavaFile.builder(pkg, cls.build())
                .addFileComment("Generated by ATAK — do not edit manually")
                .build();
    }

    private static void openApi(MethodSpec.Builder m, String summary, String code) {
        m.addAnnotation(AnnotationSpec.builder(OPERATION).addMember("summary", "$S", summary).build());
        m.addAnnotation(AnnotationSpec.builder(API_RESPONSE).addMember("responseCode", "$S", code).build());
    }

    private static TypeName resolveId(EntityModel model) {
        try { return ClassName.bestGuess(model.idType()); }
        catch (IllegalArgumentException e) { return ClassName.get(Long.class); }
    }
}
