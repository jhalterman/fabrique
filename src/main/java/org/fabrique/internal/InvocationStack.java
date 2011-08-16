package org.fabrique.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.fabrique.intercept.IMethodInterceptor;
import org.fabrique.intercept.MethodInvocation;

/**
 * Handles method interception with a stack of interceptors, concerns and mixins.
 * 
 * @author Jonathan Halterman
 */
final class InvocationStack implements MethodInterceptor {
  final Method method;
  final IMethodInterceptor[] interceptors;

  /**
   * Creates a new InterceptorStackCallback object.
   * 
   * @param method Method to intercept
   * @param interceptors
   */
  InvocationStack(Method method, List<IMethodInterceptor> interceptors) {
    this.method = method;
    this.interceptors = interceptors.toArray(new IMethodInterceptor[interceptors.size()]);
  }

  /**
   * Basic method invocation implementation.
   */
  private class InterceptedMethodInvocation implements MethodInvocation {
    final MethodProxy methodProxy;
    final Object proxy;
    final Object[] arguments;
    int index = -1;

    /**
     * Creates a new InterceptedMethodInvocation object.
     * 
     * @param proxy .
     * @param methodProxy .
     * @param arguments .
     */
    public InterceptedMethodInvocation(Object proxy, MethodProxy methodProxy, Object[] arguments) {
      this.proxy = proxy;
      this.methodProxy = methodProxy;
      this.arguments = arguments;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getArguments() {
      return arguments;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
      return method;
    }

    /**
     * {@inheritDoc}
     */
    public AccessibleObject getStaticPart() {
      return getMethod();
    }

    /**
     * {@inheritDoc}
     */
    public Object getThis() {
      return proxy;
    }

    /**
     * {@inheritDoc}
     */
    public Object proceed() throws Throwable {
      try {
        index++;
        return index == interceptors.length ? methodProxy.invokeSuper(proxy, arguments)
            : interceptors[index].invoke(this);
      } finally {
        index--;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy)
      throws Throwable {
    return new InterceptedMethodInvocation(proxy, methodProxy, args).proceed();
  }
}
