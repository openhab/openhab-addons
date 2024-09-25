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
package org.openhab.binding.onecta.internal.api;

import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.OAUTH2_SERVICE_HANDLE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationException;
import org.openhab.binding.onecta.internal.oauth2.auth.OAuthTokenRefreshListener;
import org.openhab.binding.onecta.internal.oauth2.auth.OAuthTokenRefresher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//un signleton
@NonNullByDefault
public class OnectaSignInClient implements OAuthTokenRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(OnectaSignInClient.class);

    private String refreshToken = "";
    private String userId = "";
    private String password = "";

    private String accessToken = "";

    private @Nullable OAuthTokenRefresher oAuthTokenRefresher;

    public OnectaSignInClient() {
        super();
        this.oAuthTokenRefresher = OnectaConfiguration.getOAuthTokenRefresher();
    }

    public String getToken() {
        return accessToken;
    }

    protected void signIn() throws DaikinCommunicationException {
        signIn(this.userId, this.password);
    }

    protected void signIn(String userId, String password, String refreshToken) throws DaikinCommunicationException {
        this.userId = userId;
        this.password = password;
        signIn(userId, password);
    }

    protected void signIn(String userId, String password) throws DaikinCommunicationException {
        this.userId = userId;
        this.password = password;

        logger.debug("signIn(String userId, String password)");

        oAuthTokenRefresher.unsetRefreshListener(OAUTH2_SERVICE_HANDLE);
        oAuthTokenRefresher.setRefreshListener(this, OAUTH2_SERVICE_HANDLE);
        logger.debug("refreshlistener set");

        logger.debug("Login successful");
    }

    public void fetchAccessToken() throws DaikinCommunicationException {
        logger.debug("Refresh token.");
        accessToken = "";

        try {
            oAuthTokenRefresher.refreshToken(OAUTH2_SERVICE_HANDLE);
        } catch (Throwable e) {
            throw new DaikinCommunicationException(e);
        }
    }

    public Boolean isOnline() {
        return !this.accessToken.isEmpty();
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public void onNewAccessToken(String accessToken) {
        this.accessToken = accessToken;

        logger.debug("new access token: " + accessToken);
    }
}
