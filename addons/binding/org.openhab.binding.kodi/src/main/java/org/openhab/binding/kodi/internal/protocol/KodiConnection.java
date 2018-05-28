/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.protocol;

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
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.kodi.internal.KodiEventListener;
import org.openhab.binding.kodi.internal.KodiEventListener.KodiPlaylistState;
import org.openhab.binding.kodi.internal.KodiEventListener.KodiState;
import org.openhab.binding.kodi.internal.model.KodiFavorite;
import org.openhab.binding.kodi.internal.model.KodiPVRChannel;
import org.openhab.binding.kodi.internal.model.KodiPVRChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * KodiConnection provides an API for accessing a Kodi device.
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 * @author Andreas Reinhardt & Christoph Weitkamp - Added channels for thumbnail and fanart
 * @author Christoph Weitkamp - Improvements for playing audio notifications
 */
public class KodiConnection implements KodiClientSocketEventListener {

    private final Logger logger = LoggerFactory.getLogger(KodiConnection.class);

    private static final int VOLUMESTEP = 10;
    // 0 = STOP or -1 = PLAY BACKWARDS are valid as well, but we don't want use them for FAST FORWARD or REWIND speeds
    private static final List<Integer> SPEEDS = Arrays
            .asList(new Integer[] { -32, -16, -8, -4, -2, 1, 2, 4, 8, 16, 32 });
    private static final ExpiringCacheMap<String, RawType> IMAGE_CACHE = new ExpiringCacheMap<>(
            TimeUnit.MINUTES.toMillis(15));
    private static final ExpiringCacheMap<String, JsonElement> REQUEST_CACHE = new ExpiringCacheMap<>(
            TimeUnit.MINUTES.toMillis(5));

    private String hostname;
    private URI wsUri;
    private URI imageUri;
    private KodiClientSocket socket;

    private int volume = 0;
    private KodiState currentState = KodiState.Stop;
    private KodiPlaylistState currentPlaylistState = KodiPlaylistState.CLEAR;

    private final KodiEventListener listener;

    public KodiConnection(KodiEventListener listener) {
        this.listener = listener;
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
            socket = new KodiClientSocket(this, wsUri, scheduler);
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
        params.addProperty("volume", volume);
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
        final String[] properties = { "speed", "position" };

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
                    updateState(KodiState.Stop);
                } else if (speed == 1) {
                    updateState(KodiState.Play);
                } else if (speed < 0) {
                    updateState(KodiState.Rewind);
                } else {
                    updateState(KodiState.FastForward);
                }
                requestPlayerUpdate(activePlayer);
            } else {
                updateState(KodiState.Stop);
            }
        }
    }

    private void requestPlayerUpdate(int activePlayer) {
        final String[] properties = { "title", "album", "artist", "director", "thumbnail", "file", "fanart",
                "showtitle", "streamdetails", "channel", "channeltype" };

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

                String artist = "";
                if ("movie".equals(mediaType)) {
                    artist = convertFromArray(item.get("director").getAsJsonArray());
                } else {
                    if (item.has("artist")) {
                        artist = convertFromArray(item.get("artist").getAsJsonArray());
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

                try {
                    listener.updateAlbum(album);
                    listener.updateTitle(title);
                    listener.updateShowTitle(showTitle);
                    listener.updateArtist(artist);
                    listener.updateMediaType(mediaType);
                    listener.updatePVRChannel(channel);
                    listener.updateThumbnail(thumbnail);
                    listener.updateFanart(fanart);
                } catch (Exception e) {
                    logger.error("Event listener invoking error", e);
                }
            }
        }
    }

    private JsonArray getJsonArray(String[] values) {
        JsonArray result = new JsonArray();
        for (String param : values) {
            result.add(new JsonPrimitive(param));
        }
        return result;
    }

    private String convertFromArray(JsonArray data) {
        StringBuilder result = new StringBuilder();
        for (JsonElement element : data) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(element.getAsString());
        }
        return result.toString();
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

    private RawType downloadImage(String url) {
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
        if (currentState.equals(KodiState.Stop) && state.equals(KodiState.Pause)) {
            return;
        }
        try {
            listener.updatePlayerState(state);
            // if this is a Stop then clear everything else
            if (state == KodiState.Stop) {
                listener.updateAlbum("");
                listener.updateTitle("");
                listener.updateShowTitle("");
                listener.updateArtist("");
                listener.updateMediaType("");
                listener.updatePVRChannel("");
                listener.updateThumbnail(null);
                listener.updateFanart(null);
            }
        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
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

            updateState(KodiState.Play);

            requestPlayerUpdate(playerId);
        } else if ("Player.OnPause".equals(method)) {
            updateState(KodiState.Pause);
        } else if ("Player.OnResume".equals(method)) {
            updateState(KodiState.Play);
        } else if ("Player.OnStop".equals(method)) {
            // get the end parameter and send an End state if true
            JsonObject data = json.get("data").getAsJsonObject();
            Boolean end = data.get("end").getAsBoolean();
            if (end) {
                updateState(KodiState.End);
            }
            updateState(KodiState.Stop);
        } else if ("Player.OnPropertyChanged".equals(method)) {
            logger.debug("Player.OnPropertyChanged");
        } else if ("Player.OnSpeedChanged".equals(method)) {
            JsonObject data = json.get("data").getAsJsonObject();
            JsonObject player = data.get("player").getAsJsonObject();
            int speed = player.get("speed").getAsInt();
            if (speed == 0) {
                updateState(KodiState.Pause);
            } else if (speed == 1) {
                updateState(KodiState.Play);
            } else if (speed < 0) {
                updateState(KodiState.Rewind);
            } else if (speed > 1) {
                updateState(KodiState.FastForward);
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
            if (data.has("volume")) {
                volume = data.get("volume").getAsInt();
                listener.updateVolume(volume);
            }
            if (data.has("muted")) {
                boolean muted = data.get("muted").getAsBoolean();
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

    public synchronized void updateVolume() {
        if (socket.isConnected()) {
            String[] props = { "volume", "version", "name", "muted" };

            JsonObject params = new JsonObject();
            params.add("properties", getJsonArray(props));
            JsonElement response = socket.callMethod("Application.GetProperties", params);

            if (response instanceof JsonObject) {
                JsonObject data = response.getAsJsonObject();
                if (data.has("volume")) {
                    volume = data.get("volume").getAsInt();
                    listener.updateVolume(volume);
                }
                if (data.has("muted")) {
                    boolean muted = data.get("muted").getAsBoolean();
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
            } catch (Exception e) {
                logger.error("exception during connect to {}", wsUri, e);
                socket.close();
                return false;
            }
        } else {
            // Ping Kodi with the get version command. This prevents the idle
            // timeout on the websocket.
            return !getVersion().isEmpty();
        }
    }

    public String getConnectionName() {
        return wsUri.toString();
    }

    public String getVersion() {
        if (socket.isConnected()) {
            String[] props = { "version", "name" };

            JsonObject params = new JsonObject();
            params.add("properties", getJsonArray(props));
            JsonElement response = socket.callMethod("Application.GetProperties", params);

            if (response instanceof JsonObject) {
                JsonObject result = response.getAsJsonObject();
                if (result.has("version")) {
                    JsonObject version = result.get("version").getAsJsonObject();
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

    public void sendSystemCommand(String command) {
        String method = "System." + command;
        socket.callMethod(method);
    }
}
