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
package org.openhab.binding.smartthings.internal.local;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Explicit credential and configuration validation tests.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class LocalApplianceConfigurationTest {
    @Test
    void acceptsImportedPskOrExplicitlyPinnedKeyStore() {
        LocalApplianceConfiguration config = LocalCoapTransportTest.configuration();
        assertDoesNotThrow(config::validate);
        config.ownerPsk = config.ownerPsk.repeat(2);
        config.localPort = 65535;
        config.port = 65535;
        assertDoesNotThrow(config::validate);
        config.ownerPsk = "";
        config.ownerId = "";
        config.keyStore = "client.p12";
        config.serverFingerprint = "ab:".repeat(31) + "ab";
        assertDoesNotThrow(config::validate);
        assertEquals(32, config.fingerprint().length);
    }

    @Test
    void rejectsMissingMixedInvalidAndNonCanonicalCredentialsWithoutEchoingThem() {
        LocalApplianceConfiguration config = LocalCoapTransportTest.configuration();
        for (String secret : List.of("private-value", "xx".repeat(16), "a", "00".repeat(15), "00".repeat(33))) {
            config.ownerPsk = secret;
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, config::validate);
            if (secret.length() > 1) {
                assertFalse(error.toString().contains(secret));
            }
            assertNull(error.getCause());
        }
        config.ownerPsk = "00".repeat(16);
        for (String identity : List.of("secret-identity", "1-1-1-1-1", "00000000-0000-0000-0000-000000000000")) {
            config.ownerId = identity;
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, config::validate);
            assertFalse(error.toString().contains(identity));
            assertNull(error.getCause());
        }
        config.ownerId = LocalCoapTransportTest.configuration().ownerId;
        config.keyStore = "client.p12";
        assertThrows(IllegalArgumentException.class, config::validate);
        config.ownerPsk = "";
        config.serverFingerprint = "";
        assertThrows(IllegalArgumentException.class, config::validate);
        config.serverFingerprint = "invalid-secret-fingerprint";
        var error = assertThrows(IllegalArgumentException.class, config::validate);
        assertFalse(error.toString().contains(config.serverFingerprint));
    }

    @Test
    void boundsPortsTimeoutRefreshAndHost() {
        LocalApplianceConfiguration config = LocalCoapTransportTest.configuration();
        for (int invalid : List.of(-1, 65536)) {
            config.port = invalid;
            assertThrows(IllegalArgumentException.class, config::validate);
            config.port = 0;
            config.localPort = invalid;
            assertThrows(IllegalArgumentException.class, config::validate);
            config.localPort = 0;
        }
        for (int invalid : List.of(0, 61)) {
            config.timeout = invalid;
            assertThrows(IllegalArgumentException.class, config::validate);
        }
        config.timeout = 12;
        config.refreshInterval = 9;
        assertThrows(IllegalArgumentException.class, config::validate);
        config.refreshInterval = 10;
        for (String invalid : List.of("", "coaps://device", "user@device", "device/path", "device host")) {
            config.host = invalid;
            assertThrows(IllegalArgumentException.class, config::validate);
        }
        config.host = "::1";
        assertDoesNotThrow(config::validate);
    }
}
