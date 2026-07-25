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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaUnit;
import org.openhab.binding.melcloud.internal.home.config.MelCloudHomeUnitConfig;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MelCloudHomeAtaUnitHandler} handles a single MELCloud Home Air-to-Air unit.
 *
 * <p>
 * Its main state (power, mode, temperatures, fan/vane, error/standby state, rssi) comes from the bridge's
 * centralized {@code /context} poll via {@link MelCloudHomeAtaUnitListener#onAtaUnitUpdated(MelCloudHomeAtaUnit)}.
 * Energy and outdoor-temperature telemetry are polled directly by this handler at a much longer interval, since
 * those endpoints are inherently per-unit.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtaUnitHandler extends BaseThingHandler implements MelCloudHomeAtaUnitListener {

    private static final long TELEMETRY_POLL_INTERVAL_SECONDS = 1800;
    private static final String ENERGY_CONSUMED_MEASURE = "cumulative_energy_consumed_since_last_upload";

    /**
     * Maps the MELCloud Home API's word-based operation mode onto the same numeric codes the legacy binding's
     * {@code operationMode-channel} already uses for the A.C. Device (1 = Heat, 2 = Dry, 3 = Cool, 7 = Fan,
     * 8 = Auto/Automatic), so both Thing families expose the same Number channel contract.
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
            logger.debug("Refresh command not supported");
            return;
        }

        MelCloudHomeAccountHandler handler = bridgeHandler;
        if (handler == null) {
            logger.warn("No connection to MELCloud Home available, ignoring command");
            return;
        }

        MelCloudHomeAtaControlRequest request = new MelCloudHomeAtaControlRequest();
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                request.power = command == OnOffType.ON;
                break;
            case CHANNEL_HOME_OPERATION_MODE:
                Integer operationModeCode = toInt(command);
                String operationModeWord = operationModeCode == null ? null
                        : OPERATION_MODE_CODE_TO_WORD.get(operationModeCode);
                if (operationModeWord == null) {
                    logger.debug("Unknown operation mode code '{}', ignoring command", command);
                    return;
                }
                request.operationMode = operationModeWord;
                break;
            case CHANNEL_HOME_SET_TEMPERATURE:
                Double temperature = toCelsius(command);
                if (temperature == null) {
                    return;
                }
                request.setTemperature = temperature;
                break;
            case CHANNEL_HOME_FAN_SPEED:
                Integer fanSpeedCode = toInt(command);
                String fanSpeedWord = fanSpeedCode == null ? null : FAN_SPEED_CODE_TO_WORD.get(fanSpeedCode);
                if (fanSpeedWord == null) {
                    logger.debug("Unknown fan speed code '{}', ignoring command", command);
                    return;
                }
                request.setFanSpeed = fanSpeedWord;
                break;
            case CHANNEL_HOME_VANE_HORIZONTAL:
                Integer vaneHorizontalCode = toInt(command);
                String vaneHorizontalWord = vaneHorizontalCode == null ? null
                        : VANE_HORIZONTAL_CODE_TO_WORD.get(vaneHorizontalCode);
                if (vaneHorizontalWord == null) {
                    logger.debug("Unknown vane horizontal code '{}', ignoring command", command);
                    return;
                }
                request.vaneHorizontalDirection = vaneHorizontalWord;
                break;
            case CHANNEL_HOME_VANE_VERTICAL:
                Integer vaneVerticalCode = toInt(command);
                String vaneVerticalWord = vaneVerticalCode == null ? null
                        : VANE_VERTICAL_CODE_TO_WORD.get(vaneVerticalCode);
                if (vaneVerticalWord == null) {
                    logger.debug("Unknown vane vertical code '{}', ignoring command", command);
                    return;
                }
                request.vaneVerticalDirection = vaneVerticalWord;
                break;
            default:
                logger.debug("Read-only or unknown channel {}, skipping command", channelUID);
                return;
        }

        try {
            handler.getApiClient().controlAtaUnit(handler.getAccessToken(), config.unitId, request);
        } catch (MelCloudCommException e) {
            logger.warn("Command '{}' to channel '{}' failed, reason {}. ", command, channelUID, e.getMessage());
        }
    }

    @Override
    public void onAtaUnitUpdated(MelCloudHomeAtaUnit unit) {
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_POWER, OnOffType.from(unit.isPower()));
        Integer operationModeCode = OPERATION_MODE_WORD_TO_CODE.get(unit.getOperationMode());
        if (operationModeCode != null) {
            updateState(CHANNEL_HOME_OPERATION_MODE, new DecimalType(operationModeCode));
        } else {
            logger.debug("Unknown operation mode word '{}', skipping channel update", unit.getOperationMode());
        }
        unit.getSetTemperature().ifPresent(
                value -> updateState(CHANNEL_HOME_SET_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)));
        unit.getRoomTemperature().ifPresent(
                value -> updateState(CHANNEL_HOME_ROOM_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)));
        unit.getFanSpeed().ifPresent(value -> {
            Integer code = FAN_SPEED_WORD_TO_CODE.get(value);
            if (code != null) {
                updateState(CHANNEL_HOME_FAN_SPEED, new DecimalType(code));
            } else {
                logger.debug("Unknown fan speed word '{}', skipping channel update", value);
            }
        });
        unit.getVaneHorizontalDirection().ifPresent(value -> {
            Integer code = VANE_HORIZONTAL_WORD_TO_CODE.get(value);
            if (code != null) {
                updateState(CHANNEL_HOME_VANE_HORIZONTAL, new DecimalType(code));
            } else {
                logger.debug("Unknown vane horizontal word '{}', skipping channel update", value);
            }
        });
        unit.getVaneVerticalDirection().ifPresent(value -> {
            Integer code = VANE_VERTICAL_WORD_TO_CODE.get(value);
            if (code != null) {
                updateState(CHANNEL_HOME_VANE_VERTICAL, new DecimalType(code));
            } else {
                logger.debug("Unknown vane vertical word '{}', skipping channel update", value);
            }
        });
        updateState(CHANNEL_IN_STANDBY_MODE, OnOffType.from(unit.isInStandbyMode()));
        updateState(CHANNEL_IS_IN_ERROR, OnOffType.from(unit.isInError()));
        unit.getErrorCode().ifPresent(value -> updateState(CHANNEL_ERROR_CODE, new StringType(value)));
        Integer rssi = unit.rssi;
        if (rssi != null) {
            updateState(CHANNEL_RSSI, new DecimalType(mapRssiToSignalStrength(rssi)));
        }
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
                    .ifPresent(wh -> updateState(CHANNEL_ENERGY_CONSUMED, new QuantityType<>(wh, Units.WATT_HOUR)));
            handler.getApiClient().fetchLatestOutdoorTemperature(accessToken, config.unitId, from, to).ifPresent(
                    value -> updateState(CHANNEL_OUTDOOR_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)));
        } catch (MelCloudCommException e) {
            logger.debug("Telemetry poll failed for ATA unit {}, reason {}. ", config.unitId, e.getMessage());
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
