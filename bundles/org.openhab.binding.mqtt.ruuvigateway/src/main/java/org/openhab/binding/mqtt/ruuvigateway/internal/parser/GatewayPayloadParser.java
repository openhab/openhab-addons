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
package org.openhab.binding.mqtt.ruuvigateway.internal.parser;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import fi.tkgwf.ruuvi.common.bean.RuuviMeasurement;
import fi.tkgwf.ruuvi.common.parser.impl.AnyDataFormatParser;

/**
 * The {@link GatewayPayloadParser} is responsible for parsing Ruuvi Gateway MQTT JSON payloads.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class GatewayPayloadParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayPayloadParser.class);
    private static final Gson GSON = new GsonBuilder().create();
    private static final AnyDataFormatParser parser = new AnyDataFormatParser();
    private static final Predicate<String> HEX_PATTERN_CHECKER = Pattern.compile("^([0-9A-Fa-f]{2})+$")
            .asMatchPredicate();

    /**
     * JSON MQTT payload sent by Ruuvi Gateway
     *
     * See https://docs.ruuvi.com/gw-data-formats/mqtt-time-stamped-data-from-bluetooth-sensors
     *
     * @author Sami Salonen - Initial contribution
     *
     */
    public static class GatewayPayload {
        /**
         * MAC-address of Ruuvi Gateway
         */
        public Optional<String> gwMac = Optional.empty();
        /**
         * RSSI
         */
        public int rssi;
        /**
         * Timestamp when the message from Bluetooth-sensor was relayed by Gateway
         *
         */
        public Optional<Instant> gwts = Optional.empty();

        /**
         * Timestamp (Unix-time) when the message from Bluetooth-sensor was received by Gateway
         *
         */
        public Optional<Instant> ts = Optional.empty();
        public RuuviMeasurement measurement;

        private GatewayPayload(GatewayPayloadIntermediate intermediate) throws IllegalArgumentException {
            String gwMac = intermediate.gw_mac;
            if (gwMac == null) {
                LOGGER.trace("Missing mandatory field 'gw_mac', ignoring");
            }
            this.gwMac = Optional.ofNullable(gwMac);
            rssi = intermediate.rssi;
            try {
                gwts = Optional.of(Instant.ofEpochSecond(intermediate.gwts));
            } catch (DateTimeException e) {
                LOGGER.debug("Field 'gwts' is a not valid time (epoch second), ignoring: {}", intermediate.gwts);
            }
            try {
                ts = Optional.of(Instant.ofEpochSecond(intermediate.ts));
            } catch (DateTimeException e) {
                LOGGER.debug("Field 'ts' is a not valid time (epoch second), ignoring: {}", intermediate.ts);
            }

            String localData = intermediate.data;
            if (localData == null) {
                throw new IllegalArgumentException("Missing mandatory field 'data'");
            }

            if (!HEX_PATTERN_CHECKER.test(localData)) {
                LOGGER.debug(
                        "Data is not representing manufacturer specific bluetooth advertisement, it is not valid hex: {}",
                        localData);
                throw new IllegalArgumentException(
                        "Data is not representing manufacturer specific bluetooth advertisement, it is not valid hex: "
                                + localData);
            }
            byte[] bytes = HexUtils.hexToBytes(localData);
            if (bytes.length < 4) {
                // Minimum: [AD_Len] [0xFF Type] [Company_ID_LowByte] [Company_ID_HighByte]
                throw new IllegalArgumentException("Advertisement data is too short");
            }

            // Dynamically find the 0xFF (Manufacturer Specific Data) AD Type marker
            // This handles advertisements with or without optional Flags AD structure
            // Supports both classic (max 31 bytes per AD) and extended advertisements (max 255 bytes per AD)
            // Format 5 typically: [02 01 06] [1B FF 99 04 05 ...] where 0xFF is at index 4
            // Format E1 typically: [2B FF 99 04 E1 ...] where 0xFF is at index 1 (manufacturer-specific data AD
            // structure with length 0x2B=43)
            int manufacturerIndex = -1;
            for (int i = 1; i < bytes.length; i++) {
                if ((bytes[i] & 0xff) == 0xff) {
                    // Found potential 0xFF AD Type marker
                    // Verify previous byte looks like a valid AD Length (1-255 bytes for extended advertisements)
                    if (bytes[i - 1] > 0) {
                        // Verify we have enough data: type byte + company ID (2 bytes) + at least 1 data byte
                        if (i + 3 <= bytes.length) {
                            manufacturerIndex = i;
                            break;
                        }
                    }
                }
            }

            if (manufacturerIndex < 0) {
                LOGGER.debug("Data is not representing manufacturer specific bluetooth advertisement: {}",
                        HexUtils.bytesToHex(bytes));
                throw new IllegalArgumentException(
                        "Data is not representing manufacturer specific bluetooth advertisement");
            }

            // Manufacturer data starts after 0xFF type byte (but includes company ID)
            // Parser expects: [Company_ID_2bytes] [Data_Format] [Rest of data...]
            // Example: [2B FF 99 04 E1 ...] where index 1 is 0xFF, so data starts at index 2 (99 04 E1...)
            byte[] manufacturerData = Arrays.copyOfRange(bytes, manufacturerIndex + 1, bytes.length);
            LOGGER.debug("Found 0xFF manufacturer type at index {}, extracting data from index {}: {}",
                    manufacturerIndex, manufacturerIndex + 1, HexUtils.bytesToHex(manufacturerData));
            RuuviMeasurement localManufacturerData = parser.parse(manufacturerData);
            if (localManufacturerData == null) {
                LOGGER.debug("Failed to parse manufacturer data: {}. Available parsers may not recognize this format.",
                        HexUtils.bytesToHex(manufacturerData));
                throw new IllegalArgumentException("Manufacturer data is not valid");
            }
            measurement = localManufacturerData;
        }
    }

    /**
     *
     * JSON MQTT payload sent by Ruuvi Gateway (intermediate representation).
     *
     * This intermediate representation tries to match the low level JSON, making little data validation and conversion.
     *
     * Fields are descibed in https://docs.ruuvi.com/gw-data-formats/mqtt-time-stamped-data-from-bluetooth-sensors
     *
     * Fields are marked as nullable as GSON might apply nulls at runtime.
     *
     * @author Sami Salonen - Initial Contribution
     * @see GatewayPayload Equivalent of this class but with additional data validation and typing
     *
     */
    private static class GatewayPayloadIntermediate {
        public @Nullable String gw_mac;
        public int rssi;
        public long gwts;
        public long ts;
        public @Nullable String data;
    }

    /**
     * Parse MQTT JSON payload advertised by Ruuvi Gateway
     *
     * @param jsonPayload json payload of the Ruuvi sensor MQTT topic, as bytes
     * @return parsed payload
     * @throws JsonSyntaxException raised with JSON syntax exceptions and clearly invalid JSON types
     * @throws IllegalArgumentException raised with invalid or unparseable data
     */
    public static GatewayPayload parse(byte[] jsonPayload) throws JsonSyntaxException, IllegalArgumentException {
        String jsonPayloadString = new String(jsonPayload, StandardCharsets.UTF_8);
        GatewayPayloadIntermediate payloadIntermediate = GSON.fromJson(jsonPayloadString,
                GatewayPayloadIntermediate.class);
        if (payloadIntermediate == null) {
            throw new JsonSyntaxException("JSON parsing failed");
        }
        return new GatewayPayload(payloadIntermediate);
    }
}
