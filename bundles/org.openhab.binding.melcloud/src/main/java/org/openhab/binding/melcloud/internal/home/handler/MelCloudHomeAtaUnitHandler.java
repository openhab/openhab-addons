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
package org.openhab.binding.melcloud.internal.home.handler;

import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaCapabilities;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaUnit;
import org.openhab.binding.melcloud.internal.home.config.MelCloudHomeUnitConfig;
import org.openhab.binding.melcloud.internal.logging.SensitiveDataMasker;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MelCloudHomeAtaUnitHandler} handles a single MELCloud Home Air-to-Air unit.
 *
 * <p>
 * State comes from the bridge's centralized {@code /context} poll; energy and outdoor temperature are polled
 * directly at a longer interval. Outbound commands are deduplicated and routed through the bridge's shared
 * {@code MelCloudHomeRequestPacer}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtaUnitHandler extends BaseThingHandler implements MelCloudHomeAtaUnitListener {

    private static final long TELEMETRY_POLL_INTERVAL_SECONDS = 1800;
    private static final String ENERGY_CONSUMED_MEASURE = "cumulative_energy_consumed_since_last_upload";

    /**
     * Maps the API's word-based operation mode onto the legacy binding's numeric codes (1 = Heat, 2 = Dry, 3 = Cool,
     * 7 = Fan, 8 = Auto). Only {@code 1} and {@code 3} are confirmed API values; control commands always send the
     * word value.
     */
    private static final Map<String, Integer> OPERATION_MODE_WORD_TO_CODE = Map.of("Heat", 1, "Dry", 2, "Cool", 3,
            "Fan", 7, "Automatic", 8);
    private static final Map<Integer, String> OPERATION_MODE_CODE_TO_WORD = OPERATION_MODE_WORD_TO_CODE.entrySet()
            .stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    /**
     * Mirrors {@link MelCloudHomeAtaUnit}'s own numeric-to-word normalization for fan speed, so a Number channel
     * round-trips through the same codes the raw API sometimes already sends.
     */
    private static final Map<String, Integer> FAN_SPEED_WORD_TO_CODE = Map.of("Auto", 0, "One", 1, "Two", 2, "Three", 3,
            "Four", 4, "Five", 5);
    private static final Map<Integer, String> FAN_SPEED_CODE_TO_WORD = FAN_SPEED_WORD_TO_CODE.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    /**
     * The MELCloud Home API has no numeric encoding for vane horizontal position; these codes are invented to mirror
     * the legacy binding's {@code vaneHorizontal-channel} numbering (0 = Auto, 1-5 = fixed positions, 12 = Swing).
     */
    private static final Map<String, Integer> VANE_HORIZONTAL_WORD_TO_CODE = Map.of("Auto", 0, "Left", 1, "LeftCentre",
            2, "Centre", 3, "RightCentre", 4, "Right", 5, "Swing", 12);
    private static final Map<Integer, String> VANE_HORIZONTAL_CODE_TO_WORD = VANE_HORIZONTAL_WORD_TO_CODE.entrySet()
            .stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    /**
     * Mirrors {@link MelCloudHomeAtaUnit}'s own numeric-to-word normalization for vane vertical position, so a
     * Number channel round-trips through the same codes the raw API sometimes already sends.
     */
    private static final Map<String, Integer> VANE_VERTICAL_WORD_TO_CODE = Map.of("Auto", 0, "One", 1, "Two", 2,
            "Three", 3, "Four", 4, "Five", 5, "Swing", 7);
    private static final Map<Integer, String> VANE_VERTICAL_CODE_TO_WORD = VANE_VERTICAL_WORD_TO_CODE.entrySet()
            .stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeAtaUnitHandler.class);

    private MelCloudHomeUnitConfig config = new MelCloudHomeUnitConfig();
    private @Nullable MelCloudHomeAccountHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> telemetryFuture;
    private volatile @Nullable MelCloudHomeAtaUnit lastKnownUnit;
    private volatile boolean capabilitiesPropertiesSet;
    /** Last value sent per channel since the last poll; see {@link #isRedundant(String, Object, boolean)}. */
    private final Map<String, Object> sentCommands = new ConcurrentHashMap<>();

    public MelCloudHomeAtaUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing {} handler.", getThing().getThingTypeUID());
        config = getConfigAs(MelCloudHomeUnitConfig.class);
        if (config.unitId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "unitId is required");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof MelCloudHomeAccountHandler accountHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not set");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        initializeBridge(accountHandler, bridge.getStatus());
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler != null) {
            handler.unregisterAtaUnitListener(config.unitId);
        }
        cancelTelemetryPoll();
        sentCommands.clear();
        bridgeHandler = null;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof MelCloudHomeAccountHandler accountHandler) {
            initializeBridge(accountHandler, bridgeStatusInfo.getStatus());
        }
    }

    private void initializeBridge(MelCloudHomeAccountHandler accountHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());
        this.bridgeHandler = accountHandler;
        if (bridgeStatus == ThingStatus.ONLINE) {
            accountHandler.registerAtaUnitListener(config.unitId, this);
            startTelemetryPollIfNeeded();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command '{}' to channel {}", command, channelUID);

        if (command instanceof RefreshType) {
            MelCloudHomeAtaUnit knownUnit = lastKnownUnit;
            if (knownUnit != null) {
                publishState(knownUnit);
            }
            return;
        }

        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler == null) {
            logger.debug("No connection to MELCloud Home available, ignoring command");
            return;
        }

        MelCloudHomeAtaUnit lastUnit = lastKnownUnit;
        MelCloudHomeAtaControlRequest request = new MelCloudHomeAtaControlRequest();
        String channelId = channelUID.getId();
        Object requested;
        switch (channelId) {
            case CHANNEL_POWER:
                boolean powerValue = command == OnOffType.ON;
                if (isRedundant(channelId, powerValue, lastUnit != null && lastUnit.isPower() == powerValue)) {
                    logger.debug("Skipping power command, unit already reports power={}", powerValue);
                    return;
                }
                request.power = powerValue;
                requested = powerValue;
                break;
            case CHANNEL_HOME_OPERATION_MODE:
                Integer operationModeCode = toInt(command);
                String operationModeWord = operationModeCode == null ? null
                        : OPERATION_MODE_CODE_TO_WORD.get(operationModeCode);
                if (operationModeWord == null) {
                    logger.debug("Unknown operation mode code '{}', ignoring command", command);
                    return;
                }
                if (isRedundant(channelId, operationModeWord,
                        lastUnit != null && operationModeWord.equals(lastUnit.getOperationMode()))) {
                    logger.debug("Skipping operation mode command, unit already reports mode={}", operationModeWord);
                    return;
                }
                request.operationMode = operationModeWord;
                requested = operationModeWord;
                break;
            case CHANNEL_HOME_SET_TEMPERATURE:
                Double temperature = toCelsius(command);
                if (temperature == null) {
                    return;
                }
                if (isRedundant(channelId, temperature,
                        lastUnit != null && lastUnit.getSetTemperature().filter(temperature::equals).isPresent())) {
                    logger.debug("Skipping set temperature command, unit already reports {}", temperature);
                    return;
                }
                request.setTemperature = temperature;
                requested = temperature;
                break;
            case CHANNEL_HOME_FAN_SPEED:
                Integer fanSpeedCode = toInt(command);
                String fanSpeedWord = fanSpeedCode == null ? null : FAN_SPEED_CODE_TO_WORD.get(fanSpeedCode);
                if (fanSpeedWord == null) {
                    logger.debug("Unknown fan speed code '{}', ignoring command", command);
                    return;
                }
                if (isRedundant(channelId, fanSpeedWord,
                        lastUnit != null && lastUnit.getFanSpeed().filter(fanSpeedWord::equals).isPresent())) {
                    logger.debug("Skipping fan speed command, unit already reports {}", fanSpeedWord);
                    return;
                }
                request.setFanSpeed = fanSpeedWord;
                requested = fanSpeedWord;
                break;
            case CHANNEL_HOME_VANE_HORIZONTAL:
                Integer vaneHorizontalCode = toInt(command);
                String vaneHorizontalWord = vaneHorizontalCode == null ? null
                        : VANE_HORIZONTAL_CODE_TO_WORD.get(vaneHorizontalCode);
                if (vaneHorizontalWord == null) {
                    logger.debug("Unknown vane horizontal code '{}', ignoring command", command);
                    return;
                }
                if (isRedundant(channelId, vaneHorizontalWord, lastUnit != null
                        && lastUnit.getVaneHorizontalDirection().filter(vaneHorizontalWord::equals).isPresent())) {
                    logger.debug("Skipping vane horizontal command, unit already reports {}", vaneHorizontalWord);
                    return;
                }
                request.vaneHorizontalDirection = vaneHorizontalWord;
                requested = vaneHorizontalWord;
                break;
            case CHANNEL_HOME_VANE_VERTICAL:
                Integer vaneVerticalCode = toInt(command);
                String vaneVerticalWord = vaneVerticalCode == null ? null
                        : VANE_VERTICAL_CODE_TO_WORD.get(vaneVerticalCode);
                if (vaneVerticalWord == null) {
                    logger.debug("Unknown vane vertical code '{}', ignoring command", command);
                    return;
                }
                if (isRedundant(channelId, vaneVerticalWord, lastUnit != null
                        && lastUnit.getVaneVerticalDirection().filter(vaneVerticalWord::equals).isPresent())) {
                    logger.debug("Skipping vane vertical command, unit already reports {}", vaneVerticalWord);
                    return;
                }
                request.vaneVerticalDirection = vaneVerticalWord;
                requested = vaneVerticalWord;
                break;
            default:
                logger.debug("Read-only or unknown channel {}, skipping command", channelUID);
                return;
        }

        Object sentValue = requested;
        sentCommands.put(channelId, sentValue);
        handler.getRequestPacer().schedule(() -> {
            try {
                handler.getApiClient().controlAtaUnit(handler.getAccessToken(), config.unitId, request);
            } catch (MelCloudCommException e) {
                sentCommands.remove(channelId, sentValue);
                logger.debug("Command '{}' to channel '{}' failed, reason {}. ", command, channelUID, e.getMessage());
            }
        });
    }

    /**
     * Whether a command is redundant. The value the binding itself sent for the channel since the last poll takes
     * precedence over the polled unit state, which is up to one poll interval old and does not yet reflect it.
     *
     * @param channelId the channel the command targets
     * @param requested the value the command asks for
     * @param matchesPolledState whether the last polled unit state already has that value
     */
    private boolean isRedundant(String channelId, Object requested, boolean matchesPolledState) {
        Object sent = sentCommands.get(channelId);
        return sent != null ? sent.equals(requested) : matchesPolledState;
    }

    @Override
    public void onAtaUnitMissing() {
        lastKnownUnit = null;
        sentCommands.clear();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Unit not found in the MELCloud Home account");
    }

    @Override
    public void onAtaUnitUpdated(MelCloudHomeAtaUnit unit) {
        lastKnownUnit = unit;
        sentCommands.clear();
        publishState(unit);
    }

    private void publishState(MelCloudHomeAtaUnit unit) {
        updateCapabilityProperties(unit.capabilities);
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_POWER, OnOffType.from(unit.isPower()));
        Integer operationModeCode = OPERATION_MODE_WORD_TO_CODE.get(unit.getOperationMode());
        if (operationModeCode != null) {
            updateState(CHANNEL_HOME_OPERATION_MODE, new DecimalType(operationModeCode));
        } else {
            logger.debug("Unknown operation mode word '{}', skipping channel update", unit.getOperationMode());
        }
        unit.getSetTemperature().ifPresentOrElse(
                value -> updateState(CHANNEL_HOME_SET_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)),
                () -> updateState(CHANNEL_HOME_SET_TEMPERATURE, UnDefType.UNDEF));
        unit.getRoomTemperature().ifPresentOrElse(
                value -> updateState(CHANNEL_HOME_ROOM_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)),
                () -> updateState(CHANNEL_HOME_ROOM_TEMPERATURE, UnDefType.UNDEF));
        unit.getFanSpeed().ifPresentOrElse(value -> {
            Integer code = FAN_SPEED_WORD_TO_CODE.get(value);
            if (code != null) {
                updateState(CHANNEL_HOME_FAN_SPEED, new DecimalType(code));
            } else {
                logger.debug("Unknown fan speed word '{}', skipping channel update", value);
            }
        }, () -> updateState(CHANNEL_HOME_FAN_SPEED, UnDefType.UNDEF));
        unit.getVaneHorizontalDirection().ifPresentOrElse(value -> {
            Integer code = VANE_HORIZONTAL_WORD_TO_CODE.get(value);
            if (code != null) {
                updateState(CHANNEL_HOME_VANE_HORIZONTAL, new DecimalType(code));
            } else {
                logger.debug("Unknown vane horizontal word '{}', skipping channel update", value);
            }
        }, () -> updateState(CHANNEL_HOME_VANE_HORIZONTAL, UnDefType.UNDEF));
        unit.getVaneVerticalDirection().ifPresentOrElse(value -> {
            Integer code = VANE_VERTICAL_WORD_TO_CODE.get(value);
            if (code != null) {
                updateState(CHANNEL_HOME_VANE_VERTICAL, new DecimalType(code));
            } else {
                logger.debug("Unknown vane vertical word '{}', skipping channel update", value);
            }
        }, () -> updateState(CHANNEL_HOME_VANE_VERTICAL, UnDefType.UNDEF));
        updateState(CHANNEL_IN_STANDBY_MODE, OnOffType.from(unit.isInStandbyMode()));
        updateState(CHANNEL_IS_IN_ERROR, OnOffType.from(unit.isInError()));
        unit.getErrorCode().ifPresentOrElse(value -> updateState(CHANNEL_ERROR_CODE, new StringType(value)),
                () -> updateState(CHANNEL_ERROR_CODE, UnDefType.UNDEF));
        Integer rssi = unit.rssi;
        if (rssi != null) {
            updateState(CHANNEL_RSSI, new DecimalType(mapRssiToSignalStrength(rssi)));
        } else {
            updateState(CHANNEL_RSSI, UnDefType.UNDEF);
        }
    }

    /**
     * Writes the unit's capability flags/limits as Thing properties, once. Capabilities are static for the lifetime
     * of a unit (they don't change between {@code /context} polls), so there is no need to re-write them on every
     * update.
     *
     * @param capabilities the unit's capability flags/limits, as last reported by the API
     */
    private void updateCapabilityProperties(MelCloudHomeAtaCapabilities capabilities) {
        if (capabilitiesPropertiesSet) {
            return;
        }
        updateProperties(Map.of(PROPERTY_ATA_NUMBER_OF_FAN_SPEEDS, String.valueOf(capabilities.numberOfFanSpeeds),
                PROPERTY_ATA_MIN_TEMP_HEAT, String.valueOf(capabilities.minTempHeat), PROPERTY_ATA_MAX_TEMP_HEAT,
                String.valueOf(capabilities.maxTempHeat), PROPERTY_ATA_MIN_TEMP_COOL_DRY,
                String.valueOf(capabilities.minTempCoolDry), PROPERTY_ATA_MAX_TEMP_COOL_DRY,
                String.valueOf(capabilities.maxTempCoolDry), PROPERTY_ATA_HAS_HALF_DEGREE_INCREMENTS,
                String.valueOf(capabilities.hasHalfDegreeIncrements), PROPERTY_ATA_HAS_SWING,
                String.valueOf(capabilities.hasSwing), PROPERTY_ATA_HAS_STANDBY,
                String.valueOf(capabilities.hasStandby), PROPERTY_ATA_HAS_ENERGY_CONSUMED_METER,
                String.valueOf(capabilities.hasEnergyConsumedMeter)));
        capabilitiesPropertiesSet = true;
    }

    /**
     * Maps a Wi-Fi RSSI value in dBm to the 0 (no signal) .. 4 (excellent) scale expected by the
     * {@code system.signal-strength} channel.
     *
     * @param rssi received signal strength indicator, in dBm
     * @return signal quality on a 0-4 scale
     */
    private static int mapRssiToSignalStrength(int rssi) {
        if (rssi >= -60) {
            return 4;
        } else if (rssi >= -70) {
            return 3;
        } else if (rssi >= -80) {
            return 2;
        } else if (rssi >= -90) {
            return 1;
        }
        return 0;
    }

    private void startTelemetryPollIfNeeded() {
        if (telemetryFuture == null) {
            telemetryFuture = scheduler.scheduleWithFixedDelay(this::pollTelemetry, 0, TELEMETRY_POLL_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    private void pollTelemetry() {
        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler == null) {
            return;
        }
        Instant to = Instant.now();
        Instant from = to.minus(1, ChronoUnit.DAYS);
        try {
            String accessToken = handler.getAccessToken();
            handler.getApiClient().fetchLatestEnergyWh(accessToken, config.unitId, from, to, ENERGY_CONSUMED_MEASURE)
                    .ifPresentOrElse(
                            wh -> updateState(CHANNEL_ENERGY_CONSUMED, new QuantityType<>(wh, Units.WATT_HOUR)),
                            () -> updateState(CHANNEL_ENERGY_CONSUMED, UnDefType.UNDEF));
            handler.getApiClient().fetchLatestOutdoorTemperature(accessToken, config.unitId, from, to).ifPresentOrElse(
                    value -> updateState(CHANNEL_OUTDOOR_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)),
                    () -> updateState(CHANNEL_OUTDOOR_TEMPERATURE, UnDefType.UNDEF));
        } catch (MelCloudCommException e) {
            logger.debug("Telemetry poll failed for ATA unit {}, reason {}. ",
                    SensitiveDataMasker.maskId(config.unitId), e.getMessage());
        }
    }

    /**
     * Extracts the integer channel code sent for a Number-typed command (operation mode, vane horizontal/vertical).
     *
     * @param command the received command
     * @return the code, or {@code null} if the command isn't a {@link DecimalType}
     */
    private @Nullable Integer toInt(Command command) {
        if (command instanceof DecimalType decimalCommand) {
            return decimalCommand.intValue();
        }
        logger.debug("Can't convert '{}' to a channel code", command);
        return null;
    }

    private @Nullable Double toCelsius(Command command) {
        if (command instanceof QuantityType<?> quantityCommand) {
            QuantityType<?> celsius = quantityCommand.toUnit(SIUnits.CELSIUS);
            if (celsius == null) {
                logger.debug("Can't convert '{}' to unit Celsius", quantityCommand);
                return null;
            }
            return celsius.doubleValue();
        }
        if (command instanceof DecimalType decimalCommand) {
            return decimalCommand.doubleValue();
        }
        logger.debug("Can't convert '{}' to set temperature", command);
        return null;
    }

    private void cancelTelemetryPoll() {
        ScheduledFuture<?> future = telemetryFuture;
        if (future != null) {
            future.cancel(true);
            telemetryFuture = null;
        }
    }
}
