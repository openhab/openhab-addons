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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vallox.internal.se.mapper.ChannelDescriptor;
import org.openhab.binding.vallox.internal.se.telegram.Telegram;
import org.openhab.binding.vallox.internal.se.telegram.Telegram.TelegramState;

/**
 * Expiring cache implementation.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class ValloxExpiringCacheMap {

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
        this.expiry = expiry.toNanos();
        cache = new ConcurrentHashMap<>();
    }

    /**
     * Put new {@link Telegram} into {@link ValloxExpiringCacheMap} with {@link ChannelDescriptor} as key
     *
     * @param telegram the telegram to put into map.
     */
    public void put(Telegram telegram) {
        ChannelDescriptor descriptor = ChannelDescriptor.get(telegram.getVariable());
        if (descriptor != ChannelDescriptor.NULL) {
            cache.put(descriptor, new ExpiringCacheObject(expiry, telegram));
        }
    }

    /**
     * Put new {@link ExpiringCacheObject} into cache with empty {@link Telegram}.
     * This is for Co2 channels to track their cache validity.
     *
     * @param descriptor {@link ChannelDescriptor} as the key
     */
    public void put(ChannelDescriptor descriptor) {
        cache.put(descriptor, new ExpiringCacheObject(expiry, new Telegram(TelegramState.EMPTY)));
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
    public @Nullable Byte getValue(ChannelDescriptor key) {
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
    public @Nullable Byte getIfValid(ChannelDescriptor key) {
        if (cache.containsKey(key) & !isExpired(key)) {
            return getValue(key);
        }
        return null;
    }

    /**
     * Clear cache
     */
    public void clear() {
        cache.clear();
    }
}
