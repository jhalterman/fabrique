package org.jodah.fabrique.internal;

import org.jodah.fabrique.Binding;
import org.jodah.fabrique.ConfigurationException;

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
