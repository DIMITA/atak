package io.atak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fine-grained control over a single field in generated code.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface AtakField {

    /** Override the JSON property name in DTOs. */
    String jsonName() default "";

    /** Whether the field is required (adds @NotNull / @NotBlank). */
    boolean required() default false;

    /** OpenAPI schema description for this field. */
    String description() default "";

    /** OpenAPI example value. */
    String example() default "";
}
