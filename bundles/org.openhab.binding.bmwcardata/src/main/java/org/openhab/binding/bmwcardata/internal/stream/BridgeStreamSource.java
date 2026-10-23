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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.LogRedaction;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.binding.bmwcardata.internal.stream.StreamMessageParser.ParsedMessage;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Receives the CarData stream from a local MQTT broker that the
 * <a href="https://github.com/dj0abr/bmw-mqtt-bridge">bmw-mqtt-bridge</a> republishes to.
 * <p>
 * The bridge holds the single stream connection BMW allows per account and publishes every message unchanged on
 * {@code <prefix>raw/<vin>}. It reports its own connection to BMW as retained JSON on {@code <prefix>status},
 * {@code {"connected": true|false, "timestamp": ...}}.
 *
 * @author Mark Herwege - Initial contribution
 * @author Michael Weger - Adaptation to the BMW CarData binding
 */
@NonNullByDefault
public class BridgeStreamSource implements StreamSource, MqttMessageSubscriber, MqttConnectionObserver {

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int RECONNECT_INTERVAL_MILLIS = 60_000;
    private static final int FIRST_RECONNECT_MILLIS = 10_000;

    private final Logger logger = LoggerFactory.getLogger(BridgeStreamSource.class);
    private final BrokerSettings settings;
    private final String clientId;
    private final ScheduledExecutorService scheduler;
    private final StreamListener listener;
    private final Object stateLock = new Object();

    private @Nullable MqttBrokerConnection connection;
    private volatile boolean stopped = true;
    private volatile boolean localConnected;
    private volatile @Nullable Boolean upstreamConnected;
    private volatile @Nullable StreamState lastState;

    /**
     * @param settings the local broker the bridge publishes to
     * @param clientId MQTT client id to use
     * @param scheduler scheduler for connection timeouts
     * @param listener receiver of data and state changes
     */
    public BridgeStreamSource(BrokerSettings settings, String clientId, ScheduledExecutorService scheduler,
            StreamListener listener) {
        this.settings = settings;
        this.clientId = clientId;
        this.scheduler = scheduler;
        this.listener = listener;
    }

    @Override
    public synchronized void start() {
        if (!stopped) {
            return;
        }
        stopped = false;
        localConnected = false;
        upstreamConnected = null;
        lastState = null;

        MqttBrokerConnection connection;
        try {
            connection = settings.createConnection(clientId);
        } catch (BMWCarDataException e) {
            publishState(e.getMessage());
            return;
        }
        connection.setTimeoutExecutor(scheduler, CONNECT_TIMEOUT_MILLIS);
        connection
                .setReconnectStrategy(new PeriodicReconnectStrategy(RECONNECT_INTERVAL_MILLIS, FIRST_RECONNECT_MILLIS));
        connection.addConnectionObserver(this);
        // the subscriptions are registered before connecting and restored by the connection after every reconnect
        connection.subscribe(settings.rawTopicFilter(), this);
        connection.subscribe(settings.statusTopic(), this);
        this.connection = connection;
        publishState(null);
        logger.debug("Connecting to the local broker {}:{} for the CarData stream topics {} and {}", settings.host(),
                settings.port(), settings.rawTopicFilter(), settings.statusTopic());
        connection.start().exceptionally(e -> {
            connectionStateChanged(MqttConnectionState.DISCONNECTED, e);
            return false;
        });
    }

    @Override
    public synchronized CompletableFuture<Boolean> stop() {
        synchronized (stateLock) {
            // waits for a state callback in progress, later ones see the flag
            stopped = true;
        }
        MqttBrokerConnection connection = this.connection;
        this.connection = null;
        if (connection == null) {
            return CompletableFuture.completedFuture(true);
        }
        connection.removeConnectionObserver(this);
        connection.unsubscribe(settings.rawTopicFilter(), this);
        connection.unsubscribe(settings.statusTopic(), this);
        return connection.stop();
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        if (stopped) {
            return;
        }
        localConnected = state == MqttConnectionState.CONNECTED;
        if (!localConnected) {
            // the retained status message is delivered again on reconnect
            upstreamConnected = null;
        }
        publishState(error != null ? error.getMessage() : null);
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        if (stopped) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Bridge message on {}: {}", LogRedaction.vins(topic),
                    LogRedaction.payload(new String(payload, StandardCharsets.UTF_8)));
        }
        if (topic.equals(settings.statusTopic())) {
            Boolean connected = parseStatus(payload);
            if (connected != null) {
                upstreamConnected = connected;
                publishState(null);
            }
            return;
        }
        ParsedMessage message = StreamMessageParser.parse(topic, payload);
        if (message == null) {
            logger.debug("Ignoring a message of the bridge that is not a CarData envelope with data");
            return;
        }
        if (!Boolean.TRUE.equals(upstreamConnected)) {
            // data is flowing, so the bridge is connected even if its status message was missed
            upstreamConnected = true;
            publishState(null);
        }
        logger.debug("Received stream data from the bridge: {}", message.data().keySet());
        listener.onStreamData(message.vin(), message.data());
    }

    /**
     * Reports the current state if it changed. State changes come from several threads of the MQTT client; a dedicated
     * lock keeps the reported sequence consistent with the latest state.
     */
    private void publishState(@Nullable String detail) {
        synchronized (stateLock) {
            if (stopped) {
                return;
            }
            StreamState state;
            Boolean upstream = upstreamConnected;
            if (!localConnected) {
                state = StreamState.DISCONNECTED;
            } else if (upstream == null) {
                state = StreamState.CONNECTED_UPSTREAM_UNKNOWN;
            } else {
                state = upstream ? StreamState.CONNECTED : StreamState.UPSTREAM_DISCONNECTED;
            }
            if (state != lastState || detail != null) {
                lastState = state;
                listener.onStreamStateChanged(state, detail);
            }
        }
    }

    /**
     * @return the BMW connection state reported by the bridge, {@code null} if the payload is not a status message
     */
    static @Nullable Boolean parseStatus(byte[] payload) {
        String text = new String(payload, StandardCharsets.UTF_8).strip();
        try {
            JsonElement json = JsonParser.parseString(text);
            if (json.isJsonObject()) {
                JsonElement connected = json.getAsJsonObject().get("connected");
                if (connected != null && connected.isJsonPrimitive()) {
                    return Boolean.parseBoolean(connected.getAsString());
                }
                return null;
            }
            if (json.isJsonPrimitive()) {
                return Boolean.parseBoolean(json.getAsString());
            }
        } catch (JsonParseException e) {
            // not JSON
        }
        return null;
    }
}
