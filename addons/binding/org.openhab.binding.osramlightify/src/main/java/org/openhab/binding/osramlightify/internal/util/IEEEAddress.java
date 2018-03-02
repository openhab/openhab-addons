/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.util;

import java.util.Arrays;

/**
 * A representation of a 64bit IEEE address.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class IEEEAddress {

    public static final int ADDRESS_LENGTH = 8;

    private static final byte[] allOnes = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };

    private final byte[] data;
    private String text;

    /**
     * Create a new and empty IEEEAddress.
     */
    public IEEEAddress() {
        data = new byte[ADDRESS_LENGTH];
        text = null;
    }

    /**
     * Create a new IEEEAddress and initialize with the byte[] and string given.
     *
     * Note that no checking of any kind is performed on the given data!
     */
    public IEEEAddress(byte[] data, String text) {
        this.data = data;
        this.text = text;
    }

    /**
     * Create a new IEEEAddress and initialize from the string given.
     *
     * Note that no checking of any kind is performed on the given data!
     */
    public IEEEAddress(String address) {
        data = new byte[ADDRESS_LENGTH];
        text = address;

        for (int i = 0; i < ADDRESS_LENGTH; i++) {
            data[ADDRESS_LENGTH - 1 - i] = (byte) ((Character.digit(address.charAt(i * 3), 16) << 4) | Character.digit(address.charAt(i * 3 + 1), 16));
        }
    }

    /**
     * Get the IEEE address as an array of bytes.
     *
     * @return an array backed by the value of the IEEEAddress.
     */
    public byte[] array() {
        return data;
    }

    /**
     * Clone an IEEEAddress.
     *
     * This is a deep clone.
     *
     * @return a new IEEEAddress with the same data as the original.
     */
    public IEEEAddress clone() {
        return new IEEEAddress(Arrays.copyOf(data, data.length), text);
    }

    /**
     * Test if the IEEEAddress is broadcast.
     *
     * An IEEE address is considered broadcast if all bytes are 0xff.
     *
     * @return true if the IEEE address is broadcast.
     */
    public boolean isBroadcast() {
        return Arrays.equals(data, allOnes);
    }

    /**
     * Test if the IEEEAddress is groupcast.
     *
     * An IEEE address is considered groupcast if the highest 6 bytes
     * are all 0x00.
     *
     * @return true if the IEEE address is groupcast.
     */
    public boolean isGroupcast() {
        return data[2] == 0 && data[3] == 0 && data[4] == 0 && data[5] == 0 && data[6] == 0 && data[7] == 0;
    }

    /**
     * Test if the IEEEAddress is unicast.
     *
     * An IEEE address is considered unicast if it is neither broadcast
     * nor groupcast.
     *
     * @return true if the IEEE address is unicast.
     */
    public boolean isUnicast() {
        return !isBroadcast() && !isGroupcast();
    }

    /**
     * Reset the string description to null.
     *
     * This should be used after writing a value directly into the IEEEAddress' array()
     * so that the textual description is regenerated the next time it is needed.
     */
    public void resetString() {
        text = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IEEEAddress) {
            IEEEAddress address = (IEEEAddress) obj;
            return (address == this) || data == address.array() || Arrays.equals(data, address.array());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (data[7] << 24) + (data[6] << 16) + (data[5] << 8) + data[4]
            + (data[3] << 24) + (data[2] << 16) + (data[1] << 8) + data[0];
    }

    public String toString() {
        if (text == null) {
            text = String.format("%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X",
                (data[7] & 0xff), (data[6] & 0xff), (data[5] & 0xff), (data[4] & 0xff),
                (data[3] & 0xff), (data[2] & 0xff), (data[1] & 0xff), (data[0] & 0xff));
        }

        return text;
    }
}
