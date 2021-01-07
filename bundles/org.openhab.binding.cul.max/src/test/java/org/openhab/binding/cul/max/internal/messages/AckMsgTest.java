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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.openhab.binding.cul.max.internal.messages.constants.ThermostatControlMode;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
@NonNullByDefault
class AckMsgTest {
    @Test
    public void checkSampleMessage() throws MaxCulProtocolException {
        AckMsg testling = new AckMsg("Z0E0102020CCBDD010203000119042B29");
        assertEquals(MaxCulMsgType.ACK, testling.msgType);
        assertEquals(ThermostatControlMode.MANUAL, testling.getControlMode());
        assertEquals(14, testling.len);
        assertEquals(4, testling.payload.length);
        assertEquals(1, testling.msgCount);
        assertEquals(0x2, testling.msgFlag);
        assertEquals("0CCBDD", testling.srcAddrStr);
        assertEquals("010203", testling.dstAddrStr);
        assertEquals(0, testling.groupid);
        assertEquals(false, testling.getIsNack());
        testling.printFormattedPayload();
    }

    @Test
    public void checkSampleMessage2() throws MaxCulProtocolException {
        AckMsg testling = new AckMsg("Z0E0302020CCBDD010203000119042A31"); // WALLMOUNT SHOW ACTUAL TEMP 21°
        assertEquals(MaxCulMsgType.ACK, testling.msgType);
        assertEquals(ThermostatControlMode.MANUAL, testling.getControlMode());
        assertEquals(14, testling.len);
        assertEquals(4, testling.payload.length);
        assertEquals(3, testling.msgCount);
        assertEquals(0x2, testling.msgFlag);
        assertEquals("0CCBDD", testling.srcAddrStr);
        assertEquals("010203", testling.dstAddrStr);
        assertEquals(0, testling.groupid);
        assertEquals(false, testling.getIsNack());
        testling.printFormattedPayload();
    }

    @Test
    public void checkSampleMessage3() throws MaxCulProtocolException {
        AckMsg testling = new AckMsg("Z0E040202072304010203000119002A33"); // VALVE
        assertEquals(MaxCulMsgType.ACK, testling.msgType);
        assertEquals(ThermostatControlMode.MANUAL, testling.getControlMode());
        assertEquals(14, testling.len);
        assertEquals(4, testling.payload.length);
        assertEquals(4, testling.msgCount);
        assertEquals(0x2, testling.msgFlag);
        assertEquals("072304", testling.srcAddrStr);
        assertEquals("010203", testling.dstAddrStr);
        assertEquals(0, testling.groupid);
        assertEquals(false, testling.getIsNack());
        testling.printFormattedPayload();
    }

    @Test

    public void checkSampleMessage4() throws MaxCulProtocolException {
        AckMsg testling = new AckMsg("Z0E0502020CCBDD010203000119002A31"); // WALLMOUNT SHOW DESIRED TEMP 21°
        assertEquals(MaxCulMsgType.ACK, testling.msgType);
        assertEquals(ThermostatControlMode.MANUAL, testling.getControlMode());
        assertEquals(14, testling.len);
        assertEquals(4, testling.payload.length);
        assertEquals(5, testling.msgCount);
        assertEquals(0x2, testling.msgFlag);
        assertEquals("0CCBDD", testling.srcAddrStr);
        assertEquals("010203", testling.dstAddrStr);
        assertEquals(0, testling.groupid);
        assertEquals(false, testling.getIsNack());
        testling.printFormattedPayload();
    }
}
