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
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vesync.internal.VeSyncBridgeConfiguration;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestManagedDeviceBypassV2;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestV1ManagedDeviceDetails;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2BypassPurifierStatus;
import org.openhab.binding.vesync.internal.dto.responses.v1.VeSyncV1AirPurifierDeviceDetailsResponse;
import org.openhab.core.cache.ExpiringCache;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeSyncDeviceAirPurifierHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VeSyncDeviceAirPurifierHandler extends VeSyncBaseDeviceHandler {

    public static final String DEV_TYPE_FAMILY_AIR_PURIFIER = "LAP";

    public static final int DEFAULT_AIR_PURIFIER_POLL_RATE = 120;

    public static final String DEV_FAMILY_CORE_200S = "200S";
    public static final String DEV_FAMILY_CORE_300S = "300S";
    public static final String DEV_FAMILY_CORE_400S = "400S";
    public static final String DEV_FAMILY_CORE_600S = "600S";

    public static final String DEV_FAMILY_PUR_131S = "131S";

    public static final VeSyncDeviceMetadata CORE200S = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_200S,
            Arrays.asList("C201S", "C202S"), List.of("Core200S"));

    public static final VeSyncDeviceMetadata CORE300S = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_300S,
            List.of("C301S", "C302S"), List.of("Core300S"));

    public static final VeSyncDeviceMetadata CORE400S = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_400S, List.of("C401S"),
            List.of("Core400S"));

    public static final VeSyncDeviceMetadata CORE600S = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_600S, List.of("C601S"),
            List.of("Core600S"));

    public static final VeSyncDeviceMetadata PUR131S = new VeSyncDeviceMetadata(DEV_FAMILY_PUR_131S,
            Collections.emptyList(), Arrays.asList("LV-PUR131S", "LV-RH131S"));

    public static final List<VeSyncDeviceMetadata> SUPPORTED_MODEL_FAMILIES = Arrays.asList(CORE600S, CORE400S,
            CORE300S, CORE200S, PUR131S);

    private static final List<String> CORE_400S600S_FAN_MODES = Arrays.asList(MODE_AUTO, MODE_MANUAL, MODE_SLEEP);
    private static final List<String> CORE_200S300S_FAN_MODES = Arrays.asList(MODE_MANUAL, MODE_SLEEP);
    private static final List<String> CORE_200S300S_NIGHT_LIGHT_MODES = Arrays.asList(MODE_ON, MODE_DIM, MODE_OFF);

    private final Logger logger = LoggerFactory.getLogger(VeSyncDeviceAirPurifierHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AIR_PURIFIER);

    private final Object pollLock = new Object();

    public VeSyncDeviceAirPurifierHandler(Thing thing) {
        super(thing);
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
                    toRemove = new String[] { DEVICE_CHANNEL_AF_NIGHT_LIGHT };
                    break;
                case DEV_FAMILY_PUR_131S:
                    toRemove = new String[] { DEVICE_CHANNEL_AF_NIGHT_LIGHT, DEVICE_CHANNEL_AF_CONFIG_AUTO_ROOM_SIZE,
                            DEVICE_CHANNEL_AF_CONFIG_AUTO_MODE_PREF, DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME,
                            DEVICE_CHANNEL_AIR_FILTER_LIFE_PERCENTAGE_REMAINING, DEVICE_CHANNEL_AIRQUALITY_PM25,
                            DEVICE_CHANNEL_AF_SCHEDULES_COUNT, DEVICE_CHANNEL_AF_CONFIG_DISPLAY_FOREVER };
                    break;
                default:
                    toRemove = new String[] { DEVICE_CHANNEL_AF_AUTO_OFF_CALC_TIME, DEVICE_CHANNEL_AF_SCHEDULES_COUNT };
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
                    case DEVICE_CHANNEL_CHILD_LOCK_ENABLED:
                        sendV2BypassControlCommand(DEVICE_SET_CHILD_LOCK,
                                new VeSyncRequestManagedDeviceBypassV2.SetChildLock(command.equals(OnOffType.ON)));
                        break;
                }
            } else if (command instanceof StringType) {
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_FAN_MODE_ENABLED:
                        final String targetFanMode = command.toString().toLowerCase();
                        switch (deviceFamily) {
                            case DEV_FAMILY_CORE_600S:
                            case DEV_FAMILY_CORE_400S:
                                if (!CORE_400S600S_FAN_MODES.contains(targetFanMode)) {
                                    logger.warn(
                                            "Fan mode command for \"{}\" is not valid in the (Core400S) API possible options {}",
                                            command, String.join(",", CORE_400S600S_FAN_MODES));
                                    return;
                                }
                                break;
                            case DEV_FAMILY_CORE_200S:
                            case DEV_FAMILY_CORE_300S:
                                if (!CORE_200S300S_FAN_MODES.contains(targetFanMode)) {
                                    logger.warn(
                                            "Fan mode command for \"{}\" is not valid in the (Core200S/Core300S) API possible options {}",
                                            command, String.join(",", CORE_200S300S_FAN_MODES));
                                    return;
                                }
                                break;
                        }

                        sendV2BypassControlCommand(DEVICE_SET_PURIFIER_MODE,
                                new VeSyncRequestManagedDeviceBypassV2.SetMode(targetFanMode));
                        break;
                    case DEVICE_CHANNEL_AF_NIGHT_LIGHT:
                        final String targetNightLightMode = command.toString().toLowerCase();
                        switch (deviceFamily) {
                            case DEV_FAMILY_CORE_600S:
                            case DEV_FAMILY_CORE_400S:
                                logger.warn("Core400S API does not support night light");
                                return;
                            case DEV_FAMILY_CORE_200S:
                            case DEV_FAMILY_CORE_300S:
                                if (!CORE_200S300S_NIGHT_LIGHT_MODES.contains(targetNightLightMode)) {
                                    logger.warn(
                                            "Night light mode command for \"{}\" is not valid in the (Core200S/Core300S) API possible options {}",
                                            command, String.join(",", CORE_200S300S_NIGHT_LIGHT_MODES));
                                    return;
                                }

                                sendV2BypassControlCommand(DEVICE_SET_NIGHT_LIGHT,
                                        new VeSyncRequestManagedDeviceBypassV2.SetNightLight(targetNightLightMode));

                                break;
                        }
                        break;
                }
            } else if (command instanceof QuantityType quantityCommand) {
                switch (channelUID.getId()) {
                    case DEVICE_CHANNEL_FAN_SPEED_ENABLED:
                        // If the fan speed is being set enforce manual mode
                        sendV2BypassControlCommand(DEVICE_SET_PURIFIER_MODE,
                                new VeSyncRequestManagedDeviceBypassV2.SetMode(MODE_MANUAL), false);

                        int requestedLevel = quantityCommand.intValue();
                        if (requestedLevel < 1) {
                            logger.warn("Fan speed command less than 1 - adjusting to 1 as the valid API value");
                            requestedLevel = 1;
                        }

                        switch (deviceFamily) {
                            case DEV_FAMILY_CORE_600S:
                            case DEV_FAMILY_CORE_400S:
                                if (requestedLevel > 4) {
                                    logger.warn(
                                            "Fan speed command greater than 4 - adjusting to 4 as the valid (Core400S) API value");
                                    requestedLevel = 4;
                                }
                                break;
                            case DEV_FAMILY_CORE_200S:
                            case DEV_FAMILY_CORE_300S:
                                if (requestedLevel > 3) {
                                    logger.warn(
                                            "Fan speed command greater than 3 - adjusting to 3 as the valid (Core200S/Core300S) API value");
                                    requestedLevel = 3;
                                }
                                break;
                        }

                        sendV2BypassControlCommand(DEVICE_SET_LEVEL,
                                new VeSyncRequestManagedDeviceBypassV2.SetLevelPayload(0, DEVICE_LEVEL_TYPE_WIND,
                                        requestedLevel));
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

        switch (deviceFamily) {
            case DEV_FAMILY_CORE_600S:
            case DEV_FAMILY_CORE_400S:
            case DEV_FAMILY_CORE_300S:
            case DEV_FAMILY_CORE_200S:
                processV2BypassPoll(cachedResponse);
                break;
            case DEV_FAMILY_PUR_131S:
                processV1AirPurifierPoll(cachedResponse);
                break;
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
                response = sendV1Command("POST", "https://smartapi.vesync.com/131airPurifier/v1/device/deviceDetail",
                        new VeSyncRequestV1ManagedDeviceDetails(deviceUuid));
            } else {
                logger.trace("Using cached response {}", response);
            }

            if (response.equals(EMPTY_STRING)) {
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
            logger.warn("Check Thing type has been set - API gave a unexpected response for an Air Purifier");
            return;
        }

        updateState(DEVICE_CHANNEL_ENABLED, OnOffType.from(MODE_ON.equals(purifierStatus.getDeviceStatus())));
        updateState(DEVICE_CHANNEL_CHILD_LOCK_ENABLED, OnOffType.from(MODE_ON.equals(purifierStatus.getChildLock())));
        updateState(DEVICE_CHANNEL_FAN_MODE_ENABLED, new StringType(purifierStatus.getMode()));
        updateState(DEVICE_CHANNEL_FAN_SPEED_ENABLED, new DecimalType(String.valueOf(purifierStatus.getLevel())));
        updateState(DEVICE_CHANNEL_DISPLAY_ENABLED, OnOffType.from(MODE_ON.equals(purifierStatus.getScreenStatus())));
        updateState(DEVICE_CHANNEL_AIRQUALITY_BASIC, new DecimalType(purifierStatus.getAirQuality()));
    }

    private void processV2BypassPoll(final ExpiringCache<String> cachedResponse) {
        String response;
        VeSyncV2BypassPurifierStatus purifierStatus;
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

            purifierStatus = VeSyncConstants.GSON.fromJson(response, VeSyncV2BypassPurifierStatus.class);

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

        if (!"0".equals(purifierStatus.result.getCode())) {
            logger.warn("Check Thing type has been set - API gave a unexpected response for an Air Purifier");
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

        updateState(DEVICE_CHANNEL_AF_CONFIG_AUTO_ROOM_SIZE,
                new DecimalType(purifierStatus.result.result.configuration.autoPreference.roomSize));

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
}
