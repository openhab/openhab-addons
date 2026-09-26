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
package org.openhab.binding.lghorizon.internal.mqtt;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lghorizon.internal.LGHorizonContentAnonymizer;
import org.openhab.binding.lghorizon.internal.api.LGHorizonApiException;
import org.openhab.binding.lghorizon.internal.api.LGHorizonAuthClient;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Wraps an openHAB {@link MqttBrokerConnection} to talk to the LG Horizon "obomsg" push broker over
 * MQTT-over-WebSockets (wss://.../mqtt on port 443):
 * <ul>
 * <li>host/path come from the {@code mqttBroker} entry of the service discovery document.</li>
 * <li>username = household id, password = short-lived MQTT token from {@link LGHorizonAuthClient#getMqttToken()}</li>
 * <li>reconnects (including refreshing the token first) are handled by {@link LGHorizonReconnectStrategy}</li>
 * </ul>
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class LGHorizonMqttClient implements MqttMessageSubscriber, MqttConnectionObserver {

    private static final String WEBSOCKET_PATH = "/mqtt";
    private static final int CONNECT_TIMEOUT_SECONDS = 15;

    private final Logger logger = LoggerFactory.getLogger(LGHorizonMqttClient.class);

    private final LGHorizonAuthClient authClient;
    private final LGHorizonMqttListener listener;
    private final String clientId;
    private final MqttBrokerConnection connection;

    /**
     * @param authClient
     * @param listener
     * @param scheduler
     * @throws LGHorizonApiException if the MQTT broker URL cannot be parsed or the connection cannot be established
     */
    public LGHorizonMqttClient(LGHorizonAuthClient authClient, LGHorizonMqttListener listener,
            ScheduledExecutorService scheduler) throws LGHorizonApiException {
        this.authClient = authClient;
        this.listener = listener;
        this.clientId = randomId(10);

        String mqttBrokerUrl = authClient.getServiceConfig().getServiceUrl("mqttBroker");
        String host = extractHost(mqttBrokerUrl);

        MqttBrokerConnection conn = new MqttBrokerConnection(MqttBrokerConnection.Protocol.WEBSOCKETS,
                MqttBrokerConnection.MqttVersion.V3, host, 443, true, clientId);
        conn.setWebSocketPath(WEBSOCKET_PATH);
        conn.setCleanSessionStart(true);
        conn.setKeepAliveInterval(30);
        conn.setTimeoutExecutor(scheduler, CONNECT_TIMEOUT_SECONDS * 1000);
        conn.setReconnectStrategy(new LGHorizonReconnectStrategy(authClient));
        conn.addConnectionObserver(this);
        this.connection = conn;
    }

    /**
     * @return the random client id used for this MQTT connection (10 alphanumeric characters)
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Fetches a fresh MQTT token and (re)connects. Blocks the calling thread until the connection is established or
     * {@link #CONNECT_TIMEOUT_SECONDS} elapses; callers should invoke this from a background thread (e.g. the handler's
     * own scheduler), never from the openHAB event bus thread.
     *
     * @throws LGHorizonApiException if the connection fails or times out
     */
    public void connect() throws LGHorizonApiException {
        String householdId = authClient.getHouseholdId();
        if (householdId == null) {
            throw new LGHorizonApiException("Not authenticated yet: no household id available");
        }
        String mqttToken = authClient.getMqttToken();
        connection.setCredentials(householdId, mqttToken);
        try {
            Boolean connected = connection.start().get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(connected)) {
                throw new LGHorizonApiException("Timed out connecting to the LG Horizon MQTT broker");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LGHorizonApiException("Interrupted while connecting to the LG Horizon MQTT broker", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new LGHorizonApiException("Unable to connect to the LG Horizon MQTT broker", e);
        }
    }

    /**
     * Subscribes to a topic filter (may contain {@code +}/{@code #} wildcards).
     *
     * @param topicFilter the topic filter to subscribe to
     */
    public void subscribe(String topicFilter) {
        connection.subscribe(topicFilter, this).exceptionally(e -> {
            logger.debug("Failed to subscribe to LG Horizon MQTT topic '{}': {}",
                    LGHorizonContentAnonymizer.anonymizeTopic(topicFilter), e.getMessage());
            return false;
        });
    }

    /**
     * Subscribes to all the topics the set-top boxes and account-level services publish on.
     *
     * @param householdId the household id to subscribe to
     */
    public void subscribeAccountTopics(String householdId) {
        subscribe(householdId);
        subscribe(householdId + "/" + clientId);
        subscribe(householdId + "/+/status");
        subscribe(householdId + "/watchlistService");
        subscribe(householdId + "/personalizationService");
        subscribe(householdId + "/recordingStatus");
    }

    /**
     * Publishes a JSON payload with QoS 1. Fire-and-forget: failures are logged but not retried (the box will simply
     * not react, same as if it were offline).
     *
     * @param topic the topic to publish to
     * @param payload the JSON payload to publish
     */
    public void publish(String topic, JsonObject payload) {
        if (connection.connectionState() != MqttConnectionState.CONNECTED) {
            logger.debug("Not connected to the LG Horizon MQTT broker, dropping publish to {}",
                    LGHorizonContentAnonymizer.anonymizeTopic(topic));
            return;
        }
        byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
        connection.publish(topic, bytes, 1, false).exceptionally(e -> {
            logger.warn("Failed to publish to LG Horizon MQTT topic '{}': {}",
                    LGHorizonContentAnonymizer.anonymizeTopic(topic), e.getMessage());
            return false;
        });
    }

    /**
     * @return true if the MQTT connection is currently established, false otherwise
     */
    public boolean isConnected() {
        return connection.connectionState() == MqttConnectionState.CONNECTED;
    }

    /**
     * Stops the MQTT connection and releases any resources. After calling this method, the client cannot be reused
     */
    public void disconnect() {
        connection.stop();
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        try {
            String json = new String(payload, StandardCharsets.UTF_8);
            JsonObject parsed = JsonParser.parseString(json).getAsJsonObject();
            listener.onMessage(topic, parsed);
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.debug("Ignoring non-JSON or malformed MQTT message on topic {}: {}",
                    LGHorizonContentAnonymizer.anonymizeTopic(topic), e.getMessage());
        }
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        switch (state) {
            case CONNECTED -> listener.onConnected();
            case DISCONNECTED -> {
                String message = error != null ? error.getMessage() : "unknown reason";
                logger.debug("Lost connection to LG Horizon MQTT broker: {}",
                        LGHorizonContentAnonymizer.anonymizeMessage(message), error);
                listener.onConnectionLost(error != null ? error : new Exception("unknown reason"));
            }
            case CONNECTING -> {
                // no-op: nothing for the account handler to react to yet
            }
        }
    }

    private static String extractHost(String mqttBrokerUrl) {
        try {
            URI uri = new URI(mqttBrokerUrl.contains("://") ? mqttBrokerUrl : "wss://" + mqttBrokerUrl);
            String host = uri.getHost();
            if (host != null) {
                return host;
            }
        } catch (URISyntaxException ignored) {
            // fall through to the manual strip below
        }
        return mqttBrokerUrl.replaceFirst("^\\w+://", "").replaceFirst("[:/].*$", "");
    }

    private static String randomId(int length) {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }
        return sb.toString();
    }
}
