/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.mqtt.internal.MqttThingID;
import org.osgi.service.cm.ConfigurationException;

/**
 * Tests cases for {@link org.openhab.binding.mqtt.handler.AbstractBrokerHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class AbstractBrokerHandlerTest {
    private final String HOST = "tcp://123.1.2.3";
    private final int PORT = 80;
    private SystemBrokerHandler handler;
    int stateChangeCounter = 0;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Bridge thing;

    @Mock
    private MqttService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(MqttThingID.getThingUID(HOST, PORT)).when(thing).getUID();
        doReturn(new Configuration(Collections.singletonMap("brokerid", MqttThingID.getThingUID(HOST, PORT).getId())))
                .when(thing).getConfiguration();
        handler = new SystemBrokerHandler(thing, service);
        handler.setCallback(callback);
        assertThat(handler.getThing().getConfiguration().get("brokerid"), is(MqttThingID.getThingID(HOST, PORT)));
        stateChangeCounter = 0;
    }

    @Test
    public void brokerAddedWrongID() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);
        handler.brokerAdded("nonsense_id", brokerConnection);
        assertNull(handler.connection);
        // We do not expect a status change, because brokerAdded will do nothing with invalid connections.
        verify(callback, times(0)).statusUpdated(any(), any());
    }

    @Test
    public void brokerRemovedBroker() throws ConfigurationException, MqttException {
        MqttBrokerConnectionEx connection = spy(
                new MqttBrokerConnectionEx("10.10.0.10", 80, false, "BrokerHandlerTest"));
        handler.brokerAdded(handler.brokerID, connection);
        assertThat(handler.connection, is(connection));
        handler.brokerRemoved("something", connection);
        assertNull(handler.connection);
    }

    @Test
    public void brokerAdded() throws ConfigurationException, MqttException {
        MqttBrokerConnectionEx connection = spy(
                new MqttBrokerConnectionEx("10.10.0.10", 80, false, "BrokerHandlerTest"));
        doReturn(connection).when(service).getBrokerConnection(eq(handler.brokerID));

        verify(callback, times(0)).statusUpdated(any(), any());
        handler.brokerAdded(handler.brokerID, connection);

        assertThat(handler.connection, is(connection));

        verify(connection).start();

        // First connecting then connected and another connected after the future completes
        verify(callback, times(3)).statusUpdated(any(), any());
    }
}
