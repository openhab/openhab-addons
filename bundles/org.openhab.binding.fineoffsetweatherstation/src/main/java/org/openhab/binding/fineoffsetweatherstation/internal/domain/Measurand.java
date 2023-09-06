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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_MAX_WIND_SPEED;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_MOISTURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_UV_INDEX;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_INDOOR_TEMPERATURE),

    OUTTEMP("temperature-outdoor", 0x02, "Outdoor Temperature", MeasureType.TEMPERATURE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE),

    DEWPOINT("temperature-dew-point", 0x03, "Dew point", MeasureType.TEMPERATURE),

    WINDCHILL("temperature-wind-chill", 0x04, "Wind chill", MeasureType.TEMPERATURE),

    HEATINDEX("temperature-heat-index", 0x05, "Heat index", MeasureType.TEMPERATURE),

    INHUMI("humidity-indoor", 0x06, "Indoor Humidity", MeasureType.PERCENTAGE),

    OUTHUMI("humidity-outdoor", 0x07, "Outdoor Humidity", MeasureType.PERCENTAGE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_ATMOSPHERIC_HUMIDITY),

    ABSBARO("pressure-absolute", 0x08, "Absolutely pressure", MeasureType.PRESSURE),

    RELBARO("pressure-relative", 0x09, "Relative pressure", MeasureType.PRESSURE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_BAROMETRIC_PRESSURE),

    WINDDIRECTION("direction-wind", 0x0A, "Wind Direction", MeasureType.DEGREE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION),

    WINDSPEED("speed-wind", 0x0B, "Wind Speed", MeasureType.SPEED,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED),

    GUSTSPEED("speed-gust", 0x0C, "Gust Speed", MeasureType.SPEED,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED),

    RAINEVENT("rain-event", 0x0D, "Rain Event", MeasureType.HEIGHT,
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG)),

    RAINRATE("rain-rate", 0x0E, "Rain Rate", MeasureType.HEIGHT_PER_HOUR,
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_PER_HOUR_BIG)),

    RAINHOUR("rain-hour", 0x0F, "Rain hour", MeasureType.HEIGHT,
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG)),

    RAINDAY("rain-day", 0x10, "Rain Day", MeasureType.HEIGHT,
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG),
            new ParserCustomization(ParserCustomizationType.RAIN_READING, MeasureType.HEIGHT_BIG)),

    RAINWEEK("rain-week", 0x11, "Rain Week", MeasureType.HEIGHT,
            new ParserCustomization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG),
            new ParserCustomization(ParserCustomizationType.RAIN_READING, MeasureType.HEIGHT_BIG)),

    RAINMONTH("rain-month", 0x12, "Rain Month", MeasureType.HEIGHT_BIG),

    RAINYEAR("rain-year", 0x13, "Rain Year", MeasureType.HEIGHT_BIG),

    RAINTOTALS("rain-total", 0x14, "Rain Totals", MeasureType.HEIGHT_BIG),

    LIGHT("illumination", 0x15, "Light", MeasureType.LUX),

    UV("irradiation-uv", 0x16, "UV", MeasureType.MILLIWATT_PER_SQUARE_METRE),

    UVI("uv-index", 0x17, "UV index", MeasureType.BYTE, CHANNEL_TYPE_UV_INDEX),

    TIME("time", 0x18, "Date and time", MeasureType.DATE_TIME2),

    DAYLWINDMAX("wind-max-day", 0X19, "Day max wind", MeasureType.SPEED, CHANNEL_TYPE_MAX_WIND_SPEED),

    TEMPX("temperature-channel", new int[] { 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21 }, "Temperature",
            MeasureType.TEMPERATURE),

    HUMIX("humidity-channel", new int[] { 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29 }, "Humidity",
            MeasureType.PERCENTAGE),

    SOILTEMPX("temperature-soil-channel",
            new int[] { 0x2B, 0x2D, 0x2F, 0x31, 0x33, 0x35, 0x37, 0x39, 0x3B, 0x3D, 0x3F, 0x41, 0x43, 0x45, 0x47,
                    0x49 },
            "Soil Temperature", MeasureType.TEMPERATURE),

    SOILMOISTUREX("moisture-soil-channel",
            new int[] { 0x2C, 0x2E, 0x30, 0x32, 0x34, 0x36, 0x38, 0x3A, 0x3C, 0x3E, 0x40, 0x42, 0x44, 0x46, 0x48,
                    0x4A },
            "Soil Moisture", MeasureType.PERCENTAGE, CHANNEL_TYPE_MOISTURE),

    // will no longer be used
    // skip battery-level, since it is read via Command.CMD_READ_SENSOR_ID_NEW
    LOWBATT(0x4C, new Skip(1)),

    PM25_24HAVGX("air-quality-24-hour-average-channel", new int[] { 0x4D, 0x4E, 0x4F, 0x50 },
            "PM2.5 Air Quality 24 hour average", MeasureType.PM25),

    PM25_CHX("air-quality-channel", new int[] { 0x2A, 0x51, 0x52, 0x53 }, "PM2.5 Air Quality", MeasureType.PM25),

    LEAK_CHX("water-leak-channel", new int[] { 0x58, 0x59, 0x5A, 0x5B }, "Leak", MeasureType.WATER_LEAK_DETECTION),

    // `LIGHTNING` is the name in the spec, so we keep it here as it
    LIGHTNING("lightning-distance", 0x60, "lightning distance 1~40KM", MeasureType.LIGHTNING_DISTANCE),

    LIGHTNING_TIME("lightning-time", 0x61, "lightning happened time", MeasureType.LIGHTNING_TIME),

    // `LIGHTNING_POWER` is the name in the spec, so we keep it here as it
    LIGHTNING_POWER("lightning-counter", 0x62, "lightning counter for the day", MeasureType.LIGHTNING_COUNTER),

    TF_USRX("temperature-external-channel", new int[] { 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A },
            "Soil or Water temperature", MeasureType.TEMPERATURE),

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
            "Leaf Moisture", MeasureType.PERCENTAGE, CHANNEL_TYPE_MOISTURE),

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

    PIEZO_RAIN_RATE("piezo-rain-rate", 0x80, "Rain Rate", MeasureType.HEIGHT_PER_HOUR),

    PIEZO_EVENT_RAIN("piezo-rain-event", 0x81, "Rain Event", MeasureType.HEIGHT),

    PIEZO_HOURLY_RAIN("piezo-rain-hour", 0x82, "Rain hour", MeasureType.HEIGHT),

    PIEZO_DAILY_RAIN("piezo-rain-day", 0x83, "Rain Day", MeasureType.HEIGHT_BIG),

    PIEZO_WEEKLY_RAIN("piezo-rain-week", 0x84, "Rain Week", MeasureType.HEIGHT_BIG),

    PIEZO_MONTHLY_RAIN("piezo-rain-month", 0x85, "Rain Month", MeasureType.HEIGHT_BIG),

    PIEZO_YEARLY_RAIN("piezo-rain-year", 0x86, "Rain Year", MeasureType.HEIGHT_BIG),

    PIEZO_GAIN10(0x87, new Skip(2 * 10)),

    RST_RAIN_TIME(0x88, new Skip(3)),

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

    private final int[] codes;
    private final Parser[] parsers;

    Measurand(String channelId, int code, String name, MeasureType measureType, ParserCustomization... customizations) {
        this(channelId, code, name, measureType, null, customizations);
    }

    Measurand(String channelId, int[] codes, String name, MeasureType measureType,
            ParserCustomization... customizations) {
        this(channelId, codes, name, measureType, null, customizations);
    }

    Measurand(String channelId, int code, String name, MeasureType measureType, @Nullable ChannelTypeUID channelTypeUID,
            ParserCustomization... customizations) {
        this(code, new MeasurandParser(channelId, name, measureType, channelTypeUID, customizations));
    }

    Measurand(String channelId, int[] codes, String name, MeasureType measureType,
            @Nullable ChannelTypeUID channelTypeUID, ParserCustomization... customizations) {
        this(codes, new MeasurandParser(channelId, name, measureType, channelTypeUID, customizations));
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

    private int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel, ConversionContext context,
            @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result) {
        int subOffset = 0;
        for (Parser parser : parsers) {
            subOffset += parser.extractMeasuredValues(data, offset + subOffset, channel, context, customizationType,
                    result);
        }
        return subOffset;
    }

    private interface Parser {
        int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel, ConversionContext context,
                @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result);
    }

    private static class Skip implements Parser {
        private final int skip;

        public Skip(int skip) {
            this.skip = skip;
        }

        @Override
        public int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel, ConversionContext context,
                @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result) {
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

        public int extractMeasuredValues(byte[] data, int offset, ConversionContext context,
                @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result) {
            return measurand.extractMeasuredValues(data, offset, channel, context, customizationType, result);
        }
    }

    private static class MeasurandParser implements Parser {
        private final String name;
        private final String channelPrefix;
        private final MeasureType measureType;

        private final @Nullable Map<ParserCustomizationType, ParserCustomization> customizations;
        private final @Nullable ChannelTypeUID channelTypeUID;

        MeasurandParser(String channelPrefix, String name, MeasureType measureType,
                ParserCustomization... customizations) {
            this(channelPrefix, name, measureType, null, customizations);
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

        public int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel, ConversionContext context,
                @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result) {
            MeasureType measureType = getMeasureType(customizationType);
            State state = measureType.toState(data, offset, context);
            if (state != null) {
                ChannelTypeUID channelType = channelTypeUID == null ? measureType.getChannelTypeId() : channelTypeUID;
                result.add(new MeasuredValue(measureType, channelPrefix, channel, channelType, state, name));
            }
            return measureType.getByteSize();
        }

        public MeasureType getMeasureType(@Nullable ParserCustomizationType customizationType) {
            if (customizationType == null) {
                return measureType;
            }
            return Optional.ofNullable(customizations).map(m -> m.get(customizationType))
                    .map(ParserCustomization::getMeasureType).orElse(measureType);
        }
    }
}
