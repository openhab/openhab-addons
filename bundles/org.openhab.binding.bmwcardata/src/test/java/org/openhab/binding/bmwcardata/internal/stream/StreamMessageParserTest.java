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
package org.openhab.binding.bmwcardata.internal.stream;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.stream.StreamMessageParser.ParsedMessage;

import com.google.gson.JsonPrimitive;

/**
 * Tests the {@link StreamMessageParser} and the status parsing of the {@link BridgeStreamSource}.
 *
 * @author Mark Herwege - Initial contribution
 * @author Michael Weger - Adaptation to the BMW CarData binding
 */
@NonNullByDefault
class StreamMessageParserTest {

    private static final String VIN = "WBA12345678901234";
    private static final String ENVELOPE = """
            {"vin":"%s","entityId":"e","topic":"t","timestamp":"2026-10-22T10:00:00Z",
             "data":{"vehicle.vehicle.travelledDistance":{"value":12345,"unit":"km",
             "timestamp":"2026-10-22T09:59:00Z"}}}""";

    @Test
    void parsesEnvelope() {
        ParsedMessage message = StreamMessageParser.parse("gcid/" + VIN, bytes(ENVELOPE.formatted(VIN)));

        assertNotNull(message);
        assertEquals(VIN, message.vin());
        TelematicValue value = message.data().get("vehicle.vehicle.travelledDistance");
        assertNotNull(value);
        assertEquals(new JsonPrimitive(12345), value.value);
        assertEquals("km", value.unit);
        assertEquals("2026-10-22T09:59:00Z", value.timestamp);
    }

    @Test
    void takesVinFromTopicIfMissing() {
        String envelope = """
                {"data":{"vehicle.isMoving":{"value":true}}}""";

        // topic of the bridge: <prefix>raw/<vin>
        ParsedMessage message = StreamMessageParser.parse("bmw/raw/" + VIN, bytes(envelope));

        assertNotNull(message);
        assertEquals(VIN, message.vin());
    }

    @Test
    void rejectsMessagesWithoutData() {
        assertNull(StreamMessageParser.parse("bmw/raw/" + VIN, bytes("not json")));
        assertNull(StreamMessageParser.parse("bmw/raw/" + VIN, bytes("{\"vin\":\"" + VIN + "\"}")));
        assertNull(StreamMessageParser.parse("bmw/raw/" + VIN, bytes("{\"vin\":\"" + VIN + "\",\"data\":{}}")));
    }

    @Test
    void parsesBridgeStatus() {
        assertEquals(Boolean.TRUE, BridgeStreamSource.parseStatus(bytes("{\"connected\":true,\"timestamp\":1}")));
        assertEquals(Boolean.FALSE, BridgeStreamSource.parseStatus(bytes("{\"connected\":false}")));
        assertEquals(Boolean.TRUE, BridgeStreamSource.parseStatus(bytes("true")));
        assertNull(BridgeStreamSource.parseStatus(bytes("{\"other\":1}")));
    }

    @Test
    void normalizesTopicPrefix() {
        assertEquals("bmw/", BrokerSettings.normalizeTopicPrefix(""));
        assertEquals("car/", BrokerSettings.normalizeTopicPrefix(" car "));
        assertEquals("car/", BrokerSettings.normalizeTopicPrefix("car/"));
        BrokerSettings settings = new BrokerSettings("localhost", 1883, false, "", "", "car/");
        assertEquals("car/raw/#", settings.rawTopicFilter());
        assertEquals("car/raw/" + VIN, settings.rawTopic(VIN));
        assertEquals("car/status", settings.statusTopic());
    }

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
