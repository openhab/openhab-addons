/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.dto.PINRequest;
import org.openhab.binding.mercedesme.internal.dto.TokenResponse;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AuthService} helpers for token management
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AuthService {
    public static final AccessTokenResponse INVALID_TOKEN = new AccessTokenResponse();
    private static final int EXPIRATION_BUFFER = 5;
    private static final Map<Integer, AuthService> AUTH_MAP = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    AccessTokenRefreshListener listener;
    private HttpClient httpClient;
    private String identifier;
    private AccountConfiguration config;
    private Locale locale;
    private Storage<String> storage;
    private AccessTokenResponse token;

    public AuthService(AccessTokenRefreshListener atrl, HttpClient hc, AccountConfiguration ac, Locale l,
            Storage<String> store) {
        INVALID_TOKEN.setAccessToken(Constants.NOT_SET);
        INVALID_TOKEN.setRefreshToken(Constants.NOT_SET);
        listener = atrl;
        httpClient = hc;
        config = ac;
        identifier = config.email;
        locale = l;
        storage = store;

        // restore token
        String storedObject = storage.get(identifier);
        if (storedObject == null) {
            token = INVALID_TOKEN;
            listener.onAccessTokenResponse(token);
        } else {
            token = Utils.fromString(storedObject);
            if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
                if (!Constants.NOT_SET.equals(token.getRefreshToken())) {
                    refreshToken();
                    listener.onAccessTokenResponse(token);
                } else {
                    token = INVALID_TOKEN;
                    listener.onAccessTokenResponse(token);
                }
            } else {
                listener.onAccessTokenResponse(token);
            }
        }
        AUTH_MAP.put(config.callbackPort, this);
    }

    @Nullable
    public static AuthService getAuthService(Integer key) {
        return AUTH_MAP.get(key);
    }

    /**
     *
     * @return guid from request to create token in next step
     */
    public String requestPin() {
        String configUrl = Utils.getAuthConfigURL(config.region);
        String sessionId = UUID.randomUUID().toString();
        Request configRequest = httpClient.newRequest(configUrl);
        addBasicHeaders(configRequest);
        configRequest.header("X-Trackingid", UUID.randomUUID().toString());
        configRequest.header("X-Sessionid", sessionId);
        try {
            ContentResponse cr = configRequest.timeout(Constants.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
            if (cr.getStatus() == 200) {
                logger.trace("{} Config Request PIN fine {} {}", prefix(), cr.getStatus(), cr.getContentAsString());
            } else {
                logger.trace("{} Failed to request config for pin {} {}", prefix(), cr.getStatus(),
                        cr.getContentAsString());
                return Constants.NOT_SET;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.trace("{} Failed to request config for pin {}", prefix(), e.getMessage());
            return Constants.NOT_SET;
        }

        String url = Utils.getAuthURL(config.region);
        Request req = httpClient.POST(url);
        addBasicHeaders(req);
        req.header("X-Trackingid", UUID.randomUUID().toString());
        req.header("X-Sessionid", sessionId);

        PINRequest pr = new PINRequest(config.email, locale.getCountry());
        req.header(HttpHeader.CONTENT_TYPE, "application/json");
        logger.trace("{} payload {}", url, Utils.GSON.toJson(pr));
        req.content(new StringContentProvider(Utils.GSON.toJson(pr), "utf-8"));

        try {
            ContentResponse cr = req.timeout(Constants.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
            if (cr.getStatus() == 200) {
                logger.trace("{} Request PIN fine {} {}", prefix(), cr.getStatus(), cr.getContentAsString());
                return pr.nonce;
            } else {
                logger.trace("{} Failed to request pin {} {}", prefix(), cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.trace("{} Failed to request pin {}", prefix(), e.getMessage());
        }
        return Constants.NOT_SET;
    }

    public boolean requestToken(String password) {
        try {
            // Request + headers
            String url = Utils.getTokenUrl(config.region);
            Request req = httpClient.POST(url);
            addBasicHeaders(req);
            req.header("Stage", "prod");
            req.header("X-Device-Id", UUID.randomUUID().toString());
            req.header("X-Request-Id", UUID.randomUUID().toString());

            // Content URL form
            String clientId = "client_id="
                    + URLEncoder.encode(Utils.getLoginAppId(config.region), StandardCharsets.UTF_8.toString());
            String grantAttribute = "grant_type=password";
            String userAttribute = "username=" + URLEncoder.encode(config.email, StandardCharsets.UTF_8.toString());
            String passwordAttribute = "password=" + URLEncoder.encode(password, StandardCharsets.UTF_8.toString());
            String scopeAttribute = "scope=" + URLEncoder.encode(Constants.SCOPE, StandardCharsets.UTF_8.toString());
            String content = clientId + "&" + grantAttribute + "&" + userAttribute + "&" + passwordAttribute + "&"
                    + scopeAttribute;
            req.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
            req.content(new StringContentProvider(content));

            // Send
            ContentResponse cr = req.timeout(Constants.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
            if (cr.getStatus() == 200) {
                String responseString = cr.getContentAsString();
                saveTokenResponse(responseString);
                listener.onAccessTokenResponse(token);
                return true;
            } else {
                logger.trace("{} Failed to get token {} {}", prefix(), cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException e) {
            logger.trace("{} Failed to get token {}", prefix(), e.getMessage());
        }
        return false;
    }

    public void refreshToken() {
        try {
            String url = Utils.getTokenUrl(config.region);
            Request req = httpClient.POST(url);
            req.header("X-Device-Id", UUID.randomUUID().toString());
            req.header("X-Request-Id", UUID.randomUUID().toString());

            // Content URL form
            String grantAttribute = "grant_type=refresh_token";
            String refreshTokenAttribute = "refresh_token="
                    + URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8.toString());
            String content = grantAttribute + "&" + refreshTokenAttribute;
            req.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
            req.content(new StringContentProvider(content));

            // Send
            ContentResponse cr = req.timeout(Constants.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
            if (cr.getStatus() == 200) {
                saveTokenResponse(cr.getContentAsString());
                listener.onAccessTokenResponse(token);
            } else {
                logger.trace("{} Failed to refresh token {} {}", prefix(), cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException e) {
            logger.trace("{} Failed to refresh token {}", prefix(), e.getMessage());
        }
    }

    public String getToken() {
        if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
            if (!Constants.NOT_SET.equals(token.getRefreshToken())) {
                refreshToken();
                // token shall be updated now - retry expired check
                if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
                    token = INVALID_TOKEN;
                    listener.onAccessTokenResponse(token);
                    return Constants.NOT_SET;
                }
            } else {
                token = INVALID_TOKEN;
                logger.trace("{} Refresh token empty", prefix());
            }
        }
        return token.getAccessToken();
    }

    public void addBasicHeaders(Request req) {
        req.header("Ris-Os-Name", Constants.RIS_OS_NAME);
        req.header("Ris-Os-Version", Constants.RIS_OS_VERSION);
        req.header("Ris-Sdk-Version", Utils.getRisSDKVersion(config.region));
        req.header("X-Locale", locale.getLanguage() + "-" + locale.getCountry()); // de-DE
        req.header("User-Agent", Utils.getApplication(config.region));
        req.header("X-Applicationname", Utils.getUserAgent(config.region));
        req.header("Ris-Application-Version", Utils.getRisApplicationVersion(config.region));
    }

    private void saveTokenResponse(String response) {
        TokenResponse tr = Utils.GSON.fromJson(response, TokenResponse.class);
        AccessTokenResponse atr = new AccessTokenResponse();
        if (tr != null) {
            atr.setAccessToken(tr.accessToken);
            atr.setCreatedOn(Instant.now());
            atr.setExpiresIn(tr.expiresIn);
            // Preserve refresh token if available
            if (Constants.NOT_SET.equals(tr.refreshToken) && !Constants.NOT_SET.equals(token.getRefreshToken())) {
                atr.setRefreshToken(token.getRefreshToken());
            } else if (!Constants.NOT_SET.equals(tr.refreshToken)) {
                atr.setRefreshToken(tr.refreshToken);
            } else {
                logger.trace("{} Neither new nor old refresh token available", prefix());
            }
            atr.setTokenType("Bearer");
            atr.setScope(Constants.SCOPE);
            storage.put(identifier, Utils.toString(atr));
            token = atr;
        } else {
            logger.trace("{} Token Response is null", prefix());
        }
    }

    private String prefix() {
        return "[" + config.email + "] ";
    }
}
