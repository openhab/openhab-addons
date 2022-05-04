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
package org.openhab.io.mqttembeddedbroker.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.KeyManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.io.transport.mqtt.MqttServiceObserver;
import org.openhab.io.mqttembeddedbroker.Constants;
import org.openhab.io.mqttembeddedbroker.internal.MqttEmbeddedBrokerDetectStart.MqttEmbeddedBrokerStartedListener;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.BrokerConstants;
import io.moquette.broker.ISslContextCreator;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptAcknowledgedMessage;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * The {@link EmbeddedBrokerService} starts the embedded broker, creates a
 * {@link MqttBrokerConnection} and adds it to the {@link MqttService}.
 * <p>
 * For now tls connections are offered with an accept-all trust manager
 * and a predefined keystore if "secure" is set to true.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = EmbeddedBrokerService.class, configurationPid = "org.eclipse.smarthome.mqttembeddedbroker", property = {
        org.osgi.framework.Constants.SERVICE_PID + "=org.eclipse.smarthome.mqttembeddedbroker",
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=mqtt:mqttembeddedbroker",
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=MQTT",
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=MQTT Embedded Broker" })
@NonNullByDefault
public class EmbeddedBrokerService
        implements ConfigurableService, MqttConnectionObserver, MqttServiceObserver, MqttEmbeddedBrokerStartedListener {
    private final MqttService service;
    private String persistenceFilename = "";
    // private NetworkServerTls networkServerTls; //TODO wait for NetworkServerTls implementation

    @NonNullByDefault({})
    class BrokerMetricsListenerEx implements InterceptHandler {

        @Override
        public String getID() {
            return "logger";
        }

        @Override
        public Class<?>[] getInterceptedMessageTypes() {
            return new Class<?>[] { InterceptConnectMessage.class, InterceptDisconnectMessage.class };
        }

        @Override
        public void onConnect(InterceptConnectMessage arg0) {
            logger.debug("MQTT Client connected: {}", arg0.getClientID());
        }

        @Override
        public void onConnectionLost(InterceptConnectionLostMessage arg0) {

        }

        @Override
        public void onDisconnect(InterceptDisconnectMessage arg0) {
            logger.debug("MQTT Client disconnected: {}", arg0.getClientID());
        }

        @Override
        public void onMessageAcknowledged(InterceptAcknowledgedMessage arg0) {

        }

        @Override
        public void onPublish(InterceptPublishMessage arg0) {

        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage arg0) {

        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage arg0) {

        }
    }

    protected @Nullable Server server;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedBrokerService.class);
    protected MqttEmbeddedBrokerDetectStart detectStart = new MqttEmbeddedBrokerDetectStart(this);
    protected BrokerMetricsListenerEx metrics = new BrokerMetricsListenerEx();

    private @Nullable MqttBrokerConnection connection;

    @Activate
    public EmbeddedBrokerService(@Reference MqttService mqttService, Map<String, Object> configuration) throws IOException {
        this.service = mqttService;
        initialize(configuration);
    }
    @Modified
    public void modified(Map<String, Object> configuration) throws IOException {
        deactivate();
        initialize(configuration);
    }
    
    public void initialize(Map<String, Object> configuration) throws IOException {
        ServiceConfiguration config = new Configuration(configuration).as(ServiceConfiguration.class);
        int port = config.port == null ? (config.port = config.secure ? 8883 : 1883) : config.port;

        // Create MqttBrokerConnection
        connection = service.getBrokerConnection(Constants.CLIENTID);
        if (connection != null) {
            // Close the existing connection and remove it from the service
            connection.stop();
            service.removeBrokerConnection(Constants.CLIENTID);
        }

        connection = new MqttBrokerConnection("localhost", config.port, config.secure, Constants.CLIENTID);
        connection.addConnectionObserver(this);

        if (config.username != null) {
            connection.setCredentials(config.username, config.password);
        }

        if (!config.persistenceFile.isEmpty()) {
            final String persistenceFilename = config.persistenceFile;
            if (!Paths.get(persistenceFilename).isAbsolute()) {
                Path path = Paths.get(ConfigConstants.getUserDataFolder()).toAbsolutePath();
                Files.createDirectories(path);
                this.persistenceFilename = path.resolve(persistenceFilename).toString();
            }

            logger.info("Broker persistence file: {}", persistenceFilename);
        } else {
            logger.info("Using in-memory persistence. No persistence file has been set!");
        }

        // Start embedded server
        startEmbeddedServer(port, config.secure, config.username, config.password);
    }

    @Deactivate
    public void deactivate() {
        if (service != null) {
            service.removeBrokersListener(this);
        }
        MqttBrokerConnection connection = this.connection;
        if (connection == null) {
            if (server != null) {
                server.stopServer();
            }
            server = null;
            return;
        }

        // Clean shutdown: Stop connection, wait for process to finish, shutdown server
        connection.removeConnectionObserver(this);
        try {
            connection.stop().thenRun(() -> {
                if (server != null) {
                    server.stopServer();
                    server = null;
                }
            }).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
           logger.warn("Could not cleanly shutdown connection or server.", e);
        }
        connection = null;
    }

    @Override
    public void brokerAdded(String brokerID, MqttBrokerConnection broker) {
    }

    @SuppressWarnings("null")
    @Override
    public void brokerRemoved(String brokerID, MqttBrokerConnection broker) {
        // Do not allow this connection to be removed. Add it again.
        if (broker.equals(connection)) {
            service.addBrokerConnection(brokerID, broker);
        }
    }

    /**
     * For TLS connections we need to setup a keystore and provide Moquette/Netty with an {@link SslContext}.
     * <p>
     * If a context is requested by Moquette, this creator
     * will use the bundled "serverkeystore.keystore" with password "openhab".
     *
     * @return An SslContext creator (not be confused with javas SSLContext).
     */
    ISslContextCreator nettySSLcontextCreator() {
        return () -> {
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("serverkeystore.keystore");
                KeyStore keyStore = KeyStore.getInstance("jks");
                keyStore.load(inputStream, "openhab".toCharArray());
                KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
                factory.init(keyStore, "openhab".toCharArray());
                return SslContextBuilder.forServer(factory).build();
            } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException
                    | UnrecoverableKeyException e) {
                logger.warn("Failed to create an SSL context");
                return null;
            }
        };
    }

    public void startEmbeddedServer(@Nullable Integer portParam, boolean secure, @Nullable String username,
            @Nullable String password) throws IOException {
        Server server = new Server();
        Properties properties = new Properties();

        // Host and port
        properties.put(BrokerConstants.HOST_PROPERTY_NAME, "0.0.0.0");
        int port;
        if (secure) {
            port = (portParam == null) ? port = 8883 : portParam;
            properties.put(BrokerConstants.SSL_PORT_PROPERTY_NAME, Integer.toString(port));
            properties.put(BrokerConstants.PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
            properties.put(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, "esheshesh");
            properties.put(BrokerConstants.JKS_PATH_PROPERTY_NAME, "serverkeystore.jks");
        } else {
            port = (portParam == null) ? port = 1883 : portParam;
            // with SSL_PORT_PROPERTY_NAME set, netty tries to evaluate the SSL context and shuts down immediately.
            // properties.put(BrokerConstants.SSL_PORT_PROPERTY_NAME, BrokerConstants.DISABLED_PORT_BIND);
            properties.put(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(port));
        }

        // Authentication
        IAuthenticator authentificator = null;
        if (username != null && password != null && username.length() > 0 && password.length() > 0) {
            properties.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.FALSE.toString());
            properties.put(BrokerConstants.AUTHENTICATOR_CLASS_NAME,
                    MqttEmbeddedBrokerUserAuthenticator.class.getName());
            authentificator = new MqttEmbeddedBrokerUserAuthenticator(username, password.getBytes());
            logger.debug("Broker authentication is enabled");
        } else {
            properties.put(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());
            logger.debug("Broker anonymous access enabled");
        }

        if (!persistenceFilename.isEmpty()) { // Persistence: If not set, an in-memory database is used.
            properties.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, persistenceFilename);
            properties.put(BrokerConstants.AUTOSAVE_INTERVAL_PROPERTY_NAME, "30"); // in seconds
        }

        // We may provide ACL functionality at some point as well
        IAuthorizatorPolicy authorizer = null;
        ISslContextCreator sslContextCreator = secure ? nettySSLcontextCreator() : null;

        try {
            server.startServer(new MemoryConfig(properties), null, sslContextCreator, authentificator,
                    authorizer);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Could not deserialize")) {
                Path persistenceFilePath = Paths.get((new File(persistenceFilename)).getAbsolutePath());
                logger.warn("persistence corrupt: {}, deleting {}", e.getMessage(), persistenceFilePath);
                Files.delete(persistenceFilePath);
                // retry starting broker, if it fails again, don't catch exception
                server.startServer(new MemoryConfig(properties), null, sslContextCreator, authentificator,
                        authorizer);
            }
        }
        this.server = server;
        server.addInterceptHandler(metrics);
        ScheduledExecutorService s = new ScheduledThreadPoolExecutor(1);
        detectStart.startBrokerStartedDetection(port, s);
    }

    public void stopEmbeddedServer() {
        Server server = this.server;
        if (server != null) {
            server.removeInterceptHandler(metrics);
            detectStart.stopBrokerStartDetection();
            server.stopServer();
            this.server = null;
        }
    }

    /**
     * For testing: Returns true if the embedded server confirms that the MqttBrokerConnection is connected.
     */
    protected boolean serverConfirmsEmbeddedClient() {
        return server != null && server.listConnectedClients().stream()
                .anyMatch(client -> Constants.CLIENTID.equals(client.getClientID()));
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        if (state == MqttConnectionState.CONNECTED) {
            logger.debug("Embedded broker connection connected");
        } else if (state == MqttConnectionState.CONNECTING) {
            logger.debug("Embedded broker connection still connecting");
        } else {
            if (error == null) {
                logger.warn("Embedded broker offline - Reason unknown");
            } else {
                logger.warn("Embedded broker offline", error);
            }
        }

        if (state != MqttConnectionState.CONNECTED && state != MqttConnectionState.CONNECTING) {
            stopEmbeddedServer();
        }
    }

    /**
     * The callback from the detectStart.startBrokerStartedDetection() call within
     * {@link #startEmbeddedServer(Integer, boolean, String, String, String)}.
     */
    @Override
    public void mqttEmbeddedBrokerStarted(boolean timeout) {
        MqttBrokerConnection connection = this.connection;
        MqttService service = this.service;
        if (connection == null || service == null) {
            return;
        }
        service.addBrokerConnection(Constants.CLIENTID, connection);

        connection.start().exceptionally(e -> {
            connectionStateChanged(MqttConnectionState.DISCONNECTED, e);
            return false;
        }).thenAccept(v -> {
            if (!v) {
                connectionStateChanged(MqttConnectionState.DISCONNECTED, new TimeoutException("Timeout"));
            }
        });
    }

    public @Nullable MqttBrokerConnection getConnection() {
        return connection;
    }

    public String getPersistenceFilename() {
        return persistenceFilename;
    }

    public void setPersistenceFilename(String persistenceFilename) {
        this.persistenceFilename = persistenceFilename;
    }
}
