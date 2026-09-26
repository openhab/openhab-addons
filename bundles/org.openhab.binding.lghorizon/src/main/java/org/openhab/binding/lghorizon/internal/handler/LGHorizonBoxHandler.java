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

import static org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants.*;
import static org.openhab.binding.lghorizon.internal.api.LGHorizonApiConstants.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class LGHorizonBoxHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LGHorizonBoxHandler.class);
    private final LGHorizonDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    private String deviceId = "";
    private String profileId = "";

    // Last known running state, used to guard the power toggle key
    private volatile boolean running = true;

    // Last known paused state, used to guard the play/pause toggle key
    private volatile boolean paused = false;

    // Last content id (eventId / VOD titleId / recordingId / app logoPath) we already resolved metadata for
    private volatile @Nullable String lastResolvedContentId;

    public LGHorizonBoxHandler(Thing thing, LGHorizonDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing);
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Override
    public void initialize() {
        LGHorizonBoxConfiguration config = getConfigAs(LGHorizonBoxConfiguration.class);
        if (config.deviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.box-missing-device-id");
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
        updateDeviceProperties();
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
        return bridge.getHandler() instanceof LGHorizonAccountHandler accountHandler ? accountHandler : null;
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
            case CHANNEL_POWER:
                // The box has a single physical "Power" toggle button, not separate discrete on/off keys
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
            case CHANNEL_PLAYER:
                // The box has a single physical "Play/Pause" toggle button, not separate discrete play/pause keys
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
            case CHANNEL_STOP:
                sendKeyOnPress(account, command, LGHorizonKeys.STOP);
                break;
            case CHANNEL_RECORD:
                sendKeyOnPress(account, command, LGHorizonKeys.RECORD);
                break;
            case CHANNEL_CHANNEL_UP:
                sendKeyOnPress(account, command, LGHorizonKeys.CHANNEL_UP);
                break;
            case CHANNEL_CHANNEL_DOWN:
                sendKeyOnPress(account, command, LGHorizonKeys.CHANNEL_DOWN);
                break;
            case CHANNEL_ARROW_UP:
                sendKeyOnPress(account, command, LGHorizonKeys.ARROW_UP);
                break;
            case CHANNEL_ARROW_DOWN:
                sendKeyOnPress(account, command, LGHorizonKeys.ARROW_DOWN);
                break;
            case CHANNEL_ARROW_LEFT:
                sendKeyOnPress(account, command, LGHorizonKeys.ARROW_LEFT);
                break;
            case CHANNEL_ARROW_RIGHT:
                sendKeyOnPress(account, command, LGHorizonKeys.ARROW_RIGHT);
                break;
            case CHANNEL_TOP_MENU:
                sendKeyOnPress(account, command, LGHorizonKeys.TOP_MENU);
                break;
            case CHANNEL_INFO:
                sendKeyOnPress(account, command, LGHorizonKeys.INFO);
                break;
            case CHANNEL_CONTEXT_MENU:
                sendKeyOnPress(account, command, LGHorizonKeys.CONTEXT_MENU);
                break;
            case CHANNEL_TV:
                sendKeyOnPress(account, command, LGHorizonKeys.TV);
                break;
            case CHANNEL_ENTER:
                sendKeyOnPress(account, command, LGHorizonKeys.ENTER);
                break;
            case CHANNEL_ESCAPE:
                sendKeyOnPress(account, command, LGHorizonKeys.ESCAPE);
                break;
            case CHANNEL_CHANNEL_NUMBER:
            case CHANNEL_FAVORITE_CHANNEL_NUMBER:
                resolveChannelByNumber(account, command.toString()).ifPresentOrElse(
                        channel -> account.tuneToChannel(deviceId, channel.id),
                        () -> logger.warn("Unknown LG Horizon channel number '{}' for box {}", command, deviceId));
                break;
            case CHANNEL_CHANNEL_NAME:
                resolveChannelByName(account, command.toString()).ifPresentOrElse(
                        channel -> account.tuneToChannel(deviceId, channel.id),
                        () -> logger.warn("Unknown LG Horizon channel name '{}' for box {}", command, deviceId));
                break;
            case CHANNEL_KEY_CODE:
                account.sendKey(deviceId, command.toString());
                break;
            default:
                break;
        }
    }

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
        // Fuzzy fallback: when exact match does not exist, pick the first channel whose name contains the requested
        // string (case-insensitive)
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
     * Update thing properties from fetched device data.
     */
    public void updateDeviceProperties() {
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        DeviceDto device = account.getAssignedDevice(deviceId);
        if (device != null) {
            String defaultProfileId = device.defaultProfileId;
            if (defaultProfileId != null) {
                updateProperty(PROPERTY_DEFAULT_PROFILE_ID, defaultProfileId);
            }
            String deviceType = device.deviceType;
            if (deviceType != null) {
                updateProperty(PROPERTY_DEVICE_TYPE, deviceType);
            }
            String platformType = device.platformType;
            if (platformType != null) {
                updateProperty(PROPERTY_PLATFORM_TYPE, platformType);
            }
            String serialNumber = device.serialNumber;
            if (serialNumber != null) {
                updateProperty(PROPERTY_SERIAL_NUMBER, serialNumber);
            }
            String wifiMacAddress = device.wifiMacAddress;
            if (wifiMacAddress != null) {
                updateProperty(PROPERTY_WIFI_MAC_ADDRESS, wifiMacAddress);
            }
            String ethernetMacAddress = device.ethernetMacAddress;
            if (ethernetMacAddress != null) {
                updateProperty(PROPERTY_ETHERNET_MAC_ADDRESS, ethernetMacAddress);
            }
        }
    }

    /**
     * Populates the channel-number selection channels' dynamic state options (channel number as the
     * value, channel name as the label).
     */
    public void updateChannelNumberOptions() {
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        Map<String, ChannelDto> channels = account.getChannels(getLanguage(account));
        List<String> favoriteIds = account.getFavoriteChannelIds(resolveProfileId(account));

        setChannelNumberOptions(CHANNEL_CHANNEL_NUMBER, channels.values().stream());
        setChannelNumberOptions(CHANNEL_FAVORITE_CHANNEL_NUMBER,
                channels.values().stream().filter(c -> favoriteIds.contains(c.id)));
    }

    private void setChannelNumberOptions(String channelId, Stream<ChannelDto> channels) {
        List<StateOption> options = channels.filter(c -> c.logicalChannelNumber != null && c.name != null)
                .sorted(Comparator.comparingInt(this::channelNumberOrMax))
                .map(c -> new StateOption(c.logicalChannelNumber, c.name)).toList();
        dynamicStateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), channelId), options);
    }

    /**
     * Displays an on-screen message. Called by {@link LGHorizonActions}.
     *
     * @param message
     * @param duration display duration, falls back to
     *            {@link org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants#DEFAULT_DISPLAY_MESSAGE_DURATION_SECONDS}
     *            when not supplied.
     */
    public void displayMessage(String message, @Nullable Integer duration) {
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        int resolvedDuration = duration != null ? duration : DEFAULT_DISPLAY_MESSAGE_DURATION_SECONDS;
        account.displayMessage(deviceId, message, resolvedDuration);
    }

    private String getLanguage(LGHorizonAccountHandler account) {
        DeviceDto device = account.getAssignedDevice(deviceId);
        if (device == null) {
            return DEFAULT_LANGUAGE;
        }
        String profileId = this.profileId.isBlank() ? device.defaultProfileId : this.profileId;
        return account.getLanguageForProfile(profileId);
    }

    /**
     * Handles a {@code .../status} message: coarse running state (online/standby/offline).
     *
     * @param rawState message payload
     */
    public void handleStatusMessage(String rawState) {
        running = BOX_STATE_ONLINE_RUNNING.equalsIgnoreCase(rawState);
        boolean reachable = running || BOX_STATE_ONLINE_STANDBY.equalsIgnoreCase(rawState);

        updateState(CHANNEL_POWER, OnOffType.from(running));

        if (reachable) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.box-reports-state [\"" + rawState + "\"]");
        }
    }

    /**
     * Handles a {@code CPE.uiStatus} message: what is actually playing right now.
     *
     * @param payload message payload
     */
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
            updateState(CHANNEL_SOURCE_TYPE, new StringType(sourceType));
        }

        if (playerState.has("speed")) {
            int speed = playerState.get("speed").getAsInt();
            paused = speed == 0;
            State channelPlayerState = speed == 0 ? PlayPauseType.PAUSE
                    : speed < 0 ? RewindFastforwardType.REWIND
                            : speed > 1 ? RewindFastforwardType.FASTFORWARD : PlayPauseType.PLAY;
            updateState(CHANNEL_PLAYER, channelPlayerState);
        }

        JsonObject source = playerState.has("source") && playerState.get("source").isJsonObject()
                ? playerState.getAsJsonObject("source")
                : null;
        if (source == null || sourceType == null || sourceType.isBlank()) {
            lastResolvedContentId = null;
            clearTitleMetadata();
            clearChannelSelections();
            updateState(CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
            return;
        }

        switch (sourceType.toLowerCase(java.util.Locale.ROOT)) {
            case SOURCE_TYPE_LINEAR, SOURCE_TYPE_REVIEWBUFFER -> {
                String channelId = getString(source, "channelId").orElse(null);
                if (channelId != null) {
                    updateChannelFromId(channelId);
                } else {
                    clearChannelSelections();
                }
                getString(source, "eventId").ifPresent(this::resolveEventMetadata);
            }
            case SOURCE_TYPE_REPLAY -> {
                clearChannelSelections();
                getString(source, "eventId").ifPresent(this::resolveEventMetadata);
            }
            case SOURCE_TYPE_VOD -> {
                clearChannelSelections();
                getString(source, "titleId").ifPresent(this::resolveVodMetadata);
            }
            case SOURCE_TYPE_NDVR -> {
                clearChannelSelections();
                getString(source, "recordingId").ifPresent(this::resolveRecordingMetadata);
            }
            default -> {
                // localDVR and anything else: not implemented
                lastResolvedContentId = null;
                clearTitleMetadata();
                clearChannelSelections();
                updateState(CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
            }
        }
    }

    private void updateChannelFromId(String channelId) {
        LGHorizonAccountHandler account = getAccountHandler();
        ChannelDto channel = account == null ? null : account.getChannels(getLanguage(account)).get(channelId);
        if (channel == null || account == null) {
            clearChannelSelections();
            return;
        }
        updateState(CHANNEL_CHANNEL_NAME, channel.name != null ? new StringType(channel.name) : UnDefType.UNDEF);

        State numberState = toChannelNumberState(channel.logicalChannelNumber);
        updateState(CHANNEL_CHANNEL_NUMBER, numberState);

        boolean isFavorite = account.getFavoriteChannelIds(resolveProfileId(account)).contains(channel.id);

        updateState(CHANNEL_FAVORITE_CHANNEL_NUMBER, isFavorite ? numberState : UnDefType.UNDEF);
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

    private void clearChannelSelections() {
        updateState(CHANNEL_CHANNEL_NAME, UnDefType.UNDEF);
        updateState(CHANNEL_CHANNEL_NUMBER, UnDefType.UNDEF);
        updateState(CHANNEL_FAVORITE_CHANNEL_NUMBER, UnDefType.UNDEF);
    }

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
                return;
            }
            if (detail == null) {
                clearTitleMetadata();
                updateState(CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
                lastResolvedContentId = null;
                return;
            }
            // For linear/reviewBuffer/replay, "title" is the show/program title and "episodeName" is the episode's own
            // title
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
                return;
            }
            if (detail == null) {
                clearTitleMetadata();
                updateState(CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
                lastResolvedContentId = null;
                return;
            }
            // For an episode, the field "title" holds the EPISODE's own name - the show name is in the separate
            // "seriesTitle" field instead. For a movie, "title" is the movie's own title and there is no
            // series/episode concept
            if (detail.isEpisode()) {
                applyTitleMetadata(detail.seriesTitle, detail.title, detail.season, detail.episode);
            } else {
                applyTitleMetadata(detail.title, null, null, null);
            }
            updateState(CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
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
                return;
            }
            if (detail == null) {
                clearTitleMetadata();
                updateState(CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
                lastResolvedContentId = null;
                return;
            }
            // nDVR is conditional on "source": for a "show"-sourced (standalone) recording, "title"
            // is the show name; for anything recorded as part of a series/season rule, the show name is in
            // the "showTitle" field instead
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
     * {@code seriesTitle} is only populated alongside {@code programTitle} when the content is genuinely episodic.
     */
    private void applyTitleMetadata(@Nullable String programTitle, @Nullable String episodeTitleText,
            @Nullable Integer season, @Nullable Integer episode) {
        updateState(CHANNEL_PROGRAM_TITLE, programTitle != null ? new StringType(programTitle) : UnDefType.UNDEF);

        boolean isEpisodic = (episodeTitleText != null && !episodeTitleText.isBlank()) || season != null
                || episode != null;
        updateState(CHANNEL_SERIES_TITLE,
                isEpisodic && programTitle != null ? new StringType(programTitle) : UnDefType.UNDEF);

        String resolvedEpisodeTitle = episodeTitleText != null && !episodeTitleText.isBlank() ? episodeTitleText
                : episode != null ? "Episode " + episode : null;
        updateState(CHANNEL_EPISODE_TITLE,
                resolvedEpisodeTitle != null ? new StringType(resolvedEpisodeTitle) : UnDefType.UNDEF);

        updateState(CHANNEL_SEASON, season != null ? new DecimalType(season) : UnDefType.UNDEF);
        updateState(CHANNEL_EPISODE, episode != null ? new DecimalType(episode) : UnDefType.UNDEF);
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

    private void updateImageFromUrl(String contentId, String url) {
        LGHorizonAccountHandler account = getAccountHandler();
        if (account == null) {
            return;
        }
        RawType image = account.fetchImage(url);
        if (!isStillCurrent(contentId)) {
            return;
        }
        updateState(CHANNEL_MEDIA_IMAGE, image != null ? image : UnDefType.UNDEF);
    }

    private void updateImageFromUrlOrClear(String contentId, @Nullable String url) {
        if (!isStillCurrent(contentId)) {
            return;
        }
        if (url == null) {
            updateState(CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
            return;
        }
        updateImageFromUrl(contentId, url);
    }

    private void handleAppsState(JsonObject status) {
        JsonObject appsState = status.has("appsState") && status.get("appsState").isJsonObject()
                ? status.getAsJsonObject("appsState")
                : null;
        if (appsState == null) {
            return;
        }
        updateState(CHANNEL_SOURCE_TYPE, new StringType("app"));
        clearTitleMetadata();
        clearChannelSelections();
        getString(appsState, "appName")
                .ifPresent(appName -> updateState(CHANNEL_PROGRAM_TITLE, new StringType(appName)));

        String logoPath = getString(appsState, "logoPath").orElse(null);
        if (logoPath == null) {
            lastResolvedContentId = null;
            updateState(CHANNEL_MEDIA_IMAGE, UnDefType.UNDEF);
        } else if (!logoPath.equals(lastResolvedContentId)) {
            lastResolvedContentId = logoPath;
            scheduler.execute(() -> updateImageFromUrl(logoPath, logoPath));
        }
    }

    private static Optional<String> getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? Optional.of(obj.get(key).getAsString()) : Optional.empty();
    }
}
