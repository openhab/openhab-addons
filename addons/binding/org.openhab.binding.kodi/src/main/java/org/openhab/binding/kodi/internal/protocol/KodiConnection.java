/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.protocol;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.kodi.internal.KodiEventListener;
import org.openhab.binding.kodi.internal.KodiEventListener.KodiPlaylistState;
import org.openhab.binding.kodi.internal.KodiEventListener.KodiState;
import org.openhab.binding.kodi.internal.model.KodiAudioStream;
import org.openhab.binding.kodi.internal.model.KodiDuration;
import org.openhab.binding.kodi.internal.model.KodiFavorite;
import org.openhab.binding.kodi.internal.model.KodiPVRChannel;
import org.openhab.binding.kodi.internal.model.KodiPVRChannelGroup;
import org.openhab.binding.kodi.internal.model.KodiSystemProperties;
import org.openhab.binding.kodi.internal.model.KodiVideoStream;
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

    private static final String PROPERTY_VERSION = "version";
    private static final String PROPERTY_VOLUME = "volume";
    private static final String PROPERTY_MUTED = "muted";
    private static final String PROPERTY_TOTALTIME = "totaltime";
    private static final String PROPERTY_TIME = "time";
    private static final String PROPERTY_PERCENTAGE = "percentage";
    private static final String PROPERTY_CURRENTVIDEOSTREAM = "currentvideostream";
    private static final String PROPERTY_CURRENTAUDIOSTREAM = "currentaudiostream";
    private static final String PROPERTY_CANHIBERNATE = "canhibernate";
    private static final String PROPERTY_CANREBOOT = "canreboot";
    private static final String PROPERTY_CANSHUTDOWN = "canshutdown";
    private static final String PROPERTY_CANSUSPEND = "cansuspend";

    private final Logger logger = LoggerFactory.getLogger(KodiConnection.class);

    private static final int VOLUMESTEP = 10;
    // 0 = STOP or -1 = PLAY BACKWARDS are valid as well, but we don't want use them for FAST FORWARD or REWIND speeds
    private static final List<Integer> SPEEDS = Arrays
            .asList(new Integer[] { -32, -16, -8, -4, -2, 1, 2, 4, 8, 16, 32 });
    private static final ExpiringCacheMap<String, RawType> IMAGE_CACHE = new ExpiringCacheMap<>(
            TimeUnit.MINUTES.toMillis(15));
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

    public KodiConnection(KodiEventListener listener, WebSocketClient webSocketClient) {
        this.listener = listener;
        this.webSocketClient = webSocketClient;
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
            logger.error("exception during constructing URI host={}, port={}", hostname, port, e);
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

        playInternal(item);
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
    @Nullable
    public KodiFavorite getFavorite(final String favoriteTitle) {
        for (KodiFavorite favorite : getFavorites()) {
            if (StringUtils.equalsIgnoreCase(favorite.getTitle(), favoriteTitle)) {
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
        requestPlayerItemUpdate(activePlayer);
        requestPlayerPropertiesUpdate(activePlayer);
    }

    private void requestPlayerItemUpdate(int activePlayer) {
        final String[] properties = { "title", "album", "artist", "director", "thumbnail", "file", "fanart",
                "showtitle", "streamdetails", "channel", "channeltype", "genre" };

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.add("properties", getJsonArray(properties));
        JsonElement response = socket.callMethod("Player.GetItem", params);

        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();
            if (result.has("item")) {
                JsonObject item = result.get("item").getAsJsonObject();

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
                if (item.has("thumbnail")) {
                    thumbnail = downloadImage(convertToImageUrl(item.get("thumbnail")));
                }

                RawType fanart = null;
                if (item.has("fanart")) {
                    fanart = downloadImage(convertToImageUrl(item.get("fanart")));
                }

                listener.updateAlbum(album);
                listener.updateTitle(title);
                listener.updateShowTitle(showTitle);
                listener.updateArtistList(artistList);
                listener.updateMediaType(mediaType);
                listener.updateGenreList(genreList);
                listener.updatePVRChannel(channel);
                listener.updateThumbnail(thumbnail);
                listener.updateFanart(fanart);
            }
        }
    }

    private void requestPlayerPropertiesUpdate(int activePlayer) {
        final String[] properties = { PROPERTY_CURRENTAUDIOSTREAM, PROPERTY_CURRENTVIDEOSTREAM, PROPERTY_PERCENTAGE,
                PROPERTY_TIME, PROPERTY_TOTALTIME };

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.add("properties", getJsonArray(properties));
        JsonElement response = socket.callMethod("Player.GetProperties", params);

        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();

            String audioCodec = null;
            if (result.has(PROPERTY_CURRENTAUDIOSTREAM)) {
                try {
                    KodiAudioStream audioStream = gson.fromJson(result.get(PROPERTY_CURRENTAUDIOSTREAM),
                            KodiAudioStream.class);
                    audioCodec = audioStream.getCodec();
                } catch (JsonSyntaxException e) {
                    // do nothing
                }
            }

            String videoCodec = null;
            if (result.has(PROPERTY_CURRENTVIDEOSTREAM)) {
                try {
                    KodiVideoStream videoStream = gson.fromJson(result.get(PROPERTY_CURRENTVIDEOSTREAM),
                            KodiVideoStream.class);
                    videoCodec = videoStream.getCodec();
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
            listener.updateVideoCodec(videoCodec);
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

    private String convertToImageUrl(JsonElement element) {
        String text = element.getAsString();
        if (!text.isEmpty()) {
            try {
                // we have to strip ending "/" here because Kodi returns a not valid path and filename
                // "fanart":"image://http%3a%2f%2fthetvdb.com%2fbanners%2ffanart%2foriginal%2f263365-31.jpg/"
                // "thumbnail":"image://http%3a%2f%2fthetvdb.com%2fbanners%2fepisodes%2f263365%2f5640869.jpg/"
                String encodedURL = URLEncoder.encode(StringUtils.stripEnd(text, "/"), "UTF-8");
                return imageUri.resolve(encodedURL).toString();
            } catch (UnsupportedEncodingException e) {
                logger.debug("exception during encoding {}", text, e);
                return null;
            }
        }
        return null;
    }

    private @Nullable RawType downloadImage(String url) {
        if (StringUtils.isNotEmpty(url)) {
            RawType image = IMAGE_CACHE.putIfAbsentAndGet(url, () -> {
                logger.debug("Trying to download the content of URL {}", url);
                return HttpUtil.downloadImage(url);
            });
            if (image == null) {
                logger.debug("Failed to download the content of URL {}", url);
                return null;
            } else {
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
            listener.updateAudioCodec(null);
            listener.updateVideoCodec(null);
            listener.updateCurrentTimePercentage(-1);
            listener.updateCurrentTime(-1);
            listener.updateDuration(-1);
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
                processScreensaverStateChanged(method, params);
            } else if (method.startsWith("Playlist.On")) {
                processPlaylistStateChanged(method, params);
            } else {
                logger.debug("Received unknown method: {}", method);
            }
        }
    }

    private void processPlayerStateChanged(String method, JsonObject json) {
        if ("Player.OnPlay".equals(method)) {
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

    private void processScreensaverStateChanged(String method, JsonObject json) {
        if ("GUI.OnScreensaverDeactivated".equals(method)) {
            listener.updateScreenSaverState(false);
        } else if ("GUI.OnScreensaverActivated".equals(method)) {
            listener.updateScreenSaverState(true);
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
            listener.updateMuted(false);
            listener.updateVolume(100);
        }
    }

    public synchronized void playURI(String uri) {
        JsonObject item = new JsonObject();
        item.addProperty("file", uri);

        playInternal(item);
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
            if (StringUtils.equalsIgnoreCase(pvrChannelGroup.getLabel(), pvrChannelGroupName)) {
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
            if (StringUtils.equalsIgnoreCase(pvrChannel.getLabel(), pvrChannelName)) {
                return pvrChannel.getId();
            }
        }
        return 0;
    }

    public synchronized void playPVRChannel(final int pvrChannelId) {
        JsonObject item = new JsonObject();
        item.addProperty("channelid", pvrChannelId);

        playInternal(item);
    }

    private void playInternal(JsonObject item) {
        JsonObject params = new JsonObject();
        params.add("item", item);
        socket.callMethod("Player.Open", params);
    }

    public synchronized void showNotification(String message) {
        JsonObject params = new JsonObject();
        params.addProperty("title", "openHAB");
        params.addProperty("message", message);
        socket.callMethod("GUI.ShowNotification", params);
    }

    public boolean checkConnection() {
        if (!socket.isConnected()) {
            logger.debug("checkConnection: try to connect to Kodi {}", wsUri);
            try {
                socket.open();
                return socket.isConnected();
            } catch (IOException e) {
                logger.error("exception during connect to {}", wsUri, e);
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
}
