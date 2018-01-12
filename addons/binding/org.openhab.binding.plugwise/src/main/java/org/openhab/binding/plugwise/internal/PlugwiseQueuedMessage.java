/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import java.time.LocalDateTime;

import org.openhab.binding.plugwise.internal.protocol.Message;

/**
 * A queued message that is being sent or waiting to be sent to the Stick.
 *
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseQueuedMessage {

    private final PlugwiseMessagePriority priority;
    private final LocalDateTime dateTime = LocalDateTime.now();
    private final Message message;
    private int attempts;

    public PlugwiseQueuedMessage(Message message, PlugwiseMessagePriority priority) {
        this.message = message;
        this.priority = priority;
    }

    public int getAttempts() {
        return attempts;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Message getMessage() {
        return message;
    }

    public PlugwiseMessagePriority getPriority() {
        return priority;
    }

    public void increaseAttempts() {
        attempts++;
    }
}
