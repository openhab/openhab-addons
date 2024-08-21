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
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.snmp.internal.types.SnmpChannelMode;
import org.openhab.binding.snmp.internal.types.SnmpDatatype;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.VariableBinding;

/**
 * Tests cases for {@link SnmpTargetHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SnmpTargetHandlerTest extends AbstractSnmpTargetHandlerTest {

    @Test
    public void testChannelsProperlyRefreshing() throws IOException {
        refresh(SnmpChannelMode.READ, true);
        refresh(SnmpChannelMode.READ_WRITE, true);
        refresh(SnmpChannelMode.WRITE, false);
        refresh(SnmpChannelMode.TRAP, false);
    }

    @Test
    public void testChannelsProperlyUpdate() {
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
        verifyStatus(ThingStatus.ONLINE);
    }

    @Test
    public void testCommandsAreProperlyHandledByNumberChannel() throws IOException {
        VariableBinding variable;
        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.INT32,
                new DecimalType(-5), true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof Integer32);
        assertEquals(-5, ((Integer32) variable.getVariable()).toInt());

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.UINT32,
                new DecimalType(10000), true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof UnsignedInteger32);
        assertEquals(10000, ((UnsignedInteger32) variable.getVariable()).toInt());

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER,
                SnmpDatatype.COUNTER64, new DecimalType(10000), true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof Counter64);
        assertEquals(10000, ((Counter64) variable.getVariable()).toInt());

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.FLOAT,
                new DecimalType("12.4"), true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals("12.4", variable.getVariable().toString());

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.FLOAT,
                "°C", new QuantityType<>("50 °F"), true);

        if (variable == null) {
            fail("'variable' is null");
            return;
        }

        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals("10.00", variable.getVariable().toString().substring(0, 5));

        variable = handleCommandNumberStringChannel(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.INT32,
                null, new StringType(TEST_STRING), false);
        assertNull(variable);
    }

    @Test
    public void testNumberChannelsProperlyUpdatingFloatValue() throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpChannelMode.READ, SnmpDatatype.FLOAT);
        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), new OctetString("12.4"))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(new DecimalType("12.4")));
        verifyStatus(ThingStatus.ONLINE);
    }

    @Test
    public void testCancelingAsyncRequest() {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpChannelMode.READ, SnmpDatatype.FLOAT);
        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), new OctetString("12.4"))));

        SnmpMock source = new SnmpMock();

        ResponseEvent event = new ResponseEvent(source, null, null, responsePDU, null);

        thingHandler.onResponse(event);
        assertEquals(1, source.cancelCallCounter);
        verifyStatus(ThingStatus.ONLINE);
    }

    static class SnmpMock extends Snmp {
        public int cancelCallCounter = 0;

        @Override
        public void cancel(@Nullable PDU request, org.snmp4j.event.@Nullable ResponseListener listener) {
            ++cancelCallCounter;
        }
    }
}
