/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.volumio.internal;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.volumio.internal.mapping.VolumioCommands;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Thin wrapper around the Volumio Socket.IO (WebSocket) API.
 *
 * @see <a href="https://developers.volumio.com/api/websocket-api">Volumio WebSocket API</a>
 *
 * @author Patrick Sernetz - Initial Contribution
 * @author Chris Wohlbrecht - Adaption for openHAB 3
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
public class VolumioService {

    private final Logger logger = LoggerFactory.getLogger(VolumioService.class);

    private final Socket socket;

    private volatile boolean connected;

    /** Used to log a failed connection attempt only once per outage; retries are logged at debug level. */
    private final AtomicBoolean connectErrorLogged = new AtomicBoolean();

    public VolumioService(String protocol, String hostname, int port, int timeout)
            throws URISyntaxException, UnknownHostException {
        String uriString = String.format("%s://%s:%d", protocol, hostname, port);

        URI destUri = new URI(uriString);

        IO.Options opts = new IO.Options();
        opts.reconnection = true;
        opts.reconnectionDelay = 1000 * 5;
        opts.reconnectionDelayMax = 1000 * 60;
        opts.timeout = timeout;

        // Connection to mdns endpoint is only available after fetching ip.
        InetAddress ipaddress = InetAddress.getByName(hostname);
        logger.debug("Resolving {} to IP {}", hostname, ipaddress.getHostAddress());

        socket = IO.socket(destUri, opts);

        bindDefaultEvents(hostname);
    }

    private void bindDefaultEvents(String hostname) {
        socket.on(Socket.EVENT_CONNECTING, arg0 -> logger.debug("Trying to connect to Volumio on {}", hostname));

        socket.on(Socket.EVENT_RECONNECTING, arg0 -> logger.debug("Trying to reconnect to Volumio on {}", hostname));

        socket.on(Socket.EVENT_CONNECT_ERROR, arg0 -> {
            if (connectErrorLogged.compareAndSet(false, true)) {
                logger.warn("Could not connect to Volumio on {}, will keep retrying", hostname);
            } else {
                logger.debug("Could not connect to Volumio on {}", hostname);
            }
        });

        socket.on(Socket.EVENT_CONNECT_TIMEOUT,
                arg0 -> logger.debug("Timed out while connecting to Volumio on {}", hostname));

        socket.on(Socket.EVENT_CONNECT, arg0 -> {
            logger.debug("Connected to Volumio on {}", hostname);
            connectErrorLogged.set(false);
            connected = true;
            // Volumio does not push the current state on connect, so ask for it
            getState();
            getQueue();
        }).on(Socket.EVENT_DISCONNECT, arg0 -> {
            logger.debug("Disconnected from Volumio on {}", hostname);
            connected = false;
        });
    }

    public void connect() {
        socket.connect();
    }

    public void disconnect() {
        socket.disconnect();
    }

    public void close() {
        socket.off();
        socket.close();
    }

    public void on(String eventName, Emitter.Listener listener) {
        socket.on(eventName, listener);
    }

    public void once(String eventName, Emitter.Listener listener) {
        socket.once(eventName, listener);
    }

    public void getState() {
        socket.emit(VolumioCommands.GET_STATE);
    }

    public void getQueue() {
        socket.emit(VolumioCommands.GET_QUEUE);
    }

    public void play() {
        socket.emit(VolumioCommands.PLAY);
    }

    public void pause() {
        socket.emit(VolumioCommands.PAUSE);
    }

    public void stop() {
        socket.emit(VolumioCommands.STOP);
    }

    /**
     * Seek within the current track.
     *
     * @param seconds the position to jump to, in seconds
     */
    public void seek(int seconds) {
        socket.emit(VolumioCommands.SEEK, seconds);
    }

    public void play(Integer index) {
        socket.emit(VolumioCommands.PLAY, index);
    }

    public void next() {
        socket.emit(VolumioCommands.NEXT);
    }

    public void previous() {
        socket.emit(VolumioCommands.PREVIOUS);
    }

    public void setVolume(PercentType level) {
        socket.emit(VolumioCommands.VOLUME, level.intValue());
    }

