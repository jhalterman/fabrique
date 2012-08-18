package org.jodah.fabrique;

/**
 * Configures and creates bindings.
 * 
 * <p>
 * See {@link Binder} for information on creating bindings.
 * <p>
 * See {@link Inject} for information on defining injection points.
 */
public interface Module {
  /** Matches a no argument constructor or method */
  public static final Class<?>[] NO_PARAMS = new Class<?>[] {};

  /**
   * Configures bindings for a factory.
   * 
   * <p>
   * <strong>Do not invoke this method directly</strong> to install sub-modules. Instead use
   * {@link Binder#install(Module)}.
   * 
   * @param binder Binder to use for creating bindings
   */
  void configure(Binder binder);
}
