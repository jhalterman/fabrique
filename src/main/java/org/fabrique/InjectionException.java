package org.fabrique;

/**
 * Represents an error having occured while attempting to perform a dependency injection.
 */
public class InjectionException extends RuntimeException {
  private static final long serialVersionUID = 0;

  /**
   * Creates a new InjectionException object.
   * 
   * @param pMsg Error message
   */
  public InjectionException(String pMsg) {
    super(pMsg);
  }

  /**
   * Creates a new InjectionException object.
   * 
   * @param pMsg Error message
   * @param pParent Parent exception
   */
  public InjectionException(String pMsg, Throwable pParent) {
    super(pMsg, pParent);
  }
}
