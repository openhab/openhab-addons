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
package org.openhab.binding.e3dc.internal.rscp;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.e3dc.internal.rscp.FakeRscpDevice.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Exercises the client against a fake device on the loopback interface.
 *
 * <p>
 * The fake device decodes the request byte by byte, so the documented frame and item layout is
 * verified against an independent construction rather than against the client's own decoder.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Timeout(value = 20, unit = TimeUnit.SECONDS)
class RscpClientTest {

    private static final String USER = "bernd@example.com";
    private static final String PASSWORD = "secret";

    /** Starts a fake device that runs the given script and returns the bound port. */
    private static int startDevice(DeviceScript script) throws IOException {
        ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        int port = server.getLocalPort();
        Thread thread = new Thread(() -> {
            try (Socket socket = server.accept()) {
                socket.setSoTimeout(10_000);
                script.serve(socket);
            } catch (Exception e) {
                script.failure.set(e);
            } finally {
                script.finished.countDown();
                try {
                    server.close();
                } catch (IOException ignored) {
                    // best effort
                }
            }
        }, "fake-e3dc-device");
        thread.setDaemon(true);
        thread.start();
        return port;
    }

    private abstract static class DeviceScript {
        final AtomicReference<@Nullable Exception> failure = new AtomicReference<>();
        final CountDownLatch finished = new CountDownLatch(1);

        abstract void serve(Socket socket) throws Exception;

        void await() throws Exception {
            assertTrue(finished.await(15, TimeUnit.SECONDS), "the fake device did not finish");
            Exception problem = failure.get();
            if (problem != null) {
                throw problem;
            }
        }
    }

    @Test
    void authenticationRequestFollowsTheDocumentedLayout() throws Exception {
        AtomicReference<String> observedUser = new AtomicReference<>();
        AtomicReference<Integer> observedContainerLength = new AtomicReference<>();

        DeviceScript script = new DeviceScript() {
            @Override
            void serve(Socket socket) throws Exception {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                RscpCipher cipher = new RscpCipher(RSCP_PASSWORD.getBytes(StandardCharsets.UTF_8));

                byte[] request = readEncryptedFrame(in, cipher);
                ByteBuffer buffer = ByteBuffer.wrap(request).order(ByteOrder.LITTLE_ENDIAN);
                assertEquals((byte) 0xE3, buffer.get(), "frame magic, first byte");
                assertEquals((byte) 0xDC, buffer.get(), "frame magic, second byte");
                assertEquals((byte) 0x00, buffer.get(), "control byte 0 is zero");
                int ctrl = buffer.get() & 0xFF;
                assertEquals(RscpCodec.CTRL_CRC, ctrl & RscpCodec.CTRL_CRC, "the CRC bit must be set");
                buffer.getLong();
                buffer.getInt();
                int payloadLength = buffer.getShort() & 0xFFFF;

                // the authentication is the only element, so it is the whole payload: no container with
                // tag 0 wraps it, because a frame carries its elements side by side
                assertEquals(RscpTag.RSCP_REQ_AUTHENTICATION, buffer.getInt(), "the authentication tag");
                assertEquals(RscpType.CONTAINER.getCode(), buffer.get() & 0xFF, "its type");
                int authenticationLength = buffer.getShort() & 0xFFFF;
                observedContainerLength.set(authenticationLength);
                assertEquals(payloadLength, authenticationLength + 7,
                        "the payload length counts the element header as well");

                assertEquals(RscpTag.RSCP_AUTHENTICATION_USER, buffer.getInt(), "user tag");
                assertEquals(RscpType.CSTRING.getCode(), buffer.get() & 0xFF, "user type");
                int userLength = buffer.getShort() & 0xFFFF;
                byte[] userBytes = new byte[userLength];
                buffer.get(userBytes);
                observedUser.set(new String(userBytes, StandardCharsets.UTF_8));
                assertNotEquals(0, userBytes[userLength - 1], "the user name carries no terminator");

                assertEquals(RscpTag.RSCP_AUTHENTICATION_PASSWORD, buffer.getInt(), "password tag");
                assertEquals(RscpType.CSTRING.getCode(), buffer.get() & 0xFF, "password type");
                int passwordLength = buffer.getShort() & 0xFFFF;
                buffer.position(buffer.position() + passwordLength);

                assertEquals(4, request.length - buffer.position(),
                        "only the CRC32 trailer may follow the credentials");

                out.write(cipher.encrypt(okResponse()));
                out.flush();
            }
        };
        int port = startDevice(script);

        try (RscpClient client = new RscpClient("127.0.0.1", port, USER, PASSWORD, RSCP_PASSWORD, 5000)) {
            client.connect();
            assertTrue(client.isConnected());
        }
        script.await();

        assertEquals(USER, observedUser.get());
        // each credential is a 7 byte header plus the bare text, without a terminator
        int expectedContainer = 7 + USER.getBytes(StandardCharsets.UTF_8).length + 7
                + PASSWORD.getBytes(StandardCharsets.UTF_8).length;
        assertEquals(expectedContainer, observedContainerLength.get().intValue(),
                "the container must contain exactly the two credentials");
    }

