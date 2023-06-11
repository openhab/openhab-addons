/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.senechome.internal.SenecHomeBindingConstants.*;
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
import java.util.function.Function;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.senechome.internal.json.SenecHomeResponse;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link SenecHomeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Steven Schwarznau - Initial contribution
 * @author Erwin Guib - added more channels, added some convenience methods to reduce code duplication
 */
@NonNullByDefault
public class SenecHomeHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SenecHomeHandler.class);

    // divisor to transform from milli to kilo UNIT (e.g. mW => kW)
    private static final BigDecimal DIVISOR_MILLI_TO_KILO = BigDecimal.valueOf(1000000);
    // divisor to transform from milli to "iso" UNIT (e.g. mV => V)
    private static final BigDecimal DIVISOR_MILLI_TO_ISO = BigDecimal.valueOf(1000);
    // divisor to transform from "iso" to kilo UNIT (e.g. W => kW)
    private static final BigDecimal DIVISOR_ISO_TO_KILO = BigDecimal.valueOf(1000);
    // ix (x=1,3,8) types => hex encoded integer value
    private static final String VALUE_TYPE_INT1 = "i1";
    public static final String VALUE_TYPE_INT3 = "i3";
    public static final String VALUE_TYPE_INT8 = "i8";
    // ux (x=1,3,6,8) types => hex encoded unsigned value
    private static final String VALUE_TYPE_DECIMAL = "u";
    // fl => hex encoded float
    private static final String VALUE_TYPE_FLOAT = "fl";
    // st => string
    // public static final String VALUE_TYPE_STRING = "st";

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

    public @Nullable Boolean refreshState() {
        SenecHomeResponse response = null;
        try {
            response = senecHomeApi.getStatistics();
            logger.trace("received {}", response);

            BigDecimal pvLimitation = new BigDecimal(100).subtract(getSenecValue(response.power.powerLimitation))
                    .setScale(0, RoundingMode.HALF_UP);
            updateState(CHANNEL_SENEC_POWER_LIMITATION, new QuantityType<>(pvLimitation, Units.PERCENT));

            Channel channelLimitationState = getThing().getChannel(CHANNEL_SENEC_POWER_LIMITATION_STATE);
            if (channelLimitationState != null) {
                updatePowerLimitationStatus(channelLimitationState,
                        (100 - pvLimitation.intValue()) <= config.limitationTresholdValue, config.limitationDuration);
            }
            if (response.power.currentPerMpp != null) {
                updateQtyState(CHANNEL_SENEC_CURRENT_MPP1, response.power.currentPerMpp[0], 2, Units.AMPERE);
                updateQtyState(CHANNEL_SENEC_CURRENT_MPP2, response.power.currentPerMpp[1], 2, Units.AMPERE);
                if (response.power.currentPerMpp.length > 2) {
                    // only Home V3 duo
                    updateQtyState(CHANNEL_SENEC_CURRENT_MPP3, response.power.currentPerMpp[2], 2, Units.AMPERE);
                }
            }
            if (response.power.powerPerMpp != null) {
                updateQtyState(CHANNEL_SENEC_POWER_MPP1, response.power.powerPerMpp[0], 2, Units.WATT);
                updateQtyState(CHANNEL_SENEC_POWER_MPP2, response.power.powerPerMpp[1], 2, Units.WATT);
                if (response.power.powerPerMpp.length > 2) {
                    updateQtyState(CHANNEL_SENEC_POWER_MPP3, response.power.powerPerMpp[2], 2, Units.WATT);
                }
            }
            if (response.power.voltagePerMpp != null) {
                updateQtyState(CHANNEL_SENEC_VOLTAGE_MPP1, response.power.voltagePerMpp[0], 2, Units.VOLT);
                updateQtyState(CHANNEL_SENEC_VOLTAGE_MPP2, response.power.voltagePerMpp[1], 2, Units.VOLT);
                if (response.power.voltagePerMpp.length > 2) {
                    updateQtyState(CHANNEL_SENEC_VOLTAGE_MPP3, response.power.voltagePerMpp[2], 2, Units.VOLT);
                }
            }

            updateQtyState(CHANNEL_SENEC_POWER_CONSUMPTION, response.energy.housePowerConsumption, 2, Units.WATT);
            updateQtyState(CHANNEL_SENEC_ENERGY_PRODUCTION, response.energy.inverterPowerGeneration, 2, Units.WATT);
            updateQtyState(CHANNEL_SENEC_BATTERY_POWER, response.energy.batteryPower, 2, Units.WATT);
            updateQtyState(CHANNEL_SENEC_BATTERY_CURRENT, response.energy.batteryCurrent, 2, Units.AMPERE);
            updateQtyState(CHANNEL_SENEC_BATTERY_VOLTAGE, response.energy.batteryVoltage, 2, Units.VOLT);
            updateStringStateFromInt(CHANNEL_SENEC_SYSTEM_STATE, response.energy.systemState,
                    SenecSystemStatus::descriptionFromCode);
            updateDecimalState(CHANNEL_SENEC_SYSTEM_STATE_VALUE, response.energy.systemState);
            updateQtyState(CHANNEL_SENEC_BATTERY_FUEL_CHARGE, response.energy.batteryFuelCharge, 0, Units.PERCENT);

            updateGridPowerValues(getSenecValue(response.grid.currentGridValue));
            updateQtyState(CHANNEL_SENEC_GRID_CURRENT_PH1, response.grid.currentGridCurrentPerPhase[0], 2,
                    Units.AMPERE);
            updateQtyState(CHANNEL_SENEC_GRID_CURRENT_PH2, response.grid.currentGridCurrentPerPhase[1], 2,
                    Units.AMPERE);
            updateQtyState(CHANNEL_SENEC_GRID_CURRENT_PH3, response.grid.currentGridCurrentPerPhase[2], 2,
                    Units.AMPERE);
            updateQtyState(CHANNEL_SENEC_GRID_POWER_PH1, response.grid.currentGridPowerPerPhase[0], 2, Units.WATT);
            updateQtyState(CHANNEL_SENEC_GRID_POWER_PH2, response.grid.currentGridPowerPerPhase[1], 2, Units.WATT);
            updateQtyState(CHANNEL_SENEC_GRID_POWER_PH3, response.grid.currentGridPowerPerPhase[2], 2, Units.WATT);

            updateQtyState(CHANNEL_SENEC_GRID_VOLTAGE_PH1, response.grid.currentGridVoltagePerPhase[0], 2, Units.VOLT);
            updateQtyState(CHANNEL_SENEC_GRID_VOLTAGE_PH2, response.grid.currentGridVoltagePerPhase[1], 2, Units.VOLT);
            updateQtyState(CHANNEL_SENEC_GRID_VOLTAGE_PH3, response.grid.currentGridVoltagePerPhase[2], 2, Units.VOLT);
            updateQtyState(CHANNEL_SENEC_GRID_FREQUENCY, response.grid.currentGridFrequency, 2, Units.HERTZ);

            updateQtyState(CHANNEL_SENEC_LIVE_BAT_CHARGE, response.statistics.liveBatCharge, 2, Units.KILOWATT_HOUR);
            updateQtyState(CHANNEL_SENEC_LIVE_BAT_DISCHARGE, response.statistics.liveBatDischarge, 2,
                    Units.KILOWATT_HOUR);
            updateQtyState(CHANNEL_SENEC_LIVE_GRID_IMPORT, response.statistics.liveGridImport, 2, Units.KILOWATT_HOUR);
            updateQtyState(CHANNEL_SENEC_LIVE_GRID_EXPORT, response.statistics.liveGridExport, 2, Units.KILOWATT_HOUR);
            updateQtyState(CHANNEL_SENEC_LIVE_HOUSE_CONSUMPTION, response.statistics.liveHouseConsumption, 2,
                    Units.KILOWATT_HOUR);
            updateQtyState(CHANNEL_SENEC_LIVE_POWER_GENERATOR, response.statistics.livePowerGenerator, 2,
                    Units.KILOWATT_HOUR);
            if (response.statistics.liveWallboxEnergy != null) {
                updateQtyState(CHANNEL_SENEC_LIVE_ENERGY_WALLBOX1, response.statistics.liveWallboxEnergy[0], 2,
                        Units.KILOWATT_HOUR, DIVISOR_ISO_TO_KILO);
            }

            if (response.battery.chargedEnergy != null) {
                updateQtyState(CHANNEL_SENEC_CHARGED_ENERGY_PACK1, response.battery.chargedEnergy[0], 2,
                        Units.KILOWATT_HOUR, DIVISOR_MILLI_TO_KILO);
                updateQtyState(CHANNEL_SENEC_CHARGED_ENERGY_PACK2, response.battery.chargedEnergy[1], 2,
                        Units.KILOWATT_HOUR, DIVISOR_MILLI_TO_KILO);
                updateQtyState(CHANNEL_SENEC_CHARGED_ENERGY_PACK3, response.battery.chargedEnergy[2], 2,
                        Units.KILOWATT_HOUR, DIVISOR_MILLI_TO_KILO);
                updateQtyState(CHANNEL_SENEC_CHARGED_ENERGY_PACK4, response.battery.chargedEnergy[3], 2,
                        Units.KILOWATT_HOUR, DIVISOR_MILLI_TO_KILO);
            }
            if (response.battery.dischargedEnergy != null) {
                updateQtyState(CHANNEL_SENEC_DISCHARGED_ENERGY_PACK1, response.battery.dischargedEnergy[0], 2,
                        Units.KILOWATT_HOUR, DIVISOR_MILLI_TO_KILO);
                updateQtyState(CHANNEL_SENEC_DISCHARGED_ENERGY_PACK2, response.battery.dischargedEnergy[1], 2,
                        Units.KILOWATT_HOUR, DIVISOR_MILLI_TO_KILO);
                updateQtyState(CHANNEL_SENEC_DISCHARGED_ENERGY_PACK3, response.battery.dischargedEnergy[2], 2,
                        Units.KILOWATT_HOUR, DIVISOR_MILLI_TO_KILO);
                updateQtyState(CHANNEL_SENEC_DISCHARGED_ENERGY_PACK4, response.battery.dischargedEnergy[3], 2,
                        Units.KILOWATT_HOUR, DIVISOR_MILLI_TO_KILO);
            }
            if (response.battery.cycles != null) {
                updateDecimalState(CHANNEL_SENEC_CYCLES_PACK1, response.battery.cycles[0]);
                updateDecimalState(CHANNEL_SENEC_CYCLES_PACK2, response.battery.cycles[1]);
                updateDecimalState(CHANNEL_SENEC_CYCLES_PACK3, response.battery.cycles[2]);
                updateDecimalState(CHANNEL_SENEC_CYCLES_PACK4, response.battery.cycles[3]);
            }
            if (response.battery.current != null) {
                updateQtyState(CHANNEL_SENEC_CURRENT_PACK1, response.battery.current[0], 2, Units.AMPERE);
                updateQtyState(CHANNEL_SENEC_CURRENT_PACK2, response.battery.current[1], 2, Units.AMPERE);
                updateQtyState(CHANNEL_SENEC_CURRENT_PACK3, response.battery.current[2], 2, Units.AMPERE);
                updateQtyState(CHANNEL_SENEC_CURRENT_PACK4, response.battery.current[3], 2, Units.AMPERE);
            }
            if (response.battery.voltage != null) {
                updateQtyState(CHANNEL_SENEC_VOLTAGE_PACK1, response.battery.voltage[0], 2, Units.VOLT);
                updateQtyState(CHANNEL_SENEC_VOLTAGE_PACK2, response.battery.voltage[1], 2, Units.VOLT);
                updateQtyState(CHANNEL_SENEC_VOLTAGE_PACK3, response.battery.voltage[2], 2, Units.VOLT);
                updateQtyState(CHANNEL_SENEC_VOLTAGE_PACK4, response.battery.voltage[3], 2, Units.VOLT);
            }
            if (response.battery.maxCellVoltage != null) {
                updateQtyState(CHANNEL_SENEC_MAX_CELL_VOLTAGE_PACK1, response.battery.maxCellVoltage[0], 3, Units.VOLT,
                        DIVISOR_MILLI_TO_ISO);
                updateQtyState(CHANNEL_SENEC_MAX_CELL_VOLTAGE_PACK2, response.battery.maxCellVoltage[1], 3, Units.VOLT,
                        DIVISOR_MILLI_TO_ISO);
                updateQtyState(CHANNEL_SENEC_MAX_CELL_VOLTAGE_PACK3, response.battery.maxCellVoltage[2], 3, Units.VOLT,
                        DIVISOR_MILLI_TO_ISO);
                updateQtyState(CHANNEL_SENEC_MAX_CELL_VOLTAGE_PACK4, response.battery.maxCellVoltage[3], 3, Units.VOLT,
                        DIVISOR_MILLI_TO_ISO);
            }
            if (response.battery.minCellVoltage != null) {
                updateQtyState(CHANNEL_SENEC_MIN_CELL_VOLTAGE_PACK1, response.battery.minCellVoltage[0], 3, Units.VOLT,
                        DIVISOR_MILLI_TO_ISO);
                updateQtyState(CHANNEL_SENEC_MIN_CELL_VOLTAGE_PACK2, response.battery.minCellVoltage[1], 3, Units.VOLT,
                        DIVISOR_MILLI_TO_ISO);
                updateQtyState(CHANNEL_SENEC_MIN_CELL_VOLTAGE_PACK3, response.battery.minCellVoltage[2], 3, Units.VOLT,
                        DIVISOR_MILLI_TO_ISO);
                updateQtyState(CHANNEL_SENEC_MIN_CELL_VOLTAGE_PACK4, response.battery.minCellVoltage[3], 3, Units.VOLT,
                        DIVISOR_MILLI_TO_ISO);
            }

            if (response.temperature != null) {
                updateQtyState(CHANNEL_SENEC_BATTERY_TEMPERATURE, response.temperature.batteryTemperature, 0,
                        SIUnits.CELSIUS);
                updateQtyState(CHANNEL_SENEC_CASE_TEMPERATURE, response.temperature.caseTemperature, 0,
                        SIUnits.CELSIUS);
                updateQtyState(CHANNEL_SENEC_MCU_TEMPERATURE, response.temperature.mcuTemperature, 0, SIUnits.CELSIUS);
            }

            if (response.wallbox != null && response.wallbox.state != null) {
                updateStringStateFromInt(CHANNEL_SENEC_WALLBOX1_STATE, response.wallbox.state[0],
                        SenecWallboxStatus::descriptionFromCode);
                updateDecimalState(CHANNEL_SENEC_WALLBOX1_STATE_VALUE, response.wallbox.state[0]);
                updateQtyState(CHANNEL_SENEC_WALLBOX1_CHARGING_CURRENT_PH1, response.wallbox.l1ChargingCurrent[0], 2,
                        Units.AMPERE);
                updateQtyState(CHANNEL_SENEC_WALLBOX1_CHARGING_CURRENT_PH2, response.wallbox.l2ChargingCurrent[0], 2,
                        Units.AMPERE);
                updateQtyState(CHANNEL_SENEC_WALLBOX1_CHARGING_CURRENT_PH3, response.wallbox.l3ChargingCurrent[0], 2,
                        Units.AMPERE);
                updateQtyState(CHANNEL_SENEC_WALLBOX1_CHARGING_POWER, response.wallbox.chargingPower[0], 2, Units.WATT);
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (JsonParseException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            if (response == null) {
                logger.trace("Faulty response: is null");
            } else {
                logger.trace("Faulty response: {}", response.toString());
            }
            logger.warn("Error refreshing source '{}'", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to Senec web interface:" + e.getMessage());
        }

        return Boolean.TRUE;
    }

    protected void updateStringStateFromInt(String channelName, String senecValue,
            Function<Integer, String> converter) {
        Channel channel = getThing().getChannel(channelName);
        if (channel != null) {
            Integer value = getSenecValue(senecValue).intValue();
            updateState(channel.getUID(), new StringType(converter.apply(value)));
        }
    }

    protected void updateDecimalState(String channelName, String senecValue) {
        Channel channel = getThing().getChannel(channelName);
        if (channel != null) {
            BigDecimal value = getSenecValue(senecValue);
            updateState(channel.getUID(), new DecimalType(value.intValue()));
        }
    }

    protected <Q extends Quantity<Q>> void updateQtyState(String channelName, String senecValue, int scale,
            Unit<Q> unit) {
        updateQtyState(channelName, senecValue, scale, unit, null);
    }

    protected <Q extends Quantity<Q>> void updateQtyState(String channelName, String senecValue, int scale,
            Unit<Q> unit, @Nullable BigDecimal divisor) {
        Channel channel = getThing().getChannel(channelName);
        if (channel == null) {
            return;
        }
        BigDecimal value = getSenecValue(senecValue);
        if (divisor != null) {
            value = value.divide(divisor, scale, RoundingMode.HALF_UP);
        } else {
            value = value.setScale(scale, RoundingMode.HALF_UP);
        }
        updateState(channel.getUID(), new QuantityType<Q>(value, unit));
    }

    protected BigDecimal getSenecValue(String value) {
        String[] type = value.split("_");

        if (type[0] != null) {
            if (type[0].startsWith(VALUE_TYPE_DECIMAL)) {
                return new BigDecimal(Long.valueOf(type[1], 16));
            } else if (type[0].startsWith(VALUE_TYPE_INT1)) {
                Integer val = Integer.valueOf(type[1], 16);
                if ((val & 0x8000) > 0) {
                    val = val - 0x10000;
                }
                return new BigDecimal(val);
            } else if (type[0].startsWith(VALUE_TYPE_INT3)) {
                Long val = Long.valueOf(type[1], 16);
                if ((Math.abs(val & 0x80000000)) > 0) {
                    val = val - 0x100000000L;
                }
                return new BigDecimal(val);
            } else if (type[0].startsWith(VALUE_TYPE_INT8)) {
                Long val = Long.valueOf(type[1], 16);
                if ((val & 0x80) > 0) {
                    val = val - 0x100;
                }
                return new BigDecimal(val);
            } else if (VALUE_TYPE_FLOAT.equalsIgnoreCase(type[0])) {
                return parseFloatValue(type[1]);
            }
        }

        logger.warn("Unknown value type [{}]", type[0]);
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

    protected void updateGridPowerValues(BigDecimal gridTotalValue) {
        BigDecimal gridTotal = gridTotalValue.setScale(2, RoundingMode.HALF_UP);

        Channel channelGridPower = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER);
        if (channelGridPower != null) {
            updateState(channelGridPower.getUID(), new QuantityType<>(gridTotal, Units.WATT));
        }

        Channel channelGridPowerSupply = getThing()
                .getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER_SUPPLY);
        if (channelGridPowerSupply != null) {
            BigDecimal gridSupply = gridTotal.compareTo(BigDecimal.ZERO) < 0 ? gridTotal.abs() : BigDecimal.ZERO;
            updateState(channelGridPowerSupply.getUID(), new QuantityType<>(gridSupply, Units.WATT));
        }

        Channel channelGridPowerDraw = getThing().getChannel(SenecHomeBindingConstants.CHANNEL_SENEC_GRID_POWER_DRAW);
        if (channelGridPowerDraw != null) {
            BigDecimal gridDraw = gridTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : gridTotal.abs();
            updateState(channelGridPowerDraw.getUID(), new QuantityType<>(gridDraw, Units.WATT));
        }
    }
}
