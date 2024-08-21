/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.nio.charset.StandardCharsets;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.MessageType;

/**
 * Base class to represent Plugwise protocol data units.
 *
 * In general a message consists of a hex string containing the following parts:
 * <ul>
 * <li>a type indicator - many types are yet to be reverse engineered
 * <li>a sequence number - messages are numbered so that we can keep track of them in an application
 * <li>a MAC address - the destination of the message
 * <li>a payload
 * <li>a CRC checksum that is calculated using the previously mentioned segments of the message
 * </ul>
 *
 * Before sending off a message in the Plugwise network they are prepended with a protocol header and trailer is
 * added at the end.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public abstract class Message {

    public static String getCRC(String string) {
        int crc = 0x0000;
        int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)

        for (byte b : string.getBytes(StandardCharsets.US_ASCII)) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }

        crc &= 0xFFFF;

        return (String.format("%04X", crc));
    }

    protected MessageType type;
    protected Integer sequenceNumber;
    protected MACAddress macAddress;

    protected String payload;

    protected Message(MessageType messageType) {
        this(messageType, null, null, null);
    }

    protected Message(MessageType messageType, Integer sequenceNumber, MACAddress macAddress, String payload) {
        this.type = messageType;
        this.sequenceNumber = sequenceNumber;
        this.macAddress = macAddress;
        this.payload = payload;

        if (payload != null) {
            parsePayload();
        }
    }

    protected Message(MessageType messageType, Integer sequenceNumber, String payload) {
        this(messageType, sequenceNumber, null, payload);
    }

    protected Message(MessageType messageType, MACAddress macAddress) {
        this(messageType, null, macAddress, null);
    }

    protected Message(MessageType messageType, MACAddress macAddress, String payload) {
        this(messageType, null, macAddress, payload);
    }

    protected Message(MessageType messageType, String payload) {
        this(messageType, null, null, payload);
    }

    public MACAddress getMACAddress() {
        return macAddress;
    }

    public String getPayload() {
        return payload;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public MessageType getType() {
        return type;
    }

    // Method that implementation classes have to override, and that is responsible for parsing the payload into
    // meaningful fields
    protected void parsePayload() {
    }

    protected String payloadToHexString() {
        return payload != null ? payload : "";
    }

    private String sequenceNumberToHexString() {
        return String.format("%04X", sequenceNumber);
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String toHexString() {
        StringBuilder sb = new StringBuilder();
        sb.append(typeToHexString());
        if (sequenceNumber != null) {
            sb.append(sequenceNumberToHexString());
        }
        if (macAddress != null) {
            sb.append(macAddress);
        }
        sb.append(payloadToHexString());

        String string = sb.toString();
        String crc = getCRC(string);

        return string + crc;
    }

    @Override
    public String toString() {
        return "Message [type=" + (type != null ? type.name() : null) + ", macAddress=" + macAddress
                + ", sequenceNumber=" + sequenceNumber + ", payload=" + payload + "]";
    }

    private String typeToHexString() {
        return String.format("%04X", type.toInt());
    }
}
