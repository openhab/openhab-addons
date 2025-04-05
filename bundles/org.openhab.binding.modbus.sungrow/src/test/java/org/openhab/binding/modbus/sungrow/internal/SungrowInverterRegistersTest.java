/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
class SungrowInverterRegistersTest {

    @Test
    public void testCreatePercentState() {
        SungrowInverterRegisters batteryLevelRegister = SungrowInverterRegisters.BATTERY_LEVEL;

        ModbusRegisterArray registers = new ModbusRegisterArray(1000);
        Optional<DecimalType> value = ModbusBitUtilities.extractStateFromRegisters( //
                registers, //
                0, //
                batteryLevelRegister.getType() //
        );
        assertTrue(value.isPresent());
        DecimalType decimalTypeValue = value.get();
        // Value is not scaled yet
        assertEquals(BigDecimal.valueOf(1000), decimalTypeValue.toBigDecimal());

        State state = batteryLevelRegister.createState(decimalTypeValue);
        assertInstanceOf(QuantityType.class, state);
        assertEquals("100 %", state.toFullString());
    }

    @Test
    public void testCreateQuantityTypeState() {
        SungrowInverterRegisters mpttVoltage = SungrowInverterRegisters.MPPT1_VOLTAGE;

        ModbusRegisterArray registers = new ModbusRegisterArray(1234);
        Optional<DecimalType> value = ModbusBitUtilities.extractStateFromRegisters( //
                registers, //
                0, //
                mpttVoltage.getType() //
        );
        assertTrue(value.isPresent());
        DecimalType decimalTypeValue = value.get();
        // Value is not scaled yet
        assertEquals(BigDecimal.valueOf(1234), decimalTypeValue.toBigDecimal());

        State state = mpttVoltage.createState(decimalTypeValue);
        assertInstanceOf(QuantityType.class, state);
        assertEquals("123.4 V", state.toFullString());
    }

    @Test
    public void testCreateDeviceTypeState() {
        SungrowInverterRegisters deviceType = SungrowInverterRegisters.DEVICE_TYPE;
        int valueFromHex = Integer.parseInt("E25", 16);
        ModbusRegisterArray registers = new ModbusRegisterArray(valueFromHex);
        Optional<DecimalType> value = ModbusBitUtilities.extractStateFromRegisters(registers, 0, deviceType.getType());
        assertTrue(value.isPresent());
        DecimalType decimalTypeValue = value.get();
        // Value is not scaled yet
        assertEquals(BigDecimal.valueOf(valueFromHex), decimalTypeValue.toBigDecimal());

        State state = deviceType.createState(decimalTypeValue);
        assertInstanceOf(StringType.class, state);
        assertEquals("SH15T-V11", state.toFullString());
    }

    @ParameterizedTest
    @EnumSource(value = SungrowInverterRegisters.class, names = { "POWER_FLOW_STATUS_PV_POWER",
            "POWER_FLOW_STATUS_BATTERY_CHARGING", "POWER_FLOW_STATUS_FEED_IN_POWER",
            "POWER_FLOW_STATUS_NEGATIVE_LOAD_POWER" })
    public void testCreatePowerFlowStatesOn(SungrowInverterRegisters register) {
        State state = getPowerFlowState(register);
        assertEquals(OnOffType.ON, state);
    }

    @ParameterizedTest
    @EnumSource(value = SungrowInverterRegisters.class, names = { "POWER_FLOW_STATUS_BATTERY_DISCHARGING",
            "POWER_FLOW_STATUS_POSITIVE_LOAD_POWER", "POWER_FLOW_STATUS_IMPORT_FROM_GRID" })
    public void testCreatePowerFlowStatesOff(SungrowInverterRegisters register) {
        State state = getPowerFlowState(register);
        assertEquals(OnOffType.OFF, state);
    }

    private static State getPowerFlowState(SungrowInverterRegisters register) {
        final String POWERFLOW_STATE = "10010011";
        int valueFromHex = Integer.parseInt(POWERFLOW_STATE, 2); // the second bit from the right is bit with index 1
        ModbusRegisterArray registers = new ModbusRegisterArray(valueFromHex);
        Optional<DecimalType> value = ModbusBitUtilities.extractStateFromRegisters(registers, 0, register.getType());
        assertTrue(value.isPresent());
        DecimalType decimalTypeValue = value.get();
        // Value is not scaled yet
        assertEquals(BigDecimal.valueOf(valueFromHex), decimalTypeValue.toBigDecimal());

        State state = register.createState(decimalTypeValue);
        assertInstanceOf(OnOffType.class, state);
        return state;
    }
}
