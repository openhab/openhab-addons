package org.openhab.binding.regoheatpump.internal.utils;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * A simple thread safe queue like collection that does not allow duplicate values.
 */
public class QueueHashSet<T> {
    private final HashSet<T> set = new HashSet<T>();
    private final LinkedList<T> list = new LinkedList<T>();

    /**
     * Adds the specified element to this set if it is not already present.
     *
     * @param value element to be added to the end of the list
     */
    public synchronized void add(T value) {
        if (set.add(value)) {
            list.add(value);
        }
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this set.
     *
     * @return the head of this list, or null if this list is empty
     */
    public synchronized T peek() {
        return list.peek();
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     *
     * @return the head of this list, or null if this list is empty
     */
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
