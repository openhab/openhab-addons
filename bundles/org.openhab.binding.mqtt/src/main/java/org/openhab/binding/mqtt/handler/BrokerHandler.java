/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.util.HexUtils;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.io.transport.mqtt.MqttWillAndTestament;
import org.eclipse.smarthome.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
import org.openhab.binding.mqtt.internal.ssl.Pin;
import org.openhab.binding.mqtt.internal.ssl.PinMessageDigest;
import org.openhab.binding.mqtt.internal.ssl.PinTrustManager;
import org.openhab.binding.mqtt.internal.ssl.PinType;
import org.openhab.binding.mqtt.internal.ssl.PinnedCallback;
import org.openhab.binding.mqtt.internal.ssl.PinningSSLContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler provided more detailed connection information from a
 * {@link MqttBrokerConnection} via a Thing property, put the Thing
 * offline or online depending on the connection and adds the configured
 * connection to the {@link MqttService}.
 *
 * @author David Graeff - Initial contribution
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
        // Store generated client ID if none was set by the user
        final MqttBrokerConnection connection = this.connection;
        if (connection != null && state == MqttConnectionState.CONNECTED && StringUtils.isBlank(config.clientID)) {
            config.clientID = connection.getClientId();
            Configuration editConfig = editConfiguration();
            editConfig.put("clientid", config.clientID);
            updateConfiguration(editConfig);
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
                connection.stop().get(1000, TimeUnit.MILLISECONDS);
            } else {
                logger.warn("Trying to dispose handler {} but connection is already null. Most likely this is a bug.",
                        thing.getUID());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
        }
        super.dispose();
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

        connection.setSSLContextProvider(new PinningSSLContextProvider(trustManager));
        trustManager.setCallback(callback);

        if (config.certificatepin) {
            try {
                Pin pin;
                if (StringUtils.isBlank(config.certificate)) {
                    pin = Pin.LearningPin(PinType.CERTIFICATE_TYPE);
                } else {
                    String[] split = config.certificate.split(":");
                    if (split.length != 2) {
                        throw new NoSuchAlgorithmException("Algorithm is missing");
                    }
                    pin = Pin.CheckingPin(PinType.CERTIFICATE_TYPE, new PinMessageDigest(split[0]),
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
                if (StringUtils.isBlank(config.publickey)) {
                    pin = Pin.LearningPin(PinType.PUBLIC_KEY_TYPE);
                } else {
                    String[] split = config.publickey.split(":");
                    if (split.length != 2) {
                        throw new NoSuchAlgorithmException("Algorithm is missing");
                    }
                    pin = Pin.CheckingPin(PinType.PUBLIC_KEY_TYPE, new PinMessageDigest(split[0]),
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
        if (StringUtils.isBlank(host) || host == null) {
            throw new IllegalArgumentException("Host is empty!");
        }

        final MqttBrokerConnection connection = new MqttBrokerConnection(host, config.port, config.secure,
                config.clientID);

        final String username = config.username;
        final String password = config.password;
        if (StringUtils.isNotBlank(username) && password != null) {
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

        connection.setRetain(config.retainMessages);

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
}
