/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.bridge.slip.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an wrapper around {@link java.io.DataInputStream} to support socket receive operations.
 *
 * It implements a secondary polling thread to asynchronously read bytes from the socket input stream into a buffer. And
 * it parses the bytes into SLIP messages, which are placed on a message queue. Callers can access the SLIP messages in
 * this queue independently from the polling thread.
 *
 * @author Guenther Schreiner - Initial contribution.
 * @author Andrew Fiddian-Green - Complete rewrite using asynchronous polling thread.
 */
@NonNullByDefault
class DataInputStreamWithTimeout implements Closeable {

    private static final int QUEUE_SIZE = 16;
    private static final int BUFFER_SIZE = 512;
    private static final int SLEEP_INTERVAL = 50;

    // special character that marks the first and last byte of a slip message
    private static final byte SLIP_MARK = (byte) 0xc0;

    private final Logger logger = LoggerFactory.getLogger(DataInputStreamWithTimeout.class);

    private final Queue<byte[]> slipMessageQueue = new ConcurrentLinkedQueue<>();

    private InputStream inputStream;

    private String pollException = "";
    private @Nullable Thread pollThread = null;

    /**
     * A runnable that loops to read bytes from {@link InputStream} and builds SLIP packets from them. The respective
     * SLIP packets are placed in a {@link ConcurrentLinkedQueue}.
     */
    private Runnable pollRunner = () -> {
        byte _byte;
        byte[] _bytes = new byte[BUFFER_SIZE];
        int i = 0;
        pollException = "";
        slipMessageQueue.clear();
        while (!Thread.interrupted()) {
            try {
                _bytes[i] = _byte = (byte) inputStream.read();
                if (_byte == SLIP_MARK) {
                    if (i > 0) {
                        // the minimal slip message is 7 bytes [MM PP LL CC CC KK MM]
                        if ((i > 5) && (_bytes[0] == SLIP_MARK)) {
                            slipMessageQueue.offer(Arrays.copyOfRange(_bytes, 0, i + 1));
                            if (slipMessageQueue.size() > QUEUE_SIZE) {
                                logger.debug("pollRunner() => slip message queue overflow");
                                slipMessageQueue.poll();
                            }
                        }
                        i = 0;
                        _bytes[0] = SLIP_MARK;
                        continue;
                    }
                }
                if (++i >= BUFFER_SIZE) {
                    i = 0;
                }
            } catch (SocketTimeoutException e) {
                // socket read time outs are OK => just keep on polling
                continue;
            } catch (IOException e) {
                // any other exception => stop polling
                pollException = e.getMessage();
                logger.debug("pollRunner() stopping '{}'", pollException);
                break;
            }
        }
        pollThread = null;
    };

    /**
     * Check if there was an exception on the polling loop thread and if so, throw it back on the caller thread.
     *
     * @throws IOException
     */
    private void throwIfPollException() throws IOException {
        if (!pollException.isEmpty()) {
            logger.debug("passPollException() polling loop exception {}", pollException);
            throw new IOException(pollException);
        }
    }

    /**
     * Creates a {@link DataInputStreamWithTimeout} as a wrapper around the specified underlying DataInputStream.
     *
     * @param stream the specified input stream
     */
    public DataInputStreamWithTimeout(InputStream stream) {
        inputStream = stream;
    }

    /**
     * Overridden method of {@link Closeable} interface. Stops the polling thread.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        stopPolling();
    }

    /**
     * Reads and removes the next available SLIP message from the queue. If the queue is empty, continue polling
     * until either a message is found, or the timeout expires.
     *
     * @param timeoutMSecs the timeout period in milliseconds.
     * @return the next SLIP message if there is one on the queue, or any empty byte[] array if not.
     * @throws IOException
     */
    public byte[] readSlipMessage(int timeoutMSecs) throws IOException {
        startPolling();
        int i = timeoutMSecs / SLEEP_INTERVAL;
        while (i-- >= 0) {
            try {
                byte[] slip = slipMessageQueue.remove();
                logger.trace("readSlipMessage() => return slip message");
                return slip;
            } catch (NoSuchElementException e) {
                // queue empty, wait and continue
            }
            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                logger.debug("readSlipMessage() => thread interrupt");
                throw new IOException("Thread Interrupted");
            }
            throwIfPollException();
        }
        logger.debug("readSlipMessage() => no slip message after {}mS => time out", timeoutMSecs);
        return new byte[0];
    }

    public int available() {
        int size = slipMessageQueue.size();
        logger.trace("available() => slip message count {}", size);
        return size;
    }

    public void flush() {
        logger.trace("flush() called");
        slipMessageQueue.clear();
    }

    private void startPolling() {
        if (pollThread == null) {
            logger.trace("startPolling()");
            Thread pollThreadX = pollThread = new Thread(pollRunner);
            pollThreadX.start();
        }
    }

    private void stopPolling() {
        Thread pollThreadX = pollThread;
        if (pollThreadX != null) {
            logger.trace("stopPolling()");
            pollThreadX.interrupt();
            pollThread = null;
        }
    }
}
