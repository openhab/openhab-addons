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
package org.openhab.binding.candy.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Configuration boundary checks, including rejection of URL injection.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class BiancaConfigurationTest {
    @Test
    void acceptsNetworkAddressesWithoutChangingTheRoute() {
        for (String host : List.of("washer.local", "192.0.2.10", "2001:db8::1", "[::1]")) {
            BiancaConfiguration configuration = validConfiguration();
            configuration.hostName = host;
            assertTrue(configuration.validate().isEmpty(), host);
            URI statusUri = configuration.statusUri().orElseThrow();
            assertEquals("/http-read.json", statusUri.getPath());
            assertNull(statusUri.getQuery());
            assertNull(statusUri.getUserInfo());
            assertEquals(-1, statusUri.getPort());
        }
    }

    @Test
    void rejectsUrlsPortsAndCredentialInjection() {
        for (String host : List.of("", " ", "http://washer.local", "washer.local:8080", "user:secret@washer.local",
                "washer.local/path", "washer.local?secret=1", "washer.local#fragment", "washer.local\r\nHost: other")) {
            BiancaConfiguration configuration = validConfiguration();
            configuration.hostName = host;
            assertEquals(Optional.of("@text/config-error-host"), configuration.validate(), host);
            assertTrue(configuration.statusUri().isEmpty(), host);
        }
    }

    @Test
    void validatesKeyAndPollBounds() {
        BiancaConfiguration configuration = validConfiguration();
        configuration.encryptionKey = "";
        assertEquals(Optional.of("@text/config-error-account"), configuration.validate());
        configuration.encryptionKey = "short";
        assertEquals(Optional.of("@text/config-error-key"), configuration.validate());
        configuration.encryptionKey = "abcdefghijklmnop";
        configuration.refreshInterval = 14;
        assertEquals(Optional.of("@text/config-error-interval"), configuration.validate());
        configuration.refreshInterval = 15;
        configuration.requestTimeout = 11;
        assertEquals(Optional.of("@text/config-error-interval"), configuration.validate());
        configuration.requestTimeout = 1;
        assertTrue(configuration.validate().isEmpty());
    }

    @Test
    void acceptsAccountCredentialsWithoutAManualKey() {
        BiancaConfiguration configuration = validConfiguration();
        configuration.encryptionKey = "";
        configuration.username = "owner@example.test";
        configuration.password = "dummy-password";
        assertTrue(configuration.validate().isEmpty());
        configuration.password = "";
        assertEquals(Optional.of("@text/config-error-account"), configuration.validate());
        configuration.password = "dummy-password";
        configuration.applianceId = "../other";
        assertEquals(Optional.of("@text/config-error-appliance-id"), configuration.validate());
        configuration.applianceId = "washer-12";
        assertTrue(configuration.validate().isEmpty());
    }

    private BiancaConfiguration validConfiguration() {
        BiancaConfiguration configuration = new BiancaConfiguration();
        configuration.hostName = "washer.local";
        configuration.encryptionKey = "abcdefghijklmnop";
        return configuration;
    }
}
