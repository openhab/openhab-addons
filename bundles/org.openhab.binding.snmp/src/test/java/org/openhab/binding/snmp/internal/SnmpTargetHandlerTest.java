/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.snmp.internal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.VariableBinding;

/**
 * Tests cases for {@link SnmpTargetHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class SnmpTargetHandlerTest extends AbstractSnmpTargetHandlerTest {

    @Test
    public void testChannelsProperlyRefreshing() throws IOException {
        refresh(SnmpChannelMode.READ, true);
        refresh(SnmpChannelMode.READ_WRITE, true);
        refresh(SnmpChannelMode.WRITE, false);
        refresh(SnmpChannelMode.TRAP, false);
    }

    @Test
    public void testChannelsProperlyUpdate() throws IOException {
        onResponseNumberStringChannel(SnmpChannelMode.READ, true);
        onResponseNumberStringChannel(SnmpChannelMode.READ_WRITE, true);
        onResponseNumberStringChannel(SnmpChannelMode.WRITE, false);
        onResponseNumberStringChannel(SnmpChannelMode.TRAP, false);
        assertEquals(OnOffType.ON, onResponseSwitchChannel(SnmpChannelMode.READ, SnmpDatatype.STRING, "on", "off",
                new OctetString("on"), true));
        assertEquals(OnOffType.OFF, onResponseSwitchChannel(SnmpChannelMode.READ_WRITE, SnmpDatatype.INT32, "1", "2",
                new Integer32(2), true));
        assertNull(onResponseSwitchChannel(SnmpChannelMode.WRITE, SnmpDatatype.STRING, "on", "off",
                new OctetString("on"), false));
        assertNull(
                onResponseSwitchChannel(SnmpChannelMode.TRAP, SnmpDatatype.INT32, "1", "2", new Integer32(2), false));

    }

    @Test
    public void testCommandsAreProperlyHandledByNumberChannel() throws IOException {
        VariableBinding variable;
        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.INT32,
                new DecimalType(-5), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof Integer32);
        assertEquals(-5, ((Integer32) variable.getVariable()).toInt());

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.UINT32,
                new DecimalType(10000), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof UnsignedInteger32);
        assertEquals(10000, ((UnsignedInteger32) variable.getVariable()).toInt());

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER,
                SnmpDatatype.COUNTER64, new DecimalType(10000), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof Counter64);
        assertEquals(10000, ((Counter64) variable.getVariable()).toInt());

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.FLOAT,
                new DecimalType("12.4"), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals("12.4", variable.getVariable().toString());

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.INT32,
                new StringType(TEST_STRING), false);
        assertNull(variable);

    }

    @Test
    public void testCommandsAreProperlyHandledByStringChannel() throws IOException {
        VariableBinding variable;

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING, SnmpDatatype.STRING,
                new StringType(TEST_STRING), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals(TEST_STRING, ((OctetString) variable.getVariable()).toString());

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING,
                SnmpDatatype.IPADDRESS, new StringType(TEST_STRING), false);
        assertNull(variable);

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING,
                SnmpDatatype.IPADDRESS, new DecimalType(-5), false);
        assertNull(variable);

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING,
                SnmpDatatype.IPADDRESS, new StringType(TEST_ADDRESS), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof IpAddress);
        assertEquals(TEST_ADDRESS, ((IpAddress) variable.getVariable()).toString());
    }

    @Test
    public void testCommandsAreProperlyHandledBySwitchChannel() throws IOException {
        VariableBinding variable;

        variable = handleCommandSwitchChannel(SnmpDatatype.STRING, OnOffType.ON, "on", "off", true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals("on", ((OctetString) variable.getVariable()).toString());

        variable = handleCommandSwitchChannel(SnmpDatatype.STRING, OnOffType.OFF, "on", "off", true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals("off", ((OctetString) variable.getVariable()).toString());

        variable = handleCommandSwitchChannel(SnmpDatatype.STRING, OnOffType.OFF, "on", null, false);
        assertNull(variable);
    }

    @Test
    public void testSwitchChannelsProperlyUpdatingOnValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.STRING, "on", "off");
        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), new OctetString("on"))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(OnOffType.ON));
    }

    @Test
    public void testSwitchChannelsProperlyUpdatingOffValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.INT32, "0", "3");
        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), new Integer32(3))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(OnOffType.OFF));
    }

    @Test
    public void testSwitchChannelsIgnoresArbitraryValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.COUNTER64, "0", "12223");
        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), new Counter64(17))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, never()).stateUpdated(eq(CHANNEL_UID), any());
    }

    @Test
    public void testNumberChannelsProperlyUpdatingFloatValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpChannelMode.READ, SnmpDatatype.FLOAT);
        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), new OctetString("12.4"))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(new DecimalType("12.4")));
    }

    @Test
    public void testSwitchChannelSendsUndefExceptionValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.COUNTER64, "0", "12223");
        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), Null.noSuchInstance)));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(UnDefType.UNDEF));
    }

    @Test
    public void testSwitchChannelSendsConfiguredExceptionValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.COUNTER64, "0", "12223",
                "OFF");
        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), Null.noSuchInstance)));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(OnOffType.OFF));
    }
}
