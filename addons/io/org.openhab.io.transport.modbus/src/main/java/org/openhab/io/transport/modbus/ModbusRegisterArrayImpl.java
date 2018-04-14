/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

/**
 * Immutable {@link ModbusRegisterArray} implementation
 *
 * @author Sami Salonen
 */
public class ModbusRegisterArrayImpl implements ModbusRegisterArray {

    private ModbusRegister[] registers;

    /**
     * Construct plain <code>ModbusRegister[]</code> array from register values
     *
     * @param registerValues register values, each <code>int</code> corresponding to one register
     * @return
     */
    public static ModbusRegister[] registersFromValues(int... registerValues) {
        ModbusRegister[] registers = new ModbusRegister[registerValues.length];
        for (int i = 0; i < registerValues.length; i++) {
            registers[i] = new ModbusRegisterImpl(registerValues[i]);
        }
        return registers;
    }

    /**
     * Construct ModbusRegisterArrayImpl from array of {@link ModbusRegister}
     *
     * @param registers
     */
    public ModbusRegisterArrayImpl(ModbusRegister[] registers) {
        this.registers = registers;
    }

    /**
     * Construct plain <code>ModbusRegisterArrayImpl</code> array from register values
     *
     * @param registerValues register values, each <code>int</code> corresponding to one register
     * @return
     */
    public ModbusRegisterArrayImpl(int... registerValues) {
        this(registersFromValues(registerValues));
    }

    @Override
    public ModbusRegister getRegister(int index) {
        return registers[index];
    }

    @Override
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

}
