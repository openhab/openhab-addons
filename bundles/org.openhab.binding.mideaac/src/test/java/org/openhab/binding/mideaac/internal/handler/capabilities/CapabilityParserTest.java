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

package org.openhab.binding.mideaac.internal.handler.capabilities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.handler.capabilities.CapabilityParser.CapabilityId;

/**
 * The {@link CapabilityParser} parses the capability Response.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CapabilityParserTest {

    @Test
    void testParseWithValidPayload() {
        // Arrange: Create a valid payload with known capabilities
        byte[] payload = new byte[] { (byte) 0xB5, 0x03, // Header and count (3 capabilities)
                0x14, 0x02, 0x01, 0x01, // Capability 1 (0x0214 = MODES, value = 1)
                0x16, 0x02, 0x01, 0x02, // Capability 2 (0x0216 = ENERGY, value = 2)
                0x1A, 0x02, 0x01, 0x03, // Capability 3 (0x021A = PRESET_TURBO, value = 3)
                (byte) 0xDE, (byte) 0xDF // CRC Check (trailing bytes)
        };

        CapabilityParser parser = new CapabilityParser();

        // Act: Parse the payload
        parser.parse(payload);

        // Assert: Check the parsed results
        Map<CapabilityId, Map<String, Boolean>> capabilities = parser.getCapabilities();
        assertNotNull(capabilities, "Capabilities map should not be null");

        // Check individual capabilities
        assertTrue(capabilities.containsKey(CapabilityId.MODES), "Should contain MODES capability");
        Optional.ofNullable(capabilities.get(CapabilityId.MODES)).map(modes -> modes.get("heat_mode"))
                .ifPresent(value -> assertEquals(true, value, "MODES - heat_mode should be true"));

        assertTrue(capabilities.containsKey(CapabilityId.ENERGY), "Should contain ENERGY capability");
        Optional.ofNullable(capabilities.get(CapabilityId.ENERGY)).map(modes -> modes.get("energy_stats"))
                .ifPresent(value -> assertEquals(true, value, "ENERGY - energy_stats should be true"));

        assertTrue(capabilities.containsKey(CapabilityId.PRESET_TURBO), "Should contain PRESET_TURBO capability");
        Optional.ofNullable(capabilities.get(CapabilityId.PRESET_TURBO)).map(modes -> modes.get("turbo_heat"))
                .ifPresent(value -> assertEquals(true, value, "PRESET_TURBO - turbo_heat should be true"));
    }

    @Test
    void testParseWithEmptyPayload() {
        // Arrange: Create an empty payload
        byte[] payload = new byte[] {};

        CapabilityParser parser = new CapabilityParser();

        // Act: Parse the payload
        parser.parse(payload);

        // Assert: Ensure no capabilities are parsed
        assertTrue(parser.getCapabilities().isEmpty(), "Capabilities map should be empty for an empty payload");
    }

    @Test
    void testParseWithUnknownCapability() {
        // Arrange: Create a payload with an unknown capability
        byte[] payload = new byte[] { (byte) 0xB5, 0x01, // Header and count (1 capability)
                0x50, 0x50, 0x01, 0x01 // Unknown capability (0x5050)
        };

        CapabilityParser parser = new CapabilityParser();

        // Act: Parse the payload
        parser.parse(payload);

        // Assert: Ensure unknown capability is ignored
        Map<CapabilityId, Map<String, Boolean>> capabilities = parser.getCapabilities();
        assertTrue(capabilities.isEmpty(), "Capabilities map should not contain unknown capabilities");
    }

    @Test
    void testParseWithInvalidSize() {
        // Arrange: Create a payload with an invalid size
        byte[] payload = new byte[] { (byte) 0xB5, 0x01, // Header and count (1 capability)
                0x14, 0x02, 0x00 // Capability 1 (0x0214 = MODES, size = 0)
        };

        CapabilityParser parser = new CapabilityParser();

        // Act: Parse the payload
        parser.parse(payload);

        // Assert: Ensure no capabilities are parsed
        assertTrue(parser.getCapabilities().isEmpty(), "Capabilities map should be empty for invalid size");
    }

    @Test
    void testParseWithTrailingCRC() {
        // Arrange: Create a payload with trailing CRC
        byte[] payload = new byte[] { (byte) 0xB5, 0x07, // Header and count (7 capabilities)
                0x12, 0x02, 0x01, 0x01, // Capability 1 (0x0212 = PRESET_ECO, value = 1)
                0x13, 0x02, 0x01, 0x01, // Capability 2 (0x0213 = PRESET_FREEZE_PROTECTION, value = 1)
                0x14, 0x02, 0x01, 0x01, // Capability 3 (0x0214 = MODES, value = 1)
                0x15, 0x02, 0x01, 0x01, // Capability 4 (0x0215 = SWING_MODES, value = 1)
                0x16, 0x02, 0x01, 0x01, // Capability 5 (0x0216 = ENERGY, value = 1)
                0x17, 0x02, 0x01, 0x00, // Capability 6 (0x0217 = FILTER_REMIND, value = 0)
                0x1A, 0x02, 0x01, 0x01, // Capability 7 (0x021A = PRESET_TURBO, value = 1)
                (byte) 0xDE, (byte) 0xDF // CRC Check (trailing bytes)
        };

        CapabilityParser parser = new CapabilityParser();

        // Act: Parse the payload
        parser.parse(payload);

        // Assert: Verify capabilities are parsed correctly
        Map<CapabilityId, Map<String, Boolean>> capabilities = parser.getCapabilities();
        assertNotNull(capabilities, "Capabilities map should not be null");

        // Verify specific capabilities
        assertTrue(capabilities.containsKey(CapabilityId.PRESET_ECO), "Should contain PRESET_ECO capability");
        Optional.ofNullable(capabilities.get(CapabilityId.PRESET_ECO)).map(modes -> modes.get("eco"))
                .ifPresent(value -> assertEquals(true, value, "PRESET_ECO - eco should be true"));

        assertTrue(capabilities.containsKey(CapabilityId.MODES), "Should contain MODES capability");
        Optional.ofNullable(capabilities.get(CapabilityId.MODES)).map(modes -> modes.get("heat_mode"))
                .ifPresent(value -> assertEquals(true, value, "MODES - heat_mode should be true"));

        // Ensure CRC did not cause parsing issues
        assertFalse(parser.hasAdditionalCapabilities(), "No additional capabilities");
    }

    @Test
    void testParseWithtemperature() {
        // Arrange: Create a payload with trailing CRC
        byte[] payload = new byte[] { (byte) 0xB5, 0x08, // Header and count (7 capabilities)
                0x12, 0x02, 0x01, 0x01, // Capability 1 (0x0212 = PRESET_ECO, value = 1)
                0x13, 0x02, 0x01, 0x01, // Capability 2 (0x0213 = PRESET_FREEZE_PROTECTION, value = 1)
                0x14, 0x02, 0x01, 0x01, // Capability 3 (0x0214 = MODES, value = 1)
                0x15, 0x02, 0x01, 0x01, // Capability 4 (0x0215 = SWING_MODES, value = 1)
                0x16, 0x02, 0x01, 0x01, // Capability 5 (0x0216 = ENERGY, value = 1)
                0x17, 0x02, 0x01, 0x00, // Capability 6 (0x0217 = FILTER_REMIND, value = 0)
                0x1A, 0x02, 0x01, 0x01, // Capability 7 (0x021A = PRESET_TURBO, value = 1)
                0x25, 0x02, 0x07, 0x20, 0x3c, 0x20, 0x3c, 0x20, 0x3c, 0x00, // Temperature
                (byte) 0xDE, (byte) 0xDF // CRC Check (trailing bytes)
        };

        CapabilityParser parser = new CapabilityParser();

        // Act: Parse the payload
        parser.parse(payload);

        // Assert: Verify capabilities are parsed correctly
        Map<CapabilityId, Map<String, Double>> numericCapabilities = parser.getNumericCapabilities();
        assertNotNull(numericCapabilities, "Capabilities map should not be null");

        Optional.ofNullable(numericCapabilities.get(CapabilityId.TEMPERATURES))
                .map(modes -> modes.get("cool_min_temperature")).ifPresent(value -> assertEquals(16.0, value));
        Optional.ofNullable(numericCapabilities.get(CapabilityId.TEMPERATURES))
                .map(modes -> modes.get("heat_max_temperature")).ifPresent(value -> assertEquals(30.0, value));
    }
}
