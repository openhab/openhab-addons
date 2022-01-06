/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * When there are multiple queued messages, the message priority and date/time determine which message is sent first.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
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
    UPDATE_AND_DISCOVERY

}
