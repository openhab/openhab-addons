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
package org.openhab.binding.modbus.e3dc.modbus;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;

/**
 * The {@link ModbusCallbackTest} Tests for ModbusCallbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ModbusCallbackTest {

    @Test
    public void testDebugNames() {
        Parser mcInfo = new Parser(DataType.INFO);
        assertEquals("Debug Name Info", "org.openhab.binding.modbus.e3dc.internal.modbus.Parser:INFO",
                mcInfo.toString());

        Parser mcPower = new Parser(DataType.DATA);
        assertEquals("Debug Name Power", "org.openhab.binding.modbus.e3dc.internal.modbus.Parser:DATA",
                mcPower.toString());
    }
    /*
     * @Test
     * public void testFullCallbacks() {
     * Parser mcPower = new Parser(DataType.DATA);
     * assertEquals("Debug Name Data", "org.openhab.binding.e3dc.internal.modbus.ModbusCallback:DATA",
     * mcPower.toString());
     * DataListenerMock listener = new DataListenerMock(DataType.INFO);
     * mcPower.addDataListener(listener);
     * assertEquals("AddLister callback received", 1, listener.getCallCounter());
     * byte[] dataBlock = new byte[] { 0, -14, 0, 0, -2, -47, -1, -1, 2, 47, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
     * 0, 0, 0, 0, 99, 99, 0, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
     * 125, 2, 21, 0, 0, 0, 27, 0, 26, 0, 0, 0, 103, 0, -117, 0, 0 };
     * mcPower.setArray(dataBlock);
     * assertEquals("Update data callback received", 2, listener.getCallCounter());
     * PowerBlock pb = (PowerBlock) mcPower.parse(DataType.POWER);
     * assertNotNull(pb);
     * assertEquals("Battery Supply", "303 W", pb.batteryPowerSupply.toString());
     *
     * StringBlock sb = (StringBlock) mcPower.parse(DataType.STRINGS);
     * assertNotNull(sb);
     * assertEquals("String 2 V", 533, sb.string2Volt.intValue());
     *
     * EmergencyBlock eb = (EmergencyBlock) mcPower.parse(DataType.EMERGENCY);
     * assertNotNull(eb);
     * assertEquals("EMS Status", EmergencyBlock.EP_NOT_SUPPORTED, eb.epStatus.toFullString());
     *
     * WallboxArray wa = (WallboxArray) mcPower.parse(DataType.WALLBOX);
     * assertNotNull(wa);
     * WallboxBlock wb = wa.getWallboxBlock(0);
     * assertNotNull(wb);
     * assertEquals("Wallbox Sunmode", OnOffType.ON, wb.wbSunmode);
     * assertEquals("Wallbox 1phase", OnOffType.OFF, wb.wb1phase);
     * }
     */
}
