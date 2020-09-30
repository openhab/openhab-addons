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
package org.openhab.io.mqttembeddedbroker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.openMocks;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttService;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Moquette test
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class MoquetteTest extends JavaOSGiTest {
    private static final String TEST_TOPIC = "testtopic";

    private @NonNullByDefault({}) AutoCloseable mocksCloseable;

    private @NonNullByDefault({}) ConfigurationAdmin configurationAdmin;
    private @NonNullByDefault({}) MqttService mqttService;
    private @NonNullByDefault({}) MqttBrokerConnection embeddedConnection;
    private @NonNullByDefault({}) MqttBrokerConnection clientConnection;

    /**
     * Create an observer that fails the test as soon as the broker client connection changes its connection state
     * to something else then CONNECTED.
     */
    private MqttConnectionObserver failIfChange = (state, error) -> assertThat(state,
            is(MqttConnectionState.CONNECTED));

    @BeforeEach
    public void beforeEach() throws Exception {
        registerVolatileStorageService();
        mocksCloseable = openMocks(this);
        configurationAdmin = getService(ConfigurationAdmin.class);
        mqttService = getService(MqttService.class);

        // Wait for the EmbeddedBrokerService internal connection to be connected
        embeddedConnection = new EmbeddedBrokerTools(configurationAdmin, mqttService).waitForConnection();
        embeddedConnection.setQos(1);

        clientConnection = new MqttBrokerConnection(embeddedConnection.getHost(), embeddedConnection.getPort(),
                embeddedConnection.isSecure(), "client");
        clientConnection.setQos(1);
        clientConnection.start().get(500, TimeUnit.MILLISECONDS);
        assertThat(clientConnection.connectionState(), is(MqttConnectionState.CONNECTED));
        // If the connection state changes in between -> fail
        clientConnection.addConnectionObserver(failIfChange);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (clientConnection != null) {
            clientConnection.removeConnectionObserver(failIfChange);
            clientConnection.stop().get(500, TimeUnit.MILLISECONDS);
        }
        mocksCloseable.close();
    }

    private CompletableFuture<Boolean> publish(String topic, String message) {
        return embeddedConnection.publish(topic, message.getBytes(StandardCharsets.UTF_8), 0, true);
    }

    @Test
    public void singleTopic() throws InterruptedException, ExecutionException, TimeoutException {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        futures.add(publish(TEST_TOPIC, "testPayload"));

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

        futures.add(publish(TEST_TOPIC + "/1", "testPayload1"));
        futures.add(publish(TEST_TOPIC + "/2", "testPayload2"));

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

        futures.add(publish(TEST_TOPIC + "/1", "testPayload1"));
        futures.add(publish(TEST_TOPIC + "/2", "testPayload2"));

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

        futures.add(publish(TEST_TOPIC + "/1", "testPayload1"));
        futures.add(publish(TEST_TOPIC + "/2", "testPayload2"));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        CountDownLatch c = new CountDownLatch(2);
        futures.clear();
        futures.add(clientConnection.subscribe(TEST_TOPIC + "/+", (topic, payload) -> c.countDown()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        assertTrue(c.await(1000, TimeUnit.MILLISECONDS));
    }
}
