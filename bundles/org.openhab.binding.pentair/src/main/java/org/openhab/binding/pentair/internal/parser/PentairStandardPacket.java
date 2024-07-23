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
package org.openhab.binding.pentair.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * { @link PentairStandardPacket } class implements the pentair standard packet format. Most commands sent over the bus
 * utilize this format (with the exception of the Intellichlor packets).
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairStandardPacket extends PentairBasePacket {
    public static final int A5 = 0;
    public static final int PREAMBLE = 1;
    public static final int DEST = 2;
    public static final int SOURCE = 3;
    public static final int ACTION = 4;
    public static final int LENGTH = 5;
    public static final int STARTOFDATA = 6;

    /**
     * Constructor for an empty packet. Typically used when generating a packet to
     * send. Should include all bytes starting with A5, but not including the checksum
     */
    public PentairStandardPacket() {
        super(6);

        buf[0] = (byte) 0xA5;
    }

    public PentairStandardPacket(byte[] array, int limit) {
        super(array, limit);
    }

    /*
     * Constructor to create packet from this p
     */
    public PentairStandardPacket(byte[] packet) {
        super(packet);
    }

    /**
     * Gets length of packet
     *
     * @return length of packet
     */
    public int getPacketLengthHeader() {
        return (buf[LENGTH] & 0xFF);
    }

    /**
     * Sets length of packet
     *
     * @param length length of packet
     */
    public void setPacketLengthHeader(int length) {
        if (length > (buf[LENGTH] & 0xFF)) {
            buf = new byte[length + 6];
        }
        buf[LENGTH] = (byte) length;
    }

    public int getSource() {
        return buf[SOURCE];
    }

    public int getDest() {
        return buf[DEST];
    }

    public int getAction() {
        return buf[ACTION];
    }

    /**
     * Calculate checksum of the representative packet.
     *
     * @return checksum of packet
     */
    public int calcChecksum() {
        int checksum = 0, i;

        for (i = 0; i < getPacketLengthHeader() + 6; i++) {
            checksum += buf[i] & 0xFF;
        }

        return checksum;
    }

    /**
     * Helper function to prepare the packet (including pre-amble and checksum) before being sent
     *
     * @return
     */
    public byte[] wrapPacketToSend() {
        int checksum;

        byte[] preamble = { (byte) 0xFF, (byte) 0x00, (byte) 0xFF };
        byte[] writebuf;

        writebuf = new byte[preamble.length + buf.length + 2];

        System.arraycopy(preamble, 0, writebuf, 0, preamble.length);
        System.arraycopy(this.buf, 0, writebuf, preamble.length, buf.length);

        checksum = calcChecksum();

        writebuf[writebuf.length - 2] = (byte) ((checksum >> 8) & 0xFF);
        writebuf[writebuf.length - 1] = (byte) (checksum & 0xFF);

        return writebuf;
    }
}
