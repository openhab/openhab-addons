/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.livisismarthome.internal.listener.EventListener;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class LivisiWebSocketTest {

    private @NonNullByDefault({}) LivisiWebSocketAccessible webSocket;
    private @NonNullByDefault({}) EventListenerDummy eventListener;
    private @NonNullByDefault({}) WebSocketClient webSocketClientMock;
    private @NonNullByDefault({}) Session sessionMock;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        sessionMock = mock(Session.class);

        Future<Session> futureMock = mock(Future.class);
        when(futureMock.get()).thenReturn(sessionMock);

        webSocketClientMock = mock(WebSocketClient.class);
        when(webSocketClientMock.connect(any(), any())).thenReturn(futureMock);

        HttpClient httpClientMock = mock(HttpClient.class);

        eventListener = new EventListenerDummy();
        webSocket = new LivisiWebSocketAccessible(httpClientMock, eventListener, new URI(""), 1000);
    }

    @Test
    public void testStart() throws Exception {
        startWebSocket();

        assertTrue(webSocket.isRunning());
    }

    @Test
    public void testStop() throws Exception {
        startWebSocket();

        webSocket.stop();

        assertFalse(webSocket.isRunning());
    }

    @Test
    public void testOnCloseAfterStop() throws Exception {
        startWebSocket();

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());

        webSocket.stop();
        webSocket.onClose(StatusCode.ABNORMAL, "Test");

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        // stop() itself causes a (abnormal) close event, that shouldn't get noticed
        // (otherwise it would cause a reconnect event which would lead to an infinite loop ...)
        assertFalse(eventListener.isConnectionClosedCalled());
    }

    @Test
    public void testOnCloseAfterRestart() throws Exception {
        startWebSocket();

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());

        webSocket.stop();
        webSocket.onClose(StatusCode.ABNORMAL, "Test");

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        // stop() itself causes a (abnormal) close event, that shouldn't get noticed
        // (otherwise it would cause a reconnect event which would lead to an infinite loop ...)
        assertFalse(eventListener.isConnectionClosedCalled());

        startWebSocket();

        webSocket.onClose(StatusCode.ABNORMAL, "Test");

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        // A close event after a restart of the web socket should get recognized again
        assertTrue(eventListener.isConnectionClosedCalled());
    }

    @Test
    public void testOnCloseAbnormal() throws Exception {
        startWebSocket();

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());

        webSocket.onClose(StatusCode.ABNORMAL, "Test");

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertTrue(eventListener.isConnectionClosedCalled());
    }

    @Test
    public void testOnCloseNormal() throws Exception {
        startWebSocket();

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());

        webSocket.onClose(StatusCode.NORMAL, "Test");

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        // Nothing should get noticed when a normal close is executed (for example by stopping OpenHAB)
        assertFalse(eventListener.isConnectionClosedCalled());
    }

    @Test
    public void testOnMessage() throws Exception {
        startWebSocket();

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());

        webSocket.onMessage("Test-Message");

        assertTrue(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());
    }

    @Test
    public void testOnMessageAfterStop() throws Exception {
        startWebSocket();

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());

        webSocket.stop();
        webSocket.onClose(StatusCode.ABNORMAL, "Test");
        webSocket.onMessage("Test-Message");

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());
    }

    @Test
    public void testOnError() throws Exception {
        startWebSocket();

        assertFalse(eventListener.isOnEventCalled());
        assertFalse(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());

        webSocket.onError(new RuntimeException("Test-Exception"));

        assertFalse(eventListener.isOnEventCalled());
        assertTrue(eventListener.isOnErrorCalled());
        assertFalse(eventListener.isConnectionClosedCalled());
    }

    private void startWebSocket() throws Exception {
        webSocket.start();
        when(sessionMock.isOpen()).thenReturn(true);

        webSocket.onConnect(sessionMock);
    }

    private class LivisiWebSocketAccessible extends LivisiWebSocket {

        private LivisiWebSocketAccessible(HttpClient httpClient, EventListener eventListener, URI webSocketURI,
                int maxIdleTimeout) {
            super(httpClient, eventListener, webSocketURI, maxIdleTimeout);
        }

        @Override
        WebSocketClient createWebSocketClient() {
            return webSocketClientMock;
        }

        @Override
        void startWebSocketClient(WebSocketClient client) {
        }

        @Override
        void stopWebSocketClient(WebSocketClient client) {
        }
    }

    private static class EventListenerDummy implements EventListener {

        private boolean isOnEventCalled;
        private boolean isOnErrorCalled;
        private boolean isConnectionClosedCalled;

        @Override
        public void onEvent(String msg) {
            isOnEventCalled = true;
        }

        @Override
        public void onError(Throwable cause) {
            isOnErrorCalled = true;
        }

        @Override
        public void connectionClosed() {
            isConnectionClosedCalled = true;
        }

        public boolean isOnEventCalled() {
            return isOnEventCalled;
        }

        public boolean isOnErrorCalled() {
            return isOnErrorCalled;
        }

        public boolean isConnectionClosedCalled() {
            return isConnectionClosedCalled;
        }
    }
}
