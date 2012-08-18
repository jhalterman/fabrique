package org.jodah.fabrique;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Identifies annotations that are used for binding. Only one such annotation may apply to a single
 * injection point. You must also annotate binder annotations with {@code @Retention(RUNTIME)} and
 * some combination of {@code @Target( FIELD, PARAMETER, METHOD})}.
 * 
 * For example:
 * 
 * <pre>
 *   {@code @Retention(RUNTIME)}
 *   {@code @Target({FIELD, PARAMETER, METHOD})}
 *   {@code @BindingAnnotation}
 *   public {@code @interface} Transactional {}
 * </pre>
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
@Documented
public @interface BindingAnnotation {
}
