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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;
import org.openhab.binding.dreame.internal.util.DreameDiagnostics;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.io.transport.mqtt.reconnect.AbstractReconnectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains the TLS MQTT subscription used for Dreame property push notifications.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameMqttClient implements MqttMessageSubscriber, MqttConnectionObserver {
    private static final int CONNECT_TIMEOUT_SECONDS = 15;

    private final Logger logger = LoggerFactory.getLogger(DreameMqttClient.class);
    private final DreameMqttConfiguration configuration;
    private final List<DreameProperty> properties;
    private final Consumer<DreameStatus> listener;
    private final DreameMqttMessageParser messageParser = new DreameMqttMessageParser();
    private final MqttBrokerConnection connection;

    private volatile boolean subscribed;
    private volatile boolean closed;

    public DreameMqttClient(DreameMqttConfiguration configuration, List<DreameProperty> properties,
            Consumer<DreameStatus> listener) throws DreameCloudException {
        this(configuration, properties, listener, createConnection(configuration, true));
    }

    DreameMqttClient(DreameMqttConfiguration configuration, List<DreameProperty> properties,
            Consumer<DreameStatus> listener, MqttBrokerConnection connection) {
        this.configuration = configuration;
        this.properties = properties;
        this.listener = listener;
        this.connection = connection;
        connection.addConnectionObserver(this);
    }

    static MqttBrokerConnection createConnection(DreameMqttConfiguration configuration, boolean reconnect)
            throws DreameCloudException {
        validateConfiguration(configuration);
        MqttBrokerConnection connection = new MqttBrokerConnection(MqttBrokerConnection.Protocol.TCP,
                MqttBrokerConnection.MqttVersion.V3, configuration.host(), configuration.port(), true,
                configuration.clientId());
        connection.setTrustManagers(createTrustManagers());
        connection.setCredentials(configuration.username(), configuration.password());
        connection.setCleanSessionStart(true);
        connection.setKeepAliveInterval(50);
        connection.setQos(0);
        if (!reconnect) {
            AbstractReconnectStrategy strategy = connection.getReconnectStrategy();
            if (strategy != null) {
                strategy.stop();
            }
        }
        return connection;
    }

    static void validateConfiguration(DreameMqttConfiguration configuration) {
        if (!configuration.host().matches("[A-Za-z0-9.-]{1,253}") || configuration.port() < 1
                || configuration.port() > 65535 || configuration.topic().isBlank()
                || configuration.topic().contains("#") || configuration.topic().contains("+")
                || configuration.topic().chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid Dreame MQTT routing metadata");
        }
    }

    static TrustManager[] createTrustManagers() throws DreameCloudException {
        try (InputStream input = DreameMqttClient.class.getResourceAsStream("/dreame-mqtt-root-ca.pem")) {
            if (input == null) {
                throw new IOException("Dreame MQTT root CA is missing");
            }
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(input);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);
            trustStore.setCertificateEntry("dreame-mqtt-root", certificate);
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(trustStore);
            return factory.getTrustManagers();
        } catch (GeneralSecurityException | IOException e) {
            throw new DreameCloudException("Could not initialize Dreame MQTT trust store", e);
        }
    }

    public void connect() throws DreameCloudException {
        logger.debug("Connecting to Dreame MQTT broker {}:{}", configuration.host(), configuration.port());
        try {
            if (!Boolean.TRUE.equals(connection.start().get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    || !Boolean.TRUE.equals(connection.subscribe(configuration.topic(), this)
                            .get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS))) {
                throw new DreameCloudException("Dreame MQTT connection or subscription was rejected");
            }
            subscribed = true;
            logger.debug("Dreame MQTT connected and subscribed for device updates");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DreameCloudException("Dreame MQTT connection was interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new DreameCloudException("Dreame MQTT connection failed", e);
        }
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        if (closed || !configuration.topic().equals(topic) || payload.length == 0) {
            return;
        }
        String message = new String(payload, StandardCharsets.UTF_8);
        logger.trace("Dreame MQTT message on device topic: {}", DreameDiagnostics.sanitize(message));
        try {
            DreameStatus status = messageParser.parse(message, properties);
            if (status.hasUpdates()) {
                listener.accept(status);
            }
        } catch (DreameCloudException e) {
            logger.debug("Ignoring invalid Dreame MQTT message: {}", e.getMessage());
        }
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        if (state == MqttConnectionState.DISCONNECTED) {
            subscribed = false;
            logger.debug("Dreame MQTT connection lost");
        }
    }

    public boolean isConnected() {
        return !closed && subscribed && connection.connectionState() == MqttConnectionState.CONNECTED;
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        subscribed = false;
        connection.removeConnectionObserver(this);
        connection.stop();
    }
}
