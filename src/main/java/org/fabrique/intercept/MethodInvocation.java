package org.fabrique.intercept;

import java.lang.reflect.Method;

/**
 * Description of an invocation to a method, given to an interceptor upon method-call.
 * 
 * <p>
 * A method invocation is a joinpoint and can be intercepted by a method interceptor.
 * 
 * @author AOP Alliance
 */
public interface MethodInvocation extends Invocation {
    /**
     * Gets the method being called.
     * 
     * @return The method being called.
     */
    Method getMethod();
}
