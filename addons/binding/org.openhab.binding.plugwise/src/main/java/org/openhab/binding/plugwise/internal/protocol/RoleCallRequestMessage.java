/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.DEVICE_ROLE_CALL_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests the Circle+ to return the MAC address for a specific node. This message is answered by a
 * {@link RoleCallResponseMessage} which contains the MAC address. Because a Plugwise network can have 64 devices,
 * the node ID value has a range from 0 to 63.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class RoleCallRequestMessage extends Message {

    private int nodeID;

    public RoleCallRequestMessage(MACAddress macAddress, int nodeID) {
        super(DEVICE_ROLE_CALL_REQUEST, macAddress);
        this.nodeID = nodeID;
    }

    @Override
    public String getPayload() {
        return String.format("%02X", nodeID);
    }

    @Override
    protected String payloadToHexString() {
        return String.format("%02X", nodeID);
    }

}
