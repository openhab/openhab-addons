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
package org.openhab.binding.clementineremote.internal;

import static java.lang.Thread.sleep;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLConnection;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.*;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.qspool.clementineremote.backend.pb.ClementineRemote.Message;
import de.qspool.clementineremote.backend.pb.ClementineRemote.MsgType;
import de.qspool.clementineremote.backend.pb.ClementineRemote.RequestConnect;
import de.qspool.clementineremote.backend.pb.ClementineRemote.RequestSetTrackPosition;
import de.qspool.clementineremote.backend.pb.ClementineRemote.RequestSetVolume;
import de.qspool.clementineremote.backend.pb.ClementineRemote.ResponseCurrentMetadata;
import de.qspool.clementineremote.backend.pb.ClementineRemote.ResponseUpdateTrackPosition;
import de.qspool.clementineremote.backend.pb.ClementineRemote.SongMetadata;
import tech.units.indriya.unit.Units;

/**
 * The {@link ClementineRemoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stephan Richter - Initial contribution
 */
@NonNullByDefault
public class ClementineRemoteHandler extends BaseThingHandler {

    private int currentPos = 0;
    private int volume = 50;

    /**
     * used to track the current state of the player
     */
    private enum State {
        unknown,
        playing,
        paused,
        skipping,
        stopped
    }

    private final Logger logger = LoggerFactory.getLogger(ClementineRemoteHandler.class);

    private @Nullable ClementineRemoteConfiguration config;
    private @Nullable Socket socket = null;
    private boolean enabled = true;
    private @Nullable DataOutputStream out = null;
    private @Nullable SongMetadata song = null;

    private Message.Builder builder = Message.newBuilder();
    private State state = State.unknown;

    public ClementineRemoteHandler(Thing thing) {
        super(thing);
    }

    /**
     * Try to establish a connection to the Clementine instance.
     * If the connection breaks: wait 15s, then try again
     */
    private void connect() {
        enabled = true;
        while (enabled) {
            try {
                sleep(15000);
            } catch (InterruptedException e) {
                logger.warn("sleep interrupted: {}", e.getMessage());
            }
            connectionStart();
            if (enabled) {
                logger.debug("Connection broken. Reconnectiong in 15 secs…");
            }
        }
    }

    /**
     * Try to start a new connection:
     * <ul>
     * <li>open socket</li>
     * <li>connect output stream</li>
     * <li>send connect message</li>
     * <li>wait for incoming messages</li>
     * </ul>
     * When the connection breaks: try to disconnect gracefully.
     */
    private void connectionStart() {
        try {
            if (socket == null || socket.isClosed()) {
                var address = new InetSocketAddress(config.hostname, config.port);
                socket = new Socket();
                socket.connect(address);
            }
            out = new DataOutputStream(socket.getOutputStream());
            sendConnectMessage();
            handleMessages();
        } catch (IOException e) {
            logger.debug("openConnection() failed: {}", e.getMessage());
        }
        disconnect();
    }

    private static Optional<String> detectMime(byte[] bytes) {
        try {
            InputStream is = new BufferedInputStream(new ByteArrayInputStream(bytes));
            return Optional.of(URLConnection.guessContentTypeFromStream(is));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * disconnect from clementine/clean up remains of broken connection
     */
    private void disconnect() {
        updateStatus(ThingStatus.OFFLINE);
        setState(State.unknown);

        if (out != null) { // try to close the output stream
            try {
                out.close();
            } catch (IOException e) {
                logger.warn("disconnect(): failed to close output stream socket: {}", e.getMessage());
            }
            out = null;
        }
        if (socket != null) { // try to close the connection
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn("disconnect(): failed to close clementine protobuf socket: {}", e.getMessage());
            }
            socket = null;
        }
    }

    /**
     * Discard the component. Disables the component and disconnects from player
     */
    @Override
    public void dispose() {
        enabled = false;
        disconnect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        var channel = channelUID.getId();
        switch (channel) {
            case CHANNEL_PLAYBACK:
            case CHANNEL_VOLUME:
                handleControlCommand(command);
                return;
        }
        logger.debug("No handler for command \"{}\" on channel {}", command, channel);
        return;
    }

    /**
     * handles commands from the writable channels
     *
     * @param command
     * @return
     */
    private boolean handleControlCommand(Command command) {
        if (command instanceof StringType) {
            var cmd = ((StringType) command).toString();
            switch (cmd) {
                case CMD_FORWARD:
                    return sendSkip(+15);
                case CMD_NEXT:
                    return sendMessage(MsgType.NEXT);
                case CMD_PAUSE:
                    return sendMessage(MsgType.PAUSE);
                case CMD_PLAY:
                    return sendMessage(MsgType.PLAY);
                case CMD_PREVIOUS:
                    return sendMessage(MsgType.PREVIOUS);
                case CMD_REWIND:
                    return sendSkip(-15);
                case CMD_STOP:
                    return sendMessage(MsgType.STOP);
            }
        }
        if (command instanceof PercentType) {
            return sendVolume(((PercentType) command).intValue());
        }
        logger.warn("Unknown command \"{}\" cannot be handled.", command);
        return false;
    }

    private void handleCurrentMeta(ResponseCurrentMetadata meta) {
        song = meta.getSongMetadata();
        var bytes = song.getArt().toByteArray();
        detectMime(bytes).ifPresent(mime -> updateState(CHANNEL_COVER, new RawType(bytes, mime)));
        updateState(CHANNEL_ALBUM, new StringType(song.getAlbum()));
        updateState(CHANNEL_ARTIST, new StringType(song.getArtist()));
        updateState(CHANNEL_TRACK, new StringType(song.getTrack() + ""));
        updateState(CHANNEL_TITLE, new StringType(song.getTitle()));
    }

    private void handleMessages() throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        while (enabled) {
            var len = in.readInt();
            if (len < 0 || len > MAX_SIZE) {
                logger.debug("handleMessages() ignoring data: len = {}", len);
                continue;
            }
            byte[] buf = new byte[len];
            in.readFully(buf);
            updateStatus(ThingStatus.ONLINE);
            handleMessageFromClementine(Message.parseFrom(buf));
        }
    }

