package org.jodah.fabrique;

import static org.junit.Assert.assertTrue;

import java.util.AbstractList;
import java.util.Map;

import org.jodah.fabrique.ConfigurationException;
import org.jodah.fabrique.Inject;
import org.jodah.fabrique.ObjectFactory;
import org.junit.Test;

/**
 * Tests the creation of JIT bindings.
 */
public class JitBindingTest {
  /** */
  static class CircularClassA {
    @Inject
    CircularClassB b;
  }

  /** */
  static class CircularClassB {
    @Inject
    CircularClassA a;
  }

  /**
   * Tests that retrieving a JIT abstract class is not allowed.
   */
  @Test(expected = ConfigurationException.class)
  public void testBindToAbstractClass() {
    ObjectFactory.getInstance(AbstractList.class);
  }

  /**
   * Tests that retrieving a JIT interface is not allowed.
   */
  @Test(expected = ConfigurationException.class)
  public void testBindToInterface() {
    ObjectFactory.getInstance(Map.class);
  }

  /**
   * Tests that circular dependencies are handled.
   */
  public void testCircularDependency() {
    assertTrue(ObjectFactory.getInstance(CircularClassA.class).b instanceof CircularClassB);
  }
}
