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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;

/**
 * Connection settings of a local MQTT broker in the topic format of the
 * <a href="https://github.com/dj0abr/bmw-mqtt-bridge">bmw-mqtt-bridge</a>.
 *
 * @param host host name or IP address
 * @param port port
 * @param secure whether to use TLS
 * @param username user name, empty for none
 * @param password password
 * @param topicPrefix prefix of all topics, ending with a slash
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public record BrokerSettings(String host, int port, boolean secure, String username, String password,
        String topicPrefix) {

    /** Topic prefix used by the bridge if none is configured. */
    public static final String DEFAULT_TOPIC_PREFIX = "bmw/";

    /**
     * @param prefix a configured topic prefix
     * @return the prefix with exactly one trailing slash, the bridge default if empty
     */
    public static String normalizeTopicPrefix(String prefix) {
        String trimmed = prefix.strip();
        if (trimmed.isEmpty()) {
            return DEFAULT_TOPIC_PREFIX;
        }
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    /**
     * @return the topic of the unmodified stream messages of all vehicles, {@code <prefix>raw/#}
     */
    public String rawTopicFilter() {
        return topicPrefix + "raw/#";
    }

    /**
     * @return the topic of the bridge state, {@code <prefix>status}
     */
    public String statusTopic() {
        return topicPrefix + "status";
    }

    /**
     * Creates a connection to the broker. The connection is not started.
     *
     * @param clientId MQTT client id
     */
    public MqttBrokerConnection createConnection(String clientId) throws BMWCarDataException {
        MqttBrokerConnection connection = new MqttBrokerConnection(host, port, secure, clientId);
        if (secure) {
            connection.setTrustManagers(defaultTrustManagers());
        }
        if (!username.isBlank()) {
            connection.setCredentials(username, password);
        }
        return connection;
    }

    /**
     * @return the trust managers of the JVM; {@link MqttBrokerConnection} trusts any certificate unless trust
     *         managers are set explicitly
     */
    static TrustManager[] defaultTrustManagers() throws BMWCarDataException {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            return factory.getTrustManagers();
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new BMWCarDataException("Unable to initialize TLS trust store", e);
        }
    }

    @Override
    public String toString() {
        // the password is deliberately omitted
        return "BrokerSettings [host=" + host + ", port=" + port + ", secure=" + secure + ", topicPrefix=" + topicPrefix
                + "]";
    }
}