    @Test
    void rejectedCredentialsReportAnAuthenticationFailure() throws Exception {
        DeviceScript script = new DeviceScript() {
            @Override
            void serve(Socket socket) throws Exception {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                RscpCipher cipher = new RscpCipher(RSCP_PASSWORD.getBytes(StandardCharsets.UTF_8));
                readEncryptedFrame(in, cipher);

                byte[] errorPayload = item(RscpTag.RSCP_REQ_AUTHENTICATION, RscpType.ERROR.getCode(),
                        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(2).array());
                out.write(cipher.encrypt(frame(errorPayload)));
                out.flush();
            }
        };
        int port = startDevice(script);

        RscpClient client = new RscpClient("127.0.0.1", port, USER, "wrong", RSCP_PASSWORD, 5000);
        try {
            RscpException exception = assertThrows(RscpException.class, client::connect);
            String message = String.valueOf(exception.getMessage());
            assertTrue(message.contains("credentials"), "the message must name the credentials: " + message);
        } finally {
            client.close();
        }
        script.await();
    }

    @Test
    void aLoginWithoutAccessLevelIsRejected() throws Exception {
        DeviceScript script = new DeviceScript() {
            @Override
            void serve(Socket socket) throws Exception {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                RscpCipher cipher = new RscpCipher(RSCP_PASSWORD.getBytes(StandardCharsets.UTF_8));
                readEncryptedFrame(in, cipher);

                // a wrong login is answered with an access level of zero, which is no error item
                out.write(cipher.encrypt(
                        frame(item(RscpTag.RSCP_AUTHENTICATION, RscpType.UCHAR8.getCode(), new byte[] { 0 }))));
                out.flush();
            }
        };
        int port = startDevice(script);

        RscpClient client = new RscpClient("127.0.0.1", port, USER, PASSWORD, RSCP_PASSWORD, 5000);
        try {
            RscpException exception = assertThrows(RscpException.class, client::connect);
            String message = String.valueOf(exception.getMessage());
            assertTrue(message.contains("access level"), "the message must name the access level: " + message);
            assertFalse(client.isConnected(), "a rejected login must not keep the connection open");
        } finally {
            client.close();
        }
        script.await();
    }

    @Test
    void aResponseWithoutTheAuthenticationLevelIsRejected() throws Exception {
        DeviceScript script = new DeviceScript() {
            @Override
            void serve(Socket socket) throws Exception {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                RscpCipher cipher = new RscpCipher(RSCP_PASSWORD.getBytes(StandardCharsets.UTF_8));
                readEncryptedFrame(in, cipher);

                // without the level item nothing proves that the login was accepted
                out.write(cipher.encrypt(okResponseWithoutLevel()));
                out.flush();
            }
        };
        int port = startDevice(script);

        RscpClient client = new RscpClient("127.0.0.1", port, USER, PASSWORD, RSCP_PASSWORD, 5000);
        try {
            assertThrows(RscpException.class, client::connect);
            assertFalse(client.isConnected());
        } finally {
            client.close();
        }
        script.await();
    }

