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
package org.openhab.binding.mercedesme.internal.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
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
    private static final int EXPIRATION_BUFFER = 5;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private AccessTokenRefreshListener listener;
    private AccountConfiguration config;
    private AccessTokenResponse token;
    private Storage<String> storage;
    private HttpClient httpClient;
    private String identifier;
    private Locale locale;

    public AuthService(AccessTokenRefreshListener atrl, HttpClient hc, AccountConfiguration ac, Locale l,
            Storage<String> store, String refreshToken) {
        listener = atrl;
        httpClient = hc;
        config = ac;
        identifier = config.email;
        locale = l;
        storage = store;

        // restore token from persistence if available
        String storedObject = storage.get(identifier);
        if (storedObject != null) {
            // returns INVALID_TOKEN in case of an error
            logger.trace("MB-Auth {} Restore token from persistence", prefix());
            token = Utils.fromString(storedObject);
        } else {
            // initialize token with refresh token from configuration with expiration 0 - triggers refreshToken
            logger.trace("MB-Auth {} Create token from config", prefix());
            token = new AccessTokenResponse();
            token.setAccessToken(Constants.NOT_SET);
            token.setRefreshToken(refreshToken);
            token.setExpiresIn(0);
        }

        if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
            if (!Constants.NOT_SET.equals(token.getRefreshToken())) {
                // expired but refresh token available
                refreshToken();
            } else {
                // token expired and no refresh token available will put bridge into OFFLINE mode
                listener.onAccessTokenResponse(token);
            }
        } else {
            // valid token
            listener.onAccessTokenResponse(token);
        }
        logger.trace("MB-Auth {} Token after init {}", prefix(), token.toString());
    }

    public String getToken() {
        if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
            if (!Constants.NOT_SET.equals(token.getRefreshToken())) {
                refreshToken();
            }
        }
        return token.getAccessToken();
    }

    private void refreshToken() {
        logger.trace("MB-Auth {} refresh token", prefix());
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
                token = saveTokenResponse(cr.getContentAsString());
            } else {
                token = Utils.INVALID_TOKEN;
                /**
                 * 1) remove token from storage
                 * 2) listener will be informed about INVALID_TOKEN and bridge will go OFFLINE
                 * 3) user needs to update refreshToken configuration parameter
                 */
                storage.remove(identifier);
                logger.trace("{} Failed to refresh token {} {}", prefix(), cr.getStatus(), cr.getContentAsString());
            }
            listener.onAccessTokenResponse(token);
        } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException e) {
            logger.trace("{} Failed to refresh token {}", prefix(), e.getMessage());
        }
        logger.trace("MB-Auth {} Token after refresh {}", prefix(), token.toString());
    }

    private AccessTokenResponse saveTokenResponse(String response) {
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
            return atr;
        } else {
            logger.trace("{} Token Response is null", prefix());
            return Utils.INVALID_TOKEN;
        }
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

    private String prefix() {
        return "[" + config.email + "] ";
    }
}
