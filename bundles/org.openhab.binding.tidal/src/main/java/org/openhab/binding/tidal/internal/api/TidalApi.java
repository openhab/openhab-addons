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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Hashtable;
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
import org.openhab.binding.tidal.internal.TidalBindingConstants;
import org.openhab.binding.tidal.internal.api.exception.TidalAuthorizationException;
import org.openhab.binding.tidal.internal.api.exception.TidalException;
import org.openhab.binding.tidal.internal.api.exception.TidalTokenExpiredException;
import org.openhab.binding.tidal.internal.api.model.Album;
import org.openhab.binding.tidal.internal.api.model.Albums;
import org.openhab.binding.tidal.internal.api.model.Artist;
import org.openhab.binding.tidal.internal.api.model.Artists;
import org.openhab.binding.tidal.internal.api.model.BaseEntries;
import org.openhab.binding.tidal.internal.api.model.BaseEntry;
import org.openhab.binding.tidal.internal.api.model.ModelUtil;
import org.openhab.binding.tidal.internal.api.model.Playlist;
import org.openhab.binding.tidal.internal.api.model.Playlists;
import org.openhab.binding.tidal.internal.api.model.RelationShip;
import org.openhab.binding.tidal.internal.api.model.Session;
import org.openhab.binding.tidal.internal.api.model.Stream;
import org.openhab.binding.tidal.internal.api.model.Track;
import org.openhab.binding.tidal.internal.api.model.Tracks;
import org.openhab.binding.tidal.internal.api.model.User;
import org.openhab.binding.tidal.internal.handler.TidalBridgeHandler;
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
    private final TidalBridgeHandler bridgeHandler;
    private final TidalConnector connector;

    /**
     * Constructor.
     *
     * @param oAuthClientService The authorizer used to refresh the access token when expired
     * @param scheduler
     * @param httpClient The Tidal connector handling the Web Api calls to Tidal
     */
    public TidalApi(OAuthClientService oAuthClientService, ScheduledExecutorService scheduler, HttpClient httpClient,
            TidalBridgeHandler bridgeHandler) {
        this.oAuthClientService = oAuthClientService;
        this.bridgeHandler = bridgeHandler;
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
        return Objects.requireNonNull(request(GET, TIDAL_API_URL + "/v2/users/me", "", User.class));
    }

    /**
     * @return a specific album, with its tracks
     */
    public @Nullable Album getAlbum(String albumId) {
        String userId = bridgeHandler.getUserId();
        String userCountry = bridgeHandler.getUserCountry();
        String uri = String.format("%s/v2/albums/%s?countryCode=%s&locale=fr-FR&include=coverArt&include=items",
                TIDAL_API_URL, albumId, userCountry);
        final Album album = request(GET, uri, "", Album.class);
        return album;
    }

    /**
     * @return a specific playlist, with its tracks
     */
    public @Nullable Playlist getPlaylist(String playlistId) {
        String userId = bridgeHandler.getUserId();
        String userCountry = bridgeHandler.getUserCountry();
        String uri = String.format("%s/v2/playlists/%s?countryCode=%s&locale=fr-FR&include=coverArt&include=items",
                TIDAL_API_URL, playlistId, userCountry);
        final Playlist playlist = request(GET, uri, "", Playlist.class);
        return playlist;
    }

    /**
     * @return a specific artist, with its albums
     */
    public @Nullable Artist getArtist(String albumId) {
        String userId = bridgeHandler.getUserId();
        String userCountry = bridgeHandler.getUserCountry();
        String uri = String.format(
                "%s/v2/artists/%s?countryCode=%s&locale=fr-FR&include=profileArt&include=albums&include=albums.coverArt",
                TIDAL_API_URL, albumId, userCountry);
        final Artist artist = request(GET, uri, "", Artist.class);
        return artist;
    }

    /**
     * @return Returns the albums of the user.
     */
    public List<Album> getAlbums(long offset, long limit) {
        String userId = bridgeHandler.getUserId();
        String userCountry = bridgeHandler.getUserCountry();
        String uri = String.format(
                "%s/v2/userCollections/%s/relationships/albums?countryCode=%s&locale=fr-FR&include=albums&include=albums.coverArt&include=albums.artists",
                TIDAL_API_URL, userId, userCountry);
        final Albums albums = request(GET, uri, "", Albums.class);

        return albums == null ? Collections.emptyList() : albums;
    }

    /**
     * @return Returns the albums of the user.
     */
    public List<Artist> getArtists(long offset, long limit) {
        String userId = bridgeHandler.getUserId();
        String userCountry = bridgeHandler.getUserCountry();
        String uri = String.format(
                "%s/v2/userCollections/%s/relationships/artists?countryCode=%s&locale=fr-FR&include=artists&include=artists.profileArt",
                TIDAL_API_URL, userId, userCountry);
        final Artists artists = request(GET, uri, "", Artists.class);

        return artists == null ? Collections.emptyList() : artists;
    }

    /**
     * @return Returns the albums of the user.
     */
    public List<Track> getTracks(long offset, long limit) {
        String userId = bridgeHandler.getUserId();
        String userCountry = bridgeHandler.getUserCountry();
        String uri = String.format(
                "%s/v2/userCollections/%s/relationships/tracks?countryCode=%s&locale=fr-FR&include=tracks&include=tracks.coverArt",
                TIDAL_API_URL, userId, userCountry);
        final Tracks tracks = request(GET, uri, "", Tracks.class);

        return tracks == null ? Collections.emptyList() : tracks;
    }

    /**
     * @return Returns the playlists of the user.
     */
    public List<Playlist> getPlaylists(long offset, long limit) {
        String userId = bridgeHandler.getUserId();
        String userCountry = bridgeHandler.getUserCountry();
        String uri = String.format("%s/v2/playlists?countryCode=%s&include=coverArt&filter[owners.id]=%s",
                TIDAL_API_URL, userCountry, userId);
        final Playlists playlists = request(GET, uri, "", Playlists.class);

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

    public @Nullable Session getSession() {
        Session session = request(GET, TidalBindingConstants.TIDAL_V1_API_URL + "/sessions", "", Session.class);
        return session;
    }

    public InputStream getTrackStream(String trackId) {
        String sessionId = getSession().getSessionId();
        // sessionId = "b022c7a2-3016-4a64-a657-8afa88f5102c";
        User me = getMe();
        // String uri = TidalBindingConstants.TIDAL_V1_API_URL + "/tracks/";
        // uri = uri + trackId;
        // uri = uri + "/urlpostpaywall?sessionId=" + sessionId;
        // uri = uri + "&countryCode=" + me.getCountry();
        // uri = uri + "&limit=1000";
        // uri = uri + "&urlusagemode=STREAM";
        // uri = uri + "&audioquality=LOSSLESS";
        // uri = uri + "&assetpresentation=FULL";
        //
        // Stream stream = request(GET, uri, "", Stream.class);
        // String[] urls = stream.getUrls();

        String uri = TidalBindingConstants.TIDAL_V1_API_URL + "/tracks/";
        uri = uri + trackId;
        uri = uri + "/playbackinfopostpaywall?sessionId=" + sessionId;
        uri = uri + "&countryCode=" + me.getCountry();
        uri = uri + "&limit=1000";
        uri = uri + "&playbackmode=STREAM";
        uri = uri + "&audioquality=HI_RES_LOSSLESS";
        uri = uri + "&assetpresentation=FULL";

        Stream stream = request(GET, uri, "", Stream.class);
        String[] urls = stream.getUrls();
        String manifest = stream.getManifest();

        String manifestDecode = new String(Base64.getDecoder().decode(manifest), StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(manifestDecode.getBytes(StandardCharsets.UTF_8));

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter("C:/eclipse/openhab-main/git/openhab-distro/launch/app/runtime/conf/html/test.mpd"))) {
            writer.write(manifestDecode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputStream;
    }

    public String getTrackStreamUri(String trackId) {
        String sessionId = getSession().getSessionId();
        // sessionId = "b022c7a2-3016-4a64-a657-8afa88f5102c";
        User me = getMe();
        // String uri = TidalBindingConstants.TIDAL_V1_API_URL + "/tracks/";
        // uri = uri + trackId;
        // uri = uri + "/urlpostpaywall?sessionId=" + sessionId;
        // uri = uri + "&countryCode=" + me.getCountry();
        // uri = uri + "&limit=1000";
        // uri = uri + "&urlusagemode=STREAM";
        // uri = uri + "&audioquality=LOSSLESS";
        // uri = uri + "&assetpresentation=FULL";
        //
        // Stream stream = request(GET, uri, "", Stream.class);
        // String[] urls = stream.getUrls();

        String uri = TidalBindingConstants.TIDAL_V1_API_URL + "/tracks/";
        uri = uri + trackId;
        uri = uri + "/playbackinfopostpaywall?sessionId=" + sessionId;
        uri = uri + "&countryCode=" + me.getCountry();
        uri = uri + "&limit=1000";
        uri = uri + "&playbackmode=STREAM";
        uri = uri + "&audioquality=HI_RES_LOSSLESS";
        uri = uri + "&assetpresentation=FULL";

        Stream stream = request(GET, uri, "", Stream.class);
        String[] urls = stream.getUrls();
        String manifest = stream.getManifest();

        String manifestDecode = new String(Base64.getDecoder().decode(manifest), StandardCharsets.UTF_8);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter("C:/eclipse/openhab-main/git/openhab-distro/launch/app/runtime/conf/html/test.mpd"))) {
            writer.write(manifestDecode);
            return "http://192.168.254.101:8080/static/test.mpd";
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (urls != null && urls.length > 0) {
            return urls[0];
        }

        return "";

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
    private <T> @Nullable T request(HttpMethod method, String url, String requestData, Class<T> clazz) {
        logger.debug("Request: ({}) {} - {}", method, url, requestData);

        final Function<HttpClient, Request> call = httpClient -> httpClient.newRequest(url).method(method);

        // .header("Accept", CONTENT_TYPE).content(new StringContentProvider(requestData), CONTENT_TYPE);

        try {
            String accessToken = "";
            // if (!(clazz == Stream.class)) {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new TidalAuthorizationException(
                        "No Tidal accesstoken. Did you authorize Tidal via /connecttidal ?");
            }
            // }

            final String response = requestWithRetry(call, accessToken).getContentAsString();

            Gson gson = ModelUtil.gsonInstance();

            JsonElement element = gson.fromJson(response, JsonElement.class);
            JsonObject jsonObj = element.getAsJsonObject();

            JsonElement data = jsonObj.get("data");
            JsonElement included = jsonObj.get("included");

            if (data == null) {
                T result = gson.fromJson(response, clazz);
                return result;
            } else {
                T result = gson.fromJson(data, clazz);
                if (result instanceof BaseEntry) {
                    BaseEntry entry = (BaseEntry) result;
                    BaseEntries includedRessources = gson.fromJson(included, BaseEntries.class);

                    Hashtable<String, BaseEntry> dict = new Hashtable<String, BaseEntry>();

                    for (BaseEntry includedRessource : includedRessources) {
                        dict.put(includedRessource.getId(), includedRessource);
                        logger.info("");
                    }

                    RelationShip relationShip = entry.getRelationShip();
                    relationShip.resolveDeps(dict);
                    logger.info("");
                } else if (result instanceof ArrayList<?>) {
                    @SuppressWarnings("unchecked")
                    ArrayList<BaseEntry> resultCast = (ArrayList<BaseEntry>) result;

                    Type superType = clazz.getGenericSuperclass();
                    Type typeArg = null;
                    if (superType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) superType;
                        typeArg = pt.getActualTypeArguments()[0];
                    }

                    BaseEntries includedRessources = gson.fromJson(included, BaseEntries.class);

                    if (includedRessources != null) {
                        for (BaseEntry includedRessource : includedRessources) {
                            if (typeArg != null && includedRessource.getClass() == typeArg) {
                                String includedId = includedRessource.getId();
                                resultCast.removeIf(entry -> entry.getId().equals(includedId));
                                resultCast.add(includedRessource);
                            }
                        }

                        Hashtable<String, BaseEntry> dict = new Hashtable<String, BaseEntry>();

                        for (BaseEntry includedRessource : includedRessources) {
                            if (typeArg != null && includedRessource.getClass() != typeArg) {

                                dict.put(includedRessource.getId(), includedRessource);
                                logger.info("");
                            }
                        }

                        for (BaseEntry entry : resultCast) {
                            RelationShip relationShip = entry.getRelationShip();
                            relationShip.resolveDeps(dict);
                        }
                    }
                }
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
