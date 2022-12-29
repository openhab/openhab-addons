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
package org.openhab.binding.mqtt.handler;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.internal.ssl.Pin;
import org.openhab.binding.mqtt.internal.ssl.PinMessageDigest;
import org.openhab.binding.mqtt.internal.ssl.PinTrustManager;
import org.openhab.binding.mqtt.internal.ssl.PinType;
import org.openhab.binding.mqtt.internal.ssl.PinnedCallback;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttWillAndTestament;
import org.openhab.core.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
import org.openhab.core.thing.Bridge;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler provided more detailed connection information from a
 * {@link MqttBrokerConnection} via a Thing property, put the Thing
 * offline or online depending on the connection.
 *
 * @author David Graeff - Initial contribution
 * @author Jimmy Tanagra - Add birth and shutdown message
 */
@NonNullByDefault
public class BrokerHandler extends AbstractBrokerHandler implements PinnedCallback {
    private final Logger logger = LoggerFactory.getLogger(BrokerHandler.class);
    protected BrokerHandlerConfig config = new BrokerHandlerConfig();

    public BrokerHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        super.connectionStateChanged(state, error);
        final MqttBrokerConnection connection = this.connection;
        if (connection != null && state == MqttConnectionState.CONNECTED) {
            String clientID = config.clientID;
            if (clientID == null || clientID.isBlank()) {
                // Store generated client ID if none was set by the user
                clientID = connection.getClientId();
                config.clientID = clientID;
                Configuration editConfig = editConfiguration();
                editConfig.put("clientid", clientID);
                updateConfiguration(editConfig);
            }
            publish(config.birthTopic, config.birthMessage, config.birthRetain);
        }
    }

    /**
     * This method gets called by the {@link PinningSSLContextProvider} if a new public key
     * or certificate hash got pinned. The hash is stored in the thing configuration.
     */
    @Override
    public void pinnedLearnedHash(Pin pin) {
        byte[] hash = pin.getHash();
        if (hash == null) {
            logger.error("Received pins hash is empty!");
            return;
        }
        String configKey = null;
        try {
            switch (pin.getType()) {
                case CERTIFICATE_TYPE:
                    configKey = BrokerHandlerConfig.class.getDeclaredField("certificate").getName();
                    break;
                case PUBLIC_KEY_TYPE:
                    configKey = BrokerHandlerConfig.class.getDeclaredField("publickey").getName();
                    break;
            }
        } catch (NoSuchFieldException e) {
            logger.error("Field name changed!", e);
            return;
        }

        Configuration thingConfig = editConfiguration();
        thingConfig.put(configKey, HexUtils.bytesToHex(hash));
        updateConfiguration(thingConfig);
    }

    @Override
    public void pinnedConnectionDenied(Pin pin) {
        // We don't need to handle this here, because the {@link PinningSSLContextProvider}
        // will throw a CertificateException if the connection fails.
    }

    @Override
    public void pinnedConnectionAccepted() {
    }

    @Override
    public void dispose() {
        try {
            if (connection != null) {
                publish(config.shutdownTopic, config.shutdownMessage, config.shutdownRetain).get(1000,
                        TimeUnit.MILLISECONDS);
                connection.stop().get(1000, TimeUnit.MILLISECONDS);
            } else {
                logger.warn("Trying to dispose handler {} but connection is already null. Most likely this is a bug.",
                        thing.getUID());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
        }
        super.dispose();
    }

    @Override
    public boolean discoveryEnabled() {
        return config.enableDiscovery;
    }

    /**
     * Reads the thing configuration related to public key or certificate pinning, creates an appropriate a
     * {@link PinningSSLContextProvider} and assigns it to the {@link MqttBrokerConnection} instance.
     * The instance need to be set before calling this method. If the SHA-256 algorithm is not supported
     * by the platform, this method will do nothing.
     *
     * @throws IllegalArgumentException Throws this exception, if provided hash values cannot be
     *             assigned to the {@link PinningSSLContextProvider}.
     */
    protected void assignSSLContextProvider(BrokerHandlerConfig config, MqttBrokerConnection connection,
            PinnedCallback callback) throws IllegalArgumentException {
        final PinTrustManager trustManager = new PinTrustManager();

        connection.setTrustManagers(new TrustManager[] { trustManager });
        trustManager.setCallback(callback);

        if (config.certificatepin) {
            try {
                Pin pin;
                if (config.certificate.isBlank()) {
                    pin = Pin.learningPin(PinType.CERTIFICATE_TYPE);
                } else {
                    String[] split = config.certificate.split(":");
                    if (split.length != 2) {
                        throw new NoSuchAlgorithmException("Algorithm is missing");
                    }
                    pin = Pin.checkingPin(PinType.CERTIFICATE_TYPE, new PinMessageDigest(split[0]),
                            HexUtils.hexToBytes(split[1]));
                }
                trustManager.addPinning(pin);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (config.publickeypin) {
            try {
                Pin pin;
                if (config.publickey.isBlank()) {
                    pin = Pin.learningPin(PinType.PUBLIC_KEY_TYPE);
                } else {
                    String[] split = config.publickey.split(":");
                    if (split.length != 2) {
                        throw new NoSuchAlgorithmException("Algorithm is missing");
                    }
                    pin = Pin.checkingPin(PinType.PUBLIC_KEY_TYPE, new PinMessageDigest(split[0]),
                            HexUtils.hexToBytes(split[1]));
                }
                trustManager.addPinning(pin);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Creates a broker connection based on the configuration of {@link #config}.
     *
     * @return Returns a valid MqttBrokerConnection
     * @throws IllegalArgumentException If the configuration is invalid, this exception is thrown.
     */
    protected MqttBrokerConnection createBrokerConnection() throws IllegalArgumentException {
        String host = config.host;
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host is empty!");
        }

        final MqttBrokerConnection connection = new MqttBrokerConnection(config.protocol, config.mqttVersion, host,
                config.port, config.secure, config.hostnameValidated, config.clientID);

        final String username = config.username;
        final String password = config.password;
        if (username != null && !username.isBlank() && password != null) {
            connection.setCredentials(username, password); // Empty passwords are allowed
        }

        final String topic = config.lwtTopic;
        if (topic != null) {
            final String msg = config.lwtMessage;
            MqttWillAndTestament will = new MqttWillAndTestament(topic, msg != null ? msg.getBytes() : null,
                    config.lwtQos, config.lwtRetain);
            connection.setLastWill(will);
        }

        connection.setQos(config.qos);
        if (config.reconnectTime != null) {
            connection.setReconnectStrategy(new PeriodicReconnectStrategy(config.reconnectTime, 10000));
        }
        final Integer keepAlive = config.keepAlive;
        if (keepAlive != null) {
            connection.setKeepAliveInterval(keepAlive);
        }
        if (config.timeoutInMs != null) {
            connection.setTimeoutExecutor(scheduler, TIMEOUT_DEFAULT);
        }

        return connection;
    }

    @Override
    public void initialize() {
        config = getConfigAs(BrokerHandlerConfig.class);
        final MqttBrokerConnection connection = createBrokerConnection();
        assignSSLContextProvider(config, connection, this);
        this.connection = connection;

        super.initialize();
    }

    /**
     * Calls the @NonNull MqttBrokerConnection::publish() with @Nullable topic and message
     */
    private CompletableFuture<Boolean> publish(@Nullable String topic, @Nullable String message, boolean retain) {
        if (topic == null || connection == null) {
            return CompletableFuture.completedFuture(true);
        }
        String nonNullMessage = message != null ? message : "";
        return connection.publish(topic, nonNullMessage.getBytes(), connection.getQos(), retain);
    }
}
