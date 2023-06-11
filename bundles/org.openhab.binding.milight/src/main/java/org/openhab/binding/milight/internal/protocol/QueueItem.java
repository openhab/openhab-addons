/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A queue item meant to be used for {@link QueuedSend}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class QueueItem {
    protected final int uniqueCommandId;
    protected final boolean repeatable;
    protected final int delayTime;
    protected final int repeatCommands;
    protected final DatagramPacket packet;
    private boolean invalid = false;

    final DatagramSocket socket;
    private final QueueItem root;
    private @Nullable QueueItem last;
    public @Nullable QueueItem next;

    /**
     * Add data to the send queue.
     * Commands which need to be queued up and not replacing same type commands must be non-categorised.
     * Items can be repeatable, in the sense that they do not cause side effects if they are send multiple times
     * (e.g. absolute values).
     *
     * @param socket A socket
     * @param uniqueCommandId A unique command id. A later command with the same id as previous ones will
     *            overwrite those. 0 means a non categorised entry.
     * @param data Data to be send
     * @param repeatable A repeatable command should not cause side effects by sending it multiple times.
     * @param delayTime A delay time that is used instead of the default delay time between commands for all
     *            added byte sequences.
     */
    public QueueItem(DatagramSocket socket, int uniqueCommandId, byte[] data, boolean repeatable, int delayTime,
            int repeatCommands, InetAddress address, int port) {
        this.socket = socket;
        this.uniqueCommandId = uniqueCommandId;
        this.repeatable = repeatable;
        this.delayTime = delayTime;
        this.repeatCommands = repeatCommands;
        this.root = this;
        this.packet = new DatagramPacket(data, data.length, address, port);
    }

    /**
     * @see #QueueItem(int, byte[], boolean, int)
     * @param uniqueCommandId A unique command id. A later command with the same id as previous ones will
     *            overwrite those. 0 means a non categorised entry.
     * @param data Data to be send
     * @param repeatable A repeatable command should not cause side effects by sending it multiple times.
     * @param customDelayTime A delay time that is used instead of the default delay time between commands for all
     *            added byte sequences.
     * @param root Another item is the root entry for this one.
     */
    private QueueItem(DatagramSocket socket, int uniqueCommandId, byte[] data, boolean repeatable, int customDelayTime,
            int repeatCommands, InetAddress address, int port, QueueItem root) {
        this.socket = socket;
        this.uniqueCommandId = uniqueCommandId;
        this.repeatable = repeatable;
        this.delayTime = customDelayTime;
        this.repeatCommands = repeatCommands;
        this.root = root;
        this.packet = new DatagramPacket(data, data.length, address, port);
    }

    /**
     * Add non-categorised, repeatable data to the send queue.
     *
     * <p>
     * Commands which need to be queued up and not replacing same type commands must use this method.
     * Items added to the queue are considered not repeatable (suited for relative commands where a repetition would
     * cause a change of value).
     * </p>
     *
     * <p>
     * If you want to, you can add multiple byte sequences to the queue, even if they have the same id.
     * This is used for animations and multi-message commands. Be aware that a later added command with the
     * same id will replace all those commands at once.
     * </p>
     *
     * @param data Data to be send
     */
    public static QueueItem createRepeatable(DatagramSocket socket, int customDelayTime, int repeatCommands,
            InetAddress address, int port, byte[]... data) {
        QueueItem item = new QueueItem(socket, QueuedSend.NO_CATEGORY, data[0], true, customDelayTime, repeatCommands,
                address, port);

        QueueItem next = item;
        for (int i = 1; i < data.length; ++i) {
            next = next.addRepeatable(data[i]);
        }
        return item;
    }

    /**
     * Add non-categorised, non-repeatable data to the send queue.
     *
     * <p>
     * Commands which need to be queued up and not replacing same type commands must use this method.
     * Items added to the queue are considered not repeatable (suited for relative commands where a repetition would
     * cause a change of value).
     * </p>
     *
     * <p>
     * If you want to, you can add multiple byte sequences to the queue, even if they have the same id.
     * This is used for animations and multi-message commands. Be aware that a later added command with the
     * same id will replace all those commands at once.
     * </p>
     *
     * @param data Data to be send
     */
    public static QueueItem createNonRepeatable(DatagramSocket socket, int customDelayTime, InetAddress address,
            int port, byte[]... data) {
        QueueItem item = new QueueItem(socket, QueuedSend.NO_CATEGORY, data[0], false, customDelayTime, 1, address,
                port);

        QueueItem next = item;
        for (int i = 1; i < data.length; ++i) {
            next = next.addRepeatable(data[i]);
        }
        return item;
    }

    private QueueItem createNewItem(byte[] data, boolean repeatable) {
        QueueItem newItem = new QueueItem(socket, uniqueCommandId, data, repeatable, delayTime, repeatCommands,
                packet.getAddress(), packet.getPort(), root);

        // The the next pointer of the last element in the chain to the new item
        QueueItem lastInChain = root.last != null ? root.last : root;
        lastInChain.next = newItem;

        return newItem;
    }

    /**
     * Add a command to the command chain. Can be called on any of the
     * commands in the chain, but will overwrite the next command in chain.
     *
     * This method can be used in a cascading way like:
     * QueueItem item ...;
     * send(item.addNonRepeatable(...).addNonRepeatable(...))
     *
     *
     * @param data Add data to the chain of commands
     * @return Always return the root command
     */
    public QueueItem addNonRepeatable(byte[] data) {
        root.last = createNewItem(data, false);
        return this.root;
    }

    /**
     * Add a command to the command chain. Can be called on any of the
     * commands in the chain, but will overwrite the next command in chain.
     *
     * This method can be used in a cascading way like:
     * QueueItem item ...;
     * send(item.addRepeatable(...).addRepeatable(...))
     *
     *
     * @param data Add data to the chain of commands
     * @return Always return the root command
     */
    public QueueItem addRepeatable(byte[] data) {
        root.last = createNewItem(data, true);
        return this.root;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void makeInvalid() {
        this.invalid = true;
    }
}
