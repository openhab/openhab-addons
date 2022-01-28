/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.kodi.internal.protocol;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.kodi.internal.KodiEventListener;
import org.openhab.binding.kodi.internal.KodiEventListener.KodiPlaylistState;
import org.openhab.binding.kodi.internal.KodiEventListener.KodiState;
import org.openhab.binding.kodi.internal.model.KodiAudioStream;
import org.openhab.binding.kodi.internal.model.KodiDuration;
import org.openhab.binding.kodi.internal.model.KodiFavorite;
import org.openhab.binding.kodi.internal.model.KodiPVRChannel;
import org.openhab.binding.kodi.internal.model.KodiPVRChannelGroup;
import org.openhab.binding.kodi.internal.model.KodiProfile;
import org.openhab.binding.kodi.internal.model.KodiSubtitle;
import org.openhab.binding.kodi.internal.model.KodiSystemProperties;
import org.openhab.binding.kodi.internal.model.KodiUniqueID;
import org.openhab.binding.kodi.internal.model.KodiVideoStream;
import org.openhab.core.cache.ByteArrayFileCache;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * KodiConnection provides an API for accessing a Kodi device.
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 * @author Andreas Reinhardt & Christoph Weitkamp - Added channels for thumbnail and fanart
 * @author Christoph Weitkamp - Improvements for playing audio notifications
 */
public class KodiConnection implements KodiClientSocketEventListener {

    private static final String TIMESTAMP_FRAGMENT = "#timestamp=";
    private static final String PROPERTY_FANART = "fanart";
    private static final String PROPERTY_THUMBNAIL = "thumbnail";
    private static final String PROPERTY_VERSION = "version";
    private static final String PROPERTY_SCREENSAVER = "System.ScreensaverActive";
    private static final String PROPERTY_VOLUME = "volume";
    private static final String PROPERTY_MUTED = "muted";
    private static final String PROPERTY_TOTALTIME = "totaltime";
    private static final String PROPERTY_TIME = "time";
    private static final String PROPERTY_PERCENTAGE = "percentage";
    private static final String PROPERTY_SUBTITLEENABLED = "subtitleenabled";
    private static final String PROPERTY_CURRENTSUBTITLE = "currentsubtitle";
    private static final String PROPERTY_CURRENTVIDEOSTREAM = "currentvideostream";
    private static final String PROPERTY_CURRENTAUDIOSTREAM = "currentaudiostream";
    private static final String PROPERTY_SUBTITLES = "subtitles";
    private static final String PROPERTY_AUDIOSTREAMS = "audiostreams";
    private static final String PROPERTY_CANHIBERNATE = "canhibernate";
    private static final String PROPERTY_CANREBOOT = "canreboot";
    private static final String PROPERTY_CANSHUTDOWN = "canshutdown";
    private static final String PROPERTY_CANSUSPEND = "cansuspend";
    private static final String PROPERTY_UNIQUEID = "uniqueid";

    private final Logger logger = LoggerFactory.getLogger(KodiConnection.class);

    private static final int VOLUMESTEP = 10;
    // 0 = STOP or -1 = PLAY BACKWARDS are valid as well, but we don't want use them for FAST FORWARD or REWIND speeds
    private static final List<Integer> SPEEDS = Arrays
            .asList(new Integer[] { -32, -16, -8, -4, -2, 1, 2, 4, 8, 16, 32 });
    private static final ByteArrayFileCache IMAGE_CACHE = new ByteArrayFileCache("org.openhab.binding.kodi");
    private static final ExpiringCacheMap<String, JsonElement> REQUEST_CACHE = new ExpiringCacheMap<>(
            TimeUnit.MINUTES.toMillis(5));

    private final Gson gson = new Gson();

    private String hostname;
    private URI wsUri;
    private URI imageUri;
    private KodiClientSocket socket;

    private int volume = 0;
    private KodiState currentState = KodiState.STOP;
    private KodiPlaylistState currentPlaylistState = KodiPlaylistState.CLEAR;

    private final KodiEventListener listener;
    private final WebSocketClient webSocketClient;
    private final String callbackUrl;

