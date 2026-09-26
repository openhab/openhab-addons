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
package org.openhab.binding.mercedesme.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.handler.AccountHandlerMock;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.test.storage.VolatileStorageService;

/**
 * {@link WebsocketTest} regression tests for the missed-pong watchdog: a still-outstanding ping must not have
 * its timestamp reset by a later {@code doRefresh()} tick, and the {@code PONG_TIMEOUT_MS} boundary itself
 * counts as overdue.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class WebsocketTest {

    private Websocket newConnectedWebsocket() {
        AccountHandlerMock accountHandler = new AccountHandlerMock();
        Websocket ws = new WebsocketMock(accountHandler, mock(HttpClient.class), new AccountConfiguration(),
                mock(LocaleProvider.class), new VolatileStorageService().getStorage(""));
        Session sessionMock = mock(Session.class);
        RemoteEndpoint remoteMock = mock(RemoteEndpoint.class);
        when(sessionMock.getRemote()).thenReturn(remoteMock);
        ws.onConnect(sessionMock);
        return ws;
    }

    @Test
    void sendPingDoesNotResetTimestampWhileAPingIsOutstanding() {
        Websocket ws = newConnectedWebsocket();
        ws.sendPing();
        Instant firstPingSentAt = ws.pingSentAt.get();
        assertNotNull(firstPingSentAt, "sendPing() must record a timestamp for the outstanding ping");

        // a later doRefresh() tick before any pong arrived must not restart the watchdog clock
        ws.sendPing();
        assertEquals(firstPingSentAt, ws.pingSentAt.get(),
                "sendPing() must not overwrite the timestamp of a still-outstanding ping");
    }

    @Test
    void handlePongClearsOutstandingPingSoTheNextSendPingStartsANewWatchdog() {
        Websocket ws = newConnectedWebsocket();
        ws.sendPing();
        assertNotNull(ws.pingSentAt.get());

        Frame pongFrame = mock(Frame.class);
        when(pongFrame.getType()).thenReturn(Frame.Type.PONG);
        ws.onFrame(pongFrame);
        assertNull(ws.pingSentAt.get(), "handlePong() (via onFrame) must clear pingSentAt once a pong is received");

        ws.sendPing();
        assertNotNull(ws.pingSentAt.get(),
                "sendPing() must start tracking a new ping after the previous one was answered");
    }

    @Test
    void isPongOverdueUsesInclusiveTimeoutBoundary() {
        Websocket ws = newConnectedWebsocket();
        // exactly on the boundary: Duration.toMillis() truncates, so only ">=" catches it
        ws.pingSentAt.set(Instant.now().minusMillis(Websocket.PONG_TIMEOUT_MS));
        assertTrue(ws.isPongOverdue(), "a ping outstanding for exactly PONG_TIMEOUT_MS must count as overdue");
    }

    @Test
    void isPongOverdueFalseWhenNoPingIsOutstanding() {
        Websocket ws = newConnectedWebsocket();
        assertFalse(ws.isPongOverdue(), "no ping sent yet - nothing can be overdue");
    }
}
