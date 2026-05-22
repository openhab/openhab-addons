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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Callback;
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
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;

/**
 * Verifies that a disposed {@link Shelly2RpcSocket} no longer reports anything to its message handler, and that a
 * {@code dispose()} racing a concurrent {@code onConnect()} never leaves an uncancelled ping task behind.
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
        when(session.getRemoteSocketAddress()).thenReturn(DEVICE_ADDRESS);
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

        verify(session).close(eq(StatusCode.SHUTDOWN), anyString(), any(Callback.class));
        verify(handler, never()).onConnect(any(), anyBoolean());
        verifyNoInteractions(thingTable);
        assertFalse(socket.isConnected());
    }

    @Test
    void closeBeforeDisposeIsStillReportedToTheHandler() {
        socket.onClose(StatusCode.ABNORMAL, "Device rebooted");

        verify(handler).onClose(anyBoolean(), eq(StatusCode.ABNORMAL), anyString());
    }

    @Test
    void disposeDuringConnectStillCancelsTheInstalledPingTask() throws Exception {
        Shelly2RpcSocket localSocket = new Shelly2RpcSocket("shellyplus1-test", thingTable, DEVICE_ADDRESS,
                webSocketClient, scheduler);
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        ScheduledFuture<?> pingTask = mock(ScheduledFuture.class);
        when(thingTable.getThing(DEVICE_ADDRESS)).thenReturn(thing);
        when(thing.getThingName()).thenReturn("shellyplus1-test");
        when(thing.getApi()).thenReturn(api);
        doReturn(pingTask).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));

        CountDownLatch insideConnect = new CountDownLatch(1);
        CountDownLatch releaseConnect = new CountDownLatch(1);
        when(api.getRpcHandler()).thenAnswer(invocation -> {
            insideConnect.countDown();
            releaseConnect.await();
            return handler;
        });

        Thread connectThread = new Thread(() -> localSocket.onConnect(session));
        Thread disposeThread = new Thread(localSocket::dispose);
        connectThread.setDaemon(true);
        disposeThread.setDaemon(true);

        connectThread.start();
        assertTrue(insideConnect.await(5, TimeUnit.SECONDS));

        disposeThread.start();
        awaitBlocked(disposeThread);
        releaseConnect.countDown();

        connectThread.join(5000);
        disposeThread.join(5000);

        verify(pingTask).cancel(false);
    }

    private static void awaitBlocked(Thread thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.getState() != Thread.State.BLOCKED) {
            if (System.nanoTime() > deadline) {
                fail("Thread never blocked on the socket monitor: " + thread.getState());
            }
            Thread.sleep(5);
        }
    }
}
