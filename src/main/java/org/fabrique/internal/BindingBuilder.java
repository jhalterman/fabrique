package org.fabrique.internal;

import java.lang.annotation.Annotation;

import java.util.List;

import org.fabrique.BindingAnnotation;
import org.fabrique.ConfigurationException;
import org.fabrique.Provider;
import org.fabrique.Scope;
import org.fabrique.Key;
import org.fabrique.Scopes;
import org.fabrique.builder.NamedBindingBuilder;
import org.fabrique.builder.ParamsBindingBuilder;
import org.fabrique.builder.ParamsSetBindingBuilder;
import org.fabrique.builder.ScopedBindingBuilder;
import org.fabrique.builder.TargetBindingBuilder;

/**
 * Builds a single binding according to the binder EDSL.
 * 
 * @param <T> Bound type
 */
class BindingBuilder<T> implements NamedBindingBuilder<T> {
  private final List<BindingImpl<?>> bindings;
  private BindingImpl<T> binding;
  private Class<T> type;
  private InternalFactory<T> internalFactory;

  /**
   * Creates a new BindingBuilder object.
   * 
   * @param bindings Binding storage
   * @param type Type to create binding builder for
   */
  public BindingBuilder(List<BindingImpl<?>> bindings, Class<T> type) {
    this(bindings, Key.get(type));
  }

  /**
   * Creates a new BindingBuilder object.
   * 
   * @param bindings Binding storage
   * @param key Key to create binding builder for
   */
  public BindingBuilder(List<BindingImpl<?>> bindings, Key<T> key) {
    this.bindings = bindings;
    type = key.getType();
    internalFactory = new TargetFactory<T>(key.getType());
    binding = new BindingImpl<T>(key, internalFactory);
    bindings.add(binding);
  }

  /**
   * {@inheritDoc}
   */
  public TargetBindingBuilder<T> as(Object name) {
    Validate.notNull(name, "Binding name cannot be null for " + type);
    binding.setKey(Key.get(binding.getKey().getType(), name));
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public TargetBindingBuilder<T> as(Class<? extends Annotation> annotation) {
    Validate.notNull(annotation, "Binding name annotation cannot be null for " + type);

    if (annotation.getAnnotation(BindingAnnotation.class) == null)
      throw new ConfigurationException(annotation + " must be annotated with @BindingAnnotation");

    binding.setKey(Key.get(binding.getKey().getType(), annotation));
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public void asEagerSingleton() {
    in(Scopes.EAGER_SINGLETON);
  }

  /**
   * {@inheritDoc}
   */
  public void asSingleton() {
    in(Scopes.SINGLETON);
  }

  /**
   * {@inheritDoc}
   */
  public ScopedBindingBuilder forOptionalParams(Class<?>[]... params) {
    Validate.noNullElements(params, "Binding parameters cannot be null for " + type);
    internalFactory.addOptionalParams(params);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ParamsSetBindingBuilder forOptionalParams(Class<?>... params) {
    Validate.noNullElements(params, "Binding parameters cannot be null for " + type);
    internalFactory.addOptionalParams(params);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public ParamsSetBindingBuilder forParams(Class<?>... params) {
    Validate.noNullElements(params, "Binding parameters cannot be null for " + type);
    internalFactory.setDefaultParams(params);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public void in(Scope scope) {
    Validate.notNull(scope, "Scope cannot be null");
    binding.setScope(scope);
  }

  /**
   * {@inheritDoc}
   */
  public ParamsBindingBuilder to(Class<? extends T> target) {
    Validate.validateType(target, "Target");

    if (target == binding.getKey().getType())
      throw new ConfigurationException("Target cannot be bound to itself for " + type);

    internalFactory = new TargetFactory<T>(target);
    binding.setInternalFactory(internalFactory);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public void toInstance(T targetInstance) {
    Validate.notNull(targetInstance, "Target instance cannot be null for " + type
        + ". Use Providers.<Type>of(null) instead.");

    internalFactory = new TargetInstanceFactory<T>(targetInstance);
    binding.setInternalFactory(internalFactory);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public ParamsBindingBuilder toProvider(Class<? extends Provider<? extends T>> provider) {
    Validate.validateType(provider, "Provider");

    /** Cast to throw away provided parent type */
    internalFactory = new ProviderFactory<T>((Class<? extends Provider<T>>) provider);
    binding.setInternalFactory(internalFactory);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public ParamsBindingBuilder toProvider(Provider<? extends T> provider) {
    Validate.notNull(provider, "Provider instance cannot be null for " + type);

    /** Cast to throw away provided parent type */
    internalFactory = new ProviderInstanceFactory<T>((Provider<T>) provider);
    binding.setInternalFactory(internalFactory);
    return this;
  }
}
