package org.jodah.fabrique.matcher;

/**
 * Returns {@code true} or {@code false} for a given input.
 * 
 * @author Bob Lee (crazybob@google.com)
 * @param <T> Type to match
 */
public interface Matcher<T> {
  /**
   * Returns a new matcher which returns {@code true} if both this and the given matcher return
   * {@code true}.
   * 
   * @param other Matcher to AND
   * @return ANDed matcher
   */
  Matcher<T> and(Matcher<? super T> other);

  /**
   * Returns {@code true} if this matches {@code t}, {@code false} otherwise.
   * 
   * @param other Comparison object
   */
  boolean matches(T other);

  /**
   * Returns a new matcher which returns {@code true} if either this or the given matcher return
   * {@code true}.
   * 
   * @param other Matcher to OR
   * @return ORed matcher
   */
  Matcher<T> or(Matcher<? super T> pOther);
}
