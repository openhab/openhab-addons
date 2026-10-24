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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.bmwcardata.internal.api.Tokens;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.config.AccountConfiguration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttWillAndTestament;

/**
 * Tests the {@link StreamMirror}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class StreamMirrorTest {

    private static final String VIN = "WBA12345678901234";
    private static final Instant NOW = Instant.parse("2026-10-23T10:00:00Z");
    private static final BrokerSettings SETTINGS = new BrokerSettings("localhost", 1883, false, "", "", "bmw/");
    private static final byte[] MESSAGE = ("{\"vin\":\"" + VIN
            + "\",\"data\":{\"vehicle.isMoving\":{\"value\":true,\"timestamp\":\"2026-10-23T09:59:00Z\"}}}")
            .getBytes(StandardCharsets.UTF_8);

    private @NonNullByDefault({}) MqttBrokerConnection connection;
    private @NonNullByDefault({}) Consumer<Boolean> brokerStateListener;
    private @NonNullByDefault({}) StreamMirror mirror;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        connection = mock(MqttBrokerConnection.class);
        when(connection.publish(anyString(), any(byte[].class), anyInt(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(true));
        when(connection.stop()).thenReturn(CompletableFuture.completedFuture(true));
        brokerStateListener = mock(Consumer.class);
        mirror = new StreamMirror(SETTINGS, connection, Clock.fixed(NOW, ZoneOffset.UTC), brokerStateListener);
    }

    @Test
    void setsRetainedLastWill() {
        ArgumentCaptor<MqttWillAndTestament> will = ArgumentCaptor.forClass(MqttWillAndTestament.class);
        verify(connection).setLastWill(will.capture());

        assertEquals("bmw/status", will.getValue().getTopic());
        assertEquals("{\"connected\":false}",
                new String(Objects.requireNonNull(will.getValue().getPayload()), StandardCharsets.UTF_8));
        assertTrue(will.getValue().isRetain());
    }

    @Test
    void publishesStreamMessagesUnmodifiedAndRetained() {
        when(connection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);

        mirror.publishMessage("gcid-1/" + VIN, MESSAGE);

        // the same bytes as received, not re-serialized
        verify(connection).publish(eq("bmw/raw/" + VIN), ArgumentMatchers.same(MESSAGE), eq(StreamMirror.QOS),
                eq(true));
    }

    @Test
    void publishesStreamStateRetained() {
        when(connection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);

        // the last will of a previous connection is replaced on connect
        mirror.connectionStateChanged(MqttConnectionState.CONNECTED, null);
        verify(connection).publish("bmw/status", statusPayload(false), StreamMirror.QOS, true);
        verify(brokerStateListener).accept(true);

        mirror.publishStreamState(true);
        verify(connection).publish("bmw/status", statusPayload(true), StreamMirror.QOS, true);
    }

    @Test
    void dropsMessagesWhileBrokerIsDisconnected() {
        when(connection.connectionState()).thenReturn(MqttConnectionState.DISCONNECTED);

        mirror.publishMessage("gcid-1/" + VIN, MESSAGE);
        mirror.publishStreamState(true);
        mirror.connectionStateChanged(MqttConnectionState.DISCONNECTED, null);

        verify(connection, never()).publish(anyString(), any(byte[].class), anyInt(), anyBoolean());
        verify(brokerStateListener).accept(false);

        // the state is published once the broker is back
        when(connection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);
        mirror.connectionStateChanged(MqttConnectionState.CONNECTED, null);
        verify(connection).publish("bmw/status", statusPayload(true), StreamMirror.QOS, true);
    }

    @Test
    void reportsDisconnectBeforeStopping() {
        when(connection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);

        mirror.stop().join();

        verify(connection).publish("bmw/status", StreamMirror.DISCONNECTED_PAYLOAD, StreamMirror.QOS, true);
        verify(connection).stop();
    }

    @Test
    void mirroringIsIgnoredInBridgeMode() {
        AccountConfiguration config = new AccountConfiguration();
        config.mirrorToBroker = true;
        assertTrue(config.isMirrorActive());

        config.streamMode = AccountConfiguration.STREAM_MODE_BRIDGE;
        assertFalse(config.isMirrorActive());

        config.streamMode = AccountConfiguration.STREAM_MODE_DIRECT;
        config.streaming = false;
        assertFalse(config.isMirrorActive());
    }

    @Test
    @SuppressWarnings("unchecked")
    void streamIsProcessedWhileBrokerIsUnreachable() throws Exception {
        when(connection.connectionState()).thenReturn(MqttConnectionState.DISCONNECTED);
        StreamListener listener = mock(StreamListener.class);
        Tokens tokens = new Tokens("access", "refresh", "id", "gcid-1", NOW.plusSeconds(3600));
        BMWCarDataStreamClient client = new BMWCarDataStreamClient(tokens, mock(ScheduledExecutorService.class),
                listener, mirror::publishMessage);

        client.processMessage("gcid-1/" + VIN, MESSAGE);

        ArgumentCaptor<Map<String, TelematicValue>> data = ArgumentCaptor.forClass(Map.class);
        verify(listener).onStreamData(eq(VIN), data.capture());
        assertTrue(data.getValue().containsKey("vehicle.isMoving"));

        // also a failing mirror does not stop the stream
        BMWCarDataStreamClient failing = new BMWCarDataStreamClient(tokens, mock(ScheduledExecutorService.class),
                listener, (topic, payload) -> {
                    throw new IllegalStateException("broken");
                });
        failing.processMessage("gcid-1/" + VIN, MESSAGE);
        verify(listener, times(2)).onStreamData(eq(VIN), any());
    }

    private static byte[] statusPayload(boolean connected) {
        return ("{\"connected\":" + connected + ",\"timestamp\":" + NOW.getEpochSecond() + "}")
                .getBytes(StandardCharsets.UTF_8);
    }
}
