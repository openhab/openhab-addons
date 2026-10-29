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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.paho.client.mqttv3.MqttClient;
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
        client.messageArrived("device/topic", message(
                "{\"data\":{\"method\":\"properties_changed\",\"params\":[{\"siid\":null,\"piid\":1,\"value\":1}]}}"));

        assertEquals(0, updates.get());
    }

    @Test
    void closeReleasesClientAfterDisconnectFailure() throws MqttException {
        MqttClient mqttClient = mock(MqttClient.class);
        doThrow(new MqttException(MqttException.REASON_CODE_CLIENT_EXCEPTION)).when(mqttClient).disconnectForcibly(0,
                1000, false);
        DreameMqttClient client = new DreameMqttClient(configuration(), List.of(), status -> {
        }, mqttClient);

        client.close();

        verify(mqttClient).close(true);
        assertFalse(client.isConnected());
    }

    @Test
    void closeWithoutConnectionIsIdempotent() {
        DreameMqttClient client = new DreameMqttClient(configuration(), List.of(), status -> {
        });

        client.close();
        client.close();

        assertFalse(client.isConnected());
    }

    @Test
    void configurationToStringRedactsSensitiveFields() {
        String value = configuration().toString();

        assertFalse(value.contains("mqtt-password"));
        assertFalse(value.contains("mqtt-user"));
        assertFalse(value.contains("mqtt-client"));
        assertFalse(value.contains("device/topic"));
        assertTrue(value.contains("password=***"));
    }

    private static DreameMqttConfiguration configuration() {
        return new DreameMqttConfiguration("mqtt.example.org", 8883, "mqtt-client", "mqtt-user", "mqtt-password",
                "device/topic");
    }

    private static MqttMessage message(String payload) {
        return new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
    }
}