    public KodiConnection(KodiEventListener listener, WebSocketClient webSocketClient, String callbackUrl) {
        this.listener = listener;
        this.webSocketClient = webSocketClient;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public synchronized void onConnectionClosed() {
        listener.updateConnectionState(false);
    }

    @Override
    public synchronized void onConnectionOpened() {
        listener.updateConnectionState(true);
    }

    public synchronized void connect(String hostname, int port, ScheduledExecutorService scheduler, URI imageUri) {
        this.hostname = hostname;
        this.imageUri = imageUri;
        try {
            close();
            wsUri = new URI("ws", null, hostname, port, "/jsonrpc", null, null);
            socket = new KodiClientSocket(this, wsUri, scheduler, webSocketClient);
            checkConnection();
        } catch (URISyntaxException e) {
            logger.warn("exception during constructing URI host={}, port={}", hostname, port, e);
        }
    }

    private int getActivePlayer() {
        JsonElement response = socket.callMethod("Player.GetActivePlayers");

        if (response instanceof JsonArray) {
            JsonArray result = response.getAsJsonArray();
            if (result.size() > 0) {
                JsonObject player0 = result.get(0).getAsJsonObject();
                if (player0.has("playerid")) {
                    return player0.get("playerid").getAsInt();
                }
            }
        }
        return -1;
    }

    public int getActivePlaylist() {
        for (JsonElement element : getPlaylistsInternal()) {
            JsonObject playlist = (JsonObject) element;
            if (playlist.has("playlistid")) {
                int playlistID = playlist.get("playlistid").getAsInt();
                JsonObject playlistItems = getPlaylistItemsInternal(playlistID);
                if (playlistItems.has("limits") && playlistItems.get("limits") instanceof JsonObject) {
                    JsonObject limits = playlistItems.get("limits").getAsJsonObject();
                    if (limits.has("total") && limits.get("total").getAsInt() > 0) {
                        return playlistID;
                    }
                }
            }
        }
        return -1;
    }

    public int getPlaylistID(String type) {
        for (JsonElement element : getPlaylistsInternal()) {
            JsonObject playlist = (JsonObject) element;
            if (playlist.has("playlistid") && playlist.has("type") && type.equals(playlist.get("type").getAsString())) {
                return playlist.get("playlistid").getAsInt();
            }
        }
        return -1;
    }

    private synchronized JsonArray getPlaylistsInternal() {
        String method = "Playlist.GetPlaylists";
        String hash = hostname + '#' + method;
        JsonElement response = REQUEST_CACHE.putIfAbsentAndGet(hash, () -> {
            return socket.callMethod(method);
        });

        if (response instanceof JsonArray) {
            return response.getAsJsonArray();
        } else {
            return null;
        }
    }

    private synchronized JsonObject getPlaylistItemsInternal(int playlistID) {
        JsonObject params = new JsonObject();
        params.addProperty("playlistid", playlistID);
        JsonElement response = socket.callMethod("Playlist.GetItems", params);

        if (response instanceof JsonObject) {
            return response.getAsJsonObject();
        } else {
            return null;
        }
    }

    public synchronized void playerPlayPause() {
        int activePlayer = getActivePlayer();

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        socket.callMethod("Player.PlayPause", params);
    }

    public synchronized void playerStop() {
        int activePlayer = getActivePlayer();

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        socket.callMethod("Player.Stop", params);
    }

    public synchronized void playerNext() {
        goToInternal("next");

        updatePlayerStatus();
    }

    public synchronized void playerPrevious() {
        goToInternal("previous");

        updatePlayerStatus();
    }

    private void goToInternal(String to) {
        int activePlayer = getActivePlayer();

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.addProperty("to", to);
        socket.callMethod("Player.GoTo", params);
    }

    public synchronized void playerRewind() {
        setSpeedInternal(calcNextSpeed(-1));

        updatePlayerStatus();
    }

    public synchronized void playerFastForward() {
        setSpeedInternal(calcNextSpeed(1));

        updatePlayerStatus();
    }

    private int calcNextSpeed(int modifier) {
        int activePlayer = getActivePlayer();
        if (activePlayer >= 0) {
            int position = SPEEDS.indexOf(getSpeed(activePlayer));
            if (position == -1) {
                return 0;
            } else if (position == 0 || position == (SPEEDS.size() - 1)) {
                return SPEEDS.get(position);
            } else {
                return SPEEDS.get(position + modifier);
            }
        } else {
            return 0;
        }
    }

    private void setSpeedInternal(int speed) {
        int activePlayer = getActivePlayer();

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.addProperty("speed", speed);
        socket.callMethod("Player.SetSpeed", params);
    }

    public synchronized void playlistAdd(int playlistID, String uri) {
        currentPlaylistState = KodiPlaylistState.ADD;

        JsonObject item = new JsonObject();
        item.addProperty("file", uri);

        JsonObject params = new JsonObject();
        params.addProperty("playlistid", playlistID);
        params.add("item", item);
        socket.callMethod("Playlist.Add", params);
    }

    public synchronized void playlistClear(int playlistID) {
        currentPlaylistState = KodiPlaylistState.CLEAR;

        JsonObject params = new JsonObject();
        params.addProperty("playlistid", playlistID);
        socket.callMethod("Playlist.Clear", params);
    }

    public synchronized void playlistInsert(int playlistID, String uri, int position) {
        currentPlaylistState = KodiPlaylistState.INSERT;

        JsonObject item = new JsonObject();
        item.addProperty("file", uri);

        JsonObject params = new JsonObject();
        params.addProperty("playlistid", playlistID);
        params.addProperty("position", position);
        params.add("item", item);
        socket.callMethod("Playlist.Insert", params);
    }

    public synchronized void playlistPlay(int playlistID, int position) {
        JsonObject item = new JsonObject();
        item.addProperty("playlistid", playlistID);
        item.addProperty("position", position);

        playInternal(item, null);
    }

    public synchronized void playlistRemove(int playlistID, int position) {
        currentPlaylistState = KodiPlaylistState.REMOVE;

        JsonObject params = new JsonObject();
        params.addProperty("playlistid", playlistID);
        params.addProperty("position", position);
        socket.callMethod("Playlist.Remove", params);
    }

    /**
     * Retrieves a list of favorites from the Kodi instance. The result is cached.
     *
     * @return a list of {@link KodiFavorite}
     */
    public synchronized List<KodiFavorite> getFavorites() {
        String method = "Favourites.GetFavourites";
        String hash = hostname + '#' + method;
        JsonElement response = REQUEST_CACHE.putIfAbsentAndGet(hash, () -> {
            final String[] properties = { "path", "window", "windowparameter" };

            JsonObject params = new JsonObject();
            params.add("properties", getJsonArray(properties));
            return socket.callMethod(method, params);
        });

        List<KodiFavorite> favorites = new ArrayList<>();
        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();
            if (result.has("favourites")) {
                JsonElement favourites = result.get("favourites");
                if (favourites instanceof JsonArray) {
                    for (JsonElement element : favourites.getAsJsonArray()) {
                        JsonObject object = (JsonObject) element;
                        KodiFavorite favorite = new KodiFavorite(object.get("title").getAsString());
                        favorite.setFavoriteType(object.get("type").getAsString());
                        if (object.has("path")) {
                            favorite.setPath(object.get("path").getAsString());
                        }
                        if (object.has("window")) {
                            favorite.setWindow(object.get("window").getAsString());
                            favorite.setWindowParameter(object.get("windowparameter").getAsString());
                        }
                        favorites.add(favorite);
                    }
                }
            }
        }
        return favorites;
    }

    /**
     * Returns the favorite with the given title or null.
     *
     * @param favoriteTitle the title of the favorite
     * @return the ({@link KodiFavorite}) with the given title
     */
    public @Nullable KodiFavorite getFavorite(final String favoriteTitle) {
        for (KodiFavorite favorite : getFavorites()) {
            String title = favorite.getTitle();
            if (favoriteTitle.equalsIgnoreCase(title)) {
                return favorite;
            }
        }
        return null;
    }

    /**
     * Activates the given window.
     *
     * @param window the window
     */
    public synchronized void activateWindow(final String window) {
        activateWindow(window, null);
    }

