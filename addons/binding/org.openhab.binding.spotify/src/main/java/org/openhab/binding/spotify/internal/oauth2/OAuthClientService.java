/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.oauth2;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.spotify.internal.api.SpotifyConnector;

/**
 * OAuthClientService class modeled after the ESH OAuth2 service. It has the same interface as the ESH OAuth2 service,
 * but doesn't implement it in the same way therefore some methods are not implemented.
 * This class will be removed when the ESH OAuth2 service can be used with this binding.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class OAuthClientService {

    private final SpotifyAuthorizer spotifyAuthorizer;
    private final String clientId;
    private final SpotifyAccessTokenCache accessTokenCache;

    OAuthClientService(String clientId, String clientSecret, ScheduledExecutorService scheduler,
            HttpClient httpClient) {
        this.clientId = clientId;
        SpotifyConnector connector = new SpotifyConnector(scheduler, httpClient);
        spotifyAuthorizer = new SpotifyAuthorizer(connector, clientId, clientSecret);
        accessTokenCache = new SpotifyAccessTokenCache(spotifyAuthorizer);
    }

    public void setAuthorizationCodeCredentials(AccessTokenResponse credentials) {
        accessTokenCache.setAuthorizationCodeCredentials(credentials);
    }

    public AccessTokenResponse getAccessToken() throws OAuthResponseException, OAuthException, IOException {
        AccessTokenResponse atr = new AccessTokenResponse();
        atr.setAccessToken(accessTokenCache.getAccessToken());
        return atr;
    }

    public AccessTokenResponse refreshToken() throws OAuthResponseException, OAuthException, IOException {
        accessTokenCache.invalidateValue();
        return getAccessToken();
    }

    public void addAccessTokenRefreshListener(AccessTokenRefreshListener accessTokenRefreshListener) {
        accessTokenCache.addAccessTokenRefreshListener(accessTokenRefreshListener);
    }

    public void removeAccessTokenRefreshListener(AccessTokenRefreshListener accessTokenRefreshListener) {
        // No implementation
    }

    public String getAuthorizationUrl(String redirectUri, @Nullable String scope, String state) throws OAuthException {
        return spotifyAuthorizer.formatAuthorizationUrl(redirectUri, clientId, state);
    }

    public AccessTokenResponse getAccessTokenResponseByAuthorizationCode(String reqCode, String redirectUri)
            throws OAuthResponseException, OAuthException, IOException {
        AccessTokenResponse atr = spotifyAuthorizer.requestTokens(redirectUri, reqCode);

        accessTokenCache.setAuthorizationCodeCredentials(atr);
        return atr;
    }

    public void importAccessTokenResponse(AccessTokenResponse acr) {
        accessTokenCache.setAuthorizationCodeCredentials(acr);
    }
}
