package org.jodah.fabrique;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jodah.fabrique.AbstractModule;
import org.jodah.fabrique.BindingAnnotation;
import org.jodah.fabrique.Inject;
import org.jodah.fabrique.Named;
import org.jodah.fabrique.ObjectFactory;
import org.jodah.fabrique.Provider;
import org.jodah.fabrique.Scopes;
import org.jodah.fabrique.Scopes.SimpleScope;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple Fabrique examples.
 */
public class Examples {
  /**
   * Performs setup.
   */
  @Before
  public void setup() {
    TestUtil.resetFactoryBindings();
  }

  public static class ExampleModule1 extends AbstractModule {
    protected void configure() {
      bind(List.class).to(ArrayList.class);
      bind(List.class).as("THREADSAFE").to(Vector.class);
      bind(String.class).toInstance("test");
    }
  }

  /**
   * Tests the use of binding to a type, a named binding, and binding to an instance.
   */
  @Test
  public void example1() {
    ObjectFactory.loadModules(new ExampleModule1());

    List testList = ObjectFactory.getInstance(List.class);
    assertTrue(testList instanceof ArrayList);

    List testList1 = ObjectFactory.getNamedInstance(List.class, "THREADSAFE");
    assertTrue(testList1 instanceof Vector);

    String testString = ObjectFactory.getInstance(String.class);
    assertEquals(testString, "test");
  }

  /** ******************************************************************************* */

  public static class ListProvider implements Provider<List> {
    public List get() {
      return new ArrayList();
    }
  }

  public static class ExampleModule2 extends AbstractModule {
    protected void configure() {
      bind(List.class).toProvider(ListProvider.class);
    }
  }

  /**
   * Tests the use of a provider.
   */
  @Test
  public void example2() {
    ObjectFactory.loadModules(new ExampleModule2());

    List testList = ObjectFactory.getInstance(List.class);
    assertTrue(testList instanceof ArrayList);
  }

  /** ******************************************************************************* */

  public static class TestClass {
    List list;
    String string;
    Integer integer;

    public TestClass(List list) {
      this.list = list;
    }

    public TestClass(String string) {
      this.string = string;
    }

    public TestClass(String string, Integer integer) {
      this.string = string;
      this.integer = integer;
    }
  }

  public static class ExampleModule3 extends AbstractModule {
    protected void configure() {
      bind(TestClass.class).forParams(List.class).forOptionalParams(String.class)
          .forOptionalParams(String.class, Integer.class);
      bind(List.class).to(ArrayList.class);
    }
  }

  /**
   * Tests the use of binding parameters.
   */
  @Test
  public void example3() {
    ObjectFactory.loadModules(new ExampleModule3());

    TestClass _testObject = ObjectFactory.getInstance(TestClass.class);
    assertTrue(_testObject.list instanceof ArrayList);

    List _testList = new ArrayList();
    TestClass _testObject1 = ObjectFactory.getInstance(TestClass.class, _testList);
    assertSame(_testObject1.list, _testList);

    String _testString = "test";
    TestClass _testObject2 = ObjectFactory.getInstance(TestClass.class, _testString);
    assertEquals(_testObject2.string, _testString);

    Integer _testInteger = 5;
    TestClass _testObject3 = ObjectFactory.getInstance(TestClass.class, _testString, _testInteger);
    assertEquals(_testObject3.string, _testString);
    assertEquals(_testObject3.integer, _testInteger);
  }

  /** ******************************************************************************* */

  public static class StringProvider implements Provider<String> {
    public String get() {
      return null;
    }

    public String get(Integer pInt) {
      return "Int" + pInt;
    }

    public String get(String pStr) {
      return "String" + pStr;
    }
  }

  public static class ExampleModule4 extends AbstractModule {
    protected void configure() {
      bind(String.class).toInstance("test");
      bind(Object.class).toProvider(StringProvider.class).forParams(String.class);
    }
  }

  /**
   * Tests the use of a provider with parameters.
   */
  @Test
  public void example4() {
    ObjectFactory.loadModules(new ExampleModule4());

    Object _testObject = ObjectFactory.getInstance(Object.class);
    assertEquals(_testObject, "String" + "test");

    Object _testObject1 = ObjectFactory.getInstance(Object.class, 5);
    assertEquals(_testObject1, "Int" + 5);

    Object _testObject2 = ObjectFactory.getInstance(Object.class, "aaaa");
    assertEquals(_testObject2, "String" + "aaaa");
  }

  /** ******************************************************************************* */

  public static class ExampleModule5 extends AbstractModule {
    protected void configure() {
      bind(Collection.class).to(Vector.class).asSingleton();
      bind(List.class).to(ArrayList.class).in(Scopes.SIMPLE);
    }
  }

