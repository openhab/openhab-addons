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

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.zip.CRC32;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.binding.roborock.internal.util.HashUtil;
import org.openhab.binding.roborock.internal.util.SchedulerTask;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

/**
 * The {@link RoborockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockVacuumHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RoborockVacuumHandler.class);

    @Nullable
    RoborockAccountHandler bridgeHandler;
    private final SchedulerTask initTask;
    private final SchedulerTask reconnectTask;
    private final SchedulerTask pollTask;
    private String token = "";
    private @Nullable Rriot rriot;
    private String rrHomeId = "";
    private String localKey = "";
    private @Nullable Mqtt3AsyncClient mqttClient;
    private long lastSuccessfulPollTimestamp;
    static final String salt = "TXdfu$jyZ#TZHsg4";

    public RoborockVacuumHandler(Thing thing) {
        super(thing);
        initTask = new SchedulerTask(scheduler, logger, "Init", this::initDevice);
        reconnectTask = new SchedulerTask(scheduler, logger, "Connection", this::connectToDevice);
        pollTask = new SchedulerTask(scheduler, logger, "Poll", this::pollData);
    }

    protected String getToken() {
        RoborockAccountHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            return "";
        }
        try {
            return localBridge.getToken();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            return "";
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not have any channels -> nothing to do here
    }

    @Override
    public void initialize() {
        if (!(getBridge() instanceof Bridge bridge
                && bridge.getHandler() instanceof RoborockAccountHandler accountHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No Roborock Bridge selected");
            return;
        }
        bridgeHandler = accountHandler;
        updateStatus(ThingStatus.UNKNOWN);
        token = getToken();
        if (!token.isEmpty()) {
            rriot = bridgeHandler.getRriot();
            Home home;
            home = bridgeHandler.getHomeDetail();
            if (home != null) {
                rrHomeId = Integer.toString(home.data.rrHomeId);
            }
            initTask.setNamePrefix(getThing().getUID().getId());
            reconnectTask.setNamePrefix(getThing().getUID().getId());
            pollTask.setNamePrefix(getThing().getUID().getId());
            initTask.submit();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Token empty, can't login");
        }
    }

    private synchronized void scheduleNextPoll(long initialDelaySeconds) {
        final RoborockVacuumConfiguration config = getConfigAs(RoborockVacuumConfiguration.class);
        final long delayUntilNextPoll;
        if (initialDelaySeconds < 0) {
            long intervalSeconds = config.refresh * 60;
            long secondsSinceLastPoll = (System.currentTimeMillis() - lastSuccessfulPollTimestamp) / 1000;
            long deltaRemaining = intervalSeconds - secondsSinceLastPoll;
            delayUntilNextPoll = Math.max(0, deltaRemaining);
        } else {
            delayUntilNextPoll = initialDelaySeconds;
        }
        logger.debug("{}: Scheduling next poll in {}s, refresh interval {}min", getThing().getUID().getId(),
                delayUntilNextPoll, config.refresh);
        pollTask.cancel();
        pollTask.schedule(delayUntilNextPoll);
    }

    private void initDevice() {
        connectToDevice();
    }

    private void teardownAndScheduleReconnection() {
        teardown(true);
    }

    private synchronized void teardown(boolean scheduleReconnection) {
        disconnect(scheduler);

        pollTask.cancel();

        reconnectTask.cancel();
        initTask.cancel();

        if (scheduleReconnection) {
            SchedulerTask connectTask = reconnectTask;
            connectTask.schedule(5);
        }
    }

    private void connectToDevice() {
        try {
            connect(scheduler);
            scheduleNextPoll(-1);
            logger.debug("Device connected");
            updateStatus(ThingStatus.ONLINE);
        } catch (InterruptedException | RoborockCommunicationException e) {
            logger.info("Failed to connect to device");
            // should also set thing offline
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        teardown(false);
    }

    // ecoVacs
    public void connect(/* final EventListener listener, */ ScheduledExecutorService scheduler)
            throws RoborockCommunicationException, InterruptedException {
        if (rriot == null) {
            throw new RoborockCommunicationException("Can not connect when not logged in");
        }

        String mqttHost = "";
        int mqttPort = 1883;
        String mqttUser = "";
        String mqttPassword = "";
        try {
            URI mqttURL = new URI(rriot.r.m);
            mqttHost = mqttURL.getHost();
            mqttPort = mqttURL.getPort();
            mqttUser = HashUtil.md5Hex(rriot.u + ':' + rriot.k).substring(2, 10);
            mqttPassword = HashUtil.md5Hex(rriot.s + ':' + rriot.k).substring(16);
        } catch (URISyntaxException e) {
            logger.info("Malformed mqtt URL");
        }

        Mqtt3SimpleAuth auth = Mqtt3SimpleAuth.builder().username(mqttUser).password(mqttPassword.getBytes()).build();

        final MqttClientDisconnectedListener disconnectListener = ctx -> {
            boolean expectedShutdown = ctx.getSource() == MqttDisconnectSource.USER
                    && ctx.getCause() instanceof Mqtt3DisconnectException;
            // As the client already was disconnected, there's no need to do it again in disconnect() later
            this.mqttClient = null;
            if (!expectedShutdown) {
                logger.debug("{}: MQTT disconnected (source {}): {}", getThing().getUID().getId(), ctx.getSource(),
                        ctx.getCause().getMessage());
                // listener.onEventStreamFailure(EcovacsIotMqDevice.this, ctx.getCause());
            }
        };

        final Mqtt3AsyncClient client = MqttClient.builder() //
                .useMqttVersion3() //
                .identifier(mqttUser) //
                .simpleAuth(auth) //
                .serverHost(mqttHost) //
                .serverPort(mqttPort) //
                .sslWithDefaultConfig() //
                .addDisconnectedListener(disconnectListener) //
                .buildAsync();

        try {
            this.mqttClient = client;
            client.connect().get();

            final Consumer<@Nullable Mqtt3Publish> eventCallback = publish -> {
                if (publish == null) {
                    return;
                }
                String receivedTopic = publish.getTopic().toString();
                String payload = new String(publish.getPayloadAsBytes());
                // try {
                String eventName = receivedTopic.split("/")[2].toLowerCase();
                logger.trace("{}: Got MQTT message on topic {}: {}", getThing().getUID().getId(), receivedTopic,
                        payload);
                handleMessage(eventName, publish.getPayloadAsBytes());
                // } catch (DataParsingException e) {
                // listener.onEventStreamFailure(this, e);
                // }
            };

            String topic = "rr/m/o/" + rriot.u + "/" + mqttUser + "/" + getThing().getUID().getId();

            client.subscribeWith().topicFilter(topic).callback(eventCallback).send().get();
            logger.debug("Established MQTT connection to device {}", getThing().getUID().getId());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            boolean isAuthFailure = cause instanceof Mqtt3ConnAckException connAckException
                    && connAckException.getMqttMessage().getReturnCode() == Mqtt3ConnAckReturnCode.NOT_AUTHORIZED;
            throw new RoborockCommunicationException(e);
        }
    }

    public void disconnect(ScheduledExecutorService scheduler) {
        Mqtt3AsyncClient client = this.mqttClient;
        if (client != null) {
            client.disconnect();
        }
        this.mqttClient = null;
    }

    private void pollData() {
        HomeData homeData;
        homeData = bridgeHandler.getHomeData(rrHomeId, rriot);
        if (homeData != null) {
            for (int i = 0; i < homeData.result.devices.length; i++) {
                if (getThing().getUID().getId().equals(homeData.result.devices[i].duid)) {
                    if (localKey.isEmpty()) {
                        localKey = homeData.result.devices[i].localKey;
                    }
                    updateState(RoborockBindingConstants.CHANNEL_ERROR_ID,
                            new DecimalType(homeData.result.devices[i].deviceStatus.errorCode));
                    updateState(RoborockBindingConstants.CHANNEL_STATE,
                            new DecimalType(homeData.result.devices[i].deviceStatus.vacuumState));
                    updateState(RoborockBindingConstants.CHANNEL_BATTERY,
                            new DecimalType(homeData.result.devices[i].deviceStatus.battery));
                    updateState(RoborockBindingConstants.CHANNEL_FAN_POWER,
                            new DecimalType(homeData.result.devices[i].deviceStatus.fanPower));
                    updateState(RoborockBindingConstants.CHANNEL_CONSUMABLE_MAIN_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.mainBrushWorkTime));
                    updateState(RoborockBindingConstants.CHANNEL_CONSUMABLE_SIDE_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.sideBrushWorkTime));
                    updateState(RoborockBindingConstants.CHANNEL_CONSUMABLE_FILTER_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.filterWorkTime));
                    if (homeData.result.devices[i].online == true) {
                        // get MQTT data
                    }
                }
            }
            lastSuccessfulPollTimestamp = System.currentTimeMillis();
            scheduleNextPoll(-1);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public byte[] decrypt(byte[] payload, String key) throws Exception {
        byte[] aesKeyBytes = md5bin(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        logger.info("decrypt4");
        return cipher.doFinal(payload);
    }

    private byte[] md5bin(String key) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(key.getBytes("UTF-8"));
    }

    String bytesToString(byte[] data, int start, int length) {
        return new String(data, start, length, StandardCharsets.UTF_8);
    }

    int readInt32BE(byte[] data, int start) {
        return (((data[start] & 0xFF) << 24) | ((data[start + 1] & 0xFF) << 16) | ((data[start + 2] & 0xFF) << 8)
                | (data[start + 3] & 0xFF));
    }

    int readInt16BE(byte[] data, int start) {
        return (((data[start] & 0xFF) << 8) | (data[start + 1] & 0xFF));
    }

    public String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    String encodeTimestamp(int timestamp) {
        // Convert the timestamp to a hexadecimal string and pad it to ensure it's at least 8 characters
        String hex = new BigInteger(Long.toString(timestamp)).toString(16);
        hex = String.format("%8s", hex).replace(' ', '0');
        List<String> hexChars = new ArrayList<>();
        for (char c : hex.toCharArray()) {
            hexChars.add(String.valueOf(c));
        }
        // Define the order in which to rearrange the hexadecimal characters
        int[] order = { 5, 6, 3, 7, 1, 2, 0, 4 };
        StringBuilder result = new StringBuilder();
        for (int index : order) {
            result.append(hexChars.get(index));
        }
        logger.debug("encodedtimestamp = {}", result.toString());
        return result.toString();
    }

    public void handleMessage(String eventName, byte[] message) {
        String version = bytesToString(message, 0, 3);
        // Do some checks
        if (!"1.0".equals(version)) {// && version!="A01") {
            logger.debug("Parse was not version as expected:{}", version);
            return;
        }
        byte[] buf = Arrays.copyOfRange(message, 0, message.length - 4);
        CRC32 crc32 = new CRC32();
        crc32.update(buf);

        int expectedCrc32 = readInt32BE(message, message.length - 4);
        if (crc32.getValue() != expectedCrc32) {
            logger.debug("message was not crc32 {} as expected {}", crc32.getValue(), expectedCrc32);
        }
        int sequence = readInt32BE(message, 3);
        int random = readInt32BE(message, 7);
        int timestamp = readInt32BE(message, 11);
        int protocol = readInt16BE(message, 15);
        if (protocol != 102) {
            logger.debug("we don't handle images yet");
            return;
        }
        int payloadLen = readInt16BE(message, 17);
        byte[] payload = Arrays.copyOfRange(message, 19, 19 + payloadLen - 1);
        String stringPayload = new String(payload, StandardCharsets.UTF_8);
        logger.debug(
                "parsed message version: {}, sequence: {}, random: {}, timestamp: {}, protocol: {}, payloadLen: {}",
                version, sequence, random, timestamp, protocol, payloadLen);
        logger.debug("payload = {}", payload);
        String key = encodeTimestamp(timestamp) + localKey + salt;
        logger.debug(" key = {}, localKey = {}, salt = {}", key, localKey, salt);
        try {
            byte[] result = decrypt(payload, key);
            String stringResult = new String(result, StandardCharsets.UTF_8);
            logger.debug("stringResult = {}", stringResult);
        } catch (Exception e) {
            logger.debug("Exception decrypting payload");
        }
    }
}
