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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlCommandDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.Gson;

/**
 * Tests that installing a connection from the web proxy login leaves exactly one live connection behind: the
 * replaced one is closed, a candidate that lost against dispose or close never replaces the current one, and a
 * logout finishes before a login can swap the connection.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerConnectionSwapTest {

    private final Gson gson = new Gson();
    private final HttpClient httpClient = mock(HttpClient.class);
    @SuppressWarnings("unchecked")
    private final AccountHandler handler = new AccountHandler(bridgeWithUid(), mock(Storage.class), gson, httpClient,
            mock(HTTP2Client.class), mock(AmazonEchoControlCommandDescriptionProvider.class));

    private static Bridge bridgeWithUid() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID("amazonechocontrol", "account", "test"));
        return bridge;
    }

    @AfterEach
    public void closeConnections() {
        handler.getConnection().close();
    }

    @Test
    public void setConnectionInstallsTheNewAndClosesTheReplacedConnection() {
        Connection replaced = handler.getConnection();
        Connection fresh = new Connection(null, gson, httpClient);

        handler.setConnection(fresh);

        assertSame(fresh, handler.getConnection());
        assertTrue(replaced.isClosed());
        assertFalse(fresh.isClosed());
    }

    @Test
    public void setConnectionWithTheCurrentConnectionKeepsItOpen() {
        Connection current = handler.getConnection();

        handler.setConnection(current);

        assertSame(current, handler.getConnection());
        assertFalse(current.isClosed());
    }

    @Test
    public void setConnectionRefusesAClosedCandidate() {
        Connection current = handler.getConnection();
        Connection closedCandidate = new Connection(null, gson, httpClient);
        closedCandidate.close();

        handler.setConnection(closedCandidate);

        assertSame(current, handler.getConnection());
        assertFalse(current.isClosed());
    }

    @Test
    public void setConnectionAfterDisposeClosesTheCandidateInsteadOfInstallingIt() {
        Connection current = handler.getConnection();
        Connection lateLoginCandidate = new Connection(null, gson, httpClient);

        handler.dispose();
        handler.setConnection(lateLoginCandidate);

        assertNotSame(lateLoginCandidate, handler.getConnection());
        assertSame(current, handler.getConnection());
        assertTrue(lateLoginCandidate.isClosed());
    }

    @Test
    public void aLoginWaitsForARunningLogoutInsteadOfOverlappingIt() throws InterruptedException {
        handler.setCallback(mock(ThingHandlerCallback.class));
        Connection fresh = new Connection(null, gson, httpClient);
        List<Thread> loginsDuringLogout = new ArrayList<>();
        Connection loggingOut = new Connection(null, gson, httpClient) {
            @Override
            public void logout(boolean reset) {
                if (loginsDuringLogout.isEmpty()) {
                    Thread login = new Thread(() -> handler.setConnection(fresh));
                    loginsDuringLogout.add(login);
                    login.setDaemon(true);
                    login.start();
                    assertEquals(Thread.State.BLOCKED, settledState(login));
                }
                super.logout(reset);
            }
        };
        handler.setConnection(loggingOut);

        handler.resetConnection(false);

        Thread login = loginsDuringLogout.get(0);
        login.join(TimeUnit.SECONDS.toMillis(5));
        assertFalse(login.isAlive());
        assertSame(fresh, handler.getConnection());
    }

    private static Thread.State settledState(Thread thread) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.getState() != Thread.State.BLOCKED && thread.getState() != Thread.State.TERMINATED
                && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        return thread.getState();
    }
}
