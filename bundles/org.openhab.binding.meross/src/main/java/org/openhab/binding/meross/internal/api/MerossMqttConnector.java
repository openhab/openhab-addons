/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.dto.MqttMessageBuilder;
import org.openhab.binding.meross.internal.handler.MerossBridgeHandler;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection.MqttVersion;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection.Protocol;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossMqttConnector} class is responsible for the connection to the Meross broker.
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Subscribe to messages asynchronously
 */
@NonNullByDefault
public class MerossMqttConnector implements MqttConnectionObserver {

    private static final int SECURE_WEB_SOCKET_PORT = 443;
    private static final int RECEPTION_TIMEOUT_SECONDS = 5;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final int QOS_AT_MOST_ONCE = 0;
    private static final int QOS_AT_LEAST_ONCE = 1;

    private final Logger logger = LoggerFactory.getLogger(MerossMqttConnector.class);

    private MerossBridgeHandler callback;
    private ScheduledExecutorService scheduler;

    private @Nullable MqttBrokerConnection mqttConnection;
    private @Nullable CompletableFuture<Boolean> stoppedFuture;
    private CompletableFuture<Boolean> connected = new CompletableFuture<>();

    public MerossMqttConnector(MerossBridgeHandler callback, ScheduledExecutorService scheduler) {
        this.callback = callback;
        this.scheduler = scheduler;

        String brokerAddress = MqttMessageBuilder.brokerAddress;
        String clientId = MqttMessageBuilder.clientId;
        String userId = MqttMessageBuilder.userId;
        if (brokerAddress == null || clientId == null || userId == null) {
            logger.debug("MQTT broker not configured");
            return;
        }
        String clearPassword = "%s%s".formatted(MqttMessageBuilder.userId, MqttMessageBuilder.key);
        String hashedPassword = MD5Util.getMD5String(clearPassword);

        MqttBrokerConnection connection = mqttConnection = new MqttBrokerConnection(Protocol.TCP, MqttVersion.V5,
                brokerAddress, SECURE_WEB_SOCKET_PORT, true, clientId);
        connection.setCredentials(userId, hashedPassword);
        connection.setQos(QOS_AT_LEAST_ONCE);
        connection.setKeepAliveInterval(KEEP_ALIVE_SECONDS);
        connection.addConnectionObserver(this);
    }

    /**
     * Start the MQTT connection
     *
     * @throws MqttException
     * @throws InterruptedException
     */
    synchronized public void startConnection() throws MqttException, InterruptedException {
        MqttBrokerConnection mqttConnection = this.mqttConnection;
        if (mqttConnection == null) {
            logger.debug("MQTT broker connection not initialized");
            return;
        }

        // Make sure stop finished before trying to restart
        CompletableFuture<Boolean> future = stoppedFuture;
        if (future != null) {
            try {
                future.get(RECEPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                logger.debug("finished stopping connection");
            } catch (InterruptedException e) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            } catch (ExecutionException | TimeoutException ignore) {
                // ignore
            }
            stoppedFuture = null;
        }

        logger.debug("Starting connection...");
        try {
            connected = new CompletableFuture<>();
            if (!mqttConnection.start().get(RECEPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.debug("error connecting");
                throw new MqttException("Connection execution exception");
            }
        } catch (InterruptedException e) {
            logger.debug("connection interrupted exception");
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            throw new MqttException("Connection interrupted exception");
        } catch (ExecutionException e) {
            logger.debug("connection execution exception", e.getCause());
            throw new MqttException("Connection execution exception");
        } catch (TimeoutException e) {
            logger.debug("connection timeout exception");
            throw new MqttException("Connection timeout exception");
        }
    }

    /**
     * Stop the MQTT connection.
     */
    public void stopConnection() {
        MqttBrokerConnection mqttConnection = this.mqttConnection;
        if (mqttConnection == null) {
            logger.debug("MQTT broker connection not initialized");
            return;
        }

        logger.debug("Stopping connection...");
        connected.complete(false);
        stoppedFuture = mqttConnection.stop();
    }

    /**
     * @param message The mqtt message
     * @param requestTopic The request topic
     * @throws InterruptedException
     */
    synchronized void publishMqttMessage(String requestTopic, byte[] message)
            throws MqttException, InterruptedException {
        MqttBrokerConnection mqttConnection = this.mqttConnection;
        if (mqttConnection == null) {
            logger.debug("MQTT broker connection not initialized");
            return;
        }

        try {
            connected.get(RECEPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            mqttConnection.publish(requestTopic, message, QOS_AT_MOST_ONCE, false);
        } catch (InterruptedException e) {
            logger.debug("connection interrupted exception");
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            throw new MqttException("Connection interrupted exception");
        } catch (ExecutionException e) {
            logger.debug("connection execution exception", e.getCause());
            throw new MqttException("Connection execution exception");
        } catch (TimeoutException e) {
            logger.debug("connection timeout exception");
            throw new MqttException("Connection timeout exception");
        }
    }

    public void addClientUserTopicSubscriber(MqttMessageSubscriber subscriber) {
        addTopicSubscriber(MqttMessageBuilder.buildClientUserTopic(), subscriber);
    }

    public void addClientResponseTopicSubscriber(MqttMessageSubscriber subscriber) {
        addTopicSubscriber(MqttMessageBuilder.buildClientResponseTopic(), subscriber);
    }

    public void addDeviceRequestTopicSubscriber(MqttMessageSubscriber subscriber, String deviceUUID) {
        addTopicSubscriber(MqttMessageBuilder.buildDeviceRequestTopic(deviceUUID), subscriber);
    }

    private void addTopicSubscriber(String topic, MqttMessageSubscriber subscriber) {
        MqttBrokerConnection mqttConnection = this.mqttConnection;
        if (mqttConnection == null) {
            logger.debug("MQTT broker connection not initialized");
            return;
        }

        mqttConnection.subscribe(topic, subscriber);
    }

    public void removeClientUserTopicSubscriber(MqttMessageSubscriber subscriber) {
        removeTopicSubscriber(MqttMessageBuilder.buildClientUserTopic(), subscriber);
    }

    public void removeClientResponseTopicSubscriber(MqttMessageSubscriber subscriber) {
        removeTopicSubscriber(MqttMessageBuilder.buildClientResponseTopic(), subscriber);
    }

    public void removeDeviceRequestTopicSubscriber(MqttMessageSubscriber subscriber, String deviceUUID) {
        removeTopicSubscriber(MqttMessageBuilder.buildDeviceRequestTopic(deviceUUID), subscriber);
    }

    private void removeTopicSubscriber(String topic, MqttMessageSubscriber subscriber) {
        MqttBrokerConnection mqttConnection = this.mqttConnection;
        if (mqttConnection == null) {
            logger.debug("MQTT broker connection not initialized");
            return;
        }

        mqttConnection.unsubscribe(topic, subscriber);
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        logger.debug("Connection state changed: {}", state);
        switch (state) {
            case MqttConnectionState.CONNECTING:
                break;
            case MqttConnectionState.CONNECTED:
                callback.updateBridgeStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                connected.complete(true);
                break;
            case MqttConnectionState.DISCONNECTED:
                callback.updateBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                break;
        }
    }
}
