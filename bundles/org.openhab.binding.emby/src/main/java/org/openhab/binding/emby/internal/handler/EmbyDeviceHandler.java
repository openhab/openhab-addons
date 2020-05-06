/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.*;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.emby.internal.EmbyDeviceConfiguration;
import org.openhab.binding.emby.internal.EmbyEventListener;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EmbyDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Zachary Christiansen - Initial contribution
 */

public class EmbyDeviceHandler extends BaseThingHandler implements EmbyEventListener {

    private final Logger logger = LoggerFactory.getLogger(EmbyDeviceHandler.class);

    private EmbyDeviceConfiguration config;
    private EmbyPlayStateModel currentPlayState = null;

    public EmbyDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if we are in an active playstate then processs the command
        Channel channel = this.thing.getChannel(channelUID.getId());
        String commandName;
        if (!(this.currentPlayState == null)) {
            EmbyBridgeHandler handler = (EmbyBridgeHandler) this.getBridge().getHandler();
            switch (channelUID.getId()) {
                case CHANNEL_CONTROL:
                    if (command instanceof PlayPauseType) {
                        // if we are unpause
                        if (PlayPauseType.PLAY.equals(command)) {
                            handler.sendCommand(CONTROL_SESSION + currentPlayState.getId() + CONTROL_PLAY);
                            // send the pause command
                        } else {
                            handler.sendCommand(CONTROL_SESSION + currentPlayState.getId() + CONTROL_PAUSE);
                        }
                    } else {
                        logger.debug("The channel {} receceived a command {}, this command not supported",
                                channelUID.getAsString(), command.toString());
                    }
                    break;
                case CHANNEL_MUTE:
                    if (OnOffType.ON.equals(command)) {
                        handler.sendCommand(CONTROL_SESSION + currentPlayState.getId() + CONTROL_MUTE);
                    } else {
                        handler.sendCommand(CONTROL_SESSION + currentPlayState.getId() + CONTROL_UNMUTE);
                    }
                    break;
                case CHANNEL_STOP:
                    if (OnOffType.ON.equals(command)) {
                        handler.sendCommand(CONTROL_SESSION + currentPlayState.getId() + CONTROL_STOP);
                    }
                    break;

                case CHANNEL_SENDPLAYCOMMAND:
                    logger.debug("Sending the following payload: {} for device: {}", command.toString(),
                            currentPlayState.getId());
                    handler.sendCommand(CONTROL_SESSION + currentPlayState.getId() + CONTROL_SENDPLAY,
                            command.toString());
                    break;

                case CHANNEL_GENERALCOMMAND:
                    commandName = channel.getConfiguration().get(CHANNEL_GENERALCOMMAND_NAME).toString();
                    logger.debug("Sending the following command {} for device: {}", commandName,
                            currentPlayState.getId());
                    if (OnOffType.ON.equals(command)) {
                        handler.sendCommand(
                                CONTROL_SESSION + currentPlayState.getId() + CONTROL_GENERALCOMMAND + commandName);
                    }
                    break;
                case CHANNEL_GENERALCOMMANDWITHARGS:
                    commandName = channel.getConfiguration().get(CHANNEL_GENERALCOMMAND_NAME).toString();
                    logger.debug("Sending the following command {} for device: {}", commandName,
                            currentPlayState.getId());
                    handler.sendCommand(
                            CONTROL_SESSION + currentPlayState.getId() + CONTROL_GENERALCOMMAND + commandName,
                            "Arguments:" + command + "}");
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing emby device: {}", this.getThing().getLabel());
        config = getConfigAs(EmbyDeviceConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null || !(bridge.getHandler() instanceof EmbyBridgeHandler)) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "You must choose a Emby Server for this Device.");
            return;
        }

        if (bridge.getStatus() == OFFLINE) {
            updateStatus(OFFLINE, BRIDGE_OFFLINE, "The Emby Server is currently offline.");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void updateConnectionState(boolean connected) {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No connection established");
        }
    }

    @Override
    public void updateScreenSaverState(boolean screenSaveActive) {
    }

    @Override
    public void updatePlayerState(EmbyState state) {
        switch (state) {
            case PLAY:
                updateState(CHANNEL_CONTROL, PlayPauseType.PLAY);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
            case PAUSE:
                updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
            case STOP:
            case END:
                updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
                updateState(CHANNEL_STOP, OnOffType.ON);
                break;
            case FASTFORWARD:
                updateState(CHANNEL_CONTROL, RewindFastforwardType.FASTFORWARD);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
            case REWIND:
                updateState(CHANNEL_CONTROL, RewindFastforwardType.REWIND);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
        }
    }

    @Override
    public void updateMuted(boolean muted) {
        if (muted) {
            updateState(CHANNEL_MUTE, OnOffType.ON);
        } else {
            updateState(CHANNEL_MUTE, OnOffType.OFF);
        }
    }

    @Override
    public void updateTitle(String title) {
        updateState(CHANNEL_TITLE, createStringState(title));
    }

    @Override
    public void updatePrimaryImageURL(String imageUrl) {
        updateState(CHANNEL_IMAGEURL, createStringState(imageUrl));
    }

    @Override
    public void updateShowTitle(String title) {
        updateState(CHANNEL_SHOWTITLE, createStringState(title));
    }

