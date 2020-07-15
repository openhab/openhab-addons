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

import java.util.Iterator;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable {@link ModbusRegisterArray} implementation
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusRegisterArray implements Iterable<ModbusRegister> {

    private final ModbusRegister[] registers;

    /**
     * Construct plain <code>ModbusRegister[]</code> array from register values
     *
     * @param registerValues register values, each <code>int</code> corresponding to one register
     * @return
     */
    public static ModbusRegister[] registersFromValues(int... registerValues) {
        ModbusRegister[] registers = new ModbusRegister[registerValues.length];
        for (int i = 0; i < registerValues.length; i++) {
            registers[i] = new ModbusRegister(registerValues[i]);
        }
        return registers;
    }

    /**
     * Construct ModbusRegisterArrayImpl from array of {@link ModbusRegister}
     *
     * @param registers
     */
    public ModbusRegisterArray(ModbusRegister[] registers) {
        this.registers = registers;
    }

    /**
     * Construct plain <code>ModbusRegisterArrayImpl</code> array from register values
     *
     * @param registerValues register values, each <code>int</code> corresponding to one register
     * @return
     */
    public ModbusRegisterArray(int... registerValues) {
        this(registersFromValues(registerValues));
    }

    /**
     * Return register at the given index
     *
     * Index 0 matches first register (lowest register index).
     * <p>
     *
     * @param index the index of the register to be returned.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public ModbusRegister getRegister(int index) {
        return registers[index];
    }

    /**
     * Get number of registers stored in this instance
     *
     * @return
     */
    public int size() {
        return registers.length;
    }

    @Override
    public String toString() {
        if (registers.length == 0) {
            return "ModbusRegisterArrayImpl(<empty>)";
        }
        StringBuffer buffer = new StringBuffer(registers.length * 2).append("ModbusRegisterArrayImpl(");
        return appendHexString(buffer).append(')').toString();
    }

    /**
     * Iterator over all the registers
     */
    @Override
    public Iterator<ModbusRegister> iterator() {
        return IntStream.range(0, size()).mapToObj(i -> getRegister(i)).iterator();
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
        // Initialize capacity to (n*2 + n-1), two chars per byte + spaces in between
        StringBuffer buffer = new StringBuffer(size() * 2 + (size() - 1));
        return appendHexString(buffer).toString();
    }

    /**
     * Appends the register data as hex string to the given StringBuffer
     *
     */
    public StringBuffer appendHexString(StringBuffer buffer) {
        IntStream.range(0, size()).forEachOrdered(index -> {
            getRegister(index).appendHexString(buffer);
            if (index < size() - 1) {
                buffer.append(' ');
            }
        });
        return buffer;
    }
}
