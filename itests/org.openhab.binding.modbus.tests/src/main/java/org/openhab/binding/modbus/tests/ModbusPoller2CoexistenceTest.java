/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.tests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.modbus.handler.ModbusPoller2ThingHandler;
import org.openhab.binding.modbus.handler.ModbusPollerThingHandler;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusFailureCallback;
import org.openhab.core.io.transport.modbus.ModbusReadCallback;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

/**
 * Verifies that poller2 instances sharing an endpoint retain independent callbacks and health.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault({})
public class ModbusPoller2CoexistenceTest extends AbstractModbusOSGiTest {

    private static final String HOST = "thisishost";
    private static final int PORT = 44;

    private Bridge endpoint;

    private @Mock ThingHandlerCallback firstCallback;
    private @Mock ThingHandlerCallback secondCallback;

    @BeforeEach
    public void setUp() {
        mockCommsToModbusManager();
        Configuration endpointConfiguration = new Configuration();
        endpointConfiguration.put("host", HOST);
        endpointConfiguration.put("port", PORT);
        endpointConfiguration.put("id", 9);
        endpoint = ModbusPollerThingHandlerTest.createTcpThingBuilder("shared-endpoint")
                .withConfiguration(endpointConfiguration).build();
        addThing(endpoint);
        assertThat(endpoint.getStatus(), is(equalTo(ThingStatus.ONLINE)));
    }

    @Test
    public void testOverlappingPoller2ReadsShareEndpointButDeliverOnlyOwnResults() throws ReflectiveOperationException {
        Poller2Fixture first = createPoller("first", 100, 2, 101);
        Poller2Fixture second = createPoller("second", 101, 2, 102);

        // The endpoint owns one communication interface; both overlapping refreshes enter that same queue boundary.
        first.handler().refresh();
        second.handler().refresh();
        verify(comms, times(2)).submitOneTimePoll(any(), any(), any());

        first.handler().setCallback(firstCallback);
        second.handler().setCallback(secondCallback);
        readCallback(first.handler()).handle(new AsyncModbusReadResult(Mockito.mock(ModbusReadRequestBlueprint.class),
                new ModbusRegisterArray(0x0001, 0x002A)));

        verify(firstCallback).stateUpdated(first.channelUID(), new org.openhab.core.library.types.DecimalType("42"));
        verifyNoInteractions(secondCallback);

        readCallback(second.handler()).handle(new AsyncModbusReadResult(Mockito.mock(ModbusReadRequestBlueprint.class),
                new ModbusRegisterArray(0x0002, 0x0011)));

        verify(secondCallback).stateUpdated(second.channelUID(), new org.openhab.core.library.types.DecimalType("17"));
    }

    @Test
    public void testPoller2FailureAndRecoveryDoNotChangeSiblingHealth() throws ReflectiveOperationException {
        Poller2Fixture first = createPoller("failing", 100, 1, 100);
        Poller2Fixture second = createPoller("healthy", 100, 1, 100);
        first.handler().setCallback(firstCallback);
        second.handler().setCallback(secondCallback);

        failureCallback(first.handler()).handle(new AsyncModbusFailure<>(Mockito.mock(ModbusReadRequestBlueprint.class),
                new RuntimeException("first poller transport failure")));

        waitForAssert(() -> assertThat(first.poller().getStatus(), is(equalTo(ThingStatus.OFFLINE))));
        assertThat(second.poller().getStatus(), is(equalTo(ThingStatus.ONLINE)));
        verifyNoInteractions(secondCallback);

        readCallback(first.handler()).handle(new AsyncModbusReadResult(Mockito.mock(ModbusReadRequestBlueprint.class),
                new ModbusRegisterArray(0x002A)));

        waitForAssert(() -> assertThat(first.poller().getStatus(), is(equalTo(ThingStatus.ONLINE))));
        assertThat(second.poller().getStatus(), is(equalTo(ThingStatus.ONLINE)));
        verify(firstCallback).stateUpdated(first.channelUID(), new org.openhab.core.library.types.DecimalType("42"));
        verifyNoInteractions(secondCallback);
    }

    private Poller2Fixture createPoller(String id, int start, int length, int channelAddress) {
        Configuration pollerConfiguration = new Configuration();
        pollerConfiguration.put("refresh", 0L);
        pollerConfiguration.put("start", start);
        pollerConfiguration.put("length", length);
        pollerConfiguration.put("type", ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);
        ThingUID pollerUID = new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2, id);
        ChannelUID channelUID = new ChannelUID(pollerUID, "value");
        Bridge poller = ModbusPollerThingHandlerTest.createPoller2ThingBuilder(id)
                .withConfiguration(pollerConfiguration).withBridge(endpoint.getUID())
                .withChannel(ChannelBuilder.create(channelUID, "Number")
                        .withConfiguration(new Configuration(
                                Map.of("address", Integer.toString(channelAddress), "valueType", "uint16")))
                        .build())
                .build();
        addThing(poller);
        assertThat(poller.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        return new Poller2Fixture(poller, channelUID, (ModbusPoller2ThingHandler) poller.getHandler());
    }

    private static ModbusReadCallback readCallback(ModbusPollerThingHandler handler)
            throws ReflectiveOperationException {
        Field callbackField = ModbusPollerThingHandler.class.getDeclaredField("callbackDelegator");
        callbackField.setAccessible(true);
        return (ModbusReadCallback) callbackField.get(handler);
    }

    @SuppressWarnings("unchecked")
    private static ModbusFailureCallback<ModbusReadRequestBlueprint> failureCallback(ModbusPollerThingHandler handler)
            throws ReflectiveOperationException {
        Field callbackField = ModbusPollerThingHandler.class.getDeclaredField("callbackDelegator");
        callbackField.setAccessible(true);
        return (ModbusFailureCallback<ModbusReadRequestBlueprint>) callbackField.get(handler);
    }

    private record Poller2Fixture(Bridge poller, ChannelUID channelUID, ModbusPoller2ThingHandler handler) {
    }
}
