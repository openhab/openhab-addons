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
package org.openhab.binding.plugwise.internal;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plugwise.internal.protocol.Message;

/**
 * A queued message that is being sent or waiting to be sent to the Stick.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
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
