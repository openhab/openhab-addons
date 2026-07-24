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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
 * The {@link MelCloudHomeAtaUnitHandler} handles a single MELCloud Home Air-to-Air unit (see ADR-003).
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
            case CHANNEL_OPERATION_MODE_STRING:
                request.operationMode = command.toString();
                break;
            case CHANNEL_SET_TEMPERATURE:
                Double temperature = toCelsius(command);
                if (temperature == null) {
                    return;
                }
                request.setTemperature = temperature;
                break;
            case CHANNEL_FAN_SPEED:
                request.setFanSpeed = command.toString();
                break;
            case CHANNEL_VANE_HORIZONTAL:
                request.vaneHorizontalDirection = command.toString();
                break;
            case CHANNEL_VANE_VERTICAL:
                request.vaneVerticalDirection = command.toString();
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
        updateState(CHANNEL_OPERATION_MODE_STRING, new StringType(unit.getOperationMode()));
        unit.getSetTemperature()
                .ifPresent(value -> updateState(CHANNEL_SET_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)));
        unit.getRoomTemperature()
                .ifPresent(value -> updateState(CHANNEL_ROOM_TEMPERATURE, new QuantityType<>(value, SIUnits.CELSIUS)));
        unit.getFanSpeed().ifPresent(value -> updateState(CHANNEL_FAN_SPEED, new StringType(value)));
        unit.getVaneHorizontalDirection()
                .ifPresent(value -> updateState(CHANNEL_VANE_HORIZONTAL, new StringType(value)));
        unit.getVaneVerticalDirection().ifPresent(value -> updateState(CHANNEL_VANE_VERTICAL, new StringType(value)));
        updateState(CHANNEL_IN_STANDBY_MODE, OnOffType.from(unit.isInStandbyMode()));
        updateState(CHANNEL_IS_IN_ERROR, OnOffType.from(unit.isInError()));
        unit.getErrorCode().ifPresent(value -> updateState(CHANNEL_ERROR_CODE, new StringType(value)));
        Integer rssi = unit.rssi;
        if (rssi != null) {
            updateState(CHANNEL_RSSI, new DecimalType(rssi));
        }
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
