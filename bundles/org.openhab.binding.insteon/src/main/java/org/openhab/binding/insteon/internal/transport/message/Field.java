/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.transport.message;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.utils.HexUtils;

/**
 * An Insteon message has several fields with known type and offset
 * within the message. This class represents a single field, and
 * holds name, type, and offset (but not value!).
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public final class Field {
    private final String name;
    private final int offset;
    private final FieldType type;

    public Field(String name, int offset, FieldType type) {
        this.name = name;
        this.offset = offset;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    private void check(int len, FieldType t) throws FieldException {
        if (offset + type.getSize() > len) {
            throw new FieldException("field write beyond end of msg");
        }
        if (type != t) {
            throw new FieldException("field write type mismatch!");
        }
    }

    public void set(byte[] data, String value) throws FieldException, IllegalArgumentException {
        switch (type) {
            case BYTE:
                byte b = value.isEmpty() ? 0x00 : (byte) HexUtils.toInteger(value);
                setByte(data, b);
                break;
            case ADDRESS:
                InsteonAddress address = value.isEmpty() ? InsteonAddress.UNKNOWN : new InsteonAddress(value);
                setAddress(data, address);
                break;
        }
    }

    /**
     * Sets a byte value to a byte array, at the proper offset.
     * Use this function to set the value of a field within a message.
     *
     * @param data the byte array to update
     * @param b the byte value to set
     * @throws FieldException
     */
    public void setByte(byte[] data, byte b) throws FieldException {
        check(data.length, FieldType.BYTE);
        data[offset] = b;
    }

    /**
     * Sets the value of an InsteonAddress to a message array.
     * Use this function to set the value of a field within a message.
     *
     * @param data the byte array to update
     * @param address the insteon address value to set
     * @throws FieldException
     */
    public void setAddress(byte[] data, InsteonAddress address) throws FieldException {
        check(data.length, FieldType.ADDRESS);
        System.arraycopy(address.getBytes(), 0, data, offset, type.getSize());
    }

    /**
     * Returns a byte from a byte array at the field position
     *
     * @param data the byte array to use
     * @return the byte
     * @throws FieldException
     */
    public byte getByte(byte[] data) throws FieldException {
        check(data.length, FieldType.BYTE);
        return data[offset];
    }

    /**
     * Returns an insteon address from the field position
     *
     * @param data the byte array to use
     * @return the insteon address
     * @throws FieldException
     */
    public InsteonAddress getAddress(byte[] data) throws FieldException {
        check(data.length, FieldType.ADDRESS);
        byte[] address = Arrays.copyOfRange(data, offset, offset + type.getSize());
        return new InsteonAddress(address);
    }

    /**
     * Returns a string representation for a given byte array
     *
     * @param data the byte array to use
     * @return the string representation
     */
    public String toString(byte[] data) {
        String s = name + ":";
        try {
            switch (type) {
                case BYTE:
                    s += HexUtils.getHexString(getByte(data));
                    break;
                case ADDRESS:
                    s += getAddress(data).toString();
                    break;
            }
        } catch (FieldException e) {
            s += "NULL";
        }
        return s;
    }

    @Override
    public String toString() {
        return name + " Type: " + type + " Offset: " + offset;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Field other = (Field) obj;
        return name.equals(other.name) && offset == other.offset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + offset;
        return result;
    }
}
