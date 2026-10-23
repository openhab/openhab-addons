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
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.LogRedaction;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.binding.bmwcardata.internal.api.Tokens;
import org.openhab.binding.bmwcardata.internal.stream.StreamMessageParser.ParsedMessage;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection.MqttVersion;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection.Protocol;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BMWCarDataStreamClient} receives telematic data pushed by the BMW CarData streaming broker.
 * The broker authenticates with the customer id ({@code gcid}) as user name and the id token as password and publishes
 * the data of each vehicle on topic {@code <gcid>/<vin>}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataStreamClient implements StreamSource, MqttMessageSubscriber, MqttConnectionObserver {

    private static final String HOST = "customer.streaming-cardata.bmwgroup.com";
    private static final int PORT = 9000;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final int CONNECT_TIMEOUT_MILLIS = 15_000;
    private static final int RECONNECT_INTERVAL_MILLIS = 60_000;
    private static final int FIRST_RECONNECT_MILLIS = 10_000;

    private final Logger logger = LoggerFactory.getLogger(BMWCarDataStreamClient.class);
    private final StreamListener listener;
    private final @Nullable BiConsumer<String, byte[]> rawListener;
    private final String topic;
    private final MqttBrokerConnection connection;

    /**
     * @param tokens tokens with the stream credentials
     * @param scheduler scheduler for connection timeouts
     * @param listener receiver of data and state changes
     * @param rawListener receives every message unmodified with its topic, e.g. to mirror the stream; it must not
     *            block
     */
    public BMWCarDataStreamClient(Tokens tokens, ScheduledExecutorService scheduler, StreamListener listener,
            @Nullable BiConsumer<String, byte[]> rawListener) throws BMWCarDataException {
        this.listener = listener;
        this.rawListener = rawListener;
        this.topic = tokens.gcid() + "/+";

        MqttBrokerConnection connection = new MqttBrokerConnection(Protocol.TCP, MqttVersion.V3, HOST, PORT, true, true,
                tokens.gcid());
        connection.setTrustManagers(BrokerSettings.defaultTrustManagers());
        connection.setCredentials(tokens.gcid(), tokens.idToken());
        connection.setKeepAliveInterval(KEEP_ALIVE_SECONDS);
        connection.setCleanSessionStart(true);
        connection.setTimeoutExecutor(scheduler, CONNECT_TIMEOUT_MILLIS);
        connection
                .setReconnectStrategy(new PeriodicReconnectStrategy(RECONNECT_INTERVAL_MILLIS, FIRST_RECONNECT_MILLIS));
        connection.addConnectionObserver(this);
        this.connection = connection;
    }

    /**
     * Connects to the broker and subscribes to the data of all vehicles of the customer. The subscription is restored
     * by the connection after each reconnect.
     */
    @Override
    public void start() {
        connection.subscribe(topic, this);
        connection.start().whenComplete((connected, error) -> {
            if (error != null || !Boolean.TRUE.equals(connected)) {
                logger.debug("Initial connection to BMW CarData streaming failed: {}",
                        error != null ? error.getMessage() : "timeout");
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> stop() {
        connection.removeConnectionObserver(this);
        return connection.stop();
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        if (logger.isTraceEnabled()) {
            logger.trace("BMW CarData stream message on {}: {}", LogRedaction.vins(topic),
                    LogRedaction.payload(new String(payload, StandardCharsets.UTF_8)));
        }
        BiConsumer<String, byte[]> rawListener = this.rawListener;
        if (rawListener != null) {
            try {
                rawListener.accept(topic, payload);
            } catch (RuntimeException e) {
                // a failure of the mirror must not stop the processing of the stream
                logger.debug("Forwarding a stream message failed: {}", e.getMessage());
            }
        }
        ParsedMessage message = StreamMessageParser.parse(topic, payload);
        if (message == null) {
            logger.debug("Ignoring BMW CarData stream message without data");
            return;
        }
        logger.debug("Received stream data: {}", message.data().keySet());
        listener.onStreamData(message.vin(), message.data());
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        switch (state) {
            case CONNECTED -> listener.onStreamStateChanged(StreamState.CONNECTED, null);
            case DISCONNECTED ->
                listener.onStreamStateChanged(StreamState.DISCONNECTED, error != null ? error.getMessage() : null);
            case CONNECTING -> {
                // wait for the outcome of the connection attempt
            }
        }
    }
}
