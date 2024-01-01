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
import static org.openhab.binding.snmp.internal.SnmpBindingConstants.THING_TYPE_TARGET;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.snmp.internal.types.SnmpChannelMode;
import org.openhab.binding.snmp.internal.types.SnmpDatatype;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
@NonNullByDefault
public abstract class AbstractSnmpTargetHandlerTest extends JavaTest {
    protected static final ThingUID THING_UID = new ThingUID(THING_TYPE_TARGET, "testthing");
    protected static final ChannelUID CHANNEL_UID = new ChannelUID(THING_UID, "testchannel");
    protected static final String TEST_OID = "1.2.3.4";
    protected static final String TEST_ADDRESS = "192.168.0.1";
    protected static final String TEST_STRING = "foo.";

    protected @Mock @NonNullByDefault({}) SnmpServiceImpl snmpService;
    protected @Mock @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;

    protected @NonNullByDefault({}) Thing thing;
    protected @NonNullByDefault({}) SnmpTargetHandler thingHandler;
    private @NonNullByDefault({}) AutoCloseable mocks;

    @AfterEach
    public void after() throws Exception {
        mocks.close();
    }

    protected @Nullable VariableBinding handleCommandSwitchChannel(SnmpDatatype datatype, Command command,
            String onValue, @Nullable String offValue, boolean refresh) throws IOException {
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

    protected @Nullable VariableBinding handleCommandNumberStringChannel(ChannelTypeUID channelTypeUID,
            SnmpDatatype datatype, Command command, boolean refresh) throws IOException {
        return handleCommandNumberStringChannel(channelTypeUID, datatype, null, command, refresh);
    }

    protected @Nullable VariableBinding handleCommandNumberStringChannel(ChannelTypeUID channelTypeUID,
            SnmpDatatype datatype, @Nullable String unit, Command command, boolean refresh) throws IOException {
        setup(channelTypeUID, SnmpChannelMode.WRITE, datatype, null, null, null, unit);
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
                List.of(new VariableBinding(new OID(TEST_OID), new OctetString(TEST_STRING))));
        ResponseEvent event = new ResponseEvent("test", null, null, responsePDU, null);

        thingHandler.onResponse(event);

        if (refresh) {
            verify(thingHandlerCallback, atLeast(1)).stateUpdated(eq(CHANNEL_UID), eq(new StringType(TEST_STRING)));
        } else {
            verify(thingHandlerCallback, never()).stateUpdated(any(), any());
        }
    }

    protected @Nullable State onResponseSwitchChannel(SnmpChannelMode channelMode, SnmpDatatype datatype,
            String onValue, String offValue, Variable value, boolean refresh) {
        setup(SnmpBindingConstants.CHANNEL_TYPE_UID_SWITCH, channelMode, datatype, onValue, offValue);

        PDU responsePDU = new PDU(PDU.RESPONSE, List.of(new VariableBinding(new OID(TEST_OID), value)));
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

        verifyStatus(ThingStatus.UNKNOWN);
        verify(snmpService).addCommandResponder(any());

        if (refresh) {
            ArgumentCaptor<PDU> pduCaptor = ArgumentCaptor.forClass(PDU.class);
            verify(snmpService, timeout(500).atLeast(1)).send(pduCaptor.capture(), any(), eq(null), eq(thingHandler));
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

    protected void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode, @Nullable SnmpDatatype datatype) {
        setup(channelTypeUID, channelMode, datatype, null, null);
    }

    protected void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode, @Nullable SnmpDatatype datatype,
            @Nullable String onValue, @Nullable String offValue) {
        setup(channelTypeUID, channelMode, datatype, onValue, offValue, null);
    }

    protected void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode, @Nullable SnmpDatatype datatype,
            @Nullable String onValue, @Nullable String offValue, @Nullable String exceptionValue) {
        setup(channelTypeUID, channelMode, datatype, onValue, offValue, exceptionValue, null);
    }

    protected void setup(ChannelTypeUID channelTypeUID, SnmpChannelMode channelMode, @Nullable SnmpDatatype datatype,
            @Nullable String onValue, @Nullable String offValue, @Nullable String exceptionValue,
            @Nullable String unit) {
        Map<String, Object> channelConfig = new HashMap<>();
        Map<String, Object> thingConfig = new HashMap<>();
        mocks = MockitoAnnotations.openMocks(this);

        thingConfig.put("hostname", "localhost");

        ThingBuilder thingBuilder = ThingBuilder.create(THING_TYPE_TARGET, THING_UID).withLabel("Test thing")
                .withConfiguration(new Configuration(thingConfig));

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
        if (unit != null) {
            channelConfig.put("unit", unit);
        }
        Channel channel = ChannelBuilder.create(CHANNEL_UID, itemType).withType(channelTypeUID)
                .withConfiguration(new Configuration(channelConfig)).build();
        thingBuilder.withChannel(channel);

        thing = thingBuilder.build();
        thingHandler = new SnmpTargetHandler(thing, snmpService);

        thingHandler.getThing().setHandler(thingHandler);
        thingHandler.setCallback(thingHandlerCallback);

        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(thingHandlerCallback).statusUpdated(any(), any());

        thingHandler.initialize();

        verifyStatus(ThingStatus.UNKNOWN);
    }

    protected void verifyStatus(ThingStatus status) {
        waitForAssert(() -> assertEquals(status, thingHandler.getThing().getStatusInfo().getStatus()));
    }
}
