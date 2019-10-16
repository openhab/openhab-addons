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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.io.transport.mqtt.MqttServiceObserver;
import org.openhab.io.mqttembeddedbroker.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A full implementation test, that starts the embedded MQTT broker and publishes a homeassistant MQTT discovery device
 * tree.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class EmbeddedBrokerTools {
    private final Logger logger = LoggerFactory.getLogger(EmbeddedBrokerTools.class);
    public @Nullable MqttBrokerConnection embeddedConnection = null;

    /**
     * Request the embedded broker connection from the {@link MqttService} and wait for a connection to be established.
     *
     * @throws InterruptedException
     */
    public MqttBrokerConnection waitForConnection(MqttService mqttService) throws InterruptedException {
        embeddedConnection = mqttService.getBrokerConnection(Constants.CLIENTID);
        if (embeddedConnection == null) {
            Semaphore semaphore = new Semaphore(1);
            semaphore.acquire();
            MqttServiceObserver observer = new MqttServiceObserver() {

                @Override
                public void brokerAdded(@NonNull String brokerID, @NonNull MqttBrokerConnection broker) {
                    if (brokerID.equals(Constants.CLIENTID)) {
                        embeddedConnection = broker;
                        semaphore.release();
                    }
                }

                @Override
                public void brokerRemoved(@NonNull String brokerID, @NonNull MqttBrokerConnection broker) {
                }

            };
            mqttService.addBrokersListener(observer);
            assertTrue("Wait for embedded connection client failed", semaphore.tryAcquire(700, TimeUnit.MILLISECONDS));
        }
        MqttBrokerConnection embeddedConnection = this.embeddedConnection;
        if (embeddedConnection == null) {
            throw new IllegalStateException();
        }

        logger.warn("waitForConnection {}", embeddedConnection.connectionState());
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
        assertTrue("Connection " + embeddedConnection.getClientId() + " failed. State: "
                + embeddedConnection.connectionState(), semaphore.tryAcquire(500, TimeUnit.MILLISECONDS));
        return embeddedConnection;
    }

}
