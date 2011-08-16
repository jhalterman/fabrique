package org.fabrique;

/**
 * Provides instances of type {@code T}.
 * 
 * <p>
 * Implementations may overload the {@code get} method with additional get methods requiring
 * specific parameters to be defined when creating bindings. These get methods will then be called
 * with arguments being passed through from the factory when an object is requested from the
 * provider.
 * 
 * @param <T> Type being provided
 */
public interface Provider<T> {
    /**
     * Provides instances of T.
     * 
     * @return T
     * @throws ProvisionException if an instance cannot be provided.
     */
    T get();
}
