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

import java.net.SocketException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

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
    private @Nullable Mqtt3AsyncClient mqttClient;

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
        if (token == null || token.isEmpty()) {
            Login loginResponse = webTargets.getToken(config.email, config.password);
            token = loginResponse.token;
        }

        // 1. Get AWS Identity & Temporary Credentials
        String identityId = webTargets.getAwsIdentityId(token);
        JsonObject credentials = webTargets.getAwsCredentials(identityId, token);

        String accessKeyId = credentials.get("AccessKeyId").getAsString();
        String secretKey = credentials.get("SecretKey").getAsString();
        String sessionToken = credentials.get("SessionToken").getAsString();

        // Use the constant region matching your python script
        String awsRegion = "ap-southeast-2";

        // 2. Generate SigV4 Signed WebSocket URL using our new utility
        String signedUrl = AwsIotSigV4Signer.getSignedWebSocketUrl(AWS_IOT_ENDPOINT, awsRegion, accessKeyId, secretKey,
                sessionToken);

        // 3. Connect via HiveMQ Client
        mqttClient = MqttClient.builder().useMqttVersion3()
                .identifier(thing.getUID().getId() + "-" + System.currentTimeMillis()).serverHost(AWS_IOT_ENDPOINT)
                .serverPort(443).useSslWithDefaultConfig().webSocketConfig() // <-- Changed this line
                .serverPath(signedUrl.replace("wss://" + AWS_IOT_ENDPOINT, "")).applyWebSocketConfig() // <-- Changed
                                                                                                       // this line
                .addDisconnectedListener(this::handleMqttDisconnect).buildAsync();

        mqttClient.connect().whenComplete((connAck, throwable) -> {
            if (throwable != null) {
                logger.error("MQTT Connection failed", throwable);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "MQTT Connection failed");
            } else {
                logger.info("Successfully connected to Emerald AWS IoT via WebSockets");
                updateStatus(ThingStatus.ONLINE);
                subscribeToTopics();
            }
        });
    }

    private void subscribeToTopics() {
        // Subscribe to wildcard topic based on what the python script listens to
        mqttClient.subscribeWith().topicFilter("emerald/hws/+/status") // Adjust topic structure based on the python
                                                                       // script
                .callback(publish -> {
                    String topic = publish.getTopic().toString();
                    String payload = new String(publish.getPayloadAsBytes());
                    logger.trace("Received MQTT message on topic: {} Payload: {}", topic, payload);

                    // Route to children handlers
                    this.getThing().getThings().forEach(child -> {
                        EmeraldHWSHandler handler = (EmeraldHWSHandler) child.getHandler();
                        if (handler != null) {
                            String childUuid = child.getConfiguration().as(EmeraldHWSConfiguration.class).uuid;
                            if (topic.contains(childUuid)) {
                                handler.updateFromMqtt(payload);
                            }
                        }
                    });
                }).send();
    }

    private void handleMqttDisconnect(MqttClientDisconnectedContext context) {
        Throwable cause = context.getCause();
        if (cause instanceof SocketException) {
            logger.warn("SocketException during MQTT disconnect: {}", cause.getMessage());
        } else {
            logger.error("MQTT Client disconnected unexpectedly", cause);
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "MQTT Disconnected");

        // Optionally schedule a reconnect attempt here
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
            mqttClient.disconnect();
        }
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EmeraldHWSDiscoveryService.class);
    }
}
