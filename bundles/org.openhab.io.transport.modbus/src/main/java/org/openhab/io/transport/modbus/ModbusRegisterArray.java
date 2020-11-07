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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 * Immutable {@link ModbusRegisterArray} implementation
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusRegisterArray {

    private final byte[] bytes;

    public ModbusRegisterArray(byte... bytes) {
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Construct plain <code>ModbusRegisterArray</code> array from register values
     *
     * @param registerValues register values, each <code>int</code> corresponding to one register
     * @return
     */
    public ModbusRegisterArray(int... registerValues) {
        bytes = new byte[registerValues.length * 2];
        for (int registerIndex = 0; registerIndex < registerValues.length; registerIndex++) {
            int register = registerValues[registerIndex] & 0xffff;
            // hi-byte
            bytes[registerIndex * 2] = (byte) (register >> 8);
            // lo byte
            bytes[registerIndex * 2 + 1] = (byte) register;
        }
    }

    /**
     * Get register index i as unsigned integer
     *
     * @param i register index
     * @return register value interpreted as unsigned integer (big-endian byte ordering)
     */
    public int getRegister(int i) {
        int hi = bytes[i * 2] & 0xff;
        int lo = bytes[i * 2 + 1] & 0xff;
        return ((hi << 8) | lo) & 0xffff;
    }

    /**
     * Return bytes representing the registers
     *
     *
     * Index 0: hi-byte of 1st register
     * Index 1: low-byte of 1st register
     * Index 3: hi-byte of 2nd register
     * Index 4: low-byte of 2nd register
     * ...
     *
     * @return set of bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Get number of registers stored in this instance
     *
     * @return
     */
    public int size() {
        return bytes.length / 2;
    }

    @Override
    public String toString() {
        if (bytes.length == 0) {
            return "ModbusRegisterArray(<empty>)";
        }
        return new StringBuilder(bytes.length).append("ModbusRegisterArray(").append(toHexString()).append(')')
                .toString();
    }

    /**
     * Get register data as a hex string
     *
     * For example, 04 45 00 00
     *
     * @return string representing the bytes of the register array
     */
    public String toHexString() {
        if (size() == 0) {
            return "";
        }
        return HexUtils.bytesToHex(getBytes());
    }
}
