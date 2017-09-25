/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang.StringUtils;
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
 *
 */
public class KodiConnection implements KodiClientSocketEventListener {

    private final Logger logger = LoggerFactory.getLogger(KodiConnection.class);

    private static final int VOLUMESTEP = 10;

    private URI wsUri;
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

    public synchronized void connect(String hostName, int port, ScheduledExecutorService scheduler) {
        try {
            close();
            wsUri = new URI(String.format("ws://%s:%d/jsonrpc", hostName, port));
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
        int activePlayer = getActivePlayer();

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.addProperty("to", "next");
        socket.callMethod("Player.GoTo", params);

        updatePlayerStatus();
    }

    public synchronized void playerPrevious() {
        int activePlayer = getActivePlayer();

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.addProperty("to", "previous");
        socket.callMethod("Player.GoTo", params);

        updatePlayerStatus();
    }

    public synchronized void playerRewind() {
        int activePlayer = getActivePlayer();

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.addProperty("speed", -2);
        socket.callMethod("Player.SetSpeed", params);

        updatePlayerStatus();
    }

    public synchronized void playerFastForward() {
        int activePlayer = getActivePlayer();

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.addProperty("speed", 2);
        socket.callMethod("Player.SetSpeed", params);

        updatePlayerStatus();
    }

    public synchronized void increaseVolume() {
        this.volume += VOLUMESTEP;
        JsonObject params = new JsonObject();
        params.addProperty("volume", volume);
        socket.callMethod("Application.SetVolume", params);
    }

    public synchronized void decreaseVolume() {
        this.volume -= VOLUMESTEP;
        JsonObject params = new JsonObject();
        params.addProperty("volume", volume);
        socket.callMethod("Application.SetVolume", params);
    }

    public synchronized void setVolume(int volume) {
        this.volume = volume;
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

    private void updateFanartUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }

        /*
         * try {
         *
         * String encodedURL = URLEncoder.encode(imagePath, "UTF-8");
         * String decodedURL = URLDecoder.decode(imagePath, "UTF-8");
         *
         * JsonObject params = new JsonObject();
         * params.addProperty("path", "");
         * JsonElement response = socket.callMethod("Files.PrepareDownload", params);
         *
         * } catch (Exception e) {
         * logger.error("updateFanartUrl error", e);
         * }
         */
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

                try {
                    listener.updateAlbum(album);
                    listener.updateTitle(title);
                    listener.updateShowTitle(showTitle);
                    listener.updateArtist(artist);
                    listener.updateMediaType(mediaType);
                    listener.updatePVRChannel(channel);
                } catch (Exception e) {
                    logger.error("Event listener invoking error", e);
                }

                if (item.has("fanart")) {
                    updateFanartUrl(item.get("fanart").getAsString());
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
