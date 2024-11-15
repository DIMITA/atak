package io.atak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents a lifecycle hook method inside a service that extends the generated
 * {@code Abstract<Entity>Service}.
 *
 * <p>This annotation is informational only — the processor does not generate additional
 * code for it. It exists to make hook overrides discoverable via tooling.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface AtakHook {

    enum Phase {
        BEFORE_CREATE, AFTER_CREATE,
        BEFORE_UPDATE, AFTER_UPDATE,
        BEFORE_DELETE, AFTER_DELETE
    }

    Phase value();
}
