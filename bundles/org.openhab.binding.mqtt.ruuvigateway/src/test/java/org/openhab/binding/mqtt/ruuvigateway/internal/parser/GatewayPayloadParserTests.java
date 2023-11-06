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
package org.openhab.binding.mqtt.ruuvigateway.internal.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.ruuvigateway.internal.parser.GatewayPayloadParser.GatewayPayload;

import com.google.gson.JsonSyntaxException;

/**
 * Tests for {@link GatewayPayloadParser}
 *
 * @author Sami Salonen - Initial Contribution
 */
@NonNullByDefault
public class GatewayPayloadParserTests {

    private byte[] bytes(String str) {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(str);
        buffer.rewind();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Test with valid data.
     *
     * See 'valid case' test vector from
     * https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-5-rawv2
     */
    @Test
    public void testValid() {
        GatewayPayload parsed = GatewayPayloadParser.parse(bytes(//
                "{\"gw_mac\": \"DE:AD:BE:EF:00:00\","//
                        + "  \"rssi\": -83,"//
                        + "  \"aoa\": [],"//
                        + "  \"gwts\": \"1659365438\","//
                        + "  \"ts\": \"1659365439\","//
                        + "  \"data\": \"0201061BFF99040512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F\","//
                        + "  \"coords\": \"\"" + "}"));
        assertNotNull(parsed);
        assertEquals(-83, parsed.rssi);
        assertEquals(Optional.of(Instant.ofEpochSecond(1659365438)), parsed.gwts);
        assertEquals(Optional.of(Instant.ofEpochSecond(1659365439)), parsed.ts);
        assertEquals(24.3, parsed.measurement.getTemperature());
        assertEquals(100044, parsed.measurement.getPressure());
        assertEquals(5, parsed.measurement.getDataFormat());
        assertEquals(53.49, parsed.measurement.getHumidity());
        assertEquals(0.004, parsed.measurement.getAccelerationX());
        assertEquals(-0.004, parsed.measurement.getAccelerationY());
        assertEquals(1.036, parsed.measurement.getAccelerationZ());
        assertEquals(4, parsed.measurement.getTxPower());
        assertEquals(2.9770000000000003, parsed.measurement.getBatteryVoltage());
        assertEquals(66, parsed.measurement.getMovementCounter());
        assertEquals(205, parsed.measurement.getMeasurementSequenceNumber());
    }

    @Test
    public void testInvalidJSON() {
        assertThrows(JsonSyntaxException.class, () -> {
            GatewayPayloadParser.parse(bytes(//
                    "invalid json"));
        });
    }

    @Test
    public void testUnexpectedTypes() {
        assertThrows(IllegalArgumentException.class, () -> {
            GatewayPayloadParser.parse(bytes(//
                    "{\"gw_mac\": \"DE:AD:BE:EF:00:00\","//
                            + "  \"rssi\": -83,"//
                            + "  \"aoa\": [],"//
                            + "  \"gwts\": \"1659365438\","//
                            + "  \"ts\": \"1659365438\","//
                            + "  \"data\": 666," // should be hex-string of even length
                            + "  \"coords\": \"\"" + "}"));
        });
    }

    @Test
    public void testInvalidHex() {
        assertThrows(IllegalArgumentException.class, () -> {
            GatewayPayloadParser.parse(bytes(//
                    "{\"gw_mac\": \"DE:AD:BE:EF:00:00\","//
                            + "  \"rssi\": -83,"//
                            + "  \"aoa\": [],"//
                            + "  \"gwts\": \"1659365438\","//
                            + "  \"ts\": \"1659365438\","//
                            + "  \"data\": \"XYZZ\"," // should be hex string
                            + "  \"coords\": \"\"" + "}"));
        });
    }

    @Test
    public void testUnexpectedTypes3() {
        assertThrows(JsonSyntaxException.class, () -> {
            GatewayPayloadParser.parse(bytes(//
                    """
                            {"gw_mac": "DE:AD:BE:EF:00:00",\
                              "rssi": "foobar",\
                              "aoa": [],\
                              "gwts": "1659365438",\
                              "ts": "1659365438",\
                              "data": "0201061BFF99040512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F",\
                              "coords": ""\
                            }\
                            """));
        });
    }

    @Test
    public void testDataTooShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            GatewayPayloadParser.parse(bytes(//
                    "{\"gw_mac\": \"DE:AD:BE:EF:00:00\","//
                            + "  \"rssi\": -83," + "  \"aoa\": [],"//
                            + "  \"gwts\": \"1659365438\","//
                            + "  \"ts\": \"1659365438\","//
                            + "  \"data\": \"0201061BFF990405\"," // too short
                            + "  \"coords\": \"\"" + "}"));
        });
    }

    @Test
    public void testUnexpectedManufacturer() {
        assertThrows(IllegalArgumentException.class, () -> {
            GatewayPayloadParser.parse(bytes(//
                    """
                            {"gw_mac": "DE:AD:BE:EF:00:00",\
                              "rssi": -83,\
                              "aoa": [],\
                              "gwts": "1659365438",\
                              "ts": "1659365438",\
                              "data": "0201061BFF99990512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F",\
                              "coords": ""\
                            }\
                            """));
        });
    }

    @Test
    public void testDataNotBluetoothAdvertisement() {
        assertThrows(IllegalArgumentException.class, () -> {
            GatewayPayloadParser.parse(bytes(//
                    """
                            {"gw_mac": "DE:AD:BE:EF:00:00",\
                              "rssi": -83,\
                              "aoa": [],\
                              "gwts": "1659365438",\
                              "ts": "1659365438",\
                              "data": "0201061BAA99040512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F",\
                              "coords": ""\
                            }\
                            """));
        });
    }
}
