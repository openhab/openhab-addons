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
package org.openhab.binding.shelly.internal.api2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.EOFException;
import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;

/**
 * Verifies that a disposed {@link Shelly2RpcSocket} no longer reports anything to its message handler.
 *
 * @author Markus Michels - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault({})
class Shelly2RpcSocketDisposeTest {

    private static final InetSocketAddress DEVICE_ADDRESS = new InetSocketAddress("192.168.1.55", 80);

    private @Mock ShellyThingTable thingTable;
    private @Mock WebSocketClient webSocketClient;
    private @Mock ScheduledExecutorService scheduler;
    private @Mock Shelly2RpctInterface handler;
    private @Mock Session session;

    private Shelly2RpcSocket socket;

    @BeforeEach
    void setUp() {
        socket = new Shelly2RpcSocket("shellyplus1-test", thingTable, DEVICE_ADDRESS, webSocketClient, scheduler);
        socket.addMessageHandler(handler);
        when(session.getRemoteAddress()).thenReturn(DEVICE_ADDRESS);
        when(session.isOpen()).thenReturn(true);
    }

    @Test
    void closeAfterDisposeIsNotReportedToTheHandler() {
        socket.dispose();

        socket.onClose(StatusCode.ABNORMAL, "Device rebooted");

        verify(handler, never()).onClose(anyBoolean(), anyInt(), anyString());
    }

    @Test
    void errorAfterDisposeIsNotReportedToTheHandler() {
        socket.dispose();

        socket.onError(new EOFException("connection reset"));

        verify(handler, never()).onError(any());
    }

    @Test
    void connectAfterDisposeClosesTheSessionWithoutReachingTheHandler() {
        socket.dispose();

        socket.onConnect(session);

        verify(session).close(eq(StatusCode.SHUTDOWN), anyString());
        verify(handler, never()).onConnect(any(), anyBoolean());
        verifyNoInteractions(thingTable);
        assertFalse(socket.isConnected());
    }

    @Test
    void closeBeforeDisposeIsStillReportedToTheHandler() {
        socket.onClose(StatusCode.ABNORMAL, "Device rebooted");

        verify(handler).onClose(anyBoolean(), eq(StatusCode.ABNORMAL), anyString());
    }
}
