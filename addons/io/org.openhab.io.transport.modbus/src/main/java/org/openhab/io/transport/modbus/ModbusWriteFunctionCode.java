/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;

import net.wimpi.modbus.Modbus;

/**
 * Modbus write function codes supported by this transport
 *
 * @author Sami Salonen - Initial contribution
 */
public enum ModbusWriteFunctionCode {
    WRITE_COIL(Modbus.WRITE_COIL),
    WRITE_MULTIPLE_COILS(Modbus.WRITE_MULTIPLE_COILS),
    WRITE_SINGLE_REGISTER(Modbus.WRITE_SINGLE_REGISTER),
    WRITE_MULTIPLE_REGISTERS(Modbus.WRITE_MULTIPLE_REGISTERS);

    private final int functionCode;

    ModbusWriteFunctionCode(int code) {
        functionCode = code;
    }

    /**
     * Get numeric function code represented by this instance
     *
     * @return
     */
    public int getFunctionCode() {
        return functionCode;
    }

    /**
     * Construct {@link ModbusWriteFunctionCode} from the numeric function code
     *
     * @param functionCode numeric function code
     * @return {@link ModbusWriteFunctionCode} matching the numeric function code
     * @throws IllegalArgumentException with unsupported functions
     */
    @SuppressWarnings("null")
    public static @NonNull ModbusWriteFunctionCode fromFunctionCode(int functionCode) throws IllegalArgumentException {
        return Stream.of(ModbusWriteFunctionCode.values()).filter(v -> v.getFunctionCode() == functionCode).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid functionCode"));
    }

}
