package org.fabrique.internal;

import org.fabrique.Provider;

/**
 * Produces fully injected instances of {@code T} from a provider instance.
 * 
 * @param <T> Type to produce
 */
public class ProviderInstanceFactory<T> extends ConstructionFactory<T> {
    private final Provider<T> providerInstance;

    /**
     * Creates a new ProviderFactory object.
     * 
     * @param providerInstance Provider Instance
     */
    ProviderInstanceFactory(Provider<T> providerInstance) {
        super(providerInstance.getClass(), FactoryType.Provider);
        this.providerInstance = providerInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    T get(InjectionContext context, Object[] args) {
        return constructionInjectorFor(args).construct(context, providerInstance, args);
    }

    /**
     * {@inheritDoc}
     */
    T get(InjectionContext context, ConstructionInjector<T> constructionInjector, Object[] pArgs) {
        return constructionInjector.construct(context, providerInstance, pArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void preInject() {
        injectMembers(new InjectionContext(), providerInstance);
    }
}
