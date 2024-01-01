/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
class SungrowInverterRegistersTest {
    @Test
    public void testCreatePercentTypeState() {
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
        assertInstanceOf(PercentType.class, state);
        assertEquals("100.0", state.toFullString());
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
}