  /**
   * Tests the use of scoped bindings.
   */
  @Test
  public void example5() {
    ObjectFactory.loadModules(new ExampleModule5());

    Collection _testCollection1 = ObjectFactory.getInstance(Collection.class);
    Collection _testCollection2 = ObjectFactory.getInstance(Collection.class);
    assertSame(_testCollection1, _testCollection2);

    List _testList1 = ObjectFactory.getInstance(List.class);
    List _testList2 = ObjectFactory.getInstance(List.class);
    assertSame(_testList1, _testList2);

    SimpleScope.reset();
    List _testList3 = ObjectFactory.getInstance(List.class);
    assertNotSame(_testList1, _testList3);
  }

  /** ******************************************************************************* */

  public static class TestInjectClass {
    List list;
    Integer integer;
    @Inject
    Set set;

    @Inject
    public TestInjectClass(List pList) {
      list = pList;
    }

    @Inject(optional = true)
    // same as .forOptionalParams(Set.class);
    public TestInjectClass(Set pSet) {
      set = pSet;
    }

    @Inject
    public void setInteger(Integer pInteger) {
      integer = pInteger;
    }
  }

  public static class ExampleModule6 extends AbstractModule {
    protected void configure() {
      bind(List.class).to(ArrayList.class);
      bind(Set.class).to(HashSet.class);
      bind(Integer.class).toInstance(5);
      bind(TestInjectClass.class);
    }
  }

  /**
   * Tests the use of injection.
   */
  @Test
  public void example6() {
    ObjectFactory.loadModules(new ExampleModule6());

    TestInjectClass _test = ObjectFactory.getInstance(TestInjectClass.class);

    assertTrue(_test.list instanceof ArrayList);
    assertTrue(_test.set instanceof Set);
    assertEquals(_test.integer, new Integer(5));

    HashSet _set = new HashSet();
    ObjectFactory.getInstance(TestInjectClass.class, _set);
  }

  /** ******************************************************************************* */

  public static class TestNamedInjectClass {
    List list;

    @Inject
    public TestNamedInjectClass(@Named("THREADSAFE") List pList) {
      list = pList;
    }
  }

  public static class ExampleModule7 extends AbstractModule {
    protected void configure() {
      bind(List.class).as("THREADSAFE").to(Vector.class);
      bind(TestNamedInjectClass.class);
    }
  }

  /**
   * Tests named injection.
   */
  @Test
  public void example7() {
    ObjectFactory.loadModules(new ExampleModule7());

    TestNamedInjectClass _test = ObjectFactory.getInstance(TestNamedInjectClass.class);
    assertTrue(_test.list instanceof Vector);
  }

  /** ******************************************************************************* */

  @Target({ FIELD })
  @Retention(RUNTIME)
  @BindingAnnotation
  public @interface Threadsafe {
  }

  public static class TestAnnotatedInjectClass {
    @Inject
    List list;
    @Inject
    @Threadsafe
    List safeList;
  }

  public static class ExampleModule8 extends AbstractModule {
    protected void configure() {
      bind(List.class).to(ArrayList.class);
      bind(List.class).as(Threadsafe.class).to(Vector.class);
      bind(TestAnnotatedInjectClass.class);
    }
  }

  /**
   * Tests the use of a named annotation.
   */
  @Test
  public void example8() {
    ObjectFactory.loadModules(new ExampleModule8());

    TestAnnotatedInjectClass _test = ObjectFactory.getInstance(TestAnnotatedInjectClass.class);
    assertTrue(_test.list instanceof ArrayList);
    assertTrue(_test.safeList instanceof Vector);
  }

  /** ******************************************************************************* */

  interface IBuilding {
    IRoom getRoom();
  }

  interface IRoom {
    IAppliance getAppliance();
  }

  interface IAppliance {
    Color getColor();
  }

  public static class House implements IBuilding {
    IRoom room;

    @Inject
    public House(IRoom pRoom) {
      room = pRoom;
    }

    public IRoom getRoom() {
      return room;
    }
  }

  public static class Kitchen implements IRoom {
    @Inject
    IAppliance appliance;

    public IAppliance getAppliance() {
      return appliance;
    }
  }

  public static class Sink implements IAppliance {
    Color color;

    @Inject
    public void setColor(@Named("Dark") Color pColor) {
      color = pColor;
    }

    public Color getColor() {
      return color;
    }
  }

  public static class ExampleModule9 extends AbstractModule {
    protected void configure() {
      bind(IBuilding.class).to(House.class);
      bind(IRoom.class).to(Kitchen.class);
      bind(IAppliance.class).to(Sink.class);
      bind(Color.class).as("Light").toInstance(Color.WHITE);
      bind(Color.class).as("Dark").toInstance(Color.GRAY);
    }
  }

  /**
   * Tests nested injection.
   */
  @Test
  public void example9() {
    ObjectFactory.loadModules(new ExampleModule9());

    IBuilding _building = ObjectFactory.getInstance(IBuilding.class);

    assertTrue(_building.getRoom() instanceof Kitchen);
    assertTrue(_building.getRoom().getAppliance() instanceof Sink);
    assertEquals(_building.getRoom().getAppliance().getColor(), Color.GRAY);
  }
}
