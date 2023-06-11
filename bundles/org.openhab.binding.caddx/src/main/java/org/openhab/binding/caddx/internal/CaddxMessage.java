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
package org.openhab.binding.caddx.internal;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that represents the Caddx Alarm Messages.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxMessage {
    private final Logger logger = LoggerFactory.getLogger(CaddxMessage.class);
    private final CaddxMessageType caddxMessageType;
    private final Map<String, String> propertyMap = new HashMap<>();
    private final Map<String, String> idMap = new HashMap<>();
    private final byte[] message;
    private final boolean hasAcknowledgementFlag;
    private final byte checksum1In;
    private final byte checksum2In;
    private final byte checksum1Calc;
    private final byte checksum2Calc;
    private CaddxMessageContext context;

    public CaddxMessage(CaddxMessageContext context, byte[] message, boolean withChecksum) {
        if (withChecksum && message.length < 3) {
            logger.debug("CaddxMessage: The message should be at least 3 bytes long.");
            throw new IllegalArgumentException("The message should be at least 3 bytes long");
        }
        if (!withChecksum && message.length < 1) {
            logger.debug("CaddxMessage: The message should be at least 1 byte long.");
            throw new IllegalArgumentException("The message should be at least 1 byte long");
        }

        // Received data
        this.context = context;
        byte[] msg = message;

        // Fill in the checksum
        if (withChecksum) {
            checksum1In = message[message.length - 2];
            checksum2In = message[message.length - 1];
            msg = Arrays.copyOf(message, message.length - 2);

            byte[] fletcherSum = fletcher(msg);
            checksum1Calc = fletcherSum[0];
            checksum2Calc = fletcherSum[1];
        } else {
            byte[] fletcherSum = fletcher(msg);
            checksum1Calc = fletcherSum[0];
            checksum2Calc = fletcherSum[1];

            checksum1In = checksum1Calc;
            checksum2In = checksum2Calc;
        }

        // Fill in the message
        this.message = msg;

        // Fill-in the acknowledgement flag
        if ((message[0] & 0x80) != 0) {
            hasAcknowledgementFlag = true;
            message[0] = (byte) (message[0] & 0x7f);
        } else {
            hasAcknowledgementFlag = false;
        }

        // Fill-in the message type
        CaddxMessageType mt = CaddxMessageType.valueOfMessageType(message[0]);
        if (mt == null) {
            throw new IllegalArgumentException("Unknown message");
        }
        caddxMessageType = mt;

        // Fill-in the properties
        processCaddxMessage();
    }

    public CaddxMessage(CaddxMessageContext context, CaddxMessageType type, String data) {
        int length = type.length;
        String[] tokens = data.split("\\,");
        if (length != 1 && tokens.length != length - 1) {
            logger.debug("token.length should be length-1. token.length={}, length={}", tokens.length, length);
            throw new IllegalArgumentException("CaddxMessage: data has not the correct format.");
        }

        this.context = context;
        byte[] msg = new byte[length];
        msg[0] = (byte) type.number;
        for (int i = 0; i < length - 1; i++) {
            msg[i + 1] = (byte) Integer.decode(tokens[i]).intValue();
        }

        // Fill-in the checksum
        byte[] fletcherSum = fletcher(msg);
        checksum1Calc = fletcherSum[0];
        checksum2Calc = fletcherSum[1];
        checksum1In = checksum1Calc;
        checksum2In = checksum2Calc;

        // Fill-in the message
        this.message = msg;

        // Fill-in the acknowledgement flag
        if ((message[0] & 0x80) != 0) {
            hasAcknowledgementFlag = true;
            message[0] = (byte) (message[0] & 0x7f);
        } else {
            hasAcknowledgementFlag = false;
        }

        // Fill-in the message type
        this.caddxMessageType = type;

        // Fill-in the properties
        processCaddxMessage();
    }

    public CaddxMessageContext getContext() {
        return context;
    }

    public void setContext(CaddxMessageContext context) {
        this.context = context;
    }

    public byte getChecksum1In() {
        return checksum1In;
    }

    public byte getChecksum2In() {
        return checksum2In;
    }

    public byte getChecksum1Calc() {
        return checksum1Calc;
    }

    public byte getChecksum2Calc() {
        return checksum2Calc;
    }

    public CaddxMessageType getCaddxMessageType() {
        return caddxMessageType;
    }

    public byte getMessageType() {
        return message[0];
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append(caddxMessageType.name);
        switch (caddxMessageType) {
            case ZONE_STATUS_REQUEST:
            case ZONE_STATUS_MESSAGE:
                String zone;
                try {
                    zone = "" + (Integer.parseInt(getPropertyById("zone_number")) + 1);
                } catch (NumberFormatException e) {
                    zone = "";
                }
                sb.append(" [Zone: ");
                sb.append(zone);
                sb.append("]");
                break;
            case LOG_EVENT_REQUEST:
            case LOG_EVENT_MESSAGE:
                sb.append(" [Event: ");
                sb.append(getPropertyById("panel_log_event_number"));
                sb.append("]");
                break;
            case PARTITION_STATUS_REQUEST:
            case PARTITION_STATUS_MESSAGE:
                String partition;
                try {
                    partition = "" + (Integer.parseInt(getPropertyById("partition_number")) + 1);
                } catch (NumberFormatException e) {
                    partition = "";
                }
                sb.append(" [Partition: ");
                sb.append(partition);
                sb.append("]");
                break;
            default:
                break;
        }
        return sb.toString();
    }

    public String getPropertyValue(String property) {
        if (!propertyMap.containsKey(property)) {
            logger.debug("Message does not contain property [{}]", property);
            return "";
        }
        return propertyMap.getOrDefault(property, "");
    }

    public String getPropertyById(String id) {
        if (!idMap.containsKey(id)) {
            logger.debug("Message does not contain id [{}]", id);
            return "";
        }
        return idMap.getOrDefault(id, "");
    }

    public int @Nullable [] getReplyMessageNumbers() {
        return caddxMessageType.replyMessageNumbers;
    }

    public CaddxSource getSource() {
        return getCaddxMessageType().source;
    }

    public boolean isChecksumCorrect() {
        return checksum1In == checksum1Calc && checksum2In == checksum2Calc;
    }

    public boolean isLengthCorrect() {
        return message.length == caddxMessageType.length;
    }

    public boolean hasAcknowledgementFlag() {
        return hasAcknowledgementFlag;
    }

    public byte[] getMessageFrameBytes(CaddxProtocol protocol) {
        if (protocol == CaddxProtocol.Binary) {
            return getMessageFrameBytesInBinary();
        } else {
            return getMessageFrameBytesInAscii();
        }
    }

    public byte[] getMessageBytes() {
        return message;
    }

    /**
     * Returns a string representation of a CaddxMessage.
     *
     * @return CaddxMessage string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        CaddxMessageType mt = CaddxMessageType.valueOfMessageType(message[0]);
        if (mt == null) {
            return "Unknown message type";
        }

        sb.append(String.format("Context: %s", context.toString()));
        sb.append(System.lineSeparator());

        sb.append("Message: ");
        sb.append(String.format("%2s", Integer.toHexString(message[0])));
        sb.append(" ");
        sb.append(mt.name);
        sb.append(System.lineSeparator());

        for (CaddxProperty p : mt.properties) {
            sb.append("\t").append(p.toString(message));
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

    private void putByteInBuffer(ByteBuffer frame, byte b) {
        if (b == 0x7e) {
            frame.put((byte) 0x7d);
            frame.put((byte) 0x5e);
        } else if (b == 0x7d) {
            frame.put((byte) 0x7d);
            frame.put((byte) 0x5d);
        } else {
            frame.put(b);
        }
    }

    private byte[] getByteBufferArray(ByteBuffer frame) {
        if (frame.hasArray()) {
            return frame.array();
        } else {
            byte[] byteArray = new byte[frame.capacity()];
            frame.position(0);
            frame.get(byteArray);
            return byteArray;
        }
    }

    private byte[] getMessageFrameBytesInBinary() {
        // Calculate bytes
        // 1 for the startbyte
        // 1 for the length
        // 2 for the checksum
        // n for the count of 0x7d and 0x7e occurrences in the message and checksum
        int additional = 4;
        for (int i = 0; i < message.length; i++) {
            if (message[i] == 0x7d || message[i] == 0x7e) {
                additional++;
            }
        }
        if (checksum1Calc == 0x7d || checksum1Calc == 0x7e) {
            additional++;
        }
        if (checksum2Calc == 0x7d || checksum2Calc == 0x7e) {
            additional++;
        }

        ByteBuffer frame = ByteBuffer.allocate(message.length + additional);

        // start character
        frame.put((byte) 0x7e);

        // message length
        frame.put((byte) message.length);

        // message
        for (int i = 0; i < message.length; i++) {
            putByteInBuffer(frame, message[i]);
        }

        // 1st checksum byte
        putByteInBuffer(frame, checksum1Calc);

        // 2nd checksum byte
        putByteInBuffer(frame, checksum2Calc);

        return getByteBufferArray(frame);
    }

    private byte[] getMessageFrameBytesInAscii() {
        // Calculate additional bytes
        // 1 for the start byte
        // 2 for the length
        // 4 for the checksum
        // 1 for the stop byte
        int additional = 8;

        ByteBuffer frame = ByteBuffer.allocate(2 * message.length + additional);

        // start character
        frame.put((byte) 0x0a);

        // message length
        frame.put(HexUtils.byteToHex((byte) message.length));

        // message
        for (int i = 0; i < message.length; i++) {
            frame.put(HexUtils.byteToHex(message[i]));
        }

        // Checksum 1st byte
        frame.put(HexUtils.byteToHex(checksum1Calc));

        // Checksum 2nd byte
        frame.put(HexUtils.byteToHex(checksum2Calc));

        // Stop character
        frame.put((byte) 0x0d);

        return getByteBufferArray(frame);
    }

    /**
     * Processes the incoming Caddx message and extracts the information.
     */
    private void processCaddxMessage() {
        // fill the property lookup hashmaps
        for (CaddxProperty p : caddxMessageType.properties) {
            propertyMap.put(p.getName(), p.getValue(message));
        }
        for (CaddxProperty p : caddxMessageType.properties) {
            if (!"".equals(p.getId())) {
                idMap.put(p.getId(), p.getValue(message));
            }
        }
    }

    /**
     * Calculates the Fletcher checksum of the byte array.
     *
     * @param data The input byte array
     * @return Byte array with two elements. Checksum1 and Checksum2
     */
    private byte[] fletcher(byte data[]) {
        int len = data.length;
        int sum1 = len, sum2 = len;
        for (int i = 0; i < len; i++) {
            int d = data[i] & 0xff;
            if (0xff - sum1 < d) {
                sum1 = (sum1 + 1) & 0xff;
            }
            sum1 = (sum1 + d) & 0xff;
            if (sum1 == 0xff) {
                sum1 = 0;
            }
            if (0xff - sum2 < sum1) {
                sum2 = (sum2 + 1) & 0xff;
            }
            sum2 = (sum2 + sum1) & 0xff;
            if (sum2 == 0xff) {
                sum2 = 0;
            }
        }

        return new byte[] { (byte) sum1, (byte) sum2 };
    }
}
