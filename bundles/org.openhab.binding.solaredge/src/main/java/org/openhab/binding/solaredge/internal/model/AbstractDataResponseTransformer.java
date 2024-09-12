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
package org.openhab.binding.solaredge.internal.model;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.util.Map;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePrivateApi.Value;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePrivateApi.ValueAndPercent;
import org.openhab.binding.solaredge.internal.model.AggregateDataResponsePublicApi.MeterTelemetry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * common interface for all data response classes
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
abstract class AbstractDataResponseTransformer {
    static final String UNIT_WH = "Wh";
    static final String UNIT_KWH = "kWh";
    static final String UNIT_MWH = "MWh";
    static final String UNIT_W = "W";
    static final String UNIT_KW = "kW";
    static final String UNIT_MW = "MW";

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(AbstractDataResponseTransformer.class);

    /**
     * determines the unit, also handles wrong spelling of kWh (which is spelled with capital K by API)
     *
     * @param unit
     * @return
     */
    private final @Nullable Unit<Energy> determineEnergyUnit(@Nullable String unit) {
        if (unit != null) {
            if (unit.equals(UNIT_WH)) {
                return Units.WATT_HOUR;
            } else if (unit.toLowerCase().equals(UNIT_KWH.toLowerCase())) {
                return MetricPrefix.KILO(Units.WATT_HOUR);
            } else if (unit.equals(UNIT_MWH)) {
                return MetricPrefix.MEGA(Units.WATT_HOUR);
            }
        }
        logger.debug("Could not determine energy unit: '{}'", unit);
        return null;
    }

    /**
     * determines the unit, also handles wrong spelling of kW (which is spelled with capital K by API)
     *
     * @param unit
     * @return
     */
    private final @Nullable Unit<Power> determinePowerUnit(@Nullable String unit) {
        if (unit != null) {
            if (unit.equals(UNIT_W)) {
                return Units.WATT;
            } else if (unit.toLowerCase().equals(UNIT_KW.toLowerCase())) {
                return MetricPrefix.KILO(Units.WATT);
            } else if (unit.equals(UNIT_MW)) {
                return MetricPrefix.MEGA(Units.WATT);
            }
        }
        logger.debug("Could not determine power unit: '{}'", unit);
        return null;
    }

    /**
     * converts the value to QuantityType and puts it into the targetMap. If no value or unit is provided
     * UnDefType.UNDEF will be used
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param value the value to convert
     * @param unit the unit to be used
     */
    private final <T extends Quantity<T>> void putQuantityType(Map<Channel, State> targetMap, Channel channel,
            @Nullable Double value, @Nullable Unit<T> unit) {
        State result = UnDefType.UNDEF;

        if (value != null && unit != null) {
            result = new QuantityType<>(value, unit);
            logger.debug("Channel {} transformed to QuantityType ({} {}) -> {}", channel.getUID().getId(), value, unit,
                    result);
        } else {
            logger.debug("Channel {}: no value/unit provided", channel.getUID().getId());
        }
        targetMap.put(channel, result);
    }

    /**
     * converts the value to {@code QuantityType<Power>} and puts it into the targetMap. If no value or unit is
     * provided, UnDefType.UNDEF will be used
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param value the value to convert
     * @param unitAsString unit as string
     */
    protected final void putPowerType(Map<Channel, State> targetMap, @Nullable Channel channel, @Nullable Double value,
            @Nullable String unitAsString) {
        if (channel != null) {
            Unit<Power> unit = determinePowerUnit(unitAsString);
            putQuantityType(targetMap, channel, value, unit);
        }
    }

    /**
     * converts the value to {@code QuantityType<Energy>} and puts it into the targetMap. If no value or unit is
     * provided UnDefType.UNDEF will be used
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param value the value to convert
     * @param unitAsString as string
     */
    protected final void putEnergyType(Map<Channel, State> targetMap, @Nullable Channel channel, @Nullable Double value,
            @Nullable String unitAsString) {
        if (channel != null) {
            Unit<Energy> unit = determineEnergyUnit(unitAsString);
            putQuantityType(targetMap, channel, value, unit);
        }
    }

