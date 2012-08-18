package org.jodah.fabrique;

/**
 * Represents an error having occurred when configuring or retrieving a binding.
 */
public class ConfigurationException extends RuntimeException {
  private static final long serialVersionUID = 0;

  /**
   * {@inheritDoc}
   */
  public ConfigurationException(String msg, Throwable parent) {
    super(msg, parent);
  }

  /**
   * {@inheritDoc}
   */
  public ConfigurationException(String msg) {
    super(msg);
  }

  /**
   * {@inheritDoc}
   */
  public ConfigurationException(Throwable parent) {
    super(parent);
  }
}
