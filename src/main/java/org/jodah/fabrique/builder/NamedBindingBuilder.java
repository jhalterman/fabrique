package org.jodah.fabrique.builder;

import java.lang.annotation.Annotation;

import org.jodah.fabrique.Binder;
import org.jodah.fabrique.ConfigurationException;

/**
 * Builds a named binding.
 * 
 * <p>
 * See {@link Binder} for information on creating bindings.
 * 
 * @param <T> Bound type
 */
public interface NamedBindingBuilder<T> extends TargetBindingBuilder<T> {
  /**
   * See the examples at {@link org.jodah.fabrique.Binder}.
   * 
   * @param name Binding name
   * @return LinkedBindingBuilder<T>
   * @throws ConfigurationException if {@code name} is empty
   */
  TargetBindingBuilder<T> as(Object name);

  /**
   * See the examples at {@link org.jodah.fabrique.Binder}.
   * 
   * @param annotation Binding name annotation
   * @return LinkedBindingBuilder<T>
   * @throws ConfigurationException if {@code annotation} is null
   */
  TargetBindingBuilder<T> as(Class<? extends Annotation> annotation);
}
