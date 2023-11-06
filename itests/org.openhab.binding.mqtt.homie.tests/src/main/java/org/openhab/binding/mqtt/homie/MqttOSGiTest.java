/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homie;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.test.java.JavaOSGiTest;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;

/**
 * Creates a Moquette MQTT broker instance and a {@link MqttBrokerConnection} for testing MQTT bindings.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class MqttOSGiTest extends JavaOSGiTest {

    private static final String BROKER_ID = "test-broker";
    private static final int BROKER_PORT = Integer.getInteger("mqttbroker.port", 1883);

    protected @NonNullByDefault({}) MqttBrokerConnection brokerConnection;

    private Server moquetteServer = new Server();

    @BeforeEach
    public void beforeEach() throws Exception {
        registerVolatileStorageService();

        moquetteServer = new Server();
        moquetteServer.startServer(brokerProperties());

        brokerConnection = createBrokerConnection(BROKER_ID);
    }

    @AfterEach
    public void afterEach() throws Exception {
        brokerConnection.stop().get(5, TimeUnit.SECONDS);
        moquetteServer.stopServer();
    }

    private Properties brokerProperties() {
        Properties properties = new Properties();
        properties.put(BrokerConstants.HOST_PROPERTY_NAME, BrokerConstants.HOST);
        properties.put(BrokerConstants.PORT_PROPERTY_NAME, String.valueOf(BROKER_PORT));
        properties.put(BrokerConstants.SSL_PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
        properties.put(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
        properties.put(BrokerConstants.WSS_PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
        return properties;
    }

    protected MqttBrokerConnection createBrokerConnection(String clientId) throws Exception {
        MqttBrokerConnection connection = new MqttBrokerConnection(BrokerConstants.HOST, BROKER_PORT, false, clientId);
        connection.setQos(1);
        connection.start().get(5, TimeUnit.SECONDS);

        waitForAssert(() -> assertThat(connection.connectionState(), is(MqttConnectionState.CONNECTED)));

        return connection;
    }

    protected CompletableFuture<Boolean> publish(String topic, String message) {
        return brokerConnection.publish(topic, message.getBytes(StandardCharsets.UTF_8), 1, true);
    }
}
