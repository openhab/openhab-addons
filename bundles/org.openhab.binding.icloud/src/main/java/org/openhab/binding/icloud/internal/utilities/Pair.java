package org.openhab.binding.icloud.internal.utilities;

/**
 * TODO simon This type ...
 *
 */
public class Pair<K, V> {
  public K key;

  public V value;

  public static Pair of(String key, String value) {

    Pair p = new Pair();
    p.key = key;
    p.value = value;
    return p;
  }

}
