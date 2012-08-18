package org.jodah.fabrique;

import java.lang.reflect.Method;

import org.jodah.fabrique.builder.NamedBindingBuilder;
import org.jodah.fabrique.builder.TargetBindingBuilder;
import org.jodah.fabrique.intercept.IMethodInterceptor;
import org.jodah.fabrique.internal.Validate;
import org.jodah.fabrique.matcher.Matcher;

/**
 * Base module implementation.
 */
public abstract class AbstractModule implements Module {
  protected Binder binder;

  /**
   * {@inheritDoc}
   */
  public final synchronized void configure(Binder binder) {
    if (this.binder != null)
      throw new IllegalStateException("Re-entry is not allowed.");

    Validate.notNull(binder, "Binder cannot be null");
    this.binder = binder;

    try {
      configure();
    } finally {
      binder = null;
    }
  }

  /**
   * @see Binder#bind(Class)
   */
  protected <T> NamedBindingBuilder<T> bind(Class<T> type) {
    return binder.bind(type);
  }

  /**
   * @see Binder#bind(Key)
   */
  protected <T> TargetBindingBuilder<T> bind(Key<T> key) {
    return binder.bind(key);
  }

  /**
   * Configures a {@link Binder} via the exposed methods.
   */
  protected abstract void configure();

  /**
   * @see Binder#bindInterceptor(Matcher, Matcher, IMethodInterceptor...)
   */
  protected void bindInterceptor(Matcher<? super Class<?>> classMatcher,
      Matcher<? super Method> methodMatcher, IMethodInterceptor... interceptors) {
    binder.bindInterceptors(classMatcher, methodMatcher, interceptors);
  }

  /**
   * {@inheritDoc}
   * 
   * @see Binder#install(Module)
   */
  protected void install(Module module) {
    binder.install(module);
  }
}
