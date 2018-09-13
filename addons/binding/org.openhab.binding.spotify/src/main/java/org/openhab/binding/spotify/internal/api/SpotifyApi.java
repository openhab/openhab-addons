/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.api;

import static org.eclipse.jetty.http.HttpMethod.*;
import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.spotify.internal.api.exception.SpotifyAuthorizationException;
import org.openhab.binding.spotify.internal.api.exception.SpotifyTokenExpiredException;
import org.openhab.binding.spotify.internal.api.model.AuthorizationCodeCredentials;
import org.openhab.binding.spotify.internal.api.model.CurrentlyPlayingContext;
import org.openhab.binding.spotify.internal.api.model.Device;
import org.openhab.binding.spotify.internal.api.model.Devices;
import org.openhab.binding.spotify.internal.api.model.Me;
import org.openhab.binding.spotify.internal.api.model.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle Spotify Web Api calls.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored calling Web Api and simplified code
 */
@NonNullByDefault
public class SpotifyApi {

    private static final String CONTENT_TYPE = "application/json";
    private static final String BEARER = "Bearer ";
    private static final char AMP = '&';
    private static final char QSM = '?';
    private static final CurrentlyPlayingContext EMPTY_CURRENTLYPLAYINGCONTEXT = new CurrentlyPlayingContext();

    private final Logger logger = LoggerFactory.getLogger(SpotifyApi.class);

    private final SpotifyConnector connector;
    private final SpotifyAccessTokenCache accessTokenCache;

    /**
     * Constructor.
     *
     * @param connector The Spotify connector handling the Web Api calls to Spotify
     * @param authorizer The authorizer used to refresh the access token when expired
     * @param accessTokenChangeHandler handler to be called when the access token has changed.
     */
    public SpotifyApi(SpotifyConnector connector, SpotifyAuthorizer authorizer,
            SpotifyAccessTokenChangeHandler accessTokenChangeHandler) {
        this.connector = connector;
        accessTokenCache = new SpotifyAccessTokenCache(authorizer, accessTokenChangeHandler);
    }

    /**
     * Sets the AuthorizationCodeCredentials.
     *
     * @param credentials AuthorizationCodeCredentials to set
     */
    public void setAuthorizationCodeCredentials(AuthorizationCodeCredentials credentials) {
        accessTokenCache.setAuthorizationCodeCredentials(credentials);
    }

    /**
     * @return Returns the Spotify user information
     */
    public Me getMe() {
        ContentResponse response = request(GET, SPOTIFY_API_URL, "");

        return ModelUtil.gsonInstance().fromJson(response.getContentAsString(), Me.class);
    }

    /**
     * Call Spotify Api to play the given track on the given device. If the device id is empty it will be played on
     * the active device.
     *
     * @param deviceId device to play on or empty if play on the active device
     * @param trackId id of the track to play
     */
    public void playTrack(String deviceId, String trackId) {
        String url = "play" + optionalDeviceId(deviceId, QSM);
        String jsonRequest = "{\"context_uri\":\"%s\",\"offset\":{\"position\":0}}";
        requestPlayer(PUT, url, String.format(jsonRequest, trackId));
    }

    /**
     * Call Spotify Api to start playing. If the device id is empty it will start play of the active device.
     *
     * @param deviceId device to play on or empty if play on the active device
     */
    public void play(String deviceId) {
        requestPlayer(PUT, "play" + optionalDeviceId(deviceId, QSM));
    }

    /**
     * Call Spotify Api to pause playing. If the device id is empty it will pause play of the active device.
     *
     * @param deviceId device to pause on or empty if pause on the active device
     */
    public void pause(String deviceId) {
        requestPlayer(PUT, "pause" + optionalDeviceId(deviceId, QSM));
    }

    /**
     * Call Spotify Api to play the next song. If the device id is empty it will play the next song on the active
     * device.
     *
     * @param deviceId device to play next track on or empty if play next track on the active device
     */
    public void next(String deviceId) {
        requestPlayer(POST, "next" + optionalDeviceId(deviceId, QSM));
    }

    /**
     * Call Spotify Api to play the previous song. If the device id is empty it will play the previous song on the
     * active device.
     *
     * @param deviceId device to play previous track on or empty if play previous track on the active device
     */
    public void previous(String deviceId) {
        requestPlayer(POST, "previous" + optionalDeviceId(deviceId, QSM));
    }

