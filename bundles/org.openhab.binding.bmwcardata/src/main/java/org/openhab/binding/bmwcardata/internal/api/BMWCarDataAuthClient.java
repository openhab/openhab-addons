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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.bmwcardata.internal.api.dto.DeviceCodeResponse;
import org.openhab.binding.bmwcardata.internal.api.dto.TokenResponse;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link BMWCarDataAuthClient} implements the OAuth 2.0 device authorization grant (RFC 8628) with PKCE against
 * the BMW customer identity service (GCDM). The resulting tokens are refreshed by openHAB's OAuth client service.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataAuthClient {

    private static final String BASE_URL = "https://customer.bmwgroup.com";
    private static final String DEVICE_CODE_PATH = "/gcdm/oauth/device/code";
    private static final String TOKEN_PATH = "/gcdm/oauth/token";
    /** Token endpoint, also used by openHAB's OAuth client service to refresh the tokens. */
    public static final String TOKEN_URL = BASE_URL + TOKEN_PATH;
    private static final String SCOPE = "authenticate_user openid cardata:api:read cardata:streaming:read";
    private static final String GRANT_TYPE_DEVICE_CODE = "urn:ietf:params:oauth:grant-type:device_code";
    private static final int TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 5;

    // error codes defined by RFC 6749 and RFC 8628
    private static final String ERROR_AUTHORIZATION_PENDING = "authorization_pending";
    private static final String ERROR_SLOW_DOWN = "slow_down";
    private static final String ERROR_EXPIRED_TOKEN = "expired_token";
    private static final String ERROR_ACCESS_DENIED = "access_denied";

    /**
     * A pending device authorization the user has to confirm at {@link #verificationUri}.
     */
    public record DeviceAuthorization(String deviceCode, String userCode, String verificationUri, Instant expiresAt,
            int interval, String codeVerifier) {

        public boolean isExpired(Instant now) {
            return !now.isBefore(expiresAt);
        }

        /**
         * @return the link to the BMW portal with the user code filled in, so the user only has to confirm it
         */
        public String userLink() {
            return verificationUri + (verificationUri.contains("?") ? "&" : "?") + "user_code="
                    + URLEncoder.encode(userCode, StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            // device code and verifier are deliberately omitted
            return "DeviceAuthorization [verificationUri=" + verificationUri + ", expiresAt=" + expiresAt + "]";
        }
    }

    public enum PollStatus {
        /** The user has not yet completed the authorization. */
        PENDING,
        /** The authorization is pending and the polling interval has to be increased. */
        SLOW_DOWN,
        /** The user has completed the authorization. */
        AUTHORIZED,
        /** The device code has expired, a new authorization has to be started. */
        EXPIRED,
        /** The user has denied the authorization. */
        DENIED
    }

    public record PollResult(PollStatus status, @Nullable Tokens tokens) {
    }

    private final HttpClient httpClient;
    private final Gson gson;
    private final String clientId;
    private final String baseUrl;
    private final SecureRandom random = new SecureRandom();

    public BMWCarDataAuthClient(HttpClient httpClient, Gson gson, String clientId) {
        this(httpClient, gson, clientId, BASE_URL);
    }

    BMWCarDataAuthClient(HttpClient httpClient, Gson gson, String clientId, String baseUrl) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.clientId = clientId;
        this.baseUrl = baseUrl;
    }

    /**
     * Starts a new device authorization.
     */
    public DeviceAuthorization requestDeviceCode() throws BMWCarDataException {
        String codeVerifier = createCodeVerifier();
        Fields fields = new Fields();
        fields.put("client_id", clientId);
        fields.put("response_type", "device_code");
        fields.put("scope", SCOPE);
        fields.put("code_challenge", createCodeChallenge(codeVerifier));
        fields.put("code_challenge_method", "S256");

        ContentResponse response = post(baseUrl + DEVICE_CODE_PATH, fields);
        if (response.getStatus() != 200) {
            throw new BMWCarDataException("Device authorization request failed: " + errorOf(response),
                    response.getStatus());
        }
        DeviceCodeResponse dto = parse(response.getContentAsString(), DeviceCodeResponse.class);
        String deviceCode = dto.deviceCode;
        String userCode = dto.userCode;
        String verificationUri = dto.verificationUri;
        if (deviceCode == null || userCode == null || verificationUri == null || dto.expiresIn <= 0) {
            throw new BMWCarDataException("Incomplete device authorization response");
        }
        int interval = dto.interval > 0 ? dto.interval : DEFAULT_POLL_INTERVAL_SECONDS;
        return new DeviceAuthorization(deviceCode, userCode, verificationUri, Instant.now().plusSeconds(dto.expiresIn),
                interval, codeVerifier);
    }

    /**
     * Polls the token endpoint for the result of a pending device authorization.
     */
    public PollResult pollDeviceToken(DeviceAuthorization authorization) throws BMWCarDataException {
        Fields fields = new Fields();
        fields.put("client_id", clientId);
        fields.put("grant_type", GRANT_TYPE_DEVICE_CODE);
        fields.put("device_code", authorization.deviceCode());
        fields.put("code_verifier", authorization.codeVerifier());

        ContentResponse response = post(baseUrl + TOKEN_PATH, fields);
        TokenResponse dto = parseTokenResponse(response);
        if (response.getStatus() == 200) {
            return new PollResult(PollStatus.AUTHORIZED, toTokens(dto));
        }
        String error = dto.error;
        if (error == null) {
            throw new BMWCarDataException("Device token request failed", response.getStatus());
        }
        return switch (error) {
            case ERROR_AUTHORIZATION_PENDING -> new PollResult(PollStatus.PENDING, null);
            case ERROR_SLOW_DOWN -> new PollResult(PollStatus.SLOW_DOWN, null);
            case ERROR_EXPIRED_TOKEN -> new PollResult(PollStatus.EXPIRED, null);
            case ERROR_ACCESS_DENIED -> new PollResult(PollStatus.DENIED, null);
            default -> throw new BMWCarDataException("Device token request failed: " + error, response.getStatus());
        };
    }

    private Tokens toTokens(TokenResponse dto) throws BMWCarDataException {
        String accessToken = dto.accessToken;
        String refreshToken = dto.refreshToken;
        String idToken = dto.idToken;
        String gcid = dto.gcid;
        if (accessToken == null || refreshToken == null || idToken == null || gcid == null || dto.expiresIn <= 0) {
            throw new BMWCarDataException("Incomplete token response");
        }
        return new Tokens(accessToken, refreshToken, idToken, gcid, Instant.now().plusSeconds(dto.expiresIn));
    }

    private TokenResponse parseTokenResponse(ContentResponse response) throws BMWCarDataException {
        String content = response.getContentAsString();
        if (content == null || content.isBlank()) {
            throw new BMWCarDataException("Empty token response", response.getStatus());
        }
        try {
            TokenResponse dto = gson.fromJson(content, TokenResponse.class);
            if (dto == null) {
                throw new BMWCarDataException("Empty token response", response.getStatus());
            }
            return dto;
        } catch (JsonParseException e) {
            throw new BMWCarDataException("Malformed token response", response.getStatus(), e);
        }
    }

    private <T> T parse(String content, Class<T> type) throws BMWCarDataException {
        try {
            @Nullable
            T dto = gson.fromJson(content, type);
            if (dto == null) {
                throw new BMWCarDataException("Empty response");
            }
            return dto;
        } catch (JsonParseException e) {
            throw new BMWCarDataException("Malformed response", e);
        }
    }

    private String errorOf(ContentResponse response) {
        try {
            TokenResponse dto = gson.fromJson(response.getContentAsString(), TokenResponse.class);
            String error = dto != null ? dto.error : null;
            if (error != null) {
                // the description tells which parameter BMW rejected, e.g. an unknown client id
                String description = dto != null ? dto.errorDescription : null;
                return description == null || description.isBlank() ? error : error + " (" + description + ")";
            }
        } catch (JsonParseException e) {
            // fall through to the HTTP status
        }
        return "HTTP " + response.getStatus();
    }

    private ContentResponse post(String url, Fields fields) throws BMWCarDataException {
        try {
            return httpClient.newRequest(url).method(HttpMethod.POST).header(HttpHeader.ACCEPT, "application/json")
                    .content(new FormContentProvider(fields)).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BMWCarDataException("Interrupted", e);
        } catch (TimeoutException | ExecutionException e) {
            throw new BMWCarDataException("Request to BMW authorization service failed: " + e.getMessage(), e);
        }
    }

    private String createCodeVerifier() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String createCodeChallenge(String codeVerifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandatory for every Java platform implementation
            throw new IllegalStateException(e);
        }
    }
}
