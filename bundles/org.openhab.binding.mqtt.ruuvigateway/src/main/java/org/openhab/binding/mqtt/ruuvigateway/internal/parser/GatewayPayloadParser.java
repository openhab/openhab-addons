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

    private static final Logger logger = LoggerFactory.getLogger(GatewayPayloadParser.class);
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

        private GatewayPayload(GatewayPayloadIntermediate intermediate) {
            String gwMac = intermediate.gw_mac;
            if (gwMac == null) {
                logger.trace("Missing mandatory field 'gw_mac', ignoring");
            }
            this.gwMac = Optional.ofNullable(gwMac);
            rssi = intermediate.rssi;
            try {
                gwts = Optional.of(Instant.ofEpochSecond(intermediate.gwts));
            } catch (DateTimeException e) {
                logger.trace("Field 'gwts' is a not valid time (epoch second), ignoring: {}", intermediate.gwts);
            }
            try {
                ts = Optional.of(Instant.ofEpochSecond(intermediate.ts));
            } catch (DateTimeException e) {
                logger.trace("Field 'ts' is a not valid time (epoch second), ignoring: {}", intermediate.ts);
            }

            String localData = intermediate.data;
            if (localData == null) {
                throw new JsonSyntaxException("Missing mandatory field 'data'");
            }

            if (!HEX_PATTERN_CHECKER.test(localData)) {
                logger.trace("Data is not representing manufacturer specific bluetooth advertisement: " + localData);
                throw new JsonSyntaxException("Data is not a valid hex pattern: " + localData);
            }
            byte[] bytes = HexUtils.hexToBytes(localData);
            if (bytes.length < 4) {
                // We want at least 4 bytes, ensuring bytes[4] is valid as well as Arrays.copyOfRange(bytes, 5, ...)
                // below
                // The payload length (might depend on format version ) is validated by parser.parse call
                throw new JsonSyntaxException("Manufacturerer data is too short");

            }
            if ((bytes[4] & 0xff) != 0xff) {
                logger.trace("Data is not representing manufacturer specific bluetooth advertisement: "
                        + HexUtils.bytesToHex(bytes));
                throw new JsonSyntaxException("Data is not representing manufacturer specific bluetooth advertisement");
            }
            // Manufacturer data starts after 0xFF byte, at index 5
            byte[] manufacturerData = Arrays.copyOfRange(bytes, 5, bytes.length);
            RuuviMeasurement localManufacturerData = parser.parse(manufacturerData);
            if (localManufacturerData == null) {
                logger.trace("Manufacturer data is not valid: " + HexUtils.bytesToHex(manufacturerData));
                throw new JsonSyntaxException("Manufacturer data is not valid");
            }
            measurement = localManufacturerData;
        }
    }

    /**
     *
     * JSON MQTT payload sent by Ruuvi Gateway (intermediate representation).
     *
     * This intermediate representation does not have all the typic yet set.
     *
     * Fields are descibed in https://docs.ruuvi.com/gw-data-formats/mqtt-time-stamped-data-from-bluetooth-sensors
     *
     * Fields are marked as nullable as GSON might apply nulls at runtime.
     *
     * @author Sami Salonen - Initial Contribution
     *
     */
    private static class GatewayPayloadIntermediate {
        public @Nullable String gw_mac;
        public int rssi;
        public long gwts;
        public long ts;
        public @Nullable String data;
    }

    public static GatewayPayload parse(byte[] jsonPayload) throws JsonSyntaxException {
        String jsonPayloadString = new String(jsonPayload, StandardCharsets.UTF_8);
        GatewayPayloadIntermediate payloadIntermediate = GSON.fromJson(jsonPayloadString,
                GatewayPayloadIntermediate.class);
        if (payloadIntermediate == null) {
            throw new JsonSyntaxException("JSON parsing failed");
        }
        GatewayPayload payload = new GatewayPayload(payloadIntermediate);
        return payload;
    }
}
