package org.fabrique.builder;

import org.fabrique.ConfigurationException;
import org.fabrique.Binder;
import org.fabrique.Provider;

/**
 * Builds a target binding.
 * 
 * <p>
 * See {@link Binder} for information on creating bindings.
 * 
 * @param <T> Bound type
 */
public interface TargetBindingBuilder<T> extends ParamsBindingBuilder {
  /**
   * Binds a type to a target type.
   * 
   * @param target Target to bind to
   * @return ScopedBindingBuilder
   * @throws ConfigurationException if {@code target} is invalid
   */
  ParamsBindingBuilder to(Class<? extends T> target);

  /**
   * Binds a type to a target instance.
   * 
   * @param targetInstance Target instance to bind to
   * @return ScopedBindingBuilder
   * @throws ConfigurationException if {@code targetInstance} is invalid
   */
  void toInstance(T targetInstance);

  /**
   * Binds a type to a provider instance.
   * 
   * @param provider Provider type to bind to
   * @return ScopedBindingBuilder
   * @throws ConfigurationException if {@code provider} is invalid
   */
  ParamsBindingBuilder toProvider(Provider<? extends T> provider);

  /**
   * Binds a type to a provider type.
   * 
   * @param pProviderType Provider type to bind to
   * @return ScopedBindingBuilder
   * @throws ConfigurationException if {@code provider} is invalid
   */
  ParamsBindingBuilder toProvider(Class<? extends Provider<? extends T>> provider);
}
