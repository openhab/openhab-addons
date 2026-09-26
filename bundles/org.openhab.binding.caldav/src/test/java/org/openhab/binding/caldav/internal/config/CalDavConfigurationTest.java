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
package org.openhab.binding.caldav.internal.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests optional account credentials and configuration validation.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
class CalDavConfigurationTest {
    @Test
    void acceptsAnonymousAndCompleteCredentialsForEveryAuthType() {
        for (String authType : List.of("AUTO", "BASIC", "DIGEST")) {
            AccountConfiguration config = new AccountConfiguration();
            config.url = "https://caldav.example.test/";
            config.authType = authType;
            assertDoesNotThrow(() -> CalDavConfiguration.validate(config));
            config.username = " ";
            config.password = " ";
            assertDoesNotThrow(() -> CalDavConfiguration.validate(config));
            config.username = "user";
            config.password = "secret";
            assertDoesNotThrow(() -> CalDavConfiguration.validate(config));
        }
    }

    @Test
    void rejectsIncompleteCredentials() {
        for (String blank : List.of("", " ")) {
            AccountConfiguration config = new AccountConfiguration();
            config.url = "https://caldav.example.test/";
            config.username = "user";
            config.password = blank;
            assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
            config.username = blank;
            config.password = "secret";
            assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
        }
    }

    @Test
    void anonymousAccessStillRequiresValidAccountSettings() {
        AccountConfiguration config = new AccountConfiguration();
        config.url = "https://caldav.example.test/";
        config.authType = "NONE";
        assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
        config.authType = "AUTO";
        config.requestTimeout = 0;
        assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
    }
}
