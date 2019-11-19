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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vallox.internal.se.telegram.Telegram;

/**
 * Expiring cache implementation.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class ValloxExpiringCacheMap {

    private static final byte[] BYTES_TEMPERATURES = new byte[] { (byte) 0x34, (byte) 0x32, (byte) 0x33, (byte) 0x35 };
    private static final byte[] BYTES_CO2 = new byte[] { (byte) 0x2C, (byte) 0x2B };
    private static final byte[] BYTES_CO2_SETPOINT = new byte[] { (byte) 0xB3, (byte) 0xB4 };

    private final long expiry;
    private ConcurrentMap<Byte, ExpiringCacheObject> map;

    /**
     * Create a new instance.
     *
     * @param expiry after this duration the value is expired
     */
    public ValloxExpiringCacheMap(Duration expiry) {
        this.expiry = expiry.toMillis();
        map = new ConcurrentHashMap<>();
    }

    /**
     * Put new telegram into cache
     *
     * @param telegram
     */
    public void put(Telegram telegram) {
        map.put(telegram.getVariable(), new ExpiringCacheObject(expiry, telegram));
    }

    /**
     * Return the value mapped with the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the given key, or null if there is no cached value for the given key
     */
    public ExpiringCacheObject getCacheObject(Byte key) {
        return map.get(key);
    }

    /**
     * Get telegram associated with the key
     *
     * @param key the key whose associated value is to be returned
     * @return telegram the telegram or null
     */
    @SuppressWarnings({ "null", "unused" })
    public Telegram getTelegram(Byte key) {
        ExpiringCacheObject cacheObject = map.get(key);
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
    @SuppressWarnings({ "null", "unused" })
    public Byte getValue(Byte key) {
        Telegram telegram = getTelegram(key);
        if (telegram != null) {
            return telegram.getValue();
        }
        return null;
    }

    /**
     * Check if value is present and/or expired
     *
     * @param key - find the value to be tested
     * @return false if value is not present or is expired
     */
    @SuppressWarnings({ "unused", "null" })
    public boolean isExpired(Byte key) {
        ExpiringCacheObject value = map.get(key);
        if (value != null) {
            return map.get(key).isExpired();
        }
        return true;
    }

    /**
     * Check if map contains value for the key and value not expired
     *
     * @param key
     * @return true if map contains the key and value is not expired
     */
    public boolean isValid(Byte key) {
        return (containsKey(key) & !isExpired(key));
    }

    /**
     * Check if key is in the cache
     *
     * @param key the key to check
     * @return true if cache contains a value for the key
     */
    public boolean containsKey(Byte key) {
        return map.containsKey(key);
    }

    /**
     * Iterate over byte array to check if they are in the cache
     *
     * @param array byte array to iterate
     * @return true if all bytes are in the cache
     */
    private boolean containsArrayMembers(byte[] array) {
        for (byte member : array) {
            if (!map.containsKey(member)) {
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
        return containsArrayMembers(BYTES_TEMPERATURES);
    }

    /**
     * Check if both CO2 bytes are cached
     *
     * @return true if both are in cache
     */
    public boolean containsCO2() {
        return containsArrayMembers(BYTES_CO2);
    }

    /**
     * Check if both CO2 set point bytes are cached
     *
     * @return true if both are in cache
     */
    public boolean containsCO2SetPoint() {
        return containsArrayMembers(BYTES_CO2_SETPOINT);
    }

    /**
     * Clear cache
     */
    public void clear() {
        map.clear();
    }
}
