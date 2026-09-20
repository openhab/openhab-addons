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
package org.openhab.binding.lghorizon.internal.api;

import static org.openhab.binding.lghorizon.internal.api.LGHorizonApiConstants.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.lghorizon.internal.LGHorizonContentAnonymizer;
import org.openhab.binding.lghorizon.internal.api.dto.AuthResponseDto;
import org.openhab.binding.lghorizon.internal.api.dto.ServiceConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Handles authentication against the LG Horizon "spark" cloud backend and acts as the low-level HTTP client for every
 * other REST call the binding makes.
 * <p>
 * Two auth flows are supported:
 * <ul>
 * <li>Username + password (used by e.g. Ziggo NL)</li>
 * <li>Refresh token (used by e.g. Telenet BE, UPC/Sunrise CH, Virgin Media GB) - the refresh token itself has to be
 * extracted once from the provider's web player (browser dev tools, local storage) since there is no public login flow
 * for it.</li>
 * </ul>
 * The access token obtained here is also used, unmodified, as the MQTT password (see {@link #getMqttToken()}) for the
 * real-time status channel.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public class LGHorizonAuthClient {

    private final Logger logger = LoggerFactory.getLogger(LGHorizonAuthClient.class);

    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final String apiBaseUrl;
    private final String localeCode;
    private final boolean useRefreshTokenFlow;

    private @Nullable String username;
    private @Nullable String password;
    private @Nullable String refreshToken;

    private volatile @Nullable String accessToken;
    private volatile @Nullable String householdId;
    private volatile long refreshTokenExpiryEpochSeconds;
    private volatile @Nullable ServiceConfigDto serviceConfig;

    private @Nullable Consumer<String> refreshTokenListener;

    // Used by console capture, callback for REST calls during capture window
    private volatile @Nullable BiConsumer<String, String> callCaptureListener;

    /**
     * @param apiBaseUrl base URL of the provider's "spark" REST API, e.g.
     *            {@code https://spark-prod-be.gnp.cloud.telenet.tv}
     * @param localeCode two-letter locale used in the service discovery path (e.g. {@code be}, {@code nl}, {@code ch})
     * @param useRefreshTokenFlow whether this provider requires refresh-token auth instead of username/password
     * @param username optional username for username/password auth flow
     * @param password optional password for username/password auth flow
     * @param refreshToken optional refresh token for refresh-token auth flow
     */
    public LGHorizonAuthClient(HttpClient httpClient, String apiBaseUrl, String localeCode, boolean useRefreshTokenFlow,
            @Nullable String username, @Nullable String password, @Nullable String refreshToken) {
        this.httpClient = httpClient;
        this.apiBaseUrl = apiBaseUrl;
        this.localeCode = localeCode;
        this.useRefreshTokenFlow = useRefreshTokenFlow;
        this.username = username;
        this.password = password;
        this.refreshToken = refreshToken;
    }

    /**
     * Registers/unregisters a callback that is invoked for every successful REST call this client makes. This is used
     * by the {@code lghorizon capture} console command, registered at the start of the capture window and unregistered
     * at the end, to log anonymized request/response pairs for debugging.
     *
     * @param listener callback that receives the anonymized request URL and response body, or null to unregister
     */
    public void setCallCaptureListener(@Nullable BiConsumer<String, String> listener) {
        this.callCaptureListener = listener;
    }

    /**
     * Registers a callback that is invoked every time a new refresh token is issued by the backend, so the caller (the
     * bridge handler) can persist it back into the thing configuration.
     *
     * @param listener callback that receives the new refresh token
     */
    public void setRefreshTokenListener(Consumer<String> listener) {
        this.refreshTokenListener = listener;
    }

    /**
     * Get the householdId
     *
     * @return the household id obtained from the backend after a successful login, or null if not logged in yet
     */
    public @Nullable String getHouseholdId() {
        return householdId;
    }

    /**
     * Performs the initial login (or resumes from a stored refresh token) and fetches the service discovery document.
     * Must be called once before any other method.
     *
     * @throws LGHorizonApiException if the login fails or the service discovery document cannot be fetched
     */
    public void initialize() throws LGHorizonApiException {
        fetchAccessToken();
        getServiceConfig();
    }

    private boolean usesRefreshTokenFlow() {
        String refreshToken = this.refreshToken;
        return useRefreshTokenFlow || !(refreshToken == null || refreshToken.isBlank());
    }

    /**
     * Exchanges either username/password or a refresh token for a fresh access token + refresh token pair.
     *
     * @throws LGHorizonApiException if the login fails (invalid credentials, expired refresh token, network error,
     *             etc.)
     */
    public synchronized void fetchAccessToken() throws LGHorizonApiException {
        logger.debug("Fetching LG Horizon access token (locale={})", localeCode);
        JsonObject payload = new JsonObject();
        String path;
        if (!usesRefreshTokenFlow() && accessToken == null) {
            payload.addProperty(USERNAME_FIELD, username);
            payload.addProperty(PASSWORD_FIELD, password);
            path = AUTH_SERVICE_AUTHORIZATION_PATH;
        } else {
            payload.addProperty(REFRESH_TOKEN_FIELD, refreshToken);
            path = AUTH_SERVICE_AUTHORIZATION_REFRESH_PATH;
        }

        Request request = httpClient.newRequest(apiBaseUrl + path).method(HttpMethod.POST)
                .header(CONTENT_TYPE_HEADER, APPLICATION_JSON).header(CHARSET_HEADER, UTF_8)
                .content(new StringContentProvider(APPLICATION_JSON, gson.toJson(payload), StandardCharsets.UTF_8));
        if (!usesRefreshTokenFlow() && accessToken == null) {
            request = request.header(X_DEVICE_CODE_HEADER, X_DEVICE_CODE_WEB);
        }

        ContentResponse response = send(request);
        AuthResponseDto auth = parse(response, AuthResponseDto.class);

        if (response.getStatus() >= HttpStatus.MULTIPLE_CHOICES_300 || auth.accessToken == null) {
            AuthResponseDto.ErrorDto error = auth.error;
            if (error != null && error.statusCode == CREDENTIALS_ERROR) {
                throw new LGHorizonApiException("Invalid credentials", true);
            } else if (error != null && error.statusCode == TOKEN_ERROR) {
                throw new LGHorizonApiException("Invalid or expired refresh token", true);
            } else if (error != null) {
                throw new LGHorizonApiException("LG Horizon auth error: " + error.message, true);
            }
            throw new LGHorizonApiException(
                    "Unknown LG Horizon authentication error (HTTP " + response.getStatus() + ")", true);
        }

        this.householdId = auth.householdId;
        this.accessToken = auth.accessToken;
        String newRefreshToken = auth.refreshToken;
        if (newRefreshToken != null) {
            this.refreshToken = newRefreshToken;
            Consumer<String> listener = refreshTokenListener;
            if (listener != null) {
                listener.accept(newRefreshToken);
            }
        }
        this.refreshTokenExpiryEpochSeconds = auth.refreshTokenExpiry;
        logger.debug("LG Horizon access token acquired; refresh token expires {}",
                Instant.ofEpochSecond(refreshTokenExpiryEpochSeconds));
    }

    private boolean isTokenExpiring() {
        String token = accessToken;
        if (token == null || refreshTokenExpiryEpochSeconds == 0) {
            return true;
        }
        return Instant.now().getEpochSecond() >= (refreshTokenExpiryEpochSeconds - TOKEN_REFRESH_MARGIN_SECONDS);
    }

    private void ensureFreshToken() throws LGHorizonApiException {
        if (isTokenExpiring()) {
            fetchAccessToken();
        }
    }

    /**
     * Performs an authenticated GET request against one of the "spark" REST services and parses the response as the
     * given DTO type.
     *
     * @param serviceBaseUrl the base URL of the provider's "spark" REST API
     * @param path the path to GET
     * @return the parsed response as an instance of the given type
     * @throws LGHorizonApiException if the request fails or the response cannot be parsed as the given type
     */
    public <T> T get(String serviceBaseUrl, String path, Class<T> type) throws LGHorizonApiException {
        return parse(getRaw(serviceBaseUrl, path), type);
    }

    private ContentResponse getRaw(String serviceBaseUrl, String path) throws LGHorizonApiException {
        ensureFreshToken();
        String url = serviceBaseUrl + path;
        String anonymizedUrl = String.valueOf(LGHorizonContentAnonymizer.anonymizeTopic(url));
        ContentResponse response = send(
                httpClient.newRequest(url).method(HttpMethod.GET).header(AUTHORIZATION_HEADER, BEARER + accessToken));

        if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
            logger.debug("Got HTTP 401 from {}, refreshing token and retrying once", anonymizedUrl);
            fetchAccessToken();
            response = send(httpClient.newRequest(url).method(HttpMethod.GET).header(AUTHORIZATION_HEADER,
                    BEARER + accessToken));
        }
        if (response.getStatus() >= HttpStatus.MULTIPLE_CHOICES_300) {
            throw new LGHorizonApiException(
                    "Unable to call " + anonymizedUrl + ", HTTP status " + response.getStatus());
        }
        String anonymizedBody = LGHorizonContentAnonymizer.anonymizeMessage(response.getContentAsString());
        logger.trace("Raw response from {}: {}", anonymizedUrl, anonymizedBody);
        BiConsumer<String, String> capture = callCaptureListener;
        if (capture != null) {
            capture.accept(anonymizedUrl, String.valueOf(anonymizedBody));
        }
        return response;
    }

    /**
     * Fetches a JSON response from the given service and path and parses it as a {@link JsonObject}.
     *
     * @param serviceBaseUrl the base URL of the provider's "spark" REST API
     * @param path the path to GET
     * @return the parsed JSON response as a {@link JsonObject}
     * @throws LGHorizonApiException if the request fails or the response is not valid JSON or not a JSON object
     */
    public JsonObject getAsJsonObject(String serviceBaseUrl, String path) throws LGHorizonApiException {
        return getAsJsonElement(serviceBaseUrl, path).getAsJsonObject();
    }

    /**
     * Like {@link #getAsJsonObject(String, String)}, but for responses that may be a top-level JSON array
     * (e.g. the channel list) as well as an object - used by the {@code lghorizon fingerprint} console
     * command, which wants the raw, untyped response regardless of shape.
     *
     * @param serviceBaseUrl the base URL of the provider's "spark" REST API
     * @param path the path to GET
     * @return the raw JSON response as a {@link JsonElement}, which may be an object or an array
     * @throws LGHorizonApiException if the request fails or the response is not valid JSON
     */
    public JsonElement getAsJsonElement(String serviceBaseUrl, String path) throws LGHorizonApiException {
        ContentResponse response = getRaw(serviceBaseUrl, path);
        return JsonParser.parseString(response.getContentAsString());
    }

    private ContentResponse send(Request request) throws LGHorizonApiException {
        try {
            return request.timeout(REQUEST_TIMEOUT, TimeUnit.SECONDS).send();
        } catch (TimeoutException | ExecutionException e) {
            throw new LGHorizonApiException(
                    "Unable to call " + LGHorizonContentAnonymizer.anonymizeTopic(request.getURI().toString()), e);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LGHorizonApiException("Interrupted while calling "
                    + LGHorizonContentAnonymizer.anonymizeTopic(request.getURI().toString()), ie);
        }
    }

    private <T> T parse(ContentResponse response, Class<T> type) throws LGHorizonApiException {
        try {
            @Nullable
            T result = gson.fromJson(response.getContentAsString(), type);
            if (result == null) {
                throw new LGHorizonApiException("Empty response body from "
                        + LGHorizonContentAnonymizer.anonymizeTopic(response.getRequest().getURI().toString()));
            }
            return result;
        } catch (JsonSyntaxException e) {
            throw new LGHorizonApiException("Unable to parse response from "
                    + LGHorizonContentAnonymizer.anonymizeTopic(response.getRequest().getURI().toString()), e);
        }
    }

    /**
     * Fetches and caches the service discovery document ({@code /config-service/conf/web/backoffice.json}) that maps
     * logical service names (personalizationService, linearService, mqttBroker, ...) to their concrete URLs for this
     * provider.
     *
     * @return the service discovery document as a {@link ServiceConfigDto}
     * @throws LGHorizonApiException if the request fails or the response is not valid JSON
     */
    public ServiceConfigDto getServiceConfig() throws LGHorizonApiException {
        ServiceConfigDto cached = serviceConfig;
        if (cached != null) {
            return cached;
        }
        JsonObject raw = getServiceConfigAsJsonElement().getAsJsonObject();
        Map<String, JsonObject> services = new HashMap<>();
        for (String key : raw.keySet()) {
            if (raw.get(key).isJsonObject()) {
                services.put(key, raw.get(key).getAsJsonObject());
            }
        }
        ServiceConfigDto config = new ServiceConfigDto(services);
        this.serviceConfig = config;
        return config;
    }

    /**
     * Raw (untyped) JSON of the service discovery document, for the {@code lghorizon fingerprint} console
     * command, returns the unparsed document.
     *
     * @param serviceBaseUrl the base URL of the provider's "spark" REST API
     * @retrun the service discovery document as a {@link JsonElement}
     * @throws LGHorizonApiException if the request fails or the response is not valid JSON
     */
    public JsonElement getServiceConfigAsJsonElement() throws LGHorizonApiException {
        return getAsJsonElement(apiBaseUrl, "/" + localeCode + EN_CONFIG_SERVICE_CONF_WEB_BACKOFFICE_JSON);
    }

    /**
     * Fetches a short-lived token used as the MQTT password (username is the household id).
     *
     * @return the MQTT token as a string
     * @throws LGHorizonApiException if the request fails or the response does not contain a token
     */
    public String getMqttToken() throws LGHorizonApiException {
        String url = getServiceConfig().getServiceUrl(AUTHORIZATION_SERVICE_URL_FIELD);
        JsonObject result = getAsJsonObject(url, V1_MQTT_TOKEN_PATH);
        if (!result.has(TOKEN_FIELD)) {
            throw new LGHorizonApiException("MQTT token response did not contain a '" + TOKEN_FIELD + "' field");
        }
        return result.get(TOKEN_FIELD).getAsString();
    }
}
