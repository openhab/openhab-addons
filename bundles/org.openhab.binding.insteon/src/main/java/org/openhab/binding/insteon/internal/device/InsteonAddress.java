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
package org.openhab.binding.insteon.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.utils.Utils;

/**
 * This class wraps an Insteon Address 'xx.xx.xx'
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class InsteonAddress {
    private byte highByte;
    private byte middleByte;
    private byte lowByte;
    private boolean x10;

    public InsteonAddress() {
        highByte = 0x00;
        middleByte = 0x00;
        lowByte = 0x00;
        x10 = false;
    }

    public InsteonAddress(InsteonAddress a) {
        highByte = a.highByte;
        middleByte = a.middleByte;
        lowByte = a.lowByte;
        x10 = a.x10;
    }

    public InsteonAddress(byte high, byte middle, byte low) {
        highByte = high;
        middleByte = middle;
        lowByte = low;
        x10 = false;
    }

    /**
     * Constructor
     *
     * @param address string must have format of e.g. '2a.3c.40' or (for X10) 'H.UU'
     */
    public InsteonAddress(String address) throws IllegalArgumentException {
        if (X10.isValidAddress(address)) {
            highByte = 0;
            middleByte = 0;
            lowByte = X10.addressToByte(address);
            x10 = true;
        } else {
            String[] parts = address.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Address string must have 3 bytes, has: " + parts.length);
            }
            highByte = (byte) Utils.fromHexString(parts[0]);
            middleByte = (byte) Utils.fromHexString(parts[1]);
            lowByte = (byte) Utils.fromHexString(parts[2]);
            x10 = false;
        }
    }

    /**
     * Constructor for an InsteonAddress that wraps an X10 address.
     * Simply stuff the X10 address into the lowest byte.
     *
     * @param aX10HouseUnit the house and unit number as encoded by the X10 protocol
     */
    public InsteonAddress(byte aX10HouseUnit) {
        highByte = 0;
        middleByte = 0;
        lowByte = aX10HouseUnit;
        x10 = true;
    }

    public void setHighByte(byte h) {
        highByte = h;
    }

    public void setMiddleByte(byte m) {
        middleByte = m;
    }

    public void setLowByte(byte l) {
        lowByte = l;
    }

    public byte getHighByte() {
        return highByte;
    }

    public byte getMiddleByte() {
        return middleByte;
    }

    public byte getLowByte() {
        return lowByte;
    }

    public byte getX10HouseCode() {
        return (byte) ((lowByte & 0xf0) >> 4);
    }

    public byte getX10UnitCode() {
        return (byte) ((lowByte & 0x0f));
    }

    public boolean isX10() {
        return x10;
    }

    public void storeBytes(byte[] bytes, int offset) {
        bytes[offset] = getHighByte();
        bytes[offset + 1] = getMiddleByte();
        bytes[offset + 2] = getLowByte();
    }

    public void loadBytes(byte[] bytes, int offset) {
        setHighByte(bytes[offset]);
        setMiddleByte(bytes[offset + 1]);
        setLowByte(bytes[offset + 2]);
    }

    @Override
    public String toString() {
        String s = null;
        if (isX10()) {
            byte house = (byte) (((getLowByte() & 0xf0) >> 4) & 0xff);
            byte unit = (byte) ((getLowByte() & 0x0f) & 0xff);
            s = X10.houseToString(house) + "." + X10.unitToInt(unit);
            // s = Utils.getHexString(lowByte);
        } else {
            s = Utils.getHexString(highByte) + "." + Utils.getHexString(middleByte) + "." + Utils.getHexString(lowByte);
        }
        return s;
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InsteonAddress other = (InsteonAddress) obj;
        if (highByte != other.highByte) {
            return false;
        }
        if (lowByte != other.lowByte) {
            return false;
        }
        if (middleByte != other.middleByte) {
            return false;
        }
        if (x10 != other.x10) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + highByte;
        result = prime * result + lowByte;
        result = prime * result + middleByte;
        result = prime * result + (x10 ? 1231 : 1237);
        return result;
    }

    /**
     * Test if Insteon address is valid
     *
     * @return true if address is in valid AB.CD.EF or (for X10) H.UU format
     */
    public static boolean isValid(@Nullable String addr) {
        if (addr == null) {
            return false;
        }
        if (X10.isValidAddress(addr)) {
            return true;
        }
        String[] fields = addr.split("\\.");
        if (fields.length != 3) {
            return false;
        }
        try {
            // convert the insteon xx.xx.xx address to integer to test
            @SuppressWarnings("unused")
            int test = Integer.parseInt(fields[2], 16) * 65536 + Integer.parseInt(fields[1], 16) * 256
                    + +Integer.parseInt(fields[0], 16);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Turn string into address
     *
     * @param val the string to convert
     * @return the corresponding insteon address
     */
    public static InsteonAddress parseAddress(String val) {
        return new InsteonAddress(val);
    }
}
