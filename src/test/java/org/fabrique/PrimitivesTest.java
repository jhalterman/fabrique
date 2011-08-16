package org.fabrique;

import static org.junit.Assert.assertEquals;

import org.fabrique.AbstractModule;
import org.fabrique.ObjectFactory;
import org.fabrique.Inject;
import org.fabrique.Primitives;
import org.junit.Before;
import org.junit.Test;



/**
 * Tests the usage of primitives while performing injection and retrieving instances from the Factory.
 */
public class PrimitivesTest {
  /** Tests various injection of primitives */
  public static class TestClass {
    Integer testConstructorInteger;
    @Inject
    Integer testFieldInteger;
    Integer testMethodInteger;
    int testConstructorInt;
    @Inject
    int testFieldInt;
    int testMethodInt;

    /** Injected constructor */
    @Inject
    TestClass(int pTestInt) {
      testConstructorInt = pTestInt;
    }

    /** Optional injected constructor */
    @Inject(optional = true)
    TestClass(Integer pTestInteger) {
      testConstructorInteger = pTestInteger;
    }

    /** Injected setter */
    @Inject
    void setInt(int pTestInt) {
      testMethodInt = pTestInt;
    }

    /** Injected setter */
    @Inject
    void setInteger(Integer pTestInteger) {
      testMethodInteger = pTestInteger;
    }
  }

  /**
   * Performs setup.
   */
  @Before
  public void setup() {
    TestUtil.resetFactoryBindings();
  }

  /**
   * Tests that injection of primitives works as expected.
   */
  @Test
  public void testPrimitiveInjection() {
    final int _defaultInt = 3;
    final Integer _defaultInteger = new Integer(5);

    ObjectFactory.loadModules(new AbstractModule() {
        protected void configure() {
          bind(TestClass.class);
          bind(Integer.TYPE).toInstance(_defaultInt);
          bind(Integer.class).toInstance(_defaultInteger);
        }
      });

    TestClass _test = ObjectFactory.getInstance(TestClass.class);
    assertEquals(_defaultInt, _test.testFieldInt);
    assertEquals(_defaultInteger, _test.testFieldInteger);
    assertEquals(_defaultInt, _test.testConstructorInt);
    assertEquals(_defaultInt, _test.testMethodInt);
    assertEquals(_defaultInteger, _test.testMethodInteger);

    int _overrideInt = 6;

    // Primitives.of() was not used so the Integer constructor is called
    _test = ObjectFactory.getInstance(TestClass.class, _overrideInt);
    assertEquals(0, _test.testConstructorInt);
    assertEquals(new Integer(_overrideInt), _test.testConstructorInteger);

    // The int constructor is called
    _test = ObjectFactory.getInstance(TestClass.class, Primitives.of(_overrideInt));
    assertEquals(_overrideInt, _test.testConstructorInt);

    // The Integer constructor is called
    Integer _overrideInteger = new Integer(2);
    _test = ObjectFactory.getInstance(TestClass.class, _overrideInteger);
    assertEquals(_overrideInteger, _test.testConstructorInteger);
  }
}
