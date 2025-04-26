/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import java.net.URI;
import java.net.URISyntaxException;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.EmbyDeviceConfiguration;
import org.openhab.binding.emby.internal.EmbyEventListener;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.openhab.binding.emby.internal.util.EmbyThrottle;
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
import org.openhab.core.types.RefreshType;
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
    private static final long MIN_UPDATE_INTERVAL_MS = 1000; // 1 second
    private volatile long lastUpdateCurrentTime = 0;
    private final EmbyThrottle throttle = new EmbyThrottle(1000); // 1 second throttle for all events
    private @Nullable String lastImageUrl = null;
    private @Nullable String lastShowTitle = null;
    private @Nullable String lastMediaType = null;
    private boolean lastMuted = false;
    private long lastCurrentTime = -1;
    private long lastDuration = -1;

    public EmbyDeviceHandler(Thing thing) {
        super(thing);
        config = validateConfiguration();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // 1) Handle REFRESH requests immediately
        if (command instanceof RefreshType) {
            updateState(channelUID, pollCurrentValue(channelUID));
            return;
        }
        // if we are in an active playstate then processs the command
        Channel channel = this.thing.getChannel(channelUID.getId());
        if (channel != null) {
            String commandName;
            if (bridgeHandler != null) {
                EmbyBridgeHandler handler = bridgeHandler;
                logger.debug("The channel ID of the received command is: {}", channelUID.getId());
                if (!(currentPlayState == null)) {
                    String currentSessionID = currentPlayState.getId();
                    logger.debug("The device Id is: {}, the received deviceID is: {} ", currentSessionID,
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

    private State pollCurrentValue(ChannelUID channelUID) {
        String id = channelUID.getId();
        switch (id) {
            case CHANNEL_CONTROL:
                if (currentPlayState == null) {
                    return UnDefType.UNDEF;
                }
                // if paused → PAUSE, otherwise PLAY
                Boolean paused = currentPlayState.getEmbyPlayStatePausedState();
                return Boolean.TRUE.equals(paused) ? PlayPauseType.PAUSE : PlayPauseType.PLAY;

            case CHANNEL_MUTE:
                if (currentPlayState == null) {
                    return UnDefType.UNDEF;
                }
                Boolean muted = currentPlayState.getEmbyMuteSate();
                return Boolean.TRUE.equals(muted) ? OnOffType.ON : OnOffType.OFF;

            case CHANNEL_STOP:
                if (currentPlayState == null) {
                    return UnDefType.UNDEF;
                }
                // STOP channel is ON when playback is paused/stopped
                Boolean stopped = currentPlayState.getEmbyPlayStatePausedState();
                return Boolean.TRUE.equals(stopped) ? OnOffType.ON : OnOffType.OFF;

            case CHANNEL_TITLE:
                // The item name, e.g. movie title
                return createStringState(currentPlayState != null ? currentPlayState.getNowPlayingName() : null);

            case CHANNEL_SHOWTITLE:
                // The “show” title, cached from last event
                return createStringState(lastShowTitle);

            case CHANNEL_MEDIATYPE:
                return createStringState(lastMediaType);

            case CHANNEL_CURRENTTIME:
                // seconds since start
                return (lastCurrentTime < 0) ? UnDefType.UNDEF : createQuantityState(lastCurrentTime, Units.SECOND);

            case CHANNEL_DURATION:
                return (lastDuration < 0) ? UnDefType.UNDEF : createQuantityState(lastDuration, Units.SECOND);

            case CHANNEL_IMAGEURL:
                return createStringState(lastImageUrl);

            default:
                return UnDefType.UNDEF;
        }
    }

    private EmbyDeviceConfiguration validateConfiguration() {
        EmbyDeviceConfiguration embyDeviceConfig = new EmbyDeviceConfiguration(
                String.valueOf(this.thing.getConfiguration().get(DEVICE_ID)));

        Configuration testConfig = this.thing.getChannel(CHANNEL_IMAGEURL).getConfiguration();

        Object maxWidth = testConfig.get(CHANNEL_IMAGEURL_MAXWIDTH);
        embyDeviceConfig.imageMaxWidth = (maxWidth != null) ? maxWidth.toString() : "";

        Object maxHeight = testConfig.get(CHANNEL_IMAGEURL_MAXHEIGHT);
        embyDeviceConfig.imageMaxHeight = (maxHeight != null) ? maxHeight.toString() : "";

        Object percentPlayed = testConfig.get(CHANNEL_IMAGEURL_PERCENTPLAYED);
        embyDeviceConfig.imagePercentPlayed = "true".equals(String.valueOf(percentPlayed));

        Object imageType = testConfig.get(CHANNEL_IMAGEURL_TYPE);
        embyDeviceConfig.imageImageType = (imageType != null) ? imageType.toString() : "";

        return embyDeviceConfig;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing emby device: {}", getThing().getLabel());

        // 1) Immediately show “initializing”
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Initializing Emby device");

        // 2) Offload the actual setup to the scheduler
        scheduler.execute(() -> {
            try {
                // Validate config early
                config = validateConfiguration();

                Bridge bridge = getBridge();
                if (bridge == null || bridge.getHandler() == null
                        || !(bridge.getHandler() instanceof EmbyBridgeHandler)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "You must choose an Emby Server for this Device");
                    return;
                }

                if (bridge.getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "The Emby Server is currently offline");
                    return;
                }

                // All checks passed—link the bridge handler and go ONLINE
                bridgeHandler = (EmbyBridgeHandler) bridge.getHandler();
                updateStatus(ThingStatus.ONLINE);
            } catch (ConfigValidationException cve) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configuration error: " + cve.getMessage());
            } catch (Exception e) {
                // Unexpected failure during init
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Initialization failed: " + e.getMessage());
            } catch (InterruptedException ie) {
                // Preserve interrupt and stop initialization
                Thread.currentThread().interrupt();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Initialization interrupted");
            } catch (Exception e) {
                // Unexpected failure during init
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Initialization failed: " + e.getMessage());
            }
        });
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
        if (throttle.shouldProceed("updateTitle")) {
            updateState(CHANNEL_TITLE, createStringState(title));
        }
    }

    private void updateState(EmbyState state) {
        updatePlayerState(state);
        if (state == EmbyState.STOP) {
            // Clear cached values on STOP
            lastImageUrl = null;
            lastShowTitle = null;
            lastMediaType = null;
            lastMuted = false;
            lastCurrentTime = -1;
            lastDuration = -1;

            // Clear openHAB state channels
            updateTitle("");
            updateShowTitle("");
            updatePrimaryImageURL("");
            updateMediaType("");
            updateDuration(-1);
            updateCurrentTime(-1);
        }
    }

    @Override
    public void updatePrimaryImageURL(String imageUrl) {
        if (throttle.shouldProceed("updatePrimaryImageURL")) {
            updateState(CHANNEL_IMAGEURL, createStringState(imageUrl));
            logger.trace("Throttled updatePrimaryImageURL: {}", imageUrl);
        } else {
            logger.trace("Skipped updatePrimaryImageURL to throttle frequency");
        }
    }

    @Override
    public void updateShowTitle(String title) {
        if (throttle.shouldProceed("updateShowTitle")) {
            updateState(CHANNEL_SHOWTITLE, createStringState(title));
            logger.trace("Throttled updateShowTitle: {}", title);
        } else {
            logger.trace("Skipped updateShowTitle to throttle frequency");
        }
    }

    @Override
    public void updateMediaType(String mediaType) {
        if (throttle.shouldProceed("updateMediaType")) {
            updateState(CHANNEL_MEDIATYPE, createStringState(mediaType));
            logger.trace("Throttled updateMediaType: {}", mediaType);
        } else {
            logger.trace("Skipped updateMediaType to throttle frequency");
        }
    }

    @Override
    public void updateCurrentTime(long currentTime) {
        if (throttle.shouldProceed("updateCurrentTime")) {
            updateState(CHANNEL_CURRENTTIME, createQuantityState(currentTime, Units.SECOND));
            logger.trace("Throttled updateCurrentTime: {}", currentTime);
        } else {
            logger.trace("Skipped updateCurrentTime to throttle frequency");
        }
    }

    @Override
    public void updateDuration(long duration) {
        if (throttle.shouldProceed("updateDuration")) {
            updateState(CHANNEL_DURATION, createQuantityState(duration, Units.SECOND));
        }
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
                    String newImageUrl = imageURI.toString();
                    if (!newImageUrl.equals(lastImageUrl)) {
                        updatePrimaryImageURL(newImageUrl);
                        lastImageUrl = newImageUrl;
                    }

                    boolean newMuteState = playstate.getEmbyMuteSate();
                    if (newMuteState != lastMuted) {
                        updateMuted(newMuteState);
                        lastMuted = newMuteState;
                    }

                    String newShowTitle = playstate.getNowPlayingName();
                    if (!newShowTitle.equals(lastShowTitle)) {
                        updateShowTitle(newShowTitle);
                        lastShowTitle = newShowTitle;
                    }

                    long newCurrentTime = playstate.getNowPlayingTime().longValue();
                    if (newCurrentTime != lastCurrentTime) {
                        updateCurrentTime(newCurrentTime);
                        lastCurrentTime = newCurrentTime;
                    }

                    long newDuration = playstate.getNowPlayingTotalTime().longValue();
                    if (newDuration != lastDuration) {
                        updateDuration(newDuration);
                        lastDuration = newDuration;
                    }

                    String newMediaType = playstate.getNowPlayingMediaType();
                    if (!newMediaType.equals(lastMediaType)) {
                        updateMediaType(newMediaType);
                        lastMediaType = newMediaType;
                    }
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
