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
package org.openhab.binding.millheat.internal.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.millheat.internal.MillheatCommunicationException;
import org.openhab.binding.millheat.internal.dto.DeviceDTO;
import org.openhab.binding.millheat.internal.dto.DeviceSettingsPatchRequest;
import org.openhab.binding.millheat.internal.dto.HousesResponse;
import org.openhab.binding.millheat.internal.dto.IndependentDevicesResponse;
import org.openhab.binding.millheat.internal.dto.RoomDevicesDTO;
import org.openhab.binding.millheat.internal.dto.RoomInfoDTO;
import org.openhab.binding.millheat.internal.dto.RoomTemperatureRequest;
import org.openhab.binding.millheat.internal.dto.SignInRequest;
import org.openhab.binding.millheat.internal.dto.SignInResponse;
import org.openhab.binding.millheat.internal.dto.VacationModeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Talks to the Mill cloud service at {@code api.millnorwaycloud.com}. Named for the cloud to
 * distinguish it from the separate local-network API that generation 3 devices expose directly.
 * <p>
 * Authentication is a bespoke JSON sign-in rather than OAuth2, so openHAB's
 * {@code OAuthClientService} does not apply. The access token lives for ten minutes and is
 * refreshed shortly before it expires; the refresh token is held only in memory, since the account
 * password is already in the thing configuration and a fresh sign-in after a restart costs a single
 * request.
 * <p>
 * The account is limited to 2500 requests per hour, after which the service answers 429.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
@NonNullByDefault
public class MillheatCloudApiClient {

    /** Overridable so tests can point the client at a local stub server. */
    public static String endpoint = "https://api.millnorwaycloud.com";

    private static final long REQUEST_TIMEOUT_SECONDS = 30;
    /** Renew the access token this long before it actually expires. */
    private static final long TOKEN_RENEWAL_MARGIN_SECONDS = 60;
    /** Assumed lifetime when the token carries no readable expiry. */
    private static final long DEFAULT_TOKEN_LIFETIME_SECONDS = 600;

    private final Logger logger = LoggerFactory.getLogger(MillheatCloudApiClient.class);
    private final HttpClient httpClient;
    private final Gson gson;
    private final RequestLogger requestLogger;

    private @Nullable String accessToken;
    private @Nullable String refreshToken;
    private Instant accessTokenExpiry = Instant.MIN;
    private Instant rateLimitedUntil = Instant.MIN;

