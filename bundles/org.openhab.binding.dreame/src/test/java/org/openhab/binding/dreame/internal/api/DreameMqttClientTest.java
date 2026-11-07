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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;

@NonNullByDefault
class DreameMqttClientTest {
    @Test
    void bundledDreameRootCaCreatesTrustManagers() throws DreameCloudException {
        assertTrue(DreameMqttClient.createTrustManagers().length > 0);
    }

    @Test
    void rejectsWildcardSubscriptions() {
        assertThrows(IllegalArgumentException.class, () -> DreameMqttClient.validateConfiguration(
                new DreameMqttConfiguration("host", 8883, "client", "user", "secret", "/status/+/owner/#")));
    }

    @Test
    void connectsSubscribesAndStopsCoreConnection() throws DreameCloudException {
        MqttBrokerConnection connection = mock(MqttBrokerConnection.class);
        when(connection.start()).thenReturn(CompletableFuture.completedFuture(true));
        when(connection.subscribe(org.mockito.ArgumentMatchers.eq("device/topic"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(true));
        when(connection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);
        DreameMqttClient client = new DreameMqttClient(configuration(), List.of(), status -> {
        }, connection);

        client.connect();
        assertTrue(client.isConnected());
        client.close();
        client.close();

        verify(connection).addConnectionObserver(client);
        verify(connection).start();
        verify(connection).subscribe("device/topic", client);
        verify(connection).connectionState();
        verify(connection).removeConnectionObserver(client);
        verify(connection).stop();
        verifyNoMoreInteractions(connection);
        assertFalse(client.isConnected());
    }

    @Test
    void deliversOnlyValidMessagesFromDeviceTopic() {
        AtomicInteger updates = new AtomicInteger();
        MqttBrokerConnection connection = mock(MqttBrokerConnection.class);
        DreameMqttClient client = new DreameMqttClient(configuration(), List.of(DreameProperty.BATTERY_LEVEL),
                status -> updates.incrementAndGet(), connection);
        byte[] valid = """
                {"data":{"method":"properties_changed","params":[{"siid":3,"piid":1,"value":81}]}}
                """.getBytes(StandardCharsets.UTF_8);

        client.processMessage("another/topic", valid);
        client.processMessage("device/topic", new byte[0]);
        client.processMessage("device/topic", "not-json".getBytes(StandardCharsets.UTF_8));
        client.processMessage("device/topic", valid);

        assertEquals(1, updates.get());
    }

    @Test
    void disconnectedStateClearsConnectionStatus() throws DreameCloudException {
        MqttBrokerConnection connection = mock(MqttBrokerConnection.class);
        when(connection.start()).thenReturn(CompletableFuture.completedFuture(true));
        when(connection.subscribe(org.mockito.ArgumentMatchers.eq("device/topic"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(true));
        when(connection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);
        DreameMqttClient client = new DreameMqttClient(configuration(), List.of(), status -> {
        }, connection);
        client.connect();

        client.connectionStateChanged(MqttConnectionState.DISCONNECTED, new RuntimeException("private"));

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
}
