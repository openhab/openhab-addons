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
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.CRC32;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.api.GetConsumables;
import org.openhab.binding.roborock.internal.api.GetStatus;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.binding.roborock.internal.api.enums.DockStatusType;
import org.openhab.binding.roborock.internal.api.enums.FanModeType;
import org.openhab.binding.roborock.internal.api.enums.RobotCapabilities;
import org.openhab.binding.roborock.internal.api.enums.StatusType;
import org.openhab.binding.roborock.internal.api.enums.VacuumErrorType;
import org.openhab.binding.roborock.internal.util.HashUtil;
import org.openhab.binding.roborock.internal.util.SchedulerTask;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    private int stateId;
    private boolean hasChannelStructure;
    private ConcurrentHashMap<RobotCapabilities, Boolean> deviceCapabilities = new ConcurrentHashMap<>();
    private ChannelTypeRegistry channelTypeRegistry;
    private @Nullable Mqtt3AsyncClient mqttClient;
    private long lastSuccessfulPollTimestamp;
    static final String salt = "TXdfu$jyZ#TZHsg4";
    private final Gson gson = new Gson();
    int getStatusID = 0;
    int getConsumableID = 0;

    public RoborockVacuumHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
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
        try {
            if (channelUID.getId().equals(CHANNEL_VACUUM)) {
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        sendCommand(COMMAND_APP_START);
                        return;
                    } else {
                        sendCommand(COMMAND_APP_STOP);
                        return;
                    }
                }
            }

            if (channelUID.getId().equals(CHANNEL_CONTROL) && command instanceof StringType) {
                if ("vacuum".equals(command.toString())) {
                    sendCommand(COMMAND_APP_START);
                } else if ("spot".equals(command.toString())) {
                    sendCommand(COMMAND_APP_SPOT);
                } else if ("pause".equals(command.toString())) {
                    sendCommand(COMMAND_APP_PAUSE);
                } else if ("dock".equals(command.toString())) {
                    sendCommand(COMMAND_APP_STOP);
                } else {
                    logger.info("Command {} not recognised", command.toString());
                }
                return;
            }
        } catch (UnsupportedEncodingException e) {
            logger.debug("UnsupportedEncodingException, {}", e.getMessage());
        }
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
        hasChannelStructure = false;
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

    public void connect(ScheduledExecutorService scheduler)
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
                logger.debug("{}: Got MQTT message on topic {}", getThing().getUID().getId(), receivedTopic);
                String response = handleMessage(publish.getPayloadAsBytes());
                logger.trace("MQTT message output: {}", response);

                JsonParser jsonParser = new JsonParser();
                String jsonObject = jsonParser.parse(response).getAsJsonObject().get("dps").getAsJsonObject().get("102")
                        .getAsString();
                int messageId = jsonParser.parse(jsonObject).getAsJsonObject().get("id").getAsInt();

                if (messageId == getStatusID) {
                    logger.debug("Received getStatus response, parse it");
                    handleGetStatus(jsonObject);
                } else if (messageId == getConsumableID) {
                    logger.debug("Received getConsumable response, parse it");
                    handleGetConsumables(jsonObject);
                }

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
                    updateState(CHANNEL_ERROR_ID, new DecimalType(homeData.result.devices[i].deviceStatus.errorCode));
                    updateState(CHANNEL_STATE, new DecimalType(homeData.result.devices[i].deviceStatus.vacuumState));
                    updateState(CHANNEL_BATTERY, new DecimalType(homeData.result.devices[i].deviceStatus.battery));
                    updateState(CHANNEL_FAN_POWER, new DecimalType(homeData.result.devices[i].deviceStatus.fanPower));
                    updateState(CHANNEL_MOP_DRYING,
                            new DecimalType(homeData.result.devices[i].deviceStatus.dryingStatus));

                    updateState(CHANNEL_CONSUMABLE_MAIN_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.mainBrushWorkTime));
                    updateState(CHANNEL_CONSUMABLE_SIDE_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.sideBrushWorkTime));
                    updateState(CHANNEL_CONSUMABLE_FILTER_PERC,
                            new DecimalType(homeData.result.devices[i].deviceStatus.filterWorkTime));
                }
            }
            try {
                logger.debug("Sending command: {}", COMMAND_GET_STATUS);
                sendCommand(COMMAND_GET_STATUS);
                sendCommand(COMMAND_GET_CONSUMABLE);
            } catch (UnsupportedEncodingException e) {
                // Shouldn't occur
            }
            lastSuccessfulPollTimestamp = System.currentTimeMillis();
            scheduleNextPoll(-1);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void handleGetStatus(String response) {
        GetStatus getStatus = gson.fromJson(response, GetStatus.class);
        JsonParser jsonParser = new JsonParser();
        JsonObject statusResponse = jsonParser.parse(response).getAsJsonObject().getAsJsonArray("result").get(0)
                .getAsJsonObject();
        if (!hasChannelStructure) {
            setCapabilities(statusResponse);
            createCapabilityChannels();
        }
        if (getStatus != null) {
            updateState(CHANNEL_BATTERY, new DecimalType(getStatus.result[0].battery));
            updateState(CHANNEL_FAN_POWER, new DecimalType(getStatus.result[0].fanPower));
            updateState(CHANNEL_FAN_CONTROL,
                    new DecimalType(FanModeType.getType(getStatus.result[0].fanPower).getId()));
            updateState(CHANNEL_CLEAN_AREA,
                    new QuantityType<>(getStatus.result[0].cleanArea / 1000000.0, SIUnits.SQUARE_METRE));
            updateState(CHANNEL_CLEAN_TIME,
                    new QuantityType<>(TimeUnit.SECONDS.toMinutes(getStatus.result[0].cleanTime), Units.MINUTE));
            updateState(CHANNEL_DND_ENABLED, new DecimalType(getStatus.result[0].dndEnabled));
            updateState(CHANNEL_ERROR_CODE,
                    new StringType(VacuumErrorType.getType(getStatus.result[0].errorCode).getDescription()));
            updateState(CHANNEL_ERROR_ID, new DecimalType(getStatus.result[0].errorCode));
            updateState(CHANNEL_IN_CLEANING, new DecimalType(getStatus.result[0].inCleaning));
            updateState(CHANNEL_MAP_PRESENT, new DecimalType(getStatus.result[0].mapPresent));

            // handle vacuum state
            stateId = getStatus.result[0].state;
            StatusType state = StatusType.getType(getStatus.result[0].state);
            updateState(CHANNEL_STATE, new StringType(state.getDescription()));
            updateState(CHANNEL_STATE_ID, new DecimalType(getStatus.result[0].state));

            State vacuum = OnOffType.OFF;
            String control;
            switch (state) {
                case ZONE:
                case ROOM:
                case CLEANING:
                case RETURNING:
                    control = "vacuum";
                    vacuum = OnOffType.ON;
                    break;
                case CHARGING:
                case CHARGING_ERROR:
                case DOCKING:
                case FULL:
                    control = "dock";
                    break;
                case SLEEPING:
                case PAUSED:
                case IDLE:
                    control = "pause";
                    break;
                case SPOTCLEAN:
                    control = "spot";
                    vacuum = OnOffType.ON;
                    break;
                default:
                    control = "undef";
                    break;
            }
            if ("undef".equals(control)) {
                updateState(CHANNEL_CONTROL, UnDefType.UNDEF);
            } else {
                updateState(CHANNEL_CONTROL, new StringType(control));
            }
            updateState(CHANNEL_VACUUM, vacuum);

            DockStatusType dockState = DockStatusType.getType(getStatus.result[0].dockErrorStatus);
            updateState(CHANNEL_DOCK_STATE, new StringType(dockState.getDescription()));
            updateState(CHANNEL_DOCK_STATE_ID, new DecimalType(dockState.getId()));

            // miio checks for vac capabilities before populating these channels
            updateState(CHANNEL_WATER_BOX_MODE, new DecimalType(getStatus.result[0].waterBoxMode));
            updateState(CHANNEL_MOP_MODE, new DecimalType(getStatus.result[0].mopMode));
            updateState(CHANNEL_WATERBOX_STATUS, new DecimalType(getStatus.result[0].waterBoxStatus));
            updateState(CHANNEL_WATERBOX_CARRIAGE, new DecimalType(getStatus.result[0].waterBoxCarriageStatus));
            updateState(CHANNEL_LOCKSTATUS, new DecimalType(getStatus.result[0].lockStatus));
            updateState(CHANNEL_MOP_FORBIDDEN, new DecimalType(getStatus.result[0].mopForbiddenEnable));
            updateState(CHANNEL_LOCATING, new DecimalType(getStatus.result[0].isLocating));
            updateState(CHANNEL_CLEAN_MOP_START, new DecimalType(0));
            updateState(CHANNEL_CLEAN_MOP_STOP, new DecimalType(0));
            updateState(CHANNEL_COLLECT_DUST, new DecimalType(0));
            // I don't see a suitable piece of data for this with my Qrevo S
            // updateState(CHANNEL_MOP_TOTAL_DRYTIME,
            // new QuantityType<>(TimeUnit.SECONDS.toMinutes(getStatus.result[0].mopDryTime), Units.MINUTE));
        }
    }

    public void handleGetConsumables(String response) {
        GetConsumables getConsumables = gson.fromJson(response, GetConsumables.class);
        if (getConsumables != null) {
            updateState(CHANNEL_CONSUMABLE_MAIN_TIME, new DecimalType(getConsumables.result[0].mainBrushWorkTime));
            updateState(CHANNEL_CONSUMABLE_SIDE_TIME, new DecimalType(getConsumables.result[0].sideBrushWorkTime));
            updateState(CHANNEL_CONSUMABLE_FILTER_TIME, new DecimalType(getConsumables.result[0].filterWorkTime));
            updateState(CHANNEL_CONSUMABLE_SENSOR_TIME, new DecimalType(getConsumables.result[0].sensorDirtyTime));
        }
    }

    private void setCapabilities(JsonObject statusResponse) {
        for (RobotCapabilities capability : RobotCapabilities.values()) {
            if (statusResponse.has(capability.getStatusFieldName())) {
                deviceCapabilities.putIfAbsent(capability, false);
                logger.debug("Setting additional vacuum {}", capability);
            }
        }
    }

    private void createCapabilityChannels() {
        ThingBuilder thingBuilder = editThing();
        int cnt = 0;

        for (Entry<RobotCapabilities, Boolean> robotCapability : deviceCapabilities.entrySet()) {
            RobotCapabilities capability = robotCapability.getKey();
            Boolean channelCreated = robotCapability.getValue();
            if (!channelCreated) {
                if (thing.getChannels().stream()
                        .anyMatch(ch -> ch.getUID().getId().equalsIgnoreCase(capability.getChannel()))) {
                    logger.debug("Channel already available...skip creation of channel '{}'.", capability.getChannel());
                    deviceCapabilities.replace(capability, true);
                    continue;
                }
                logger.debug("Creating dynamic channel for capability {}", capability);
                ChannelType channelType = channelTypeRegistry.getChannelType(capability.getChannelType());
                if (channelType != null) {
                    logger.debug("Found channelType '{}' for capability {}", channelType, capability.name());
                    ChannelUID channelUID = new ChannelUID(getThing().getUID(), capability.getChannel());
                    Channel channel = ChannelBuilder.create(channelUID, channelType.getItemType())
                            .withType(capability.getChannelType()).withLabel(channelType.getLabel()).build();
                    thingBuilder.withChannel(channel);
                    cnt++;
                } else {
                    logger.debug("ChannelType {} not found (Unexpected). Available types:",
                            capability.getChannelType());
                    for (ChannelType ct : channelTypeRegistry.getChannelTypes()) {
                        logger.debug("Available channelType: '{}' '{}' '{}'", ct.getUID(), ct.toString(),
                                ct.getConfigDescriptionURI());
                    }
                }
            }
        }
        if (cnt > 0) {
            updateThing(thingBuilder.build());
        }
        hasChannelStructure = true;
    }

    public byte[] decrypt(byte[] payload, String key) throws Exception {
        byte[] aesKeyBytes = md5bin(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(payload);
    }

    public byte[] encrypt(byte[] payload, String key) throws Exception {
        byte[] aesKeyBytes = md5bin(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
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

    void writeInt32BE(byte[] msg, int value, int start) {
        msg[start + 0] = (byte) ((value >> 24) & 0xFF);
        msg[start + 1] = (byte) ((value >> 16) & 0xFF);
        msg[start + 2] = (byte) ((value >> 8) & 0xFF);
        msg[start + 3] = (byte) (value & 0xFF);
    }

    void writeInt16BE(byte[] msg, int value, int start) {
        msg[start + 0] = (byte) ((value >> 8) & 0xFF);
        msg[start + 1] = (byte) (value & 0xFF);
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
        return result.toString();
    }

    public String handleMessage(byte[] message) {
        String version = bytesToString(message, 0, 3);
        // Do some checks
        if (!"1.0".equals(version)) {// && version!="A01") {
            logger.debug("Parse was not version as expected:{}", version);
            return "";
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
        }
        int payloadLen = readInt16BE(message, 17);
        byte[] payload = Arrays.copyOfRange(message, 19, 19 + payloadLen);
        String stringPayload = new String(payload, StandardCharsets.UTF_8);
        logger.debug(
                "parsed message version: {}, sequence: {}, random: {}, timestamp: {}, protocol: {}, payloadLen: {}",
                version, sequence, random, timestamp, protocol, payloadLen);
        String key = encodeTimestamp(timestamp) + localKey + salt;
        try {
            byte[] result = decrypt(payload, key);
            String stringResult = new String(result, StandardCharsets.UTF_8);
            return stringResult;
        } catch (Exception e) {
            logger.debug("Exception decrypting payload, {}", e.getMessage());
            return "";
        }
    }

    public void sendCommand(String method) throws UnsupportedEncodingException {
        sendCommand(method, "");
    }

    public void sendCommand(String method, String params) throws UnsupportedEncodingException {
        int timestamp = (int) Instant.now().getEpochSecond();
        int protocol = 101;
        Random random = new Random();
        int id = random.nextInt(22767 + 1) + 10000;

        Map<String, Object> inner = new HashMap<>();
        inner.put("id", id);
        inner.put("method", method);
        inner.put("params", "[]");

        Map<String, Object> dps = new HashMap<>();
        dps.put(Integer.toString(protocol), new Gson().toJson(inner));

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("t", timestamp);
        payloadMap.put("dps", dps);

        String payload = new Gson().toJson(payloadMap);
        logger.debug("payload = {}", payload);
        byte[] message = build(getThing().getUID().getId(), protocol, timestamp, payload.getBytes("UTF-8"));
        // now send message via mqtt
        String mqttUser = HashUtil.md5Hex(rriot.u + ':' + rriot.k).substring(2, 10);

        String topic = "rr/m/i/" + rriot.u + "/" + mqttUser + "/" + getThing().getUID().getId();
        mqttClient.publishWith().topic(topic).payload(message).retain(false).send()
                .whenComplete((mqtt3Publish, throwable) -> {
                    if (throwable != null) {
                        logger.info("mqtt publish failed");
                    } else {
                        logger.info("mqtt publish succeeded");
                        if (COMMAND_GET_STATUS.equals(method)) {
                            getStatusID = id;
                        } else if (COMMAND_GET_CONSUMABLE.equals(method)) {
                            getConsumableID = id;
                        }
                    }
                });

        // Mqtt3Publish publishMessage = Mqtt3Publish.builder().topic(topic).payload(message).retain(false).build();

        // mqttClient.publish(publishMessage);
        // handleMessage(message); // helps confirm we have encoded it correctly
    }

    byte[] build(String deviceId, int protocol, int timestamp, byte[] payload) {
        try {
            String key = encodeTimestamp(timestamp) + localKey + salt;
            byte[] encrypted = encrypt(payload, key);

            Random random = new Random();
            int randomInt = random.nextInt(90000) + 10000;
            int seq = random.nextInt(900000) + 100000;

            int totalLength = 23 + encrypted.length;
            byte[] msg = new byte[totalLength];
            // Writing fixed string '1.0'
            msg[0] = 49; // ASCII for '1'
            msg[1] = 46; // ASCII for '.'
            msg[2] = 48; // ASCII for '0'
            writeInt32BE(msg, (int) (seq & 0xFFFFFFFF), 3);
            writeInt32BE(msg, (int) (randomInt & 0xFFFFFFFF), 7);
            writeInt32BE(msg, timestamp, 11);
            writeInt16BE(msg, protocol, 15);
            writeInt16BE(msg, encrypted.length, 17);
            // Manually copying encrypted data into msg
            for (int i = 0; i < encrypted.length; i++) {
                msg[19 + i] = encrypted[i];
            }
            byte[] buf = Arrays.copyOfRange(msg, 0, msg.length - 4);
            CRC32 crc32 = new CRC32();
            crc32.update(buf);
            writeInt32BE(msg, (int) crc32.getValue(), msg.length - 4);
            return msg;
        } catch (Exception e) {
            logger.debug("Exception encrypting payload, {}", e.getMessage());
            return new byte[0];
        }
    }
}
