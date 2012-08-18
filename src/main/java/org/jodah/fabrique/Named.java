package org.jodah.fabrique;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Annotates named objects to coincide with named bindings, allowing injection of named instances
 * wherever this annotation is used.
 */
@Retention(RUNTIME)
@Target({ PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD,
    ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE })
@BindingAnnotation
@Documented
public @interface Named {
  String value();
}
