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
package org.openhab.binding.smartthings.internal.api;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class to handle Smartthings Web Api calls.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartthingsApi {

    private static final String BEARER = "Bearer ";

    private final Logger logger = LoggerFactory.getLogger(SmartthingsApi.class);

    private final OAuthClientService oAuthClientService;
    private final SmartthingsNetworkConnector networkConnector;

    /**
     * Constructor.
     *
     * @param authorizer The authorizer used to refresh the access token when expired
     * @param connector The Spotify connector handling the Web Api calls to Spotify
     */
    public SmartthingsApi(HttpClientFactory httpClientFactory, OAuthClientService oAuthClientService) {
        this.oAuthClientService = oAuthClientService;
        this.networkConnector = new SmartthingsNetworkConnectorImpl(httpClientFactory, oAuthClientService);
    }

    public JsonArray GetAllDevices() {
        JsonElement result = DoRequest("https://api.smartthings.com/v1/devices");
        JsonElement res1 = ((JsonObject) result).get("items");
        JsonArray devices = res1.getAsJsonArray();
        return devices;

    }

    public void SendCommand(String deviceId, String jsonMsg) {
        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();

            String uri = "https://api.smartthings.com/v1/devices/" + deviceId + "/commands";

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException(
                        "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
            } else {

                networkConnector.DoRequest(uri, null, accessToken, jsonMsg, HttpMethod.POST);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (OAuthException | OAuthResponseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public @Nullable JsonObject SendStatus(String deviceId, String jsonMsg) {
        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();

            String uri = "https://api.smartthings.com/v1/devices/" + deviceId + "/status";

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException(
                        "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
            } else {

                JsonObject res = networkConnector.DoRequest(uri, null, accessToken, jsonMsg, HttpMethod.GET);
                return res;
            }
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (OAuthException | OAuthResponseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    private @Nullable JsonElement DoRequest(String uri) {
        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException(
                        "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
            } else {

                JsonObject res = networkConnector.DoRequest(uri, null, accessToken, null, HttpMethod.GET);
                return res;
            }
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (OAuthException | OAuthResponseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     * Calls the Spotify Web Api with the given method and given url as parameters of the call to Spotify.
     *
     * @param method Http method to perform
     * @param url url path to call to Spotify
     * @param requestData data to pass along with the call as content
     * @param clazz data type of return data, if null no data is expected to be returned.
     * @return the response give by Spotify
     */
    /*
     * private <T> @Nullable T request(HttpMethod method, String url, String requestData, Class<T> clazz) {
     * logger.debug("Request: ({}) {} - {}", method, url, requestData);
     * final Function<HttpClient, Request> call = httpClient -> httpClient.newRequest(url).method(method)
     * .header("Accept", CONTENT_TYPE).content(new StringContentProvider(requestData), CONTENT_TYPE);
     * try {
     * final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
     * final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();
     *
     * if (accessToken == null || accessToken.isEmpty()) {
     * throw new SpotifyAuthorizationException(
     * "No Spotify accesstoken. Did you authorize Spotify via /connectspotify ?");
     * } else {
     * final String response = requestWithRetry(call, accessToken).getContentAsString();
     *
     * return clazz == String.class ? (@Nullable T) response : fromJson(response, clazz);
     * }
     * } catch (final IOException e) {
     * throw new SpotifyException(e.getMessage(), e);
     * } catch (OAuthException | OAuthResponseException e) {
     * throw new SpotifyAuthorizationException(e.getMessage(), e);
     * }
     * }
     */

}