/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
 * @since 2.1
 */
public class QueuedSend implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(QueuedSend.class);

    BlockingQueue<QueueItem> queue = new LinkedBlockingQueue<>(20);
    protected final DatagramPacket packet;
    protected final DatagramSocket datagramSocket;
    private int delayBetweenCommands = 100;
    private int repeatCommands = 1;
    private boolean willbeclosed = false;
    private Thread thread;

    public static final byte NO_CATEGORY = 0;

    /**
     * Creates a new send queue and starts the background thread. Call setAddress and
     * setPort before using any of the queue commands.
     *
     * @throws SocketException
     */
    public QueuedSend() throws SocketException {
        byte[] a = new byte[0];
        packet = new DatagramPacket(a, a.length);
        datagramSocket = new DatagramSocket();
    }

    /**
     * Start the send thread of this queue. Call dispose() to quit the thread.
     */
    public void start() {
        willbeclosed = false;
        thread = new Thread(this);
        thread.start();
    }

    public int getDelayBetweenCommands() {
        return delayBetweenCommands;
    }

    public int getRepeatCommands() {
        return repeatCommands;
    }

    public void setRepeatCommands(int repeatCommands) {
        repeatCommands = Math.max(1, Math.min(5, repeatCommands));
        this.repeatCommands = repeatCommands;
    }

    public void setDelayBetweenCommands(int ms) {
        ms = Math.max(0, Math.min(400, ms));
        delayBetweenCommands = ms;
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

            if (item == null || item.uniqueCommandId == QueueItem.INVALID) {
                // Just in case it is a command chain, set the item to null to not process any chained commands.
                item = null;
                continue;
            }

            packet.setData(item.data);
            try {
                for (int i = 0; i < (item.repeatable ? repeatCommands : 1); ++i) {
                    datagramSocket.send(packet);

                    if (logger.isDebugEnabled()) {
                        StringBuilder s = new StringBuilder();
                        for (int c = 0; c < item.data.length; ++c) {
                            s.append(String.format("%02X ", item.data[c]));
                        }
                        logger.debug("Sent packet '{}' to bridge {}", s.toString(),
                                packet.getAddress().getHostAddress());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to send Message to '{}': {}", packet.getAddress().getHostAddress(),
                        e.getMessage());
            }

            try {
                Thread.sleep((item.customDelayTime != 0) ? item.customDelayTime : delayBetweenCommands);
            } catch (InterruptedException e) {
                if (!willbeclosed) {
                    logger.error("Queue sleep failed: {}", e.getLocalizedMessage());
                }
                break;
            }
        }
    }

    /**
     * Once disposed, this object can't be reused anymore.
     */
    public void dispose() {
        willbeclosed = true;
        if (thread != null) {
            try {
                thread.join(delayBetweenCommands);
            } catch (InterruptedException e) {
            }
            thread.interrupt();
        }
        thread = null;
    }

    public void setRepeatTimes(int times) {
        repeatCommands = times;
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
                if (item != null && item.uniqueCommandId == uniqueCommandId) {
                    item.uniqueCommandId = QueueItem.INVALID; // invalidate
                }
            } catch (IllegalStateException e) {
                // Ignore threading errors
                logger.error("{}", e.getLocalizedMessage());
            } catch (NoSuchElementException e) {
                // The element might have been processed already while iterate.
                // Ignore NoSuchElementException here.
            }
        }
    }

    /**
     * Add data to the send queue. Use a category of 0 to make an item non-categorised.
     * Commands which need to be queued up and not replacing same type commands must be non-categorised.
     * Items added to the queue are considered repeatable, in the sense that they do not cause side effects
     * if they are send multiple times (e.g. absolute values).
     *
     * If you want to, you can add multiple byte sequences to the queue, even if they have the same id.
     * This is used for animations and multi-message commands. Be aware that a later added command with the
     * same id will replace all those commands at once.
     *
     * @param uniqueCommandId A unique command id. Commands with the same id will overwrite themself.
     * @param data Data to be send
     */
    public void queueRepeatable(int uniqueCommandId, byte[]... data) {
        removeFromQueue(uniqueCommandId);
        QueueItem item = QueueItem.createRepeatable(uniqueCommandId, data[0]);
        QueueItem next = item;
        for (int i = 1; i < data.length; ++i) {
            next = next.addRepeatable(data[i]);
        }
        queue.offer(item);
    }

    /**
     * Add data to the send queue.
     * You have to create your own QueueItem, but this allows to you create a chain of commands. A chain will always
     * executed in order and without interrupting the sequence with another command. A chain will be removed completely
     * if another command with the same category is added except if the chain has been started to be processed.
     *
     * @param item A queue item, cannot be null.
     */
    public void queue(QueueItem item) {
        if (item.uniqueCommandId != NO_CATEGORY) {
            removeFromQueue(item.uniqueCommandId);
        }
        queue.offer(item);
    }

    public InetAddress getAddr() {
        return packet.getAddress();
    }

    public int getPort() {
        return packet.getPort();
    }

    public DatagramSocket getSocket() {
        return datagramSocket;
    }

    public void setAddress(InetAddress address) {
        packet.setAddress(address);
    }

    public void setPort(int port) {
        packet.setPort(port);
    }
}