    @Test
    void aResponseIsDecodedAcrossSeveralCipherBlocks() throws Exception {
        DeviceScript script = new DeviceScript() {
            @Override
            void serve(Socket socket) throws Exception {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                RscpCipher cipher = new RscpCipher(RSCP_PASSWORD.getBytes(StandardCharsets.UTF_8));

                // the authentication request
                readEncryptedFrame(in, cipher);
                out.write(cipher.encrypt(okResponse()));
                out.flush();

                // the tag request, answered with a payload spanning several cipher blocks
                readEncryptedFrame(in, cipher);
                ByteArrayOutputStream children = new ByteArrayOutputStream();
                for (int i = 0; i < 20; i++) {
                    children.write(item(2000 + i, RscpType.INT32.getCode(),
                            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(100 + i).array()));
                }
                byte[] response = frame(
                        item(RscpTag.EMS_GET_POWER_SETTINGS, RscpType.CONTAINER.getCode(), children.toByteArray()));
                assertTrue(response.length > Rijndael256.BLOCK * 2, "the response must span several blocks");

                // deliver it in two chunks so the client has to buffer across block boundaries
                byte[] encrypted = cipher.encrypt(response);
                out.write(encrypted, 0, Rijndael256.BLOCK);
                out.flush();
                // a pause between the chunks makes it likely that they arrive separately; the result does not depend on
                // it
                Thread.sleep(100);
                out.write(encrypted, Rijndael256.BLOCK, encrypted.length - Rijndael256.BLOCK);
                out.flush();
            }
        };
        int port = startDevice(script);

        try (RscpClient client = new RscpClient("127.0.0.1", port, USER, PASSWORD, RSCP_PASSWORD, 5000)) {
            client.connect();
            RscpValue response = client.requestTag(RscpTag.EMS_REQ_GET_POWER_SETTINGS);

            assertEquals(RscpType.CONTAINER, response.getType());
            assertEquals(RscpTag.EMS_GET_POWER_SETTINGS, response.getTag());
            assertEquals(20, response.getChildren().size(), "every nested item must survive the response");

            Number first = response.findNumber(2000);
            assertNotNull(first, "the first nested item must carry a number");
            assertEquals(100, first.intValue());

            // a value from the far end of a response that spans several cipher blocks
            Number last = response.findNumber(2019);
            assertNotNull(last, "the last nested item must carry a number");
            assertEquals(119, last.intValue());
        }
        script.await();
    }

    @Test
    void concurrentRequestsAreSentOneAtATime() throws Exception {
        AtomicReference<@Nullable Boolean> overlapped = new AtomicReference<>();
        DeviceScript script = new DeviceScript() {
            @Override
            void serve(Socket socket) throws Exception {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                RscpCipher cipher = new RscpCipher(RSCP_PASSWORD.getBytes(StandardCharsets.UTF_8));

                readEncryptedFrame(in, cipher);
                out.write(cipher.encrypt(okResponse()));
                out.flush();

                for (int i = 0; i < 2; i++) {
                    readEncryptedFrame(in, cipher);
                    // the second request would already be on the wire here if the client did not wait for the answer;
                    // a request that must not arrive can only be shown by waiting, so the pause is generous for slow
                    // runners
                    Thread.sleep(500);
                    if (in.available() > 0) {
                        overlapped.set(Boolean.TRUE);
                    }
                    out.write(cipher.encrypt(okResponse()));
                    out.flush();
                }
            }
        };
        int port = startDevice(script);

        try (RscpClient client = new RscpClient("127.0.0.1", port, USER, PASSWORD, RSCP_PASSWORD, 5000)) {
            client.connect();
            AtomicReference<@Nullable Exception> failure = new AtomicReference<>();
            Runnable ask = () -> {
                try {
                    client.requestTag(RscpTag.EMS_REQ_GET_POWER_SETTINGS);
                } catch (RscpException e) {
                    failure.set(e);
                }
            };
            Thread first = new Thread(ask, "request-1");
            Thread second = new Thread(ask, "request-2");
            first.setDaemon(true);
            second.setDaemon(true);
            first.start();
            second.start();
            first.join(10_000);
            second.join(10_000);

            assertFalse(first.isAlive(), "the first request must have finished");
            assertFalse(second.isAlive(), "the second request must have finished");
            assertNull(failure.get(), "both requests must be answered");
        }
        script.await();
        assertNull(overlapped.get(), "the second request must wait for the answer of the first");
    }

