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
package org.openhab.binding.velbus.internal.packets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusPacket} represents a base class for a Velbus packet and contains
 * functionality that is applicable to all Velbus packets.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public abstract class VelbusPacket {
    public static final byte STX = 0x0F;
    public static final byte PRIO_HI = (byte) 0xF8;
    public static final byte PRIO_LOW = (byte) 0xFB;
    public static final byte ETX = 0x04;

    private byte address;
    private byte priority;
    private byte rtr;

    protected VelbusPacket(byte address, byte priority) {
        this(address, priority, false);
    }

    protected VelbusPacket(byte address, byte priority, boolean rtr) {
        this.address = address;
        this.priority = priority;
        this.rtr = rtr ? (byte) 0x40 : (byte) 0x00;
    }

    public byte[] getBytes() {
        byte[] dataBytes = getDataBytes();
        int dataBytesLength = dataBytes.length;
        byte[] packetBytes = new byte[6 + dataBytesLength];

        packetBytes[0] = STX;
        packetBytes[1] = priority;
        packetBytes[2] = address;
        packetBytes[3] = (byte) (rtr | dataBytesLength);

        System.arraycopy(dataBytes, 0, packetBytes, 4, dataBytes.length);

        packetBytes[4 + dataBytesLength] = computeCRCByte(packetBytes);
        packetBytes[5 + dataBytesLength] = ETX;

        return packetBytes;
    }

    protected abstract byte[] getDataBytes();

    public static byte computeCRCByte(byte[] packetBytes) {
        int crc = 0;

        for (int i = 0; i < packetBytes.length - 2; i++) {
            crc = (crc + (packetBytes[i] & 0xFF)) & 0xFF;
        }

        return (byte) (0x100 - crc);
    }
}
