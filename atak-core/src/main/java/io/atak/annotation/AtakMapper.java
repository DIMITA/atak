package io.atak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placed on the entity class alongside {@link AtakEntity} to customise mapper generation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AtakMapper {

    /**
     * When {@code true}, the processor generates a Spring {@code @Component}-annotated
     * mapper. When {@code false}, a plain static utility class is produced.
     */
    boolean springComponent() default true;
}
