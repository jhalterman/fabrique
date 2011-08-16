package org.fabrique.internal;

import org.fabrique.ConfigurationException;
import org.fabrique.ObjectFactory;
import org.fabrique.Provider;
import org.fabrique.Key;

/**
 * Constructs fully injected instances of {@code T}.
 * 
 * @param <T> Constructed type
 */
abstract class ConstructionFactory<T> extends InternalFactory<T> {
    /**
     * Creates a new ConstructionFactory object.
     * 
     * @param subject .
     * @param factoryType .
     */
    ConstructionFactory(Class<?> subject, FactoryType factoryType) {
        super(subject, factoryType);
    }

    /**
     * Gets the construction injector for {@code args}.
     * 
     * @param args Construction Args
     * @throws InjectionException If any of {@code args} is null
     * @throws ConfigurationException If no construction method matches {@code args}
     */
    protected ConstructionInjector<T> constructionInjectorFor(Object[] args) {
        ConstructionInjector<T> injector = Injectors.injectorFor(subject, constructionInjectors,
                args);
        if (injector == null)
            throw new ConfigurationException(
                    factoryType.equals(FactoryType.Target) ? Errors.noConstructor(subject, args)
                            : Errors.noProviderMethod(subject, args));
        return injector;
    }

    /**
     * {@inheritDoc}
     */
    T get(InjectionContext pContext, Object[] args) {
        return get(pContext, constructionInjectorFor(args), args);
    }

    /**
     * {@inheritDoc}
     */
    Provider<T> getProvider(Object[] args) {
        ConstructionInjector<T> injector = constructionInjectorFor(args);

        /** If no args, verify that default injector can be fulfilled by the factory */
        if (args == null) {
            for (Key<?> dependency : injector.getDependencies()) {
                try {
                    ObjectFactory.getBinding(dependency);
                } catch (ConfigurationException e) {
                    throw new ConfigurationException(
                            factoryType.equals(FactoryType.Target) ? Errors.noConstructor(subject,
                                    args) : Errors.noProviderMethod(subject, args));
                }
            }
        }

        return new ProviderFactoryAdapter<T>(this, injector, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void initialize() {
        super.initialize();
        constructionInjectors = Injectors.constructionInjectorsFor(
                subject,
                factoryType,
                defaultParams,
                optionalParams == null ? null : optionalParams.toArray(new Class<?>[optionalParams
                        .size()][]));
        defaultParams = null;
        optionalParams = null;
    }
}
