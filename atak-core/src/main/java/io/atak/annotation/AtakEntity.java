package io.atak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JPA entity for full ATAK code generation.
 *
 * <p>The annotation processor will generate at compile time:
 * <ul>
 *   <li>RequestDto / ResponseDto</li>
 *   <li>Mapper</li>
 *   <li>JPA Repository</li>
 *   <li>Abstract + concrete Service</li>
 *   <li>REST Controller with OpenAPI annotations</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AtakEntity {

    /** Base path for the generated REST controller. Defaults to lowercase entity name. */
    String path() default "";

    /** Whether to generate the CRUD controller. */
    boolean generateController() default true;

    /** Whether to inject OpenAPI / Swagger annotations. */
    boolean openApi() default true;
}
