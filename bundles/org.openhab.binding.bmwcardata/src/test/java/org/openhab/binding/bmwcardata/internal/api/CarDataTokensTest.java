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
package org.openhab.binding.bmwcardata.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Response;
import org.junit.jupiter.api.Test;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;

/**
 * Tests the {@link CarDataTokens}.
 *
 * @author Mark Herwege - Initial contribution
 * @author Michael Weger - Adaptation to the BMW CarData binding
 */
@NonNullByDefault
class CarDataTokensTest {

    private static final Instant NOW = Instant.parse("2026-10-22T10:00:00Z");
    private static final Tokens TOKENS = new Tokens("access-secret", "refresh-secret", "id-secret", "gcid",
            NOW.plus(Duration.ofHours(1)));

    @Test
    void roundTrip() {
        AccessTokenResponse response = CarDataTokens.toAccessTokenResponse(TOKENS, NOW);

        assertEquals(NOW, response.getCreatedOn());
        assertEquals(3600, response.getExpiresIn());
        assertFalse(response.isExpired(NOW, 10));
        assertEquals(TOKENS, CarDataTokens.fromAccessTokenResponse(response, null, null));
    }

    @Test
    void refreshResponseWithoutBmwFieldsKeepsKnownValues() {
        // a refresh response as core stores it, in case BMW leaves out the gcid or id_token
        AccessTokenResponse refreshed = new AccessTokenResponse();
        refreshed.setAccessToken("access-2");
        refreshed.setRefreshToken("refresh-2");
        refreshed.setCreatedOn(NOW);
        refreshed.setExpiresIn(3600);

        Tokens tokens = CarDataTokens.fromAccessTokenResponse(refreshed, TOKENS, "gcid");
        assertNotNull(tokens);
        assertEquals("access-2", tokens.accessToken());
        assertEquals("id-secret", tokens.idToken());
        assertEquals("gcid", tokens.gcid());

        assertNull(CarDataTokens.fromAccessTokenResponse(refreshed, null, "gcid"));

        refreshed.setExtraFields(Map.of(CarDataTokens.ID_TOKEN, "id-2", CarDataTokens.GCID, "gcid-2"));
        tokens = CarDataTokens.fromAccessTokenResponse(refreshed, TOKENS, "gcid");
        assertNotNull(tokens);
        assertEquals("id-2", tokens.idToken());
        assertEquals("gcid-2", tokens.gcid());
    }

    @Test
    void responseWithoutCreationTimeIsRejected() {
        AccessTokenResponse response = CarDataTokens.toAccessTokenResponse(TOKENS, NOW);
        response.setCreatedOn(null);

        assertNull(CarDataTokens.fromAccessTokenResponse(response, TOKENS, "gcid"));
    }

    @Test
    void errorsMapToExceptions() {
        OAuthResponseException invalidGrant = new OAuthResponseException();
        invalidGrant.setError("invalid_grant");
        assertInstanceOf(BMWCarDataAuthException.class, CarDataTokens.toException(invalidGrant));

        OAuthResponseException serverError = new OAuthResponseException();
        serverError.setError("temporarily_unavailable");
        assertFalse(CarDataTokens.toException(serverError) instanceof BMWCarDataAuthException);
        assertFalse(CarDataTokens.toException(new OAuthResponseException()) instanceof BMWCarDataAuthException);

        assertInstanceOf(BMWCarDataAuthException.class, CarDataTokens.toException(new OAuthException("no token")));
    }

    @Test
    void unauthorizedResponseHiddenInIOExceptionRequiresReauthorization() {
        // core's HTTP client fails a 401 without WWW-Authenticate header, core wraps the failure in an IOException
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(401);
        IOException unauthorized = new IOException("oauth communication failed",
                new ExecutionException(new HttpResponseException("Authentication challenge", response)));
        assertInstanceOf(BMWCarDataAuthException.class, CarDataTokens.toException(unauthorized));

        IOException timeout = new IOException("timeout");
        assertFalse(CarDataTokens.toException(timeout) instanceof BMWCarDataAuthException);
    }

    @Test
    void tokensStayOutOfToString() {
        assertFalse(TOKENS.toString().contains("secret"), TOKENS.toString());
    }
}
