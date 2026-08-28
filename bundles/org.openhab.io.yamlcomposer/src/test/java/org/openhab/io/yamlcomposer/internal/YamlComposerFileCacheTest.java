/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.yamlcomposer.internal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.io.yamlcomposer.internal.YamlComposer.CacheEntry;

/**
 * The {@link YamlComposerFileCacheTest} contains tests for the caching behavior of the {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("File Cache Behavior")
class YamlComposerFileCacheTest extends AbstractYamlComposerTest {
    @Test
    @DisplayName("Caches include bytes and mtime on first load")
    void cachesIncludeEntry() throws Exception {
        Path included = writeFixture("cache_included.inc.yaml", "key: value");
        Path main = writeFixture("main_cache.yaml", """
                data1: !include cache_included.inc.yaml
                data2: !include cache_included.inc.yaml
                """);

        ConcurrentHashMap<Path, CacheEntry> includeCache = new ConcurrentHashMap<>();
        YamlComposer.load(main, p -> {
        }, env -> {
        }, logSession, includeCache);

        Path real = included.toRealPath();
        assertTrue(includeCache.containsKey(real));
        CacheEntry entry = includeCache.get(real);
        assertNotNull(entry);
        assertArrayEquals(Files.readAllBytes(real), entry.bytes());
        assertEquals(Files.getLastModifiedTime(real).toMillis(), entry.mtime());
    }

    @Test
    @DisplayName("Refreshes cached entry when mtime differs")
    void refreshesCacheOnMtimeChange() throws Exception {
        Path included = writeFixture("cache_refresh.inc.yaml", "a: old");
        Path main = writeFixture("main_refresh.yaml", "data: !include cache_refresh.inc.yaml");

        Path real = included.toRealPath();

        ConcurrentHashMap<Path, CacheEntry> includeCache = new ConcurrentHashMap<>();
        // Insert a stale entry with an older mtime and different bytes
        long staleMtime = Math.max(0L, Files.getLastModifiedTime(real).toMillis() - 10_000L);
        includeCache.put(real, new CacheEntry("stale".getBytes(), staleMtime));

        // Update the file to ensure a newer mtime and new content
        Files.writeString(included, "a: refreshed");

        YamlComposer.load(main, p -> {
        }, env -> {
        }, logSession, includeCache);

        CacheEntry entry = includeCache.get(real);
        assertNotNull(entry);
        assertArrayEquals(Files.readAllBytes(real), entry.bytes());
        assertEquals(Files.getLastModifiedTime(real).toMillis(), entry.mtime());
    }
}
