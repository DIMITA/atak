package io.atak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as filterable in generated query endpoints.
 *
 * <p>The processor generates:
 * <ul>
 *   <li>{@code <Entity>Filter} — DTO with optional filter fields</li>
 *   <li>{@code <Entity>Specification} — JPA {@code Specification<Entity>} implementation</li>
 *   <li>A {@code findAll(filter, pageable)} method on {@code Abstract<Entity>Service}</li>
 *   <li>A {@code GET /?field=value} endpoint on the controller</li>
 * </ul>
 *
 * <h3>Filter types</h3>
 * <ul>
 *   <li>{@link FilterType#LIKE} — case-insensitive substring match (default for String)</li>
 *   <li>{@link FilterType#EQUALS} — exact match (default for non-String)</li>
 *   <li>{@link FilterType#RANGE} — generates {@code minField} + {@code maxField} params</li>
 *   <li>{@link FilterType#IN} — generates {@code fieldIn: List<T>} param</li>
 * </ul>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface AtakFilter {

    FilterType value() default FilterType.AUTO;

    enum FilterType {
        /** Infer from field type: {@code LIKE} for String, {@code EQUALS} otherwise. */
        AUTO,
        /** Case-insensitive {@code LIKE %value%}. */
        LIKE,
        /** Exact equality predicate. */
        EQUALS,
        /** Generates {@code minField} + {@code maxField} query params. */
        RANGE,
        /** Generates {@code fieldIn} query param accepting a comma-separated list. */
        IN
    }
}
