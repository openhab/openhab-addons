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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ImageCache}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
class ImageCacheTest {

    @Test
    void testCacheHit() {
        ImageCache cache = new ImageCache();
        String key = ImageCache.cacheKey("item1", "Primary", "tag1", 512);
        byte[] data = new byte[] { 1, 2, 3 };

        cache.put(key, data);

        byte[] result = cache.get(key);
        assertNotNull(result);
        assertArrayEquals(data, result);
    }

    @Test
    void testCacheMiss() {
        ImageCache cache = new ImageCache();
        String key = ImageCache.cacheKey("item1", "Primary", "tag1", 512);

        assertNull(cache.get(key));
    }

    @Test
    void testEvictUnused() {
        ImageCache cache = new ImageCache();
        String key1 = ImageCache.cacheKey("item1", "Primary", "tag1", 512);
        String key2 = ImageCache.cacheKey("item2", "Backdrop", "tag2", 256);

        cache.put(key1, new byte[] { 1 });
        cache.put(key2, new byte[] { 2 });

        // Evict entries not in the active set — keep only item1
        cache.evictUnused(Set.of("item1"));

        assertNotNull(cache.get(key1));
        assertNull(cache.get(key2));
    }

    @Test
    void testEvictAll() {
        ImageCache cache = new ImageCache();
        String key1 = ImageCache.cacheKey("item1", "Primary", "tag1", 512);

        cache.put(key1, new byte[] { 1 });
        cache.evictUnused(Set.of());

        assertNull(cache.get(key1));
    }

    @Test
    void testCacheKeyFormat() {
        String key = ImageCache.cacheKey("abc", "Primary", "notag", 512);
        assertEquals("abc|Primary|notag|512", key);
    }
}
