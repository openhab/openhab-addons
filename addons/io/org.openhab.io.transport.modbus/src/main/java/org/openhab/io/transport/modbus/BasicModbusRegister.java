/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
public class BasicModbusRegister implements ModbusRegister {

    private SimpleInputRegister wrapped;

    /**
     * Constructs a new instance for bytes
     *
     * @param b1 the first (hi) byte of the word.
     * @param b2 the second (low) byte of the word.
     */
    public BasicModbusRegister(byte b1, byte b2) {
        wrapped = new SimpleInputRegister(b1, b2);
    }

    /**
     * Construct register for at
     *
     * @param val value representing register data. The <code>int</code> will be downcasted to <code>short</code>.
     */
    public BasicModbusRegister(int val) {
        wrapped = new SimpleInputRegister(val);
    }

    @Override
    public byte[] getBytes() {
        return wrapped.toBytes();
    }

    @Override
    public int getValue() {
        return wrapped.getValue();
    }

    @Override
    public int toUnsignedShort() {
        return wrapped.toUnsignedShort();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("ModbusRegisterImpl(");
        buffer.append("uint16=").append(toUnsignedShort()).append(", hex=");
        return appendHexString(buffer).append(')').toString();
    }
}
