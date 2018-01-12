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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.kodi.internal.KodiEventListener;
import org.openhab.binding.kodi.internal.KodiEventListener.KodiState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * KodiConnection provides an api for accessing a Kodi device.
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 * @author Andreas Reinhardt & Christoph Weitkamp - Added channels for thumbnail and fanart
 *
 */
public class KodiConnection implements KodiClientSocketEventListener {

    private final Logger logger = LoggerFactory.getLogger(KodiConnection.class);

    private static final int VOLUMESTEP = 10;
    // 0 = STOP or -1 = PLAY BACKWARDS are valid as well, but we don't want use them for FAST FORWARD or REWIND speeds
    private static final List<Integer> SPEEDS = Arrays
            .asList(new Integer[] { -32, -16, -8, -4, -2, 1, 2, 4, 8, 16, 32 });
    private static final ExpiringCacheMap<String, RawType> IMAGE_CACHE = new ExpiringCacheMap<>(
            TimeUnit.MINUTES.toMillis(15)); // 15min

    private URI wsUri;
    private URI imageUri;
    private KodiClientSocket socket;

    private int volume = 0;
    private KodiState currentState = KodiState.Stop;

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

    public synchronized void connect(String hostName, int port, ScheduledExecutorService scheduler, URI imageUri) {
        this.imageUri = imageUri;
        try {
            close();
            wsUri = new URI("ws", null, hostName, port, "/jsonrpc", null, null);
            socket = new KodiClientSocket(this, wsUri, scheduler);
            checkConnection();
        } catch (URISyntaxException e) {
            logger.error("exception during constructing URI host={}, port={}", hostName, port, e);
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

    private void goToInternal(@NonNull String to) {
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
                return (int) SPEEDS.get(position + modifier);
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
        this.volume = volume;

        JsonObject params = new JsonObject();
        params.addProperty("volume", this.volume);
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
                    title = convertToText(item.get("title"));
                }

                String showTitle = "";
                if (item.has("showtitle")) {
                    showTitle = convertToText(item.get("showtitle"));
                }

                String album = "";
                if (item.has("album")) {
                    album = convertToText(item.get("album"));
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
        for (JsonElement x : data) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(convertToText(x));
        }
        return result.toString();
    }

    private String convertToText(JsonElement element) {
        String text = element.getAsString();
        return text;
        // try {
        // return new String(text.getBytes("ISO-8859-1"));
        // } catch (UnsupportedEncodingException e) {
        // return text;
        // }
    }

    private String convertToImageUrl(JsonElement element) {
        String text = convertToText(element);
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
            if (!IMAGE_CACHE.containsKey(url)) {
                IMAGE_CACHE.put(url, () -> {
                    logger.debug("Trying to download the content of URL {}", url);
                    return HttpUtil.downloadImage(url);
                });
            }
            RawType image = IMAGE_CACHE.get(url);
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
            logger.debug("Unknown event from Kodi {}: {}", method, json.toString());
        }
        listener.updateConnectionState(true);
    }

    private void processApplicationStateChanged(String method, JsonObject json) {
        if ("Application.OnVolumeChanged".equals(method)) {
            // get the player id and make a new request for the media details
            JsonObject data = json.get("data").getAsJsonObject();

            int volume = data.get("volume").getAsInt();
            boolean muted = data.get("muted").getAsBoolean();
            try {
                listener.updateVolume(volume);
                listener.updateMuted(muted);
            } catch (Exception e) {
                logger.error("Event listener invoking error", e);
            }

            this.volume = volume;
        } else {
            logger.debug("Unknown event from Kodi {}: {}", method, json.toString());
        }
        listener.updateConnectionState(true);
    }

    private void processSystemStateChanged(String method, JsonObject json) {
        if ("System.OnQuit".equals(method) || "System.OnRestart".equals(method) || "System.OnSleep".equals(method)) {
            listener.updateConnectionState(false);
        } else if ("System.OnWake".equals(method)) {
            listener.updateConnectionState(true);
        } else {
            logger.debug("Unknown event from Kodi {}: {}", method, json.toString());
        }
    }

    private void processScreensaverStateChanged(String method, JsonObject json) {
        if ("GUI.OnScreensaverDeactivated".equals(method)) {
            updateScreenSaverStatus(false);
        } else if ("GUI.OnScreensaverActivated".equals(method)) {
            updateScreenSaverStatus(true);
        } else {
            logger.debug("Unknown event from Kodi {}: {}", method, json.toString());
        }
        listener.updateConnectionState(true);
    }

    private void updateScreenSaverStatus(boolean screenSaverActive) {
        try {
            listener.updateScreenSaverState(screenSaverActive);
        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
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
                JsonObject result = response.getAsJsonObject();
                if (result.has("volume")) {
                    volume = result.get("volume").getAsInt();
                    listener.updateVolume(volume);
                }
                if (result.has("muted")) {
                    boolean muted = result.get("muted").getAsBoolean();
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

        JsonObject params = new JsonObject();
        params.add("item", item);
        socket.callMethod("Player.Open", params);
    }

    private synchronized JsonArray getChannelGroups(final String channelType) {
        JsonObject params = new JsonObject();
        params.addProperty("channeltype", channelType);
        JsonElement response = socket.callMethod("PVR.GetChannelGroups", params);

        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();
            if (result.has("channelgroups")) {
                return result.get("channelgroups").getAsJsonArray();
            }
        }
        return null;
    }

    public int getChannelGroupID(final String channelType, final String channelGroupName) {
        JsonArray channelGroups = getChannelGroups(channelType);
        if (channelGroups instanceof JsonArray) {
            for (JsonElement element : channelGroups) {
                JsonObject channelGroup = (JsonObject) element;
                String label = channelGroup.get("label").getAsString();
                if (StringUtils.equalsIgnoreCase(label, channelGroupName)) {
                    return channelGroup.get("channelgroupid").getAsInt();
                }
            }
        }
        return 0;
    }

    private synchronized JsonArray getChannels(final int channelGroupID) {
        JsonObject params = new JsonObject();
        params.addProperty("channelgroupid", channelGroupID);
        JsonElement response = socket.callMethod("PVR.GetChannels", params);

        if (response instanceof JsonObject) {
            JsonObject result = response.getAsJsonObject();
            if (result.has("channels")) {
                return result.get("channels").getAsJsonArray();
            }
        }
        return null;
    }

    public int getChannelID(final int channelGroupID, final String channelName) {
        JsonArray channels = getChannels(channelGroupID);
        if (channels instanceof JsonArray) {
            for (JsonElement element : channels) {
                JsonObject channel = (JsonObject) element;
                String label = channel.get("label").getAsString();
                if (StringUtils.equalsIgnoreCase(label, channelName)) {
                    return channel.get("channelid").getAsInt();
                }
            }
        }
        return 0;
    }

    public synchronized void playPVRChannel(final int channelID) {
        JsonObject item = new JsonObject();
        item.addProperty("channelid", channelID);

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

    public void playNotificationSoundURI(String uri) {
        playURI(uri);
    }

    public void sendSystemCommand(String command) {
        String method = "System." + command;
        socket.callMethod(method);
    }

}
