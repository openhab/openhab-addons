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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Access.*;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;

/**
 * The {@link ReadRangeTest} verifies that registers are grouped into as few Modbus requests as possible.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ReadRangeTest {

    @Test
    public void testContiguousRegistersAreOneBlock() {
        List<ReadRange> ranges = ReadRange.of(List.of(register(100, 2), register(102, 1), register(103, 4)));

        assertEquals(List.of(new ReadRange(100, 7)), ranges);
    }

    @Test
    public void testUnsortedRegisters() {
        List<ReadRange> ranges = ReadRange.of(List.of(register(103, 4), register(100, 2), register(102, 1)));

        assertEquals(List.of(new ReadRange(100, 7)), ranges);
    }

    @Test
    public void testSmallGapIsReadAlong() {
        List<ReadRange> ranges = ReadRange.of(List.of(register(100, 1), register(110, 1)));

        assertEquals(List.of(new ReadRange(100, 11)), ranges);
    }

    @Test
    public void testLargeGapIsReadSeparately() {
        List<ReadRange> ranges = ReadRange.of(List.of(register(100, 1), register(300, 1)));

        assertEquals(List.of(new ReadRange(100, 1), new ReadRange(300, 1)), ranges);
    }

    @Test
    public void testMaximumRequestLengthIsRespected() {
        List<Register> registers = new ArrayList<>();
        for (int address = 0; address < 200; address++) {
            registers.add(register(address, 1));
        }

        List<ReadRange> ranges = ReadRange.of(registers);

        assertEquals(List.of(new ReadRange(0, 125), new ReadRange(125, 75)), ranges);
    }

    @Test
    public void testSingleRegister() {
        List<ReadRange> ranges = ReadRange.of(List.of(register(4097, 1)));

        assertEquals(List.of(new ReadRange(4097, 1)), ranges);
    }

    @Test
    public void testContains() {
        ReadRange range = new ReadRange(100, 10);

        assertTrue(range.contains(register(100, 1)));
        assertTrue(range.contains(register(105, 5)));
        assertFalse(range.contains(register(99, 1)));
        assertFalse(range.contains(register(109, 2)));
    }

    @Test
    public void testToString() {
        assertEquals("512..517", new ReadRange(512, 6).toString());
    }

    private static Register register(int address, int length) {
        return new Register("register-" + address, "group", address, length, U16, READ_ONLY, null, Map.of(), List.of());
    }
}
