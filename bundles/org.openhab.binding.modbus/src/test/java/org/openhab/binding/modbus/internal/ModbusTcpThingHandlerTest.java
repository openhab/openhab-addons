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
package org.openhab.binding.modbus.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Objects;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openhab.binding.modbus.internal.handler.ModbusTcpThingHandler;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.openhab.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;

@RunWith(MockitoJUnitRunner.class)
public class ModbusTcpThingHandlerTest {

    @Mock
    private ModbusManager modbusManager;

    private static BridgeBuilder createTcpThingBuilder(String id) {
        return BridgeBuilder.create(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_TCP,
                new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_TCP, id));
    }

    @SuppressWarnings("null")
    @Test
    public void testInitializeAndSlaveEndpoint() {
        Configuration thingConfig = new Configuration();
        thingConfig.put("host", "thisishost");
        thingConfig.put("port", 44);
        thingConfig.put("id", 9);
        thingConfig.put("timeBetweenTransactionsMillis", 1);
        thingConfig.put("timeBetweenReconnectMillis", 2);
        thingConfig.put("connectMaxTries", 3);
        thingConfig.put("reconnectAfterMillis", 4);
        thingConfig.put("connectTimeoutMillis", 5);

        EndpointPoolConfiguration expectedPoolConfiguration = new EndpointPoolConfiguration();
        expectedPoolConfiguration.setConnectMaxTries(3);
        expectedPoolConfiguration.setConnectTimeoutMillis(5);
        expectedPoolConfiguration.setInterConnectDelayMillis(2);
        expectedPoolConfiguration.setInterTransactionDelayMillis(1);
        expectedPoolConfiguration.setReconnectAfterMillis(4);

        Bridge thing = createTcpThingBuilder("tcpendpoint").withConfiguration(thingConfig).build();
        ThingHandlerCallback thingCallback = Mockito.mock(ThingHandlerCallback.class);
        Mockito.doAnswer(invocation -> {
            thing.setStatusInfo((ThingStatusInfo) invocation.getArgument(1));
            return null;
        }).when(thingCallback).statusUpdated(ArgumentMatchers.same(thing), ArgumentMatchers.any());

        ModbusTcpThingHandler thingHandler = new ModbusTcpThingHandler(thing, () -> modbusManager);
        thingHandler.setCallback(thingCallback);
        thingHandler.initialize();

        assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        ModbusSlaveEndpoint slaveEndpoint = thingHandler.asSlaveEndpoint();
        assertThat(slaveEndpoint, is(equalTo(new ModbusTCPSlaveEndpoint("thisishost", 44))));
        assertThat(thingHandler.getSlaveId(), is(9));

        InOrder orderedVerify = Mockito.inOrder(modbusManager);
        orderedVerify.verify(modbusManager).addListener(thingHandler);
        ModbusSlaveEndpoint endpoint = thingHandler.asSlaveEndpoint();
        Objects.requireNonNull(endpoint);
        orderedVerify.verify(modbusManager).setEndpointPoolConfiguration(endpoint, expectedPoolConfiguration);
    }

    @Test
    public void testTwoDifferentEndpointWithDifferentParameters() {
        // thing1
        Configuration thingConfig = new Configuration();
        thingConfig.put("host", "thisishost");
        thingConfig.put("port", 44);
        thingConfig.put("connectMaxTries", 1);
        thingConfig.put("timeBetweenTransactionsMillis", 1);

        final Bridge thing = createTcpThingBuilder("tcpendpoint").withConfiguration(thingConfig).build();
        ThingHandlerCallback thingCallback = Mockito.mock(ThingHandlerCallback.class);
        Mockito.doAnswer(invocation -> {
            thing.setStatusInfo((ThingStatusInfo) invocation.getArgument(1));
            return null;
        }).when(thingCallback).statusUpdated(ArgumentMatchers.same(thing), ArgumentMatchers.any());

        ModbusTcpThingHandler thingHandler = new ModbusTcpThingHandler(thing, () -> modbusManager);
        thingHandler.setCallback(thingCallback);
        thingHandler.initialize();
        assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        EndpointPoolConfiguration poolConfiguration = new EndpointPoolConfiguration();
        poolConfiguration.setInterTransactionDelayMillis(2);
        // Different endpoint (port 45), so should not affect this thing
        thingHandler.onEndpointPoolConfigurationSet(new ModbusTCPSlaveEndpoint("thisishost", 45), poolConfiguration);
        assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
    }

    @Test
    public void testTwoIdenticalEndpointWithDifferentParameters() {
        // thing1
        Configuration thingConfig = new Configuration();
        thingConfig.put("host", "thisishost");
        thingConfig.put("port", 44);
        thingConfig.put("connectMaxTries", 1);
        thingConfig.put("timeBetweenTransactionsMillis", 1);

        final Bridge thing = createTcpThingBuilder("tcpendpoint").withConfiguration(thingConfig).build();
        ThingHandlerCallback thingCallback = Mockito.mock(ThingHandlerCallback.class);
        Mockito.doAnswer(invocation -> {
            thing.setStatusInfo((ThingStatusInfo) invocation.getArgument(1));
            return null;
        }).when(thingCallback).statusUpdated(ArgumentMatchers.same(thing), ArgumentMatchers.any());

        ModbusTcpThingHandler thingHandler = new ModbusTcpThingHandler(thing, () -> modbusManager);
        thingHandler.setCallback(thingCallback);
        thingHandler.initialize();
        assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        EndpointPoolConfiguration poolConfiguration = new EndpointPoolConfiguration();
        poolConfiguration.setInterTransactionDelayMillis(2);
        // Same endpoint and different parameters -> OFFLINE
        thingHandler.onEndpointPoolConfigurationSet(new ModbusTCPSlaveEndpoint("thisishost", 44), poolConfiguration);
        assertThat(thing.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        assertThat(thing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @Test
    public void testTwoIdenticalEndpointWithSameParameters() {
        // thing1
        Configuration thingConfig = new Configuration();
        thingConfig.put("host", "thisishost");
        thingConfig.put("port", 44);
        thingConfig.put("connectMaxTries", 1);
        thingConfig.put("timeBetweenTransactionsMillis", 1);

        final Bridge thing = createTcpThingBuilder("tcpendpoint").withConfiguration(thingConfig).build();
        ThingHandlerCallback thingCallback = Mockito.mock(ThingHandlerCallback.class);
        Mockito.doAnswer(invocation -> {
            thing.setStatusInfo((ThingStatusInfo) invocation.getArgument(1));
            return null;
        }).when(thingCallback).statusUpdated(ArgumentMatchers.same(thing), ArgumentMatchers.any());

        ModbusTcpThingHandler thingHandler = new ModbusTcpThingHandler(thing, () -> modbusManager);
        thingHandler.setCallback(thingCallback);
        thingHandler.initialize();
        assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        EndpointPoolConfiguration poolConfiguration = new EndpointPoolConfiguration();
        poolConfiguration.setInterTransactionDelayMillis(1);
        // Same endpoint and same parameters -> should not affect this thing
        thingHandler.onEndpointPoolConfigurationSet(new ModbusTCPSlaveEndpoint("thisishost", 44), poolConfiguration);
        assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
    }
}
