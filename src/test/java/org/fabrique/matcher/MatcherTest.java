package org.fabrique.matcher;

import static org.fabrique.TestUtil.assertEqualWhenReserialized;
import static org.fabrique.TestUtil.assertEqualsBothWays;
import static org.fabrique.matcher.Matchers.annotatedWith;
import static org.fabrique.matcher.Matchers.any;
import static org.fabrique.matcher.Matchers.identicalTo;
import static org.fabrique.matcher.Matchers.inPackage;
import static org.fabrique.matcher.Matchers.inSubpackage;
import static org.fabrique.matcher.Matchers.isMethod;
import static org.fabrique.matcher.Matchers.not;
import static org.fabrique.matcher.Matchers.only;
import static org.fabrique.matcher.Matchers.returns;
import static org.fabrique.matcher.Matchers.subclassesOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;

import org.fabrique.Named;
import org.fabrique.matcher.Matcher;
import org.fabrique.matcher.Matchers;
import org.junit.Test;

/**
 * @author crazybob@google.com (Bob Lee)
 * @author Jonathan Halterman
 */
public class MatcherTest {
  @interface Baz {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Foo {
  }

  /** Test runnable class. */
  abstract static class MyRunnable implements Runnable {
  }

  /**  */
  @Foo
  static class Bar {
  }

  /** */
  @Baz
  static class Car {
  }

  /**
   * Tests the and matches.
   */
  @Test
  public void testAnd() {
    assertTrue(any().and(any()).matches(null));
    assertFalse(any().and(not(any())).matches(null));
    assertEquals("and(any(), any())", any().and(any()).toString());
    assertEqualsBothWays(any().and(any()), any().and(any()));
    assertFalse(any().and(any()).equals(not(any())));
  }

