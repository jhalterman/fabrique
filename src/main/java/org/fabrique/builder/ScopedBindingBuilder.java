package org.fabrique.builder;

import org.fabrique.ConfigurationException;
import org.fabrique.Binder;
import org.fabrique.Scope;

/**
 * Builds a scoped binding.
 * 
 * <p>
 * See {@link Binder} for information on creating bindings.
 */
public interface ScopedBindingBuilder {
    /**
     * Creates the binding in an eager singleton scope.
     */
    void asEagerSingleton();

    /**
     * Creates the binding in a singleton scope.
     */
    void asSingleton();

    /**
     * Creates the binding in {@code scope}.
     * 
     * @param scope
     * @throws ConfigurationException if {@code scope} is null
     */
    void in(Scope scope);
}
