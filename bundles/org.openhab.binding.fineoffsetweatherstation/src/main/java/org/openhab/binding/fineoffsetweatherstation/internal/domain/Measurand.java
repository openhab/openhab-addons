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

import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_FREE_HEAP_SIZE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_MAX_WIND_SPEED;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_MOISTURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_UV_INDEX;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 * The measurands of supported by the gateway.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public enum Measurand {

    INTEMP("temperature-indoor", 0x01, "Indoor Temperature", MeasureType.TEMPERATURE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_INDOOR_TEMPERATURE,
            http(HttpGroup.WH25, "intemp")),

    OUTTEMP("temperature-outdoor", 0x02, "Outdoor Temperature", MeasureType.TEMPERATURE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE, http(HttpGroup.COMMON_LIST)),

    DEWPOINT("temperature-dew-point", 0x03, "Dew point", MeasureType.TEMPERATURE, http(HttpGroup.COMMON_LIST)),

    WINDCHILL("temperature-wind-chill", 0x04, "Wind chill", MeasureType.TEMPERATURE, http(HttpGroup.COMMON_LIST)),

    HEATINDEX("temperature-heat-index", 0x05, "Heat index", MeasureType.TEMPERATURE, http(HttpGroup.COMMON_LIST)),

    INHUMI("humidity-indoor", 0x06, "Indoor Humidity", MeasureType.PERCENTAGE, http(HttpGroup.WH25, "inhumi")),

    OUTHUMI("humidity-outdoor", 0x07, "Outdoor Humidity", MeasureType.PERCENTAGE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_ATMOSPHERIC_HUMIDITY, http(HttpGroup.COMMON_LIST)),

    ABSBARO("pressure-absolute", 0x08, "Absolutely pressure", MeasureType.PRESSURE, http(HttpGroup.WH25, "abs")),

    RELBARO("pressure-relative", 0x09, "Relative pressure", MeasureType.PRESSURE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_BAROMETRIC_PRESSURE, http(HttpGroup.WH25, "rel")),

    WINDDIRECTION("direction-wind", 0x0A, "Wind Direction", MeasureType.DEGREE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION, http(HttpGroup.COMMON_LIST)),

    WINDSPEED("speed-wind", 0x0B, "Wind Speed", MeasureType.SPEED,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED, http(HttpGroup.COMMON_LIST)),

    GUSTSPEED("speed-gust", 0x0C, "Gust Speed", MeasureType.SPEED,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED, http(HttpGroup.COMMON_LIST)),

    RAINEVENT("rain-event", 0x0D, "Rain Event", MeasureType.HEIGHT, http(HttpGroup.RAIN),
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG)),

    RAINRATE("rain-rate", 0x0E, "Rain Rate", MeasureType.HEIGHT_PER_HOUR, http(HttpGroup.RAIN),
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_PER_HOUR_BIG)),

    RAINHOUR("rain-hour", 0x0F, "Rain hour", MeasureType.HEIGHT, http(HttpGroup.RAIN),
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG)),

    RAINDAY("rain-day", 0x10, "Rain Day", MeasureType.HEIGHT, http(HttpGroup.RAIN),
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG),
            new ParserCustomization(ParserCustomizationType.RAIN_READING, MeasureType.HEIGHT_BIG)),

    RAINWEEK("rain-week", 0x11, "Rain Week", MeasureType.HEIGHT, http(HttpGroup.RAIN),
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG),
            new ParserCustomization(ParserCustomizationType.RAIN_READING, MeasureType.HEIGHT_BIG)),

    RAINMONTH("rain-month", 0x12, "Rain Month", MeasureType.HEIGHT_BIG, http(HttpGroup.RAIN)),

    RAINYEAR("rain-year", 0x13, "Rain Year", MeasureType.HEIGHT_BIG, http(HttpGroup.RAIN)),

    RAINTOTALS("rain-total", 0x14, "Rain Totals", MeasureType.HEIGHT_BIG, http(HttpGroup.RAIN)),

    // 0x15 is reported as lux illumination or as solar radiation (W/m²); see SOLAR_RADIATION below
    LIGHT("illumination", 0x15, "Light", MeasureType.LUX, http(HttpGroup.COMMON_LIST)),

    UV("irradiation-uv", 0x16, "UV", MeasureType.MILLIWATT_PER_SQUARE_METRE, http(HttpGroup.COMMON_LIST)),

    UVI("uv-index", 0x17, "UV index", MeasureType.BYTE, CHANNEL_TYPE_UV_INDEX, http(HttpGroup.COMMON_LIST)),

    TIME("time", 0x18, "Date and time", MeasureType.DATE_TIME2),

    DAYLWINDMAX("wind-max-day", 0X19, "Day max wind", MeasureType.SPEED, CHANNEL_TYPE_MAX_WIND_SPEED,
            http(HttpGroup.COMMON_LIST)),

    TEMPX("temperature-channel", new int[] { 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21 }, "Temperature",
            MeasureType.TEMPERATURE, http(HttpGroup.CH_AISLE, "temp")),

    HUMIX("humidity-channel", new int[] { 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29 }, "Humidity",
            MeasureType.PERCENTAGE, http(HttpGroup.CH_AISLE, "humidity")),

    SOILTEMPX("temperature-soil-channel",
            new int[] { 0x2B, 0x2D, 0x2F, 0x31, 0x33, 0x35, 0x37, 0x39, 0x3B, 0x3D, 0x3F, 0x41, 0x43, 0x45, 0x47,
                    0x49 },
            "Soil Temperature", MeasureType.TEMPERATURE),

    SOILMOISTUREX("moisture-soil-channel",
            new int[] { 0x2C, 0x2E, 0x30, 0x32, 0x34, 0x36, 0x38, 0x3A, 0x3C, 0x3E, 0x40, 0x42, 0x44, 0x46, 0x48,
                    0x4A },
            "Soil Moisture", MeasureType.PERCENTAGE, CHANNEL_TYPE_MOISTURE, http(HttpGroup.CH_SOIL, "humidity")),

    // will no longer be used
    // skip battery-level, since it is read via Command.CMD_READ_SENSOR_ID_NEW
    LOWBATT(0x4C, new Skip(1)),

    PM25_24HAVGX("air-quality-24-hour-average-channel", new int[] { 0x4D, 0x4E, 0x4F, 0x50 },
            "PM2.5 Air Quality 24 hour average", MeasureType.PM25),

    PM25_CHX("air-quality-channel", new int[] { 0x2A, 0x51, 0x52, 0x53 }, "PM2.5 Air Quality", MeasureType.PM25,
            http(HttpGroup.CH_PM25, "PM25")),

    LEAK_CHX("water-leak-channel", new int[] { 0x58, 0x59, 0x5A, 0x5B }, "Leak", MeasureType.WATER_LEAK_DETECTION,
            http(HttpGroup.CH_LEAK, "status")),

    // `LIGHTNING` is the name in the spec, so we keep it here as it
    LIGHTNING("lightning-distance", 0x60, "Lightning distance 1~40KM", MeasureType.LIGHTNING_DISTANCE,
            http(HttpGroup.LIGHTNING, "distance")),

    LIGHTNING_TIME("lightning-time", 0x61, "Lightning happened time", MeasureType.LIGHTNING_TIME,
            http(HttpGroup.LIGHTNING, "date")),

    // `LIGHTNING_POWER` is the name in the spec, so we keep it here as it
    LIGHTNING_POWER("lightning-counter", 0x62, "lightning counter for the day", MeasureType.LIGHTNING_COUNTER,
            http(HttpGroup.LIGHTNING, "count")),

    TF_USRX(new int[] { 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A },
            new MeasurandParser("temperature-external-channel", "Soil or Water temperature", MeasureType.TEMPERATURE)
                    .http(HttpGroup.CH_TEMP, "temp"),
            // skip battery-level, since it is read via Command.CMD_READ_SENSOR_ID_NEW
            new Skip(1)),

    // This is for heap : the available stack top. If it is reducing, it means the stack is using up.
    ITEM_HEAP_FREE("free-heap-size", 0x6c, "Free Heap Size", MeasureType.MEMORY, CHANNEL_TYPE_FREE_HEAP_SIZE),

    WIND_DIRECTION_AVG_10M("direction-wind-avg-10min", 0x6D, "Wind Direction (10-minute average)", MeasureType.DEGREE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION, http(HttpGroup.COMMON_LIST)),

    ITEM_SENSOR_CO2_WH46(0x6B,
            new MeasurandParser("sensor-co2-temperature", "Temperature (CO₂-Sensor)", MeasureType.TEMPERATURE)
                    .http(HttpGroup.CO2, "temp"),
            new MeasurandParser("sensor-co2-humidity", "Humidity (CO₂-Sensor)", MeasureType.PERCENTAGE)
                    .http(HttpGroup.CO2, "humidity"),
            new MeasurandParser("sensor-co2-pm10", "PM10 Air Quality (CO₂-Sensor)", MeasureType.PM10)
                    .http(HttpGroup.CO2, "PM10"),
            new MeasurandParser("sensor-co2-pm10-24-hour-average", "PM10 Air Quality 24 hour average (CO₂-Sensor)",
                    MeasureType.PM10).http(HttpGroup.CO2, "PM10_24H"),
            new MeasurandParser("sensor-co2-pm25", "PM2.5 Air Quality (CO₂-Sensor)", MeasureType.PM25)
                    .http(HttpGroup.CO2, "PM25"),
            new MeasurandParser("sensor-co2-pm25-24-hour-average", "PM2.5 Air Quality 24 hour average (CO₂-Sensor)",
                    MeasureType.PM25).http(HttpGroup.CO2, "PM25_24H"),
            new MeasurandParser("sensor-co2-co2", "CO₂", MeasureType.CO2).http(HttpGroup.CO2, "CO2"),
            new MeasurandParser("sensor-co2-co2-24-hour-average", "CO₂ 24 hour average", MeasureType.CO2)
                    .http(HttpGroup.CO2, "CO2_24H"),
            // the battery status can only be retrieved here and not via Command.CMD_READ_SENSOR_ID_NEW, since WH46
            // is not listed in the sensor array
            new MeasurandParser("sensor-co2-wh46-battery-level", "Battery Level WH46", MeasureType.BATTERY_LEVEL),
            new MeasurandParser("sensor-co2-pm1", "PM1 Air Quality (CO₂-Sensor)", MeasureType.PM1).http(HttpGroup.CO2,
                    "PM1"),
            new MeasurandParser("sensor-co2-pm1-24-hour-average", "PM1 Air Quality 24 hour average (CO₂-Sensor)",
                    MeasureType.PM1).http(HttpGroup.CO2, "PM1_24H"),
            new MeasurandParser("sensor-co2-pm4", "PM4 Air Quality (CO₂-Sensor)", MeasureType.PM4).http(HttpGroup.CO2,
                    "PM4"),
            new MeasurandParser("sensor-co2-pm4-24-hour-average", "PM4 Air Quality 24 hour average (CO₂-Sensor)",
                    MeasureType.PM4).http(HttpGroup.CO2, "PM4_24H")),

    ITEM_SENSOR_CO2(0x70,
            new MeasurandParser("sensor-co2-temperature", "Temperature (CO₂-Sensor)", MeasureType.TEMPERATURE),
            new MeasurandParser("sensor-co2-humidity", "Humidity (CO₂-Sensor)", MeasureType.PERCENTAGE),
            new MeasurandParser("sensor-co2-pm10", "PM10 Air Quality (CO₂-Sensor)", MeasureType.PM10),
            new MeasurandParser("sensor-co2-pm10-24-hour-average", "PM10 Air Quality 24 hour average (CO₂-Sensor)",
                    MeasureType.PM10),
            new MeasurandParser("sensor-co2-pm25", "PM2.5 Air Quality (CO₂-Sensor)", MeasureType.PM25),
            new MeasurandParser("sensor-co2-pm25-24-hour-average", "PM2.5 Air Quality 24 hour average (CO₂-Sensor)",
                    MeasureType.PM25),
            new MeasurandParser("sensor-co2-co2", "CO₂", MeasureType.CO2),
            new MeasurandParser("sensor-co2-co2-24-hour-average", "CO₂ 24 hour average", MeasureType.CO2),
            // skip battery-level, since it is read via Command.CMD_READ_SENSOR_ID_NEW
            new Skip(1)),

    LEAF_WETNESS_CHX("leaf-wetness-channel", new int[] { 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79 },
            "Leaf Moisture", MeasureType.PERCENTAGE, CHANNEL_TYPE_MOISTURE, http(HttpGroup.CH_LEAF, "humidity")),

    /**
     * 1 Traditional rain gauge
     * 2 Piezoelectric rain gauge
     */
    RAIN_PRIO(0x7a, new Skip(1)),

    /**
     * 0 = RFM433M
     * 1 = RFM868M
     * default = RFM915M
     */
    RCSATION(0x7b, new Skip(1)),

    // the piezo (haptic) rain gauge reuses the low rain item codes within the HTTP piezoRain group
    PIEZO_RAIN_RATE("piezo-rain-rate", 0x80, "Rain Rate", MeasureType.HEIGHT_PER_HOUR,
            http(HttpGroup.PIEZO_RAIN, 0x0E)),

    PIEZO_EVENT_RAIN("piezo-rain-event", 0x81, "Rain Event", MeasureType.HEIGHT, http(HttpGroup.PIEZO_RAIN, 0x0D)),

    PIEZO_HOURLY_RAIN("piezo-rain-hour", 0x82, "Rain hour", MeasureType.HEIGHT, http(HttpGroup.PIEZO_RAIN, 0x0F)),

    PIEZO_DAILY_RAIN("piezo-rain-day", 0x83, "Rain Day", MeasureType.HEIGHT_BIG, http(HttpGroup.PIEZO_RAIN, 0x10)),

    PIEZO_WEEKLY_RAIN("piezo-rain-week", 0x84, "Rain Week", MeasureType.HEIGHT_BIG, http(HttpGroup.PIEZO_RAIN, 0x11)),

    PIEZO_MONTHLY_RAIN("piezo-rain-month", 0x85, "Rain Month", MeasureType.HEIGHT_BIG,
            http(HttpGroup.PIEZO_RAIN, 0x12)),

    PIEZO_YEARLY_RAIN("piezo-rain-year", 0x86, "Rain Year", MeasureType.HEIGHT_BIG, http(HttpGroup.PIEZO_RAIN, 0x13)),

    PIEZO_GAIN10(0x87, new Skip(2 * 10)),

    RST_RAIN_TIME(0x88, new Skip(3)),

    // the WN38 black globe sensor reports a black globe temperature (BGT) and a wet bulb globe temperature (WBGT)
    BLACK_GLOBE_TEMPERATURE("temperature-black-globe", 0xA1, "Black Globe Temperature", MeasureType.TEMPERATURE,
            http(HttpGroup.COMMON_LIST)),

    WET_BULB_GLOBE_TEMPERATURE("temperature-wet-bulb-globe", 0xA2, "Wet Bulb Globe Temperature",
            MeasureType.TEMPERATURE, http(HttpGroup.COMMON_LIST)),

    // measurands only available via the Ecowitt HTTP API (no TCP item code)
    FEELS_LIKE("temperature-feels-like", "Feels like temperature", MeasureType.TEMPERATURE,
            http(HttpGroup.COMMON_LIST, "3")),

    VPD("vapor-pressure-deficit", "Vapor Pressure Deficit", MeasureType.VAPOR_PRESSURE_DEFICIT,
            http(HttpGroup.COMMON_LIST, "5")),

    // shares item code 0x15 with LIGHT; used when the gateway reports solar radiation in W/m² instead of lux
    SOLAR_RADIATION("irradiation-solar", "Solar irradiation", MeasureType.SOLAR_RADIATION,
            httpAlt(HttpGroup.COMMON_LIST, 0x15)),

    // rainfall over the last 24 hours; only reported via the HTTP API (no dedicated TCP item code), in the rain group
    // for the traditional gauge and in the piezoRain group for the haptic gauge - both keyed by id 0x7C
    RAIN_24HOURS("rain-24-hours", "Rainfall last 24 hours", MeasureType.HEIGHT_BIG, http(HttpGroup.RAIN, "0x7C")),

    PIEZO_RAIN_24HOURS("piezo-rain-24-hours", "Rainfall last 24 hours", MeasureType.HEIGHT_BIG,
            http(HttpGroup.PIEZO_RAIN, "0x7C")),

    PIEZO_RAIN_STATE("piezo-rain-state", "Raining (piezo)", MeasureType.RAIN_STATE,
            http(HttpGroup.PIEZO_RAIN, "srain_piezo")),

    ;

    private static final Map<Byte, SingleChannelMeasurand> MEASURANDS = new HashMap<>();

    static {
        for (Measurand measurand : values()) {
            for (int i = 0; i < measurand.codes.length; i++) {
                int code = measurand.codes[i];
                // if we get more than one code this measurand has multiple channels
                Integer channel = measurand.codes.length == 1 ? null : i + 1;
                MEASURANDS.put((byte) code, new SingleChannelMeasurand(measurand, channel));
            }
        }
    }

    /**
     * The lookup from an HTTP {@code get_livedata_info} group + id/field to a measurand's parser. It is built from the
     * {@code http(...)} declarations on the enum constants above, so each data point's HTTP mapping lives in one place
     * right next to its definition.
     */
    private static final Map<HttpGroup, Map<String, HttpBinding>> HTTP_LOOKUP = new EnumMap<>(HttpGroup.class);

    static {
        for (Measurand measurand : values()) {
            for (Parser parser : measurand.parsers) {
                if (!(parser instanceof MeasurandParser measurandParser)) {
                    continue;
                }
                HttpSource source = measurandParser.httpSource;
                if (source == null) {
                    continue;
                }
                Map<String, HttpBinding> bindings = Objects
                        .requireNonNull(HTTP_LOOKUP.computeIfAbsent(source.group, g -> new HashMap<>()));
                String normalizedKey = normalizeId(source.resolveKey(measurand));
                if (source.alternate) {
                    HttpBinding existing = bindings.get(normalizedKey);
                    if (existing != null) {
                        existing.setAlternate(measurandParser);
                        continue;
                    }
                }
                bindings.put(normalizedKey, new HttpBinding(measurandParser, null));
            }
        }
    }

    private static String codeKey(int code) {
        return "0x" + Integer.toHexString(code & 0xFF);
    }

    static HttpSource http(HttpGroup group) {
        return new HttpSource(group, null, null, false);
    }

    static HttpSource http(HttpGroup group, int httpCode) {
        return new HttpSource(group, httpCode, null, false);
    }

    static HttpSource http(HttpGroup group, String key) {
        return new HttpSource(group, null, key, false);
    }

    static HttpSource httpAlt(HttpGroup group, int httpCode) {
        return new HttpSource(group, httpCode, null, true);
    }

    /**
     * Normalizes a JSON {@code id} so that hex codes ({@code "0x02"}, {@code "0x0D"}) match regardless of casing or
     * zero-padding, while decimal and string tokens ({@code "3"}, {@code "srain_piezo"}) are kept as their own keys -
     * note that hex {@code "0x03"} and decimal {@code "3"} are deliberately different keys (dew point vs. feels-like).
     */
    public static String normalizeId(String id) {
        String trimmed = id.trim();
        if (trimmed.length() > 2 && trimmed.charAt(0) == '0'
                && (trimmed.charAt(1) == 'x' || trimmed.charAt(1) == 'X')) {
            try {
                return "0x" + Integer.toHexString(Integer.parseInt(trimmed.substring(2), 16) & 0xFF);
            } catch (NumberFormatException e) {
                return trimmed.toLowerCase();
            }
        }
        return trimmed;
    }

    public static @Nullable HttpBinding getHttpBinding(HttpGroup group, String id) {
        Map<String, HttpBinding> map = HTTP_LOOKUP.get(group);
        if (map == null) {
            return null;
        }
        return map.get(normalizeId(id));
    }

    private final int[] codes;
    private final Parser[] parsers;

    /**
     * Constructor for measurands that are only available via the HTTP API and therefore have no TCP item code.
     */
    Measurand(String channelId, String name, MeasureType measureType, HttpSource httpSource) {
        this(new int[0], new MeasurandParser(channelId, name, measureType).http(httpSource));
    }

    Measurand(String channelId, int code, String name, MeasureType measureType, ParserCustomization... customizations) {
        this(channelId, code, name, measureType, (ChannelTypeUID) null, customizations);
    }

    Measurand(String channelId, int code, String name, MeasureType measureType, HttpSource httpSource,
            ParserCustomization... customizations) {
        this(new int[] { code },
                new MeasurandParser(channelId, name, measureType, null, customizations).http(httpSource));
    }

    Measurand(String channelId, int[] codes, String name, MeasureType measureType,
            ParserCustomization... customizations) {
        this(channelId, codes, name, measureType, (ChannelTypeUID) null, customizations);
    }

    Measurand(String channelId, int[] codes, String name, MeasureType measureType, HttpSource httpSource,
            ParserCustomization... customizations) {
        this(codes, new MeasurandParser(channelId, name, measureType, null, customizations).http(httpSource));
    }

    Measurand(String channelId, int code, String name, MeasureType measureType, @Nullable ChannelTypeUID channelTypeUID,
            ParserCustomization... customizations) {
        this(code, new MeasurandParser(channelId, name, measureType, channelTypeUID, customizations));
    }

    Measurand(String channelId, int code, String name, MeasureType measureType, @Nullable ChannelTypeUID channelTypeUID,
            HttpSource httpSource, ParserCustomization... customizations) {
        this(new int[] { code },
                new MeasurandParser(channelId, name, measureType, channelTypeUID, customizations).http(httpSource));
    }

    Measurand(String channelId, int[] codes, String name, MeasureType measureType,
            @Nullable ChannelTypeUID channelTypeUID, ParserCustomization... customizations) {
        this(codes, new MeasurandParser(channelId, name, measureType, channelTypeUID, customizations));
    }

    Measurand(String channelId, int[] codes, String name, MeasureType measureType,
            @Nullable ChannelTypeUID channelTypeUID, HttpSource httpSource, ParserCustomization... customizations) {
        this(codes, new MeasurandParser(channelId, name, measureType, channelTypeUID, customizations).http(httpSource));
    }

    Measurand(int code, Parser... parsers) {
        this(new int[] { code }, parsers);
    }

    Measurand(int[] codes, Parser... parsers) {
        this.codes = codes;
        this.parsers = parsers;
    }

    public static @Nullable SingleChannelMeasurand getByCode(byte code) {
        return MEASURANDS.get(code);
    }

    private int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel,
            @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result,
            DebugDetails debugDetails) {
        int subOffset = 0;
        for (Parser parser : parsers) {
            subOffset += parser.extractMeasuredValues(data, offset + subOffset, channel, customizationType, result,
                    debugDetails);
        }
        return subOffset;
    }

    private interface Parser {
        int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel,
                @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result,
                DebugDetails debugDetails);
    }

    private static class Skip implements Parser {
        private final int skip;

        public Skip(int skip) {
            this.skip = skip;
        }

        @Override
        public int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel,
                @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result,
                DebugDetails debugDetails) {
            debugDetails.addDebugDetails(offset, skip, "skipped");
            return skip;
        }
    }

    public enum ParserCustomizationType {
        ELV,
        RAIN_READING
    }

    private static class ParserCustomization {

        private final ParserCustomizationType type;
        private final MeasureType measureType;

        public ParserCustomization(ParserCustomizationType type, MeasureType measureType) {
            this.type = type;
            this.measureType = measureType;
        }

        public ParserCustomizationType getType() {
            return type;
        }

        public MeasureType getMeasureType() {
            return measureType;
        }
    }

    public static class SingleChannelMeasurand {
        private final Measurand measurand;
        private final @Nullable Integer channel;

        public SingleChannelMeasurand(Measurand measurand, @Nullable Integer channel) {
            this.measurand = measurand;
            this.channel = channel;
        }

        public int extractMeasuredValues(byte[] data, int offset, @Nullable ParserCustomizationType customizationType,
                List<MeasuredValue> result, DebugDetails debugDetails) {
            return measurand.extractMeasuredValues(data, offset, channel, customizationType, result, debugDetails);
        }

        public String getDebugString() {
            return measurand.name() + (channel == null ? "" : " channel " + channel);
        }
    }

    private static class MeasurandParser implements Parser {
        private final String name;
        private final String channelPrefix;
        private final MeasureType measureType;

        private final @Nullable Map<ParserCustomizationType, ParserCustomization> customizations;
        private final @Nullable ChannelTypeUID channelTypeUID;

        private @Nullable HttpSource httpSource;

        MeasurandParser(String channelPrefix, String name, MeasureType measureType,
                ParserCustomization... customizations) {
            this(channelPrefix, name, measureType, null, customizations);
        }

        /**
         * Declares where this value is found in the Ecowitt HTTP {@code get_livedata_info} response. Returns
         * {@code this} so it can be chained when constructing the parser inline.
         */
        MeasurandParser http(HttpSource httpSource) {
            this.httpSource = httpSource;
            return this;
        }

        MeasurandParser http(HttpGroup group, String key) {
            return http(Measurand.http(group, key));
        }

        MeasurandParser(String channelPrefix, String name, MeasureType measureType,
                @Nullable ChannelTypeUID channelTypeUID, ParserCustomization... customizations) {
            this.channelPrefix = channelPrefix;
            this.name = name;
            this.measureType = measureType;
            this.channelTypeUID = channelTypeUID;
            if (customizations.length == 0) {
                this.customizations = null;
            } else {
                this.customizations = Collections.unmodifiableMap(
                        Arrays.stream(customizations).collect(Collectors.toMap(ParserCustomization::getType,
                                customization -> customization, (a, b) -> b, HashMap::new)));
            }
        }

        @Override
        public int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel,
                @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result,
                DebugDetails debugDetails) {
            MeasureType measureType = getMeasureType(customizationType);
            State state = measureType.toState(data, offset);
            if (state != null) {
                debugDetails.addDebugDetails(offset, measureType.getByteSize(),
                        measureType.name() + ": " + state.toFullString());
                ChannelTypeUID channelType = channelTypeUID == null ? measureType.getChannelTypeId() : channelTypeUID;
                result.add(new MeasuredValue(measureType, channelPrefix, channel, channelType, state, name));
            } else {
                debugDetails.addDebugDetails(offset, measureType.getByteSize(), measureType.name() + ": null");
            }
            return measureType.getByteSize();
        }

        public MeasureType getMeasureType(@Nullable ParserCustomizationType customizationType) {
            if (customizationType == null) {
                return measureType;
            }
            return Objects.requireNonNull(Optional.ofNullable(customizations).map(m -> m.get(customizationType))
                    .map(ParserCustomization::getMeasureType).orElse(measureType));
        }

        @Nullable
        MeasuredValue parseHttp(String val, @Nullable String unit, @Nullable Integer channel,
                @Nullable ParserCustomizationType customizationType) {
            MeasureType measureType = getMeasureType(customizationType);
            State state = measureType.parseState(val, unit);
            if (state == null) {
                return null;
            }
            ChannelTypeUID channelType = channelTypeUID == null ? measureType.getChannelTypeId() : channelTypeUID;
            return new MeasuredValue(measureType, channelPrefix, channel, channelType, state, name);
        }
    }

    /**
     * Binds an HTTP {@code get_livedata_info} entry to a measurand's parser. The optional {@code alternate} is used
     * when the reported unit does not fit the primary parser's canonical dimension - this lets the lux illumination
     * and the W/m² solar-radiation channel share the item code {@code 0x15}.
     */
    public static final class HttpBinding {
        private final MeasurandParser parser;
        private @Nullable MeasurandParser alternate;

        HttpBinding(MeasurandParser parser, @Nullable MeasurandParser alternate) {
            this.parser = parser;
            this.alternate = alternate;
        }

        void setAlternate(MeasurandParser alternate) {
            this.alternate = alternate;
        }

        public @Nullable MeasuredValue parse(String val, @Nullable String unit, @Nullable Integer channel,
                @Nullable ParserCustomizationType customizationType) {
            MeasuredValue value = parser.parseHttp(val, unit, channel, customizationType);
            @Nullable
            MeasurandParser alternate = this.alternate;
            if (value == null && alternate != null) {
                value = alternate.parseHttp(val, unit, channel, customizationType);
            }
            return value;
        }
    }

    /**
     * Declares where a measurand's value is found in the Ecowitt HTTP {@code get_livedata_info} response, created via
     * the {@link Measurand#http} / {@link Measurand#httpAlt} factory methods on the enum constants.
     */
    static final class HttpSource {
        private final HttpGroup group;
        // explicit HTTP item code (used when it differs from the TCP code, e.g. the piezo rain channels)
        private final @Nullable Integer httpCode;
        // string id (e.g. "srain_piezo") or named field (e.g. "intemp"); null means "reuse the TCP item code"
        private final @Nullable String key;
        // whether this measurand is the dimension alternate for an already-registered code (see SOLAR_RADIATION)
        private final boolean alternate;

        private HttpSource(HttpGroup group, @Nullable Integer httpCode, @Nullable String key, boolean alternate) {
            this.group = group;
            this.httpCode = httpCode;
            this.key = key;
            this.alternate = alternate;
        }

        /**
         * @return the lookup key: the explicit string id/field, the explicit HTTP item code, or - as a fallback for a
         *         measurand declaring {@code http(group)} - its single TCP item code
         */
        private String resolveKey(Measurand measurand) {
            String explicitKey = key;
            if (explicitKey != null) {
                return explicitKey;
            }
            Integer explicitCode = httpCode;
            if (explicitCode != null) {
                return codeKey(explicitCode);
            }
            return codeKey(measurand.codes[0]);
        }
    }
}
