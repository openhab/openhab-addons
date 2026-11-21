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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;

/**
 * Tests the state the {@link BridgeStreamSource} derives from the status of the bridge and the data it receives.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class BridgeStreamSourceTest {

    private static final String VIN = "WBA12345678901234";
    private static final BrokerSettings SETTINGS = new BrokerSettings("localhost", 1883, false, "", "", "bmw/");
    private static final byte[] MESSAGE = ("{\"vin\":\"" + VIN
            + "\",\"data\":{\"vehicle.isMoving\":{\"value\":true,\"timestamp\":\"2026-10-23T09:59:00Z\"}}}")
            .getBytes(StandardCharsets.UTF_8);

    private @NonNullByDefault({}) StreamListener listener;
    private @NonNullByDefault({}) BridgeStreamSource source;

    @BeforeEach
    void setUp() {
        MqttBrokerConnection connection = mock(MqttBrokerConnection.class);
        when(connection.start()).thenReturn(CompletableFuture.completedFuture(true));
        listener = mock(StreamListener.class);
        source = new BridgeStreamSource(SETTINGS, "client", mock(ScheduledExecutorService.class), listener) {
            @Override
            MqttBrokerConnection createConnection() {
                return connection;
            }
        };
        source.start();
        source.connectionStateChanged(MqttConnectionState.CONNECTED, null);
    }

    @Test
    void dataFillsInAnUnknownBridgeState() {
        assertEquals(StreamState.CONNECTED_UPSTREAM_UNKNOWN, lastState());

        source.processMessage("bmw/raw/" + VIN, MESSAGE);

        assertEquals(StreamState.CONNECTED, lastState());
        verify(listener).onStreamData(eq(VIN), any());
    }

    @Test
    void dataDoesNotOverrideAnExplicitDisconnectedStatus() {
        source.processMessage("bmw/status", status(false));
        assertEquals(StreamState.UPSTREAM_DISCONNECTED, lastState());

        // e.g. an old retained message, replayed by the broker on subscribing
        source.processMessage("bmw/raw/" + VIN, MESSAGE);

        assertEquals(StreamState.UPSTREAM_DISCONNECTED, lastState());
        verify(listener, never()).onStreamStateChanged(eq(StreamState.CONNECTED), any());
        // the data itself is still processed; outdated values are dropped by their timestamps
        verify(listener).onStreamData(eq(VIN), any());
    }

    @Test
    void aNewStatusEstablishesRecovery() {
        source.processMessage("bmw/status", status(false));
        source.processMessage("bmw/status", status(true));

        assertEquals(StreamState.CONNECTED, lastState());
    }

    @Test
    void retainedStatusWinsOverDataReplayedBeforeItAfterReconnect() {
        source.processMessage("bmw/status", status(false));
        source.connectionStateChanged(MqttConnectionState.DISCONNECTED, null);
        source.connectionStateChanged(MqttConnectionState.CONNECTED, null);

        // the broker replays the retained messages of both topics in no particular order
        source.processMessage("bmw/raw/" + VIN, MESSAGE);
        source.processMessage("bmw/status", status(false));

        assertEquals(StreamState.UPSTREAM_DISCONNECTED, lastState());
    }

    private StreamState lastState() {
        ArgumentCaptor<StreamState> state = ArgumentCaptor.forClass(StreamState.class);
        verify(listener, atLeastOnce()).onStreamStateChanged(state.capture(), any());
        return state.getValue();
    }

    private static byte[] status(boolean connected) {
        return ("{\"connected\":" + connected + ",\"timestamp\":1761300000}").getBytes(StandardCharsets.UTF_8);
    }
}
