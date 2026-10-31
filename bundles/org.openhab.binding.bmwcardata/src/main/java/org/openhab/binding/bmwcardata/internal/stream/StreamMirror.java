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
import java.util.concurrent.ScheduledFuture;
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
 * <li>every stream message unmodified on {@code <prefix>raw/<vin>}, retained only if configured, like
 * {@code MQTT_RETAIN} of the bridge</li>
 * <li>the state of the BMW connection as retained JSON {@code {"connected":true|false,"timestamp":<epoch seconds>}} on
 * {@code <prefix>status}, with the last will {@code {"connected":false}}; like the bridge, a disconnect is only
 * published once it lasts for {@value #DISCONNECT_DELAY_SECONDS} seconds</li>
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
    // like STATUS_STABLE_DELAY of the bridge: a disconnect is only published once it lasts, so that short drops of the
    // connection do not let the state of the consumers flap
    static final long DISCONNECT_DELAY_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(StreamMirror.class);
    private final BrokerSettings settings;
    private final MqttBrokerConnection connection;
    private final Clock clock;
    private final Consumer<Boolean> brokerStateListener;
    private final ScheduledExecutorService scheduler;
    // a retained message is replayed to every new subscriber, however old it is
    private final boolean retainMessages;
    // serializes the decisions on the stream state with their publication, so that a delayed disconnect cannot be
    // published after a newer connect; publishing is asynchronous and does not block
    private final Object stateLock = new Object();

    // guarded by stateLock: the published state of the BMW connection, null until known, published again after each
    // reconnect to the local broker
    private @Nullable Boolean streamConnected;
    private @Nullable ScheduledFuture<?> pendingDisconnect;
    private boolean stopped;

    /**
     * @param settings the local broker
     * @param clientId MQTT client id to use
     * @param scheduler scheduler for connection timeouts and the delayed publication of a disconnect
     * @param retainMessages whether the stream messages are published retained; the status always is
     * @param brokerStateListener informed whether the local broker is connected
     */
    public StreamMirror(BrokerSettings settings, String clientId, ScheduledExecutorService scheduler,
            boolean retainMessages, Consumer<Boolean> brokerStateListener) throws BMWCarDataException {
        this(settings, settings.createConnection(clientId), Clock.systemUTC(), scheduler, retainMessages,
                brokerStateListener);
        connection.setTimeoutExecutor(scheduler, CONNECT_TIMEOUT_MILLIS);
        connection
                .setReconnectStrategy(new PeriodicReconnectStrategy(RECONNECT_INTERVAL_MILLIS, FIRST_RECONNECT_MILLIS));
    }

    StreamMirror(BrokerSettings settings, MqttBrokerConnection connection, Clock clock,
            ScheduledExecutorService scheduler, boolean retainMessages, Consumer<Boolean> brokerStateListener) {
        this.settings = settings;
        this.connection = connection;
        this.clock = clock;
        this.scheduler = scheduler;
        this.retainMessages = retainMessages;
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
        synchronized (stateLock) {
            stopped = true;
            cancelPendingDisconnect();
        }
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
        connection.publish(topic, payload, QOS, retainMessages).exceptionally(e -> {
            logger.debug("Mirroring a stream message failed: {}", e.getMessage());
            return false;
        });
    }

    /**
     * Publishes the state of the connection to BMW: a connect right away, a disconnect only once it lasts for
     * {@link #DISCONNECT_DELAY_SECONDS}, and only changes.
     */
    public void publishStreamState(boolean connected) {
        synchronized (stateLock) {
            if (stopped) {
                return;
            }
            if (connected) {
                cancelPendingDisconnect();
                if (!Boolean.TRUE.equals(streamConnected)) {
                    streamConnected = true;
                    publishStatusIfConnected(true);
                }
            } else if (pendingDisconnect == null && !Boolean.FALSE.equals(streamConnected)) {
                pendingDisconnect = scheduler.schedule(this::confirmDisconnect, DISCONNECT_DELAY_SECONDS,
                        TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Publishes right away that the connection to BMW was stopped on purpose, e.g. for a new authorization; unlike a
     * drop reported by {@link #publishStreamState(boolean)}, it is not delayed.
     */
    public void publishStreamStopped() {
        synchronized (stateLock) {
            if (stopped) {
                return;
            }
            cancelPendingDisconnect();
            if (!Boolean.FALSE.equals(streamConnected)) {
                streamConnected = false;
                publishStatusIfConnected(false);
            }
        }
    }

    private void confirmDisconnect() {
        synchronized (stateLock) {
            // a connect in the meantime cleared the pending disconnect
            if (stopped || pendingDisconnect == null) {
                return;
            }
            pendingDisconnect = null;
            streamConnected = false;
            publishStatusIfConnected(false);
        }
    }

    private void cancelPendingDisconnect() {
        ScheduledFuture<?> pending = pendingDisconnect;
        if (pending != null) {
            pending.cancel(false);
            pendingDisconnect = null;
        }
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        switch (state) {
            case CONNECTED -> {
                logger.debug("Connected to the local broker for mirroring the CarData stream");
                synchronized (stateLock) {
                    // replaces the last will of a previous connection; until the state of the stream is known, the
                    // retained disconnected state of the last will or of the last stop stays
                    Boolean known = streamConnected;
                    if (known != null) {
                        publishStatus(known);
                    }
                }
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

    private void publishStatusIfConnected(boolean connected) {
        if (isConnected()) {
            publishStatus(connected);
        }
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