    public MillheatCloudApiClient(final HttpClient httpClient, final Gson gson, final RequestLogger requestLogger) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.requestLogger = requestLogger;
    }

    /**
     * Exchanges username and password for an access and refresh token. Any previously held tokens
     * are discarded.
     */
    public void signIn(final String username, final String password) throws MillheatCommunicationException {
        accessToken = null;
        refreshToken = null;
        accessTokenExpiry = Instant.MIN;

        final SignInResponse response = send(HttpMethod.POST, "/customer/auth/sign-in",
                new SignInRequest(username, password), false, SignInResponse.class);
        if (response == null) {
            throw new MillheatCommunicationException("Sign-in returned an empty response");
        }
        storeTokens(response);
    }

    /**
     * Renews the access token using the refresh token. Unlike every other authenticated call, this
     * endpoint expects the refresh token, not the access token, in the Authorization header.
     */
    public void refreshAccessToken() throws MillheatCommunicationException {
        final String token = refreshToken;
        if (token == null) {
            throw new MillheatCommunicationException("Cannot refresh without a refresh token");
        }
        final Request request = newRequest(HttpMethod.POST, "/customer/auth/refresh").header("Authorization",
                "Bearer " + token);
        final SignInResponse response = execute(request, SignInResponse.class);
        if (response == null) {
            throw new MillheatCommunicationException("Token refresh returned an empty response");
        }
        storeTokens(response);
    }

    /** True once a sign-in has produced tokens. */
    public boolean isAuthenticated() {
        return accessToken != null;
    }

    /** Discards all tokens, forcing a fresh sign-in on the next request. */
    public void clearTokens() {
        accessToken = null;
        refreshToken = null;
        accessTokenExpiry = Instant.MIN;
    }

    /**
     * The instant before which no further request should be attempted because the service reported
     * that the account's hourly request budget is exhausted.
     */
    public Instant rateLimitedUntil() {
        return rateLimitedUntil;
    }

    public HousesResponse getHouses() throws MillheatCommunicationException {
        final HousesResponse response = get("/houses", HousesResponse.class);
        return response == null ? new HousesResponse(List.of(), List.of()) : response;
    }

    /**
     * Returns every device in the house, grouped by room, with settings and telemetry embedded.
     * This is the binding's main poll: one request covers all device state in a house.
     */
    public List<RoomDevicesDTO> getHouseDevices(final String houseId) throws MillheatCommunicationException {
        final List<RoomDevicesDTO> rooms = send(HttpMethod.GET, "/houses/" + encode(houseId) + "/devices", null, true,
                new TypeToken<List<RoomDevicesDTO>>() {
                }.getType());
        return rooms == null ? List.of() : rooms;
    }

    /** Returns the devices in the house that belong to no room. */
    public List<DeviceDTO> getIndependentDevices(final String houseId) throws MillheatCommunicationException {
        final IndependentDevicesResponse response = get("/houses/" + encode(houseId) + "/devices/independent",
                IndependentDevicesResponse.class);
        if (response == null) {
            return List.of();
        }
        final List<DeviceDTO> items = response.items();
        return items == null ? List.of() : items;
    }

    /** Returns a room's setpoints, active mode and aggregate measurements. */
    public @Nullable RoomInfoDTO getRoomInfo(final String roomId) throws MillheatCommunicationException {
        return get("/rooms/" + encode(roomId) + "/devices", RoomInfoDTO.class);
    }

    /** Applies settings to a device by replacing the desired half of its shadow. */
    public void patchDeviceSettings(final String deviceId, final DeviceSettingsPatchRequest request)
            throws MillheatCommunicationException {
        send(HttpMethod.PATCH, "/devices/" + encode(deviceId) + "/settings", request, true, Void.class);
    }

    /** Changes one or more of a room's three program setpoints. */
    public void setRoomTemperatures(final String roomId, final RoomTemperatureRequest request)
            throws MillheatCommunicationException {
        send(HttpMethod.POST, "/rooms/" + encode(roomId) + "/temperature", request, true, Void.class);
    }

    /** Enables or updates vacation mode for a house. */
    public void setVacationMode(final String houseId, final VacationModeRequest request, final boolean alreadyActive)
            throws MillheatCommunicationException {
        send(alreadyActive ? HttpMethod.PATCH : HttpMethod.POST, "/houses/" + encode(houseId) + "/mode/vacation",
                request, true, Void.class);
    }

    /** Disables vacation mode for a house. */
    public void clearVacationMode(final String houseId) throws MillheatCommunicationException {
        send(HttpMethod.DELETE, "/houses/" + encode(houseId) + "/mode/vacation", null, true, Void.class);
    }

    private <T> @Nullable T get(final String path, final Class<T> responseType) throws MillheatCommunicationException {
        return send(HttpMethod.GET, path, null, true, responseType);
    }

    private <T> @Nullable T send(final HttpMethod method, final String path, final @Nullable Object body,
            final boolean authenticated, final java.lang.reflect.Type responseType)
            throws MillheatCommunicationException {
        if (authenticated) {
            ensureValidAccessToken();
        }
        try {
            return execute(buildRequest(method, path, body, authenticated), responseType);
        } catch (final MillheatCommunicationException e) {
            if (!authenticated || !e.isUnauthorized()) {
                throw e;
            }
            // The token was rejected earlier than its expiry claimed. Renew once and retry.
            logger.debug("Access token rejected, refreshing and retrying {} {}", method, path);
            refreshAccessToken();
            return execute(buildRequest(method, path, body, true), responseType);
        }
    }

    private Request buildRequest(final HttpMethod method, final String path, final @Nullable Object body,
            final boolean authenticated) {
        final Request request = newRequest(method, path);
        if (authenticated) {
            request.header("Authorization", "Bearer " + accessToken);
        }
        if (body != null) {
            request.content(new StringContentProvider(gson.toJson(body), StandardCharsets.UTF_8), "application/json");
        }
        return request;
    }

    private Request newRequest(final HttpMethod method, final String path) {
        final Request request = httpClient.newRequest(endpoint + path).method(method)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        requestLogger.listenTo(request);
        return request;
    }

    private <T> @Nullable T execute(final Request request, final java.lang.reflect.Type responseType)
            throws MillheatCommunicationException {
        final ContentResponse response;
        try {
            response = request.send();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MillheatCommunicationException("Interrupted while calling the Mill cloud API", e);
        } catch (final ExecutionException e) {
            // Jetty's authentication handling rejects a 401 that carries no WWW-Authenticate
            // header as a protocol violation, and Mill sends exactly that. Recover the status so
            // the caller can refresh the token instead of seeing an opaque transport failure.
            if (e.getCause() instanceof HttpResponseException httpResponseException) {
                final int failedStatus = httpResponseException.getResponse().getStatus();
                throw new MillheatCommunicationException(failedStatus, "Mill cloud API responded with " + failedStatus);
            }
            throw new MillheatCommunicationException("Error calling the Mill cloud API: " + e.getMessage(), e);
        } catch (final TimeoutException e) {
            throw new MillheatCommunicationException("Error calling the Mill cloud API: " + e.getMessage(), e);
        }

        final int status = response.getStatus();
        final String payload = response.getContentAsString();
        if (status == 429) {
            // No Retry-After is sent, so back off for the remainder of the current budget window.
            rateLimitedUntil = Instant.now().plusSeconds(300);
            throw new MillheatCommunicationException(status,
                    "Mill cloud API request budget exhausted, backing off until " + rateLimitedUntil);
        }
        if (status < 200 || status >= 300) {
            throw new MillheatCommunicationException(status,
                    "Mill cloud API responded with " + status + ": " + payload);
        }
        rateLimitedUntil = Instant.MIN;
        if (responseType == Void.class || payload.isBlank()) {
            return null;
        }
        try {
            return gson.fromJson(payload, responseType);
        } catch (final JsonParseException e) {
            throw new MillheatCommunicationException("Unparseable response from the Mill cloud API: " + e.getMessage(),
                    e);
        }
    }

    private void ensureValidAccessToken() throws MillheatCommunicationException {
        if (accessToken == null) {
            throw new MillheatCommunicationException("Not signed in to the Mill cloud API");
        }
        if (Instant.now().isAfter(accessTokenExpiry.minusSeconds(TOKEN_RENEWAL_MARGIN_SECONDS))) {
            refreshAccessToken();
        }
    }

    private void storeTokens(final SignInResponse response) throws MillheatCommunicationException {
        final String newAccessToken = response.idToken();
        if (newAccessToken == null || newAccessToken.isBlank()) {
            throw new MillheatCommunicationException("Mill cloud API returned no access token");
        }
        accessToken = newAccessToken;
        final String newRefreshToken = response.refreshToken();
        if (newRefreshToken != null && !newRefreshToken.isBlank()) {
            refreshToken = newRefreshToken;
        }
        accessTokenExpiry = expiryOf(newAccessToken);
    }

    /**
     * Reads the {@code exp} claim from a JWT without verifying its signature, which the client has
     * no key for and does not need: the server is the authority on validity, and a wrong guess here
     * only costs one extra refresh.
     */
    private Instant expiryOf(final String token) {
        final Instant fallback = Instant.now().plusSeconds(DEFAULT_TOKEN_LIFETIME_SECONDS);
        final String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return fallback;
        }
        try {
            final String claims = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            final JsonObject json = JsonParser.parseString(claims).getAsJsonObject();
            if (!json.has("exp")) {
                return fallback;
            }
            return Instant.ofEpochSecond(json.get("exp").getAsLong());
        } catch (final RuntimeException e) {
            logger.debug("Could not read expiry from access token, assuming {} seconds",
                    DEFAULT_TOKEN_LIFETIME_SECONDS);
            return fallback;
        }
    }

    private static String encode(final String pathSegment) {
        try {
            return URLEncoder.encode(pathSegment, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            return pathSegment;
        }
    }
}
