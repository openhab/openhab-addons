/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e3dc.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.e3dc.internal.dto.EmergencyBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.PowerBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.StringBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxArray;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxBlock;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;
import org.openhab.core.library.types.OnOffType;

/**
 * The {@link DataBlockTest} Test Data Transfer Objects of frequent Data Block
 *
 * @author Bernd Weymann - Initial contribution
 */
public class DataBlockTest {
    private Parser mc;
    private Parser mcNegativePVSupply;

    @BeforeEach
    public void setup() {
        {
            byte[] dataBlock = new byte[] { 0, -14, 0, 0, -2, -47, -1, -1, 2, 47, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 99, 99, 0, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 1, 125, 2, 21, 0, 0, 0, 27, 0, 26, 0, 0, 0, 103, 0, -117, 0, 0 };
            mc = new Parser(DataType.DATA);
            mc.setArray(dataBlock);
        }
        {
            // 65098 bytes [-2, 74]
            // 65535 bytes [-1, -1]
            byte[] dataBlock = new byte[] { -2, -74, -1, -1, -2, -47, -1, -1, 2, 47, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 99, 99, 0, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 1, 125, 2, 21, 0, 0, 0, 27, 0, 26, 0, 0, 0, 103, 0, -117, 0, 0 };
            mcNegativePVSupply = new Parser(DataType.DATA);
            mcNegativePVSupply.setArray(dataBlock);
        }
    }

    @Test
    public void testValidPowerBlock() {
        Optional<Data> dataOpt = mc.parse(DataType.POWER);
        assertTrue(dataOpt.isPresent());
        PowerBlock b = (PowerBlock) dataOpt.get();
        assertEquals("242 W", b.pvPowerSupply.toString(), "PV Supply");
        assertEquals("14 W", b.gridPowerSupply.toString(), "Grid Supply");
        assertEquals("0 W", b.gridPowerConsumpition.toString(), "Grid Consumption");
        assertEquals("303 W", b.batteryPowerSupply.toString(), "Battery Supply");
    }

    @Test
    public void testValidPowerBlockNegativePVSupply() {
        Optional<Data> dataOpt = mcNegativePVSupply.parse(DataType.POWER);
        assertTrue(dataOpt.isPresent());
        PowerBlock b = (PowerBlock) dataOpt.get();
        assertEquals("-330 W", b.pvPowerSupply.toString(), "PV Supply");
        assertEquals("14 W", b.gridPowerSupply.toString(), "Grid Supply");
        assertEquals("0 W", b.gridPowerConsumpition.toString(), "Grid Consumption");
        assertEquals("303 W", b.batteryPowerSupply.toString(), "Battery Supply");
    }

    @Test
    public void testValidWallboxBlock() {
        Optional<Data> wba = mc.parse(DataType.WALLBOX);
        assertTrue(wba.isPresent());
        WallboxArray a = (WallboxArray) wba.get();
        assertNotNull(a);
        Optional<WallboxBlock> o = a.getWallboxBlock(0);
        WallboxBlock b = o.get();
        assertNotNull(b);
        assertEquals(OnOffType.ON, b.wbAvailable, "Wallbox available");
        assertEquals(OnOffType.ON, b.wbSunmode, "Wallbox Sunmode");
        assertEquals(OnOffType.OFF, b.wb1phase, "Wallbox 1phase");
        assertEquals(OnOffType.OFF, b.wbCharging, "Wallbox charging");
    }

    @Test
    public void testValidEmergency() {
        Optional<Data> dataOpt = mc.parse(DataType.EMERGENCY);
        assertTrue(dataOpt.isPresent());
        EmergencyBlock b = (EmergencyBlock) dataOpt.get();
        assertEquals(EmergencyBlock.EP_NOT_SUPPORTED, b.epStatus.toFullString(), "EMS Status");
        assertEquals(OnOffType.OFF, b.batteryChargingLocked, "Battery charging locked");
        assertEquals(OnOffType.OFF, b.batteryDischargingLocked, "Battery discharging locked");
        assertEquals(OnOffType.OFF, b.epPossible, "EP possible");
        assertEquals(OnOffType.OFF, b.weatherPredictedCharging, "Weather Predicted charging");
        assertEquals(OnOffType.OFF, b.regulationStatus, "Regulation Status");
        assertEquals(OnOffType.OFF, b.chargeLockTime, "Charge Lock Time");
        assertEquals(OnOffType.OFF, b.dischargeLockTime, "Discharge Lock Time");
    }

    @Test
    public void testValidStringDetailsStringBlock() {
        Optional<Data> dataOpt = mc.parse(DataType.STRINGS);
        assertTrue(dataOpt.isPresent());
        StringBlock b = (StringBlock) dataOpt.get();
        assertEquals(381, b.string1Volt.intValue(), "String 1 V");
        assertEquals("V", b.string1Volt.getUnit().toString(), "String 1 V");
        assertEquals(533, b.string2Volt.intValue(), "String 2 V");
        assertEquals("V", b.string2Volt.getUnit().toString(), "String 1 V");
        assertEquals(0, b.string3Volt.intValue(), "String 3 V");
        assertEquals("V", b.string3Volt.getUnit().toString(), "String 1 V");

        assertEquals(0.27, b.string1Ampere.doubleValue(), 0.01, "String 1 A");
        assertEquals("A", b.string1Ampere.getUnit().toString(), "String 1 A");
        assertEquals(0.26, b.string2Ampere.doubleValue(), 0.01, "String 2 A");
        assertEquals("A", b.string2Ampere.getUnit().toString(), "String 2 A");
        assertEquals(0, b.string3Ampere.doubleValue(), 0.01, "String 3 A");
        assertEquals("A", b.string3Ampere.getUnit().toString(), "String 3 A");

        assertEquals(103, b.string1Watt.intValue(), "String 1 W");
        assertEquals("W", b.string1Watt.getUnit().toString(), "String 1 W");
        assertEquals(139, b.string2Watt.intValue(), "String 2 W");
        assertEquals("W", b.string2Watt.getUnit().toString(), "String 2 W");
        assertEquals(0, b.string3Watt.intValue(), "String 3 W");
        assertEquals("W", b.string3Watt.getUnit().toString(), "String 3 W");
    }

    @Test
    public void testInvalidInfoblock() {
        Optional<Data> infoOpt = mc.parse(DataType.INFO);
        assertFalse(infoOpt.isPresent());
    }
}