    /**
     * Activates the given window.
     *
     * @param window the window
     * @param windowParameter list of parameters of the window
     */
    public synchronized void activateWindow(final String window, @Nullable final String[] windowParameter) {
        JsonObject params = new JsonObject();
        params.addProperty("window", window);
        if (windowParameter != null) {
            params.add("parameters", getJsonArray(windowParameter));
        }
        socket.callMethod("GUI.ActivateWindow", params);
    }

    public synchronized void increaseVolume() {
        setVolumeInternal(this.volume + VOLUMESTEP);
    }

    public synchronized void decreaseVolume() {
        setVolumeInternal(this.volume - VOLUMESTEP);
    }

    public synchronized void setVolume(int volume) {
        setVolumeInternal(volume);
    }

    private void setVolumeInternal(int volume) {
        JsonObject params = new JsonObject();
        params.addProperty(PROPERTY_VOLUME, volume);
        socket.callMethod("Application.SetVolume", params);
    }

    public int getVolume() {
        return volume;
    }

    public synchronized void setMute(boolean mute) {
        JsonObject params = new JsonObject();
        params.addProperty("mute", mute);
        socket.callMethod("Application.SetMute", params);
    }

    public synchronized void setAudioStream(int stream) {
        JsonObject params = new JsonObject();
        params.addProperty("stream", stream);
        int activePlayer = getActivePlayer();
        params.addProperty("playerid", activePlayer);
        socket.callMethod("Player.SetAudioStream", params);
    }

    public synchronized void setVideoStream(int stream) {
        JsonObject params = new JsonObject();
        params.addProperty("stream", stream);
        int activePlayer = getActivePlayer();
        params.addProperty("playerid", activePlayer);
        socket.callMethod("Player.SetVideoStream", params);
    }

    public synchronized void setSubtitle(int subtitle) {
        JsonObject params = new JsonObject();
        params.addProperty("subtitle", subtitle);
        int activePlayer = getActivePlayer();
        params.addProperty("playerid", activePlayer);
        socket.callMethod("Player.SetSubtitle", params);
    }

    public synchronized void setSubtitleEnabled(boolean subtitleenabled) {
        JsonObject params = new JsonObject();
        params.addProperty("subtitle", subtitleenabled ? "on" : "off");
        int activePlayer = getActivePlayer();
        params.addProperty("playerid", activePlayer);
        socket.callMethod("Player.SetSubtitle", params);
    }

