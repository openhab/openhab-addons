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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamClient.BackoffWaiter;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamFailure.Kind;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Tests that an unexpected {@link Throwable} (also an {@link Error}) from a listener callback does not kill the reader
 * thread, that a {@link VirtualMachineError} is not swallowed, and that a repeating defect does not flood the log.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
class StreamClientDefectTest extends StreamTestSupport {

    /** Stands in for an out-of-memory or stack-overflow error without allocating or recursing. */
    private static final class FakeVmError extends VirtualMachineError { // NOPMD DoNotExtendJavaLangError
        private static final long serialVersionUID = 1L;
    }

    /** A listener that throws from the chosen callback for its first calls and works afterwards. */
    private static final class DefectiveListener implements StreamListener {
        final AtomicInteger activeCalls = new AtomicInteger();
        final AtomicInteger messageCalls = new AtomicInteger();
        final List<Kind> lost = new CopyOnWriteArrayList<>();
        final CountDownLatch healthyMessage = new CountDownLatch(1);
        private final AtomicInteger lostCalls = new AtomicInteger();
        private final int failingCalls;
        private final Supplier<Throwable> defect;
        private final String callback;

        DefectiveListener(String callback, int failingCalls, Supplier<Throwable> defect) {
            this.callback = callback;
            this.failingCalls = failingCalls;
            this.defect = defect;
        }

        private void maybeThrow(String name, int call) {
            if (name.equals(callback) && call <= failingCalls) {
                sneak(defect.get());
            }
        }

        @Override
        public void onStreamActive() {
            maybeThrow("active", activeCalls.incrementAndGet());
        }

        @Override
        public boolean onMessage(byte[] message) {
            maybeThrow("message", messageCalls.incrementAndGet());
            // Reaching this line means the call did not throw, so the third attempt got through.
            if (!"message".equals(callback) || messageCalls.get() > failingCalls) {
                healthyMessage.countDown();
            }
            return true;
        }

        @Override
        public void onStreamLost(StreamFailure failure) {
            int call = lostCalls.incrementAndGet();
            lost.add(failure.getKind());
            maybeThrow("lost", call);
        }

        int lostCalls() {
            return lostCalls.get();
        }

        private static void sneak(Throwable t) {
            if (t instanceof Error e) {
                throw e;
            }
            throw (RuntimeException) t;
        }
    }

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Logger bindingLogger = (Logger) LoggerFactory.getLogger("ROOT");
    private Level previousLevel = Level.INFO;
    private boolean captured;
    private final AtomicLong nanos = new AtomicLong(5_000_000_000L);
    private Thread.@Nullable UncaughtExceptionHandler previousHandler;

    @BeforeEach
    void attachAppender() {
        previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (LoggerFactory.getILoggerFactory() instanceof LoggerContext) {
            bindingLogger = (Logger) LoggerFactory.getLogger("org.openhab.binding.tibberpulselocal");
            previousLevel = bindingLogger.getLevel() != null ? bindingLogger.getLevel() : Level.INFO;
            bindingLogger.setLevel(Level.TRACE);
            appender.start();
            bindingLogger.addAppender(appender);
            captured = true;
        }
    }

    @AfterEach
    void detachAppender() {
        Thread.setDefaultUncaughtExceptionHandler(previousHandler);
        if (captured) {
            bindingLogger.detachAppender(appender);
            bindingLogger.setLevel(previousLevel);
            appender.stop();
        }
    }

    private List<ILoggingEvent> warnings() {
        List<ILoggingEvent> result = new ArrayList<>();
        for (ILoggingEvent event : new ArrayList<>(appender.list)) {
            if (event.getLevel() == Level.WARN && event.getLoggerName().endsWith("StreamClient")) {
                result.add(event);
            }
        }
        return result;
    }

    /** Waits like the real waiter would not, but counts and stops the loop after the given number of waits. */
    private static RecordingWaiter waits(int count) {
        return new RecordingWaiter(count);
    }

