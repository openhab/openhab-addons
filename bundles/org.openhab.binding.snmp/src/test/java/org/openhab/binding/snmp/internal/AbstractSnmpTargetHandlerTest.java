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
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.java.JavaTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

/**
 * Tests cases for {@link SnmpTargetHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public abstract class AbstractSnmpTargetHandlerTest extends JavaTest {
    protected static final ThingUID THING_UID = new ThingUID(THING_TYPE_TARGET, "testthing");
    protected static final ChannelUID CHANNEL_UID = new ChannelUID(THING_UID, "testchannel");
    protected static final String TEST_OID = "1.2.3.4";
    protected static final String TEST_ADDRESS = "192.168.0.1";
    protected static final String TEST_STRING = "foo.";

    protected @Mock SnmpServiceImpl snmpService;
    protected @Mock ThingHandlerCallback thingHandlerCallback;

    protected Thing thing;
    protected SnmpTargetHandler thingHandler;

    protected VariableBinding handleCommandSwitchChannel(SnmpDatatype datatype, Command command, String onValue,
            String offValue, boolean refresh) throws IOException {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, SnmpChannelMode.WRITE, datatype, onValue, offValue);
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

    protected VariableBinding handleCommandNumberStringChannel(ChannelTypeUID channelTypeUID, SnmpDatatype datatype,
            Command command, boolean refresh) throws IOException {
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

    protected void onResponseNumberStringChannel(SnmpChannelMode channelMode, boolean refresh) {
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

    protected State onResponseSwitchChannel(SnmpChannelMode channelMode, SnmpDatatype datatype, String onValue,
            String offValue, Variable value, boolean refresh) {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, channelMode, datatype, onValue, offValue);

        PDU responsePDU = new PDU(PDU.RESPONSE,
                Collections.singletonList(new VariableBinding(new OID(TEST_OID), value)));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);

        thingHandler.onResponse(event);

        if (refresh) {
            ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
            verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), stateCaptor.capture());
            return stateCaptor.getValue();
        } else {
            verify(thingHandlerCallback, never()).stateUpdated(any(), any());
            return null;
        }
    }

    protected void refresh(SnmpChannelMode channelMode, boolean refresh) throws IOException {
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

    protected void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode) {
        setup(channelTypeUID, channelMode, null);
    }

    protected void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode, SnmpDatatype datatype) {
        setup(channelTypeUID, channelMode, datatype, null, null);
    }

    protected void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode, SnmpDatatype datatype,
            String onValue, String offValue) {
        setup(channelTypeUID, channelMode, datatype, onValue, offValue, null);
    }

    protected void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode, SnmpDatatype datatype,
            String onValue, String offValue, String exceptionValue) {
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
            if (onValue != null) {
                channelConfig.put("onvalue", onValue);
            }
            if (offValue != null) {
                channelConfig.put("offvalue", offValue);
            }
            if (exceptionValue != null) {
                channelConfig.put("exceptionValue", exceptionValue);
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

        waitForAssert(() -> assertEquals(ThingStatus.ONLINE, thingHandler.getThing().getStatusInfo().getStatus()));
    }
}
