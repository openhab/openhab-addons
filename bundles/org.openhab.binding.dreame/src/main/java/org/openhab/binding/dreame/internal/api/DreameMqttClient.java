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
package org.openhab.binding.dreame.internal.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;
import org.openhab.binding.dreame.internal.util.DreameDiagnostics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains the TLS MQTT subscription used for Dreame property push notifications.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameMqttClient implements MqttCallbackExtended {
    private final Logger logger = LoggerFactory.getLogger(DreameMqttClient.class);
    private final DreameMqttConfiguration configuration;
    private final List<DreameProperty> properties;
    private final Consumer<DreameStatus> listener;
    private final DreameMqttMessageParser messageParser = new DreameMqttMessageParser();

    private volatile @Nullable MqttClient client;

    public DreameMqttClient(DreameMqttConfiguration configuration, List<DreameProperty> properties,
            Consumer<DreameStatus> listener) {
        this.configuration = configuration;
        this.properties = properties;
        this.listener = listener;
    }

    public void connect() throws MqttException {
        MqttClient mqttClient = new MqttClient("ssl://" + configuration.host() + ":" + configuration.port(),
                configuration.clientId(), new MemoryPersistence());
        mqttClient.setCallback(this);

        MqttConnectOptions options = createConnectOptions(configuration);
        client = mqttClient;
        logger.debug("Connecting to Dreame MQTT broker {}:{}", configuration.host(), configuration.port());
        mqttClient.connect(options);
    }

    static MqttConnectOptions createConnectOptions(DreameMqttConfiguration configuration) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(configuration.username());
        options.setPassword(configuration.password().toCharArray());
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(15);
        options.setKeepAliveInterval(50);
        options.setSocketFactory(createSocketFactory());
        options.setHttpsHostnameVerificationEnabled(true);
        return options;
    }

    static javax.net.ssl.SSLSocketFactory createSocketFactory() throws MqttException {
        try (InputStream input = DreameMqttClient.class.getResourceAsStream("/dreame-mqtt-root-ca.pem")) {
            if (input == null) {
                throw new IOException("Dreame MQTT root CA is missing");
            }
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(input);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);
            trustStore.setCertificateEntry("dreame-mqtt-root", certificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException | IOException e) {
            throw new MqttException(e);
        }
    }

    @Override
    public void connectComplete(boolean reconnect, @Nullable String serverURI) {
        MqttClient mqttClient = client;
        if (mqttClient == null) {
            return;
        }
        try {
            mqttClient.subscribe(configuration.topic(), 0);
            logger.debug("Dreame MQTT {} and subscribed for device updates", reconnect ? "reconnected" : "connected");
        } catch (MqttException e) {
            logger.debug("Could not subscribe to Dreame MQTT updates: {}", e.getMessage(), e);
        }
    }

    @Override
    public void connectionLost(@Nullable Throwable cause) {
        logger.debug("Dreame MQTT connection lost: {}", cause == null ? "unknown cause" : cause.getMessage());
    }

    @Override
    public void messageArrived(@Nullable String topic, @Nullable MqttMessage message) {
        if (message == null || message.getPayload().length == 0) {
            return;
        }
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.trace("Dreame MQTT message on device topic: {}", DreameDiagnostics.sanitize(payload));
        try {
            DreameStatus status = messageParser.parse(payload, properties);
            if (status.hasUpdates()) {
                listener.accept(status);
            }
        } catch (DreameCloudException e) {
            logger.debug("Ignoring invalid Dreame MQTT message: {}", e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(@Nullable IMqttDeliveryToken token) {
    }

    public boolean isConnected() {
        MqttClient mqttClient = client;
        return mqttClient != null && mqttClient.isConnected();
    }

    public void close() {
        MqttClient mqttClient = client;
        client = null;
        if (mqttClient != null) {
            try {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                mqttClient.close();
            } catch (MqttException e) {
                logger.debug("Error while closing Dreame MQTT client: {}", e.getMessage());
            }
        }
    }
}
