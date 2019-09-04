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
package org.openhab.io.mqttembeddedbroker;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Moquette test
 *
 * @author Jan N. Klug - Initial contribution
 */
public class MoquetteTest extends JavaOSGiTest {
    private static final String TEST_TOPIC = "testtopic";

    private MqttService mqttService;
    private MqttBrokerConnection embeddedConnection;
    private MqttBrokerConnection clientConnection;

    /**
     * Create an observer that fails the test as soon as the broker client connection changes its connection state
     * to something else then CONNECTED.
     */
    private MqttConnectionObserver failIfChange = (state, error) -> assertThat(state,
            is(MqttConnectionState.CONNECTED));

    @Before
    public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
        registerVolatileStorageService();
        initMocks(this);
        mqttService = getService(MqttService.class);

        // Wait for the EmbeddedBrokerService internal connection to be connected
        embeddedConnection = new EmbeddedBrokerTools().waitForConnection(mqttService);
        embeddedConnection.setQos(1);
        embeddedConnection.setRetain(true);

        clientConnection = new MqttBrokerConnection(embeddedConnection.getHost(), embeddedConnection.getPort(),
                embeddedConnection.isSecure(), "client");
        clientConnection.setQos(1);
        clientConnection.start().get(500, TimeUnit.MILLISECONDS);
        assertThat(clientConnection.connectionState(), is(MqttConnectionState.CONNECTED));
        // If the connection state changes in between -> fail
        clientConnection.addConnectionObserver(failIfChange);
    }

    @After
    public void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
        if (clientConnection != null) {
            clientConnection.removeConnectionObserver(failIfChange);
            clientConnection.stop().get(500, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void singleTopic() throws InterruptedException, ExecutionException, TimeoutException {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        futures.add(embeddedConnection.publish(TEST_TOPIC, "testPayload".getBytes(StandardCharsets.UTF_8)));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        CountDownLatch c = new CountDownLatch(1);
        futures.clear();
        futures.add(clientConnection.subscribe(TEST_TOPIC, (topic, payload) -> c.countDown()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        assertTrue(c.await(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void multipleTopicsWithSingleSubscription()
            throws InterruptedException, ExecutionException, TimeoutException {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        futures.add(embeddedConnection.publish(TEST_TOPIC + "/1", "testPayload1".getBytes(StandardCharsets.UTF_8)));
        futures.add(embeddedConnection.publish(TEST_TOPIC + "/2", "testPayload2".getBytes(StandardCharsets.UTF_8)));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        CountDownLatch c = new CountDownLatch(2);
        futures.clear();
        futures.add(clientConnection.subscribe(TEST_TOPIC + "/1", (topic, payload) -> c.countDown()));
        futures.add(clientConnection.subscribe(TEST_TOPIC + "/2", (topic, payload) -> c.countDown()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        assertTrue(c.await(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void multipleTopicsWithHashWildcardSubscription()
            throws InterruptedException, ExecutionException, TimeoutException {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        futures.add(embeddedConnection.publish(TEST_TOPIC + "/1", "testPayload1".getBytes(StandardCharsets.UTF_8)));
        futures.add(embeddedConnection.publish(TEST_TOPIC + "/2", "testPayload2".getBytes(StandardCharsets.UTF_8)));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        CountDownLatch c = new CountDownLatch(2);
        futures.clear();
        futures.add(clientConnection.subscribe(TEST_TOPIC + "/#", (topic, payload) -> c.countDown()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        assertTrue(c.await(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void multipleTopicsWithPlusWildcardSubscription()
            throws InterruptedException, ExecutionException, TimeoutException {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        futures.add(embeddedConnection.publish(TEST_TOPIC + "/1", "testPayload1".getBytes(StandardCharsets.UTF_8)));
        futures.add(embeddedConnection.publish(TEST_TOPIC + "/2", "testPayload2".getBytes(StandardCharsets.UTF_8)));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        CountDownLatch c = new CountDownLatch(2);
        futures.clear();
        futures.add(clientConnection.subscribe(TEST_TOPIC + "/+", (topic, payload) -> c.countDown()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        assertTrue(c.await(1000, TimeUnit.MILLISECONDS));
    }
}
