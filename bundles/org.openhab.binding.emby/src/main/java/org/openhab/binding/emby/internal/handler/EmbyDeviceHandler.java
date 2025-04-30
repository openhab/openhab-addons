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
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL_CONFIG_MAXHEIGHT;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL_CONFIG_MAXWIDTH;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL_CONFIG_PERCENTPLAYED;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_IMAGEURL_CONFIG_TYPE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_MEDIATYPE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CHANNEL_MUTE;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.EmbyDeviceConfiguration;
import org.openhab.binding.emby.internal.EmbyEventListener;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.openhab.binding.emby.internal.util.EmbyThrottle;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.validation.ConfigValidationException;
import org.openhab.core.config.core.validation.ConfigValidationMessage;
import org.openhab.core.i18n.TranslationProvider;
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
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link EmbyDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyDeviceHandler extends BaseThingHandler implements EmbyEventListener {

    private final Logger logger = LoggerFactory.getLogger(EmbyDeviceHandler.class);
    private @Nullable EmbyDeviceConfiguration config;
    private @Nullable EmbyPlayStateModel currentPlayState = null;
    private @Nullable EmbyBridgeHandler bridgeHandler;
    private static final long MIN_UPDATE_INTERVAL_MS = 1000; // 1 second
    private final EmbyThrottle throttle = new EmbyThrottle(1000); // 1 second throttle for all events
    private @Nullable String lastImageUrl = null;
    private @Nullable String lastShowTitle = null;
    private @Nullable String lastMediaType = null;
    private boolean lastMuted = false;
    private long lastCurrentTime = -1;
    private long lastDuration = -1;
    @Reference
    private @Nullable TranslationProvider i18nProvider;

    private static final List<String> ALLOWED_IMAGE_TYPES = Collections.unmodifiableList(Arrays.asList("Primary", "Art",
            "Backdrop", "Banner", "Logo", "Thumb", "Disc", "Box", "Screenshot", "Menu", "Chapter"));

    public EmbyDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(EmbyActions.class);
    }

    public void sendGeneralCommand(String commandName) {
        if (bridgeHandler == null || currentPlayState == null) {
            throw new IllegalStateException("Cannot send command: no bridge or no active session");
        }
        String sessionId = currentPlayState.getId();
        String url = CONTROL_SESSION + sessionId + CONTROL_GENERALCOMMAND + commandName;
        bridgeHandler.sendCommand(url);
    }

    /**
     * Send a general Emby command with a JSON‐formatted arguments blob.
     *
     * @param commandName the generic command name (e.g. "SetVolume", "DisplayMessage", etc.)
     * @param jsonArguments a JSON string of the form `{ "Name": "Value", … }`
     */
    public void sendGeneralCommandWithArgs(String commandName, String jsonArguments) {
        if (bridgeHandler == null || currentPlayState == null) {
            throw new IllegalStateException("Cannot send command: no bridge or no active session");
        }
        String sessionId = currentPlayState.getId();

        // parse the user-supplied JSON into a JsonObject
        JsonObject args = JsonParser.parseString(jsonArguments).getAsJsonObject();

        // envelope around arguments
        JsonObject envelope = new JsonObject();
        envelope.add("Arguments", args);

        String url = CONTROL_SESSION + sessionId + CONTROL_GENERALCOMMAND + commandName;
        bridgeHandler.sendCommand(url, envelope.toString());
    }

    public void sendPlayWithParams(String itemIds, String playCommand, @Nullable Integer startPositionTicks,
            @Nullable String mediaSourceId, @Nullable Integer audioStreamIndex, @Nullable Integer subtitleStreamIndex,
            @Nullable Integer startIndex) {
        // make sure we have a bridge + session
        if (bridgeHandler == null || currentPlayState == null) {
            throw new IllegalStateException("No bridge or active session available");
        }

        String sessionId = currentPlayState.getId();

        JsonObject payload = new JsonObject();
        payload.addProperty("ItemIds", itemIds);
        payload.addProperty("PlayCommand", playCommand);
        if (startPositionTicks != null) {
            payload.addProperty("StartPositionTicks", startPositionTicks);
        }
        if (mediaSourceId != null) {
            payload.addProperty("MediaSourceId", mediaSourceId);
        }
        if (audioStreamIndex != null) {
            payload.addProperty("AudioStreamIndex", audioStreamIndex);
        }
        if (subtitleStreamIndex != null) {
            payload.addProperty("SubtitleStreamIndex", subtitleStreamIndex);
        }
        if (startIndex != null) {
            payload.addProperty("StartIndex", startIndex);
        }

        String url = CONTROL_SESSION + sessionId + CONTROL_SENDPLAY;
        bridgeHandler.sendCommand(url, payload.toString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(channelUID, pollCurrentValue(channelUID));
            return;
        }

        Channel channel = this.thing.getChannel(channelUID.getId());
        if (channel != null) {
            if (bridgeHandler != null && config != null) {
                EmbyBridgeHandler handler = bridgeHandler;
                if (currentPlayState != null) {
                    String currentSessionID = currentPlayState.getId();
                    switch (channelUID.getId()) {
                        case CHANNEL_CONTROL:
                            if (command instanceof PlayPauseType) {
                                if (PlayPauseType.PLAY.equals(command)) {
                                    handler.sendCommand(CONTROL_SESSION + currentSessionID + CONTROL_PLAY);
                                } else {
                                    handler.sendCommand(CONTROL_SESSION + currentSessionID + CONTROL_PAUSE);
                                }
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

                        default:
                            logger.warn("Unsupported channel: {}", channelUID.getAsString());
                            break;
                    }
                }
            } else {
                updateStatus(OFFLINE, CONFIGURATION_ERROR, "No Emby server bridge linked or configuration invalid.");
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
                Boolean stopped = currentPlayState.getEmbyPlayStatePausedState();
                return Boolean.TRUE.equals(stopped) ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_TITLE:
                return createStringState(currentPlayState != null ? currentPlayState.getNowPlayingName() : null);
            case CHANNEL_SHOWTITLE:
                return createStringState(lastShowTitle);
            case CHANNEL_MEDIATYPE:
                return createStringState(lastMediaType);
            case CHANNEL_CURRENTTIME:
                return (lastCurrentTime < 0) ? UnDefType.UNDEF
                        : createQuantityState(convertNanoTimeToSeconds(lastCurrentTime), Units.SECOND);
            case CHANNEL_DURATION:
                return (lastDuration < 0) ? UnDefType.UNDEF
                        : createQuantityState(convertNanoTimeToSeconds(lastDuration), Units.SECOND);
            case CHANNEL_IMAGEURL:
                return createStringState(lastImageUrl);
            default:
                return UnDefType.UNDEF;
        }
    }

    private String getOrDefault(Configuration config, String key, String defaultValue) {
        String value = (String) config.get(key);
        return value != null ? value : defaultValue;
    }

    private EmbyDeviceConfiguration validateConfiguration() throws ConfigValidationException {
        Object deviceId = this.thing.getConfiguration().get(DEVICE_ID);
        if (deviceId == null || deviceId.toString().isEmpty()) {
            throwValidationError(DEVICE_ID, "Missing value for key: " + DEVICE_ID);
        }
        EmbyDeviceConfiguration embyDeviceConfig = new EmbyDeviceConfiguration(deviceId.toString());

        Configuration imgCfg = this.thing.getChannel(CHANNEL_IMAGEURL).getConfiguration();

        String maxWidth = getOrDefault(imgCfg, CHANNEL_IMAGEURL_CONFIG_MAXWIDTH, "");
        if (!maxWidth.matches("\\d*")) {
            throwValidationError(CHANNEL_IMAGEURL_CONFIG_MAXWIDTH, "Image max width must be a number");
        }
        embyDeviceConfig.imageMaxWidth = maxWidth;

        String maxHeight = getOrDefault(imgCfg, CHANNEL_IMAGEURL_CONFIG_MAXHEIGHT, "");
        if (!maxHeight.matches("\\d*")) {
            throwValidationError(CHANNEL_IMAGEURL_CONFIG_MAXHEIGHT, "Image max height must be a number");
        }
        embyDeviceConfig.imageMaxHeight = maxHeight;

        String pctPlayed = getOrDefault(imgCfg, CHANNEL_IMAGEURL_CONFIG_PERCENTPLAYED, "false");
        if (!("true".equalsIgnoreCase(pctPlayed) || "false".equalsIgnoreCase(pctPlayed))) {
            throwValidationError(CHANNEL_IMAGEURL_CONFIG_PERCENTPLAYED, "Image percent-played must be true or false");
        }
        embyDeviceConfig.imagePercentPlayed = Boolean.parseBoolean(pctPlayed);

        String imgType = getOrDefault(imgCfg, CHANNEL_IMAGEURL_CONFIG_TYPE, "Primary");
        if (!ALLOWED_IMAGE_TYPES.contains(imgType)) {
            throwValidationError(CHANNEL_IMAGEURL_CONFIG_TYPE,
                    "Invalid image type: " + imgType + ". Allowed values: " + ALLOWED_IMAGE_TYPES);
        }
        embyDeviceConfig.imageImageType = imgType;

        return embyDeviceConfig;
    }

    private void throwValidationError(String parameterName, String errorMessage) throws ConfigValidationException {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        ConfigValidationMessage validationMessage = new ConfigValidationMessage(parameterName, "error", errorMessage);
        throw new ConfigValidationException(bundle, i18nProvider, Collections.singletonList(validationMessage));
    }

    public static double convertNanoTimeToSeconds(long nanoTime) {
        return nanoTime / 1_000_000_000.0;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Initializing Emby device");
        scheduler.execute(() -> {
            try {
                EmbyDeviceConfiguration cfg = validateConfiguration();
                this.config = cfg;

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

                this.bridgeHandler = (EmbyBridgeHandler) bridge.getHandler();
                updateStatus(ThingStatus.ONLINE);
            } catch (ConfigValidationException cve) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configuration error: " + cve.getMessage());
            } catch (Exception e) {
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
            lastImageUrl = null;
            lastShowTitle = null;
            lastMediaType = null;
            lastMuted = false;
            lastCurrentTime = -1;
            lastDuration = -1;

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
            updateState(CHANNEL_CURRENTTIME, createQuantityState(convertNanoTimeToSeconds(currentTime), Units.SECOND));
            logger.trace("Throttled updateCurrentTime: {}", currentTime);
        } else {
            logger.trace("Skipped updateCurrentTime to throttle frequency");
        }
    }

    @Override
    public void updateDuration(long duration) {
        if (throttle.shouldProceed("updateDuration")) {
            updateState(CHANNEL_DURATION, createQuantityState(convertNanoTimeToSeconds(duration), Units.SECOND));
        }
    }

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
        if (config != null && playstate.compareDeviceId(config.deviceID)) {
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
                    config != null ? config.deviceID : "null", playstate.getDeviceName());
        }
    }
}
