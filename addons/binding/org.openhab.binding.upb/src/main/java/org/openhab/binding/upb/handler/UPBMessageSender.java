/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.handler;

import org.openhab.binding.upb.internal.MessageBuilder;

/**
 * Defines the functions for sending messages to a UPB modem.
 *
 * @author Chris Van Orman
 * @since 2.0.0
 */
public interface UPBMessageSender {

    /**
     * Sends the provide message to a UPB modem.
     * 
     * @param message the message to send
     */
    void sendMessage(MessageBuilder message);
}
