/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttActionCallback;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NhcMqttConnection2} manages the MQTT connection to the Connected Controller. It allows receiving state
 * information about specific devices and sending updates to specific devices.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcMqttConnection2 implements MqttActionCallback {

    private final Logger logger = LoggerFactory.getLogger(NhcMqttConnection2.class);

    private volatile @Nullable MqttBrokerConnection mqttConnection;

    private volatile @Nullable CompletableFuture<Boolean> subscribedFuture;
    private volatile @Nullable CompletableFuture<Boolean> stoppedFuture;

    private MqttMessageSubscriber messageSubscriber;
    private MqttConnectionObserver connectionObserver;

    private TrustManager trustManagers[];
    private String clientId;

    private volatile String cocoAddress = "";
    private volatile int port;
    private volatile String profile = "";
    private volatile String token = "";

    NhcMqttConnection2(String clientId, MqttMessageSubscriber messageSubscriber,
            MqttConnectionObserver connectionObserver) throws CertificateException {
        trustManagers = getTrustManagers();
        this.clientId = clientId;
        this.messageSubscriber = messageSubscriber;
        this.connectionObserver = connectionObserver;
    }

    private TrustManager[] getTrustManagers() throws CertificateException {
        ResourceBundle certificatesBundle = ResourceBundle.getBundle("nikohomecontrol/certificates");

        try {
            // Load server public certificates into key store
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            InputStream certificateStream;
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            for (String certName : certificatesBundle.keySet()) {
                certificateStream = new ByteArrayInputStream(
                        certificatesBundle.getString(certName).getBytes(StandardCharsets.UTF_8));
                X509Certificate certificate = (X509Certificate) cf.generateCertificate(certificateStream);
                keyStore.setCertificateEntry(certName, certificate);
            }

            ResourceBundle.clearCache();

            // Create trust managers used to validate server
            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmFactory.init(keyStore);
            return tmFactory.getTrustManagers();
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
            logger.debug("error with SSL context creation: {} ", e.getMessage());
            throw new CertificateException("SSL context creation exception", e);
        } finally {
            ResourceBundle.clearCache();
        }
    }

    /**
     * Start a secure MQTT connection and subscribe to all topics.
     *
     * @param subscriber MqttMessageSubscriber that will handle received messages
     * @param cocoAddress IP Address of the Niko Connected Controller
     * @param port Port for MQTT communication with the Niko Connected Controller
     * @param token JWT token for the hobby profile
     * @throws MqttException
     */
    synchronized void startConnection(String cocoAddress, int port, String profile, String token) throws MqttException {
        CompletableFuture<Boolean> future = stoppedFuture;
        if (future != null) {
            try {
                future.get(5000, TimeUnit.MILLISECONDS);
                logger.debug("finished stopping connection");
            } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
                logger.debug("error stopping connection");
            }
            stoppedFuture = null;
        }

        logger.debug("starting connection...");
        this.cocoAddress = cocoAddress;
        this.port = port;
        this.profile = profile;
        this.token = token;
        MqttBrokerConnection connection = createMqttConnection();
        connection.addConnectionObserver(connectionObserver);
        mqttConnection = connection;
        try {
            if (connection.start().get(5000, TimeUnit.MILLISECONDS)) {
                if (subscribedFuture == null) {
                    subscribedFuture = connection.subscribe("#", messageSubscriber);
                }
            } else {
                logger.debug("error connecting");
                throw new MqttException("Connection execution exception");
            }
        } catch (InterruptedException e) {
            logger.debug("connection interrupted exception");
            throw new MqttException("Connection interrupted exception");
        } catch (ExecutionException e) {
            logger.debug("connection execution exception", e.getCause());
            throw new MqttException("Connection execution exception");
        } catch (TimeoutException e) {
            logger.debug("connection timeout exception");
            throw new MqttException("Connection timeout exception");
        }
    }

    private MqttBrokerConnection createMqttConnection() throws MqttException {
        MqttBrokerConnection connection = new MqttBrokerConnection(cocoAddress, port, true, false, clientId);
        connection.setTrustManagers(trustManagers);
        connection.setCredentials(profile, token);
        connection.setQos(1);
        return connection;
    }

    /**
     * Stop the MQTT connection.
     */
    void stopConnection() {
        logger.debug("stopping connection...");
        MqttBrokerConnection connection = mqttConnection;
        if (connection != null) {
            connection.removeConnectionObserver(connectionObserver);
        }
        stoppedFuture = stopConnection(connection);
        mqttConnection = null;

        CompletableFuture<Boolean> future = subscribedFuture;
        if (future != null) {
            future.complete(false);
            subscribedFuture = null;
        }
    }

    private CompletableFuture<Boolean> stopConnection(@Nullable MqttBrokerConnection connection) {
        if (connection != null) {
            return connection.stop();
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    /**
     * @return true if connection established and subscribed to all topics
     */
    private boolean isConnected() {
        MqttBrokerConnection connection = mqttConnection;
        CompletableFuture<Boolean> future = subscribedFuture;

        if (connection != null) {
            try {
                if ((future != null) && future.get(5000, TimeUnit.MILLISECONDS)) {
                    MqttConnectionState state = connection.connectionState();
                    logger.debug("connection state {} for {}", state, connection.getClientId());
                    return state == MqttConnectionState.CONNECTED;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Publish a message on the general connection.
     *
     * @param topic
     * @param payload
     * @throws MqttException
     */
    void connectionPublish(String topic, String payload) throws MqttException {
        MqttBrokerConnection connection = mqttConnection;
        if (connection == null) {
            logger.debug("cannot publish, no connection");
            throw new MqttException("No connection exception");
        }

        if (isConnected()) {
            logger.debug("publish {}, {}", topic, payload);
            connection.publish(topic, payload.getBytes(), connection.getQos(), false);
        } else {
            logger.debug("cannot publish, not subscribed to connection messages");
        }
    }

    @Override
    public void onSuccess(String topic) {
        logger.debug("publish succeeded {}", topic);
    }

    @Override
    public void onFailure(String topic, Throwable error) {
        logger.debug("publish failed {}, {}", topic, error.getMessage(), error);
    }
}
