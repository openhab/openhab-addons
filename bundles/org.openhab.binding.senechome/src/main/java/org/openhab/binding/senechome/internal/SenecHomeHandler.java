/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.senechome.internal;

import static org.openhab.core.types.RefreshType.REFRESH;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.senechome.internal.json.SenecHomeResponse;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SenecHomeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Steven Schwarznau - Initial contribution
 */
@NonNullByDefault
public class SenecHomeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SenecHomeHandler.class);
    private final static String VALUE_TYPE_INT = "u3";
    private final static String VALUE_TYPE_UNSIGNED_INT = "u8";
    private final static String VALUE_TYPE_FLOAT = "fl";

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable PowerLimitationStatusDTO limitationStatus = null;
    private final @Nullable SenecHomeApi senecHomeApi;
    private SenecHomeConfigurationDTO config = new SenecHomeConfigurationDTO();
    private final ExpiringCache<Boolean> refreshCache = new ExpiringCache<>(Duration.ofSeconds(5), this::refreshState);

    public SenecHomeHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.senecHomeApi = new SenecHomeApi(httpClient);
    }

    @Override
    public void handleRemoval() {
        stopJobIfRunning();
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            refresh();
        } else {
            logger.trace("The SenecHome-Binding is a read-only binding and can not handle commands");
        }
    }

    @Override
    public void dispose() {
        stopJobIfRunning();
        super.dispose();
    }

    protected void stopJobIfRunning() {
        final ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SenecHomeConfigurationDTO.class);
        senecHomeApi.setHostname(config.hostname);
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refreshInterval, TimeUnit.SECONDS);
        limitationStatus = null;
    }

    private void refresh() {
        refreshCache.getValue();
    }

    private void updateChannelState(String channelConstant, State channelState) {
        Channel channel = getThing().getChannel(channelConstant);
        if (channel != null) {
            updateState(channel.getUID(), channelState);
        }
    }

    public @Nullable Boolean refreshState() {
        try {
            SenecHomeResponse response = senecHomeApi.getStatistics();
            logger.trace("received {}", response);

            BigDecimal pvLimitation = new BigDecimal(100).subtract(getSenecValue(response.limitation.powerLimitation))
                    .setScale(0, RoundingMode.HALF_UP);

            updateState(SenecHomeBindingConstants.CHANNEL_SENEC_POWER_LIMITATION,
                    new QuantityType<Dimensionless>(pvLimitation, Units.PERCENT));

            Channel channelLimitationState = getThing()
                    .getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_POWER_LIMITATION_STATE);
            if (channelLimitationState != null) {
                updatePowerLimitationStatus(channelLimitationState,
                        (100 - pvLimitation.intValue()) <= config.limitationTresholdValue, config.limitationDuration);
            }

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_POWER_CONSUMPTION, new QuantityType<Power>(
                    getSenecValue(response.energy.homePowerConsumption).setScale(2, RoundingMode.HALF_UP), Units.WATT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_ENERGY_PRODUCTION,
                    new QuantityType<Power>(
                            getSenecValue(response.energy.inverterPowerGeneration).setScale(0, RoundingMode.HALF_UP),
                            Units.WATT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_BATTERY_POWER, new QuantityType<Power>(
                    getSenecValue(response.energy.batteryPower).setScale(2, RoundingMode.HALF_UP), Units.WATT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_BATTERY_FUEL_CHARGE,
                    new QuantityType<Dimensionless>(
                            getSenecValue(response.energy.batteryFuelCharge).setScale(0, RoundingMode.HALF_UP),
                            Units.PERCENT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_CURRENT_PH1,
                    new QuantityType<ElectricCurrent>(getSenecValue(response.grid.currentGridCurrentPerPhase[0])
                            .setScale(2, RoundingMode.HALF_UP), Units.AMPERE));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_CURRENT_PH2,
                    new QuantityType<ElectricCurrent>(getSenecValue(response.grid.currentGridCurrentPerPhase[1])
                            .setScale(2, RoundingMode.HALF_UP), Units.AMPERE));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_CURRENT_PH3,
                    new QuantityType<ElectricCurrent>(getSenecValue(response.grid.currentGridCurrentPerPhase[2])
                            .setScale(2, RoundingMode.HALF_UP), Units.AMPERE));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER_PH1,
                    new QuantityType<Power>(
                            getSenecValue(response.grid.currentGridPowerPerPhase[0]).setScale(2, RoundingMode.HALF_UP),
                            Units.WATT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER_PH2,
                    new QuantityType<Power>(
                            getSenecValue(response.grid.currentGridPowerPerPhase[1]).setScale(2, RoundingMode.HALF_UP),
                            Units.WATT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER_PH3,
                    new QuantityType<Power>(
                            getSenecValue(response.grid.currentGridPowerPerPhase[2]).setScale(2, RoundingMode.HALF_UP),
                            Units.WATT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_VOLTAGE_PH1,
                    new QuantityType<ElectricPotential>(getSenecValue(response.grid.currentGridVoltagePerPhase[0])
                            .setScale(2, RoundingMode.HALF_UP), Units.VOLT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_VOLTAGE_PH2,
                    new QuantityType<ElectricPotential>(getSenecValue(response.grid.currentGridVoltagePerPhase[1])
                            .setScale(2, RoundingMode.HALF_UP), Units.VOLT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_VOLTAGE_PH3,
                    new QuantityType<ElectricPotential>(getSenecValue(response.grid.currentGridVoltagePerPhase[2])
                            .setScale(2, RoundingMode.HALF_UP), Units.VOLT));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_FREQUENCY, new QuantityType<Frequency>(
                    getSenecValue(response.grid.currentGridFrequency).setScale(2, RoundingMode.HALF_UP), Units.HERTZ));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_SYSTEM_STATE_VALUE,
                    new DecimalType(getSenecValue(response.energy.systemState).intValue()));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_LIVE_BAT_CHARGE,
                    new QuantityType<Energy>(
                            getSenecValue(response.statistics.liveBatCharge).setScale(2, RoundingMode.HALF_UP),
                            Units.WATT_HOUR));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_LIVE_BAT_DISCHARGE,
                    new QuantityType<Energy>(
                            getSenecValue(response.statistics.liveBatDischarge).setScale(2, RoundingMode.HALF_UP),
                            Units.WATT_HOUR));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_LIVE_GRID_IMPORT,
                    new QuantityType<Energy>(
                            getSenecValue(response.statistics.liveGridImport).setScale(2, RoundingMode.HALF_UP),
                            Units.WATT_HOUR));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_LIVE_GRID_EXPORT,
                    new QuantityType<Energy>(
                            getSenecValue(response.statistics.liveGridExport).setScale(2, RoundingMode.HALF_UP),
                            Units.WATT_HOUR));

            updateChannelState(SenecHomeBindingConstants.CHANNEL_SENEC_BATTERY_VOLTAGE,
                    new QuantityType<ElectricPotential>(
                            getSenecValue(response.energy.batteryVoltage).setScale(2, RoundingMode.HALF_UP),
                            Units.VOLT));

            Channel channelSystemState = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_SYSTEM_STATE);
            if (channelSystemState != null) {
                updateSystemState(channelSystemState, getSenecValue(response.energy.systemState).intValue());
            }

            updateGridPowerValues(getSenecValue(response.grid.currentGridValue));

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error refreshing source '{}'", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to Senec web interface:" + e.getMessage());
        }

        return Boolean.TRUE;
    }

    protected BigDecimal getSenecValue(String value) {
        String[] type = value.split("_");

        if (VALUE_TYPE_INT.equalsIgnoreCase(type[0])) {
            return new BigDecimal(Integer.valueOf(type[1], 16));
        } else if (VALUE_TYPE_UNSIGNED_INT.equalsIgnoreCase(type[0])) {
            return new BigDecimal(Integer.valueOf(type[1], 16));
        } else if (VALUE_TYPE_FLOAT.equalsIgnoreCase(type[0])) {
            return parseFloatValue(type[1]);
        } else {
            logger.warn("Unknown value type [{}]", type[0]);
        }

        return BigDecimal.ZERO;
    }

    /**
     * Parse the hex coded float value of Senec device and return as BigDecimal
     *
     * @param value String with hex float
     * @return BigDecimal with float value
     */
    private static BigDecimal parseFloatValue(String value) {
        // sample: value = 43E26188

        float f = Float.intBitsToFloat(Integer.parseUnsignedInt(value, 16));
        return new BigDecimal(f);
    }

    protected void updatePowerLimitationStatus(Channel channel, boolean status, int duration) {
        if (this.limitationStatus != null) {
            if (this.limitationStatus.state == status) {
                long stateSince = new Date().getTime() - this.limitationStatus.time;

                if (((int) (stateSince / 1000)) < duration) {
                    // skip updating state (possible flapping state)
                    return;
                } else {
                    logger.debug("{} longer than required duration {}", status, duration);
                }
            } else {
                this.limitationStatus.state = status;
                this.limitationStatus.time = new Date().getTime();

                // skip updating state (state changed, possible flapping state)
                return;
            }
        } else {
            this.limitationStatus = new PowerLimitationStatusDTO();
            this.limitationStatus.state = status;
        }

        logger.debug("Updating power limitation state {}", status);
        updateState(channel.getUID(), status ? OnOffType.ON : OnOffType.OFF);
    }

    protected void updateSystemState(Channel channel, int code) {
        updateState(channel.getUID(), new StringType(SenecBatteryStatus.descriptionFromCode(code)));
    }

    protected void updateGridPowerValues(BigDecimal gridTotalValue) {
        BigDecimal gridTotal = gridTotalValue.setScale(2, RoundingMode.HALF_UP);

        Channel channelGridPower = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER);
        if (channelGridPower != null) {
            updateState(channelGridPower.getUID(), new QuantityType<Power>(gridTotal, Units.WATT));
        }

        Channel channelGridPowerSupply = getThing()
                .getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER_SUPPLY);
        if (channelGridPowerSupply != null) {
            BigDecimal gridSupply = gridTotal.compareTo(BigDecimal.ZERO) < 0 ? gridTotal.abs() : BigDecimal.ZERO;
            updateState(channelGridPowerSupply.getUID(), new QuantityType<Power>(gridSupply, Units.WATT));
        }

        Channel channelGridPowerDraw = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER_DRAW);
        if (channelGridPowerDraw != null) {
            BigDecimal gridDraw = gridTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : gridTotal.abs();
            updateState(channelGridPowerDraw.getUID(), new QuantityType<Power>(gridDraw, Units.WATT));
        }
    }
}
