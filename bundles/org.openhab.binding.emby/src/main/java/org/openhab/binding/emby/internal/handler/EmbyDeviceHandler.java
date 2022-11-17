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
package org.openhab.binding.emby.internal.handler;

import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_CONTROL;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_CURRENTTIME;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_DURATION;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_GENERALCOMMAND;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_GENERALCOMMANDWITHARGS;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_GENERALCOMMAND_NAME;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL_MAXHEIGHT;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL_MAXWIDTH;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL_PERCENTPLAYED;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL_TYPE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_MEDIATYPE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_MUTE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_SENDPLAYCOMMAND;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_SHOWTITLE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_STOP;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_TITLE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONTROL_GENERALCOMMAND;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONTROL_MUTE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONTROL_PAUSE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONTROL_PLAY;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONTROL_SENDPLAY;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONTROL_SESSION;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONTROL_STOP;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONTROL_UNMUTE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.DEVICE_ID;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatusDetail.BRIDGE_OFFLINE;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import java.net.URI;
import java.net.URISyntaxException;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.EmbyDeviceConfiguration;
import org.openhab.binding.emby.internal.EmbyEventListener;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EmbyDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyDeviceHandler extends BaseThingHandler implements EmbyEventListener {

    private final Logger logger = LoggerFactory.getLogger(EmbyDeviceHandler.class);
    private EmbyDeviceConfiguration config;
    private @Nullable EmbyPlayStateModel currentPlayState = null;
    private @Nullable EmbyBridgeHandler bridgeHandler;

    public EmbyDeviceHandler(Thing thing) {
        super(thing);
        config = validateConfiguration();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if we are in an active playstate then processs the command
        Channel channel = this.thing.getChannel(channelUID.getId());
        if (channel != null) {
            String commandName;
            if (bridgeHandler != null) {
                EmbyBridgeHandler handler = bridgeHandler;
                logger.trace("The channel ID of the received command is: {}", channelUID.getId());
                if (!(currentPlayState == null)) {
                    String currentSessionID = currentPlayState.getId();
                    logger.trace("The device Id is: {}, the received deviceID is: {} ", currentSessionID,
                            config.deviceID);
                    switch (channelUID.getId()) {
                        case CHANNEL_CONTROL:
                            if (command instanceof PlayPauseType) {
                                // if we are unpause
                                if (PlayPauseType.PLAY.equals(command)) {
                                    handler.sendCommand(CONTROL_SESSION + currentSessionID + CONTROL_PLAY);
                                    // send the pause command
                                } else {
                                    handler.sendCommand(CONTROL_SESSION + currentSessionID + CONTROL_PAUSE);
                                }
                            } else {
                                logger.warn("The channel {} receceived a command {}, this command is not supported",
                                        channelUID.getAsString(), command.toString());
                            }
                            break;
                        case CHANNEL_MUTE:
                            if (OnOffType.ON.equals(command)) {
                                handler.sendCommand(CONTROL_SESSION + currentSessionID + CONTROL_MUTE);
                            } else {
                                handler.sendCommand(CONTROL_SESSION + currentSessionID + CONTROL_UNMUTE);
                            }
                            break;
                        case CHANNEL_STOP:
                            if (OnOffType.ON.equals(command)) {
                                handler.sendCommand(CONTROL_SESSION + currentSessionID + CONTROL_STOP);
                            }
                            break;

                        case CHANNEL_SENDPLAYCOMMAND:
                            logger.trace("Sending the following payload: {} for device: {}", command.toString(),
                                    currentSessionID);
                            handler.sendCommand(CONTROL_SESSION + currentSessionID + CONTROL_SENDPLAY,
                                    command.toString());
                            break;

                        case CHANNEL_GENERALCOMMAND:
                            commandName = channel.getConfiguration().get(CHANNEL_GENERALCOMMAND_NAME).toString();
                            logger.trace("Sending the following command {} for device: {}", commandName,
                                    currentSessionID);
                            if (OnOffType.ON.equals(command)) {
                                handler.sendCommand(
                                        CONTROL_SESSION + currentSessionID + CONTROL_GENERALCOMMAND + commandName);
                            }
                            break;
                        case CHANNEL_GENERALCOMMANDWITHARGS:
                            commandName = channel.getConfiguration().get(CHANNEL_GENERALCOMMAND_NAME).toString();
                            logger.trace("Sending the following command {} for device: {}", commandName,
                                    currentSessionID);
                            handler.sendCommand(
                                    CONTROL_SESSION + currentSessionID + CONTROL_GENERALCOMMAND + commandName,
                                    "Arguments:" + command + "}");
                            break;
                        default:
                            logger.warn("The channel {} is not a supported channel", channelUID.getAsString());
                            break;
                    }
                }

            } else {
                updateStatus(OFFLINE, CONFIGURATION_ERROR,
                        "Unable to handle command, You must choose a Emby Server for this Device.");
            }
        }
    }

    private EmbyDeviceConfiguration validateConfiguration() {
        EmbyDeviceConfiguration embyDeviceConfig = new EmbyDeviceConfiguration(
                this.thing.getConfiguration().get(DEVICE_ID).toString());
        Configuration testConfig = this.thing.getChannel(CHANNEL_IMAGEURL).getConfiguration();
        if (!(testConfig.get(CHANNEL_IMAGEURL_MAXWIDTH) == null)) {
            embyDeviceConfig.imageMaxWidth = testConfig.get(CHANNEL_IMAGEURL_MAXWIDTH).toString();
        }
        if (!(testConfig.get(CHANNEL_IMAGEURL_MAXHEIGHT) == null)) {
            embyDeviceConfig.imageMaxHeight = testConfig.get(CHANNEL_IMAGEURL_MAXHEIGHT).toString();
        }
        String testPercentPlayed = "true";
        if (!(testConfig.get(CHANNEL_IMAGEURL_PERCENTPLAYED) == null)) {
            testPercentPlayed = testConfig.get(CHANNEL_IMAGEURL_PERCENTPLAYED).toString();
        }

        embyDeviceConfig.imageImageType = testConfig.get(CHANNEL_IMAGEURL_TYPE).toString();

        embyDeviceConfig.imagePercentPlayed = testPercentPlayed.equals("true");

        return embyDeviceConfig;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing emby device: {}", this.getThing().getLabel());
        config = validateConfiguration();
        updateStatus(ThingStatus.UNKNOWN);
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null || !(bridge.getHandler() instanceof EmbyBridgeHandler)) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "Can't initialize thing, You must choose a Emby Server for this Device.");
            return;
        }

        if (bridge.getStatus() == OFFLINE) {
            updateStatus(OFFLINE, BRIDGE_OFFLINE, "The Emby Server is currently offline.");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        bridgeHandler = (EmbyBridgeHandler) bridge.getHandler();
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
        updateState(CHANNEL_CURRENTTIME, createQuantityState(currentTime, Units.SECOND));
    }

    @Override
    public void updateDuration(long duration) {
        updateState(CHANNEL_DURATION, createQuantityState(duration, Units.SECOND));
    }

    /**
     * Wrap the given String in a new {@link StringType} or returns {@link UnDefType#UNDEF} if the String is empty.
     */
    private State createStringState(@Nullable String string) {
        if (string == null || string.isEmpty()) {
            return UnDefType.UNDEF;
        } else {
            return new StringType(string);
        }
    }

    private State createQuantityState(@Nullable Number value, Unit<?> unit) {
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
            try {
                URI imageURI = playstate.getPrimaryImageURL(hostname, embyport, config.imageImageType,
                        config.imageMaxWidth, config.imageMaxHeight);
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
                        e.getMessage());
            }
        } else {
            logger.trace("{} does not equal {} the event is for device named: {} ", playstate.getDeviceId(),
                    config.deviceID, playstate.getDeviceName());
        }
    }
}
