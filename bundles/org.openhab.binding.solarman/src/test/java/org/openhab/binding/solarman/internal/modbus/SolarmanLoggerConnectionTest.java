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
package org.openhab.binding.solarman.internal.modbus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarman.internal.SolarmanLoggerConfiguration;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;

/**
 * Tests TCP reassembly and cleanup using fragmented input without timing assumptions.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SolarmanLoggerConnectionTest {
    @Test
    void reassemblesFragmentedV5FramesAndClosesOwnedSocket() throws Exception {
        byte[] frame = new byte[20];
        frame[0] = (byte) 165;
        frame[1] = 7;
        frame[19] = 21;
        AtomicInteger reads = new AtomicInteger();
        InputStream fragmented = new FilterInputStream(new ByteArrayInputStream(frame)) {
            @Override
            public synchronized int read(byte @Nullable [] bytes, int offset, int length) throws IOException {
                reads.incrementAndGet();
                return super.read(Objects.requireNonNull(bytes), offset, Math.min(2, length));
            }
        };
        Socket socket = mock(Socket.class);
        when(socket.isConnected()).thenReturn(true);
        when(socket.getInputStream()).thenReturn(fragmented);
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket);
        assertArrayEquals(frame, connection.sendRequest(new byte[] { (byte) 165 }));
        assertTrue(reads.get() > 2);
        connection.close();
        assertFalse(connection.isConnected());
        verify(socket).close();
    }

    @Test
    void rejectsTruncatedFramesAndPreventsReconnectionAfterDisposal() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.isConnected()).thenReturn(true);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { (byte) 165, 7, 0 }));
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        try (SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket)) {
            assertThrows(SolarmanConnectionException.class, () -> connection.sendRequest(new byte[] { (byte) 165 }));
        }
        SolarmanLoggerConnector connector = new SolarmanLoggerConnector(new SolarmanLoggerConfiguration());
        connector.close();
        try (SolarmanLoggerConnection rejected = connector.createConnection()) {
            assertFalse(rejected.isConnected());
            assertFalse(rejected.isOpen());
        }
    }
}
