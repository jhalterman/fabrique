package org.jodah.fabrique.internal;

import java.util.HashSet;
import java.util.Set;

/**
 * Maintains context while performing injection. Used to track circular dependencies.
 * 
 * <p>
 * See {@link ConstructionContext} for additional behavior with resolving circular constructor
 * dependencies.
 */
public final class InjectionContext {
  private Set<Class<?>> constructing;

  /**
   * Marks {@code type} as currently being constructed.
   * 
   * @param type Type to mark as being constructed
   * @return boolean False if {@code type} is currently being constructed.
   */
  public boolean constructing(Class<?> type) {
    if (constructing == null)
      constructing = new HashSet<Class<?>>();
    return constructing.add(type);
  }

  /**
   * Marks {@code type} as finished being constructed.
   * 
   * @param type Finished type
   */
  public void finished(Class<?> type) {
    constructing.remove(type);
  }
}
