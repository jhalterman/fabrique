package org.fabrique.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic MultiMap implementation using a lazy ArrayList to store values.
 * 
 * @param <K> Key type
 * @param <V> Value type
 */
public class MultiMap<K, V> {
  private Map<K, List<V>> map = new HashMap<K, List<V>>();

  /**
   * Clears the multimap.
   */
  public void clear() {
    map.clear();
  }

  /**
   * Gets the list of values for the given key. If no values exist for the key null is returned.
   * 
   * @param key Key
   * @return List of values
   */
  public List<V> get(K key) {
    return map.get(key);
  }

  /**
   * Returns the keys.
   * 
   * @return Iterable keys
   */
  public Iterable<K> keys() {
    return map.keySet();
  }

  /**
   * Adds a value to the map for the specified key.
   * 
   * @param key Key
   * @param value Value
   */
  public void put(K key, V value) {
    values(key).add(value);
  }

  /**
   * Adds a set of values for the specified key.
   * 
   * @param key Key
   * @param values Values
   */
  public void putAll(K key, List<V> values) {
    values(key).addAll(values);
  }

  /**
   * Sets the list of values for the given key overriding any existing list.
   * 
   * @param key Key
   * @param values List
   */
  public void set(K key, List<V> values) {
    map.put(key, values);
  }

  /**
   * Gets the values for the given key. Creates a new list if none exists.
   * 
   * @param key Key
   * @return List of values
   */
  private List<V> values(K key) {
    List<V> values = map.get(key);

    if (values == null) {
      values = new ArrayList<V>();
      map.put(key, values);
    }

    return values;
  }
}
