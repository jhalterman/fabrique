package org.fabrique.intercept;

/**
 * This interface represents an invocation in the program.
 * 
 * <p>
 * An invocation is a joinpoint and can be intercepted by an interceptor.
 * 
 * @author AOP Alliance
 */
public interface Invocation extends Joinpoint {
    /**
     * Get the arguments as an array object. It is possible to change element values within this
     * array to change the arguments.
     * 
     * @return The argument of the invocation
     */
    Object[] getArguments();
}
