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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Command;

/**
 * @author Andreas Berger - Initial contribution
 * @author Christopher Smith - Tests for framed TCP responses
 */
@NonNullByDefault
class FineOffsetGatewayQueryServiceTest {

    private static final String FIRMWARE = "GW1000_V1.6.8";

    @Test
    void failedLiveDataReadIsReportedAsNull() {
        // distinguishes a failed poll from a valid empty response, so the handler does not age out channels
        FineOffsetGatewayQueryService service = new FineOffsetGatewayQueryService(new FineOffsetGatewayConfiguration(),
                null) {
            @Override
            protected byte @Nullable [] executeCommand(Command command) {
                return null;
            }
        };
        assertThat(service.getMeasuredValues(), nullValue());
    }

    @Test
    void failedSensorReadIsReportedAsNull() throws IOException {
        // an empty result would make every sensor Thing look unpaired
        try (FineOffsetGatewayQueryService service = new FineOffsetGatewayQueryService(
                new FineOffsetGatewayConfiguration(), null) {
            @Override
            protected byte @Nullable [] executeCommand(Command command) {
                return null;
            }
        }) {
            assertThat(service.getRegisteredSensors(), nullValue());
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void responseDeliveredInSegmentsIsReadCompletely() throws Exception {
        byte[] response = firmwareResponse(FIRMWARE);
        byte[] first = Arrays.copyOfRange(response, 0, 4);
        byte[] second = Arrays.copyOfRange(response, 4, response.length);
        try (FakeGateway gateway = new FakeGateway(List.of(List.of(first, second)));
                FineOffsetGatewayQueryService service = new FineOffsetGatewayQueryService(gateway.config(), null)) {
            assertThat(service.getFirmwareVersion(), is(FIRMWARE));
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void invalidResponseDiscardsTheConnection() throws Exception {
        byte[] corrupt = firmwareResponse(FIRMWARE);
        corrupt[corrupt.length - 1]++;
        // trailing bytes that would be mistaken for the start of the next response on a reused connection
        byte[] trailing = new byte[] { (byte) 0xff, (byte) 0xff, 0x50, 0x01 };
        try (FakeGateway gateway = new FakeGateway(
                List.of(List.of(corrupt, trailing), List.of(firmwareResponse(FIRMWARE))));
                FineOffsetGatewayQueryService service = new FineOffsetGatewayQueryService(gateway.config(), null)) {
            assertThat(service.getFirmwareVersion(), nullValue());
            assertThat(service.getFirmwareVersion(), is(FIRMWARE));
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void requestIsRepeatedWhenTheGatewayClosedTheConnection() throws Exception {
        try (FakeGateway gateway = new FakeGateway(
                List.of(List.of(firmwareResponse(FIRMWARE)), List.of(firmwareResponse(FIRMWARE))), true);
                FineOffsetGatewayQueryService service = new FineOffsetGatewayQueryService(gateway.config(), null)) {
            assertThat(service.getFirmwareVersion(), is(FIRMWARE));
            // the gateway dropped the idle connection meanwhile: the request is sent again on a new one
            assertThat(service.getFirmwareVersion(), is(FIRMWARE));
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void slowlySegmentedResponseEndsAtTheReplyTimeout() throws Exception {
        byte[] response = firmwareResponse(FIRMWARE);
        // each segment arrives well within the 1 s reply timeout, but the whole response takes more than 2 s
        List<byte[]> segments = new ArrayList<>();
        for (int i = 0; i < response.length; i++) {
            segments.add(new byte[] { response[i] });
        }
        try (FakeGateway gateway = new FakeGateway(List.of(segments), false, 150);
                FineOffsetGatewayQueryService service = new FineOffsetGatewayQueryService(gateway.config(1), null)) {
            assertThat(service.getFirmwareVersion(), nullValue());
        }
    }

    /** {@code 0xff 0xff 0x50 <size> <length> <version> <checksum>}, size counting all bytes but the leading two. */
    private static byte[] firmwareResponse(String version) {
        byte[] text = version.getBytes(StandardCharsets.US_ASCII);
        byte[] data = new byte[text.length + 6];
        data[0] = (byte) 0xff;
        data[1] = (byte) 0xff;
        data[2] = 0x50;
        data[3] = (byte) (data.length - 2);
        data[4] = (byte) text.length;
        System.arraycopy(text, 0, data, 5, text.length);
        byte checksum = 0;
        for (int i = 2; i < data.length - 1; i++) {
            checksum += data[i];
        }
        data[data.length - 1] = checksum;
        return data;
    }

    /**
     * Serves one connection per entry: it answers a single request with the given chunks, written separately so
     * they may arrive in several TCP segments, then waits for the client to close the connection.
     */
    private static class FakeGateway implements AutoCloseable {
        private final ServerSocket server;
        private final Thread thread;

        private final boolean closeAfterResponse;
        private final long chunkDelayMillis;

        FakeGateway(List<List<byte[]>> connections) throws IOException {
            this(connections, false);
        }

        FakeGateway(List<List<byte[]>> connections, boolean closeAfterResponse) throws IOException {
            this(connections, closeAfterResponse, 50);
        }

        /**
         * @param closeAfterResponse whether to close each connection right after the response, like a gateway that
         *            drops idle clients
         * @param chunkDelayMillis the pause after writing each chunk
         */
        FakeGateway(List<List<byte[]>> connections, boolean closeAfterResponse, long chunkDelayMillis)
                throws IOException {
            this.closeAfterResponse = closeAfterResponse;
            this.chunkDelayMillis = chunkDelayMillis;
            server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
            thread = new Thread(() -> serve(connections), "fake-gateway");
            thread.setDaemon(true);
            thread.start();
        }

        FineOffsetGatewayConfiguration config() {
            return config(new FineOffsetGatewayConfiguration().timeout);
        }

        FineOffsetGatewayConfiguration config(int timeoutSeconds) {
            FineOffsetGatewayConfiguration config = new FineOffsetGatewayConfiguration();
            config.timeout = timeoutSeconds;
            config.ip = server.getInetAddress().getHostAddress();
            config.port = server.getLocalPort();
            return config;
        }

        private void serve(List<List<byte[]>> connections) {
            try {
                for (List<byte[]> chunks : connections) {
                    try (Socket socket = server.accept()) {
                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream();
                        if (in.read(new byte[64]) < 0) {
                            return;
                        }
                        for (byte[] chunk : chunks) {
                            out.write(chunk);
                            out.flush();
                            Thread.sleep(chunkDelayMillis);
                        }
                        while (!closeAfterResponse && in.read() >= 0) {
                            // wait until the client closes the connection
                        }
                    } catch (SocketException e) {
                        // the client closed the connection while unread data was pending; serve the next one
                        if (server.isClosed()) {
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                // server closed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void close() throws IOException {
            server.close();
            thread.interrupt();
        }
    }
}
