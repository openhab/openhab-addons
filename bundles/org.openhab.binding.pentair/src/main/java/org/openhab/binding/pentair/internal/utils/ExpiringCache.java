/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pentair.internal.utils;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This is a modified version of the ExpiryCache which adds functions such as getLastKnownValue. It also allows an
 * interface via Supplier which will return the value, or through a function which calls putValue.
 *
 * There must be provided an action in order to retrieve/calculate the value. This action will be called only if the
 * answer from the last calculation is not valid anymore, i.e. if it is expired.
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Martin van Wingerden - Add Duration constructor
 * @author Jeff James - Added added getLastKnownValue
 *
 * @param <V> the type of the value
 */
@NonNullByDefault
public class ExpiringCache<V> {
    private final long expiry;

    private SoftReference<@Nullable V> value = new SoftReference<>(null);
    private long expiresAt;

    public interface RefreshAction {
        void refresh();
    }

    public ExpiringCache() {
        this.expiry = 0;
    }

    /**
     * Create a new instance.
     *
     * @param expiry the duration for how long the value stays valid
     * @param action the action to retrieve/calculate the value
     * @throws IllegalArgumentException For an expire value <=0.
     */
    public ExpiringCache(Duration expiry) {
        if (expiry.isNegative() || expiry.isZero()) {
            throw new IllegalArgumentException("Cache expire time must be greater than 0");
        }
        this.expiry = expiry.toNanos();
    }

    /**
     * Create a new instance.
     *
     * @param expiry the duration in milliseconds for how long the value stays valid
     * @param action the action to retrieve/calculate the value
     */
    public ExpiringCache(long expiry) {
        this(Duration.ofMillis(expiry));
    }

    /**
     * Returns the value - possibly from the cache, if it is still valid.
     */
    public synchronized @Nullable V getValue(Supplier<@Nullable V> action) {
        @Nullable
        V cachedValue = value.get();
        if (cachedValue == null || isExpired()) {
            return refreshValue(action);
        }
        return cachedValue;
    }

    /**
     * Returns the value - either from the cache or will call the action function which is responsible for calling
     * putValue.
     */
    public synchronized @Nullable V getValue(RefreshAction action) {
        @Nullable
        V cachedValue = value.get();
        if (cachedValue == null || isExpired()) {
            action.refresh();
            cachedValue = value.get();
        }

        return cachedValue;
    }

    /**
     * Returns the last known value
     */
    public synchronized @Nullable V getLastKnownValue() {
        return value.get();
    }

    /**
     * Puts a new value into the cache.
     *
     * @param value the new value
     */
    public final synchronized void putValue(@Nullable V value) {
        this.value = new SoftReference<>(value);
        expiresAt = calcExpiresAt();
    }

    /**
     * Invalidates the value in the cache.
     */
    public final synchronized void invalidateValue() {
        value = new SoftReference<>(null);
        expiresAt = 0;
    }

    /**
     * Refreshes and returns the value in the cache.
     * If null returned from action.get, the get action should have sued putValue to update the item
     *
     * @return the new value
     */
    public synchronized @Nullable V refreshValue(Supplier<@Nullable V> action) {
        @Nullable
        V freshValue = action.get();
        if (freshValue == null) {
            return null;
        }
        value = new SoftReference<>(freshValue);
        expiresAt = calcExpiresAt();
        return freshValue;
    }

    /**
     * Checks if the value is expired.
     *
     * @return true if the value is expired
     */
    public boolean isExpired() {
        return expiresAt < System.nanoTime();
    }

    private long calcExpiresAt() {
        return System.nanoTime() + expiry;
    }
}
