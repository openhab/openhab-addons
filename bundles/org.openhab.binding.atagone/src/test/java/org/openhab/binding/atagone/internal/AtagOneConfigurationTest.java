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
package org.openhab.binding.atagone.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.atagone.internal.AtagOneConfiguration.ConnectionType;

/**
 * Tests the connection choice in {@link AtagOneConfiguration}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
class AtagOneConfigurationTest {

    private static AtagOneConfiguration config(String connection, String hostname, String email, String password) {
        AtagOneConfiguration config = new AtagOneConfiguration();
        config.connection = connection;
        config.hostname = hostname;
        config.email = email;
        config.password = password;
        return config;
    }

    private static @Nullable ConnectionType type(String name) {
        return "NONE".equals(name) ? null : ConnectionType.valueOf(name);
    }

    @ParameterizedTest(name = "{0}, host \"{1}\", email \"{2}\", password \"{3}\" -> {4}")
    @CsvSource({
            // auto prefers the portal whenever the account is complete
            "auto, '', a@b.c, secret, REMOTE", //
            "auto, 10.0.0.2, a@b.c, secret, REMOTE", //
            "auto, 10.0.0.2, '', '', LOCAL", //
            "auto, 10.0.0.2, a@b.c, '', LOCAL", //
            "auto, '', '', '', NONE", //
            "auto, ' ', a@b.c, ' ', NONE", //
            // local and remote name their prerequisites and ignore the other side
            "local, 10.0.0.2, a@b.c, secret, LOCAL", //
            "local, '', a@b.c, secret, NONE", //
            "remote, 10.0.0.2, a@b.c, secret, REMOTE", //
            "remote, 10.0.0.2, '', '', NONE", //
            "remote, 10.0.0.2, a@b.c, '', NONE" })
    void resolvesTheConnection(String connection, String hostname, String email, String password, String expected) {
        assertEquals(type(expected), config(connection, hostname, email, password).resolveConnectionType());
    }

    @ParameterizedTest(name = "{0}, email \"{1}\", password \"{2}\" -> incomplete {3}")
    @CsvSource({ "auto, a@b.c, '', true", "auto, '', secret, true", "auto, a@b.c, secret, false", "auto, '', '', false",
            "auto, ' ', ' ', false", "local, a@b.c, '', false", "remote, a@b.c, '', false" })
    void judgesTheCredentialsIncompleteOnlyForAuto(String connection, String email, String password, boolean expected) {
        assertEquals(expected, config(connection, "10.0.0.2", email, password).hasIncompleteCredentials());
    }

    @ParameterizedTest(name = "{0}, host \"{1}\", email \"{2}\", password \"{3}\" -> local details {4}")
    @CsvSource({ "auto, 10.0.0.2, a@b.c, secret, true", "auto, '', a@b.c, secret, false",
            "auto, 10.0.0.2, '', '', false", "auto, 10.0.0.2, a@b.c, '', false",
            "remote, 10.0.0.2, a@b.c, secret, false", "local, 10.0.0.2, a@b.c, secret, false" })
    void readsTheLocalDetailsOnlyForAutoWithBothSettings(String connection, String hostname, String email,
            String password, boolean expected) {
        assertEquals(expected, config(connection, hostname, email, password).usesLocalDetails());
    }

    @Test
    void defaultsKeepExistingHostnameOnlyConfigsOnLocal() {
        AtagOneConfiguration config = new AtagOneConfiguration();
        config.hostname = "10.0.0.2";
        assertEquals(ConnectionType.LOCAL, config.resolveConnectionType());
    }

    @Test
    void missingSettingsMessageFollowsTheChosenConnection() {
        assertEquals("@text/offline.conf-error.hostname-missing",
                AtagOneHandler.missingSettingsMessage(config("local", "", "", "")));
        assertEquals("@text/offline.conf-error.credentials-missing",
                AtagOneHandler.missingSettingsMessage(config("remote", "", "", "")));
        assertEquals("@text/offline.conf-error.connection-missing",
                AtagOneHandler.missingSettingsMessage(config("auto", "", "", "")));
        assertEquals("@text/offline.conf-error.credentials-incomplete",
                AtagOneHandler.missingSettingsMessage(config("auto", "10.0.0.2", "a@b.c", "")));
    }
}
