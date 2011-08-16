package org.fabrique.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.fabrique.Provider;
import org.fabrique.Key;
import org.fabrique.Primitives;
import org.fabrique.ProvisionException;

/**
 * Provides fully injected instances of {@code T} using a provider method.
 * 
 * @param <T> Provided type
 */
public class ProviderMethodInjector<T> extends AbstractDependencyInjector implements
        ConstructionInjector<T> {
    private final Method providerMethod;

    /**
     * Creates a new ProviderMethodInjector object.
     * 
     * @param providerMethod Provder 'get' method
     * @param dependencies .
     */
    ProviderMethodInjector(Method providerMethod, Key<?>[] dependencies) {
        super(dependencies, true);
        this.providerMethod = providerMethod;

        if (!Modifier.isPublic(providerMethod.getModifiers())
                || !Modifier.isPublic(providerMethod.getDeclaringClass().getModifiers()))
            providerMethod.setAccessible(true);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T construct(InjectionContext context, Provider<T> provider, Object[] args) {
        Object[] constructionArgs = args;

        try {
            constructionArgs = constructionArgs == null ? injectDependencies(context) : Primitives
                    .convertPrimitives(args);
            return (T) providerMethod.invoke(provider, constructionArgs);
        } catch (Exception e) {
            throw new ProvisionException("Provider 'get' failed for " + providerMethod, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object pObject) {
        return pObject instanceof ProviderMethodInjector
                && ((ProviderMethodInjector<?>) pObject).providerMethod.equals(providerMethod);
    }
}
