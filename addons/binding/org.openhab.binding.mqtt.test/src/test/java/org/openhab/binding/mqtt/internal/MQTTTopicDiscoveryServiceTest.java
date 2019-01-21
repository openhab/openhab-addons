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
package org.openhab.binding.mqtt.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryParticipant;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.handler.BrokerHandler;
import org.openhab.binding.mqtt.handler.BrokerHandlerEx;
import org.openhab.binding.mqtt.handler.MqttBrokerConnectionEx;
import org.openhab.binding.mqtt.internal.MqttBrokerHandlerFactory;
import org.openhab.binding.mqtt.internal.MqttThingID;
import org.osgi.service.cm.ConfigurationException;

/**
 * Test cases for the {@link MQTTTopicDiscoveryService} service.
 *
 * @author David Graeff - Initial contribution
 */
public class MQTTTopicDiscoveryServiceTest {
    private ScheduledExecutorService scheduler;

    private MqttBrokerHandlerFactory subject;

    @Mock
    private Bridge thing;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    MQTTTopicDiscoveryParticipant listener;

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
        config.put("host", "10.10.0.10");
        config.put("port", 80);
        when(thing.getConfiguration()).thenReturn(config);

        handler = spy(new BrokerHandlerEx(thing, connection));
        handler.setCallback(callback);

        subject = new MqttBrokerHandlerFactory();
    }

    @After
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    public void firstSubscribeThenHandler() {
        handler.initialize();
        BrokerHandlerEx.verifyCreateBrokerConnection(handler, 1);

        subject.subscribe(listener, "topic");
        subject.createdHandler(handler);
        assertThat(subject.subscriber.get(listener).topic, is("topic"));
        // Simulate receiving
        final byte[] bytes = "TEST".getBytes();
        subject.subscriber.get(listener).observedBrokerHandlers.get(thing.getUID()).processMessage("topic", bytes);
        verify(listener).receivedMessage(eq(thing.getUID()), eq(connection), eq("topic"), eq(bytes));
    }

    @Test
    public void firstHandlerThanSubscribe() {
        handler.initialize();
        BrokerHandlerEx.verifyCreateBrokerConnection(handler, 1);

        subject.createdHandler(handler);
        subject.subscribe(listener, "topic");
        assertThat(subject.subscriber.get(listener).topic, is("topic"));

        // Simulate receiving
        final byte[] bytes = "TEST".getBytes();
        subject.subscriber.get(listener).observedBrokerHandlers.get(thing.getUID()).processMessage("topic", bytes);
        verify(listener).receivedMessage(eq(thing.getUID()), eq(connection), eq("topic"), eq(bytes));
    }

    @Test
    public void handlerInitializeAfterSubscribe() {
        subject.createdHandler(handler);
        subject.subscribe(listener, "topic");
        assertThat(subject.subscriber.get(listener).topic, is("topic"));

        // No observed broker handler, because no connection created yet within the handler
        assertThat(subject.subscriber.get(listener).observedBrokerHandlers.size(), is(0));

        // Init handler -> create connection
        handler.initialize();
        BrokerHandlerEx.verifyCreateBrokerConnection(handler, 1);

        // Simulate receiving
        final byte[] bytes = "TEST".getBytes();
        subject.subscriber.get(listener).observedBrokerHandlers.get(thing.getUID()).processMessage("topic", bytes);
        verify(listener).receivedMessage(eq(thing.getUID()), eq(connection), eq("topic"), eq(bytes));
    }

    @Test
    public void topicVanished() {
        handler.initialize();
        BrokerHandlerEx.verifyCreateBrokerConnection(handler, 1);

        subject.createdHandler(handler);
        subject.subscribe(listener, "topic");
        assertThat(subject.subscriber.get(listener).topic, is("topic"));

        // Simulate receiving
        final byte[] bytes = "".getBytes();
        subject.subscriber.get(listener).observedBrokerHandlers.get(thing.getUID()).processMessage("topic", bytes);
        verify(listener).topicVanished(eq(thing.getUID()), eq(connection), eq("topic"));
    }

}
