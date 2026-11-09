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
package org.openhab.binding.lutron.internal.hw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for the connection supervision of {@link HwTelnetBridgeHandler} against a fake processor on the loopback
 * interface.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public class HwTelnetBridgeHandlerTest {
    private static final int WAIT_MS = 20_000;
    private static final int STARTUP_COMMANDS = 5;

    private final Bridge bridge = mock(Bridge.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private @Nullable ServerSocket server;
    private @Nullable Socket processorSocket;
    private @Nullable HwTelnetBridgeHandler handler;
    private @Nullable BufferedReader fromBinding;

    @BeforeEach
    public void setUp() throws Exception {
        ServerSocket listening = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        server = listening;
        ThingUID uid = new ThingUID("lutron:hwtelnetbridge:test");
        when(bridge.getUID()).thenReturn(uid);
        when(bridge.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null));
        when(bridge.getConfiguration())
                .thenReturn(new Configuration(Map.of("ipAddress", "127.0.0.1", "port", listening.getLocalPort())));

        HwTelnetBridgeHandler created = new HwTelnetBridgeHandler(bridge);
        created.setCallback(callback);
        handler = created;
        created.initialize();

        // A processor without Ethernet logins just shows its prompt
        // Bounded, so that a regression fails the test instead of hanging the build
        listening.setSoTimeout(WAIT_MS);
        Socket socket = listening.accept();
        socket.setSoTimeout(WAIT_MS);
        processorSocket = socket;
        socket.getOutputStream().write("LNET> ".getBytes(StandardCharsets.US_ASCII));
        socket.getOutputStream().flush();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
        fromBinding = reader;
        verify(callback, timeout(WAIT_MS)).statusUpdated(any(),
                argThat(info -> info.getStatus() == ThingStatus.ONLINE));
        for (int i = 0; i < STARTUP_COMMANDS; i++) {
            assertNotNull(reader.readLine());
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        HwTelnetBridgeHandler current = handler;
        if (current != null) {
            current.dispose();
        }
        Socket socket = processorSocket;
        if (socket != null) {
            socket.close();
        }
        ServerSocket listening = server;
        if (listening != null) {
            listening.close();
        }
    }

    private Socket processorSocket() {
        Socket socket = processorSocket;
        assertNotNull(socket);
        return socket;
    }

    @Test
    public void processorThatNeverRepliedIsReportedAsLost() {
        // A network failure immediately after login must not disable connection supervision.
        handler().idleTimeoutMs = -1;

        handler().heartbeat();

        verify(callback).statusUpdated(any(),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR
                        && String.valueOf(info.getDescription()).contains("Suppress Replies")));
    }

    private HwTelnetBridgeHandler handler() {
        HwTelnetBridgeHandler current = handler;
        assertNotNull(current);
        return current;
    }

    @Test
    public void heartbeatAsksProcessorForAReply() throws Exception {
        handler().heartbeat();

        BufferedReader reader = fromBinding;
        assertNotNull(reader);
        assertEquals("DLMON", reader.readLine());
    }

    @Test
    public void silentConnectionIsReportedAsLost() throws Exception {
        assertSilenceIsReportedAsLostAfter("Dimmer level monitoring enabled");
    }

    @Test
    public void monitoringWithoutCommandRepliesDoesNotDisableTimeout() throws Exception {
        assertSilenceIsReportedAsLostAfter("DL, [01:08:01:54], 50");
    }

    private void assertSilenceIsReportedAsLostAfter(String line) throws Exception {
        CountDownLatch received = new CountDownLatch(1);
        handler().addLineListener(message -> received.countDown());
        processorSocket().getOutputStream().write((line + "\r\n").getBytes(StandardCharsets.US_ASCII));
        processorSocket().getOutputStream().flush();
        assertTrue(received.await(WAIT_MS, TimeUnit.MILLISECONDS), "line was not received");
        // Any time since the last data received exceeds a negative timeout
        handler().idleTimeoutMs = -1;

        handler().heartbeat();

        verify(callback).statusUpdated(any(), argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
    }
}
