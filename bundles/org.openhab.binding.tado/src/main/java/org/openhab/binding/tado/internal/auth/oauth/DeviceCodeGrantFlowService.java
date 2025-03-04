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
package org.openhab.binding.tado.internal.auth.oauth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.tado.internal.handler.TadoHomeHandler;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link DeviceCodeGrantFlowService} implements oAuth "Device Code Grant Flow" RFC-8628
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
public class DeviceCodeGrantFlowService implements AutoCloseable {

    private static final String DEVICE_GRANT = "urn:ietf:params:oauth:grant-type:device_code";

    private final Logger logger = LoggerFactory.getLogger(DeviceCodeGrantFlowService.class);
    private final Gson gson = new GsonBuilder().create();

    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;
    private final TadoHomeHandler handler;
    private final OAuthClientService inner;

    private final String tokenUrl;
    private final String deviceUrl;
    private final String clientId;
    private final String scope;

    private @Nullable ScheduledFuture<?> step4and5Task;

    public DeviceCodeGrantFlowService(ScheduledExecutorService scheduler, HttpClient httpClient,
            OAuthFactory oAuthFactory, TadoHomeHandler handler, String handle, String tokenUrl, String deviceUrl,
            String clientId, String scope) {

        this.scheduler = scheduler;
        this.httpClient = httpClient;
        this.handler = handler;

        this.tokenUrl = tokenUrl;
        this.deviceUrl = deviceUrl;
        this.clientId = clientId;
        this.scope = scope;

        this.inner = oAuthFactory.createOAuthClientService(handle, tokenUrl, deviceUrl, clientId, null, scope, false);
    }

