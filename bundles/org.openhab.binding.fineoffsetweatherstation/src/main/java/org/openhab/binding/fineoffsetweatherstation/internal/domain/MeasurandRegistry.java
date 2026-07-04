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
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.Measurand.measurand;
import static org.openhab.binding.fineoffsetweatherstation.internal.domain.Measurand.skip;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;

/**
 * Indexes all measurand definitions for lookup by TCP item code and by HTTP group + id.
 * Built once from the fluent {@link #builder()} DSL; reached via {@link #standard()}.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class MeasurandRegistry {

    private static @Nullable MeasurandRegistry standard;

    /**
     * TCP index: a binary live-data item {@code code} (one byte) to what it parses into - see {@link #tcpByCode}.
     */
    private final Map<Byte, CodeBinding> byCode;
    /**
     * Ecowitt HTTP API index: an {@link HttpGroup} to a map of normalized id/field to its {@link HttpBinding} - see
     * {@link #http}.
     */
    private final Map<HttpGroup, Map<String, HttpBinding>> byHttp;

    private MeasurandRegistry(Map<Byte, CodeBinding> byCode, Map<HttpGroup, Map<String, HttpBinding>> byHttp) {
        this.byCode = byCode;
        this.byHttp = byHttp;
    }

    public static synchronized MeasurandRegistry standard() {
        MeasurandRegistry result = standard;
        if (result == null) {
            result = defineStandard(builder()).build();
            standard = result;
        }
        return result;
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * Resolves a TCP live-data item code (as sent over the binary protocol and parsed by {@code FineOffsetDataParser})
     * to its {@link CodeBinding}, or {@code null} if the code is unknown.
     */
    public @Nullable CodeBinding tcpByCode(byte code) {
        return byCode.get(code);
    }

    /**
     * Resolves an entry of the Ecowitt {@code get_livedata_info} HTTP response (as parsed by {@code EcowittDataParser})
     * - identified by its {@link HttpGroup} and id/field - to its {@link HttpBinding}, or {@code null} if unknown. The
     * {@code id} is matched after passing through {@link #normalizeId}.
     */
    public @Nullable HttpBinding http(HttpGroup group, String id) {
        Map<String, HttpBinding> map = byHttp.get(group);
        return map == null ? null : map.get(normalizeId(id));
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

    private static Builder defineStandard(Builder b) {
        b.add(0x01,
                measurand("temperature-indoor", "Indoor Temperature", MeasureType.TEMPERATURE)
                        .channelType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_INDOOR_TEMPERATURE)
                        .http(HttpGroup.WH25, "intemp"));
        b.add(0x02,
                measurand("temperature-outdoor", "Outdoor Temperature", MeasureType.TEMPERATURE)
                        .channelType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE)
                        .http(HttpGroup.COMMON_LIST));
        b.add(0x03,
                measurand("temperature-dew-point", "Dew point", MeasureType.TEMPERATURE).http(HttpGroup.COMMON_LIST));
        b.add(0x04,
                measurand("temperature-wind-chill", "Wind chill", MeasureType.TEMPERATURE).http(HttpGroup.COMMON_LIST));
        b.add(0x05,
                measurand("temperature-heat-index", "Heat index", MeasureType.TEMPERATURE).http(HttpGroup.COMMON_LIST));
        b.add(0x06,
                measurand("humidity-indoor", "Indoor Humidity", MeasureType.PERCENTAGE).http(HttpGroup.WH25, "inhumi"));
        b.add(0x07,
                measurand("humidity-outdoor", "Outdoor Humidity", MeasureType.PERCENTAGE)
                        .channelType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_ATMOSPHERIC_HUMIDITY)
                        .http(HttpGroup.COMMON_LIST));
        b.add(0x08, measurand("pressure-absolute", "Absolutely pressure", MeasureType.PRESSURE).http(HttpGroup.WH25,
                "abs"));
        b.add(0x09,
                measurand("pressure-relative", "Relative pressure", MeasureType.PRESSURE)
                        .channelType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_BAROMETRIC_PRESSURE)
                        .http(HttpGroup.WH25, "rel"));
        b.add(0x0A,
                measurand("direction-wind", "Wind Direction", MeasureType.DEGREE)
                        .channelType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION)
                        .http(HttpGroup.COMMON_LIST));
        b.add(0x0B,
                measurand("speed-wind", "Wind Speed", MeasureType.SPEED)
                        .channelType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED)
                        .http(HttpGroup.COMMON_LIST));
        b.add(0x0C,
                measurand("speed-gust", "Gust Speed", MeasureType.SPEED)
                        .channelType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED)
                        .http(HttpGroup.COMMON_LIST));
        b.add(0x0D, measurand("rain-event", "Rain Event", MeasureType.HEIGHT).http(HttpGroup.RAIN)
                .customization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG));
        b.add(0x0E, measurand("rain-rate", "Rain Rate", MeasureType.HEIGHT_PER_HOUR).http(HttpGroup.RAIN)
                .customization(ParserCustomizationType.ELV, MeasureType.HEIGHT_PER_HOUR_BIG));
        b.add(0x0F, measurand("rain-hour", "Rain hour", MeasureType.HEIGHT).http(HttpGroup.RAIN)
                .customization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG));
        b.add(0x10,
                measurand("rain-day", "Rain Day", MeasureType.HEIGHT).http(HttpGroup.RAIN)
                        .customization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG)
                        .customization(ParserCustomizationType.RAIN_READING, MeasureType.HEIGHT_BIG));
        b.add(0x11,
                measurand("rain-week", "Rain Week", MeasureType.HEIGHT).http(HttpGroup.RAIN)
                        .customization(ParserCustomizationType.ELV, MeasureType.HEIGHT_BIG)
                        .customization(ParserCustomizationType.RAIN_READING, MeasureType.HEIGHT_BIG));
        b.add(0x12, measurand("rain-month", "Rain Month", MeasureType.HEIGHT_BIG).http(HttpGroup.RAIN));
        b.add(0x13, measurand("rain-year", "Rain Year", MeasureType.HEIGHT_BIG).http(HttpGroup.RAIN));
        b.add(0x14, measurand("rain-total", "Rain Totals", MeasureType.HEIGHT_BIG).http(HttpGroup.RAIN));
        b.add(0x15, measurand("illumination", "Light", MeasureType.LUX).http(HttpGroup.COMMON_LIST));
        b.add(0x16,
                measurand("irradiation-uv", "UV", MeasureType.MILLIWATT_PER_SQUARE_METRE).http(HttpGroup.COMMON_LIST));
        b.add(0x17, measurand("uv-index", "UV index", MeasureType.BYTE).channelType(CHANNEL_TYPE_UV_INDEX)
                .http(HttpGroup.COMMON_LIST));
        b.add(0x18, measurand("time", "Date and time", MeasureType.DATE_TIME2));
        b.add(0X19, measurand("wind-max-day", "Day max wind", MeasureType.SPEED)
                .channelType(CHANNEL_TYPE_MAX_WIND_SPEED).http(HttpGroup.COMMON_LIST));
        b.addChannels(new int[] { 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21 },
                measurand("temperature-channel", "Temperature", MeasureType.TEMPERATURE).http(HttpGroup.CH_AISLE,
                        "temp"));
        b.addChannels(new int[] { 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29 },
                measurand("humidity-channel", "Humidity", MeasureType.PERCENTAGE).http(HttpGroup.CH_AISLE, "humidity"));
        b.addChannels(new int[] { 0x2B, 0x2D, 0x2F, 0x31, 0x33, 0x35, 0x37, 0x39, 0x3B, 0x3D, 0x3F, 0x41, 0x43, 0x45,
                0x47, 0x49 }, measurand("temperature-soil-channel", "Soil Temperature", MeasureType.TEMPERATURE));
        b.addChannels(
                new int[] { 0x2C, 0x2E, 0x30, 0x32, 0x34, 0x36, 0x38, 0x3A, 0x3C, 0x3E, 0x40, 0x42, 0x44, 0x46, 0x48,
                        0x4A },
                measurand("moisture-soil-channel", "Soil Moisture", MeasureType.PERCENTAGE)
                        .channelType(CHANNEL_TYPE_MOISTURE).http(HttpGroup.CH_SOIL, "humidity"));
        b.skip(0x4C, 1);
        b.addChannels(new int[] { 0x4D, 0x4E, 0x4F, 0x50 }, measurand("air-quality-24-hour-average-channel",
                "PM2.5 Air Quality 24 hour average", MeasureType.PM25));
        b.addChannels(new int[] { 0x2A, 0x51, 0x52, 0x53 },
                measurand("air-quality-channel", "PM2.5 Air Quality", MeasureType.PM25).http(HttpGroup.CH_PM25,
                        "PM25"));
        b.addChannels(new int[] { 0x58, 0x59, 0x5A, 0x5B },
                measurand("water-leak-channel", "Leak", MeasureType.WATER_LEAK_DETECTION).http(HttpGroup.CH_LEAK,
                        "status"));
        b.add(0x60, measurand("lightning-distance", "Lightning distance 1~40KM", MeasureType.LIGHTNING_DISTANCE)
                .http(HttpGroup.LIGHTNING, "distance"));
        b.add(0x61, measurand("lightning-time", "Lightning happened time", MeasureType.LIGHTNING_TIME)
                .http(HttpGroup.LIGHTNING, "date"));
        b.add(0x62, measurand("lightning-counter", "lightning counter for the day", MeasureType.LIGHTNING_COUNTER)
                .http(HttpGroup.LIGHTNING, "count"));
        b.addChannels(new int[] { 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A },
                measurand("temperature-external-channel", "Soil or Water temperature", MeasureType.TEMPERATURE)
                        .http(HttpGroup.CH_TEMP, "temp"),
                // skip battery-level, since it is read via Command.CMD_READ_SENSOR_ID_NEW
                skip(1));
        b.addGroup(0x6B,
                measurand("sensor-co2-temperature", "Temperature (CO₂-Sensor)", MeasureType.TEMPERATURE)
                        .http(HttpGroup.CO2, "temp"),
                measurand("sensor-co2-humidity", "Humidity (CO₂-Sensor)", MeasureType.PERCENTAGE).http(HttpGroup.CO2,
                        "humidity"),
                measurand("sensor-co2-pm10", "PM10 Air Quality (CO₂-Sensor)", MeasureType.PM10).http(HttpGroup.CO2,
                        "PM10"),
                measurand("sensor-co2-pm10-24-hour-average", "PM10 Air Quality 24 hour average (CO₂-Sensor)",
                        MeasureType.PM10).http(HttpGroup.CO2, "PM10_24H"),
                measurand("sensor-co2-pm25", "PM2.5 Air Quality (CO₂-Sensor)", MeasureType.PM25).http(HttpGroup.CO2,
                        "PM25"),
                measurand("sensor-co2-pm25-24-hour-average", "PM2.5 Air Quality 24 hour average (CO₂-Sensor)",
                        MeasureType.PM25).http(HttpGroup.CO2, "PM25_24H"),
                measurand("sensor-co2-co2", "CO₂", MeasureType.CO2).http(HttpGroup.CO2, "CO2"),
                measurand("sensor-co2-co2-24-hour-average", "CO₂ 24 hour average", MeasureType.CO2).http(HttpGroup.CO2,
                        "CO2_24H"),
                // the battery status can only be retrieved here and not via Command.CMD_READ_SENSOR_ID_NEW, since WH46
                // is not listed in the sensor array
                measurand("sensor-co2-wh46-battery-level", "Battery Level WH46", MeasureType.BATTERY_LEVEL),
                measurand("sensor-co2-pm1", "PM1 Air Quality (CO₂-Sensor)", MeasureType.PM1).http(HttpGroup.CO2, "PM1"),
                measurand("sensor-co2-pm1-24-hour-average", "PM1 Air Quality 24 hour average (CO₂-Sensor)",
                        MeasureType.PM1).http(HttpGroup.CO2, "PM1_24H"),
                measurand("sensor-co2-pm4", "PM4 Air Quality (CO₂-Sensor)", MeasureType.PM4).http(HttpGroup.CO2, "PM4"),
                measurand("sensor-co2-pm4-24-hour-average", "PM4 Air Quality 24 hour average (CO₂-Sensor)",
                        MeasureType.PM4).http(HttpGroup.CO2, "PM4_24H"));
        b.add(0x6c, measurand("free-heap-size", "Free Heap Size", MeasureType.MEMORY)
                .channelType(CHANNEL_TYPE_FREE_HEAP_SIZE));
        b.add(0x6D,
                measurand("direction-wind-avg-10min", "Wind Direction (10-minute average)", MeasureType.DEGREE)
                        .channelType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION)
                        .http(HttpGroup.COMMON_LIST));
        b.addGroup(0x70, measurand("sensor-co2-temperature", "Temperature (CO₂-Sensor)", MeasureType.TEMPERATURE),
                measurand("sensor-co2-humidity", "Humidity (CO₂-Sensor)", MeasureType.PERCENTAGE),
                measurand("sensor-co2-pm10", "PM10 Air Quality (CO₂-Sensor)", MeasureType.PM10),
                measurand("sensor-co2-pm10-24-hour-average", "PM10 Air Quality 24 hour average (CO₂-Sensor)",
                        MeasureType.PM10),
                measurand("sensor-co2-pm25", "PM2.5 Air Quality (CO₂-Sensor)", MeasureType.PM25),
                measurand("sensor-co2-pm25-24-hour-average", "PM2.5 Air Quality 24 hour average (CO₂-Sensor)",
                        MeasureType.PM25),
                measurand("sensor-co2-co2", "CO₂", MeasureType.CO2),
                measurand("sensor-co2-co2-24-hour-average", "CO₂ 24 hour average", MeasureType.CO2),
                // skip battery-level, since it is read via Command.CMD_READ_SENSOR_ID_NEW
                skip(1));
        b.addChannels(new int[] { 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79 },
                measurand("leaf-wetness-channel", "Leaf Moisture", MeasureType.PERCENTAGE)
                        .channelType(CHANNEL_TYPE_MOISTURE).http(HttpGroup.CH_LEAF, "humidity"));
        b.skip(0x7a, 1);
        b.skip(0x7b, 1);
        b.add(0x80, measurand("piezo-rain-rate", "Rain Rate", MeasureType.HEIGHT_PER_HOUR).http(HttpGroup.PIEZO_RAIN,
                0x0E));
        b.add(0x81, measurand("piezo-rain-event", "Rain Event", MeasureType.HEIGHT).http(HttpGroup.PIEZO_RAIN, 0x0D));
        b.add(0x82, measurand("piezo-rain-hour", "Rain hour", MeasureType.HEIGHT).http(HttpGroup.PIEZO_RAIN, 0x0F));
        b.add(0x83, measurand("piezo-rain-day", "Rain Day", MeasureType.HEIGHT_BIG).http(HttpGroup.PIEZO_RAIN, 0x10));
        b.add(0x84, measurand("piezo-rain-week", "Rain Week", MeasureType.HEIGHT_BIG).http(HttpGroup.PIEZO_RAIN, 0x11));
        b.add(0x85,
                measurand("piezo-rain-month", "Rain Month", MeasureType.HEIGHT_BIG).http(HttpGroup.PIEZO_RAIN, 0x12));
        b.add(0x86, measurand("piezo-rain-year", "Rain Year", MeasureType.HEIGHT_BIG).http(HttpGroup.PIEZO_RAIN, 0x13));
        b.skip(0x87, 2 * 10);
        b.skip(0x88, 3);
        b.add(0xA1, measurand("temperature-black-globe", "Black Globe Temperature", MeasureType.TEMPERATURE)
                .http(HttpGroup.COMMON_LIST));
        b.add(0xA2, measurand("temperature-wet-bulb-globe", "Wet Bulb Globe Temperature", MeasureType.TEMPERATURE)
                .http(HttpGroup.COMMON_LIST));
        b.addHttpOnly(measurand("temperature-feels-like", "Feels like temperature", MeasureType.TEMPERATURE)
                .http(HttpGroup.COMMON_LIST, "3"));
        b.addHttpOnly(measurand("vapor-pressure-deficit", "Vapor Pressure Deficit", MeasureType.VAPOR_PRESSURE_DEFICIT)
                .http(HttpGroup.COMMON_LIST, "5"));
        // shares item code 0x15 with LIGHT; used when the gateway reports solar radiation in W/m² instead of lux
        b.addHttpOnly(measurand("irradiation-solar", "Solar irradiation", MeasureType.SOLAR_RADIATION)
                .httpAlt(HttpGroup.COMMON_LIST, 0x15));
        // rainfall over the last 24 hours; only reported via the HTTP API (no dedicated TCP item code)
        b.addHttpOnly(measurand("rain-24-hours", "Rainfall last 24 hours", MeasureType.HEIGHT_BIG).http(HttpGroup.RAIN,
                "0x7C"));
        b.addHttpOnly(measurand("piezo-rain-24-hours", "Rainfall last 24 hours", MeasureType.HEIGHT_BIG)
                .http(HttpGroup.PIEZO_RAIN, "0x7C"));
        b.addHttpOnly(measurand("piezo-rain-state", "Raining (piezo)", MeasureType.RAIN_STATE)
                .http(HttpGroup.PIEZO_RAIN, "srain_piezo"));
        return b;
    }

    /**
     * Collects measurand definitions one at a time and incrementally fills the TCP ({@code byCode}) and HTTP
     * ({@code byHttp}) indexes. Each {@code add*}/{@code skip} call registers a single definition (the relative call
     * order matters for HTTP alternates - see {@link #registerHttp}); {@link #build()} freezes the result.
     */
    @SuppressWarnings("UnusedReturnValue")
    static final class Builder {
        private final Map<Byte, CodeBinding> byCode = new HashMap<>();
        private final Map<HttpGroup, Map<String, HttpBinding>> byHttp = new HashMap<>();

        /**
         * Registers a single-channel measurand: the TCP item {@code code} resolves to {@code measurand} (no channel
         * index),
         * and - if {@code measurand} declares an HTTP source - it is indexed for the Ecowitt API under that code's key.
         */
        Builder add(int code, Measurand measurand) {
            byCode.put((byte) code, new CodeBinding(measurand, null, measurand.getChannelPrefix()));
            registerHttp(measurand, code);
            return this;
        }

        /**
         * Registers one measurand spread across several TCP item codes, one per sensor channel: {@code codes[i]}
         * resolves to {@code measurand} with channel {@code i+1}. The HTTP binding (if any) is registered once, keyed
         * via the
         * first code.
         */
        Builder addChannels(int[] codes, Measurand measurand) {
            for (int i = 0; i < codes.length; i++) {
                byCode.put((byte) codes[i], new CodeBinding(measurand, i + 1, measurand.getChannelPrefix()));
            }
            registerHttp(measurand, codes[0]);
            return this;
        }

        /**
         * Multi-channel measurand whose per-code payload is the {@code measurand} parser followed by fixed
         * {@code trailingSlots}
         * (e.g. a trailing {@link Skip}). For each code {@code i} the channel {@code i+1} is bound; the slots after
         * {@code measurand} ignore the channel. HTTP for {@code measurand} is registered once under the first code.
         */
        Builder addChannels(int[] codes, Measurand measurand, Parser... trailingSlots) {
            Parser[] slots = new Parser[trailingSlots.length + 1];
            slots[0] = measurand;
            System.arraycopy(trailingSlots, 0, slots, 1, trailingSlots.length);
            for (int i = 0; i < codes.length; i++) {
                byCode.put((byte) codes[i],
                        new CodeBinding(new MeasurandGroup(slots), i + 1, measurand.getChannelPrefix()));
            }
            registerHttp(measurand, codes[0]);
            return this;
        }

        /**
         * Registers a measurand that exists only in the Ecowitt HTTP API and has no TCP item code. {@code measurand}
         * must
         * therefore declare an explicit HTTP key or http-code (the TCP-code fallback is never available here).
         */
        Builder addHttpOnly(Measurand measurand) {
            registerHttp(measurand, null);
            return this;
        }

        /**
         * Registers a compound TCP item {@code code} whose payload is the ordered {@code slots} ({@link Measurand}s and
         * {@link Skip}s) parsed sequentially as one block. Each {@link Measurand} slot that declares an HTTP source is
         * indexed for the Ecowitt API individually.
         */
        Builder addGroup(int code, Parser... slots) {
            byCode.put((byte) code,
                    new CodeBinding(new MeasurandGroup(slots), null, "group 0x" + Integer.toHexString(code & 0xFF)));
            for (Parser slot : slots) {
                if (slot instanceof Measurand m) {
                    registerHttp(m, code);
                }
            }
            return this;
        }

        /**
         * Registers a TCP item {@code code} whose {@code bytes}-long payload is consumed and discarded - it produces no
         * channel and has no Ecowitt HTTP API representation (e.g. battery levels read elsewhere, gauge-type markers).
         */
        Builder skip(int code, int bytes) {
            byCode.put((byte) code, new CodeBinding(new Skip(bytes), null, "skipped"));
            return this;
        }

        /**
         * Indexes {@code measurand}'s HTTP source (if it has one) under its {@link HttpGroup} and {@link #normalizeId
         * normalized} key. An {@link HttpSource#isAlternate() alternate} source attaches to the primary already
         * registered for that key instead of replacing it (e.g. solar radiation in W/measurand² sharing item code
         * {@code 0x15}
         * with lux illumination), so the primary must be registered first. {@code tcpCode} supplies the key only when
         * the source declares neither an explicit key nor an explicit http-code.
         */
        void registerHttp(Measurand measurand, @Nullable Integer tcpCode) {
            HttpSource source = measurand.getHttpSource();
            if (source == null) {
                return;
            }
            Map<String, HttpBinding> bindings = Objects
                    .requireNonNull(byHttp.computeIfAbsent(source.getGroup(), g -> new HashMap<>()));
            String key = normalizeId(source.resolveKey(tcpCode));
            if (source.isAlternate()) {
                HttpBinding existing = bindings.get(key);
                if (existing != null) {
                    existing.setAlternate(measurand);
                    return;
                }
            }
            bindings.put(key, new HttpBinding(measurand, null));
        }

        /**
         * Freezes the collected TCP and HTTP indexes into an immutable {@link MeasurandRegistry}.
         */
        public MeasurandRegistry build() {
            Map<HttpGroup, Map<String, HttpBinding>> frozenHttp = new HashMap<>();
            byHttp.forEach((group, bindings) -> frozenHttp.put(group, Map.copyOf(bindings)));
            return new MeasurandRegistry(Map.copyOf(byCode), Map.copyOf(frozenHttp));
        }
    }
}
