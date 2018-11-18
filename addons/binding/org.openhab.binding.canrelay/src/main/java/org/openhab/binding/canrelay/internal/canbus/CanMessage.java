/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.canbus;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * CanMessage represents a CanMessage transmitted over the CANBUS. Only supports base canIDs, no support for extended
 * CANBUS specification at this point. So canID can have 11bits max and data can have 8 unsigned bytes max (represented
 * as shorts)
 *
 * @author Lubos Housa - Initial contribution
 */
@NonNullByDefault
public class CanMessage {

    private static final short MAX_DATA_SIZE = 8;
    private static final short MAX_DATA_VALUE = 0xFF; // max 8 bits value
    private static final short MAX_CAN_ID = 0x7FF; // max 11 bits value

    /** CAN ID. Max 11 bits */
    private final short id;

    /**
     * CAN message data - need to use short since byte is signed and 1 data can have 8bits, but unsigned. The other
     * option may be char, but int/short is easier to manipulate with anyway in client code
     */
    private final List<Short> data;

    private CanMessage(short id, List<Short> data) {
        this.id = id;
        this.data = data;
    }

    /**
     * Return canID of this CanMessage
     */
    public short getId() {
        return id;
    }

    /**
     * Return the list of bytes representing the data payload of this CanMessage
     */
    public List<Short> getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id) + this.data.hashCode();
    }

    @Override
    @NonNullByDefault({})
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CanMessage)) {
            return false;
        }
        CanMessage other = (CanMessage) obj;
        if (getId() != other.getId()) {
            return false;
        }
        return getData().equals(other.getData());
    }

    /**
     * Using analog representation of canMessage as socketcan. https://github.com/linux-can/can-utils. E.g. candump
     * For example for canID = 0x01 and databytes = 0x22 and 0x89 it would return CanMessage[001#22 89]
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("CanMessage[").append(String.format("%03x#", getId()));

        // now loop through all data bytes and simply append as hex too
        boolean first = true;
        for (Short dataByte : data) {
            if (!first) {
                result.append(" ");
            }
            result.append(String.format("%02x", dataByte));
            first = false;
        }
        result.append("]");

        return result.toString();
    }

    public static class Builder {
        private short id;
        private List<Short> data = new LinkedList<>();

        /**
         * Sets the canID of the message to be created. Mind that it internally checks the canID is unsigned and max 11
         * bit value
         *
         * @param id canID to set
         * @return builder to use builder pattern
         */
        public Builder id(int id) {
            if (id < 0 || id > MAX_CAN_ID) {
                throw new IllegalArgumentException(
                        "CanMessage supports only base canID! Max 11 bits. Attempt to set invalid canID: "
                                + Integer.toHexString(id) + " to this " + toString());
            }
            this.id = (short) id;
            return this;
        }

        /**
         * Sets the next databyte in the can message to be created. You can chain calls to this methods in order to set
         * more databytes. Mind that you can only save up to 8 data bytes in the message. And the actual values need to
         * be unsigned max 8 bits
         *
         * @param dataByte next databyte to set
         * @return builder to use builder pattern
         */
        public Builder withDataByte(int dataByte) {
            if (dataByte < 0 || dataByte > MAX_DATA_VALUE) {
                throw new IllegalArgumentException(
                        "CanMessage can only store unsigned bytes of data! I.e. max 8 bits and > 0. Cannot store additional databyte: "
                                + Integer.toHexString(dataByte) + " to this " + toString());
            }

            if (data.size() == MAX_DATA_SIZE) {
                throw new IllegalArgumentException(
                        "CanMessage can only store 8 unsigned bytes of data! Cannot store additional databyte: "
                                + Integer.toHexString(dataByte) + " to this " + toString());
            }
            this.data.add((short) dataByte);
            return this;
        }

        /**
         * Build the CANMessage using this builder
         *
         * @return new instance of CANMessage initiated using this builder
         */
        public CanMessage build() {
            if (data.isEmpty()) {
                throw new IllegalStateException("CanMessage has to store at least 1 data byte! This " + toString()
                        + " does not have any data bytes set.");
            }
            return new CanMessage(this.id, Collections.unmodifiableList(this.data));
        }
    }

    /**
     * Create new builder to instantiate CanMessage instances
     */
    public static Builder newBuilder() {
        return new Builder();
    }
}
