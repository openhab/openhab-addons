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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;

@NonNullByDefault
class DreameVacuumMqttClientTest {
    @Test
    void subscribesOnceAndStopsCoreConnection() {
        MqttBrokerConnection connection = mock(MqttBrokerConnection.class);
        when(connection.start()).thenReturn(CompletableFuture.completedFuture(true));
        when(connection.subscribe(org.mockito.ArgumentMatchers.eq("device/topic"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(true));
        when(connection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);
        List<String> diagnostics = new ArrayList<>();
        DreameVacuumMqttClient client = client(connection, diagnostics::add, properties -> {
        });

        client.connect();
        client.connect();

        assertEquals(List.of("subscribed (device connectivity not established)"), diagnostics);
        assertTrue(client.isSubscribed());
        client.close();
        client.close();
        verify(connection).addConnectionObserver(client);
        verify(connection).start();
        verify(connection).subscribe("device/topic", client);
        verify(connection).connectionState();
        verify(connection).removeConnectionObserver(client);
        verify(connection).stop();
        verifyNoMoreInteractions(connection);
    }

    @Test
    void lateConnectionCompletionDoesNotSubscribeAfterClose() {
        MqttBrokerConnection connection = mock(MqttBrokerConnection.class);
        CompletableFuture<Boolean> start = new CompletableFuture<>();
        when(connection.start()).thenReturn(start);
        List<String> diagnostics = new ArrayList<>();
        DreameVacuumMqttClient client = client(connection, diagnostics::add, properties -> {
        });

        client.connect();
        client.close();
        start.complete(true);

        assertTrue(diagnostics.isEmpty());
        verify(connection).addConnectionObserver(client);
        verify(connection).start();
        verify(connection).removeConnectionObserver(client);
        verify(connection).stop();
        verifyNoMoreInteractions(connection);
    }

    @Test
    void rejectionAndDisconnectUseSafeDiagnostics() {
        MqttBrokerConnection connection = mock(MqttBrokerConnection.class);
        when(connection.start()).thenReturn(CompletableFuture.completedFuture(false));
        List<String> diagnostics = new ArrayList<>();
        DreameVacuumMqttClient client = client(connection, diagnostics::add, properties -> {
        });

        client.connect();
        client.connectionStateChanged(MqttConnectionState.DISCONNECTED,
                new RuntimeException("private-host secret-token"));

        assertFalse(client.isSubscribed());
        assertEquals(List.of("subscribe-rejected", "connection-lost"), diagnostics);
    }

    @Test
    void forwardsPropertiesOnlyForMatchingTopicBeforeClose() {
        MqttBrokerConnection connection = mock(MqttBrokerConnection.class);
        List<Map<String, Integer>> received = new ArrayList<>();
        DreameVacuumMqttClient client = client(connection, diagnostic -> {
        }, received::add);
        byte[] message = """
                {"method":"properties_changed","params":[{"siid":3,"piid":1,"value":96}]}
                """.getBytes(StandardCharsets.UTF_8);

        client.processMessage("another/device", message);
        assertTrue(received.isEmpty());
        client.processMessage("device/topic", message);
        assertEquals(List.of(Map.of("3/1", 96)), received);
        client.close();
        client.processMessage("device/topic", message);
        assertEquals(1, received.size());
    }

    private static DreameVacuumMqttClient client(MqttBrokerConnection connection, Consumer<String> diagnostics,
            Consumer<Map<String, Integer>> properties) {
        return new DreameVacuumMqttClient(configuration(), "dreame.vacuum.r9445d", diagnostics, properties, connection);
    }

    private static DreameMqttConfiguration configuration() {
        return new DreameMqttConfiguration("host", 8883, "client", "user", "secret", "device/topic");
    }
}
