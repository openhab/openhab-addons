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

/**
 * Immutable {@link ModbusRegisterArray} implementation
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class BasicModbusRegisterArray implements ModbusRegisterArray {

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
            registers[i] = new BasicModbusRegister(registerValues[i]);
        }
        return registers;
    }

    /**
     * Construct ModbusRegisterArrayImpl from array of {@link ModbusRegister}
     *
     * @param registers
     */
    public BasicModbusRegisterArray(ModbusRegister[] registers) {
        this.registers = registers;
    }

    /**
     * Construct plain <code>ModbusRegisterArrayImpl</code> array from register values
     *
     * @param registerValues register values, each <code>int</code> corresponding to one register
     * @return
     */
    public BasicModbusRegisterArray(int... registerValues) {
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
