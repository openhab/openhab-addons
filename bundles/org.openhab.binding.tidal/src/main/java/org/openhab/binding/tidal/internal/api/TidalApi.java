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
package org.openhab.binding.tidal.internal.api;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.openhab.binding.tidal.internal.TidalBindingConstants.TIDAL_API_URL;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tidal.internal.api.exception.TidalAuthorizationException;
import org.openhab.binding.tidal.internal.api.exception.TidalException;
import org.openhab.binding.tidal.internal.api.exception.TidalTokenExpiredException;
import org.openhab.binding.tidal.internal.api.model.Album;
import org.openhab.binding.tidal.internal.api.model.Albums;
import org.openhab.binding.tidal.internal.api.model.Artist;
import org.openhab.binding.tidal.internal.api.model.Artists;
import org.openhab.binding.tidal.internal.api.model.ModelUtil;
import org.openhab.binding.tidal.internal.api.model.Playlist;
import org.openhab.binding.tidal.internal.api.model.Playlists;
import org.openhab.binding.tidal.internal.api.model.Track;
import org.openhab.binding.tidal.internal.api.model.Tracks;
import org.openhab.binding.tidal.internal.api.model.User;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Class to handle Tidal Web Api calls.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TidalApi {

    private static final String CONTENT_TYPE = "application/json";
    private static final String BEARER = "Bearer ";
    private static final char AMP = '&';
    private static final char QSM = '?';
    private static final String PLAY_TRACK_URIS = "{\"uris\":[%s],\"offset\":{\"position\":%d},\"position_ms\":%d}";
    private static final String PLAY_TRACK_CONTEXT_URI = "{\"context_uri\":\"%s\",\"offset\":{\"position\":%d},\"position_ms\":%d}}";
    private static final String TRANSFER_PLAY = "{\"device_ids\":[\"%s\"],\"play\":%b}";

    private final Logger logger = LoggerFactory.getLogger(TidalApi.class);

    private final OAuthClientService oAuthClientService;
    private final TidalConnector connector;

    /**
     * Constructor.
     *
     * @param oAuthClientService The authorizer used to refresh the access token when expired
     * @param scheduler
     * @param httpClient The Tidal connector handling the Web Api calls to Tidal
     */
    public TidalApi(OAuthClientService oAuthClientService, ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.oAuthClientService = oAuthClientService;
        connector = new TidalConnector(scheduler, httpClient);
    }

    /**
     * Method to return an optional device id url pattern. If device id is empty an empty string is returned else the
     * device id url query pattern prefixed with the given prefix char
     *
     * @param deviceId device to play on or empty if play on the active device
     * @param prefix char to prefix to the deviceId string if present
     * @return empty string or query string part for device id
     */
    private String optionalDeviceId(String deviceId, char prefix) {
        return deviceId.isEmpty() ? "" : String.format("%cdevice_id=%s", prefix, deviceId);
    }

    /**
     * @return Returns the Spotify user information
     */
    public User getMe() {
        return Objects.requireNonNull(request(GET, TIDAL_API_URL + "/v2/users/me", "", "data", User.class));
    }

    /**
     * @return Returns the albums of the user.
     */
    public List<Album> getAlbums(long offset, long limit) {
        final Albums albums = request(GET, TIDAL_API_URL
                + "/v2/userCollections/192468940/relationships/albums?countryCode=FR&locale=fr-FR&include=albums", "",
                "included", Albums.class);

        return albums == null ? Collections.emptyList() : albums;
    }

    /**
     * @return Returns the albums of the user.
     */
    public List<Artist> getArtists(long offset, long limit) {
        final Artists artists = request(GET, TIDAL_API_URL
                + "/v2/userCollections/192468940/relationships/artists?countryCode=FR&locale=fr-FR&include=artists", "",
                "included", Artists.class);

        return artists == null ? Collections.emptyList() : artists;
    }

    /**
     * @return Returns the albums of the user.
     */
    public List<Track> getTracks(long offset, long limit) {
        final Tracks tracks = request(GET, TIDAL_API_URL
                + "/v2/userCollections/192468940/relationships/tracks?countryCode=FR&locale=fr-FR&include=tracks", "",
                "included", Tracks.class);

        return tracks == null ? Collections.emptyList() : tracks;
    }

    /**
     * @return Returns the playlists of the user.
     */
    public List<Playlist> getPlaylists(long offset, long limit) {
        final Playlists playlists = request(GET,
                TIDAL_API_URL + "/v2/playlists?countryCode=FR&include=coverArt&filter%5Bowners.id%5D=192468940", "",
                "data", Playlists.class);

        return playlists == null ? Collections.emptyList() : playlists;
    }

    /**
     * Parses the Tidal returned json.
     *
     * @param <T> z data type to return
     * @param content json content to parse
     * @param clazz data type to return
     * @throws TidalException throws a {@link TidalException} in case the json could not be parsed.
     * @return parsed json.
     */
    private static <T> @Nullable T fromJson(String content, Class<T> clazz) {
        try {
            return (T) ModelUtil.gsonInstance().fromJson(content, clazz);
        } catch (final JsonSyntaxException e) {
            throw new TidalException("Unknown Tidal response:" + content, e);
        }
    }

    /**
     * Calls the Tidal Web Api with the given method and given url as parameters of the call to Tidal.
     *
     * @param method Http method to perform
     * @param url url path to call to Tidal
     * @param requestData data to pass along with the call as content
     * @param clazz data type of return data, if null no data is expected to be returned.
     * @return the response give by Tidal
     */
    private <T> @Nullable T request(HttpMethod method, String url, String requestData, String attr, Class<T> clazz) {
        logger.debug("Request: ({}) {} - {}", method, url, requestData);

        final Function<HttpClient, Request> call = httpClient -> httpClient.newRequest(url).method(method);

        // .header("Accept", CONTENT_TYPE).content(new StringContentProvider(requestData), CONTENT_TYPE);

        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();
            // String accessToken =
            // "eyJraWQiOiJ2OU1GbFhqWSIsImFsZyI6IkVTMjU2In0.eyJ0eXBlIjoibzJfYWNjZXNzIiwidWlkIjoxOTI0Njg5NDAsInNjb3BlIjoicGxheWxpc3RzLndyaXRlIGNvbGxlY3Rpb24ucmVhZCBzZWFyY2gud3JpdGUgZW50aXRsZW1lbnRzLnJlYWQgcGxheWxpc3RzLnJlYWQgdXNlci5yZWFkIHNlYXJjaC5yZWFkIHJlY29tbWVuZGF0aW9ucy5yZWFkIHBsYXliYWNrIGNvbGxlY3Rpb24ud3JpdGUiLCJnVmVyIjowLCJzVmVyIjowLCJjaWQiOjE0NzM1LCJleHAiOjE3NjAxMTcyNzYsInNpZCI6ImY4NzQyODIzLWE1ZGMtNDg4Yi05Zjg0LWE2YTgxYzI1MjI3ZSIsImlzcyI6Imh0dHBzOi8vYXV0aC50aWRhbC5jb20vdjEifQ.BY1xj_10NEfyE4zAOz_8CJk5O5cTBLYTmJ79boYO3TUVntoH6uVoxdXtdslxDMP9uGL54xBla-BTA91C3JLx4A";

            if (accessToken == null || accessToken.isEmpty()) {
                throw new TidalAuthorizationException(
                        "No Tidal accesstoken. Did you authorize Tidal via /connecttidal ?");
            } else {
                final String response = requestWithRetry(call, accessToken).getContentAsString();

                Gson gson = ModelUtil.gsonInstance();

                JsonElement element = gson.fromJson(response, JsonElement.class);
                JsonObject jsonObj = element.getAsJsonObject();

                JsonElement data = jsonObj.get("data");
                JsonElement included = jsonObj.get("included");

                JsonElement decode = jsonObj.get(attr);

                T result = gson.fromJson(decode, clazz);

                // return clazz == String.class ? (@Nullable T) response : fromJson(response, clazz);
                return result;
            }
        } catch (final IOException e) {
            throw new TidalException(e.getMessage(), e);
        } catch (OAuthException | OAuthResponseException e) {
            throw new TidalAuthorizationException(e.getMessage(), e);
        }
    }

    private ContentResponse requestWithRetry(final Function<HttpClient, Request> call, final String accessToken)
            throws OAuthException, IOException, OAuthResponseException {
        try {
            return connector.request(call, BEARER + accessToken);
        } catch (final TidalTokenExpiredException e) {
            // Retry with new access token
            return connector.request(call, BEARER + oAuthClientService.refreshToken().getAccessToken());
        }
    }
}
