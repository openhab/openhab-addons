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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the {@link MacResolver} class.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestMacParser {

    @BeforeEach
    void setup() throws Exception {
        MacResolver.clearCacheForTests();
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
        MacResolver.clearCacheForTests();

        String line = "192.168.1.10    0x1 0x2  aa:bb:cc:dd:ee:ff  *  br0";
        parseLine(line);

        assertEquals("AA:BB:CC:DD:EE:FF", MacResolver.getCachedForTests("192.168.1.10"));
    }

    @Test
    void testParseLineWindowsStyle() throws Exception {
        MacResolver.clearCacheForTests();

        String line = "  192.168.1.50       aa-bb-cc-dd-ee-ff     dynamic";
        parseLine(line);

        assertEquals("AA:BB:CC:DD:EE:FF", MacResolver.getCachedForTests("192.168.1.50"));
    }

    @Test
    void testParseLineIgnoresInvalid() throws Exception {
        MacResolver.clearCacheForTests();

        parseLine("this is not an arp entry");
        parseLine("999.999.999.999 aa:bb:cc:dd:ee:ff");

        assertTrue(MacResolver.cacheIsEmptyForTests());
    }

    // -----------------------------
    // Cache behaviour
    // -----------------------------

    @Test
    void testCacheHitShortCircuitsLookup() throws Exception {
        MacResolver.clearCacheForTests();
        MacResolver.putCachedForTests("10.0.0.5", "AA:BB:CC:DD:EE:FF", Instant.now().plusSeconds(60));

        String mac = getMacInternal("10.0.0.5");

        assertEquals("AA:BB:CC:DD:EE:FF", mac);
    }

    @Test
    void testCacheExpiry() throws Exception {
        MacResolver.clearCacheForTests();

        MacResolver.putCachedForTests("1.2.3.4", "AA:BB:CC:DD:EE:FF", Instant.now().minusSeconds(120));

        assertNull(MacResolver.getCachedForTests("1.2.3.4"));
    }

    @Test
    void testBlankIpReturnsNull() throws Exception {
        assertNull(getMacInternal(""));
        assertNull(getMacInternal("   "));
    }

    // -----------------------------
    // runCommandAndParse() test
    // -----------------------------

    @Test
    void testRunCommandAndParse() throws Exception {
        MacResolver.clearCacheForTests();

        String fakeLine = "192.168.1.77 aa:bb:cc:dd:ee:ff";

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            runCommand("cmd", "/c", "echo " + fakeLine);
        } else {
            runCommand("echo", fakeLine);
        }

        assertEquals("AA:BB:CC:DD:EE:FF", MacResolver.getCachedForTests("192.168.1.77"));
    }

    // -------------------------------------------------------
    // Reflection helpers to call private static methods
    // -------------------------------------------------------

    private static Object invokePrivate(String method, Class<?> paramType, Object arg) throws Exception {
        Method m = MacResolver.class.getDeclaredMethod(method, paramType);
        m.setAccessible(true);
        return m.invoke(null, arg);
    }

    private static void parseLine(String line) throws Exception {
        invokePrivate("parseLine", String.class, line);
    }

    private static String normalize(String mac) throws Exception {
        return (String) invokePrivate("normalizeMac", String.class, mac);
    }

    private static boolean isValid(String mac) throws Exception {
        return (boolean) invokePrivate("isValidMac", String.class, mac);
    }

    private static String getMacInternal(String ip) throws Exception {
        return (String) invokePrivate("getMacFromIpInternal", String.class, ip);
    }

    private static void runCommand(String... cmd) throws Exception {
        Method m = MacResolver.class.getDeclaredMethod("runCommandAndParse", String[].class);
        m.setAccessible(true);
        m.invoke(null, (Object) cmd);
    }
}
