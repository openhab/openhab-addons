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
package org.openhab.binding.squeezebox.internal.handler;

import static org.openhab.binding.squeezebox.internal.SqueezeBoxBindingConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.squeezebox.internal.config.SqueezeBoxServerConfig;
import org.openhab.binding.squeezebox.internal.dto.ButtonDTO;
import org.openhab.binding.squeezebox.internal.dto.ButtonDTODeserializer;
import org.openhab.binding.squeezebox.internal.dto.ButtonsDTO;
import org.openhab.binding.squeezebox.internal.dto.StatusResponseDTO;
import org.openhab.binding.squeezebox.internal.model.Favorite;
import org.openhab.core.io.net.http.HttpRequestBuilder;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Handles connection and event handling to a SqueezeBox Server.
 *
 * @author Markus Wolters - Initial contribution
 * @author Ben Jones - ?
 * @author Dan Cunningham - OH2 port
 * @author Daniel Walters - Fix player discovery when player name contains spaces
 * @author Mark Hilbush - Improve reconnect logic. Improve player status updates.
 * @author Mark Hilbush - Implement AudioSink and notifications
 * @author Mark Hilbush - Added duration channel
 * @author Mark Hilbush - Added login/password authentication for LMS
 * @author Philippe Siem - Improve refresh of cover art url,remote title, artist, album, genre, year.
 * @author Patrik Gfeller - Support for mixer volume message added
 * @author Mark Hilbush - Get favorites from LMS; update channel and send to players
 * @author Mark Hilbush - Add like/unlike functionality
 */
