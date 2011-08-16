package org.fabrique.internal;

import org.fabrique.ConfigurationException;
import org.fabrique.Binding;

/**
 * Loads bindings.
 */
public interface BindingLoader {
  /**
   * Loads a binding.
   * 
   * @param binding Binding to load
   * @throws ConfigurationException if a binding for the key already exists
   */
  void loadBinding(Binding<?> binding) throws ConfigurationException;

  /**
   * Removes a binding. Used for late binding construction when an exception occurs.
   * 
   * @param binding pBinding
   */
  void removeBinding(Binding<?> binding);
}
