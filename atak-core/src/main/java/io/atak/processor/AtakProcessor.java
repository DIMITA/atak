package io.atak.processor;

import com.squareup.javapoet.JavaFile;
import io.atak.annotation.AtakEntity;
import io.atak.generator.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Main ATAK annotation processor.
 *
 * <p>Triggered by {@code @AtakEntity} at compile time. For each annotated class it generates:
 * <ol>
 *   <li>RequestDto + ResponseDto (+ audit fields when {@code @AtakAudit})</li>
 *   <li>Mapper</li>
 *   <li>JPA Repository (+ JpaSpecificationExecutor when filters/tenant are used)</li>
 *   <li>AbstractService + Service stub</li>
 *   <li>REST Controller (optional)</li>
 *   <li>Filter DTO + Specification (when {@code @AtakFilter} fields exist)</li>
 *   <li>AuditBase class (when {@code @AtakAudit} is present)</li>
 * </ol>
 */
@SupportedAnnotationTypes("io.atak.annotation.AtakEntity")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class AtakProcessor extends AbstractProcessor {

    private EntityModelFactory modelFactory;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        modelFactory = new EntityModelFactory(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(AtakEntity.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@AtakEntity must be applied to a class",
                        element);
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            try {
                processEntity(typeElement);
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "ATAK processor failed for " + typeElement.getSimpleName() + ": " + e.getMessage(),
                        element);
            }
        }
        return true;
    }

    private void processEntity(TypeElement typeElement) throws IOException {
        EntityModel model = modelFactory.build(typeElement);

        List<JavaFile> files = new ArrayList<>();

        // Core CRUD layer
        files.addAll(DtoGenerator.generate(model));
        files.add(MapperGenerator.generate(model));
        files.add(RepositoryGenerator.generate(model));
        files.addAll(ServiceGenerator.generate(model));

        if (model.generateController()) {
            files.add(ControllerGenerator.generate(model));
        }

        // V2 — dynamic filters
        if (model.hasFilters()) {
            files.addAll(FilterGenerator.generate(model));
        }

        // V3 — audit base class
        if (model.hasAudit()) {
            files.add(AuditGenerator.generate(model));
        }

        for (JavaFile file : files) {
            file.writeTo(processingEnv.getFiler());
        }

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "[ATAK] Generated API layer for: " + typeElement.getSimpleName()
                        + (model.hasFilters()  ? " [filters]"  : "")
                        + (model.hasAudit()    ? " [audit]"    : "")
                        + (model.hasSecurity() ? " [security]" : "")
                        + (model.hasTenant()   ? " [tenant]"   : ""));
    }
}
