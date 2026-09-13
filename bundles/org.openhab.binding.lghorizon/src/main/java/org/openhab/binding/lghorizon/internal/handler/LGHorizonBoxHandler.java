/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lghorizon.internal.handler;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants;
import org.openhab.binding.lghorizon.internal.action.LGHorizonActions;
import org.openhab.binding.lghorizon.internal.api.LGHorizonKeys;
import org.openhab.binding.lghorizon.internal.api.dto.ChannelDto;
import org.openhab.binding.lghorizon.internal.api.dto.CustomerDto.DeviceDto;
import org.openhab.binding.lghorizon.internal.api.dto.EventDetailDto;
import org.openhab.binding.lghorizon.internal.api.dto.RecordingDetailDto;
import org.openhab.binding.lghorizon.internal.api.dto.VodDetailDto;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles a single LG Horizon set-top box. All actual network I/O goes through the parent
 * {@link LGHorizonAccountHandler}; this handler only translates channel commands to/from the box's MQTT protocol and
 * keeps openHAB channel state up to date as MQTT status/UI-status messages arrive.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public class LGHorizonBoxHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LGHorizonBoxHandler.class);
    private final LGHorizonDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    private String deviceId = "";
    private String profileId = "";

    // Last known running state, used to guard the power toggle key (see below). Defaults to {@code true}
    // (running) so an OFF command is never silently dropped before the first status message arrives; a
    // false-positive ON attempt against an already-running box is harmless (the box just ignores it), while
    // a dropped OFF command is a much worse failure mode to default into.
    private volatile boolean running = true;

    // Last known paused state, from the {@code speed} field of the most recent {@code CPE.uiStatus} message
    // (0 = paused). Used to guard the play/pause toggle key the same way {@link #running} guards power - see
    // the comment on the CHANNEL_PLAYER case below.
    private volatile boolean paused = false;

    // Last content id (eventId / VOD titleId / recordingId / app logoPath) we already resolved metadata
    // for, to avoid re-fetching from the network on every uiStatus message while the same content is still
    // playing.
    private volatile @Nullable String lastResolvedContentId;

    public LGHorizonBoxHandler(Thing thing, LGHorizonDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing);
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Override
    public void initialize() {
        LGHorizonBoxConfiguration config = getConfigAs(LGHorizonBoxConfiguration.class);
        if (config.deviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "deviceId must be set");
            return;
        }
        this.deviceId = config.deviceId;
        this.profileId = config.profileId;

        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        account.registerBox(deviceId, this);

        DeviceDto device = account.getAssignedDevice(deviceId);
        if (device != null) {
            String defaultProfileId = device.defaultProfileId;
            if (defaultProfileId != null) {
                updateProperty(LGHorizonBindingConstants.PROPERTY_DEFAULT_PROFILE_ID, defaultProfileId);
            }
            String deviceType = device.deviceType;
            if (deviceType != null) {
                updateProperty(LGHorizonBindingConstants.PROPERTY_DEVICE_TYPE, deviceType);
            }
            String platformType = device.platformType;
            if (platformType != null) {
                updateProperty(LGHorizonBindingConstants.PROPERTY_PLATFORM_TYPE, platformType);
            }
            String serialNumber = device.serialNumber;
            if (serialNumber != null) {
                updateProperty(LGHorizonBindingConstants.PROPERTY_SERIAL_NUMBER, serialNumber);
            }
            String wifiMacAddress = device.wifiMacAddress;
            if (wifiMacAddress != null) {
                updateProperty(LGHorizonBindingConstants.PROPERTY_WIFI_MAC_ADDRESS, wifiMacAddress);
            }
            String ethernetMacAddress = device.ethernetMacAddress;
            if (ethernetMacAddress != null) {
                updateProperty(LGHorizonBindingConstants.PROPERTY_ETHERNET_MAC_ADDRESS, ethernetMacAddress);
            }
        }
    }

    @Override
    public void dispose() {
        LGHorizonAccountHandler account = getAccountHandler();
        if (account != null && !deviceId.isBlank()) {
            account.unregisterBox(deviceId);
        }
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(LGHorizonActions.class);
    }

    private @Nullable LGHorizonAccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        Object handler = bridge.getHandler();
        return handler instanceof LGHorizonAccountHandler accountHandler ? accountHandler : null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        if (command instanceof RefreshType) {
            account.requestBoxState(deviceId);
            return;
        }

        switch (channelUID.getId()) {
            case LGHorizonBindingConstants.CHANNEL_POWER:
                // The box has a single physical "Power" toggle button, not separate discrete on/off keys -
                // Guard on the last known state so we never send the toggle in the wrong direction if our cached state
                // happens to be stale - that would turn the box off when asked to turn it on, or vice versa.
                if (command == OnOffType.ON) {
                    if (!running) {
                        account.sendKey(deviceId, LGHorizonKeys.POWER);
                    }
                } else if (command == OnOffType.OFF) {
                    if (running) {
                        account.sendKey(deviceId, LGHorizonKeys.POWER);
                    }
                }
                break;
            case LGHorizonBindingConstants.CHANNEL_PLAYER:
                // Like Power, the box has a single physical "Play/Pause" toggle button, not separate discrete
                // play/pause keys - gated on the box being ONLINE_RUNNING and on the current paused state, so the
                // toggle never fires in the wrong direction.
                if (command == PlayPauseType.PLAY) {
                    if (running && paused) {
                        account.sendKey(deviceId, LGHorizonKeys.PLAY_PAUSE);
                    }
                } else if (command == PlayPauseType.PAUSE) {
                    if (running && !paused) {
                        account.sendKey(deviceId, LGHorizonKeys.PLAY_PAUSE);
                    }
                } else if (command == RewindFastforwardType.FASTFORWARD) {
                    account.sendKey(deviceId, LGHorizonKeys.FAST_FORWARD);
                } else if (command == RewindFastforwardType.REWIND) {
                    account.sendKey(deviceId, LGHorizonKeys.REWIND);
                }
                break;
            case LGHorizonBindingConstants.CHANNEL_STOP:
                sendKeyOnPress(account, command, LGHorizonKeys.STOP);
                break;
            case LGHorizonBindingConstants.CHANNEL_RECORD:
                sendKeyOnPress(account, command, LGHorizonKeys.RECORD);
                break;
            case LGHorizonBindingConstants.CHANNEL_CHANNEL_UP:
                sendKeyOnPress(account, command, LGHorizonKeys.CHANNEL_UP);
                break;
            case LGHorizonBindingConstants.CHANNEL_CHANNEL_DOWN:
                sendKeyOnPress(account, command, LGHorizonKeys.CHANNEL_DOWN);
                break;
            case LGHorizonBindingConstants.CHANNEL_ARROW_UP:
                sendKeyOnPress(account, command, LGHorizonKeys.ARROW_UP);
                break;
            case LGHorizonBindingConstants.CHANNEL_ARROW_DOWN:
                sendKeyOnPress(account, command, LGHorizonKeys.ARROW_DOWN);
                break;
            case LGHorizonBindingConstants.CHANNEL_ARROW_LEFT:
                sendKeyOnPress(account, command, LGHorizonKeys.ARROW_LEFT);
                break;
            case LGHorizonBindingConstants.CHANNEL_ARROW_RIGHT:
                sendKeyOnPress(account, command, LGHorizonKeys.ARROW_RIGHT);
                break;
            case LGHorizonBindingConstants.CHANNEL_TOP_MENU:
                sendKeyOnPress(account, command, LGHorizonKeys.TOP_MENU);
                break;
            case LGHorizonBindingConstants.CHANNEL_INFO:
                sendKeyOnPress(account, command, LGHorizonKeys.INFO);
                break;
            case LGHorizonBindingConstants.CHANNEL_CONTEXT_MENU:
                sendKeyOnPress(account, command, LGHorizonKeys.CONTEXT_MENU);
                break;
            case LGHorizonBindingConstants.CHANNEL_TV:
                sendKeyOnPress(account, command, LGHorizonKeys.TV);
                break;
            case LGHorizonBindingConstants.CHANNEL_ENTER:
                sendKeyOnPress(account, command, LGHorizonKeys.ENTER);
                break;
            case LGHorizonBindingConstants.CHANNEL_ESCAPE:
                sendKeyOnPress(account, command, LGHorizonKeys.ESCAPE);
                break;
            case LGHorizonBindingConstants.CHANNEL_CHANNEL_NUMBER:
            case LGHorizonBindingConstants.CHANNEL_FAVORITE_CHANNEL_NUMBER:
                resolveChannelByNumber(account, command.toString()).ifPresentOrElse(
                        channel -> account.tuneToChannel(deviceId, channel.id),
                        () -> logger.warn("Unknown LG Horizon channel number '{}' for box {}", command, deviceId));
                break;
            case LGHorizonBindingConstants.CHANNEL_CHANNEL_NAME:
                resolveChannelByName(account, command.toString()).ifPresentOrElse(
                        channel -> account.tuneToChannel(deviceId, channel.id),
                        () -> logger.warn("Unknown LG Horizon channel name '{}' for box {}", command, deviceId));
                break;
            case LGHorizonBindingConstants.CHANNEL_KEY_CODE:
                account.sendKey(deviceId, command.toString());
                break;
            default:
                break;
        }
    }

    /**
     * Handles a command-only, stateless key channel (Switch, {@code autoUpdatePolicy="veto"}): only a press
     * (ON) does anything, matching how a physical remote button works - there's no meaningful "released"
     * state to react to, and OFF is never sent by these channels' own semantics anyway.
     */
    private void sendKeyOnPress(LGHorizonAccountHandler account, Command command, String w3cKey) {
        if (command == OnOffType.ON) {
            account.sendKey(deviceId, w3cKey);
        }
    }

    private Optional<ChannelDto> resolveChannelByNumber(LGHorizonAccountHandler account, String number) {
        return account.getChannels(getLanguage(account)).values().stream()
                .filter(c -> c.id != null && number.equals(c.logicalChannelNumber)).findFirst();
    }

    private Optional<ChannelDto> resolveChannelByName(LGHorizonAccountHandler account, String name) {
        // Fuzzy fallback: channel with the lowest logical channel number whose name contains the given text,
        // case-insensitive - e.g. "vtm" matches VTM HD/2/3/4/GOLD/series, and deterministically picks
        // whichever of those has the lowest channel number (usually the "main" one). Only used when no exact match
        // exists, so an exact name is never shadowed by a shorter, unrelated channel that happens to contain it as a
        // substring. A channel with a missing or non-numeric channel number sorts last, never winning over
        // one with a real number.
        String needle = name.toLowerCase();
        return account.getChannels(getLanguage(account)).values().stream()
                .filter(c -> c.id != null && c.name != null && c.name.toLowerCase().contains(needle))
                .min(Comparator.comparingInt(this::channelNumberOrMax));
    }

    private int channelNumberOrMax(ChannelDto channel) {
        String number = channel.logicalChannelNumber;
        if (number == null) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Populates the five channel-number selection channels' dynamic state options (channel number as the
     * value, channel name as the label): the unfiltered {@code channel-number}, plus four filtered subsets
     * crossing "TV vs radio" with "favorite vs not" - {@code tv-channel-number}, {@code radio-channel-number},
     * {@code favorite-tv-channel-number}, {@code favorite-radio-channel-number}. All five accept the same
     * channel numbers as commands (a channel is addressed identically regardless of which list it appeared
     * in); they only differ in which numbers show up as options, and which one reflects the currently
     * playing channel - see {@link #updateChannelFromId}.
     */
    public void updateChannelNumberOptions() {
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        Map<String, ChannelDto> channels = account.getChannels(getLanguage(account));
        List<String> favoriteIds = account.getFavoriteChannelIds(resolveProfileId(account));

        setChannelNumberOptions(LGHorizonBindingConstants.CHANNEL_CHANNEL_NUMBER, channels.values().stream());
        setChannelNumberOptions(LGHorizonBindingConstants.CHANNEL_FAVORITE_CHANNEL_NUMBER,
                channels.values().stream().filter(c -> favoriteIds.contains(c.id)));
    }

    private void setChannelNumberOptions(String channelId, Stream<ChannelDto> channels) {
        List<StateOption> options = channels.filter(c -> c.logicalChannelNumber != null && c.name != null)
                .sorted(Comparator.comparingInt(this::channelNumberOrMax))
                .map(c -> new StateOption(c.logicalChannelNumber, c.name)).toList();
        dynamicStateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), channelId), options);
    }

    /**
     * Displays an on-screen message. Called by {@link LGHorizonActions}. {@code duration} falls
     * back to {@link LGHorizonBindingConstants#DEFAULT_DISPLAY_MESSAGE_DURATION_SECONDS} when not supplied.
     */
    public void displayMessage(String message, @Nullable Integer duration) {
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        int resolvedDuration = duration != null ? duration
                : LGHorizonBindingConstants.DEFAULT_DISPLAY_MESSAGE_DURATION_SECONDS;
        account.displayMessage(deviceId, message, resolvedDuration);
    }

    private String getLanguage(LGHorizonAccountHandler account) {
        DeviceDto device = account.getAssignedDevice(deviceId);
        if (device == null) {
            return "en";
        }
        String profileId = this.profileId.isBlank() ? device.defaultProfileId : this.profileId;
        return account.getLanguageForProfile(profileId);
    }

    // ------------------------------------------------------------------
    // Inbound updates, called by LGHorizonAccountHandler
    // ------------------------------------------------------------------

    /** Handles a {@code .../status} message: coarse running state (online/standby/offline). */
    public void handleStatusMessage(String rawState) {
        running = "ONLINE_RUNNING".equalsIgnoreCase(rawState);
        boolean reachable = running || "ONLINE_STANDBY".equalsIgnoreCase(rawState);

        updateState(LGHorizonBindingConstants.CHANNEL_POWER, OnOffType.from(running));

        if (reachable) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.box-reports-state [\"" + rawState + "\"]");
        }
    }

    /** Handles a {@code CPE.uiStatus} message: what is actually playing right now. */
    public void handleUiStatusMessage(JsonObject payload) {
        updateStatus(ThingStatus.ONLINE);
        if (!payload.has("status") || !payload.get("status").isJsonObject()) {
            return;
        }
        JsonObject status = payload.getAsJsonObject("status");
        String uiStatus = getString(status, "uiStatus").orElse("");

        if ("apps".equalsIgnoreCase(uiStatus)) {
            handleAppsState(status);
            return;
        }

        JsonObject playerState = status.has("playerState") && status.get("playerState").isJsonObject()
                ? status.getAsJsonObject("playerState")
                : null;
        if (playerState == null) {
            return;
        }

        String sourceType = getString(playerState, "sourceType").orElse(null);
        if (sourceType != null) {
            updateState(LGHorizonBindingConstants.CHANNEL_SOURCE_TYPE, new StringType(sourceType));
        }

        if (playerState.has("speed")) {
            int speed = playerState.get("speed").getAsInt();
            paused = speed == 0;
            State channelPlayerState = speed == 0 ? PlayPauseType.PAUSE
                    : speed < 0 ? RewindFastforwardType.REWIND
                            : speed > 1 ? RewindFastforwardType.FASTFORWARD : PlayPauseType.PLAY;
            updateState(LGHorizonBindingConstants.CHANNEL_PLAYER, channelPlayerState);
        }

        JsonObject source = playerState.has("source") && playerState.get("source").isJsonObject()
                ? playerState.getAsJsonObject("source")
                : null;
        if (source == null || sourceType == null || sourceType.isBlank()) {
            lastResolvedContentId = null;
            clearTitleMetadata();
            clearChannelSelections();
            updateState(LGHorizonBindingConstants.CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
            return;
        }

        switch (sourceType.toLowerCase(java.util.Locale.ROOT)) {
            case "linear", "reviewbuffer" -> {
                String channelId = getString(source, "channelId").orElse(null);
                if (channelId != null) {
                    updateChannelFromId(channelId);
                } else {
                    clearChannelSelections();
                }
                getString(source, "eventId").ifPresent(this::resolveEventMetadata);
            }
            case "replay" -> {
                clearChannelSelections();
                getString(source, "eventId").ifPresent(this::resolveEventMetadata);
            }
            case "vod" -> {
                clearChannelSelections();
                getString(source, "titleId").ifPresent(this::resolveVodMetadata);
            }
            case "ndvr" -> {
                clearChannelSelections();
                getString(source, "recordingId").ifPresent(this::resolveRecordingMetadata);
            }
            default -> {
                // localDVR and anything else: not implemented - reset all title/image channels
                lastResolvedContentId = null;
                clearTitleMetadata();
                clearChannelSelections();
                updateState(LGHorizonBindingConstants.CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
            }
        }
    }

    /**
     * Updates {@code channel-name} plus all five number-selection channels for the currently playing linear
     * channel: {@code channel-number} always gets the number, while {@code tv-channel-number}/
     * {@code radio-channel-number}/{@code favorite-tv-channel-number}/{@code favorite-radio-channel-number}
     * only get it when the channel actually qualifies for that particular list - {@link UnDefType#UNDEF}
     * otherwise, so switching to a channel that isn't a favorite (or isn't radio) doesn't leave a stale
     * number from whatever qualified before. If the channel id can't be resolved at all, every one of these
     * six channels is cleared via {@link #clearChannelSelections}.
     */
    private void updateChannelFromId(String channelId) {
        LGHorizonAccountHandler account = getAccountHandler();
        ChannelDto channel = account == null ? null : account.getChannels(getLanguage(account)).get(channelId);
        if (channel == null || account == null) {
            clearChannelSelections();
            return;
        }
        updateState(LGHorizonBindingConstants.CHANNEL_CHANNEL_NAME,
                channel.name != null ? new StringType(channel.name) : UnDefType.UNDEF);

        State numberState = toChannelNumberState(channel.logicalChannelNumber);
        updateState(LGHorizonBindingConstants.CHANNEL_CHANNEL_NUMBER, numberState);

        boolean isFavorite = account.getFavoriteChannelIds(resolveProfileId(account)).contains(channel.id);

        updateState(LGHorizonBindingConstants.CHANNEL_FAVORITE_CHANNEL_NUMBER,
                isFavorite ? numberState : UnDefType.UNDEF);
    }

    private State toChannelNumberState(@Nullable String logicalChannelNumber) {
        if (logicalChannelNumber == null) {
            return UnDefType.UNDEF;
        }
        try {
            return new DecimalType(logicalChannelNumber);
        } catch (NumberFormatException e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Clears {@code channel-name} and all five number-selection channels - used whenever the box is not
     * showing a specific, currently-known linear channel (VOD, a recording, replay, an app, localDVR, or an
     * unresolvable channel id), so none of them keep showing a stale value from whatever was on before.
     */
    private void clearChannelSelections() {
        updateState(LGHorizonBindingConstants.CHANNEL_CHANNEL_NAME, UnDefType.UNDEF);
        updateState(LGHorizonBindingConstants.CHANNEL_CHANNEL_NUMBER, UnDefType.UNDEF);
        updateState(LGHorizonBindingConstants.CHANNEL_FAVORITE_CHANNEL_NUMBER, UnDefType.UNDEF);
    }

    /** Whether {@code contentId} is still what we're currently resolving for - see {@link #resolveEventMetadata}. */
    private boolean isStillCurrent(String contentId) {
        return contentId.equals(lastResolvedContentId);
    }

    private void resolveEventMetadata(String eventId) {
        if (eventId.equals(lastResolvedContentId)) {
            return;
        }
        lastResolvedContentId = eventId;
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        String language = getLanguage(account);
        scheduler.execute(() -> {
            EventDetailDto detail = account.getEventDetail(eventId, language);
            if (!isStillCurrent(eventId)) {
                // A newer request superseded this one while the REST call was in flight.
                return;
            }
            if (detail == null) {
                clearTitleMetadata();
                updateState(LGHorizonBindingConstants.CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
                lastResolvedContentId = null;
                return;
            }
            // For linear/reviewBuffer/replay, "title" is the show/program title and "episodeName" is the episode's own
            // title - both already unambiguous.
            applyTitleMetadata(detail.title, detail.episodeName, detail.seasonNumber, detail.episodeNumber);

            String imageUrl = null;
            if (detail.channelId != null) {
                ChannelDto channel = account.getChannels(language).get(detail.channelId);
                imageUrl = channel == null ? null : channel.getStreamImage();
            }
            updateImageFromUrlOrClear(eventId, imageUrl);
        });
    }

    private void resolveVodMetadata(String titleId) {
        if (titleId.equals(lastResolvedContentId)) {
            return;
        }
        lastResolvedContentId = titleId;
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        String language = getLanguage(account);
        String effectiveProfileId = resolveProfileId(account);
        scheduler.execute(() -> {
            VodDetailDto detail = account.getVodDetail(titleId, effectiveProfileId, language);
            if (!isStillCurrent(titleId)) {
                // A newer request superseded this one while the REST call was in flight.
                return;
            }
            if (detail == null) {
                clearTitleMetadata();
                updateState(LGHorizonBindingConstants.CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
                lastResolvedContentId = null;
                return;
            }
            // For an episode, the field "title" holds the EPISODE's own name - the show name is in the separate
            // "seriesTitle" field instead. For a movie, "title" is the movie's own title and there is no
            // series/episode concept.
            if (detail.isEpisode()) {
                applyTitleMetadata(detail.seriesTitle, detail.title, detail.season, detail.episode);
            } else {
                applyTitleMetadata(detail.title, null, null, null);
            }
            updateState(LGHorizonBindingConstants.CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
        });
    }

    private void resolveRecordingMetadata(String recordingId) {
        if (recordingId.equals(lastResolvedContentId)) {
            return;
        }
        lastResolvedContentId = recordingId;
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        String language = getLanguage(account);
        String effectiveProfileId = resolveProfileId(account);
        scheduler.execute(() -> {
            RecordingDetailDto detail = account.getRecordingDetail(recordingId, effectiveProfileId, language);
            if (!isStillCurrent(recordingId)) {
                // A newer request superseded this one while the REST call was in flight.
                return;
            }
            if (detail == null) {
                clearTitleMetadata();
                updateState(LGHorizonBindingConstants.CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
                lastResolvedContentId = null;
                return;
            }
            // nDVR is conditional on "source": for a "show"-sourced (standalone) recording, "title"
            // is the show name; for anything recorded as part of a series/season rule, the show name is in
            // the "showTitle" field instead - RecordingDetailDto.getShowTitle() already applies this distinction.
            applyTitleMetadata(detail.getShowTitle(), detail.episodeTitle, detail.seasonNumber, detail.episodeNumber);
            String imageUrl = null;
            if (detail.channelId != null) {
                ChannelDto channel = account.getChannels(language).get(detail.channelId);
                imageUrl = channel == null ? null : channel.getStreamImage();
            }
            updateImageFromUrlOrClear(recordingId, imageUrl);
        });
    }

    /**
     * Populates the five separate title/episode channels from whichever content type just resolved -
     * {@code programTitle} always mirrors the reference implementation's own "show_title" concept (computed
     * differently per content type, but always meant as *the* title), while {@code seriesTitle} is only
     * populated alongside it when the content is genuinely episodic (an episode title, season, or episode
     * number is present) - so a rule can check "is seriesTitle set" as a simple signal for "is this a TV
     * series episode" versus a movie or standalone broadcast. A missing episode title text falls back to a
     * plain "Episode N" using the episode number, if that's available.
     */
    private void applyTitleMetadata(@Nullable String programTitle, @Nullable String episodeTitleText,
            @Nullable Integer season, @Nullable Integer episode) {
        updateState(LGHorizonBindingConstants.CHANNEL_PROGRAM_TITLE,
                programTitle != null ? new StringType(programTitle) : UnDefType.UNDEF);

        boolean isEpisodic = (episodeTitleText != null && !episodeTitleText.isBlank()) || season != null
                || episode != null;
        updateState(LGHorizonBindingConstants.CHANNEL_SERIES_TITLE,
                isEpisodic && programTitle != null ? new StringType(programTitle) : UnDefType.UNDEF);

        String resolvedEpisodeTitle = episodeTitleText != null && !episodeTitleText.isBlank() ? episodeTitleText
                : episode != null ? "Episode " + episode : null;
        updateState(LGHorizonBindingConstants.CHANNEL_EPISODE_TITLE,
                resolvedEpisodeTitle != null ? new StringType(resolvedEpisodeTitle) : UnDefType.UNDEF);

        updateState(LGHorizonBindingConstants.CHANNEL_SEASON,
                season != null ? new DecimalType(season) : UnDefType.UNDEF);
        updateState(LGHorizonBindingConstants.CHANNEL_EPISODE,
                episode != null ? new DecimalType(episode) : UnDefType.UNDEF);
    }

    private void clearTitleMetadata() {
        applyTitleMetadata(null, null, null, null);
    }

    private String resolveProfileId(LGHorizonAccountHandler account) {
        DeviceDto device = account.getAssignedDevice(deviceId);
        if (device == null) {
            return "";
        }
        String resolved = this.profileId.isBlank() ? device.defaultProfileId : this.profileId;
        return resolved != null ? resolved : "";
    }

    /** Fetches the image bytes (a real network call) and updates the media-image channel. */
    private void updateImageFromUrl(String contentId, String url) {
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        RawType image = account.fetchImage(url);
        if (!isStillCurrent(contentId)) {
            // A newer request superseded this one while the REST call was in flight.
            return;
        }
        updateState(LGHorizonBindingConstants.CHANNEL_MEDIA_IMAGE, image != null ? image : UnDefType.UNDEF);
    }

    /** Clears media-image immediately if no image URL could be resolved at all, otherwise fetches it. */
    private void updateImageFromUrlOrClear(String contentId, @Nullable String url) {
        if (!isStillCurrent(contentId)) {
            // A newer request superseded this one while the REST call was in flight.
            return;
        }
        if (url == null) {
            updateState(LGHorizonBindingConstants.CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
            return;
        }
        updateImageFromUrl(contentId, url);
    }

    /** Handles the {@code status.appsState} shape used when {@code status.uiStatus} is {@code "apps"}. */
    private void handleAppsState(JsonObject status) {
        JsonObject appsState = status.has("appsState") && status.get("appsState").isJsonObject()
                ? status.getAsJsonObject("appsState")
                : null;
        if (appsState == null) {
            return;
        }
        updateState(LGHorizonBindingConstants.CHANNEL_SOURCE_TYPE, new StringType("app"));
        clearTitleMetadata();
        clearChannelSelections();
        getString(appsState, "appName").ifPresent(
                appName -> updateState(LGHorizonBindingConstants.CHANNEL_PROGRAM_TITLE, new StringType(appName)));

        String logoPath = getString(appsState, "logoPath").orElse(null);
        if (logoPath == null) {
            lastResolvedContentId = null;
            updateState(LGHorizonBindingConstants.CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
        } else if (!logoPath.equals(lastResolvedContentId)) {
            lastResolvedContentId = logoPath;
            scheduler.execute(() -> updateImageFromUrl(logoPath, logoPath));
        }
    }

    private static Optional<String> getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? Optional.of(obj.get(key).getAsString()) : Optional.empty();
    }
}
