package org.jodah.fabrique.intercept;

/**
 * Intercepts calls on an interface on its way to the target. These are nested "on top" of the
 * target.
 * 
 * <p>
 * The user should implement the invoke(IMethodInvocation) method to modify the original behavior.
 * 
 * <p>
 * Ex: The following class implements a tracing interceptor (traces all the calls on the intercepted
 * method(s)):
 * 
 * <pre>
 * class TracingInterceptor implements IMethodInterceptor {
 *    public Object invoke(MethodInvocation i) throws Throwable {
 *      System.out.println("method "+i.getMethod()+" is called on "+i.getThis()+" with args "+i.getArguments());
 *      Object _ret=i.proceed();
 *      System.out.println("method "+i.getMethod()+" returns "+ret);
 *      return _ret;
 *    }
 * }</pre>
 * 
 * @author AOP Alliance
 */
public interface IMethodInterceptor extends Interceptor {
  /**
   * Implement this method to perform extra treatments before and after the invocation. Polite
   * implementations would certainly like to invoke {@link Joinpoint.proceed()}.
   * 
   * @param pInvocation The method invocation joinpoint
   * @return The result of the call to {@link Joinpoint.proceed()}, might be intercepted by the
   *         interceptor.
   * @throws Throwable If the interceptors or the target-object throws an exception.
   */
  Object invoke(MethodInvocation pInvocation) throws Throwable;
}
