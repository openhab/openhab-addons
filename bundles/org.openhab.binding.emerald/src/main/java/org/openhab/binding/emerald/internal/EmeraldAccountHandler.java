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
package org.openhab.binding.emerald.internal;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.emerald.internal.api.EmeraldList;
import org.openhab.binding.emerald.internal.api.Login;
import org.openhab.binding.emerald.internal.discovery.EmeraldDiscoveryService;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.OnAttemptingConnectReturn;
import software.amazon.awssdk.crt.mqtt5.OnConnectionFailureReturn;
import software.amazon.awssdk.crt.mqtt5.OnConnectionSuccessReturn;
import software.amazon.awssdk.crt.mqtt5.OnDisconnectionReturn;
import software.amazon.awssdk.crt.mqtt5.OnStoppedReturn;
import software.amazon.awssdk.crt.mqtt5.PublishReturn;
import software.amazon.awssdk.crt.mqtt5.QOS;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket;
import software.amazon.awssdk.iot.AwsIotMqtt5ClientBuilder;

/**
 * The {@link EmeraldAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class EmeraldAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EmeraldAccountHandler.class);

    private static final String AWS_IOT_ENDPOINT = "a13v32g67itvz9-ats.iot.ap-southeast-2.amazonaws.com";

    private @Nullable EmeraldAccountConfiguration config;
    protected ScheduledExecutorService executorService = this.scheduler;
    private @NonNullByDefault({}) EmeraldWebTargets webTargets;
    private @Nullable EmeraldList emeraldList;
    private @Nullable Mqtt5Client mqttClient;

    String token = "";

    public EmeraldAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        config = getConfigAs(EmeraldAccountConfiguration.class);
        webTargets = new EmeraldWebTargets(httpClient);
    }

    public EmeraldList getApi() {
        EmeraldList api = emeraldList;
        if (api == null) {
            throw new IllegalStateException("API has not been initialized");
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
        config = getConfigAs(EmeraldAccountConfiguration.class);

        if (configure()) {
            updateStatus(ThingStatus.UNKNOWN);

            pollData();

            scheduler.execute(() -> {
                try {
                    setupMqttConnection();
                } catch (Exception e) {
                    logger.error("Failed to setup MQTT Stream", e);
                }
            });
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        EmeraldAccountConfiguration localConfig = config;

        if (localConfig == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing configuration");
            return false;
        }
        if (localConfig.email.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing email configuration");
            return false;
        }
        if (localConfig.password.isBlank()) {
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

        String cleanEndpoint = AWS_IOT_ENDPOINT.replace("https://", "").replace("wss://", "").replaceAll("/$", "")
                .trim();
        String awsRegion = "ap-southeast-2";
        String clientId = "emeraldhws_" + (System.currentTimeMillis() / 1000L);

        StaticCredentialsProvider credentialsProvider = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                .withAccessKeyId(accessKeyId.getBytes(StandardCharsets.UTF_8))
                .withSecretAccessKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .withSessionToken(sessionToken.getBytes(StandardCharsets.UTF_8)).build();

        AwsIotMqtt5ClientBuilder.WebsocketSigv4Config sigv4Config = new AwsIotMqtt5ClientBuilder.WebsocketSigv4Config();
        sigv4Config.region = awsRegion;
        sigv4Config.credentialsProvider = credentialsProvider;

        AwsIotMqtt5ClientBuilder builder = AwsIotMqtt5ClientBuilder.newWebsocketMqttBuilderWithSigv4Auth(cleanEndpoint,
                sigv4Config);

        builder.withClientId(clientId);

        builder.withPublishEvents(new Mqtt5ClientOptions.PublishEvents() {
            @Override
            public void onMessageReceived(@Nullable Mqtt5Client client, @Nullable PublishReturn publishReturn) {
                if (publishReturn == null || publishReturn.getPublishPacket() == null) {
                    return;
                }

                PublishPacket packet = publishReturn.getPublishPacket();
                String topic = packet.getTopic();
                byte[] payloadBytes = packet.getPayload();

                if (topic == null || payloadBytes == null) {
                    return;
                }

                String payload = new String(payloadBytes, StandardCharsets.UTF_8);
                logger.trace("Received MQTT message on topic: {} Payload: {}", topic, payload);

                getThing().getThings().forEach(child -> {
                    EmeraldHWSHandler handler = (EmeraldHWSHandler) child.getHandler();
                    if (handler != null) {
                        EmeraldHWSConfiguration childConfig = child.getConfiguration()
                                .as(EmeraldHWSConfiguration.class);

                        if (!childConfig.uuid.isEmpty() && topic.contains(childConfig.uuid)) {
                            handler.updateFromMqtt(payload);
                        }
                    }
                });
            }
        });

        builder.withLifeCycleEvents(new Mqtt5ClientOptions.LifecycleEvents() {
            @Override
            public void onAttemptingConnect(@Nullable Mqtt5Client client,
                    @Nullable OnAttemptingConnectReturn onAttemptingConnectReturn) {
                logger.debug("Attempting to connect to Emerald AWS IoT via AWS CRT MQTT5...");
            }

            @Override
            public void onConnectionSuccess(@Nullable Mqtt5Client client,
                    @Nullable OnConnectionSuccessReturn onConnectionSuccessReturn) {
                logger.info("Successfully connected to Emerald AWS IoT via official AWS CRT!");
                updateStatus(ThingStatus.ONLINE);
                subscribeToTopics();
            }

            @Override
            public void onConnectionFailure(@Nullable Mqtt5Client client,
                    @Nullable OnConnectionFailureReturn onConnectionFailureReturn) {
                String error = (onConnectionFailureReturn != null)
                        ? String.valueOf(onConnectionFailureReturn.getErrorCode())
                        : "Unknown";
                logger.error("AWS CRT MQTT Connection failed. Error Code: {}", error);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "MQTT Connection failed");
            }

            @Override
            public void onDisconnection(@Nullable Mqtt5Client client,
                    @Nullable OnDisconnectionReturn onDisconnectionReturn) {
                String error = (onDisconnectionReturn != null) ? String.valueOf(onDisconnectionReturn.getErrorCode())
                        : "Unknown";
                logger.warn("AWS CRT MQTT Disconnected. Error Code: {}", error);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "MQTT Disconnected");
            }

            @Override
            public void onStopped(@Nullable Mqtt5Client client, @Nullable OnStoppedReturn onStoppedReturn) {
                logger.debug("AWS CRT MQTT Client stopped.");
            }
        });

        Mqtt5Client localClient = builder.build();
        localClient.start();
        mqttClient = localClient;
    }

    private void subscribeToTopics() {
        @Nullable
        Mqtt5Client localMqttClient = mqttClient;

        if (localMqttClient == null) {
            return;
        }

        int childCount = getThing().getThings().size();

        if (childCount == 0) {
            logger.info(
                    "openHAB hasn't attached the child Heat Pumps to the Bridge yet. Delaying subscription by 5 seconds...");
            scheduler.schedule(this::subscribeToTopics, 5, TimeUnit.SECONDS);
            return;
        }

        logger.info("Attempting explicit MQTT subscriptions for {} attached Heat Pumps.", childCount);

        getThing().getThings().forEach(child -> {
            EmeraldHWSConfiguration childConfig = child.getConfiguration().as(EmeraldHWSConfiguration.class);

            if (!childConfig.uuid.isEmpty()) {
                String topic = "ep/heat_pump/from_gw/" + childConfig.uuid;

                SubscribePacket.SubscribePacketBuilder subBuilder = new SubscribePacket.SubscribePacketBuilder();
                subBuilder.withSubscription(topic, QOS.AT_LEAST_ONCE);

                localMqttClient.subscribe(subBuilder.build()).whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        logger.error("Failed to subscribe to explicit MQTT topic: {}", topic, throwable);
                    } else {
                        logger.info("Successfully subscribed to explicit Emerald Heat Pump MQTT topic: {}", topic);
                        requestStatusUpdate(childConfig.uuid);
                    }
                });
            }
        });
    }

    public void sendControlMessage(String deviceId, JsonObject commandPayload) {
        Mqtt5Client localMqttClient = mqttClient;
        if (localMqttClient == null) {
            logger.warn("MQTT client not connected. Cannot send control message.");
            return;
        }

        String propertyId = null;
        String macAddress = null;

        try {
            EmeraldList api = getApi();
            for (int i = 0; i < api.info.property.length; i++) {
                for (int j = 0; j < api.info.property[i].heatpump.length; j++) {
                    if (deviceId.equals(api.info.property[i].heatpump[j].id)) {
                        propertyId = api.info.property[i].id;
                        macAddress = api.info.property[i].heatpump[j].macAddress;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not fetch API details for control message", e);
        }

        if (propertyId == null || macAddress == null) {
            logger.warn("Missing property_id or mac_address. Cannot send control message for {}", deviceId);
            return;
        }

        JsonArray payloadArray = new JsonArray();
        JsonObject metadata = new JsonObject();
        int msgId = new Random().nextInt(9900) + 100;

        metadata.addProperty("msg_id", String.valueOf(msgId));
        metadata.addProperty("namespace", "business");
        metadata.addProperty("direction", "app2gw");
        metadata.addProperty("command", "control");
        metadata.addProperty("property_id", propertyId);
        metadata.addProperty("device_id", deviceId);
        metadata.addProperty("hw_id", macAddress);

        payloadArray.add(metadata);
        payloadArray.add(commandPayload);

        String payload = payloadArray.toString();
        String topic = "ep/heat_pump/to_gw/" + deviceId;

        PublishPacket.PublishPacketBuilder pubBuilder = new PublishPacket.PublishPacketBuilder();
        pubBuilder.withTopic(topic);
        pubBuilder.withPayload(payload.getBytes(StandardCharsets.UTF_8));
        pubBuilder.withQOS(QOS.AT_LEAST_ONCE);

        localMqttClient.publish(pubBuilder.build()).whenComplete((pubAck, throwable) -> {
            if (throwable != null) {
                logger.error("Failed to send control message to HWS {}", deviceId, throwable);
            } else {
                logger.debug("Successfully sent control message to HWS {}: {}", deviceId, commandPayload.toString());
            }
        });
    }

    private void requestStatusUpdate(String deviceId) {
        Mqtt5Client localMqttClient = mqttClient;
        if (localMqttClient == null) {
            return;
        }

        String propertyId = null;
        String macAddress = null;

        try {
            EmeraldList api = getApi();
            for (int i = 0; i < api.info.property.length; i++) {
                for (int j = 0; j < api.info.property[i].heatpump.length; j++) {
                    if (deviceId.equals(api.info.property[i].heatpump[j].id)) {
                        propertyId = api.info.property[i].id;
                        macAddress = api.info.property[i].heatpump[j].macAddress;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not fetch API details for status update", e);
        }

        if (propertyId == null || macAddress == null) {
            logger.warn("Missing property_id or mac_address. Cannot send comp_query for {}", deviceId);
            return;
        }

        JsonArray payloadArray = new JsonArray();
        JsonObject metadata = new JsonObject();
        int msgId = new Random().nextInt(9900) + 100;

        metadata.addProperty("msg_id", String.valueOf(msgId));
        metadata.addProperty("namespace", "business");
        metadata.addProperty("direction", "app2gw");
        metadata.addProperty("command", "comp_query");
        metadata.addProperty("property_id", propertyId);
        metadata.addProperty("device_id", deviceId);
        metadata.addProperty("hw_id", macAddress);

        payloadArray.add(metadata);
        payloadArray.add(new JsonObject());

        String payload = payloadArray.toString();
        String topic = "ep/heat_pump/to_gw/" + deviceId;

        PublishPacket.PublishPacketBuilder pubBuilder = new PublishPacket.PublishPacketBuilder();
        pubBuilder.withTopic(topic);
        pubBuilder.withPayload(payload.getBytes(StandardCharsets.UTF_8));
        pubBuilder.withQOS(QOS.AT_LEAST_ONCE);

        localMqttClient.publish(pubBuilder.build()).whenComplete((pubAck, throwable) -> {
            if (throwable != null) {
                logger.error("Failed to request status update (comp_query) for HWS {}", deviceId, throwable);
            } else {
                logger.debug("Successfully sent comp_query to HWS {}", deviceId);
            }
        });
    }

    protected void pollData() {
        EmeraldAccountConfiguration localConfig = config;

        if (localConfig == null) {
            return;
        }

        try {
            if ("".equals(token)) {
                Login loginResponse = webTargets.getToken(localConfig.email, localConfig.password);
                if (loginResponse != null) {
                    token = loginResponse.token;
                }
            }
            EmeraldList localList = webTargets.getList(localConfig.email, localConfig.password);
            emeraldList = localList;

            if (localList != null && localList.info != null && localList.info.property != null) {
                for (int i = 0; i < localList.info.property.length; i++) {
                    for (int j = 0; j < localList.info.property[i].heatpump.length; j++) {
                        logger.info("Found Heat Pump id = {}", localList.info.property[i].heatpump[j].id);
                    }
                }
            }

            getThing().getThings().forEach(thing -> {
                EmeraldHWSHandler handler = (EmeraldHWSHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateChannels();
                }
            });

            updateStatus(ThingStatus.ONLINE);
        } catch (EmeraldAuthenticationException e) {
            logger.debug("Unexpected authentication error connecting to Emerald API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (EmeraldCommunicationException e) {
            logger.debug("Unexpected error connecting to Emerald API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        Mqtt5Client localMqttClient = mqttClient;
        if (localMqttClient != null) {
            localMqttClient.stop(null);
            localMqttClient.close();
        }
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EmeraldDiscoveryService.class);
    }
}
