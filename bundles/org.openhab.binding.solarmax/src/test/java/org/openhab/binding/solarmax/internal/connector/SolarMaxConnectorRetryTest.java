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
package org.openhab.binding.solarmax.internal.connector;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link SolarMaxConnectorRetryTest} tests timeouts and retries of the {@link SolarMaxConnector} against a fake
 * device on a local server socket, answering with a response recorded from a SolarMax 10MT2.
 *
 * @author Christian Schreiner - Initial contribution
 */
@NonNullByDefault
public class SolarMaxConnectorRetryTest {

    private static final String HOST = "127.0.0.1";

    // recorded from a SolarMax 10MT2 (device address 1)
    private static final String RESPONSE = "{01;FB;8F|64:PAC=290;SYS=4E28,0;SAL=0;UD01=1011;UD02=F99;ID01=1E;"
            + "ID02=1B;PDC=1EC;PD01=108;PD02=E4;TNF=138C;UL1=900;TKK=1F;KDY=5;KT0=14ED8|216B}";

    // all commands don't fit into a single request (max. 16 per request), so a poll needs two requests
    private static final int REQUESTS_PER_POLL = 2;

    private static final long NO_TIME_LIMIT = 60_000;

    @FunctionalInterface
    private interface Behaviour {
        /**
         * @param connectionNumber 1 for the first connection
         */
        void handle(int connectionNumber, Socket socket) throws IOException, InterruptedException;
    }

    /**
     * a fake device accepting one connection at a time (like the real one)
     */
    private static class FakeSolarMax implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final AtomicInteger connections = new AtomicInteger();
        private final Thread thread;

