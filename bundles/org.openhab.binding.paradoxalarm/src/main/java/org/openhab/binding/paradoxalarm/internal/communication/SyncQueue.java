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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SyncQueue} is used to synchronize communication to/from Paradox system. All requests go into sendQueue and
 * upon send are popped from send queue and are pushed into receiveQueue.
 * Due to nature of Paradox communication receive queue is with priority, i.e. if there is anything in receive queue we
 * attempt to read the socket first and only after receive queue is empty then we attempt to send. We never send any
 * packet if we have something to read.
 * For more details about usage see method {@link AbstractCommunicator#submitRequest(IRequest)}
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class SyncQueue {

    private final Logger logger = LoggerFactory.getLogger(SyncQueue.class);

    private BlockingQueue<IRequest> sendQueue = new ArrayBlockingQueue<>(1000, true);
    private BlockingQueue<IRequest> receiveQueue = new ArrayBlockingQueue<>(10, true);

    private static SyncQueue syncQueue;

    private SyncQueue() {
    }

    public static SyncQueue getInstance() {
        SyncQueue temp = syncQueue;
        if (temp == null) {
            synchronized (SyncQueue.class) {
                temp = syncQueue;
                if (temp == null) {
                    syncQueue = new SyncQueue();
                }
            }
        }
        return syncQueue;
    }

    public synchronized void add(IRequest request) {
        logger.trace("Adding to queue request={}", request);
        sendQueue.add(request);
    }

    public synchronized void moveRequest() {
        IRequest request = sendQueue.poll();
        request.setTimeStamp();
        logger.trace("Moving from Tx to RX queue request={}", request);
        receiveQueue.add(request);
    }

    public synchronized IRequest poll() {
        IRequest request = receiveQueue.poll();
        logger.trace("Removing from queue request={}", request);
        return request;
    }

    public synchronized IRequest removeSendRequest() {
        IRequest request = sendQueue.poll();
        logger.trace("Removing from queue request={}", request);
        return request;
    }

    public synchronized IRequest peekSendQueue() {
        return sendQueue.peek();
    }

    public IRequest peekReceiveQueue() {
        return receiveQueue.peek();
    }

    public synchronized boolean hasPacketToReceive() {
        return receiveQueue.peek() != null;
    }

    public synchronized boolean hasPacketsToSend() {
        return sendQueue.peek() != null;
    }

    public synchronized boolean canSend() {
        return receiveQueue.isEmpty();
    }
}
