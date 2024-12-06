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

    private final byte highByte;
    private final byte middleByte;
    private final byte lowByte;

    public InsteonAddress(InsteonAddress address) {
        this.highByte = address.highByte;
        this.middleByte = address.middleByte;
        this.lowByte = address.lowByte;
    }

    public InsteonAddress(byte highByte, byte middleByte, byte lowByte) {
        this.highByte = highByte;
        this.middleByte = middleByte;
        this.lowByte = lowByte;
    }

    public InsteonAddress(byte[] b) throws ArrayIndexOutOfBoundsException {
        this.highByte = b[0];
        this.middleByte = b[1];
        this.lowByte = b[2];
    }

    public InsteonAddress(String address) throws IllegalArgumentException {
        String[] parts = address.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Address string must have 3 bytes, has: " + parts.length);
        }
        try {
            this.highByte = (byte) HexUtils.toInteger(parts[0]);
            this.middleByte = (byte) HexUtils.toInteger(parts[1]);
            this.lowByte = (byte) HexUtils.toInteger(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Address string must have hexadecimal bytes");
        }
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

    public byte[] getBytes() {
        return new byte[] { highByte, middleByte, lowByte };
    }

    @Override
    public String toString() {
        return String.format("%02X.%02X.%02X", highByte, middleByte, lowByte);
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
        return highByte == other.highByte && middleByte == other.middleByte && lowByte == other.lowByte;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + highByte;
        result = prime * result + middleByte;
        result = prime * result + lowByte;
        return result;
    }

    /**
     * Returns if Insteon address is valid
     *
     * @return true if address is valid
     */
    public static boolean isValid(@Nullable String address) {
        if (address == null) {
            return false;
        }
        try {
            new InsteonAddress(address);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
