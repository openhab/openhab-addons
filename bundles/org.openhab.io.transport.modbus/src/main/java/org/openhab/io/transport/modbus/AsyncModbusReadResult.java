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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Encapsulates result of modbus read operations
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class AsyncModbusReadResult {

    private ModbusReadRequestBlueprint request;

    @Nullable
    private BitArray bits;

    @Nullable
    private ModbusRegisterArray registers;

    @Nullable
    private Exception cause;

    public AsyncModbusReadResult(ModbusReadRequestBlueprint request, ModbusRegisterArray registers) {
        Objects.requireNonNull(request, "Request must not be null!");
        Objects.requireNonNull(registers, "Registers must not be null!");
        this.request = request;
        this.registers = registers;
    }

    public AsyncModbusReadResult(ModbusReadRequestBlueprint request, BitArray bits) {
        Objects.requireNonNull(request, "Request must not be null!");
        Objects.requireNonNull(bits, "Bits must not be null!");
        this.request = request;
        this.bits = bits;
    }

    public AsyncModbusReadResult(ModbusReadRequestBlueprint request, Exception cause) {
        Objects.requireNonNull(request, "Request must not be null!");
        Objects.requireNonNull(cause, "Cause must not be null!");
        this.request = request;
        this.cause = cause;
    }

    /**
     * Whether this response is in error, that is cause is non-null
     *
     * @return whether this response is in error
     */
    public boolean hasError() {
        return cause != null;
    }

    /**
     * Get request matching this response
     *
     * @return request object
     */
    public ModbusReadRequestBlueprint getRequest() {
        return request;
    }

    /**
     * Get "coil" or "discrete input" bit data in the case of no errors
     *
     * @return bit data
     */
    @Nullable
    public BitArray getBits() {
        return bits;
    }

    /**
     * Get "input register" or "holding register" data in the case of no errors
     *
     * @return register data
     */
    @Nullable
    public ModbusRegisterArray getRegisters() {
        return registers;
    }

    /**
     * Get cause of error
     *
     * @return exception representing error
     */
    @Nullable
    public Exception getCause() {
        return cause;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("AsyncModbusReadResult(");
        builder.append("request = ");
        builder.append(request);
        if (bits != null) {
            builder.append(", bits = ");
            builder.append(bits);
        } else if (registers != null) {
            builder.append(", registers = ");
            builder.append(registers);
        } else {
            builder.append(", error = ");
            builder.append(cause);
        }
        builder.append(")");
        return builder.toString();
    }
}
