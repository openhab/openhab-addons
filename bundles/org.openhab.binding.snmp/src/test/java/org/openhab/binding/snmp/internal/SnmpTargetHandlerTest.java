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
import static org.openhab.binding.snmp.internal.SnmpBindingConstants.THING_TYPE_TARGET;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.VariableBinding;

/**
 * Tests cases for {@link SnmpTargetHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class SnmpTargetHandlerTest extends JavaTest {
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_TARGET, "testthing");
    private static final ChannelUID CHANNEL_UID = new ChannelUID(THING_UID, "testchannel");
    private static final String TEST_OID = "1.2.3.4";
    private static final String TEST_ADDRESS = "192.168.0.1";
    private static final String TEST_STRING = "foo";

    private @Mock SnmpServiceImpl snmpService;
    private @Mock ThingHandlerCallback thingHandlerCallback;

    private Thing thing;
    private SnmpTargetHandler thingHandler;

    @Test
    public void testInitializationWithoutChannelsEndsWithUnknown() {
        setup(null, null);
        waitForAssert(() -> assertEquals(ThingStatus.ONLINE, thingHandler.getThing().getStatusInfo().getStatus()));
    }

    @Test
    public void testChannelsProperlyRefreshing() throws IOException {
        basetestChannelIsRefreshing(SnmpChannelMode.READ, true);
        basetestChannelIsRefreshing(SnmpChannelMode.READ_WRITE, true);
        basetestChannelIsRefreshing(SnmpChannelMode.WRITE, false);
        basetestChannelIsRefreshing(SnmpChannelMode.TRAP, false);
    }

    @Test
    public void testChannelsProperlyUpdate() throws IOException {
        basetestCommandUpdatesChannel(SnmpChannelMode.READ, true);
        basetestCommandUpdatesChannel(SnmpChannelMode.READ_WRITE, true);
        basetestCommandUpdatesChannel(SnmpChannelMode.WRITE, false);
        basetestCommandUpdatesChannel(SnmpChannelMode.TRAP, false);
    }

    @Test
    public void testCommandsAreProperlyHandledByNumberChannel() throws IOException {
        VariableBinding variable;
        variable = basetestHandleCommand(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.INT32,
                new DecimalType(-5), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof Integer32);
        assertEquals(-5, ((Integer32) variable.getVariable()).toInt());

        variable = basetestHandleCommand(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.UINT32,
                new DecimalType(10000), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof UnsignedInteger32);
        assertEquals(10000, ((UnsignedInteger32) variable.getVariable()).toInt());

        variable = basetestHandleCommand(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.COUNTER64,
                new DecimalType(10000), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof Counter64);
        assertEquals(10000, ((Counter64) variable.getVariable()).toInt());

        variable = basetestHandleCommand(SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER, SnmpDatatype.INT32,
                new StringType(TEST_STRING), false);
        assertNull(variable);
    }

    @Test
    public void testCommandsAreProperlyHandledByStringChannel() throws IOException {
        VariableBinding variable;
        variable = basetestHandleCommand(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING, SnmpDatatype.STRING,
                new DecimalType(-5), false);
        assertNull(variable);

        variable = basetestHandleCommand(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING, SnmpDatatype.STRING,
                new StringType(TEST_STRING), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof OctetString);
        assertEquals(TEST_STRING, ((OctetString) variable.getVariable()).toString());

        variable = basetestHandleCommand(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING, SnmpDatatype.IPADDRESS,
                new StringType(TEST_STRING), false);
        assertNull(variable);

        variable = basetestHandleCommand(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING, SnmpDatatype.IPADDRESS,
                new StringType(TEST_ADDRESS), true);
        assertEquals(new OID(TEST_OID), variable.getOid());
        assertTrue(variable.getVariable() instanceof IpAddress);
        assertEquals(TEST_ADDRESS, ((IpAddress) variable.getVariable()).toString());
    }

    private VariableBinding basetestHandleCommand(ChannelTypeUID channelTypeUID, SnmpDatatype datatype, Command command,
            boolean refresh) throws IOException {
        setup(channelTypeUID, SnmpChannelMode.WRITE, datatype);
        thingHandler.handleCommand(CHANNEL_UID, command);

        if (refresh) {
            ArgumentCaptor<PDU> pduCaptor = ArgumentCaptor.forClass(PDU.class);
            verify(snmpService, times(1)).send(pduCaptor.capture(), any(), eq(null), eq(thingHandler));
            return pduCaptor.getValue().getVariableBindings().stream().findFirst().orElse(null);
        } else {
            verify(snmpService, never()).send(any(), any(), eq(null), eq(thingHandler));
            return null;
        }
    }

    private void basetestCommandUpdatesChannel(SnmpChannelMode channelMode, boolean refresh) {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING, channelMode);

        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), new OctetString(TEST_STRING))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);

        thingHandler.onResponse(event);

        if (refresh) {
            verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(new StringType(TEST_STRING)));
        } else {
            verify(thingHandlerCallback, never()).stateUpdated(any(), any());
        }
    }

    private void basetestChannelIsRefreshing(SnmpChannelMode channelMode, boolean refresh) throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_STRING, channelMode);

        waitForAssert(() -> assertEquals(ThingStatus.ONLINE, thingHandler.getThing().getStatusInfo().getStatus()));
        verify(snmpService).addCommandResponder(any());

        if (refresh) {
            ArgumentCaptor<PDU> pduCaptor = ArgumentCaptor.forClass(PDU.class);
            verify(snmpService, atLeast(1)).send(pduCaptor.capture(), any(), eq(null), eq(thingHandler));
            Vector<? extends VariableBinding> variables = pduCaptor.getValue().getVariableBindings();
            assertTrue(variables.stream().filter(v -> v.getOid().toDottedString().equals(TEST_OID)).findFirst()
                    .isPresent());
        } else {
            verify(snmpService, never()).send(any(), any(), eq(null), eq(thingHandler));
        }
    }

    private void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode) {
        setup(channelTypeUID, channelMode, null);
    }

    private void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode, SnmpDatatype datatype) {
        Map<String, Object> channelConfig = new HashMap<>();
        Map<String, Object> thingConfig = new HashMap<>();
        MockitoAnnotations.initMocks(this);

        thingConfig.put("hostname", "localhost");

        ThingBuilder thingBuilder = ThingBuilder.create(THING_TYPE_TARGET, THING_UID).withLabel("Test thing")
                .withConfiguration(new Configuration(thingConfig));

        if (channelTypeUID != null && channelMode != null) {
            String itemType = SnmpBindingConstants.CHANNEL_TYPE_UID_NUMBER.equals(channelTypeUID) ? "Number" : "String";
            channelConfig.put("oid", TEST_OID);
            channelConfig.put("mode", channelMode.name());
            if (datatype != null) {
                channelConfig.put("datatype", datatype.name());
            }
            Channel channel = ChannelBuilder.create(CHANNEL_UID, itemType).withType(channelTypeUID)
                    .withConfiguration(new Configuration(channelConfig)).build();
            thingBuilder.withChannel(channel);
        }

        thing = thingBuilder.build();
        thingHandler = new SnmpTargetHandler(thing, snmpService);

        thingHandler.getThing().setHandler(thingHandler);
        thingHandler.setCallback(thingHandlerCallback);

        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(thingHandlerCallback).statusUpdated(any(), any());

        thingHandler.initialize();
    }
}