public class SqueezeBoxServerHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxServerHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(SQUEEZEBOXSERVER_THING_TYPE);

    // time in seconds to try to reconnect
    private static final int RECONNECT_TIME = 60;

    // the value by which the volume is changed by each INCREASE or
    // DECREASE-Event
    private static final int VOLUME_CHANGE_SIZE = 5;
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String CHANNEL_CONFIG_QUOTE_LIST = "quoteList";

    private static final String JSONRPC_STATUS_REQUEST = "{\"id\":1,\"method\":\"slim.request\",\"params\":[\"@@MAC@@\",[\"status\",\"-\",\"tags:yagJlNKjcB\"]]}";

    private List<SqueezeBoxPlayerEventListener> squeezeBoxPlayerListeners = Collections
            .synchronizedList(new ArrayList<>());

    private Map<String, SqueezeBoxPlayer> players = Collections.synchronizedMap(new HashMap<>());

    // client socket and listener thread
    private Socket clientSocket;
    private SqueezeServerListener listener;
    private Future<?> reconnectFuture;

    private String host;

    private int cliport;

    private int webport;

    private String userId;

    private String password;

    private final Gson gson = new GsonBuilder().registerTypeAdapter(ButtonDTO.class, new ButtonDTODeserializer())
            .create();
    private String jsonRpcUrl;
    private String basicAuthorization;

    public SqueezeBoxServerHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("initializing server handler for thing {}", getThing().getUID());
        scheduler.submit(this::connect);
    }

    @Override
    public void dispose() {
        logger.debug("disposing server handler for thing {}", getThing().getUID());
        cancelReconnect();
        disconnect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Checks if we have a connection to the Server
     *
     * @return
     */
    public synchronized boolean isConnected() {
        if (clientSocket == null) {
            return false;
        }

        // NOTE: isConnected() returns true once a connection is made and will
        // always return true even after the socket is closed
        // http://stackoverflow.com/questions/10163358/
        return clientSocket.isConnected() && !clientSocket.isClosed();
    }

    public void mute(String mac) {
        sendCommand(mac + " mixer muting 1");
    }

    public void unMute(String mac) {
        sendCommand(mac + " mixer muting 0");
    }

    public void powerOn(String mac) {
        sendCommand(mac + " power 1");
    }

    public void powerOff(String mac) {
        sendCommand(mac + " power 0");
    }

    public void syncPlayer(String mac, String player2mac) {
        sendCommand(mac + " sync " + player2mac);
    }

    public void unSyncPlayer(String mac) {
        sendCommand(mac + " sync -");
    }

    public void play(String mac) {
        sendCommand(mac + " play");
    }

    public void playUrl(String mac, String url) {
        sendCommand(mac + " playlist play " + url);
    }

    public void pause(String mac) {
        sendCommand(mac + " pause 1");
    }

    public void unPause(String mac) {
        sendCommand(mac + " pause 0");
    }

    public void stop(String mac) {
        sendCommand(mac + " stop");
    }

    public void prev(String mac) {
        sendCommand(mac + " playlist index -1");
    }

    public void next(String mac) {
        sendCommand(mac + " playlist index +1");
    }

    public void clearPlaylist(String mac) {
        sendCommand(mac + " playlist clear");
    }

    public void deletePlaylistItem(String mac, int playlistIndex) {
        sendCommand(mac + " playlist delete " + playlistIndex);
    }

    public void playPlaylistItem(String mac, int playlistIndex) {
        sendCommand(mac + " playlist index " + playlistIndex);
    }

    public void addPlaylistItem(String mac, String url) {
        addPlaylistItem(mac, url, null);
    }

    public void addPlaylistItem(String mac, String url, String title) {
        StringBuilder playlistCommand = new StringBuilder();
        playlistCommand.append(mac).append(" playlist add ").append(url);
        if (title != null) {
            playlistCommand.append(" ").append(title);
        }
        sendCommand(playlistCommand.toString());
    }

    public void setPlayingTime(String mac, int time) {
        sendCommand(mac + " time " + time);
    }

    public void setRepeatMode(String mac, int repeatMode) {
        sendCommand(mac + " playlist repeat " + repeatMode);
    }

    public void setShuffleMode(String mac, int shuffleMode) {
        sendCommand(mac + " playlist shuffle " + shuffleMode);
    }

    public void volumeUp(String mac, int currentVolume) {
        setVolume(mac, currentVolume + VOLUME_CHANGE_SIZE);
    }

    public void volumeDown(String mac, int currentVolume) {
        setVolume(mac, currentVolume - VOLUME_CHANGE_SIZE);
    }

    public void setVolume(String mac, int volume) {
        int newVolume = volume;
        newVolume = Math.min(100, newVolume);
        newVolume = Math.max(0, newVolume);
        sendCommand(mac + " mixer volume " + newVolume);
    }

    public void showString(String mac, String line) {
        showString(mac, line, 5);
    }

    public void showString(String mac, String line, int duration) {
        sendCommand(mac + " show line1:" + line + " duration:" + duration);
    }

    public void showStringHuge(String mac, String line) {
        showStringHuge(mac, line, 5);
    }

    public void showStringHuge(String mac, String line, int duration) {
        sendCommand(mac + " show line1:" + line + " font:huge duration:" + duration);
    }

    public void showStrings(String mac, String line1, String line2) {
        showStrings(mac, line1, line2, 5);
    }

    public void showStrings(String mac, String line1, String line2, int duration) {
        sendCommand(mac + " show line1:" + line1 + " line2:" + line2 + " duration:" + duration);
    }

    public void playFavorite(String mac, String favorite) {
        sendCommand(mac + " favorites playlist play item_id:" + favorite);
    }

    public void rate(String mac, String rateCommand) {
        if (rateCommand != null) {
            sendCommand(mac + " " + rateCommand);
        }
    }

    public void sleep(String mac, Duration sleepDuration) {
        sendCommand(mac + " sleep " + sleepDuration.toSeconds());
    }

    /**
     * Send a generic command to a given player
     *
     * @param mac
     * @param command
     */
    public void playerCommand(String mac, String command) {
        sendCommand(mac + " " + command);
    }

    /**
     * Ask for player list
     */
    public void requestPlayers() {
        sendCommand("players 0");
    }

    /**
     * Ask for favorites list
     */
    public void requestFavorites() {
        sendCommand("favorites items 0 100");
    }

    /**
     * Login to server
     */
    public void login() {
        if (userId.isEmpty()) {
            return;
        }
        // Create basic auth string for jsonrpc interface
        basicAuthorization = new String(
                Base64.getEncoder().encode((userId + ":" + password).getBytes(StandardCharsets.UTF_8)));
        logger.debug("Logging into Squeeze Server using userId={}", userId);
        sendCommand("login " + userId + " " + password);
    }

    /**
     * Send a command to the Squeeze Server.
     */
    private synchronized void sendCommand(String command) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        if (!isConnected()) {
            logger.debug("no connection to squeeze server when trying to send command, returning...");
            return;
        }

        logger.debug("Sending command: {}", sanitizeCommand(command));
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            writer.write(command + NEW_LINE);
            writer.flush();
        } catch (IOException e) {
            logger.error("Error while sending command to Squeeze Server ({}) ", sanitizeCommand(command), e);
        }
    }

    /*
     * Remove password from login command to prevent it from being logged
     */
    String sanitizeCommand(String command) {
        String sanitizedCommand = command;
        if (command.startsWith("login")) {
            sanitizedCommand = command.replace(password, "**********");
        }
        return sanitizedCommand;
    }

    /**
     * Connects to a SqueezeBox Server
     */
    private void connect() {
        logger.trace("attempting to get a connection to the server");
        disconnect();
        SqueezeBoxServerConfig config = getConfigAs(SqueezeBoxServerConfig.class);
        this.host = config.ipAddress;
        this.cliport = config.cliport;
        this.webport = config.webport;
        this.userId = config.userId;
        this.password = config.password;

        if (host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "host is not set");
            return;
        }
        // Create URL for jsonrpc interface
        jsonRpcUrl = String.format("http://%s:%d/jsonrpc.js", host, webport);

        try {
            clientSocket = new Socket(host, cliport);
        } catch (IOException e) {
            logger.debug("unable to open socket to server: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            scheduleReconnect();
            return;
        }

        try {
            listener = new SqueezeServerListener();
            listener.start();
            logger.debug("listener connection started to server {}:{}", host, cliport);
        } catch (IllegalThreadStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        // Mark the server ONLINE. bridgeStatusChanged will cause the players to come ONLINE
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Disconnects from a SqueezeBox Server
     */
    private void disconnect() {
        try {
            if (listener != null) {
                listener.terminate();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            logger.trace("Error attempting to disconnect from Squeeze Server", e);
            return;
        } finally {
            clientSocket = null;
            listener = null;
        }
        players.clear();
        logger.trace("Squeeze Server connection stopped.");
    }

    private class SqueezeServerListener extends Thread {
        private boolean terminate = false;

        public SqueezeServerListener() {
            super("Squeeze Server Listener");
        }

        public void terminate() {
            logger.debug("setting squeeze server listener terminate flag");
            this.terminate = true;
        }

        @Override
        public void run() {
            BufferedReader reader = null;
            boolean endOfStream = false;
            ScheduledFuture<?> requestFavoritesJob = null;

            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                login();
                updateStatus(ThingStatus.ONLINE);
                requestPlayers();
                requestFavoritesJob = scheduleRequestFavorites();
                sendCommand("listen 1");

                String message = null;
                while (!terminate && (message = reader.readLine()) != null) {
                    // Message is very long and frequent; only show when running at trace level logging
                    logger.trace("Message received: {}", message);

                    // Fix for some third-party apps that are sending "subscribe playlist"
                    if (message.startsWith("listen 1") || message.startsWith("subscribe playlist")) {
                        continue;
                    }

                    if (message.startsWith("players 0")) {
                        handlePlayersList(message);
                    } else if (message.startsWith("favorites")) {
                        handleFavorites(message);
                    } else {
                        handlePlayerUpdate(message);
                    }
                }
                if (message == null) {
                    endOfStream = true;
                }
            } catch (IOException e) {
                if (!terminate) {
                    logger.warn("failed to read line from squeeze server socket: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    scheduleReconnect();
                }
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // ignore
                    }
                    reader = null;
                }
            }

            // check for end of stream from readLine
            if (endOfStream && !terminate) {
                logger.info("end of stream received from socket during readLine");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "end of stream on socket read");
                scheduleReconnect();
            }
            if (requestFavoritesJob != null && !requestFavoritesJob.isDone()) {
                requestFavoritesJob.cancel(true);
                logger.debug("Canceled request favorites job");
            }
            logger.debug("Squeeze Server listener exiting.");
        }

        private String decode(String raw) {
            return URLDecoder.decode(raw, StandardCharsets.UTF_8);
        }

        private String encode(String raw) {
            return URLEncoder.encode(raw, StandardCharsets.UTF_8);
        }

        @NonNullByDefault
        private class KeyValue {
            final String key;
            final String value;

            public KeyValue(String key, String value) {
                this.key = key;
                this.value = value;
            }
        }

        private List<KeyValue> decodeKeyValueResponse(String[] response) {
            final List<KeyValue> keysAndValues = new ArrayList<>();
            if (response != null) {
                for (String line : response) {
                    final String decoded = decode(line);
                    int colonPos = decoded.indexOf(":");
                    if (colonPos < 0) {
                        continue;
                    }
                    keysAndValues.add(new KeyValue(decoded.substring(0, colonPos), decoded.substring(colonPos + 1)));
                }
            }
            return keysAndValues;
        }

        private void handlePlayersList(String message) {
            final Set<String> connectedPlayers = new HashSet<>();

            // Split out players
            String[] playersList = message.split("playerindex\\S*\\s");
            for (String playerParams : playersList) {
                // For each player, split out parameters and decode parameter
                final Map<String, String> keysAndValues = decodeKeyValueResponse(playerParams.split("\\s")).stream()
                        .collect(Collectors.toMap(kv -> kv.key, kv -> kv.value));
                final String macAddress = keysAndValues.get("playerid");

                // if none found then ignore this set of params
                if (macAddress == null) {
                    continue;
                }

                final SqueezeBoxPlayer player = new SqueezeBoxPlayer(macAddress, keysAndValues.get("name"),
                        keysAndValues.get("ip"), keysAndValues.get("model"), keysAndValues.get("uuid"));
                if ("1".equals(keysAndValues.get("connected"))) {
                    connectedPlayers.add(macAddress);
                }

                // Save player if we haven't seen it yet
                if (!players.containsKey(macAddress)) {
                    players.put(macAddress, player);
                    updatePlayer(listener -> listener.playerAdded(player));
                    // tell the server we want to subscribe to player updates
                    sendCommand(player.macAddress + " status - 1 subscribe:10 tags:yagJlNKjcA");
                }
            }
            for (final SqueezeBoxPlayer player : players.values()) {
                final boolean connected = connectedPlayers.contains(player.macAddress);
                updatePlayer(listener -> listener.connectedStateChangeEvent(player.macAddress, connected));
            }
        }

        private void handlePlayerUpdate(String message) {
            String[] messageParts = message.split("\\s");
            if (messageParts.length < 2) {
                logger.warn("Invalid message - expecting at least 2 parts. Ignoring.");
                return;
            }

            final String mac = decode(messageParts[0]);

            // get the message type
            String messageType = messageParts[1];
            switch (messageType) {
                case "client":
                    handleClientMessage(mac, messageParts);
                    break;
                case "status":
                    handleStatusMessage(mac, messageParts);
                    break;
                case "playlist":
                    handlePlaylistMessage(mac, messageParts);
                    break;
                case "prefset":
                    handlePrefsetMessage(mac, messageParts);
                    break;
                case "mixer":
                    handleMixerMessage(mac, messageParts);
                    break;
                case "ir":
                    final String ircode = messageParts[2];
                    updatePlayer(listener -> listener.irCodeChangeEvent(mac, ircode));
                    break;
                default:
                    logger.trace("Unhandled player update message type '{}'.", messageType);
            }
        }

        private void handleMixerMessage(String mac, String[] messageParts) {
            if (messageParts.length < 4) {
                return;
            }
            String action = messageParts[2];

            switch (action) {
                case "volume":
                    String volumeStringValue = decode(messageParts[3]);
                    updatePlayer(listener -> {
                        try {
                            int volume = Math.round(Float.parseFloat(volumeStringValue));

                            // Check if we received a relative volume change, or an absolute
                            // volume value.
                            if (volumeStringValue.contains("+") || (volumeStringValue.contains("-"))) {
                                listener.relativeVolumeChangeEvent(mac, volume);
                            } else {
                                listener.absoluteVolumeChangeEvent(mac, volume);
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("Unable to parse volume [{}] received from mixer message.", volumeStringValue,
                                    e);
                        }
                    });
                    break;
                default:
                    logger.trace("Unhandled mixer message type '{}'", Arrays.toString(messageParts));

            }
        }

        private void handleClientMessage(final String mac, String[] messageParts) {
            if (messageParts.length < 3) {
                return;
            }

            String action = messageParts[2];
            final boolean connected;

            if ("new".equals(action) || "reconnect".equals(action)) {
                connected = true;
            } else if ("disconnect".equals(action) || "forget".equals(action)) {
                connected = false;
            } else {
                logger.trace("Unhandled client message type '{}'", Arrays.toString(messageParts));
                return;
            }

            updatePlayer(listener -> listener.connectedStateChangeEvent(mac, connected));
        }

        private void handleStatusMessage(final String mac, String[] messageParts) {
            String remoteTitle = "", artist = "", album = "", genre = "", year = "", albumArtist = "", trackArtist = "",
                    band = "", composer = "", conductor = "";
            boolean coverart = false;
            String coverid = null;
            String artworkUrl = null;

            for (KeyValue entry : decodeKeyValueResponse(messageParts)) {
                try {
                    // Parameter Power
                    if ("power".equals(entry.key)) {
                        final boolean power = "1".equals(entry.value);
                        updatePlayer(listener -> listener.powerChangeEvent(mac, power));
                    }
                    // Parameter Volume
                    else if ("mixer volume".equals(entry.key)) {
                        final int volume = (int) Double.parseDouble(entry.value);
                        updatePlayer(listener -> listener.absoluteVolumeChangeEvent(mac, volume));
                    }
                    // Parameter Mode
                    else if ("mode".equals(entry.key)) {
                        updatePlayer(listener -> listener.modeChangeEvent(mac, entry.value));
                    }
                    // Parameter Playing Time
                    else if ("time".equals(entry.key) && !"N/A".equals(entry.value)) {
                        final int time = (int) Double.parseDouble(entry.value);
                        updatePlayer(listener -> listener.currentPlayingTimeEvent(mac, time));
                    }
                    // Parameter duration
                    else if ("duration".equals(entry.key)) {
                        final int duration = (int) Double.parseDouble(entry.value);
                        updatePlayer(listener -> listener.durationEvent(mac, duration));
                    }
                    // Parameter Playing Playlist Index
                    else if ("playlist_cur_index".equals(entry.key)) {
                        final int index = (int) Double.parseDouble(entry.value);
                        updatePlayer(listener -> listener.currentPlaylistIndexEvent(mac, index));
                    }
                    // Parameter Playlist Number Tracks
                    else if ("playlist_tracks".equals(entry.key)) {
                        final int track = (int) Double.parseDouble(entry.value);
                        updatePlayer(listener -> listener.numberPlaylistTracksEvent(mac, track));
                    }
                    // Parameter Playlist Repeat Mode
                    else if ("playlist repeat".equals(entry.key)) {
                        final int repeat = (int) Double.parseDouble(entry.value);
                        updatePlayer(listener -> listener.currentPlaylistRepeatEvent(mac, repeat));
                    }
                    // Parameter Playlist Shuffle Mode
                    else if ("playlist shuffle".equals(entry.key)) {
                        final int shuffle = (int) Double.parseDouble(entry.value);
                        updatePlayer(listener -> listener.currentPlaylistShuffleEvent(mac, shuffle));
                    }
                    // Parameter Title
                    else if ("title".equals(entry.key)) {
                        updatePlayer(listener -> listener.titleChangeEvent(mac, entry.value));
                    }
                    // Parameter Remote Title (radio)
                    else if ("remote_title".equals(entry.key)) {
                        remoteTitle = entry.value;
                    }
                    // Parameter Artist
                    else if ("artist".equals(entry.key)) {
                        artist = entry.value;
                    }
                    // Parameter Album
                    else if ("album".equals(entry.key)) {
                        album = entry.value;
                    }
                    // Parameter Genre
                    else if ("genre".equals(entry.key)) {
                        genre = entry.value;
                    }
                    // Parameter Album Artist
                    else if ("albumartist".equals(entry.key)) {
                        albumArtist = entry.value;
                    }
                    // Parameter Track Artist
                    else if ("trackartist".equals(entry.key)) {
                        trackArtist = entry.value;
                    }
                    // Parameter Band
                    else if ("band".equals(entry.key)) {
                        band = entry.value;
                    }
                    // Parameter Composer
                    else if ("composer".equals(entry.key)) {
                        composer = entry.value;
                    }
                    // Parameter Conductor
                    else if ("conductor".equals(entry.key)) {
                        conductor = entry.value;
                    }
                    // Parameter Year
                    else if ("year".equals(entry.key)) {
                        year = entry.value;
                    }
                    // Parameter artwork_url contains url to cover art
                    else if ("artwork_url".equals(entry.key)) {
                        artworkUrl = entry.value;
                    }
                    // When coverart is "1" coverid will contain a unique coverart id
                    else if ("coverart".equals(entry.key)) {
                        coverart = "1".equals(entry.value);
                    }
                    // Id for covert art (only valid when coverart is "1")
                    else if ("coverid".equals(entry.key)) {
                        coverid = entry.value;
                    } else {
                        // Added to be able to see additional status message types
                        logger.trace("Unhandled status message type '{}' (value '{}')", entry.key, entry.value);
                    }
                } catch (NumberFormatException e) {
                    // Skip this key/value
                    logger.debug("Cannot parse number in status message: key '{}', value '{}'", entry.key, entry.value);
                }
            }

            final String finalUrl = constructCoverArtUrl(mac, coverart, coverid, artworkUrl);
            final String finalRemoteTitle = remoteTitle;
            final String finalArtist = artist;
            final String finalAlbum = album;
            final String finalGenre = genre;
            final String finalYear = year;
            final String finalAlbumArtist = albumArtist;
            final String finalTrackArtist = trackArtist;
            final String finalBand = band;
            final String finalComposer = composer;
            final String finalConductor = conductor;

            updatePlayer(listener -> {
                listener.coverArtChangeEvent(mac, finalUrl);
                listener.remoteTitleChangeEvent(mac, finalRemoteTitle);
                listener.artistChangeEvent(mac, finalArtist);
                listener.albumChangeEvent(mac, finalAlbum);
                listener.genreChangeEvent(mac, finalGenre);
                listener.yearChangeEvent(mac, finalYear);
                listener.albumArtistChangeEvent(mac, finalAlbumArtist);
                listener.trackArtistChangeEvent(mac, finalTrackArtist);
                listener.bandChangeEvent(mac, finalBand);
                listener.composerChangeEvent(mac, finalComposer);
                listener.conductorChangeEvent(mac, finalConductor);
            });
        }

        private String constructCoverArtUrl(String mac, boolean coverart, String coverid, String artwork_url) {
            String hostAndPort;
            if (!userId.isEmpty()) {
                hostAndPort = "http://" + encode(userId) + ":" + encode(password) + "@" + host + ":" + webport;
            } else {
                hostAndPort = "http://" + host + ":" + webport;
            }

            // Default to using the convenience artwork URL (should be rare)
            String url = hostAndPort + "/music/current/cover.jpg?player=" + encode(mac);

            // If additional artwork info provided, use that instead
            if (coverart) {
                if (coverid != null) {
                    // Typically is used to access cover art of local music files
                    url = hostAndPort + "/music/" + coverid + "/cover.jpg";
                }
            } else if (artwork_url != null) {
                if (artwork_url.startsWith("http")) {
                    // Typically indicates that cover art is not local to LMS
                    url = artwork_url;
                } else if (artwork_url.startsWith("/")) {
                    // Typically used for default coverart for plugins (e.g. Pandora, etc.)
                    url = hostAndPort + artwork_url;
                } else {
                    // Another variation of default coverart for plugins (e.g. Pandora, etc.)
                    url = hostAndPort + "/" + artwork_url;
                }
            }
            return url;
        }

        private void handlePlaylistMessage(final String mac, String[] messageParts) {
            if (messageParts.length < 3) {
                return;
            }
            String action = messageParts[2];
            String mode;
            if ("newsong".equals(action)) {
                mode = "play";
                // Execute in separate thread to avoid delaying listener
                scheduler.execute(() -> updateCustomButtons(mac));
                // Set the track duration to 0
                updatePlayer(listener -> listener.durationEvent(mac, 0));
            } else if ("pause".equals(action)) {
                if (messageParts.length < 4) {
                    return;
                }
                mode = "0".equals(messageParts[3]) ? "play" : "pause";
            } else if ("stop".equals(action)) {
                mode = "stop";
            } else if ("play".equals(action) && "playlist".equals(messageParts[1])) {
                if (messageParts.length >= 4) {
                    handleSourceChangeMessage(mac, messageParts[3]);
                }
                return;
            } else {
                // Added so that actions (such as delete, index, jump, open) are not treated as "play"
                logger.trace("Unhandled playlist message type '{}'", Arrays.toString(messageParts));
                return;
            }
            final String value = mode;
            updatePlayer(listener -> listener.modeChangeEvent(mac, value));
        }

        private void handleSourceChangeMessage(String mac, String rawSource) {
            String source = URLDecoder.decode(rawSource, StandardCharsets.UTF_8);
            updatePlayer(listener -> listener.sourceChangeEvent(mac, source));
        }

        private void handlePrefsetMessage(final String mac, String[] messageParts) {
            if (messageParts.length < 5) {
                return;
            }
            // server prefsets
            if ("server".equals(messageParts[2])) {
                String function = messageParts[3];
                String value = messageParts[4];
                if ("power".equals(function)) {
                    final boolean power = "1".equals(value);
                    updatePlayer(listener -> listener.powerChangeEvent(mac, power));
                } else if ("volume".equals(function)) {
                    final int volume = (int) Double.parseDouble(value);
                    updatePlayer(listener -> listener.absoluteVolumeChangeEvent(mac, volume));
                }
            }
        }

        private void handleFavorites(String message) {
            String[] messageParts = message.split("\\s");
            if (messageParts.length == 2 && "changed".equals(messageParts[1])) {
                // LMS informing us that favorites have changed; request an update to the favorites list
                requestFavorites();
                return;
            }
            if (messageParts.length < 7) {
                logger.trace("No favorites in message.");
                return;
            }

            List<Favorite> favorites = new ArrayList<>();
            Favorite f = null;
            boolean isTypePlaylist = false;
            for (KeyValue entry : decodeKeyValueResponse(messageParts)) {
                // Favorite ID (in form xxxxxxxxx.n)
                if ("id".equals(entry.key)) {
                    f = new Favorite(entry.value);
                    favorites.add(f);
                    isTypePlaylist = false;
                }
                // Favorite name
                else if ("name".equals(entry.key)) {
                    f.name = entry.value;
                } else if ("type".equals(entry.key) && "playlist".equals(entry.value)) {
                    isTypePlaylist = true;
                }
                // When "1", favorite is a submenu with additional favorites
                else if ("hasitems".equals(entry.key)) {
                    boolean hasitems = "1".equals(entry.value);
                    if (f != null) {
                        // Except for some favorites (e.g. Spotify) use hasitems:1 and type:playlist
                        if (hasitems && !isTypePlaylist) {
                            // Skip subfolders
                            favorites.remove(f);
                            f = null;
                        }
                    }
                }
            }
            updatePlayer(listener -> listener.updateFavoritesListEvent(favorites));
            updateChannelFavoritesList(favorites);
        }

        private void updateChannelFavoritesList(List<Favorite> favorites) {
            final Channel channel = getThing().getChannel(CHANNEL_FAVORITES_LIST);
            if (channel == null) {
                logger.debug("Channel {} doesn't exist. Delete & add thing to get channel.", CHANNEL_FAVORITES_LIST);
                return;
            }

            // Get channel config parameter indicating whether name should be wrapped with double quotes
            Boolean includeQuotes = Boolean.FALSE;
            if (channel.getConfiguration().containsKey(CHANNEL_CONFIG_QUOTE_LIST)) {
                includeQuotes = (Boolean) channel.getConfiguration().get(CHANNEL_CONFIG_QUOTE_LIST);
            }

            String quote = includeQuotes.booleanValue() ? "\"" : "";
            StringBuilder sb = new StringBuilder();
            for (Favorite favorite : favorites) {
                sb.append(favorite.shortId).append("=").append(quote).append(favorite.name.replace(",", ""))
                        .append(quote).append(",");
            }

            if (sb.length() == 0) {
                updateState(CHANNEL_FAVORITES_LIST, UnDefType.NULL);
            } else {
                // Drop the last comma
                sb.setLength(sb.length() - 1);
                String favoritesList = sb.toString();
                logger.trace("Updating favorites channel for {} to state {}", getThing().getUID(), favoritesList);
                updateState(CHANNEL_FAVORITES_LIST, new StringType(favoritesList));
            }
        }

        private ScheduledFuture<?> scheduleRequestFavorites() {
            // Delay the execution to give the player thing handlers a chance to initialize
            return scheduler.schedule(SqueezeBoxServerHandler.this::requestFavorites, 3L, TimeUnit.SECONDS);
        }

        private void updateCustomButtons(final String mac) {
            String response = executePost(jsonRpcUrl, JSONRPC_STATUS_REQUEST.replace("@@MAC@@", mac));
            if (response != null) {
                logger.trace("Status response: {}", response);
                String likeCommand = null;
                String unlikeCommand = null;
                try {
                    StatusResponseDTO status = gson.fromJson(response, StatusResponseDTO.class);
                    if (status != null && status.result != null && status.result.remoteMeta != null
                            && status.result.remoteMeta.buttons != null) {
                        ButtonsDTO buttons = status.result.remoteMeta.buttons;
                        if (buttons.repeat != null && buttons.repeat.isCustom()) {
                            likeCommand = buttons.repeat.command;
                        }
                        if (buttons.shuffle != null && buttons.shuffle.isCustom()) {
                            unlikeCommand = buttons.shuffle.command;
                        }
                    }
                } catch (JsonSyntaxException e) {
                    logger.debug("JsonSyntaxException parsing status response: {}", response, e);
                }
                final String like = likeCommand;
                final String unlike = unlikeCommand;
                updatePlayer(listener -> listener.buttonsChangeEvent(mac, like, unlike));
            }
        }

        private String executePost(String url, String content) {
            // @formatter:off
            HttpRequestBuilder builder = HttpRequestBuilder.postTo(url)
                .withTimeout(Duration.ofSeconds(5))
                .withContent(content)
                .withHeader("charset", "utf-8")
                .withHeader("Content-Type", "application/json");
            // @formatter:on
            if (basicAuthorization != null) {
                builder = builder.withHeader("Authorization", "Basic " + basicAuthorization);
            }
            try {
                return builder.getContentAsString();
            } catch (IOException e) {
                logger.debug("Bridge: IOException on jsonrpc call: {}", e.getMessage(), e);
                return null;
            }
        }
    }

    /**
     * Interface to allow us to pass function call-backs to SqueezeBox Player
     * Event Listeners
     *
     * @author Dan Cunningham
     *
     */
    interface PlayerUpdateEvent {
        void updateListener(SqueezeBoxPlayerEventListener listener);
    }

    /**
     * Update Listeners and child Squeeze Player Things
     *
     * @param event
     */
    private void updatePlayer(PlayerUpdateEvent event) {
        // update listeners like disco services
        synchronized (squeezeBoxPlayerListeners) {
            for (SqueezeBoxPlayerEventListener listener : squeezeBoxPlayerListeners) {
                event.updateListener(listener);
            }
        }
        // update our children
        Bridge bridge = getThing();

        List<Thing> things = bridge.getThings();
        for (Thing thing : things) {
            ThingHandler handler = thing.getHandler();
            if (handler instanceof SqueezeBoxPlayerEventListener playerEventListener
                    && !squeezeBoxPlayerListeners.contains(handler)) {
                event.updateListener(playerEventListener);
            }
        }
    }

    /**
     * Adds a listener for player events
     *
     * @param squeezeBoxPlayerListener
     * @return
     */
    public boolean registerSqueezeBoxPlayerListener(SqueezeBoxPlayerEventListener squeezeBoxPlayerListener) {
        logger.trace("Registering player listener");
        return squeezeBoxPlayerListeners.add(squeezeBoxPlayerListener);
    }

    /**
     * Removes a listener from player events
     *
     * @param squeezeBoxPlayerListener
     * @return
     */
    public boolean unregisterSqueezeBoxPlayerListener(SqueezeBoxPlayerEventListener squeezeBoxPlayerListener) {
        logger.trace("Unregistering player listener");
        return squeezeBoxPlayerListeners.remove(squeezeBoxPlayerListener);
    }

    /**
     * Removed a player from our known list of players, will populate again if
     * player is seen
     *
     * @param mac
     */
    public void removePlayerCache(String mac) {
        players.remove(mac);
    }

    /**
     * Schedule the server to try and reconnect
     */
    private void scheduleReconnect() {
        logger.debug("scheduling squeeze server reconnect in {} seconds", RECONNECT_TIME);
        cancelReconnect();
        reconnectFuture = scheduler.schedule(this::connect, RECONNECT_TIME, TimeUnit.SECONDS);
    }

    /**
     * Clears our reconnect job if exists
     */
    private void cancelReconnect() {
        if (reconnectFuture != null) {
            reconnectFuture.cancel(true);
        }
    }
}
