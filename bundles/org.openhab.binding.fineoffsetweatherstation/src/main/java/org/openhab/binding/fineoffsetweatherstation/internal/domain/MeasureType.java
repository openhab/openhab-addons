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
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_PM1;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_PM10;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_PM25;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_PM4;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_PRESSURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_RAIN;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_RAIN_RATE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_RAIN_STATE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_SOLAR_RADIATION;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_TEMPERATURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_UV_RADIATION;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_VAPOR_PRESSURE_DEFICIT;
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
import static org.openhab.core.thing.DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_BATTERY_LEVEL;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.Utils;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the measured type with conversion from the sensors' bytes (TCP protocol) and from the
 * pre-decoded value/unit strings (Ecowitt HTTP API) to the openHAB state.
 * <p>
 * The HTTP API reports each value in the unit the gateway owner configured (e.g. °C or °F). The string
 * decoders normalize that to the same canonical unit the byte decoders produce, so a channel's state is
 * identical regardless of transport or the gateway's display settings.
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

    PM1(MICROGRAM_PER_CUBICMETRE, 2, CHANNEL_TYPE_PM1, (data, offset) -> toUInt16(data, offset) / 10.),

    PM25(MICROGRAM_PER_CUBICMETRE, 2, CHANNEL_TYPE_PM25, (data, offset) -> toUInt16(data, offset) / 10.),

    PM4(MICROGRAM_PER_CUBICMETRE, 2, CHANNEL_TYPE_PM4, (data, offset) -> toUInt16(data, offset) / 10.),

    PM10(MICROGRAM_PER_CUBICMETRE, 2, CHANNEL_TYPE_PM10, (data, offset) -> toUInt16(data, offset) / 10.),

    CO2(PARTS_PER_MILLION, 2, CHANNEL_TYPE_CO2, Utils::toUInt16),

    // solar radiation in W/m² (HTTP only); the lux illumination channel and this share the item code 0x15,
    // disambiguated by the reported unit's dimension
    SOLAR_RADIATION(Units.IRRADIANCE, 4, CHANNEL_TYPE_SOLAR_RADIATION, (data, offset) -> toUInt32(data, offset) / 10.),

    // vapor pressure deficit, conventionally expressed in kPa (HTTP only)
    VAPOR_PRESSURE_DEFICIT(KILO(PASCAL), 2, CHANNEL_TYPE_VAPOR_PRESSURE_DEFICIT,
            (data, offset) -> toUInt16(data, offset) / 1000.),

    // is-it-raining flag (HTTP only)
    RAIN_STATE(1, CHANNEL_TYPE_RAIN_STATE, (data, offset) -> OnOffType.from(toUInt8(data[offset]) != 0),
            (val, unit) -> {
                Double number = parseNumber(val);
                return number == null ? null : OnOffType.from(number != 0);
            }),

    WATER_LEAK_DETECTION(1, CHANNEL_TYPE_WATER_LEAK_DETECTION,
            (data, offset) -> OnOffType.from(toUInt8(data[offset]) != 0),
            (val, unit) -> OnOffType.from(!"normal".equalsIgnoreCase(val.trim()) && !"0".equals(val.trim()))),

    LIGHTNING_DISTANCE(KILO(METRE), 1, CHANNEL_TYPE_LIGHTNING_DISTANCE, (data, offset) -> {
        int distance = toUInt8(data[offset]);
        if (distance == 0xFF) {
            return null;
        }
        return distance;
    }),

    LIGHTNING_COUNTER(4, CHANNEL_TYPE_LIGHTNING_COUNTER, (data, offset) -> new DecimalType(toUInt32(data, offset)),
            (val, unit) -> parseDecimal(val)),

    LIGHTNING_TIME(4, CHANNEL_TYPE_LIGHTNING_TIME, (data, offset) -> {
        int epochSecond = toUInt32(data, offset);
        if (epochSecond == 0xFFFFFFFF) {
            return UnDefType.UNDEF;
        }
        return new DateTimeType(Instant.ofEpochSecond(epochSecond));
    }, (val, unit) -> parseDateTime(val)),

    MILLIWATT_PER_SQUARE_METRE(MILLI(Units.WATT).divide(SQUARE_METRE), 2, CHANNEL_TYPE_UV_RADIATION,
            (data, offset) -> Utils.toUInt16(data, offset) / 10.),

    BYTE(1, null, (data, offset) -> new DecimalType(toUInt8(data[offset])), (val, unit) -> parseDecimal(val)),
    MEMORY(Units.BYTE, 4, null, Utils::toUInt32),

    DATE_TIME2(6, null, (data, offset) -> new DateTimeType(Instant.ofEpochSecond(toUInt32(data, offset))),
            (val, unit) -> parseDateTime(val)),

    BATTERY_LEVEL(1, SYSTEM_CHANNEL_TYPE_UID_BATTERY_LEVEL, (data, offset) -> {
        @Nullable
        Integer level = new BatteryStatus(BatteryStatus.Type.LEVEL, data[offset]).getPercentage();
        return level == null ? null : new QuantityType<>(level, PERCENT);
    }, (val, unit) -> {
        @Nullable
        Integer level = new BatteryStatus(BatteryStatus.Type.LEVEL, (byte) parseInt(val)).getPercentage();
        return level == null ? null : new QuantityType<>(level, PERCENT);
    });

    private static final Logger LOGGER = LoggerFactory.getLogger(MeasureType.class);

    /**
     * splits a value like {@code "20.2"}, {@code "0.0 m/s"}, {@code "461.08 W/m2"} or {@code "51%"} into the
     * numeric part and the (optional) inline unit token
     */
    private static final Pattern VALUE_PATTERN = Pattern.compile("^\\s*([-+]?\\d*\\.?\\d+)\\s*(.*?)\\s*$");

    /**
     * representative Beaufort to m/s conversion (scale midpoints), used when the gateway reports wind in Beaufort
     */
    private static final double[] BEAUFORT_TO_MPS = { 0, 0.9, 2.5, 4.4, 6.7, 9.4, 12.3, 15.5, 19.0, 22.6, 26.5, 30.6,
            34.0 };

    private final int byteSize;
    private final @Nullable ChannelTypeUID channelTypeUID;
    private final StateConverter stateConverter;
    private final @Nullable StringStateConverter stringStateConverter;

    /**
     * @param unit the canonical unit
     * @param byteSize the size in the sensors' payload
     * @param channelTypeUID the channel type
     * @param valueExtractor a function to extract the sensor data into a number of the dimension defined by the unit
     */
    MeasureType(Unit<?> unit, int byteSize, @Nullable ChannelTypeUID channelTypeUID,
            BiFunction<byte[], Integer, @Nullable Number> valueExtractor) {
        this(byteSize, channelTypeUID, (bytes, offset) -> {
            Number value = valueExtractor.apply(bytes, offset);
            return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
        }, (val, reportedUnit) -> parseQuantity(val, reportedUnit, unit));
    }

    /**
     * @param byteSize the size in the sensors' payload
     * @param channelTypeUID the channel type
     * @param stateConverter converts the sensor bytes (TCP) into the openHAB state
     * @param stringStateConverter converts the pre-decoded value/unit string (HTTP) into the openHAB state
     */
    MeasureType(int byteSize, @Nullable ChannelTypeUID channelTypeUID, StateConverter stateConverter,
            @Nullable StringStateConverter stringStateConverter) {
        this.byteSize = byteSize;
        this.channelTypeUID = channelTypeUID;
        this.stateConverter = stateConverter;
        this.stringStateConverter = stringStateConverter;
    }

    public int getByteSize() {
        return byteSize;
    }

    public @Nullable ChannelTypeUID getChannelTypeId() {
        return channelTypeUID;
    }

    public @Nullable State toState(byte[] data, int offset) {
        return stateConverter.toState(data, offset);
    }

    /**
     * Converts a value (and optional unit) string from the HTTP API into the canonical openHAB state.
     *
     * @param val the value, possibly with an inline unit (e.g. {@code "0.0 m/s"})
     * @param unit the unit from a separate JSON field, or {@code null} when the unit is inline / absent
     * @return the state, or {@code null} if it cannot be parsed (e.g. unknown unit, dimension mismatch)
     */
    public @Nullable State parseState(String val, @Nullable String unit) {
        StringStateConverter converter = stringStateConverter;
        if (converter == null) {
            return null;
        }
        return converter.toState(val, unit);
    }

    private static @Nullable State parseQuantity(String val, @Nullable String unitField, Unit<?> canonical) {
        Matcher matcher = VALUE_PATTERN.matcher(val);
        if (!matcher.matches()) {
            return null;
        }
        double number;
        try {
            number = Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
        // an inline unit (e.g. "1003.8 hPa") wins over a separate "unit" field, which in grouped
        // entries (wh25, ch_aisle, …) only applies to the temperature value
        String inline = matcher.group(2).trim();
        String token = !inline.isEmpty() ? inline : (unitField != null && !unitField.isBlank() ? unitField.trim() : "");
        if ("BFT".equalsIgnoreCase(token)) {
            int force = (int) Math.round(number);
            if (force < 0 || force >= BEAUFORT_TO_MPS.length) {
                return null;
            }
            return convert(new QuantityType<>(BEAUFORT_TO_MPS[force], METRE_PER_SECOND), canonical);
        }
        Unit<?> reported = token.isEmpty() ? canonical : resolveUnit(token);
        if (reported == null) {
            LOGGER.debug("unknown unit token '{}' in value '{}'", token, val);
            return null;
        }
        return convert(new QuantityType<>(number, reported), canonical);
    }

    private static @Nullable State convert(QuantityType<?> quantity, Unit<?> canonical) {
        QuantityType<?> converted = quantity.toUnit(canonical);
        if (converted == null) {
            // dimension mismatch (e.g. W/m² reported for the lux illumination channel)
            return null;
        }
        // unit conversion (e.g. °F -> °C) can leave a floating-point tail; round to the sensors' precision
        double rounded = converted.toBigDecimal().setScale(4, RoundingMode.HALF_UP).doubleValue();
        return new QuantityType<>(rounded, canonical);
    }

    private static @Nullable Unit<?> resolveUnit(String token) {
        return switch (token) {
            case "C" -> CELSIUS;
            case "F" -> ImperialUnits.FAHRENHEIT;
            case "hPa" -> HECTO(PASCAL);
            case "kPa" -> KILO(PASCAL);
            case "inHg" -> ImperialUnits.INCH_OF_MERCURY;
            case "mmHg" -> Units.MILLIMETRE_OF_MERCURY;
            case "m/s" -> METRE_PER_SECOND;
            case "km/h" -> SIUnits.KILOMETRE_PER_HOUR;
            case "mph" -> ImperialUnits.MILES_PER_HOUR;
            case "knots" -> Units.KNOT;
            case "mm" -> MILLI(METRE);
            case "in" -> ImperialUnits.INCH;
            case "km" -> KILO(METRE);
            case "mi" -> ImperialUnits.MILE;
            case "mm/Hr" -> MILLIMETRE_PER_HOUR;
            case "in/Hr" -> Units.INCHES_PER_HOUR;
            case "W/m2" -> Units.IRRADIANCE;
            case "lux" -> Units.LUX;
            case "Klux", "klux" -> KILO(Units.LUX);
            case "V" -> Units.VOLT;
            case "L" -> Units.LITRE;
            case "m3", "m³" -> SIUnits.CUBIC_METRE;
            case "gal" -> ImperialUnits.GALLON_LIQUID_US;
            case "%" -> PERCENT;
            default -> null;
        };
    }

    private static @Nullable State parseDecimal(String val) {
        Matcher matcher = VALUE_PATTERN.matcher(val);
        if (!matcher.matches()) {
            return null;
        }
        try {
            return new DecimalType(new BigDecimal(matcher.group(1)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static @Nullable Double parseNumber(String val) {
        Matcher matcher = VALUE_PATTERN.matcher(val);
        if (!matcher.matches()) {
            return null;
        }
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseInt(String val) {
        Double number = parseNumber(val);
        return number == null ? 0 : (int) Math.round(number);
    }

    private static @Nullable State parseDateTime(String val) {
        try {
            // the HTTP API reports local time without a zone offset (e.g. "2024-11-25T20:41:16")
            LocalDateTime localDateTime = LocalDateTime.parse(val.trim());
            return new DateTimeType(localDateTime.atZone(ZoneId.systemDefault()));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private interface StateConverter {
        @Nullable
        State toState(byte[] data, int offset);
    }

    private interface StringStateConverter {
        @Nullable
        State toState(String val, @Nullable String unit);
    }
}
