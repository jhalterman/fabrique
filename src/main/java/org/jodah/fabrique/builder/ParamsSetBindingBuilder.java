package org.jodah.fabrique.builder;

import org.jodah.fabrique.Binder;
import org.jodah.fabrique.ConfigurationException;

/**
 * Defines explicit optional construction parameters for the bound type, allowing arguments to be
 * passed to a target constructor or provider 'get' method. This serves as an alternative to the
 * {@code @Inject(optional = true)} annotation for target constructors.
 * 
 * <p>
 * Specifying explicit optional construction parameters replaces any existing optional constructor
 * or provider 'get' methods with ones matching the specified optional parameters.
 * 
 * <p>
 * See {@link Binder} for information on creating bindings.
 */
public interface ParamsSetBindingBuilder extends ScopedBindingBuilder {
  /**
   * Binds a set of optional params sets matching the binding's constructor or provider get method.
   * 
   * @param params Set of params sets for target or provider
   * @return ScopedBindingBuilder
   * @throws ConfigurationException If any element in {@code params} is null
   */
  ScopedBindingBuilder forOptionalParams(Class<?>[]... params);

  /**
   * Binds an optional params set matching the binding's constructor or provider get method.
   * 
   * @param params Params for target or provider
   * @return ParamsSetBindingBuilder
   * @throws ConfigurationException If any element in {@code params} is null
   */
  ParamsSetBindingBuilder forOptionalParams(Class<?>... params);
}