  /**
   * Tests the annotated with matcher.
   */
  @Test
  public void testAnnotatedWith() {
    assertTrue(annotatedWith(Foo.class).matches(Bar.class));
    assertFalse(annotatedWith(Foo.class).matches(MatcherTest.class.getMethods()[0]));
    assertEquals("annotatedWith(Foo.class)", annotatedWith(Foo.class).toString());
    assertEqualsBothWays(annotatedWith(Foo.class), annotatedWith(Foo.class));
    assertFalse(annotatedWith(Foo.class).equals(annotatedWith(Named.class)));

    try {
      annotatedWith(Baz.class);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  /**
   * Tests the any matcher.
   */
  @Test
  public void testAny() {
    assertTrue(any().matches(null));
    assertEquals("any()", any().toString());
    assertEqualsBothWays(any(), any());
    assertFalse(any().equals(not(any())));
  }

  /**
   * Tests the identical to matcher.
   */
  @Test
  public void testIdenticalTo() {
    Object o = new Object();
    assertEquals("identicalTo(1)", identicalTo(1).toString());
    assertTrue(identicalTo(o).matches(o));
    assertFalse(identicalTo(o).matches(new Object()));
    assertEqualsBothWays(identicalTo(o), identicalTo(o));
    assertFalse(identicalTo(1).equals(identicalTo(new Integer(1))));
  }

  /**
   * Tests the in package matcher.
   */
  @Test
  public void testInPackage() {
    Package matchersPackage = Matchers.class.getPackage();
    assertEquals("inPackage(org.fabrique.matcher)", inPackage(matchersPackage)
        .toString());
    assertTrue(inPackage(matchersPackage).matches(MatcherTest.class));
    assertFalse(inPackage(matchersPackage).matches(Object.class));
    assertEqualsBothWays(inPackage(matchersPackage), inPackage(matchersPackage));
    assertFalse(inPackage(matchersPackage).equals(inPackage(Object.class.getPackage())));
  }

  /**
   * Tests the in subpackage matcher.
   */
  @Test
  public void testInSubpackage() {
    String stringPackageName = String.class.getPackage().getName();
    assertEquals("inSubpackage(java.lang)", inSubpackage(stringPackageName).toString());
    assertTrue(inSubpackage(stringPackageName).matches(Object.class));
    assertTrue(inSubpackage(stringPackageName).matches(Method.class));
    assertFalse(inSubpackage(stringPackageName).matches(Matchers.class));
    assertFalse(inSubpackage("jav").matches(Object.class));
    assertEqualsBothWays(inSubpackage(stringPackageName), inSubpackage(stringPackageName));
    assertFalse(inSubpackage(stringPackageName).equals(
        inSubpackage(Matchers.class.getPackage().getName())));
  }

  /**
   * Tests the method matcher.
   */
  @Test
  public void testIsMethod() throws NoSuchMethodException {
    Matcher<Method> predicate = isMethod("toString");
    assertTrue(predicate.matches(Object.class.getMethod("toString")));
    assertFalse(predicate.matches(Object.class.getMethod("hashCode")));
    assertEquals("method(toString)", isMethod("toString").toString());
    assertEqualsBothWays(predicate, isMethod("toString"));
    assertFalse(predicate.equals(isMethod("hashCode")));

    Class<?>[] _addAllParams = new Class<?>[] { Integer.TYPE, Collection.class };
    predicate = isMethod("addAll", _addAllParams);
    assertTrue(predicate.matches(ArrayList.class.getMethod("addAll", _addAllParams)));
    assertFalse(predicate.matches(Object.class.getMethod("hashCode")));
  }

  /**
   * Tests the not matcher.
   */
  @Test
  public void testNot() {
    assertFalse(not(any()).matches(null));
    assertEquals("not(any())", not(any()).toString());
    assertEqualsBothWays(not(any()), not(any()));
    assertFalse(not(any()).equals(any()));
  }

  /**
   * Tests the only matcher.
   */
  @Test
  public void testOnly() {
    assertTrue(only(1000).matches(1000));
    assertFalse(only(1).matches(1000));
    assertEquals("only(1)", only(1).toString());
    assertEqualsBothWays(only(1), only(1));
    assertFalse(only(1).equals(only(2)));
  }

  /**
   * Tests the or matcher.
   */
  @Test
  public void testOr() {
    assertTrue(any().or(not(any())).matches(null));
    assertFalse(not(any()).or(not(any())).matches(null));
    assertEquals("or(any(), any())", any().or(any()).toString());
    assertEqualsBothWays(any().or(any()), any().or(any()));
    assertFalse(any().or(any()).equals(not(any())));
  }

  /**
   * Tests the returns matcher.
   * 
   * @throws NoSuchMethodException .
   */
  @Test
  public void testReturns() throws NoSuchMethodException {
    Matcher<Method> predicate = returns(only(String.class));
    assertTrue(predicate.matches(Object.class.getMethod("toString")));
    assertFalse(predicate.matches(Object.class.getMethod("hashCode")));
    assertEquals("returns(only(class java.lang.String))", returns(only(String.class)).toString());
    assertEqualsBothWays(predicate, returns(only(String.class)));
    assertFalse(predicate.equals(returns(only(Integer.class))));
  }

  /**
   * Tests the serialization matcher.
   * 
   * @throws IOException .
   */
  @Test
  public void testSerialization() throws IOException {
    assertEqualWhenReserialized(any());
    assertEqualWhenReserialized(not(any()));
    assertEqualWhenReserialized(annotatedWith(Named.class));
    assertEqualWhenReserialized(only("foo"));
    assertEqualWhenReserialized(identicalTo(Object.class));
    assertEqualWhenReserialized(inPackage(String.class.getPackage()));
    assertEqualWhenReserialized(inSubpackage(String.class.getPackage().getName()));
    assertEqualWhenReserialized(returns(any()));
    assertEqualWhenReserialized(subclassesOf(AbstractList.class));
    assertEqualWhenReserialized(only("a").or(only("b")));
    assertEqualWhenReserialized(only("a").and(only("b")));
  }

  /**
   * Tests the subclass of matcher.
   */
  @Test
  public void testSubclassesOf() {
    assertTrue(subclassesOf(Runnable.class).matches(Runnable.class));
    assertTrue(subclassesOf(Runnable.class).matches(MyRunnable.class));
    assertFalse(subclassesOf(Runnable.class).matches(Object.class));
    assertEquals("subclassesOf(Runnable.class)", subclassesOf(Runnable.class).toString());
    assertEqualsBothWays(subclassesOf(Runnable.class), subclassesOf(Runnable.class));
    assertFalse(subclassesOf(Runnable.class).equals(subclassesOf(Object.class)));
  }
}
