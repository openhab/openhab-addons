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
package org.openhab.binding.lutron.internal.hw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.lutron.internal.hw.HwTelnetConnection.HwLoginException;

/**
 * Tests for {@link HwTelnetConnection} against a fake HomeWorks processor on the loopback interface.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public class HwTelnetConnectionTest {
    private static final long WAIT_SECONDS = 20;

    /**
     * A processor that accepts one connection and exposes its streams to the test.
     */
    private static class FakeProcessor implements AutoCloseable {
        final ServerSocket server;
        final BlockingQueue<Socket> accepted = new LinkedBlockingQueue<>();
        private @Nullable Socket clientSocket;

        FakeProcessor() throws IOException {
            server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
            // Bounded, so that a regression fails the test instead of hanging the build
            server.setSoTimeout((int) TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
            Thread acceptor = new Thread(() -> {
                try {
                    Socket socket = server.accept();
                    socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
                    accepted.add(socket);
                } catch (IOException e) {
                    // closed by the test
                }
            }, "fake-processor-accept");
            acceptor.setDaemon(true);
            acceptor.start();
        }

        int port() {
            return server.getLocalPort();
        }

        synchronized Socket socket() throws InterruptedException {
            Socket socket = clientSocket;
            if (socket == null) {
                socket = accepted.poll(WAIT_SECONDS, TimeUnit.SECONDS);
                assertNotNull(socket, "client did not connect");
                clientSocket = socket;
            }
            return socket;
        }

        void send(String text) throws IOException, InterruptedException {
            OutputStream out = socket().getOutputStream();
            out.write(text.getBytes(StandardCharsets.US_ASCII));
            out.flush();
        }

        @Override
        public synchronized void close() throws IOException {
            Socket socket = clientSocket != null ? clientSocket : accepted.peek();
            if (socket != null) {
                socket.close();
            }
            server.close();
        }
    }

    private static class RecordingListener implements HwTelnetConnection.Listener {
        final BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        final CountDownLatch closed = new CountDownLatch(1);

        @Override
        public void lineReceived(HwTelnetConnection source, String line) {
            lines.add(line);
        }

        @Override
        public void connectionClosed(HwTelnetConnection source, String reason) {
            closed.countDown();
        }

        String nextLine() throws InterruptedException {
            String line = lines.poll(WAIT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(line, "no line received");
            return line;
        }
    }

    private @Nullable FakeProcessor processor;
    private @Nullable HwTelnetConnection connection;

    @BeforeEach
    public void setUp() throws IOException {
        processor = new FakeProcessor();
    }

    @AfterEach
    public void tearDown() throws IOException {
        HwTelnetConnection current = connection;
        if (current != null) {
            current.close();
        }
        FakeProcessor fake = processor;
        if (fake != null) {
            fake.close();
        }
    }

    private FakeProcessor fake() {
        FakeProcessor fake = processor;
        assertNotNull(fake);
        return fake;
    }

    private HwTelnetConnection connect(@Nullable String user, @Nullable String password,
            HwTelnetConnection.Listener listener) {
        HwTelnetConnection created = new HwTelnetConnection("127.0.0.1", fake().port(), user, password, listener,
                "test-reader");
        connection = created;
        return created;
    }

    private BufferedReader reader() throws IOException, InterruptedException {
        return new BufferedReader(new InputStreamReader(fake().socket().getInputStream(), StandardCharsets.US_ASCII));
    }

    /**
     * Opens the connection on a helper thread, because open() blocks until the login has finished.
     */
    private Thread openInBackground(HwTelnetConnection conn, BlockingQueue<Object> outcome) {
        Thread opener = new Thread(() -> {
            try {
                conn.open();
                outcome.add("open");
            } catch (IOException e) {
                outcome.add(e);
            }
        }, "test-opener");
        opener.setDaemon(true);
        opener.start();
        return opener;
    }

    @Test
    public void loginSendsUserAndPasswordAndStripsPrompts() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect("lutron", "secret", listener);
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);

        fake().send("\r\nLOGIN: ");
        BufferedReader in = reader();
        assertEquals("lutron, secret", in.readLine());
        fake().send("\r\nlogin successful\r\n\r\nLNET> ");

        assertEquals("open", outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS));
        assertTrue(conn.isReady());

        fake().send("\r\nKBP, [01:05:25],  1\r\n\r\nKBR, [01:05:25],  1\r\n\r\nLNET> DL, [01:01:03:06:01],   0\r\n");
        assertEquals("KBP, [01:05:25],  1", listener.nextLine());
        assertEquals("KBR, [01:05:25],  1", listener.nextLine());
        assertEquals("DL, [01:01:03:06:01],   0", listener.nextLine());
        assertTrue(listener.lines.isEmpty(), "login and prompt text must not reach the listener");
    }

    @Test
    public void lineSplitAcrossReadsIsReassembled() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect("lutron", "secret", listener);
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);

        fake().send("LOGIN: ");
        BufferedReader in = reader();
        in.readLine();
        fake().send("login successful\r\nLNET> ");
        assertEquals("open", outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS));

        fake().send("KBP, [01:05");
        fake().send(":25],  6\r\n");
        assertEquals("KBP, [01:05:25],  6", listener.nextLine());
    }

    @Test
    public void rejectedLoginFailsWithoutRetrying() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect("lutron", "wrong", listener);
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);

        fake().send("LOGIN: ");
        BufferedReader in = reader();
        assertEquals("lutron, wrong", in.readLine());
        // The processor shows a new login prompt after a rejection. The connection must not answer it.
        fake().send("login incorrect\r\n\r\nLOGIN: ");

        Object result = outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS);
        assertTrue(result instanceof HwLoginException, "expected a login failure but got " + result);
        assertFalse(conn.isReady());

        // Closing the connection ends the stream; no second login line may have been sent.
        conn.close();
        assertNull(in.readLine());
    }

    @Test
    public void loginPromptWithoutCredentialsFails() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect(null, null, listener);
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);

        fake().send("LOGIN: ");

        Object result = outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS);
        assertTrue(result instanceof HwLoginException, "expected a login failure but got " + result);
    }

    @Test
    public void commandPromptWithoutLoginIsReady() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect(null, null, listener);
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);

        fake().send("LNET> ");

        assertEquals("open", outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS));
        assertTrue(conn.isReady());
    }

    @Test
    public void delayedLoginPromptStillAuthenticatesBeforeSendingCommands() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect("lutron", "secret", listener);
        // Plenty of margin over the deliberate silence below, for slow runners
        conn.loginTimeoutMs = 30_000;
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);
        // Synchronize on the connection, then keep the processor silent beyond the old three-second fallback.
        BufferedReader in = reader();
        assertNull(outcome.poll(5, TimeUnit.SECONDS), "silence must not complete the login");
        assertFalse(conn.isReady());
        assertThrows(IOException.class, () -> conn.send("DLMON"));

        fake().send("LOGIN: ");
        assertEquals("lutron, secret", in.readLine());
        fake().send("login successful\r\n");

        assertEquals("open", outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS));
        assertTrue(conn.isReady());
    }

    @Test
    public void silentProcessorTimesOutWithoutSendingCommands() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect("lutron", "secret", listener);
        conn.loginTimeoutMs = 500;
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);
        BufferedReader in = reader();

        Object result = outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS);

        assertTrue(result instanceof IOException && !(result instanceof HwLoginException),
                "expected a retryable communication failure but got " + result);
        assertFalse(conn.isReady());
        assertNull(in.readLine(), "no credentials or startup commands may be sent without a prompt");
    }

    @Test
    public void sendWritesCommandWithLineEnding() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect("lutron", "secret", listener);
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);

        fake().send("LOGIN: ");
        BufferedReader in = reader();
        in.readLine();
        fake().send("login successful\r\nLNET> ");
        assertEquals("open", outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS));

        conn.send("KBMON");
        assertEquals("KBMON", in.readLine());
    }

    @Test
    public void sendBeforeLoginIsRejected() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect("lutron", "secret", listener);

        assertThrows(IOException.class, () -> conn.send("KBMON"));
    }

    @Test
    public void processorClosingTheConnectionIsReported() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect("lutron", "secret", listener);
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);

        fake().send("LOGIN: ");
        BufferedReader in = reader();
        in.readLine();
        fake().send("login successful\r\nLNET> ");
        assertEquals("open", outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS));

        fake().socket().close();

        assertTrue(listener.closed.await(WAIT_SECONDS, TimeUnit.SECONDS), "close was not reported");
        assertFalse(conn.isReady());
    }

    @Test
    public void closingOurselvesIsNotReportedAsALostConnection() throws Exception {
        RecordingListener listener = new RecordingListener();
        HwTelnetConnection conn = connect("lutron", "secret", listener);
        BlockingQueue<Object> outcome = new LinkedBlockingQueue<>();
        openInBackground(conn, outcome);

        fake().send("LOGIN: ");
        BufferedReader in = reader();
        in.readLine();
        fake().send("login successful\r\nLNET> ");
        assertEquals("open", outcome.poll(WAIT_SECONDS, TimeUnit.SECONDS));

        conn.close();

        // The processor sees the connection end, and nothing was reported to the listener.
        assertNull(in.readLine());
        assertFalse(listener.closed.await(1, TimeUnit.SECONDS));
    }
}
