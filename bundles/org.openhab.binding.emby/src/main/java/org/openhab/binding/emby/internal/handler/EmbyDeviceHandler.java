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

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.*;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
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
    private @Nullable EmbyPlayStateModel currentPlayState;
    private @Nullable EmbyBridgeHandler bridgeHandler;
    private @Nullable String lastImageUrl;
    private @Nullable String lastShowTitle;
    private @Nullable String lastMediaType;
    private boolean lastMuted = false;
    private long lastCurrentTime = -1;
    private long lastDuration = -1;

    private final EmbyThrottle throttle = new EmbyThrottle(1000);

    private TranslationProvider i18nProvider;

    private static final List<String> ALLOWED_IMAGE_TYPES = Collections.unmodifiableList(Arrays.asList("Primary", "Art",
            "Backdrop", "Banner", "Logo", "Thumb", "Disc", "Box", "Screenshot", "Menu", "Chapter"));

    public EmbyDeviceHandler(Thing thing, TranslationProvider i18nProvider) {
        super(thing);
        this.i18nProvider = i18nProvider;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(EmbyActions.class);
    }

    public void sendGeneralCommand(String commandName) {
        final EmbyBridgeHandler handler = bridgeHandler;
        final EmbyPlayStateModel play = currentPlayState;
        if (handler == null || play == null) {
            throw new IllegalStateException("Cannot send command: no bridge or no active session");
        }
        String url = CONTROL_SESSION + play.getId() + CONTROL_GENERALCOMMAND + commandName;
        handler.sendCommand(url);
    }

    public void sendGeneralCommandWithArgs(String commandName, String jsonArguments) {
        final EmbyBridgeHandler handler = bridgeHandler;
        final EmbyPlayStateModel play = currentPlayState;
        if (handler == null || play == null) {
            throw new IllegalStateException("Cannot send command: no bridge or no active session");
        }
        JsonObject args = JsonParser.parseString(jsonArguments).getAsJsonObject();
        JsonObject envelope = new JsonObject();
        envelope.add("Arguments", args);
        String url = CONTROL_SESSION + play.getId() + CONTROL_GENERALCOMMAND + commandName;
        handler.sendCommand(url, envelope.toString());
    }

    public void sendPlayWithParams(String itemIds, String playCommand, @Nullable Integer startPositionTicks,
            @Nullable String mediaSourceId, @Nullable Integer audioStreamIndex, @Nullable Integer subtitleStreamIndex,
            @Nullable Integer startIndex) {
        final EmbyBridgeHandler handler = bridgeHandler;
        final EmbyPlayStateModel play = currentPlayState;
        if (handler == null || play == null) {
            throw new IllegalStateException("No bridge or active session available");
        }

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

        String url = CONTROL_SESSION + play.getId() + CONTROL_SENDPLAY;
        handler.sendCommand(url, payload.toString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(channelUID, pollCurrentValue(channelUID));
            return;
        }

        final Channel channel = thing.getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("Unsupported channel: {}", channelUID);
            return;
        }

        final EmbyBridgeHandler handler = bridgeHandler;
        final EmbyDeviceConfiguration cfg = config;
        if (handler == null || cfg == null) {
            return;
        }

        final EmbyPlayStateModel play = currentPlayState;
        switch (channelUID.getId()) {
            case CHANNEL_CONTROL:
                if (play == null) {
                    updateState(channelUID, UnDefType.UNDEF);
                    return;
                }
                if (command instanceof PlayPauseType) {
                    String url = CONTROL_SESSION + play.getId()
                            + (PlayPauseType.PLAY.equals(command) ? CONTROL_PLAY : CONTROL_PAUSE);
                    handler.sendCommand(url);
                }
                break;

            case CHANNEL_MUTE:
                if (play == null) {
                    updateState(channelUID, UnDefType.UNDEF);
                    return;
                }
                String muteUrl = CONTROL_SESSION + play.getId()
                        + (OnOffType.ON.equals(command) ? CONTROL_MUTE : CONTROL_UNMUTE);
                handler.sendCommand(muteUrl);
                break;

            case CHANNEL_STOP:
                if (play == null) {
                    updateState(channelUID, UnDefType.UNDEF);
                    return;
                }
                if (OnOffType.ON.equals(command)) {
                    handler.sendCommand(CONTROL_SESSION + play.getId() + CONTROL_STOP);
                }
                break;

            default:
                logger.warn("Unsupported channel: {}", channelUID.getAsString());
                break;
        }
    }

    private State pollCurrentValue(ChannelUID channelUID) {
        final EmbyPlayStateModel play = currentPlayState;

        return switch (channelUID.getId()) {
            case CHANNEL_CONTROL -> {
                if (play == null) {
                    yield UnDefType.UNDEF;
                }
                Boolean paused = play.getEmbyPlayStatePausedState();
                yield Boolean.TRUE.equals(paused) ? PlayPauseType.PAUSE : PlayPauseType.PLAY;
            }
            case CHANNEL_MUTE -> {
                if (play == null) {
                    yield UnDefType.UNDEF;
                }
                Boolean muted = play.getEmbyMuteSate();
                yield Boolean.TRUE.equals(muted) ? OnOffType.ON : OnOffType.OFF;
            }
            case CHANNEL_STOP -> {
                if (play == null) {
                    yield UnDefType.UNDEF;
                }
                Boolean stopped = play.getEmbyPlayStatePausedState();
                yield Boolean.TRUE.equals(stopped) ? OnOffType.ON : OnOffType.OFF;
            }
            case CHANNEL_TITLE -> createStringState(play != null ? play.getNowPlayingName() : null);
            case CHANNEL_SHOWTITLE -> createStringState(lastShowTitle);
            case CHANNEL_MEDIATYPE -> createStringState(lastMediaType);
            case CHANNEL_CURRENTTIME -> (lastCurrentTime < 0) ? UnDefType.UNDEF
                    : createQuantityState(convertTicksToSeconds(lastCurrentTime), Units.SECOND);
            case CHANNEL_DURATION -> (lastDuration < 0) ? UnDefType.UNDEF
                    : createQuantityState(convertTicksToSeconds(lastDuration), Units.SECOND);
            case CHANNEL_IMAGEURL -> createStringState(lastImageUrl);
            default -> UnDefType.UNDEF;
        };
    }

    private void updateState(EmbyState state) {
        updatePlayerState(state);

        if (state == EmbyState.STOP || state == EmbyState.END) {
            // reset all last-* fields
            lastImageUrl = null;
            lastShowTitle = null;
            lastMediaType = null;
            lastMuted = false;
            lastCurrentTime = -1;
            lastDuration = -1;

            // restore original post-stop behavior
            updatePlayerState(state); // sets CONTROL and STOP channels
            updateState(CHANNEL_MUTE, OnOffType.from(false));

            updateTitle("");
            updateShowTitle("");
            updatePrimaryImageURL("");
            updateMediaType("");
            updateCurrentTime(-1);
            updateDuration(-1);
        }
    }

    @Override
    public void handleEvent(EmbyPlayStateModel playstate, String hostname, int embyport) {
        final EmbyDeviceConfiguration cfg = config;
        if (cfg == null || !playstate.compareDeviceId(cfg.deviceID)) {
            return;
        }
        this.currentPlayState = playstate;

        try {
            URI imageURI = playstate.getPrimaryImageURL(hostname, embyport, cfg.imageImageType, cfg.imageMaxWidth,
                    cfg.imageMaxHeight);

            if (playstate.getNowPlayingItem() == null) {
                updateState(EmbyState.END);
                updateState(EmbyState.STOP);
                return;
            }

            if (playstate.getEmbyPlayStatePausedState()) {
                logger.debug("Setting state to PAUSE for {}", playstate.getDeviceName());
                updateState(EmbyState.PAUSE);
            } else {
                logger.debug("Setting state to PLAY for {}", playstate.getDeviceName());
                updateState(EmbyState.PLAY);
            }

            // Image URL
            String newImage = imageURI.toString();
            if (!newImage.equals(lastImageUrl)) {
                updatePrimaryImageURL(newImage);
                logger.trace("Throttled updatePrimaryImageURL: {}", newImage);
                lastImageUrl = newImage;
            }

            // Mute (instant)
            boolean newMute = playstate.getEmbyMuteSate();
            if (newMute != lastMuted) {
                updateState(CHANNEL_MUTE, OnOffType.from(newMute));
                logger.trace("updateMuted: {}", newMute);
                lastMuted = newMute;
            }

            // Show Title
            String newTitle = playstate.getNowPlayingName();
            if (!newTitle.equals(lastShowTitle)) {
                updateShowTitle(newTitle);
                logger.trace("Throttled updateShowTitle: {}", newTitle);

                lastShowTitle = newTitle;
            }

            // CurrentTime
            long newTime = playstate.getNowPlayingTime().longValue();
            if (newTime != lastCurrentTime) {
                updateCurrentTime(newTime);
                logger.trace("Throttled updateCurrentTime: {}", newTime);

                lastCurrentTime = newTime;
            }

            // Duration
            long newDur = playstate.getNowPlayingTotalTime().longValue();
            if (newDur != lastDuration) {
                updateDuration(newDur);
                logger.trace("Throttled updateDuration: {}", newDur);

                lastDuration = newDur;
            }

            // MediaType
            String newType = playstate.getNowPlayingMediaType();
            if (!newType.equals(lastMediaType)) {
                updateMediaType(newType);
                logger.trace("Throttled updateMediaType: {}", newType);
                lastMediaType = newType;
            }

        } catch (URISyntaxException e) {
            logger.debug("Unable to create image URL for {}: {}", playstate.getDeviceName(), e.getMessage());
        }
    }

    private EmbyDeviceConfiguration validateConfiguration() throws ConfigValidationException {
        Object deviceId = requireNonNull(thing.getConfiguration().get(CONFIG_DEVICE_ID));
        if (deviceId.toString().isEmpty()) {
            throwValidationError(CONFIG_DEVICE_ID, "@text/thing.status.device.config.noDeviceID");
        }
        EmbyDeviceConfiguration cfg = new EmbyDeviceConfiguration(deviceId.toString());

        final Channel imgChannel = thing.getChannel(CHANNEL_IMAGEURL);
        if (imgChannel == null) {
            throwValidationError(CHANNEL_IMAGEURL,
                    "@text/thing.status.device.config.noChannelDefined: " + CHANNEL_IMAGEURL);
        } else {
            Configuration imgCfg = imgChannel.getConfiguration();
            String maxWidth = getOrDefault(imgCfg, CHANNEL_IMAGEURL_CONFIG_MAXWIDTH, "");
            if (!maxWidth.matches("\\d*")) {
                throwValidationError(CHANNEL_IMAGEURL_CONFIG_MAXWIDTH,
                        "@text/thing.status.device.config.notNumber" + CHANNEL_IMAGEURL_CONFIG_MAXWIDTH);
            } else {
                cfg.imageMaxWidth = maxWidth;
            }
            String maxHeight = getOrDefault(imgCfg, CHANNEL_IMAGEURL_CONFIG_MAXHEIGHT, "");
            if (!maxHeight.matches("\\d*")) {
                throwValidationError(CHANNEL_IMAGEURL_CONFIG_MAXHEIGHT,
                        "@text/thing.status.device.config.notNumber" + CHANNEL_IMAGEURL_CONFIG_MAXHEIGHT);
            }
            cfg.imageMaxHeight = maxHeight;
            String pct = getOrDefault(imgCfg, CHANNEL_IMAGEURL_CONFIG_PERCENTPLAYED, "false");
            if (!("true".equalsIgnoreCase(pct) || "false".equalsIgnoreCase(pct))) {
                throwValidationError(CHANNEL_IMAGEURL_CONFIG_PERCENTPLAYED,
                        "thing.status.device.config.booleanRequried" + CHANNEL_IMAGEURL_CONFIG_PERCENTPLAYED);
            }
            cfg.imagePercentPlayed = Boolean.parseBoolean(pct);

            String imgType = getOrDefault(imgCfg, CHANNEL_IMAGEURL_CONFIG_TYPE, "Primary");
            if (!ALLOWED_IMAGE_TYPES.contains(imgType)) {
                throwValidationError(CHANNEL_IMAGEURL_CONFIG_TYPE,
                        "@text/thing.status.device.config.invalidImageType" + imgType);
            }
            cfg.imageImageType = imgType;
        }
        return cfg;
    }

    private void throwValidationError(String parameterName, String errorMessage) throws ConfigValidationException {
        TranslationProvider provider = Objects.requireNonNull(i18nProvider,
                "TranslationProvider must not be null for validation");
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        ConfigValidationMessage msg = new ConfigValidationMessage(parameterName, "error", errorMessage);
        throw new ConfigValidationException(bundle, provider, Collections.singletonList(msg));
    }

    private String getOrDefault(Configuration config, String key, String defaultValue) {
        String val = (String) config.get(key);
        return val != null ? val : defaultValue;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                this.config = validateConfiguration();

                if (!(getBridge() instanceof Bridge bridge)
                        || !(bridge.getHandler() instanceof EmbyBridgeHandler bridgeHandler)) {
                    updateStatus(ThingStatus.OFFLINE, CONFIGURATION_ERROR, "@text/thing.status.device.noBridge");
                    return;
                }
                this.bridgeHandler = bridgeHandler;

                if (bridge.getStatus() == OFFLINE) {
                    updateStatus(OFFLINE, BRIDGE_OFFLINE, "@text/thing.status.device.bridgeOffline");
                    return;
                }

                updateStatus(ThingStatus.ONLINE);
            } catch (ConfigValidationException e) {
                updateStatus(ThingStatus.OFFLINE, CONFIGURATION_ERROR,
                        "@text/thing.status.device.configInValid " + e.getMessage());
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, COMMUNICATION_ERROR, "@text/thing.status.device.initalizationFalied");
                logger.error("Initialization failed: {}", e.getMessage());
            }
        });
    }

    @Override
    public void updateConnectionState(boolean connected) {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, COMMUNICATION_ERROR);
        }
    }

    @Override
    public void updateScreenSaverState(boolean screenSaveActive) {
        /* no-op */ }

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
    public void updateTitle(String title) {
        if (throttle.shouldProceed("updateTitle")) {
            updateState(CHANNEL_TITLE, createStringState(title));
        }
    }

    @Override
    public void updatePrimaryImageURL(String imageUrl) {
        if (throttle.shouldProceed("updatePrimaryImageURL")) {
            updateState(CHANNEL_IMAGEURL, createStringState(imageUrl));
        }
    }

    @Override
    public void updateShowTitle(String title) {
        if (throttle.shouldProceed("updateShowTitle")) {
            updateState(CHANNEL_SHOWTITLE, createStringState(title));
        }
    }

    @Override
    public void updateMediaType(String mediaType) {
        if (throttle.shouldProceed("updateMediaType")) {
            updateState(CHANNEL_MEDIATYPE, createStringState(mediaType));
        }
    }

    @Override
    public void updateCurrentTime(long currentTime) {
        if (throttle.shouldProceed("updateCurrentTime")) {
            updateState(CHANNEL_CURRENTTIME, createQuantityState(convertTicksToSeconds(currentTime), Units.SECOND));
        }
    }

    @Override
    public void updateDuration(long duration) {
        if (throttle.shouldProceed("updateDuration")) {
            updateState(CHANNEL_DURATION, createQuantityState(convertTicksToSeconds(duration), Units.SECOND));
        }
    }

    private State createStringState(@Nullable String string) {
        return (string == null || string.isBlank()) ? UnDefType.UNDEF : new StringType(string);
    }

    private State createQuantityState(@Nullable Number value, Unit<?> unit) {
        return (value == null) ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    public static double convertTicksToSeconds(long ticks) {
        double raw = ticks / 10_000_000.0;
        return Math.round(raw * 10.0) / 10.0;
    }
}
