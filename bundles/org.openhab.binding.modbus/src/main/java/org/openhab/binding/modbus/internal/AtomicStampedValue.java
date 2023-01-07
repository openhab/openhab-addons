/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Timestamp-value pair that can be updated atomically
 *
 * @author Sami Salonen - Initial contribution
 *
 * @param <V> type of the value
 */
@NonNullByDefault
public class AtomicStampedValue<V> implements Cloneable {

    private long stamp;
    private V value;

    private AtomicStampedValue(AtomicStampedValue<V> copy) {
        this(copy.stamp, copy.value);
    }

    /**
     * Construct new stamped key-value pair
     *
     * @param stamp stamp for the data
     * @param value value for the data
     *
     * @throws NullPointerException when key or value is null
     */
    public AtomicStampedValue(long stamp, V value) {
        Objects.requireNonNull(value, "value should not be null!");
        this.stamp = stamp;
        this.value = value;
    }

    /**
     * Update data in this instance atomically
     *
     * @param stamp stamp for the data
     * @param value value for the data
     *
     * @throws NullPointerException when value is null
     */
    public synchronized void update(long stamp, V value) {
        Objects.requireNonNull(value, "value should not be null!");
        this.stamp = stamp;
        this.value = value;
    }

    /**
     * Copy data atomically and return the new (shallow) copy
     *
     * @return new copy of the data
     * @throws CloneNotSupportedException
     */
    @SuppressWarnings("unchecked")
    public synchronized AtomicStampedValue<V> copy() {
        return (AtomicStampedValue<V>) this.clone();
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
    public synchronized @Nullable AtomicStampedValue<V> copyIfStampAfter(long stampMin) {
        if (stampMin <= this.stamp) {
            return new AtomicStampedValue<>(this);
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
    public static int compare(@SuppressWarnings("rawtypes") @Nullable AtomicStampedValue x,
            @SuppressWarnings("rawtypes") @Nullable AtomicStampedValue y) {
        if (x == null) {
            return -1;
        } else if (y == null) {
            return 1;
        } else {
            return Long.compare(x.stamp, y.stamp);
        }
    }
}
