package org.fabrique.internal;

import org.fabrique.Binding;
import org.fabrique.Provider;
import org.fabrique.Scope;
import org.fabrique.Key;
import org.fabrique.ScopedProvider;

/**
 * Binding implementation.
 * 
 * @param <T> Bound type
 */
public class BindingImpl<T> implements Binding<T> {
  private Scope scope;
  private InternalFactory<T> internalFactory;
  private Key<T> key;
  private ScopedProvider<T> scopedProvider;

  /**
   * Creates a new BindingImpl object.
   * 
   * @param key Binding key
   * @param pInternalFactory Default internal factory
   */
  public BindingImpl(Key<T> key, InternalFactory<T> interalFactory) {
    this.key = key;
    this.internalFactory = interalFactory;
  }

  /**
   * {@inheritDoc}
   */
  public T get(InjectionContext context, Object[] args) {
    if (scopedProvider == null)
      return internalFactory.get(context, args);
    scopedProvider.setProvider(internalFactory.getProvider(args));
    return scopedProvider.get();
  }

  /**
   * Gets the internal factory.
   * 
   * @return InternalFactory
   */
  public InternalFactory<T> getInternalFactory() {
    return internalFactory;
  }

  /**
   * {@inheritDoc}
   */
  public Key<T> getKey() {
    return key;
  }

  /**
   * {@inheritDoc}
   */
  public Provider<T> getProvider() {
    return getProvider(null);
  }

  /**
   * {@inheritDoc}
   */
  public Provider<T> getProvider(Object[] args) {
    if (scopedProvider == null)
      return internalFactory.getProvider(args);
    scopedProvider.setProvider(internalFactory.getProvider(args));
    return scopedProvider;
  }

  /**
   * {@inheritDoc}
   */
  public Scope getScope() {
    return scope;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return key.toString();
  }

  /**
   * Sets the internal factory as {@code internalFactory}.
   * 
   * @param internalFactory New internal factory
   */
  void setInternalFactory(InternalFactory<T> internalFactory) {
    this.internalFactory = internalFactory;
  }

  /**
   * Sets the binding key as {@code key}.
   * 
   * @param key New key
   */
  void setKey(Key<T> key) {
    this.key = key;
  }

  /**
   * Sets a binding scope.
   * 
   * @param scope New scope
   */
  void setScope(Scope scope) {
    this.scope = scope;
    scopedProvider = scope.scope(key);
  }
}
