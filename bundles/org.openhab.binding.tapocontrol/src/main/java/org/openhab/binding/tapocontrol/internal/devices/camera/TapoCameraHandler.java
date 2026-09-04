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
package org.openhab.binding.tapocontrol.internal.devices.camera;

import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.api.camera.TapoCameraApi;
import org.openhab.binding.tapocontrol.internal.api.camera.TapoCameraApiException;
import org.openhab.binding.tapocontrol.internal.api.camera.TapoCameraCommands;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoDeviceInfo;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoLastAlarmInfo;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoLedInfo;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoLensMaskInfo;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoMotionDetection;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoMsgAlarmInfo;
import org.openhab.binding.tapocontrol.internal.dto.camera.TapoPresets;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Handler for Tapo IP cameras exposing local-API features not available through ONVIF.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class TapoCameraHandler extends BaseThingHandler {
    private static final int ERROR_AUTH_FAILURE = TapoCameraApi.ERROR_AUTH_FAILURE;
    // device-side codes signalling that a module, method or parameter is not implemented by this model
    private static final int ERROR_METHOD_UNSUPPORTED = -40100;
    private static final int ERROR_PARAM_UNSUPPORTED = -40101;
    private static final int ERROR_METHOD_NOT_EXIST = -40105;
    private static final Logger LOGGER = LoggerFactory.getLogger(TapoCameraHandler.class);

    private final @Nullable HttpClient httpClient; // may be null only in unit tests

    private volatile boolean deviceInfoRead;
    private volatile @Nullable TapoCameraApi api;
    private volatile @Nullable ScheduledFuture<?> scheduledJob;
    private volatile @Nullable TapoPresets presets;
    // replaced wholesale on updates so poll and command threads always see an immutable snapshot
    private volatile EnumSet<TapoCameraFeature> detectedFeatures = EnumSet.allOf(TapoCameraFeature.class);
    // snapshot of the channels defined by the thing-type, used to add/remove channels as features are detected
    private List<Channel> originalChannels = List.of();
    private boolean originalChannelsInitialized;

    public TapoCameraHandler(Thing thing, @Nullable HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    /** Injection point so tests can supply a mocked API. */
    protected TapoCameraApi createApi(TapoCameraConfiguration config) {
        HttpClient client = httpClient;
        if (client == null) {
            throw new IllegalStateException("no httpClient configured");
        }
        return new TapoCameraApi(client, config.ipAddress(), config.httpPort(), config.username(), config.password(),
                new Gson());
    }

    EnumSet<TapoCameraFeature> getDetectedFeatures() {
        return detectedFeatures; // stored sets are never mutated in place, safe to expose directly
    }

    @Override
    public void initialize() {
        TapoCameraConfiguration config = TapoCameraConfiguration.from(thing);
        if (config.ipAddress().isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ipAddress must be set");
            return;
        }
        if (!originalChannelsInitialized) {
            originalChannels = List.copyOf(thing.getChannels());
            originalChannelsInitialized = true;
        }
        updateStatus(ThingStatus.UNKNOWN);
        try {
            api = createApi(config);
        } catch (IllegalStateException e) {
            LOGGER.debug("{}: cannot create camera api", thing.getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        int pollingInterval = config.pollingInterval();
        scheduledJob = pollingInterval > 0
                ? scheduler.scheduleWithFixedDelay(this::poll, 0, pollingInterval, TimeUnit.SECONDS)
                : scheduler.schedule(this::poll, 0, TimeUnit.SECONDS);
    }

    private void cancelJob() {
        ScheduledFuture<?> job = scheduledJob;
        if (job != null) {
            job.cancel(true);
            scheduledJob = null;
        }
    }

    void poll() {
        TapoCameraApi cameraApi = api;
        if (cameraApi == null) {
            return;
        }
        boolean retriedAuth = false;
        while (cameraApi == api) {
            try {
                ensureLoggedIn(cameraApi);
                readDeviceInfo(cameraApi);
                readAlarm(cameraApi);
                readPrivacy(cameraApi);
                readMotion(cameraApi);
                readLed(cameraApi);
                readPresets(cameraApi);
                if (cameraApi != api) {
                    return;
                }
                updateStatus(ThingStatus.ONLINE);
                synchronizeChannels();
                break;
            } catch (StopCycleException e) {
                TapoCameraApiException cause = e.cause();
                if (cause.getErrorCode() == ERROR_AUTH_FAILURE && !retriedAuth) {
                    // single bounded re-login within the same cycle
                    retriedAuth = true;
                    cameraApi.clearSession();
                    LOGGER.debug("{}: session expired, re-authenticating once", thing.getUID());
                    continue;
                }
                offlineFromCause(cause);
                break;
            } catch (TapoCameraApiException e) {
                offlineFromCause(e);
                break;
            }
        }
    }

    private void ensureLoggedIn(TapoCameraApi cameraApi) throws TapoCameraApiException {
        if (!cameraApi.isLoggedIn()) {
            cameraApi.login();
            if (cameraApi != api) {
                return;
            }
            // features may come back after reconnect
            detectedFeatures = EnumSet.allOf(TapoCameraFeature.class);
            deviceInfoRead = false; // properties must be refreshed for the new session
        }
    }

    /** Nested control-flow marker wrapping the API exception that aborted the poll cycle. */
    private static final class StopCycleException extends Exception {
        private static final long serialVersionUID = 1L;
        private final TapoCameraApiException failure;

        StopCycleException(TapoCameraApiException failure) {
            super(failure);
            this.failure = failure;
        }

        TapoCameraApiException cause() {
            return failure;
        }
    }

    /**
     * Executes one guarded read. Failures other than the protocol's "method unsupported" code abort the cycle;
     * an unsupported method drops the feature for the session and yields {@code null}.
     */
    private @Nullable JsonObject readSection(TapoCameraApi cameraApi, JsonObject command,
            @Nullable TapoCameraFeature feature, String module, String section) throws StopCycleException {
        try {
            JsonObject response = cameraApi.sendCommand(command);
            if (cameraApi != api) {
                return null;
            }
            // Some camera firmwares (e.g. C125) return secured-command results unwrapped, i.e. the
            // module object sits at the top level instead of under a "result" key. Accept both shapes.
            JsonElement result = response.get("result");
            JsonObject root = (result != null && result.isJsonObject()) ? result.getAsJsonObject() : response;
            JsonObject moduleObj = root.getAsJsonObject(module);
            if (moduleObj == null) {
                return null;
            }
            JsonObject sectionObj = moduleObj.getAsJsonObject(section);
            return sectionObj != null ? sectionObj : null;
        } catch (TapoCameraApiException e) {
            if (cameraApi != api) {
                return null;
            }
            if (!isCapabilityBoundary(e.getErrorCode())) {
                // anything but a capability boundary may be transient — abort the cycle instead of dropping the feature
                throw new StopCycleException(e);
            }
            if (feature != null) {
                // copy-on-write: publish a fresh set instead of mutating one readers may hold
                var updated = EnumSet.noneOf(TapoCameraFeature.class);
                updated.addAll(detectedFeatures);
                updated.remove(feature);
                detectedFeatures = updated;
            }
            LOGGER.debug("{}: {}#{} not supported (error {}), skipping", thing.getUID(), module, section,
                    e.getErrorCode());
            return null;
        }
    }

    /**
     * Capability-boundary codes indicate that the camera model does not implement a module, method or parameter.
     * They are returned by various firmware versions as -40100, -40101 or -40105; none of them invalidates the
     * session, so the poll drops just the affected feature instead of aborting the whole cycle.
     */
    private static boolean isCapabilityBoundary(int errorCode) {
        return errorCode == ERROR_METHOD_UNSUPPORTED || errorCode == ERROR_PARAM_UNSUPPORTED
                || errorCode == ERROR_METHOD_NOT_EXIST;
    }

    private void offlineFromCause(TapoCameraApiException e) {
        switch (e.getErrorCode()) {
            case ERROR_AUTH_FAILURE ->
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            case 0 -> {
                LOGGER.debug("{}: communication problem: {}", thing.getUID(), e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            default -> {
                LOGGER.debug("{}: request failed (error {}): {}", thing.getUID(), e.getErrorCode(), e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void readDeviceInfo(TapoCameraApi cameraApi) throws StopCycleException {
        if (deviceInfoRead) {
            return;
        }
        JsonObject json = readSection(cameraApi, TapoCameraCommands.getDeviceInfo(), null,
                TapoCameraCommands.MODULE_DEVICE_INFO, TapoCameraCommands.SECTION_BASIC_INFO);
        if (json == null || cameraApi != api) {
            return;
        }
        TapoDeviceInfo info = TapoDeviceInfo.fromJson(json);
        putProperty(Thing.PROPERTY_MODEL_ID, info.model());
        putProperty(Thing.PROPERTY_MAC_ADDRESS, info.mac());
        putProperty(Thing.PROPERTY_FIRMWARE_VERSION, info.swVersion());
        deviceInfoRead = true;
    }

    private void putProperty(String name, String value) {
        if (!value.isBlank()) {
            updateProperty(name, value);
        }
    }

    private void readAlarm(TapoCameraApi cameraApi) throws StopCycleException {
        if (!detectedFeatures.contains(TapoCameraFeature.ALARM)) {
            return;
        }
        JsonObject alarmJson = readSection(cameraApi, TapoCameraCommands.getAlertConfig(), TapoCameraFeature.ALARM,
                TapoCameraCommands.MODULE_MSG_ALARM, TapoCameraCommands.SECTION_MSG_ALARM_INFO);
        if (alarmJson != null && cameraApi == api) {
            TapoMsgAlarmInfo alarm = TapoMsgAlarmInfo.fromJson(alarmJson);
            updateState(channel(CHANNEL_GROUP_CAMERA_ALARM, CHANNEL_ALARM_MODE),
                    new StringType(alarm.enabled() ? alarmModeLabel(alarm.alarmModes()) : "off"));
        }
        if (cameraApi != api) {
            return;
        }
        JsonObject lastAlarmJson = readSection(cameraApi, TapoCameraCommands.getLastAlarmInfo(),
                TapoCameraFeature.ALARM, TapoCameraCommands.MODULE_SYSTEM, TapoCameraCommands.SECTION_LAST_ALARM_INFO);
        if (lastAlarmJson != null && cameraApi == api) {
            TapoLastAlarmInfo lastAlarm = TapoLastAlarmInfo.fromJson(lastAlarmJson);
            if (!lastAlarm.type().isEmpty()) {
                updateState(channel(CHANNEL_GROUP_CAMERA_ALARM, CHANNEL_LAST_ALARM_TYPE),
                        new StringType(lastAlarm.type()));
            }
            if (lastAlarm.timeEpochSeconds() > 0) {
                updateState(channel(CHANNEL_GROUP_CAMERA_ALARM, CHANNEL_LAST_ALARM_TIME),
                        new DateTimeType(
                                ZonedDateTime.ofInstant(java.time.Instant.ofEpochSecond(lastAlarm.timeEpochSeconds()),
                                        ZoneId.systemDefault())));
            }
        }
    }

    private static String alarmModeLabel(List<String> modes) {
        boolean sound = modes.contains("sound");
        boolean light = modes.contains("light");
        if (sound && light) {
            return "both";
        }
        if (sound) {
            return "sound";
        }
        if (light) {
            return "light";
        }
        return "off";
    }

    private void readPrivacy(TapoCameraApi cameraApi) throws StopCycleException {
        if (!detectedFeatures.contains(TapoCameraFeature.PRIVACY)) {
            return;
        }
        JsonObject json = readSection(cameraApi, TapoCameraCommands.getLensMaskInfo(), TapoCameraFeature.PRIVACY,
                TapoCameraCommands.MODULE_LENS_MASK, TapoCameraCommands.SECTION_LENS_MASK_INFO);
        if (json != null && cameraApi == api) {
            updateState(channel(CHANNEL_GROUP_CAMERA_PRIVACY, CHANNEL_PRIVACY_MODE),
                    OnOffType.from(TapoLensMaskInfo.fromJson(json).enabled()));
        }
    }

    private void readMotion(TapoCameraApi cameraApi) throws StopCycleException {
        if (!detectedFeatures.contains(TapoCameraFeature.MOTION_DETECTION)) {
            return;
        }
        JsonObject json = readSection(cameraApi, TapoCameraCommands.getDetectionConfig(),
                TapoCameraFeature.MOTION_DETECTION, TapoCameraCommands.MODULE_MOTION_DETECTION,
                TapoCameraCommands.SECTION_MOTION_DET);
        if (json != null && cameraApi == api) {
            TapoMotionDetection detection = TapoMotionDetection.fromJson(json);
            if (detection.enabled() != null) {
                updateState(channel(CHANNEL_GROUP_CAMERA_MOTION, CHANNEL_MOTION_ENABLED),
                        OnOffType.from(detection.enabled()));
            }
            Integer digital = detection.digitalSensitivity();
            if (digital != null) {
                updateState(channel(CHANNEL_GROUP_CAMERA_MOTION, CHANNEL_MOTION_SENSITIVITY), new PercentType(digital));
            }
        }
    }

    private void readLed(TapoCameraApi cameraApi) throws StopCycleException {
        if (!detectedFeatures.contains(TapoCameraFeature.LED)) {
            return;
        }
        JsonObject json = readSection(cameraApi, TapoCameraCommands.getLedConfig(), TapoCameraFeature.LED,
                TapoCameraCommands.MODULE_LED, TapoCameraCommands.SECTION_LED_CONFIG);
        if (json != null && cameraApi == api) {
            updateState(channel(CHANNEL_GROUP_CAMERA_SYSTEM, CHANNEL_LED_STATUS),
                    OnOffType.from(TapoLedInfo.fromJson(json).enabled()));
        }
    }

    private void readPresets(TapoCameraApi cameraApi) throws StopCycleException {
        if (!detectedFeatures.contains(TapoCameraFeature.PRESETS)) {
            return;
        }
        JsonObject json = readSection(cameraApi, TapoCameraCommands.getPresets(), TapoCameraFeature.PRESETS,
                TapoCameraCommands.MODULE_PRESET, TapoCameraCommands.SECTION_PRESET);
        if (json != null && cameraApi == api) {
            presets = TapoPresets.fromJson(json);
        }
    }

    /**
     * Removes channels for features the camera does not support and restores them when features come back
     * (e.g. after a reconnect). Called at the end of a successful poll cycle.
     */
    private void synchronizeChannels() {
        Set<TapoCameraFeature> features = detectedFeatures;
        Set<String> supportedGroups = features.stream().map(TapoCameraHandler::channelGroupFor)
                .collect(Collectors.toSet());
        List<Channel> desiredChannels = originalChannels.stream().filter(ch -> isChannelRetained(ch, supportedGroups))
                .toList();
        Set<ChannelUID> currentUids = thing.getChannels().stream().map(Channel::getUID).collect(Collectors.toSet());
        Set<ChannelUID> desiredUids = desiredChannels.stream().map(Channel::getUID).collect(Collectors.toSet());
        if (!currentUids.equals(desiredUids)) {
            LOGGER.debug("{}: synchronizing camera channels, current={}, desired={}", thing.getUID(), currentUids,
                    desiredUids);
            ThingBuilder builder = editThing();
            builder.withChannels(desiredChannels);
            updateThing(builder.build());
        }
    }

    private static boolean isChannelRetained(Channel channel, Set<String> supportedGroups) {
        String groupId = channel.getUID().getGroupId();
        return groupId == null || supportedGroups.contains(groupId);
    }

    private static String channelGroupFor(TapoCameraFeature feature) {
        return switch (feature) {
            case ALARM -> CHANNEL_GROUP_CAMERA_ALARM;
            case PRIVACY -> CHANNEL_GROUP_CAMERA_PRIVACY;
            case MOTION_DETECTION -> CHANNEL_GROUP_CAMERA_MOTION;
            case PRESETS -> CHANNEL_GROUP_CAMERA_PRESETS;
            case LED -> CHANNEL_GROUP_CAMERA_SYSTEM;
        };
    }

    private ChannelUID channel(String group, String id) {
        return new ChannelUID(thing.getUID(), group + "#" + id);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        processCommand(channelUID, command);
    }

    void processCommand(ChannelUID channelUID, Command command) {
        TapoCameraApi cameraApi = api;
        if (cameraApi == null || !cameraApi.isLoggedIn()) {
            LOGGER.debug("{}: ignoring command, camera not connected", thing.getUID());
            return;
        }
        try {
            processCommandInternal(cameraApi, channelUID, command);
        } catch (TapoCameraApiException | StopCycleException | UnsupportedFeatureException e) {
            LOGGER.debug("{}: command {} on {} failed: {}", thing.getUID(), command, channelUID, e.getMessage());
        }
    }

    private void processCommandInternal(TapoCameraApi cameraApi, ChannelUID channelUID, Command command)
            throws TapoCameraApiException, StopCycleException {
        String rawGroup = channelUID.getGroupId();
        String group = rawGroup != null ? rawGroup : "";
        String id = channelUID.getIdWithoutGroup();

        if (command instanceof RefreshType) {
            refreshGroup(cameraApi, group);
            return;
        }

        switch (group) {
            case CHANNEL_GROUP_CAMERA_ALARM -> handleAlarmCommand(cameraApi, id, command);
            case CHANNEL_GROUP_CAMERA_PRIVACY -> handlePrivacyCommand(cameraApi, command);
            case CHANNEL_GROUP_CAMERA_MOTION -> handleMotionCommand(cameraApi, id, command);
            case CHANNEL_GROUP_CAMERA_PRESETS -> handlePresetCommand(cameraApi, command);
            case CHANNEL_GROUP_CAMERA_SYSTEM -> handleSystemCommand(cameraApi, command);
            default -> LOGGER.debug("{}: ignoring command for unknown channel {}", thing.getUID(), channelUID);
        }
    }

    private void handleAlarmCommand(TapoCameraApi cameraApi, String id, Command command)
            throws TapoCameraApiException, StopCycleException {
        requireFeature(TapoCameraFeature.ALARM);
        switch (id) {
            case CHANNEL_MANUAL_ALARM -> {
                cameraApi.sendCommand(TapoCameraCommands.manualAlarm(command == OnOffType.ON));
                // manual_msg_alarm is an action without config read-back; reflect the requested siren state
                updateState(channel(CHANNEL_GROUP_CAMERA_ALARM, CHANNEL_MANUAL_ALARM),
                        command == OnOffType.ON ? OnOffType.ON : OnOffType.OFF);
                refreshGroup(cameraApi, CHANNEL_GROUP_CAMERA_ALARM);
            }
            case CHANNEL_ALARM_MODE -> {
                String mode = command.toString();
                List<String> modes = switch (mode) {
                    case "sound" -> List.of("sound");
                    case "light" -> List.of("light");
                    case "both" -> List.of("sound", "light");
                    default -> List.of();
                };
                cameraApi.sendCommand(TapoCameraCommands.setAlertConfig(!"off".equals(mode), modes));
                refreshGroup(cameraApi, CHANNEL_GROUP_CAMERA_ALARM);
            }
            default -> LOGGER.debug("{}: ignoring unknown alarm channel {}", thing.getUID(), id);
        }
    }

    private void handlePrivacyCommand(TapoCameraApi cameraApi, Command command)
            throws TapoCameraApiException, StopCycleException {
        requireFeature(TapoCameraFeature.PRIVACY);
        cameraApi.sendCommand(TapoCameraCommands.setLensMaskEnabled(command == OnOffType.ON));
        refreshGroup(cameraApi, CHANNEL_GROUP_CAMERA_PRIVACY);
    }

    private void handleMotionCommand(TapoCameraApi cameraApi, String id, Command command)
            throws TapoCameraApiException, StopCycleException {
        requireFeature(TapoCameraFeature.MOTION_DETECTION);
        switch (id) {
            case CHANNEL_MOTION_ENABLED -> {
                boolean on = command == OnOffType.ON;
                cameraApi.sendCommand(TapoCameraCommands.setDetectionConfig(on, null));
                refreshGroup(cameraApi, CHANNEL_GROUP_CAMERA_MOTION);
            }
            case CHANNEL_MOTION_SENSITIVITY -> {
                if (!(command instanceof DecimalType decimal)) {
                    LOGGER.debug("{}: ignoring unsupported sensitivity command {}", thing.getUID(), command);
                    return;
                }
                cameraApi.sendCommand(TapoCameraCommands.setDetectionConfig(null, decimal.intValue()));
                refreshGroup(cameraApi, CHANNEL_GROUP_CAMERA_MOTION);
            }
            default -> LOGGER.debug("{}: ignoring unknown motion detection channel {}", thing.getUID(), id);
        }
    }

    private void handlePresetCommand(TapoCameraApi cameraApi, Command command)
            throws TapoCameraApiException, StopCycleException {
        requireFeature(TapoCameraFeature.PRESETS);
        if (!(command instanceof DecimalType decimal)) {
            LOGGER.debug("{}: ignoring unsupported gotoPreset command {}", thing.getUID(), command);
            return;
        }
        TapoPresets currentPresets = presets;
        int requested = decimal.intValue();
        if (currentPresets == null || !currentPresets.ids().contains(requested)) {
            LOGGER.debug("{}: ignoring gotoPreset {}, no such preset", thing.getUID(), requested);
            return;
        }
        cameraApi.sendCommand(TapoCameraCommands.moveToPreset(requested));
        // no read-back: the camera moves physically, configuration is unchanged
    }

    private void handleSystemCommand(TapoCameraApi cameraApi, Command command)
            throws TapoCameraApiException, StopCycleException {
        requireFeature(TapoCameraFeature.LED);
        cameraApi.sendCommand(TapoCameraCommands.setLedEnabled(command == OnOffType.ON));
        refreshGroup(cameraApi, CHANNEL_GROUP_CAMERA_SYSTEM);
    }

    private void requireFeature(TapoCameraFeature feature) {
        if (!detectedFeatures.contains(feature)) {
            throw new UnsupportedFeatureException(feature);
        }
    }

    private void refreshGroup(TapoCameraApi cameraApi, String group) throws TapoCameraApiException, StopCycleException {
        switch (group) {
            case CHANNEL_GROUP_CAMERA_ALARM -> readAlarm(cameraApi);
            case CHANNEL_GROUP_CAMERA_PRIVACY -> readPrivacy(cameraApi);
            case CHANNEL_GROUP_CAMERA_MOTION -> readMotion(cameraApi);
            case CHANNEL_GROUP_CAMERA_PRESETS -> readPresets(cameraApi);
            case CHANNEL_GROUP_CAMERA_SYSTEM -> readLed(cameraApi);
            default -> LOGGER.debug("{}: no refresh defined for group {}", thing.getUID(), group);
        }
    }

    /** Marker for commands on channels whose backing feature was not detected on this camera. */
    private static final class UnsupportedFeatureException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        UnsupportedFeatureException(TapoCameraFeature feature) {
            super(feature + " not supported by this camera");
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        TapoCameraApi cameraApi = api;
        if (cameraApi != null) {
            cameraApi.clearSession(); // credentials may have changed
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void dispose() {
        cancelJob();
        TapoCameraApi cameraApi = api;
        api = null;
        if (cameraApi != null) {
            cameraApi.clearSession();
        }
        super.dispose();
    }
}