    private void handleMessageFromClementine(Message message) {
        MsgType type = message.getType();
        switch (type) {
            case CURRENT_METAINFO:
                handleCurrentMeta(message.getResponseCurrentMetadata());
                break;
            case PAUSE:
                setState(State.paused);
                break;
            case PLAY:
                setState(State.playing);
                break;
            case SET_VOLUME:
                handleVolume(message.getRequestSetVolume());
                break;
            case STOP:
                handleStop();
                break;
            case UPDATE_TRACK_POSITION:
                handleTrackPos(message.getResponseUpdateTrackPosition());
                break;
            case KEEP_ALIVE:
                break;
            default:
                logger.info("handleMessageFromClementine(): received {} message from clementine: {}", type, message);
        }
    }

    private void handleStop() {
        setTrack("-", "-", "-", "-");
        setState(State.stopped);
        handleNullableTrackPos(null);
    }

    private void handleNullableTrackPos(@Nullable Integer newPos) {
        if (newPos != null) {
            setState(State.playing);
            currentPos = newPos;
        } else {
            currentPos = 0;
        }

        updateState(CHANNEL_POSITION, new QuantityType<>(currentPos, Units.SECOND));
    }

    private void handleTrackPos(ResponseUpdateTrackPosition message) {
        handleNullableTrackPos(message.getPosition());
    }

    private void handleVolume(RequestSetVolume message) {
        volume = message.getVolume();
        updateState(CHANNEL_VOLUME, new PercentType(volume));
    }

    @Override
    public void initialize() {
        config = getConfigAs(ClementineRemoteConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(this::connect);

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private void sendConnectMessage() throws IOException {
        RequestConnect req = builder.getRequestConnectBuilder().build();
        Message msg = builder.setRequestConnect(req).build();
        sendMessageOrThrow(msg);
    }

    private boolean sendSkip(int d) {
        state = State.skipping;
        var pos = RequestSetTrackPosition.newBuilder().setPosition(currentPos + d);
        Message msg = builder.setType(MsgType.SET_TRACK_POSITION).setRequestSetTrackPosition(pos).build();
        return sendMessage(msg);
    }

    private boolean sendMessage(MsgType type) {
        state = State.unknown;
        return sendMessage(builder.setType(type).build());
    }

    private boolean sendMessage(Message message) {
        if (out == null) {
            return false;
        }
        try {
            sendMessageOrThrow(message);
            return true;
        } catch (IOException e) {
            logger.warn("sendMessage({}) failed: {}", message, e.getMessage());
            disconnect();
            return false;
        }
    }

    private void sendMessageOrThrow(Message message) throws IOException {
        byte[] bytes = message.toByteArray();
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    private boolean sendVolume(int percent) {
        var volume = RequestSetVolume.newBuilder().setVolume(percent);
        Message msg = builder.setType(MsgType.SET_VOLUME).setRequestSetVolume(volume).build();
        return sendMessage(msg);
    }

    private boolean setState(State newState) {
        if (state == newState) {
            return false;
        }
        state = newState;
        switch (state) {
            case playing:
                updateState(CHANNEL_PLAYBACK, new StringType(CMD_PLAY));
                break;
            case paused:
            case stopped:
                updateState(CHANNEL_PLAYBACK, new StringType(CMD_PAUSE));
                break;
        }
        updateState(CHANNEL_STATE, new StringType(state.toString()));
        return true;
    }

    private void setTrack(String artist, String album, String track, String title) {
        updateState(CHANNEL_ALBUM, new StringType(album));
        updateState(CHANNEL_ARTIST, new StringType(artist));
        updateState(CHANNEL_TRACK, new StringType(track));
        updateState(CHANNEL_TITLE, new StringType(title));
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        dispose();
        scheduler.execute(this::connect);
    }
}
