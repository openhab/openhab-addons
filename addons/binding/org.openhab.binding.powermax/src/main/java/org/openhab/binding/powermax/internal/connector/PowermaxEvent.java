/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.connector;

import java.util.EventObject;

import org.openhab.binding.powermax.internal.message.PowermaxBaseMessage;

/**
 * Event for messages received from the Visonic alarm panel
 *
 * @author Laurent Garnier
 * @since 1.9.0
 */
public class PowermaxEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private PowermaxBaseMessage powermaxMessage;

    /**
     * Constructor
     *
     * @param source
     * @param powermaxMessage
     *            the message object built from the received message
     */
    public PowermaxEvent(Object source, PowermaxBaseMessage powermaxMessage) {
        super(source);
        this.powermaxMessage = powermaxMessage;
    }

    /**
     * Returns the message object build from the received message
     *
     * @return powermaxMessage: the message object built from the received message
     */
    public PowermaxBaseMessage getPowermaxMessage() {
        return powermaxMessage;
    }

}
