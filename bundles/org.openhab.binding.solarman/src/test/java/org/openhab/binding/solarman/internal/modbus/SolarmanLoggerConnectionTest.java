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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.solarman.internal.SolarmanLoggerConfiguration;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanProtocolException;

/**
 * Tests TCP reassembly and cleanup using fragmented input without timing assumptions.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
@Timeout(10)
class SolarmanLoggerConnectionTest {
    @ParameterizedTest
    @CsvSource({ "false,false", "false,true", "true,false", "true,true" })
    void skipsHeartbeatWithoutRepeatingReadOrWrite(boolean strict, boolean write) throws Exception {
        byte[] request = request(write);
        byte[] response = response(request, write);
        byte[] heartbeat = heartbeat(request);
        assertThrows(SolarmanProtocolException.class,
                () -> SolarmanResponseValidator.validateEnvelope(heartbeat, request, true));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Socket socket = socket(fragmented(join(heartbeat, heartbeat, heartbeat, response)), output);
        try (SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket)) {
            SolarmanV5Protocol protocol = new SolarmanV5Protocol(configuration(), strict);
            if (write) {
                assertTrue(protocol.writeRegisters(connection, 184, new byte[] { 0, 78 }));
            } else {
                assertArrayEquals(new byte[] { 0, 78 },
                        protocol.readRegisters(connection, (byte) 3, 184, 184).get(184));
            }
            assertArrayEquals(request, output.toByteArray(), "Only the original request may be sent");
        }
    }

    @Test
    void leavesHeartbeatAfterResponseForTheNextExchange() throws Exception {
        byte[] request = request(false);
        byte[] response = response(request, false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Socket socket = socket(fragmented(join(response, heartbeat(request), response)), output);
        try (SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket)) {
            assertArrayEquals(response, connection.sendRequest(request));
            assertArrayEquals(response, connection.sendRequest(request));
            assertArrayEquals(join(request, request), output.toByteArray());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "checksum", "serial", "start", "end", "type", "control" })
    void doesNotDiscardInvalidOrUnrecognizedHeartbeat(String field) throws Exception {
        byte[] request = request(false);
        byte[] invalid = heartbeat(request);
        switch (field) {
            case "checksum" -> invalid[12] ^= 1;
            case "serial" -> invalid[7] ^= 1;
            case "start" -> invalid[0] = 0;
            case "end" -> invalid[13] = 0;
            case "type" -> invalid[11] = 1;
            case "control" -> invalid[4] = 0x48;
            default -> throw new IllegalArgumentException(field);
        }
        if (!"checksum".equals(field)) {
            checksum(invalid);
        }
        Socket socket = socket(new ByteArrayInputStream(join(invalid, response(request, false))),
                new ByteArrayOutputStream());
        try (SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket)) {
            assertThrows(SolarmanProtocolException.class,
                    () -> new SolarmanV5Protocol(configuration(), true).readRegisters(connection, (byte) 3, 184, 184));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "checksum", "serial", "sequence", "crc" })
    void stillRejectsInvalidModbusResponseAfterHeartbeat(String field) throws Exception {
        byte[] request = request(false);
        byte[] invalid = response(request, false);
        switch (field) {
            case "checksum" -> invalid[invalid.length - 2] ^= 1;
            case "serial" -> invalid[7] ^= 1;
            case "sequence" -> invalid[5] ^= 1;
            case "crc" -> invalid[invalid.length - 3] ^= 1;
            default -> throw new IllegalArgumentException(field);
        }
        if (!"checksum".equals(field)) {
            checksum(invalid);
        }
        Socket socket = socket(fragmented(join(heartbeat(request), invalid)), new ByteArrayOutputStream());
        try (SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket)) {
            assertThrows(SolarmanProtocolException.class,
                    () -> new SolarmanV5Protocol(configuration(), true).readRegisters(connection, (byte) 3, 184, 184));
        }
    }

    @Test
    void boundsHeartbeatCountAndClosesConnectionWithoutResending() throws Exception {
        byte[] request = request(false);
        byte[] heartbeat = heartbeat(request);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Socket socket = socket(new ByteArrayInputStream(join(heartbeat, heartbeat, heartbeat, heartbeat)), output);
        try (SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket)) {
            SolarmanConnectionException failure = assertThrows(SolarmanConnectionException.class,
                    () -> connection.sendRequest(request));
            assertEquals("Too many V5 heartbeats while waiting for the Modbus response",
                    Objects.requireNonNull(failure.getCause()).getMessage());
            assertFalse(connection.isOpen());
            assertArrayEquals(request, output.toByteArray());
            verify(socket).close();
        }
    }

    @Test
    void sharesOneDeadlineAcrossHeartbeatAndFragmentedResponse() throws Exception {
        byte[] request = request(false);
        AtomicLong clock = new AtomicLong();
        byte[] frames = join(heartbeat(request), response(request, false));
        InputStream delayed = new FilterInputStream(new ByteArrayInputStream(frames)) {
            @Override
            public int read(byte @Nullable [] bytes, int offset, int length) throws IOException {
                clock.addAndGet(2_000_000_000L);
                return super.read(Objects.requireNonNull(bytes), offset, Math.min(length, 11));
            }
        };
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Socket socket = socket(delayed, output);
        try (SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket, clock::get)) {
            SolarmanConnectionException failure = assertThrows(SolarmanConnectionException.class,
                    () -> connection.sendRequest(request));
            assertInstanceOf(SocketTimeoutException.class, failure.getCause());
            assertEquals(10_000_000_000L, clock.get());
            assertFalse(connection.isOpen());
            assertArrayEquals(request, output.toByteArray());
            verify(socket).setSoTimeout(2000);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void failsWhenHeartbeatIsNotFollowedByResponse(boolean timeout) throws Exception {
        byte[] request = request(false);
        InputStream input = new ByteArrayInputStream(heartbeat(request));
        if (timeout) {
            input = new FilterInputStream(input) {
                @Override
                public int read(byte @Nullable [] bytes, int offset, int length) throws IOException {
                    int count = super.read(Objects.requireNonNull(bytes), offset, length);
                    if (count < 0) {
                        throw new SocketTimeoutException("Simulated logger silence");
                    }
                    return count;
                }
            };
        }
        Socket socket = socket(input, new ByteArrayOutputStream());
        try (SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket)) {
            assertThrows(SolarmanConnectionException.class, () -> connection.sendRequest(request));
            assertFalse(connection.isOpen());
        }
    }

    @Test
    void keepsRawModbusFramingUnchanged() throws Exception {
        byte[] response = HexFormat.of().parseHex("000100000005010302004E");
        Socket socket = socket(fragmented(response), new ByteArrayOutputStream());
        try (SolarmanLoggerConnection connection = new SolarmanLoggerConnection(socket)) {
            assertArrayEquals(response, connection.sendRequest(new byte[] { 0, 1, 0, 0, 0, 6, 1, 3 }));
            verify(socket, never()).setSoTimeout(anyInt());
        }
    }

    @Test
    void closeInterruptsSocketReadAfterHeartbeat() throws Exception {
        byte[] request = request(false);
        CountDownLatch waitingForResponse = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                Socket client = new Socket(server.getInetAddress(), server.getLocalPort());
                Socket peer = server.accept();
                SolarmanLoggerConnection connection = new SolarmanLoggerConnection(client, () -> {
                    if (reads.incrementAndGet() == 4) {
                        waitingForResponse.countDown();
                    }
                    return System.nanoTime();
                })) {
            peer.setSoTimeout(5000);
            FutureTask<SolarmanConnectionException> exchange = new FutureTask<>(
                    () -> assertThrows(SolarmanConnectionException.class, () -> connection.sendRequest(request)));
            Thread worker = Thread.ofVirtual().start(exchange);
            try {
                assertArrayEquals(request, peer.getInputStream().readNBytes(request.length));
                peer.getOutputStream().write(heartbeat(request));
                assertTrue(waitingForResponse.await(5, TimeUnit.SECONDS));
                connection.close();
                assertInstanceOf(IOException.class, exchange.get(5, TimeUnit.SECONDS).getCause());
                assertFalse(connection.isOpen());
            } finally {
                connection.close();
                exchange.cancel(true);
                worker.join(1000);
            }
        }
    }

    private Socket socket(InputStream input, ByteArrayOutputStream output) throws IOException {
        Socket socket = mock(Socket.class);
        when(socket.isConnected()).thenReturn(true);
        when(socket.getInputStream()).thenReturn(input);
        when(socket.getOutputStream()).thenReturn(output);
        return socket;
    }

    private InputStream fragmented(byte[] bytes) {
        return new FilterInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            public int read(byte @Nullable [] buffer, int offset, int length) throws IOException {
                return super.read(Objects.requireNonNull(buffer), offset, Math.min(2, length));
            }
        };
    }

    private SolarmanLoggerConfiguration configuration() {
        SolarmanLoggerConfiguration configuration = new SolarmanLoggerConfiguration();
        configuration.serialNumber = "1234567890";
        return configuration;
    }

    private byte[] request(boolean write) throws SolarmanException {
        SolarmanV5Protocol protocol = new SolarmanV5Protocol(configuration(), true);
        return protocol.buildSolarmanV5Frame(
                write ? protocol.buildModbusWriteHoldingRegistersFrame((byte) 1, 184, new byte[] { 0, 78 })
                        : protocol.buildModbusReadHoldingRegistersFrame((byte) 1, (byte) 3, 184, 1));
    }

    private byte[] heartbeat(byte[] request) {
        // Captured 14-byte heartbeat shape, with a synthetic logger identity and an independent sequence.
        byte[] heartbeat = HexFormat.of().parseHex("A501001047D29E00000000000015");
        System.arraycopy(request, 7, heartbeat, 7, 4);
        checksum(heartbeat);
        return heartbeat;
    }

    private byte[] response(byte[] request, boolean write) {
        byte[] rtu = HexFormat.of().parseHex(write ? "011000B80001" : "010302004E");
        byte[] response = new byte[25 + rtu.length + 4];
        response[0] = (byte) 0xA5;
        ByteBuffer.wrap(response, 1, 2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) (response.length - 13));
        response[3] = 0x10;
        response[4] = 0x15;
        response[5] = request[5];
        System.arraycopy(request, 7, response, 7, 4);
        response[11] = 2;
        System.arraycopy(rtu, 0, response, 25, rtu.length);
        ByteBuffer.wrap(response, 25 + rtu.length, 2).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) CRC16Modbus.calculate(rtu));
        response[response.length - 1] = 0x15;
        checksum(response);
        return response;
    }

    private void checksum(byte[] frame) {
        int sum = 0;
        for (int i = 1; i < frame.length - 2; i++) {
            sum += Byte.toUnsignedInt(frame[i]);
        }
        frame[frame.length - 2] = (byte) sum;
    }

    private byte[] join(byte[]... frames) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Arrays.stream(frames).forEach(stream::writeBytes);
        return stream.toByteArray();
    }

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
