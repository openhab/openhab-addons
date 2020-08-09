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
package org.openhab.binding.modbus.e3dc.dto;

import static org.junit.Assert.*;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.modbus.e3dc.internal.dto.EmergencyBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.PowerBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.StringBlock;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxArray;
import org.openhab.binding.modbus.e3dc.internal.dto.WallboxBlock;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;

/**
 * The {@link DataBlockTest} Test Data Transfer Objects of frequent Data Block
 *
 * @author Bernd Weymann - Initial contribution
 */
public class DataBlockTest {
    private Parser mc;

    @Before
    public void setup() {
        byte[] dataBlock = new byte[] { 0, -14, 0, 0, -2, -47, -1, -1, 2, 47, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 99, 99, 0, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                125, 2, 21, 0, 0, 0, 27, 0, 26, 0, 0, 0, 103, 0, -117, 0, 0 };
        mc = new Parser(DataType.DATA);
        mc.setArray(dataBlock);
    }

    @Test
    public void testValidPowerBlock() {
        Optional<Data> dataOpt = mc.parse(DataType.POWER);
        assertTrue(dataOpt.isPresent());
        PowerBlock b = (PowerBlock) dataOpt.get();
        assertEquals("PV Supply", "242.0 W", b.pvPowerSupply.toString());
        assertEquals("Grid Supply", "14.0 W", b.gridPowerSupply.toString());
        assertEquals("Grid Consumption", "0.0 W", b.gridPowerConsumpition.toString());
        assertEquals("Battery Supply", "303.0 W", b.batteryPowerSupply.toString());
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
        assertEquals("Wallbox available", OnOffType.ON, b.wbAvailable);
        assertEquals("Wallbox Sunmode", OnOffType.ON, b.wbSunmode);
        assertEquals("Wallbox 1phase", OnOffType.OFF, b.wb1phase);
        assertEquals("Wallbox charging", OnOffType.OFF, b.wbCharging);
    }

    @Test
    public void testValidEmergency() {
        Optional<Data> dataOpt = mc.parse(DataType.EMERGENCY);
        assertTrue(dataOpt.isPresent());
        EmergencyBlock b = (EmergencyBlock) dataOpt.get();
        assertEquals("EMS Status", EmergencyBlock.EP_NOT_SUPPORTED, b.epStatus.toFullString());
        assertEquals("Battery charging locked", OnOffType.OFF, b.batteryChargingLocked);
        assertEquals("Battery discharging locked", OnOffType.OFF, b.batteryDischargingLocked);
        assertEquals("EP possible", OnOffType.OFF, b.epPossible);
        assertEquals("Weather Predicted charging", OnOffType.OFF, b.weatherPredictedCharging);
        assertEquals("Regulation Status", OnOffType.OFF, b.regulationStatus);
        assertEquals("Charge Lock Time", OnOffType.OFF, b.chargeLockTime);
        assertEquals("Discharge Lock Time", OnOffType.OFF, b.dischargeLockTime);
    }

    @Test
    public void testValidStringDetailsStringBlock() {
        Optional<Data> dataOpt = mc.parse(DataType.STRINGS);
        assertTrue(dataOpt.isPresent());
        StringBlock b = (StringBlock) dataOpt.get();
        assertEquals("String 1 V", 381, b.string1Volt.intValue());
        assertEquals("String 1 V", "V", b.string1Volt.getUnit().toString());
        assertEquals("String 2 V", 533, b.string2Volt.intValue());
        assertEquals("String 1 V", "V", b.string2Volt.getUnit().toString());
        assertEquals("String 3 V", 0, b.string3Volt.intValue());
        assertEquals("String 1 V", "V", b.string3Volt.getUnit().toString());

        assertEquals("String 1 A", 0.27, b.string1Ampere.doubleValue(), 0.01);
        assertEquals("String 1 A", "A", b.string1Ampere.getUnit().toString());
        assertEquals("String 2 A", 0.26, b.string2Ampere.doubleValue(), 0.01);
        assertEquals("String 2 A", "A", b.string2Ampere.getUnit().toString());
        assertEquals("String 3 A", 0, b.string3Ampere.doubleValue(), 0.01);
        assertEquals("String 3 A", "A", b.string3Ampere.getUnit().toString());

        assertEquals("String 1 W", 103, b.string1Watt.intValue());
        assertEquals("String 1 W", "W", b.string1Watt.getUnit().toString());
        assertEquals("String 2 W", 139, b.string2Watt.intValue());
        assertEquals("String 2 W", "W", b.string2Watt.getUnit().toString());
        assertEquals("String 3 W", 0, b.string3Watt.intValue());
        assertEquals("String 3 W", "W", b.string3Watt.getUnit().toString());
    }

    @Test
    public void testInvalidInfoblock() {
        Optional<Data> infoOpt = mc.parse(DataType.INFO);
        assertFalse(infoOpt.isPresent());
    }
}
