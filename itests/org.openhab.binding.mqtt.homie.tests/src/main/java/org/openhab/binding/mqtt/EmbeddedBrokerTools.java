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
package org.openhab.binding.mqtt;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttService;
import org.openhab.core.io.transport.mqtt.MqttServiceObserver;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * A full implementation test, that starts the embedded MQTT broker and publishes a homeassistant MQTT discovery device
 * tree.
 *
 * @author David Graeff - Initial contribution
 * @author Wouter Born - Support running MQTT itests in parallel by reconfiguring embedded broker port
 */
@NonNullByDefault
public class EmbeddedBrokerTools {

    private static final int BROKER_PORT = Integer.getInteger("mqttembeddedbroker.port", 1883);

    private final ConfigurationAdmin configurationAdmin;
    private final MqttService mqttService;

    public @Nullable MqttBrokerConnection embeddedConnection;

    public EmbeddedBrokerTools(ConfigurationAdmin configurationAdmin, MqttService mqttService) {
        this.configurationAdmin = configurationAdmin;
        this.mqttService = mqttService;
    }

    /**
     * Request the embedded broker connection from the {@link MqttService} and wait for a connection to be established.
     *
     * @throws InterruptedException
     * @throws IOException
     */
    public MqttBrokerConnection waitForConnection() throws InterruptedException, IOException {
        reconfigurePort();

        embeddedConnection = mqttService.getBrokerConnection(Constants.CLIENTID);
        if (embeddedConnection == null) {
            Semaphore semaphore = new Semaphore(1);
            semaphore.acquire();
            MqttServiceObserver observer = new MqttServiceObserver() {

                @Override
                public void brokerAdded(String brokerID, MqttBrokerConnection broker) {
                    if (brokerID.equals(Constants.CLIENTID)) {
                        embeddedConnection = broker;
                        semaphore.release();
                    }
                }

                @Override
                public void brokerRemoved(String brokerID, MqttBrokerConnection broker) {
                }
            };
            mqttService.addBrokersListener(observer);
            assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS), "Wait for embedded connection client failed");
        }
        MqttBrokerConnection embeddedConnection = this.embeddedConnection;
        if (embeddedConnection == null) {
            throw new IllegalStateException();
        }

        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        MqttConnectionObserver mqttConnectionObserver = (state, error) -> {
            if (state == MqttConnectionState.CONNECTED) {
                semaphore.release();
            }
        };
        embeddedConnection.addConnectionObserver(mqttConnectionObserver);
        if (embeddedConnection.connectionState() == MqttConnectionState.CONNECTED) {
            semaphore.release();
        }
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS), "Connection " + embeddedConnection.getClientId()
                + " failed. State: " + embeddedConnection.connectionState());
        return embeddedConnection;
    }

    public void reconfigurePort() throws IOException {
        Configuration configuration = configurationAdmin.getConfiguration(Constants.PID, null);

        Dictionary<String, Object> properties = configuration.getProperties();
        if (properties == null) {
            properties = new Hashtable<>();
        }

        Integer currentPort = (Integer) properties.get(Constants.PORT);
        if (currentPort == null || currentPort.intValue() != BROKER_PORT) {
            properties.put(Constants.PORT, BROKER_PORT);
            configuration.update(properties);
            // Remove the connection to make sure the test waits for the new connection to become available
            mqttService.removeBrokerConnection(Constants.CLIENTID);
        }
    }
}