    // The reader survives a defect in each callback

    @Test
    void whenOnStreamLostThrowsAnErrorThenTheThreadSurvivesAndReconnects() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.closing());
        DefectiveListener listener = new DefectiveListener("lost", 2, NoSuchMethodError::new);
        RecordingWaiter waiter = waits(3);
        client(bridge, threadName(), LONG_TIMING, waiter, listener).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        assertEquals(List.of(2L, 4L, 8L), waiter.seconds(), "the normal backoff applies after the defect");
        assertEquals(3, listener.lostCalls(), "the third call was made on a surviving thread");
        assertEquals(3, bridge.accepted());
    }

    @Test
    void whenOnMessageThrowsAnErrorThenTheAttemptEndsAsProtocolFailureAndTheClientReconnects() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        DefectiveListener listener = new DefectiveListener("message", 2, NoSuchMethodError::new);
        RecordingWaiter waiter = waits(5);
        client(bridge, threadName(), LONG_TIMING, waiter, listener).start();

        assertTrue(listener.healthyMessage.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));

        assertEquals(List.of(Kind.PROTOCOL, Kind.PROTOCOL), listener.lost);
        assertEquals(List.of(2L, 4L), waiter.seconds());
        assertEquals(3, bridge.accepted());
    }

    @Test
    void whenOnStreamActiveThrowsAnErrorThenTheAttemptEndsAsProtocolFailureAndTheClientReconnects() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        DefectiveListener listener = new DefectiveListener("active", 2, NoClassDefFoundError::new);
        RecordingWaiter waiter = waits(5);
        client(bridge, threadName(), LONG_TIMING, waiter, listener).start();

        assertTrue(listener.healthyMessage.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));

        assertEquals(List.of(Kind.PROTOCOL, Kind.PROTOCOL), listener.lost);
        assertEquals(List.of(2L, 4L), waiter.seconds());
        assertEquals(3, listener.activeCalls.get());
    }

    @Test
    void whenACallbackThrowsALinkageErrorThenItIsSurvivedLikeAnyOtherError() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        DefectiveListener listener = new DefectiveListener("message", 1, () -> new LinkageError());
        client(bridge, threadName(), LONG_TIMING, waits(5), listener).start();

        assertTrue(listener.healthyMessage.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
    }

    // A VirtualMachineError is not swallowed

    private void assertRethrown(String callback, FakeBridge bridge) throws Exception {
        AtomicReference<@Nullable Throwable> uncaught = new AtomicReference<>();
        CountDownLatch seen = new CountDownLatch(1);
        // The default handler also keeps the expected stack trace out of the test output.
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            uncaught.set(error);
            seen.countDown();
        });
        DefectiveListener listener = new DefectiveListener(callback, Integer.MAX_VALUE, FakeVmError::new);
        RecordingWaiter waiter = waits(5);
        String name = threadName();
        client(bridge, name, LONG_TIMING, waiter, listener).start();

        assertTrue(seen.await(WAIT_MILLIS, TimeUnit.MILLISECONDS), "the error reaches the thread");
        assertInstanceOf(FakeVmError.class, uncaught.get());
        assertTrue(awaitThreadEnd(name, WAIT_MILLIS), "the thread ends");
        assertEquals(0, waiter.waits(), "no reconnect follows");
        assertEquals(1, bridge.accepted());
        assertTrue(bridge.awaitNoOpenConnections(WAIT_MILLIS), "the socket was closed before the error propagated");
        assertTrue(warnings().isEmpty(), "it is not reported as a defect of the binding");
    }

    @Test
    void whenOnMessageThrowsAVirtualMachineErrorThenItIsRethrownAfterClosingTheSocket() throws Exception {
        assertRethrown("message", bridge(FakeBridge.sending(validWire())));
    }

    @Test
    void whenOnStreamActiveThrowsAVirtualMachineErrorThenItIsRethrownAfterClosingTheSocket() throws Exception {
        assertRethrown("active", bridge(FakeBridge.sending(validWire())));
    }

    @Test
    void whenOnStreamLostThrowsAVirtualMachineErrorThenItIsRethrown() throws Exception {
        assertRethrown("lost", bridge(FakeBridge.closing()));
    }

    // The log is not flooded

    @Test
    void whenTheSameErrorRepeatsOnEveryAttemptThenItIsLoggedOnceAndAgainAfterAnHour() throws Exception {
        assumeTrue(captured, "needs logback");
        FakeBridge bridge = bridge(FakeBridge.closing());
        DefectiveListener listener = new DefectiveListener("lost", Integer.MAX_VALUE, NoSuchMethodError::new);
        AtomicInteger waits = new AtomicInteger();
        CountDownLatch finished = new CountDownLatch(1);
        BackoffWaiter waiter = (delay, stopped) -> {
            int n = waits.incrementAndGet();
            if (n == 2) {
                nanos.addAndGet(StreamClient.DEFECT_RELOG_NANOS);
            }
            if (n == 5) {
                finished.countDown();
                return true;
            }
            return false;
        };
        client(bridge.port(), threadName(), LONG_TIMING, waiter, nanos::get, listener).start();

        assertTrue(finished.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));

        // Attempt 1 logs, 2 does not, 3 follows the one hour jump and logs, 4 and 5 do not.
        List<ILoggingEvent> warnings = warnings();
        assertEquals(2, warnings.size());
        for (ILoggingEvent warning : warnings) {
            assertTrue(warning.getFormattedMessage().contains("java.lang.NoSuchMethodError"));
            assertNull(warning.getThrowableProxy(), "no stack trace is attached");
        }
    }

    @Test
    void whenDifferentErrorsOccurThenEachKindIsLoggedOnce() throws Exception {
        assumeTrue(captured, "needs logback");
        FakeBridge bridge = bridge(FakeBridge.closing());
        AtomicInteger calls = new AtomicInteger();
        StreamListener listener = new StreamListener() {
            @Override
            public void onStreamActive() {
            }

            @Override
            public boolean onMessage(byte[] message) {
                return true;
            }

            @Override
            public void onStreamLost(StreamFailure failure) {
                switch (calls.incrementAndGet() % 2) {
                    case 1 -> throw new NoSuchMethodError();
                    default -> throw new NoClassDefFoundError();
                }
            }
        };
        RecordingWaiter waiter = waits(6);
        client(bridge.port(), threadName(), LONG_TIMING, waiter, nanos::get, listener).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        List<String> texts = warnings().stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertEquals(2, texts.size(), texts.toString());
        assertTrue(texts.get(0).contains("NoSuchMethodError"));
        assertTrue(texts.get(1).contains("NoClassDefFoundError"));
    }

    // stop() semantics stay as they are

    @Test
    void whenStopIsCalledAfterADefectThenTheThreadEndsWithinTheJoinTimeAndNoCallbackFollows() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        DefectiveListener listener = new DefectiveListener("message", Integer.MAX_VALUE, NoSuchMethodError::new);
        CountDownLatch waiting = new CountDownLatch(1);
        BackoffWaiter waiter = (delay, stopped) -> {
            waiting.countDown();
            return stopped.await(60, TimeUnit.SECONDS);
        };
        String name = threadName();
        StreamClient client = client(bridge, name, LONG_TIMING, waiter, listener);
        client.start();
        assertTrue(waiting.await(WAIT_MILLIS, TimeUnit.MILLISECONDS), "the failed attempt led to a backoff wait");
        assertTrue(isAlive(name));

        client.stop();

        assertFalse(isAlive(name), "stop joined the thread");
        assertEquals(1, bridge.accepted(), "no reconnect after stop");
        assertEquals(1, listener.lostCalls());
        assertEquals(1, listener.messageCalls.get());
    }
}
