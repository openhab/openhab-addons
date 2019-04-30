/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.osramlightify.internal;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;

import org.openhab.binding.osramlightify.internal.LightifyTransmitQueueSender;

/**
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyTransmitQueue<T> {

    private final Logger logger = LoggerFactory.getLogger(LightifyTransmitQueue.class);

    private final Queue<T> queue = new LinkedBlockingQueue<T>();

    private final LightifyTransmitQueueSender<T> sender;

    public LightifyTransmitQueue(LightifyTransmitQueueSender<T> sender) {
        this.sender = sender;
    }

    public synchronized void enqueue(T msg) {
        boolean wasEmpty = queue.isEmpty();
        if (queue.offer(msg)) {
            if (wasEmpty) {
                send();
            }
        } else {
            logger.error("Transmit queue overflow. Lost message: {}", msg);
        }
    }

    public T peek() {
        return queue.peek();
    }

    public synchronized T sendNext() {
        T msg = queue.poll();
        send();
        return msg;
    }

    public synchronized void send() {
        while (!queue.isEmpty() && !sender.transmitQueueSender(queue.peek())) {
            // sender says the message is bad so discard it and try the next.
            queue.poll();
        }
    }
}
