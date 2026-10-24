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

import java.util.List;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.BitFlag;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link RegisterDecoderTest} verifies the decoding of the Modbus data types used by the Hager Flow registers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class RegisterDecoderTest {

    private static final String GROUP = "general-information";

    /** Identifies the Thing in the log messages of the decoder. */
    private static final String CONTEXT = "test";

    @Test
    public void testUnsigned16WithUnit() {
        State state = decode(register("max-current-phase", 0, 1, U16, Units.AMPERE), new ModbusRegisterArray(16));

        assertEquals("16 A", state.toString());
    }

    @Test
    public void testSigned32WithUnit() {
        // 0xffffff9c is -100 as signed 32 bit
        State state = decode(register("all-battery-power-sum", 0, 2, I32, Units.WATT),
                new ModbusRegisterArray(0xffff, 0xff9c));

        assertEquals("-100 W", state.toString());
    }

    @Test
    public void testUnsigned32() {
        // 65538 needs both words, most significant word first
        State state = decode(register("evcs-count", 0, 2, U32, null), new ModbusRegisterArray(0x0001, 0x0002));

        assertEquals(new DecimalType(65538), state);
    }

    @Test
    public void testFloat64() {
        // 1234.5 as IEEE 754 double, most significant word first
        State state = decode(register("powermeter-energy-sum", 0, 4, F64, Units.WATT_HOUR),
                new ModbusRegisterArray(0x4093, 0x4a00, 0x0000, 0x0000));

        assertEquals("1234.5 Wh", state.toString());
    }

    @Test
    public void testStringStopsAtNulByte() {
        // 0x4556 is "EV", 0x4353 is "CS", the trailing zero word ends the string
        State state = decode(register("evcs-name", 0, 3, STRING, null), new ModbusRegisterArray(0x4556, 0x4353, 0));

        assertEquals(new StringType("EVCS"), state);
    }

    @Test
    public void testBoolean() {
        Register on = register("storage-bat-status", 0, 1, BOOL, null);
        Register off = register("powermeter-working-status", 1, 1, BOOL, null);
        ModbusRegisterArray registers = new ModbusRegisterArray(1, 0);

        assertEquals(OnOffType.ON, decode(on, registers, 0));
        assertEquals(OnOffType.OFF, decode(off, registers, 0));
    }

    @Test
    public void testStorageDcdcStatusShowsValuesBeyondBoolean() {
        // the device reports 2 for this register while the battery sits at its reserve level
        State state = decode(HagerFlowStorageRegisterMap.STORAGE_DCDC_STATUS, new ModbusRegisterArray(2));

        assertEquals(new DecimalType(2), state);
    }

    @Test
    public void testEnumeration() {
        Register register = new Register("blackout-active", GROUP, 0, 1, U16, READ_ONLY, null,
                Map.of(0, "NOT_POSSIBLE", 1, "ACTIVE"), List.of());

        assertEquals(new StringType("ACTIVE"), decode(register, new ModbusRegisterArray(1)));
        assertEquals(UnDefType.UNDEF, decode(register, new ModbusRegisterArray(9)));
    }

    @Test
    public void testSgReadyActivePublishesTheDocumentedValues() {
        assertEquals(new StringType("Inactive"),
                decode(HagerFlowSgReadyRegisterMap.SGREADY_ACTIVE, new ModbusRegisterArray(0)));
        assertEquals(new StringType("active"),
                decode(HagerFlowSgReadyRegisterMap.SGREADY_ACTIVE, new ModbusRegisterArray(1)));
    }

    @Test
    public void testBitFlags() {
        Register register = new Register("evcs-state", GROUP, 0, 1, U16, READ_ONLY, null, Map.of(),
                List.of(new BitFlag(0, "evcs-state-normal-mode", "normal mode", "not normal mode"),
                        new BitFlag(2, "evcs-state-sun-mode-active", "sun mode", "no sun mode")));

        // bit 0 and bit 2 set
        List<ChannelUpdate> updates = RegisterDecoder.decode(CONTEXT, register, new ModbusRegisterArray(0b101), 0);

        assertEquals(2, updates.size());
        assertEquals("evcs-state-normal-mode", updates.get(0).channelId());
        assertEquals(OnOffType.ON, updates.get(0).state());
        assertEquals("evcs-state-sun-mode-active", updates.get(1).channelId());
        assertEquals(OnOffType.ON, updates.get(1).state());
    }

    @Test
    public void testUnsetBitFlag() {
        Register register = new Register("evcs-state", GROUP, 0, 1, U16, READ_ONLY, null, Map.of(),
                List.of(new BitFlag(3, "evcs-state-temperature-derating-active", "derating", "no derating")));

        List<ChannelUpdate> updates = RegisterDecoder.decode(CONTEXT, register, new ModbusRegisterArray(0b0111), 0);

        assertEquals(1, updates.size());
        assertEquals(OnOffType.OFF, updates.get(0).state());
    }

    @Test
    public void testBitfieldIsNotDecoded() {
        Register register = new Register("status", "charging-session-information", 0, 4, BITFIELD, READ_ONLY, null,
                Map.of(), List.of());

        assertTrue(RegisterDecoder.decode(CONTEXT, register, new ModbusRegisterArray(1, 2, 3, 4), 0).isEmpty());
    }

    @Test
    public void testAddressIsRelativeToBlock() {
        Register register = register("max-power", 278, 2, U32, Units.WATT);
        ModbusRegisterArray registers = new ModbusRegisterArray(0, 0, 0x0000, 0x2710);

        State state = decode(register, registers, 276);

        assertEquals("10000 W", state.toString());
    }

    @Test
    public void testRegisterOutsideBlock() {
        Register register = register("max-power", 278, 2, U32, Units.WATT);

        assertTrue(RegisterDecoder.decode(CONTEXT, register, new ModbusRegisterArray(1), 278).isEmpty());
        assertTrue(RegisterDecoder.decode(CONTEXT, register, new ModbusRegisterArray(1, 2), 400).isEmpty());
    }

    private static State decode(Register register, ModbusRegisterArray registers) {
        return decode(register, registers, register.address());
    }

    private static State decode(Register register, ModbusRegisterArray registers, int blockStart) {
        List<ChannelUpdate> updates = RegisterDecoder.decode(CONTEXT, register, registers, blockStart);
        assertEquals(1, updates.size(), "expected exactly one channel update");
        return updates.get(0).state();
    }

    private static Register register(String channelId, int address, int length, DataType dataType,
            @Nullable Unit<?> unit) {
        return new Register(channelId, GROUP, address, length, dataType, READ_ONLY, unit, Map.of(), List.of());
    }
}
