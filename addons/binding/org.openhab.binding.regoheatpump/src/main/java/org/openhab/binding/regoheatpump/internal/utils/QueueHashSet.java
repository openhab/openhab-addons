package org.openhab.binding.regoheatpump.internal.utils;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * A simple thread safe queue like collection that does not allow duplicate values.
 */
public class QueueHashSet<T> {
    private final HashSet<T> set = new HashSet<T>();
    private final LinkedList<T> list = new LinkedList<T>();

    public synchronized void push(T value) {
        if (set.add(value)) {
            list.add(value);
        }
    }

    public synchronized T peek() {
        return list.peek();
    }

    public synchronized T poll() {
        final T value = list.poll();
        if (value != null) {
            set.remove(value);
        }
        return value;
    }

    public synchronized void clear() {
        set.clear();
        list.clear();
    }
}
