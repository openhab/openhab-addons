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
package org.openhab.binding.panamaxfurman.internal.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanCommunicationEventListener;
import org.openhab.binding.panamaxfurman.internal.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read incoming data from the Power Conditioner device and notify listeners accordingly.
 *
 * @author Dave Badia - Initial contribution
 *
 */
@NonNullByDefault
class InputReaderThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(InputReaderThread.class);

    private static final int SHUTDOWN_TIMEOUT_MILLIS = 500;

    private final String connectionName;
    private final Semaphore readerReadySemaphore;
    private final PanamaxFurmanCommunicationEventListener eventGenerator;
    private volatile boolean stopReader = false;
    private volatile boolean isReading = true;
    private BufferedReader bufferedReader;

    /**
     * This latch is used to track when the thread has terminated
     */
    private CountDownLatch stopLatch = new CountDownLatch(1);

    /**
     * Construct a reader thread to poll the given InputStream for incoming data
     */
    InputReaderThread(String connectionName, Semaphore readerReadySemaphore, InputStream inputStream,
            PanamaxFurmanCommunicationEventListener eventListener) {
        this.connectionName = connectionName;
        this.readerReadySemaphore = readerReadySemaphore;
        this.eventGenerator = eventListener;
        logger.trace("creating InputStreamReaderThread  @{}", connectionName);
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        this.setDaemon(true);
        this.setName("PanamaxFurmanInputStreamReader-" + connectionName);
        try {
            readerReadySemaphore.acquire();
        } catch (InterruptedException e) {
            logger.debug("Interrupted during readerReadySemaphore.acquire().  @{}", connectionName, e);
        }
    }

    @Override
    public void run() {
        try {
            logger.debug("InputStreamReaderThread.run stopReader={}  @{}", stopReader, connectionName);
            readUntilTimeoutOrError();
        } catch (IOException e) {
            // This is normal when the socket is closed, so don't log anything if we are stopping
            if (!stopReader) {
                logger.debug("Error reading data from @{}. ", connectionName, e);
            }
        } finally {
            isReading = false;
            // Notify the stopReader method caller that the reader is stopped.
            this.stopLatch.countDown();
            Util.closeQuietly(bufferedReader);
        }
        logger.debug("InputStreamReaderThread thread exiting.  @{}", connectionName);
    }

    boolean isReading() {
        return isReading;
    }

    private void readUntilTimeoutOrError() throws IOException {
        while (!stopReader && !Thread.currentThread().isInterrupted()) {
            String receivedData = null;
            readerReadySemaphore.release();
            logger.trace("InputStreamReaderThread.run waiting to read data  @{}", connectionName);
            // Blocks until data is received OR socketReadTimeoutSeconds is reached
            receivedData = bufferedReader.readLine();
            if (receivedData == null) {
                throw new IOException("End of stream reached @" + connectionName);
            } else {
                logger.trace("<< {}    @{}", receivedData, connectionName);
                if (receivedData.trim().length() > 0) {
                    if (!logger.isTraceEnabled()) {
                        logger.debug("<< {}    @{}", receivedData, connectionName);
                    }
                    eventGenerator.onInformationReceived(receivedData);
                }
            }
        }
    }

    void shutdown() {
        stopReader = true;
        long startNanos = System.nanoTime();
        // Closing the reader will trigger an exception to exit out of the while() loop
        Util.closeQuietly(bufferedReader);
        try {
            // This needs to return quickly so keep the timeout short
            boolean stopped = this.stopLatch.await(SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

            if (stopped) {
                logger.debug("Took {}ms for InputStreamReaderThread to stop.  @{}",
                        (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)), connectionName);
            } else {
                logger.debug("Timed out after {}ms waiting for InputStreamReaderThread to stop.  @{}",
                        SHUTDOWN_TIMEOUT_MILLIS, connectionName);
            }
        } catch (InterruptedException e) {
            logger.debug("Interrupted while waiting for InputStreamReaderThread to stop.  @{}", connectionName, e);
        }
    }
}
