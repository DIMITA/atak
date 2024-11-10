package io.atak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Customises DTO generation for an {@link AtakEntity}-annotated class.
 * Place on individual fields to override per-field behaviour,
 * or on the class alongside {@code @AtakEntity} to override class-level defaults.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface AtakDto {

    /** Human-readable description injected as @Schema(description = ...). */
    String description() default "";

    /** When {@code false} the field is excluded from the RequestDto. */
    boolean inRequest() default true;

    /** When {@code false} the field is excluded from the ResponseDto. */
    boolean inResponse() default true;
}