    /**
     * Call Spotify Api to play set the volume. If the device id is empty it will set the volume on the active device.
     *
     * @param deviceId device to set the Volume on or empty if set volume on the active device
     * @param volumePercent volume percentage value to set
     */
    public void setVolume(String deviceId, int volumePercent) {
        requestPlayer(PUT, String.format("volume?volume_percent=%1d", volumePercent) + optionalDeviceId(deviceId, AMP));
    }

    /**
     * Call Spotify Api to play set the repeat state. If the device id is empty it will set the repeat state on the
     * active device.
     *
     * @param deviceId device to set repeat state on or empty if set repeat on the active device
     * @param repeateState set the spotify repeat state
     */
    public void setRepeatState(String deviceId, String repeateState) {
        requestPlayer(PUT, String.format("repeat?state=%s", repeateState) + optionalDeviceId(deviceId, AMP));
    }

    /**
     * Call Spotify Api to play set the shuffle. If the device id is empty it will set shuffle state on the active
     * device.
     *
     * @param deviceId device to set shuffle state on or empty if set shuffle on the active device
     * @param state the shuffle state to set
     */
    public void setShuffleState(String deviceId, OnOffType state) {
        requestPlayer(PUT, String.format("shuffle?state=%s", state == OnOffType.OFF ? "false" : "true")
                + optionalDeviceId(deviceId, AMP));
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
     * @return Calls Spotify Api and returns the list of device or an empty list if nothing was returned
     */
    public List<Device> listDevices() {
        ContentResponse response = requestPlayer(GET, "devices");
        Devices deviceList = ModelUtil.gsonInstance().fromJson(response.getContentAsString(), Devices.class);

        return deviceList == null || deviceList.getDevices() == null ? Collections.emptyList()
                : deviceList.getDevices();
    }

    /**
     * @return Calls Spotify Api and returns the current playing context of the user or an empty object if no context as
     *         returned by Spotify
     */
    public CurrentlyPlayingContext getPlayerInfo() {
        ContentResponse response = requestPlayer(GET, "");
        CurrentlyPlayingContext context = ModelUtil.gsonInstance().fromJson(response.getContentAsString(),
                CurrentlyPlayingContext.class);

        return context == null ? EMPTY_CURRENTLYPLAYINGCONTEXT : context;
    }

    /**
     * Calls the Spotify player Web Api with the given method and appends the given url as parameters of the call to
     * Spotify.
     *
     * @param method Http method to perform
     * @param url url path to call to spotify
     * @return the response give by Spotify
     */
    private ContentResponse requestPlayer(HttpMethod method, String url) {
        return requestPlayer(method, url, "");
    }

    /**
     * Calls the Spotify player Web Api with the given method and appends the given url as parameters of the call to
     * Spotify.
     *
     * @param method Http method to perform
     * @param url url path to call to spotify
     * @param requestData data to pass along with the call as content
     * @return the response give by Spotify
     */
    private ContentResponse requestPlayer(HttpMethod method, String url, String requestData) {
        return request(method, SPOTIFY_API_PLAYER_URL + (url.isEmpty() ? "" : ('/' + url)), requestData);
    }

    /**
     * Calls the Spotify Web Api with the given method and given url as parameters of the call to Spotify.
     *
     * @param method Http method to perform
     * @param url url path to call to spotify
     * @param requestData data to pass along with the call as content
     * @return the response give by Spotify
     */
    private ContentResponse request(HttpMethod method, String url, String requestData) {
        logger.debug("Request: ({}) {} - {}", method, url, requestData);
        Function<HttpClient, Request> call = httpClient -> httpClient.newRequest(url).method(method)
                .header("Accept", CONTENT_TYPE).content(new StringContentProvider(requestData), CONTENT_TYPE);

        try {
            String accessToken = accessTokenCache.getAccessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new SpotifyAuthorizationException("No spotify accesstoken. Is this thing authorized?");
            } else {
                return connector.request(call, BEARER + accessToken);
            }
        } catch (SpotifyTokenExpiredException e) {
            // Retry with new access token
            accessTokenCache.invalidateValue();
            return connector.request(call, BEARER + accessTokenCache.getAccessToken());
        }
    }
}
