/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.worxlandroid.internal.mqtt;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.mqtt.MqttException;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.OnConnectionFailureReturn;
import software.amazon.awssdk.crt.mqtt.OnConnectionSuccessReturn;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

/**
 * {@link AWSClient} AWS client
 *
 * @author Nils - Initial contribution
 */
@NonNullByDefault
public class AWSClient implements MqttClientConnectionEvents {
    private static final QualityOfService QOS = QualityOfService.AT_MOST_ONCE;
    private static final String AUTHORIZER_NAME = "com-worxlandroid-customer";
    private static final String MQTT_USERNAME = "openhab";

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("AWSClient");
    private final Map<String, Consumer<MqttMessage>> subscriptions = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(AWSClient.class);
    private final AWSClientCallbackI clientCallback;

    private @Nullable MqttClientConnection mqttClient;
    private LocalDateTime lastResumed = LocalDateTime.MIN;
    private boolean connected;

    public AWSClient(AWSClientCallbackI clientCallback) {
        this.clientCallback = clientCallback;
    }

    public void connect(String endpoint, String userId, String productUuid, String token) {
        String[] tok = token.replaceAll("_", "/").replaceAll("-", "+").split("\\.");

        try {
            MqttClientConnection connection = AwsIotMqttConnectionBuilder.newDefaultBuilder()
                    // .withCustomAuthorizer(MQTT_USERNAME, AUTHORIZER_NAME, tok[2], null, MQTT_USERNAME, token)
                    .withClientId("WX/USER/%s/%s/%s".formatted(userId, MQTT_USERNAME, productUuid))
                    .withEndpoint(endpoint).withUsername(MQTT_USERNAME).withCleanSession(false).withKeepAliveSecs(300)
                    .withConnectionEventCallbacks(this).withWebsockets(true)
                    .withWebsocketHandshakeTransform(handshakeArgs -> {
                        HttpRequest httpRequest = handshakeArgs.getHttpRequest();
                        httpRequest.addHeader("x-amz-customauthorizer-name", AUTHORIZER_NAME);
                        httpRequest.addHeader("x-amz-customauthorizer-signature", tok[2]);
                        httpRequest.addHeader("jwt", tok[0] + "." + tok[1]);
                        handshakeArgs.complete(httpRequest);
                    }).build();
            connection.connect().get();
            this.mqttClient = connection;
        } catch (MqttException | UnsupportedEncodingException | InterruptedException | ExecutionException e) {
            clientCallback.onAWSConnectionFailed(e.getMessage());
        }
    }

    public void dispose() {
        disconnect();
        subscriptions.clear();
    }

    @Override
    public void onConnectionSuccess(@NonNullByDefault({}) OnConnectionSuccessReturn data) {
        onConnectionResumed(data.getSessionPresent());
    }

    @Override
    public void onConnectionResumed(boolean sessionPresent) {
        connected = sessionPresent;
        if (sessionPresent) {
            lastResumed = LocalDateTime.now();
            logger.debug("last connection resume {}", lastResumed);
            subscriptions.forEach(this::subscribe);
            clientCallback.onAWSConnectionSuccess();
        } else {
            clientCallback.onAWSConnectionClosed();
        }
    }

    @Override
    public void onConnectionInterrupted(int errorCode) {
        LocalDateTime interrupted = LocalDateTime.now();
        connected = false;
        String error = CRT.awsErrorString(errorCode);
        logger.debug("connection interrupted errorcode: {} : {}", errorCode, error);

        scheduler.schedule(() -> {
            /**
             * workaround -> after 20 minutes the connection is interrupted but immediately resumed (~0,5sec).
             * ConnectionBuilder with ".withKeepAliveSecs(300)" doesn't work
             */
            boolean isBetween = lastResumed.isAfter(interrupted) && lastResumed.isBefore(LocalDateTime.now());
            logger.debug("lastResumed: {}  interrupted: {} in: {}", lastResumed, interrupted, isBetween);
            if (!isBetween) {
                clientCallback.onAWSConnectionClosed();
            }
        }, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onConnectionFailure(@NonNullByDefault({}) OnConnectionFailureReturn data) {
        connected = false;
        if (data.getErrorCode() == 5134) {
            clientCallback.onAWSConnectionFailed("Error code 5134: banned 24h");
        } else {
            logger.debug("{}", data.toString());
            clientCallback.onAWSConnectionClosed();
        }
    };

    public void disconnect() {
        MqttClientConnection connection = mqttClient;
        if (connection != null) {
            connection.disconnect();
            connection.close();
            mqttClient = null;
        }
        connected = false;
    }

    public void subscribe(String topic, Consumer<MqttMessage> handler) {
        MqttClientConnection connection = mqttClient;
        if (connection != null) {
            subscriptions.put(topic, handler);
            connection.subscribe(topic, QOS, handler);
        } else {
            logger.warn("Tried to subscribe on {} when connection is closed", topic);
        }
    }

    public void unsubscribe(String topic) {
        MqttClientConnection connection = mqttClient;
        if (connection != null) {
            subscriptions.remove(topic);
            connection.unsubscribe(topic);
        } else {
            logger.warn("Tried to unsubscribe from {} when connection is closed", topic);
        }
    }

    public void publish(String topic, String payload) {
        MqttClientConnection connection = mqttClient;
        if (connection != null) {
            connection.publish(new MqttMessage(topic, payload.getBytes(StandardCharsets.UTF_8), QOS));
        } else {
            logger.warn("Tried to publish on {} when connection is closed", topic);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
