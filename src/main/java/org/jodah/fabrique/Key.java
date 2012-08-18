package org.jodah.fabrique;

import java.lang.annotation.Annotation;

import org.jodah.fabrique.internal.Validate;

/**
 * Encapsulates a binding key, used for storing and retrieving bindings from a factory.
 * 
 * @param <T> Bound type
 */
public class Key<T> {
  private final Class<T> type;
  private Object name;
  private final int hashCode;

  /**
   * Creates a new Key object.
   * 
   * @param pType Key type
   * @param pName Key name
   * @throws ConfigurationException if {@code pType} is null
   */
  private Key(Class<T> pType, Object pName) {
    Validate.notNull(pType, "Key type cannot be null");
    type = pType;
    name = pName;
    hashCode = computeHashCode();
  }

  /**
   * Gets a key for an injection type.
   * 
   * @param <T> Bound type
   * @param pType Key type
   * @return Key<T>
   * @throws ConfigurationException if {@code pType} is null
   */
  public static <T> Key<T> get(Class<T> pType) {
    return new Key<T>(pType, null);
  }

  /**
   * Gets a key for an injection type and meta information.
   * 
   * @param <T> Bound type
   * @param pType Key type
   * @param pName Key name
   * @return Key<T>
   * @throws ConfigurationException if {@code pType} is null
   */
  public static <T> Key<T> get(Class<T> pType, Object pName) {
    return new Key<T>(pType, pName);
  }

  /**
   * Gets a key for an injection type and an annotation.
   * 
   * @param <T> Bound type
   * @param pType Key type
   * @param pAnnotation Key name
   * @return Key<T>
   * @throws ConfigurationException if {@code pType} is null
   */
  public static <T> Key<T> get(Class<T> pType, Annotation pAnnotation) {
    return new Key<T>(pType, pAnnotation.annotationType());
  }

  /**
   * Gets the key name.
   * 
   * @return Object
   */
  public Object getName() {
    return name;
  }

  /**
   * Gets the key type.
   * 
   * @return Class<T>
   */
  public Class<T> getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int hashCode() {
    return hashCode;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object pOther) {
    if (pOther == this)
      return true;
    if (!(pOther instanceof Key<?>))
      return false;
    Key<?> _other = (Key<?>) pOther;
    return name != null ? type.equals(_other.getType()) && name.equals(_other.getName()) : type
        .equals(_other.getType());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return (name == null) ? type.toString() : (type.toString() + " as " + name);
  }

  /**
   * Computes a key hash.
   * 
   * @return int
   */
  private int computeHashCode() {
    return (name != null) ? ((type.hashCode() * 31) + name.hashCode()) : type.hashCode();
  }
}
