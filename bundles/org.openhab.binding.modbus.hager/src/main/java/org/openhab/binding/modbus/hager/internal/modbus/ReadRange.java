/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.hager.internal.modbus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;

/**
 * The {@link ReadRange} is a contiguous block of registers which is read with a single Modbus request.
 *
 * @param start zero based address of the first register
 * @param length number of 16 bit words in the block
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record ReadRange(int start, int length) {

    /** A Modbus request can read at most 125 registers. */
    private static final int MAX_LENGTH = 125;

    /**
     * Distance in words up to which a gap between two registers is read along instead of being requested separately -
     * reading a few unused words is cheaper than an additional Modbus round trip.
     */
    private static final int MAX_GAP = 16;

    /**
     * Groups the given registers into read blocks, in ascending address order.
     *
     * @param registers the registers to read
     * @return the blocks covering all given registers
     */
    public static List<ReadRange> of(List<Register> registers) {
        List<Register> sorted = new ArrayList<>(registers);
        sorted.sort(Comparator.comparingInt(Register::address));

        List<ReadRange> ranges = new ArrayList<>();
        int start = -1;
        int end = -1;
        for (Register register : sorted) {
            int registerEnd = register.address() + register.length();
            if (start < 0) {
                start = register.address();
                end = registerEnd;
            } else if (register.address() - end <= MAX_GAP && registerEnd - start <= MAX_LENGTH) {
                end = Math.max(end, registerEnd);
            } else {
                ranges.add(new ReadRange(start, end - start));
                start = register.address();
                end = registerEnd;
            }
        }
        if (start >= 0) {
            ranges.add(new ReadRange(start, end - start));
        }
        return List.copyOf(ranges);
    }

    /**
     * Tells whether the given register is completely contained in this block.
     *
     * @param register the register to look for
     * @return true if the register is read with this block
     */
    public boolean contains(Register register) {
        return register.address() >= start && register.address() + register.length() <= start + length;
    }

    @Override
    public String toString() {
        return String.format("%d..%d", start, start + length - 1);
    }
}
