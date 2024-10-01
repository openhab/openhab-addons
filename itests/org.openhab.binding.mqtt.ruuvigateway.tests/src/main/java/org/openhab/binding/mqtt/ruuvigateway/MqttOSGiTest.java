/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.ruuvigateway;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.items.ItemProvider;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ManagedItemProvider;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.link.ItemChannelLinkProvider;
import org.openhab.core.thing.link.ManagedItemChannelLinkProvider;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;

/**
 * Creates a Moquette MQTT broker instance and a {@link MqttBrokerConnection} for testing MQTT bindings.
 *
 * @author Wouter Born - Initial contribution
 * @author Sami Salonen - Copied to MQTT Ruuvi Gateway addon
 */
@NonNullByDefault
public class MqttOSGiTest extends JavaOSGiTest {

    private static final String BROKER_ID = "test-broker";
    @SuppressWarnings("null")
    private static final int BROKER_PORT = Integer.getInteger("mqttbroker.port", 1883);

    protected @NonNullByDefault({}) MqttBrokerConnection brokerConnection;

    private Server moquetteServer = new Server();
    protected @NonNullByDefault({}) ManagedThingProvider thingProvider;
    protected @NonNullByDefault({}) ManagedItemProvider itemProvider;
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;
    protected @NonNullByDefault({}) ManagedItemChannelLinkProvider itemChannelLinkProvider;
    protected @NonNullByDefault({}) Inbox inbox;

    @BeforeEach
    public void beforeEach() throws Exception {
        registerVolatileStorageService();

        thingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertNotNull(thingProvider, "Could not get ManagedThingProvider");

        itemProvider = getService(ItemProvider.class, ManagedItemProvider.class);
        assertNotNull(itemProvider, "Could not get ManagedItemProvider");
        itemRegistry = getService(ItemRegistry.class);
        assertNotNull(itemProvider, "Could not get ItemRegistry");

        itemChannelLinkProvider = getService(ItemChannelLinkProvider.class, ManagedItemChannelLinkProvider.class);
        assertNotNull(itemChannelLinkProvider, "Could not get ManagedItemChannelLinkProvider");

        inbox = getService(Inbox.class);
        assertNotNull(inbox, "Could not get Inbox");

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
