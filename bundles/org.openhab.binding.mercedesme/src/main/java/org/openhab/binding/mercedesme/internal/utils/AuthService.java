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
package org.openhab.binding.mercedesme.internal.utils;

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

    private HttpClient httpClient;
    private String identifier;
    private AccountConfiguration config;
    private Locale locale;
    private Storage<AccessTokenResponse> storage;
    private AccessTokenResponse token;

    public AuthService(HttpClient hc, AccountConfiguration ac, Locale l, Storage<AccessTokenResponse> store) {
        httpClient = hc;
        identifier = config.email;
        config = ac;
        locale = l;
        storage = store;

        // restore token
        Object storedObject = storage.get(identifier);
        if (storedObject == null) {
            token = INVALID_TOKEN;
        } else {
            token = (AccessTokenResponse) storedObject;
            if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
                refreshToken();
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
        req.header("Ris-Os-Name", Constants.RIS_OS_NAME);
        req.header("Ris-Os-Version", Constants.RIS_OS_VERSION);
        req.header("Ris-Sdk-Version", Utils.getRisSDKVersion(config.region));
        req.header("X-Locale", locale.getLanguage() + "-" + locale.getCountry()); // de-DE
        req.header("User-Agent", Utils.getApplication(config.region));
        req.header("X-Applicationname", Utils.getUserAgent(config.region));
        req.header("Ris-Application-Version", Utils.getRisApplicationVersion(config.region));
        req.header("X-Trackingid", UUID.randomUUID().toString());
        req.header("X-Sessionid", UUID.randomUUID().toString());
        req.header(HttpHeader.CONTENT_TYPE, "application/json");

        PINRequest pr = new PINRequest(config.email, locale.getCountry());
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

    public void requestToken(String password) {
        logger.info("Request Token");
        try {
            Request req = getTokenRequest();
            req.header("Stage", "prod");
            String clientid = "client_id="
                    + URLEncoder.encode(Utils.getLoginAppId(config.region), StandardCharsets.UTF_8.toString());
            String grantAttribute = "grant_type=password";
            String userAttribute = "username=" + URLEncoder.encode(config.region, StandardCharsets.UTF_8.toString());
            String passwordAttribute = "password=" + URLEncoder.encode(password, StandardCharsets.UTF_8.toString());
            String scopeAttribute = "scope=" + URLEncoder.encode(Constants.SCOPE, StandardCharsets.UTF_8.toString());
            String content = clientid + "&" + grantAttribute + "&" + userAttribute + "&" + passwordAttribute + "&"
                    + scopeAttribute;
            logger.info("Get Token Content {}", content);
            req.content(new StringContentProvider(content));
            ContentResponse cr = req.send();
            if (cr.getStatus() == 200) {
                logger.info("Success getting token");
                saveTokenResponse(cr.getContentAsString());
                logger.info("ATR {}", token);
            } else {
                logger.debug("Failed to get image resources {} {}", cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException e) {
            logger.debug("Error getting image resources {}", e.getMessage());
        }
    }

    public void refreshToken() {
        logger.info("Refresh Token");
        try {
            Request req = getTokenRequest();
            String grantAttribute = "grant_type=refresh_token";
            String refreshTokenAttribute = "refresh_token="
                    + URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8.toString());
            String content = grantAttribute + "&" + refreshTokenAttribute;
            req.content(new StringContentProvider(content));
            ContentResponse cr = req.send();
            if (cr.getStatus() == 200) {
                logger.info("Success getting token");
                saveTokenResponse(cr.getContentAsString());
                logger.info("ATR {}", token);
            } else {
                logger.debug("Failed to get image resources {} {}", cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException e) {
            logger.debug("Error getting image resources {}", e.getMessage());
        }
    }

    public String getToken() {
        if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
            refreshToken();
            // token shall be updated now - retry expired check
            if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
                logger.warn("Not able to return fresh token");
                return Constants.NOT_SET;
            }
        }
        return token.getAccessToken();
    }

    private Request getTokenRequest() {
        String url = Utils.getTokenUrl(config.region);
        logger.info("Get Token base URL {}", url);
        Request req = httpClient.POST(url);
        req.header("Ris-Os-Name", Constants.RIS_OS_NAME);
        req.header("Ris-Os-Version", Constants.RIS_OS_VERSION);
        req.header("Ris-Sdk-Version", Utils.getRisSDKVersion(config.region));
        req.header("X-Locale", locale.getLanguage() + "-" + locale.getCountry()); // de-DE
        req.header("User-Agent", Utils.getApplication(config.region));
        req.header("X-Applicationname", Utils.getUserAgent(config.region));
        req.header("Ris-Application-Version", Utils.getRisApplicationVersion(config.region));
        req.header("X-Device-Id", UUID.randomUUID().toString());
        req.header("X-Request-Id", UUID.randomUUID().toString());
        req.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        return req;
    }

    private void saveTokenResponse(String response) {
        logger.info("Save new token");
        TokenResponse tr = Utils.GSON.fromJson(response, TokenResponse.class);
        AccessTokenResponse atr = new AccessTokenResponse();
        atr.setAccessToken(tr.access_token);
        atr.setCreatedOn(Instant.now());
        atr.setExpiresIn(tr.expires_in);
        atr.setRefreshToken(tr.refresh_token);
        atr.setTokenType("Bearer");
        atr.setScope(Constants.SCOPE);
        storage.put(identifier, atr);
        token = atr;
    }
}
