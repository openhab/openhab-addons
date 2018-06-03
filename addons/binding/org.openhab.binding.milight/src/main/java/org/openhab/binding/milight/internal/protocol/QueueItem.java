/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.protocol;

/**
 *
 * @author David Graeff - Initial contribution
 */
public class QueueItem {
    static final int INVALID = -1;
    byte[] data;
    int uniqueCommandId;
    boolean repeatable;
    int customDelayTime;

    private QueueItem root, last = null;
    QueueItem next = null;

    /**
     * Add data to the send queue.
     * Commands which need to be queued up and not replacing same type commands must be non-categorised.
     * Items can be repeatable, in the sense that they do not cause side effects if they are send multiple times
     * (e.g. absolute values).
     *
     * @param uniqueCommandId A unique command id. A later command with the same id as previous ones will
     *            overwrite those. 0 means a non categorised entry.
     * @param data Data to be send
     *
     * @param repeatable A repeatable command should not cause side effects by sending it multiple times.
     * @param customDelayTime A delay time that is used instead of the default delay time between commands for all
     *            added byte sequences.
     */
    QueueItem(int uniqueCommandId, byte[] data, boolean repeatable, int customDelayTime) {
        this.data = data;
        this.uniqueCommandId = uniqueCommandId;
        this.repeatable = repeatable;
        this.customDelayTime = customDelayTime;
        this.root = this;
    }

    /**
     * Used for animations. A custom delay time is used before advancing to the next queued command.
     *
     * @param uniqueCommandId A command id.
     * @param data Command data to be send.
     * @param customDelayTime Custom delay time in ms.
     * @return Returns a QueuedItem for being used with the {@see QueuedSend.queue()} method.
     */
    public static QueueItem createRepeatable(int uniqueCommandId, byte[] data, int customDelayTime) {
        return new QueueItem(uniqueCommandId, data, true, customDelayTime);
    }

    public static QueueItem createRepeatable(int uniqueCommandId, byte[] data) {
        return new QueueItem(uniqueCommandId, data, true, 0);
    }

    public static QueueItem createRepeatable(byte[] data) {
        return new QueueItem(QueuedSend.NO_CATEGORY, data, true, 0);
    }

    public static QueueItem createNonRepeatable(int uniqueCommandId, byte[] data) {
        return new QueueItem(uniqueCommandId, data, false, 0);
    }

    /**
     * Add non-categorised, non-repeatable data to the send queue
     * Commands which need to be queued up and not replacing same type commands must use this method.
     * Items added to the queue are considered not repeatable (suited for relative commands where a repetition would
     * cause a change of value).
     *
     * @param data Data to be send
     */
    public static QueueItem createNonRepeatable(byte[] data) {
        return new QueueItem(QueuedSend.NO_CATEGORY, data, false, 0);
    }

    /**
     * Add a command to the command chain. addNext() can be called on any of the
     * commands in the chain, but will overwrite the next command in chain.
     *
     * The addNext() command can be used in a cascading way like:
     * QueueItem item ...;
     * send(item.addNext(...).addNext(...))
     *
     *
     * @param data Add data to the chain of commands
     * @param repeatable Repeatable or not repeatable command
     * @return Always return the root command
     */
    public QueueItem addNonRepeatable(byte[] data) {
        QueueItem lastInChain = root.last != null ? root.last : root;

        // Create new item and set the pointer to the root element
        QueueItem newItem = new QueueItem(this.uniqueCommandId, data, false, this.customDelayTime);
        newItem.root = root;

        // The the next pointer of the last element in the chain to the new item
        lastInChain.next = newItem;

        // Set the pointer to the last element in the chain to the new command
        root.last = newItem;

        // Always return the root
        return this.root;
    }

    public QueueItem addRepeatable(byte[] data) {
        addNonRepeatable(data);
        root.last.repeatable = true;
        return this.root;
    }
}
