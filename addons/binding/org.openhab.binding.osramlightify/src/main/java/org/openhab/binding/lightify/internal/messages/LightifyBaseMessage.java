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

import org.openhab.binding.osramlightify.internal.util.IEEEAddress;

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

        private final byte packetType;

        PacketType(int packetType) {
            this.packetType = (byte) packetType;
        }

        public byte toByte() {
            return packetType;
        }
    }

    public enum Command {
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

        private final byte command;

        Command(int command) {
            this.command = (byte) command;
        }

        public byte toByte() {
            return command;
        }
    }

    private static String describeStatusCode(byte statusCode) {
        String description;

        switch ((int) statusCode & 0xff) {
            case 0x00:
                description = "OK";
                break;

            case 0x01:
                description = "INCORRECT_PARAMETERS";
                break;

            case 0x15:
                description = "WRONG_TYPE";
                // Group address but not a GROUPCAST packet type
                // or GROUPCAST packet type for a UNICAST only command
                break;

            case 0x16:
                description = "RESYNC_REQUIRED";
                // Next command MUST be a LIST_PAIRED_DEVICES. This appears to be a form
                // of resync procedure and 0x16 will repeat until a LIST_PAIRED_DEVICES
                // is sent.
                break;

            case 0xFF:
                description = "UNKNOWN_COMMAND";
                // ??? followed by RESYNC_REQUIRED in response to everything?
                break;

            default:
                description = "UNKNOWN";
                break;
        }

        return String.format("%02X (%s)", statusCode, description);
    }

    private boolean haveResponse = false;

    private PacketType packetType;
    private Command command;
    private int seqNo;
    private byte statusCode;
    private IEEEAddress deviceAddress;

    public LightifyBaseMessage(LightifyDeviceHandler deviceHandler, Command command) {
        if (deviceHandler != null) {
            deviceAddress = deviceHandler.getDeviceAddress();
        }

        if (deviceAddress != null && deviceAddress.isUnicast()) {
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
            + (statusCode != 0 ? ", Status = " + describeStatusCode(statusCode) : "")
            + (deviceAddress == null ? "" : ", Address = " + deviceAddress);
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
        int messageLength = HEADER_LENGTH + (deviceAddress != null ? deviceAddress.ADDRESS_LENGTH : 0) + length;
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
        if (deviceAddress != null) {
            encodedMessage.put(deviceAddress.array());
        }

        return encodedMessage;
    }

    protected ByteBuffer encodeMessage(ByteBuffer message) throws LightifyMessageTooLongException {
        message.putInt(4, seqNo);
        return message;
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
        data.get(); // packet type - same as we sent
        data.get(); // command - sames as we sent
        seqNo = data.getInt();

        // When decoding messages we are interpreting a wire-format response so
        // there is a status code but no address (at least not as part of the header).
        statusCode = data.get();

        if (statusCode != 0) {
            throw new LightifyException("status = " + describeStatusCode(statusCode) + " response to " + this);
        }

        haveResponse = true;
        return true;
    }
}
