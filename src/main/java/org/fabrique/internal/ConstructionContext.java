package org.fabrique.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.HashSet;
import java.util.Set;

/**
 * Maintains context for an instance of {@code T} that is being constructed.
 * 
 * <p>
 * Note: Implemented for later use in case we want to allow circular dependencies.
 * 
 * @param <T> Type being constructed
 */
public class ConstructionContext<T> {
  private DelegatingInvocationHandler<T> invocationHandler;
  private T object;
  private T proxy;
  private boolean constructing;

  /**
   * Invocation handler that delegates to an internal object. Used to proxy objects resulting from a
   * circular dependency.
   */
  static class DelegatingInvocationHandler<T> implements InvocationHandler {
    private T delegate;

    /**
     * {@inheritDoc}
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (delegate == null)
        throw new IllegalStateException(
            "The object being requested is not yet constructed. Please wait until after injection is complete to use this object.");
      try {
        return method.invoke(delegate, args);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    }

    /**
     * Sets the delegate
     * 
     * @param delegate New delegate
     */
    void setDelegate(T delegate) {
      this.delegate = delegate;
    }
  }

  /**
   * Marks construction as complete.
   */
  public void complete() {
    constructing = false;
    invocationHandler = null;
  }

  /**
   * Marks construction as being in progress.
   */
  public void constructing() {
    constructing = true;
  }

  /**
   * Gets the object being constructed.
   * 
   * @return T
   */
  public T getObject() {
    return object;
  }

  /**
   * Creates a proxy for {@code pType} backed by a delegating invocation handler.
   * 
   * @param type Type to create proxy for
   * @return Object Proxy for {@code pType}
   */
  public T getProxy(Class<? extends T> type) {
    if (proxy == null) {
      DelegatingInvocationHandler<T> invocationHandler = new DelegatingInvocationHandler<T>();
      Set<Class<?>> _interfaces = new HashSet<Class<?>>();
      interfacesFor(type, _interfaces);
      proxy = type.cast(Proxy.newProxyInstance(type.getClassLoader(),
          (Class[]) _interfaces.toArray(), invocationHandler));
    }

    return proxy;
  }

  /**
   * Whether the contextualized object is currently being constructed.
   * 
   * @return boolean
   */
  public boolean isConstructing() {
    return constructing;
  }

  /**
   * Removes the object.
   */
  public void removeObject() {
    object = null;
  }

  /**
   * Sets the object.
   * 
   * @param object New object
   */
  public void setObject(T object) {
    this.object = object;
  }

  /**
   * Sets the delegate for the proxy.
   * 
   * @param delegate New delegate
   */
  public void setProxyDelegate(T delegate) {
    if (invocationHandler != null)
      invocationHandler.setDelegate(delegate);
  }

  /**
   * Obtains all interfaces and superinterfaces for {@code pClass} storing them in
   * {@code pInterfaces}.
   * 
   * <p>
   * Note: {@code pInterfaces} must be pre-initialized.
   * 
   * @param type Class to obtain interfaces for
   * @param interfaces Interface storage
   */
  private void interfacesFor(Class<?> type, Set<Class<?>> interfaces) {
    Class<?> superClass = type.getSuperclass();
    if (superClass != null)
      interfacesFor(superClass, interfaces);
    for (Class<?> _interface : type.getInterfaces()) {
      interfaces.add(_interface);
      interfacesFor(_interface, interfaces);
    }
  }
}
