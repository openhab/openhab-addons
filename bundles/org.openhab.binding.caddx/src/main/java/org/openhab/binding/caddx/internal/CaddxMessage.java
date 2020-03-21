/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;

/**
 * A class that represents the Caddx Alarm Messages.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxMessage {
    private byte[] message;
    private boolean hasAcknowledgementFlag = false;
    private byte checksum1In;
    private byte checksum2In;
    private byte checksum1Calc;
    private byte checksum2Calc;
    private final CaddxMessageType caddxMessageType;
    private final Map<String, String> propertyMap = new HashMap<>();
    private final Map<String, String> idMap = new HashMap<>();

    /**
     * Constructor.
     *
     * @param message
     *            - the message received
     */
    public CaddxMessage(byte[] message, boolean withChecksum) {
        if (withChecksum && message.length < 3) {
            throw new IllegalArgumentException("The message should be at least 3 bytes long");
        }
        if (!withChecksum && message.length < 1) {
            throw new IllegalArgumentException("The message should be at least 1 byte long");
        }

        // Received data
        byte[] msg = message;
        if (withChecksum) {
            checksum1In = message[message.length - 2];
            checksum2In = message[message.length - 1];
            msg = Arrays.copyOf(message, message.length - 2);
        }

        // Calculate the checksum
        byte[] fletcherSum = fletcher(msg);
        checksum1Calc = fletcherSum[0];
        checksum2Calc = fletcherSum[1];
        // Make the In checksum same as the Calculated in case it is not supplied
        if (!withChecksum) {
            checksum1In = checksum1Calc;
            checksum2In = checksum2Calc;
        }

        this.message = msg;

        // fill the message type
        caddxMessageType = CaddxMessageType.valueOfMessageType((message[0] & 0x7f));

        // Fill-in the properties
        processCaddxMessage();
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

    /**
     * Builds a Caddx message for a zone bypass toggle command
     *
     * @param data The zone number
     * @return The Caddx message object
     */
    public static CaddxMessage buildZoneBypassToggle(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x3f;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a zone status request command
     *
     * @param data The zone number
     * @return The Caddx message object
     */
    public static CaddxMessage buildZoneStatusRequest(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x24;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a zone name request command
     *
     * @param data The zone number
     * @return The Caddx message object
     */
    public static CaddxMessage buildZoneNameRequest(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x23;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a partition status request command
     *
     * @param data The partition number
     * @return The Caddx message object
     */
    public static CaddxMessage buildPartitionStatusRequest(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x26;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a partition snapshot request command
     *
     * @param data The partition number
     * @return The Caddx message object
     */
    public static CaddxMessage buildPartitionSnapshotRequest(String data) {
        byte[] arr = new byte[1];
        arr[0] = 0x27;

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a partition primary command
     *
     * @param data Two values comma separated. The command, The partition number. e.g. "1,0"
     * @return The Caddx message object
     */
    public static CaddxMessage buildPartitionPrimaryCommand(String data) {
        String[] tokens = data.split(",");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("buildPartitionPrimaryCommand(): data has not the correct format.");
        }

        byte[] arr = new byte[3];
        arr[0] = 0x3e;
        arr[1] = (byte) Integer.parseInt(tokens[0]);
        arr[2] = (byte) (1 << Integer.parseInt(tokens[1]));

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a partition secondary command
     *
     * @param data Two values comma separated. The command, The partition number. e.g. "1,0"
     * @return The Caddx message object
     */
    public static CaddxMessage buildPartitionSecondaryCommand(String data) {
        String[] tokens = data.split(",");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("buildPartitionSecondaryCommand(): data has not the correct format.");
        }

        byte[] arr = new byte[3];
        arr[0] = 0x3e;
        arr[1] = (byte) Integer.parseInt(tokens[0]);
        arr[2] = (byte) (1 << Integer.parseInt(tokens[1]));

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a system status request command
     *
     * @param data Should be passed empty
     * @return The Caddx message object
     */
    public static CaddxMessage buildSystemStatusRequest(String data) {
        byte[] arr = new byte[1];
        arr[0] = 0x28;

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a interface configuration request command
     *
     * @param data Should be passed empty
     * @return The Caddx message object
     */
    public static CaddxMessage buildInterfaceConfigurationRequest(String data) {
        byte[] arr = new byte[1];
        arr[0] = 0x21;

        return new CaddxMessage(arr, false);
    }

    /**
     * Builds a Caddx message for a log event request command
     *
     * @param data Should be the number of the event
     * @return The Caddx message object
     */
    public static CaddxMessage buildLogEventRequest(String data) {
        byte[] arr = new byte[2];
        arr[0] = 0x2a;
        arr[1] = (byte) Integer.parseInt(data);

        return new CaddxMessage(arr, false);
    }

    /**
     * Returns the Caddx Message Type.
     *
     * @return messageType
     */
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
            case Zone_Status_Request:
            case Zone_Status_Message:
                sb.append(" [Zone: ");
                sb.append(getPropertyById("zone_number"));
                sb.append("]");
                break;
            case Log_Event_Request:
            case Log_Event_Message:
                sb.append(" [Event: ");
                sb.append(getPropertyById("panel_log_event_number"));
                sb.append("]");
                break;
            case Partition_Status_Request:
            case Partition_Status_Message:
                sb.append(" [Partition: ");
                sb.append(getPropertyById("partition_number"));
                sb.append("]");
                break;
            default:
                break;
        }
        return sb.toString();
    }

    public String getPropertyValue(String property) {
        return propertyMap.get(property);
    }

    public String getPropertyById(String id) {
        return idMap.get(id);
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

        byte[] frame = new byte[message.length + additional];
        frame[0] = 0x7e;
        frame[1] = (byte) message.length;

        int fi = 2;
        for (int i = 0; i < message.length; i++) {
            byte b = message[i];
            if (b == 0x7e) {
                frame[fi++] = 0x7d;
                b = 0x5e;
            } else if (b == 0x7d) {
                frame[fi++] = 0x7d;
                b = 0x5d;
            }
            frame[fi++] = b;
        }

        // 1st checksum byte
        if (checksum1Calc == 0x7e) {
            frame[fi++] = 0x7d;
            frame[fi++] = 0x5e;
        } else if (checksum1Calc == 0x7d) {
            frame[fi++] = 0x7d;
            frame[fi++] = 0x5d;
        } else {
            frame[fi++] = checksum1Calc;
        }
        // 2nd checksum byte
        if (checksum2Calc == 0x7e) {
            frame[fi++] = 0x7d;
            frame[fi++] = 0x5e;
        } else if (checksum2Calc == 0x7d) {
            frame[fi++] = 0x7d;
            frame[fi++] = 0x5d;
        } else {
            frame[fi++] = checksum2Calc;
        }

        return frame;
    }

    private byte[] getMessageFrameBytesInAscii() {
        // Calculate additional bytes
        // 1 for the start byte
        // 2 for the length
        // 4 for the checksum
        // 1 for the stop byte
        int additional = 8;

        int fi = 0;
        byte[] frame = new byte[2 * message.length + additional];

        // start character
        frame[fi++] = 0x0a;

        // message length
        byte[] tempArray = HexUtils.byteToHex((byte) message.length);
        frame[fi++] = tempArray[0];
        frame[fi++] = tempArray[1];

        // message
        for (int i = 0; i < message.length; i++) {
            byte b = message[i];
            tempArray = HexUtils.byteToHex(b);
            frame[fi++] = tempArray[0];
            frame[fi++] = tempArray[1];
        }

        // Checksum 1st byte
        tempArray = HexUtils.byteToHex(checksum1Calc);
        frame[fi++] = tempArray[0];
        frame[fi++] = tempArray[1];

        // Checksum 2nd byte
        tempArray = HexUtils.byteToHex(checksum2Calc);
        frame[fi++] = tempArray[0];
        frame[fi++] = tempArray[1];

        // Stop character
        frame[fi++] = (byte) 0x0d;

        return frame;
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

        sb.append("Message: ");
        sb.append(String.format("%2s", Integer.toHexString(message[0])));
        sb.append(" ");
        sb.append(mt.name);
        sb.append("\r\n");

        for (CaddxProperty p : mt.properties) {
            sb.append("\t" + p.toString(message));
            sb.append("\r\n");
        }

        return sb.toString();
    }

    /**
     * Processes the incoming Caddx message and extracts the information.
     */
    private void processCaddxMessage() {
        if ((message[0] & 0x80) != 0) {
            hasAcknowledgementFlag = true;
            message[0] = (byte) (message[0] & 0x7f);
        }

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
