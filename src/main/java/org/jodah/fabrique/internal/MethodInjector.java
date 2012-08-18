package org.jodah.fabrique.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jodah.fabrique.InjectionException;
import org.jodah.fabrique.Key;

/**
 * Performs dependency injection for a single method.
 */
public class MethodInjector extends AbstractDependencyInjector implements MemberInjector {
  private final Method method;

  /**
   * Creates a new MethodInjector object.
   * 
   * @param method Method to inject
   * @param dependencies .
   * @param optional .
   */
  MethodInjector(Method method, Key<?>[] dependencies, boolean optional) {
    super(dependencies, optional);
    this.method = method;

    if (!Modifier.isPublic(method.getModifiers())
        || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
      method.setAccessible(true);
  }

  /**
   * {@inheritDoc}
   */
  public void inject(InjectionContext context, Object object) {
    try {
      method.invoke(object, injectDependencies(context));
    } catch (Exception e) {
      if (!optional)
        throw new InjectionException("Method injection failed for " + method, e);
    }
  }
}
