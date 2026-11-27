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
package org.openhab.binding.heos.internal.resources;

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests to validate the connection handling of the HeosSendCommand
 *
 * @author Chris Harris - Initial Contribution
 */
@NonNullByDefault
public class HeosSendCommandTest {

    /**
     * A connected client must actually be closed. Regression test: the guard used to be
     * inverted, so a live connection was never disconnected and its socket was leaked on
     * every reconnect.
     */
    @Test
    public void disconnectClosesAConnectedClient() throws IOException {
        Telnet client = mock(Telnet.class);
        when(client.isConnected()).thenReturn(true);

        new HeosSendCommand(client).disconnect();

        verify(client).disconnect();
    }

    /**
     * Disconnecting an already-disconnected client is a no-op.
     */
    @Test
    public void disconnectIgnoresAnAlreadyDisconnectedClient() throws IOException {
        Telnet client = mock(Telnet.class);
        when(client.isConnected()).thenReturn(false);

        new HeosSendCommand(client).disconnect();

        verify(client, never()).disconnect();
    }

    /**
     * A failure while closing must be swallowed, so that teardown of one connection cannot
     * prevent the rest of the shutdown path from running.
     */
    @Test
    public void disconnectSwallowsFailureToClose() throws IOException {
        Telnet client = mock(Telnet.class);
        when(client.isConnected()).thenReturn(true);
        doThrow(new IOException("boom")).when(client).disconnect();

        new HeosSendCommand(client).disconnect();

        verify(client).disconnect();
    }
}
