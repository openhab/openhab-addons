/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal.toberemoved.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Complementary class to {@link org.eclipse.smarthome.core.cache.ExpiringCache}, implementing an async variant
 * of an expiring cache. Returns the cached value immediately to the callback if not expired yet, otherwise issue
 * a fetch and notify callback implementors asynchronously.
 *
 * @author David Graeff - Initial contribution
 *
 * @param <V> the type of the cached value
 */
public class ExpiringCacheAsync<V> {
    final long expiry;
    ExpiringCacheUpdate cacheUpdater;
    long expiresAt = 0;
    boolean refreshRequested = false;
    V value;
    final List<Consumer<V>> waitingCacheCallbacks = new LinkedList<>();

    /**
     * Implement the requestCacheUpdate method which will be called when the cache
     * needs an updated value. Call {@see setValue} to update the cached value.
     */
    public static interface ExpiringCacheUpdate {
        void requestCacheUpdate();
    }

    /**
     * Create a new instance.
     *
     * @param expiry the duration in milliseconds for how long the value stays valid. Must be greater than 0.
     * @param cacheUpdater The cache will use this callback if a new value is needed. Must not be null.
     * @throws IllegalArgumentException For an expire value <=0 or a null cacheUpdater.
     */
    public ExpiringCacheAsync(long expiry, ExpiringCacheUpdate cacheUpdater) throws IllegalArgumentException {
        this.expiry = TimeUnit.MILLISECONDS.toNanos(expiry);
        this.cacheUpdater = cacheUpdater;
        if (expiry <= 0) {
            throw new IllegalArgumentException("Cache expire time must be greater than 0");
        }
        if (cacheUpdater == null) {
            throw new IllegalArgumentException("A cache updater is necessary");
        }
    }

    /**
     * Returns the value - possibly from the cache, if it is still valid.
     *
     * @return the value
     */
    public void getValue(Consumer<V> callback) {
        if (isExpired()) {
            refreshValue(callback);
        } else {
            callback.accept(value);
        }
    }

    /**
     * Invalidates the value in the cache.
     */
    public void invalidateValue() {
        expiresAt = 0;
    }

    /**
     * Updates the cached value with the given one.
     *
     * @param newValue The new value. All listeners, registered by getValueAsync() and refreshValue(), will be notified
     *            of the new value.
     */
    public void setValue(V newValue) {
        refreshRequested = false;
        value = newValue;
        expiresAt = getCurrentNanoTime() + expiry;
        // Inform all callback handlers of the new value and clear the list
        for (Consumer<V> callback : waitingCacheCallbacks) {
            callback.accept(value);
        }
        waitingCacheCallbacks.clear();
    }

    /**
     * Returns an arbitrary time reference in nanoseconds.
     * This is used for the cache to determine if a value has expired.
     */
    public long getCurrentNanoTime() {
        return System.nanoTime();
    }

    /**
     * Refreshes and returns the value asynchronously.
     *
     * @return the new value
     */
    public void refreshValue(Consumer<V> callback) {
        waitingCacheCallbacks.add(callback);
        if (refreshRequested) {
            return;
        }
        refreshRequested = true;
        expiresAt = 0;
        cacheUpdater.requestCacheUpdate();
    }

    /**
     * Checks if the value is expired.
     *
     * @return true if the value is expired
     */
    public boolean isExpired() {
        return expiresAt < getCurrentNanoTime();
    }

    /**
     * Return the raw value, no matter if it is already
     * expired or still valid.
     */
    public V getExpiredValue() {
        return value;
    }
}
