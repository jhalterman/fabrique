package org.fabrique.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with aspects.
 */
final class Aspects {
    private Aspects() {
    }

    /**
     * Returns matching method aspects for {@code type}.
     * 
     * @param type Type to find matching aspects for
     * @return List of matching MethodAspects
     */
    static List<MethodAspect> matchesFor(Class<?> type) {
        List<MethodAspect> matchingAspects = new ArrayList<MethodAspect>();

        for (MethodAspect _methodAspect : AspectStore.methodAspects()) {
            if (_methodAspect.matches(type)) {
                matchingAspects.add(_methodAspect);
            }
        }

        return matchingAspects;
    }
}
