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
package org.openhab.binding.melcloud.internal.home.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MelCloudHomeAccountConfig#toString()}: the username is the account's email address and must
 * never appear unmasked.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeAccountConfigTest {

    @Test
    void whenCredentialsAreSetThenToStringRedactsThem() {
        // Arrange
        MelCloudHomeAccountConfig config = new MelCloudHomeAccountConfig();
        config.username = "user@example.com";
        config.password = "secret";

        // Act
        String result = config.toString();

        // Assert
        assertFalse(result.contains("user@example.com"));
        assertFalse(result.contains("secret"));
        assertEquals("[username=<redacted>, password=<redacted>, enableRealtimeUpdates=true]", result);
    }

    @Test
    void whenCredentialsAreEmptyThenToStringReportsThemAsEmpty() {
        // Arrange
        MelCloudHomeAccountConfig config = new MelCloudHomeAccountConfig();

        // Act
        String result = config.toString();

        // Assert
        assertEquals("[username=<empty>, password=<empty>, enableRealtimeUpdates=true]", result);
    }
}
