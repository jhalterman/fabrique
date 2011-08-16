package org.fabrique.internal;

import org.fabrique.Provider;

/**
 * Adapts a provider to an internal factory and construction injector, providing type safe instances
 * of {@code T} from the internal factory.
 * 
 * @param <T> Provided Type
 */
class ProviderFactoryAdapter<T> implements Provider<T> {
  private final ConstructionInjector<T> constructionInjector;
  private final InternalFactory<T> internalFactory;
  private final Object[] args;

  /**
   * Creates a new ProviderInternalFactoryAdapter object.
   * 
   * @param internalFactory InternalFactory
   * @param constructionInjector Construction injector
   * @param pArgs Construction args
   */
  ProviderFactoryAdapter(InternalFactory<T> internalFactory,
      ConstructionInjector<T> constructionInjector, Object[] args) {
    this.internalFactory = internalFactory;
    this.constructionInjector = constructionInjector;
    this.args = args;
  }

  /**
   * {@inheritDoc}
   */
  public T get() {
    return internalFactory.get(new InjectionContext(), constructionInjector, args);
  }
}
