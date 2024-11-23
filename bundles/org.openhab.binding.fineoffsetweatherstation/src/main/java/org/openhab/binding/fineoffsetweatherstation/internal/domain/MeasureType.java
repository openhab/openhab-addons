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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import static javax.measure.MetricPrefix.HECTO;
import static javax.measure.MetricPrefix.KILO;
import static javax.measure.MetricPrefix.MILLI;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_CO2;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_HUMIDITY;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_ILLUMINATION;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_LIGHTNING_COUNTER;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_LIGHTNING_DISTANCE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_LIGHTNING_TIME;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_PM10;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_PM25;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_PRESSURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_RAIN;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_RAIN_RATE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_TEMPERATURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_UV_RADIATION;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_WATER_LEAK_DETECTION;
import static org.openhab.binding.fineoffsetweatherstation.internal.Utils.toInt16;
import static org.openhab.binding.fineoffsetweatherstation.internal.Utils.toUInt16;
import static org.openhab.binding.fineoffsetweatherstation.internal.Utils.toUInt32;
import static org.openhab.binding.fineoffsetweatherstation.internal.Utils.toUInt8;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.SIUnits.METRE;
import static org.openhab.core.library.unit.SIUnits.PASCAL;
import static org.openhab.core.library.unit.SIUnits.SQUARE_METRE;
import static org.openhab.core.library.unit.Units.DEGREE_ANGLE;
import static org.openhab.core.library.unit.Units.METRE_PER_SECOND;
import static org.openhab.core.library.unit.Units.MICROGRAM_PER_CUBICMETRE;
import static org.openhab.core.library.unit.Units.MILLIMETRE_PER_HOUR;
import static org.openhab.core.library.unit.Units.PARTS_PER_MILLION;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.Utils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Represents the measured type with conversion from the sensors' bytes to the openHAB state.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public enum MeasureType {

    TEMPERATURE(CELSIUS, 2, CHANNEL_TYPE_TEMPERATURE, (data, offset) -> toInt16(data, offset) / 10.),

    PERCENTAGE(PERCENT, 1, CHANNEL_TYPE_HUMIDITY, (data, offset) -> toUInt8(data[offset])),

    PRESSURE(HECTO(PASCAL), 2, CHANNEL_TYPE_PRESSURE, (data, offset) -> toUInt16(data, offset) / 10.),

    DEGREE(DEGREE_ANGLE, 2, null, Utils::toUInt16),

    SPEED(METRE_PER_SECOND, 2, null, (data, offset) -> toUInt16(data, offset) / 10.),

    HEIGHT(MILLI(METRE), 2, CHANNEL_TYPE_RAIN, (data, offset) -> toUInt16(data, offset) / 10.),

    HEIGHT_BIG(MILLI(METRE), 4, CHANNEL_TYPE_RAIN, (data, offset) -> toUInt32(data, offset) / 10.),

    HEIGHT_PER_HOUR(MILLIMETRE_PER_HOUR, 2, CHANNEL_TYPE_RAIN_RATE, (data, offset) -> toUInt16(data, offset) / 10.),

    HEIGHT_PER_HOUR_BIG(MILLIMETRE_PER_HOUR, 4, CHANNEL_TYPE_RAIN_RATE, (data, offset) -> toUInt32(data, offset) / 10.),

    LUX(Units.LUX, 4, CHANNEL_TYPE_ILLUMINATION, (data, offset) -> toUInt32(data, offset) / 10.),

    PM25(MICROGRAM_PER_CUBICMETRE, 2, CHANNEL_TYPE_PM25, (data, offset) -> toUInt16(data, offset) / 10.),

    PM10(MICROGRAM_PER_CUBICMETRE, 2, CHANNEL_TYPE_PM10, (data, offset) -> toUInt16(data, offset) / 10.),

    CO2(PARTS_PER_MILLION, 2, CHANNEL_TYPE_CO2, Utils::toUInt16),

    WATER_LEAK_DETECTION(1, CHANNEL_TYPE_WATER_LEAK_DETECTION,
            (data, offset, context) -> OnOffType.from(toUInt8(data[offset]) != 0)),

    LIGHTNING_DISTANCE(KILO(METRE), 1, CHANNEL_TYPE_LIGHTNING_DISTANCE, (data, offset) -> {
        int distance = toUInt8(data[offset]);
        if (distance == 0xFF) {
            return null;
        }
        return distance;
    }),

    LIGHTNING_COUNTER(4, CHANNEL_TYPE_LIGHTNING_COUNTER,
            (data, offset, context) -> new DecimalType(toUInt32(data, offset))),

    LIGHTNING_TIME(4, CHANNEL_TYPE_LIGHTNING_TIME, (data, offset, context) -> {
        int epochSecond = toUInt32(data, offset);
        if (epochSecond == 0xFFFFFFFF) {
            return UnDefType.UNDEF;
        }
        return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), context.getZoneId()));
    }),

    MILLIWATT_PER_SQUARE_METRE(MILLI(Units.WATT).divide(SQUARE_METRE), 2, CHANNEL_TYPE_UV_RADIATION,
            (data, offset) -> Utils.toUInt16(data, offset) / 10.),

    BYTE(1, null, (data, offset, context) -> new DecimalType(toUInt8(data[offset]))),
    MEMORY(Units.BYTE, 4, null, Utils::toUInt32),

    DATE_TIME2(6, null, (data, offset, context) -> new DateTimeType(
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(toUInt32(data, offset)), context.getZoneId())));

    private final int byteSize;
    private final @Nullable ChannelTypeUID channelTypeUID;
    private final StateConverter stateConverter;

    /**
     * @param unit the unit
     * @param byteSize the size in the sensors' payload
     * @param channelTypeUID the channel type
     * @param valueExtractor a function to extract the sensor data into a number of the dimension defined by the unit
     */
    MeasureType(Unit<?> unit, int byteSize, @Nullable ChannelTypeUID channelTypeUID,
            BiFunction<byte[], Integer, @Nullable Number> valueExtractor) {
        this(byteSize, channelTypeUID, (bytes, offset, context) -> {
            Number value = valueExtractor.apply(bytes, offset);
            return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
        });
    }

    /**
     * @param byteSize the size in the sensors' payload
     * @param channelTypeUID the channel type
     * @param stateConverter a function to extract the sensor data into the openHAB's state
     */
    MeasureType(int byteSize, @Nullable ChannelTypeUID channelTypeUID, StateConverter stateConverter) {
        this.byteSize = byteSize;
        this.channelTypeUID = channelTypeUID;
        this.stateConverter = stateConverter;
    }

    public int getByteSize() {
        return byteSize;
    }

    public @Nullable ChannelTypeUID getChannelTypeId() {
        return channelTypeUID;
    }

    public @Nullable State toState(byte[] data, int offset, ConversionContext context) {
        return stateConverter.toState(data, offset, context);
    }

    private interface StateConverter {
        @Nullable
        State toState(byte[] data, int offset, ConversionContext context);
    }
}
