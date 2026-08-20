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
package org.openhab.binding.modbus.sungrow.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
class SungrowIHomeManagerRegistersTest {

    @Test
    void testCreatePercentState() {
        SungrowIHomeManagerRegisters batteryLevelRegister = SungrowIHomeManagerRegisters.BATTERY_LEVEL;

        ModbusRegisterArray registers = new ModbusRegisterArray(1000);
        Optional<DecimalType> value = ModbusBitUtilities.extractStateFromRegisters( //
                registers, //
                0, //
                batteryLevelRegister.getType() //
        );
        assertTrue(value.isPresent());

        State state = batteryLevelRegister.createState(value.get());
        assertInstanceOf(QuantityType.class, state);
        assertEquals("100 %", state.toFullString());
    }

    @Test
    void testCreateScaledPowerState() {
        SungrowIHomeManagerRegisters totalActivePowerRegister = SungrowIHomeManagerRegisters.TOTAL_ACTIVE_POWER;

        State state = totalActivePowerRegister.createState(new DecimalType(123));
        assertInstanceOf(QuantityType.class, state);
        QuantityType<?> quantityType = (QuantityType<?>) state;
        QuantityType<?> wattValue = quantityType.toUnit(Units.WATT);
        assertNotNull(wattValue);
        assertEquals("1230 W", wattValue.toFullString());
    }

    @Test
    void testCreateChargerStatusState() {
        SungrowIHomeManagerRegisters chargerStatusRegister = SungrowIHomeManagerRegisters.CHARGER_STATUS;

        State state = chargerStatusRegister.createState(new DecimalType(3));
        assertInstanceOf(StringType.class, state);
        assertEquals("Charging", state.toFullString());
    }
}
