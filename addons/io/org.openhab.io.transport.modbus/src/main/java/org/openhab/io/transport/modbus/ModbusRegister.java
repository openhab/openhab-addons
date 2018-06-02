/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for 16 bit Modbus registers.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusRegister {

    /**
     * Get raw data represented by this register. Since register is 16 bits, array of length 2 will be returned.
     *
     * @return byte array of length 2, high byte first.
     */
    public byte[] getBytes();

    /**
     * Returns the value of this register as integer representing 16 bit data parsed as signed integer.
     *
     * @return the register content as unsigned integer
     */
    public int getValue();

    /**
     * Returns the value of this register as integer representing 16 bit data parsed as unsigned integer.
     *
     * @return the register content as unsigned integer
     */
    public int toUnsignedShort();

    /**
     * Returns the register value as hex string
     *
     * For example, 12 34
     *
     * @return string representing the register data
     */
    default String toHexString() {
        StringBuffer buffer = new StringBuffer(5);
        return appendHexString(buffer).toString();
    }

    /**
     * Appends the register value as hex string to the given StringBuffer
     *
     */
    default StringBuffer appendHexString(StringBuffer buffer) {
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
