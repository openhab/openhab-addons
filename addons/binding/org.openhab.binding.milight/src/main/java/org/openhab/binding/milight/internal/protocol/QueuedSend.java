/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
 * @author David Graeff <david.graeff@web.de>
 * @since 2.1
 *
 */
public class QueuedSend implements Runnable {
    private static class QueueItem {
        byte[] data;
        int unique_command_id;
        boolean repeatable;
        int custom_delay_time;

        public QueueItem(byte[] data, int unique_command_id, boolean repeatable, int custom_delay_time) {
            super();
            this.data = data;
            this.unique_command_id = unique_command_id;
            this.repeatable = repeatable;
            this.custom_delay_time = custom_delay_time;
        }
    }

    BlockingQueue<QueueItem> queue = new LinkedBlockingQueue<>(20);
    protected final DatagramPacket packet;
    protected final DatagramSocket datagramSocket;
    private static final Logger logger = LoggerFactory.getLogger(QueuedSend.class);
    private int delay_between_commands = 100;
    private int repeat_commands = 1;
    private boolean willbeclosed = false;
    private Thread thread;

    public static final byte NO_CATEGORY = 0;

    /**
     * Creates a new send queue and starts the background thread.
     *
     * @param addr May be null but has to be set by setAddress before first call to queue().
     * @param port The destination port
     * @throws SocketException
     */
    public QueuedSend(InetAddress addr, int port) throws SocketException {
        byte[] a = new byte[0];
        packet = new DatagramPacket(a, a.length, addr, port);
        datagramSocket = new DatagramSocket();
        thread = new Thread(this);
        thread.start();
    }

    public int getDelayBetweenCommands() {
        return delay_between_commands;
    }

    public int getRepeatCommands() {
        return repeat_commands;
    }

    public void setRepeatCommands(int repeat_commands) {
        this.repeat_commands = repeat_commands;
    }

    public void setDelayBetweenCommands(int ms) {
        delay_between_commands = ms;
    }

    /**
     * The queue process
     */
    @Override
    public void run() {
        while (!willbeclosed) {
            QueueItem item;
            int delay_time = delay_between_commands;
            try {
                // block/wait for another item
                item = queue.take();
            } catch (InterruptedException e) {
                if (!willbeclosed) {
                    logger.error("Queue take failed: " + e.getLocalizedMessage());
                }
                break;
            }

            if (item == null) {
                continue;
            }

            if (item.custom_delay_time != 0) {
                delay_time = item.custom_delay_time;
            }

            packet.setData(item.data);
            try {
                int repeat_remaining = item.repeatable ? repeat_commands : 1;
                repeat_remaining = Math.min(repeat_remaining, 1);

                StringBuffer s = new StringBuffer();
                for (int i = 0; i < item.data.length; ++i) {
                    s.append(String.format("%02X ", item.data[i]));
                }

                while (repeat_remaining > 0) {
                    datagramSocket.send(packet);
                    --repeat_remaining;
                    // logger.debug("Sent packet '{}' to bridge {}", s.toString(),
                    // packet.getAddress().getHostAddress());
                }

            } catch (Exception e) {
                logger.error("Failed to send Message to '{}': {}", packet.getAddress().getHostAddress(),
                        e.getMessage());
            }

            try {
                Thread.sleep(delay_time);
            } catch (InterruptedException e) {
                if (!willbeclosed) {
                    logger.error("Queue sleep failed: " + e.getLocalizedMessage());
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
                thread.join(delay_between_commands);
            } catch (InterruptedException e) {
            }
            thread.interrupt();
        }
        thread = null;
    }

    public void setRepeatTimes(int times) {
        repeat_commands = times;
    }

    private void remove_from_queue(int unique_command_id) {
        if (unique_command_id > 0) {
            synchronized (queue) {
                while (queue.iterator().hasNext()) {
                    try {
                        if (queue.iterator().next().unique_command_id == unique_command_id) {
                            queue.iterator().remove();
                        }
                    } catch (NoSuchElementException | IllegalStateException e) {
                        // The element might have been processed already while iterate.
                        // Ignore NoSuchElementException here.
                    }
                }
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
     * @param unique_command_id A unique command id. Commands with the same id will overwrite themself.
     * @param data Data to be send
     */
    public void queueRepeatable(int unique_command_id, byte[]... data) {
        synchronized (queue) {
            remove_from_queue(unique_command_id);
            for (int i = 0; i < data.length; ++i) {
                queue.offer(new QueueItem(data[i], unique_command_id, true, 0));
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
     * @param unique_command_id A unique command id. Commands with the same id will overwrite themself.
     * @param custom_delay_time A delay time that is used instead of the default delay time between commands for all
     *            added byte sequences.
     * @param data Data to be send
     */
    public void queueRepeatable(int unique_command_id, int custom_delay_time, byte[]... data) {
        synchronized (queue) {
            remove_from_queue(unique_command_id);
            for (int i = 0; i < data.length; ++i) {
                queue.offer(new QueueItem(data[i], unique_command_id, true, custom_delay_time));
            }
        }
    }

    /**
     * Add data to the send queue. Use a category of 0 to make an item non-categorised.
     * Commands which need to be queued up and not replacing same type commands must be non-categorised.
     * Items added to the queue are considered not repeatable (suited for relative commands where a repetition would
     * cause a change of value).
     *
     * If you want to, you can add multiple byte sequences to the queue, even if they have the same id.
     * This is used for animations and multi-message commands. Be aware that a later added command with the
     * same id will replace all those commands at once.
     *
     * @param unique_command_id A unique command id. Commands with the same id will overwrite themself.
     * @param data Data to be send
     */
    public void queue(int unique_command_id, byte[]... data) {
        remove_from_queue(unique_command_id);
        synchronized (queue) {
            for (int i = 0; i < data.length; ++i) {
                queue.offer(new QueueItem(data[i], unique_command_id, false, 0));
            }
        }
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
