/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.oauth2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.spotify.internal.api.exception.SpotifyAuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache for the Spotify access token and refresh token. If the access token is expired or if the data is invalidated it
 * calls Spotify to get a new access token prior to returning the access token.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class SpotifyAccessTokenCache {

    /**
     * Subtract this value from the actual provided expiring time so the refresh call be certainly be done before it's
     * actually expired.
     */
    private static final long EXPIRE_MARGIN_NANO = TimeUnit.MINUTES.toNanos(1);
    private final Logger logger = LoggerFactory.getLogger(SpotifyAccessTokenCache.class);

    private final SpotifyAuthorizer authorizer;
    private final List<AccessTokenRefreshListener> listeners = new ArrayList<>();

    private String accessToken = "";
    private String refreshToken = "";
    private long expiresAt;

    /**
     * Constructor.
     *
     * @param authorizer The authorizer used to call the Spotify Web Api refresh method.
     * @param accessTokenChangeHandler handler to be called when the access token has changed.
     */
    public SpotifyAccessTokenCache(SpotifyAuthorizer authorizer) {
        this.authorizer = authorizer;
    }

    public void addAccessTokenRefreshListener(AccessTokenRefreshListener accessTokenRefreshListener) {
        listeners.add(accessTokenRefreshListener);
    }

    /**
     * Sets the authorization credentials. This methods can be used to set the initial credentials.
     *
     * @param credentials The authorization credentials
     */
    public void setAuthorizationCodeCredentials(AccessTokenResponse credentials) {
        refreshToken = credentials.getRefreshToken() == null ? "" : credentials.getRefreshToken();
        updateAccessToken(credentials);
    }

    /**
     * Returns the value - possibly from the cache, if it is still valid otherwise it calls Spotify to obtain a new
     * access token.
     */
    @Nullable
    public synchronized String getAccessToken() {
        if (isExpired()) {
            logger.debug("Access token time expired, getting a new access token.");
            if (refreshToken.isEmpty()) {
                throw new SpotifyAuthorizationException("No Spotify refresh token. Please authorize this thing first.");
            }
            updateAccessToken(authorizer.refresh(refreshToken));
        }
        return accessToken;
    }

    /**
     * Invalidates the access token and refresh token.
     */
    public final synchronized void invalidateValue() {
        expiresAt = 0;
    }

    /**
     * Updates the access token and expires state from the given credentials.
     *
     * @param credentials credentials to update values from
     */
    private void updateAccessToken(AccessTokenResponse credentials) {
        accessToken = credentials.getAccessToken() == null ? "" : credentials.getAccessToken();
        long expiresInNanos = TimeUnit.SECONDS.toNanos(credentials.getExpiresIn());
        // Guard minimal expires time so it will never be less then the value provided by Spotify
        expiresAt = System.nanoTime() + Math.min(expiresInNanos, expiresInNanos - EXPIRE_MARGIN_NANO);
        listeners.forEach(l -> l.onTokenResponse(credentials));
    }

    /**
     * Checks if the value is expired (or not available at all).
     *
     * @return true if the value is expired
     */
    private boolean isExpired() {
        return accessToken.isEmpty() || expiresAt < System.nanoTime();
    }
}
