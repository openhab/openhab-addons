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
package org.openhab.binding.zoneminder.internal.handler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zoneminder.internal.dto.AuthResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ZmAuth} manages the authentication process when Zoneminder
 * authentication is enabled. This class requests access and refresh tokens based
 * on the expiration times provided by the Zoneminder server.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ZmAuth {

    private final Logger logger = LoggerFactory.getLogger(ZmAuth.class);

    private final ZmBridgeHandler bridgeHandler;
    private final String authContent;
    private final boolean usingAuthorization;
    private boolean isAuthorized;

    private @Nullable String refreshToken;
    private long refreshTokenExpiresAt;
    private @Nullable String accessToken;
    private long accessTokenExpiresAt;

    public ZmAuth(ZmBridgeHandler handler) {
        this(handler, null, null);
    }

    public ZmAuth(ZmBridgeHandler handler, @Nullable String user, @Nullable String pass) {
        this.bridgeHandler = handler;
        if (user == null || pass == null) {
            logger.debug("ZmAuth: Authorization is disabled");
            usingAuthorization = false;
            isAuthorized = true;
            authContent = "";
        } else {
            logger.debug("ZmAuth: Authorization is enabled");
            usingAuthorization = true;
            isAuthorized = false;
            String encodedUser = URLEncoder.encode(user, StandardCharsets.UTF_8);
            String encodedPass = URLEncoder.encode(pass, StandardCharsets.UTF_8);
            authContent = encodedUser == null ? ""
                    : String.format("user=%s&pass=%s&stateful=1", encodedUser, encodedPass);
        }
    }

    public String getAccessToken() {
        String localAccessToken = accessToken;
        return localAccessToken != null ? localAccessToken : "";
    }

    public boolean usingAuthorization() {
        return usingAuthorization;
    }

    public boolean isAuthorized() {
        if (usingAuthorization()) {
            checkTokens();
        }
        return isAuthorized;
    }

    private void checkTokens() {
        if (isExpired(refreshTokenExpiresAt)) {
            getNewRefreshToken();
        } else if (isExpired(accessTokenExpiresAt)) {
            getNewAccessToken();
        }
    }

    @SuppressWarnings("null")
    private synchronized void getNewRefreshToken() {
        // First check to see if another thread has updated it
        if (!isExpired(refreshTokenExpiresAt)) {
            return;
        }
        String url = bridgeHandler.buildLoginUrl();
        logger.debug("ZmAuth: Update expired REFRESH token using url '{}'", url);
        String response = bridgeHandler.executePost(url, authContent, "application/x-www-form-urlencoded");
        if (response != null) {
            Gson gson = bridgeHandler.getGson();
            AuthResponseDTO auth = gson.fromJson(response, AuthResponseDTO.class);
            if (auth != null && auth.exception == null && auth.refreshToken != null && auth.accessToken != null) {
                updateRefreshToken(auth);
                updateAccessToken(auth);
                isAuthorized = true;
                return;
            }
        }
        isAuthorized = false;
    }

    @SuppressWarnings("null")
    private synchronized void getNewAccessToken() {
        // First check to see if another thread has updated it
        if (!isExpired(accessTokenExpiresAt)) {
            return;
        }
        String url = bridgeHandler.buildLoginUrl(String.format("?token=%s", refreshToken));
        logger.debug("ZmAuth: Update expired ACCESS token using url '{}'", url);
        String response = bridgeHandler.executeGet(url);
        if (response != null) {
            Gson gson = bridgeHandler.getGson();
            AuthResponseDTO auth = gson.fromJson(response, AuthResponseDTO.class);
            if (auth != null && auth.exception == null && auth.accessToken != null) {
                updateAccessToken(auth);
                isAuthorized = true;
                return;
            }
        }
        isAuthorized = false;
    }

    private void updateAccessToken(AuthResponseDTO auth) {
        accessToken = auth.accessToken;
        accessTokenExpiresAt = getExpiresAt(auth.accessTokenExpires);
        logger.trace("ZmAuth: New access token:  {}", accessToken);
        logger.trace("ZmAuth: New access token expires in {} sec", getExpiresIn(accessTokenExpiresAt));
    }

    private void updateRefreshToken(AuthResponseDTO auth) {
        refreshToken = auth.refreshToken;
        refreshTokenExpiresAt = getExpiresAt(auth.refreshTokenExpires);
        logger.trace("ZmAuth: New refresh token: {}", refreshToken);
        logger.trace("ZmAuth: New refresh token expires in {} sec", getExpiresIn(refreshTokenExpiresAt));
    }

    private boolean isExpired(long expiresAt) {
        return (System.currentTimeMillis() / 1000) > expiresAt;
    }

    private long getExpiresAt(String expiresInSeconds) {
        try {
            return (System.currentTimeMillis() / 1000) + (Integer.parseInt(expiresInSeconds) - 300);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long getExpiresIn(long expiresAtSeconds) {
        return expiresAtSeconds - (System.currentTimeMillis() / 1000);
    }
}
