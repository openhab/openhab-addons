/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    INTEMP("temperature-indoor", (byte) 0x01, "Indoor Temperature", MeasureType.TEMPERATURE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_INDOOR_TEMPERATURE),

    OUTTEMP("temperature-outdoor", (byte) 0x02, "Outdoor Temperature", MeasureType.TEMPERATURE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE),

    DEWPOINT("temperature-dew-point", (byte) 0x03, "Dew point", MeasureType.TEMPERATURE),

    WINDCHILL("temperature-wind-chill", (byte) 0x04, "Wind chill", MeasureType.TEMPERATURE),

    HEATINDEX("temperature-heat-index", (byte) 0x05, "Heat index", MeasureType.TEMPERATURE),

    INHUMI("humidity-indoor", (byte) 0x06, "Indoor Humidity", MeasureType.PERCENTAGE),

    OUTHUMI("humidity-outdoor", (byte) 0x07, "Outdoor Humidity", MeasureType.PERCENTAGE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_ATMOSPHERIC_HUMIDITY),

    ABSBARO("pressure-absolute", (byte) 0x08, "Absolutely pressure", MeasureType.PRESSURE),

    RELBARO("pressure-relative", (byte) 0x09, "Relative pressure", MeasureType.PRESSURE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_BAROMETRIC_PRESSURE),

    WINDDIRECTION("direction-wind", (byte) 0x0A, "Wind Direction", MeasureType.DEGREE,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION),

    WINDSPEED("speed-wind", (byte) 0x0B, "Wind Speed", MeasureType.SPEED,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED),

    GUSTSPEED("speed-gust", (byte) 0x0C, "Gust Speed", MeasureType.SPEED,
            DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED),

    RAINEVENT("rain-event", (byte) 0x0D, "Rain Event", MeasureType.HEIGHT),

    RAINRATE("rain-rate", (byte) 0x0E, "Rain Rate", MeasureType.HEIGHT_PER_HOUR),

    RAINHOUR("rain-hour", (byte) 0x0F, "Rain hour", MeasureType.HEIGHT),

    RAINDAY("rain-day", (byte) 0x10, "Rain Day", MeasureType.HEIGHT),

    RAINWEEK("rain-week", (byte) 0x11, "Rain Week", MeasureType.HEIGHT),

    RAINMONTH("rain-month", (byte) 0x12, "Rain Month", MeasureType.HEIGHT_BIG),

    RAINYEAR("rain-year", (byte) 0x13, "Rain Year", MeasureType.HEIGHT_BIG),

    RAINTOTALS("rain-total", (byte) 0x14, "Rain Totals", MeasureType.HEIGHT_BIG),

    LIGHT("illumination", (byte) 0x15, "Light", MeasureType.LUX),

    UV("irradiation-uv", (byte) 0x16, "UV", MeasureType.MICROWATT_PER_SQUARE_CENTIMETRE),

    UVI("uv-index", (byte) 0x17, "UV index", MeasureType.BYTE, CHANNEL_TYPE_UV_INDEX),

    TIME("time", (byte) 0x18, "Date and time", MeasureType.DATE_TIME2),

    DAYLWINDMAX("wind-max-day", (byte) 0X19, "Day max wind", MeasureType.SPEED, CHANNEL_TYPE_MAX_WIND_SPEED),

    TEMP1("temperature-channel-1", (byte) 0x1A, "Temperature 1", MeasureType.TEMPERATURE),

    TEMP2("temperature-channel-2", (byte) 0x1B, "Temperature 2", MeasureType.TEMPERATURE),

    TEMP3("temperature-channel-3", (byte) 0x1C, "Temperature 3", MeasureType.TEMPERATURE),

    TEMP4("temperature-channel-4", (byte) 0x1D, "Temperature 4", MeasureType.TEMPERATURE),

    TEMP5("temperature-channel-5", (byte) 0x1E, "Temperature 5", MeasureType.TEMPERATURE),

    TEMP6("temperature-channel-6", (byte) 0x1F, "Temperature 6", MeasureType.TEMPERATURE),

    TEMP7("temperature-channel-7", (byte) 0x20, "Temperature 7", MeasureType.TEMPERATURE),

    TEMP8("temperature-channel-8", (byte) 0x21, "Temperature 8", MeasureType.TEMPERATURE),

    HUMI1("humidity-channel-1", (byte) 0x22, "Humidity 1", MeasureType.PERCENTAGE),

    HUMI2("humidity-channel-2", (byte) 0x23, "Humidity 2", MeasureType.PERCENTAGE),

    HUMI3("humidity-channel-3", (byte) 0x24, "Humidity 3", MeasureType.PERCENTAGE),

    HUMI4("humidity-channel-4", (byte) 0x25, "Humidity 4", MeasureType.PERCENTAGE),

    HUMI5("humidity-channel-5", (byte) 0x26, "Humidity 5", MeasureType.PERCENTAGE),

    HUMI6("humidity-channel-6", (byte) 0x27, "Humidity 6", MeasureType.PERCENTAGE),

    HUMI7("humidity-channel-7", (byte) 0x28, "Humidity 7", MeasureType.PERCENTAGE),

    HUMI8("humidity-channel-8", (byte) 0x29, "Humidity 8", MeasureType.PERCENTAGE),

    SOILTEMP1("temperature-soil-channel-1", (byte) 0x2B, "Soil Temperature 1", MeasureType.TEMPERATURE),

    SOILTEMP2("temperature-soil-channel-2", (byte) 0x2D, "Soil Temperature 2", MeasureType.TEMPERATURE),

    SOILTEMP3("temperature-soil-channel-3", (byte) 0x2F, "Soil Temperature 3", MeasureType.TEMPERATURE),

    SOILTEMP4("temperature-soil-channel-4", (byte) 0x31, "Soil Temperature 4", MeasureType.TEMPERATURE),

    SOILTEMP5("temperature-soil-channel-5", (byte) 0x33, "Soil Temperature 5", MeasureType.TEMPERATURE),

    SOILTEMP6("temperature-soil-channel-6", (byte) 0x35, "Soil Temperature 6", MeasureType.TEMPERATURE),

    SOILTEMP7("temperature-soil-channel-7", (byte) 0x37, "Soil Temperature 7", MeasureType.TEMPERATURE),

    SOILTEMP8("temperature-soil-channel-8", (byte) 0x39, "Soil Temperature 8", MeasureType.TEMPERATURE),

    SOILTEMP9("temperature-soil-channel-9", (byte) 0x3B, "Soil Temperature 9", MeasureType.TEMPERATURE),

    SOILTEMP10("temperature-soil-channel-10", (byte) 0x3D, "Soil Temperature 10", MeasureType.TEMPERATURE),

    SOILTEMP11("temperature-soil-channel-11", (byte) 0x3F, "Soil Temperature 11", MeasureType.TEMPERATURE),

    SOILTEMP12("temperature-soil-channel-12", (byte) 0x41, "Soil Temperature 12", MeasureType.TEMPERATURE),

    SOILTEMP13("temperature-soil-channel-13", (byte) 0x43, "Soil Temperature 13", MeasureType.TEMPERATURE),

    SOILTEMP14("temperature-soil-channel-14", (byte) 0x45, "Soil Temperature 14", MeasureType.TEMPERATURE),

    SOILTEMP15("temperature-soil-channel-15", (byte) 0x47, "Soil Temperature 15", MeasureType.TEMPERATURE),

    SOILTEMP16("temperature-soil-channel-16", (byte) 0x49, "Soil Temperature 16", MeasureType.TEMPERATURE),

    SOILMOISTURE1("moisture-soil-channel-1", (byte) 0x2C, "Soil Moisture 1", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE2("moisture-soil-channel-2", (byte) 0x2E, "Soil Moisture 2", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE3("moisture-soil-channel-3", (byte) 0x30, "Soil Moisture 3", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE4("moisture-soil-channel-4", (byte) 0x32, "Soil Moisture 4", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE5("moisture-soil-channel-5", (byte) 0x34, "Soil Moisture 5", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE6("moisture-soil-channel-6", (byte) 0x36, "Soil Moisture 6", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE7("moisture-soil-channel-7", (byte) 0x38, "Soil Moisture 7", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE8("moisture-soil-channel-8", (byte) 0x3A, "Soil Moisture 8", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE9("moisture-soil-channel-9", (byte) 0x3C, "Soil Moisture 9", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE10("moisture-soil-channel-10", (byte) 0x3E, "Soil Moisture 10", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE11("moisture-soil-channel-11", (byte) 0x40, "Soil Moisture 11", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE12("moisture-soil-channel-12", (byte) 0x42, "Soil Moisture 12", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE13("moisture-soil-channel-13", (byte) 0x44, "Soil Moisture 13", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE14("moisture-soil-channel-14", (byte) 0x46, "Soil Moisture 14", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE15("moisture-soil-channel-15", (byte) 0x48, "Soil Moisture 15", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    SOILMOISTURE16("moisture-soil-channel-16", (byte) 0x4A, "Soil Moisture 16", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    // will no longer be used
    // skip battery-level, since it is read via Command.CMD_READ_SENSOR_ID_NEW
    LOWBATT((byte) 0x4C, new Skip(1)),

    PM25_24HAVG1("air-quality-24-hour-average-channel-1", (byte) 0x4D, "PM2.5 Air Quality 24 hour average channel 1",
            MeasureType.PM25),

    PM25_24HAVG2("air-quality-24-hour-average-channel-2", (byte) 0x4E, "PM2.5 Air Quality 24 hour average channel 2",
            MeasureType.PM25),

    PM25_24HAVG3("air-quality-24-hour-average-channel-3", (byte) 0x4F, "PM2.5 Air Quality 24 hour average channel 3",
            MeasureType.PM25),

    PM25_24HAVG4("air-quality-24-hour-average-channel-4", (byte) 0x50, "PM2.5 Air Quality 24 hour average channel 4",
            MeasureType.PM25),

    PM25_CH1("air-quality-channel-1", (byte) 0x2A, "PM2.5 Air Quality channel 1", MeasureType.PM25),

    PM25_CH2("air-quality-channel-2", (byte) 0x51, "PM2.5 Air Quality channel 2", MeasureType.PM25),

    PM25_CH3("air-quality-channel-3", (byte) 0x52, "PM2.5 Air Quality channel 3", MeasureType.PM25),

    PM25_CH4("air-quality-channel-4", (byte) 0x53, "PM2.5 Air Quality channel 4", MeasureType.PM25),

    LEAK_CH1("water-leak-channel-1", (byte) 0x58, "Leak channel 1", MeasureType.WATER_LEAK_DETECTION),

    LEAK_CH2("water-leak-channel-2", (byte) 0x59, "Leak channel 2", MeasureType.WATER_LEAK_DETECTION),

    LEAK_CH3("water-leak-channel-3", (byte) 0x5A, "Leak channel 3", MeasureType.WATER_LEAK_DETECTION),

    LEAK_CH4("water-leak-channel-4", (byte) 0x5B, "Leak channel 4", MeasureType.WATER_LEAK_DETECTION),

    // `LIGHTNING` is the name in the spec, so we keep it here as it
    LIGHTNING("lightning-distance", (byte) 0x60, "lightning distance 1~40KM", MeasureType.LIGHTNING_DISTANCE),

    LIGHTNING_TIME("lightning-time", (byte) 0x61, "lightning happened time", MeasureType.LIGHTNING_TIME),

    // `LIGHTNING_POWER` is the name in the spec, so we keep it here as it
    LIGHTNING_POWER("lightning-counter", (byte) 0x62, "lightning counter for the day", MeasureType.LIGHTNING_COUNTER),

    TF_USR1("temperature-external-channel-1", (byte) 0x63, "Soil or Water temperature channel 1",
            MeasureType.TEMPERATURE),

    TF_USR2("temperature-external-channel-2", (byte) 0x64, "Soil or Water temperature channel 2",
            MeasureType.TEMPERATURE),

    TF_USR3("temperature-external-channel-3", (byte) 0x65, "Soil or Water temperature channel 3",
            MeasureType.TEMPERATURE),

    TF_USR4("temperature-external-channel-4", (byte) 0x66, "Soil or Water temperature channel 4",
            MeasureType.TEMPERATURE),

    TF_USR5("temperature-external-channel-5", (byte) 0x67, "Soil or Water temperature channel 5",
            MeasureType.TEMPERATURE),

    TF_USR6("temperature-external-channel-6", (byte) 0x68, "Soil or Water temperature channel 6",
            MeasureType.TEMPERATURE),

    TF_USR7("temperature-external-channel-7", (byte) 0x69, "Soil or Water temperature channel 7",
            MeasureType.TEMPERATURE),

    TF_USR8("temperature-external-channel-8", (byte) 0x6A, "Soil or Water temperature channel 8",
            MeasureType.TEMPERATURE),

    ITEM_SENSOR_CO2((byte) 0x70,
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

    ITEM_LEAF_WETNESS_CH1("leaf-wetness-channel-1", (byte) 0x72, "Leaf Moisture channel 1", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    ITEM_LEAF_WETNESS_CH2("leaf-wetness-channel-2", (byte) 0x73, "Leaf Moisture channel 2", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    ITEM_LEAF_WETNESS_CH3("leaf-wetness-channel-3", (byte) 0x74, "Leaf Moisture channel 3", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    ITEM_LEAF_WETNESS_CH4("leaf-wetness-channel-4", (byte) 0x75, "Leaf Moisture channel 4", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    ITEM_LEAF_WETNESS_CH5("leaf-wetness-channel-5", (byte) 0x76, "Leaf Moisture channel 5", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    ITEM_LEAF_WETNESS_CH6("leaf-wetness-channel-6", (byte) 0x77, "Leaf Moisture channel 6", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    ITEM_LEAF_WETNESS_CH7("leaf-wetness-channel-7", (byte) 0x78, "Leaf Moisture channel 7", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),

    ITEM_LEAF_WETNESS_CH8("leaf-wetness-channel-8", (byte) 0x79, "Leaf Moisture channel 8", MeasureType.PERCENTAGE,
            CHANNEL_TYPE_MOISTURE),;

    private static final Map<Byte, Measurand> MEASURANDS = new HashMap<>();

    static {
        for (Measurand value : values()) {
            MEASURANDS.put(value.code, value);
        }
    }

    private final byte code;
    private final Parser[] parsers;

    Measurand(String channelId, byte code, String name, MeasureType measureType) {
        this(channelId, code, name, measureType, null);
    }

    Measurand(String channelId, byte code, String name, MeasureType measureType,
            @Nullable ChannelTypeUID channelTypeUID) {
        this(code, new MeasurandParser(channelId, name, measureType, channelTypeUID));
    }

    Measurand(byte code, Parser... parsers) {
        this.code = code;
        this.parsers = parsers;
    }

    public static @Nullable Measurand getByCode(byte code) {
        return MEASURANDS.get(code);
    }

    public int extractMeasuredValues(byte[] data, int offset, ConversionContext context, List<MeasuredValue> result) {
        int subOffset = 0;
        for (Parser parser : parsers) {
            subOffset += parser.extractMeasuredValues(data, offset + subOffset, context, result);
        }
        return subOffset;
    }

    private interface Parser {
        int extractMeasuredValues(byte[] data, int offset, ConversionContext context, List<MeasuredValue> result);
    }

    private static class Skip implements Parser {
        private final int skip;

        public Skip(int skip) {
            this.skip = skip;
        }

        @Override
        public int extractMeasuredValues(byte[] data, int offset, ConversionContext context,
                List<MeasuredValue> result) {
            return skip;
        }
    }

    private static class MeasurandParser implements Parser {
        private final String name;
        private final String channelId;
        private final MeasureType measureType;
        private final @Nullable ChannelTypeUID channelTypeUID;

        MeasurandParser(String channelId, String name, MeasureType measureType) {
            this(channelId, name, measureType, null);
        }

        MeasurandParser(String channelId, String name, MeasureType measureType,
                @Nullable ChannelTypeUID channelTypeUID) {
            this.channelId = channelId;
            this.name = name;
            this.measureType = measureType;
            this.channelTypeUID = channelTypeUID == null ? measureType.getChannelTypeId() : channelTypeUID;
        }

        public int extractMeasuredValues(byte[] data, int offset, ConversionContext context,
                List<MeasuredValue> result) {
            State state = measureType.toState(data, offset, context);
            if (state != null) {
                result.add(new MeasuredValue(measureType, channelId, channelTypeUID, state, name));
            }
            return measureType.getByteSize();
        }
    }
}
