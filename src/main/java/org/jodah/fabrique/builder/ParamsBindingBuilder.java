package org.jodah.fabrique.builder;

import org.jodah.fabrique.Binder;
import org.jodah.fabrique.ConfigurationException;

/**
 * Defines explicit construction parameters for the bound type, allowing arguments to be passed to a
 * target constructor or provider 'get' method. This serves as an alternative to the {@code @Inject}
 * annotation for target constructors.
 * 
 * <p>
 * Specifying explicit construction parameters replaces the default constructor or provider 'get'
 * method with one matching the specified parameters.
 * 
 * <p>
 * See {@link Binder} for information on creating bindings.
 */
public interface ParamsBindingBuilder extends ParamsSetBindingBuilder {
  /**
   * Binds a set of param types matching the binding's constructor or provider 'get' method.
   * 
   * @param params Params for target or provider
   * @return ParamsSetBindingBuilder
   * @throws ConfigurationException If any element in {@code params} is null
   */
  ParamsSetBindingBuilder forParams(Class<?>... params);
}
