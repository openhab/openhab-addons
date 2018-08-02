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
