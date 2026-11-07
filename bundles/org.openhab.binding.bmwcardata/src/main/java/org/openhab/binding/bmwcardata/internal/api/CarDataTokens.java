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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;

/**
 * The {@link CarDataTokens} converts between the binding's {@link Tokens} and the {@link AccessTokenResponse} of
 * openHAB's OAuth client service, which stores the tokens and refreshes them.
 * <p>
 * BMW's token response carries two non-standard fields needed for the stream: {@code id_token}, the stream password,
 * and {@code gcid}, the stream user name. Core keeps them in the extra fields of the response.
 *
 * @author Mark Herwege - Initial contribution
 * @author Michael Weger - Adaptation to the BMW CarData binding
 */
@NonNullByDefault
public final class CarDataTokens {

    static final String ID_TOKEN = "id_token";
    static final String GCID = "gcid";

    // the status of an unexpected response in the message of core's OAuthConnector
    private static final Pattern HTTP_STATUS_CODE = Pattern.compile("http code (\\d{3})");
    // BMW documents invalid_token for a 400 of the token endpoint, e.g. for an expired refresh token
    private static final Set<String> REAUTHORIZATION_ERRORS = Set.of("invalid_grant", "invalid_token", "expired_token",
            "access_denied", "unauthorized_client", "invalid_client");

    private CarDataTokens() {
        // utility class
    }

    /**
     * @param tokens tokens obtained from the device authorization
     * @param now time the tokens were issued, used by core to determine their expiry
     * @return the response to import into the OAuth client service
     */
    public static AccessTokenResponse toAccessTokenResponse(Tokens tokens, Instant now) {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(tokens.accessToken());
        response.setRefreshToken(tokens.refreshToken());
        response.setTokenType("Bearer");
        // without a creation time core treats the token as expired and refreshes it on every use
        response.setCreatedOn(now);
        response.setExpiresIn(Math.max(0, Duration.between(now, tokens.expiresAt()).toSeconds()));
        response.setExtraFields(Map.of(ID_TOKEN, tokens.idToken(), GCID, tokens.gcid()));
        return response;
    }

    /**
     * @param response response from the OAuth client service
     * @param previous the tokens before this response, whose id token is kept if the response has none
     * @param knownGcid gcid to use if the response does not carry one, e.g. after a refresh
     * @return the tokens, or {@code null} if the response lacks a required value
     */
    public static @Nullable Tokens fromAccessTokenResponse(AccessTokenResponse response, @Nullable Tokens previous,
            @Nullable String knownGcid) {
        String accessToken = response.getAccessToken();
        String refreshToken = response.getRefreshToken();
        String idToken = response.getExtraFields().get(ID_TOKEN);
        if ((idToken == null || idToken.isBlank()) && previous != null) {
            idToken = previous.idToken();
        }
        String gcid = response.getExtraFields().get(GCID);
        if (gcid == null || gcid.isBlank()) {
            gcid = knownGcid;
        }
        Instant createdOn = response.getCreatedOn();
        if (accessToken == null || refreshToken == null || idToken == null || gcid == null || createdOn == null) {
            return null;
        }
        return new Tokens(accessToken, refreshToken, idToken, gcid, createdOn.plusSeconds(response.getExpiresIn()));
    }

    /**
     * @param e error response of the token endpoint, relayed by the OAuth client service
     * @return a {@link BMWCarDataAuthException} if a new authorization is required, otherwise a
     *         {@link BMWCarDataException}
     */
    public static BMWCarDataException toException(OAuthResponseException e) {
        // the fields are filled by Gson, so they can be null despite their declaration
        String error = Objects.requireNonNullElse(e.getError(), "");
        String message = "Token refresh failed: " + (error.isBlank() ? "unknown error" : error);
        return REAUTHORIZATION_ERRORS.contains(error) ? new BMWCarDataAuthException(message, 0)
                : new BMWCarDataException(message);
    }

    /**
     * Maps a failure of the OAuth client service. Core's OAuthConnector reports a response other than 200, 400 and
     * 401 only in the message, e.g. {@code Bad http response, http code 503}. BMW's token endpoint was seen returning
     * server errors for a short time, which must not discard a valid authorization.
     *
     * @param e failure of the OAuth client service
     * @return a {@link BMWCarDataException} with the status for a server error, a rate limit or a timeout of the token
     *         endpoint, otherwise an exception that requires a new authorization
     */
    public static BMWCarDataException toException(OAuthException e) {
        String message = Objects.requireNonNullElse(e.getMessage(), "");
        Matcher statusCode = HTTP_STATUS_CODE.matcher(message);
        if (statusCode.find()) {
            int status = Integer.parseInt(statusCode.group(1));
            if (status >= 500 || status == 429 || status == 408) {
                return new BMWCarDataException("Token refresh failed: HTTP " + status, status, e);
            }
        }
        return new BMWCarDataAuthException("Stored authorization not usable: " + message, 0);
    }

    /**
     * @param e communication failure of the OAuth client service
     * @return a {@link BMWCarDataAuthException} if the token endpoint rejected the request with HTTP 401, otherwise a
     *         {@link BMWCarDataException}
     */
    public static BMWCarDataException toException(IOException e) {
        if (HttpResponses.isUnauthorizedResponse(e)) {
            return new BMWCarDataAuthException("Token refresh rejected", 401);
        }
        return new BMWCarDataException("Token refresh failed: " + e.getMessage(), e);
    }
}
