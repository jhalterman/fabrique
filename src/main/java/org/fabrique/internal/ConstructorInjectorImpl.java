package org.fabrique.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.fabrique.Provider;
import org.fabrique.Key;
import org.fabrique.Primitives;
import org.fabrique.ProvisionException;

/**
 * Constructs fully injected instances of {@code T} using a constructor.
 * 
 * @param <T> Type to construct
 */
public class ConstructorInjectorImpl<T> extends AbstractDependencyInjector implements
        ConstructionInjector<T> {
    private final Constructor<T> constructor;
    private ConstructorProxy<T> constructorProxy;

    /**
     * Creates a new ConstructorInjector object.
     * 
     * @param constructor Constructor to call
     * @param dependencies Injection dependencies
     */
    ConstructorInjectorImpl(Constructor<T> constructor, Key<?>[] dependencies) {
        super(dependencies, true);
        this.constructor = constructor;
        if (constructor != null && !Modifier.isPublic(constructor.getModifiers()))
            constructor.setAccessible(true);
    }

    /**
     * {@inheritDoc}
     */
    public T construct(InjectionContext context, Provider<T> provider, Object[] args) {
        /** Track circular dependencies */
        if (!context.constructing(constructor.getDeclaringClass()))
            throw new ProvisionException("Circular dependency detected while constructing "
                    + constructor.getDeclaringClass());

        Object[] constructionArgs = args;

        try {
            constructionArgs = constructionArgs == null ? injectDependencies(context) : Primitives
                    .convertPrimitives(args);
            if (constructorProxy == null)
                constructorProxy = ConstructorProxies.proxyFor(constructor);

            T object = constructorProxy.newInstance(constructionArgs);
            context.finished(constructor.getDeclaringClass());

            return object;
        } catch (Exception e) {
            throw new ProvisionException("Construction failed for " + constructor, e);
        }
    }
}
