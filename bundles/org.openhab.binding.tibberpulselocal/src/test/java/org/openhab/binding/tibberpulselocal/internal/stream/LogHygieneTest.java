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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.Fixtures;
import org.openhab.binding.tibberpulselocal.internal.sml.SmlDecoder;
import org.openhab.binding.tibberpulselocal.internal.sml.SmlReading;
import org.openhab.binding.tibberpulselocal.internal.stream.RecordingListener.Lost;
import org.openhab.binding.tibberpulselocal.internal.stream.RecordingListener.Message;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamFailure.Kind;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;

/**
 * Tests the scenarios of the {@code connection} spec, "Credential and identifier protection", for the transport and
 * decoding layers: logs of a full run at TRACE, and the text of every failure, reading, and envelope.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
class LogHygieneTest extends StreamTestSupport {

    private static final String SECRET = "s3cret-Pw";

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Logger bindingLogger = (Logger) LoggerFactory.getLogger("ROOT");
    private Level previousLevel = Level.INFO;

    @BeforeEach
    void attachAppender() {
        assumeTrue(LoggerFactory.getILoggerFactory() instanceof LoggerContext, "needs logback");
        bindingLogger = (Logger) LoggerFactory.getLogger("org.openhab.binding.tibberpulselocal");
        previousLevel = bindingLogger.getLevel() != null ? bindingLogger.getLevel() : Level.INFO;
        bindingLogger.setLevel(Level.TRACE);
        appender.start();
        bindingLogger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        bindingLogger.detachAppender(appender);
        bindingLogger.setLevel(previousLevel);
        appender.stop();
    }

    private static String render(ILoggingEvent event) {
        StringBuilder text = new StringBuilder(event.getFormattedMessage());
        for (IThrowableProxy proxy = event.getThrowableProxy(); proxy != null; proxy = proxy.getCause()) {
            text.append(' ').append(proxy.getClassName()).append(' ').append(proxy.getMessage());
        }
        return text.toString();
    }

    private static void assertNoSecrets(String where, String text) {
        String basic = Base64.getEncoder().encodeToString(("admin:" + SECRET).getBytes(StandardCharsets.UTF_8));
        String bare = Base64.getEncoder().encodeToString(SECRET.getBytes(StandardCharsets.UTF_8));
        assertFalse(text.contains(SECRET), where + " contains the password");
        assertFalse(text.contains(basic), where + " contains the Authorization credential");
        assertFalse(text.contains(bare), where + " contains the Base64 of the password");
        assertFalse(text.toLowerCase(Locale.ROOT).contains("authorization"),
                where + " contains the Authorization header");
        assertFalse(text.contains(Fixtures.METER_ID), where + " contains the meter id");
        assertFalse(text.contains(Fixtures.DEVICE_ID), where + " contains the device id");
    }

    /** Decodes like the bridge handler will, so the decoder runs under the same log capture. */
    private static RecordingListener decodingListener() {
        return new RecordingListener(message -> {
            Envelope envelope = Envelope.parse(message);
            return envelope != null && envelope.isSml()
                    && !SmlDecoder.decode(message, envelope.payloadOffset()).isEmpty();
        });
    }

    @Test
    void whenTheClientRunsThroughSuccessRejectionRefusalAndDisposalThenNoSecretIsLogged() throws Exception {
        // success, with decoding, then disposal
        FakeBridge bridge = bridge(FakeBridge.sending(FrameBytes.concat(validWire(), validWire())));
        RecordingListener listener = decodingListener();
        StreamClient running = client(bridge.port(), SECRET, threadName(), LONG_TIMING, new RecordingWaiter(1),
                System::nanoTime, listener);
        running.start();
        listener.await(Message.class, WAIT_MILLIS);
        listener.await(Message.class, WAIT_MILLIS);
        running.stop();

        // rejection
        FakeBridge rejecting = bridge(FakeBridge.answeringThenClosing("HTTP/1.1 401 Unauthorized\r\n\r\n"));
        RecordingListener rejected = new RecordingListener();
        client(rejecting.port(), SECRET, threadName(), LONG_TIMING, new RecordingWaiter(1), System::nanoTime, rejected)
                .start();
        StreamFailure rejection = rejected.await(Lost.class, WAIT_MILLIS).failure();

        // refusal
        RecordingListener refused = new RecordingListener();
        RecordingWaiter refusedWaiter = new RecordingWaiter(1);
        client(closedPort(), SECRET, threadName(), LONG_TIMING, refusedWaiter, System::nanoTime, refused).start();
        StreamFailure refusal = refused.await(Lost.class, WAIT_MILLIS).failure();
        assertTrue(refusedWaiter.awaitFinished(WAIT_MILLIS));

        // listener defect, which is logged at warn
        FakeBridge defective = bridge(FakeBridge.sending(validWire()));
        RecordingWaiter defectiveWaiter = new RecordingWaiter(1);
        StreamListener failing = new StreamListener() {
            @Override
            public void onStreamActive() {
            }

            @Override
            public boolean onMessage(byte[] message) {
                throw new IllegalStateException(SECRET + Fixtures.METER_ID);
            }

            @Override
            public void onStreamLost(StreamFailure failure) {
            }
        };
        client(defective.port(), SECRET, threadName(), LONG_TIMING, defectiveWaiter, System::nanoTime, failing).start();
        assertTrue(defectiveWaiter.awaitFinished(WAIT_MILLIS));

        assertEquals(Kind.REJECTED, rejection.getKind());
        assertEquals(Kind.UNREACHABLE, refusal.getKind());
        List<ILoggingEvent> events = new ArrayList<>(appender.list);
        assertFalse(events.isEmpty(), "the capture works: the client logs at debug");
        boolean warned = false;
        for (ILoggingEvent event : events) {
            assertNoSecrets("log entry '" + event.getFormattedMessage() + "'", render(event));
            warned |= event.getLevel() == Level.WARN;
        }
        assertTrue(warned, "the defective listener is reported at warn, without its message");
        assertNoSecrets("failure text", rejection + " " + rejection.getMessage() + refusal + refusal.getMessage());
    }

    @Test
    void whenACallbackThrowsAnErrorThenOnlyClassAndLocationAreLogged() throws Exception {
        String defectText = SECRET + Fixtures.METER_ID + Fixtures.DEVICE_ID + HOST;
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        RecordingWaiter waiter = new RecordingWaiter(3);
        StreamListener failing = new StreamListener() {
            @Override
            public void onStreamActive() {
                throw new NoClassDefFoundError(defectText);
            }

            @Override
            public boolean onMessage(byte[] message) {
                return true;
            }

            @Override
            public void onStreamLost(StreamFailure failure) {
                throw new NoSuchMethodError(defectText);
            }
        };
        client(bridge.port(), SECRET, threadName(), LONG_TIMING, waiter, System::nanoTime, failing).start();
        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        List<ILoggingEvent> warnings = new ArrayList<>();
        for (ILoggingEvent event : new ArrayList<>(appender.list)) {
            String text = render(event);
            assertNoSecrets("log entry '" + event.getFormattedMessage() + "'", text);
            assertFalse(text.contains(HOST), "log entry contains the host");
            if (event.getLevel() == Level.WARN) {
                warnings.add(event);
            }
        }
        // The active callback fails first and ends the attempt, so the loss callback fails after it: one each.
        assertEquals(2, warnings.size());
        for (ILoggingEvent warning : warnings) {
            assertNull(warning.getThrowableProxy(), "no stack trace is logged");
            assertTrue(warning.getFormattedMessage().contains("java.lang.NoClassDefFoundError")
                    || warning.getFormattedMessage().contains("java.lang.NoSuchMethodError"));
        }
    }

    @Test
    void whenAnyFailureOrReadingIsPrintedThenItCarriesNoIdentifier() {
        for (Kind kind : Kind.values()) {
            assertNoSecrets("failure " + kind, new StreamFailure(kind).toString());
        }
        assertNoSecrets("failure 503", new StreamFailure(503).toString());

        SmlReading reading = SmlDecoder.decode(Fixtures.observedFrame(), 0).get(0);
        assertNoSecrets("reading", reading.toString());
        byte[] message = FrameBytes.messageBytes(Fixtures.DEVICE_ID, "sml", Fixtures.observedFrame());
        Envelope envelope = Envelope.parse(message);
        assertNotNull(envelope);
        assertNoSecrets("envelope", envelope.toString());
        assertNoSecrets("exception", assertThrows(IllegalArgumentException.class,
                () -> new StreamClient("a\r\nb", SECRET, "name", new RecordingListener())).toString());
    }
}
