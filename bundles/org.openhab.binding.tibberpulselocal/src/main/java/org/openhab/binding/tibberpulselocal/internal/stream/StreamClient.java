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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamFailure.Kind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps one stream to a Tibber bridge open and delivers its messages to a {@link StreamListener}.
 *
 * The bridge does not perform the websocket handshake. After a plain {@code GET /ws} request with Basic
 * authentication it pushes websocket frames on the raw connection and sends no HTTP response, so the stream is read
 * from a plain socket. The approach follows the Home Assistant integration
 * <a href="https://github.com/marq24/ha-tibber-pulse-local">marq24/ha-tibber-pulse-local</a>; no code was copied.
 *
 * A client is single-use: {@link #start()} once, {@link #stop()} once, and a new instance for the next start. This
 * removes every restart race, and the handler can guarantee that at most one stream of its bridge exists.
 *
 * One daemon thread runs the whole supervision loop (resolve, connect, request, read, backoff wait). This is a
 * deliberate exception to the guideline to use the shared scheduler: the bridge delivers a push stream that needs a
 * blocking read for the whole life of the thing, which would occupy a shared scheduler thread permanently. The same
 * pattern is used by other socket-based bindings such as {@code dsmr} and {@code gce}.
 *
 * Failure handling: after every ended attempt the listener is told, then the client waits min(2 x 2^(n-1), 60)
 * seconds for the nth consecutive failure (404: 60 seconds) and tries again. The counter starts over only when the
 * ended stream stayed open for 30 seconds after its first valid reading. A rejected password ends the thread.
 * An unexpected {@link Throwable} from a callback or from anywhere in an attempt (also an {@link Error} such as a
 * {@link LinkageError} caused by a mismatching core version) ends only that attempt as {@link Kind#PROTOCOL}; the
 * reader thread survives and reconnects. A {@link VirtualMachineError} is rethrown.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public final class StreamClient {

    static final int PORT = 80;

    /** The timings that tests shorten. Backoff, stable period, and head limit are fixed constants. */
    record Timing(Duration connectTimeout, Duration liveness, Duration joinTimeout) {
        static final Timing PRODUCTION = new Timing(Duration.ofSeconds(10), Duration.ofSeconds(30),
                Duration.ofSeconds(2));
    }

    /** Waits between attempts; replaced in tests to run without real delays. */
    @FunctionalInterface
    interface BackoffWaiter {
        /**
         * @param delay how long to wait before the next attempt
         * @param stopped counted down when {@link StreamClient#stop()} is called
         * @return true if the client was stopped while waiting
         */
        boolean awaitStop(Duration delay, CountDownLatch stopped) throws InterruptedException;
    }

    static final long BACKOFF_FIRST_SECONDS = 2;
    static final long BACKOFF_MAX_SECONDS = 60;
    static final long ENDPOINT_MISSING_RETRY_SECONDS = 60;
    static final Duration STABLE_AFTER = Duration.ofSeconds(30);
    static final int MAX_HTTP_HEAD_BYTES = 8 * 1024;
    /** A defect that repeats is logged at warn again only after this time; in between it is logged at debug. */
    static final long DEFECT_RELOG_NANOS = TimeUnit.HOURS.toNanos(1);
    private static final int MAX_TRACKED_DEFECTS = 16;

    private static final Pattern STATUS_LINE = Pattern.compile("HTTP/1\\.[01] (\\d{3})(?: .*)?");

    private final Logger logger = LoggerFactory.getLogger(StreamClient.class);

    private final String host;
    private final int port;
    private final String password;
    private final String threadName;
    private final Timing timing;
    private final BackoffWaiter waiter;
    private final LongSupplier clock;
    private final StreamListener listener;

    private final CountDownLatch stopLatch = new CountDownLatch(1);
    /** Guards only the four fields below; no external code runs under it. */
    private final Object lock = new Object();
    private boolean started;
    private boolean stopped;
    private @Nullable Thread thread;
    private @Nullable Socket socket;

    // Used by the reader thread only.
    private boolean activeSignalled;
    private long firstValidNanos;
    private boolean hasValid;
    /** Description of each logged defect and the clock reading of its last warn entry. */
    private final Map<String, Long> defectsLoggedAt = new HashMap<>();

    /**
     * @param host the bridge host name or address, connected on port 80
     * @param password the bridge password; the user name is fixed to {@code admin}
     * @param threadName name of the reader thread
     * @param listener receives the events
     * @throws IllegalArgumentException if the host name contains whitespace or control characters
     */
    public StreamClient(String host, String password, String threadName, StreamListener listener) {
        this(host, PORT, password, threadName, Timing.PRODUCTION,
                (delay, stopped) -> stopped.await(delay.toMillis(), TimeUnit.MILLISECONDS), System::nanoTime, listener);
    }

    StreamClient(String host, int port, String password, String threadName, Timing timing, BackoffWaiter waiter,
            LongSupplier clock, StreamListener listener) {
        // The host is written into the Host header; refuse anything that could end the header line.
        for (int i = 0; i < host.length(); i++) {
            char c = host.charAt(i);
            if (c <= ' ' || c == 0x7f) {
                throw new IllegalArgumentException("Invalid host name");
            }
        }
        this.host = host;
        this.port = port;
        this.password = password;
        this.threadName = threadName;
        this.timing = timing;
        this.waiter = waiter;
        this.clock = clock;
        this.listener = listener;
    }

    /**
     * Starts the reader thread; the first attempt begins at once. Returns immediately.
     *
     * @throws IllegalStateException if called twice or after {@link #stop()}
     */
    public void start() {
        Thread t = new Thread(this::run, threadName);
        t.setDaemon(true);
        synchronized (lock) {
            if (started || stopped) {
                throw new IllegalStateException("StreamClient is single-use");
            }
            started = true;
            thread = t;
        }
        t.start();
    }

    /**
     * Closes the connection, ends a backoff wait, and waits for the reader thread to end (at most two seconds).
     * Safe to call several times. Called from the reader thread itself (a callback), it does not wait for the thread.
     * See {@link StreamListener} for the
     * guarantee about callbacks.
     */
    public void stop() {
        Thread t;
        Socket s;
        synchronized (lock) {
            if (stopped) {
                return;
            }
            stopped = true;
            t = thread;
            s = socket;
        }
        stopLatch.countDown();
        // Closing is the only way to end a read or connect that is blocked in another thread.
        closeQuietly(s);
        if (t != null && !t.equals(Thread.currentThread())) {
            t.interrupt();
            try {
                t.join(timing.joinTimeout().toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isStopped() {
        synchronized (lock) {
            return stopped;
        }
    }

    private void run() {
        int failures = 0;
        while (!isStopped()) {
            AttemptResult result = attempt();
            if (isStopped()) {
                return;
            }
            StreamFailure failure = result.failure();
            logger.debug("Stream attempt ended: {}", failure.getMessage());
            notifyLost(failure);
            if (failure.isFinal()) {
                return;
            }
            if (result.stable()) {
                failures = 0;
            }
            failures++;
            long seconds = failure.getKind() == Kind.ENDPOINT_MISSING ? ENDPOINT_MISSING_RETRY_SECONDS
                    : backoffSeconds(failures);
            try {
                if (waiter.awaitStop(Duration.ofSeconds(seconds), stopLatch)) {
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * @param failures number of consecutive unstable failures, starting at 1
     * @return the delay before the next attempt: 2, 4, 8, 16, 32, 60, 60, ... seconds
     */
    static long backoffSeconds(int failures) {
        return Math.min(BACKOFF_MAX_SECONDS, BACKOFF_FIRST_SECONDS << Math.min(Math.max(failures - 1, 0), 5));
    }

    private record AttemptResult(StreamFailure failure, boolean stable) {
    }

    /**
     * Runs one connection from resolve to the end of the stream. The socket is closed on return. Throws only a
     * {@link VirtualMachineError}; every other unexpected {@link Throwable} ends the attempt as a protocol failure.
     */
    private AttemptResult attempt() {
        activeSignalled = false;
        hasValid = false;
        Socket s = new Socket();
        StreamFailure failure;
        try {
            synchronized (lock) {
                if (stopped) {
                    return new AttemptResult(new StreamFailure(Kind.CLOSED_BY_BRIDGE), false);
                }
                socket = s;
            }
            failure = connectAndStream(s);
        } catch (Throwable t) { // NOPMD AvoidCatchingThrowable - reportDefect rethrows VirtualMachineError
            reportDefect(t);
            failure = new StreamFailure(Kind.PROTOCOL);
        } finally {
            synchronized (lock) {
                socket = null;
            }
            closeQuietly(s);
        }
        boolean stable = hasValid && clock.getAsLong() - firstValidNanos >= STABLE_AFTER.toNanos();
        return new AttemptResult(failure, stable);
    }

    private StreamFailure connectAndStream(Socket s) {
        OutputStream out;
        try {
            InetAddress address = InetAddress.getByName(host);
            if (isStopped()) {
                return new StreamFailure(Kind.CLOSED_BY_BRIDGE);
            }
            s.connect(new InetSocketAddress(address, port), (int) timing.connectTimeout().toMillis());
            s.setTcpNoDelay(true);
            s.setSoTimeout((int) timing.liveness().toMillis());
            out = s.getOutputStream();
            out.write(buildRequest(host, password));
            out.flush();
        } catch (IOException e) {
            return new StreamFailure(Kind.UNREACHABLE, e);
        }
        try {
            return stream(s.getInputStream(), out);
        } catch (StreamFailure e) {
            return e;
        } catch (SocketTimeoutException e) {
            return new StreamFailure(Kind.SILENT, e);
        } catch (IOException e) {
            return new StreamFailure(Kind.CLOSED_BY_BRIDGE, e);
        }
    }

    private StreamFailure stream(InputStream rawIn, OutputStream out) throws IOException {
        BufferedInputStream in = new BufferedInputStream(rawIn);
        // Created before the first read so that the liveness clock starts when the request was sent.
        WebSocketMessageReader reader = new WebSocketMessageReader(in, out, clock, timing.liveness().toNanos(),
                this::onFrame);
        long startNanos = clock.getAsLong();
        in.mark(1);
        int first = in.read();
        if (first < 0) {
            return new StreamFailure(Kind.CLOSED_BY_BRIDGE);
        }
        if (first == 'H') {
            StreamFailure failure = readHttpHead(in, startNanos);
            if (failure != null) {
                return failure;
            }
        } else {
            in.reset();
        }
        while (!isStopped()) {
            byte[] message = reader.readMessage();
            if (isStopped()) {
                break;
            }
            if (listener.onMessage(message) && !hasValid) {
                hasValid = true;
                firstValidNanos = clock.getAsLong();
            }
        }
        return new StreamFailure(Kind.CLOSED_BY_BRIDGE);
    }

    private void onFrame() {
        if (!activeSignalled && !isStopped()) {
            activeSignalled = true;
            listener.onStreamActive();
        }
    }

    /**
     * Reads the rest of an HTTP head whose first byte, {@code H}, was consumed.
     *
     * @return null for status 101 (frames follow), otherwise the failure the status stands for
     */
    private @Nullable StreamFailure readHttpHead(InputStream in, long startNanos) throws IOException {
        ByteArrayOutputStream head = new ByteArrayOutputStream();
        head.write('H');
        int state = 0; // matched characters of CR LF CR LF
        while (state < 4) {
            if (head.size() >= MAX_HTTP_HEAD_BYTES) {
                return new StreamFailure(Kind.PROTOCOL);
            }
            // The socket timeout alone would not stop a head that drips in byte by byte.
            if (clock.getAsLong() - startNanos > timing.liveness().toNanos()) {
                return new StreamFailure(Kind.SILENT);
            }
            int b = in.read();
            if (b < 0) {
                return new StreamFailure(Kind.CLOSED_BY_BRIDGE);
            }
            head.write(b);
            if (b == ((state & 1) == 0 ? '\r' : '\n')) {
                state++;
            } else {
                state = b == '\r' ? 1 : 0;
            }
        }
        String text = head.toString(StandardCharsets.ISO_8859_1);
        Matcher m = STATUS_LINE.matcher(text.substring(0, text.indexOf("\r\n")));
        if (!m.matches()) {
            return new StreamFailure(Kind.PROTOCOL);
        }
        int status = Integer.parseInt(m.group(1));
        return switch (status) {
            case 101 -> null;
            case 401, 403 -> new StreamFailure(Kind.REJECTED);
            case 404 -> new StreamFailure(Kind.ENDPOINT_MISSING);
            default -> new StreamFailure(status);
        };
    }

    private void notifyLost(StreamFailure failure) {
        try {
            listener.onStreamLost(failure);
        } catch (Throwable t) { // NOPMD AvoidCatchingThrowable - reportDefect rethrows VirtualMachineError
            reportDefect(t);
        }
    }

    /**
     * Logs an unexpected {@link Throwable} from an attempt or a callback, or rethrows it if the JVM is no longer
     * reliable. A {@link VirtualMachineError} (out of memory, stack overflow) is not a defect of this binding and
     * cannot be recovered from reliably, so it must reach the thread's uncaught-exception handling; the socket is
     * closed by the caller's {@code finally} before it propagates. Anything else, including {@link LinkageError}
     * from a core that does not match the binding, is logged and survived so that the stream keeps reconnecting.
     *
     * The same defect repeats on every attempt, so it is logged at warn once per hour per description and at debug
     * otherwise.
     */
    private void reportDefect(Throwable t) {
        if (t instanceof VirtualMachineError vme) {
            throw vme;
        }
        String description = describe(t);
        long now = clock.getAsLong();
        Long last = defectsLoggedAt.get(description);
        if (last != null && now - last < DEFECT_RELOG_NANOS) {
            logger.debug("Repeated unexpected error in the stream client: {}", description);
            return;
        }
        if (defectsLoggedAt.size() >= MAX_TRACKED_DEFECTS) {
            defectsLoggedAt.clear();
        }
        defectsLoggedAt.put(description, now);
        logger.warn("Unexpected error in the stream client: {}", description);
    }

    /**
     * Names an unexpected error without its message: the error comes from code that handles the credential or the
     * meter data, so its text must not reach a log. The class and the throwing location are enough to find a defect.
     */
    private static String describe(Throwable e) {
        StackTraceElement[] trace = e.getStackTrace();
        return trace.length > 0 ? e.getClass().getName() + " at " + trace[0] : e.getClass().getName();
    }

    /**
     * Builds the stream request. The result contains the credential and must not be logged.
     */
    static byte[] buildRequest(String host, String password) {
        String hostHeader = host.indexOf(':') >= 0 && !host.startsWith("[") ? "[" + host + "]" : host;
        String credential = Base64.getEncoder().encodeToString(("admin:" + password).getBytes(StandardCharsets.UTF_8));
        return ("GET /ws HTTP/1.1\r\nHost: " + hostHeader + "\r\nAuthorization: Basic " + credential + "\r\n\r\n")
                .getBytes(StandardCharsets.US_ASCII);
    }

    private static void closeQuietly(@Nullable Socket s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                // Nothing to recover: the socket is discarded either way.
            }
        }
    }
}
