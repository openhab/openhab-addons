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
package org.openhab.binding.emeraldhws.internal;

import static org.openhab.binding.emeraldhws.internal.EmeraldHWSBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.openhab.binding.emeraldhws.internal.api.List;
import org.openhab.binding.emeraldhws.internal.api.Login;
import org.openhab.binding.emeraldhws.internal.discovery.EmeraldHWSDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link EmeraldHWSHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author paul@smedley.id.au - Initial contribution
 */
@NonNullByDefault
public class EmeraldHWSAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EmeraldHWSAccountHandler.class);

    private static final String AWS_IOT_ENDPOINT = "a13v32g67itvz9-ats.iot.ap-southeast-2.amazonaws.com";

    private @Nullable EmeraldHWSAccountConfiguration config;
    protected ScheduledExecutorService executorService = this.scheduler;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @NonNullByDefault({}) EmeraldHWSWebTargets webTargets;
    private HttpClient httpClient = new HttpClient();
    private @Nullable List emeraldHWSList;
    private @Nullable MqttAsyncClient mqttClient;

    private final Gson gson = new Gson();
    String token = "";

    public EmeraldHWSAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        config = getConfigAs(EmeraldHWSAccountConfiguration.class);
        webTargets = new EmeraldHWSWebTargets(httpClient);
    }

    public List getApi() {
        List api = emeraldHWSList;
        if (api == null) {
            throw new IllegalStateException();
        }
        return api;
    }

    public ThingUID getUID() {
        logger.info("thing.getUID() = {}", thing.getUID());
        return thing.getUID();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not have any channels -> nothing to do here
    }

    @Override
    public void initialize() {
        config = getConfigAs(EmeraldHWSAccountConfiguration.class);

        if (configure()) {
            updateStatus(ThingStatus.UNKNOWN);

            scheduler.execute(() -> {
                // Initial API Poll
                pollData();

                // Initialize MQTT
                try {
                    setupMqttConnection();
                } catch (Exception e) {
                    logger.error("Failed to setup MQTT Stream", e);
                }
            });

            pollingJob = executorService.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.email.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing email configuration");
            return false;
        }
        if (config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing password configuration");
            return false;
        }
        return true;
    }

    private void setupMqttConnection() throws Exception {
        String identityId = webTargets.getAwsIdentityId();
        JsonObject credentials = webTargets.getAwsCredentials(identityId);

        String accessKeyId = credentials.get("AccessKeyId").getAsString();
        String secretKey = credentials.get("SecretKey").getAsString();
        String sessionToken = credentials.get("SessionToken").getAsString();
        String awsRegion = "ap-southeast-2";

        String cleanEndpoint = AWS_IOT_ENDPOINT.replace("https://", "").replace("wss://", "").replaceAll("/$", "")
                .trim();

        String queryString = AwsIotSigV4Signer.getSignedQueryString(cleanEndpoint, awsRegion, accessKeyId, secretKey,
                sessionToken);

        // Paho's getRawQuery() accurately transmits the pristine string without Java corruption
        String brokerUrl = "wss://" + cleanEndpoint + ":443/mqtt?" + queryString;
        String clientId = "emeraldhws_" + (System.currentTimeMillis() / 1000L);

        mqttClient = new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(@Nullable Throwable cause) {
                logger.error("MQTT Client disconnected unexpectedly", cause);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "MQTT Disconnected");
            }

            @Override
            public void messageArrived(@Nullable String topic, @Nullable MqttMessage message) {
            }

            @Override
            public void deliveryComplete(@Nullable IMqttDeliveryToken token) {
            }
        });

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        options.setCleanSession(true);

        // Required AWS SNI Injection (TLS Handshake)
        SSLSocketFactory defaultFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        options.setSocketFactory(new SSLSocketFactory() {
            private Socket injectSNI(Socket socket) {
                if (socket instanceof SSLSocket) {
                    SSLSocket sslSocket = (SSLSocket) socket;
                    SSLParameters params = sslSocket.getSSLParameters();
                    params.setServerNames(Arrays.asList(new SNIHostName(cleanEndpoint)));
                    sslSocket.setSSLParameters(params);
                }
                return socket;
            }

            @Override
            public String[] getDefaultCipherSuites() {
                return defaultFactory.getDefaultCipherSuites();
            }

            @Override
            public String[] getSupportedCipherSuites() {
                return defaultFactory.getSupportedCipherSuites();
            }

            @Override
            public Socket createSocket(@Nullable Socket s, @Nullable String host, int port, boolean autoClose)
                    throws IOException {
                return injectSNI(defaultFactory.createSocket(s, host, port, autoClose));
            }

            @Override
            public Socket createSocket(@Nullable String host, int port) throws IOException {
                return injectSNI(defaultFactory.createSocket(host, port));
            }

            @Override
            public Socket createSocket(@Nullable String host, int port, @Nullable InetAddress localHost, int localPort)
                    throws IOException {
                return injectSNI(defaultFactory.createSocket(host, port, localHost, localPort));
            }

            @Override
            public Socket createSocket(@Nullable InetAddress host, int port) throws IOException {
                return injectSNI(defaultFactory.createSocket(host, port));
            }

            @Override
            public Socket createSocket(@Nullable InetAddress address, int port, @Nullable InetAddress localAddress,
                    int localPort) throws IOException {
                return injectSNI(defaultFactory.createSocket(address, port, localAddress, localPort));
            }

            @Override
            public Socket createSocket() throws IOException {
                return injectSNI(defaultFactory.createSocket());
            }
        });

        mqttClient.connect(options, null, new IMqttActionListener() {
            @Override
            public void onSuccess(@Nullable IMqttToken asyncActionToken) {
                logger.info("Successfully connected to Emerald AWS IoT via Paho WebSockets!");
                updateStatus(ThingStatus.ONLINE);
                subscribeToTopics();
            }

            @Override
            public void onFailure(@Nullable IMqttToken asyncActionToken, @Nullable Throwable exception) {
                logger.error("MQTT Connection failed", exception);
                String errMsg = exception != null ? exception.getMessage() : "Unknown error";
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "MQTT Connection failed: " + errMsg);
            }
        });
    }

    private void subscribeToTopics() {
        try {
            if (mqttClient != null) {
                mqttClient.subscribe("emerald/hws/+/status", 0, (topic, message) -> {
                    String payload = new String(message.getPayload());
                    logger.trace("Received MQTT message on topic: {} Payload: {}", topic, payload);

                    this.getThing().getThings().forEach(child -> {
                        EmeraldHWSHandler handler = (EmeraldHWSHandler) child.getHandler();
                        if (handler != null && topic != null) {
                            String childUuid = child.getConfiguration().as(EmeraldHWSConfiguration.class).uuid;
                            if (topic.contains(childUuid)) {
                                handler.updateFromMqtt(payload);
                            }
                        }
                    });
                });
            }
        } catch (Exception e) {
            logger.error("Failed to subscribe to MQTT topics", e);
        }
    }

    protected void pollData() {
        try {
            if ("".equals(token)) {
                Login loginResponse;
                loginResponse = webTargets.getToken(config.email, config.password);
                if (loginResponse != null) {
                    token = loginResponse.token;
                }
            }
            emeraldHWSList = webTargets.getList(config.email, config.password);
            for (int i = 0; i < emeraldHWSList.info.property.length; i++) {
                for (int j = 0; j < emeraldHWSList.info.property[i].heatpump.length; j++) {
                    logger.info("Found Heat Pump id = {}", emeraldHWSList.info.property[i].heatpump[j].id);
                }
            }

            this.getThing().getThings().forEach(thing -> {
                EmeraldHWSHandler handler = (EmeraldHWSHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateChannels();
                }
            });

            updateStatus(ThingStatus.ONLINE);
        } catch (EmeraldHWSAuthenticationException e) {
            logger.debug("Unexpected authentication error connecting to Emerald API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        } catch (EmeraldHWSCommunicationException e) {
            logger.debug("Unexpected error connecting to Emerald API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
    }

    /**
     * The actual polling loop
     */
    protected void pollingCode() {
        pollData();
    }

    @Override
    public void dispose() {
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
            } catch (MqttException e) {
                logger.debug("Error disconnecting Paho MQTT client: {}", e.getMessage());
            }
        }
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EmeraldHWSDiscoveryService.class);
    }
}
