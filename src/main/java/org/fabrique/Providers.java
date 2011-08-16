package org.fabrique;

/**
 * Utility methods for creating and working with providers.
 */
public final class Providers {
  /**
   * Creates a new Providers object.
   */
  private Providers() {
  }

  /**
   * Creates a provider for {@code T} providing {@code instance}.
   * 
   * @param <T> Provided type
   * @param instance Instance
   * @return T
   */
  public static <T> Provider<T> of(final T instance) {
    return new Provider<T>() {
      public T get() {
        return instance;
      }

      @Override
      public String toString() {
        return "of(" + instance + ")";
      }
    };
  }
}
