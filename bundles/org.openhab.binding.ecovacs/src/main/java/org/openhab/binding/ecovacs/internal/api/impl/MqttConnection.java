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
package org.openhab.binding.ecovacs.internal.api.impl;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi.Credentials;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiConfiguration;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.Device;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import io.netty.handler.ssl.util.SimpleTrustManagerFactory;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class MqttConnection implements MqttClientDisconnectedListener {
    private final Logger logger = LoggerFactory.getLogger(MqttConnection.class);
    private final Map<String, MqttEventReceiver> listeners = new HashMap<>();
    private @Nullable Mqtt3AsyncClient client;

    MqttConnection() {
    }

    synchronized void connectIfNeeded(EcovacsApiConfiguration config, Credentials creds)
            throws EcovacsApiException, InterruptedException {
        if (this.client != null) {
            logger.trace("MQTT connection already established, skipping connect");
            return;
        }

        String userName = String.format("%s@%s", creds.userId(), config.getRealm().split("\\.")[0]);
        String host = String.format("mq-%s.%s", config.getContinent(), config.getRealm());

        Mqtt3SimpleAuth auth = Mqtt3SimpleAuth.builder().username(userName).password(creds.token().getBytes()).build();

        MqttClientSslConfig sslConfig = MqttClientSslConfig.builder().trustManagerFactory(createTrustManagerFactory())
                .build();

        final Mqtt3AsyncClient client = MqttClient.builder().useMqttVersion3()
                .identifier(userName + "/" + config.getDeviceId()).simpleAuth(auth).serverHost(host).serverPort(8883)
                .sslConfig(sslConfig).addDisconnectedListener(this).buildAsync();

        try {
            client.connect().get();
            this.client = client;
            logger.debug("Established MQTT connection");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            boolean isAuthFailure = cause instanceof Mqtt3ConnAckException connAckException
                    && connAckException.getMqttMessage().getReturnCode() == Mqtt3ConnAckReturnCode.NOT_AUTHORIZED;
            throw new EcovacsApiException(e, isAuthFailure);
        }
    }

    void subscribeDevice(Device device, MqttEventReceiver listener) throws EcovacsApiException, InterruptedException {
        final Consumer<@Nullable Mqtt3Publish> eventCallback = publish -> {
            if (publish == null) {
                return;
            }
            String receivedTopic = publish.getTopic().toString();
            String payload = new String(publish.getPayloadAsBytes());
            String eventName = receivedTopic.split("/")[2].toLowerCase();
            logger.trace("{}: Got MQTT message on topic {}: {}", device.getName(), receivedTopic, payload);
            listener.onEvent(eventName, payload);
        };

        String topic = String.format("iot/atr/+/%s/%s/%s/+", device.getDid(), device.getDeviceClass(),
                device.getResource());
        final Mqtt3AsyncClient client;
        synchronized (this) {
            client = this.client;
        }
        if (client == null) {
            throw new IllegalStateException("Can not subscribe while not connected");
        }

        try {
            client.subscribeWith().topicFilter(topic).qos(MqttQos.AT_LEAST_ONCE).callback(eventCallback).send().get();
            logger.debug("{}: Subscribed to MQTT topic {}", device.getName(), topic);
            synchronized (this) {
                listeners.put(device.getName(), listener);
            }
        } catch (ExecutionException e) {
            throw new EcovacsApiException(e);
        }
    }

    void unsubscribe(Device device) throws EcovacsApiException, InterruptedException {
        Mqtt3AsyncClient client = null;
        synchronized (this) {
            logger.debug("{}: Unsubscribing from MQTT events", device.getName());
            if (listeners.remove(device.getName()) != null && listeners.isEmpty()) {
                client = this.client;
                this.client = null;
            }
        }
        if (client != null) {
            try {
                logger.debug("No more listeners, disconnecting MQTT connection");
                client.disconnect().get();
            } catch (ExecutionException e) {
                throw new EcovacsApiException(e);
            }
        }
    }

    @Override
    public void onDisconnected(@Nullable MqttClientDisconnectedContext context) {
        // Context as well as cause and source are guaranteed to not be null (just marked with an incompatible
        // annotation)
        MqttClientDisconnectedContext ctx = Objects.requireNonNull(context);
        MqttDisconnectSource source = Objects.requireNonNull(ctx.getSource());
        Throwable cause = Objects.requireNonNull(ctx.getCause());

        boolean expectedShutdown = source == MqttDisconnectSource.USER && cause instanceof Mqtt3DisconnectException;
        logger.debug("MQTT connection disconnected (source {}, cause {})", source, cause.getMessage());
        final ArrayList<MqttEventReceiver> listeners;
        synchronized (this) {
            listeners = new ArrayList<>(this.listeners.values());
        }
        listeners.forEach(listener -> listener.onEventStreamDisconnected(expectedShutdown, cause));
    }

    private TrustManagerFactory createTrustManagerFactory() {
        return new SimpleTrustManagerFactory() {
            @Override
            protected void engineInit(@Nullable KeyStore keyStore) throws Exception {
            }

            @Override
            protected void engineInit(@Nullable ManagerFactoryParameters managerFactoryParameters) throws Exception {
            }

            @Override
            protected TrustManager[] engineGetTrustManagers() {
                return new TrustManager[] { TrustAllTrustManager.getInstance() };
            }
        };
    }
}
