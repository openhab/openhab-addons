/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.osramlightify.internal.LightifyTransmitQueueSender;

/**
 * @author Mike Jagdis - Initial contribution
 */
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
