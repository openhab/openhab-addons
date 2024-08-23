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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.snmp.internal.types.SnmpChannelMode;
import org.openhab.binding.snmp.internal.types.SnmpDatatype;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

/**
 * Tests cases for {@link SnmpTargetHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class StringChannelTest extends AbstractSnmpTargetHandlerTest {

    @Test
    public void testCommandsAreProperlyHandledByStringChannel() throws IOException {
        VariableBinding variable;

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING, SnmpDatatype.STRING,
                new StringType(TEST_STRING), true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

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
                SnmpDatatype.HEXSTRING, new StringType("AA bf 11"), true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals("aa bf 11", ((OctetString) variable.getVariable()).toHexString(' '));

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING,
                SnmpDatatype.IPADDRESS, new StringType(TEST_ADDRESS), true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof IpAddress);
        assertEquals(TEST_ADDRESS, ((IpAddress) variable.getVariable()).toString());
    }

    @Test
    public void testStringChannelsProperlyUpdatingOnHexString() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING, SnmpChannelMode.READ, SnmpDatatype.HEXSTRING);
        PDU responsePDU = new PDU(PDU.RESPONSE,
                List.of(new VariableBinding(new OID(TEST_OID), OctetString.fromHexStringPairs("aa11bb"))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(new StringType("aa 11 bb")));
        verifyStatus(ThingStatus.ONLINE);
    }
}
