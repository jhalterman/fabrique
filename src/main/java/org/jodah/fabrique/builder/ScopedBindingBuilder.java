package org.jodah.fabrique.builder;

import org.jodah.fabrique.Binder;
import org.jodah.fabrique.ConfigurationException;
import org.jodah.fabrique.Scope;

/**
 * Builds a scoped binding.
 * 
 * <p>
 * See {@link Binder} for information on creating bindings.
 */
public interface ScopedBindingBuilder {
  /**
   * Creates the binding in an eager singleton scope.
   */
  void asEagerSingleton();

  /**
   * Creates the binding in a singleton scope.
   */
  void asSingleton();

  /**
   * Creates the binding in {@code scope}.
   * 
   * @param scope
   * @throws ConfigurationException if {@code scope} is null
   */
  void in(Scope scope);
}
