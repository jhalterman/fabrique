package org.fabrique;

/**
 * Produces providers that are capable of producing objects within a specific scope.
 * 
 * <p>
 * Note: Scopes do not take binding arguments into account. Objects produced for a scoped binding
 * will be unique for the scope, regardless of arguments.
 */
public interface Scope {
  /**
   * Returns a scoped provider constructed specifically for they binding {@code key}. The scoped
   * provider is capable producing objects within a particular scope.
   * 
   * @param key Binding key
   * @param pProvider Unscoped provider
   * @return ScopedProvider Scoped provider
   */
  <T> ScopedProvider<T> scope(Key<T> key);
}
