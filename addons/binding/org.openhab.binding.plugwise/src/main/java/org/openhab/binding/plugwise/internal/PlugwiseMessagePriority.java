/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

/**
 * When there are multiple queued messages, the message priority and date/time determine which message is sent first.
 *
 * @author Wouter Born - Initial contribution
 */
public enum PlugwiseMessagePriority {

    /**
     * Messages caused by Thing channel commands have the highest priority, e.g. to switch power on/off
     */
    COMMAND,

    /**
     * Messages that update the state of Thing channels immediately after a command has been sent.
     */
    FAST_UPDATE,

    /**
     * Messages for normal state updates and Thing discovery. E.g. scheduled tasks that update the state of a
     * channel.
     */
    UPDATE_AND_DISCOVERY;

}
