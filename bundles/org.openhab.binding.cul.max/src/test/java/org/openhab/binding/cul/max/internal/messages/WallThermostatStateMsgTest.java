/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.messages;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.openhab.binding.cul.max.internal.messages.constants.ThermostatControlMode;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
@NonNullByDefault
class WallThermostatStateMsgTest {
    // 17:36
    @Test
    public void checkSampleMessage() throws MaxCulProtocolException {
        WallThermostatStateMsg testling = new WallThermostatStateMsg("Z0FCF04700CCBDD0000000018042A00EA30");
        assertEquals(MaxCulMsgType.WALL_THERMOSTAT_STATE, testling.msgType);
        assertEquals(15, testling.len);
        assertEquals(5, testling.payload.length);
        assertEquals(0, testling.groupid);
        assertEquals(-49, testling.msgCount);
        assertEquals(0x4, testling.msgFlag);
        assertEquals("0CCBDD", testling.srcAddrStr);
        assertEquals("000000", testling.dstAddrStr);
        assertEquals(21.0, testling.getDesiredTemperature());
        assertEquals(23.4, testling.getMeasuredTemperature());
        assertEquals(ThermostatControlMode.AUTO, testling.getControlMode());
        assertEquals(false, testling.isLockedForManualSetPoint());
        assertEquals(false, testling.isBatteryLow());
    }

    @Test // 05.01.2021 23:00 17C° Mode TEMPORARY
    public void checkSampleMessage2() throws MaxCulProtocolException {
        WallThermostatStateMsg testling = new WallThermostatStateMsg("Z104404700CCBDD000000005A002205952E19");
        assertEquals(MaxCulMsgType.WALL_THERMOSTAT_STATE, testling.msgType);
        assertEquals(16, testling.len);
        assertEquals(6, testling.payload.length);
        assertEquals(0, testling.groupid);
        assertEquals(68, testling.msgCount);
        assertEquals(0x4, testling.msgFlag);
        assertEquals("0CCBDD", testling.srcAddrStr);
        assertEquals("000000", testling.dstAddrStr);
        assertEquals(17.0, testling.getDesiredTemperature());
        assertEquals(null, testling.getMeasuredTemperature());
        assertEquals(ThermostatControlMode.TEMPORARY, testling.getControlMode());
        assertEquals(false, testling.isLockedForManualSetPoint());
        assertEquals(false, testling.isBatteryLow());
    }

    // Z105B04700CCBDD000000005A002227150924 //07.01.2021 4:30 17C° Mode TEMPORARY
}
