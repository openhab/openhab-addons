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
package org.openhab.binding.vallox.internal.se.cache;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vallox.internal.se.mapper.ChannelDescriptor;
import org.openhab.binding.vallox.internal.se.telegram.Telegram;

/**
 * Expiring cache implementation.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class ValloxExpiringCacheMap {

    // @formatter:off
    /**
     * Set of channels containing temperatures. Used for checking if all these are cached
     */
    private static final Set<ChannelDescriptor> TEMPERATURES = Collections.unmodifiableSet(Stream.of(
                ChannelDescriptor.TEMPERATURE_INSIDE,
                ChannelDescriptor.TEMPERATURE_OUTSIDE,
                ChannelDescriptor.TEMPERATURE_EXHAUST,
                ChannelDescriptor.TEMPERATURE_INCOMING).collect(Collectors.toSet()));

    /**
     * Set of channels containing CO2 values. Used for checking if all these are cached
     */
    private static final Set<ChannelDescriptor> CO2 = Collections.unmodifiableSet(Stream.of(
                ChannelDescriptor.CO2_HIGH,
                ChannelDescriptor.CO2_LOW).collect(Collectors.toSet()));

    /**
     * Set of channels containing CO2 set point values. Used for checking if all these are cached
     */
    private static final Set<ChannelDescriptor> CO2_SETPOINT = Collections.unmodifiableSet(Stream.of(
                ChannelDescriptor.CO2_SETPOINT_HIGH,
                ChannelDescriptor.CO2_SETPOINT_LOW).collect(Collectors.toSet()));
    // @formatter:on

    /**
     * Expiring time for cached values
     */
    private final long expiry;

    /**
     * The map used for caching
     */
    private ConcurrentMap<ChannelDescriptor, ExpiringCacheObject> cache;

    /**
     * Create a new instance.
     *
     * @param expiry after this duration the value is expired
     */
    public ValloxExpiringCacheMap(Duration expiry) {
        this.expiry = expiry.toMillis();
        cache = new ConcurrentHashMap<>();
    }

    /**
     * Put new telegram into cache
     *
     * @param telegram
     */
    public void put(Telegram telegram) {
        ChannelDescriptor descriptor = ChannelDescriptor.get(telegram.getVariable());
        if (descriptor != ChannelDescriptor.NULL) {
            cache.put(descriptor, new ExpiringCacheObject(expiry, telegram));
        }
    }

    /**
     * Return the value mapped with the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the given key, or null if there is no cached value for the given key
     */
    public @Nullable ExpiringCacheObject getCacheObject(ChannelDescriptor key) {
        return cache.get(key);
    }

    /**
     * Get telegram associated with the key
     *
     * @param key the key whose associated value is to be returned
     * @return telegram the telegram or null
     */
    public @Nullable Telegram getTelegram(ChannelDescriptor key) {
        ExpiringCacheObject cacheObject = getCacheObject(key);
        if (cacheObject != null) {
            return cacheObject.get();
        }
        return null;
    }

    /**
     * Get telegrams value associated with the key
     *
     * @param key the key whose associated value is to be returned
     * @return value the telegrams value or null
     */
    public byte getValue(ChannelDescriptor key) {
        Telegram telegram = getTelegram(key);
        if (telegram != null) {
            return telegram.getValue();
        }
        return (byte) 0xFF;
    }

    /**
     * Check if value is present and/or expired
     *
     * @param key - find the value to be tested
     * @return false if value is not present or is expired
     */
    public boolean isExpired(ChannelDescriptor key) {
        ExpiringCacheObject value = getCacheObject(key);
        if (value != null) {
            return cache.get(key).isExpired();
        }
        return true;
    }

    /**
     * Check if map contains value for the key and value not expired
     *
     * @param key
     * @return true if map contains the key and value is not expired
     */
    public boolean isValid(ChannelDescriptor key) {
        return (containsKey(key) & !isExpired(key));
    }

    /**
     * Check if key is in the cache
     *
     * @param key the key to check
     * @return true if cache contains a value for the key
     */
    public boolean containsKey(@Nullable ChannelDescriptor key) {
        if (key == null) {
            return false;
        }
        return cache.containsKey(key);
    }

    /**
     * Iterate over byte array to check if they are in the cache
     *
     * @param array byte array to iterate
     * @return true if all bytes are in the cache
     */
    private boolean containsMembers(Set<ChannelDescriptor> set) {
        for (ChannelDescriptor member : set) {
            if (!cache.containsKey(member)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if all four temperatures are cached
     *
     * @return true if all four are in cache
     */
    public boolean containsTemperatures() {
        return containsMembers(TEMPERATURES);
    }

    /**
     * Check if both CO2 bytes are cached
     *
     * @return true if both are in cache
     */
    public boolean containsCO2() {
        return containsMembers(CO2);
    }

    /**
     * Check if both CO2 set point bytes are cached
     *
     * @return true if both are in cache
     */
    public boolean containsCO2SetPoint() {
        return containsMembers(CO2_SETPOINT);
    }

    /**
     * Clear cache
     */
    public void clear() {
        cache.clear();
    }
}
