package org.openhab.binding.gmailparadoxparser.internal;

public interface Cache<K, V> {
    void put(K key, V value);

    V get(K key);

    void refresh();

    void refresh(String query);

    default void initialize() {
        this.refresh();
    }
}
