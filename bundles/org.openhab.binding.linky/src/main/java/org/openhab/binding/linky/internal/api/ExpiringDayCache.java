/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
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
 * The cache expires after the current day; it is possible to shift the beginning time of the day.
 *
 * Soft Reference is not used to store the cached value because JVM Garbage Collector is clearing it too much often.
 *
 * @author Laurent Garnier - Initial contribution
 *
 * @param <V> the type of the value
 */
@NonNullByDefault
public class ExpiringDayCache<V> {
    private final Logger logger = LoggerFactory.getLogger(ExpiringDayCache.class);

    private final String name;
    private final int beginningHour;
    private final int beginningMinute;
    private Supplier<@Nullable V> action;

    private @Nullable V value;
    private LocalDateTime expiresAt;
    public boolean missingData = false;

    /**
     * Create a new instance.
     *
     * @param name the name of this cache
     * @param beginningHour the hour in the day at which the validity period is starting
     * @param action the action to retrieve/calculate the value
     */
    public ExpiringDayCache(String name, int beginningHour, int beginningMinute, Supplier<@Nullable V> action) {
        this.name = name;
        this.beginningHour = beginningHour;
        this.beginningMinute = beginningMinute;
        this.expiresAt = calcAlreadyExpired();
        this.action = action;
    }

    /**
     * Returns the value - possibly from the cache, if it is still valid.
     */
    public synchronized Optional<V> getValue() {
        @Nullable
        V cachedValue = value;
        if (cachedValue == null || isExpired()) {
            logger.debug("getValue from cache \"{}\" is requiring a fresh value", name);
            cachedValue = refreshValue();
        } else {
            logger.debug("getValue from cache \"{}\" is returning a cached value", name);
        }
        return Optional.ofNullable(cachedValue);
    }

    /**
     * Returns if the value is Present or not.
     */
    public boolean isPresent() {
        V cachedValue = value;
        return (cachedValue != null && !isExpired());
    }

    public void invalidate() {
        value = null;
    }

    /**
     * Refreshes and returns the value in the cache.
     *
     * @return the new value
     */
    public synchronized @Nullable V refreshValue() {
        value = action.get();
        expiresAt = calcNextExpiresAt();
        return value;
    }

    /**
     * Checks if the value is expired.
     *
     * @return true if the value is expired
     */
    public boolean isExpired() {
        return !LocalDateTime.now().isBefore(expiresAt);
    }

    private LocalDateTime calcNextExpiresAt() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.withHour(beginningHour).withMinute(beginningMinute).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime result = now.isBefore(limit) ? limit : limit.plusDays(1);
        logger.debug("calcNextExpiresAt result = {}", result.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }

    private LocalDateTime calcAlreadyExpired() {
        return LocalDateTime.now().minusDays(1);
    }
}