        FakeSolarMax(Behaviour behaviour) throws IOException {
            serverSocket = new ServerSocket(0, 50, InetAddress.getByName(HOST));
            thread = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    Socket socket;
                    try {
                        socket = serverSocket.accept();
                    } catch (IOException e) {
                        return; // server socket closed
                    }
                    try (Socket s = socket) {
                        behaviour.handle(connections.incrementAndGet(), s);
                    } catch (IOException e) {
                        // client went away, wait for the next connection
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        int connections() {
            return connections.get();
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
            thread.interrupt();
        }
    }

    private static void readRequest(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        // read up to the end of the request
        int b = in.read();
        while (b != -1 && b != '}') {
            b = in.read();
        }
    }

    private static void answer(Socket socket) throws IOException {
        readRequest(socket);
        OutputStream out = socket.getOutputStream();
        out.write(RESPONSE.getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    private static void neverAnswer(Socket socket) throws IOException {
        readRequest(socket);
        // block until the client gives up and closes the connection
        InputStream in = socket.getInputStream();
        int b = in.read();
        while (b != -1) {
            b = in.read();
        }
    }

    private static String message(Exception e) {
        return Objects.requireNonNullElse(e.getMessage(), "");
    }

    private static SolarMaxData poll(FakeSolarMax device, SolarMaxConnectionSettings settings)
            throws SolarMaxException {
        return SolarMaxConnector.getAllValuesFromSolarMax(HOST, device.port(), 1, settings);
    }

    @Test
    public void pollSucceeds() throws Exception {
        try (FakeSolarMax device = new FakeSolarMax((n, s) -> answer(s))) {
            SolarMaxData data = poll(device, new SolarMaxConnectionSettings(1000, 1, 50, NO_TIME_LIMIT));

            assertTrue(data.wasCommunicationSuccessful());
            assertEquals(20008, data.get(SolarMaxCommandKey.operatingState));
            assertEquals(REQUESTS_PER_POLL, device.connections());
        }
    }

    @Test
    public void connectionClosedByDeviceIsRetried() throws Exception {
        // the device drops the first connection, then answers normally
        Behaviour dropFirst = (n, s) -> {
            if (n > 1) {
                answer(s);
            }
        };
        try (FakeSolarMax device = new FakeSolarMax(dropFirst)) {
            SolarMaxData data = poll(device, new SolarMaxConnectionSettings(1000, 1, 50, NO_TIME_LIMIT));

            assertTrue(data.wasCommunicationSuccessful());
            assertEquals(20008, data.get(SolarMaxCommandKey.operatingState));
            assertEquals(1 + REQUESTS_PER_POLL, device.connections());
        }
    }

    @Test
    public void withoutRetriesADroppedConnectionFailsThePoll() throws Exception {
        Behaviour dropFirst = (n, s) -> {
            if (n > 1) {
                answer(s);
            }
        };
        try (FakeSolarMax device = new FakeSolarMax(dropFirst)) {
            assertThrows(SolarMaxException.class,
                    () -> poll(device, new SolarMaxConnectionSettings(1000, 0, 50, NO_TIME_LIMIT)));
            assertEquals(1, device.connections());
        }
    }

    @Test
    public void slowAnswerWithinTimeoutSucceeds() throws Exception {
        Behaviour slow = (n, s) -> {
            readRequest(s);
            Thread.sleep(400);
            s.getOutputStream().write(RESPONSE.getBytes(StandardCharsets.US_ASCII));
        };
        try (FakeSolarMax device = new FakeSolarMax(slow)) {
            SolarMaxData data = poll(device, new SolarMaxConnectionSettings(1000, 0, 50, NO_TIME_LIMIT));

            assertTrue(data.wasCommunicationSuccessful());
            assertEquals(REQUESTS_PER_POLL, device.connections());
        }
    }

    @Test
    public void slowAnswerBeyondTimeoutFailsWithCause() throws Exception {
        Behaviour slow = (n, s) -> {
            readRequest(s);
            Thread.sleep(400);
            s.getOutputStream().write(RESPONSE.getBytes(StandardCharsets.US_ASCII));
        };
        try (FakeSolarMax device = new FakeSolarMax(slow)) {
            SolarMaxException e = assertThrows(SolarMaxException.class,
                    () -> poll(device, new SolarMaxConnectionSettings(150, 0, 50, NO_TIME_LIMIT)));
            assertTrue(message(e).contains("SocketTimeoutException"), message(e));
        }
    }

    /**
     * sends the response byte by byte with a pause before each byte that is shorter than the timeout, so each single
     * read succeeds
     */
    private static Behaviour trickle(long pauseMillis) {
        return (n, s) -> {
            readRequest(s);
            OutputStream out = s.getOutputStream();
            for (byte b : RESPONSE.getBytes(StandardCharsets.US_ASCII)) {
                Thread.sleep(pauseMillis);
                out.write(b);
                out.flush();
            }
        };
    }

    @Test
    public void tricklingAnswerIsLimitedToTheTimeout() throws Exception {
        // without a limit for the complete response this would take more than 150 characters * 20 ms = 3 s
        try (FakeSolarMax device = new FakeSolarMax(trickle(20))) {
            long start = System.nanoTime();
            SolarMaxException e = assertThrows(SolarMaxException.class,
                    () -> poll(device, new SolarMaxConnectionSettings(300, 0, 50, NO_TIME_LIMIT)));
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

            assertEquals(1, device.connections());
            assertTrue(elapsedMillis >= 300, "elapsed " + elapsedMillis + " ms");
            assertTrue(elapsedMillis < 1000, "elapsed " + elapsedMillis + " ms");
            assertTrue(message(e).contains("SocketTimeoutException"), message(e));
        }
    }

    @Test
    public void retryOfTricklingAnswerStaysWithinTheTimeBudget() throws Exception {
        // budget 1000 ms: the first attempt is cut off after 300 ms, so the retry (50 ms + max. 2 * 300 ms) still fits.
        // Before the response was limited as a whole, the first attempt alone took longer than the budget.
        try (FakeSolarMax device = new FakeSolarMax(trickle(20))) {
            long start = System.nanoTime();
            assertThrows(SolarMaxException.class, () -> poll(device, new SolarMaxConnectionSettings(300, 1, 50, 1000)));
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

            assertEquals(2, device.connections());
            assertTrue(elapsedMillis < 1000, "elapsed " + elapsedMillis + " ms");
        }
    }

    @Test
    public void tricklingAnswerWithinTheTimeoutSucceeds() throws Exception {
        // about 150 characters * 2 ms = 0.3 s per request, well within the timeout
        try (FakeSolarMax device = new FakeSolarMax(trickle(2))) {
            SolarMaxData data = poll(device, new SolarMaxConnectionSettings(3000, 0, 50, NO_TIME_LIMIT));

            assertTrue(data.wasCommunicationSuccessful());
            assertEquals(REQUESTS_PER_POLL, device.connections());
        }
    }

    @Test
    public void deviceNeverAnsweringFailsAfterTimeoutTimesAttempts() throws Exception {
        try (FakeSolarMax device = new FakeSolarMax((n, s) -> neverAnswer(s))) {
            long start = System.nanoTime();
            SolarMaxException e = assertThrows(SolarMaxException.class,
                    () -> poll(device, new SolarMaxConnectionSettings(300, 1, 100, NO_TIME_LIMIT)));
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

            // first attempt + 1 retry of the first request, each waiting for the timeout, plus the retry delay
            assertEquals(2, device.connections());
            assertTrue(elapsedMillis >= 650, "elapsed " + elapsedMillis + " ms");
            assertTrue(elapsedMillis < 3000, "elapsed " + elapsedMillis + " ms");
            assertTrue(message(e).contains("SocketTimeoutException"), message(e));
        }
    }

    @Test
    public void retryIsSkippedIfItDoesNotFitIntoTheTimeBudget() throws Exception {
        try (FakeSolarMax device = new FakeSolarMax((n, s) -> neverAnswer(s))) {
            // after the first timeout (300 ms) only 200 ms of the budget are left, a retry could take 100 + 2 * 300 ms
            assertThrows(SolarMaxException.class, () -> poll(device, new SolarMaxConnectionSettings(300, 1, 100, 500)));
            assertEquals(1, device.connections());
        }
    }

    @Test
    public void refusedConnectionNamesTheCause() throws Exception {
        int port;
        try (ServerSocket closed = new ServerSocket(0, 1, InetAddress.getByName(HOST))) {
            port = closed.getLocalPort();
        }
        SolarMaxException e = assertThrows(SolarMaxException.class, () -> SolarMaxConnector
                .getAllValuesFromSolarMax(HOST, port, 1, new SolarMaxConnectionSettings(1000, 0, 50, NO_TIME_LIMIT)));

        assertInstanceOf(SolarMaxConnectionException.class, e);
        assertTrue(message(e).startsWith("Error connecting to portNumber '" + port + "' on host '" + HOST + "': "),
                message(e));
        assertTrue(message(e).contains("ConnectException"), message(e));
    }

    @Test
    public void describeIncludesTypeAndMessage() {
        assertEquals("SocketTimeoutException: connect timed out",
                SolarMaxConnector.describe(new java.net.SocketTimeoutException("connect timed out")));
        assertEquals("IOException", SolarMaxConnector.describe(new IOException()));
    }
}
