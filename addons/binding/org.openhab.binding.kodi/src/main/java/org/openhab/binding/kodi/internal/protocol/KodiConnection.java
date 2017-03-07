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
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.kodi.internal.KodiEventListener;
import org.openhab.binding.kodi.internal.KodiEventListener.KodiState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * KodiConnection provides an api for accessing a kodi device.
 *
 * @author Paul Frank
 *
 */
public class KodiConnection implements KodiClientSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(KodiConnection.class);

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
            wsUri = new URI(String.format("ws://%s:%d/jsonrpc", hostName, port));
            socket = new KodiClientSocket(this, wsUri, scheduler);
            socket.open();
        } catch (Throwable t) {
            logger.error("exception during connect to {}", wsUri.toString(), t);
        }
    }

    private int getActivePlayer() {
        JsonElement response = socket.callMethod("Player.GetActivePlayers");
        if (response != null) {
            boolean playing = response.isJsonArray() && response.getAsJsonArray().size() > 0;
            if (playing) {
                JsonObject player0 = response.getAsJsonArray().get(0).getAsJsonObject();
                return player0.get("playerid").getAsInt();
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
        params.addProperty("speed", "-1");
        socket.callMethod("Player.SetSpeed", params);

        updatePlayerStatus();

    }

    public synchronized void playerFastForward() {
        int activePlayer = getActivePlayer();

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.addProperty("speed", "2");
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

        JsonObject result = response.getAsJsonObject();
        return result.get("speed").getAsInt();
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
        requestPlayerUpdate(activePlayer, true);
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

    private void requestPlayerUpdate(int activePlayer, boolean updateMediaType) {
        final String[] properties = { "title", "album", "artist", "director", "thumbnail", "file", "fanart",
                "showtitle", "streamdetails" };

        JsonObject params = new JsonObject();
        params.addProperty("playerid", activePlayer);
        params.add("properties", getJsonArray(properties));
        JsonElement response = socket.callMethod("Player.GetItem", params);

        JsonObject item = ((JsonObject) response).get("item").getAsJsonObject();

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

        String artist = "";
        if (mediaType.equals("movie")) {

            artist = convertFromArray(item.get("director").getAsJsonArray());
        } else {
            if (item.has("artist")) {
                artist = convertFromArray(item.get("artist").getAsJsonArray());
            }
        }

        try {
            listener.updateAlbum(album);
            listener.updateTitle(title);
            listener.updateShowTitle(showTitle);
            listener.updateArtist(artist);
            if (updateMediaType) {
                listener.updateMediaType(mediaType);
            }
        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }

        if (item.has("fanart")) {
            updateFanartUrl(item.get("fanart").getAsString());
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
                listener.updateArtist("");
                listener.updateTitle("");
                listener.updateMediaType("");
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

            if (data.has("item")) {
                JsonObject item = data.get("item").getAsJsonObject();
                String mediaType = item.get("type").getAsString();
                listener.updateMediaType(mediaType);
            }
            requestPlayerUpdate(playerId, false);
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
            logger.warn("Unkown Player Message: {}", method);
        }
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
            logger.debug("Unknown event from kodi {}: {}", method, json.toString());
        }
    }

    private void processSystemStateChanged(String method, JsonObject json) {
        if ("System.OnQuit".equals(method) || "System.OnSleep".equals(method) || "System.OnRestart".equals(method)) {
            try {
                listener.updateConnectionState(false);
            } catch (Exception e) {
                logger.error("Event listener invoking error", e);
            }
        }
    }

    private void processScreensaverStateChanged(String method, JsonObject json) {
        if ("GUI.OnScreensaverDeactivated".equals(method)) {
            updateScreenSaverStatus(false);
        } else if ("GUI.OnScreensaverActivated".equals(method)) {
            updateScreenSaverStatus(true);
        }
    }

    private void updateScreenSaverStatus(boolean screenSaverActive) {
        try {
            listener.updateScreenSaverState(screenSaverActive);
        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }

    public synchronized void close() {
        socket = null;
    }

    public synchronized void updateVolume() {
        if (socket.isConnected()) {
            String[] props = { "volume", "version", "name", "muted" };

            JsonObject params = new JsonObject();
            params.add("properties", getJsonArray(props));

            JsonElement response = socket.callMethod("Application.GetProperties", params);

            if (response instanceof JsonObject) {
                JsonObject result = (JsonObject) response;
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

    public synchronized void showNotification(String message) {
        JsonObject params = new JsonObject();
        params.addProperty("title", "openHAB");
        params.addProperty("message", message);
        socket.callMethod("GUI.ShowNotification", params);
    }

    public boolean checkConnection() {
        if (!socket.isConnected()) {
            logger.debug("checkConnection: try to connect to kodi {}", wsUri.toString());
            try {
                socket.open();
                return socket.isConnected();
            } catch (Throwable t) {
                logger.error("exception during connect to {}", wsUri.toString(), t);
                try {
                    socket.close();
                } catch (Exception e) {
                }
                return false;
            }
        } else {
            // Ping kodi with the get version command. This prevents the idle timeout on the websocket
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
                JsonObject result = (JsonObject) response;
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