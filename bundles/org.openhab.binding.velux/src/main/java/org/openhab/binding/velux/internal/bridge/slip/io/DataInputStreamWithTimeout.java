/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a wrapper around {@link java.io.InputStream} to support socket receive operations.
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

    private static final int SLEEP_INTERVAL_MSECS = 50;
    private static final long MAX_WAIT_SECONDS = 15;

    private final Logger logger = LoggerFactory.getLogger(DataInputStreamWithTimeout.class);

    private final Queue<byte[]> slipMessageQueue = new ConcurrentLinkedQueue<>();
    private final InputStream inputStream;
    private final VeluxBridgeHandler bridge;

    private @Nullable Poller poller;
    private @Nullable Future<Boolean> future;
    private @Nullable ExecutorService executor;

    /**
     * Creates a {@link DataInputStreamWithTimeout} as a wrapper around the specified underlying {@link InputStream}
     *
     * @param inputStream the specified input stream
     * @param bridge the actual Bridge Thing instance
     */
    public DataInputStreamWithTimeout(InputStream inputStream, VeluxBridgeHandler bridge) {
        this.inputStream = inputStream;
        this.bridge = bridge;
    }

    /**
     * Overridden method of {@link Closeable} interface. Stops the polling task.
     *
     * @throws IOException (although actually no exceptions are thrown)
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
     * @throws IOException if the poller task has unexpectedly terminated e.g. via an IOException, or if either the
     *             poller task, or the calling thread have been interrupted
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
                // queue empty, fall through and continue
            }
            try {
                Future<Boolean> future = this.future;
                if ((future != null) && future.isDone()) {
                    future.get(); // throws ExecutionException, InterruptedException
                    // future terminated without exception, but prematurely, which is itself an exception
                    throw new IOException("Poller thread terminated prematurely");
                }
                Thread.sleep(SLEEP_INTERVAL_MSECS); // throws InterruptedException
            } catch (ExecutionException | InterruptedException e) {
                // re-cast other exceptions as IOException
                throw new IOException(e);
            }
        }
        logger.debug("readSlipMessage() => no slip message");
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
        if (future == null) {
            logger.debug("startPolling() called");
            slipMessageQueue.clear();
            poller = new Poller(inputStream, slipMessageQueue);
            ExecutorService executor = this.executor = Executors.newSingleThreadExecutor(bridge.getThreadFactory());
            future = executor.submit(poller);
        }
    }

    /**
     * Stop the polling task
     */
    private void stopPolling() {
        logger.debug("stopPolling() called");

        Poller poller = this.poller;
        Future<Boolean> future = this.future;
        ExecutorService executor = this.executor;

        this.poller = null;
        this.future = null;
        this.executor = null;

        if (executor != null) {
            executor.shutdown();
        }
        if (poller != null) {
            poller.interrupt();
        }
        if (future != null) {
            try {
                future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                // expected exception due to e.g. IOException on socket close
            } catch (TimeoutException | InterruptedException e) {
                // unexpected exception due to e.g. KLF200 'zombie state'
                logger.warn("stopPolling() exception '{}' => PLEASE REPORT !!", e.getMessage());
            }
        }
    }
}
