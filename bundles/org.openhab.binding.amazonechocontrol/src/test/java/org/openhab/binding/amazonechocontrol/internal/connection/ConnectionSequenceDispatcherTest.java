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
package org.openhab.binding.amazonechocontrol.internal.connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/**
 * Tests the sequence node dispatcher lifecycle around logout, re-login and terminal close. Without a running
 * dispatcher every TTS, announcement, volume and routine command is queued forever and never sent, silently.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class ConnectionSequenceDispatcherTest {

    private final Connection connection = new Connection(null, new Gson(), mock(HttpClient.class));

    @AfterEach
    public void stopDispatcher() {
        connection.logout(false);
    }

    @Test
    public void dispatcherRunsOnANewConnection() {
        assertTrue(connection.isSequenceNodeDispatcherRunning());
    }

    @Test
    public void logoutStopsTheDispatcher() {
        connection.logout(false);

        assertFalse(connection.isSequenceNodeDispatcherRunning());
    }

    @Test
    public void ensureRestartsTheDispatcherAfterLogout() {
        connection.logout(false);

        connection.ensureSequenceNodeDispatcherIsRunning();

        assertTrue(connection.isSequenceNodeDispatcherRunning());
    }

    @Test
    public void closeIsTerminalEvenWhenALateLoginTriesToRevive() {
        connection.close();

        connection.ensureSequenceNodeDispatcherIsRunning();

        assertFalse(connection.isSequenceNodeDispatcherRunning());
    }

    @Test
    public void ensuringAgainKeepsTheSameRunningDispatcher() {
        connection.ensureSequenceNodeDispatcherIsRunning();
        Object dispatcher = connection.sequenceNodeDispatcher();

        connection.ensureSequenceNodeDispatcherIsRunning();

        assertSame(dispatcher, connection.sequenceNodeDispatcher());
        assertTrue(connection.isSequenceNodeDispatcherRunning());
    }
}
