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
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CHANNEL_ALBUM;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CHANNEL_ARTIST;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CHANNEL_COVER;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CHANNEL_PLAYBACK;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CHANNEL_POSITION;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CHANNEL_STATE;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CHANNEL_TITLE;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CHANNEL_TRACK;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CHANNEL_VOLUME;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CMD_FORWARD;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CMD_NEXT;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CMD_PAUSE;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CMD_PLAY;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CMD_PREVIOUS;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CMD_REWIND;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.CMD_STOP;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.MAX_SIZE;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLConnection;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.qspool.clementineremote.backend.pb.ClementineRemote.Message;
import de.qspool.clementineremote.backend.pb.ClementineRemote.MsgType;
import de.qspool.clementineremote.backend.pb.ClementineRemote.RequestSetTrackPosition;
import de.qspool.clementineremote.backend.pb.ClementineRemote.RequestSetVolume;
import de.qspool.clementineremote.backend.pb.ClementineRemote.ResponseCurrentMetadata;
import de.qspool.clementineremote.backend.pb.ClementineRemote.ResponseUpdateTrackPosition;
import de.qspool.clementineremote.backend.pb.ClementineRemote.SongMetadata;

/**
 * The {@link ClementineRemoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * It uses Clementine's own, protocol buffers based communication sheme:
 * https://github.com/clementine-player/Android-Remote/wiki/Developer-Documentation
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
        UNKNOWN,
        PLAYING,
        PAUSED,
        SKIPPING,
        STOPPED
    }

    private final Logger logger = LoggerFactory.getLogger(ClementineRemoteHandler.class);

    private @Nullable ClementineRemoteConfiguration config;
    private @Nullable Socket socket = null;
    private boolean enabled = true;
    private @Nullable DataOutputStream out = null;
    private @Nullable SongMetadata song = null;

    private Message.Builder builder = Message.newBuilder();
    private State state = State.UNKNOWN;

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
            connectionStart();
            if (enabled) {
                logger.debug("Connection broken. Reconnectiong in 15 secs…");
                sleep15secs();
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
        setState(State.UNKNOWN);

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
        if (command instanceof RefreshType){
            propagate(song);
            propagate(state);
        }

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
        propagate(song);
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
                setState(State.PAUSED);
                break;
            case PLAY:
                setState(State.PLAYING);
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
        setState(State.STOPPED);
        handleNullableTrackPos(null);
    }

    private void handleNullableTrackPos(@Nullable Integer newPos) {
        if (newPos != null) {
            setState(State.PLAYING);
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

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(this::connect);
    }

    private void propagate(SongMetadata song){
        if (song == null) {
            return;
        }
        var bytes = song.getArt().toByteArray();
        detectMime(bytes).ifPresent(mime -> updateState(CHANNEL_COVER, new RawType(bytes, mime)));
        updateState(CHANNEL_ALBUM, new StringType(song.getAlbum()));
        updateState(CHANNEL_ARTIST, new StringType(song.getArtist()));
        updateState(CHANNEL_TRACK, new StringType(song.getTrack() + ""));
        updateState(CHANNEL_TITLE, new StringType(song.getTitle()));
    }

    private void propagate(State state){
        switch (state) {
            case PLAYING:
                updateState(CHANNEL_PLAYBACK, new StringType(CMD_PLAY));
                break;
            case PAUSED:
            case STOPPED:
                updateState(CHANNEL_PLAYBACK, new StringType(CMD_PAUSE));
                break;
        }
        updateState(CHANNEL_STATE, new StringType(state.toString()));
    }

    private void sleep15secs() {
        try {
            sleep(15000);
        } catch (InterruptedException e) {
            logger.warn("sleep interrupted: {}", e.getMessage());
        }
    }

    private void sendConnectMessage() throws IOException {
        var req = builder.getRequestConnectBuilder();
        if (config.authCode != null && config.authCode != 0) {
            req.setAuthCode(config.authCode);
        }
        sendMessageOrThrow(builder.setRequestConnect(req.build()).build());
    }

    private boolean sendSkip(int d) {
        state = State.SKIPPING;
        var pos = RequestSetTrackPosition.newBuilder().setPosition(currentPos + d);
        Message msg = builder.setType(MsgType.SET_TRACK_POSITION).setRequestSetTrackPosition(pos).build();
        return sendMessage(msg);
    }

    private boolean sendMessage(MsgType type) {
        state = State.UNKNOWN;
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
        propagate(state = newState);
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
