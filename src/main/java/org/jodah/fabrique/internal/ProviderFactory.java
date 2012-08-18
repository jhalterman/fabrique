package org.jodah.fabrique.internal;

import org.jodah.fabrique.Provider;

/**
 * Produces fully injected instances of {@code T} from a provider.
 * 
 * @param <T> Provided type
 */
public class ProviderFactory<T> extends ConstructionFactory<T> {
  protected final Class<? extends Provider<T>> providerType;
  private TargetFactory<Provider<T>> providerFactory;

  /**
   * Creates a new ProviderFactory object.
   * 
   * @param providerType Provider type
   */
  ProviderFactory(Class<? extends Provider<T>> providerType) {
    super(providerType, FactoryType.Provider);
    this.providerType = providerType;
  }

  /**
   * {@inheritDoc}
   */
  T get(InjectionContext pContext, ConstructionInjector<T> constructionInjector, Object[] args) {
    return constructionInjector.construct(pContext, providerFactory.get(pContext, null), args);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  void initialize() {
    super.initialize();
    providerFactory = new TargetFactory<Provider<T>>(providerType);
    providerFactory.initialize();
  }
}
