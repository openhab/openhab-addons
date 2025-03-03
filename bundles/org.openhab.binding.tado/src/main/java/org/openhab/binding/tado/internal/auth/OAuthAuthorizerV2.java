/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tado.internal.auth;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.tado.internal.handler.TadoHomeHandler;
import org.openhab.binding.tado.swagger.codegen.api.ApiException;
import org.openhab.binding.tado.swagger.codegen.api.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This is an implementation of the new OAuth2 workflow required by Tado after March 15 2025.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class OAuthAuthorizerV2 implements Authorizer, Closeable {

    private static final String DEVICE_URL = "https://login.tado.com/oauth2/device_authorize";
    private static final String TOKEN_URL = "https://login.tado.com/oauth2/token";

    private static final String DEVICE_GRANT = "urn:ietf:params:oauth:grant-type:device_code";
    private static final String TOKEN_GRANT = "refresh_token";

    private static final String CLIENT_ID = "1bb50063-6b0c-4d11-bd99-387f4a91cc46";

    private static final String SCOPE = "offline_access";

    private final Logger logger = LoggerFactory.getLogger(OAuthAuthorizerV2.class);
    private final Gson gson = new GsonBuilder().create();

    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;

    private @Nullable String deviceCode;
    private @Nullable String authenticationUri;
    private @Nullable String accessToken;
    private @Nullable String accessTokenType;
    private @Nullable String refreshToken;

    private Duration fetchTokensInterval = Duration.ofSeconds(5);
    private Instant authenticationExpireTime = Instant.EPOCH;
    private Instant accessTokenExpireTime = Instant.EPOCH;

    private @Nullable ScheduledFuture<?> fetchTokensTask;
    private @Nullable TadoHomeHandler handler;

    public OAuthAuthorizerV2(ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.scheduler = scheduler;
        this.httpClient = httpClient;
    }

    @Override
    public void addAuthorization(@Nullable Request request) throws ApiException, IOException {
        if (request == null) {
            throw new ApiException("Null 'request'");
        }
        if (refreshToken == null) {
            throw new ApiException("Null 'refreshToken'");
        }
        if (accessTokenType == null) {
            throw new ApiException("Null 'tokenType'");
        }
        if (Instant.now().isAfter(accessTokenExpireTime)) {
            refreshTokens();
        }
        if (accessToken == null) {
            throw new ApiException("Null 'accessToken'");
        }
        request.header(HttpHeader.AUTHORIZATION, accessTokenType + " " + accessToken);
    }

    private long asLong(@Nullable Object object) {
        return object instanceof Integer i ? i : 0;
    }

    private @Nullable String asString(@Nullable Object object) {
        return object instanceof String string ? string : null;
    }

    private synchronized void cancelFetchTokensTask() {
        ScheduledFuture<?> task = fetchTokensTask;
        if (task != null) {
            task.cancel(false);
        }
        fetchTokensTask = null;
    }

    @Override
    public void close() {
        cancelFetchTokensTask();
    }

    /**
     * Start the authentication process by fetching the target uri and device code
     */
    private synchronized void fetchAuthenticationParams() {
        List<String> queryParams = new ArrayList<>();
        queryParams.add(queryParam("client_id", CLIENT_ID));
        queryParams.add(queryParam("scope", SCOPE));

        Request request = httpClient.newRequest(DEVICE_URL + "?" + String.join("&", queryParams))
                .method(HttpMethod.POST).timeout(5, TimeUnit.SECONDS);
        logger.trace("fetchAuthenticationParams() => {}", request);

        try {
            ContentResponse response = request.send();
            String content = response.getContentAsString();
            logger.trace("fetchAuthenticationParams() <= {}", content);
            if (response.getStatus() == HttpStatus.OK_200) {
                Map<?, ?> tokenValues = gson.fromJson(content, Map.class);
                if (tokenValues != null) {
                    String deviceCode = asString(tokenValues.get("device_code"));
                    String verificationUriComplete = asString(tokenValues.get("verification_uri_complete"));
                    long expiresIn = asLong(tokenValues.get("expires_in"));
                    long interval = asLong(tokenValues.get("interval"));

                    if (deviceCode != null && verificationUriComplete != null && expiresIn > 0 && interval > 0) {
                        this.deviceCode = deviceCode;
                        this.authenticationUri = verificationUriComplete;
                        this.authenticationExpireTime = Instant.now().plusSeconds(expiresIn);
                        this.fetchTokensInterval = Duration.ofSeconds(interval);
                        return;
                    }
                }
            }
            logger.debug("fetchAuthenticationParams() error <= {}", response);
        } catch (Exception e) {
            logger.debug("fetchAuthenticationParams() error calling {}", DEVICE_URL, e);
        }

        this.deviceCode = null;
        this.authenticationUri = null;
        this.authenticationExpireTime = Instant.EPOCH;
        this.fetchTokensInterval = Duration.ofSeconds(5);
        sendDeviceCodeToHandler();
    }

    /**
     * Check for completion of the authentication process and fetch the access and refresh tokens
     */
    private synchronized void fetchTokensForDevice() {
        List<String> queryParams = new ArrayList<>();
        queryParams.add(queryParam("client_id", CLIENT_ID));
        queryParams.add(queryParam("grant_type", DEVICE_GRANT));
        queryParams.add(queryParam("device_code", Objects.requireNonNull(deviceCode)));

        Request request = httpClient.newRequest(TOKEN_URL + "?" + String.join("&", queryParams)).method(HttpMethod.POST)
                .timeout(5, TimeUnit.SECONDS);
        logger.trace("fetchTokensForDevice() => {}", request);

        try {
            ContentResponse response = request.send();
            String content = response.getContentAsString();
            logger.trace("fetchTokensForDevice() <= {}", content);
            if (response.getStatus() == HttpStatus.OK_200) {
                Map<?, ?> tokenValues = gson.fromJson(content, Map.class);
                if (tokenValues != null) {
                    String accessToken = asString(tokenValues.get("access_token"));
                    String accessTokenType = asString(tokenValues.get("token_type"));
                    String refreshToken = asString(tokenValues.get("refresh_token"));
                    long expiresIn = asLong(tokenValues.get("expires_in"));

                    if (accessToken != null && refreshToken != null && accessTokenType != null && expiresIn > 0) {
                        this.accessToken = accessToken;
                        this.accessTokenType = accessTokenType;
                        this.accessTokenExpireTime = Instant.now().plusSeconds(expiresIn);
                        this.refreshToken = refreshToken;
                        return;
                    }
                }
                logger.debug("fetchTokensForDevice() error <= {}", response);
            }
            // other HttpStatus codes (i.e. not ready) are to be expected
        } catch (Exception e) {
            logger.debug("fetchTokensForDevice() error calling {}", TOKEN_URL, e);
        }
    }

    /**
     * @return true if we have both deviceCode and refreshToken
     */
    public boolean isAuthenticated() {
        return deviceCode != null && refreshToken != null;
    }

    private String queryParam(String key, String value) {
        try {
            return key + "=" + URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return key + "=" + value;
        }
    }

    /**
     * Refresh the access and refresh tokens
     */
    private synchronized void refreshTokens() {
        List<String> queryParams = new ArrayList<>();
        queryParams.add(queryParam("client_id", CLIENT_ID));
        queryParams.add(queryParam("grant_type", TOKEN_GRANT));
        queryParams.add(queryParam("refresh_token", Objects.requireNonNull(refreshToken)));

        Request request = httpClient.newRequest(TOKEN_URL + "?" + String.join("&", queryParams)).method(HttpMethod.POST)
                .timeout(5, TimeUnit.SECONDS);
        logger.trace("refreshTokens() => {}", request);

        try {
            ContentResponse response = request.send();
            String content = response.getContentAsString();
            logger.trace("refreshTokens() <= {}", content);
            if (response.getStatus() == HttpStatus.OK_200) {
                Map<?, ?> tokenValues = gson.fromJson(content, Map.class);
                if (tokenValues != null) {
                    String accessToken = asString(tokenValues.get("access_token"));
                    String refreshToken = asString(tokenValues.get("refresh_token"));
                    long expiresIn = asLong(tokenValues.get("expires_in"));

                    if (accessToken != null && refreshToken != null && expiresIn > 0) {
                        this.accessToken = accessToken;
                        this.accessTokenExpireTime = Instant.now().plusSeconds(expiresIn);
                        this.refreshToken = refreshToken;
                        return;
                    }
                }
            }
            logger.debug("refreshTokens() error <= {}", response);
        } catch (Exception e) {
            logger.debug("refreshTokens() error calling {}", TOKEN_URL, e);
        }

        this.accessToken = null;
        this.accessTokenExpireTime = Instant.EPOCH;
        this.refreshToken = null;
    }

    private void sendDeviceCodeToHandler() {
        TadoHomeHandler handler = this.handler;
        if (handler != null) {
            handler.setDeviceCode(deviceCode);
        }
    }

    public OAuthAuthorizerV2 setDeviceCode(@Nullable String deviceCode) {
        this.deviceCode = deviceCode;
        return this;
    }

    public OAuthAuthorizerV2 setHandler(@Nullable TadoHomeHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Starts the authentication process. Specifically it executes the following steps as described in the article in
     * the link below:
     * <ul>
     * <li>Starts the process (step 1.) with Http POST call to the device authentication url</li>
     * <li>Processes the response (step 2.) to get 'deviceCode' and 'authenticationUri'</li>
     * <li>Notifies the handler with the 'deviceCode' (so it can be persisted)</li>
     * <li>In the meantime the user is expected to visit the 'authenticationUri' (step 3.) to do the authentication</li>
     * <li>Starts the polling process (step 4.) with repeated Http POST calls during the authentication time window</li>
     * <li>Processes the responses (step 5.) until one returns an 'accessToken' and 'refreshToken'</li>
     * <li>And finally stops the polling (step 5.) when done</li>
     * </ul>
     * Notes:
     * <ol>
     * <li>If the authentication is already completed then skip the whole process</li>
     * <li>If there is already a 'deviceCode' it skips over step 1.</li>
     * <li>If the authentication time window is still open, the subsequent calls skip over step 1.</li>
     * </ol>
     *
     * @see <a href=
     *      "https://support.tado.com/en/articles/8565472-how-do-i-authenticate-to-access-the-rest-api">Article</a>
     *
     * @return the authentication uri that the user shall visit to authenticate (the servlet wraps it).
     * @throws ApiException
     */
    public synchronized @Nullable String startAuthentication() throws ApiException {
        if (isAuthenticated()) {
            return null;
        }

        if ((deviceCode == null) || Instant.now().isAfter(authenticationExpireTime)) {
            fetchAuthenticationParams();
        }

        if (deviceCode == null || authenticationUri == null) {
            throw new ApiException("Missing 'deviceCode' or 'authenticationUri'");
        }

        cancelFetchTokensTask();

        accessTokenExpireTime = Instant.EPOCH; // force an update
        long fetchTokensDelay = fetchTokensInterval.toSeconds();

        fetchTokensTask = scheduler.scheduleWithFixedDelay(() -> {
            if (accessTokenExpireTime.equals(Instant.EPOCH) && Instant.now().isBefore(authenticationExpireTime)) {
                fetchTokensForDevice();
            }
            if (accessTokenExpireTime.isAfter(Instant.EPOCH) || Instant.now().isAfter(authenticationExpireTime)) {
                cancelFetchTokensTask();
            }
        }, fetchTokensDelay, fetchTokensDelay, TimeUnit.SECONDS);

        return authenticationUri;
    }
}
