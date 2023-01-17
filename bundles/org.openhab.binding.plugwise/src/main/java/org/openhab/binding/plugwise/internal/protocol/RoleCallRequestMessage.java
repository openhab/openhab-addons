/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.DEVICE_ROLE_CALL_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests the Circle+ to return the MAC address for a specific node. This message is answered by a
 * {@link RoleCallResponseMessage} which contains the MAC address. Because a Plugwise network can have 64 devices,
 * the node ID value has a range from 0 to 63.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
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
