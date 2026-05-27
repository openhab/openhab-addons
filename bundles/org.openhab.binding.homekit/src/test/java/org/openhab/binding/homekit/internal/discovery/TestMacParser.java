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
package org.openhab.binding.homekit.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the {@link MacResolver} class.
 * <p>
 * NOTE: intentionally without {@code @NonNullByDefault} since compiler WARN is better than a compiler ERROR
 * <p>
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
class TestMacParser {

    final MacResolver macResolver = new MacResolver();

    @BeforeEach
    void setup() throws Exception {
        macResolver.testClearCache();
    }

    // -----------------------------
    // Normalization + Validation
    // -----------------------------

    @Test
    void testNormalizeMac() throws Exception {
        assertEquals("AA:BB:CC:DD:EE:FF", normalize("aa-bb-cc-dd-ee-ff"));
        assertEquals("AA:BB:CC:DD:EE:FF", normalize("AA:BB:CC:DD:EE:FF"));
        assertEquals("AA:BB:CC:DD:EE:FF", normalize("aa:bb:cc:dd:ee:ff"));
    }

    @Test
    void testIsValidMac() throws Exception {
        assertTrue(isValid("AA:BB:CC:DD:EE:FF"));
        assertFalse(isValid("00:00:00:00:00:00"));
        assertFalse(isValid("AA:BB:CC:DD:EE")); // too short
        assertFalse(isValid("GG:HH:II:JJ:KK:LL")); // invalid hex
    }

    // -----------------------------
    // parseLine() tests
    // -----------------------------

    @Test
    void testParseLineLinuxStyle() throws Exception {
        macResolver.testClearCache();

        String line = "192.168.1.10    0x1 0x2  aa:bb:cc:dd:ee:ff  *  br0";
        parseLine(line);

        assertEquals("AA:BB:CC:DD:EE:FF", macResolver.testGetCached("192.168.1.10"));
    }

    @Test
    void testParseLineWindowsStyle() throws Exception {
        macResolver.testClearCache();

        String line = "  192.168.1.50       aa-bb-cc-dd-ee-ff     dynamic";
        parseLine(line);

        assertEquals("AA:BB:CC:DD:EE:FF", macResolver.testGetCached("192.168.1.50"));
    }

    @Test
    void testParseLineIgnoresInvalid() throws Exception {
        macResolver.testClearCache();

        parseLine("this is not an arp entry");
        parseLine("999.999.999.999 aa:bb:cc:dd:ee:ff");

        assertTrue(macResolver.testCacheIsEmpty());
    }

    // -----------------------------
    // Cache behaviour
    // -----------------------------

    @Test
    void testCacheHitShortCircuitsLookup() throws Exception {
        macResolver.testClearCache();
        macResolver.testPutCached("10.0.0.5", "AA:BB:CC:DD:EE:FF", Instant.now().plusSeconds(60));

        String mac = resolveMac("10.0.0.5");

        assertEquals("AA:BB:CC:DD:EE:FF", mac);
    }

    @Test
    void testCacheExpiry() throws Exception {
        macResolver.testClearCache();

        macResolver.testPutCached("1.2.3.4", "AA:BB:CC:DD:EE:FF", Instant.now().minusSeconds(120));

        assertNull(macResolver.testGetCached("1.2.3.4"));
    }

    @Test
    void testBlankIpReturnsNull() throws Exception {
        assertNull(resolveMac(""));
        assertNull(resolveMac("   "));
    }

    // -----------------------------
    // runCommandAndParse() test
    // -----------------------------

    @Test
    void testRunCommandAndParse() throws Exception {
        macResolver.testClearCache();

        String fakeLine = "192.168.1.77 aa:bb:cc:dd:ee:ff";
        String prop = System.getProperty("os.name");
        assertNotNull(prop);

        if (prop.toLowerCase().contains("win")) {
            runCommand("cmd", "/c", "echo " + fakeLine);
        } else {
            runCommand("echo", fakeLine);
        }

        assertEquals("AA:BB:CC:DD:EE:FF", macResolver.testGetCached("192.168.1.77"));
    }

    @Test
    void testListenerIsNotifiedOnMacResolution() throws Exception {
        macResolver.testClearCache();

        // --- Arrange ---
        String ip = "192.168.10.20";
        String mac = "AA:BB:CC:DD:EE:FF";

        // Use a latch to wait for async callback
        CountDownLatch latch = new CountDownLatch(1);

        final String[] callbackIp = new String[1];
        final String[] callbackMac = new String[1];

        MacResolverListener listener = (resolvedIp, resolvedMac) -> {
            callbackIp[0] = resolvedIp;
            callbackMac[0] = resolvedMac;
            latch.countDown();
        };

        macResolver.addMacResolverListener(listener);

        try {
            // --- Act ---
            // Simulate ARP output line that parseLine() would see
            String arpLine = ip + "    aa:bb:cc:dd:ee:ff";
            invokePrivate("parseLine", String.class, arpLine);

            // Wait for listener to be notified (async)
            boolean notified = latch.await(1, TimeUnit.SECONDS);

            // --- Assert ---
            assertTrue(notified, "Listener should have been notified");
            assertEquals(ip, callbackIp[0]);
            assertEquals(mac, callbackMac[0]);

        } finally {
            macResolver.removeMacResolverListener(listener);
        }
    }

    @Test
    void testListenerNotNotifiedTwiceForSameIp() throws Exception {
        macResolver.testClearCache();

        String ip = "10.0.0.99";

        AtomicInteger counter = new AtomicInteger(0);

        MacResolverListener listener = (resolvedIp, resolvedMac) -> counter.incrementAndGet();
        macResolver.addMacResolverListener(listener);

        try {
            // Simulate two ARP lines for the same IP
            invokePrivate("parseLine", String.class, ip + " aa:bb:cc:dd:ee:ff");
            invokePrivate("parseLine", String.class, ip + " aa:bb:cc:dd:ee:ff");

            // Give async notifications time to fire
            Thread.sleep(100);

            assertEquals(1, counter.get(), "Listener should be notified exactly once");

        } finally {
            macResolver.removeMacResolverListener(listener);
        }
    }

    // -------------------------------------------------------
    // Reflection helpers to call private static methods
    // -------------------------------------------------------

    private Object invokePrivate(String method, Class<?> paramType, Object arg) throws Exception {
        Method m = MacResolver.class.getDeclaredMethod(method, paramType);
        m.setAccessible(true);
        return m.invoke(macResolver, arg);
    }

    private void parseLine(String line) throws Exception {
        invokePrivate("parseLine", String.class, line);
    }

    private String normalize(String mac) throws Exception {
        return (String) invokePrivate("normalizeMac", String.class, mac);
    }

    private boolean isValid(String mac) throws Exception {
        return (boolean) invokePrivate("isValidMac", String.class, mac);
    }

    private String resolveMac(String ip) throws Exception {
        return (String) invokePrivate("resolveMac", String.class, ip);
    }

    private void runCommand(String... cmd) throws Exception {
        Method m = MacResolver.class.getDeclaredMethod("runCommandAndParse", String[].class);
        m.setAccessible(true);
        m.invoke(macResolver, (Object) cmd);
    }
}
