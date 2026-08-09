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
package org.openhab.binding.dreame.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;

@NonNullByDefault
class DreameAuthenticationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T18:00:00Z"), ZoneOffset.UTC);

    @Test
    void loginStoresSessionAndNormalizesRegion() throws DreameCloudException {
        DreameAuthenticationService authentication = new DreameAuthenticationService(CLOCK);

        authentication.login("user@example.com", "secret", "de", body -> {
            assertTrue(body.contains("country=DE&lang=de"));
            return JsonParser.parseString("""
                    {"access_token":"access","refresh_token":"refresh","tenant_id":"tenant",
                     "uid":"user","expires_in":7200,"region":"eu"}
                    """).getAsJsonObject();
        });

        assertEquals("eu", authentication.country());
        assertEquals("access", authentication.accessToken());
        assertEquals("tenant", authentication.tenantId());
        assertEquals("user", authentication.userId());
    }

    @Test
    void expiredAccessTokenIsRefreshed() throws DreameCloudException {
        DreameAuthenticationService authentication = new DreameAuthenticationService(CLOCK);
        authentication.login("user", "secret", "eu", body -> JsonParser.parseString("""
                {"access_token":"old","refresh_token":"refresh-old","expires_in":1,"region":"eu"}
                """).getAsJsonObject());
        String[] refreshBody = { "" };

        authentication.ensureAuthenticated(body -> {
            refreshBody[0] = body;
            return JsonParser.parseString("""
                    {"access_token":"new","refresh_token":"refresh-new","expires_in":7200,"region":"eu"}
                    """).getAsJsonObject();
        });

        assertTrue(refreshBody[0].contains("grant_type=refresh_token"));
        assertTrue(refreshBody[0].contains("refresh_token=refresh-old"));
        assertEquals("new", authentication.accessToken());
    }

    @Test
    void logoutClearsAuthenticationState() throws DreameCloudException {
        DreameAuthenticationService authentication = new DreameAuthenticationService(CLOCK);
        authentication.login("user", "secret", "eu", body -> JsonParser.parseString("""
                {"access_token":"access","refresh_token":"refresh","expires_in":7200,"region":"eu"}
                """).getAsJsonObject());

        authentication.logout();

        assertThrows(DreameCloudException.class,
                () -> authentication.ensureAuthenticated(body -> JsonParser.parseString("{}").getAsJsonObject()));
        assertEquals("", authentication.accessToken());
        assertEquals("000000", authentication.tenantId());
    }

    @Test
    void invalidLoginResponseIsRejected() {
        DreameAuthenticationService authentication = new DreameAuthenticationService(CLOCK);

        assertThrows(DreameCloudException.class, () -> authentication.login("user", "secret", "eu",
                body -> JsonParser.parseString("{}").getAsJsonObject()));
    }
}
