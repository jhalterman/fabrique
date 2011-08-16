package org.fabrique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabrique.ObjectFactory;
import org.fabrique.internal.BindingLoader;

/**
 * Binder factory testing utility methods.
 */
public class TestUtil {
  private static final String BINDING_LOADER_FIELD_NAME = "bindingLoader";
  private static BindingLoader bindingLoader;
  private static Method clearBindingsMethod;

  /**
   * Fails unless {@code object} doesn't equal itself when reserialized.
   */
  public static void assertEqualWhenReserialized(Object object) throws IOException {
    Object reserialized = reserialize(object);
    assertEquals(object, reserialized);
    assertEquals(object.hashCode(), reserialized.hashCode());
  }

  public static <E> E reserialize(E original) throws IOException {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      new ObjectOutputStream(out).writeObject(original);
      ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
      @SuppressWarnings("unchecked")
      E reserialized = (E) new ObjectInputStream(in).readObject();
      return reserialized;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Fails unless {@code expected.equals(actual)}, {@code actual.equals(expected)} and their hash
   * codes are equal. This is useful for testing the equals method itself.
   */
  public static void assertEqualsBothWays(Object expected, Object actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    assertTrue("expected.equals(actual)", expected.equals(actual));
    assertTrue("actual.equals(expected)", actual.equals(expected));
    assertEquals("hashCode", expected.hashCode(), actual.hashCode());
  }

  /**
   * Returns the protected factory binding loader.
   * 
   * @return IBindingLoader
   */
  public static BindingLoader getBindingLoader() {
    if (bindingLoader == null) {
      try {
        Field field = ObjectFactory.class.getDeclaredField(BINDING_LOADER_FIELD_NAME);
        field.setAccessible(true);
        bindingLoader = (BindingLoader) field.get(ObjectFactory.class);
      } catch (Exception e) {
        System.out.println(e);
      }
    }

    return bindingLoader;
  }

  /**
   * Resets the {@link ObjectFactory} bindings and method aspects.
   */
  public static void resetFactoryBindings() {
    try {
      if (clearBindingsMethod == null) {
        clearBindingsMethod = ObjectFactory.class.getDeclaredMethod("clearBindings");
        clearBindingsMethod.setAccessible(true);
      }

      clearBindingsMethod.invoke(ObjectFactory.class);
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
