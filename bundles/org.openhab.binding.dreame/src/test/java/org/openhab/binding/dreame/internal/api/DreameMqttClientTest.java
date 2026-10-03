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
package org.openhab.binding.dreame.internal.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameProperty;

@NonNullByDefault
class DreameMqttClientTest {

    @Test
    void bundledDreameRootCaCreatesSocketFactory() throws MqttException {
        assertNotNull(DreameMqttClient.createSocketFactory());
    }

    @Test
    void connectionOptionsEnableSecureAutomaticReconnect() throws MqttException {
        DreameMqttConfiguration configuration = configuration();

        MqttConnectOptions options = DreameMqttClient.createConnectOptions(configuration);

        assertTrue(options.isCleanSession());
        assertTrue(options.isAutomaticReconnect());
        assertTrue(options.isHttpsHostnameVerificationEnabled());
        assertEquals(15, options.getConnectionTimeout());
        assertEquals(50, options.getKeepAliveInterval());
        assertEquals("mqtt-user", options.getUserName());
        assertArrayEquals("mqtt-password".toCharArray(), options.getPassword());
        assertNotNull(options.getSocketFactory());
    }

    @Test
    void validPropertyMessageIsDeliveredToListener() {
        AtomicInteger updates = new AtomicInteger();
        DreameMqttClient client = new DreameMqttClient(configuration(), List.of(DreameProperty.BATTERY_LEVEL),
                status -> updates.incrementAndGet());
        String payload = """
                {"data":{"method":"properties_changed","params":[{"siid":3,"piid":1,"value":81}]}}
                """;

        client.messageArrived("device/topic", message(payload));

        assertEquals(1, updates.get());
    }

    @Test
    void emptyUnsupportedAndInvalidMessagesAreIgnored() {
        AtomicInteger updates = new AtomicInteger();
        DreameMqttClient client = new DreameMqttClient(configuration(), List.of(DreameProperty.BATTERY_LEVEL),
                status -> updates.incrementAndGet());

        client.messageArrived(null, null);
        client.messageArrived("device/topic", new MqttMessage());
        client.messageArrived("device/topic", message("{\"data\":{\"method\":\"event\"}}"));
        client.messageArrived("device/topic", message("not-json"));

        assertEquals(0, updates.get());
    }

    @Test
    void closeWithoutConnectionIsIdempotent() {
        DreameMqttClient client = new DreameMqttClient(configuration(), List.of(), status -> {
        });

        client.close();
        client.close();

        assertFalse(client.isConnected());
    }

    private static DreameMqttConfiguration configuration() {
        return new DreameMqttConfiguration("mqtt.example.org", 8883, "mqtt-client", "mqtt-user", "mqtt-password",
                "device/topic");
    }

    private static MqttMessage message(String payload) {
        return new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
    }
}
