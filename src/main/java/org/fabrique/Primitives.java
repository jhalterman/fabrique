package org.fabrique;

/**
 * Utility methods for working with primitives.
 */
public final class Primitives {
  private Primitives() {
  }

  /**
   * Encapsulates a primitive value.
   */
  public static class Primitive {
    private final Class<?> primitiveType;
    private final Object primitive;

    /**
     * Creates a new Primitive object.
     * 
     * @param primitive Primitive boxed object
     * @param primitiveType Primitive type
     */
    private Primitive(Object primitive, Class<?> primitiveType) {
      this.primitive = primitive;
      this.primitiveType = primitiveType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
      return object.equals(primitive) && object.getClass().equals(primitiveType);
    }

    /**
     * Returns the boxed primitive.
     * 
     * @return Object
     */
    public Object getPrimitive() {
      return primitive;
    }

    /**
     * Returns the primitive type.
     * 
     * @return Class<?>
     */
    public Class<?> getType() {
      return primitiveType;
    }
  }

  /**
   * Gets the non-primitive class for the primitive class {@code primitiveClass}.
   * 
   * @param primitiveType Primitive type
   * @return Class<?>
   * @throws ConfigurationException if {@code primitiveClass} cannot be converted to a non-primitive
   */
  public static Class<?> classFor(Class<?> primitiveType) {
    if (primitiveType.equals(Integer.TYPE))
      return Integer.class;
    else if (primitiveType.equals(Boolean.TYPE))
      return Boolean.class;
    else if (primitiveType.equals(Long.TYPE))
      return Long.class;
    else if (primitiveType.equals(Character.TYPE))
      return Character.class;
    else if (primitiveType.equals(Byte.TYPE))
      return Byte.class;
    else if (primitiveType.equals(Short.TYPE))
      return Short.class;
    else if (primitiveType.equals(Float.TYPE))
      return Float.class;
    else if (primitiveType.equals(Double.TYPE))
      return Double.class;
    return null;
  }

  /**
   * Converts an array of potential {@link Primitive} object to an array of boxed primitive objects.
   * 
   * @param args Objects to convert
   * @return Object[]
   */
  public static Object[] convertPrimitives(Object[] args) {
    Object[] result = new Object[args.length];
    for (int i = 0; i < result.length; i++)
      if (args[i] instanceof Primitive)
        result[i] = ((Primitive) args[i]).getPrimitive();
      else
        result[i] = args[i];
    return result;
  }

  /**
   * Obtains a {@link Primitive} object for the value {@code primitive}.
   * 
   * @param primitive Value to obtain primitive for
   * @return Object
   */
  public static Object of(int primitive) {
    return new Primitive(primitive, Integer.TYPE);
  }

  /**
   * Obtains a {@link Primitive} object for the value {@code primitive}.
   * 
   * @param primitive Value to obtain primitive for
   * @return Object
   */
  public static Object of(boolean primitive) {
    return new Primitive(primitive, Boolean.TYPE);
  }

  /**
   * Obtains a {@link Primitive} object for the value {@code primitive}.
   * 
   * @param primitive Value to obtain primitive for
   * @return Object
   */
  public static Object of(long primitive) {
    return new Primitive(primitive, Long.TYPE);
  }

  /**
   * Obtains a {@link Primitive} object for the value {@code primitive}.
   * 
   * @param primitive Value to obtain primitive for
   * @return Object
   */
  public static Object of(char primitive) {
    return new Primitive(primitive, Character.TYPE);
  }

  /**
   * Obtains a {@link Primitive} object for the value {@code primitive}.
   * 
   * @param primitive Value to obtain primitive for
   * @return Object
   */
  public static Object of(byte primitive) {
    return new Primitive(primitive, Byte.TYPE);
  }

  /**
   * Obtains a {@link Primitive} object for the value {@code primitive}.
   * 
   * @param primitive Value to obtain primitive for
   * @return Object
   */
  public static Object of(short primitive) {
    return new Primitive(primitive, Short.TYPE);
  }

  /**
   * Obtains a {@link Primitive} object for the value {@code primitive}.
   * 
   * @param primitive Value to obtain primitive for
   * @return Object
   */
  public static Object of(float primitive) {
    return new Primitive(primitive, Float.TYPE);
  }

  /**
   * Obtains a {@link Primitive} object for the value {@code primitive}.
   * 
   * @param primitive Value to obtain primitive for
   * @return Object
   */
  public static Object of(double primitive) {
    return new Primitive(primitive, Double.TYPE);
  }
}
