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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
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
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
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

    private @Nullable RoborockAccountConfiguration config;
    private final SchedulerTask initTask;
    private final SchedulerTask reconnectTask;
    private final RoborockWebTargets webTargets;
    private @Nullable Mqtt5AsyncClient mqttClient;
    private String token = "";
    private String baseUri = "";
    private Rriot rriot = new Login().new Rriot();
    private SecureRandom secureRandom = new SecureRandom();

    /** The file we store definitions in */
    private final File loginFile = new File(RoborockBindingConstants.FILENAME_LOGINDATA);
    protected final Map<Thing, RoborockVacuumHandler> childDevices = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    public RoborockAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        webTargets = new RoborockWebTargets(httpClient);
        initTask = new SchedulerTask(scheduler, logger, "Init", this::initDevice);
        reconnectTask = new SchedulerTask(scheduler, logger, "Connection", this::connectToDevice);
    }

    public String getToken() {
        return token;
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    public Login doLogin() {
        try {
            Login login = webTargets.doLogin(baseUri, config.email, config.password);
            if (login != null) {
                return login;
            }
        } catch (RoborockAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication error " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "NoSuchAlgorithmException error " + e.getMessage());
        } catch (RoborockCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error " + e.getMessage());
        }
        return new Login();
    }

    @Nullable
    public Home getHomeDetail() {
        try {
            return webTargets.getHomeDetail(baseUri, token, rriot);
        } catch (RoborockAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication error " + e.getMessage());
            return new Home();
        } catch (RoborockCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error " + e.getMessage());
            return new Home();
        }
    }

    @Nullable
    public HomeData getHomeData(String rrHomeId) {
        try {
            return webTargets.getHomeData(rrHomeId, rriot);
        } catch (RoborockAuthenticationException | NoSuchAlgorithmException | InvalidKeyException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication error " + e.getMessage());
            return new HomeData();
        } catch (RoborockCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error " + e.getMessage());
            return new HomeData();
        }
    }

    @Nullable
    public String getRoutines(String deviceId) {
        if (rriot != null) {
            try {
                return webTargets.getRoutines(deviceId, rriot);
            } catch (RoborockAuthenticationException | NoSuchAlgorithmException | InvalidKeyException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Authentication error " + e.getMessage());
                return new String();
            } catch (RoborockCommunicationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication error " + e.getMessage());
                return new String();
            }
        } else {
            return new String();
        }
    }

    @Nullable
    public String setRoutine(String sceneID) {
        try {
            return webTargets.setRoutine(sceneID, rriot);
        } catch (RoborockAuthenticationException | NoSuchAlgorithmException | InvalidKeyException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication error " + e.getMessage());
            return "";
        } catch (RoborockCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error " + e.getMessage());
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
        if (config.email.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing email address configuration");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        initTask.setNamePrefix(getThing().getUID().getId());
        reconnectTask.setNamePrefix(getThing().getUID().getId());
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
        childDevices.put(childThing, (RoborockVacuumHandler) childHandler);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("Child released from gateway: {}  {} -> {} {}", childThing.getUID(), childThing.getLabel(),
                getThing().getUID(), getThing().getLabel());
        childDevices.remove(childThing);
    }

    private void initDevice() {
        if (baseUri.isEmpty()) {
            try {
                baseUri = webTargets.getUrlByEmail(config.email);
            } catch (RoborockAuthenticationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Authentication error " + e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "NoSuchAlgorithmException error " + e.getMessage());
            } catch (RoborockCommunicationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication error " + e.getMessage());
            }
        }
        Login loginResponse;
        try {
            if (loginFile.exists()) {
                // read date from loginFile
                final byte[] contents = Files.readAllBytes(loginFile.toPath());
                final String json = new String(contents, StandardCharsets.UTF_8);
                loginResponse = gson.fromJson(json, Login.class);
            } else {
                loginResponse = doLogin();
                if (loginResponse.code.equals("200")) {
                    // save data to loginFile if call is successful
                    loginFile.getParentFile().mkdirs();
                    final String json = gson.toJson(loginResponse);
                    final byte[] contents = json.getBytes(StandardCharsets.UTF_8);
                    Files.write(loginFile.toPath(), contents);
                }
            }
            if (loginResponse.code.equals("200")) {
                Map<String, String> properties = editProperties();
                properties.put("token", loginResponse.data.token);
                properties.put("rriot", gson.toJson(loginResponse.data.rriot));
                updateProperties(properties);
                token = loginResponse.data.token;
                rriot = loginResponse.data.rriot;
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error code " + loginResponse.code + " reported");
            }
        } catch (IOException e) {
            logger.debug("IOException reading {}: {}", loginFile.toPath(), e.getMessage(), e);
        }
        updateStatus(ThingStatus.ONLINE);
        connectToDevice();
    }

    private void teardownAndScheduleReconnection() {
        teardown(true);
    }

    private synchronized void teardown(boolean scheduleReconnection) {
        disconnect(scheduler);

        reconnectTask.cancel();
        initTask.cancel();

        if (scheduleReconnection) {
            SchedulerTask connectTask = reconnectTask;
            connectTask.schedule(5);
        }
    }

    private void connectToDevice() {
        if (!token.isEmpty()) {
            try {
                connect(scheduler);
                logger.debug("Bridge connected to MQTT");
                updateStatus(ThingStatus.ONLINE);
            } catch (InterruptedException | RoborockCommunicationException e) {
                logger.debug("Failed to connect to MQTT");
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            logger.debug("token is empty, can't login to MQTT yet");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        teardown(false);
    }

    public void connect(ScheduledExecutorService scheduler)
            throws RoborockCommunicationException, InterruptedException {
        if (rriot.r.m.isEmpty() || rriot.k.isEmpty() || rriot.s.isEmpty() || rriot.u.isEmpty()) {
            logger.trace("rriot is empty, delay connection to MQTT server");
            return;
        }
        String mqttHost = "";
        int mqttPort = 1883;
        String mqttUser = "";
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

        Mqtt5SimpleAuth auth = Mqtt5SimpleAuth.builder().username(mqttUser).password(mqttPassword.getBytes()).build();

        final MqttClientDisconnectedListener disconnectListener = ctx -> {
            boolean expectedShutdown = ctx.getSource() == MqttDisconnectSource.USER
                    && ctx.getCause() instanceof Mqtt5DisconnectException;
            // As the client already was disconnected, there's no need to do it again in disconnect() later
            this.mqttClient = null;
            if (!expectedShutdown) {
                logger.debug("{}: MQTT disconnected (source {}): {}", getThing().getUID().getId(), ctx.getSource(),
                        ctx.getCause().getMessage());
                onEventStreamFailure(ctx.getCause());
            }
        };

        final Mqtt5AsyncClient client = MqttClient.builder() //
                .useMqttVersion5() //
                .identifier(mqttUser) //
                .simpleAuth(auth) //
                .serverHost(mqttHost) //
                .serverPort(mqttPort) //
                .sslWithDefaultConfig() //
                .addDisconnectedListener(disconnectListener) //
                .automaticReconnectWithDefaultConfig() //
                .buildAsync();

        try {
            this.mqttClient = client;
            client.connect().get();

            final Consumer<@Nullable Mqtt5Publish> eventCallback = publish -> {
                if (publish == null) {
                    return;
                }
                String receivedTopic = publish.getTopic().toString();
                // try {

                String destination = receivedTopic.substring(receivedTopic.lastIndexOf('/') + 1);
                logger.debug("Received MQTT message for device {}", destination);

                // check list of child handlers and send message to the right one
                for (Entry<Thing, RoborockVacuumHandler> entry : childDevices.entrySet()) {
                    if (entry.getKey().getUID().getAsString().contains(destination)) {
                        logger.trace("Submit response to child {} -> {}", destination, entry.getKey().getUID());
                        byte[] payload = publish.getPayloadAsBytes();

                        entry.getValue().handleMessage(payload);
                        return;
                    }
                }

                // } catch (DataParsingException e) {
                // onEventStreamFailure(e);
                // }
            };

            String topic = "rr/m/o/" + rriot.u + "/" + mqttUser + "/#";

            client.subscribeWith().topicFilter(topic).callback(eventCallback).send().get();
            logger.debug("Established MQTT connection, subscribed to topic {}", topic);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            boolean isAuthFailure = cause instanceof Mqtt5ConnAckException connAckException
                    && connAckException.getMqttMessage().getReasonCode() == Mqtt5ConnAckReasonCode.NOT_AUTHORIZED;
            teardownAndScheduleReconnection();
            throw new RoborockCommunicationException(e);
        }
    }

    public void disconnect(ScheduledExecutorService scheduler) {
        Mqtt5AsyncClient client = this.mqttClient;
        if (client != null) {
            client.disconnect();
        }
        this.mqttClient = null;
    }

    private String getEndpoint() {
        try {
            byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(rriot.k.getBytes());
            byte[] subArray = new byte[6];
            System.arraycopy(md5Bytes, 8, subArray, 0, 6);
            return Base64.getEncoder().encodeToString(subArray);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    public int sendCommand(String method, String params, String thingID, String localKey, byte[] nonce)
            throws UnsupportedEncodingException {
        int timestamp = (int) Instant.now().getEpochSecond();
        int protocol = 101;
        int id = secureRandom.nextInt(22767 + 1) + 10000;

        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < nonce.length; i++) {
            hexStringBuffer.append(byteToHex(nonce[i]));
        }
        String nonceHex = hexStringBuffer.toString();

        Map<String, Object> security = new HashMap<>();
        security.put("endpoint", getEndpoint());
        security.put("nonce", nonceHex.toLowerCase());

        Map<String, Object> inner = new HashMap<>();
        inner.put("id", id);
        inner.put("method", method);
        inner.put("params", params);
        inner.put("security", security);

        Map<String, Object> dps = new HashMap<>();
        dps.put(Integer.toString(protocol), gson.toJson(inner));

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("t", timestamp);
        payloadMap.put("dps", dps);

        String payload = gson.toJson(payloadMap);
        String modPayload = payload.replace(":\\\"[", ":[").replace("]\\\"}", "]}");
        logger.trace("Modified payload = {}", modPayload);

        byte[] message = build(thingID, localKey, protocol, timestamp, modPayload.getBytes("UTF-8"));
        // now send message via mqtt
        String mqttUser = ProtocolUtils.md5Hex(rriot.u + ':' + rriot.k).substring(2, 10);

        String topic = "rr/m/i/" + rriot.u + "/" + mqttUser + "/" + thingID;
        logger.debug("Publishing {} message to {}", method, topic);
        mqttClient.publishWith().topic(topic).payload(message).retain(false).send()
                .whenComplete((mqtt5Publish, throwable) -> {
                    if (throwable != null) {
                        logger.debug("mqtt publish failed");
                    }
                });
        return id;
    }

    byte[] build(String thingID, String localKey, int protocol, int timestamp, byte[] payload) {
        try {
            String key = ProtocolUtils.encodeTimestamp(timestamp) + localKey + SALT;
            byte[] encrypted = ProtocolUtils.encrypt(payload, key);

            int randomInt = secureRandom.nextInt(90000) + 10000;
            int seq = secureRandom.nextInt(900000) + 100000;

            int totalLength = 23 + encrypted.length;
            byte[] msg = new byte[totalLength];
            // Writing fixed string '1.0'
            msg[0] = 49; // ASCII for '1'
            msg[1] = 46; // ASCII for '.'
            msg[2] = 48; // ASCII for '0'
            ProtocolUtils.writeInt32BE(msg, (int) (seq & 0xFFFFFFFF), 3);
            ProtocolUtils.writeInt32BE(msg, (int) (randomInt & 0xFFFFFFFF), 7);
            ProtocolUtils.writeInt32BE(msg, timestamp, 11);
            ProtocolUtils.writeInt16BE(msg, protocol, 15);
            ProtocolUtils.writeInt16BE(msg, encrypted.length, 17);
            // Manually copying encrypted data into msg
            for (int i = 0; i < encrypted.length; i++) {
                msg[19 + i] = encrypted[i];
            }
            byte[] buf = Arrays.copyOfRange(msg, 0, msg.length - 4);
            CRC32 crc32 = new CRC32();
            crc32.update(buf);
            ProtocolUtils.writeInt32BE(msg, (int) crc32.getValue(), msg.length - 4);
            return msg;
        } catch (Exception e) {
            logger.debug("Exception encrypting payload, {}", e.getMessage());
            return new byte[0];
        }
    }

    public void onEventStreamFailure(Throwable error) {
        logger.debug("Device connection failed, reconnecting", error);
        teardownAndScheduleReconnection();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RoborockVacuumDiscoveryService.class);
    }
}