    private int getSpeed(int activePlayer) {
        final String[] properties = { "speed" };

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.add("properties", getJsonArray(properties));
        JsonElement response = socket.callMethod("Player.GetProperties", params);

        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();
            if (result.has("speed")) {
                return result.get("speed").getAsInt();
            }
        }
        return 0;
    }

    public synchronized void updatePlayerStatus() {
        if (socket.isConnected()) {
            int activePlayer = getActivePlayer();
            if (activePlayer >= 0) {
                int speed = getSpeed(activePlayer);
                if (speed == 0) {
                    updateState(KodiState.STOP);
                } else if (speed == 1) {
                    updateState(KodiState.PLAY);
                } else if (speed < 0) {
                    updateState(KodiState.REWIND);
                } else {
                    updateState(KodiState.FASTFORWARD);
                }
                requestPlayerUpdate(activePlayer);
            } else {
                updateState(KodiState.STOP);
            }
        }
    }

    private void requestPlayerUpdate(int activePlayer) {
        requestPlayerPropertiesUpdate(activePlayer);
        requestPlayerItemUpdate(activePlayer);
    }

    private void requestPlayerItemUpdate(int activePlayer) {
        final String[] properties = { PROPERTY_UNIQUEID, "title", "originaltitle", "album", "artist", "track",
                "director", PROPERTY_THUMBNAIL, PROPERTY_FANART, "file", "showtitle", "season", "episode", "channel",
                "channeltype", "genre", "mpaa", "rating", "votes", "userrating" };

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.add("properties", getJsonArray(properties));
        JsonElement response = socket.callMethod("Player.GetItem", params);

        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();
            if (result.has("item")) {
                JsonObject item = result.get("item").getAsJsonObject();

                int mediaid = -1;
                if (item.has("id")) {
                    mediaid = item.get("id").getAsInt();
                }

                double rating = -1;
                if (item.has("rating")) {
                    rating = item.get("rating").getAsDouble();
                }

                double userrating = -1;
                if (item.has("userrating")) {
                    userrating = item.get("userrating").getAsDouble();
                }

                String mpaa = "";
                if (item.has("mpaa")) {
                    mpaa = item.get("mpaa").getAsString();
                }

                String mediafile = "";
                if (item.has("file")) {
                    mediafile = item.get("file").getAsString();
                }

                String uniqueIDDouban = "";
                String uniqueIDImdb = "";
                String uniqueIDTmdb = "";
                String uniqueIDImdbtvshow = "";
                String uniqueIDTmdbtvshow = "";
                String uniqueIDTmdbepisode = "";

                if (item.has(PROPERTY_UNIQUEID)) {
                    try {
                        KodiUniqueID uniqueID = gson.fromJson(item.get(PROPERTY_UNIQUEID), KodiUniqueID.class);
                        if (uniqueID != null) {
                            uniqueIDImdb = uniqueID.getImdb();
                            uniqueIDDouban = uniqueID.getDouban();
                            uniqueIDTmdb = uniqueID.getTmdb();
                            uniqueIDImdbtvshow = uniqueID.getImdbtvshow();
                            uniqueIDTmdbtvshow = uniqueID.getTmdbtvshow();
                            uniqueIDTmdbepisode = uniqueID.getTmdbepisode();
                        }
                    } catch (JsonSyntaxException e) {
                        // do nothing
                    }
                }

                String originaltitle = "";
                if (item.has("originaltitle")) {
                    originaltitle = item.get("originaltitle").getAsString();
                }

                String title = "";
                if (item.has("title")) {
                    title = item.get("title").getAsString();
                }
                if (title.isEmpty()) {
                    title = item.get("label").getAsString();
                }

                String showTitle = "";
                if (item.has("showtitle")) {
                    showTitle = item.get("showtitle").getAsString();
                }

                int season = -1;
                if (item.has("season")) {
                    season = item.get("season").getAsInt();
                }

                int episode = -1;
                if (item.has("episode")) {
                    episode = item.get("episode").getAsInt();
                }

                String album = "";
                if (item.has("album")) {
                    album = item.get("album").getAsString();
                }

                String mediaType = item.get("type").getAsString();
                if ("channel".equals(mediaType) && item.has("channeltype")) {
                    String channelType = item.get("channeltype").getAsString();
                    if ("radio".equals(channelType)) {
                        mediaType = "radio";
                    }
                }

                List<String> artistList = null;
                if ("movie".equals(mediaType) && item.has("director")) {
                    artistList = convertFromArrayToList(item.get("director").getAsJsonArray());
                } else {
                    if (item.has("artist")) {
                        artistList = convertFromArrayToList(item.get("artist").getAsJsonArray());
                    }
                }

                List<String> genreList = null;
                if (item.has("genre")) {
                    JsonElement genre = item.get("genre");
                    if (genre instanceof JsonArray) {
                        genreList = convertFromArrayToList(genre.getAsJsonArray());
                    }
                }

                String channel = "";
                if (item.has("channel")) {
                    channel = item.get("channel").getAsString();
                }

                RawType thumbnail = null;
                if (item.has(PROPERTY_THUMBNAIL)) {
                    thumbnail = getImageForElement(item.get(PROPERTY_THUMBNAIL));
                }

                RawType fanart = null;
                if (item.has(PROPERTY_FANART)) {
                    fanart = getImageForElement(item.get(PROPERTY_FANART));
                }

                listener.updateMediaID(mediaid);
                listener.updateAlbum(album);
                listener.updateTitle(title);
                listener.updateOriginalTitle(originaltitle);
                listener.updateShowTitle(showTitle);
                listener.updateArtistList(artistList);
                listener.updateMediaType(mediaType);
                listener.updateGenreList(genreList);
                listener.updatePVRChannel(channel);
                listener.updateThumbnail(thumbnail);
                listener.updateFanart(fanart);
                listener.updateSeason(season);
                listener.updateEpisode(episode);
                listener.updateMediaFile(mediafile);
                listener.updateMpaa(mpaa);
                listener.updateRating(rating);
                listener.updateUserRating(userrating);
                listener.updateUniqueIDDouban(uniqueIDDouban);
                listener.updateUniqueIDImdb(uniqueIDImdb);
                listener.updateUniqueIDTmdb(uniqueIDTmdb);
                listener.updateUniqueIDImdbtvshow(uniqueIDImdbtvshow);
                listener.updateUniqueIDTmdbtvshow(uniqueIDTmdbtvshow);
                listener.updateUniqueIDTmdbepisode(uniqueIDTmdbepisode);
            }
        }
    }

    private void requestPlayerPropertiesUpdate(int activePlayer) {
        final String[] properties = { PROPERTY_SUBTITLEENABLED, PROPERTY_CURRENTSUBTITLE, PROPERTY_CURRENTAUDIOSTREAM,
                PROPERTY_CURRENTVIDEOSTREAM, PROPERTY_PERCENTAGE, PROPERTY_TIME, PROPERTY_TOTALTIME,
                PROPERTY_AUDIOSTREAMS, PROPERTY_SUBTITLES };

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.add("properties", getJsonArray(properties));
        JsonElement response = socket.callMethod("Player.GetProperties", params);

        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();

            if (result.has(PROPERTY_AUDIOSTREAMS)) {
                try {
                    JsonElement audioGroup = result.get(PROPERTY_AUDIOSTREAMS);
                    if (audioGroup instanceof JsonArray) {
                        List<KodiAudioStream> audioStreamList = new ArrayList<>();
                        for (JsonElement element : audioGroup.getAsJsonArray()) {
                            KodiAudioStream audioStream = gson.fromJson(element, KodiAudioStream.class);
                            audioStreamList.add(audioStream);
                        }
                        listener.updateAudioStreamOptions(audioStreamList);
                    }
                } catch (JsonSyntaxException e) {
                    // do nothing
                }
            }

            if (result.has(PROPERTY_SUBTITLES)) {
                try {
                    JsonElement subtitleGroup = result.get(PROPERTY_SUBTITLES);
                    if (subtitleGroup instanceof JsonArray) {
                        List<KodiSubtitle> subtitleList = new ArrayList<>();
                        for (JsonElement element : subtitleGroup.getAsJsonArray()) {
                            KodiSubtitle subtitle = gson.fromJson(element, KodiSubtitle.class);
                            subtitleList.add(subtitle);
                        }
                        listener.updateSubtitleOptions(subtitleList);
                    }
                } catch (JsonSyntaxException e) {
                    // do nothing
                }
            }

            boolean subtitleEnabled = false;
            if (result.has(PROPERTY_SUBTITLEENABLED)) {
                subtitleEnabled = result.get(PROPERTY_SUBTITLEENABLED).getAsBoolean();
            }

            int subtitleIndex = -1;
            String subtitleLanguage = null;
            String subtitleName = null;
            if (result.has(PROPERTY_CURRENTSUBTITLE)) {
                try {
                    KodiSubtitle subtitleStream = gson.fromJson(result.get(PROPERTY_CURRENTSUBTITLE),
                            KodiSubtitle.class);
                    if (subtitleStream != null) {
                        subtitleIndex = subtitleStream.getIndex();
                        subtitleLanguage = subtitleStream.getLanguage();
                        subtitleName = subtitleStream.getName();
                    }
                } catch (JsonSyntaxException e) {
                    // do nothing
                }
            }

            String audioCodec = null;
            int audioIndex = -1;
            int audioChannels = 0;
            String audioLanguage = null;
            String audioName = null;
            if (result.has(PROPERTY_CURRENTAUDIOSTREAM)) {
                try {
                    KodiAudioStream audioStream = gson.fromJson(result.get(PROPERTY_CURRENTAUDIOSTREAM),
                            KodiAudioStream.class);
                    if (audioStream != null) {
                        audioCodec = audioStream.getCodec();
                        audioIndex = audioStream.getIndex();
                        audioChannels = audioStream.getChannels();
                        audioLanguage = audioStream.getLanguage();
                        audioName = audioStream.getName();
                    }
                } catch (JsonSyntaxException e) {
                    // do nothing
                }
            }

            String videoCodec = null;
            int videoWidth = 0;
            int videoHeight = 0;
            int videoIndex = -1;
            if (result.has(PROPERTY_CURRENTVIDEOSTREAM)) {
                try {
                    KodiVideoStream videoStream = gson.fromJson(result.get(PROPERTY_CURRENTVIDEOSTREAM),
                            KodiVideoStream.class);
                    if (videoStream != null) {
                        videoCodec = videoStream.getCodec();
                        videoWidth = videoStream.getWidth();
                        videoHeight = videoStream.getHeight();
                        videoIndex = videoStream.getIndex();
                    }
                } catch (JsonSyntaxException e) {
                    // do nothing
                }
            }

            double percentage = -1;
            if (result.has(PROPERTY_PERCENTAGE)) {
                percentage = result.get(PROPERTY_PERCENTAGE).getAsDouble();
            }

            long currentTime = -1;
            if (result.has(PROPERTY_TIME)) {
                try {
                    KodiDuration time = gson.fromJson(result.get(PROPERTY_TIME), KodiDuration.class);
                    currentTime = time.toSeconds();
                } catch (JsonSyntaxException e) {
                    // do nothing
                }
            }

            long duration = -1;
            if (result.has(PROPERTY_TOTALTIME)) {
                try {
                    KodiDuration totalTime = gson.fromJson(result.get(PROPERTY_TOTALTIME), KodiDuration.class);
                    duration = totalTime.toSeconds();
                } catch (JsonSyntaxException e) {
                    // do nothing
                }
            }

            listener.updateAudioCodec(audioCodec);
            listener.updateAudioIndex(audioIndex);
            listener.updateAudioName(audioName);
            listener.updateAudioLanguage(audioLanguage);
            listener.updateAudioChannels(audioChannels);
            listener.updateVideoCodec(videoCodec);
            listener.updateVideoIndex(videoIndex);
            listener.updateVideoHeight(videoHeight);
            listener.updateVideoWidth(videoWidth);
            listener.updateSubtitleEnabled(subtitleEnabled);
            listener.updateSubtitleIndex(subtitleIndex);
            listener.updateSubtitleName(subtitleName);
            listener.updateSubtitleLanguage(subtitleLanguage);
            listener.updateCurrentTimePercentage(percentage);
            listener.updateCurrentTime(currentTime);
            listener.updateDuration(duration);
        }
    }

    private JsonArray getJsonArray(String[] values) {
        JsonArray result = new JsonArray();
        for (String param : values) {
            result.add(new JsonPrimitive(param));
        }
        return result;
    }

    private List<String> convertFromArrayToList(JsonArray data) {
        List<String> list = new ArrayList<>();
        for (JsonElement element : data) {
            list.add(element.getAsString());
        }
        return list;
    }

    private @Nullable RawType getImageForElement(JsonElement element) {
        String text = element.getAsString();
        if (!text.isEmpty()) {
            String url = stripImageUrl(text);
            if (url != null) {
                return downloadImageFromCache(url);
            }
        }
        return null;
    }

    private @Nullable String stripImageUrl(String url) {
        // we have to strip ending "/" here because Kodi returns a not valid path and filename
        // "fanart":"image://http%3a%2f%2fthetvdb.com%2fbanners%2ffanart%2foriginal%2f263365-31.jpg/"
        // "thumbnail":"image://http%3a%2f%2fthetvdb.com%2fbanners%2fepisodes%2f263365%2f5640869.jpg/"
        String encodedURL = URLEncoder.encode(stripEnd(url, '/'), StandardCharsets.UTF_8);
        return imageUri.resolve(encodedURL).toString();
    }

    private String stripEnd(final String str, final char suffix) {
        int end = str.length();
        if (end == 0) {
            return str;
        }
        while (end > 0 && str.charAt(end - 1) == suffix) {
            end--;
        }
        return str.substring(0, end);
    }

    private @Nullable RawType downloadImage(String url) {
        logger.debug("Trying to download the content of URL '{}'", url);
        RawType downloadedImage = HttpUtil.downloadImage(url);
        if (downloadedImage == null) {
            logger.debug("Failed to download the content of URL '{}'", url);
        }
        return downloadedImage;
    }

    private @Nullable RawType downloadImageFromCache(String url) {
        if (IMAGE_CACHE.containsKey(url)) {
            try {
                byte[] bytes = IMAGE_CACHE.get(url);
                String contentType = HttpUtil.guessContentTypeFromData(bytes);
                return new RawType(bytes,
                        contentType == null || contentType.isEmpty() ? RawType.DEFAULT_MIME_TYPE : contentType);
            } catch (IOException e) {
                logger.trace("Failed to download the content of URL '{}'", url, e);
            }
        } else {
            RawType image = downloadImage(url);
            if (image != null) {
                IMAGE_CACHE.put(url, image.getBytes());
                return image;
            }
        }
        return null;
    }

    public KodiState getState() {
        return currentState;
    }

    public KodiPlaylistState getPlaylistState() {
        return currentPlaylistState;
    }

    private void updateState(KodiState state) {
        // sometimes get a Pause immediately after a Stop - so just ignore
        if (currentState.equals(KodiState.STOP) && state.equals(KodiState.PAUSE)) {
            return;
        }
        listener.updatePlayerState(state);
        // if this is a Stop then clear everything else
        if (state == KodiState.STOP) {
            listener.updateAlbum("");
            listener.updateTitle("");
            listener.updateShowTitle("");
            listener.updateArtistList(null);
            listener.updateMediaType("");
            listener.updateGenreList(null);
            listener.updatePVRChannel("");
            listener.updateThumbnail(null);
            listener.updateFanart(null);
            listener.updateCurrentTimePercentage(-1);
            listener.updateCurrentTime(-1);
            listener.updateDuration(-1);
            listener.updateMediaID(-1);
            listener.updateOriginalTitle("");
            listener.updateSeason(-1);
            listener.updateEpisode(-1);
            listener.updateMediaFile("");
            listener.updateMpaa("");
            listener.updateRating(-1);
            listener.updateUserRating(-1);
            listener.updateUniqueIDDouban("");
            listener.updateUniqueIDImdb("");
            listener.updateUniqueIDTmdb("");
            listener.updateUniqueIDImdbtvshow("");
            listener.updateUniqueIDTmdbtvshow("");
            listener.updateUniqueIDTmdbepisode("");
            listener.updateAudioStreamOptions(new ArrayList<>());
            listener.updateSubtitleOptions(new ArrayList<>());
            listener.updateAudioCodec(null);
            listener.updateVideoCodec(null);
            listener.updateAudioIndex(-1);
            listener.updateAudioName(null);
            listener.updateAudioLanguage(null);
            listener.updateAudioChannels(-1);
            listener.updateVideoIndex(-1);
            listener.updateVideoHeight(-1);
            listener.updateVideoWidth(-1);
            listener.updateSubtitleIndex(-1);
            listener.updateSubtitleName(null);
            listener.updateSubtitleLanguage(null);
        }
        // keep track of our current state
        currentState = state;
    }

    @Override
    public void handleEvent(JsonObject json) {
        JsonElement methodElement = json.get("method");

        if (methodElement != null) {
            String method = methodElement.getAsString();
            JsonObject params = json.get("params").getAsJsonObject();
            if (method.startsWith("Player.On")) {
                processPlayerStateChanged(method, params);
            } else if (method.startsWith("Application.On")) {
                processApplicationStateChanged(method, params);
            } else if (method.startsWith("System.On")) {
                processSystemStateChanged(method, params);
            } else if (method.startsWith("GUI.OnScreensaver")) {
                processScreenSaverStateChanged(method, params);
            } else if (method.startsWith("Input.OnInput")) {
                processInputRequestedStateChanged(method, params);
            } else if (method.startsWith("Playlist.On")) {
                processPlaylistStateChanged(method, params);
            } else {
                logger.debug("Received unknown method: {}", method);
            }
        }
    }

    private void processPlayerStateChanged(String method, JsonObject json) {
        if ("Player.OnPlay".equals(method) || "Player.OnAVStart".equals(method)) {
            // get the player id and make a new request for the media details

            JsonObject data = json.get("data").getAsJsonObject();
            JsonObject player = data.get("player").getAsJsonObject();
            Integer playerId = player.get("playerid").getAsInt();

            updateState(KodiState.PLAY);

            requestPlayerUpdate(playerId);
        } else if ("Player.OnPause".equals(method)) {
            updateState(KodiState.PAUSE);
        } else if ("Player.OnResume".equals(method)) {
            updateState(KodiState.PLAY);
        } else if ("Player.OnStop".equals(method)) {
            // get the end parameter and send an End state if true
            JsonObject data = json.get("data").getAsJsonObject();
            Boolean end = data.get("end").getAsBoolean();
            if (end) {
                updateState(KodiState.END);
            }
            updateState(KodiState.STOP);
        } else if ("Player.OnPropertyChanged".equals(method)) {
            logger.debug("Player.OnPropertyChanged");
        } else if ("Player.OnSpeedChanged".equals(method)) {
            JsonObject data = json.get("data").getAsJsonObject();
            JsonObject player = data.get("player").getAsJsonObject();
            int speed = player.get("speed").getAsInt();
            if (speed == 0) {
                updateState(KodiState.PAUSE);
            } else if (speed == 1) {
                updateState(KodiState.PLAY);
            } else if (speed < 0) {
                updateState(KodiState.REWIND);
            } else if (speed > 1) {
                updateState(KodiState.FASTFORWARD);
            }
        } else {
            logger.debug("Unknown event from Kodi {}: {}", method, json);
        }
        listener.updateConnectionState(true);
    }

    private void processApplicationStateChanged(String method, JsonObject json) {
        if ("Application.OnVolumeChanged".equals(method)) {
            // get the player id and make a new request for the media details
            JsonObject data = json.get("data").getAsJsonObject();
            if (data.has(PROPERTY_VOLUME)) {
                volume = data.get(PROPERTY_VOLUME).getAsInt();
                listener.updateVolume(volume);
            }
            if (data.has(PROPERTY_MUTED)) {
                boolean muted = data.get(PROPERTY_MUTED).getAsBoolean();
                listener.updateMuted(muted);
            }
        } else {
            logger.debug("Unknown event from Kodi {}: {}", method, json);
        }
        listener.updateConnectionState(true);
    }

    private void processSystemStateChanged(String method, JsonObject json) {
        if ("System.OnQuit".equals(method) || "System.OnRestart".equals(method) || "System.OnSleep".equals(method)) {
            listener.updateConnectionState(false);
        } else if ("System.OnWake".equals(method)) {
            listener.updateConnectionState(true);
        } else {
            logger.debug("Unknown event from Kodi {}: {}", method, json);
        }
    }

    private void processScreenSaverStateChanged(String method, JsonObject json) {
        if ("GUI.OnScreensaverDeactivated".equals(method)) {
            listener.updateScreenSaverState(false);
        } else if ("GUI.OnScreensaverActivated".equals(method)) {
            listener.updateScreenSaverState(true);
        } else {
            logger.debug("Unknown event from Kodi {}: {}", method, json);
        }
        listener.updateConnectionState(true);
    }

    private void processInputRequestedStateChanged(String method, JsonObject json) {
        if ("Input.OnInputFinished".equals(method)) {
            listener.updateInputRequestedState(false);
        } else if ("Input.OnInputRequested".equals(method)) {
            listener.updateInputRequestedState(true);
        } else {
            logger.debug("Unknown event from Kodi {}: {}", method, json);
        }
        listener.updateConnectionState(true);
    }

    private void processPlaylistStateChanged(String method, JsonObject json) {
        if ("Playlist.OnAdd".equals(method)) {
            currentPlaylistState = KodiPlaylistState.ADDED;

            listener.updatePlaylistState(KodiPlaylistState.ADDED);
        } else if ("Playlist.OnRemove".equals(method)) {
            currentPlaylistState = KodiPlaylistState.REMOVED;

            listener.updatePlaylistState(KodiPlaylistState.REMOVED);
        } else {
            logger.debug("Unknown event from Kodi {}: {}", method, json);
        }
        listener.updateConnectionState(true);
    }

    public synchronized void close() {
        if (socket != null && socket.isConnected()) {
            socket.close();
        }
    }

    public void updateScreenSaverState() {
        if (socket.isConnected()) {
            String[] props = { PROPERTY_SCREENSAVER };

            JsonObject params = new JsonObject();
            params.add("booleans", getJsonArray(props));
            JsonElement response = socket.callMethod("XBMC.GetInfoBooleans", params);

            if (response instanceof JsonObject) {
                JsonObject data = response.getAsJsonObject();
                if (data.has(PROPERTY_SCREENSAVER)) {
                    listener.updateScreenSaverState(data.get(PROPERTY_SCREENSAVER).getAsBoolean());
                }
            }
        } else {
            listener.updateScreenSaverState(false);
        }
    }

    public void updateVolume() {
        if (socket.isConnected()) {
            String[] props = { PROPERTY_VOLUME, PROPERTY_MUTED };

            JsonObject params = new JsonObject();
            params.add("properties", getJsonArray(props));
            JsonElement response = socket.callMethod("Application.GetProperties", params);

            if (response instanceof JsonObject) {
                JsonObject data = response.getAsJsonObject();
                if (data.has(PROPERTY_VOLUME)) {
                    volume = data.get(PROPERTY_VOLUME).getAsInt();
                    listener.updateVolume(volume);
                }
                if (data.has(PROPERTY_MUTED)) {
                    boolean muted = data.get(PROPERTY_MUTED).getAsBoolean();
                    listener.updateMuted(muted);
                }
            }
        } else {
            listener.updateVolume(100);
            listener.updateMuted(false);
        }
    }

    public void updateCurrentProfile() {
        if (socket.isConnected()) {
            JsonElement response = socket.callMethod("Profiles.GetCurrentProfile");

            try {
                final KodiProfile profile = gson.fromJson(response, KodiProfile.class);
                if (profile != null) {
                    listener.updateCurrentProfile(profile.getLabel());
                }
            } catch (JsonSyntaxException e) {
                logger.debug("Json syntax exception occurred: {}", e.getMessage(), e);
            }
        }
    }

    public synchronized void playURI(String uri) {
        String fileUri = uri;
        JsonObject item = new JsonObject();
        JsonObject options = null;

        if (uri.contains(TIMESTAMP_FRAGMENT)) {
            fileUri = uri.substring(0, uri.indexOf(TIMESTAMP_FRAGMENT));
            String timestamp = uri.substring(uri.indexOf(TIMESTAMP_FRAGMENT) + TIMESTAMP_FRAGMENT.length());
            try {
                int s = Integer.parseInt(timestamp);
                options = new JsonObject();
                options.add("resume", timeValueFromSeconds(s));
            } catch (NumberFormatException e) {
                logger.warn("Illegal parameter for timestamp - it must be an integer: {}", timestamp);
            }
        }
        item.addProperty("file", fileUri);
        playInternal(item, options);
    }

    public synchronized List<KodiPVRChannelGroup> getPVRChannelGroups(final String pvrChannelType) {
        String method = "PVR.GetChannelGroups";
        String hash = hostname + '#' + method + "#channeltype=" + pvrChannelType;
        JsonElement response = REQUEST_CACHE.putIfAbsentAndGet(hash, () -> {
            JsonObject params = new JsonObject();
            params.addProperty("channeltype", pvrChannelType);
            return socket.callMethod(method, params);
        });

        List<KodiPVRChannelGroup> pvrChannelGroups = new ArrayList<>();
        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();
            if (result.has("channelgroups")) {
                JsonElement channelgroups = result.get("channelgroups");
                if (channelgroups instanceof JsonArray) {
                    for (JsonElement element : channelgroups.getAsJsonArray()) {
                        JsonObject object = (JsonObject) element;
                        KodiPVRChannelGroup pvrChannelGroup = new KodiPVRChannelGroup();
                        pvrChannelGroup.setId(object.get("channelgroupid").getAsInt());
                        pvrChannelGroup.setLabel(object.get("label").getAsString());
                        pvrChannelGroup.setChannelType(pvrChannelType);
                        pvrChannelGroups.add(pvrChannelGroup);
                    }
                }
            }
        }
        return pvrChannelGroups;
    }

    public int getPVRChannelGroupId(final String channelType, final String pvrChannelGroupName) {
        List<KodiPVRChannelGroup> pvrChannelGroups = getPVRChannelGroups(channelType);
        for (KodiPVRChannelGroup pvrChannelGroup : pvrChannelGroups) {
            String label = pvrChannelGroup.getLabel();
            if (pvrChannelGroupName.equalsIgnoreCase(label)) {
                return pvrChannelGroup.getId();
            }
        }
        // if we don't find a matching PVR channel group return the first (which is the default: "All channels")
        return pvrChannelGroups.isEmpty() ? 0 : pvrChannelGroups.get(0).getId();
    }

    public synchronized List<KodiPVRChannel> getPVRChannels(final int pvrChannelGroupId) {
        String method = "PVR.GetChannels";
        String hash = hostname + '#' + method + "#channelgroupid=" + pvrChannelGroupId;
        JsonElement response = REQUEST_CACHE.putIfAbsentAndGet(hash, () -> {
            JsonObject params = new JsonObject();
            params.addProperty("channelgroupid", pvrChannelGroupId);
            return socket.callMethod(method, params);
        });

        List<KodiPVRChannel> pvrChannels = new ArrayList<>();
        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();
            if (result.has("channels")) {
                JsonElement channels = result.get("channels");
                if (channels instanceof JsonArray) {
                    for (JsonElement element : channels.getAsJsonArray()) {
                        JsonObject object = (JsonObject) element;
                        KodiPVRChannel pvrChannel = new KodiPVRChannel();
                        pvrChannel.setId(object.get("channelid").getAsInt());
                        pvrChannel.setLabel(object.get("label").getAsString());
                        pvrChannel.setChannelGroupId(pvrChannelGroupId);
                        pvrChannels.add(pvrChannel);
                    }
                }
            }
        }
        return pvrChannels;
    }

    public int getPVRChannelId(final int pvrChannelGroupId, final String pvrChannelName) {
        for (KodiPVRChannel pvrChannel : getPVRChannels(pvrChannelGroupId)) {
            String label = pvrChannel.getLabel();
            if (pvrChannelName.equalsIgnoreCase(label)) {
                return pvrChannel.getId();
            }
        }
        return 0;
    }

    public synchronized void playPVRChannel(final int pvrChannelId) {
        JsonObject item = new JsonObject();
        item.addProperty("channelid", pvrChannelId);

        playInternal(item, null);
    }

    private void playInternal(JsonObject item, JsonObject options) {
        JsonObject params = new JsonObject();
        params.add("item", item);
        if (options != null) {
            params.add("options", options);
        }
        socket.callMethod("Player.Open", params);
    }

    public synchronized void showNotification(String title, BigDecimal displayTime, String icon, String message) {
        JsonObject params = new JsonObject();
        params.addProperty("message", message);
        if (title != null) {
            params.addProperty("title", title);
        }
        if (displayTime != null) {
            params.addProperty("displaytime", displayTime.longValue());
        }
        if (icon != null) {
            params.addProperty("image", callbackUrl + "/icon/" + icon.toLowerCase() + ".png");
        }
        socket.callMethod("GUI.ShowNotification", params);
    }

    public boolean checkConnection() {
        if (!socket.isConnected()) {
            logger.debug("checkConnection: try to connect to Kodi {}", wsUri);
            try {
                socket.open();
                return socket.isConnected();
            } catch (IOException e) {
                logger.debug("exception during connect to {}", wsUri, e);
                socket.close();
                return false;
            }
        } else {
            // Ping Kodi with the get version command. This prevents the idle timeout on the web socket.
            return !getVersion().isEmpty();
        }
    }

    public String getConnectionName() {
        return wsUri.toString();
    }

    public String getVersion() {
        if (socket.isConnected()) {
            String[] props = { PROPERTY_VERSION };

            JsonObject params = new JsonObject();
            params.add("properties", getJsonArray(props));
            JsonElement response = socket.callMethod("Application.GetProperties", params);

            if (response instanceof JsonObject) {
                JsonObject result = response.getAsJsonObject();
                if (result.has(PROPERTY_VERSION)) {
                    JsonObject version = result.get(PROPERTY_VERSION).getAsJsonObject();
                    int major = version.get("major").getAsInt();
                    int minor = version.get("minor").getAsInt();
                    String revision = version.get("revision").getAsString();
                    return String.format("%d.%d (%s)", major, minor, revision);
                }
            }
        }
        return "";
    }

    public void input(String key) {
        socket.callMethod("Input." + key);
    }

    public void inputText(String text) {
        JsonObject params = new JsonObject();
        params.addProperty("text", text);
        socket.callMethod("Input.SendText", params);
    }

    public void inputAction(String action) {
        JsonObject params = new JsonObject();
        params.addProperty("action", action);
        socket.callMethod("Input.ExecuteAction", params);
    }

    public void inputButtonEvent(String buttonEvent) {
        logger.debug("inputButtonEvent {}.", buttonEvent);

        String button = buttonEvent;
        String keymap = "KB";
        Integer holdtime = null;

        if (buttonEvent.contains(";")) {
            String[] params = buttonEvent.split(";");
            switch (params.length) {
                case 2:
                    button = params[0];
                    keymap = params[1];
                    break;
                case 3:
                    button = params[0];
                    keymap = params[1];
                    try {
                        holdtime = Integer.parseInt(params[2]);
                    } catch (NumberFormatException nfe) {
                        holdtime = null;
                    }
                    break;
            }
        }

        this.inputButtonEvent(button, keymap, holdtime);
    }

    private void inputButtonEvent(String button, String keymap, Integer holdtime) {
        JsonObject params = new JsonObject();
        params.addProperty("button", button);
        params.addProperty("keymap", keymap);
        if (holdtime != null) {
            params.addProperty("holdtime", holdtime.intValue());
        }
        JsonElement result = socket.callMethod("Input.ButtonEvent", params);
        logger.debug("inputButtonEvent result {}.", result);
    }

    public void getSystemProperties() {
        KodiSystemProperties systemProperties = null;
        if (socket.isConnected()) {
            String[] props = { PROPERTY_CANHIBERNATE, PROPERTY_CANREBOOT, PROPERTY_CANSHUTDOWN, PROPERTY_CANSUSPEND };

            JsonObject params = new JsonObject();
            params.add("properties", getJsonArray(props));
            JsonElement response = socket.callMethod("System.GetProperties", params);

            try {
                systemProperties = gson.fromJson(response, KodiSystemProperties.class);
            } catch (JsonSyntaxException e) {
                // do nothing
            }
        }
        listener.updateSystemProperties(systemProperties);
    }

    public void sendApplicationQuit() {
        String method = "Application.Quit";
        socket.callMethod(method);
    }

    public void sendSystemCommand(String command) {
        String method = "System." + command;
        socket.callMethod(method);
    }

    public void profile(String profile) {
        JsonObject params = new JsonObject();
        params.addProperty("profile", profile);
        socket.callMethod("Profiles.LoadProfile", params);
    }

    public KodiProfile[] getProfiles() {
        KodiProfile[] profiles = new KodiProfile[0];
        if (socket.isConnected()) {
            JsonElement response = socket.callMethod("Profiles.GetProfiles");

            try {
                JsonObject profilesJson = response.getAsJsonObject();
                profiles = gson.fromJson(profilesJson.get("profiles"), KodiProfile[].class);
            } catch (JsonSyntaxException e) {
                logger.debug("Json syntax exception occurred: {}", e.getMessage(), e);
            }
        }
        return profiles;
    }

    public void setTime(int time) {
        int seconds = time;
        JsonObject params = new JsonObject();
        params.addProperty("playerid", 1);
        JsonObject value = new JsonObject();
        JsonObject timeValue = timeValueFromSeconds(seconds);

        value.add("time", timeValue);
        params.add("value", value);
        socket.callMethod("Player.Seek", params);
    }

    private JsonObject timeValueFromSeconds(int seconds) {
        JsonObject timeValue = new JsonObject();
        int s = seconds;

        if (s >= 3600) {
            int hours = s / 3600;
            timeValue.addProperty("hours", hours);
            s = s % 3600;
        }
        if (s >= 60) {
            int minutes = s / 60;
            timeValue.addProperty("minutes", minutes);
            s = seconds % 60;
        }
        timeValue.addProperty("seconds", s);
        return timeValue;
    }
}
