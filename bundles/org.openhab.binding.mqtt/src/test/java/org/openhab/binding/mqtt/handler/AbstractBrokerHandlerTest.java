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
package org.openhab.binding.mqtt.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.internal.MqttThingID;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.io.transport.mqtt.MqttService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.osgi.service.cm.ConfigurationException;

/**
 * Tests cases for {@link org.openhab.binding.mqtt.handler.AbstractBrokerHandler}.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class AbstractBrokerHandlerTest {
    private final String HOST = "tcp://123.1.2.3";
    private final int PORT = 80;
    private SystemBrokerHandler handler;
    int stateChangeCounter = 0;

    private @Mock ThingHandlerCallback callback;
    private @Mock Bridge thing;
    private @Mock MqttService service;

    @BeforeEach
    public void setUp() {
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

        verify(callback, times(0)).statusUpdated(any(), any());
        handler.brokerAdded(handler.brokerID, connection);

        assertThat(handler.connection, is(connection));

        verify(connection).start();

        // First connecting then connected and another connected after the future completes
        verify(callback, times(3)).statusUpdated(any(), any());
    }
}
