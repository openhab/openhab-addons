/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.binding.modbus.internal.handler.ModbusTcpThingHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.core.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.openhab.core.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * @author Sami Salonen - Initial contribution
 */
public class ModbusTcpThingHandlerTest extends AbstractModbusOSGiTest {

    private static BridgeBuilder createTcpThingBuilder(String id) {
        return BridgeBuilder.create(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_TCP,
                new ThingUID(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_TCP, id));
    }

    @Test
    public void testInitializeAndSlaveEndpoint() throws EndpointNotInitializedException {
        // Using mocked modbus manager
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
        addThing(thing);
        assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        ModbusTcpThingHandler thingHandler = (ModbusTcpThingHandler) thing.getHandler();
        assertNotNull(thingHandler);
        ModbusSlaveEndpoint slaveEndpoint = thingHandler.getEndpoint();
        assertThat(slaveEndpoint, is(equalTo(new ModbusTCPSlaveEndpoint("thisishost", 44, false))));
        assertThat(thingHandler.getSlaveId(), is(9));

        InOrder orderedVerify = Mockito.inOrder(mockedModbusManager);
        ModbusSlaveEndpoint endpoint = thingHandler.getEndpoint();
        Objects.requireNonNull(endpoint);
        orderedVerify.verify(mockedModbusManager).newModbusCommunicationInterface(endpoint, expectedPoolConfiguration);
    }

    @Test
    public void testTwoDifferentEndpointWithDifferentParameters() {
        // Real implementation needed to validate this behaviour
        swapModbusManagerToReal();
        // thing1
        {
            Configuration thingConfig = new Configuration();
            thingConfig.put("host", "thisishost");
            thingConfig.put("port", 44);
            thingConfig.put("connectMaxTries", 1);
            thingConfig.put("timeBetweenTransactionsMillis", 1);

            final Bridge thing = createTcpThingBuilder("tcpendpoint").withConfiguration(thingConfig).build();
            addThing(thing);
            assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));

            ModbusTcpThingHandler thingHandler = (ModbusTcpThingHandler) thing.getHandler();
            assertNotNull(thingHandler);

            EndpointPoolConfiguration expectedPoolConfiguration = new EndpointPoolConfiguration();
            expectedPoolConfiguration.setConnectMaxTries(1);
            expectedPoolConfiguration.setInterTransactionDelayMillis(1);

            // defaults
            expectedPoolConfiguration.setConnectTimeoutMillis(10_000);
            expectedPoolConfiguration.setInterConnectDelayMillis(0);
            expectedPoolConfiguration.setReconnectAfterMillis(0);

            assertEquals(expectedPoolConfiguration, realModbusManager
                    .getEndpointPoolConfiguration(new ModbusTCPSlaveEndpoint("thisishost", 44, false)));
        }
        {
            Configuration thingConfig = new Configuration();
            thingConfig.put("host", "thisishost");
            thingConfig.put("port", 45);
            thingConfig.put("connectMaxTries", 1);
            thingConfig.put("timeBetweenTransactionsMillis", 100);

            final Bridge thing = createTcpThingBuilder("tcpendpoint2").withConfiguration(thingConfig).build();
            addThing(thing);
            // Different endpoint (port 45), so should be OK even though timeBetweenTransactionsMillis is different
            assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));

            ModbusTcpThingHandler thingHandler = (ModbusTcpThingHandler) thing.getHandler();
            assertNotNull(thingHandler);
        }
    }

    @Test
    public void testTwoIdenticalEndpointWithDifferentParameters() {
        // Real implementation needed to validate this behaviour
        swapModbusManagerToReal();
        // thing1
        {
            Configuration thingConfig = new Configuration();
            thingConfig.put("host", "thisishost");
            thingConfig.put("port", 44);
            thingConfig.put("connectMaxTries", 1);
            thingConfig.put("timeBetweenTransactionsMillis", 1);

            final Bridge thing = createTcpThingBuilder("tcpendpoint").withConfiguration(thingConfig).build();
            addThing(thing);
            assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));

            ModbusTcpThingHandler thingHandler = (ModbusTcpThingHandler) thing.getHandler();
            assertNotNull(thingHandler);
        }
        {

            Configuration thingConfig = new Configuration();
            thingConfig.put("host", "thisishost");
            thingConfig.put("port", 44);
            thingConfig.put("connectMaxTries", 1);
            thingConfig.put("timeBetweenTransactionsMillis", 100);

            final Bridge thing = createTcpThingBuilder("tcpendpoint2").withConfiguration(thingConfig).build();
            addThing(thing);
            assertThat(thing.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(thing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        }
        {
            //
            // Ensure the right EndpointPoolConfiguration is still in place
            //
            EndpointPoolConfiguration expectedPoolConfiguration = new EndpointPoolConfiguration();
            expectedPoolConfiguration.setConnectMaxTries(1);
            expectedPoolConfiguration.setInterTransactionDelayMillis(1); // Note: not 100

            // defaults
            expectedPoolConfiguration.setConnectTimeoutMillis(10_000);
            expectedPoolConfiguration.setInterConnectDelayMillis(0);
            expectedPoolConfiguration.setReconnectAfterMillis(0);

            assertEquals(expectedPoolConfiguration, realModbusManager
                    .getEndpointPoolConfiguration(new ModbusTCPSlaveEndpoint("thisishost", 44, false)));
        }
    }

    @Test
    public void testTwoIdenticalEndpointWithSameParameters() {
        // Real implementation needed to validate this behaviour
        swapModbusManagerToReal();
        // thing1
        {
            Configuration thingConfig = new Configuration();
            thingConfig.put("host", "thisishost");
            thingConfig.put("port", 44);
            thingConfig.put("connectMaxTries", 1);
            thingConfig.put("timeBetweenTransactionsMillis", 1);

            final Bridge thing = createTcpThingBuilder("tcpendpoint").withConfiguration(thingConfig).build();
            addThing(thing);
            assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));

            ModbusTcpThingHandler thingHandler = (ModbusTcpThingHandler) thing.getHandler();
            assertNotNull(thingHandler);
        }
        {
            Configuration thingConfig = new Configuration();
            thingConfig.put("host", "thisishost");
            thingConfig.put("port", 44);
            thingConfig.put("connectMaxTries", 1);
            thingConfig.put("timeBetweenTransactionsMillis", 1);
            thingConfig.put("connectTimeoutMillis", 10000); // default

            final Bridge thing = createTcpThingBuilder("tcpendpoint2").withConfiguration(thingConfig).build();
            addThing(thing);
            // Same endpoint and same parameters -> should not affect this thing
            assertThat(thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        }
    }
}
