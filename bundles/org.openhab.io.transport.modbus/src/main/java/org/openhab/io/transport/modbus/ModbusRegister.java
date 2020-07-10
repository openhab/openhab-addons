/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.io.transport.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

import net.wimpi.modbus.procimg.SimpleInputRegister;

/**
 * Basic {@link ModbusRegister} implementation
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusRegister {

    private final SimpleInputRegister wrapped;

    /**
     * Constructs a new instance for bytes
     *
     * @param b1 the first (hi) byte of the word.
     * @param b2 the second (low) byte of the word.
     */
    public ModbusRegister(byte b1, byte b2) {
        wrapped = new SimpleInputRegister(b1, b2);
    }

    /**
     * Construct register for at
     *
     * @param val value representing register data. The <code>int</code> will be downcasted to <code>short</code>.
     */
    public ModbusRegister(int val) {
        wrapped = new SimpleInputRegister(val);
    }

    /**
     * Get raw data represented by this register. Since register is 16 bits, array of length 2 will be returned.
     *
     * @return byte array of length 2, high byte first.
     */
    public byte[] getBytes() {
        return wrapped.toBytes();
    }

    /**
     * Returns the value of this register as integer representing 16 bit data parsed as signed integer.
     *
     * @return the register content as unsigned integer
     */
    public int getValue() {
        return wrapped.getValue();
    }

    /**
     * Returns the value of this register as integer representing 16 bit data parsed as unsigned integer.
     *
     * @return the register content as unsigned integer
     */
    public int toUnsignedShort() {
        return wrapped.toUnsignedShort();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("ModbusRegisterImpl(");
        buffer.append("uint16=").append(toUnsignedShort()).append(", hex=");
        return appendHexString(buffer).append(')').toString();
    }

    /**
     * Returns the register value as hex string
     *
     * For example, 12 34
     *
     * @return string representing the register data
     */
    public String toHexString() {
        StringBuffer buffer = new StringBuffer(5);
        return appendHexString(buffer).toString();
    }

    /**
     * Appends the register value as hex string to the given StringBuffer
     *
     */
    public StringBuffer appendHexString(StringBuffer buffer) {
        byte[] bytes = getBytes();
        for (int i = 0; i < 2; i++) {
            byte b = bytes[i];
            String byteHex = Long.toHexString(b & 0xff);
            if ((b & 0xff) < 0x10) {
                buffer.append('0');
            }
            buffer.append(byteHex);
            if (i == 0) {
                buffer.append(' ');
            }
        }
        return buffer;
    }
}
