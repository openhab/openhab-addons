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
package org.openhab.binding.smartthings.internal.ocf;

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
class ApplianceConfigurationTest {
    @Test
    void acceptsImportedPskOrExplicitlyPinnedKeyStore() {
        ApplianceConfiguration config = CoapTransportTest.configuration();
        assertDoesNotThrow(config::validate);
        config.ownerPsk = config.ownerPsk.repeat(2);
        config.clientPort = 65535;
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
        ApplianceConfiguration config = CoapTransportTest.configuration();
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
        config.ownerId = CoapTransportTest.configuration().ownerId;
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
    void acceptsAutomaticIdentityWithCaTrustOrAnExplicitPinAndOptionalImportedIdentity() {
        ApplianceConfiguration config = new ApplianceConfiguration();
        config.host = "127.0.0.1";
        assertDoesNotThrow(config::validate);
        config.serverFingerprint = "ab".repeat(32);
        assertDoesNotThrow(config::validate);
        config.serverFingerprint = "";
        config.keyStore = "client.p12";
        config.keyStorePassword = "test-password";
        assertDoesNotThrow(config::validate);
        config.keyStore = "";
        assertThrows(IllegalArgumentException.class, config::validate);
        config.keyStorePassword = "";
        config.ownerId = "01234567-89ab-cdef-0123-456789abcdef";
        assertThrows(IllegalArgumentException.class, config::validate);
    }

    @Test
    void boundsPortsTimeoutRefreshAndHost() {
        ApplianceConfiguration config = CoapTransportTest.configuration();
        for (int invalid : List.of(-1, 65536)) {
            config.port = invalid;
            assertThrows(IllegalArgumentException.class, config::validate);
            config.port = 0;
            config.clientPort = invalid;
            assertThrows(IllegalArgumentException.class, config::validate);
            config.clientPort = 0;
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
