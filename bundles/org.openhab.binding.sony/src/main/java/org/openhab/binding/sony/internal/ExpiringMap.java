/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This is a limited functional map (not using the full Map interface) that will allow entries to expire after a certain
 * period of time. Once expired, a listener will be notified of the expiration
 *
 * @author Tim Roberts - Initial contribution
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@NonNullByDefault
public class ExpiringMap<K, V> implements AutoCloseable {

    /** The lock used to control access to both internal maps */
    private final ReadWriteLock mapLock = new ReentrantReadWriteLock();

    /** The internal map used to store key/values */
    private final Map<K, V> internalMap = new HashMap<>();

    /** The map of keys to their creation timestamps */
    private final Map<K, Long> timeStamps = new HashMap<>();

    /** The expiring check job */
    private final @Nullable Future<?> expireCheck;

    /** The array list of expire listeners */
    private final List<ExpireListener<K, V>> expireListeners = new CopyOnWriteArrayList<>();

    /**
     * Constructs the map from the parameters
     *
     * @param scheduler the possibly null scheduler (if null, nothing will expire)
     * @param expireTime the expiration time
     * @param timeUnit the non-null time unit of expireTime
     */
    public ExpiringMap(final @Nullable ScheduledExecutorService scheduler, final int expireTime,
            final TimeUnit timeUnit) {
        Objects.requireNonNull(timeUnit, "timeUnit cannot be null");

        if (scheduler == null) {
            expireCheck = null;
        } else {
            expireCheck = scheduler.scheduleWithFixedDelay(() -> {
                final long now = System.currentTimeMillis();
                final Lock writeLock = mapLock.writeLock();
                writeLock.lock();
                try {
                    timeStamps.entrySet().removeIf(e -> {
                        if (e.getValue() + expireTime <= now) {
                            final K key = e.getKey();
                            final V val = internalMap.remove(key);
                            expireListeners.forEach(l -> l.expired(key, val));
                            return true;
                        }
                        return false;
                    });
                } finally {
                    writeLock.unlock();
                }
            }, expireTime, expireTime, timeUnit);
        }
    }

    /**
     * Adds a listener for expiration notices
     *
     * @param listener a non-null listener
     */
    public void addExpireListener(final ExpireListener<K, V> listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        expireListeners.add(listener);
    }

    /**
     * Gets a value associated with the key
     *
     * @param key a non-null key
     * @return the value associated with the key or null if not found
     */
    public @Nullable V get(final @Nullable K key) {
        Objects.requireNonNull(key, "key cannot be null");
        final Lock readLock = mapLock.readLock();
        readLock.lock();
        try {
            return internalMap.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Puts a value associated with the key into the map
     *
     * @param key a non-null key
     * @param value a non-null value
     * @return the old key value if replaced (or null if nothing replaced)
     */
    public @Nullable V put(final K key, final V value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        final Lock writeLock = mapLock.writeLock();
        writeLock.lock();
        try {
            timeStamps.put(key, System.currentTimeMillis());
            return internalMap.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes the value associated with the key and returns it
     * 
     * @param key the non-null key
     * @return the value associated with the key or null if key not found
     */
    public @Nullable V remove(final @Nullable K key) {
        Objects.requireNonNull(key, "key cannot be null");
        final Lock writeLock = mapLock.writeLock();
        writeLock.lock();
        try {
            timeStamps.remove(key);
            return internalMap.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void close() {
        SonyUtil.cancel(expireCheck);
    }

    /**
     * This represents a functional interface to defined an expiration callback
     *
     * @author Tim Roberts - Initial contribution
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    @NonNullByDefault
    public interface ExpireListener<K, V> {
        void expired(K key, V value);
    }
}
