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
package org.openhab.binding.snmp.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.snmp.internal.types.SnmpChannelMode;
import org.openhab.binding.snmp.internal.types.SnmpDatatype;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ThingStatus;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Opaque;
import org.snmp4j.smi.VariableBinding;

/**
 * Tests cases for {@link SnmpTargetHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class NumberChannelTest extends AbstractSnmpTargetHandlerTest {

    @Test
    public void testNumberChannelsProperlyUpdatingOnOpaque() {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpChannelMode.READ, SnmpDatatype.FLOAT);
        PDU responsePDU = new PDU(PDU.RESPONSE, List.of(new VariableBinding(new OID(TEST_OID),
                new Opaque(new byte[] { (byte) 0x9f, 0x78, 0x04, 0x41, 0x5b, 0x33, 0x33 }))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        final ArgumentCaptor<DecimalType> captor = ArgumentCaptor.forClass(DecimalType.class);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), captor.capture());
        assertEquals(13.7, captor.getValue().doubleValue(), 0.001);
        verifyStatus(ThingStatus.ONLINE);
    }

    @Test
    public void testNumberChannelsProperlyUpdatingOnInteger() {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpChannelMode.READ, SnmpDatatype.COUNTER64);
        PDU responsePDU = new PDU(PDU.RESPONSE,
                List.of(new VariableBinding(new OID(TEST_OID), new Counter64(1234567891333L))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(new DecimalType(1234567891333L)));
        verifyStatus(ThingStatus.ONLINE);
    }

    @Test
    public void testNumberChannelsProperlyUpdatingOnQuantityType() {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpChannelMode.READ, SnmpDatatype.FLOAT, null, null, null,
                "°C");
        PDU responsePDU = new PDU(PDU.RESPONSE, List.of(new VariableBinding(new OID(TEST_OID),
                new Opaque(new byte[] { (byte) 0x9f, 0x78, 0x04, 0x41, 0x5b, 0x33, 0x33 }))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);
        thingHandler.onResponse(event);
        final ArgumentCaptor<QuantityType<?>> captor = ArgumentCaptor.forClass(QuantityType.class);
        verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), captor.capture());
        assertEquals(13.7, captor.getValue().doubleValue(), 0.001);
        assertEquals(SIUnits.CELSIUS, captor.getValue().getUnit());
        verifyStatus(ThingStatus.ONLINE);
    }
}