    @Test
    void requestingWithoutAConnectionFails() {
        try (RscpClient client = new RscpClient("127.0.0.1", 1, USER, PASSWORD, RSCP_PASSWORD, 1000)) {
            assertFalse(client.isConnected());
            assertThrows(RscpException.class, () -> client.requestTag(RscpTag.EMS_REQ_POWER_PV));
        }
    }

    @Test
    void aFailedConnectionAttemptClosesItsSocket() {
        FakeSocket socket = new FakeSocket(false);
        RscpClient client = new RscpClient("127.0.0.1", 1, USER, PASSWORD, RSCP_PASSWORD, 1000) {
            @Override
            Socket createSocket() {
                return socket;
            }
        };
        try {
            assertThrows(RscpException.class, client::connect);
            assertTrue(socket.isClosed(), "a failed connection attempt must close its socket");
        } finally {
            client.close();
        }
    }

    @Test
    void closingTheClientAbortsAPendingConnectionAttempt() throws Exception {
        FakeSocket socket = new FakeSocket(true);
        RscpClient client = new RscpClient("127.0.0.1", 1, USER, PASSWORD, RSCP_PASSWORD, 60_000) {
            @Override
            Socket createSocket() {
                return socket;
            }
        };
        AtomicReference<@Nullable Exception> failure = new AtomicReference<>();
        Thread connector = new Thread(() -> {
            try {
                client.connect();
            } catch (Exception e) {
                failure.set(e);
            }
        }, "connect-attempt");
        connector.setDaemon(true);
        try {
            connector.start();
            assertTrue(socket.awaitConnectStarted(), "the connection attempt must have started");
            client.close();
            connector.join(10_000);
            assertFalse(connector.isAlive(), "close() must abort a pending connect instead of leaving it blocked");
            assertTrue(failure.get() instanceof RscpException,
                    "the aborted attempt must fail with an RscpException: " + failure.get());
            assertTrue(socket.isClosed(), "the socket of the aborted attempt must be closed");
        } finally {
            client.close();
        }
    }

    // ------------------------------------------------------------------ helpers

    private static byte[] okResponse() {
        byte[] child = item(RscpTag.EMS_MAX_CHARGE_POWER, RscpType.INT32.getCode(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(9000).array());
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        payload.writeBytes(
                item(RscpTag.RSCP_AUTHENTICATION, RscpType.UCHAR8.getCode(), new byte[] { (byte) AUTH_LEVEL_USER }));
        payload.writeBytes(item(RscpTag.EMS_GET_POWER_SETTINGS, RscpType.CONTAINER.getCode(), child));
        return frame(payload.toByteArray());
    }

    private static byte[] okResponseWithoutLevel() {
        byte[] child = item(RscpTag.EMS_MAX_CHARGE_POWER, RscpType.INT32.getCode(),
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(9000).array());
        return frame(item(RscpTag.EMS_GET_POWER_SETTINGS, RscpType.CONTAINER.getCode(), child));
    }

    /**
     * A socket double that either fails the connect at once or blocks in it until the client closes the socket,
     * which is what a real socket's connect does when another thread closes the socket during the connect.
     */
    private static class FakeSocket extends Socket {

        private final boolean blockConnect;
        private final CountDownLatch connectStarted = new CountDownLatch(1);
        private final CountDownLatch closedWhileConnecting = new CountDownLatch(1);
        private volatile boolean closed;

        FakeSocket(boolean blockConnect) {
            this.blockConnect = blockConnect;
        }

        @Override
        public void connect(@Nullable SocketAddress endpoint, int timeout) throws IOException {
            connectStarted.countDown();
            if (!blockConnect) {
                throw new SocketTimeoutException("the fake device does not answer");
            }
            try {
                if (!closedWhileConnecting.await(5, TimeUnit.SECONDS)) {
                    throw new SocketTimeoutException("the client did not close the socket during the connect");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException("interrupted while connecting");
            }
            throw new SocketException("Socket closed");
        }

        @Override
        public void close() {
            closed = true;
            closedWhileConnecting.countDown();
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        boolean awaitConnectStarted() throws InterruptedException {
            return connectStarted.await(10, TimeUnit.SECONDS);
        }
    }
}
