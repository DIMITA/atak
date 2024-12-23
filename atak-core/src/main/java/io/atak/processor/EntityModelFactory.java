package io.atak.processor;

import io.atak.annotation.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/** Builds an {@link EntityModel} from a type element annotated with {@code @AtakEntity}. */
final class EntityModelFactory {

    private final ProcessingEnvironment env;

    EntityModelFactory(ProcessingEnvironment env) {
        this.env = env;
    }

    EntityModel build(TypeElement typeElement) {
        AtakEntity atakEntity = typeElement.getAnnotation(AtakEntity.class);
        AtakAudit  atakAudit  = typeElement.getAnnotation(AtakAudit.class);
        boolean    hasSecurity = typeElement.getAnnotation(AtakSecurity.class) != null;

        String packageName = env.getElementUtils()
                .getPackageOf(typeElement).getQualifiedName().toString();
        String simpleName = typeElement.getSimpleName().toString();

        String idType = resolveIdType(typeElement);
        String path = atakEntity.path().isBlank()
                ? "/" + simpleName.toLowerCase() + "s"
                : atakEntity.path();

        List<EntityModel.FieldModel> fields          = new ArrayList<>();
        List<EntityModel.FieldModel> filterableFields = new ArrayList<>();
        String tenantFieldName = null;

        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.FIELD) continue;
            VariableElement field = (VariableElement) enclosed;
            String fieldName = field.getSimpleName().toString();

            if (field.getAnnotation(AtakIgnore.class) != null) continue;
            if (fieldName.equals("serialVersionUID"))           continue;

            // @AtakTenant: record field name, exclude from DTOs by default
            if (field.getAnnotation(AtakTenant.class) != null) {
                tenantFieldName = fieldName;
                continue;
            }

            EntityModel.FieldModel fm = buildFieldModel(field);
            fields.add(fm);

            if (fm.filterType() != null) {
                filterableFields.add(fm);
            }
        }

        return new EntityModel(
                packageName, simpleName, idType,
                atakEntity.generateController(), atakEntity.openApi(), path,
                fields,
                filterableFields,
                atakAudit != null, atakAudit != null && atakAudit.trackUser(),
                hasSecurity,
                tenantFieldName
        );
    }

    private EntityModel.FieldModel buildFieldModel(VariableElement field) {
        String name     = field.getSimpleName().toString();
        String typeName = field.asType().toString();

        AtakDto    dto = field.getAnnotation(AtakDto.class);
        AtakField  af  = field.getAnnotation(AtakField.class);
        AtakFilter flt = field.getAnnotation(AtakFilter.class);

        boolean inRequest  = dto == null || dto.inRequest();
        boolean inResponse = dto == null || dto.inResponse();
        boolean required   = af != null && af.required();

        String description = af != null && !af.description().isBlank() ? af.description()
                           : dto != null && !dto.description().isBlank() ? dto.description() : "";
        String example     = af != null ? af.example() : "";
        String jsonName    = af != null && !af.jsonName().isBlank() ? af.jsonName() : name;

        EntityModel.FilterType filterType = null;
        if (flt != null) {
            filterType = resolveFilterType(flt.value(), typeName);
        }

        return new EntityModel.FieldModel(
                name, typeName, inRequest, inResponse, required, description, example, jsonName,
                filterType
        );
    }

    private EntityModel.FilterType resolveFilterType(AtakFilter.FilterType declared, String typeName) {
        if (declared != AtakFilter.FilterType.AUTO) {
            return EntityModel.FilterType.valueOf(declared.name());
        }
        return typeName.equals("java.lang.String") || typeName.equals("String")
                ? EntityModel.FilterType.LIKE
                : EntityModel.FilterType.EQUALS;
    }

    private String resolveIdType(TypeElement typeElement) {
        for (Element e : typeElement.getEnclosedElements()) {
            if (e.getKind() != ElementKind.FIELD) continue;
            boolean hasId = e.getAnnotationMirrors().stream().anyMatch(a -> {
                String n = ((TypeElement) ((DeclaredType) a.getAnnotationType()).asElement())
                        .getQualifiedName().toString();
                return n.equals("jakarta.persistence.Id") || n.equals("javax.persistence.Id");
            });
            if (hasId) {
                TypeMirror t = ((VariableElement) e).asType();
                return t.getKind() == TypeKind.DECLARED ? t.toString() : box(t.getKind());
            }
        }
        return "java.lang.Long";
    }

    private String box(TypeKind kind) {
        return switch (kind) {
            case INT  -> "java.lang.Integer";
            case LONG -> "java.lang.Long";
            default   -> "java.lang.Long";
        };
    }
}
