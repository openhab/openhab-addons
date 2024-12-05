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
package org.openhab.binding.vesync.internal.handlers;

import static org.openhab.binding.vesync.internal.VeSyncConstants.*;
import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vesync.internal.VeSyncBridgeConfiguration;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestManagedDeviceBypassV2;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestV1ManagedDeviceDetails;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestV1SetLevel;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestV1SetMode;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestV1SetStatus;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncResponse;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2BypassPurifierStatus;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2Ver2BypassPurifierStatus;
import org.openhab.binding.vesync.internal.dto.responses.v1.VeSyncV1AirPurifierDeviceDetailsResponse;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeSyncDeviceAirPurifierHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class VeSyncDeviceAirPurifierHandler extends VeSyncBaseDeviceHandler {

    public static final String DEV_TYPE_FAMILY_AIR_PURIFIER = "LAP";

    public static final int DEFAULT_AIR_PURIFIER_POLL_RATE = 120;

    public static final String DEV_FAMILY_CORE_200S = "200S";
    public static final String DEV_FAMILY_CORE_300S = "300S";
    public static final String DEV_FAMILY_CORE_400S = "400S";
    public static final String DEV_FAMILY_CORE_600S = "600S";

    public static final String DEV_FAMILY_PUR_131S = "131S";

    public static final String DEV_FAMILY_VITAL_100S = "V100S";

    public static final String DEV_FAMILY_VITAL_200S = "V200S";

    private static final List<String> FAN_MODES_WITH_PET = Arrays.asList(MODE_AUTO, MODE_MANUAL, MODE_SLEEP, MODE_PET);

    private static final List<String> FAN_MODES_NO_PET = Arrays.asList(MODE_AUTO, MODE_MANUAL, MODE_SLEEP);
    private static final List<String> FAN_MODES_MAN_SLEEP = Arrays.asList(MODE_MANUAL, MODE_SLEEP);
    private static final List<String> NIGHT_LIGHTS = Arrays.asList(MODE_ON, MODE_DIM, MODE_OFF);

    private static final List<String> NO_NIGHT_LIGHTS = Collections.emptyList();
    public static final VeSyncDevicePurifierMetadata CORE200S = new VeSyncDevicePurifierMetadata(1,
            DEV_FAMILY_CORE_200S, Arrays.asList("C201S", "C202S"), List.of("Core200S"), FAN_MODES_MAN_SLEEP, 1, 3,
            NIGHT_LIGHTS);

    public static final VeSyncDevicePurifierMetadata CORE300S = new VeSyncDevicePurifierMetadata(1,
            DEV_FAMILY_CORE_300S, List.of("C301S", "C302S"), List.of("Core300S"), FAN_MODES_NO_PET, 1, 3, NIGHT_LIGHTS);

    public static final VeSyncDevicePurifierMetadata CORE400S = new VeSyncDevicePurifierMetadata(1,
            DEV_FAMILY_CORE_400S, List.of("C401S"), List.of("Core400S"), FAN_MODES_NO_PET, 1, 4, NO_NIGHT_LIGHTS);

    public static final VeSyncDevicePurifierMetadata CORE600S = new VeSyncDevicePurifierMetadata(1,
            DEV_FAMILY_CORE_600S, List.of("C601S"), List.of("Core600S"), FAN_MODES_NO_PET, 1, 4, NO_NIGHT_LIGHTS);

    public static final VeSyncDevicePurifierMetadata VITAL100S = new VeSyncDevicePurifierMetadata(2,
            DEV_FAMILY_VITAL_100S, List.of("V102S"), Collections.emptyList(), FAN_MODES_NO_PET, 1, 5, NO_NIGHT_LIGHTS);

    public static final VeSyncDevicePurifierMetadata VITAL200S = new VeSyncDevicePurifierMetadata(2,
            DEV_FAMILY_VITAL_200S, List.of("V201S"), Collections.emptyList(), FAN_MODES_WITH_PET, 1, 5,
            NO_NIGHT_LIGHTS);

    public static final VeSyncDevicePurifierMetadata PUR131S = new VeSyncDevicePurifierMetadata(1, DEV_FAMILY_PUR_131S,
            Collections.emptyList(), Arrays.asList("LV-PUR131S", "LV-RH131S"), FAN_MODES_NO_PET, 1, 3, NO_NIGHT_LIGHTS);

    public static final Map<String, VeSyncDevicePurifierMetadata> DEV_FAMILY_PURIFIER_MAP = new HashMap<String, VeSyncDevicePurifierMetadata>() {
        {
            put(PUR131S.deviceFamilyName, PUR131S);
            put(CORE200S.deviceFamilyName, CORE200S);
            put(CORE300S.deviceFamilyName, CORE300S);
            put(CORE400S.deviceFamilyName, CORE400S);
            put(CORE600S.deviceFamilyName, CORE600S);
            put(VITAL100S.deviceFamilyName, VITAL100S);
            put(VITAL200S.deviceFamilyName, VITAL200S);
        }
    };
    public static final List<VeSyncDeviceMetadata> SUPPORTED_MODEL_FAMILIES = DEV_FAMILY_PURIFIER_MAP.values().stream()
            .collect(Collectors.toList());

    private final Logger logger = LoggerFactory.getLogger(VeSyncDeviceAirPurifierHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AIR_PURIFIER);

    private final Object pollLock = new Object();

    public VeSyncDeviceAirPurifierHandler(Thing thing, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        customiseChannels();
    }

    @Override
    protected @NotNull String[] getChannelsToRemove() {
        final String deviceFamily = getThing().getProperties().get(DEVICE_PROP_DEVICE_FAMILY);
        String[] toRemove = new String[] {};
        if (deviceFamily != null) {
            switch (deviceFamily) {
                case DEV_FAMILY_CORE_600S:
                case DEV_FAMILY_CORE_400S:
                    toRemove = new String[] { DEVICE_CHANNEL_AF_NIGHT_LIGHT, DEVICE_CHANNEL_AF_LIGHT_DETECTION,
                            DEVICE_CHANNEL_AF_LIGHT_DETECTED };
                    break;
                case DEV_FAMILY_PUR_131S:
                    toRemove = new String[] { DEVICE_CHANNEL_AF_NIGHT_LIGHT, DEVICE_CHANNEL_AF_CONFIG_AUTO_ROOM_SIZE,
                            DEVICE_CHANNEL_AF_CONFIG_AUTO_MODE_PREF, DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME,
                            DEVICE_CHANNEL_AIRQUALITY_PM25, DEVICE_CHANNEL_AF_SCHEDULES_COUNT,
                            DEVICE_CHANNEL_AF_CONFIG_DISPLAY_FOREVER, DEVICE_CHANNEL_ERROR_CODE,
                            DEVICE_CHANNEL_CHILD_LOCK_ENABLED, DEVICE_CHANNEL_AF_LIGHT_DETECTION,
                            DEVICE_CHANNEL_AF_LIGHT_DETECTED };
                    break;
                case DEV_FAMILY_VITAL_100S:
                case DEV_FAMILY_VITAL_200S:
                    toRemove = new String[] { DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME, DEVICE_CHANNEL_AF_SCHEDULES_COUNT,
                            DEVICE_CHANNEL_AF_NIGHT_LIGHT, DEVICE_CHANNEL_AF_CONFIG_AUTO_MODE_PREF,
                            DEVICE_CHANNEL_AF_CONFIG_DISPLAY_FOREVER, DEVICE_CHANNEL_AF_CONFIG_AUTO_ROOM_SIZE,
                            DEVICE_CHANNEL_AF_LIGHT_DETECTION, DEVICE_CHANNEL_AF_LIGHT_DETECTED,
                            DEVICE_CHANNEL_ERROR_CODE };
                    break;
                default:
                    toRemove = new String[] { DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME, DEVICE_CHANNEL_AF_SCHEDULES_COUNT,
                            DEVICE_CHANNEL_AF_LIGHT_DETECTION, DEVICE_CHANNEL_AF_LIGHT_DETECTED };
            }
        }
        return toRemove;
    }

    @Override
    public void updateBridgeBasedPolls(final VeSyncBridgeConfiguration config) {
        Integer pollRate = config.airPurifierPollInterval;
        if (pollRate == null) {
            pollRate = DEFAULT_AIR_PURIFIER_POLL_RATE;
        }

        if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            setBackgroundPollInterval(-1);
        } else {
            setBackgroundPollInterval(pollRate);
        }
    }

    @Override
    public void dispose() {
        this.setBackgroundPollInterval(-1);
    }

    @Override
    public String getDeviceFamilyProtocolPrefix() {
        return DEV_TYPE_FAMILY_AIR_PURIFIER;
    }

    @Override
    public List<VeSyncDeviceMetadata> getSupportedDeviceMetadata() {
        return SUPPORTED_MODEL_FAMILIES;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        final String deviceFamily = getThing().getProperties().get(DEVICE_PROP_DEVICE_FAMILY);
        if (deviceFamily == null) {
            return;
        }
        final String deviceUuid = getThing().getProperties().get(DEVICE_PROP_DEVICE_UUID);
        if (deviceUuid == null) {
            return;
        }
        final VeSyncDevicePurifierMetadata devContraints = DEV_FAMILY_PURIFIER_MAP.get(deviceFamily);
        if (devContraints == null) {
            logger.warn("{}", getLocalizedText("warning.device.command-device-family-not-found", deviceFamily));
            return;
        }

        scheduler.submit(() -> {

            if (command instanceof OnOffType) {
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_ENABLED:
                        switch (deviceFamily) {
                            case DEV_FAMILY_VITAL_100S:
                            case DEV_FAMILY_VITAL_200S:
                                sendV2BypassControlCommand(DEVICE_SET_SWITCH,
                                        new VeSyncRequestManagedDeviceBypassV2.SetPowerPayload(
                                                command.equals(OnOffType.ON), 0));
                                break;
                            case DEV_FAMILY_PUR_131S:
                                sendV1ControlCommand("131airPurifier/v1/device/deviceStatus",
                                        new VeSyncRequestV1SetStatus(deviceUuid,
                                                command.equals(OnOffType.ON) ? "on" : "off"));
                                break;
                            default:
                                sendV2BypassControlCommand(DEVICE_SET_SWITCH,
                                        new VeSyncRequestManagedDeviceBypassV2.SetSwitchPayload(
                                                command.equals(OnOffType.ON), 0));
                        }
                        break;
                    case DEVICE_CHANNEL_DISPLAY_ENABLED:
                        switch (deviceFamily) {
                            case DEV_FAMILY_VITAL_100S:
                            case DEV_FAMILY_VITAL_200S:
                                sendV2BypassControlCommand(DEVICE_SET_DISPLAY,
                                        new VeSyncRequestManagedDeviceBypassV2.SetScreenSwitchPayload(
                                                command.equals(OnOffType.ON)));
                                break;
                            case DEV_FAMILY_PUR_131S:
                                sendV1ControlCommand("131airPurifier/v1/device/updateScreen",
                                        new VeSyncRequestV1SetStatus(deviceUuid,
                                                command.equals(OnOffType.ON) ? "on" : "off"));
                                break;
                            default:
                                sendV2BypassControlCommand(DEVICE_SET_DISPLAY,
                                        new VeSyncRequestManagedDeviceBypassV2.SetState(command.equals(OnOffType.ON)));

                                break;
                        }
                        break;
                    case DEVICE_CHANNEL_CHILD_LOCK_ENABLED:
                        switch (deviceFamily) {
                            case DEV_FAMILY_VITAL_100S:
                            case DEV_FAMILY_VITAL_200S:
                                sendV2BypassControlCommand(DEVICE_SET_CHILD_LOCK,
                                        new VeSyncRequestManagedDeviceBypassV2.SetChildLockPayload(
                                                command.equals(OnOffType.ON)));
                                break;
                            default:
                                sendV2BypassControlCommand(DEVICE_SET_CHILD_LOCK,
                                        new VeSyncRequestManagedDeviceBypassV2.SetChildLock(
                                                command.equals(OnOffType.ON)));
                                break;
                        }
                        break;
                    case DEVICE_CHANNEL_AF_LIGHT_DETECTION:
                        sendV2BypassControlCommand(DEVICE_SET_LIGHT_DETECTION,
                                new VeSyncRequestManagedDeviceBypassV2.SetLightDetectionPayload(
                                        command.equals(OnOffType.ON)));
                        break;
                }
            } else if (command instanceof StringType) {
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_FAN_MODE_ENABLED:
                        final String targetFanMode = command.toString().toLowerCase();

                        if (!devContraints.isFanModeSupported(targetFanMode)) {
                            logger.warn("{}", getLocalizedText("warning.device.fan-mode-invalid", command,
                                    devContraints.deviceFamilyName, String.join(",", devContraints.fanModes)));
                            pollForUpdate();
                            return;
                        }
                        switch (deviceFamily) {
                            case DEV_FAMILY_VITAL_100S:
                            case DEV_FAMILY_VITAL_200S:
                                sendV2BypassControlCommand(DEVICE_SET_PURIFIER_MODE,
                                        new VeSyncRequestManagedDeviceBypassV2.SetWorkModePayload(targetFanMode));
                                break;
                            case DEV_FAMILY_PUR_131S:
                                sendV1ControlCommand("131airPurifier/v1/device/updateMode",
                                        new VeSyncRequestV1SetMode(deviceUuid, targetFanMode));
                                break;
                            default:
                                sendV2BypassControlCommand(DEVICE_SET_PURIFIER_MODE,
                                        new VeSyncRequestManagedDeviceBypassV2.SetMode(targetFanMode));
                        }
                        break;
                    case DEVICE_CHANNEL_AF_NIGHT_LIGHT:
                        final String targetNightLightMode = command.toString().toLowerCase();
                        if (!devContraints.isNightLightModeSupported(targetNightLightMode)) {
                            logger.warn("{}", getLocalizedText("warning.device.night-light-invalid", command,
                                    devContraints.deviceFamilyName, String.join(",", devContraints.nightLightModes)));
                            pollForUpdate();
                            return;
                        }
                        sendV2BypassControlCommand(DEVICE_SET_NIGHT_LIGHT,
                                new VeSyncRequestManagedDeviceBypassV2.SetNightLight(targetNightLightMode));
                        break;
                }
            } else if (command instanceof QuantityType quantityCommand) {
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_FAN_SPEED_ENABLED:
                        int requestedLevel = quantityCommand.intValue();
                        if (!devContraints.isFanSpeedSupported(requestedLevel)) {
                            logger.warn("{}",
                                    getLocalizedText("warning.device.fan-speed-invalid", command,
                                            devContraints.deviceFamilyName, String.valueOf(devContraints.minFanSpeed),
                                            String.valueOf(devContraints.maxFanSpeed)));
                            requestedLevel = requestedLevel < devContraints.minFanSpeed ? devContraints.minFanSpeed
                                    : devContraints.maxFanSpeed;
                        }
                        switch (deviceFamily) {
                            case DEV_FAMILY_VITAL_100S:
                            case DEV_FAMILY_VITAL_200S:
                                sendV2BypassControlCommand(DEVICE_SET_PURIFIER_MODE,
                                        new VeSyncRequestManagedDeviceBypassV2.SetWorkModePayload(MODE_MANUAL));
                                sendV2BypassControlCommand(DEVICE_SET_LEVEL,
                                        new VeSyncRequestManagedDeviceBypassV2.SetManualSpeedLevelPayload(
                                                requestedLevel));
                                break;
                            case DEV_FAMILY_PUR_131S:
                                sendV1ControlCommand("131airPurifier/v1/device/updateMode",
                                        new VeSyncRequestV1SetMode(deviceUuid, MODE_MANUAL), false);
                                sendV1ControlCommand("131airPurifier/v1/device/updateSpeed",
                                        new VeSyncRequestV1SetLevel(deviceUuid, requestedLevel));
                                break;
                            default:
                                sendV2BypassControlCommand(DEVICE_SET_PURIFIER_MODE,
                                        new VeSyncRequestManagedDeviceBypassV2.SetMode(MODE_MANUAL), false);
                                sendV2BypassControlCommand(DEVICE_SET_LEVEL,
                                        new VeSyncRequestManagedDeviceBypassV2.SetLevelPayload(0,
                                                DEVICE_LEVEL_TYPE_WIND, requestedLevel));
                        }
                        break;
                }
            } else if (command instanceof RefreshType) {
                pollForUpdate();
            } else {
                logger.trace("UNKNOWN COMMAND: {} {}", command.getClass().toString(), channelUID);
            }
        });
    }

    @Override
    protected void pollForDeviceData(final ExpiringCache<String> cachedResponse) {
        final String deviceFamily = getThing().getProperties().get(DEVICE_PROP_DEVICE_FAMILY);
        if (deviceFamily == null) {
            return;
        }
        if (!DEV_FAMILY_PUR_131S.equals(deviceFamily)) {
            processV2BypassPoll(cachedResponse);
        } else {
            processV1AirPurifierPoll(cachedResponse);
        }
    }

    private void processV1AirPurifierPoll(final ExpiringCache<String> cachedResponse) {
        final String deviceUuid = getThing().getProperties().get(DEVICE_PROP_DEVICE_UUID);
        if (deviceUuid == null) {
            return;
        }

        String response;
        VeSyncV1AirPurifierDeviceDetailsResponse purifierStatus;
        synchronized (pollLock) {
            response = cachedResponse.getValue();
            boolean cachedDataUsed = response != null;
            if (response == null) {
                logger.trace("Requesting fresh response");
                response = sendV1Command("131airPurifier/v1/device/deviceDetail",
                        new VeSyncRequestV1ManagedDeviceDetails(deviceUuid));
            } else {
                logger.trace("Using cached response {}", response);
            }

            if (EMPTY_STRING.equals(response)) {
                return;
            }

            purifierStatus = VeSyncConstants.GSON.fromJson(response, VeSyncV1AirPurifierDeviceDetailsResponse.class);

            if (purifierStatus == null) {
                return;
            }

            if (!cachedDataUsed) {
                cachedResponse.putValue(response);
            }
        }

        // Bail and update the status of the thing - it will be updated to online by the next search
        // that detects it is online.
        if (purifierStatus.isDeviceOnline()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        if (!"0".equals(purifierStatus.getCode())) {
            logger.warn("{}", getLocalizedText("warning.device.unexpected-resp-for-air-purifier"));
            return;
        }

        updateState(DEVICE_CHANNEL_ENABLED, OnOffType.from(MODE_ON.equals(purifierStatus.getDeviceStatus())));
        updateState(DEVICE_CHANNEL_CHILD_LOCK_ENABLED, OnOffType.from(MODE_ON.equals(purifierStatus.getChildLock())));
        updateState(DEVICE_CHANNEL_FAN_MODE_ENABLED, new StringType(purifierStatus.getMode()));
        updateState(DEVICE_CHANNEL_FAN_SPEED_ENABLED, new DecimalType(String.valueOf(purifierStatus.getLevel())));
        updateState(DEVICE_CHANNEL_DISPLAY_ENABLED, OnOffType.from(MODE_ON.equals(purifierStatus.getScreenStatus())));
        updateState(DEVICE_CHANNEL_AIRQUALITY_BASIC, new DecimalType(purifierStatus.getAirQuality()));
        updateState(DEVICE_CHANNEL_AIR_FILTER_LIFE_PERCENTAGE_REMAINING,
                new QuantityType<>(purifierStatus.filter.getPercent(), Units.PERCENT));
    }

    private void processV2BypassPoll(final ExpiringCache<String> cachedResponse) {
        final String deviceFamily = getThing().getProperties().get(DEVICE_PROP_DEVICE_FAMILY);

        final VeSyncDevicePurifierMetadata devContraints = DEV_FAMILY_PURIFIER_MAP.get(deviceFamily);
        if (devContraints == null) {
            logger.warn("{}", getLocalizedText("warning.device.command-device-family-not-found", deviceFamily));
            return;
        }

        String response;
        VeSyncResponse purifierStatus = null;

        synchronized (pollLock) {
            response = cachedResponse.getValue();
            boolean cachedDataUsed = response != null;
            if (response == null) {
                logger.trace("Requesting fresh response");
                response = sendV2BypassCommand(DEVICE_GET_PURIFIER_STATUS,
                        new VeSyncRequestManagedDeviceBypassV2.EmptyPayload());
            } else {
                logger.trace("Using cached response {}", response);
            }

            if (response.equals(EMPTY_STRING)) {
                return;
            }
            if (devContraints.protocolV2Version == 2) {
                purifierStatus = VeSyncConstants.GSON.fromJson(response, VeSyncV2Ver2BypassPurifierStatus.class);
            } else {
                purifierStatus = VeSyncConstants.GSON.fromJson(response, VeSyncV2BypassPurifierStatus.class);
            }

            if (purifierStatus == null) {
                return;
            }

            if (!cachedDataUsed) {
                cachedResponse.putValue(response);
            }
        }

        // Bail and update the status of the thing - it will be updated to online by the next search
        // that detects it is online.
        if (purifierStatus.isMsgDeviceOffline()) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        } else if (purifierStatus.isMsgSuccess()) {
            updateStatus(ThingStatus.ONLINE);
        }

        if (devContraints.protocolV2Version == 2) {
            parseV2Ver2Poll((VeSyncV2Ver2BypassPurifierStatus) purifierStatus);
        } else {
            parseV2Ver1Poll((VeSyncV2BypassPurifierStatus) purifierStatus);
        }
    }

    private void parseV2Ver1Poll(final VeSyncV2BypassPurifierStatus purifierStatus) {
        if (!"0".equals(purifierStatus.result.getCode())) {
            logger.warn("{}", getLocalizedText("warning.device.unexpected-resp-for-air-purifier"));
            return;
        }

        updateState(DEVICE_CHANNEL_ENABLED, OnOffType.from(purifierStatus.result.result.enabled));
        updateState(DEVICE_CHANNEL_CHILD_LOCK_ENABLED, OnOffType.from(purifierStatus.result.result.childLock));
        updateState(DEVICE_CHANNEL_DISPLAY_ENABLED, OnOffType.from(purifierStatus.result.result.display));
        updateState(DEVICE_CHANNEL_AIR_FILTER_LIFE_PERCENTAGE_REMAINING,
                new QuantityType<>(purifierStatus.result.result.filterLife, Units.PERCENT));
        updateState(DEVICE_CHANNEL_FAN_MODE_ENABLED, new StringType(purifierStatus.result.result.mode));
        updateState(DEVICE_CHANNEL_FAN_SPEED_ENABLED, new DecimalType(purifierStatus.result.result.level));
        updateState(DEVICE_CHANNEL_ERROR_CODE, new DecimalType(purifierStatus.result.result.deviceErrorCode));
        updateState(DEVICE_CHANNEL_AIRQUALITY_BASIC, new DecimalType(purifierStatus.result.result.airQuality));
        updateState(DEVICE_CHANNEL_AIRQUALITY_PM25,
                new QuantityType<>(purifierStatus.result.result.airQualityValue, Units.MICROGRAM_PER_CUBICMETRE));
        updateState(DEVICE_CHANNEL_AF_CONFIG_DISPLAY_FOREVER,
                OnOffType.from(purifierStatus.result.result.configuration.displayForever));
        updateState(DEVICE_CHANNEL_AF_CONFIG_AUTO_MODE_PREF,
                new StringType(purifierStatus.result.result.configuration.autoPreference.autoType));
        updateState(DEVICE_CHANNEL_AF_CONFIG_AUTO_ROOM_SIZE, new QuantityType<>(
                purifierStatus.result.result.configuration.autoPreference.roomSize, ImperialUnits.SQUARE_FOOT));

        // Only 400S appears to have this JSON extension object
        if (purifierStatus.result.result.extension != null) {
            if (purifierStatus.result.result.extension.timerRemain > 0) {
                updateState(DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME, new DateTimeType(LocalDateTime.now()
                        .plus(purifierStatus.result.result.extension.timerRemain, ChronoUnit.SECONDS).toString()));
            } else {
                updateState(DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME, new DateTimeItem("nullEnforcements").getState());
            }
            updateState(DEVICE_CHANNEL_AF_SCHEDULES_COUNT,
                    new DecimalType(purifierStatus.result.result.extension.scheduleCount));
        }

        // Not applicable to 400S payload's
        if (purifierStatus.result.result.nightLight != null) {
            updateState(DEVICE_CHANNEL_AF_NIGHT_LIGHT, new DecimalType(purifierStatus.result.result.nightLight));
        }
    }

    private void parseV2Ver2Poll(final VeSyncV2Ver2BypassPurifierStatus purifierStatus) {
        if (!"0".equals(purifierStatus.result.getCode())) {
            logger.warn("{}", getLocalizedText("warning.device.unexpected-resp-for-air-purifier"));
            return;
        }

        updateState(DEVICE_CHANNEL_ENABLED, OnOffType.from(purifierStatus.result.result.getPowerSwitch()));
        updateState(DEVICE_CHANNEL_CHILD_LOCK_ENABLED,
                OnOffType.from(purifierStatus.result.result.getChildLockSwitch()));
        updateState(DEVICE_CHANNEL_AIRQUALITY_BASIC, new DecimalType(purifierStatus.result.result.airQuality));
        updateState(DEVICE_CHANNEL_AIRQUALITY_PM25,
                new QuantityType<>(purifierStatus.result.result.pm25, Units.MICROGRAM_PER_CUBICMETRE));
        updateState(DEVICE_CHANNEL_AIR_FILTER_LIFE_PERCENTAGE_REMAINING,
                new QuantityType<>(purifierStatus.result.result.filterLifePercent, Units.PERCENT));
        updateState(DEVICE_CHANNEL_AF_LIGHT_DETECTION,
                OnOffType.from(purifierStatus.result.result.getLightDetectionSwitch()));
        updateState(DEVICE_CHANNEL_AF_LIGHT_DETECTED,
                OnOffType.from(purifierStatus.result.result.getEnvironmentLightState()));
        updateState(DEVICE_CHANNEL_DISPLAY_ENABLED, OnOffType.from(purifierStatus.result.result.getScreenSwitch()));
        updateState(DEVICE_CHANNEL_FAN_MODE_ENABLED, new StringType(purifierStatus.result.result.workMode));
        updateState(DEVICE_CHANNEL_FAN_SPEED_ENABLED, new DecimalType(purifierStatus.result.result.fanSpeedLevel));
        updateState(DEVICE_CHANNEL_ERROR_CODE, new DecimalType(purifierStatus.result.result.errorCode));
    }
}
