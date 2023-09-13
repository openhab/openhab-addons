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
package org.openhab.binding.onewire.internal.owserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.OwPageBuffer;

/**
 * The {@link OwserverPacket} class provides a single packet for communication with the owserver
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class OwserverPacket {
    public static final int PROTOCOL_VERSION = 0;

    // 6x4 bytes
    public static final int HEADER_SIZE = 24;
    protected int payloadLength = 0;

    protected final OwserverPacketType packetType;

    protected int protocolVersion = PROTOCOL_VERSION;
    protected int controlFlags;
    protected int packetCode = 0;
    protected int packetSize = 0;
    protected int payloadOffset = 0;

    protected byte[] payload = new byte[0];

    /**
     * constructor for new packet
     *
     * @param packetType packetType;
     */
    public OwserverPacket(OwserverPacketType packetType) {
        this.packetType = packetType;
        setControlFlags(OwserverControlFlag.OWNET, OwserverControlFlag.DEVICE_DISPLAY);
    }

    /**
     * constructor for reading packet from stream
     *
     * @param owInputStream input stream to read from
     * @throws IOException
     * @throws OwException in case an error occurs
     */
    public OwserverPacket(DataInputStream owInputStream, OwserverPacketType packetType)
            throws IOException, OwException {
        this.packetType = packetType;

        // header
        protocolVersion = owInputStream.readInt();
        payloadLength = owInputStream.readInt();
        packetCode = owInputStream.readInt();
        controlFlags = owInputStream.readInt();
        packetSize = owInputStream.readInt();
        payloadOffset = owInputStream.readInt();

        // payload
        if (payloadLength != -1) {
            if ((protocolVersion != PROTOCOL_VERSION) || !OwserverControlFlag.OWNET.isSet(controlFlags)) {
                throw new OwException("invalid data read");
            }
            if (payloadLength > 0) {
                payload = new byte[payloadLength];
                owInputStream.readFully(payload, 0, payloadLength);
            }
        }
    }

    /**
     * constructor for a new request message
     *
     * @param owMessageType
     * @param path
     * @param owControlFlags
     */
    public OwserverPacket(OwserverMessageType owMessageType, String path, OwserverControlFlag... owControlFlags) {
        this(OwserverPacketType.REQUEST);
        packetCode = owMessageType.getValue();
        setPayload(path);
        setTemperatureScale(OwserverTemperatureScale.CENTIGRADE);
        setControlFlags(owControlFlags);
        if (owMessageType == OwserverMessageType.WRITE) {
            packetSize = 0x00000000;
        } else {
            packetSize = 0x00010000;
        }
    }

    /**
     * set one or more control flags for this packet
     *
     * @param flags one or more flags as OwControlFlag
     */
    public void setControlFlags(OwserverControlFlag... flags) {
        for (int i = 0; i < flags.length; i++) {
            controlFlags |= flags[i].getValue();
        }
    }

    /**
     * check if a certain flag is set in this packet
     *
     * @param flag flag to be tested
     * @return true if flag is set
     */
    public boolean hasControlFlag(OwserverControlFlag flag) {
        return flag.isSet(controlFlags);
    }

    /**
     * set this packet's pressure scale
     *
     * @param pressureScale
     */
    public void setPressureScale(OwserverPressureScale pressureScale) {
        controlFlags = pressureScale.setFlag(controlFlags);
    }

    /**
     * get this packets pressure scale
     *
     * @return
     */
    public OwserverPressureScale getPressureScale() {
        return OwserverPressureScale.getFlag(controlFlags);
    }

    /**
     * set this packet's temperature scale
     *
     * @param temperatureScale
     */
    public void setTemperatureScale(OwserverTemperatureScale temperatureScale) {
        controlFlags = temperatureScale.setFlag(controlFlags);
    }

    /**
     * get this packets temperature scale
     *
     * @return
     */
    public OwserverTemperatureScale getTemperatureScale() {
        return OwserverTemperatureScale.getFlag(controlFlags);
    }

    /**
     * set (or replace) this packet's payload from a string
     *
     * @param payload string representation of the payload
     */
    public void setPayload(String payload) {
        byte[] bytes = payload.getBytes();
        payloadLength = bytes.length + 1;
        this.payload = new byte[payloadLength];
        System.arraycopy(bytes, 0, this.payload, 0, bytes.length);
    }

    /**
     * append to this packet's payload from a string
     *
     * @param payload string representation of the payload to append
     */
    public void appendPayload(String payload) {
        byte[] appendBytes = payload.getBytes();

        byte[] fullPayload = new byte[this.payload.length + appendBytes.length];
        System.arraycopy(this.payload, 0, fullPayload, 0, this.payload.length);
        System.arraycopy(appendBytes, 0, fullPayload, this.payload.length, appendBytes.length);

        this.packetSize += appendBytes.length;
        this.payloadLength = fullPayload.length;
        this.payload = fullPayload;
    }

    /**
     * set this packet payload from a OwPageBuffer
     *
     * @param payload string representation of the payload
     */
    public void setPayload(OwPageBuffer payload) {
        byte[] bytes = payload.getBytes();
        payloadLength = bytes.length + 1;
        this.payload = new byte[payloadLength];
        System.arraycopy(bytes, 0, this.payload, 0, bytes.length);
    }

    /**
     * get the payload of this packet
     *
     * @return string representation of this packet's payload
     */
    public String getPayloadString() {
        if (payloadLength > 0) {
            // already null terminated strings skip the termination character
            if (payload[payloadLength - 1] == 0) {
                return new String(payload, 0, payloadLength - 1);
            } else {
                return new String(payload, 0, payloadLength);
            }
        } else {
            return "";
        }
    }

    /**
     * set this packet's return code (0 is ok)
     *
     * @param returnCode an integer
     */
    public void setReturnCode(int returnCode) {
        if (packetType == OwserverPacketType.RETURN) {
            this.packetCode = returnCode;
        } else {
            throw new IllegalStateException("setting return code not allowed in REQUEST packets");
        }
    }

    /**
     * get this packet's return code (0 is ok)
     *
     * @return
     */
    public int getReturnCode() {
        if (packetType == OwserverPacketType.RETURN) {
            return packetCode;
        } else {
            throw new IllegalStateException("getting return code not allowed in REQUEST packets");
        }
    }

    /**
     * set this packet's message type
     *
     * @param messageType
     */
    public void setMessageType(OwserverMessageType messageType) {
        if (packetType == OwserverPacketType.REQUEST) {
            packetCode = messageType.getValue();
        } else {
            throw new IllegalStateException("setting message type not allowed in RETURN packets");
        }
    }

    /**
     * get this packets message type
     *
     * @return
     */
    public OwserverMessageType getMessageType() {
        if (packetType == OwserverPacketType.REQUEST) {
            return OwserverMessageType.fromInt(packetCode);
        } else {
            throw new IllegalStateException("getting message type not allowed in RETURN packets");
        }
    }

    /**
     * check if packed is valid return packet
     *
     * @return true if valid
     */
    public boolean isValidReturnPacket() {
        return (packetCode == 0 && packetType == OwserverPacketType.RETURN);
    }

    /**
     * check if packed is valid return packet
     *
     * @return true if valid
     */
    public boolean isPingPacket() {
        return (payloadLength == -1 && packetType == OwserverPacketType.RETURN);
    }

    /**
     * get the payload of this packet
     *
     * @return OwPageBuffer with this packet's payload
     */
    public OwPageBuffer getPayload() {
        return new OwPageBuffer(payload);
    }

    /**
     * check if this packet has a payload
     *
     * @return true if payload present
     */
    public boolean hasPayload() {
        return (payloadLength > 0);
    }

    /**
     * convert this packet to an array of bytes
     *
     * @return array of bytes
     */
    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_SIZE + payloadLength);
        byteBuffer.putInt(protocolVersion);
        byteBuffer.putInt(payloadLength);
        byteBuffer.putInt(packetCode);
        byteBuffer.putInt(controlFlags);
        byteBuffer.putInt(packetSize);
        byteBuffer.putInt(payloadOffset);
        if (payloadLength > 0) {
            byteBuffer.put(payload);
        }

        return byteBuffer.array();
    }

    @Override
    public String toString() {
        String prefix;

        if (packetType == OwserverPacketType.RETURN) {
            prefix = String.format("return code %d", packetCode);
        } else {
            prefix = String.format("messageType %s", OwserverMessageType.fromInt(packetCode));
        }

        return String.format("%s, size %d, controlFlags 0x%08x, payload '%s'", prefix, HEADER_SIZE + payloadLength,
                controlFlags, getPayloadString().replaceAll("\\p{C}", "?"));
    }
}
