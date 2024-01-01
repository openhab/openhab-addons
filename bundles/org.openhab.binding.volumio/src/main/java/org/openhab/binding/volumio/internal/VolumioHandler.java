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

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.volumio.internal.mapping.VolumioData;
import org.openhab.binding.volumio.internal.mapping.VolumioEvents;
import org.openhab.binding.volumio.internal.mapping.VolumioServiceTypes;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * The {@link VolumioHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Sernetz - Initial Contribution
 * @author Chris Wohlbrecht - Adaption for openHAB 3
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
public class VolumioHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VolumioHandler.class);

    private @Nullable VolumioService volumio;

    private final VolumioData state = new VolumioData();

    public VolumioHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        VolumioService volumioLocal = volumio;

        if (volumioLocal == null) {
            if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Volumio service was not yet initialized, cannot handle command.");
            }
            return;
        }

        try {
            switch (channelUID.getId()) {
                case VolumioBindingConstants.CHANNEL_PLAYER:
                    handlePlaybackCommands(command);
                    break;
                case VolumioBindingConstants.CHANNEL_VOLUME:
                    handleVolumeCommand(command);
                    break;

                case VolumioBindingConstants.CHANNEL_ARTIST:
                case VolumioBindingConstants.CHANNEL_ALBUM:
                case VolumioBindingConstants.CHANNEL_TRACK_TYPE:
                case VolumioBindingConstants.CHANNEL_TITLE:
                    break;

                case VolumioBindingConstants.CHANNEL_PLAY_RADIO_STREAM:
                    if (command instanceof StringType) {
                        final String uri = command.toFullString();
                        volumioLocal.replacePlay(uri, "Radio", VolumioServiceTypes.WEBRADIO);
                    }

                    break;

                case VolumioBindingConstants.CHANNEL_PLAY_URI:
                    if (command instanceof StringType) {
                        final String uri = command.toFullString();
                        volumioLocal.replacePlay(uri, "URI", VolumioServiceTypes.WEBRADIO);
                    }

                    break;

                case VolumioBindingConstants.CHANNEL_PLAY_FILE:
                    if (command instanceof StringType) {
                        final String uri = command.toFullString();
                        volumioLocal.replacePlay(uri, "", VolumioServiceTypes.MPD);
                    }

                    break;

                case VolumioBindingConstants.CHANNEL_PLAY_PLAYLIST:
                    if (command instanceof StringType) {
                        final String playlistName = command.toFullString();
                        volumioLocal.playPlaylist(playlistName);
                    }

                    break;
                case VolumioBindingConstants.CHANNEL_CLEAR_QUEUE:
                    if ((command instanceof OnOffType) && (command == OnOffType.ON)) {
                        volumioLocal.clearQueue();
                        // Make it feel like a toggle button ...
                        updateState(channelUID, OnOffType.OFF);
                    }
                    break;
                case VolumioBindingConstants.CHANNEL_PLAY_RANDOM:
                    if (command instanceof OnOffType) {
                        boolean enableRandom = command == OnOffType.ON;
                        volumioLocal.setRandom(enableRandom);
                    }
                    break;
                case VolumioBindingConstants.CHANNEL_PLAY_REPEAT:
                    if (command instanceof OnOffType) {
                        boolean enableRepeat = command == OnOffType.ON;
                        volumioLocal.setRepeat(enableRepeat);
                    }
                    break;
                case "REFRESH":
                    logger.debug("Called Refresh");
                    volumioLocal.getState();
                    break;
                case VolumioBindingConstants.CHANNEL_SYSTEM_COMMAND:
                    if (command instanceof StringType) {
                        sendSystemCommand(command);
                        updateState(VolumioBindingConstants.CHANNEL_SYSTEM_COMMAND, UnDefType.UNDEF);
                    } else if (RefreshType.REFRESH == command) {
                        updateState(VolumioBindingConstants.CHANNEL_SYSTEM_COMMAND, UnDefType.UNDEF);
                    }
                    break;
                case VolumioBindingConstants.CHANNEL_STOP:
                    if (command instanceof StringType) {
                        handleStopCommand(command);
                        updateState(VolumioBindingConstants.CHANNEL_STOP, UnDefType.UNDEF);
                    } else if (RefreshType.REFRESH == command) {
                        updateState(VolumioBindingConstants.CHANNEL_STOP, UnDefType.UNDEF);
                    }
                    break;
                default:
                    logger.error("Unknown channel: {}", channelUID.getId());
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void sendSystemCommand(Command command) {
        VolumioService volumioLocal = volumio;

        if (volumioLocal == null) {
            if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Volumio service was not yet initialized, cannot handle send system command.");
            }
            return;
        }

        if (command instanceof StringType) {
            volumioLocal.sendSystemCommand(command.toString());
            updateState(VolumioBindingConstants.CHANNEL_SYSTEM_COMMAND, UnDefType.UNDEF);
        } else if (command.equals(RefreshType.REFRESH)) {
            updateState(VolumioBindingConstants.CHANNEL_SYSTEM_COMMAND, UnDefType.UNDEF);
        }
    }

    /**
     * Set all channel of thing to UNDEF during connection.
     */
    private void clearChannels() {
        for (Channel channel : getThing().getChannels()) {
            updateState(channel.getUID(), UnDefType.UNDEF);
        }
    }

    private void handleVolumeCommand(Command command) {
        VolumioService volumioLocal = volumio;

        if (volumioLocal == null) {
            if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Volumio service was not yet initialized, cannot handle volume command.");
            }
            return;
        }

        if (command instanceof PercentType commandAsPercentType) {
            volumioLocal.setVolume(commandAsPercentType);
        } else if (command instanceof RefreshType) {
            volumioLocal.getState();
        } else {
            logger.error("Command is not handled");
        }
    }

    private void handleStopCommand(Command command) {
        VolumioService volumioLocal = volumio;

        if (volumioLocal == null) {
            if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Volumio service was not yet initialized, cannot handle stop command.");
            }
            return;
        }

        if (command instanceof StringType) {
            volumioLocal.stop();
            updateState(VolumioBindingConstants.CHANNEL_STOP, UnDefType.UNDEF);
        } else if (command.equals(RefreshType.REFRESH)) {
            updateState(VolumioBindingConstants.CHANNEL_STOP, UnDefType.UNDEF);
        }
    }

    private void handlePlaybackCommands(Command command) {
        VolumioService volumioLocal = volumio;

        if (volumioLocal == null) {
            if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Volumio service was not yet initialized, cannot handle playback command.");
            }
            return;
        }
        if (command instanceof PlayPauseType playPauseCmd) {
            switch (playPauseCmd) {
                case PLAY:
                    volumioLocal.play();
                    break;
                case PAUSE:
                    volumioLocal.pause();
                    break;
            }
        } else if (command instanceof NextPreviousType nextPreviousType) {
            switch (nextPreviousType) {
                case PREVIOUS:
                    volumioLocal.previous();
                    break;
                case NEXT:
                    volumioLocal.next();
                    break;
            }
        } else if (command instanceof RewindFastforwardType fastForwardType) {
            switch (fastForwardType) {
                case FASTFORWARD:
                case REWIND:
                    logger.warn("Not implemented yet");
                    break;
            }
        } else if (command instanceof RefreshType) {
            volumioLocal.getState();
        } else {
            logger.error("Command is not handled: {}", command);
        }
    }

    /**
     * Bind default listeners to volumio session.
     * - EVENT_CONNECT - Connection to volumio was established
     * - EVENT_DISCONNECT - Connection was disconnected
     * - PUSH.STATE -
     */
    private void bindDefaultListener() {
        VolumioService volumioLocal = volumio;

        if (volumioLocal == null) {
            if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Volumio service was not yet initialized.");
            }
            return;
        }

        volumioLocal.on(Socket.EVENT_CONNECT, connectListener());
        volumioLocal.on(Socket.EVENT_DISCONNECT, disconnectListener());
        volumioLocal.on(VolumioEvents.PUSH_STATE, pushStateListener());
    }

    /**
     * Read the configuration and connect to volumio device. The Volumio impl. is
     * async so it should not block the process in any way.
     */
    @Override
    public void initialize() {
        String hostname = (String) getThing().getConfiguration().get(VolumioBindingConstants.CONFIG_PROPERTY_HOSTNAME);
        int port = ((BigDecimal) getThing().getConfiguration().get(VolumioBindingConstants.CONFIG_PROPERTY_PORT))
                .intValueExact();
        String protocol = (String) getThing().getConfiguration().get(VolumioBindingConstants.CONFIG_PROPERTY_PROTOCOL);
        int timeout = ((BigDecimal) getThing().getConfiguration().get(VolumioBindingConstants.CONFIG_PROPERTY_TIMEOUT))
                .intValueExact();

        if (hostname == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration incomplete, missing hostname");
        } else if (protocol == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration incomplete, missing protocol");
        } else {
            logger.debug("Trying to connect to Volumio on {}://{}:{}", protocol, hostname, port);
            try {
                VolumioService volumioLocal = new VolumioService(protocol, hostname, port, timeout);
                volumio = volumioLocal;
                clearChannels();
                bindDefaultListener();
                updateStatus(ThingStatus.OFFLINE);
                volumioLocal.connect();
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        VolumioService volumioLocal = volumio;
        if (volumioLocal != null) {
            scheduler.schedule(() -> {
                if (volumioLocal.isConnected()) {
                    logger.warn("Timeout during disconnect event");
                } else {
                    volumioLocal.close();
                }
                clearChannels();
            }, 30, TimeUnit.SECONDS);

            volumioLocal.disconnect();
        }
    }

    /** Listener **/

    /**
     * As soon as the Connect Listener is executed
     * the ThingStatus is set to ONLINE.
     */
    private Emitter.Listener connectListener() {
        return arg -> updateStatus(ThingStatus.ONLINE);
    }

    /**
     * As soon as the Disconnect Listener is executed
     * the ThingStatus is set to OFFLINE.
     */
    private Emitter.Listener disconnectListener() {
        return arg0 -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    /**
     * On received a pushState Event, the ThingChannels are
     * updated if there is a change and they are linked.
     */
    private Emitter.Listener pushStateListener() {
        return data -> {
            try {
                JSONObject jsonObject = (JSONObject) data[0];
                logger.debug("{}", jsonObject.toString());
                state.update(jsonObject);
                if (isLinked(VolumioBindingConstants.CHANNEL_TITLE) && state.isTitleDirty()) {
                    updateState(VolumioBindingConstants.CHANNEL_TITLE, state.getTitle());
                }
                if (isLinked(VolumioBindingConstants.CHANNEL_ARTIST) && state.isArtistDirty()) {
                    updateState(VolumioBindingConstants.CHANNEL_ARTIST, state.getArtist());
                }
                if (isLinked(VolumioBindingConstants.CHANNEL_ALBUM) && state.isAlbumDirty()) {
                    updateState(VolumioBindingConstants.CHANNEL_ALBUM, state.getAlbum());
                }
                if (isLinked(VolumioBindingConstants.CHANNEL_VOLUME) && state.isVolumeDirty()) {
                    updateState(VolumioBindingConstants.CHANNEL_VOLUME, state.getVolume());
                }
                if (isLinked(VolumioBindingConstants.CHANNEL_PLAYER) && state.isStateDirty()) {
                    updateState(VolumioBindingConstants.CHANNEL_PLAYER, state.getState());
                }
                if (isLinked(VolumioBindingConstants.CHANNEL_TRACK_TYPE) && state.isTrackTypeDirty()) {
                    updateState(VolumioBindingConstants.CHANNEL_TRACK_TYPE, state.getTrackType());
                }

                if (isLinked(VolumioBindingConstants.CHANNEL_PLAY_RANDOM) && state.isRandomDirty()) {
                    updateState(VolumioBindingConstants.CHANNEL_PLAY_RANDOM, state.getRandom());
                }
                if (isLinked(VolumioBindingConstants.CHANNEL_PLAY_REPEAT) && state.isRepeatDirty()) {
                    updateState(VolumioBindingConstants.CHANNEL_PLAY_REPEAT, state.getRepeat());
                }
                /**
                 * if (isLinked(CHANNEL_COVER_ART) && state.isCoverArtDirty()) {
                 * updateState(CHANNEL_COVER_ART, state.getCoverArt());
                 * }
                 */
            } catch (JSONException e) {
                logger.error("Could not refresh channel: {}", e.getMessage());
            }
        };
    }
}
