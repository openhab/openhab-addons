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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * @author Markus Michels - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
@SuppressWarnings("null")
class Shelly2DebugLogSocketTest {

    private @NonNullByDefault({}) @Mock WebSocketClient webSocketClient;
    private @NonNullByDefault({}) @Mock Shelly2DebugLogListener listener;
    private @NonNullByDefault({}) @Mock Session session;

    private @NonNullByDefault({}) Shelly2DebugLogSocket socket;

    @BeforeEach
    void setUp() {
        socket = new Shelly2DebugLogSocket("test", new InetSocketAddress("127.0.0.1", 80), webSocketClient, listener);
        when(session.isOpen()).thenReturn(true);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "Digest username=\"admin\", response=\"abc\"" })
    void connectSetsAuthorizationHeaderOnlyWhenProvided(@Nullable String authHeader) throws Exception {
        socket.connect(authHeader);

        ArgumentCaptor<ClientUpgradeRequest> captor = ArgumentCaptor.forClass(ClientUpgradeRequest.class);
        verify(webSocketClient).connect(any(), any(), captor.capture());
        assertThat(captor.getValue().getHeader("Host"), is("127.0.0.1:80"));
        assertThat(captor.getValue().getHeader("Authorization"), is(authHeader));
    }

    @Test
    void closeOfActiveSessionNotifiesListener() {
        socket.onConnect(session);

        socket.onClose(session, 1006, "gone");

        verify(listener).onDebugLogClosed(socket);
    }

    @Test
    void closeAfterDisconnectDoesNotNotifyListener() {
        socket.onConnect(session);

        socket.disconnect();
        socket.onClose(session, 1000, "Socket closed");

        verify(session).close(anyInt(), anyString());
        verify(listener, never()).onDebugLogClosed(any());
    }
}
