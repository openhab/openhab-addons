/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.message;

import java.util.EventObject;

/**
 * Event for messages received from the Visonic alarm panel
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxMessageEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private PowermaxBaseMessage message;

    public PowermaxMessageEvent(Object source, PowermaxBaseMessage message) {
        super(source);
        this.message = message;
    }

    /**
     * @return the message object built from the received message
     */
    public PowermaxBaseMessage getMessage() {
        return message;
    }

}
