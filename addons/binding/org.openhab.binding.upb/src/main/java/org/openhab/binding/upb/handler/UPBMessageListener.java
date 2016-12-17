/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.handler;

import org.openhab.binding.upb.internal.UPBMessage;

/**
 * Defines functions for a class to listen for {@link UPBMessage}s.
 *
 * @author Chris Van Orman
 * @since 2.0.0
 *
 */
public interface UPBMessageListener {

    /**
     * Sets the {@link UPBMessageSender} that can be used to send messages to a UPB modem.
     *
     * @param messageSender the {@link UPBMessageSender} to set.
     */
    void setMessageSender(UPBMessageSender messageSender);

    /**
     * Called whenever a {@link UPBMessage} has been received.
     * 
     * @param message the received message.
     */
    void messageReceived(UPBMessage message);
}
