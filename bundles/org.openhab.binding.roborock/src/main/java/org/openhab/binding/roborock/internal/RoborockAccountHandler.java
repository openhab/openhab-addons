/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.Login;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.binding.roborock.internal.discovery.RoborockVacuumDiscoveryService;
import org.openhab.binding.roborock.internal.util.ProtocolUtils;
import org.openhab.binding.roborock.internal.util.SchedulerTask;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

/**
 * The {@link RoborockAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(RoborockAccountHandler.class);

    private final Storage<String> sessionStorage;
    private @Nullable RoborockAccountConfiguration config;
    private final SchedulerTask initTask;
    private final SchedulerTask mqttConnectTask;
    private final RoborockWebTargets webTargets;
    private @Nullable Mqtt5AsyncClient mqttClient;
    private String token = "";
    private String baseUri = "";
    private Rriot rriot = new Login().new Rriot();
    private final SecureRandom secureRandom = new SecureRandom();
    private String mqttUser = "";
    protected final Map<String, RoborockVacuumHandler> childDevices = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    public RoborockAccountHandler(Bridge bridge, Storage<String> stateStorage, HttpClient httpClient) {
        super(bridge);
        webTargets = new RoborockWebTargets(httpClient);
        sessionStorage = stateStorage;
        initTask = new SchedulerTask(scheduler, logger, "API Init", this::initAPI);
        mqttConnectTask = new SchedulerTask(scheduler, logger, "MQTT Connection", this::establishMQTTConnection);
    }

    public String getToken() {
        return token;
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    @Nullable
    public Home getHomeDetail() {
        try {
            return webTargets.getHomeDetail(baseUri, token);
        } catch (RoborockException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error " + e.getMessage());
            return new Home();
        }
    }

    @Nullable
    public HomeData getHomeData(String rrHomeId) {
        try {
            return webTargets.getHomeData(rrHomeId, rriot);
        } catch (RoborockException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error " + e.getMessage());
            return new HomeData();
        }
    }

    @Nullable
    public String getRoutines(String deviceId) {
        try {
            return webTargets.getRoutines(deviceId, rriot);
        } catch (RoborockException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error " + e.getMessage());
            return "";
        }
    }

    @Nullable
    public String setRoutine(String sceneID) {
        try {
            return webTargets.setRoutine(sceneID, rriot);
        } catch (RoborockException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error " + e.getMessage());
            return "";
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not have any channels -> nothing to do here
    }

    @Override
    public void initialize() {
        config = getConfigAs(RoborockAccountConfiguration.class);
        if (config == null || config.email.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing email address configuration");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        initTask.setNamePrefix(getThing().getUID().getId());
        mqttConnectTask.setNamePrefix(getThing().getUID().getId());
        initTask.submit();
    }

    @Override
    public void handleRemoval() {
        teardown(false);
        super.handleRemoval();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("Child registered with gateway: {}  {} -> {} {}", childThing.getUID(), childThing.getLabel(),
                getThing().getUID(), getThing().getLabel());
        if (childHandler instanceof RoborockVacuumHandler) {
            childDevices.put(childThing.getUID().getId(), (RoborockVacuumHandler) childHandler);
        } else {
            logger.warn("Initialized child handler is not a RoborockVacuumHandler: {}",
                    childHandler.getClass().getName());
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("Child released from gateway: {}  {} -> {} {}", childThing.getUID(), childThing.getLabel(),
                getThing().getUID(), getThing().getLabel());
        childDevices.remove(childThing.getUID().getId());
    }

    private void initAPI() {
        if (baseUri.isEmpty()) {
            try {
                baseUri = webTargets.getUrlByEmail(config.email);
            } catch (RoborockException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error " + e.getMessage());
            }
        }
        String sessionStoreToken = sessionStorage.get("token");
        String sessionStoreRriot = sessionStorage.get("rriot");

        if (!(sessionStoreToken == null) && !(sessionStoreRriot == null)) {
            logger.debug("Retrieved token and rriot values from sessionStorage");
            token = sessionStoreToken;
            @Nullable
            Rriot rriotTemp = gson.fromJson(sessionStoreRriot, Rriot.class);
            if (rriotTemp != null) {
                rriot = rriotTemp;
            }
        } else {
            logger.debug("No available token or rriot values from sessionStorage, logging in");
            try {
                String response = webTargets.doLogin(baseUri, config.email, config.password);
                int code = 0;
                if (!response.isEmpty() && JsonParser.parseString(response).getAsJsonObject().has("code")) {
                    code = JsonParser.parseString(response).getAsJsonObject().get("code").getAsInt();
                }
                if (code == 200) {
                    logger.debug("Successful login, parsing parameters");
                    Login loginResponse = gson.fromJson(response, Login.class);
                    sessionStorage.put("token", loginResponse.data.token);
                    sessionStorage.put("rriot", gson.toJson(loginResponse.data.rriot));
                    token = loginResponse.data.token;
                    rriot = loginResponse.data.rriot;
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unsuccessful login: "
                            + JsonParser.parseString(response).getAsJsonObject().get("msg").getAsString());
                    return;
                }
            } catch (RoborockException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error " + e.getMessage());
            }
        }
        updateStatus(ThingStatus.ONLINE);
        mqttConnectTask.submit();
    }

    private synchronized void teardown(boolean scheduleReconnection) {
        disconnectMqttClient();

        mqttConnectTask.cancel();
        initTask.cancel();

        if (scheduleReconnection) {
            initTask.submit();
        }
    }

    private void establishMQTTConnection() {
        if (token.isEmpty() || rriot.r == null || rriot.r.m.isEmpty() || rriot.k.isEmpty() || rriot.s.isEmpty()
                || rriot.u.isEmpty()) {
            logger.debug("token and/or rriot are empty, delay connection to MQTT server");
            return;
        }

        try {
            connectMqttClient();
            logger.debug("Bridge connected to MQTT");
            updateStatus(ThingStatus.ONLINE);
        } catch (InterruptedException | RoborockException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.no-mqtt");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        teardown(false);
    }

    public void connectMqttClient() throws RoborockException, InterruptedException {
        String mqttHost = "";
        int mqttPort = 1883;
        String mqttPassword = "";
        try {
            URI mqttURL = new URI(rriot.r.m);
            mqttHost = mqttURL.getHost();
            mqttPort = mqttURL.getPort();
            mqttUser = ProtocolUtils.md5Hex(rriot.u + ':' + rriot.k).substring(2, 10);
            mqttPassword = ProtocolUtils.md5Hex(rriot.s + ':' + rriot.k).substring(16);
        } catch (URISyntaxException e) {
            logger.error("Malformed mqtt URL");
        }

        final MqttClientConnectedListener connectedListener = ctx -> {
            String topic = "rr/m/o/" + rriot.u + "/" + ProtocolUtils.md5Hex(rriot.u + ':' + rriot.k).substring(2, 10)
                    + "/#";
            this.mqttClient.subscribeWith().topicFilter(topic).callback(this::handleMessage).send()
                    .whenComplete((subAck, error) -> {
                        if (error == null) {
                            logger.debug("Subscribed to topic {}", topic);
                        } else {
                            logger.debug("Unable to subscribe to {}", topic, error);
                        }
                    });
        };

        final MqttClientDisconnectedListener disconnectListener = ctx -> {
            boolean expectedShutdown = ctx.getSource() == MqttDisconnectSource.USER
                    && ctx.getCause() instanceof Mqtt5DisconnectException;
            // As the client already was disconnected, there's no need to do it again in disconnect() later
            this.mqttClient = null;
            if (!expectedShutdown) {
                logger.debug("{}: MQTT disconnected (source {}): {}", getThing().getUID().getId(), ctx.getSource(),
                        ctx.getCause().getMessage());
                mqttConnectTask.cancel();
                mqttConnectTask.schedule(60);
            }
        };

        final Mqtt5AsyncClient client = MqttClient.builder() //
                .useMqttVersion5() //
                .identifier(mqttUser) //
                .simpleAuth() //
                .username(mqttUser) //
                .password(mqttPassword.getBytes()) //
                .applySimpleAuth() //
                .serverHost(mqttHost) //
                .serverPort(mqttPort) //
                .sslWithDefaultConfig() //
                .addConnectedListener(connectedListener) //
                .addDisconnectedListener(disconnectListener) //
                .automaticReconnectWithDefaultConfig() //
                .buildAsync();

        try {
            this.mqttClient = client;
            client.connectWith().noSessionExpiry().cleanStart(false).send().get();
            logger.debug("Established MQTT connection.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            boolean isAuthFailure = cause instanceof Mqtt5ConnAckException connAckException
                    && connAckException.getMqttMessage().getReasonCode() == Mqtt5ConnAckReasonCode.NOT_AUTHORIZED;
            mqttConnectTask.cancel();
            mqttConnectTask.schedule(280);
            if (isAuthFailure) {
                logger.debug("Authorization failure.");
            }
            throw new RoborockException(e);
        }
    }

    public void handleMessage(@Nullable Mqtt5Publish publish) {
        if (publish == null || publish.getPayload().isEmpty()) {
            logger.debug("handleMessage - empty publish received");
            return;
        }

        String receivedTopic = publish.getTopic().toString();
        String destination = receivedTopic.substring(receivedTopic.lastIndexOf('/') + 1);
        logger.debug("Received MQTT message for device {}", destination);

        // check list of child handlers and send message to the right one
        RoborockVacuumHandler handler = childDevices.get(destination);
        if (handler != null) {
            handler.handleMessage(publish.getPayloadAsBytes());
        } else {
            logger.warn("Received MQTT message for unknown device destination: {}", destination);
        }
    }

    public void disconnectMqttClient() {
        Mqtt5AsyncClient client = this.mqttClient;
        if (client != null) {
            client.disconnect();
        }
        this.mqttClient = null;
    }

    public int sendRPCCommand(String method, String params, String thingID, String localKey, byte[] nonce, int id)
            throws UnsupportedEncodingException {
        int timestamp = (int) Instant.now().getEpochSecond();
        int protocol = 101;

        String nonceHex = HexFormat.of().formatHex(nonce);

        Map<String, Object> security = new HashMap<>();
        security.put("endpoint", ProtocolUtils.getEndpoint(rriot));
        security.put("nonce", nonceHex.toLowerCase());

        JsonElement paramsElement = JsonParser.parseString(params);
        Map<String, Object> inner = new HashMap<>();
        inner.put("id", id);
        inner.put("method", method);
        inner.put("params", paramsElement);
        inner.put("security", security);

        Map<String, Object> dps = new HashMap<>();
        dps.put(Integer.toString(protocol), gson.toJson(inner));

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("t", timestamp);
        payloadMap.put("dps", dps);

        String payload = gson.toJson(payloadMap);
        logger.trace("MQTT payload = {}", payload);

        byte[] message = build(localKey, protocol, timestamp, payload.getBytes(StandardCharsets.UTF_8));
        // now send message via mqtt
        String topic = "rr/m/i/" + rriot.u + "/" + mqttUser + "/" + thingID;
        if (this.mqttClient != null && this.mqttClient.getState().isConnected()) {
            logger.debug("Publishing {} message to {}", method, topic);
            mqttClient.publishWith().topic(topic).qos(MqttQos.AT_LEAST_ONCE).payload(message).retain(false).send()
                    .whenComplete((mqtt5Publish, throwable) -> {
                        if (throwable != null) {
                            logger.debug("mqtt publish failed");
                        }
                    });
            return id;
        } else {
            logger.debug("Failed to publish {} message to {}, this.mqttClient == null", method, topic);
            return -1;
        }
    }

    byte[] build(String localKey, int protocol, int timestamp, byte[] payload) {
        try {
            String key = ProtocolUtils.encodeTimestamp(timestamp) + localKey + SALT;
            byte[] encryptedPayload = ProtocolUtils.encrypt(payload, key);

            int randomInt = secureRandom.nextInt(90000) + 10000;
            int seq = secureRandom.nextInt(900000) + 100000;

            int totalLength = HEADER_LENGTH_WITHOUT_CRC + encryptedPayload.length + CRC_LENGTH;
            byte[] message = new byte[totalLength];

            // Write fixed string '1.0'
            message[0] = '1';
            message[1] = '.';
            message[2] = '0';

            // Write integer fields
            ProtocolUtils.writeInt32BE(message, seq, SEQ_OFFSET);
            ProtocolUtils.writeInt32BE(message, randomInt, RANDOM_OFFSET);
            ProtocolUtils.writeInt32BE(message, timestamp, TIMESTAMP_OFFSET);
            ProtocolUtils.writeInt16BE(message, protocol, PROTOCOL_OFFSET);
            ProtocolUtils.writeInt16BE(message, encryptedPayload.length, PAYLOAD_OFFSET);

            // Copy encrypted payload
            System.arraycopy(encryptedPayload, 0, message, HEADER_LENGTH_WITHOUT_CRC, encryptedPayload.length);

            // Calculate CRC32 and write to the end
            CRC32 crc32 = new CRC32();
            crc32.update(message, 0, message.length - CRC_LENGTH); // Calculate CRC for all bytes except the last 4 (CRC
                                                                   // field
            // itself)
            ProtocolUtils.writeInt32BE(message, (int) crc32.getValue(), message.length - CRC_LENGTH);
            return message;
        } catch (Exception e) {
            logger.debug("Exception encrypting payload, {}", e.getMessage());
            return new byte[0];
        }
    }

    public void onEventStreamFailure(Throwable error) {
        logger.debug("Device connection failed, reconnecting", error);
        mqttConnectTask.schedule(60);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RoborockVacuumDiscoveryService.class);
    }
}
