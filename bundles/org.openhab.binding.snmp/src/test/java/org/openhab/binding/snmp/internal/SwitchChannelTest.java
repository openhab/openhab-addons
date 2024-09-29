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
package org.openhab.binding.snmp.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.snmp.internal.types.SnmpChannelMode;
import org.openhab.binding.snmp.internal.types.SnmpDatatype;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

/**
 * Tests cases for {@link SnmpTargetHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SwitchChannelTest extends AbstractSnmpTargetHandlerTest {

    @Test
    public void testCommandsAreProperlyHandledBySwitchChannel() throws IOException {
        VariableBinding variable;

        variable = handleCommandSwitchChannel(SnmpDatatype.STRING, OnOffType.ON, "on", "off", true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals("on", ((OctetString) variable.getVariable()).toString());

        variable = handleCommandSwitchChannel(SnmpDatatype.STRING, OnOffType.OFF, "on", "off", true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals("off", ((OctetString) variable.getVariable()).toString());

        variable = handleCommandSwitchChannel(SnmpDatatype.STRING, OnOffType.OFF, "on", null, false);
        assertNull(variable);
    }

    @Test
    public void testSwitchChannelsProperlyUpdatingOnValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.STRING, "on", "off");
        PDU responsePDU = new PDU(PDU.RESPONSE, List.of(new VariableBinding(new OID(TEST_OID), new OctetString("on"))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verifyResponse(OnOffType.ON);
    }

    @Test
    public void testSwitchChannelsProperlyUpdatingOffValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.INT32, "0", "3");
        PDU responsePDU = new PDU(PDU.RESPONSE, List.of(new VariableBinding(new OID(TEST_OID), new Integer32(3))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verifyResponse(OnOffType.OFF);
    }

    @Test
    public void testSwitchChannelsProperlyUpdatingHexValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.HEXSTRING, "AA bb 11",
                "cc ba 1d");
        PDU responsePDU = new PDU(PDU.RESPONSE,
                List.of(new VariableBinding(new OID(TEST_OID), OctetString.fromHexStringPairs("aabb11"))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verifyResponse(OnOffType.ON);
    }

    @Test
    public void testSwitchChannelsIgnoresArbitraryValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.COUNTER64, "0", "12223");
        PDU responsePDU = new PDU(PDU.RESPONSE, List.of(new VariableBinding(new OID(TEST_OID), new Counter64(17))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, never()).stateUpdated(eq(CHANNEL_UID), any());
        verifyStatus(ThingStatus.ONLINE);
    }

    @Test
    public void testSwitchChannelSendsUndefExceptionValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.COUNTER64, "0", "12223");
        PDU responsePDU = new PDU(PDU.RESPONSE, List.of(new VariableBinding(new OID(TEST_OID), Null.noSuchInstance)));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verifyResponse(UnDefType.UNDEF);
    }

    @Test
    public void testSwitchChannelSendsConfiguredExceptionValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.READ, SnmpDatatype.COUNTER64, "0", "12223",
                "OFF");
        PDU responsePDU = new PDU(PDU.RESPONSE, List.of(new VariableBinding(new OID(TEST_OID), Null.noSuchInstance)));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verifyResponse(OnOffType.OFF);
    }

    private void verifyResponse(State expectedState) {
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(expectedState));
        verifyStatus(ThingStatus.ONLINE);
    }
}
