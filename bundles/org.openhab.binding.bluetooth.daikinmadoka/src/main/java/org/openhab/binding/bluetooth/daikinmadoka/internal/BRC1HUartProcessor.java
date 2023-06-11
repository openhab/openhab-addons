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
package org.openhab.binding.bluetooth.daikinmadoka.internal;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.ResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * As the protocol emutes an UART communication over BLE (characteristics write/notify), this class takes care of BLE
 * transport.
 *
 * @author Benjamin Lafois - Initial contribution
 */
@NonNullByDefault
public class BRC1HUartProcessor {

    private final Logger logger = LoggerFactory.getLogger(BRC1HUartProcessor.class);

    /**
     * Maximum number of bytes per message chunk, including headers
     */
    public static final int MAX_CHUNK_SIZE = 20;

    /**
     * In the unlikely event of messages arrive in wrong order, this comparator will sort the queue
     */
    private Comparator<byte[]> chunkSorter = (byte[] m1, byte[] m2) -> m1[0] - m2[0];

    private ConcurrentSkipListSet<byte[]> uartMessages = new ConcurrentSkipListSet<>(chunkSorter);

    private ResponseListener responseListener;

    private final Lock stateLock = new ReentrantLock();

    public BRC1HUartProcessor(ResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    private boolean isMessageComplete() {
        int messagesInQueue = this.uartMessages.size();

        if (messagesInQueue <= 0) {
            return false;
        }

        byte[] firstMessageInQueue = uartMessages.first();
        if (firstMessageInQueue.length < 2) {
            return false;
        }

        int expectedChunks = (int) Math.ceil(firstMessageInQueue[1] / (MAX_CHUNK_SIZE - 1.0));
        if (expectedChunks != messagesInQueue) {
            return false;
        }

        // Check that we have every single ID
        int expected = 0;
        for (byte[] m : this.uartMessages) {
            if (m.length < 2) {
                return false;
            }

            if (m[0] != expected++) {
                return false;
            }
        }
        return true;
    }

    public void chunkReceived(byte[] byteValue) {
        byte[] fullReceivedMessage = null;
        stateLock.lock();
        try {
            this.uartMessages.add(byteValue);
            if (isMessageComplete()) {
                logger.debug("Complete message received!");

                // Beyond this point, full message received
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                for (byte[] msg : uartMessages) {
                    if (msg.length > 1) {
                        bos.write(msg, 1, msg.length - 1);
                    }
                }

                this.uartMessages.clear();
                fullReceivedMessage = bos.toByteArray();
            }
        } finally {
            stateLock.unlock();
        }

        if (fullReceivedMessage != null) {
            this.responseListener.receivedResponse(fullReceivedMessage);
        }
    }

    public void abandon() {
        this.uartMessages.clear();
    }
}
