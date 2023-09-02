/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
    private static final AccessTokenResponse INVALID_TOKEN = new AccessTokenResponse();
    private static final int EXPIRATION_BUFFER = 5;
    private static final Map<Integer, AuthService> AUTH_MAP = new HashMap<Integer, AuthService>();
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
            logger.info("Got nothing from storage for {}", identifier);
            token = INVALID_TOKEN;
            logger.info("no token found in storage");
            listener.onAccessTokenResponse(token);
        } else {
            logger.info("Got {} from storage for {}", storedObject, identifier);
            token = (AccessTokenResponse) Utils.fromString(storedObject);
            if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
                if (!token.getRefreshToken().equals(Constants.NOT_SET)) {
                    refreshToken();
                    listener.onAccessTokenResponse(token);
                } else {
                    logger.info("Refresh token empty");
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
     * @return guid from request to create password in next step
     */
    public String requestPin() {
        String url = Utils.getAuthURL(config.region);
        Request req = httpClient.POST(url);
        addBasicHeaders(req);
        req.header("X-Trackingid", UUID.randomUUID().toString());
        req.header("X-Sessionid", UUID.randomUUID().toString());

        PINRequest pr = new PINRequest(config.email, locale.getCountry());
        req.header(HttpHeader.CONTENT_TYPE, "application/json");
        req.content(new StringContentProvider(Utils.GSON.toJson(pr), "utf-8"));

        try {
            ContentResponse cr = req.send();
            if (cr.getStatus() == 200) {
                logger.debug("Success requesting PIN");
                return pr.nonce;
            } else {
                logger.debug("Failed to get image resources {} {}", cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Error getting image resources {}", e.getMessage());
        }
        return Constants.NOT_SET;
    }

    public boolean requestToken(String password) {
        logger.info("Request Token");
        try {
            // Request + headers
            String url = Utils.getTokenUrl(config.region);
            logger.info("Get Token base URL {}", url);
            Request req = httpClient.POST(url);
            addBasicHeaders(req);
            req.header("Stage", "prod");
            req.header("X-Device-Id", UUID.randomUUID().toString());
            req.header("X-Request-Id", UUID.randomUUID().toString());

            // Content URL form
            String clientid = "client_id="
                    + URLEncoder.encode(Utils.getLoginAppId(config.region), StandardCharsets.UTF_8.toString());
            String grantAttribute = "grant_type=password";
            String userAttribute = "username=" + URLEncoder.encode(config.email, StandardCharsets.UTF_8.toString());
            String passwordAttribute = "password=" + URLEncoder.encode(password, StandardCharsets.UTF_8.toString());
            String scopeAttribute = "scope=" + URLEncoder.encode(Constants.SCOPE, StandardCharsets.UTF_8.toString());
            String content = clientid + "&" + grantAttribute + "&" + userAttribute + "&" + passwordAttribute + "&"
                    + scopeAttribute;
            logger.info("Get Token Content {}", content);
            req.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
            req.content(new StringContentProvider(content));

            // Send
            ContentResponse cr = req.send();
            if (cr.getStatus() == 200) {
                String responseString = cr.getContentAsString();
                logger.info("Success getting token: {}", responseString);
                saveTokenResponse(responseString);
                logger.info("ATR {}", token);
                listener.onAccessTokenResponse(token);
                return true;
            } else {
                logger.debug("Failed to get token {} {}", cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException e) {
            logger.debug("Failed to get token {}", e.getMessage());
        }
        return false;
    }

    public void refreshToken() {
        logger.info("Refresh Token");
        try {
            // Request + headers
            String url = Utils.getTokenUrl(config.region);
            logger.info("Get Token base URL {}", url);
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
            ContentResponse cr = req.send();
            if (cr.getStatus() == 200) {
                logger.info("Success getting token");
                saveTokenResponse(cr.getContentAsString());
                listener.onAccessTokenResponse(token);
                logger.info("ATR {}", token);
            } else {
                logger.debug("Failed to get image resources {} {}", cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException e) {
            logger.debug("Error getting image resources {}", e.getMessage());
        }
    }

    public String getToken() {
        logger.info("Investigate token {}", token);
        if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
            logger.info("Token {} expired", token);
            if (!token.getRefreshToken().equals(Constants.NOT_SET)) {
                refreshToken();
                // token shall be updated now - retry expired check
                if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
                    token = INVALID_TOKEN;
                    logger.warn("Not able to return fresh token");
                    listener.onAccessTokenResponse(token);
                    return Constants.NOT_SET;
                }
            } else {
                token = INVALID_TOKEN;
                logger.info("Refresh token empty");
            }
        }
        return token.getAccessToken();
    }

    private void addBasicHeaders(Request req) {
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
        atr.setAccessToken(tr.access_token);
        atr.setCreatedOn(Instant.now());
        atr.setExpiresIn(tr.expires_in);
        // Preserve refresh token if available
        if (Constants.NOT_SET.equals(tr.refresh_token) && !Constants.NOT_SET.equals(token.getRefreshToken())) {
            logger.info("Preserve refresh token {}", token.getRefreshToken());
            atr.setRefreshToken(token.getRefreshToken());
        } else if (!Constants.NOT_SET.equals(tr.refresh_token)) {
            logger.info("New refresh token {}", tr.refresh_token);
            atr.setRefreshToken(tr.refresh_token);
        } else {
            logger.info("Neither new nor old refresh token available");
        }
        atr.setTokenType("Bearer");
        atr.setScope(Constants.SCOPE);
        logger.info("Store at {} token {}", identifier, atr);
        storage.put(identifier, Utils.toString(atr));
        token = atr;
    }
}
