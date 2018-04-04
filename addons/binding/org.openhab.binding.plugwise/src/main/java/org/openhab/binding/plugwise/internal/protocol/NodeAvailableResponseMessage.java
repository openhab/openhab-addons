/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.NODE_AVAILABLE_RESPONSE;

/**
 * Response to a device when its {@link NodeAvailableMessage} is "accepted".
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class NodeAvailableResponseMessage extends Message {

    private boolean acceptanceCode;

    private String destinationMAC;

    public NodeAvailableResponseMessage(boolean code, String destination) {
        super(NODE_AVAILABLE_RESPONSE);
        acceptanceCode = code;
        destinationMAC = destination;
    }

    public boolean isAcceptanceCode() {
        return acceptanceCode;
    }

    @Override
    protected String payloadToHexString() {
        return String.format("%02X", acceptanceCode ? 1 : 0) + destinationMAC;
    }

}
