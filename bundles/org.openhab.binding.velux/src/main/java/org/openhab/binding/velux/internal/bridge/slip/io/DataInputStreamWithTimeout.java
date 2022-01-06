/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an wrapper around {@link java.io.InputStream} to support socket receive operations.
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

    private static final int QUEUE_SIZE = 512;
    private static final int BUFFER_SIZE = 512;
    private static final int SLEEP_INTERVAL_MSECS = 50;

    // special character that marks the first and last byte of a slip message
    private static final byte SLIP_MARK = (byte) 0xc0;
    private static final byte SLIP_PROT = 0;

    private final Logger logger = LoggerFactory.getLogger(DataInputStreamWithTimeout.class);

    private final Queue<byte[]> slipMessageQueue = new ConcurrentLinkedQueue<>();

    private InputStream inputStream;

    private @Nullable String pollException = null;
    private @Nullable Poller pollRunner = null;
    private ExecutorService executor;

    private class Poller implements Callable<Boolean> {

        private boolean interrupted = false;
        private Future<Boolean> pollerFinished;

        public Poller(ExecutorService executor) {
            logger.trace("Poller: created");
            pollerFinished = executor.submit(this);
        }

        public void interrupt() {
            interrupted = true;
            try {
                pollerFinished.get();
            } catch (InterruptedException | ExecutionException e) {
            }
        }

        /**
         * Task that loops to read bytes from {@link InputStream} and build SLIP packets from them. The SLIP packets are
         * placed in a {@link ConcurrentLinkedQueue}. It loops continuously until 'interrupt()' or 'Thread.interrupt()'
         * are called when terminates early after the next socket read timeout.
         */
        @Override
        public Boolean call() throws Exception {
            logger.trace("Poller.call(): started");
            byte[] buf = new byte[BUFFER_SIZE];
            int byt;
            int i = 0;

            // clean start, no exception, empty queue
            pollException = null;
            slipMessageQueue.clear();

            // loop forever; on shutdown interrupt() gets called to break out of the loop
            while (true) {
                try {
                    if (interrupted) {
                        // fully flush the input buffer
                        inputStream.readAllBytes();
                        break;
                    }
                    byt = inputStream.read();
                    if (byt < 0) {
                        // end of stream is OK => keep on polling
                        continue;
                    }
                    buf[i] = (byte) byt;
                    if ((i > 0) && (buf[i] == SLIP_MARK)) {
                        // the minimal slip message is 7 bytes [MM PP LL CC CC KK MM]
                        if ((i > 5) && (buf[0] == SLIP_MARK) && (buf[1] == SLIP_PROT)) {
                            slipMessageQueue.offer(Arrays.copyOfRange(buf, 0, i + 1));
                            if (slipMessageQueue.size() > QUEUE_SIZE) {
                                logger.warn("Poller.call(): slip message queue overflow => PLEASE REPORT !!");
                                slipMessageQueue.poll();
                            }
                            i = 0;
                        } else {
                            logger.warn("Poller.call(): non slip messsage discarded => PLEASE REPORT !!");
                            buf[0] = SLIP_MARK;
                            i = 1;
                        }
                        continue;
                    }
                    if (++i >= BUFFER_SIZE) {
                        logger.warn("Poller.call(): input buffer overrun => PLEASE REPORT !!");
                        i = 0;
                    }
                } catch (SocketTimeoutException e) {
                    // socket read time outs are OK => keep on polling; unless interrupted
                    if (interrupted) {
                        break;
                    }
                    continue;
                } catch (IOException e) {
                    // any other exception => stop polling
                    String msg = e.getMessage();
                    pollException = msg != null ? msg : "Generic IOException";
                    logger.debug("Poller.call(): stopping '{}'", pollException);
                    break;
                }
            }

            logger.trace("Poller.call(): ended");
            // we only get here if shutdown or an error occurs so free ourself so we can be recreated again
            pollRunner = null;
            return true;
        }
    }

    /**
     * Check if there was an exception on the polling loop task and if so, throw it back on the caller thread.
     *
     * @throws IOException
     */
    private void throwIfPollException() throws IOException {
        if (pollException != null) {
            logger.debug("passPollException() polling loop exception {}", pollException);
            throw new IOException(pollException);
        }
    }

    /**
     * Creates a {@link DataInputStreamWithTimeout} as a wrapper around the specified underlying {@link InputStream}
     *
     * @param stream the specified input stream
     * @param bridge the actual Bridge Thing instance
     */
    public DataInputStreamWithTimeout(InputStream stream, VeluxBridgeHandler bridge) {
        inputStream = stream;
        executor = Executors.newSingleThreadExecutor(bridge.getThreadFactory());
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
    public synchronized byte[] readSlipMessage(int timeoutMSecs) throws IOException {
        startPolling();
        int i = (timeoutMSecs / SLEEP_INTERVAL_MSECS) + 1;
        while (i-- >= 0) {
            try {
                byte[] slip = slipMessageQueue.remove();
                logger.trace("readSlipMessage() => return slip message");
                return slip;
            } catch (NoSuchElementException e) {
                // queue empty, wait and continue
            }
            throwIfPollException();
            try {
                Thread.sleep(SLEEP_INTERVAL_MSECS);
            } catch (InterruptedException e) {
                logger.debug("readSlipMessage() => thread interrupt");
            }
        }
        logger.debug("readSlipMessage() => no slip message after {}mS => time out", timeoutMSecs);
        return new byte[0];
    }

    /**
     * Get the number of incoming messages in the queue
     *
     * @return the number of incoming messages in the queue
     */
    public int available() {
        int size = slipMessageQueue.size();
        logger.trace("available() => slip message count {}", size);
        return size;
    }

    /**
     * Clear the queue
     */
    public void flush() {
        logger.trace("flush() called");
        slipMessageQueue.clear();
    }

    /**
     * Start the polling task
     */
    private void startPolling() {
        if (pollRunner == null) {
            logger.trace("startPolling()");
            pollRunner = new Poller(executor);
        }
    }

    /**
     * Stop the polling task
     */
    private void stopPolling() {
        Poller pollRunner = this.pollRunner;
        if (pollRunner != null) {
            logger.trace("stopPolling()");
            pollRunner.interrupt();
        }
        executor.shutdown();
    }
}
