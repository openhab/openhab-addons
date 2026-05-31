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
package org.openhab.binding.roborock.internal;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.action.RoborockVacuumActions;
import org.openhab.binding.roborock.internal.api.GetCleanRecord;
import org.openhab.binding.roborock.internal.api.GetConsumables;
import org.openhab.binding.roborock.internal.api.GetDndTimer;
import org.openhab.binding.roborock.internal.api.GetNetworkInfo;
import org.openhab.binding.roborock.internal.api.GetStatus;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.HomeData.Devices;
import org.openhab.binding.roborock.internal.api.HomeData.Rooms;
import org.openhab.binding.roborock.internal.api.enums.ConsumablesType;
import org.openhab.binding.roborock.internal.api.enums.FanModeType;
import org.openhab.binding.roborock.internal.api.enums.RobotCapabilities;
import org.openhab.binding.roborock.internal.api.enums.StatusType;
import org.openhab.binding.roborock.internal.api.enums.VacuumErrorType;
import org.openhab.binding.roborock.internal.map.RRMapData;
import org.openhab.binding.roborock.internal.map.RRMapParser;
import org.openhab.binding.roborock.internal.map.RRMapRenderer;
import org.openhab.binding.roborock.internal.transport.CloudMqttTransport;
import org.openhab.binding.roborock.internal.transport.LocalDirectTransport;
import org.openhab.binding.roborock.internal.transport.RoborockCommandTransport;
import org.openhab.binding.roborock.internal.util.ProtocolUtils;
import org.openhab.binding.roborock.internal.util.RequestCorrelationTracker;
import org.openhab.binding.roborock.internal.util.SchedulerTask;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link RoborockVacuumHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockVacuumHandler extends BaseThingHandler {

    private static final int REQUEST_ID_SYNC_DIRECT_COMPLETED = -2;
    private static final String MAP_DOWNLOAD_REQUEST_METHOD = "downloadRrMap";
    private static final long MAP_DOWNLOAD_TIMEOUT_MS = 30_000L;

    private final Logger logger = LoggerFactory.getLogger(RoborockVacuumHandler.class);

    private @Nullable RoborockAccountHandler bridgeHandler;
    private final SchedulerTask initTask;
    private final SchedulerTask pollTask;
    private final SchedulerTask statusPollTask;
    private final RoborockStateDescriptionOptionProvider stateDescriptionProvider;
    private String token = "";
    private Rooms[] homeRooms = new Rooms[0];
    private String rrHomeId = "";
    private String localKey = "";
    private String localIP = "";
    private String endpointPrefix = "";
    private final byte[] nonce = new byte[16];
    private boolean hasChannelStructure;
    private RoborockCommunicationMode communicationMode = RoborockCommunicationMode.CLOUD;
    private @Nullable RoborockCommandTransport cloudTransport;
    private @Nullable RoborockCommandTransport directTransport;
    private ConcurrentHashMap<RobotCapabilities, Boolean> deviceCapabilities = new ConcurrentHashMap<>();
    private ChannelTypeRegistry channelTypeRegistry;
    private long lastSuccessfulPollTimestamp;
    private long lastCloudOnlyPollTimestamp;
    private long lastMapPollTimestamp;
    private boolean supportsRoutines = true;
    private boolean cloudMapRefreshDisabledLogged;
    private boolean cloudMetadataRefreshDisabledLogged;
    private volatile boolean vacuumChannelOn;
    private @Nullable Integer lastKnownStateId;
    private final Gson gson = new Gson();
    private final SecureRandom secureRandom = new SecureRandom();
    protected RoborockVacuumConfiguration config = new RoborockVacuumConfiguration();

    private String lastHistoryID = "";

    private final RequestCorrelationTracker requestCorrelationTracker = new RequestCorrelationTracker();
    private final RRMapParser rrMapParser = new RRMapParser();
    private final RRMapRenderer rrMapRenderer = new RRMapRenderer();
    private final MapUpdateDeduplicator mapUpdateDeduplicator = new MapUpdateDeduplicator();
    private final Map<Integer, CompletableFuture<byte[]>> pendingRrMapDownloads = new ConcurrentHashMap<>();

    private static final Set<RobotCapabilities> FEATURES_CHANNELS = Collections.unmodifiableSet(
            EnumSet.of(RobotCapabilities.SEGMENT_STATUS, RobotCapabilities.MAP_STATUS, RobotCapabilities.LED_STATUS,
                    RobotCapabilities.CARPET_MODE, RobotCapabilities.FW_FEATURES, RobotCapabilities.ROOM_MAPPING,
                    RobotCapabilities.MULTI_MAP_LIST, RobotCapabilities.CUSTOMIZE_CLEAN_MODE,
                    RobotCapabilities.COLLECT_DUST, RobotCapabilities.CLEAN_MOP_START, RobotCapabilities.CLEAN_MOP_STOP,
                    RobotCapabilities.MOP_DRYING, RobotCapabilities.MOP_DRYING_REMAINING_TIME,
                    RobotCapabilities.DOCK_STATE_ID, RobotCapabilities.CLEAN_PERCENT));

    public RoborockVacuumHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry,
            RoborockStateDescriptionOptionProvider stateDescriptionProvider) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
        this.stateDescriptionProvider = stateDescriptionProvider;
        initTask = new SchedulerTask(scheduler, logger, "Init", this::initDevice);
        pollTask = new SchedulerTask(scheduler, logger, "Poll", this::pollData);
        statusPollTask = new SchedulerTask(scheduler, logger, "StatusPoll", this::pollStatusOnly);
        secureRandom.nextBytes(nonce);
    }

    protected String getTokenFromBridge() {
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

    @Nullable
    protected String setRoutineViaBridge(String routine) {
        RoborockAccountHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            return "";
        }
        return localBridge.setRoutine(routine);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RoborockVacuumActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (RefreshType.REFRESH == command) {
                return;
            }
            if (channelUID.getId().equals(CHANNEL_RPC) || channelUID.getId().equals(CHANNEL_COMMAND)) {
                String[] commandArray = command.toFullString().split(",", 2);
                if (commandArray.length == 1) {
                    sendRPCCommand(commandArray[0]);
                } else {
                    sendRPCCommand(commandArray[0], commandArray[1]);
                }
                return;
            }
            if (channelUID.getId().equals(CHANNEL_VACUUM)) {
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        sendRPCCommand(COMMAND_APP_START);
                        return;
                    } else {
                        sendRPCCommand(COMMAND_APP_STOP);
                        return;
                    }
                }
            }
            if (channelUID.getId().equals(CHANNEL_CONTROL) && command instanceof StringType) {
                if ("vacuum".equals(command.toString())) {
                    sendRPCCommand(COMMAND_APP_START);
                } else if ("spot".equals(command.toString())) {
                    sendRPCCommand(COMMAND_APP_SPOT);
                } else if ("pause".equals(command.toString())) {
                    sendRPCCommand(COMMAND_APP_PAUSE);
                } else if ("dock".equals(command.toString())) {
                    sendRPCCommand(COMMAND_APP_CHARGE);
                } else {
                    logger.warn("Command {} not recognised", command.toString());
                }
                return;
            }

            if (channelUID.getId().equals(CHANNEL_ROUTINE)) {
                setRoutineViaBridge(command.toString());
                return;
            }

            if (channelUID.getId().equals(CHANNEL_FAN_POWER)) {
                sendRPCCommand(COMMAND_SET_MODE, "[" + command.toString() + "]");
                return;
            }

            if (channelUID.getId().equals(RobotCapabilities.WATERBOX_MODE.getChannel())) {
                sendRPCCommand(COMMAND_SET_WATERBOX_MODE, "[" + command.toString() + "]");
                return;
            }
            if (channelUID.getId().equals(RobotCapabilities.MOP_MODE.getChannel())) {
                sendRPCCommand(COMMAND_SET_MOP_MODE, "[" + command.toString() + "]");
                return;
            }
            if (channelUID.getId().equals(RobotCapabilities.SEGMENT_CLEAN.getChannel()) && !command.toString().isEmpty()
                    && !command.toString().contentEquals("-")) {
                sendRPCCommand(COMMAND_START_SEGMENT, "[" + command.toString() + "]");
                updateState(RobotCapabilities.SEGMENT_CLEAN.getChannel(), new StringType("-"));
                return;
            }
            if (channelUID.getId().equals(CHANNEL_FAN_CONTROL) && command instanceof DecimalType) {
                if (Integer.valueOf(command.toString()) > 0) {
                    sendRPCCommand(COMMAND_SET_MODE, "[" + command.toString() + "]");
                }
                return;
            }
            if (channelUID.getId().equals(CHANNEL_CONSUMABLE_RESET)) {
                sendRPCCommand(COMMAND_CONSUMABLES_RESET, "[" + command.toString() + "]");
                updateState(CHANNEL_CONSUMABLE_RESET, new StringType("none"));
            }

            if (channelUID.getId().equals(RobotCapabilities.COLLECT_DUST.getChannel())) {
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        sendRPCCommand(COMMAND_SET_COLLECT_DUST);
                        return;
                    } else {
                        sendRPCCommand(COMMAND_STOP_COLLECT_DUST);
                        return;
                    }
                }
            }

            if (channelUID.getId().equals(RobotCapabilities.CLEAN_MOP_START.getChannel())
                    && command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    sendRPCCommand(COMMAND_SET_CLEAN_MOP_START);
                    return;
                }
            }
            if (channelUID.getId().equals(RobotCapabilities.CLEAN_MOP_STOP.getChannel())
                    && command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    sendRPCCommand(COMMAND_SET_CLEAN_MOP_STOP);
                    return;
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.debug("UnsupportedEncodingException, {}", e.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(RoborockVacuumConfiguration.class);
        communicationMode = config.getCommunicationMode();
        lastCloudOnlyPollTimestamp = 0;
        lastMapPollTimestamp = 0;
        vacuumChannelOn = false;
        lastKnownStateId = null;
        cloudMapRefreshDisabledLogged = false;
        cloudMetadataRefreshDisabledLogged = false;
        mapUpdateDeduplicator.reset();

        if (!(getBridge() instanceof Bridge bridge
                && bridge.getHandler() instanceof RoborockAccountHandler accountHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/offline.conf-error.no-bridge");
            return;
        }
        bridgeHandler = accountHandler;
        cloudTransport = new CloudMqttTransport(accountHandler, config.duid, nonce);
        LocalDirectTransport localDirectTransport = new LocalDirectTransport();
        localDirectTransport.setMessageConsumer(this::handleMessage);
        directTransport = localDirectTransport;
        refreshTransportContext();
        hasChannelStructure = false;
        applyCloudOnlyCapabilityPolicies();

        initTask.setNamePrefix(getThing().getUID().getId());
        pollTask.setNamePrefix(getThing().getUID().getId());
        statusPollTask.setNamePrefix(getThing().getUID().getId());
        updateStatus(ThingStatus.UNKNOWN);
        initTask.schedule(5);
    }

    private synchronized void scheduleNextPoll(long initialDelaySeconds) {
        final long delayUntilNextPoll;
        if (initialDelaySeconds < 0) {
            long intervalSeconds = config.getRefreshIntervalSeconds();
            long secondsSinceLastPoll = (System.currentTimeMillis() - lastSuccessfulPollTimestamp) / 1000;
            long deltaRemaining = intervalSeconds - secondsSinceLastPoll;
            delayUntilNextPoll = Math.max(0, deltaRemaining);
        } else {
            delayUntilNextPoll = initialDelaySeconds;
        }
        logger.debug("{}: Scheduling next poll in {}s, refresh interval {}s", config.duid, delayUntilNextPoll,
                config.getRefreshIntervalSeconds());
        pollTask.cancel();
        pollTask.schedule(delayUntilNextPoll);
    }

    private synchronized void scheduleNextStatusPoll(long delaySeconds) {
        logger.debug("{}: Scheduling status-only poll in {}s, fast refresh interval {}s", config.duid, delaySeconds,
                config.getFastRefreshIntervalSeconds());
        statusPollTask.cancel();
        statusPollTask.schedule(delaySeconds);
    }

    private void initDevice() {
        RoborockAccountHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            if (token.isEmpty()) {
                token = getTokenFromBridge();
                endpointPrefix = localBridgeHandler.getEndpointPrefix();
                if (!token.isEmpty()) {
                    if (rrHomeId.isEmpty()) {
                        Home home = localBridgeHandler.getHomeDetail();
                        if (home != null) {
                            rrHomeId = Integer.toString(home.data.rrHomeId);
                        }
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "@text/offline.conf-error.no-token");
                    return;
                }
            }

            scheduleNextPoll(-1);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/offline.conf-error.no-token");
        }
    }

    private synchronized void teardown(boolean scheduleReconnection) {
        pollTask.cancel();
        statusPollTask.cancel();
        initTask.cancel();
        requestCorrelationTracker.cleanupExpired(0);

        if (scheduleReconnection) {
            initTask.schedule(30);
        }
    }

    @Override
    public void dispose() {
        RoborockCommandTransport localDirectTransport = directTransport;
        if (localDirectTransport != null) {
            localDirectTransport.dispose();
        }
        teardown(false);
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initTask.submit();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            teardown(false);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void updateDevice(Devices devices[]) {
        for (int i = 0; i < devices.length; i++) {
            if (config.duid.equals(devices[i].duid)) {
                if (localKey.isEmpty()) {
                    localKey = devices[i].localKey;
                    refreshTransportContext();
                }
                if (devices[i].online) {
                    sendAllMqttCommands();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error.vac-offline");
                }
            }
        }
    }

    private void pollData() {
        RoborockAccountHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            applyCloudOnlyCapabilityPolicies();
            boolean cloudOnlyRefreshDue = isCloudOnlyRefreshDue();
            HomeData homeData = localBridgeHandler.getHomeData();
            if (homeData != null && homeData.result != null) {
                homeRooms = homeData.result.rooms;
                Devices devices[] = homeData.result.devices;
                updateDevice(devices);
                Devices receivedDevices[] = homeData.result.receivedDevices;
                updateDevice(receivedDevices);
            }

            if (supportsRoutines && isCloudMetadataRefreshAllowed() && cloudOnlyRefreshDue) {
                String routinesResponse = localBridgeHandler.getRoutines(config.duid);
                List<StateOption> options = new ArrayList<>();
                JsonObject parsedRoutines = routinesResponse != null && !routinesResponse.isEmpty()
                        ? JsonParser.parseString(routinesResponse).getAsJsonObject()
                        : null;
                if (parsedRoutines != null && parsedRoutines.get("result").isJsonArray()
                        && parsedRoutines.get("result").getAsJsonArray().size() > 0
                        && parsedRoutines.get("result").getAsJsonArray().get(0).isJsonObject()) {
                    JsonArray routinesArray = parsedRoutines.get("result").getAsJsonArray();
                    Map<String, Object> routines = new HashMap<>();
                    for (int i = 0; i < routinesArray.size(); ++i) {
                        JsonObject routinesJsonObject = routinesArray.get(i).getAsJsonObject();
                        routines.put(routinesJsonObject.get("id").getAsString(),
                                routinesJsonObject.get("name").getAsString());
                        options.add(new StateOption(routinesJsonObject.get("id").getAsString(),
                                routinesJsonObject.get("name").getAsString()));
                    }
                    updateState(CHANNEL_ROUTINES, new StringType(gson.toJson(routines)));
                    stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_ROUTINE),
                            options);
                } else {
                    logger.debug("Routines not supported for device {}", config.duid);
                    supportsRoutines = false;
                }
            } else if (supportsRoutines && !isCloudMetadataRefreshAllowed()) {
                disableRoutinesState("cloudMetadataRefresh=off in direct communication mode");
            }

            if (cloudOnlyRefreshDue) {
                lastCloudOnlyPollTimestamp = System.currentTimeMillis();
            }

            lastSuccessfulPollTimestamp = System.currentTimeMillis();
            scheduleNextPoll(-1);

            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/offline.conf-error.no-bridge");
            return;
        }
    }

    private void pollStatusOnly() {
        @Nullable
        Integer nextStatusPollDelaySeconds = VacuumRefreshPolicy.getStatusPollDelaySeconds(communicationMode,
                vacuumChannelOn, config.getFastRefreshIntervalSeconds());
        if (nextStatusPollDelaySeconds == null) {
            statusPollTask.cancel();
            return;
        }
        requestImmediateStatusUpdate("vacuum channel is ON");
        try {
            requestMapRefreshIfDue(false, "vacuum channel is ON");
        } catch (UnsupportedEncodingException e) {
            logger.warn("Failed to request map update during status-only poll: {}", e.getMessage());
        }
        scheduleNextStatusPoll(nextStatusPollDelaySeconds.intValue());
    }

    private void sendAllMqttCommands() {
        try {
            requestCorrelationTracker.cleanupExpired(MAP_REQUEST_CORRELATION_TIMEOUT_MS);
            boolean cloudOnlyRefreshDue = isCloudOnlyRefreshDue();
            registerRequest("getStatus", sendRPCCommand(COMMAND_GET_STATUS));
            registerRequest("getConsumable", sendRPCCommand(COMMAND_GET_CONSUMABLE));
            registerRequest("getNetworkInfo", sendRPCCommand(COMMAND_GET_NETWORK_INFO));
            registerRequest("getCleanSummary", sendRPCCommand(COMMAND_GET_CLEAN_SUMMARY));
            registerRequest("getDndTimer", sendRPCCommand(COMMAND_GET_DND_TIMER));
            if (isCloudMetadataRefreshAllowed() && cloudOnlyRefreshDue) {
                registerRequest("getRoomMapping", sendRPCCommand(COMMAND_GET_ROOM_MAPPING));
            } else if (!isCloudMetadataRefreshAllowed()) {
                disableRoomMappingState("cloudMetadataRefresh=off in direct communication mode");
            }
            registerRequest("getSegmentStatus", sendRPCCommand(COMMAND_GET_SEGMENT_STATUS));
            registerRequest("getMapStatus", sendRPCCommand(COMMAND_GET_MAP_STATUS));
            registerRequest("getLedStatus", sendRPCCommand(COMMAND_GET_LED_STATUS));
            registerRequest("getCarpetMode", sendRPCCommand(COMMAND_GET_CARPET_MODE));
            registerRequest("getFwFeatures", sendRPCCommand(COMMAND_GET_FW_FEATURES));
            registerRequest("getMultiMapsList", sendRPCCommand(COMMAND_GET_MULTI_MAP_LIST));
            registerRequest("getCustomizeCleanMode", sendRPCCommand(COMMAND_GET_CUSTOMIZE_CLEAN_MODE));
            requestMapRefreshIfDue(cloudOnlyRefreshDue, "periodic poll cycle");
        } catch (UnsupportedEncodingException e) {
            logger.warn("Failed to send MQTT commands due to unsupported encoding: {}", e.getMessage());
        }
    }

    public void handleMessage(byte[] payload) {
        requestCorrelationTracker.cleanupExpired(MAP_REQUEST_CORRELATION_TIMEOUT_MS);
        try {
            int connectNonce = -1;
            int ackNonce = -1;
            @Nullable
            RoborockCommandTransport currentDirectTransport = directTransport;
            if (currentDirectTransport instanceof LocalDirectTransport localDirect) {
                connectNonce = localDirect.getConnectNonce();
                ackNonce = localDirect.getAckNonce();
            }

            ProtocolUtils.DecodedMessage decodedMessage = ProtocolUtils.decodeMessage(payload, localKey, nonce,
                    endpointPrefix, connectNonce, ackNonce);
            if (decodedMessage instanceof ProtocolUtils.IgnoredResponse) {
                return;
            }

            if (decodedMessage instanceof ProtocolUtils.MapPayloadResponse mapPayloadResponse) {
                handleGetMap(mapPayloadResponse.requestId(), mapPayloadResponse.payload());
                return;
            }

            String response = ((ProtocolUtils.JsonPayloadResponse) decodedMessage).payload();

            JsonElement parsedResponse = JsonParser.parseString(response);
            if (!parsedResponse.isJsonObject()) {
                return;
            }
            JsonObject responseJson = parsedResponse.getAsJsonObject();
            JsonElement rpcPayload = extractRpcPayload(responseJson);
            if (rpcPayload != null && rpcPayload.isJsonPrimitive()) {
                String jsonString = rpcPayload.getAsString();
                if (!jsonString.endsWith("\"result\":[\"ok\"]}") && !jsonString.endsWith("\"result\":[]}")
                        && JsonParser.parseString(jsonString).getAsJsonObject().has("id")
                        && JsonParser.parseString(jsonString).getAsJsonObject().has("result")) {
                    int messageId = JsonParser.parseString(jsonString).getAsJsonObject().get("id").getAsInt();
                    String methodName = requestCorrelationTracker.findMethodByRequestId(messageId);

                    if (methodName == null) {
                        logger.trace("Received response {} for unknown or already handled message ID: {}", response,
                                messageId);
                        return;
                    }

                    switch (methodName) {
                        case "getStatus":
                        case COMMAND_GET_STATUS:
                            handleGetStatus(jsonString);
                            break;
                        case "getConsumable":
                        case COMMAND_GET_CONSUMABLE:
                            handleGetConsumables(jsonString);
                            break;
                        case "getRoomMapping":
                        case COMMAND_GET_ROOM_MAPPING:
                            handleGetRoomMapping(jsonString);
                            break;
                        case "getNetworkInfo":
                        case COMMAND_GET_NETWORK_INFO:
                            handleGetNetworkInfo(jsonString);
                            break;
                        case "getCleanRecord":
                        case COMMAND_GET_CLEAN_RECORD:
                            handleGetCleanRecord(jsonString);
                            break;
                        case "getCleanSummary":
                        case COMMAND_GET_CLEAN_SUMMARY:
                            handleGetCleanSummary(jsonString);
                            break;
                        case "getDndTimer":
                        case COMMAND_GET_DND_TIMER:
                            handleGetDndTimer(jsonString);
                            break;
                        case "getSegmentStatus":
                        case COMMAND_GET_SEGMENT_STATUS:
                            handleGetSegmentStatus(jsonString);
                            break;
                        case "getMapStatus":
                        case COMMAND_GET_MAP_STATUS:
                            handleGetMapStatus(jsonString);
                            break;
                        case "getLedStatus":
                        case COMMAND_GET_LED_STATUS:
                            handleGetLedStatus(jsonString);
                            break;
                        case "getCarpetMode":
                        case COMMAND_GET_CARPET_MODE:
                            handleGetCarpetMode(jsonString);
                            break;
                        case "getFwFeatures":
                        case COMMAND_GET_FW_FEATURES:
                            handleGetFwFeatures(jsonString);
                            break;
                        case "getMultiMapsList":
                        case COMMAND_GET_MULTI_MAP_LIST:
                            handleGetMultiMapsList(jsonString);
                            break;
                        case "getCustomizeCleanMode":
                        case COMMAND_GET_CUSTOMIZE_CLEAN_MODE:
                            handleGetCustomizeCleanMode(jsonString);
                            break;
                        default:
                            logger.debug("No handler for method: {}", methodName);
                            break;
                    }
                    requestCorrelationTracker.removeByRequestId(messageId);
                }
            } else {
                // handle live updates ie any one (or more of values of dps)
                // "dps":{"121":8,"122":100,"123":104,"124":203,"125":75,"126":63,"127":50,"128":0,"133":1,"134":0}
                JsonElement dpsElement = responseJson.get("dps");
                if (dpsElement != null && dpsElement.isJsonObject()) {
                    JsonObject dpsJsonObject = dpsElement.getAsJsonObject();
                    if (dpsJsonObject.has("121")) {
                        int stateInt = dpsJsonObject.get("121").getAsInt();
                        updateStateIdAndRequestStatusIfChanged(stateInt, true);
                    } else if (dpsJsonObject.has("122")) {
                        int battery = dpsJsonObject.get("122").getAsInt();
                        updateState(CHANNEL_BATTERY, new DecimalType(battery));
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            // Occasionally get non-JSON returned from the Roborock MQTT server
        }
    }

    private void handleGetStatus(String response) {
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
                    new QuantityType<>(getStatus.result[0].cleanArea / 1000000D, SIUnits.SQUARE_METRE));
            updateState(CHANNEL_CLEAN_TIME,
                    new QuantityType<>(TimeUnit.SECONDS.toMinutes(getStatus.result[0].cleanTime), Units.MINUTE));
            updateState(CHANNEL_DND_ENABLED, new DecimalType(getStatus.result[0].dndEnabled));
            updateState(CHANNEL_ERROR_CODE,
                    new StringType(VacuumErrorType.getType(getStatus.result[0].errorCode).getDescription()));
            updateState(CHANNEL_ERROR_ID, new DecimalType(getStatus.result[0].errorCode));
            updateState(CHANNEL_IN_CLEANING, OnOffType.from(1 == getStatus.result[0].inCleaning));
            updateState(CHANNEL_MAP_PRESENT, OnOffType.from(1 == getStatus.result[0].mapPresent));
            updateStateIdAndRequestStatusIfChanged(getStatus.result[0].state, false);

            OnOffType vacuum = OnOffType.OFF;
            StatusType state = StatusType.getType(getStatus.result[0].state);
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
            updateVacuumChannelState(vacuum);

            if (this.deviceCapabilities.containsKey(RobotCapabilities.DOCK_STATE_ID)) {
                updateState(CHANNEL_DOCK_STATE_ID, new DecimalType(getStatus.result[0].dockErrorStatus));
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
            if (deviceCapabilities.containsKey(RobotCapabilities.CLEAN_PERCENT)) {
                updateState(CHANNEL_CLEAN_PERCENT, new DecimalType(getStatus.result[0].cleanPercent));
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
        if (getNetworkInfo != null && getNetworkInfo.result != null) {
            updateState(CHANNEL_SSID, new StringType(getNetworkInfo.result.ssid));
            updateState(CHANNEL_BSSID, new StringType(getNetworkInfo.result.bssid));
            updateState(CHANNEL_RSSI, new DecimalType(getNetworkInfo.result.rssi));
            if (localIP.isEmpty()) {
                localIP = getNetworkInfo.result.ip;
                refreshTransportContext();
            }
        }
    }

    private void handleGetCleanRecord(String response) {
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
                historyRecord.put("area", getCleanRecord.result[0].cleanedArea);
                updateState(CHANNEL_HISTORY_AREA,
                        new QuantityType<>(getCleanRecord.result[0].cleanedArea / 1000000D, SIUnits.SQUARE_METRE));
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
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray()) {
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
                        registerRequest("getCleanRecord",
                                sendRPCCommand(COMMAND_GET_CLEAN_RECORD, "[" + lastClean + "]"));
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
            if (cleanSummary.has("records") && cleanSummary.get("records").isJsonArray()) {
                JsonArray cleanSummaryRecords = cleanSummary.get("records").getAsJsonArray();
                if (!cleanSummaryRecords.isEmpty()) {
                    String lastClean = cleanSummaryRecords.get(0).getAsString();
                    if (!lastClean.equals(lastHistoryID)) {
                        lastHistoryID = lastClean;
                        try {
                            registerRequest("getCleanRecord",
                                    sendRPCCommand(COMMAND_GET_CLEAN_RECORD, "[" + lastClean + "]"));
                        } catch (UnsupportedEncodingException e) {
                            // Shouldn't occur
                        }
                    }
                }
            }
        }
    }

    private void handleGetDndTimer(String response) {
        GetDndTimer getDndTimer = gson.fromJson(response, GetDndTimer.class);
        if (getDndTimer != null) {
            updateState(CHANNEL_DND_FUNCTION, new DecimalType(getDndTimer.result[0].enabled));
            updateState(CHANNEL_DND_START, new StringType(
                    String.format("%02d:%02d", getDndTimer.result[0].startHour, getDndTimer.result[0].startMinute)));
            updateState(CHANNEL_DND_END, new StringType(
                    String.format("%02d:%02d", getDndTimer.result[0].endHour, getDndTimer.result[0].endMinute)));
        }
    }

    private void handleGetSegmentStatus(String response) {
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray() && JsonParser
                .parseString(response).getAsJsonObject().get("result").getAsJsonArray().get(0).isJsonPrimitive()) {
            JsonArray getSegmentStatus = JsonParser.parseString(response).getAsJsonObject().get("result")
                    .getAsJsonArray();
            String stat = getSegmentStatus.get(0).getAsString();
            if (!"OK".equals(stat)) {
                updateState(RobotCapabilities.SEGMENT_STATUS.getChannel(), OnOffType.from(1 == Integer.parseInt(stat)));
            }
        }
    }

    private void handleGetMapStatus(String response) {
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray() && JsonParser
                .parseString(response).getAsJsonObject().get("result").getAsJsonArray().get(0).isJsonPrimitive()) {
            JsonArray getSegmentStatus = JsonParser.parseString(response).getAsJsonObject().get("result")
                    .getAsJsonArray();
            try {
                String stat = getSegmentStatus.get(0).getAsString();
                if (!"OK".equals(stat)) {
                    updateState(RobotCapabilities.MAP_STATUS.getChannel(), OnOffType.from(1 == Integer.parseInt(stat)));
                }
            } catch (ClassCastException | IllegalStateException e) {
                logger.debug("Could not update numeric channel {} with '{}': {}",
                        RobotCapabilities.MAP_STATUS.getChannel(), response, e.getMessage());
            }
        }
    }

    private void handleGetLedStatus(String response) {
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray() && JsonParser
                .parseString(response).getAsJsonObject().get("result").getAsJsonArray().get(0).isJsonPrimitive()) {
            JsonArray getSegmentStatus = JsonParser.parseString(response).getAsJsonObject().get("result")
                    .getAsJsonArray();
            try {
                String stat = getSegmentStatus.get(0).getAsString();
                if (!"OK".equals(stat)) {
                    updateState(RobotCapabilities.LED_STATUS.getChannel(), OnOffType.from(1 == Integer.parseInt(stat)));
                }
            } catch (ClassCastException | IllegalStateException e) {
                logger.debug("Could not update numeric channel {} with '{}': {}",
                        RobotCapabilities.LED_STATUS.getChannel(), response, e.getMessage());
            }
        }
    }

    private void handleGetCarpetMode(String response) {
        try {
            JsonArray getCarpetMode = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
            updateState(RobotCapabilities.CARPET_MODE.getChannel(),
                    new StringType(getCarpetMode.get(0).getAsJsonObject().toString()));
        } catch (ClassCastException | IllegalStateException e) {
            logger.debug("Could not update numeric channel {} with '{}': {}",
                    RobotCapabilities.CARPET_MODE.getChannel(), response, e.getMessage());
        }
    }

    private void handleGetFwFeatures(String response) {
        JsonArray getFwFeatures = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
        Map<String, String> properties = editProperties();
        properties.put("fw-features", getFwFeatures.toString());
        updateProperties(properties);
    }

    private void handleGetMultiMapsList(String response) {
        JsonArray getMultiMapsList = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
        updateState(RobotCapabilities.MULTI_MAP_LIST.getChannel(),
                new StringType(getMultiMapsList.get(0).getAsJsonObject().toString()));
    }

    private void handleGetCustomizeCleanMode(String response) {
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray()) {
            JsonArray getCustomizeCleanMode = JsonParser.parseString(response).getAsJsonObject().get("result")
                    .getAsJsonArray();
            updateState(RobotCapabilities.CUSTOMIZE_CLEAN_MODE.getChannel(),
                    new StringType(getCustomizeCleanMode.toString()));
        }
    }

    private void handleGetMap(int requestId, byte[] mapPayload) {
        String methodName = requestCorrelationTracker.findMethodByRequestId(requestId);
        CompletableFuture<byte[]> pendingDownload = pendingRrMapDownloads.remove(Integer.valueOf(requestId));
        if (pendingDownload != null) {
            pendingDownload.complete(mapPayload);
        }
        if (!"getMap".equals(methodName) && !COMMAND_GET_MAP.equals(methodName)
                && !MAP_DOWNLOAD_REQUEST_METHOD.equals(methodName) && pendingDownload == null) {
            logger.debug("Ignoring map response for request id {} with unknown method mapping", requestId);
            return;
        }

        try {
            RRMapData mapData = rrMapParser.parse(mapPayload);
            byte[] pngBytes = rrMapRenderer.renderAsPng(mapData);
            if (mapUpdateDeduplicator.shouldPublish(pngBytes)) {
                updateState(CHANNEL_VACUUM_MAP, new RawType(pngBytes, "image/png"));
            } else {
                logger.trace("Suppressing duplicate map image update for request id {}", requestId);
            }
        } catch (RoborockException e) {
            logger.debug("Failed to parse map payload for request id {}: {}", requestId, e.getMessage());
        } finally {
            requestCorrelationTracker.removeByRequestId(requestId);
        }
    }

    public @Nullable String downloadRrMap(@Nullable String requestedDirectory) {
        int requestId = 0;
        try {
            requestCorrelationTracker.cleanupExpired(MAP_REQUEST_CORRELATION_TIMEOUT_MS);
            RoborockCommunicationMode selectedMode = RoborockTransportRouting.selectTransportMode(communicationMode,
                    COMMAND_GET_MAP);
            RoborockCommandTransport selectedTransport = selectedMode == RoborockCommunicationMode.DIRECT
                    ? directTransport
                    : cloudTransport;
            if (selectedTransport == null) {
                logger.debug("Cannot download RR map because transport {} is not available.", selectedMode);
                return null;
            }

            do {
                requestId = secureRandom.nextInt(22767 + 1) + 10000;
            } while (requestCorrelationTracker.isRequestIdInUse(requestId));

            CompletableFuture<byte[]> rrMapFuture = new CompletableFuture<>();
            pendingRrMapDownloads.put(Integer.valueOf(requestId), rrMapFuture);
            registerRequest(MAP_DOWNLOAD_REQUEST_METHOD, requestId);
            int sentRequestId = selectedTransport.sendCommand(COMMAND_GET_MAP, "[]", requestId);
            if (sentRequestId <= 0) {
                logger.debug("Cannot download RR map because request id {} could not be sent.", sentRequestId);
                return null;
            }

            byte[] mapPayload = rrMapFuture.get(MAP_DOWNLOAD_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            byte[] gzipPayload = gzip(mapPayload);
            String fileName = "roborock-" + getThing().getUID().getId() + "-" + requestId + ".rrmap";
            Path mapFile = storeRrMapFile(gzipPayload, fileName, requestedDirectory);
            return mapFile.toAbsolutePath().toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while waiting for RR map download: {}", e.getMessage());
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("Timed out while waiting for RR map payload: {}", e.getMessage());
        } catch (UnsupportedEncodingException e) {
            logger.debug("Failed to request RR map payload: {}", e.getMessage());
        } catch (IOException e) {
            logger.debug("Failed to store RR map payload: {}", e.getMessage());
        } finally {
            if (requestId > 0) {
                pendingRrMapDownloads.remove(Integer.valueOf(requestId));
                requestCorrelationTracker.removeByRequestId(requestId);
            }
        }
        return null;
    }

    static Path getDefaultRrMapDownloadDirectory() {
        String userHome = System.getProperty("user.home", ".");
        return Paths.get(userHome);
    }

    static Path resolveRrMapDownloadDirectory(@Nullable String requestedDirectory, Path defaultDirectory) {
        if (requestedDirectory == null || requestedDirectory.isBlank()) {
            return defaultDirectory;
        }
        if (requestedDirectory.indexOf('\u0000') >= 0) {
            return defaultDirectory;
        }
        try {
            Path candidate = Paths.get(requestedDirectory.trim()).normalize();
            if (Files.exists(candidate) && !Files.isDirectory(candidate)) {
                return defaultDirectory;
            }
            return candidate;
        } catch (InvalidPathException | SecurityException e) {
            return defaultDirectory;
        }
    }

    private Path storeRrMapFile(byte[] gzipPayload, String fileName, @Nullable String requestedDirectory)
            throws IOException {
        Path defaultDirectory = getDefaultRrMapDownloadDirectory();
        Path targetDirectory = resolveRrMapDownloadDirectory(requestedDirectory, defaultDirectory);
        if (requestedDirectory != null && !requestedDirectory.isBlank() && targetDirectory.equals(defaultDirectory)
                && !isRequestedDefaultDirectory(requestedDirectory, defaultDirectory)) {
            logger.warn("Invalid or unusable RR map download directory '{}'. Falling back to '{}'.", requestedDirectory,
                    defaultDirectory.toAbsolutePath());
        }

        try {
            return writeRrMapFile(gzipPayload, targetDirectory, fileName);
        } catch (IOException e) {
            if (requestedDirectory != null && !requestedDirectory.isBlank()
                    && !targetDirectory.equals(defaultDirectory)) {
                logger.warn("Failed to store RR map in '{}': {}. Falling back to '{}'.", targetDirectory,
                        e.getMessage(), defaultDirectory.toAbsolutePath());
                return writeRrMapFile(gzipPayload, defaultDirectory, fileName);
            }
            throw e;
        }
    }

    private static boolean isRequestedDefaultDirectory(String requestedDirectory, Path defaultDirectory) {
        try {
            return Paths.get(requestedDirectory.trim()).equals(defaultDirectory);
        } catch (InvalidPathException | SecurityException e) {
            return false;
        }
    }

    private static Path writeRrMapFile(byte[] gzipPayload, Path directory, String fileName) throws IOException {
        Files.createDirectories(directory);
        Path mapFile = directory.resolve(fileName);
        Files.write(mapFile, gzipPayload, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        return mapFile;
    }

    private static byte[] gzip(byte[] input) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(output)) {
            gzipOutputStream.write(input);
            gzipOutputStream.finish();
            return output.toByteArray();
        }
    }

    private void registerRequest(String methodName, int requestId) {
        if (requestId == REQUEST_ID_SYNC_DIRECT_COMPLETED) {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Skipping request registration for method '{}' because direct response handling already completed synchronously.",
                        methodName);
            }
            return;
        }
        if (requestId == -1) {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Skipping request registration for method '{}' because transport returned timeout sentinel request id -1.",
                        methodName);
            }
            if ("getMap".equals(methodName)) {
                setMapStateUndefinedAndResetDeduplicator();
            }
            return;
        }
        if (requestId <= 0) {
            logger.debug("Skipping request registration for method '{}' due to invalid request id {}", methodName,
                    requestId);
            if ("getMap".equals(methodName)) {
                setMapStateUndefinedAndResetDeduplicator();
            }
            return;
        }
        if (requestCorrelationTracker.isRequestIdInUse(requestId)) {
            return;
        }
        requestCorrelationTracker.register(methodName, requestId);
        if ("getMap".equals(methodName)) {
            logger.debug("Registered getMap request correlation id {}", requestId);
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

    private int sendRPCCommand(String method) throws UnsupportedEncodingException {
        return sendRPCCommand(method, "[]");
    }

    private int sendRPCCommand(String method, String params) throws UnsupportedEncodingException {
        if (bridgeHandler == null) {
            return 0;
        }
        try {
            requestCorrelationTracker.cleanupExpired(MAP_REQUEST_CORRELATION_TIMEOUT_MS);
            @Nullable
            String methodName = null;
            int id;
            do {
                id = secureRandom.nextInt(22767 + 1) + 10000;
                methodName = requestCorrelationTracker.isRequestIdInUse(id) ? "inUse" : null;
            } while (methodName != null);
            RoborockCommunicationMode selectedMode = RoborockTransportRouting.selectTransportMode(communicationMode,
                    method);
            RoborockCommandTransport selectedTransport = selectedMode == RoborockCommunicationMode.DIRECT
                    ? directTransport
                    : cloudTransport;
            if (selectedTransport == null) {
                logger.debug("No available transport for mode {} and method {}", selectedMode, method);
                return -1;
            }
            if (communicationMode == RoborockCommunicationMode.DIRECT && selectedMode == RoborockCommunicationMode.CLOUD
                    && RoborockTransportRouting.isCloudOnlyMethod(method)) {
                logger.debug(
                        "Direct communication mode selected for {}, but method '{}' remains cloud-only and is routed via cloud transport.",
                        config.duid, method);
            }
            // Pre-register for direct mode responses (sync handling) or cloud-routed map requests
            // to avoid race condition where response arrives before correlation is registered
            boolean preRegisterForDirect = selectedMode == RoborockCommunicationMode.DIRECT
                    && shouldPreRegisterForDirectResponseHandling(method);
            boolean preRegisterForCloudMap = selectedMode == RoborockCommunicationMode.CLOUD
                    && COMMAND_GET_MAP.equals(method);
            boolean preRegisterRequest = preRegisterForDirect || preRegisterForCloudMap;
            if (preRegisterRequest) {
                requestCorrelationTracker.register(method, id);
                if (preRegisterForCloudMap) {
                    logger.debug("Pre-registered cloud map correlation for request id {}", id);
                }
            }
            int requestId = selectedTransport.sendCommand(method, params, id);
            if (requestId <= 0 && preRegisterRequest) {
                requestCorrelationTracker.removeByRequestId(id);
                return requestId;
            }
            if (preRegisterRequest && !requestCorrelationTracker.isRequestIdInUse(id)) {
                return REQUEST_ID_SYNC_DIRECT_COMPLETED;
            }
            return requestId;
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            return 0;
        }
    }

    private @Nullable JsonElement extractRpcPayload(JsonObject responseJson) {
        JsonElement dpsElement = responseJson.get("dps");
        if (dpsElement == null || !dpsElement.isJsonObject()) {
            return null;
        }
        JsonObject dpsJson = dpsElement.getAsJsonObject();
        if (dpsJson.has("102")) {
            return dpsJson.get("102");
        }
        if (dpsJson.has("101")) {
            return dpsJson.get("101");
        }
        return null;
    }

    private boolean shouldPreRegisterForDirectResponseHandling(String method) {
        return switch (method) {
            case COMMAND_GET_STATUS, COMMAND_GET_CONSUMABLE, COMMAND_GET_NETWORK_INFO, COMMAND_GET_CLEAN_SUMMARY,
                    COMMAND_GET_DND_TIMER, COMMAND_GET_ROOM_MAPPING, COMMAND_GET_SEGMENT_STATUS, COMMAND_GET_MAP_STATUS,
                    COMMAND_GET_LED_STATUS, COMMAND_GET_CARPET_MODE, COMMAND_GET_FW_FEATURES,
                    COMMAND_GET_MULTI_MAP_LIST, COMMAND_GET_CUSTOMIZE_CLEAN_MODE, COMMAND_GET_CLEAN_RECORD,
                    COMMAND_GET_MAP ->
                true;
            default -> false;
        };
    }

    private void updateStateIdAndRequestStatusIfChanged(int stateId, boolean triggerImmediateStatusQuery) {
        @Nullable
        Integer previousStateId = lastKnownStateId;
        updateState(CHANNEL_STATE_ID, new DecimalType(stateId));
        lastKnownStateId = stateId;
        if (triggerImmediateStatusQuery && VacuumRefreshPolicy.shouldRequestImmediateStatus(previousStateId, stateId)) {
            requestImmediateStatusUpdate("state-id changed from " + previousStateId + " to " + stateId);
        }
    }

    private void requestImmediateStatusUpdate(String reason) {
        try {
            logger.debug("{}: Triggering immediate '{}' request because {}", config.duid, COMMAND_GET_STATUS, reason);
            registerRequest("getStatus", sendRPCCommand(COMMAND_GET_STATUS));
        } catch (UnsupportedEncodingException e) {
            logger.debug("UnsupportedEncodingException while requesting immediate status, {}", e.getMessage());
        }
    }

    private void updateVacuumChannelState(OnOffType vacuumState) {
        boolean wasVacuumOn = vacuumChannelOn;
        vacuumChannelOn = OnOffType.ON.equals(vacuumState);
        updateState(CHANNEL_VACUUM, vacuumState);
        if (vacuumChannelOn != wasVacuumOn) {
            @Nullable
            Integer nextStatusPollDelaySeconds = VacuumRefreshPolicy.getStatusPollDelaySeconds(communicationMode,
                    vacuumChannelOn, config.getFastRefreshIntervalSeconds());
            if (nextStatusPollDelaySeconds != null) {
                scheduleNextStatusPoll(nextStatusPollDelaySeconds.intValue());
            } else {
                statusPollTask.cancel();
            }
        }
    }

    private boolean isCloudMapRefreshAllowed() {
        return CloudRefreshPolicy.isCloudMapRefreshAllowed(communicationMode, config);
    }

    private boolean isCloudMetadataRefreshAllowed() {
        return CloudRefreshPolicy.isCloudMetadataRefreshAllowed(communicationMode, config);
    }

    private boolean isCloudOnlyRefreshDue() {
        return CloudRefreshPolicy.isCloudOnlyRefreshDue(System.currentTimeMillis(), lastCloudOnlyPollTimestamp,
                config.getCloudRefreshIntervalSeconds());
    }

    private int getMapRefreshDuringCleaningIntervalSeconds() {
        return config.getMapRefreshDuringCleaningIntervalSeconds(communicationMode);
    }

    private boolean isCleaningMapRefreshDue(long now) {
        return VacuumRefreshPolicy.isRefreshDue(now, lastMapPollTimestamp,
                getMapRefreshDuringCleaningIntervalSeconds());
    }

    private void requestMapRefreshIfDue(boolean cloudOnlyRefreshDue, String reason)
            throws UnsupportedEncodingException {
        if (!isCloudMapRefreshAllowed()) {
            disableMapState("cloudMapRefresh=off in direct communication mode");
            return;
        }

        long now = System.currentTimeMillis();
        boolean mapRefreshDue = vacuumChannelOn ? isCleaningMapRefreshDue(now) : cloudOnlyRefreshDue;
        if (!mapRefreshDue) {
            return;
        }

        registerRequest("getMap", sendRPCCommand(COMMAND_GET_MAP));
        lastMapPollTimestamp = now;
        logger.debug("{}: Requested map refresh because {} (vacuumOn={}, interval={}s)", config.duid, reason,
                vacuumChannelOn, getMapRefreshDuringCleaningIntervalSeconds());
    }

    private void applyCloudOnlyCapabilityPolicies() {
        if (!isCloudMapRefreshAllowed()) {
            disableMapState("cloudMapRefresh=off in direct communication mode");
        }
        if (!isCloudMetadataRefreshAllowed()) {
            disableRoutinesState("cloudMetadataRefresh=off in direct communication mode");
            disableRoomMappingState("cloudMetadataRefresh=off in direct communication mode");
        }
    }

    private void disableMapState(String reason) {
        if (thing.getChannel(CHANNEL_VACUUM_MAP) != null) {
            setMapStateUndefinedAndResetDeduplicator();
        }
        if (!cloudMapRefreshDisabledLogged) {
            logger.info("Cloud map refresh disabled for {}: {}. Channel '{}' is set to UNDEF.", config.duid, reason,
                    CHANNEL_VACUUM_MAP);
            cloudMapRefreshDisabledLogged = true;
        }
    }

    private void disableRoutinesState(String reason) {
        updateChannelStateIfExists(CHANNEL_ROUTINES, UnDefType.UNDEF);
        if (!cloudMetadataRefreshDisabledLogged) {
            logger.info(
                    "Cloud metadata refresh disabled for {}: {}. Channels '{}' and '{}' are set to UNDEF when available.",
                    config.duid, reason, CHANNEL_ROUTINES, RobotCapabilities.ROOM_MAPPING.getChannel());
            cloudMetadataRefreshDisabledLogged = true;
        }
    }

    private void disableRoomMappingState(String reason) {
        updateChannelStateIfExists(RobotCapabilities.ROOM_MAPPING.getChannel(), UnDefType.UNDEF);
        if (!cloudMetadataRefreshDisabledLogged) {
            logger.info(
                    "Cloud metadata refresh disabled for {}: {}. Channels '{}' and '{}' are set to UNDEF when available.",
                    config.duid, reason, CHANNEL_ROUTINES, RobotCapabilities.ROOM_MAPPING.getChannel());
            cloudMetadataRefreshDisabledLogged = true;
        }
    }

    private void updateChannelStateIfExists(String channelId, State state) {
        if (thing.getChannel(channelId) != null) {
            updateState(channelId, state);
        }
    }

    private void setMapStateUndefinedAndResetDeduplicator() {
        updateState(CHANNEL_VACUUM_MAP, UnDefType.UNDEF);
        mapUpdateDeduplicator.reset();
    }

    private void refreshTransportContext() {
        @Nullable
        String configuredLocalHost = config.getLocalHostOrNull();
        String effectiveLocalHost = configuredLocalHost != null ? configuredLocalHost : localIP;
        RoborockCommandTransport localCloudTransport = cloudTransport;
        if (localCloudTransport != null) {
            localCloudTransport.updateContext(localKey, effectiveLocalHost, config.localPort, endpointPrefix);
        }
        RoborockCommandTransport localDirectTransport = directTransport;
        if (localDirectTransport != null) {
            localDirectTransport.updateContext(localKey, effectiveLocalHost, config.localPort, endpointPrefix);
        }
    }
}