    /**
     * Begins the Device Code Grant Flow authentication process. Specifically it executes the
     * following steps as described in the article in the link below:
     * <ul>
     * <li>Step 1. create a request and POST it to the 'device authorize url'</li>
     * <li>Step 2. process the response and create a {@link DeviceCodeResponse}</li>
     * <li>Step 3. the user goes off to authenticate themselves at the 'user authentication url</li>
     * <li>Step 4. repeatedly create a request and POST it to the 'token url'</li>
     * <li>Step 5. repeatedly read the response and eventually create a {@link AccessTokenResponse}</li>
     * </ul>
     *
     * @see <a href=
     *      "https://support.tado.com/en/articles/8565472-how-do-i-authenticate-to-access-the-rest-api">Article</a>
     *
     * @return the uri that the user shall visit to authenticate, or null if no visit is required.
     */
    public synchronized @Nullable String beginAuthenticationAndGetUserUri() {
        try {
            if (inner.getAccessTokenResponse() != null) {
                return null;
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.warn("Unxexpected error {}", e.getMessage(), e);
            return null;
        }

        DeviceCodeResponse deviceCode = deviceCodeResponseRestore();
        deviceCode = step1and2Execute(deviceCodeResponseRestore());
        deviceCodePersist(deviceCode);

        step4and5Cancel();

        if (deviceCode == null) {
            logger.warn("Unxexpected null 'deviceCode'");
            return null;
        }

        long seconds = deviceCode.getInterval().getSeconds();

        step4and5Task = scheduler.scheduleWithFixedDelay(() -> {
            DeviceCodeResponse localDeviceCode = deviceCodeResponseRestore();
            AccessTokenResponse localAccessToken = null;
            Instant now = Instant.now();
            if ((localDeviceCode != null) && !localDeviceCode.isExpired(now)) {
                localAccessToken = step4and5Execute(localDeviceCode);
            }
            if (localAccessToken != null) {
                try {
                    inner.importAccessTokenResponse(localAccessToken);
                } catch (OAuthException e) {
                    logger.warn("Unxexpected error {}", e.getMessage(), e);
                }
            }
            if (localAccessToken != null || localDeviceCode == null || localDeviceCode.isExpired(now)) {
                step4and5Cancel();
            }
        }, seconds, seconds, TimeUnit.SECONDS);

        return deviceCode.getUserUri();
    }

    @Override
    public void close() {
        step4and5Cancel();
    }

    /**
     * Persist the given oAuth {@link DeviceCodeResponse}
     * <p>
     * This is not implemented in OH core, so we don't have access to the oAuth persistence
     * store. So we mis-use the thing configuration persistence for this purpose.
     */
    private synchronized void deviceCodePersist(@Nullable DeviceCodeResponse response) {
        handler.deviceCodeResponsePersist(response);
    }

    /**
     * Restore the oAuth {@link DeviceCodeResponse} from persistence.
     * <p>
     * This is not implemented in OH core, so we don't have access to the oAuth persistence
     * store. So we mis-use the thing configuration persistence for this purpose.
     */
    private synchronized @Nullable DeviceCodeResponse deviceCodeResponseRestore() {
        return handler.deviceCodeResponseRestore();
    }

    public @Nullable AccessTokenResponse getAccessTokenResponse()
            throws OAuthException, IOException, OAuthResponseException {
        return inner.getAccessTokenResponse();
    }

    /**
     * Start the first steps of the Device Code Grant Flow authentication process as follows:
     * <ul>
     * <li>Step 1. create a request and POST it to the 'device authorize url'</li>
     * <li>Step 2. process the response and create a {@link DeviceCodeResponse}</li>
     * </ul>
     *
     * @param priorDeviceCode a {@link DeviceCodeResponse} object or null
     * @return a {@link DeviceCodeResponse} object or null
     */
    private synchronized DeviceCodeResponse step1and2Execute(DeviceCodeResponse priorDeviceCode) {
        if (!priorDeviceCode.isExpired(Instant.now())) {
            return priorDeviceCode;
        }

        List<String> queryParams = new ArrayList<>();
        queryParams.add(valuePairToQueryParam("client_id", clientId));
        queryParams.add(valuePairToQueryParam("scope", scope));

        Request request = httpClient.newRequest(deviceUrl + "?" + String.join("&", queryParams)) //
                .method(HttpMethod.POST).timeout(5, TimeUnit.SECONDS);
        logger.trace("step1and2Execute() => {}", request);

        try {
            ContentResponse response = request.send();
            String content = response.getContentAsString();
            logger.trace("step1and2Execute() <= {}", content);
            if (response.getStatus() == HttpStatus.OK_200) {
                Map<?, ?> tokenValues = gson.fromJson(content, Map.class);
                if (tokenValues != null) {
                    String deviceCode = tokenToString(tokenValues.get("device_code"));
                    String userUri = tokenToString(tokenValues.get("verification_uri_complete"));
                    long expiresIn = tokenToLong(tokenValues.get("expires_in"));
                    long interval = tokenToLong(tokenValues.get("interval"));

                    if (deviceCode != null && userUri != null && expiresIn > 0 && interval > 0) {
                        return new DeviceCodeResponse(deviceCode, userUri, expiresIn, interval);
                    }
                }
            }
            logger.debug("step1and2Execute() error <= {}", response);
        } catch (Exception e) {
            logger.debug("step1and2Execute() error calling {}", deviceUrl, e);
        }
        return null;
    }

    private synchronized void step4and5Cancel() {
        ScheduledFuture<?> task = step4and5Task;
        if (task != null) {
            task.cancel(false);
        }
        step4and5Task = null;
    }

    /**
     * Whilst the user is completing the Device Code Grant Flow authentication process step 3.
     * we continue, in parallel, the completion of the authentication process by repeating the
     * following steps:
     * <ul>
     * <li>Step 4. repeatedly create a request and POST it to the 'token url'</li>
     * <li>Step 5. repeatedly read the response and eventually create a {@link AccessTokenResponse}</li>
     * </ul>
     *
     * @param deviceCode a {@link DeviceCodeResponse}
     * @return an {@link AccessTokenResponse} object or null
     */
    private synchronized AccessTokenResponse step4and5Execute(DeviceCodeResponse deviceCode) {
        List<String> queryParams = new ArrayList<>();
        queryParams.add(valuePairToQueryParam("client_id", clientId));
        queryParams.add(valuePairToQueryParam("grant_type", DEVICE_GRANT));
        queryParams.add(valuePairToQueryParam("device_code", deviceCode.getDeviceCode()));

        Request request = httpClient.newRequest(tokenUrl + "?" + String.join("&", queryParams)) //
                .method(HttpMethod.POST).timeout(5, TimeUnit.SECONDS);
        logger.trace("step4and5Execute() => {}", request);

        try {
            ContentResponse response = request.send();
            String content = response.getContentAsString();
            logger.trace("step4and5Execute() <= {}", content);
            if (response.getStatus() == HttpStatus.OK_200) {
                Map<?, ?> tokenValues = gson.fromJson(content, Map.class);
                if (tokenValues != null) {
                    String accessToken = tokenToString(tokenValues.get("access_token"));
                    String accessTokenType = tokenToString(tokenValues.get("token_type"));
                    String refreshToken = tokenToString(tokenValues.get("refresh_token"));
                    long expiresIn = tokenToLong(tokenValues.get("expires_in"));

                    if (accessToken != null && refreshToken != null && accessTokenType != null && expiresIn > 0) {
                        AccessTokenResponse result = new AccessTokenResponse();
                        result.setAccessToken(accessToken);
                        result.setTokenType(accessTokenType);
                        result.setExpiresIn(expiresIn);
                        result.setRefreshToken(refreshToken);
                        result.setScope(scope);
                        result.setCreatedOn(Instant.now());
                        return result;
                    }
                }
                logger.debug("step4and5Execute() error <= {}", response);
            }
        } catch (Exception e) {
            logger.debug("step4and5Execute() error calling {}", tokenUrl, e);
        }
        return null;
    }

    /**
     * Private class for token parsing
     */
    private long tokenToLong(@Nullable Object object) {
        return object instanceof Integer i ? i : 0;
    }

    /**
     * Private class for token parsing
     */
    private @Nullable String tokenToString(@Nullable Object object) {
        return object instanceof String string ? string : null;
    }

    /**
     * Private class for formatting query parameters
     */
    private String valuePairToQueryParam(String key, String value) {
        try {
            return key + "=" + URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return key + "=" + value;
        }
    }
}
