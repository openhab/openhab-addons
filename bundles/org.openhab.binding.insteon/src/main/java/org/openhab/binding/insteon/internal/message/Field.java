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
package org.openhab.binding.insteon.internal.message;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.utils.Utils;

/**
 * An Insteon message has several fields with known type and offset
 * within the message. This class represents a single field, and
 * holds name, type, and offset (but not value!).
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public final class Field {
    private final String name;
    private final int offset;
    private final DataType type;

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public DataType getType() {
        return type;
    }

    public Field(String name, DataType type, int off) {
        this.name = name;
        this.type = type;
        this.offset = off;
    }

    private void check(int arrayLen, DataType t) throws FieldException {
        checkSpace(arrayLen);
        checkType(t);
    }

    private void checkSpace(int arrayLen) throws FieldException {
        if (offset + type.getSize() > arrayLen) {
            throw new FieldException("field write beyond end of msg");
        }
    }

    private void checkType(DataType t) throws FieldException {
        if (type != t) {
            throw new FieldException("field write type mismatch!");
        }
    }

    @Override
    public String toString() {
        return getName() + " Type: " + getType() + " Offset " + getOffset();
    }

    public String toString(byte[] array) {
        String s = name + ":";
        try {
            switch (type) {
                case BYTE:
                    s += Utils.getHexByte(getByte(array));
                    break;
                case INT:
                    s += Integer.toString(getInt(array));
                    break;
                case ADDRESS:
                    s += getAddress(array).toString();
                    break;
                default:
                    break;
            }
        } catch (FieldException e) {
            // will just return empty string
        }
        return s;
    }

    public void set(byte[] array, Object o) throws FieldException {
        switch (getType()) {
            case BYTE:
                setByte(array, (Byte) o);
                break;
            case INT:
                setInt(array, (Integer) o);
                break;
            // case FLOAT: setFloat(array, (float) o); break;
            case ADDRESS:
                setAddress(array, (InsteonAddress) o);
                break;
            default:
                throw new FieldException("Not implemented data type " + getType() + "!");
        }
    }

    /**
     * Writes a byte value to a byte array, at the proper offset.
     * Use this function to set the value of a field within a message.
     *
     * @param array the destination array
     * @param b the value you want to set the byte to
     * @throws FieldException
     */
    public void setByte(byte[] array, byte b) throws FieldException {
        check(array.length, DataType.BYTE);
        array[offset] = b;
    }

    /**
     * Writes the value of an integer field to a byte array
     * Use this function to set the value of a field within a message.
     *
     * @param array the destination array
     * @param i the integer value to set
     */
    public void setInt(byte[] array, int i) throws FieldException {
        check(array.length, DataType.INT);
        array[offset] = (byte) ((i >>> 24) & 0xFF);
        array[offset + 1] = (byte) ((i >>> 16) & 0xFF);
        array[offset + 2] = (byte) ((i >>> 8) & 0xFF);
        array[offset + 3] = (byte) ((i >>> 0) & 0xFF);
    }

    /**
     * Writes the value of an InsteonAddress to a message array.
     * Use this function to set the value of a field within a message.
     *
     * @param array the destination array
     * @param adr the insteon address value to set
     */

    public void setAddress(byte[] array, InsteonAddress adr) throws FieldException {
        check(array.length, DataType.ADDRESS);
        adr.storeBytes(array, offset);
    }

    /**
     * Fetch a byte from the array at the field position
     *
     * @param array the array to fetch from
     * @return the byte value of the field
     */
    public byte getByte(byte[] array) throws FieldException {
        check(array.length, DataType.BYTE);
        return array[offset];
    }

    /**
     * Fetch an int from the array at the field position
     *
     * @param array the array to fetch from
     * @return the int value of the field
     */
    public int getInt(byte[] array) throws FieldException {
        check(array.length, DataType.INT);
        byte b1 = array[offset];
        byte b2 = array[offset + 1];
        byte b3 = array[offset + 2];
        byte b4 = array[offset + 3];
        int value = ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
        return value;
    }

    /**
     * Fetch an insteon address from the field position
     *
     * @param array the array to fetch from
     * @return the address
     */

    public InsteonAddress getAddress(byte[] array) throws FieldException {
        check(array.length, DataType.ADDRESS);
        InsteonAddress adr = new InsteonAddress();
        adr.loadBytes(array, offset);
        return adr;
    }

    /**
     * Equals test
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof Field) {
            Field f = (Field) o;
            return (f.getName().equals(getName())) && (f.getOffset() == getOffset());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getOffset());
    }
}
