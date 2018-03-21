/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

import net.wimpi.modbus.procimg.InputRegister;

/**
 * Implementation of {@link ModbusRegisterArray} which wraps array of {@link InputRegister}
 *
 * @author Sami Salonen
 */
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
        return new StringBuilder("RegisterArrayWrappingInputRegister(hiLowBytes=[ ").append(StringUtils.join(
                Stream.of(wrapped).map(reg -> reg.toBytes()).map(bytes -> Arrays.toString(bytes)).iterator(), ", "))
                .append(" ])").toString();
    }

}
