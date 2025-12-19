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
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.openhab.binding.roborock.internal.api.GetUrlByEmail;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.Login;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.binding.roborock.internal.discovery.RoborockVacuumDiscoveryService;
import org.openhab.binding.roborock.internal.util.ProtocolUtils;
import org.openhab.binding.roborock.internal.util.SchedulerTask;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.core.Configuration;
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

/**
 * The {@link RoborockAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockAccountHandler extends BaseBridgeHandler implements MqttCallbackExtended {

    private final Logger logger = LoggerFactory.getLogger(RoborockAccountHandler.class);

    private final Storage<String> sessionStorage;
    private @Nullable RoborockAccountConfiguration config;
    private final SchedulerTask initTask;
    private final SchedulerTask mqttConnectTask;
    private final RoborockWebTargets webTargets;
    private @Nullable MqttClient mqttClient;
    private volatile boolean disposed = false;
    private String country = "";
    private String countryCode = "";
    private String token = "";
    private String baseUri = "";
    private Rriot rriot = new Login().new Rriot();
    private final SecureRandom secureRandom = new SecureRandom();
    private String mqttUser = "";
    protected final Map<String, RoborockVacuumHandler> childDevices = new ConcurrentHashMap<>();
    private final ExpiringCache<Home> homeCache = new ExpiringCache<>(Duration.ofMinutes(10), this::refreshHome);
    private final ExpiringCache<HomeData> homeDataCache = new ExpiringCache<>(Duration.ofMinutes(10),
            this::refreshHomeData);

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
        return homeCache.getValue();
    }

    @Nullable
    public Home refreshHome() {
        try {
            return webTargets.getHomeDetail(baseUri, token);
        } catch (RoborockException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error " + e.getMessage());
            return null;
        }
    }

    @Nullable
    public HomeData getHomeData() {
        return homeDataCache.getValue();
    }

    @Nullable
    public HomeData refreshHomeData() {
        try {
            Home home = homeCache.getValue();
            if (home == null) {
                return new HomeData();
            }
            return webTargets.getHomeData(Integer.toString(home.data.rrHomeId), rriot);
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
        RoborockAccountConfiguration localConfig = config = getConfigAs(RoborockAccountConfiguration.class);
        if (localConfig.email.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-email");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        initTask.setNamePrefix(getThing().getUID().getId());
        mqttConnectTask.setNamePrefix(getThing().getUID().getId());
        initTask.submit();
    }

    @Override
    public void handleRemoval() {
        teardown();
        super.handleRemoval();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("Child registered with gateway: {}  {} -> {} {}", childThing.getUID(), childThing.getLabel(),
                getThing().getUID(), getThing().getLabel());
        if (childHandler instanceof RoborockVacuumHandler vacuumHandler) {
            childDevices.put(childThing.getUID().getId(), vacuumHandler);
        } else {
            logger.debug("Initialized child handler is not a RoborockVacuumHandler: {}",
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
        if (disposed) {
            return;
        }
        RoborockAccountConfiguration localConfig = config;
        if (localConfig == null) {
            return;
        }
        if (baseUri.isEmpty()) {
            try {
                GetUrlByEmail getUrlByEmail = webTargets.getUrlByEmail(localConfig.email);
                if (getUrlByEmail != null && getUrlByEmail.data != null) {
                    baseUri = getUrlByEmail.data.url;
                    country = getUrlByEmail.data.country;
                    countryCode = getUrlByEmail.data.countrycode;
                    logger.trace("Country determined to be {} and code {}", country, countryCode);
                } else {
                    baseUri = EU_IOT_BASE_URL;
                }
            } catch (RoborockException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error " + e.getMessage());
                return;
            }
        }
        String sessionStoreToken = sessionStorage.get("token");
        String sessionStoreRriot = sessionStorage.get("rriot");

        if (sessionStoreToken != null && sessionStoreRriot != null) {
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
                if (localConfig.twofa.isBlank()) {
                    String response = webTargets.requestCodeV4(baseUri, localConfig.email);
                    updateStatus(ThingStatus.UNKNOWN);
                    return;
                } else {
                    String response = "";
                    if (!country.isBlank()) {
                        response = webTargets.doLoginV4(baseUri, country, countryCode, localConfig.email,
                                localConfig.twofa);
                        Configuration configuration = editConfiguration();
                        configuration.put("twofa", "");
                        updateConfiguration(configuration);
                    } else {
                        response = webTargets.doLogin(baseUri, localConfig.email, localConfig.twofa);
                    }
                    int code = 0;
                    String message = "";
                    if (response != null && !response.isEmpty()
                            && JsonParser.parseString(response).getAsJsonObject().has("code")) {
                        code = JsonParser.parseString(response).getAsJsonObject().get("code").getAsInt();
                        message = JsonParser.parseString(response).getAsJsonObject().get("msg").getAsString();
                    }
                    if (code == 200) {
                        Login loginResponse = gson.fromJson(response, Login.class);
                        if (loginResponse == null) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "@text/offline.comm-error.login-failed " + message);
                            return;
                        }
                        sessionStorage.put("token", loginResponse.data.token);
                        sessionStorage.put("rriot", gson.toJson(loginResponse.data.rriot));
                        token = loginResponse.data.token;
                        rriot = loginResponse.data.rriot;
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/offline.comm-error.login-failed " + message);
                        return;
                    }
                }
            } catch (RoborockException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error " + e.getMessage());
                return;
            }
        }
        updateStatus(ThingStatus.ONLINE);
        mqttConnectTask.submit();
    }

    private synchronized void teardown() {
        disposed = true;
        initTask.cancel();
        mqttConnectTask.cancel();
        disconnectMqttClient();
    }

    private void establishMQTTConnection() {
        if (disposed) {
            return;
        }
        if (token.isEmpty() || rriot.r == null || rriot.r.m.isEmpty() || rriot.k.isEmpty() || rriot.s.isEmpty()
                || rriot.u.isEmpty()) {
            logger.debug("token and/or rriot are empty, delay connection to MQTT server");
            return;
        }

        try {
            connectMqttClient();
            logger.debug("Bridge connected to MQTT");
            updateStatus(ThingStatus.ONLINE);
        } catch (MqttException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.no-mqtt");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        teardown();
    }

    public void connectMqttClient() throws MqttException {
        try {
            URI mqttURL = new URI(rriot.r.m);
            mqttUser = ProtocolUtils.md5Hex(rriot.u + ':' + rriot.k).substring(2, 10);
            String mqttPassword = ProtocolUtils.md5Hex(rriot.s + ':' + rriot.k).substring(16);

            String serverURI = "ssl://" + mqttURL.getHost() + ":" + mqttURL.getPort();
            String clientId = mqttUser;

            MqttClient localMqttClient = mqttClient = new MqttClient(serverURI, clientId, new MemoryPersistence());
            localMqttClient.setCallback(this);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);
            connOpts.setUserName(mqttUser);
            connOpts.setPassword(mqttPassword.toCharArray());
            connOpts.setAutomaticReconnect(true);
            connOpts.setConnectionTimeout(60);
            connOpts.setKeepAliveInterval(0);

            localMqttClient.connect(connOpts);
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.comm-error.mqtt-url-bad");
            throw new MqttException(e);
        } catch (MqttException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.mqtt-login-fail");
            throw e;
        }
    }

    @Override
    public void connectComplete(boolean reconnect, @Nullable String serverURI) {
        if (disposed) {
            return;
        }
        logger.debug("MQTT connection established. Reconnect: {}, Server URI: {}", reconnect, serverURI);

        // Subscribe to topics after a successful connection
        try {
            String mqttUser = ProtocolUtils.md5Hex(rriot.u + ':' + rriot.k).substring(2, 10);
            String topic = "rr/m/o/" + rriot.u + "/" + mqttUser + "/#";
            MqttClient localMqttClient = mqttClient;
            if (localMqttClient != null) {
                localMqttClient.subscribe(topic, 0);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (MqttException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.mqtt-subscribe-fail");
        }
    }

    @Override
    public void connectionLost(@Nullable Throwable cause) {
        if (disposed) {
            return;
        }
        // Additional logic can be placed here if specific actions are needed on disconnect
    }

    @Override
    public void messageArrived(@Nullable String topic, @Nullable MqttMessage message) throws Exception {
        if (disposed) {
            return;
        }
        String localTopic = topic;
        MqttMessage localMessage = message;
        if (localTopic == null || localMessage == null) {
            return;
        }
        byte[] payload = localMessage.getPayload();
        if (payload == null || payload.length == 0) {
            return;
        }

        String destination = localTopic.substring(localTopic.lastIndexOf('/') + 1);
        logger.debug("Received MQTT message for device {}", destination);

        // Check list of child handlers and send message to the right one
        RoborockVacuumHandler handler = childDevices.get(destination);
        if (handler != null) {
            handler.handleMessage(payload);
        } else {
            logger.warn("Received MQTT message for unknown device destination: {}", destination);
        }
    }

    @Override
    public void deliveryComplete(@Nullable IMqttDeliveryToken token) {
    }

    public void disconnectMqttClient() {
        MqttClient localMqttClient = mqttClient;
        if (localMqttClient != null) {
            try {
                if (localMqttClient.isConnected()) {
                    localMqttClient.disconnect();
                }
                localMqttClient.close();
            } catch (MqttException e) {
                logger.error("Error while disconnecting MQTT client.", e);
            }
        }
        this.mqttClient = null;
    }

    public int sendRPCCommand(String method, String params, String thingID, String localKey, byte[] nonce, int id)
            throws UnsupportedEncodingException {
        int timestamp = (int) Instant.now().getEpochSecond();
        int protocol = 101;
        MqttClient localMqttClient = mqttClient;

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

        byte[] messageBytes = build(localKey, protocol, timestamp, payload.getBytes(StandardCharsets.UTF_8));
        // now send message via mqtt
        String topic = "rr/m/i/" + rriot.u + "/" + mqttUser + "/" + thingID;

        if (localMqttClient != null && localMqttClient.isConnected()) {
            logger.debug("Publishing {} message to {}", method, topic);
            try {
                MqttMessage message = new MqttMessage(messageBytes);
                message.setQos(1);
                message.setRetained(false);
                localMqttClient.publish(topic, message);
                return id;
            } catch (MqttException e) {
                logger.error("MQTT publish failed", e);
                return -1;
            }
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
        if (disposed) {
            return;
        }
        logger.debug("Device connection failed, reconnecting", error);
        mqttConnectTask.schedule(60);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RoborockVacuumDiscoveryService.class);
    }
}
