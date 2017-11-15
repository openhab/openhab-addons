/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyUnsupportedValueException;

/**
 * Base class for Lightify messages classes.
 *
 * All other message classes should extend this class.
 *
 * @author Mike Jagdis - Initial contribution
 */
public abstract class LightifyBaseMessage {

    public static final short HEADER_LENGTH = 6;
    public static final short ADDRESS_LENGTH = 8;
    public static final short NAME_LENGTH = 16;
    public static final short GROUP_ID_LENGTH = 2;

    public enum PacketType {
        UNICAST(0x00),
        UNICAST_RESPONSE(0x01),
        GROUPCAST(0x02),
        GROUPCAST_RESPONSE(0x03);

        private final int packetType;

        PacketType(int packetType) {
            this.packetType = packetType;
        }

        public byte toByte() {
            return (byte) packetType;
        }

        public static PacketType fromByte(int input) throws LightifyUnsupportedValueException {
            input &= 0xff;
            for (PacketType packetType : PacketType.values()) {
                if (packetType.packetType == input) {
                    return packetType;
                }
            }

            throw new LightifyUnsupportedValueException(PacketType.class, input);
        }

    }

    public enum Command {
        GATEWAY_RESET(0x00), // Blind guess, no data yet...
        LIST_PAIRED_DEVICES(0x13),
        LIST_GROUPS(0x1E),
        GET_GROUP_INFO(0x26),
        SET_LUMINANCE(0x31),
        SET_SWITCH(0x32),
        SET_TEMPERATURE(0x33),
        SET_COLOR(0x36),
        ACTIVATE_SCENE(0x52),
        GET_DEVICE_INFO(0x68),
        GET_GATEWAY_FIRMWARE_VERSION(0x6F),
        GET_WIFI_VERSION(0xE3);

        private final int command;

        Command(int command) {
            this.command = command;
        }

        public byte toByte() {
            return (byte) command;
        }

        public static Command fromByte(int input) throws LightifyUnsupportedValueException {
            input &= 0xff;
            for (Command command : Command.values()) {
                if (command.command == input) {
                    return command;
                }
            }

            throw new LightifyUnsupportedValueException(Command.class, input);
        }

    }

    public enum StatusCode {
        OK(0x00),
        INCORRECT_PARAMETERS(0x01),
        WRONG_TYPE(0x15),           // Group address but not a GROUPCAST packet type
                                    // or GROUPCAST packet type for a UNICAST only command
        RESYNC_REQUIRED(0x16),      // Next command MUST be a LIST_PAIRED_DEVICES. This appears to be a form
                                    // of resync procedure and 0x16 will repeat until a LIST_PAIRED_DEVICES
                                    // is sent.
        UNKNOWN_COMMAND(0xFF);      // ??? followed by RESYNC_REQUIRED in response to everything?

        private final int statusCode;

        StatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public byte toByte() {
            return (byte) statusCode;
        }

        public static StatusCode fromByte(int input) throws LightifyUnsupportedValueException {
            input &= 0xff;
            for (StatusCode statusCode : StatusCode.values()) {
                if (statusCode.statusCode == input) {
                    return statusCode;
                }
            }

            throw new LightifyUnsupportedValueException(StatusCode.class, input);
        }
    }

    private boolean haveResponse = false;

    private PacketType packetType;
    private Command command;
    private int seqNo;
    private StatusCode statusCode;
    private byte[] addressBytes;

    public LightifyBaseMessage(LightifyDeviceHandler deviceHandler, Command command) {
        if (deviceHandler != null) {
            byte[] bytes = DatatypeConverter.parseHexBinary(deviceHandler.getDeviceAddress().replaceAll(":", ""));
            addressBytes = new byte[] { bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0] };
        }

        if (addressBytes != null
        && (addressBytes[2] != 0 || addressBytes[3] != 0 || addressBytes[4] != 0
        || addressBytes[5] != 0 || addressBytes[6] != 0 || addressBytes[7] != 0)) {
            packetType = PacketType.UNICAST;
        } else {
            packetType = PacketType.GROUPCAST;
        }

        this.command = command;
    }

    @Override
    public String toString() {
        return
            "Packet type = " + packetType
            + ", Command = " + command
            + ", Seq number = " + seqNo
            + (statusCode != null ? ", Status = " + statusCode : "")
            + (addressBytes == null ? "" : ", Address = "
                + String.format("%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X",
                    (addressBytes[7] & 0xff), (addressBytes[6] & 0xff), (addressBytes[5] & 0xff), (addressBytes[4] & 0xff),
                    (addressBytes[3] & 0xff), (addressBytes[2] & 0xff), (addressBytes[1] & 0xff), (addressBytes[0] & 0xff)));
    }

    public boolean isPoller() {
        return false;
    }

    public boolean isResponse() {
        return haveResponse;
    }

    // ****************************************
    //      Request transmission section
    // ****************************************

    protected ByteBuffer encodeMessage(int length) throws LightifyMessageTooLongException {
        int messageLength = HEADER_LENGTH + (addressBytes != null ? addressBytes.length : 0) + length;
        if ((messageLength & ~0xffff) != 0) {
            throw new LightifyMessageTooLongException("Requested message length of " + messageLength + " must be less than 65535");
        }

        haveResponse = false;

        ByteBuffer encodedMessage = ByteBuffer.allocate(Short.BYTES + (messageLength & 0xffff))
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort((short) messageLength)
            .put(packetType.toByte())
            .put(command.toByte())
            .putInt(seqNo);

        // When encoding messages we are creating wire-format commands so no
        // status code but there might be an address.
        if (addressBytes != null) {
            encodedMessage.put(addressBytes);
        }

        return encodedMessage;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }


    // ****************************************
    //        Response handling section
    // ****************************************

    public int getSeqNo() {
        return seqNo;
    }

    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {

        data.rewind();

        data.getShort(); // message length
        PacketType packetType = PacketType.fromByte(data.get());
        Command command = Command.fromByte(data.get());
        seqNo = data.getInt();

        // When decoding messages we are interpreting a wire-format response so
        // there is a status code but no address (at least not as part of the header).
        statusCode = StatusCode.fromByte(data.get());

        if (statusCode != StatusCode.OK) {
            throw new LightifyException("status = " + statusCode + " response to " + this);
        }

        haveResponse = true;
        return true;
    }

    public String decodeDeviceAddress(ByteBuffer data) {
        byte[] bytes = new byte[ADDRESS_LENGTH];
        data.get(bytes);
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X",
            (bytes[7] & 0xff), (bytes[6] & 0xff), (bytes[5] & 0xff), (bytes[4] & 0xff),
            (bytes[3] & 0xff), (bytes[2] & 0xff), (bytes[1] & 0xff), (bytes[0] & 0xff));
    }

    public String makeGroupAddress(short id) {
        return String.format("00:00:00:00:00:00:%02X:%02X",
            ((id >> 8) & 0xff), (id & 0xff));
    }

    public String decodeHex(ByteBuffer data, int length) {
        byte[] bytes = new byte[length];
        data.get(bytes);
        return DatatypeConverter.printHexBinary(bytes);
    }

    public String decodeName(ByteBuffer data) {
        byte[] bytes = new byte[NAME_LENGTH];
        data.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }
}
