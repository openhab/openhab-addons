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
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.NODE_AVAILABLE_RESPONSE;

/**
 * Response to a device when its {@link NodeAvailableMessage} is "accepted".
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
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
