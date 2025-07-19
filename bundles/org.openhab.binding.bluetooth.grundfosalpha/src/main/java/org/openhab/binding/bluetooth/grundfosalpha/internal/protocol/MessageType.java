/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.grundfosalpha.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This represents the different types of messages that can be requested and received.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum MessageType {
    FlowHead(new byte[] { 0x1f, 0x00, 0x01, 0x30, 0x01, 0x00, 0x00, 0x18 },
            new byte[] { (byte) 0x03, (byte) 0x5d, (byte) 0x01, (byte) 0x21 }),
    Power(new byte[] { 0x2c, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x25 },
            new byte[] { (byte) 0x03, (byte) 0x57, (byte) 0x00, (byte) 0x45 });

    private final byte[] responseType;
    private final byte[] request;

    MessageType(byte[] responseType, byte[] requestMessage) {
        this.responseType = responseType;

        int messageLength = requestMessage.length + 3;
        request = new byte[messageLength + 4];

        // Append the header
        MessageHeader.setRequestHeader(request, messageLength);

        // Append the message type-specific part
        System.arraycopy(requestMessage, 0, request, MessageHeader.LENGTH, requestMessage.length);

        CRC16Calculator.put(request, messageLength);
    }

    public byte[] responseType() {
        return responseType;
    }

    public byte[] request() {
        return request;
    }
}
