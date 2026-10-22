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
package org.openhab.binding.bmwcardata.internal.api;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue.StreamMessage;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection.MqttVersion;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection.Protocol;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link BMWCarDataStreamClient} receives telematic data pushed by the BMW CarData streaming broker.
 * The broker authenticates with the customer id ({@code gcid}) as user name and the id token as password and publishes
 * the data of each vehicle on topic {@code <gcid>/<vin>}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataStreamClient implements MqttMessageSubscriber, MqttConnectionObserver {

    private static final String HOST = "customer.streaming-cardata.bmwgroup.com";
    private static final int PORT = 9000;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final int CONNECT_TIMEOUT_MILLIS = 15_000;
    private static final int RECONNECT_INTERVAL_MILLIS = 60_000;
    private static final int FIRST_RECONNECT_MILLIS = 10_000;

    /**
     * Receives the data and connection changes of the stream.
     */
    public interface Listener {
        void onStreamData(String vin, Map<String, TelematicValue> data);

        void onStreamConnectionChanged(boolean connected, @Nullable String reason);
    }

    private final Logger logger = LoggerFactory.getLogger(BMWCarDataStreamClient.class);
    private final Gson gson;
    private final Listener listener;
    private final String topic;
    private final MqttBrokerConnection connection;

    public BMWCarDataStreamClient(Tokens tokens, Gson gson, ScheduledExecutorService scheduler, Listener listener)
            throws BMWCarDataException {
        this.gson = gson;
        this.listener = listener;
        this.topic = tokens.gcid() + "/+";

        MqttBrokerConnection connection = new MqttBrokerConnection(Protocol.TCP, MqttVersion.V3, HOST, PORT, true, true,
                tokens.gcid());
        // the connection trusts any certificate unless trust managers are set explicitly
        connection.setTrustManagers(defaultTrustManagerFactory().getTrustManagers());
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
    public void start() {
        connection.subscribe(topic, this);
        connection.start().whenComplete((connected, error) -> {
            if (error != null || !Boolean.TRUE.equals(connected)) {
                logger.debug("Initial connection to BMW CarData streaming failed: {}",
                        error != null ? error.getMessage() : "timeout");
            }
        });
    }

    /**
     * Disconnects from the broker.
     *
     * @return a future completed when the connection is closed
     */
    public CompletableFuture<Boolean> stop() {
        connection.removeConnectionObserver(this);
        return connection.stop();
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        StreamMessage message;
        try {
            message = gson.fromJson(new String(payload, StandardCharsets.UTF_8), StreamMessage.class);
        } catch (JsonParseException e) {
            logger.debug("Ignoring malformed BMW CarData stream message: {}", e.getMessage());
            return;
        }
        if (message == null) {
            return;
        }
        String vin = message.vin;
        if (vin == null) {
            vin = topic.substring(topic.lastIndexOf('/') + 1);
        }
        Map<String, TelematicValue> data = message.data;
        if (data != null && !data.isEmpty()) {
            logger.debug("Received stream data: {}", data.keySet());
            listener.onStreamData(vin, data);
        }
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        switch (state) {
            case CONNECTED -> listener.onStreamConnectionChanged(true, null);
            case DISCONNECTED -> listener.onStreamConnectionChanged(false, error != null ? error.getMessage() : null);
            case CONNECTING -> {
                // wait for the outcome of the connection attempt
            }
        }
    }

    private static TrustManagerFactory defaultTrustManagerFactory() throws BMWCarDataException {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            return factory;
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new BMWCarDataException("Unable to initialize TLS trust store", e);
        }
    }
}
