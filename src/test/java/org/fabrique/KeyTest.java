package org.fabrique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Tests {@link com.dorado.common.binder.Key}.
 */
public class KeyTest {
  /**
   * Tests that keys are equal as expected.
   */
  @Test
  public void testKeyEquals() {
    String _msg = "Keys should be equal";

    Key _list1 = Key.get(List.class);
    Key _list2 = Key.get(List.class);

    assertEquals(_msg, _list1, _list2);

    String _name = "NAME";
    _list1 = Key.get(List.class, _name);
    _list2 = Key.get(List.class, _name);

    assertEquals(_msg, _list1, _list2);

    Object _nameObj = new Object();
    _list1 = Key.get(List.class, _nameObj);
    _list2 = Key.get(List.class, _nameObj);

    assertEquals(_msg, _list1, _list2);
  }

  /**
   * Verifies that a null type cannot be used to produce a key.
   */
  @Test(expected = ConfigurationException.class)
  public void testKeyForNullType() {
    Key.get(null);
  }

  /**
   * Tests that keys are not equal as expected.
   */
  @Test
  public void testKeyNotEqual() {
    String _msg = "Keys should not be equal";
    String _name = "NAME";
    String _name1 = "NAME1";
    Object _obj1 = new Object();
    Object _obj2 = new Object();

    Key _list1 = Key.get(List.class);
    Key _list2 = Key.get(Map.class);

    assertNotSame(_msg, _list1, _list2);

    _list1 = Key.get(List.class, _name);
    _list2 = Key.get(Map.class, _name);

    assertNotSame(_msg, _list1, _list2);

    _list1 = Key.get(List.class, _name);
    _list2 = Key.get(List.class, _name1);

    assertNotSame(_msg, _list1, _list2);

    _list1 = Key.get(List.class, _obj1);
    _list2 = Key.get(List.class, _obj2);

    assertNotSame(_msg, _list1, _list2);
  }
}
