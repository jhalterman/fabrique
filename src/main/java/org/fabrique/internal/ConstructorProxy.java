package org.fabrique.internal;

import java.lang.reflect.InvocationTargetException;

/**
 * Proxies calls to a {@link java.lang.reflect.Constructor} for a class {@code T}.
 * 
 * @param <T> Proxied type
 */
interface ConstructorProxy<T> {
    /**
     * Constructs an instance of {@code T} for the given arguments.
     * 
     * @param args Constructor arguments
     */
    T newInstance(Object... args) throws InvocationTargetException;
}
