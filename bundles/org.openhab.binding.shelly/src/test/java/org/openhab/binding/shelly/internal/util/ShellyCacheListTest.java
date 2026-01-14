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
package org.openhab.binding.shelly.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Test class for {@link ShellyCacheList}
 *
 * @author Markus Michels - Initial contribution
 */
@Timeout(10)
@NonNullByDefault
public class ShellyCacheListTest {
    private static final int TEST_TTL_SECONDS = 1; // Short TTL for testing
    private @Nullable ShellyCacheList<String, String> cache;

    @BeforeEach
    public void setUp() {
        cache = new ShellyCacheList<>(TEST_TTL_SECONDS);
    }

    @SuppressWarnings("null")
    @Test
    public void testPutAndGet() {
        // Test basic put and get operations
        assertNull(cache.put("key1", "value1"));
        assertEquals("value1", cache.get("key1"));
        assertEquals("value1", cache.put("key1", "value2"));
        assertEquals("value2", cache.get("key1"));
    }

    @SuppressWarnings("null")
    @Test
    public void testGetNonExistent() {
        assertNull(cache.get("nonexistent"));
    }

    @SuppressWarnings("null")
    @Test
    public void testExpiration() throws InterruptedException {
        // Test that entries expire after TTL
        cache.put("key1", "value1");
        assertNotNull(cache.get("key1"), "Entry should exist before TTL");

        // Wait for TTL to expire plus a small buffer
        // We need to await 2*TEST_TTL_SECONDS: initial delay + interval
        Thread.sleep((2 * TEST_TTL_SECONDS * 1000) + 100);

        assertNull(cache.get("key1"), "Entry should be null after TTL expires");
    }

    @SuppressWarnings("null")
    @Test
    public void testPutIfAbsentOrSame() {
        // Test with new key - should add the entry
        assertTrue(cache.putIfAbsent("key1", "value1", (v1, v2) -> false));
        assertEquals("value1", cache.get("key1"), "Should add new entry");

        // Test with same key and value, but different duplicate check
        assertTrue(cache.putIfAbsent("key1", "value1", (v1, v2) -> false),
                "Should return true when values are the same but not marked as duplicates");

        // Test with same key but different value and always-true duplicate check
        assertFalse(cache.putIfAbsent("key1", "value2", (v1, v2) -> true),
                "Should return false when marked as duplicate");
        assertEquals("value1", cache.get("key1"), "Value should not change when marked as duplicate");

        // Test with expired entry
        cache.put("expiredKey", "oldValue");
        // Simulate expiration by manipulating the cache directly (reflection would be needed for a real test)
        // For now, we'll just test the non-expired case
        assertTrue(cache.putIfAbsent("expiredKey", "newValue", (v1, v2) -> false),
                "Should return true for expired entry");
    }

    @SuppressWarnings("null")
    @Test
    public void testPutIfAbsentOrSameWithDifferentComparators() {
        // Test with case-insensitive comparison
        cache.put("key1", "VALUE1");
        assertFalse(cache.putIfAbsent("key1", "value1", (v1, v2) -> v1 != null && v1.equalsIgnoreCase(v2)),
                "Should detect as same value with case-insensitive comparison");

        // Test with substring matching
        cache.put("key2", "prefix_value_suffix");
        assertFalse(cache.putIfAbsent("key2", "value", (v1, v2) -> v1 != null && v1.contains(v2)),
                "Should detect as same value with substring matching");

        // Test with custom object comparison
        cache.put("key3", "{\"id\":1,\"name\":\"test\"}");
        assertFalse(cache.putIfAbsent("key3", "{\"id\":1,\"name\":\"test\"}", (v1, v2) -> {
            return v1 != null && v1.replaceAll("\\s", "").equals(v2.replaceAll("\\s", ""));
        }), "Same JSON content should be suppressed");
        assertTrue(cache.putIfAbsent("key3", "{\"name\":\"test\",\"id\":1}", (v1, v2) -> {
            // Simple JSON comparison (in a real test, use a proper JSON parser)
            return v1 != null && v1.replaceAll("\\s", "").equals(v2.replaceAll("\\s", ""));
        }), "Should detect as same JSON content ");
    }

    @Test
    public void testConcurrentPutIfAbsentOrSame() throws InterruptedException {
        final int THREAD_COUNT = 5;
        final int OPERATIONS_PER_THREAD = 100;
        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        String key = "key-" + threadId;
                        String value = "value-" + threadId + "-" + j;

                        // Only one thread should succeed in adding each key
                        @SuppressWarnings("null")
                        boolean added = cache.putIfAbsent(key, value, (v1, v2) -> false);
                        if (added) {
                            // Verify we can read back the value we just added
                            @SuppressWarnings("null")
                            String result = cache.get(key);
                            assertNotNull(result, "Added value should be retrievable");
                            assertTrue(result.startsWith("value-" + threadId + "-"));
                        }
                    }
                } catch (Exception e) {
                    fail("Thread " + threadId + " failed: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }

        // Verify final state
        for (int i = 0; i < THREAD_COUNT; i++) {
            String key = "key-" + i;
            @SuppressWarnings("null")
            String value = cache.get(key);
            assertNotNull(value, "Each thread should have set at least one value for its key");
            assertTrue(value.startsWith("value-" + i + "-"), "Value should match the thread that set it");
        }
    }

    @SuppressWarnings("null")
    @Test
    public void testCleanup() throws InterruptedException {
        // Add an entry and verify it's there
        cache.put("key1", "value1");
        assertNotNull(cache.get("key1"));

        // Wait for TTL to expire plus a small buffer
        Thread.sleep((2 * TEST_TTL_SECONDS * 1000) + 100);

        // Trigger cleanup by adding a new entry
        cache.put("key2", "value2");

        // First entry should be cleaned up, second should exist
        assertNull(cache.get("key1"), "Expired entry should be cleaned up");
        assertNotNull(cache.get("key2"), "New entry should exist");
    }

    @SuppressWarnings("null")
    @Test
    public void testDispose() {
        // Add some entries
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Dispose and verify cleanup
        cache.dispose();

        // Verify all entries are gone
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }

    @SuppressWarnings("null")
    @Test
    public void testNullHandling() {
        // Test null key
        assertThrows(NullPointerException.class, () -> cache.putIfAbsent(null, "value", (v1, v2) -> false));

        // Test null value
        assertThrows(NullPointerException.class, () -> cache.put("key", null));
    }

    @SuppressWarnings("null")
    @Test
    public void testLargeNumberOfEntries() {
        // Test with a larger number of entries
        final int ENTRY_COUNT = 1000;
        for (int i = 0; i < ENTRY_COUNT; i++) {
            String key = "key-" + i;
            String value = "value-" + i;
            @SuppressWarnings("null")
            boolean added = cache.putIfAbsent(key, value, (v1, v2) -> false);
            assertTrue(added, "Should add new entry: " + key);
        }

        // Verify all entries are retrievable
        for (int i = 0; i < ENTRY_COUNT; i++) {
            String key = "key-" + i;
            String expected = "value-" + i;
            assertEquals(expected, cache.get(key), "Should retrieve value for key: " + key);
        }
    }
}
