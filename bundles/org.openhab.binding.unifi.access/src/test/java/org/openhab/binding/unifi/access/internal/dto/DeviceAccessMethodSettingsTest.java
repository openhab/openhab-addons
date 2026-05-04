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
package org.openhab.binding.unifi.access.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests for {@link DeviceAccessMethodSettings} covering flat and wrapped
 * formats, Boolean and String enabled values, and the
 * {@link DeviceAccessMethodSettings#resolveAccessMethods()} normalizer.
 *
 * @author Dan Cunningham - Initial contribution
 */
class DeviceAccessMethodSettingsTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    // --- Flat format with Boolean enabled values ---

    @Test
    void flatFormatWithBooleanEnabled() {
        String json = """
                {
                    "nfc": {"enabled": true},
                    "pin_code": {"enabled": false, "pin_code_shuffle": true}
                }
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings);
        assertNotNull(settings.nfc);
        assertTrue(settings.nfc.isEnabled());

        assertNotNull(settings.pinCode);
        assertFalse(settings.pinCode.isEnabled());
        assertTrue(settings.pinCode.isShuffleEnabled());
    }

    // --- Wrapped format with access_methods ---

    @Test
    void wrappedFormatWithAccessMethods() {
        String json = """
                {
                    "access_methods": {
                        "nfc": {"enabled": "yes"}
                    },
                    "device_id": "abc123"
                }
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings);
        assertEquals("abc123", settings.deviceId);
        assertNotNull(settings.accessMethods);
        assertNotNull(settings.accessMethods.nfc);
        assertTrue(settings.accessMethods.nfc.isEnabled());
    }

    // --- String "yes"/"no" enabled values ---

    @Test
    void stringYesNoEnabledValues() {
        String json = """
                {
                    "nfc": {"enabled": "yes"},
                    "face": {"enabled": "no", "anti_spoofing_level": "high", "detect_distance": "near"}
                }
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings);
        assertNotNull(settings.nfc);
        assertTrue(settings.nfc.isEnabled());

        assertNotNull(settings.face);
        assertFalse(settings.face.isEnabled());
        assertEquals("high", settings.face.antiSpoofingLevel);
        assertEquals("near", settings.face.detectDistance);
    }

    // --- resolveAccessMethods() returns inner object when wrapped ---

    @Test
    void resolveAccessMethodsReturnsInnerWhenWrapped() {
        String json = """
                {
                    "access_methods": {
                        "nfc": {"enabled": true}
                    },
                    "device_id": "dev-001"
                }
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings);
        DeviceAccessMethodSettings resolved = settings.resolveAccessMethods();
        assertNotNull(resolved);
        assertSame(settings.accessMethods, resolved);
        assertNotNull(resolved.nfc);
        assertTrue(resolved.nfc.isEnabled());
    }

    // --- resolveAccessMethods() returns this when flat ---

    @Test
    void resolveAccessMethodsReturnsSelfWhenFlat() {
        String json = """
                {
                    "nfc": {"enabled": true}
                }
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings);
        assertNull(settings.accessMethods);
        DeviceAccessMethodSettings resolved = settings.resolveAccessMethods();
        assertSame(settings, resolved);
    }

    // --- isEnabled() for various inputs ---

    @Test
    void isEnabledReturnsTrueForBooleanTrue() {
        String json = """
                {"nfc": {"enabled": true}}
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings.nfc);
        assertTrue(settings.nfc.isEnabled());
    }

    @Test
    void isEnabledReturnsTrueForStringYes() {
        String json = """
                {"nfc": {"enabled": "yes"}}
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings.nfc);
        assertTrue(settings.nfc.isEnabled());
    }

    @Test
    void isEnabledReturnsTrueForStringTrue() {
        String json = """
                {"nfc": {"enabled": "true"}}
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings.nfc);
        assertTrue(settings.nfc.isEnabled());
    }

    @Test
    void isEnabledReturnsFalseForBooleanFalse() {
        String json = """
                {"nfc": {"enabled": false}}
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings.nfc);
        assertFalse(settings.nfc.isEnabled());
    }

    @Test
    void isEnabledReturnsFalseForStringNo() {
        String json = """
                {"nfc": {"enabled": "no"}}
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings.nfc);
        assertFalse(settings.nfc.isEnabled());
    }

    @Test
    void isEnabledReturnsFalseForStringFalse() {
        String json = """
                {"nfc": {"enabled": "false"}}
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings.nfc);
        assertFalse(settings.nfc.isEnabled());
    }

    @Test
    void isEnabledReturnsFalseForNull() {
        String json = """
                {"nfc": {}}
                """;

        DeviceAccessMethodSettings settings = gson.fromJson(json, DeviceAccessMethodSettings.class);

        assertNotNull(settings.nfc);
        assertFalse(settings.nfc.isEnabled());
    }
}
