/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.dto.CloudCredentials;
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

    private static final int SECURE_TCP_PORT = 443;
    private static final int RECEPTION_TIMEOUT_SECONDS = 5;
    private static final int CONNECTION_TIMEOUT_SECONDS = 5;
    private static final int MQTT_DISCONNECT_DELAY_SECONDS = 60;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final int QOS_AT_MOST_ONCE = 0;
    private static final int QOS_AT_LEAST_ONCE = 1;

    private final Logger logger = LoggerFactory.getLogger(MerossMqttConnector.class);

    private MerossBridgeHandler callback;
    private ScheduledExecutorService scheduler;

    private MqttMessageBuilder mqttMessageBuilder = new MqttMessageBuilder();
    private @Nullable MqttBrokerConnection mqttConnection;
    private @Nullable CompletableFuture<Boolean> stoppedFuture;
    private @Nullable ScheduledFuture<?> disconnectFuture;
    private CompletableFuture<Boolean> connected = new CompletableFuture<>();

    public MerossMqttConnector(MerossBridgeHandler callback, CloudCredentials credentials,
            ScheduledExecutorService scheduler) {
        this.callback = callback;
        this.scheduler = scheduler;

        String userId = credentials.userId();
        String key = credentials.key();
        String brokerAddress = credentials.mqttDomain();

        if (brokerAddress == null || key == null || userId == null) {
            logger.debug("MQTT broker not configured");
            return;
        }
        mqttMessageBuilder.setUserId(userId);
        mqttMessageBuilder.setKey(key);

        String clearPassword = "%s%s".formatted(userId, key);
        String hashedPassword = MD5Util.getMD5String(clearPassword);
        String clientId = mqttMessageBuilder.getClientId();

        MqttBrokerConnection connection = mqttConnection = new MqttBrokerConnection(Protocol.TCP, MqttVersion.V3,
                brokerAddress, SECURE_TCP_PORT, true, clientId);
        connection.setTimeoutExecutor(scheduler, CONNECTION_TIMEOUT_SECONDS * 1000);
        connection.setCredentials(userId, hashedPassword);
        connection.setQos(QOS_AT_LEAST_ONCE);
        connection.setKeepAliveInterval(KEEP_ALIVE_SECONDS);
        connection.setCleanSessionStart(false);
        connection.addConnectionObserver(this);
    }

    /**
     * Get the mqtt message builder for this connection.
     *
     * @return the mqtt message builder
     */
    public MqttMessageBuilder getMqttMessageBuilder() {
        return mqttMessageBuilder;
    }

    /**
     * Start the MQTT connection
     *
     * @throws MqttException
     * @throws InterruptedException
     */
    public synchronized void startConnection() throws MqttException, InterruptedException {
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
        logger.trace("Connecting with clientId: {}", mqttMessageBuilder.getClientId());
        try {
            connected = mqttConnection.start();
            if (!connected.get(RECEPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
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
        ScheduledFuture<?> disconnectFuture = this.disconnectFuture;
        if (disconnectFuture != null) {
            disconnectFuture.cancel(true);
            this.disconnectFuture = null;
        }
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
            if (connected.get(RECEPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                mqttConnection.publish(requestTopic, message, QOS_AT_MOST_ONCE, false);
            } else {
                throw new MqttException("Disconnected exception");
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

    public void addClientUserTopicSubscriber(MqttMessageSubscriber subscriber) {
        addTopicSubscriber(mqttMessageBuilder.buildClientUserTopic(), subscriber);
    }

    public void addClientResponseTopicSubscriber(MqttMessageSubscriber subscriber) {
        addTopicSubscriber(mqttMessageBuilder.buildClientResponseTopic(), subscriber);
    }

    public void addDeviceRequestTopicSubscriber(MqttMessageSubscriber subscriber, String deviceUUID) {
        addTopicSubscriber(mqttMessageBuilder.buildDeviceRequestTopic(deviceUUID), subscriber);
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
        removeTopicSubscriber(mqttMessageBuilder.buildClientUserTopic(), subscriber);
    }

    public void removeClientResponseTopicSubscriber(MqttMessageSubscriber subscriber) {
        removeTopicSubscriber(mqttMessageBuilder.buildClientResponseTopic(), subscriber);
    }

    public void removeDeviceRequestTopicSubscriber(MqttMessageSubscriber subscriber, String deviceUUID) {
        removeTopicSubscriber(mqttMessageBuilder.buildDeviceRequestTopic(deviceUUID), subscriber);
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
        logger.trace("Connection state changed: {}", state);
        switch (state) {
            case CONNECTING:
                break;
            case CONNECTED:
                ScheduledFuture<?> disconnectFuture = this.disconnectFuture;
                if (disconnectFuture != null) {
                    disconnectFuture.cancel(true);
                    this.disconnectFuture = null;
                }
                callback.updateBridgeStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                connected.complete(true);
                break;
            case DISCONNECTED:
                connected = new CompletableFuture<>();
                // The transport tries to reconnect anyway. If we put the bridge offline immediately, it will trigger a
                // re-initialization of all devices creating a lot of traffic. Devices can still be commanded through
                // local http connections even if the connection to the Meross MQTT broker is disrupted. So don't put
                // the bridge offline immediately.
                logger.debug("Disconnected", error);
                if (this.disconnectFuture == null) {
                    this.disconnectFuture = scheduler.schedule(() -> {
                        callback.updateBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    }, MQTT_DISCONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
                }
                break;
        }
    }
}
