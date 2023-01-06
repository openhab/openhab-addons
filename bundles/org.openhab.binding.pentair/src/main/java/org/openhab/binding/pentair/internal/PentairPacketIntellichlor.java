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
package org.openhab.binding.pentair.internal;

/**
 * Pentair Intellichlor specialation of a PentairPacket. Includes public variables for many of the reverse engineered
 * packet content. Note, Intellichlor packet is of a different format and all helper functions in the base PentairPacket
 * may not apply.
 *
 * This packet can be a 3 or 4 data byte packet.
 *
 * 10 02 50 00 00 62 10 03
 * 10 02 00 01 00 00 13 10 03
 *
 * @author Jeff James - initial contribution
 *
 */
public class PentairPacketIntellichlor extends PentairPacket { // 29 byte packet format
    protected static final int CMD = 3; // not sure what this is, needs to be 11 for SALT_OUTPUT or SALINITY to be valid

    // 3 Length command
    protected static final int SALTOUTPUT = 4;

    // 4 Length command
    protected static final int SALINITY = 4;

    /** length of the packet - 3 or 4 data bytes */
    protected int length;
    /** for a saltoutput packet, represents the salt output percent */
    public int saltoutput;
    /** for a salinity packet, is value of salinity. Must be multiplied by 50 to get the actual salinity value. */
    public int salinity;

    /**
     * Constructor for Intellichlor packet. Does not call super constructure since the Intellichlor packet is structure
     * so differently
     *
     * @param buf
     * @param length
     */
    public PentairPacketIntellichlor(byte[] buf, int length) {
        this.buf = buf;
        this.length = length;

        if (length == 3) {
            saltoutput = buf[SALTOUTPUT];
        } else if (length == 4) {
            salinity = buf[SALINITY] & 0xFF; // make sure it is positive
        }
    }

    /**
     * Constructor for empty Intellichlor packet
     */
    public PentairPacketIntellichlor() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.pentair.PentairPacket#getLength()
     */
    @Override
    public int getLength() {
        return length;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.pentair.PentairPacket#setLength(int)
     */
    @Override
    public void setLength(int length) {
        if (length != this.length) {
            buf = new byte[length + 2];
        }
        this.length = length;
    }

    /**
     * Gets the command byte for this packet
     *
     * @return command
     */
    public int getCmd() {
        return buf[CMD];
    }

    @Override
    public String toString() {
        return bytesToHex(buf, length + 5);
    }
}
