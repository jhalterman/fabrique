package org.fabrique.internal;

import org.fabrique.ObjectFactory;
import org.fabrique.Key;

/**
 * Handles the injection of dependencies.
 */
abstract class AbstractDependencyInjector implements DependencyInjector {
  protected final Key<?>[] dependencies;
  protected final boolean optional;

  /**
   * Creates a new DependencyInjector object.
   * 
   * @param dependency Single dependency
   * @param optional Whether the dependency is optional
   */
  AbstractDependencyInjector(Key<?> dependency, boolean optional) {
    this.dependencies = new Key[] { dependency };
    this.optional = optional;
  }

  /**
   * Creates a new DependencyInjector object.
   * 
   * @param dependencies Dependencies
   * @param optional Whether the dependency is optional
   */
  AbstractDependencyInjector(Key<?>[] dependencies, boolean optional) {
    this.dependencies = dependencies;
    this.optional = optional;
  }

  /**
   * {@inheritDoc}
   */
  public Key<?>[] getDependencies() {
    return dependencies;
  }

  /**
   * {@inheritDoc}
   */
  public Object[] injectDependencies(InjectionContext context) {
    if (dependencies == null)
      return null;

    Object[] _result = new Object[dependencies.length];

    int i = 0;

    for (Key<?> _dependency : dependencies)
      _result[i++] = ObjectFactory.getBinding(_dependency).get(context, null);
    return _result;
  }
}
