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
package org.openhab.binding.volvocars.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.TOKEN_URL;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.volvocars.internal.config.AccountConfiguration;
import org.openhab.binding.volvocars.internal.servlet.VolvoCarsAuthServletTestAccess;
import org.openhab.core.test.storage.VolatileStorage;

/**
 * Tests for {@link VolvoCarsOAuthClient}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class VolvoCarsOAuthClientTest {
    private static final String PREFIX = "volvocars:account:test";
    private static final String TOKEN_RESPONSE = """
            {"access_token": "access-1", "refresh_token": "refresh-1", "token_type": "Bearer", "expires_in": 1799}
            """;

    private final HttpMocks http = new HttpMocks();
    private final VolatileStorage<String> storage = new VolatileStorage<>();
    private final AccountConfiguration config = new AccountConfiguration();
    private Instant now = Instant.parse("2026-01-01T00:00:00Z");

    private final Clock clock = new Clock() {
        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(@Nullable ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    };

    @BeforeEach
    void setUp() {
        config.clientId = "client";
        config.clientSecret = "secret";
        config.apiKey = "key";
        config.redirectUri = "http://openhab:8080/volvocars";
    }

    private VolvoCarsOAuthClient createClient() {
        return new VolvoCarsOAuthClient(http.httpClient, storage, PREFIX, config, clock);
    }

    @Test
    void codeChallengeMatchesRfc7636Example() {
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
                VolvoCarsOAuthClient.computeCodeChallenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
    }

    @Test
    void authorizationUrlContainsPkceParameters() {
        Map<String, String> query = VolvoCarsAuthServletTestAccess.parseQuery(createClient().createAuthorizationUrl());

        assertEquals("code", query.get("response_type"));
        assertEquals("client", query.get("client_id"));
        assertEquals("http://openhab:8080/volvocars", query.get("redirect_uri"));
        assertEquals("S256", query.get("code_challenge_method"));
        assertNotNull(query.get("code_challenge"));
        assertTrue(query.getOrDefault("scope", "").contains("conve:doors_status"));
        assertTrue(query.getOrDefault("scope", "").contains("location:read"));
    }

    @Test
    void customScopesAreUsed() {
        config.scopes = " openid conve:odometer_status ";
        Map<String, String> query = VolvoCarsAuthServletTestAccess.parseQuery(createClient().createAuthorizationUrl());

        assertEquals("openid conve:odometer_status", query.get("scope"));
    }

    @Test
    void authorizeStoresRefreshToken() throws VolvoCarsException {
        VolvoCarsOAuthClient client = createClient();
        String state = stateOf(client.createAuthorizationUrl());
        http.respond(TOKEN_URL, 200, TOKEN_RESPONSE);

        assertFalse(client.isAuthorized());
        client.authorize("code", state);

        assertTrue(client.isAuthorized());
        assertFalse(client.isPendingState(state));
        assertEquals("access-1", client.getAccessToken());
        assertEquals("refresh-1", storage.get(PREFIX + ".refreshToken"));
    }

    @Test
    void authorizeRejectsUnknownState() {
        VolvoCarsOAuthClient client = createClient();
        client.createAuthorizationUrl();

        assertThrows(VolvoCarsException.class, () -> client.authorize("code", "other-state"));
        assertTrue(http.requestedUrls.isEmpty());
    }

    @Test
    void pendingAuthorizationExpires() {
        VolvoCarsOAuthClient client = createClient();
        String state = stateOf(client.createAuthorizationUrl());
        now = now.plus(Duration.ofMinutes(16));

        assertFalse(client.isPendingState(state));
        assertThrows(VolvoCarsException.class, () -> client.authorize("code", state));
    }

    @Test
    void accessTokenIsRefreshedWhenExpiredAndRotatedRefreshTokenIsStored() throws VolvoCarsException {
        VolvoCarsOAuthClient client = createClient();
        String state = stateOf(client.createAuthorizationUrl());
        http.respond(TOKEN_URL, 200, TOKEN_RESPONSE);
        client.authorize("code", state);

        http.respond(TOKEN_URL, 200, """
                {"access_token": "access-2", "refresh_token": "refresh-2", "expires_in": 1799}
                """);
        now = now.plus(Duration.ofMinutes(10));
        assertEquals("access-1", client.getAccessToken());

        now = now.plus(Duration.ofMinutes(20));
        assertEquals("access-2", client.getAccessToken());
        assertEquals("refresh-2", storage.get(PREFIX + ".refreshToken"));
    }

    @Test
    void rejectedRefreshTokenRequiresNewAuthorization() throws VolvoCarsException {
        VolvoCarsOAuthClient client = createClient();
        String state = stateOf(client.createAuthorizationUrl());
        http.respond(TOKEN_URL, 200, TOKEN_RESPONSE);
        client.authorize("code", state);
        client.invalidateAccessToken();

        http.respond(TOKEN_URL, 400, "{\"error\": \"invalid_grant\"}");
        VolvoCarsException e = assertThrows(VolvoCarsException.class, client::getAccessToken);

        assertTrue(e.isAuthorizationRequired());
        assertFalse(client.isAuthorized());
        assertNull(storage.get(PREFIX + ".refreshToken"));
    }

    @Test
    void temporaryTokenEndpointFailureKeepsAuthorization() throws VolvoCarsException {
        VolvoCarsOAuthClient client = createClient();
        String state = stateOf(client.createAuthorizationUrl());
        http.respond(TOKEN_URL, 200, TOKEN_RESPONSE);
        client.authorize("code", state);
        client.invalidateAccessToken();

        http.respond(TOKEN_URL, 503, "");
        VolvoCarsException e = assertThrows(VolvoCarsException.class, client::getAccessToken);

        assertFalse(e.isAuthorizationRequired());
        assertTrue(client.isAuthorized());
    }

    @Test
    void changedClientIdDiscardsTokens() throws VolvoCarsException {
        VolvoCarsOAuthClient client = createClient();
        String state = stateOf(client.createAuthorizationUrl());
        http.respond(TOKEN_URL, 200, TOKEN_RESPONSE);
        client.authorize("code", state);

        config.clientId = "other-client";
        assertFalse(createClient().isAuthorized());
        assertNull(storage.get(PREFIX + ".refreshToken"));
    }

    private static String stateOf(String authorizationUrl) {
        String state = VolvoCarsAuthServletTestAccess.parseQuery(authorizationUrl).get("state");
        assertNotNull(state);
        return state;
    }
}
