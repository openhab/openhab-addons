/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Timestamped key-value pair that can be updated atomically
 *
 * @author Sami Salonen - Initial contribution
 *
 * @param <K> type of the key
 * @param <V> type of the value
 */
@NonNullByDefault
public class AtomicStampedKeyValue<K, V> implements Cloneable {

    private long stamp;
    private K key;
    private V value;

    private AtomicStampedKeyValue(AtomicStampedKeyValue<K, V> copy) {
        this(copy.stamp, copy.key, copy.value);
    }

    /**
     * Construct new stamped key-value pair
     *
     * @param stamp stamp for the data
     * @param key key for the data
     * @param value value for the data
     *
     * @throws NullPointerException when key or value is null
     */
    public AtomicStampedKeyValue(long stamp, K key, V value) {
        Objects.requireNonNull(key, "key should not be null!");
        Objects.requireNonNull(value, "value should not be null!");
        this.stamp = stamp;
        this.key = key;
        this.value = value;
    }

    /**
     * Update data in this instance atomically
     *
     * @param stamp stamp for the data
     * @param key key for the data
     * @param value value for the data
     *
     * @throws NullPointerException when key or value is null
     */
    public synchronized void update(long stamp, K key, V value) {
        Objects.requireNonNull(key, "key should not be null!");
        Objects.requireNonNull(value, "value should not be null!");
        this.stamp = stamp;
        this.key = key;
        this.value = value;
    }

    /**
     * Copy data atomically and return the new (shallow) copy
     *
     * @return new copy of the data
     * @throws CloneNotSupportedException
     */
    @SuppressWarnings("unchecked")
    public synchronized AtomicStampedKeyValue<K, V> copy() {
        return (AtomicStampedKeyValue<K, V>) this.clone();
    }

    /**
     * Synchronized implementation of clone with exception swallowing
     */
    @Override
    protected synchronized Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // We should never end up here since this class implements Cloneable
            throw new RuntimeException(e);
        }
    }

    /**
     * Copy data atomically if data is after certain stamp ("fresh" enough)
     *
     * @param stampMin
     * @return null, if the stamp of this instance is before stampMin. Otherwise return the data copied
     */
    public synchronized @Nullable AtomicStampedKeyValue<K, V> copyIfStampAfter(long stampMin) {
        if (stampMin <= this.stamp) {
            return new AtomicStampedKeyValue<>(this);
        } else {
            return null;
        }
    }

    /**
     * Get stamp
     */
    public long getStamp() {
        return stamp;
    }

    /**
     * Get key
     */
    public K getKey() {
        return key;
    }

    /**
     * Get value
     */
    public V getValue() {
        return value;
    }

    /**
     * Compare two AtomicStampedKeyValue objects based on stamps
     *
     * Nulls are ordered first
     *
     * @param x first instance
     * @param y second instance
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     */
    public static int compare(@SuppressWarnings("rawtypes") @Nullable AtomicStampedKeyValue x,
            @SuppressWarnings("rawtypes") @Nullable AtomicStampedKeyValue y) {
        if (x == null) {
            return -1;
        } else if (y == null) {
            return 1;
        } else {
            return Long.compare(x.stamp, y.stamp);
        }
    }
}
