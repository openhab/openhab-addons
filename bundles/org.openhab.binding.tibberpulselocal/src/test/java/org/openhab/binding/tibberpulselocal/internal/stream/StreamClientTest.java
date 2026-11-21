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
package org.openhab.binding.tibberpulselocal.internal.stream;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.Fixtures;
import org.openhab.binding.tibberpulselocal.internal.stream.RecordingListener.Active;
import org.openhab.binding.tibberpulselocal.internal.stream.RecordingListener.Lost;
import org.openhab.binding.tibberpulselocal.internal.stream.RecordingListener.Message;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamClient.Timing;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamFailure.Kind;

/**
 * Tests the stream client against the local bridge double: request, HTTP answers, frames, failures, and the status
 * kinds. Backoff and shutdown have their own classes.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
class StreamClientTest extends StreamTestSupport {

    private static final String HTTP_401 = "HTTP/1.1 401 Unauthorized\r\nWWW-Authenticate: Basic\r\n\r\n";

    private static Lost awaitLost(RecordingListener listener) throws InterruptedException {
        return listener.await(Lost.class, WAIT_MILLIS);
    }

    @Test
    void whenTheStreamIsOpenedThenTheRequestBytesAreExactlyThePlainRequest() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.silent());
        client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), new RecordingListener()).start();

        String request = bridge.nextRequest(WAIT_MILLIS);
        assertNotNull(request);

        String credential = Base64.getEncoder().encodeToString("admin:p4ss:word".getBytes(StandardCharsets.UTF_8));
        assertEquals("GET /ws HTTP/1.1\r\nHost: 127.0.0.1\r\nAuthorization: Basic " + credential + "\r\n\r\n", request);
        assertFalse(request.contains("Upgrade"));
        assertFalse(request.contains("Connection"));
        assertFalse(request.contains("Sec-WebSocket"));
        assertFalse(request.contains("?"));
    }

    @Test
    void whenIpv6HostIsGivenThenTheHostHeaderIsBracketed() {
        String request = new String(StreamClient.buildRequest("2001:db8::1", "x"), StandardCharsets.US_ASCII);

        assertTrue(request.contains("\r\nHost: [2001:db8::1]\r\n"));
        assertTrue(new String(StreamClient.buildRequest("[2001:db8::1]", "x"), StandardCharsets.US_ASCII)
                .contains("\r\nHost: [2001:db8::1]\r\n"));
    }

    @Test
    void whenPasswordHasNonAsciiCharactersThenItIsSentAsUtf8() {
        String request = new String(StreamClient.buildRequest("bridge.example", "päss"), StandardCharsets.US_ASCII);

        String expected = Base64.getEncoder().encodeToString("admin:päss".getBytes(StandardCharsets.UTF_8));
        assertTrue(request.contains("Authorization: Basic " + expected + "\r\n"));
    }

    @Test
    void whenHostContainsLineBreaksThenTheClientRefusesIt() {
        RecordingListener listener = new RecordingListener();

        assertThrows(IllegalArgumentException.class,
                () -> new StreamClient("a\r\nX-Injected: 1", "pw", "name", listener));
        assertThrows(IllegalArgumentException.class, () -> new StreamClient("a b", "pw", "name", listener));
        assertThrows(IllegalArgumentException.class, () -> new StreamClient("a\u0000b", "pw", "name", listener));
    }

    @Test
    void whenBridgeSendsFramesWithoutHttpResponseThenTheReadingIsDelivered() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        RecordingListener listener = new RecordingListener();
        client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), listener).start();

        listener.await(Active.class, WAIT_MILLIS);
        Message message = listener.await(Message.class, WAIT_MILLIS);

        assertArrayEquals(FrameBytes.messageBytes(Fixtures.DEVICE_ID, "sml", Fixtures.observedFrame()),
                message.bytes());
        assertEquals(1, bridge.accepted());
    }

    @Test
    void whenTheObservedMessageArrivesByteWiseThenItIsDelivered() throws Exception {
        FakeBridge bridge = bridge(connection -> {
            connection.sendChunked(validWire(), 1);
            connection.readUntilClosed();
        });
        RecordingListener listener = new RecordingListener();
        client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), listener).start();

        Message message = listener.await(Message.class, WAIT_MILLIS);

        assertEquals(37 + 712, message.bytes().length);
    }

    @Test
    void whenFirstFrameArrivesThenTheStreamIsActiveOnceAndBeforeTheMessage() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire(), validWire()));
        RecordingListener listener = new RecordingListener();
        client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), listener).start();

        assertInstanceOf(Active.class, listener.next(WAIT_MILLIS));
        assertInstanceOf(Message.class, listener.next(WAIT_MILLIS));
        assertInstanceOf(Message.class, listener.next(WAIT_MILLIS));
    }

    @Test
    void whenOnlyAPingArrivesThenTheStreamIsActiveAndAMaskedPongIsAnswered() throws Exception {
        BlockingQueue<byte[]> pong = new ArrayBlockingQueue<>(1);
        FakeBridge bridge = bridge(connection -> {
            connection.send(FrameBytes.frame(FrameBytes.PING, true, "abc".getBytes(StandardCharsets.US_ASCII)));
            pong.add(connection.readExactly(2 + 4 + 3));
            connection.readUntilClosed();
        });
        RecordingListener listener = new RecordingListener();
        client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), listener).start();

        listener.await(Active.class, WAIT_MILLIS);
        byte[] frame = pong.poll(WAIT_MILLIS, TimeUnit.MILLISECONDS);

        assertNotNull(frame);
        assertEquals(0x8A, frame[0] & 0xFF);
        assertEquals(0x83, frame[1] & 0xFF);
        byte[] unmasked = new byte[3];
        for (int i = 0; i < 3; i++) {
            unmasked[i] = (byte) (frame[6 + i] ^ frame[2 + (i & 3)]);
        }
        assertEquals("abc", new String(unmasked, StandardCharsets.US_ASCII));
        assertTrue(listener.isEmpty(), "a ping is no message");
    }

    @Test
    void whenBridgeAnswers101WithHeadersThenTheHeadersAreSkippedAndFramesAreRead() throws Exception {
        String http = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n\r\n";
        FakeBridge bridge = bridge(FakeBridge.answering(http, validWire()));
        RecordingListener listener = new RecordingListener();
        client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), listener).start();

        Message message = listener.await(Message.class, WAIT_MILLIS);

        assertEquals(37 + 712, message.bytes().length);
    }

    @Test
    void whenBridgeAnswers401ThenExactlyOneRequestIsSentAndTheThreadEnds() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.answeringThenClosing(HTTP_401));
        RecordingListener listener = new RecordingListener();
        RecordingWaiter waiter = new RecordingWaiter(100);
        String name = threadName();
        client(bridge, name, LONG_TIMING, waiter, listener).start();

        Lost lost = awaitLost(listener);

        assertEquals(Kind.REJECTED, lost.failure().getKind());
        assertTrue(lost.failure().isFinal());
        assertTrue(awaitThreadEnd(name, WAIT_MILLIS), "the client must give up");
        assertEquals(1, bridge.accepted());
        assertEquals(0, waiter.waits(), "no retry is scheduled");
        assertTrue(listener.isEmpty());
    }

    @Test
    void whenBridgeAnswers403ThenTheBehaviourEqualsTheWrongPassword() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.answeringThenClosing("HTTP/1.1 403 Forbidden\r\n\r\n"));
        RecordingListener listener = new RecordingListener();
        RecordingWaiter waiter = new RecordingWaiter(100);
        String name = threadName();
        client(bridge, name, LONG_TIMING, waiter, listener).start();

        assertEquals(Kind.REJECTED, awaitLost(listener).failure().getKind());
        assertTrue(awaitThreadEnd(name, WAIT_MILLIS));
        assertEquals(1, bridge.accepted());
        assertEquals(0, waiter.waits());
    }

    @Test
    void whenBridgeAnswers404ThenTheFirmwareIsReportedAndTheRetryIsEverySixtySeconds() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.answeringThenClosing("HTTP/1.1 404 Not Found\r\n\r\n"));
        RecordingListener listener = new RecordingListener();
        RecordingWaiter waiter = new RecordingWaiter(2);
        client(bridge, threadName(), LONG_TIMING, waiter, listener).start();

        assertEquals(Kind.ENDPOINT_MISSING, awaitLost(listener).failure().getKind());
        assertTrue(waiter.awaitFinished(WAIT_MILLIS));
        assertEquals(List.of(60L, 60L), waiter.seconds());
        assertFalse(awaitLost(listener).failure().isFinal());
    }

    @Test
    void whenBridgeAnswers500ThenTheStatusIsCarriedAndTheNextAttemptFollowsTheBackoff() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.answeringThenClosing("HTTP/1.1 500 Internal Server Error\r\n\r\n"));
        RecordingListener listener = new RecordingListener();
        RecordingWaiter waiter = new RecordingWaiter(2);
        client(bridge, threadName(), LONG_TIMING, waiter, listener).start();

        StreamFailure failure = awaitLost(listener).failure();

        assertEquals(Kind.HTTP_STATUS, failure.getKind());
        assertEquals(500, failure.getHttpStatus());
        assertFalse(failure.isFinal());
        assertTrue(waiter.awaitFinished(WAIT_MILLIS));
        assertEquals(List.of(2L, 4L), waiter.seconds());
        assertEquals(2, bridge.accepted());
    }

    @Test
    void whenTheHttpHeadExceedsEightKilobytesThenTheFailureIsRecoverable() throws Exception {
        String head = "HTTP/1.1 200 OK\r\nX-Filler: " + "a".repeat(9000);
        FakeBridge bridge = bridge(FakeBridge.answering(head));
        RecordingListener listener = new RecordingListener();
        client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), listener).start();

        StreamFailure failure = awaitLost(listener).failure();

        assertEquals(Kind.PROTOCOL, failure.getKind());
        assertFalse(failure.isFinal());
    }

    @Test
    void whenTheHttpHeadHasNoStatusLineThenTheFailureIsAProtocolError() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.answeringThenClosing("Hello there\r\n\r\n"));
        RecordingListener listener = new RecordingListener();
        client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), listener).start();

        assertEquals(Kind.PROTOCOL, awaitLost(listener).failure().getKind());
    }

    @Test
    void whenBridgeIsClosedThenTheConnectionIsRefusedAndTheFailureIsUnreachable() throws Exception {
        RecordingListener listener = new RecordingListener();
        RecordingWaiter waiter = new RecordingWaiter(1);
        client(closedPort(), threadName(), LONG_TIMING, waiter, System::nanoTime, listener).start();

        assertEquals(Kind.UNREACHABLE, awaitLost(listener).failure().getKind());
        assertTrue(waiter.awaitFinished(WAIT_MILLIS));
        assertEquals(List.of(2L), waiter.seconds());
    }

    @Test
    void whenTheHostNameCarriesAPortThenItDoesNotResolveAndTheFailureIsUnreachable() throws Exception {
        RecordingListener listener = new RecordingListener();
        // Validation accepts "name:port"; the resolver rejects it as a malformed IPv6 literal, without a DNS query.
        client("bridge.example:80", closedPort(), PASSWORD, threadName(), LONG_TIMING, new RecordingWaiter(1),
                System::nanoTime, listener).start();

        assertEquals(Kind.UNREACHABLE, awaitLost(listener).failure().getKind());
    }

    @Test
    void whenBridgeSendsACloseFrameThenTheStreamIsClosedByTheBridge() throws Exception {
        FakeBridge bridge = bridge(
                FakeBridge.sending(validWire(), FrameBytes.frame(FrameBytes.CLOSE, true, new byte[0])));
        RecordingListener listener = new RecordingListener();
        RecordingWaiter waiter = new RecordingWaiter(1);
        client(bridge, threadName(), LONG_TIMING, waiter, listener).start();

        assertEquals(Kind.CLOSED_BY_BRIDGE, awaitLost(listener).failure().getKind());
        assertTrue(waiter.awaitFinished(WAIT_MILLIS));
        assertEquals(List.of(2L), waiter.seconds());
    }

    @Test
    void whenConnectionEndsWithoutCloseFrameThenTheBehaviourEqualsTheCloseFrame() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sendingThenClosing(validWire()));
        RecordingListener listener = new RecordingListener();
        RecordingWaiter waiter = new RecordingWaiter(1);
        client(bridge, threadName(), LONG_TIMING, waiter, listener).start();

        assertEquals(Kind.CLOSED_BY_BRIDGE, awaitLost(listener).failure().getKind());
        assertTrue(waiter.awaitFinished(WAIT_MILLIS));
        assertEquals(List.of(2L), waiter.seconds());
    }

    @Test
    void whenConnectionIsResetThenTheBehaviourEqualsTheCloseFrame() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.resetting());
        RecordingListener listener = new RecordingListener();
        RecordingWaiter waiter = new RecordingWaiter(1);
        client(bridge, threadName(), LONG_TIMING, waiter, listener).start();

        assertEquals(Kind.CLOSED_BY_BRIDGE, awaitLost(listener).failure().getKind());
        assertTrue(waiter.awaitFinished(WAIT_MILLIS));
        assertEquals(List.of(2L), waiter.seconds());
    }

    @Test
    void whenBridgeSendsNothingThenTheStreamIsReportedSilentAfterTheLivenessLimit() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.silent());
        RecordingListener listener = new RecordingListener();
        RecordingWaiter waiter = new RecordingWaiter(1);
        Timing shortLiveness = new Timing(Duration.ofSeconds(5), Duration.ofMillis(300), Duration.ofSeconds(2));
        client(bridge, threadName(), shortLiveness, waiter, listener).start();

        assertEquals(Kind.SILENT, awaitLost(listener).failure().getKind());
        assertTrue(waiter.awaitFinished(WAIT_MILLIS));
        assertEquals(List.of(2L), waiter.seconds());
    }

    @Test
    void whenBridgeStopsAfterTheFirstFrameThenTheStreamIsReportedSilent() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        RecordingListener listener = new RecordingListener();
        Timing shortLiveness = new Timing(Duration.ofSeconds(5), Duration.ofMillis(300), Duration.ofSeconds(2));
        client(bridge, threadName(), shortLiveness, new RecordingWaiter(1), listener).start();

        listener.await(Message.class, WAIT_MILLIS);

        assertEquals(Kind.SILENT, awaitLost(listener).failure().getKind());
    }

    @Test
    void whenBridgeBreaksTheProtocolThenTheStreamIsLostAsProtocolError() throws Exception {
        FakeBridge bridge = bridge(
                FakeBridge.sending(FrameBytes.frame(FrameBytes.CONTINUATION, true, new byte[] { 1 })));
        RecordingListener listener = new RecordingListener();
        client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), listener).start();

        assertEquals(Kind.PROTOCOL, awaitLost(listener).failure().getKind());
    }

    @Test
    void whenTheListenerFailsThenTheClientReportsAProtocolErrorAndKeepsSupervising() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        RecordingWaiter waiter = new RecordingWaiter(1);
        StreamListener failing = new StreamListener() {
            @Override
            public void onStreamActive() {
            }

            @Override
            public boolean onMessage(byte[] message) {
                throw new IllegalStateException("listener defect");
            }

            @Override
            public void onStreamLost(StreamFailure failure) {
            }
        };
        client(bridge, threadName(), LONG_TIMING, waiter, failing).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS), "the loop goes on to the backoff wait");
        assertEquals(List.of(2L), waiter.seconds());
    }

    @Test
    void whenAClientIsStartedTwiceOrAfterStopThenItIsRefused() throws IOException {
        FakeBridge bridge = bridge(FakeBridge.silent());
        StreamClient first = client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1), new RecordingListener());
        StreamClient second = client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1),
                new RecordingListener());

        first.start();
        assertThrows(IllegalStateException.class, first::start);
        second.stop();
        assertThrows(IllegalStateException.class, second::start);
    }

    @Test
    void whenTheBackoffTableIsAskedThenItGrowsFromTwoToSixtySeconds() {
        long[] expected = { 2, 4, 8, 16, 32, 60, 60, 60 };
        for (int n = 1; n <= expected.length; n++) {
            assertEquals(expected[n - 1], StreamClient.backoffSeconds(n), "failure " + n);
        }
        assertEquals(60, StreamClient.backoffSeconds(1000));
    }
}
