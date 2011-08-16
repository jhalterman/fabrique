package org.fabrique;

/**
 * Scoped provider. Provides object instances within the context of a scope. Wraps an unscoped
 * provider and allows the unscoped provider to be replaced after creation.
 * 
 * @param <T> Provided type
 */
public abstract class ScopedProvider<T> implements Provider<T> {
  protected Provider<? extends T> provider;

  /**
   * Sets the internal unscoped provider. This can be replaced after the scoped provider is created
   * so that changing arguments can be swapped in via the unscoped provider.
   * 
   * @param unscoped Unscoped provider
   */
  public void setProvider(Provider<? extends T> unscoped) {
    provider = unscoped;
  }
}
