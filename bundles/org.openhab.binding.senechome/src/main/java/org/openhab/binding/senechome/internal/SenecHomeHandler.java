/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.senechome.internal.json.SenecHomeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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
    private @NonNullByDefault({}) SenecHomeConfigurationDTO config;
    private @Nullable PowerLimitationStatusDTO limitationStatus = null;
    private  @Nullable SenecHomeApi senecHomeApi;
    private Gson gson;
    private SenecHomeApiFactory apiFactory;

    public SenecHomeHandler(Thing thing, Gson gson, SenecHomeApiFactory apiFactory) {
        super(thing);
        this.gson = gson;
        this.apiFactory = apiFactory;
    }

    @Override
    public void handleRemoval() {
        stopJobIfRunning();
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /* not implemented - binding only supports reading */
    }

    @Override
    public void dispose() {
        stopJobIfRunning();
        super.dispose();
    }

    protected void stopJobIfRunning() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SenecHomeConfigurationDTO.class);
        senecHomeApi = apiFactory.getHttpApi(config, gson);
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void refresh() {
        try {
            SenecHomeResponse response = senecHomeApi.getStatistics();
            logger.trace("received {}", response);

            BigDecimal pvLimitation = new BigDecimal(100).subtract(getSenecValue(response.limitation.powerLimitation))
                    .setScale(0, RoundingMode.HALF_UP);

            Channel channelLimitation = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_POWER_LIMITATION);
            if (channelLimitation != null) {
                updateState(channelLimitation.getUID(),
                        new QuantityType<Dimensionless>(pvLimitation, SmartHomeUnits.PERCENT));
            }

            Channel channelLimitationState = getThing()
                    .getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_POWER_LIMITATION_STATE);
            if (channelLimitationState != null) {
                updatePowerLimitationStatus(channelLimitationState,
                        (100 - pvLimitation.intValue()) <= config.limitationTresholdValue, config.limitationDuration);
            }

            Channel channelConsumption = getThing()
                    .getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_POWER_CONSUMPTION);
            if (channelConsumption != null) {
                updateState(channelConsumption.getUID(),
                        new QuantityType<Power>(
                                getSenecValue(response.energy.homePowerConsumption).setScale(2, RoundingMode.HALF_UP),
                                SmartHomeUnits.WATT));
            }

            Channel channelEnergyProduction = getThing()
                    .getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_ENERGY_PRODUCTION);
            if (channelEnergyProduction != null) {
                updateState(channelEnergyProduction.getUID(), new QuantityType<Power>(
                        getSenecValue(response.energy.inverterPowerGeneration).setScale(0, RoundingMode.HALF_UP),
                        SmartHomeUnits.WATT));
            }

            Channel channelBatteryPower = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_BATTERY_POWER);
            if (channelBatteryPower != null) {
                updateState(channelBatteryPower.getUID(),
                        new QuantityType<Power>(
                                getSenecValue(response.energy.batteryPower).setScale(2, RoundingMode.HALF_UP),
                                SmartHomeUnits.WATT));
            }

            Channel channelBatteryFuelCharge = getThing()
                    .getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_BATTERY_FUEL_CHARGE);
            if (channelBatteryFuelCharge != null) {
                updateState(channelBatteryFuelCharge.getUID(),
                        new QuantityType<Dimensionless>(
                                getSenecValue(response.energy.batteryFuelCharge).setScale(0, RoundingMode.HALF_UP),
                                SmartHomeUnits.PERCENT));
            }

            Channel channelBatteryState = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_BATTERY_STATE);
            if (channelBatteryState != null) {
                updateBatteryState(channelBatteryState, getSenecValue(response.energy.batteryState).intValue());
            }

            updateGridPowerValues(getThing(), getSenecValue(response.grid.currentGridValue));

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error refreshing source '{}'", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to Senec web interface:" + e.getMessage());
        }
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

        String binaryString = Long.toString(Long.parseLong(value, 16), 2);
        while (binaryString.length() < 32) {
            binaryString = "0" + binaryString;
        }

        int sign = Character.getNumericValue(binaryString.charAt(0));
        if (sign == 0) {
            sign = -1;
        }
        sign = sign * -1;

        if (Long.parseLong(binaryString.substring(1), 2) == 0L) {
            return BigDecimal.ZERO;
        } else {
            int exponent = Integer.parseInt(binaryString.substring(1, 9), 2) - 127;

            String mantissa = binaryString.substring(9);

            double significand = 1;
            for (int i = 1; i < 24; i++) {
                significand += (1 / Math.pow(2, i)) * Character.getNumericValue(mantissa.charAt(i - 1));
            }

            return new BigDecimal(significand * Math.pow(2, exponent) * sign);
        }
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

    protected void updateBatteryState(Channel channel, int code) {
        updateState(channel.getUID(), new StringType(SenecBatteryStatus.fromCode(code).name()));
    }

    protected void updateGridPowerValues(Thing thing, BigDecimal gridTotalValue) {
        BigDecimal gridTotal = gridTotalValue.setScale(2, RoundingMode.HALF_UP);

        Channel channelGridPower = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER);
        if (channelGridPower != null) {
            updateState(channelGridPower.getUID(), new QuantityType<Power>(gridTotal, SmartHomeUnits.WATT));
        }

        Channel channelGridPowerSupply = getThing()
                .getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER_SUPPLY);
        if (channelGridPowerSupply != null) {
            BigDecimal gridSupply = gridTotal.compareTo(BigDecimal.ZERO) < 0 ? gridTotal.abs() : BigDecimal.ZERO;
            updateState(channelGridPowerSupply.getUID(), new QuantityType<Power>(gridSupply, SmartHomeUnits.WATT));
        }

        Channel channelGridPowerDraw = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER_DRAW);
        if (channelGridPowerDraw != null) {
            BigDecimal gridDraw = gridTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : gridTotal.abs();
            updateState(channelGridPowerDraw.getUID(), new QuantityType<Power>(gridDraw, SmartHomeUnits.WATT));
        }
    }
}
