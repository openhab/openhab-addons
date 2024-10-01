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
package org.openhab.binding.volumio.internal;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

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
 * @author Patrick Sernetz - Initial Contribution
 * @author Chris Wohlbrecht - Adaption for openHAB 3
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
public class VolumioService {

    private final Logger logger = LoggerFactory.getLogger(VolumioService.class);

    private final Socket socket;

    private boolean connected;

    public VolumioService(String protocol, String hostname, int port, int timeout)
            throws URISyntaxException, UnknownHostException {
        String uriString = String.format("%s://%s:%d", protocol, hostname, port);

        URI destUri = new URI(uriString);

        IO.Options opts = new IO.Options();
        opts.reconnection = true;
        opts.reconnectionDelay = 1000 * 30;
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

        socket.on(Socket.EVENT_CONNECT_ERROR, arg0 -> logger.error("Could not connect to Volumio on {}", hostname));

        socket.on(Socket.EVENT_CONNECT_TIMEOUT,
                arg0 -> logger.error("Timedout while conntecting to Volumio on {}", hostname));

        socket.on(Socket.EVENT_CONNECT, arg0 -> {
            logger.info("Connected to Volumio on {}", hostname);
            setConnected(true);

        }).on(Socket.EVENT_DISCONNECT, arg0 -> {
            logger.warn("Disconnected from Volumio on {}", hostname);
            setConnected(false);
        });
    }

    public void connect() throws InterruptedException {
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

    public void play() {
        socket.emit(VolumioCommands.PLAY);
    }

    public void pause() {
        socket.emit(VolumioCommands.PAUSE);
    }

    public void stop() {
        socket.emit(VolumioCommands.STOP);
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

    public void shutdown() {
        socket.emit(VolumioCommands.SHUTDOWN);
    }

    public void reboot() {
        socket.emit(VolumioCommands.REBOOT);
    }

    public void playPlaylist(String playlistName) {
        JSONObject item = new JSONObject();

        try {
            item.put("name", playlistName);

            socket.emit(VolumioCommands.PLAY_PLAYLIST, item);
        } catch (JSONException e) {
            logger.error("The following error occurred {}", e.getMessage());
        }
    }

    public void clearQueue() {
        socket.emit(VolumioCommands.CLEAR_QUEUE);
    }

    public void setRandom(boolean val) {
        JSONObject item = new JSONObject();

        try {
            item.put("value", val);

            socket.emit(VolumioCommands.RANDOM, item);
        } catch (JSONException e) {
            logger.error("The following error occurred {}", e.getMessage());
        }
    }

    public void setRepeat(boolean val) {
        JSONObject item = new JSONObject();

        try {
            item.put("value", val);

            socket.emit(VolumioCommands.REPEAT, item);
        } catch (JSONException e) {
            logger.error("The following error occurred {}", e.getMessage());
        }
    }

    public void playFavorites(String favoriteName) {
        JSONObject item = new JSONObject();

        try {
            item.put("name", favoriteName);

            socket.emit(VolumioCommands.PLAY_FAVOURITES, item);
        } catch (JSONException e) {
            logger.error("The following error occurred {}", e.getMessage());
        }
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

    public void playURI(String uri) {
        JSONObject item = new JSONObject();
        logger.debug("PlayURI: {}", uri);
        try {
            item.put("uri", uri);

            socket.emit(VolumioCommands.PLAY, uri);
        } catch (JSONException e) {
            logger.error("The following error occurred {}", e.getMessage());
        }
    }

    public void addPlay(String uri, String title, String serviceType) {
        JSONObject item = new JSONObject();

        try {
            item.put("uri", uri);
            item.put("title", title);
            item.put("service", serviceType);

            socket.emit(VolumioCommands.ADD_PLAY, item);
        } catch (JSONException e) {
            logger.error("The following error occurred {}", e.getMessage());
        }
    }

    public void replacePlay(String uri, String title, String serviceType) {
        JSONObject item = new JSONObject();

        try {
            item.put("uri", uri);
            item.put("title", title);
            item.put("service", serviceType);

            socket.emit(VolumioCommands.REPLACE_AND_PLAY, item);
        } catch (JSONException e) {
            logger.error("The following error occurred {}", e.getMessage());
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean status) {
        this.connected = status;
    }

    public void sendSystemCommand(String string) {
        logger.warn("Jukebox Command: {}", string);
        switch (string) {
            case VolumioCommands.SHUTDOWN:
                shutdown();
                break;
            case VolumioCommands.REBOOT:
                reboot();
                break;
            default:
                break;
        }
    }
}
