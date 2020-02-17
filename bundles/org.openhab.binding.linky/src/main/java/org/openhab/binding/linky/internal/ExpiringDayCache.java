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
package org.openhab.binding.linky.internal;

import java.lang.ref.SoftReference;
import java.time.LocalDate;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple expiring and reloading cache implementation.
 *
 * There must be provided an action in order to retrieve/calculate the value. This action will be called only if the
 * answer from the last calculation is not valid anymore, i.e. if it is expired.
 *
 * @author Laurent Garnier - Initial contribution
 *
 * @param <V> the type of the value
 */
@NonNullByDefault
public class ExpiringDayCache<V> {
    private final Logger logger = LoggerFactory.getLogger(ExpiringDayCache.class);

    private String name;

    private final Supplier<@Nullable V> action;

    private SoftReference<@Nullable V> value = new SoftReference<>(null);
    private LocalDate expiresAt;

    /**
     * Create a new instance.
     *
     * @param name the name of this cache
     * @param action the action to retrieve/calculate the value
     */
    public ExpiringDayCache(String name, Supplier<@Nullable V> action) {
        this.name = name;
        this.expiresAt = calcAlreadyExpired();
        this.action = action;
    }

    /**
     * Returns the value - possibly from the cache, if it is still valid.
     */
    public synchronized @Nullable V getValue() {
        @Nullable
        V cachedValue = value.get();
        if (cachedValue == null || isExpired()) {
            logger.debug("getValue from cache \"{}\" is requiring a fresh value", name);
            return refreshValue();
        }
        logger.debug("getValue from cache \"{}\" is returing a cached value", name);
        return cachedValue;
    }

    /**
     * Puts a new value into the cache.
     *
     * @param value the new value
     */
    public final synchronized void putValue(@Nullable V value) {
        this.value = new SoftReference<>(value);
        expiresAt = calcNextExpiresAt();
    }

    /**
     * Invalidates the value in the cache.
     */
    public final synchronized void invalidateValue() {
        value = new SoftReference<>(null);
        expiresAt = calcAlreadyExpired();
    }

    /**
     * Refreshes and returns the value in the cache.
     *
     * @return the new value
     */
    public synchronized @Nullable V refreshValue() {
        @Nullable
        V freshValue = action.get();
        value = new SoftReference<>(freshValue);
        expiresAt = calcNextExpiresAt();
        return freshValue;
    }

    /**
     * Checks if the value is expired.
     *
     * @return true if the value is expired
     */
    public boolean isExpired() {
        return !LocalDate.now().isBefore(expiresAt);
    }

    private LocalDate calcNextExpiresAt() {
        return LocalDate.now().plusDays(1);
    }

    private LocalDate calcAlreadyExpired() {
        return LocalDate.now().minusDays(1);
    }
}
