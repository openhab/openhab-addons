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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.apache.commons.net.telnet.TelnetClient;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A telnet connection to the Ethernet port (Link 9) of a HomeWorks processor.
 *
 * After login the processor presents the same interface as its RS232 ports, apart from the prompt text ({@code LNET>}
 * instead of {@code L232>}). The login is a single {@code user, password} line sent in response to a {@code LOGIN:}
 * prompt. The processor answers with {@code login successful} or {@code login incorrect}, and prompts again after a
 * rejected login. Prompts are not terminated by a newline, so incoming data is assembled into lines here and prompts
 * are removed before lines are passed to the listener.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class HwTelnetConnection implements Closeable {

    /**
     * Receives the output of a {@link HwTelnetConnection}. Callbacks run on the connection's reader thread.
     */
    public interface Listener {
        /**
         * A line was received from the processor. Prompts have been removed and the line is neither empty nor blank.
         */
        void lineReceived(HwTelnetConnection source, String line);

        /**
         * The connection was lost or closed by the processor. Not called after {@link HwTelnetConnection#close()}.
         */
        void connectionClosed(HwTelnetConnection source, String reason);
    }

    /**
     * The processor rejected the login or a login is required but no credentials were supplied.
     */
    public static class HwLoginException extends IOException {
        private static final long serialVersionUID = 1L;

        public HwLoginException(String message) {
            super(message);
        }
    }

    /**
     * Gives access to the socket, so that it can be closed while a write is blocked on it.
     */
    private static final class HwTelnetClient extends TelnetClient {
        @Nullable
        Socket socket() {
            return _socket_;
        }
    }

    static final int DEFAULT_PORT = 23;

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int LOGIN_TIMEOUT_MS = 10_000;
    private static final int WRITE_TIMEOUT_MS = 10_000;
    private static final int QUIT_TIMEOUT_MS = 1_000;
    private static final int BUFFER_SIZE = 1024;
    private static final int MAX_PENDING_CHARS = 4096;

    private static final Pattern LOGIN_PROMPT = Pattern.compile("(?i).*login:\\s*");
    private static final Pattern LOGIN_SUCCESSFUL = Pattern.compile("(?i)\\s*login successful.*");
    private static final Pattern LOGIN_INCORRECT = Pattern.compile("(?i)\\s*login incorrect.*");
    // Processor prompts are L232> on serial and LNET> on telnet. They can precede an event on the same line.
    private static final Pattern COMMAND_PROMPT = Pattern.compile("\\s*[A-Z0-9]{3,5}>\\s*");
    private static final Pattern LEADING_PROMPTS = Pattern.compile("^\\s*(?:[A-Z0-9]{3,5}>\\s*)+");

    private enum State {
        AWAITING_LOGIN_PROMPT,
        AWAITING_LOGIN_RESULT,
        READY,
        CLOSED
    }

    private final Logger logger = LoggerFactory.getLogger(HwTelnetConnection.class);

    private final String host;
    private final int port;
    private final @Nullable String user;
    private final @Nullable String password;
    private final Listener listener;
    private final String threadName;
    private final ScheduledExecutorService scheduler;

    private final HwTelnetClient client = new HwTelnetClient();
    // Serializes commands. A write blocks when the processor stops reading and the send buffer is full, so close()
    // never takes this lock and every write is bounded by a timeout that closes the socket.
    private final ReentrantLock writeLock = new ReentrantLock();
    private final CompletableFuture<Boolean> loginResult = new CompletableFuture<>();
    private final StringBuilder pending = new StringBuilder();

    private volatile State state = State.AWAITING_LOGIN_PROMPT;
    private volatile boolean closing;
    // Not final so that tests can shorten it
    volatile long loginTimeoutMs = LOGIN_TIMEOUT_MS;
    volatile long writeTimeoutMs = WRITE_TIMEOUT_MS;
    private volatile long lastReceivedNanos = System.nanoTime();
    private volatile @Nullable OutputStream output;
    private volatile @Nullable Socket socket;
    private @Nullable Thread readerThread;

    public HwTelnetConnection(String host, int port, @Nullable String user, @Nullable String password,
            Listener listener, String threadName, ScheduledExecutorService scheduler) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.listener = listener;
        this.threadName = threadName;
        this.scheduler = scheduler;
    }

    /**
     * Connects and logs in. Blocks until the session is ready for commands.
     *
     * @throws HwLoginException if the processor rejects the login, or asks for a login and none is configured
     * @throws IOException if the connection fails or the processor does not respond
     */
    public void open() throws IOException {
        logger.debug("Connecting to HomeWorks processor at {}:{}", host, port);
        client.setConnectTimeout(CONNECT_TIMEOUT_MS);
        client.connect(host, port);
        socket = client.socket();
        client.setKeepAlive(true);
        output = client.getOutputStream();
        InputStream input = client.getInputStream();
        logger.debug("Connected to {}:{}, waiting for login prompt", host, port);

        // A dedicated thread, because the reader blocks on the socket for the whole life of the connection and would
        // otherwise occupy a thread of the shared scheduler pool. Everything else runs on the scheduler.
        Thread thread = new Thread(() -> readLoop(input), threadName);
        thread.setDaemon(true);
        readerThread = thread;
        thread.start();

        try {
            waitForLogin();
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }
    }

    private void waitForLogin() throws IOException {
        try {
            loginResult.get(loginTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new IOException("Timed out waiting for login prompt or result", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for login", e);
        } catch (ExecutionException e) {
            throw loginFailure(e);
        }
    }

    private IOException loginFailure(ExecutionException e) {
        Throwable cause = e.getCause();
        return cause instanceof IOException io ? io : new IOException("Login failed", e);
    }

    /**
     * Sends a command line to the processor.
     *
     * @throws IOException if the connection is closed, or the processor did not accept the command in time, in which
     *             case the connection is closed and the loss is reported to the listener
     */
    public void send(String command) throws IOException {
        writeLock.lock();
        try {
            write(command, writeTimeoutMs);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Sends {@code QUIT} if no other command is being sent, waiting at most a short time for it, and then closes the
     * connection.
     */
    public void quitAndClose() {
        if (writeLock.tryLock()) {
            try {
                write("QUIT", QUIT_TIMEOUT_MS);
            } catch (IOException e) {
                logger.debug("Could not send QUIT before closing: {}", e.getMessage());
            } finally {
                writeLock.unlock();
            }
        }
        close();
    }

    private void write(String command, long timeoutMs) throws IOException {
        OutputStream out = output;
        if (out == null || state == State.CLOSED) {
            throw new IOException("Connection is closed");
        }
        if (state != State.READY) {
            throw new IOException("Login is not complete");
        }
        logger.debug("Sending to HomeWorks processor: {}", command);
        ScheduledFuture<?> watchdog = scheduler.schedule(this::closeSocket, timeoutMs, TimeUnit.MILLISECONDS);
        try {
            out.write((command + "\r\n").getBytes(StandardCharsets.US_ASCII));
            out.flush();
        } catch (IOException e) {
            if (watchdog.isDone() && !watchdog.isCancelled()) {
                throw new IOException("Processor did not accept data for " + timeoutMs + " ms", e);
            }
            throw e;
        } finally {
            watchdog.cancel(false);
        }
    }

    /**
     * Returns how long ago data of any kind was last received from the processor. Used to detect a connection that
     * was lost without the operating system noticing.
     */
    public long millisSinceLastReceived() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastReceivedNanos);
    }

    public boolean isReady() {
        return state == State.READY;
    }

    @Override
    public void close() {
        closing = true;
        state = State.CLOSED;
        loginResult.completeExceptionally(new IOException("Connection closed"));
        // Makes a blocked write fail at once
        closeSocket();
        try {
            client.disconnect();
        } catch (IOException e) {
            logger.debug("Error closing telnet connection to {}:{}: {}", host, port, e.getMessage());
        }
        output = null;
        Thread thread = readerThread;
        if (thread != null && !thread.equals(Thread.currentThread())) {
            thread.interrupt();
        }
        logger.debug("Closed telnet connection to {}:{}", host, port);
    }

    /**
     * Closes the socket, which makes a blocked write fail and ends the reader.
     */
    private void closeSocket() {
        Socket current = socket;
        if (current != null) {
            try {
                current.close();
            } catch (IOException e) {
                logger.debug("Error closing socket to {}:{}: {}", host, port, e.getMessage());
            }
        }
    }

    private void readLoop(InputStream input) {
        byte[] buffer = new byte[BUFFER_SIZE];
        String reason = "Connection closed by processor";
        try {
            int count;
            while ((count = input.read(buffer)) != -1) {
                lastReceivedNanos = System.nanoTime();
                String chunk = new String(buffer, 0, count, StandardCharsets.US_ASCII);
                if (logger.isTraceEnabled()) {
                    logger.trace("Raw data from {}:{}: {}", host, port, escape(chunk));
                }
                handleData(chunk);
            }
        } catch (IOException e) {
            String message = e.getMessage();
            reason = message != null ? message : "Read error";
            if (!closing) {
                logger.debug("Error reading from {}:{}: {}", host, port, reason);
            }
        }
        if (!closing) {
            logger.debug("Telnet connection to {}:{} ended: {}", host, port, reason);
            state = State.CLOSED;
            loginResult.completeExceptionally(new IOException(reason));
            listener.connectionClosed(this, reason);
        }
    }

    void handleData(String chunk) {
        pending.append(chunk);

        int end;
        while ((end = indexOfLineEnd(pending)) >= 0) {
            String line = pending.substring(0, end);
            pending.delete(0, end + 1);
            handleLine(line);
        }

        // What is left has no line ending. It may be a prompt, which the processor does not terminate.
        String partial = pending.toString();
        if (!partial.isEmpty() && state == State.AWAITING_LOGIN_PROMPT && LOGIN_PROMPT.matcher(partial).matches()) {
            pending.setLength(0);
            sendLogin();
        } else if (state == State.AWAITING_LOGIN_PROMPT && COMMAND_PROMPT.matcher(partial).matches()) {
            // A command prompt instead of a login prompt: the processor does not require a login.
            logger.debug("Received command prompt from {}:{} without a login prompt, no login is required", host, port);
            pending.setLength(0);
            state = State.READY;
            loginResult.complete(Boolean.TRUE);
        } else if (partial.length() > MAX_PENDING_CHARS) {
            logger.debug("Discarding {} characters of unterminated data from {}:{}", partial.length(), host, port);
            pending.setLength(0);
        }
    }

    private static int indexOfLineEnd(StringBuilder text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\r' || c == '\n') {
                return i;
            }
        }
        return -1;
    }

    private void handleLine(String rawLine) {
        State current = state;
        if (current == State.AWAITING_LOGIN_RESULT) {
            if (LOGIN_SUCCESSFUL.matcher(rawLine).matches()) {
                logger.debug("Login to {}:{} successful", host, port);
                state = State.READY;
                loginResult.complete(Boolean.TRUE);
                return;
            } else if (LOGIN_INCORRECT.matcher(rawLine).matches()) {
                logger.debug("Login to {}:{} rejected by processor", host, port);
                loginResult.completeExceptionally(new HwLoginException("Login rejected by processor"));
                return;
            }
        }

        String line = LEADING_PROMPTS.matcher(rawLine).replaceFirst("").trim();
        if (line.isEmpty()) {
            return;
        }

        if (state == State.READY) {
            listener.lineReceived(this, line);
        } else {
            logger.debug("Ignoring line received before login completed: {}", line);
        }
    }

    private void sendLogin() {
        String loginUser = user;
        String loginPassword = password;
        if (loginUser == null || loginUser.isEmpty()) {
            logger.debug("Processor at {}:{} requires a login but no user is configured", host, port);
            loginResult.completeExceptionally(new HwLoginException("Processor requires a login"));
            return;
        }
        state = State.AWAITING_LOGIN_RESULT;
        // The password is never logged.
        logger.debug("Sending login for user {}", loginUser);
        String loginLine = loginPassword == null || loginPassword.isEmpty() ? loginUser
                : loginUser + ", " + loginPassword;
        try {
            OutputStream out = output;
            if (out == null) {
                throw new IOException("Connection is closed");
            }
            out.write((loginLine + "\r\n").getBytes(StandardCharsets.US_ASCII));
            out.flush();
        } catch (IOException e) {
            loginResult.completeExceptionally(e);
        }
    }

    private static String escape(String text) {
        return text.replace("\r", "\\r").replace("\n", "\\n");
    }
}
