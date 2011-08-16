package org.fabrique;

import org.fabrique.internal.InjectionContext;

/**
 * Encapsulates a single binding.
 * 
 * <p>
 * See {@link Binder} for information on creating bindings.
 * <p>
 * See {@link Inject} for information on defining injection points.
 * 
 * @param <T> Bound type
 */
public interface Binding<T> {
  /**
   * Gets a a fully injected instance of {@code T} for the binding and args {@code args}.
   * 
   * <p>
   * Note: It is not recommended to call this method directly. Instead call
   * ObjectFactory.getInstance.
   * 
   * @param context Injection Context
   * @param args Construction arguments
   * @return T
   */
  T get(InjectionContext context, Object[] args);

  /**
   * Returns the key for this binding.
   * 
   * @return Key<T>
   */
  Key<T> getKey();

  /**
   * Returns the provider used to fulfill instantiation requests for this binding using construction
   * arguments {@code args}.
   * 
   * @param args
   * @return IProvider<T>
   * @throws ConfigurationException If provider is not configured for parameters, yet {@code args}
   *           is not null
   */
  Provider<T> getProvider(Object[] args);

  /**
   * Returns the provider used to fulfill instantiation requests for this binding.
   * 
   * @return IProvider<T>
   */
  Provider<T> getProvider();

  /**
   * Returns the scope for the binding.
   * 
   * @return IScope
   */
  Scope getScope();
}