    @Override
    public void updateMediaType(String mediaType) {
        updateState(CHANNEL_MEDIATYPE, createStringState(mediaType));
    }

    @Override
    public void updateCurrentTime(long currentTime) {
        updateState(CHANNEL_CURRENTTIME, createQuantityState(currentTime, SmartHomeUnits.SECOND));
    }

    @Override
    public void updateDuration(long duration) {
        updateState(CHANNEL_DURATION, createQuantityState(duration, SmartHomeUnits.SECOND));
    }

    /**
     * Wrap the given String in a new {@link StringType} or returns {@link UnDefType#UNDEF} if the String is empty.
     */
    private State createStringState(String string) {
        if (string == null || string.isEmpty()) {
            return UnDefType.UNDEF;
        } else {
            return new StringType(string);
        }
    }

    /**
     * Wrap the given list of Strings in a new {@link StringType} or returns {@link UnDefType#UNDEF} if the list of
     * Strings is empty.
     */
    private State createStringListState(List<String> list) {
        if (list == null || list.isEmpty()) {
            return UnDefType.UNDEF;
        } else {
            return createStringState(list.stream().collect(Collectors.joining(", ")));
        }
    }

    /**
     * Wrap the given RawType and return it as {@link State} or return {@link UnDefType#UNDEF} if the RawType is null.
     */
    private State createImageState(RawType image) {
        if (image == null) {
            return UnDefType.UNDEF;
        } else {
            return image;
        }
    }

    private State createQuantityState(Number value, Unit<?> unit) {
        return (value == null) ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    private void updateState(EmbyState state) {
        updatePlayerState(state);

        // if this is a Stop then clear everything else
        if (state == EmbyState.STOP) {
            updateTitle("");
            updateShowTitle("");
            updatePrimaryImageURL("");
            updateMediaType("");
            updateDuration(-1);
            this.currentPlayState = null;
            updateCurrentTime(-1);
        }

    }

    @Override
    public void handleEvent(EmbyPlayStateModel playstate, String hostname, int embyport) {
        // check the deviceId of this handler against the deviceId of the event to see if it matches
        if (playstate.compareDeviceId(config.deviceID)) {
            this.currentPlayState = playstate;
            logger.debug("the deviceId for: {} matches the deviceId of the thing so we will update stringUrl",
                    playstate.getDeviceName());
            Configuration config = this.thing.getChannel(CHANNEL_IMAGEURL).getConfiguration();
            String maxWidth = null;
            String maxHeight = null;
            Boolean percentPlayed = null;

            // String maxWidth = config.get(CHANNEL_IMAGEURL_MAXWIDTH).toString();
            // String maxHeight = config.get(CHANNEL_IMAGEURL_MAXHEIGHT).toString();
            // Boolean percentPlayed = (Boolean) config.get(CHANNEL_IMAGEURL_PERCENTPLAYED);
            // if (percentPlayed == null) {
            // percentPlayed = true;
            // }

            try {
                maxWidth = this.thing.getChannel(CHANNEL_IMAGEURL).getConfiguration().get(CHANNEL_IMAGEURL_MAXWIDTH)
                        .toString();
            } catch (NullPointerException e) {
                logger.debug("The maxWidth was not set so we keep value as null");
            }

            try {
                maxHeight = this.thing.getChannel(CHANNEL_IMAGEURL).getConfiguration().get(CHANNEL_IMAGEURL_MAXHEIGHT)
                        .toString();
            } catch (NullPointerException e) {
                logger.debug("The maxHeight was not set so we keep value as null");
            }
            try {
                percentPlayed = (Boolean) this.thing.getChannel(CHANNEL_IMAGEURL).getConfiguration()
                        .get(CHANNEL_IMAGEURL_PERCENTPLAYED);
                if (percentPlayed == null) {
                    percentPlayed = true;
                }
            } catch (NullPointerException e) {
                logger.debug("The Percent Played setting was not set so we keep value as true");
            }

            try {
                URI imageURI = playstate.getPrimaryImageURL(hostname, embyport,
                        config.get(CHANNEL_IMAGEURL_TYPE).toString(), maxWidth, maxHeight, percentPlayed);
                if (imageURI.getHost().equals("NotPlaying")) {
                    updateState(EmbyState.END);
                    updateState(EmbyState.STOP);

                } else {
                    if (playstate.getEmbyPlayStatePausedState()) {
                        logger.debug("The playstate for {} is being set to pause", playstate.getDeviceName());
                        updateState(EmbyState.PAUSE);

                    } else {
                        logger.debug("The playstate for {} is being set to play", playstate.getDeviceName());
                        updateState(EmbyState.PLAY);
                    }
                    updatePrimaryImageURL(imageURI.toString());
                    updateMuted(playstate.getEmbyMuteSate());
                    updateShowTitle(playstate.getNowPlayingName());
                    updateCurrentTime(playstate.getNowPlayingTime().longValue());
                    updateDuration(playstate.getNowPlayingTotalTime().longValue());
                    updateMediaType(playstate.getNowPlayingMediaType());
                }
            } catch (URISyntaxException e) {
                logger.debug("unable to create image url for: {} due to exception: {} ", playstate.getDeviceName(),
                        e.toString());
            }
        } else {
            logger.debug("{} does not equal {} the event is for device named: {} ", playstate.getDeviceId(),
                    config.deviceID, playstate.getDeviceName());
        }
    }
}
