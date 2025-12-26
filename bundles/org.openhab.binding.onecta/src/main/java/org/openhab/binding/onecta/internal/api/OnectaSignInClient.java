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
package org.openhab.binding.onecta.internal.api;

import static org.openhab.binding.onecta.internal.constants.OnectaBridgeConstants.OAUTH2_SERVICE_HANDLE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationException;
import org.openhab.binding.onecta.internal.oauth2.auth.OAuthTokenRefreshListener;
import org.openhab.binding.onecta.internal.oauth2.auth.OAuthTokenRefresher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaSignInClient implements OAuthTokenRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(OnectaSignInClient.class);

    private @Nullable OAuthTokenRefresher oAuthTokenRefresher;

    public OnectaSignInClient() {
        super();
    }

    public void SignIn() throws DaikinCommunicationException {
        try {
            this.oAuthTokenRefresher = OnectaConfiguration.getOAuthTokenRefresher();
            oAuthTokenRefresher.unsetRefreshListener(OAUTH2_SERVICE_HANDLE);
            oAuthTokenRefresher.setRefreshListener(this, OAUTH2_SERVICE_HANDLE);
        } catch (Throwable e) {
            throw new DaikinCommunicationException(e);
        }
    }

    public void refreshToken() throws DaikinCommunicationException {
        logger.debug("Refresh token.");
        try {
            oAuthTokenRefresher.refreshToken(OAUTH2_SERVICE_HANDLE);
        } catch (Throwable e) {
            throw new DaikinCommunicationException(e);
        }
    }

    @Override
    public void onNewAccessToken(String accessToken) {
        logger.debug("new access token: {}", accessToken);
    }

    public String getToken() throws DaikinCommunicationException {
        try {
            return oAuthTokenRefresher.getAccessTokenFromStorage(OAUTH2_SERVICE_HANDLE).get();
        } catch (Throwable e) {
            throw new DaikinCommunicationException(e);
        }
    }

    public Boolean isOnline() {
        try {
            return !getToken().isEmpty();
        } catch (Throwable e) {
            return false;
        }
    }
}
