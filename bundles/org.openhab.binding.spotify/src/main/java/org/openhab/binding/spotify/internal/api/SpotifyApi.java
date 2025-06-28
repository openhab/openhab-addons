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
package org.openhab.binding.spotify.internal.api;

import static org.eclipse.jetty.http.HttpMethod.*;
import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.spotify.internal.api.exception.SpotifyAuthorizationException;
import org.openhab.binding.spotify.internal.api.exception.SpotifyException;
import org.openhab.binding.spotify.internal.api.exception.SpotifyTokenExpiredException;
import org.openhab.binding.spotify.internal.api.model.AddedShow;
import org.openhab.binding.spotify.internal.api.model.AddedShows;
import org.openhab.binding.spotify.internal.api.model.Album;
import org.openhab.binding.spotify.internal.api.model.Albums;
import org.openhab.binding.spotify.internal.api.model.ApiSearchResult;
import org.openhab.binding.spotify.internal.api.model.Artist;
import org.openhab.binding.spotify.internal.api.model.Artists;
import org.openhab.binding.spotify.internal.api.model.Categorie;
import org.openhab.binding.spotify.internal.api.model.CategoriesResult;
import org.openhab.binding.spotify.internal.api.model.CurrentlyPlayingContext;
import org.openhab.binding.spotify.internal.api.model.Device;
import org.openhab.binding.spotify.internal.api.model.Devices;
import org.openhab.binding.spotify.internal.api.model.FollowedArtists;
import org.openhab.binding.spotify.internal.api.model.Me;
import org.openhab.binding.spotify.internal.api.model.ModelUtil;
import org.openhab.binding.spotify.internal.api.model.NewReleases;
import org.openhab.binding.spotify.internal.api.model.Playlist;
import org.openhab.binding.spotify.internal.api.model.Playlists;
import org.openhab.binding.spotify.internal.api.model.SavedAlbum;
import org.openhab.binding.spotify.internal.api.model.SavedAlbums;
import org.openhab.binding.spotify.internal.api.model.Track;
import org.openhab.binding.spotify.internal.api.model.Tracks;
import org.openhab.binding.spotify.internal.api.model.UserTrackEntries;
import org.openhab.binding.spotify.internal.api.model.UserTrackEntry;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

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
    private static final String PLAY_TRACK_URIS = "{\"uris\":[%s],\"offset\":{\"position\":%d},\"position_ms\":%d}";
    private static final String PLAY_TRACK_CONTEXT_URI = "{\"context_uri\":\"%s\",\"offset\":{\"position\":%d},\"position_ms\":%d}}";
    private static final String ENQUEUE_URI = "uri=%s";
    private static final String TRANSFER_PLAY = "{\"device_ids\":[\"%s\"],\"play\":%b}";

    private final Logger logger = LoggerFactory.getLogger(SpotifyApi.class);

    private final OAuthClientService oAuthClientService;
    private final SpotifyConnector connector;

    /**
     * Constructor.
     *
     * @param oAuthClientService The authorizer used to refresh the access token when expired
     * @param scheduler
     * @param httpClient The Spotify connector handling the Web Api calls to Spotify
     */
    public SpotifyApi(OAuthClientService oAuthClientService, ScheduledExecutorService scheduler,
            HttpClient httpClient) {
        this.oAuthClientService = oAuthClientService;
        connector = new SpotifyConnector(scheduler, httpClient);
    }

    /**
     * @return Returns the Spotify user information
     */
    public Me getMe() {
        return Objects.requireNonNull(request(GET, SPOTIFY_API_URL, "", Me.class));
    }

    /**
     * Call Spotify Api to play the given track on the given device. If the device id is empty it will be played on
     * the active device.
     *
     * @param deviceId device to play on or empty if play on the active device
     * @param trackId id of the track to play
     * @param offset offset
     * @param positionMs position in ms
     */
    public void playTrack(String deviceId, String trackId, int offset, int positionMs) {
        final String url = "play" + optionalDeviceId(deviceId, QSM);
        final String play;
        if (trackId.contains(":track:")) {
            play = String.format(PLAY_TRACK_URIS,
                    Arrays.asList(trackId.split(",")).stream().map(t -> '"' + t + '"').collect(Collectors.joining(",")),
                    offset, positionMs);
        } else {
            play = String.format(PLAY_TRACK_CONTEXT_URI, trackId, offset, positionMs);
        }
        requestPlayer(PUT, url, play, String.class);
    }

    /**
     * Call Spotify Api to play the given track on the given device. If the device id is empty it will be played on
     * the active device.
     *
     * @param deviceId device to play on or empty if play on the active device
     * @param trackId id of the track to play
     * @param offset offset
     * @param positionMs position in ms
     */
    public void queueTrack(String deviceId, String trackId, int offset, int positionMs) {
        String url = "queue";
        url = url + "?uri=" + trackId;
        url = url + optionalDeviceId(deviceId, QSM);
        requestPlayer(POST, url, "", String.class);
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
     * Call Spotify Api to transfer playing to. Depending on play value is start play or pause.
     *
     * @param deviceId device to play on. It can not be empty.
     * @param play if true transfers and starts to play, else transfers but pauses.
     */
    public void transferPlay(String deviceId, boolean play) {
        requestPlayer(PUT, "", String.format(TRANSFER_PLAY, deviceId, play), String.class);
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
     * @param repeateState set the Spotify repeat state
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
    public List<Device> getDevices() {
        final Devices deviceList = requestPlayer(GET, "devices", "", Devices.class);

        return deviceList == null || deviceList.getDevices() == null ? Collections.emptyList()
                : deviceList.getDevices();
    }

    /**
     * @return Returns the playlists of the user.
     */
    public List<Playlist> getPlaylists(long offset, long limit) {
        final Playlists playlists = request(GET, SPOTIFY_API_URL + "/playlists?offset=" + offset + "&limit=" + limit,
                "", Playlists.class);

        return playlists == null || playlists.getItems() == null ? Collections.emptyList() : playlists.getItems();
    }

    /**
     * @return Returns the albums of the user.
     */
    public List<SavedAlbum> getSavedAlbums(long offset, long limit) {
        final SavedAlbums albums = request(GET, SPOTIFY_API_URL + "/albums?offset" + offset + "&limit=" + limit, "",
                SavedAlbums.class);

        return albums == null || albums.getItems() == null ? Collections.emptyList() : albums.getItems();
    }

    /**
     * @return Returns the albums of the user.
     */
    public List<Album> getNewReleases(long offset, long limit) {
        final NewReleases newReleases = request(GET,
                SPOTIFY_API_BASE_URL + "/browse/new-releases?offset" + offset + "&limit=" + limit, "",
                NewReleases.class);

        return newReleases == null || newReleases.albums.getItems() == null ? Collections.emptyList()
                : newReleases.albums.getItems();
    }

    /**
     * @return Returns an album
     */
    public @Nullable Album getAlbum(String albumId) {
        final Album album = request(GET, SPOTIFY_API_BASE_URL + "/albums/" + albumId, "", Album.class);

        return album;
    }

    /**
     * @return Returns the artists of the user.
     */
    public List<Artist> getArtists(long offset, long limit) {
        final FollowedArtists followedArtists = request(GET,
                SPOTIFY_API_URL + "/following?type=artist&offset" + offset + "&limit=" + limit, "",
                FollowedArtists.class);

        return followedArtists == null || followedArtists.getArtists() == null ? Collections.emptyList()
                : followedArtists.getArtists().getItems();
    }

    /**
     * @return Returns the artists of the user.
     */
    public List<Categorie> getCategories(long offset, long limit) {
        final CategoriesResult categoriesRes = request(GET,
                SPOTIFY_API_BASE_URL + "/browse/categories?offset" + offset + "&limit=" + limit, "",
                CategoriesResult.class);

        return categoriesRes == null || categoriesRes.categories.getItems() == null ? Collections.emptyList()
                : categoriesRes.categories.getItems();
    }

    /**
     * @return Returns the artists of the user.
     */
    public List<Album> getArtistAlbums(String artistId) {
        final Albums albums = request(GET, SPOTIFY_API_BASE_URL + "/artists/" + artistId + "/albums", "", Albums.class);

        return albums == null || albums.getItems() == null ? Collections.emptyList() : albums.getItems();
    }

    /**
     * @return Returns the artists of the user.
     */
    public List<Artist> getTopArtists(long offset, long limit) {
        final Artists topArtists = request(GET, SPOTIFY_API_URL + "/top/artists?offset" + offset + "&limit=" + limit,
                "", Artists.class);

        return topArtists == null || topArtists.getItems() == null ? Collections.emptyList() : topArtists.getItems();
    }

    /**
     * @return Returns the artists of the user.
     */
    public List<AddedShow> getShows(long offset, long limit) {
        final AddedShows shows = request(GET, SPOTIFY_API_URL + "/shows?offset" + offset + "&limit=" + limit, "",
                AddedShows.class);

        return shows == null || shows.getItems() == null ? Collections.emptyList() : shows.getItems();
    }

    /**
     * @return Returns the artists of the user.
     */
    public List<Track> getTopTracks(long offset, long limit) {
        final Tracks topTracks = request(GET, SPOTIFY_API_URL + "/top/tracks?offset" + offset + "&limit=" + limit, "",
                Tracks.class);

        return topTracks == null || topTracks.getItems() == null ? Collections.emptyList() : topTracks.getItems();
    }

    /**
     * @return Returns the artists of the user.
     */
    public List<UserTrackEntry> getTracks(long offset, long limit) {
        final UserTrackEntries tracks = request(GET, SPOTIFY_API_URL + "/tracks?offset" + offset + "&limit=" + limit,
                "", UserTrackEntries.class);

        return tracks == null || tracks.getItems() == null ? Collections.emptyList() : tracks.getItems();
    }

    /**
     * @return Returns the artists of the user.
     */
    public List<UserTrackEntry> getRecentlyPlayedTracks(long offset, long limit) {
        final UserTrackEntries tracks = request(GET,
                SPOTIFY_API_URL + "/player/recently-played?offset" + offset + "&limit=" + limit, "",
                UserTrackEntries.class);

        return tracks == null || tracks.getItems() == null ? Collections.emptyList() : tracks.getItems();
    }

    /**
     * @return Returns a playlist details
     */
    public @Nullable Playlist getPlaylist(String uri) {
        final Playlist playlist = request(GET,
                SPOTIFY_API_BASE_URL + "/playlists/" + uri.replace("spotify:playlist:", ""), "", Playlist.class);
        return playlist;
    }

    /**
     * @return Returns the artists of the user.
     */
    public @Nullable ApiSearchResult search(long offset, long limit) {
        final ApiSearchResult searchResult = request(GET,
                SPOTIFY_API_BASE_URL + "/search?q=cure"
                        + "&type=show%2Cepisode%2Caudiobook%2Calbum%2Cartist%2Cplaylist%2Ctrack" + "&offset=" + offset
                        + "&limit=" + limit,
                "", ApiSearchResult.class);

        return searchResult;
    }

    /**
     * @return Calls Spotify Api and returns the current playing context of the user or an empty object if no context as
     *         returned by Spotify
     */
    public CurrentlyPlayingContext getPlayerInfo() {
        final CurrentlyPlayingContext context = requestPlayer(GET, "", "", CurrentlyPlayingContext.class);

        return context == null ? EMPTY_CURRENTLYPLAYINGCONTEXT : context;
    }

    /**
     * Calls the Spotify player Web Api with the given method and appends the given url as parameters of the call to
     * Spotify.
     *
     * @param method Http method to perform
     * @param url url path to call to Spotify
     */
    private void requestPlayer(HttpMethod method, String url) {
        requestPlayer(method, url, "", String.class);
    }

    /**
     * Calls the Spotify player Web Api with the given method and appends the given url as parameters of the call to
     * Spotify.
     *
     * @param method Http method to perform
     * @param url url path to call to Spotify
     * @param requestData data to pass along with the call as content
     * @param clazz data type of return data, if null no data is expected to be returned.
     * @return the response give by Spotify
     */
    private <T> @Nullable T requestPlayer(HttpMethod method, String url, String requestData, Class<T> clazz) {
        return request(method, SPOTIFY_API_PLAYER_URL + (url.isEmpty() ? "" : ('/' + url)), requestData, clazz);
    }

    /**
     * Parses the Spotify returned json.
     *
     * @param <T> z data type to return
     * @param content json content to parse
     * @param clazz data type to return
     * @throws SpotifyException throws a {@link SpotifyException} in case the json could not be parsed.
     * @return parsed json.
     */
    private static <T> @Nullable T fromJson(String content, Class<T> clazz) {
        try {
            return (T) ModelUtil.gsonInstance().fromJson(content, clazz);
        } catch (final JsonSyntaxException e) {
            throw new SpotifyException("Unknown Spotify response:" + content, e);
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
    private <T> @Nullable T request(HttpMethod method, String url, String requestData, Class<T> clazz) {
        logger.debug("Request: ({}) {} - {}", method, url, requestData);
        final Function<HttpClient, Request> call = httpClient -> httpClient.newRequest(url).method(method)
                .header("Accept", CONTENT_TYPE).content(new StringContentProvider(requestData), CONTENT_TYPE);
        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new SpotifyAuthorizationException(
                        "No Spotify accesstoken. Did you authorize Spotify via /connectspotify ?");
            } else {
                final String response = requestWithRetry(call, accessToken).getContentAsString();

                return clazz == String.class ? (@Nullable T) response : fromJson(response, clazz);
            }
        } catch (final IOException e) {
            throw new SpotifyException(e.getMessage(), e);
        } catch (OAuthException | OAuthResponseException e) {
            throw new SpotifyAuthorizationException(e.getMessage(), e);
        }
    }

    private ContentResponse requestWithRetry(final Function<HttpClient, Request> call, final String accessToken)
            throws OAuthException, IOException, OAuthResponseException {
        try {
            return connector.request(call, BEARER + accessToken);
        } catch (final SpotifyTokenExpiredException e) {
            // Retry with new access token
            return connector.request(call, BEARER + oAuthClientService.refreshToken().getAccessToken());
        }
    }
}
