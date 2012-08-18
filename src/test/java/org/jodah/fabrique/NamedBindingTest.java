package org.jodah.fabrique;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.jodah.fabrique.AbstractModule;
import org.jodah.fabrique.BindingAnnotation;
import org.jodah.fabrique.ConfigurationException;
import org.jodah.fabrique.Inject;
import org.jodah.fabrique.Named;
import org.jodah.fabrique.ObjectFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the creation of various named bindings.
 */
public class NamedBindingTest {
  @Target(PARAMETER)
  @Retention(RUNTIME)
  public @interface InvalidName {
  }

  @Target({ PARAMETER, FIELD })
  @Retention(RUNTIME)
  @BindingAnnotation
  public @interface Threadsafe {
  }

  @Target(PARAMETER)
  @Retention(RUNTIME)
  @BindingAnnotation
  public @interface Transactional {
  }

  /** Tests named injection */
  static class TestClass {
    Collection<?> collection;
    Collection<?> otherCollection;
    @Inject
    @Threadsafe
    Collection<?> threadsafeCollection;
    Collection<?> uniqueCollection;

    /** Injected constructor */
    @Inject
    TestClass(Collection<?> pCollection) {
      collection = pCollection;
    }

    /** Injected setter */
    @Inject
    void setCollection(@Named("Unique") Collection<?> pCollection) {
      uniqueCollection = pCollection;
    }

    /** Injected setter */
    @Inject
    void setCollection2(@Threadsafe Collection<?> pCollection) {
      otherCollection = pCollection;
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
   * Tests that a binding annotation is invalid if it lacks @BindingAnnotation.
   */
  @Test(expected = ConfigurationException.class)
  public void testInvalidNameAnnotation() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).as(InvalidName.class);
      }
    });
  }

  /**
   * Tests that a binding with a named annotation.
   */
  @Test
  public void testNamedAnnotationBinding() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(List.class).to(ArrayList.class);
        bind(List.class).as(Threadsafe.class).to(Vector.class);
      }
    });

    assertTrue(ObjectFactory.getInstance(List.class) instanceof ArrayList);
    assertTrue(ObjectFactory.getNamedInstance(List.class, Threadsafe.class) instanceof Vector);
  }

  /**
   * Tests that basic named injection works as expected.
   */
  @Test
  public void testNamedInjection() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(TestClass.class);
        bind(Collection.class).to(ArrayList.class);
        bind(Collection.class).as(Threadsafe.class).to(Vector.class);
        bind(Collection.class).as("Unique").to(HashSet.class);
      }
    });

    TestClass _test = ObjectFactory.getInstance(TestClass.class);
    assertTrue(_test.collection instanceof ArrayList);
    assertTrue(_test.threadsafeCollection instanceof Vector);
    assertTrue(_test.otherCollection instanceof Vector);
    assertTrue(_test.uniqueCollection instanceof HashSet);
  }

  /**
   * Tests that a binding with a primitive name works as expected.
   */
  @Test
  public void testPrimitiveNamedBinding() {
    final int _testInt1 = 5;
    final int _testInt2 = 9;

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(List.class).as(_testInt1).to(ArrayList.class);
        bind(List.class).as(_testInt2).to(Vector.class);
      }
    });

    assertTrue(ObjectFactory.getNamedInstance(List.class, _testInt1) instanceof ArrayList);
    assertTrue(ObjectFactory.getNamedInstance(List.class, _testInt2) instanceof Vector);
  }
}
