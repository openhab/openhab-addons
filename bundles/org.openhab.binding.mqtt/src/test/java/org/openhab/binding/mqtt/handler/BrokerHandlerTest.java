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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.mqtt.internal.MqttThingID;
import org.osgi.service.cm.ConfigurationException;

/**
 * Test cases for {@link BrokerHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class BrokerHandlerTest {
    private ScheduledExecutorService scheduler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Bridge thing;

    @Mock
    private MqttService service;

    private MqttBrokerConnectionEx connection;

    private BrokerHandler handler;

    @Before
    public void setUp() throws ConfigurationException, MqttException {
        scheduler = new ScheduledThreadPoolExecutor(1);
        MockitoAnnotations.initMocks(this);
        when(thing.getUID()).thenReturn(MqttThingID.getThingUID("10.10.0.10", 80));
        connection = spy(new MqttBrokerConnectionEx("10.10.0.10", 80, false, "BrokerHandlerTest"));
        connection.setTimeoutExecutor(scheduler, 10);
        connection.setConnectionCallback(connection);

        Configuration config = new Configuration();
        when(thing.getConfiguration()).thenReturn(config);

        handler = spy(new BrokerHandlerEx(thing, connection));
        handler.setCallback(callback);
    }

    @After
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlerInitWithoutUrl()
            throws InterruptedException, IllegalArgumentException, MqttException, ConfigurationException {
        // Assume it is a real handler and not a mock as defined above
        handler = new BrokerHandler(thing);
        assertThat(initializeHandlerWaitForTimeout(), is(true));
    }

    @Test
    public void createBrokerConnection() {
        Configuration config = new Configuration();
        config.put("host", "10.10.0.10");
        config.put("port", 80);
        when(thing.getConfiguration()).thenReturn(config);
        handler.initialize();
        verify(handler).createBrokerConnection();
    }

    @Test
    public void handlerInit()
            throws InterruptedException, IllegalArgumentException, MqttException, ConfigurationException {
        assertThat(initializeHandlerWaitForTimeout(), is(true));

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeast(3)).statusUpdated(eq(thing), statusInfoCaptor.capture());
        Assert.assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
    }

    /**
     * Utility method for tests that need the handler to be initialized to go on.
     *
     * @return Return true if successful. You usually want to use:
     *         assertThat(initializeHandlerWaitForTimeout(), is(true));
     * @throws InterruptedException
     * @throws IllegalArgumentException
     * @throws MqttException
     * @throws ConfigurationException
     */
    boolean initializeHandlerWaitForTimeout()
            throws InterruptedException, IllegalArgumentException, MqttException, ConfigurationException {
        MqttBrokerConnection c = connection;

        MqttConnectionObserverEx o = new MqttConnectionObserverEx();
        c.addConnectionObserver(o);

        assertThat(connection.connectionState(), is(MqttConnectionState.DISCONNECTED));
        handler.initialize();
        verify(connection, times(2)).addConnectionObserver(any());
        verify(connection, times(1)).start();
        // First we expect a CONNECTING state and then a CONNECTED unique state change
        assertThat(o.counter, is(2));
        // First we expect a CONNECTING state and then a CONNECTED state change
        // (and other CONNECTED after the future completes)
        verify(handler, times(3)).connectionStateChanged(any(), any());
        return true;
    }
}
