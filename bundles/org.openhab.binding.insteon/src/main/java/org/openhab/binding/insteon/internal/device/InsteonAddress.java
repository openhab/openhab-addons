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
import org.openhab.binding.insteon.internal.utils.HexUtils;

/**
 * The {@link InsteonAddress} represents an Insteon address
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonAddress implements DeviceAddress {
    public static final InsteonAddress UNKNOWN = new InsteonAddress("00.00.00");
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

    public InsteonAddress(InsteonAddress address) {
        highByte = address.highByte;
        middleByte = address.middleByte;
        lowByte = address.lowByte;
        x10 = address.x10;
    }

    public InsteonAddress(byte high, byte middle, byte low) {
        highByte = high;
        middleByte = middle;
        lowByte = low;
        x10 = false;
    }

    public InsteonAddress(byte[] b) throws ArrayIndexOutOfBoundsException {
        this.highByte = b[0];
        this.middleByte = b[1];
        this.lowByte = b[2];
    }

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
            try {
                highByte = (byte) HexUtils.toInteger(parts[0]);
                middleByte = (byte) HexUtils.toInteger(parts[1]);
                lowByte = (byte) HexUtils.toInteger(parts[2]);
                x10 = false;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Address string must have hexadecimal bytes");
            }
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

    public byte[] getBytes() {
        return new byte[] { highByte, middleByte, lowByte };
    }

    @Override
    public String toString() {
        if (isX10()) {
            byte house = (byte) (((getLowByte() & 0xf0) >> 4) & 0xff);
            byte unit = (byte) ((getLowByte() & 0x0f) & 0xff);
            return X10.houseToString(house) + "." + X10.unitToInt(unit);
        } else {
            return String.format("%02X.%02X.%02X", highByte, middleByte, lowByte);
        }
    }

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
        return highByte == other.highByte && middleByte == other.middleByte && lowByte == other.lowByte
                && x10 == other.x10;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + highByte;
        result = prime * result + middleByte;
        result = prime * result + lowByte;
        result = prime * result + (x10 ? 1231 : 1237);
        return result;
    }

    /**
     * Tests if Insteon address is valid
     *
     * @return true if address is valid
     */
    public static boolean isValid(@Nullable String address) {
        if (address == null) {
            return false;
        }
        if (X10.isValidAddress(address)) {
            return true;
        }
        try {
            new InsteonAddress(address);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
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
