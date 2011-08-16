package org.fabrique.internal;

import org.fabrique.InjectionException;
import org.fabrique.Key;

/**
 * Defines behavior for a type capable of managing and injecting dependencies.
 */
public interface DependencyInjector {
  /**
   * Gets dependencies.
   * 
   * @return Key<?>[]
   */
  public Key<?>[] getDependencies();

  /**
   * Provides injection of dependencies.
   * 
   * @param context Injection Context
   * @return Object[]
   * @throws InjectionException If a required dependency does not have a binding
   */
  Object[] injectDependencies(InjectionContext context);
}
