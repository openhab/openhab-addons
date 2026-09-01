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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import com.google.gson.Gson;

/**
 * Tests that installing a connection from the web proxy login leaves exactly one live connection behind: the
 * replaced one is closed, and a candidate that lost against dispose or close never replaces the current one.
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
}