    /**
     * converts the value to {@code QuantityType<Energy>} and puts it into the targetMap. If no value or unit is
     * provided, UnDefType.UNDEF will be used
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param value the value to convert
     */
    protected final void putEnergyType(Map<Channel, State> targetMap, @Nullable Channel channel, Value value) {
        putEnergyType(targetMap, channel, value.value, value.unit);
    }

    /**
     * converts the meter value to {@code QuantityType<Energy>} and puts it into the targetMap. If multiple meter value
     * are provided a sum will be calculated. If no
     * unit can be determined UnDefType.UNDEF will be used
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param values one or more meter values
     */
    protected final void putEnergyType(Map<Channel, State> targetMap, @Nullable Channel channel, @Nullable String unit,
            MeterTelemetry... values) {
        double sum = 0.0;
        for (MeterTelemetry value : values) {
            Double innerValue = value.value;
            if (innerValue != null) {
                sum += innerValue;
            }
        }
        putEnergyType(targetMap, channel, sum, unit);
    }

    /**
     * put value as StringType into targetMap.
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param value the value
     */
    protected final void putStringType(Map<Channel, State> targetMap, @Nullable Channel channel,
            @Nullable String value) {
        if (channel != null) {
            State result = UnDefType.UNDEF;

            if (value != null) {
                result = new StringType(value);
                logger.debug("Channel {} transformed to StringType ({}) -> {}", channel.getUID().getId(), value,
                        result);

            } else {
                logger.debug("Channel {}: no value provided.", channel);
            }
            targetMap.put(channel, result);
        }
    }

    /**
     * put value as PercentType into targetMap.
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param value the value to convert
     * @param factor to be applied (usually 1 or 100)
     */
    protected final void putPercentType(Map<Channel, State> targetMap, @Nullable Channel channel,
            @Nullable Double value, int factor) {
        if (channel != null) {
            State result = UnDefType.UNDEF;

            if (value != null) {
                result = new QuantityType<>(value * factor, Units.PERCENT);
            } else {
                logger.debug("Channel {}: no value provided.", channel.getUID().getAsString());
            }
            targetMap.put(channel, result);
        }
    }

    /**
     * put value as PercentType into targetMap.
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param value the value to convert
     */
    protected final void putPercentType(Map<Channel, State> targetMap, @Nullable Channel channel,
            @Nullable Double value) {
        putPercentType(targetMap, channel, value, 1);
    }

    /**
     * put value as PercentType into targetMap.
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param value the value to convert
     */
    protected final void putPercentType(Map<Channel, State> targetMap, @Nullable Channel channel,
            ValueAndPercent value) {
        putPercentType(targetMap, channel, value.percentage, 100);
    }

    /**
     * calculates percentage and puts it into the targetmap
     *
     * @param targetMap result will be put into this map
     * @param channel channel to assign the value
     * @param dividendAsState
     * @param divisorAsState
     */
    protected final void putPercentType(Map<Channel, State> targetMap, @Nullable Channel channel,
            @Nullable State dividendAsState, @Nullable State divisorAsState) {
        Double percent = null;

        if (dividendAsState != null && divisorAsState != null) {
            DecimalType dividendAsDecimalType = dividendAsState.as(DecimalType.class);
            DecimalType divisorAsDecimalType = divisorAsState.as(DecimalType.class);

            if (dividendAsDecimalType != null && divisorAsDecimalType != null) {
                double dividend = dividendAsDecimalType.doubleValue();
                double divisor = divisorAsDecimalType.doubleValue();
                if (dividend >= 0.0 && divisor > 0.0) {
                    percent = dividend / divisor * 100;
                }
            }
        }

        putPercentType(targetMap, channel, percent);
    }

    /**
     * converts the aggregate period to the correpsonding channel group.
     *
     * @param period
     * @return
     */
    protected String convertPeriodToGroup(AggregatePeriod period) {
        String group = "undefined";
        switch (period) {
            case DAY:
                group = CHANNEL_GROUP_AGGREGATE_DAY;
                break;
            case WEEK:
                group = CHANNEL_GROUP_AGGREGATE_WEEK;
                break;
            case MONTH:
                group = CHANNEL_GROUP_AGGREGATE_MONTH;
                break;
            case YEAR:
                group = CHANNEL_GROUP_AGGREGATE_YEAR;
                break;
        }
        return group;
    }
}
