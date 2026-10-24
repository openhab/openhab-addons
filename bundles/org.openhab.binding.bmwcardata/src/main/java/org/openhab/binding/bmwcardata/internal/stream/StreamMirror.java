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
package org.openhab.binding.bmwcardata.internal.stream;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttWillAndTestament;
import org.openhab.core.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link StreamMirror} republishes the direct CarData stream to a local MQTT broker in the topic format of the
 * <a href="https://github.com/dj0abr/bmw-mqtt-bridge">bmw-mqtt-bridge</a>, so that other tools, and this binding in
 * bridge mode, can share the single stream connection BMW allows per account:
 * <ul>
 * <li>every stream message unmodified and retained on {@code <prefix>raw/<vin>}</li>
 * <li>the state of the BMW connection as retained JSON {@code {"connected":true|false,"timestamp":<epoch seconds>}} on
 * {@code <prefix>status}, with the last will {@code {"connected":false}}</li>
 * </ul>
 * Publishing never blocks the stream: while the local broker is not connected, messages are dropped instead of
 * buffered, as the next message supersedes them anyway. The connection to the local broker reconnects on its own.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class StreamMirror implements MqttConnectionObserver {

    // as published by the bmw-mqtt-bridge
    static final int QOS = 0;
    static final byte[] DISCONNECTED_PAYLOAD = "{\"connected\":false}".getBytes(StandardCharsets.UTF_8);

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int RECONNECT_INTERVAL_MILLIS = 60_000;
    private static final int FIRST_RECONNECT_MILLIS = 10_000;
    private static final long STOP_TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(StreamMirror.class);
    private final BrokerSettings settings;
    private final MqttBrokerConnection connection;
    private final Clock clock;
    private final Consumer<Boolean> brokerStateListener;

    // last known state of the BMW connection, published again after each reconnect to the local broker
    private volatile boolean streamConnected;

    /**
     * @param settings the local broker
     * @param clientId MQTT client id to use
     * @param scheduler scheduler for connection timeouts
     * @param brokerStateListener informed whether the local broker is connected
     */
    public StreamMirror(BrokerSettings settings, String clientId, ScheduledExecutorService scheduler,
            Consumer<Boolean> brokerStateListener) throws BMWCarDataException {
        this(settings, settings.createConnection(clientId), Clock.systemUTC(), brokerStateListener);
        connection.setTimeoutExecutor(scheduler, CONNECT_TIMEOUT_MILLIS);
        connection
                .setReconnectStrategy(new PeriodicReconnectStrategy(RECONNECT_INTERVAL_MILLIS, FIRST_RECONNECT_MILLIS));
    }

    StreamMirror(BrokerSettings settings, MqttBrokerConnection connection, Clock clock,
            Consumer<Boolean> brokerStateListener) {
        this.settings = settings;
        this.connection = connection;
        this.clock = clock;
        this.brokerStateListener = brokerStateListener;
        // consumers notice when openHAB disappears without stopping the mirror
        connection.setLastWill(new MqttWillAndTestament(settings.statusTopic(), DISCONNECTED_PAYLOAD, QOS, true));
        connection.addConnectionObserver(this);
    }

    public void start() {
        logger.debug("Mirroring the CarData stream to the local broker {}:{} with topic prefix {}", settings.host(),
                settings.port(), settings.topicPrefix());
        connection.start().whenComplete((connected, error) -> {
            if (error != null || !Boolean.TRUE.equals(connected)) {
                logger.debug("Initial connection to the local broker failed: {}",
                        error != null ? error.getMessage() : "timeout");
            }
        });
    }

    /**
     * Reports that openHAB stops mirroring, as a clean disconnect does not trigger the last will, and disconnects.
     */
    public CompletableFuture<Boolean> stop() {
        connection.removeConnectionObserver(this);
        CompletableFuture<Boolean> published = isConnected()
                ? connection.publish(settings.statusTopic(), DISCONNECTED_PAYLOAD, QOS, true)
                : CompletableFuture.completedFuture(true);
        return published.orTimeout(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS).handle((result, error) -> null)
                .thenCompose(nothing -> connection.stop());
    }

    /**
     * Republishes a message received from the BMW stream.
     *
     * @param streamTopic topic of the message at the BMW broker, {@code <gcid>/<vin>}
     * @param payload payload of the message, published unmodified
     */
    public void publishMessage(String streamTopic, byte[] payload) {
        if (!isConnected()) {
            return;
        }
        // like the bridge: the gcid level is replaced by <prefix>raw
        int slash = streamTopic.indexOf('/');
        String topic = settings.topicPrefix() + "raw" + (slash >= 0 ? streamTopic.substring(slash) : "");
        connection.publish(topic, payload, QOS, true).exceptionally(e -> {
            logger.debug("Mirroring a stream message failed: {}", e.getMessage());
            return false;
        });
    }

    /**
     * Publishes the state of the connection to BMW.
     */
    public void publishStreamState(boolean connected) {
        streamConnected = connected;
        if (isConnected()) {
            publishStatus(connected);
        }
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        switch (state) {
            case CONNECTED -> {
                logger.debug("Connected to the local broker for mirroring the CarData stream");
                // replaces the last will of a previous connection
                publishStatus(streamConnected);
                brokerStateListener.accept(true);
            }
            case DISCONNECTED -> {
                logger.debug("Disconnected from the local broker for mirroring the CarData stream: {}",
                        error != null ? error.getMessage() : "unknown reason");
                brokerStateListener.accept(false);
            }
            case CONNECTING -> {
                // wait for the outcome of the connection attempt
            }
        }
    }

    private boolean isConnected() {
        return connection.connectionState() == MqttConnectionState.CONNECTED;
    }

    private void publishStatus(boolean connected) {
        String status = "{\"connected\":" + connected + ",\"timestamp\":" + clock.instant().getEpochSecond() + "}";
        connection.publish(settings.statusTopic(), status.getBytes(StandardCharsets.UTF_8), QOS, true)
                .exceptionally(e -> {
                    logger.debug("Publishing the stream state failed: {}", e.getMessage());
                    return false;
                });
    }
}
