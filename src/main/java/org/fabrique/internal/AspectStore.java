package org.fabrique.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains aspects.
 */
public final class AspectStore {
    /** Stores all method aspects */
    private static final List<MethodAspect> methodAspects = new ArrayList<MethodAspect>();

    private AspectStore() {
    }

    /**
     * Adds a method aspect to the store.
     * 
     * @param aspect MethodAspect
     */
    static void addMethodAspect(MethodAspect aspect) {
        methodAspects.add(aspect);
    }

    /**
     * Gets the aspects from the store.
     * 
     * @return Iterable<MethodAspect>
     */
    static Iterable<MethodAspect> methodAspects() {
        return methodAspects;
    }
}
