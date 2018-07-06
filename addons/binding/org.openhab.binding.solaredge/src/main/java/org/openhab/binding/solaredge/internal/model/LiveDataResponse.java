/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * this class is used to map the live data json response
 *
 * @author Alexander Friese - initial contribution
 */
public class LiveDataResponse implements DataResponse {
    private static final String GRID = "GRID";
    private static final String LOAD = "LOAD";
    private static final String PV = "PV";
    private static final String STORAGE = "STORAGE";
    private static final Double ZERO_POWER = 0.0;

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(LiveDataResponse.class);

    public static class Value {
        public String status;
        public Double currentPower;
    }

    public static class BatteryValue {
        public String status;
        public Double currentPower;
        public Double chargeLevel;
        public String critical;
    }

    public static class Connection {
        public String from;
        public String to;
    }

    public static class SiteCurrentPowerFlow {
        public String unit;

        @SerializedName(GRID)
        public Value grid;

        @SerializedName(LOAD)
        public Value load;

        @SerializedName(PV)
        public Value pv;

        @SerializedName(STORAGE)
        public BatteryValue storage;

        public List<Connection> connections;

    }

    private SiteCurrentPowerFlow siteCurrentPowerFlow;

    @Override
    public Map<Channel, State> getValues() {
        Map<Channel, State> valueMap = new HashMap<>();

        if (siteCurrentPowerFlow != null) {

            if (siteCurrentPowerFlow.pv != null) {
                assignValue(valueMap, LiveDataChannels.PRODUCTION, siteCurrentPowerFlow.pv.currentPower,
                        siteCurrentPowerFlow.unit);
                assignValue(valueMap, LiveDataChannels.PV_STATUS, siteCurrentPowerFlow.pv.status);
            }

            if (siteCurrentPowerFlow.load != null) {
                assignValue(valueMap, LiveDataChannels.CONSUMPTION, siteCurrentPowerFlow.load.currentPower,
                        siteCurrentPowerFlow.unit);
                assignValue(valueMap, LiveDataChannels.LOAD_STATUS, siteCurrentPowerFlow.load.status);
            }

            if (siteCurrentPowerFlow.storage != null) {
                assignValue(valueMap, LiveDataChannels.BATTERY_STATUS, siteCurrentPowerFlow.storage.status);
                assignValue(valueMap, LiveDataChannels.BATTERY_CRITICAL, siteCurrentPowerFlow.storage.critical);
                assignPercentage(valueMap, LiveDataChannels.BATTERY_LEVEL, siteCurrentPowerFlow.storage.chargeLevel);
            }

            if (siteCurrentPowerFlow.grid != null) {
                assignValue(valueMap, LiveDataChannels.GRID_STATUS, siteCurrentPowerFlow.grid.status);
            }

            // init fields with zero
            assignValue(valueMap, LiveDataChannels.IMPORT, ZERO_POWER, siteCurrentPowerFlow.unit);
            assignValue(valueMap, LiveDataChannels.EXPORT, ZERO_POWER, siteCurrentPowerFlow.unit);
            assignValue(valueMap, LiveDataChannels.BATTERY_CHARGE, ZERO_POWER, siteCurrentPowerFlow.unit);
            assignValue(valueMap, LiveDataChannels.BATTERY_DISCHARGE, ZERO_POWER, siteCurrentPowerFlow.unit);
            assignValue(valueMap, LiveDataChannels.BATTERY_CHARGE_DISCHARGE, ZERO_POWER, siteCurrentPowerFlow.unit);

            // determine power flow from connection list
            if (siteCurrentPowerFlow.connections != null) {
                for (Connection con : siteCurrentPowerFlow.connections) {
                    if (con.from.equalsIgnoreCase(GRID)) {
                        assignValue(valueMap, LiveDataChannels.IMPORT, siteCurrentPowerFlow.grid.currentPower,
                                siteCurrentPowerFlow.unit);
                    } else if (con.to.equalsIgnoreCase(GRID)) {
                        assignValue(valueMap, LiveDataChannels.EXPORT, siteCurrentPowerFlow.grid.currentPower,
                                siteCurrentPowerFlow.unit);

                    }
                    if (con.from.equalsIgnoreCase(STORAGE)) {
                        assignValue(valueMap, LiveDataChannels.BATTERY_DISCHARGE,
                                siteCurrentPowerFlow.storage.currentPower, siteCurrentPowerFlow.unit);
                        assignValue(valueMap, LiveDataChannels.BATTERY_CHARGE_DISCHARGE,
                                -1 * siteCurrentPowerFlow.storage.currentPower, siteCurrentPowerFlow.unit);
                    } else if (con.to.equalsIgnoreCase(STORAGE)) {
                        assignValue(valueMap, LiveDataChannels.BATTERY_CHARGE,
                                siteCurrentPowerFlow.storage.currentPower, siteCurrentPowerFlow.unit);
                        assignValue(valueMap, LiveDataChannels.BATTERY_CHARGE_DISCHARGE,
                                siteCurrentPowerFlow.storage.currentPower, siteCurrentPowerFlow.unit);
                    }
                }
            }
        }
        return valueMap;
    }

    public final SiteCurrentPowerFlow getSiteCurrentPowerFlow() {
        return siteCurrentPowerFlow;
    }

    public final void setSiteCurrentPowerFlow(SiteCurrentPowerFlow siteCurrentPowerFlow) {
        this.siteCurrentPowerFlow = siteCurrentPowerFlow;
    }

    /**
     * converts the value to QuantityType. If no unit can be determined UnDefType.UNDEF will be used
     *
     * @param targetMap result will be put into this map
     * @param channel   channel to assign the value
     * @param value     the value to convert
     */
    protected final void assignValue(Map<Channel, State> targetMap, Channel channel, Double value,
            String unitAsString) {
        State result = UnDefType.UNDEF;

        if (value != null && unitAsString != null) {
            Unit<Power> unit = determinePowerUnit(unitAsString);
            if (unit != null) {
                result = new QuantityType<Power>(value, unit);
            } else {
                logger.debug("Channel {}: Could not determine unit: '{}'", channel, unit);
            }
        } else {
            logger.debug("Channel {}: no value provided or value has no unit.", channel);
        }
        targetMap.put(channel, result);
    }

    /**
     * assign simple String values
     *
     * @param targetMap result will be put into this map
     * @param channel   channel to assign the value
     * @param value     the value
     */
    protected final void assignValue(Map<Channel, State> targetMap, Channel channel, String value) {
        State result = UnDefType.UNDEF;

        if (value != null) {
            result = new StringType(value);
        } else {
            logger.debug("Channel {}: no value provided.", channel);
        }
        targetMap.put(channel, result);
    }

    /**
     * converts the value to QuantityType
     *
     * @param targetMap result will be put into this map
     * @param channel   channel to assign the value
     * @param value     the value to convert
     */
    protected final void assignPercentage(Map<Channel, State> targetMap, Channel channel, Double value) {
        State result = UnDefType.UNDEF;

        if (value != null) {
            result = new QuantityType<Dimensionless>(value, SmartHomeUnits.PERCENT);
        } else {
            logger.debug("Channel {}: no value provided.", channel.getFQName());
        }
        targetMap.put(channel, result);
    }
}
