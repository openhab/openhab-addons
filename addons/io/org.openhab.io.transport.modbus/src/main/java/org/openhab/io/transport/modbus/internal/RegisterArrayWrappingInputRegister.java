/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

import net.wimpi.modbus.procimg.InputRegister;

/**
 * Implementation of {@link ModbusRegisterArray} which wraps array of {@link InputRegister}
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class RegisterArrayWrappingInputRegister implements ModbusRegisterArray {

    private class RegisterReference implements ModbusRegister {

        private InputRegister wrappedRegister;

        public RegisterReference(int index) {
            this.wrappedRegister = wrapped[index];
        }

        @Override
        public byte[] getBytes() {
            return wrappedRegister.toBytes();
        }

        @Override
        public int getValue() {
            return wrappedRegister.getValue();
        }

        @Override
        public int toUnsignedShort() {
            return wrappedRegister.toUnsignedShort();
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer("ModbusRegisterImpl(");
            buffer.append("uint16=").append(toUnsignedShort()).append(", hex=");
            return appendHexString(buffer).append(')').toString();
        }

    }

    private InputRegister[] wrapped;
    private Map<Integer, ModbusRegister> cache = new HashMap<>();

    public RegisterArrayWrappingInputRegister(InputRegister[] wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ModbusRegister getRegister(int index) {
        return cache.computeIfAbsent(index, i -> new RegisterReference(i));
    }

    @Override
    public int size() {
        return wrapped.length;
    }

    @Override
    public String toString() {
        if (wrapped.length == 0) {
            return "RegisterArrayWrappingInputRegister(<empty>)";
        }
        StringBuffer buffer = new StringBuffer(wrapped.length * 2).append("RegisterArrayWrappingInputRegister(");
        return appendHexString(buffer).append(')').toString();
    }

}
