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
package org.openhab.binding.milight.internal.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements a queue for UDP sending, where each item to be send is associated with an id.
 * If a new item is added, that has the same id of an already queued item, it replaces the
 * queued item. This is used for milight packets, where older bridges accept commands with a 100ms
 * delay only. The user may issue absolute brightness or color changes faster than 1/10s though, and we don't
 * want to just queue up those commands but apply the newest command only.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class QueuedSend implements Runnable, Closeable {
    private final Logger logger = LoggerFactory.getLogger(QueuedSend.class);

    final BlockingQueue<QueueItem> queue = new LinkedBlockingQueue<>(20);
    private boolean willbeclosed = false;
    private @Nullable Thread thread;

    public static final byte NO_CATEGORY = 0;

    /**
     * Start the send thread of this queue. Call dispose() to quit the thread.
     */
    public void start() {
        willbeclosed = false;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * The queue process
     */
    @Override
    public void run() {
        QueueItem item = null;
        while (!willbeclosed) {
            // If the command belongs to a chain of commands, get the next command now.
            if (item != null && item.next != null) {
                item = item.next;
            } else {
                try {
                    // block/wait for another item
                    item = queue.take();
                } catch (InterruptedException e) {
                    if (!willbeclosed) {
                        logger.error("Queue take failed: {}", e.getLocalizedMessage());
                    }
                    break;
                }
            }

            if (item.isInvalid()) {
                // Just in case it is a command chain, set the item to null to not process any chained commands.
                item = null;
                continue;
            }

            try {
                for (int i = 0; i < (item.repeatable ? item.repeatCommands : 1); ++i) {
                    item.socket.send(item.packet);

                    if (ProtocolConstants.DEBUG_SESSION) {
                        StringBuilder s = new StringBuilder();
                        for (int c = 0; c < item.packet.getData().length; ++c) {
                            s.append(String.format("%02X ", item.packet.getData()[c]));
                        }
                        logger.debug("Sent packet '{}' to bridge {}", s.toString(),
                                item.packet.getAddress().getHostAddress());
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to send Message to '{}': {}", item.packet.getAddress().getHostAddress(),
                        e.getMessage());
            }

            try {
                Thread.sleep(item.delayTime);
            } catch (InterruptedException e) {
                if (!willbeclosed) {
                    logger.warn("Queue sleep failed: {}", e.getLocalizedMessage());
                }
                break;
            }
        }
    }

    /**
     * Mark all commands in the queue invalid that have the same unique id as the given one. This does not synchronise
     * with the sender thread. If an element has been started to being processed, this method has no more effect on that
     * element. Command chains are always executed in a row. Even if the head of the command queue has been marked
     * as invalid, if the processing has been started, the chain will be processed completely.
     *
     * @param uniqueCommandId
     */
    private void removeFromQueue(int uniqueCommandId) {
        Iterator<QueueItem> iterator = queue.iterator();
        while (iterator.hasNext()) {
            try {
                QueueItem item = iterator.next();
                if (item.uniqueCommandId == uniqueCommandId) {
                    item.makeInvalid();
                }
            } catch (IllegalStateException e) {
                // Ignore threading errors
            } catch (NoSuchElementException e) {
                // The element might have been processed already while iterate.
                // Ignore NoSuchElementException here.
            }
        }
    }

    /**
     * Add data to the send queue.
     *
     * <p>
     * You have to create your own QueueItem. This allows to you create a chain of commands. A chain will always
     * executed in order and without interrupting the sequence with another command. A chain will be removed completely
     * if another command with the same category is added except if the chain has been started to be processed.
     * </p>
     *
     * @param item A queue item, cannot be null.
     */
    public void queue(QueueItem item) {
        if (item.uniqueCommandId != NO_CATEGORY) {
            removeFromQueue(item.uniqueCommandId);
        }
        queue.offer(item);
    }

    /**
     * Once closed, this object can't be reused anymore.
     */
    @Override
    public void close() throws IOException {
        willbeclosed = true;
        final Thread threadL = this.thread;
        if (threadL != null) {
            try {
                threadL.join(200);
            } catch (InterruptedException e) {
            }
            threadL.interrupt();
        }
        this.thread = null;
    }
}
