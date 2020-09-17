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
package org.openhab.binding.dreamscreen.internal.message;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.types.RefreshType;

/**
 * The {@link DreamScreenMessage} implements general message handling.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public abstract class DreamScreenMessage {
    private final byte group;
    private final byte commandUpper;
    private final byte commandLower;
    protected final ByteBuffer payload;
    protected final int payloadLen;

    public static DreamScreenMessage fromPacket(final DatagramPacket packet) throws DreamScreenMessageInvalid {
        final int len = packet.getLength();
        final byte[] data = packet.getData();
        final int msgLen;
        if (len > 6 && data[0] == (byte) 0xFC) {
            msgLen = data[1] & 0xFF;
            if (msgLen + 2 > len) {
                throw new DreamScreenMessageInvalid("Invalid length");
            } else if (data[msgLen + 1] != calcCRC8(data)) {
                throw new DreamScreenMessageInvalid("Invalid CRC");
            }
        } else {
            throw new DreamScreenMessageInvalid("Message not long enough");
        }

        if (SerialNumberMessage.matches(data)) {
            return new SerialNumberMessage(data);
        } else if (RefreshTvMessage.matches(data)) {
            return new RefreshTvMessage(data);
        } else if (RefreshMessage.matches(data)) {
            return new RefreshMessage(data);
        } else if (ModeMessage.matches(data)) {
            return new ModeMessage(data);
        } else if (InputMessage.matches(data)) {
            return new InputMessage(data);
        } else if (ColorMessage.matches(data)) {
            return new ColorMessage(data);
        } else if (AmbientModeTypeMessage.matches(data)) {
            return new AmbientModeTypeMessage(data);
        } else if (SceneMessage.matches(data)) {
            return new SceneMessage(data);
        }
        throw new DreamScreenMessageInvalid("Message not currently handled");
    }

    protected static boolean matches(byte[] data, byte commandUpper, byte commandLower) {
        return data[4] == commandUpper && data[5] == commandLower;
    }

    protected DreamScreenMessage(final byte[] data) {
        this.group = data[2];
        // this.flags = data[off + 3];
        this.commandUpper = data[4];
        this.commandLower = data[5];
        this.deviceType = data[data.length - 1];
        this.payloadLen = (data[1] & 0xFF) - 5;
        this.payload = ByteBuffer.allocate(payloadLen);
        this.payload.put(data, 6, payloadLen);
        this.payload.rewind();
    }

    protected DreamScreenMessage(byte group, byte commandUpper, byte commandLower, byte[] payload) {
        this.group = group;
        // this.flags = flags;
        this.commandUpper = commandUpper;
        this.commandLower = commandLower;
        this.payloadLen = payload.length;
        this.payload = ByteBuffer.wrap(payload);
    }

    public DatagramPacket readPacket(final InetAddress address, final int port) {
        return toPacket(address, port, (byte) 0x16);
    }

    public DatagramPacket writePacket(final InetAddress address, final int port) {
        return toPacket(address, port, (byte) 0x17);
    }

    public DatagramPacket broadcastReadPacket(final InetAddress address, final int port) {
        return toPacket(address, port, (byte) 0x30);
    }

    private DatagramPacket toPacket(final InetAddress address, final int port, final byte flags) {
        byte[] data = new byte[this.payloadLen + 7];
        data[0] = (byte) 0xFC;
        data[1] = (byte) (this.payloadLen + 5);
        data[2] = this.group;
        data[3] = flags;
        data[4] = this.commandUpper;
        data[5] = this.commandLower;
        this.payload.get(data, 6, this.payloadLen);
        data[this.payloadLen + 6] = calcCRC8(data);

        return new DatagramPacket(data, this.payloadLen + 7, address, port);
    }

    private static final byte calcCRC8(byte[] data) {
        int size = (data[1] & 0xFF) + 1;
        int cntr = 0;
        byte crc = 0x00;
        while (cntr < size) {
            crc = CRC_TABLE[(byte) (crc ^ (data[cntr])) & 0xFF];
            cntr++;
        }
        return crc;
    }

    private final static byte[] CRC_TABLE = new byte[] { 0x00, 0x07, 0x0E, 0x09, 0x1C, 0x1B, 0x12, 0x15, 0x38, 0x3F,
            0x36, 0x31, 0x24, 0x23, 0x2A, 0x2D, 0x70, 0x77, 0x7E, 0x79, 0x6C, 0x6B, 0x62, 0x65, 0x48, 0x4F, 0x46, 0x41,
            0x54, 0x53, 0x5A, 0x5D, (byte) 0xE0, (byte) 0xE7, (byte) 0xEE, (byte) 0xE9, (byte) 0xFC, (byte) 0xFB,
            (byte) 0xF2, (byte) 0xF5, (byte) 0xD8, (byte) 0xDF, (byte) 0xD6, (byte) 0xD1, (byte) 0xC4, (byte) 0xC3,
            (byte) 0xCA, (byte) 0xCD, (byte) 0x90, (byte) 0x97, (byte) 0x9E, (byte) 0x99, (byte) 0x8C, (byte) 0x8B,
            (byte) 0x82, (byte) 0x85, (byte) 0xA8, (byte) 0xAF, (byte) 0xA6, (byte) 0xA1, (byte) 0xB4, (byte) 0xB3,
            (byte) 0xBA, (byte) 0xBD, (byte) 0xC7, (byte) 0xC0, (byte) 0xC9, (byte) 0xCE, (byte) 0xDB, (byte) 0xDC,
            (byte) 0xD5, (byte) 0xD2, (byte) 0xFF, (byte) 0xF8, (byte) 0xF1, (byte) 0xF6, (byte) 0xE3, (byte) 0xE4,
            (byte) 0xED, (byte) 0xEA, (byte) 0xB7, (byte) 0xB0, (byte) 0xB9, (byte) 0xBE, (byte) 0xAB, (byte) 0xAC,
            (byte) 0xA5, (byte) 0xA2, (byte) 0x8F, (byte) 0x88, (byte) 0x81, (byte) 0x86, (byte) 0x93, (byte) 0x94,
            (byte) 0x9D, (byte) 0x9A, 0x27, 0x20, 0x29, 0x2E, 0x3B, 0x3C, 0x35, 0x32, 0x1F, 0x18, 0x11, 0x16, 0x03,
            0x04, 0x0D, 0x0A, 0x57, 0x50, 0x59, 0x5E, 0x4B, 0x4C, 0x45, 0x42, 0x6F, 0x68, 0x61, 0x66, 0x73, 0x74, 0x7D,
            0x7A, (byte) 0x89, (byte) 0x8E, (byte) 0x87, (byte) 0x80, (byte) 0x95, (byte) 0x92, (byte) 0x9B,
            (byte) 0x9C, (byte) 0xB1, (byte) 0xB6, (byte) 0xBF, (byte) 0xB8, (byte) 0xAD, (byte) 0xAA, (byte) 0xA3,
            (byte) 0xA4, (byte) 0xF9, (byte) 0xFE, (byte) 0xF7, (byte) 0xF0, (byte) 0xE5, (byte) 0xE2, (byte) 0xEB,
            (byte) 0xEC, (byte) 0xC1, (byte) 0xC6, (byte) 0xCF, (byte) 0xC8, (byte) 0xDD, (byte) 0xDA, (byte) 0xD3,
            (byte) 0xD4, 0x69, 0x6E, 0x67, 0x60, 0x75, 0x72, 0x7B, 0x7C, 0x51, 0x56, 0x5F, 0x58, 0x4D, 0x4A, 0x43, 0x44,
            0x19, 0x1E, 0x17, 0x10, 0x05, 0x02, 0x0B, 0x0C, 0x21, 0x26, 0x2F, 0x28, 0x3D, 0x3A, 0x33, 0x34, 0x4E, 0x49,
            0x40, 0x47, 0x52, 0x55, 0x5C, 0x5B, 0x76, 0x71, 0x78, 0x7F, 0x6A, 0x6D, 0x64, 0x63, 0x3E, 0x39, 0x30, 0x37,
            0x22, 0x25, 0x2C, 0x2B, 0x06, 0x01, 0x08, 0x0F, 0x1A, 0x1D, 0x14, 0x13, (byte) 0xAE, (byte) 0xA9,
            (byte) 0xA0, (byte) 0xA7, (byte) 0xB2, (byte) 0xB5, (byte) 0xBC, (byte) 0xBB, (byte) 0x96, (byte) 0x91,
            (byte) 0x98, (byte) 0x9F, (byte) 0x8A, (byte) 0x8D, (byte) 0x84, (byte) 0x83, (byte) 0xDE, (byte) 0xD9,
            (byte) 0xD0, (byte) 0xD7, (byte) 0xC2, (byte) 0xC5, (byte) 0xCC, (byte) 0xCB, (byte) 0xE6, (byte) 0xE1,
            (byte) 0xE8, (byte) 0xEF, (byte) 0xFA, (byte) 0xFD, (byte) 0xF4, (byte) 0xF3 };
}
