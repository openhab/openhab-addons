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
package org.openhab.binding.roborock.internal.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.roborock.internal.RoborockException;
import org.openhab.binding.roborock.internal.util.ProtocolUtils;

@NonNullByDefault({})
class LocalDirectTransportTest {

    @Test
    void normalizeFrameStripsLengthPrefixForV10AndL01() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        Method normalize = LocalDirectTransport.class.getDeclaredMethod("normalizeFrame", byte[].class);
        normalize.setAccessible(true);

        byte[] v10 = buildRawFrame("1.0", 5, "{}".getBytes(StandardCharsets.UTF_8));
        byte[] prefixedV10 = withPrefix(v10);
        Object normalizedV10Object = normalize.invoke(transport, prefixedV10);
        assertNotNull(normalizedV10Object);
        byte[] normalizedV10 = (byte[]) normalizedV10Object;
        assertEquals(v10.length, normalizedV10.length);
        assertEquals('1', normalizedV10[0]);

        byte[] l01 = buildRawFrame("L01", 5, "{}".getBytes(StandardCharsets.UTF_8));
        byte[] prefixedL01 = withPrefix(l01);
        Object normalizedL01Object = normalize.invoke(transport, prefixedL01);
        assertNotNull(normalizedL01Object);
        byte[] normalizedL01 = (byte[]) normalizedL01Object;
        assertEquals(l01.length, normalizedL01.length);
        assertEquals('L', normalizedL01[0]);
    }

    @Test
    void isControlOrAckFrameRecognizesShortHelloAndZeroPayload() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        Method isControl = LocalDirectTransport.class.getDeclaredMethod("isControlOrAckFrame", byte[].class);
        isControl.setAccessible(true);

        byte[] shortHello = new byte[17];
        shortHello[0] = '1';
        shortHello[1] = '.';
        shortHello[2] = '0';
        ProtocolUtils.writeInt16BE(shortHello, 1, 15);
        assertTrue(Boolean.TRUE.equals(isControl.invoke(transport, shortHello)));

        byte[] zeroPayload = buildRawFrame("1.0", 5, new byte[0]);
        assertTrue(Boolean.TRUE.equals(isControl.invoke(transport, zeroPayload)));
    }

    @Test
    void extractResponseRequestIdReadsJsonIdFromRpcPayload() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        String localKey = "local-key";
        transport.updateContext(localKey, "127.0.0.1", 58867, "ABCDEF==");

        String rpc = "{\"id\":43210,\"result\":[{\"enabled\":1}]}";
        String payloadText = "{\"dps\":{\"102\":" + quoteJsonString(rpc) + "}}";
        byte[] frame = buildEncryptedV10Frame(localKey, payloadText.getBytes(StandardCharsets.UTF_8));

        Method extract = LocalDirectTransport.class.getDeclaredMethod("extractResponseRequestId", byte[].class);
        extract.setAccessible(true);

        Integer requestId = (Integer) extract.invoke(transport, frame);
        assertNotNull(requestId);
        assertEquals(43210, requestId.intValue());
    }

    @Test
    void extractResponseRequestIdSupportsDndAndCleanSummaryPayloadShapes() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        String localKey = "local-key";
        transport.updateContext(localKey, "127.0.0.1", 58867, "ABCDEF==");

        Method extract = LocalDirectTransport.class.getDeclaredMethod("extractResponseRequestId", byte[].class);
        extract.setAccessible(true);

        String dndRpc = "{\"id\":51001,\"result\":[{\"start_hour\":22,\"start_minute\":0,\"end_hour\":8,\"end_minute\":0,\"enabled\":1}]}";
        String dndPayload = "{\"dps\":{\"102\":" + quoteJsonString(dndRpc) + "}}";
        byte[] dndFrame = buildEncryptedV10Frame(localKey, dndPayload.getBytes(StandardCharsets.UTF_8));
        Integer dndId = (Integer) extract.invoke(transport, dndFrame);
        assertNotNull(dndId);
        assertEquals(51001, dndId.intValue());

        String summaryRpc = "{\"id\":51002,\"result\":[12345,6789000,9,[\"record-1\"]]}";
        String summaryPayload = "{\"dps\":{\"102\":" + quoteJsonString(summaryRpc) + "}}";
        byte[] summaryFrame = buildEncryptedV10Frame(localKey, summaryPayload.getBytes(StandardCharsets.UTF_8));
        Integer summaryId = (Integer) extract.invoke(transport, summaryFrame);
        assertNotNull(summaryId);
        assertEquals(51002, summaryId.intValue());
    }

    @Test
    void readMatchingResponseFrameSkipsAckAndMismatchedFrames() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        String localKey = "local-key";
        transport.updateContext(localKey, "127.0.0.1", 58867, "ABCDEF==");

        Method readMatching = LocalDirectTransport.class.getDeclaredMethod("readMatchingResponseFrame", Socket.class,
                int.class, String.class, int.class);
        readMatching.setAccessible(true);

        byte[] ackFrame = buildRawFrame("1.0", 5, new byte[0]);

        String wrongRpc = "{\"id\":62002,\"result\":[{\"enabled\":0}]}";
        String wrongPayload = "{\"dps\":{\"102\":" + quoteJsonString(wrongRpc) + "}}";
        byte[] wrongFrame = buildEncryptedV10Frame(localKey, wrongPayload.getBytes(StandardCharsets.UTF_8));

        String matchRpc = "{\"id\":62001,\"result\":[{\"enabled\":1}]}";
        String matchPayload = "{\"dps\":{\"102\":" + quoteJsonString(matchRpc) + "}}";
        byte[] matchingFrame = buildEncryptedV10Frame(localKey, matchPayload.getBytes(StandardCharsets.UTF_8));

        byte[] wireBytes = concat(withPrefix(ackFrame), withPrefix(wrongFrame), withPrefix(matchingFrame));
        FakeSocket fakeSocket = new FakeSocket(wireBytes);

        Object selectedObject = readMatching.invoke(transport, fakeSocket, 62001, "get_dnd_timer", 200);
        assertNotNull(selectedObject);
        byte[] selected = (byte[]) selectedObject;
        assertEquals(matchingFrame.length, selected.length);
        assertEquals(matchingFrame[0], selected[0]);
        assertEquals(matchingFrame[1], selected[1]);
        assertEquals(matchingFrame[2], selected[2]);
    }

    @Test
    void readMatchingResponseFrameDispatchesOutOfOrderFrameToConsumer() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        String localKey = "local-key";
        transport.updateContext(localKey, "127.0.0.1", 58867, "ABCDEF==");

        AtomicInteger dispatchedRequestId = new AtomicInteger(-1);
        transport.setMessageConsumer(frame -> {
            try {
                Method extract = LocalDirectTransport.class.getDeclaredMethod("extractResponseRequestId", byte[].class);
                extract.setAccessible(true);
                Integer requestId = (Integer) extract.invoke(transport, frame);
                if (requestId != null) {
                    dispatchedRequestId.set(requestId.intValue());
                }
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });

        Method readMatching = LocalDirectTransport.class.getDeclaredMethod("readMatchingResponseFrame", Socket.class,
                int.class, String.class, int.class);
        readMatching.setAccessible(true);

        String wrongRpc = "{\"id\":62002,\"result\":[{\"enabled\":0}]}";
        String wrongPayload = "{\"dps\":{\"102\":" + quoteJsonString(wrongRpc) + "}}";
        byte[] wrongFrame = buildEncryptedV10Frame(localKey, wrongPayload.getBytes(StandardCharsets.UTF_8));

        String matchRpc = "{\"id\":62001,\"result\":[{\"enabled\":1}]}";
        String matchPayload = "{\"dps\":{\"102\":" + quoteJsonString(matchRpc) + "}}";
        byte[] matchingFrame = buildEncryptedV10Frame(localKey, matchPayload.getBytes(StandardCharsets.UTF_8));

        byte[] wireBytes = concat(withPrefix(wrongFrame), withPrefix(matchingFrame));
        FakeSocket fakeSocket = new FakeSocket(wireBytes);

        Object selectedObject = readMatching.invoke(transport, fakeSocket, 62001, "get_dnd_timer", 200);
        assertNotNull(selectedObject);
        byte[] selected = (byte[]) selectedObject;
        assertEquals(62002, dispatchedRequestId.get());
        assertEquals(matchingFrame.length, selected.length);
    }

    @Test
    void readSingleFrameRejectsOversizedPrefixedLength() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        Method readSingleFrame = LocalDirectTransport.class.getDeclaredMethod("readSingleFrame", Socket.class);
        readSingleFrame.setAccessible(true);

        byte[] oversizedPrefix = new byte[] { 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        FakeSocket fakeSocket = new FakeSocket(oversizedPrefix);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> readSingleFrame.invoke(transport, fakeSocket));
        Throwable cause = exception.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof java.io.IOException);
        String causeMessage = cause.getMessage();
        assertNotNull(causeMessage);
        assertTrue(causeMessage.contains("prefix length"));
    }

    @Test
    void readSingleFrameRejectsNonPositivePayloadLength() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        Method readSingleFrame = LocalDirectTransport.class.getDeclaredMethod("readSingleFrame", Socket.class);
        readSingleFrame.setAccessible(true);

        byte[] malformedNonPrefixedHeader = new byte[19];
        malformedNonPrefixedHeader[0] = '1';
        malformedNonPrefixedHeader[1] = '.';
        malformedNonPrefixedHeader[2] = '0';
        ProtocolUtils.writeInt16BE(malformedNonPrefixedHeader, 5, 15);
        ProtocolUtils.writeInt16BE(malformedNonPrefixedHeader, 0, 17);
        FakeSocket fakeSocket = new FakeSocket(malformedNonPrefixedHeader);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> readSingleFrame.invoke(transport, fakeSocket));
        Throwable cause = exception.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof java.io.IOException);
        String causeMessage = cause.getMessage();
        assertNotNull(causeMessage);
        assertTrue(causeMessage.contains("payload length"));
    }

    @Test
    void readSingleFrameRejectsMalformedPrefixLengthWithoutAllocatingPayloadBuffer() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        Method readSingleFrame = LocalDirectTransport.class.getDeclaredMethod("readSingleFrame", Socket.class);
        readSingleFrame.setAccessible(true);

        byte[] zeroLengthPrefix = new byte[] { 0x00, 0x00, 0x00, 0x00 };
        FakeSocket fakeSocket = new FakeSocket(zeroLengthPrefix);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> readSingleFrame.invoke(transport, fakeSocket));
        Throwable cause = exception.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof java.io.IOException);
        String causeMessage = cause.getMessage();
        assertNotNull(causeMessage);
        assertTrue(causeMessage.contains("prefix length"));
    }

    @Test
    void updateContextInvalidatesExistingSession() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();

        Field sessionSocketField = LocalDirectTransport.class.getDeclaredField("sessionSocket");
        sessionSocketField.setAccessible(true);
        sessionSocketField.set(transport, new Socket());

        transport.updateContext("local-key", "127.0.0.1", 58867, "ABCDEF==");
        transport.updateContext("local-key", "192.168.1.10", 58867, "ABCDEF==");

        Object sessionSocket = sessionSocketField.get(transport);
        assertEquals(null, sessionSocket);
    }

    @Test
    void ensureConnectedSessionReusesHealthySocket() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();

        Field sessionSocketField = LocalDirectTransport.class.getDeclaredField("sessionSocket");
        sessionSocketField.setAccessible(true);

        ReusableFakeSocket fakeSocket = new ReusableFakeSocket();
        sessionSocketField.set(transport, fakeSocket);

        Method ensureConnected = LocalDirectTransport.class.getDeclaredMethod("ensureConnectedSession", String.class,
                int.class, boolean.class);
        ensureConnected.setAccessible(true);

        Object result = ensureConnected.invoke(transport, "get_status", 70001, false);
        assertEquals(fakeSocket, result);
    }

    @Test
    void sendCommandOnSessionInvalidatesOnFirstNoMatchTimeout() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        transport.updateContext("local-key", "127.0.0.1", 58867, "ABCDEF==");

        Field sessionSocketField = LocalDirectTransport.class.getDeclaredField("sessionSocket");
        sessionSocketField.setAccessible(true);
        TimeoutOnlySocket timeoutSocket = new TimeoutOnlySocket();
        sessionSocketField.set(transport, timeoutSocket);

        Method sendOnSession = LocalDirectTransport.class.getDeclaredMethod("sendCommandOnSession", String.class,
                String.class, int.class, boolean.class);
        sendOnSession.setAccessible(true);

        int requestId = 62011;
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> sendOnSession.invoke(transport, "get_status", "[]", requestId, false));
        assertTrue(exception.getCause() instanceof SocketTimeoutException);
        assertEquals(null, sessionSocketField.get(transport));
        assertTrue(timeoutSocket.wasClosed());
    }

    @Test
    void sendCommandOnSessionInvalidatesOnNoMatchTimeoutWithoutThresholdState() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        transport.updateContext("local-key", "127.0.0.1", 58867, "ABCDEF==");

        Field sessionSocketField = LocalDirectTransport.class.getDeclaredField("sessionSocket");
        sessionSocketField.setAccessible(true);
        TimeoutOnlySocket timeoutSocket = new TimeoutOnlySocket();
        sessionSocketField.set(transport, timeoutSocket);

        Method sendOnSession = LocalDirectTransport.class.getDeclaredMethod("sendCommandOnSession", String.class,
                String.class, int.class, boolean.class);
        sendOnSession.setAccessible(true);

        int requestId = 62012;
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> sendOnSession.invoke(transport, "get_status", "[]", requestId, false));
        assertTrue(exception.getCause() instanceof SocketTimeoutException);
        assertEquals(null, sessionSocketField.get(transport));
        assertTrue(timeoutSocket.wasClosed());
    }

    @Test
    void sendCommandRetriesOnceAfterNoMatchTimeout() throws Exception {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            byte[] helloResponse = withPrefix(buildHelloResponseFrame("1.0", 0x12345678));

            ScriptedSocket firstSocket = new ScriptedSocket(new HelloThenTimeoutInputStream(helloResponse));

            String localKey = "local-key";
            String rpcPayload = "{\"id\":62021,\"result\":[{\"state\":8}]}";
            String payload = "{\"dps\":{\"102\":" + quoteJsonString(rpcPayload) + "}}";
            byte[] matchingResponse = withPrefix(
                    buildEncryptedV10Frame(localKey, payload.getBytes(StandardCharsets.UTF_8)));
            ScriptedSocket secondSocket = new ScriptedSocket(
                    new ByteArrayInputStream(concat(helloResponse, matchingResponse)));

            AtomicInteger supplierCalls = new AtomicInteger();
            Supplier<Socket> socketSupplier = () -> supplierCalls.getAndIncrement() == 0 ? firstSocket : secondSocket;

            LocalDirectTransport transport = new LocalDirectTransport(scheduler, 0, System::nanoTime, millis -> {
            }, socketSupplier);
            transport.updateContext(localKey, "127.0.0.1", 58867, "ABCDEF==");

            int result = transport.sendCommand("get_status", "[]", 62021);
            assertEquals(62021, result);
            assertEquals(2, supplierCalls.get());
            assertTrue(firstSocket.wasClosed());

            Field sessionSocketField = LocalDirectTransport.class.getDeclaredField("sessionSocket");
            sessionSocketField.setAccessible(true);
            assertEquals(secondSocket, sessionSocketField.get(transport));
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    void sendCommandNoMatchTimeoutFailsAfterSingleRetry() throws Exception {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            byte[] helloResponse = withPrefix(buildHelloResponseFrame("1.0", 0x12345678));
            ScriptedSocket firstSocket = new ScriptedSocket(new HelloThenTimeoutInputStream(helloResponse));
            ScriptedSocket secondSocket = new ScriptedSocket(new HelloThenTimeoutInputStream(helloResponse));

            AtomicInteger supplierCalls = new AtomicInteger();
            Supplier<Socket> socketSupplier = () -> supplierCalls.getAndIncrement() == 0 ? firstSocket : secondSocket;

            LocalDirectTransport transport = new LocalDirectTransport(scheduler, 0, System::nanoTime, millis -> {
            }, socketSupplier);
            transport.updateContext("local-key", "127.0.0.1", 58867, "ABCDEF==");

            int result = transport.sendCommand("get_status", "[]", 62022);
            assertEquals(-1, result);
            assertEquals(2, supplierCalls.get());
            assertTrue(firstSocket.wasClosed());
            assertTrue(secondSocket.wasClosed());
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    void getConnectAndAckNonceReflectProtocolSession() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();

        Field protocolSessionField = LocalDirectTransport.class.getDeclaredField("protocolSession");
        protocolSessionField.setAccessible(true);
        Object protocolSession = protocolSessionField.get(transport);
        assertNotNull(protocolSession);

        Field connectNonceField = protocolSession.getClass().getDeclaredField("connectNonce");
        connectNonceField.setAccessible(true);
        int connectNonce = connectNonceField.getInt(protocolSession);

        Field ackNonceField = protocolSession.getClass().getDeclaredField("ackNonce");
        ackNonceField.setAccessible(true);
        ackNonceField.setInt(protocolSession, 12345);

        assertEquals(connectNonce, transport.getConnectNonce());
        assertEquals(12345, transport.getAckNonce());
    }

    @Test
    void l01JsonFrameNeedsHandshakeContextForDecode() throws Exception {
        String localKey = "abcdef1234567890";
        int connectNonce = 23456;
        int ackNonce = 65432;
        byte[] frame = buildL01Frame(localKey, connectNonce, ackNonce,
                "{\"id\":50001,\"result\":[{\"start_hour\":22,\"enabled\":1}]}");

        ProtocolUtils.DecodedMessage missingContext = ProtocolUtils.decodeMessage(frame, localKey, new byte[16],
                "ABCDEF==");
        assertTrue(missingContext instanceof ProtocolUtils.IgnoredResponse);

        ProtocolUtils.DecodedMessage withContext = ProtocolUtils.decodeMessage(frame, localKey, new byte[16],
                "ABCDEF==", connectNonce, ackNonce);
        assertTrue(withContext instanceof ProtocolUtils.JsonPayloadResponse);
    }

    @Test
    void extractResponseRequestIdDecodesL01WithNegativeAckNonce() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        String localKey = "abcdef1234567890";
        transport.updateContext(localKey, "127.0.0.1", 58867, "ABCDEF==");

        Field protocolSessionField = LocalDirectTransport.class.getDeclaredField("protocolSession");
        protocolSessionField.setAccessible(true);
        Object protocolSession = protocolSessionField.get(transport);
        assertNotNull(protocolSession);

        Field connectNonceField = protocolSession.getClass().getDeclaredField("connectNonce");
        connectNonceField.setAccessible(true);
        int connectNonce = connectNonceField.getInt(protocolSession);

        int ackNonce = 0x9ABCDEFF;
        Field ackNonceField = protocolSession.getClass().getDeclaredField("ackNonce");
        ackNonceField.setAccessible(true);
        ackNonceField.setInt(protocolSession, ackNonce);

        Field handshakeContextField = protocolSession.getClass().getDeclaredField("handshakeContextAvailable");
        handshakeContextField.setAccessible(true);
        handshakeContextField.setBoolean(protocolSession, true);

        byte[] frame = buildL01Frame(localKey, connectNonce, ackNonce, "{\"id\":62055,\"result\":[1]}");

        Method extract = LocalDirectTransport.class.getDeclaredMethod("extractResponseRequestId", byte[].class);
        extract.setAccessible(true);

        Integer requestId = (Integer) extract.invoke(transport, frame);
        assertNotNull(requestId);
        assertEquals(62055, requestId.intValue());
    }

    @Test
    void helloNegotiationRemembersLastGoodVariantAndFallsBack() throws Exception {
        LocalDirectTransport transport = new LocalDirectTransport();
        Method performHello = LocalDirectTransport.class.getDeclaredMethod("performHelloHandshake", Socket.class,
                java.io.OutputStream.class, String.class, int.class);
        performHello.setAccessible(true);

        ByteArrayOutputStream firstHelloWrites = new ByteArrayOutputStream();
        Socket firstSocket = new HandshakeSocket(
                new TimeoutThenBytesInputStream(buildHelloResponseFrame("L01", 0x91ABCDEF)));
        Object firstSucceeded = performHello.invoke(transport, firstSocket, firstHelloWrites, "get_status", 91001);
        assertTrue(Boolean.TRUE.equals(firstSucceeded));
        assertEquals("1.0", extractHelloRequestVersion(firstHelloWrites.toByteArray(), 0));
        assertEquals("L01", extractHelloRequestVersion(firstHelloWrites.toByteArray(), 1));

        Field protocolSessionField = LocalDirectTransport.class.getDeclaredField("protocolSession");
        protocolSessionField.setAccessible(true);
        protocolSessionField.set(transport, new LocalDirectProtocolSession());

        ByteArrayOutputStream secondHelloWrites = new ByteArrayOutputStream();
        Socket secondSocket = new HandshakeSocket(
                new TimeoutThenBytesInputStream(buildHelloResponseFrame("1.0", 0x12345678)));
        Object secondSucceeded = performHello.invoke(transport, secondSocket, secondHelloWrites, "get_consumable",
                91002);
        assertTrue(Boolean.TRUE.equals(secondSucceeded));
        assertEquals("L01", extractHelloRequestVersion(secondHelloWrites.toByteArray(), 0));
        assertEquals("1.0", extractHelloRequestVersion(secondHelloWrites.toByteArray(), 1));
    }

    @Test
    void keepaliveSchedulingAndCancelLifecycle() throws Exception {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            LocalDirectTransport transport = new LocalDirectTransport(scheduler, 0, System::nanoTime, millis -> {
            });

            Method scheduleKeepalive = LocalDirectTransport.class.getDeclaredMethod("scheduleKeepalive");
            scheduleKeepalive.setAccessible(true);
            scheduleKeepalive.invoke(transport);

            Field keepaliveFutureField = LocalDirectTransport.class.getDeclaredField("keepaliveFuture");
            keepaliveFutureField.setAccessible(true);
            ScheduledFuture<?> future = (ScheduledFuture<?>) keepaliveFutureField.get(transport);
            assertNotNull(future);

            Method invalidateSession = LocalDirectTransport.class.getDeclaredMethod("invalidateSession", String.class);
            invalidateSession.setAccessible(true);
            invalidateSession.invoke(transport, "test");

            ScheduledFuture<?> cancelledFuture = (ScheduledFuture<?>) keepaliveFutureField.get(transport);
            assertEquals(null, cancelledFuture);
            assertTrue(future.isCancelled());
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    void keepaliveFailureInvalidatesSession() throws Exception {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            LocalDirectTransport transport = new LocalDirectTransport(scheduler, 0, System::nanoTime, millis -> {
            });

            Field sessionSocketField = LocalDirectTransport.class.getDeclaredField("sessionSocket");
            sessionSocketField.setAccessible(true);
            PingFailureSocket pingFailureSocket = new PingFailureSocket();
            sessionSocketField.set(transport, pingFailureSocket);

            Method runKeepalive = LocalDirectTransport.class.getDeclaredMethod("runKeepalive");
            runKeepalive.setAccessible(true);
            runKeepalive.invoke(transport);

            assertEquals(null, sessionSocketField.get(transport));
            assertTrue(pingFailureSocket.wasClosed());
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    void directInterCommandPacingSleepsForRemainingGap() throws Exception {
        AtomicLong nowNanos = new AtomicLong(TimeUnit.MILLISECONDS.toNanos(200));
        AtomicLong sleptMillis = new AtomicLong(-1);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            LocalDirectTransport transport = new LocalDirectTransport(scheduler, 40, nowNanos::get,
                    millis -> sleptMillis.set(millis));

            Field lastFinishedField = LocalDirectTransport.class.getDeclaredField("lastDirectCommandFinishedNanos");
            lastFinishedField.setAccessible(true);
            lastFinishedField.setLong(transport, TimeUnit.MILLISECONDS.toNanos(170));

            Method enforceGap = LocalDirectTransport.class.getDeclaredMethod("enforceInterCommandGap", String.class,
                    int.class);
            enforceGap.setAccessible(true);
            enforceGap.invoke(transport, "get_status", 99001);

            assertEquals(10L, sleptMillis.get());
        } finally {
            scheduler.shutdownNow();
        }
    }

    private static byte[] buildRawFrame(String version, int protocol, byte[] payload) {
        byte[] frame = new byte[19 + payload.length + 4];
        frame[0] = (byte) version.charAt(0);
        frame[1] = (byte) version.charAt(1);
        frame[2] = (byte) version.charAt(2);
        ProtocolUtils.writeInt32BE(frame, 555555, 3);
        ProtocolUtils.writeInt32BE(frame, 77777, 7);
        ProtocolUtils.writeInt32BE(frame, (int) (System.currentTimeMillis() / 1000L), 11);
        ProtocolUtils.writeInt16BE(frame, protocol, 15);
        ProtocolUtils.writeInt16BE(frame, payload.length, 17);
        System.arraycopy(payload, 0, frame, 19, payload.length);

        java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
        crc32.update(frame, 0, frame.length - 4);
        ProtocolUtils.writeInt32BE(frame, (int) crc32.getValue(), frame.length - 4);
        return frame;
    }

    private static byte[] withPrefix(byte[] frame) {
        byte[] prefixed = new byte[frame.length + 4];
        ProtocolUtils.writeInt32BE(prefixed, frame.length, 0);
        System.arraycopy(frame, 0, prefixed, 4, frame.length);
        return prefixed;
    }

    private static byte[] buildL01Frame(String localKey, int connectNonce, int ackNonce, String rpcPayload)
            throws RoborockException {
        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        int sequence = 600001;
        int random = 70001;
        String payloadJson = "{\"dps\":{\"102\":" + quoteJsonString(rpcPayload) + "}}";
        byte[] encrypted = ProtocolUtils.encryptL01(payloadJson.getBytes(StandardCharsets.UTF_8), localKey, timestamp,
                sequence, random, connectNonce, ackNonce);

        byte[] frame = new byte[19 + encrypted.length + 4];
        frame[0] = 'L';
        frame[1] = '0';
        frame[2] = '1';
        ProtocolUtils.writeInt32BE(frame, sequence, 3);
        ProtocolUtils.writeInt32BE(frame, random, 7);
        ProtocolUtils.writeInt32BE(frame, timestamp, 11);
        ProtocolUtils.writeInt16BE(frame, 5, 15);
        ProtocolUtils.writeInt16BE(frame, encrypted.length, 17);
        System.arraycopy(encrypted, 0, frame, 19, encrypted.length);

        CRC32 crc32 = new CRC32();
        crc32.update(frame, 0, frame.length - 4);
        ProtocolUtils.writeInt32BE(frame, (int) crc32.getValue(), frame.length - 4);
        return frame;
    }

    private static byte[] buildEncryptedV10Frame(String localKey, byte[] plaintextPayload) {
        try {
            int timestamp = (int) (System.currentTimeMillis() / 1000L);
            byte[] encryptedPayload = ProtocolUtils.encrypt(plaintextPayload, ProtocolUtils.encodeTimestamp(timestamp)
                    + localKey + org.openhab.binding.roborock.internal.RoborockBindingConstants.SALT);
            byte[] frame = new byte[19 + encryptedPayload.length + 4];
            frame[0] = '1';
            frame[1] = '.';
            frame[2] = '0';
            ProtocolUtils.writeInt32BE(frame, 555555, 3);
            ProtocolUtils.writeInt32BE(frame, 77777, 7);
            ProtocolUtils.writeInt32BE(frame, timestamp, 11);
            ProtocolUtils.writeInt16BE(frame, 5, 15);
            ProtocolUtils.writeInt16BE(frame, encryptedPayload.length, 17);
            System.arraycopy(encryptedPayload, 0, frame, 19, encryptedPayload.length);

            CRC32 crc32 = new CRC32();
            crc32.update(frame, 0, frame.length - 4);
            ProtocolUtils.writeInt32BE(frame, (int) crc32.getValue(), frame.length - 4);
            return frame;
        } catch (RoborockException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] concat(byte[]... chunks) {
        int total = 0;
        for (byte[] chunk : chunks) {
            total += chunk.length;
        }
        byte[] result = new byte[total];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, result, offset, chunk.length);
            offset += chunk.length;
        }
        return result;
    }

    private static String quoteJsonString(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 8);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static byte[] buildHelloResponseFrame(String version, int ackNonce) {
        byte[] frame = new byte[21];
        frame[0] = (byte) version.charAt(0);
        frame[1] = (byte) version.charAt(1);
        frame[2] = (byte) version.charAt(2);
        ProtocolUtils.writeInt32BE(frame, 1, 3);
        ProtocolUtils.writeInt32BE(frame, ackNonce, 7);
        ProtocolUtils.writeInt32BE(frame, (int) (System.currentTimeMillis() / 1000L), 11);
        ProtocolUtils.writeInt16BE(frame, 1, 15);

        CRC32 crc32 = new CRC32();
        crc32.update(frame, 0, 17);
        ProtocolUtils.writeInt32BE(frame, (int) crc32.getValue(), 17);
        return frame;
    }

    private static String extractHelloRequestVersion(byte[] writes, int requestIndex) {
        int requestLength = 25;
        int base = requestIndex * requestLength;
        return new String(new byte[] { writes[base + 4], writes[base + 5], writes[base + 6] }, StandardCharsets.UTF_8);
    }

    private static final class FakeSocket extends Socket {
        private final InputStream inputStream;

        FakeSocket(byte[] bytes) {
            this.inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public void setSoTimeout(int timeout) {
            // no-op for deterministic in-memory stream
        }
    }

    private static final class HandshakeSocket extends Socket {
        private final InputStream inputStream;

        HandshakeSocket(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public void setSoTimeout(int timeout) {
            // no-op for deterministic in-memory stream
        }
    }

    private static final class TimeoutThenBytesInputStream extends InputStream {
        private final ByteArrayInputStream delegate;
        private boolean timedOut;

        TimeoutThenBytesInputStream(byte[] bytes) {
            this.delegate = new ByteArrayInputStream(bytes);
        }

        @Override
        public int read() throws java.io.IOException {
            if (!timedOut) {
                timedOut = true;
                throw new SocketTimeoutException("simulated handshake timeout");
            }
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws java.io.IOException {
            if (!timedOut) {
                timedOut = true;
                throw new SocketTimeoutException("simulated handshake timeout");
            }
            return delegate.read(b, off, len);
        }
    }

    private static final class ReusableFakeSocket extends Socket {
        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public boolean isInputShutdown() {
            return false;
        }

        @Override
        public boolean isOutputShutdown() {
            return false;
        }

        @Override
        public InetAddress getInetAddress() {
            return null;
        }
    }

    private static final class TimeoutOnlySocket extends Socket {
        private final InputStream inputStream = new TimeoutOnlyInputStream();
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private boolean closed;

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public java.io.OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public void setSoTimeout(int timeout) {
            // no-op for deterministic timeout stream
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public boolean isInputShutdown() {
            return false;
        }

        @Override
        public boolean isOutputShutdown() {
            return false;
        }

        @Override
        public void close() {
            closed = true;
        }

        boolean wasClosed() {
            return closed;
        }
    }

    private static final class TimeoutOnlyInputStream extends InputStream {
        @Override
        public int read() throws java.io.IOException {
            throw new SocketTimeoutException("simulated frame timeout");
        }

        @Override
        public int read(byte[] b, int off, int len) throws java.io.IOException {
            throw new SocketTimeoutException("simulated frame timeout");
        }
    }

    private static final class HelloThenTimeoutInputStream extends InputStream {
        private final ByteArrayInputStream helloBytes;

        HelloThenTimeoutInputStream(byte[] helloBytes) {
            this.helloBytes = new ByteArrayInputStream(helloBytes);
        }

        @Override
        public int read() throws java.io.IOException {
            int value = helloBytes.read();
            if (value >= 0) {
                return value;
            }
            throw new SocketTimeoutException("simulated command response timeout");
        }

        @Override
        public int read(byte[] b, int off, int len) throws java.io.IOException {
            int read = helloBytes.read(b, off, len);
            if (read >= 0) {
                return read;
            }
            throw new SocketTimeoutException("simulated command response timeout");
        }
    }

    private static final class ScriptedSocket extends Socket {
        private final InputStream inputStream;
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private boolean closed;
        private boolean connected;

        ScriptedSocket(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void connect(java.net.SocketAddress endpoint, int timeout) {
            connected = true;
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public void setSoTimeout(int timeout) {
            // no-op
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public boolean isInputShutdown() {
            return false;
        }

        @Override
        public boolean isOutputShutdown() {
            return false;
        }

        @Override
        public void close() {
            closed = true;
        }

        boolean wasClosed() {
            return closed;
        }
    }

    private static final class PingFailureSocket extends Socket {
        private final InputStream inputStream = new TimeoutOnlyInputStream();
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private boolean closed;

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public java.io.OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public void setSoTimeout(int timeout) {
            // no-op
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public boolean isInputShutdown() {
            return false;
        }

        @Override
        public boolean isOutputShutdown() {
            return false;
        }

        @Override
        public void close() {
            closed = true;
        }

        boolean wasClosed() {
            return closed;
        }
    }
}
