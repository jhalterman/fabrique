package org.jodah.fabrique.internal;

/**
 * Defines behavior for an injector that injects member references.
 */
public interface MemberInjector {
  /**
   * Performs a member injection for {@code object}.
   * 
   * @param context Injection Context
   * @param object Object to perform injection for
   * @throws InjectionException If {@code pObject} or any dependencies could not be injected
   */
  void inject(InjectionContext context, Object object);
}
