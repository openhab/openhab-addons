/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.internal.handler;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Latest successful register image for one poller read window.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
final class PollerRegisterCache {

    private int start;
    private @Nullable ModbusRegisterArray registers;

    public synchronized void acceptSuccessfulPoll(int start, ModbusRegisterArray registers) {
        this.start = start;
        this.registers = registers;
    }

    public synchronized Optional<ModbusRegisterArray> read(int address, int length) {
        ModbusRegisterArray localRegisters = registers;
        long offset = (long) address - start;
        if (localRegisters == null || length <= 0 || offset < 0 || offset + length > localRegisters.size()) {
            return Optional.empty();
        }

        int[] result = new int[length];
        for (int index = 0; index < length; index++) {
            result[index] = localRegisters.getRegister((int) offset + index);
        }
        return Optional.of(new ModbusRegisterArray(result));
    }

    public synchronized Optional<ModbusRegisterArray> overlayRegisterBit(int address, int bit, boolean set) {
        if (bit < 0 || bit >= Short.SIZE) {
            return Optional.empty();
        }

        Optional<ModbusRegisterArray> current = read(address, 1);
        if (current.isEmpty()) {
            return Optional.empty();
        }

        ModbusRegisterArray localRegisters = registers;
        if (localRegisters == null) {
            return Optional.empty();
        }
        int offset = address - start;
        int[] updated = new int[localRegisters.size()];
        for (int index = 0; index < updated.length; index++) {
            updated[index] = localRegisters.getRegister(index);
        }
        updated[offset] = set ? updated[offset] | 1 << bit : updated[offset] & ~(1 << bit);
        registers = new ModbusRegisterArray(updated);
        return Optional.of(new ModbusRegisterArray(updated[offset]));
    }
}
