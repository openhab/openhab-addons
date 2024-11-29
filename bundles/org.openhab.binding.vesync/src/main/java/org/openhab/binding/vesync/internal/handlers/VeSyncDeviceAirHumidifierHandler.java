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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vesync.internal.VeSyncBridgeConfiguration;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestManagedDeviceBypassV2;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncResponse;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2BypassHumidifierStatus;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2Ver2BypassHumidifierStatus;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
 * The {@link VeSyncDeviceAirHumidifierHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class VeSyncDeviceAirHumidifierHandler extends VeSyncBaseDeviceHandler {

    public static final String DEV_TYPE_FAMILY_AIR_HUMIDIFIER = "LUH";

    public static final int DEFAULT_AIR_PURIFIER_POLL_RATE = 120;
    private static final int MIN_TARGET_HUMIDITY = 30;
    private static final int MAX_TARGET_HUMIDITY = 80;

    public static final String DEV_FAMILY_CLASSIC_200S = "Classic 200S";
    public static final String DEV_FAMILY_CLASSIC_300S = "Classic 300S";
    public static final String DEV_FAMILY_DUAL_200S = "Dual 200S";
    public static final String DEV_FAMILY_600S = "600S";
    public static final String DEV_FAMILY_OASIS_MIST_EU = "Oasis Mist EU";
    public static final String DEV_FAMILY_OASIS_MIST = "Oasis Mist";

    public static final String DEV_FAMILY_OASIS_MIST_1000 = "Oasis Mist 1000";

    private static final List<String> AUTO_MAN_SLEEP_MODES = Arrays.asList(MODE_AUTO, MODE_MANUAL, MODE_SLEEP);

    private static final List<String> AUTO_MAN_MODES = Arrays.asList(MODE_AUTO, MODE_MANUAL);

    private static final List<String> CLASSIC_300S_NIGHT_LIGHT_MODES = Arrays.asList(MODE_ON, MODE_DIM, MODE_OFF);

    public static final VeSyncDeviceHumidifierMetadata CLASSIC200S = new VeSyncDeviceHumidifierMetadata(1,
            DEV_FAMILY_CLASSIC_200S, Collections.emptyList(), List.of("Classic200S"), AUTO_MAN_MODES, 1, 3, -1, -1,
            false, Collections.emptyList());

    public static final VeSyncDeviceHumidifierMetadata CLASSIC300S = new VeSyncDeviceHumidifierMetadata(1,
            DEV_FAMILY_CLASSIC_300S, Arrays.asList("A601S"), List.of("Classic300S"), AUTO_MAN_SLEEP_MODES, 1, 3, -1, -1,
            false, CLASSIC_300S_NIGHT_LIGHT_MODES);

    public static final VeSyncDeviceHumidifierMetadata DUAL200S = new VeSyncDeviceHumidifierMetadata(1,
            DEV_FAMILY_DUAL_200S, Arrays.asList("D301S"), List.of("Dual200S"), AUTO_MAN_MODES, 1, 2, -1, -1, false,
            Collections.emptyList());

    public static final VeSyncDeviceHumidifierMetadata LV600S = new VeSyncDeviceHumidifierMetadata(1, DEV_FAMILY_600S,
            Arrays.asList("A602S"), Collections.emptyList(), AUTO_MAN_SLEEP_MODES, 1, 3, 0, 3, true,
            CLASSIC_300S_NIGHT_LIGHT_MODES);

    public static final VeSyncDeviceHumidifierMetadata OASIS_MIST_EU = new VeSyncDeviceHumidifierMetadata(1,
            DEV_FAMILY_OASIS_MIST_EU, Collections.emptyList(), Arrays.asList("LUH-O451S-WEU"), AUTO_MAN_MODES, 1, 3, 0,
            3, false, CLASSIC_300S_NIGHT_LIGHT_MODES);

    public static final VeSyncDeviceHumidifierMetadata OASIS_MIST = new VeSyncDeviceHumidifierMetadata(1,
            DEV_FAMILY_OASIS_MIST, Arrays.asList("O451S", "O601S"), Collections.emptyList(), AUTO_MAN_SLEEP_MODES, 1, 3,
            0, 3, true, Collections.emptyList());

    public static final VeSyncDeviceHumidifierMetadata OASIS_MIST_1000 = new VeSyncDeviceHumidifierMetadata(2,
            DEV_FAMILY_OASIS_MIST_1000, Arrays.asList("M101S"), Collections.emptyList(), AUTO_MAN_SLEEP_MODES, 1, 3, 0,
            3, false, Collections.emptyList());

    public static final List<VeSyncDeviceMetadata> SUPPORTED_MODEL_FAMILIES = Arrays.asList(LV600S, CLASSIC300S,
            CLASSIC200S, DUAL200S, OASIS_MIST, OASIS_MIST_EU);

    private final Logger logger = LoggerFactory.getLogger(VeSyncDeviceAirHumidifierHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AIR_HUMIDIFIER);

    private final Object pollLock = new Object();

    public static final Map<String, VeSyncDeviceHumidifierMetadata> DEV_FAMILY_HUMIDIFER_MAP = new HashMap<String, VeSyncDeviceHumidifierMetadata>() {
        {
            put(CLASSIC200S.deviceFamilyName, CLASSIC200S);
            put(CLASSIC300S.deviceFamilyName, CLASSIC300S);
            put(DUAL200S.deviceFamilyName, DUAL200S);
            put(LV600S.deviceFamilyName, LV600S);
            put(OASIS_MIST.deviceFamilyName, OASIS_MIST);
            put(OASIS_MIST_EU.deviceFamilyName, OASIS_MIST_EU);
            put(OASIS_MIST_1000.deviceFamilyName, OASIS_MIST_1000);
        }
    };

    public VeSyncDeviceAirHumidifierHandler(Thing thing, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
    }

    @Override
    protected String[] getChannelsToRemove() {
        String[] toRemove = new String[] {};
        final String deviceFamily = getThing().getProperties().get(DEVICE_PROP_DEVICE_FAMILY);
        if (deviceFamily != null) {
            switch (deviceFamily) {
                case DEV_FAMILY_CLASSIC_300S:
                    toRemove = new String[] { DEVICE_CHANNEL_WARM_ENABLED, DEVICE_CHANNEL_WARM_LEVEL,
                            DEVICE_CHANNEL_AF_SCHEDULES_COUNT, DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME };
                    break;
                case DEV_FAMILY_DUAL_200S:
                case DEV_FAMILY_CLASSIC_200S:
                    toRemove = new String[] { DEVICE_CHANNEL_WARM_ENABLED, DEVICE_CHANNEL_WARM_LEVEL,
                            DEVICE_CHANNEL_AF_NIGHT_LIGHT, DEVICE_CHANNEL_AF_SCHEDULES_COUNT,
                            DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME };
                    break;
                case DEV_FAMILY_OASIS_MIST_1000:
                    toRemove = new String[] { DEVICE_CHANNEL_WARM_ENABLED, DEVICE_CHANNEL_WARM_LEVEL,
                            DEVICE_CHANNEL_AF_NIGHT_LIGHT };
                    break;
                case DEV_FAMILY_OASIS_MIST:
                    toRemove = new String[] { DEVICE_CHANNEL_AF_NIGHT_LIGHT, DEVICE_CHANNEL_AF_SCHEDULES_COUNT,
                            DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME };
                    break;
                case DEV_FAMILY_OASIS_MIST_EU:
                    toRemove = new String[] { DEVICE_CHANNEL_AF_SCHEDULES_COUNT, DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME };
                    break;
            }
        }
        return toRemove;
    }

    @Override
    public void initialize() {
        super.initialize();
        customiseChannels();
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
        return DEV_TYPE_FAMILY_AIR_HUMIDIFIER;
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
        final VeSyncDeviceHumidifierMetadata devContraints = DEV_FAMILY_HUMIDIFER_MAP.get(deviceFamily);
        if (devContraints == null) {
            logger.warn("{}", getLocalizedText("warning.device.command-device-family-not-found", deviceFamily));
            return;
        }

        scheduler.submit(() -> {

            if (command instanceof OnOffType) {
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_ENABLED:
                        sendV2BypassControlCommand(DEVICE_SET_SWITCH,
                                new VeSyncRequestManagedDeviceBypassV2.SetSwitchPayload(command.equals(OnOffType.ON),
                                        0));
                        break;
                    case DEVICE_CHANNEL_DISPLAY_ENABLED:
                        sendV2BypassControlCommand(DEVICE_SET_DISPLAY,
                                new VeSyncRequestManagedDeviceBypassV2.SetState(command.equals(OnOffType.ON)));
                        break;
                    case DEVICE_CHANNEL_STOP_AT_TARGET:
                        sendV2BypassControlCommand(DEVICE_SET_AUTOMATIC_STOP,
                                new VeSyncRequestManagedDeviceBypassV2.EnabledPayload(command.equals(OnOffType.ON)));
                        break;
                    case DEVICE_CHANNEL_WARM_ENABLED:
                        logger.warn("{}", getLocalizedText("warning.device.warm-mode-unsupported"));
                        break;
                }
            } else if (command instanceof QuantityType quantityCommand) {
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_CONFIG_TARGET_HUMIDITY:
                        int targetHumidity = quantityCommand.intValue();
                        if (targetHumidity < MIN_TARGET_HUMIDITY) {
                            logger.warn("{}", getLocalizedText("warning.device.humidity-under", MIN_TARGET_HUMIDITY));
                            targetHumidity = MIN_TARGET_HUMIDITY;
                        } else if (targetHumidity > MAX_TARGET_HUMIDITY) {
                            logger.warn("{}", getLocalizedText("warning.device.humidity-over", MAX_TARGET_HUMIDITY));
                            targetHumidity = MAX_TARGET_HUMIDITY;
                        }

                        sendV2BypassControlCommand(DEVICE_SET_HUMIDITY_MODE,
                                new VeSyncRequestManagedDeviceBypassV2.SetMode(
                                        devContraints.getProtocolMode(MODE_AUTO)),
                                false);

                        sendV2BypassControlCommand(DEVICE_SET_TARGET_HUMIDITY_MODE,
                                new VeSyncRequestManagedDeviceBypassV2.SetTargetHumidity(targetHumidity));
                        break;
                    case DEVICE_CHANNEL_MIST_LEVEL:
                        int targetMistLevel = quantityCommand.intValue();
                        if (!devContraints.isTargetMistLevelSupported(targetMistLevel)) {
                            logger.warn("{}",
                                    getLocalizedText("warning.device.mist-level-invalid", command,
                                            devContraints.deviceFamilyName, devContraints.targetMinMistLevel,
                                            devContraints.targetMaxMistLevel));
                            targetMistLevel = targetMistLevel < devContraints.targetMinMistLevel
                                    ? devContraints.targetMinMistLevel
                                    : devContraints.targetMaxMistLevel;
                        }

                        // If more devices have this the hope is it's those with the prefix LUH so the check can
                        // be simplified, originally devices mapped 1/5/9 to 1/2/3.
                        if (!DEV_FAMILY_DUAL_200S.equals(deviceFamily)) {
                            // Re-map to what appears to be bitwise encoding of the states
                            switch (targetMistLevel) {
                                case 1:
                                    targetMistLevel = 1;
                                    break;
                                case 2:
                                    targetMistLevel = 5;
                                    break;
                                case 3:
                                    targetMistLevel = 9;
                                    break;
                            }
                        }

                        sendV2BypassControlCommand(DEVICE_SET_HUMIDITY_MODE,
                                new VeSyncRequestManagedDeviceBypassV2.SetMode(MODE_MANUAL), false);

                        sendV2BypassControlCommand(DEVICE_SET_VIRTUAL_LEVEL,
                                new VeSyncRequestManagedDeviceBypassV2.SetLevelPayload(0, DEVICE_LEVEL_TYPE_MIST,
                                        targetMistLevel));
                        break;
                    case DEVICE_CHANNEL_WARM_LEVEL:
                        int targetWarmMistLevel = quantityCommand.intValue();
                        if (!devContraints.isTargetWramMistLevelSupported(targetWarmMistLevel)) {
                            logger.warn("{}",
                                    getLocalizedText("warning.device.mist-level-invalid", command,
                                            devContraints.deviceFamilyName, devContraints.targetMinWarmMistLevel,
                                            devContraints.targetMaxWarmMistLevel));
                            targetWarmMistLevel = targetWarmMistLevel < devContraints.targetMinWarmMistLevel
                                    ? devContraints.targetMinWarmMistLevel
                                    : devContraints.targetMaxWarmMistLevel;
                        }

                        sendV2BypassControlCommand(DEVICE_SET_LEVEL,
                                new VeSyncRequestManagedDeviceBypassV2.SetLevelPayload(0, DEVICE_LEVEL_TYPE_WARM_MIST,
                                        targetWarmMistLevel));
                        break;
                }
            } else if (command instanceof StringType) {
                final String targetMode = command.toString().toLowerCase();
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_HUMIDIFIER_MODE:
                        if (!devContraints.fanModes.contains(targetMode)) {
                            logger.warn("{}", getLocalizedText("warning.device.humidity-mode", command,
                                    devContraints.deviceFamilyName, String.join(",", devContraints.fanModes)));
                            return;
                        }
                        sendV2BypassControlCommand(DEVICE_SET_HUMIDITY_MODE,
                                new VeSyncRequestManagedDeviceBypassV2.SetMode(
                                        devContraints.getProtocolMode(targetMode)));
                        break;
                    case DEVICE_CHANNEL_AF_NIGHT_LIGHT:
                        if (!devContraints.nightLightModes.contains(targetMode)) {
                            logger.warn("{}", getLocalizedText("warning.device.night-light-invalid", command,
                                    devContraints.deviceFamilyName, String.join(",", devContraints.nightLightModes)));
                            return;
                        }

                        int targetValue;
                        switch (targetMode) {
                            case MODE_OFF:
                                targetValue = 0;
                                break;
                            case MODE_DIM:
                                targetValue = 50;
                                break;
                            case MODE_ON:
                                targetValue = 100;
                                break;
                            default:
                                return; // should never hit
                        }
                        sendV2BypassControlCommand(DEVICE_SET_NIGHT_LIGHT_BRIGHTNESS,
                                new VeSyncRequestManagedDeviceBypassV2.SetNightLightBrightness(targetValue));
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
        String response;
        VeSyncResponse humidifierStatus;

        final String deviceFamily = getThing().getProperties().get(DEVICE_PROP_DEVICE_FAMILY);

        final VeSyncDeviceHumidifierMetadata devContraints = DEV_FAMILY_HUMIDIFER_MAP.get(deviceFamily);
        if (devContraints == null) {
            logger.warn("{}", getLocalizedText("warning.device.poll-device-family-not-found", deviceFamily));
            return;
        }

        synchronized (pollLock) {
            response = cachedResponse.getValue();
            boolean cachedDataUsed = response != null;
            if (response == null) {
                logger.trace("Requesting fresh response");
                response = sendV2BypassCommand(DEVICE_GET_HUMIDIFIER_STATUS,
                        new VeSyncRequestManagedDeviceBypassV2.EmptyPayload());
            } else {
                logger.trace("Using cached response {}", response);
            }

            if (response.equals(EMPTY_STRING)) {
                return;
            }

            if (devContraints.protocolV2Version == 2) {
                humidifierStatus = VeSyncConstants.GSON.fromJson(response, VeSyncV2Ver2BypassHumidifierStatus.class);
            } else {
                humidifierStatus = VeSyncConstants.GSON.fromJson(response, VeSyncV2BypassHumidifierStatus.class);
            }

            if (humidifierStatus == null) {
                return;
            }

            if (!cachedDataUsed) {
                cachedResponse.putValue(response);
            }
        }

        // Bail and update the status of the thing - it will be updated to online by the next search
        // that detects it is online.
        if (humidifierStatus.isMsgDeviceOffline()) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        } else if (humidifierStatus.isMsgSuccess()) {
            updateStatus(ThingStatus.ONLINE);
        }

        if (devContraints.protocolV2Version != 2) {
            parseV2Ver1Poll((VeSyncV2BypassHumidifierStatus) humidifierStatus, deviceFamily);
        } else {
            parseV2Ver2Poll((VeSyncV2Ver2BypassHumidifierStatus) humidifierStatus);
        }
    }

    private void parseV2Ver1Poll(final VeSyncV2BypassHumidifierStatus humidifierStatus,
            final @Nullable String deviceFamily) {
        if (!"0".equals(humidifierStatus.result.getCode())) {
            logger.warn("{}", getLocalizedText("warning.device.unexpected-resp-for-air-humidifier"));
            return;
        }

        updateState(DEVICE_CHANNEL_ENABLED, OnOffType.from(humidifierStatus.result.result.enabled));
        updateState(DEVICE_CHANNEL_DISPLAY_ENABLED, OnOffType.from(humidifierStatus.result.result.display));
        updateState(DEVICE_CHANNEL_WATER_LACKS, OnOffType.from(humidifierStatus.result.result.waterLacks));
        updateState(DEVICE_CHANNEL_HUMIDITY_HIGH, OnOffType.from(humidifierStatus.result.result.humidityHigh));
        updateState(DEVICE_CHANNEL_WATER_TANK_LIFTED, OnOffType.from(humidifierStatus.result.result.waterTankLifted));
        updateState(DEVICE_CHANNEL_STOP_AT_TARGET,
                OnOffType.from(humidifierStatus.result.result.automaticStopReachTarget));
        updateState(DEVICE_CHANNEL_HUMIDITY,
                new QuantityType<>(humidifierStatus.result.result.humidity, Units.PERCENT));
        updateState(DEVICE_CHANNEL_MIST_LEVEL, new DecimalType(humidifierStatus.result.result.mistLevel));
        // Map back HUMIDITY -> AUTO if necessary for devices where auto is remapped
        if (MODE_AUTO_HUMIDITY.equals(humidifierStatus.result.result.mode)) {
            humidifierStatus.result.result.mode = MODE_AUTO;
        }
        updateState(DEVICE_CHANNEL_HUMIDIFIER_MODE, new StringType(humidifierStatus.result.result.mode));

        // Only the 300S supports nightlight currently of tested devices.
        if (DEV_FAMILY_CLASSIC_300S.equals(deviceFamily) || DEV_FAMILY_600S.equals(deviceFamily)) {
            // Map the numeric that only applies to the same modes as the Air Filter 300S series.
            if (humidifierStatus.result.result.nightLightBrightness == 0) {
                updateState(DEVICE_CHANNEL_AF_NIGHT_LIGHT, new StringType(MODE_OFF));
            } else if (humidifierStatus.result.result.nightLightBrightness == 100) {
                updateState(DEVICE_CHANNEL_AF_NIGHT_LIGHT, new StringType(MODE_ON));
            } else {
                updateState(DEVICE_CHANNEL_AF_NIGHT_LIGHT, new StringType(MODE_DIM));
            }
        }
        if (DEV_FAMILY_600S.equals(deviceFamily) || DEV_FAMILY_OASIS_MIST.equals(deviceFamily)) {
            updateState(DEVICE_CHANNEL_WARM_ENABLED, OnOffType.from(humidifierStatus.result.result.warnEnabled));
            updateState(DEVICE_CHANNEL_WARM_LEVEL, new DecimalType(humidifierStatus.result.result.warmLevel));
        }
        updateState(DEVICE_CHANNEL_CONFIG_TARGET_HUMIDITY,
                new QuantityType<>(humidifierStatus.result.result.configuration.autoTargetHumidity, Units.PERCENT));
    }

    private void parseV2Ver2Poll(final VeSyncV2Ver2BypassHumidifierStatus humidifierStatus) {
        if (!"0".equals(humidifierStatus.result.getCode())) {
            logger.warn("{}", getLocalizedText("warning.device.unexpected-resp-for-air-humidifier"));
            return;
        }

        updateState(DEVICE_CHANNEL_ENABLED, OnOffType.from(humidifierStatus.result.result.getPowerSwitch()));
        updateState(DEVICE_CHANNEL_DISPLAY_ENABLED, OnOffType.from(humidifierStatus.result.result.getScreenSwitch()));
        updateState(DEVICE_CHANNEL_WATER_LACKS, OnOffType.from(humidifierStatus.result.result.getWaterLacksState()));
        updateState(DEVICE_CHANNEL_WATER_TANK_LIFTED,
                OnOffType.from(humidifierStatus.result.result.getWaterTankLifted()));
        updateState(DEVICE_CHANNEL_STOP_AT_TARGET, OnOffType.from(humidifierStatus.result.result.getAutoStopSwitch()));
        updateState(DEVICE_CHANNEL_HUMIDITY,
                new QuantityType<>(humidifierStatus.result.result.humidity, Units.PERCENT));
        updateState(DEVICE_CHANNEL_MIST_LEVEL, new DecimalType(humidifierStatus.result.result.mistLevel));
        if (MODE_AUTO_HUMIDITY.equals(humidifierStatus.result.result.workMode)) {
            humidifierStatus.result.result.workMode = MODE_AUTO;
        }
        updateState(DEVICE_CHANNEL_HUMIDIFIER_MODE, new StringType(humidifierStatus.result.result.workMode));
        updateState(DEVICE_CHANNEL_CONFIG_TARGET_HUMIDITY,
                new QuantityType<>(humidifierStatus.result.result.targetHumidity, Units.PERCENT));
        updateState(DEVICE_CHANNEL_ERROR_CODE, new DecimalType(humidifierStatus.result.result.errorCode));
        updateState(DEVICE_CHANNEL_AF_SCHEDULES_COUNT, new DecimalType(humidifierStatus.result.result.scheduleCount));
        if (humidifierStatus.result.result.timerRemain > 0) {
            updateState(DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME, new DateTimeType(LocalDateTime.now()
                    .plus(humidifierStatus.result.result.timerRemain, ChronoUnit.MINUTES).toString()));
        } else {
            updateState(DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME, new DateTimeItem("nullEnforcements").getState());
        }
    }
}
