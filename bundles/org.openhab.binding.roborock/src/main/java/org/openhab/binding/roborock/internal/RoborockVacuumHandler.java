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
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.api.GetCleanRecord;
import org.openhab.binding.roborock.internal.api.GetConsumables;
import org.openhab.binding.roborock.internal.api.GetDndTimer;
import org.openhab.binding.roborock.internal.api.GetNetworkInfo;
import org.openhab.binding.roborock.internal.api.GetStatus;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.HomeData.Rooms;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.binding.roborock.internal.api.enums.ConsumablesType;
import org.openhab.binding.roborock.internal.api.enums.DockStatusType;
import org.openhab.binding.roborock.internal.api.enums.FanModeType;
import org.openhab.binding.roborock.internal.api.enums.RobotCapabilities;
import org.openhab.binding.roborock.internal.api.enums.StatusType;
import org.openhab.binding.roborock.internal.api.enums.VacuumErrorType;
import org.openhab.binding.roborock.internal.util.ProtocolUtils;
import org.openhab.binding.roborock.internal.util.SchedulerTask;
import org.openhab.core.library.types.DateTimeType;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
    private @NonNullByDefault({}) Rooms[] homeRooms; // fixme should not be using nonnullbydefault
    private String rrHomeId = "";
    private String localKey = "";
    private boolean hasChannelStructure;
    private ConcurrentHashMap<RobotCapabilities, Boolean> deviceCapabilities = new ConcurrentHashMap<>();
    private ChannelTypeRegistry channelTypeRegistry;
    private @Nullable Mqtt3AsyncClient mqttClient;
    private long lastSuccessfulPollTimestamp;
    private final Gson gson = new Gson();
    private String lastHistoryID = "";
    private int getStatusID = 0;
    private int getConsumableID = 0;
    private int getRoomMappingID = 0;
    private int getNetworkInfoID = 0;
    private int getCleanRecordID = 0;
    private int getCleanSummaryID = 0;
    private int getDndTimerID = 0;
    private int getSegmentStatusID = 0;
    private int getMapStatusID = 0;
    private int getLedStatusID = 0;
    private int getCarpetModeID = 0;
    private int getFwFeaturesID = 0;
    private int getMultiMapsListID = 0;
    private int getCustomizeCleanModeID = 0;
    private int getMapID = 0;

    private static final Set<RobotCapabilities> FEATURES_CHANNELS = Collections.unmodifiableSet(Stream.of(
            RobotCapabilities.SEGMENT_STATUS, RobotCapabilities.MAP_STATUS, RobotCapabilities.LED_STATUS,
            RobotCapabilities.CARPET_MODE, RobotCapabilities.FW_FEATURES, RobotCapabilities.ROOM_MAPPING,
            RobotCapabilities.MULTI_MAP_LIST, RobotCapabilities.CUSTOMIZE_CLEAN_MODE, RobotCapabilities.COLLECT_DUST,
            RobotCapabilities.CLEAN_MOP_START, RobotCapabilities.CLEAN_MOP_STOP, RobotCapabilities.MOP_DRYING,
            RobotCapabilities.MOP_DRYING_REMAINING_TIME, RobotCapabilities.DOCK_STATE_ID).collect(Collectors.toSet()));

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
            if (channelUID.getId().equals(CHANNEL_RPC)) {
                sendCommand(command.toString());
                return;
            }
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
            if (channelUID.getId().equals(CHANNEL_FAN_POWER)) {
                sendCommand(COMMAND_SET_MODE, "[" + command.toString() + "]");
                return;
            }

            if (channelUID.getId().equals(RobotCapabilities.WATERBOX_MODE.getChannel())) {
                sendCommand(COMMAND_SET_WATERBOX_MODE, "[" + command.toString() + "]");
                return;
            }
            if (channelUID.getId().equals(RobotCapabilities.MOP_MODE.getChannel())) {
                sendCommand(COMMAND_SET_MOP_MODE, "[" + command.toString() + "]");
                return;
            }
            if (channelUID.getId().equals(RobotCapabilities.SEGMENT_CLEAN.getChannel()) && !command.toString().isEmpty()
                    && !command.toString().contentEquals("-")) {
                sendCommand(COMMAND_START_SEGMENT, "[" + command.toString() + "]");
                updateState(RobotCapabilities.SEGMENT_CLEAN.getChannel(), new StringType("-"));
                return;
            }
            if (channelUID.getId().equals(CHANNEL_FAN_CONTROL)) {
                if (Integer.valueOf(command.toString()) > 0) {
                    sendCommand(COMMAND_SET_MODE, "[" + command.toString() + "]");
                }
                return;
            }
            if (channelUID.getId().equals(CHANNEL_CONSUMABLE_RESET)) {
                sendCommand(COMMAND_CONSUMABLES_RESET, "[" + command.toString() + "]");
                updateState(CHANNEL_CONSUMABLE_RESET, new StringType("none"));
            }

            if (channelUID.getId().equals(RobotCapabilities.COLLECT_DUST.getChannel()) && !command.toString().isEmpty()
                    && !command.toString().contentEquals("-")) {
                sendCommand(COMMAND_SET_COLLECT_DUST);
                return;
            }

            if (channelUID.getId().equals(RobotCapabilities.CLEAN_MOP_START.getChannel())
                    && !command.toString().isEmpty() && !command.toString().contentEquals("-")) {
                sendCommand(COMMAND_SET_CLEAN_MOP_START);
                return;
            }
            if (channelUID.getId().equals(RobotCapabilities.CLEAN_MOP_STOP.getChannel())
                    && !command.toString().isEmpty() && !command.toString().contentEquals("-")) {
                sendCommand(COMMAND_SET_CLEAN_MOP_STOP);
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
            Home home = bridgeHandler.getHomeDetail();
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
            logger.debug("Failed to connect to device");
            updateStatus(ThingStatus.OFFLINE);
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
            throw new RoborockCommunicationException("Can't connect when not logged in");
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
                // try {
                logger.debug("{}: Got MQTT message on topic {}", getThing().getUID().getId(), receivedTopic);
                String response = ProtocolUtils.handleMessage(publish.getPayloadAsBytes(), localKey);
                logger.trace("MQTT message output: {}", response);

                String jsonString = JsonParser.parseString(response).getAsJsonObject().get("dps").getAsJsonObject()
                        .get("102").getAsString();
                int messageId = JsonParser.parseString(jsonString).getAsJsonObject().get("id").getAsInt();

                if (messageId == getStatusID) {
                    logger.debug("Received getStatus response, parse it");
                    handleGetStatus(jsonString);
                } else if (messageId == getConsumableID) {
                    logger.debug("Received getConsumable response, parse it");
                    handleGetConsumables(jsonString);
                } else if (messageId == getRoomMappingID) {
                    logger.debug("Received getRoomMapping response, parse it");
                    handleGetRoomMapping(jsonString);
                } else if (messageId == getNetworkInfoID) {
                    logger.debug("Received getNetworkInfo response, parse it");
                    handleGetNetworkInfo(jsonString);
                } else if (messageId == getCleanRecordID) {
                    logger.debug("Received getCleanRecord response, parse it");
                    handleGetCleanRecord(jsonString);
                } else if (messageId == getCleanSummaryID) {
                    logger.debug("Received getCleanSummary response, parse it");
                    handleGetCleanSummary(jsonString);
                } else if (messageId == getDndTimerID) {
                    logger.debug("Received getDndTimer response, parse it");
                    handleGetDndTimer(jsonString);
                } else if (messageId == getSegmentStatusID) {
                    logger.debug("Received getSegmentStatus response, parse it");
                    handleGetSegmentStatus(jsonString);
                } else if (messageId == getMapStatusID) {
                    logger.debug("Received getMapStatus response, parse it");
                    handleGetMapStatus(jsonString);
                } else if (messageId == getLedStatusID) {
                    logger.debug("Received getLedStatus response, parse it");
                    handleGetLedStatus(jsonString);
                } else if (messageId == getCarpetModeID) {
                    logger.debug("Received getCarpetMode response, parse it");
                    handleGetCarpetMode(jsonString);
                } else if (messageId == getFwFeaturesID) {
                    logger.debug("Received getFwFeatures response, parse it");
                    handleGetFwFeatures(jsonString);
                } else if (messageId == getMultiMapsListID) {
                    logger.debug("Received MultiMapsList response, parse it");
                    handleGetMultiMapsList(jsonString);
                } else if (messageId == getCustomizeCleanModeID) {
                    logger.debug("Received getCustomizeCleanMode response, parse it");
                    handleGetCustomizeCleanMode(jsonString);
                } else if (messageId == getMapID) {
                    logger.debug("Received getMap response, parse it");
                    handleGetMap(jsonString);
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
            teardownAndScheduleReconnection();
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
        HomeData homeData = bridgeHandler.getHomeData(rrHomeId, rriot);
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
                    // also look at array size of homeData.result.rooms[i] and populate rooms list....
                    homeRooms = homeData.result.rooms;
                }
            }
            try {
                String response = bridgeHandler.getRoutines(rrHomeId, rriot);
                logger.trace("Response from getRoutines = {}", response);

                sendCommand(COMMAND_GET_STATUS);
                sendCommand(COMMAND_GET_CONSUMABLE);
                sendCommand(COMMAND_GET_ROOM_MAPPING);
                sendCommand(COMMAND_GET_NETWORK_INFO);
                sendCommand(COMMAND_GET_CLEAN_SUMMARY);
                sendCommand(COMMAND_GET_DND_TIMER);
                sendCommand(COMMAND_GET_SEGMENT_STATUS);
                sendCommand(COMMAND_GET_MAP_STATUS);
                sendCommand(COMMAND_GET_LED_STATUS);
                sendCommand(COMMAND_GET_CARPET_MODE);
                sendCommand(COMMAND_GET_FW_FEATURES);
                sendCommand(COMMAND_GET_MULTI_MAPS_LIST);
                sendCommand(COMMAND_GET_CUSTOMIZE_CLEAN_MODE);
                sendCommand(COMMAND_GET_MAP);
            } catch (UnsupportedEncodingException e) {
                // Shouldn't occur
            }
            lastSuccessfulPollTimestamp = System.currentTimeMillis();
            scheduleNextPoll(-1);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private void handleGetStatus(String response) {
        logger.trace("handleGetStatus - response {}", response);
        JsonObject statusResponse = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("result").get(0)
                .getAsJsonObject();
        GetStatus getStatus;
        getStatus = gson.fromJson(response, GetStatus.class);
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

            if (this.deviceCapabilities.containsKey(RobotCapabilities.DOCK_STATE_ID)) {
                DockStatusType dockState = DockStatusType.getType(getStatus.result[0].dockErrorStatus);
                updateState(CHANNEL_DOCK_STATE, new StringType(dockState.getDescription()));
                updateState(CHANNEL_DOCK_STATE_ID, new DecimalType(dockState.getId()));
            }

            if (deviceCapabilities.containsKey(RobotCapabilities.WATERBOX_MODE)) {
                updateState(RobotCapabilities.WATERBOX_MODE.getChannel(),
                        new DecimalType(getStatus.result[0].waterBoxMode));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.MOP_MODE)) {
                updateState(RobotCapabilities.MOP_MODE.getChannel(), new DecimalType(getStatus.result[0].mopMode));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.WATERBOX_STATUS)) {
                updateState(RobotCapabilities.WATERBOX_STATUS.getChannel(),
                        new DecimalType(getStatus.result[0].waterBoxStatus));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.WATERBOX_CARRIAGE)) {
                updateState(RobotCapabilities.WATERBOX_CARRIAGE.getChannel(),
                        new DecimalType(getStatus.result[0].waterBoxCarriageStatus));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.LOCKSTATUS)) {
                updateState(RobotCapabilities.LOCKSTATUS.getChannel(), new DecimalType(getStatus.result[0].lockStatus));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.MOP_FORBIDDEN)) {
                updateState(RobotCapabilities.MOP_FORBIDDEN.getChannel(),
                        new DecimalType(getStatus.result[0].mopForbiddenEnable));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.LOCATING)) {
                updateState(RobotCapabilities.LOCATING.getChannel(), new DecimalType(getStatus.result[0].isLocating));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.CLEAN_MOP_START)) {
                updateState(RobotCapabilities.CLEAN_MOP_START.getChannel(), new DecimalType(0));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.CLEAN_MOP_STOP)) {
                updateState(RobotCapabilities.CLEAN_MOP_STOP.getChannel(), new DecimalType(0));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.COLLECT_DUST)) {
                updateState(RobotCapabilities.COLLECT_DUST.getChannel(), new DecimalType(0));
            }
            if (deviceCapabilities.containsKey(RobotCapabilities.MOP_DRYING_REMAINING_TIME)) {
                updateState(CHANNEL_MOP_TOTAL_DRYTIME,
                        new QuantityType<>(TimeUnit.SECONDS.toMinutes(getStatus.result[0].dryStatus), Units.MINUTE));
            }
        }
    }

    private void handleGetConsumables(String response) {
        GetConsumables getConsumables = gson.fromJson(response, GetConsumables.class);
        if (getConsumables != null) {
            int mainBrush = getConsumables.result[0].mainBrushWorkTime;
            int sideBrush = getConsumables.result[0].sideBrushWorkTime;
            int filter = getConsumables.result[0].filterWorkTime;
            int sensor = getConsumables.result[0].sensorDirtyTime;
            updateState(CHANNEL_CONSUMABLE_MAIN_TIME, new QuantityType<>(
                    ConsumablesType.remainingHours(mainBrush, ConsumablesType.MAIN_BRUSH), Units.HOUR));
            updateState(CHANNEL_CONSUMABLE_MAIN_PERC,
                    new DecimalType(ConsumablesType.remainingPercent(mainBrush, ConsumablesType.MAIN_BRUSH)));
            updateState(CHANNEL_CONSUMABLE_SIDE_TIME, new QuantityType<>(
                    ConsumablesType.remainingHours(sideBrush, ConsumablesType.SIDE_BRUSH), Units.HOUR));
            updateState(CHANNEL_CONSUMABLE_SIDE_PERC,
                    new DecimalType(ConsumablesType.remainingPercent(sideBrush, ConsumablesType.SIDE_BRUSH)));
            updateState(CHANNEL_CONSUMABLE_FILTER_TIME,
                    new QuantityType<>(ConsumablesType.remainingHours(filter, ConsumablesType.FILTER), Units.HOUR));
            updateState(CHANNEL_CONSUMABLE_FILTER_PERC,
                    new DecimalType(ConsumablesType.remainingPercent(filter, ConsumablesType.FILTER)));
            updateState(CHANNEL_CONSUMABLE_SENSOR_TIME,
                    new QuantityType<>(ConsumablesType.remainingHours(sensor, ConsumablesType.SENSOR), Units.HOUR));
            updateState(CHANNEL_CONSUMABLE_SENSOR_PERC,
                    new DecimalType(ConsumablesType.remainingPercent(sensor, ConsumablesType.SENSOR)));
        }
    }

    private void handleGetRoomMapping(String response) {
        logger.trace("getRoomMapping response = {}", response);
        for (RobotCapabilities cmd : FEATURES_CHANNELS) {
            if (COMMAND_GET_ROOM_MAPPING.equals(cmd.getCommand())) {
                JsonArray rooms = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
                if (rooms.size() > 0) {
                    JsonArray mappedRoom = new JsonArray();
                    String name = "Not found";
                    for (JsonElement roomE : rooms) {
                        JsonArray room = roomE.getAsJsonArray();
                        for (int i = 0; i < homeRooms.length; i++) {
                            if (room.get(1).getAsString().equals(Integer.toString(homeRooms[i].id))) {
                                name = homeRooms[i].name;
                                break;
                            }
                        }
                        room.set(1, new JsonPrimitive(name));
                        if (room.size() == 3) {
                            room.remove(2);
                        }
                        mappedRoom.add(room);
                    }
                    updateState(cmd.getChannel(), new StringType(mappedRoom.toString()));
                } else {
                    updateState(cmd.getChannel(), new StringType(response));
                }
                break;
            }
        }
    }

    private void handleGetNetworkInfo(String response) {
        GetNetworkInfo getNetworkInfo = gson.fromJson(response, GetNetworkInfo.class);
        if (getNetworkInfo != null) {
            updateState(CHANNEL_SSID, new StringType(getNetworkInfo.result.ssid));
            updateState(CHANNEL_BSSID, new StringType(getNetworkInfo.result.bssid));
            updateState(CHANNEL_RSSI, new DecimalType(getNetworkInfo.result.rssi));
        }
    }

    private void handleGetCleanRecord(String response) {
        logger.trace("handleGetCleanRecord, response = {}", response);
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray()
                && JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray().size() > 0
                && JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray().get(0)
                        .isJsonArray()) {
            JsonArray historyData = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray()
                    .get(0).getAsJsonArray();
            Map<String, Object> historyRecord = new HashMap<>();
            for (int i = 0; i < historyData.size(); ++i) {
                try {
                    BigInteger value = historyData.get(i).getAsBigInteger();
                    switch (i) {
                        case 0:
                            DateTimeType begin = new DateTimeType(Instant.ofEpochSecond(value.longValue()));
                            historyRecord.put("begin", begin.format(null));
                            updateState(CHANNEL_HISTORY_START_TIME, begin);
                            break;
                        case 1:
                            DateTimeType end = new DateTimeType(Instant.ofEpochSecond(value.longValue()));
                            historyRecord.put("end", end.format(null));
                            updateState(CHANNEL_HISTORY_END_TIME, end);
                            break;
                        case 2:
                            long duration = TimeUnit.SECONDS.toMinutes(value.intValue());
                            historyRecord.put("duration", duration);
                            updateState(CHANNEL_HISTORY_DURATION, new QuantityType<>(duration, Units.MINUTE));
                            break;
                        case 3:
                            historyRecord.put("area", value);
                            updateState(CHANNEL_HISTORY_AREA, new QuantityType<>(value, SIUnits.SQUARE_METRE));
                            break;
                        case 4:
                            historyRecord.put("error", value.intValue());
                            updateState(CHANNEL_HISTORY_ERROR, new DecimalType(value.intValue()));
                            break;
                        case 5:
                            historyRecord.put("complete", value.intValue());
                            updateState(CHANNEL_HISTORY_FINISH, new DecimalType(value.intValue()));
                            break;
                        case 6:
                            historyRecord.put("startType", value.intValue());
                            break;
                        case 7:
                            historyRecord.put("cleanType", value.intValue());
                            break;
                        case 8:
                            historyRecord.put("finishReason", value.intValue());
                            updateState(CHANNEL_HISTORY_FINISHREASON, new DecimalType(value.intValue()));
                            break;
                    }
                } catch (ClassCastException | NumberFormatException | IllegalStateException e) {
                }
            }
            updateState(CHANNEL_HISTORY_RECORD, new StringType(gson.toJson(historyRecord)));
        } else {
            GetCleanRecord getCleanRecord = gson.fromJson(response, GetCleanRecord.class);
            Map<String, Object> historyRecord = new HashMap<>();
            if (getCleanRecord != null) {
                DateTimeType begin = new DateTimeType(Instant.ofEpochSecond(getCleanRecord.result[0].begin));
                historyRecord.put("begin", begin.format(null));
                updateState(CHANNEL_HISTORY_START_TIME, begin);
                DateTimeType end = new DateTimeType(Instant.ofEpochSecond(getCleanRecord.result[0].end));
                historyRecord.put("end", end.format(null));
                updateState(CHANNEL_HISTORY_END_TIME, end);
                long duration = TimeUnit.SECONDS.toMinutes(getCleanRecord.result[0].duration);
                historyRecord.put("duration", duration);
                updateState(CHANNEL_HISTORY_DURATION, new QuantityType<>(duration, Units.MINUTE));
                historyRecord.put("area", getCleanRecord.result[0].area);
                updateState(CHANNEL_HISTORY_AREA,
                        new QuantityType<>(getCleanRecord.result[0].area, SIUnits.SQUARE_METRE));
                historyRecord.put("error", getCleanRecord.result[0].error);
                updateState(CHANNEL_HISTORY_ERROR, new DecimalType(getCleanRecord.result[0].error));
                historyRecord.put("complete", getCleanRecord.result[0].complete);
                updateState(CHANNEL_HISTORY_FINISH, new DecimalType(getCleanRecord.result[0].complete));
                historyRecord.put("finish_reason", getCleanRecord.result[0].finishReason);
                updateState(CHANNEL_HISTORY_FINISHREASON, new DecimalType(getCleanRecord.result[0].finishReason));
                historyRecord.put("dust_collection_status", getCleanRecord.result[0].dustCollectionStatus);
                updateState(CHANNEL_HISTORY_DUSTCOLLECTION,
                        new DecimalType(getCleanRecord.result[0].dustCollectionStatus));
                updateState(CHANNEL_HISTORY_RECORD, new StringType(gson.toJson(historyRecord)));
            }
        }
    }

    private void handleGetCleanSummary(String response) {
        logger.trace("handleGetCleanSummary, response = {}", response);
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray()) {
            logger.debug("old clean summary format");
            JsonArray historyData = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
            updateState(CHANNEL_HISTORY_TOTALTIME,
                    new QuantityType<>(TimeUnit.SECONDS.toMinutes(historyData.get(0).getAsLong()), Units.MINUTE));
            updateState(CHANNEL_HISTORY_TOTALAREA,
                    new QuantityType<>(historyData.get(1).getAsDouble() / 1000000D, SIUnits.SQUARE_METRE));
            updateState(CHANNEL_HISTORY_COUNT, new DecimalType(historyData.get(2).toString()));
            if (historyData.get(3).getAsJsonArray().size() > 0) {
                String lastClean = historyData.get(3).getAsJsonArray().get(0).getAsString();
                if (!lastClean.equals(lastHistoryID)) {
                    lastHistoryID = lastClean;
                    try {
                        sendCommand(COMMAND_GET_CLEAN_RECORD, "[" + lastClean + "]");
                    } catch (UnsupportedEncodingException e) {
                        // Shouldn't occur
                    }
                }
            }
        } else {
            JsonObject cleanSummary = JsonParser.parseString(response).getAsJsonObject().get("result")
                    .getAsJsonObject();
            updateState(CHANNEL_HISTORY_TOTALTIME, new QuantityType<>(
                    TimeUnit.SECONDS.toMinutes(cleanSummary.get("clean_time").getAsLong()), Units.MINUTE));
            updateState(CHANNEL_HISTORY_TOTALAREA,
                    new QuantityType<>(cleanSummary.get("clean_area").getAsDouble() / 1000000D, SIUnits.SQUARE_METRE));
            updateState(CHANNEL_HISTORY_COUNT, new DecimalType(cleanSummary.get("clean_count").getAsLong()));
            if (cleanSummary.has("records") & cleanSummary.get("records").isJsonArray()) {
                JsonArray cleanSummaryRecords = cleanSummary.get("records").getAsJsonArray();
                if (!cleanSummaryRecords.isEmpty()) {
                    String lastClean = cleanSummaryRecords.get(0).getAsString();
                    if (!lastClean.equals(lastHistoryID)) {
                        lastHistoryID = lastClean;
                        try {
                            logger.debug("sending command for getCleanRecord, id = {}", lastClean);
                            sendCommand(COMMAND_GET_CLEAN_RECORD, "[" + lastClean + "]");
                        } catch (UnsupportedEncodingException e) {
                            // Shouldn't occur
                        }
                    }
                }
            }
        }
    }

    private void handleGetDndTimer(String response) {
        logger.trace("handleGetDndTimer, response = {}", response);
        GetDndTimer getDndTimer = gson.fromJson(response, GetDndTimer.class);
        updateState(CHANNEL_DND_FUNCTION, new DecimalType(getDndTimer.result[0].enabled));
        updateState(CHANNEL_DND_START, new StringType(
                String.format("%02d:%02d", getDndTimer.result[0].startHour, getDndTimer.result[0].startMinute)));
        updateState(CHANNEL_DND_END, new StringType(
                String.format("%02d:%02d", getDndTimer.result[0].endHour, getDndTimer.result[0].endMinute)));
    }

    private void handleGetSegmentStatus(String response) {
        logger.trace("handleGetSegmentStatus, response = {}", response);
        JsonArray getSegmentStatus = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
        Integer stat = getSegmentStatus.get(0).getAsInt();
        updateState(RobotCapabilities.SEGMENT_STATUS.getChannel(), new DecimalType(stat));
    }

    private void handleGetMapStatus(String response) {
        logger.trace("handleGetMapStatus, response = {}", response);
        JsonArray getSegmentStatus = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
        Integer stat = getSegmentStatus.get(0).getAsInt();
        updateState(RobotCapabilities.MAP_STATUS.getChannel(), new DecimalType(stat));
    }

    private void handleGetLedStatus(String response) {
        logger.trace("handleGetLedStatus, response = {}", response);
        JsonArray getSegmentStatus = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
        Integer stat = getSegmentStatus.get(0).getAsInt();
        updateState(RobotCapabilities.LED_STATUS.getChannel(), new DecimalType(stat));
    }

    private void handleGetCarpetMode(String response) {
        logger.trace("handleGetCarpetMode, response = {}", response);
        JsonArray getCarpetMode = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
        updateState(RobotCapabilities.CARPET_MODE.getChannel(),
                new StringType(getCarpetMode.get(0).getAsJsonObject().toString()));
    }

    private void handleGetFwFeatures(String response) {
        logger.trace("handleGetFwFeatures, response = {}", response);
        JsonArray getFwFeatures = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
        updateState(RobotCapabilities.FW_FEATURES.getChannel(), new StringType(getFwFeatures.toString()));
    }

    private void handleGetMultiMapsList(String response) {
        logger.trace("handleGetMultiMapsList, response = {}", response);
        JsonArray getMultiMapsList = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
        updateState(RobotCapabilities.MULTI_MAP_LIST.getChannel(),
                new StringType(getMultiMapsList.get(0).getAsJsonObject().toString()));
    }

    private void handleGetCustomizeCleanMode(String response) {
        logger.trace("handleGetCustomizeCleanMode, response = {}", response);
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray()) {
            JsonArray getCustomizeCleanMode = JsonParser.parseString(response).getAsJsonObject().get("result")
                    .getAsJsonArray();
            updateState(RobotCapabilities.CUSTOMIZE_CLEAN_MODE.getChannel(),
                    new StringType(getCustomizeCleanMode.toString()));
        }
    }

    private void handleGetMap(String response) {
        logger.trace("handleGetMap, response = {}", response);
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

    private void sendCommand(String method) throws UnsupportedEncodingException {
        sendCommand(method, "[]");
    }

    private void sendCommand(String method, String params) throws UnsupportedEncodingException {
        int timestamp = (int) Instant.now().getEpochSecond();
        int protocol = 101;
        Random random = new Random();
        int id = random.nextInt(22767 + 1) + 10000;

        byte[] nonce = new byte[16];
        new java.security.SecureRandom().nextBytes(nonce);

        Map<String, Object> security = new HashMap<>();
        security.put("endpoint", getEndpoint());
        security.put("nonce", new String(nonce, StandardCharsets.UTF_8));

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

        byte[] message = build(getThing().getUID().getId(), protocol, timestamp, modPayload.getBytes("UTF-8"));
        // now send message via mqtt
        String mqttUser = ProtocolUtils.md5Hex(rriot.u + ':' + rriot.k).substring(2, 10);

        String topic = "rr/m/i/" + rriot.u + "/" + mqttUser + "/" + getThing().getUID().getId();
        mqttClient.publishWith().topic(topic).payload(message).retain(false).send()
                .whenComplete((mqtt3Publish, throwable) -> {
                    if (throwable != null) {
                        logger.debug("mqtt publish failed");
                    } else {
                        if (COMMAND_GET_STATUS.equals(method)) {
                            getStatusID = id;
                        } else if (COMMAND_GET_CONSUMABLE.equals(method)) {
                            getConsumableID = id;
                        } else if (COMMAND_GET_ROOM_MAPPING.equals(method)) {
                            getRoomMappingID = id;
                        } else if (COMMAND_GET_NETWORK_INFO.equals(method)) {
                            getNetworkInfoID = id;
                        } else if (COMMAND_GET_CLEAN_RECORD.equals(method)) {
                            getCleanRecordID = id;
                        } else if (COMMAND_GET_CLEAN_SUMMARY.equals(method)) {
                            getCleanSummaryID = id;
                        } else if (COMMAND_GET_DND_TIMER.equals(method)) {
                            getDndTimerID = id;
                        } else if (COMMAND_GET_SEGMENT_STATUS.equals(method)) {
                            getSegmentStatusID = id;
                        } else if (COMMAND_GET_MAP_STATUS.equals(method)) {
                            getMapStatusID = id;
                        } else if (COMMAND_GET_LED_STATUS.equals(method)) {
                            getLedStatusID = id;
                        } else if (COMMAND_GET_CARPET_MODE.equals(method)) {
                            getCarpetModeID = id;
                        } else if (COMMAND_GET_FW_FEATURES.equals(method)) {
                            getFwFeaturesID = id;
                        } else if (COMMAND_GET_MULTI_MAPS_LIST.equals(method)) {
                            getMultiMapsListID = id;
                        } else if (COMMAND_GET_CUSTOMIZE_CLEAN_MODE.equals(method)) {
                            getCustomizeCleanModeID = id;
                        } else if (COMMAND_GET_MAP.equals(method)) {
                            getMapID = id;
                        }
                    }
                });

        // Mqtt3Publish publishMessage = Mqtt3Publish.builder().topic(topic).payload(message).retain(false).build();

        // mqttClient.publish(publishMessage);
        // handleMessage(message); // helps confirm we have encoded it correctly
    }

    byte[] build(String deviceId, int protocol, int timestamp, byte[] payload) {
        try {
            String key = ProtocolUtils.encodeTimestamp(timestamp) + localKey + SALT;
            byte[] encrypted = ProtocolUtils.encrypt(payload, key);

            Random random = new Random();
            int randomInt = random.nextInt(90000) + 10000;
            int seq = random.nextInt(900000) + 100000;

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
}
