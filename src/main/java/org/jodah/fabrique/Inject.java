package org.jodah.fabrique;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Defines an injection point on a constructor, method or field where dependencies will be injected
 * by the {@link ObjectFactory}.
 * 
 * <p>
 * Every class can have only one constructor marked with {@code @Inject} with the {@code optional}
 * attribute set to false. This serves as the default constructor for the class. Additional
 * constructors may be marked with {@code @Inject} with the {@code optional} attribute set to true.
 * These constructors can be invoked explicitly by providing construction arguments to the factory.
 * If no class constructor is marked with {@code @Inject}, the default no argument constructor will
 * be used.
 * 
 * <p>
 * Constructors marked with {@code @Inject} will be overridden by any explicit binding parameters.
 * The {@code @Inject} annotation is not necessary for use on provider 'get' methods, though it can
 * be used to inject dependencies elsewhere in a provider.
 * 
 * <p>
 * The {@code optional} attribute indicates whether dependencies for an injection are optional. Only
 * one non-optional constructor is allowed. When set to true for a method, injection will be skipped
 * if any of the method parameters cannot be constructed. When set to true for a field, injection
 * will be skipped if the field type cannot be constructed.
 */
@Target({ CONSTRUCTOR, METHOD, FIELD })
@Retention(RUNTIME)
@Documented
public @interface Inject {
  boolean optional() default false;
}
