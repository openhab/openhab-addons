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

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a Callable to read SLIP messages from the input stream.
 *
 * It implements a secondary polling thread to asynchronously read bytes from the socket input stream into a buffer.
 * And it parses the bytes into SLIP messages, which are placed on a message queue.
 *
 * @author Andrew Fiddian-Green - Initial Contribution; refactored from private class in DataInputStreamWithTimeout
 */
@NonNullByDefault
public class Poller implements Callable<Boolean> {

    private static final int BUFFER_SIZE = 512;
    private static final int QUEUE_SIZE = 512;

    // special character that marks the first and last byte of a slip message
    private static final byte SLIP_MARK = (byte) 0xc0;
    private static final byte SLIP_PROT = 0;

    private final Logger logger = LoggerFactory.getLogger(Poller.class);

    private final InputStream inputStream;
    private final Queue<byte[]> messageQueue;

    private @Nullable volatile Thread thread;

    public Poller(InputStream stream, Queue<byte[]> queue) {
        logger.trace("Poller: created");
        inputStream = stream;
        messageQueue = queue;
    }

    public void interrupt() {
        Thread thread = this.thread;
        if ((thread != null) && thread.isAlive()) {
            thread.interrupt();
        }
    }

    /**
     * Task that loops to read bytes from inputStream and build SLIP packets from them. The SLIP packets are placed in
     * messageQueue. It runs until 'interrupt()' or 'Thread.interrupt()' are called.
     *
     * @throws IOException in case of socket read errors
     */
    @Override
    public Boolean call() throws IOException {
        thread = Thread.currentThread();
        logger.trace("Poller.call(): started");
        byte[] buf = new byte[BUFFER_SIZE];
        int byt;
        int i = 0;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                byt = inputStream.read(); // throws IOException
                // end of stream is OK => continue polling
                if (byt < 0) {
                    continue;
                }
            } catch (SocketTimeoutException e) {
                // socket read time out is OK => continue polling
                continue;
            }
            buf[i] = (byte) byt;
            if ((i > 0) && (buf[i] == SLIP_MARK)) {
                // the minimal slip message is 7 bytes [MM PP LL CC CC KK MM]
                if ((i > 5) && (buf[0] == SLIP_MARK) && (buf[1] == SLIP_PROT)) {
                    messageQueue.offer(Arrays.copyOfRange(buf, 0, i + 1));
                    if (messageQueue.size() > QUEUE_SIZE) {
                        logger.warn("Poller.call(): slip message queue overflow => PLEASE REPORT !!");
                        messageQueue.poll();
                    }
                    i = 0;
                } else {
                    if (logger.isWarnEnabled()) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j <= i; j++) {
                            sb.append(String.format("%02X ", buf[j]));
                        }
                        logger.warn("Poller.call(): non slip messsage {} discarded => PLEASE REPORT !!", sb.toString());
                    }
                    buf[0] = SLIP_MARK;
                    i = 1;
                }
                continue;
            }
            if (++i >= BUFFER_SIZE) {
                logger.warn("Poller.call(): input buffer overrun => PLEASE REPORT !!");
                i = 0;
            }
        }
        logger.trace("Poller.call(): completed");
        return true;
    }
}
