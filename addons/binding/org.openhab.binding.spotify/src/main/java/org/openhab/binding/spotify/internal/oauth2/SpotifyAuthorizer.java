/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.oauth2;

import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.B64Code;
import org.openhab.binding.spotify.internal.api.SpotifyConnector;
import org.openhab.binding.spotify.internal.api.model.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle calls to Spotify Web Api authorization and refresh calls.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Complete rewrite. Combined refresh and authorize.
 */
@NonNullByDefault
public class SpotifyAuthorizer {

    private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String AUTHORIZATION_URL = "%s?client_id=%s&response_type=code&redirect_uri=%s&state=%s&scope=%s";
    private static final String REFRESH_QUERY = "grant_type=refresh_token&refresh_token=";
    private static final String REQUEST_TOKENS_QUERY = "grant_type=authorization_code&code=%s&redirect_uri=%s";
    private static final String AUTHORIZATION_HEADER_BASIC = "Basic ";

    private final Logger logger = LoggerFactory.getLogger(SpotifyAuthorizer.class);
    private final SpotifyConnector connector;
    private final String clientId;
    private final String clientSecret;

    /**
     * Constructor.
     *
     * @param connector The Spotify connector handling the Web Api calls to Spotify
     * @param clientId The Spotify provided App clientId
     * @param clientSecret The Spotify provided App clientSecret
     */
    public SpotifyAuthorizer(SpotifyConnector connector, String clientId, String clientSecret) {
        this.connector = connector;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     *
     * @param redirectUri The redirectUri url Spotify will redirect to
     * @param clientId
     * @param reqCode A unique code to identify this call. This will be send back by Spotify with te callback
     * @return The url to call the Spotify to authorize the binding
     */
    public String formatAuthorizationUrl(String redirectUri, String clientId, String reqCode) {
        try {
            return String.format(AUTHORIZATION_URL, SPOTIFY_AUTHORIZE_URL, clientId,
                    URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name()), reqCode, SPOTIFY_SCOPES);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Error creating the spotify authorization url: ", e);
            return redirectUri;
        }
    }

    /**
     * Call the Spotify Web API to retrieve the access and refresh tokens.
     *
     * This is step 2 in Spotify Web API authorization code flow.
     * See https://developer.spotify.com/web-api/authorization-guide/
     *
     * @param redirectUri The redirectUri url Spotify will redirect to
     * @param reqCode A unique code to identify this call. This will be send back by Spotify with te callback
     * @return The parsed Spotify credentials of the authorized user
     */
    public AccessTokenResponse requestTokens(String redirectUri, String reqCode) {
        logger.debug("Call Spotify to request access and refresh token.");
        return request(client -> postRequest(client, String.format(REQUEST_TOKENS_QUERY, reqCode, redirectUri)));
    }

    /**
     * Call the Spotify Web API to get a new access token and refresh token.
     *
     * This is step 4 in Spotify Web API authorization code flow.
     * See https://developer.spotify.com/web-api/authorization-guide/
     *
     * @param refreshToken The current refresh token
     * @return The parsed Spotify credentials of the authorized user with the new access token and refresh token
     */
    public AccessTokenResponse refresh(String refreshToken) {
        logger.debug("Call Spotify to get a new refresh token.");
        return request(client -> postRequest(client, REFRESH_QUERY + refreshToken));
    }

    private AccessTokenResponse request(Function<HttpClient, Request> function) {
        String authString = B64Code.encode(String.format("%s:%s", clientId, clientSecret));
        ContentResponse response = connector.request(function, AUTHORIZATION_HEADER_BASIC + authString);
        return ModelUtil.gsonInstance().fromJson(response.getContentAsString(), AccessTokenResponse.class);
    }

    /**
     * Performs a HTTP POST action with the given content.
     *
     * @param httpClient HttpClient sending the post
     * @param content content to post
     * @return The {@link Request} object
     */
    private Request postRequest(HttpClient httpClient, String content) {
        return httpClient.POST(SPOTIFY_API_TOKEN_URL).content(new StringContentProvider(content),
                APPLICATION_X_WWW_FORM_URLENCODED);
    }
}
