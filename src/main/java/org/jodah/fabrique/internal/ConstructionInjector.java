package org.jodah.fabrique.internal;

import org.jodah.fabrique.Provider;

/**
 * Defines behavior for an injector that constructs fully injected instances of {@code T}.
 * 
 * @param <T> Type to construct
 */
public interface ConstructionInjector<T> extends DependencyInjector {
  /**
   * Performs construction for the wrapped type.
   * 
   * @param context Injection Context
   * @param provider Optional provider
   * @param args Construction arguments
   * @throws ProvisionException If circular dependency is detected or if {@code T} or any
   *           dependencies could not be constructed
   */
  T construct(InjectionContext context, Provider<T> provider, Object[] args);
}