    /**
     * Raise or lower the volume by Volumio's built-in step ({@code volume} with {@code +} or {@code -}).
     */
    public void changeVolume(boolean increase) {
        socket.emit(VolumioCommands.VOLUME, increase ? "+" : "-");
    }

    public void setMute(boolean mute) {
        socket.emit(mute ? VolumioCommands.MUTE : VolumioCommands.UNMUTE, "");
    }

    public void shutdown() {
        socket.emit(VolumioCommands.SHUTDOWN);
    }

    public void reboot() {
        socket.emit(VolumioCommands.REBOOT);
    }

    public void playPlaylist(String playlistName) {
        emitJson(VolumioCommands.PLAY_PLAYLIST, "name", playlistName);
    }

    public void clearQueue() {
        socket.emit(VolumioCommands.CLEAR_QUEUE);
    }

    public void setRandom(boolean val) {
        emitJson(VolumioCommands.RANDOM, "value", val);
    }

    public void setRepeat(boolean val) {
        emitJson(VolumioCommands.REPEAT, "value", val);
    }

    public void playFavorites(String favoriteName) {
        emitJson(VolumioCommands.PLAY_FAVOURITES, "name", favoriteName);
    }

    /**
     * Play a radio station from volumio´s Radio Favourites identifed by
     * its index.
     */
    public void playRadioFavourite(final Integer index) {
        logger.debug("socket.emit({})", VolumioCommands.PLAY_RADIO_FAVOURITES);

        socket.once("pushPlayRadioFavourites", arg -> play(index));

        socket.emit(VolumioCommands.PLAY_RADIO_FAVOURITES);
    }

    public void addPlay(String uri, String title, String serviceType) {
        emitItem(VolumioCommands.ADD_PLAY, uri, title, serviceType);
    }

    public void replacePlay(String uri, String title, String serviceType) {
        emitItem(VolumioCommands.REPLACE_AND_PLAY, uri, title, serviceType);
    }

    public void addToQueue(String uri) {
        emitJson(VolumioCommands.ADD_TO_QUEUE, "uri", uri);
    }

    /**
     * @param index position in the queue, 0 for the first track
     */
    public void removeFromQueue(int index) {
        emitJson(VolumioCommands.REMOVE_FROM_QUEUE, "value", index);
    }

    /** Add all tracks of a playlist to the queue, without replacing it. */
    public void enqueuePlaylist(String playlistName) {
        emitJson(VolumioCommands.ENQUEUE, "name", playlistName);
    }

    public void addToFavourites(String uri, String title, String serviceType) {
        emitItem(VolumioCommands.ADD_TO_FAVOURITES, uri, title, serviceType);
    }

    public void removeFromFavourites(String uri, String serviceType) {
        try {
            JSONObject item = new JSONObject();
            item.put("uri", uri);
            item.put("service", serviceType);
            socket.emit(VolumioCommands.REMOVE_FROM_FAVOURITES, item);
        } catch (JSONException e) {
            logger.debug("Could not build payload for {}: {}", VolumioCommands.REMOVE_FROM_FAVOURITES, e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Execute a system command. The command is matched case-insensitively.
     *
     * @return false if the command is not known
     */
    public boolean sendSystemCommand(String command) {
        logger.debug("Sending system command: {}", command);
        if (VolumioCommands.SHUTDOWN.equalsIgnoreCase(command)) {
            shutdown();
            return true;
        } else if (VolumioCommands.REBOOT.equalsIgnoreCase(command)) {
            reboot();
            return true;
        }
        return false;
    }

    private void emitItem(String event, String uri, String title, String serviceType) {
        try {
            JSONObject item = new JSONObject();
            item.put("uri", uri);
            item.put("title", title);
            item.put("service", serviceType);
            socket.emit(event, item);
        } catch (JSONException e) {
            logger.debug("Could not build payload for {}: {}", event, e.getMessage());
        }
    }

    private void emitJson(String event, String key, Object value) {
        try {
            JSONObject item = new JSONObject();
            item.put(key, value);
            socket.emit(event, item);
        } catch (JSONException e) {
            logger.debug("Could not build payload for {}: {}", event, e.getMessage());
        }
    }
}
