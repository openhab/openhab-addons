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
package org.openhab.binding.jellyfin.internal.api;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thread-safe in-memory cache for Jellyfin image bytes, keyed by item ID, image type, tag and width.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ImageCache {

    private final ConcurrentHashMap<String, byte[]> store = new ConcurrentHashMap<>();

    /**
     * Build a cache key from the given parameters.
     *
     * @param itemId Jellyfin item UUID string
     * @param imageType Jellyfin image type (e.g. {@code "Primary"})
     * @param tag Image tag (or {@code "notag"} when absent)
     * @param width Requested maximum width in pixels
     * @return opaque cache key
     */
    public static String cacheKey(String itemId, String imageType, String tag, int width) {
        return itemId + "|" + imageType + "|" + tag + "|" + width;
    }

    /**
     * Return cached bytes for the given key, or {@code null} on a cache miss.
     *
     * @param key cache key produced by {@link #cacheKey}
     * @return cached byte array or {@code null}
     */
    public byte @Nullable [] get(String key) {
        return store.get(key);
    }

    /**
     * Store image bytes under the given key.
     *
     * @param key cache key produced by {@link #cacheKey}
     * @param bytes raw image bytes
     */
    public void put(String key, byte[] bytes) {
        store.put(key, bytes);
    }

    /**
     * Remove all cache entries whose item ID (the prefix before the first {@code |}) is
     * not present in {@code activeItemIds}.
     *
     * @param activeItemIds set of item IDs that are currently playing; entries for all other IDs are evicted
     */
    public void evictUnused(Set<String> activeItemIds) {
        store.keySet().removeIf(key -> {
            int sep = key.indexOf('|');
            String itemId = sep >= 0 ? key.substring(0, sep) : key;
            return !activeItemIds.contains(itemId);
        });
    }
}
