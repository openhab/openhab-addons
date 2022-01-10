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
package org.openhab.binding.mqtt.internal;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryParticipant;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.handler.BrokerHandler;
import org.openhab.binding.mqtt.handler.BrokerHandlerEx;
import org.openhab.binding.mqtt.handler.MqttBrokerConnectionEx;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Test cases for the {@link MQTTTopicDiscoveryService} service.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class MQTTTopicDiscoveryServiceTest {
    private ScheduledExecutorService scheduler;

    private MqttBrokerHandlerFactory subject;

    @Mock
    private MqttService mqttService;

    @Mock
    private Bridge thing;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    MQTTTopicDiscoveryParticipant listener;

    private MqttBrokerConnectionEx connection;

    private BrokerHandler handler;

    @BeforeEach
    public void setUp() {
        scheduler = new ScheduledThreadPoolExecutor(1);

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

        subject = new MqttBrokerHandlerFactory(mqttService);
    }

    @AfterEach
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    public void firstSubscribeThenHandler() {
        handler.initialize();
        BrokerHandlerEx.verifyCreateBrokerConnection(handler, 1);

        subject.subscribe(listener, "topic");
        subject.createdHandler(handler);
        assertThat(subject.discoveryTopics.get("topic"), hasItem(listener));
        // Simulate receiving
        final byte[] bytes = "TEST".getBytes();
        connection.getSubscribers().get("topic").messageArrived("topic", bytes, false);
        verify(listener).receivedMessage(eq(thing.getUID()), eq(connection), eq("topic"), eq(bytes));
    }

    @Test
    public void firstHandlerThenSubscribe() {
        handler.initialize();
        BrokerHandlerEx.verifyCreateBrokerConnection(handler, 1);

        subject.createdHandler(handler);
        subject.subscribe(listener, "topic");
        assertThat(subject.discoveryTopics.get("topic"), hasItem(listener));

        // Simulate receiving
        final byte[] bytes = "TEST".getBytes();
        connection.getSubscribers().get("topic").messageArrived("topic", bytes, false);
        verify(listener).receivedMessage(eq(thing.getUID()), eq(connection), eq("topic"), eq(bytes));
    }

    @Test
    public void handlerInitializeAfterSubscribe() {
        subject.createdHandler(handler);
        subject.subscribe(listener, "topic");
        assertThat(subject.discoveryTopics.get("topic"), hasItem(listener));

        // Init handler -> create connection
        handler.initialize();
        BrokerHandlerEx.verifyCreateBrokerConnection(handler, 1);

        // Simulate receiving
        final byte[] bytes = "TEST".getBytes();
        connection.getSubscribers().get("topic").messageArrived("topic", bytes, false);
        verify(listener).receivedMessage(eq(thing.getUID()), eq(connection), eq("topic"), eq(bytes));
    }

    @Test
    public void topicVanished() {
        handler.initialize();
        BrokerHandlerEx.verifyCreateBrokerConnection(handler, 1);

        subject.createdHandler(handler);
        subject.subscribe(listener, "topic");
        assertThat(subject.discoveryTopics.get("topic"), hasItem(listener));

        // Simulate receiving
        final byte[] bytes = "".getBytes();
        connection.getSubscribers().get("topic").messageArrived("topic", bytes, false);
        verify(listener).topicVanished(eq(thing.getUID()), eq(connection), eq("topic"));
    }
}
