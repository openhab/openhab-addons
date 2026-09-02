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
package org.openhab.binding.jellyfin.internal.util.discovery;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DeviceIdSanitizer}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
class DeviceIdSanitizerTest {

    @Test
    void testAlphanumericAndAllowedCharsUnchanged() {
        assertEquals("abc-XYZ_123", DeviceIdSanitizer.sanitize("abc-XYZ_123"),
                "Alphanumeric characters, hyphens and underscores must not be modified");
    }

    @Test
    void testColonReplacedWithHyphen() {
        assertEquals("device-id", DeviceIdSanitizer.sanitize("device:id"));
    }

    @Test
    void testSlashReplacedWithHyphen() {
        assertEquals("path-to-device", DeviceIdSanitizer.sanitize("path/to/device"));
    }

    @Test
    void testAtSignReplacedWithHyphen() {
        assertEquals("user-host", DeviceIdSanitizer.sanitize("user@host"));
    }

    @Test
    void testMultipleSpecialCharsAllReplaced() {
        assertEquals("device-with-special-chars-", DeviceIdSanitizer.sanitize("device:with/special@chars!"));
    }

    @Test
    void testSpaceReplacedWithHyphen() {
        assertEquals("My-Device", DeviceIdSanitizer.sanitize("My Device"));
    }

    @Test
    void testEmptyStringReturnedUnchanged() {
        assertEquals("", DeviceIdSanitizer.sanitize(""));
    }

    @Test
    void testAlreadySanitizedStringUnchanged() {
        assertEquals("already-clean_123", DeviceIdSanitizer.sanitize("already-clean_123"));
    }

    @Test
    void testUnicodeCharactersReplaced() {
        assertEquals("caf-", DeviceIdSanitizer.sanitize("caf\u00e9"));
    }

    @Test
    void testDotReplacedWithHyphen() {
        assertEquals("192-168-1-1", DeviceIdSanitizer.sanitize("192.168.1.1"));
    }
}
