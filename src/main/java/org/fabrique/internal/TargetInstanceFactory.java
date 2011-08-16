package org.fabrique.internal;

import org.fabrique.Provider;

/**
 * Produces references to a target instance.
 * 
 * @param <T> Type to produce
 */
public class TargetInstanceFactory<T> extends InternalFactory<T> {
  private final T targetInstance;

  /**
   * Creates a new TargetInstanceFactory object.
   * 
   * @param targetInstance Target Instance
   */
  TargetInstanceFactory(T targetInstance) {
    super(targetInstance.getClass(), FactoryType.Target);
    this.targetInstance = targetInstance;
  }

  /**
   * {@inheritDoc}
   */
  T get(InjectionContext context, ConstructionInjector<T> constructionInjector, Object[] args) {
    throw new IllegalStateException("Unreachable");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  T get(InjectionContext context, Object[] args) {
    return targetInstance;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  Provider<T> getProvider(Object[] args) {
    return new Provider<T>() {
      public T get() {
        return targetInstance;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  void preInject() {
    injectMembers(new InjectionContext(), targetInstance);
  }
}
