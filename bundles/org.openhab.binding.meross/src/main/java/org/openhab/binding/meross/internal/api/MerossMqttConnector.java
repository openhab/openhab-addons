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

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meross.internal.dto.MqttMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;

/**
 * The {@link MerossMqttConnector} class is responsible for publishing
 * MQTT messages to the Meross broker.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossMqttConnector {
    private static final Logger logger = LoggerFactory.getLogger(MerossMqttConnector.class);
    private static final int SECURE_WEB_SOCKET_PORT = 443;
    private static final int RECEPTION_TIMEOUT_SECONDS = 5;
    private static final int KEEP_ALIVE_SECONDS = 1;

    /**
     * @param message The mqtt message
     * @param requestTopic The request topic
     * @return The mqtt response
     */
    static synchronized String publishMqttMessage(byte[] message, String requestTopic) throws ConnectException {
        String clearPassword = "%s%s".formatted(MqttMessageBuilder.userId, MqttMessageBuilder.key);
        String hashedPassword = MD5Util.getMD5String(clearPassword);

        Mqtt5BlockingClient client = Mqtt5Client.builder().identifier(MqttMessageBuilder.clientId)
                .serverHost(MqttMessageBuilder.brokerAddress).serverPort(SECURE_WEB_SOCKET_PORT).sslWithDefaultConfig()
                .automaticReconnectWithDefaultConfig().buildBlocking();

        Mqtt5ConnAck connAck = client.connectWith().cleanStart(false).keepAlive(KEEP_ALIVE_SECONDS).simpleAuth()
                .username(MqttMessageBuilder.userId).password(hashedPassword.getBytes(StandardCharsets.UTF_8))
                .applySimpleAuth().send();
        if (connAck.getReasonCode().getCode() != Mqtt5ConnAckReasonCode.SUCCESS.getCode()) {
            if (connAck.getReasonString().isPresent()) {
                logger.debug("Connection failed: {}", connAck.getReasonString().get());
                throw new ConnectException("Connection failed" + connAck.getReasonString());
            }
        }

        Mqtt5Subscribe subscribeMessage = Mqtt5Subscribe.builder().addSubscription()
                .topicFilter(MqttMessageBuilder.buildClientUserTopic()).qos(MqttQos.AT_LEAST_ONCE).applySubscription()
                .addSubscription().topicFilter(MqttMessageBuilder.buildClientResponseTopic()).qos(MqttQos.AT_LEAST_ONCE)
                .applySubscription().build();

        client.subscribe(subscribeMessage);

        Mqtt5Publish publishMessage = Mqtt5Publish.builder().topic(requestTopic).qos(MqttQos.AT_MOST_ONCE)
                .payload(message).build();

        client.publish(publishMessage);

        String incomingResponse = null;
        try (final Mqtt5BlockingClient.Mqtt5Publishes publishes = client
                .publishes(MqttGlobalPublishFilter.SUBSCRIBED)) {
            Optional<Mqtt5Publish> publishesResponse = publishes.receive(RECEPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (publishesResponse.isPresent()) {
                Mqtt5Publish mqtt5PublishResponse = publishesResponse.get();
                if (mqtt5PublishResponse.getPayload().isPresent()) {
                    incomingResponse = StandardCharsets.UTF_8.decode(mqtt5PublishResponse.getPayload().get())
                            .toString();
                } else {
                    logger.debug("Received a  MQTT message without a payload");
                }
            } else {
                logger.debug("Did not receive MQTT message within timeout");
            }
        } catch (InterruptedException e) {
            logger.debug("InterruptedException: {}", e.getMessage());
        }
        client.disconnect();
        if (incomingResponse != null) {
            return incomingResponse;
        }
        return "";
    }
}
