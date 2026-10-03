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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
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
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;

/**
 * @author Markus Michels - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault({})
class Shelly2RpcSocketSessionTest {

    private static final InetSocketAddress DEVICE_ADDRESS = new InetSocketAddress("192.168.1.55", 80);

    private @Mock ShellyThingTable thingTable;
    private @Mock WebSocketClient webSocketClient;
    private @Mock ScheduledExecutorService scheduler;
    private @Mock Shelly2RpctInterface handler;
    private @Mock ShellyThingInterface thing;
    private @Mock Shelly2ApiRpc api;
    private @Mock ScheduledFuture<?> pingTask;

    private Shelly2RpcSocket socket;

    @BeforeEach
    void setUp() {
        socket = new Shelly2RpcSocket("shellyplus1-test", thingTable, DEVICE_ADDRESS, webSocketClient, scheduler);
        when(thingTable.getThing(DEVICE_ADDRESS)).thenReturn(thing);
        when(thing.getThingName()).thenReturn("shellyplus1-test");
        when(thing.getApi()).thenReturn(api);
        when(api.getRpcHandler()).thenReturn(handler);
        doReturn(pingTask).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    void closeOfReplacedSessionKeepsTheCurrentSession() {
        Session oldSession = openSession();
        Session newSession = openSession();
        socket.onConnect(oldSession);
        socket.onConnect(newSession);

        socket.onClose(oldSession, StatusCode.ABNORMAL, "late close");
        socket.onError(oldSession, new EOFException("late error"));

        assertTrue(socket.isConnected());
        verify(handler, never()).onClose(anyBoolean(), anyInt(), anyString());
        verify(handler, never()).onError(any());
    }

    @Test
    void closeOfCurrentSessionIsReported() {
        Session session = openSession();
        socket.onConnect(session);

        socket.onClose(session, StatusCode.ABNORMAL, "Device rebooted");

        assertFalse(socket.isConnected());
        verify(handler).onClose(anyBoolean(), eq(StatusCode.ABNORMAL), anyString());
    }

    @Test
    void connectOfThingWithoutRpcApiClosesTheSession() {
        when(thing.getApi()).thenReturn(mock(ShellyApiInterface.class));
        Session session = openSession();

        socket.onConnect(session);

        verify(session).close(eq(StatusCode.SHUTDOWN), anyString());
        assertFalse(socket.isConnected());
    }

    @Test
    void connectAfterDisposeIsRejected() {
        socket.dispose();

        assertThrows(ShellyApiException.class, socket::connect);
        verifyNoInteractions(webSocketClient);
    }

    @Test
    void unexpectedHandlerExceptionDoesNotEscapeOnMessage() throws Exception {
        socket.onConnect(openSession());
        doThrow(new IndexOutOfBoundsException("boom")).when(handler).onNotifyStatus(any());

        assertDoesNotThrow(() -> socket.onMessage(mock(Session.class),
                "{\"src\":\"shellyplus1-test\",\"method\":\"NotifyStatus\",\"params\":{}}"));
    }

    private Session openSession() {
        Session session = mock(Session.class);
        when(session.getRemoteAddress()).thenReturn(DEVICE_ADDRESS);
        when(session.isOpen()).thenReturn(true);
        when(session.getRemote()).thenReturn(mock(RemoteEndpoint.class));
        return session;
    }
}
