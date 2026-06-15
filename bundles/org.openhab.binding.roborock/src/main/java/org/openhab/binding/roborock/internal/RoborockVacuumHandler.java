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
import org.openhab.core.library.CoreItemFactory;
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
import org.openhab.core.thing.type.ChannelTypeUID;
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
    private boolean b01 = false;
    private boolean q7 = false;
    private boolean q10 = false;
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
            if (channelUID.getId().equals(CHANNEL_DP_COMMAND)) {
                String jsonPayload = command.toString();
                try {
                    JsonElement parsed = JsonParser.parseString(jsonPayload);
                    if (parsed.isJsonObject()) {
                        Map<String, Object> dpsMap = new HashMap<>();
                        for (Map.Entry<String, JsonElement> e : parsed.getAsJsonObject().entrySet()) {
                            dpsMap.put(e.getKey(), e.getValue());
                        }
                        if (!dpsMap.isEmpty()) {
                            sendQ10DpCommand(dpsMap);
                            logger.debug("Successfully forwarded DP command map to device: {}", jsonPayload);
                        } else {
                            logger.warn("Received empty or invalid DP payload.");
                        }
                        logger.warn("Received non-object DP payload (expected JSON object): {}", jsonPayload);
                    }
                } catch (JsonSyntaxException e) {
                    logger.warn("Failed to parse incoming DP payload. {}", e.getMessage());
                }
                return;
            }
            if (channelUID.getId().equals(CHANNEL_VACUUM)) {
                if (command instanceof OnOffType) {
                    if (q10) {
                        if (command.equals(OnOffType.ON)) {
                            sendQ10DpCommand(Map.of("201", Map.of("cmd", 1)));
                        } else {
                            sendQ10DpCommand(Map.of("206", 0));
                        }
                    } else {
                        if (command.equals(OnOffType.ON)) {
                            sendRPCCommand(COMMAND_APP_START);
                            return;
                        } else {
                            sendRPCCommand(COMMAND_APP_STOP);
                            return;
                        }
                    }
                    return;
                }
            }
            if (channelUID.getId().equals(CHANNEL_CONTROL) && command instanceof StringType) {
                if (q10) {
                    switch (command.toString()) {
                        case "vacuum" -> sendQ10DpCommand(Map.of("201", Map.of("cmd", 1)));
                        // Q10 has no separate spot mode observed in captures — treat as full clean
                        case "spot" -> sendQ10DpCommand(Map.of("201", Map.of("cmd", 1)));
                        case "pause" -> sendQ10DpCommand(Map.of("204", 0));
                        case "dock" -> sendQ10DpCommand(Map.of("202", 5));
                        default -> logger.warn("Q10 command {} not recognised", command.toString());
                    }
                } else {
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
                }
                return;
            }

            if (channelUID.getId().equals(CHANNEL_ROUTINE)) {
                setRoutineViaBridge(command.toString());
                return;
            }

            if (channelUID.getId().equals(CHANNEL_FAN_POWER)) {
                if (q10) {
                    sendQ10DpCommand(Map.of("123", Integer.parseInt(command.toString())));
                } else {
                    sendRPCCommand(COMMAND_SET_MODE, "[" + command.toString() + "]");
                }
                return;
            }

            if (channelUID.getId().equals(RobotCapabilities.WATERBOX_MODE.getChannel())) {
                if (q10) {
                    sendQ10DpCommand(Map.of("124", Integer.parseInt(command.toString())));
                } else {
                    sendRPCCommand(COMMAND_SET_WATERBOX_MODE, "[" + command.toString() + "]");
                }
                return;
            }
            if (channelUID.getId().equals(RobotCapabilities.MOP_MODE.getChannel())) {
                sendRPCCommand(COMMAND_SET_MOP_MODE, "[" + command.toString() + "]");
                return;
            }
            if (channelUID.getId().equals(RobotCapabilities.SEGMENT_CLEAN.getChannel()) && !command.toString().isEmpty()
                    && !command.toString().contentEquals("-")) {
                if (q10) {
                    try {
                        // Room IDs arrive as a comma-separated string or single integer
                        List<Integer> roomIds = java.util.Arrays.stream(command.toString().split(",")).map(String::trim)
                                .filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();
                        // Note: "clean_paramters" is a typo in the Q10 protocol — do not correct it
                        sendQ10DpCommand(Map.of("201", Map.of("cmd", 2, "clean_paramters", roomIds)));
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid Q10 room id list '{}': {}", command, e.getMessage());
                    }
                } else {
                    sendRPCCommand(COMMAND_START_SEGMENT, "[" + command.toString() + "]");
                }
                updateState(RobotCapabilities.SEGMENT_CLEAN.getChannel(), new StringType("-"));
                return;
            }
            if (channelUID.getId().equals(CHANNEL_FAN_CONTROL) && command instanceof DecimalType) {
                if (Integer.parseInt(command.toString()) > 0) {
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
        b01 = isB01Device();
        cloudTransport = new CloudMqttTransport(accountHandler, config.duid, nonce, b01, q7, q10);
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

    private boolean isB01Device() {
        q7 = false;
        q10 = false;
        String protocol = getThing().getProperties().getOrDefault(THING_PROPERTY_PROTOCOL, "");
        if (!"B01".equalsIgnoreCase(protocol)) {
            return false;
        }
        b01 = true;
        String name = getThing().getProperties().getOrDefault(THING_PROPERTY_DEVICE_NAME, "");
        String nameUpper = name.toUpperCase(java.util.Locale.ROOT);
        if (nameUpper.contains("Q7")) {
            q7 = true;
        } else if (nameUpper.contains("Q10")) {
            q10 = true;
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), CHANNEL_DP_COMMAND);
            Channel channel = thing.getChannel(channelUID);
            if (channel == null) {
                logger.debug("Adding channel for DP commands, on device {}", getThing().getUID());
                ThingBuilder thingBuilder = editThing();
                channel = ChannelBuilder.create(channelUID, CoreItemFactory.STRING).withLabel("Execute DP Command")
                        .withType(new ChannelTypeUID(BINDING_ID, "DPCommand")).build();
                thingBuilder.withChannel(channel);
                updateThing(thingBuilder.build());
            }
        }
        return true;
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
                    if (q10) {
                        sendQ10DpCommand(Map.of("102", Map.of("cmd", 1)));
                    }

                }
                if (devices[i].online) {
                    if (!q10) {
                        sendAllMqttCommands();
                    }
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
            registerRequest("getCleanSummary", sendRPCCommand(COMMAND_GET_CLEAN_SUMMARY));
            if (!b01) {
                registerRequest("getDndTimer", sendRPCCommand(COMMAND_GET_DND_TIMER));
                registerRequest("getNetworkInfo", sendRPCCommand(COMMAND_GET_NETWORK_INFO));
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
            }
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

                // B01 responses have a different envelope shape — normalise to V1 before dispatching
                if (b01) {
                    String normalised = normaliseB01Response(jsonString);
                    if (normalised != null) {
                        dispatchNormalisedResponse(normalised);
                    }
                } else {
                    dispatchNormalisedResponse(jsonString);
                    return;
                }
            }

            // Flat DP push handler — runs for all B01 devices on every frame.
            // Q10 frames can carry both an RPC result (dps.10001) and flat shadow DPs
            // simultaneously, so this block runs after the RPC block rather than as an else branch.
            JsonElement dpsElement = responseJson.get("dps");
            if (dpsElement != null && dpsElement.isJsonObject()) {
                handleFlatDpPush(dpsElement.getAsJsonObject());
            }
        } catch (JsonSyntaxException e) {
            logger.trace("Ignored non-JSON or malformed message from device: {}", e.getMessage());
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

            StatusType state = StatusType.getType(getStatus.result[0].state);

            OnOffType vacuum = switch (state) {
                case ZONE, ROOM, CLEANING, RETURNING, SPOTCLEAN -> OnOffType.ON;
                default -> OnOffType.OFF;
            };

            String control = switch (state) {
                case ZONE, ROOM, CLEANING, RETURNING -> "vacuum";
                case CHARGING, CHARGING_ERROR, DOCKING, FULL -> "dock";
                case SLEEPING, PAUSED, IDLE -> "pause";
                case SPOTCLEAN -> "spot";
                default -> "undef";
            };

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
        if (response != null && !response.isEmpty()) {
            String safeResponse = response;
            JsonObject responseObj = JsonParser.parseString(safeResponse).getAsJsonObject();
            if (responseObj.get("result").isJsonArray()
                    && responseObj.getAsJsonObject().get("result").getAsJsonArray().size() > 0
                    && responseObj.get("result").getAsJsonArray().get(0).isJsonArray()) {
                JsonArray historyData = responseObj.get("result").getAsJsonArray().get(0).getAsJsonArray();
                Map<String, Object> historyRecord = new HashMap<>();
                for (int i = 0; i < historyData.size(); ++i) {
                    try {
                        BigInteger value = historyData.get(i).getAsBigInteger();
                        switch (i) {
                            case 0 -> {
                                DateTimeType begin = new DateTimeType(Instant.ofEpochSecond(value.longValue()));
                                historyRecord.put("begin", begin.format(null));
                                updateState(CHANNEL_HISTORY_START_TIME, begin);
                            }
                            case 1 -> {
                                DateTimeType end = new DateTimeType(Instant.ofEpochSecond(value.longValue()));
                                historyRecord.put("end", end.format(null));
                                updateState(CHANNEL_HISTORY_END_TIME, end);
                            }
                            case 2 -> {
                                long duration = TimeUnit.SECONDS.toMinutes(value.intValue());
                                historyRecord.put("duration", duration);
                                updateState(CHANNEL_HISTORY_DURATION, new QuantityType<>(duration, Units.MINUTE));
                            }
                            case 3 -> {
                                historyRecord.put("area", value);
                                updateState(CHANNEL_HISTORY_AREA, new QuantityType<>(value, SIUnits.SQUARE_METRE));
                            }
                            case 4 -> {
                                historyRecord.put("error", value.intValue());
                                updateState(CHANNEL_HISTORY_ERROR, new DecimalType(value.intValue()));
                            }
                            case 5 -> {
                                historyRecord.put("complete", value.intValue());
                                updateState(CHANNEL_HISTORY_FINISH, new DecimalType(value.intValue()));
                            }
                            case 6 -> historyRecord.put("startType", value.intValue());
                            case 7 -> historyRecord.put("cleanType", value.intValue());
                            case 8 -> {
                                historyRecord.put("finishReason", value.intValue());
                                updateState(CHANNEL_HISTORY_FINISHREASON, new DecimalType(value.intValue()));
                            }
                        }
                    } catch (ClassCastException | NumberFormatException | IllegalStateException e) {
                        logger.trace("Failed to parse clean record entry: {}", e.getMessage());
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
    }

    private void handleGetCleanSummary(String response) {
        if (response != null && !response.isEmpty()) {
            String safeResponse = response;
            JsonObject responseObj = JsonParser.parseString(safeResponse).getAsJsonObject();
            if (responseObj.get("result").isJsonArray()) {
                JsonArray historyData = responseObj.get("result").getAsJsonArray();
                updateState(CHANNEL_HISTORY_TOTALTIME,
                        new QuantityType<>(TimeUnit.SECONDS.toMinutes(historyData.get(0).getAsLong()), Units.MINUTE));
                updateState(CHANNEL_HISTORY_TOTALAREA,
                        new QuantityType<>(historyData.get(1).getAsDouble() / 1000000D, SIUnits.SQUARE_METRE));
                updateState(CHANNEL_HISTORY_COUNT, new DecimalType(historyData.get(2).toString()));
                if (!b01 && historyData.get(3).getAsJsonArray().size() > 0) {
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
                JsonObject cleanSummary = responseObj.get("result").getAsJsonObject();
                updateState(CHANNEL_HISTORY_TOTALTIME, new QuantityType<>(
                        TimeUnit.SECONDS.toMinutes(cleanSummary.get("clean_time").getAsLong()), Units.MINUTE));
                updateState(CHANNEL_HISTORY_TOTALAREA, new QuantityType<>(
                        cleanSummary.get("clean_area").getAsDouble() / 1000000D, SIUnits.SQUARE_METRE));
                updateState(CHANNEL_HISTORY_COUNT, new DecimalType(cleanSummary.get("clean_count").getAsLong()));
                if (!b01 && cleanSummary.has("records") && cleanSummary.get("records").isJsonArray()) {
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
    }

    private void handleGetDndTimer(String response) {
        GetDndTimer getDndTimer = gson.fromJson(response, GetDndTimer.class);
        if (getDndTimer != null) {
            updateState(CHANNEL_DND_FUNCTION, new DecimalType(getDndTimer.result[0].enabled));
            updateState(CHANNEL_DND_START, new StringType(
                    "%02d:%02d".formatted(getDndTimer.result[0].startHour, getDndTimer.result[0].startMinute)));
            updateState(CHANNEL_DND_END, new StringType(
                    "%02d:%02d".formatted(getDndTimer.result[0].endHour, getDndTimer.result[0].endMinute)));
        }
    }

    private void handleGetSegmentStatus(String response) {
        if (response != null && !response.isEmpty()) {
            String safeResponse = response;
            JsonObject responseObj = JsonParser.parseString(safeResponse).getAsJsonObject();
            if (responseObj.get("result").isJsonArray()
                    && responseObj.get("result").getAsJsonArray().get(0).isJsonPrimitive()) {
                JsonArray getSegmentStatus = responseObj.get("result").getAsJsonArray();
                String stat = getSegmentStatus.get(0).getAsString();
                if (!"OK".equals(stat)) {
                    updateState(RobotCapabilities.SEGMENT_STATUS.getChannel(),
                            OnOffType.from(1 == Integer.parseInt(stat)));
                }
            }
        }
    }

    private void handleGetMapStatus(String response) {
        if (response != null && !response.isEmpty()) {
            String safeResponse = response;
            JsonObject responseObj = JsonParser.parseString(safeResponse).getAsJsonObject();
            if (responseObj.get("result").isJsonArray()
                    && responseObj.get("result").getAsJsonArray().get(0).isJsonPrimitive()) {
                JsonArray getSegmentStatus = responseObj.getAsJsonObject().get("result").getAsJsonArray();
                try {
                    String stat = getSegmentStatus.get(0).getAsString();
                    if (!"OK".equals(stat)) {
                        updateState(RobotCapabilities.MAP_STATUS.getChannel(),
                                OnOffType.from(1 == Integer.parseInt(stat)));
                    }
                } catch (ClassCastException | IllegalStateException e) {
                    logger.debug("Could not update numeric channel {} with '{}': {}",
                            RobotCapabilities.MAP_STATUS.getChannel(), response, e.getMessage());
                }
            }
        }
    }

    private void handleGetLedStatus(String response) {
        if (response != null && !response.isEmpty()) {
            String safeResponse = response;
            JsonObject responseObj = JsonParser.parseString(safeResponse).getAsJsonObject();
            if (responseObj.get("result").isJsonArray()
                    && responseObj.get("result").getAsJsonArray().get(0).isJsonPrimitive()) {
                JsonArray getSegmentStatus = responseObj.get("result").getAsJsonArray();
                try {
                    String stat = getSegmentStatus.get(0).getAsString();
                    if (!"OK".equals(stat)) {
                        updateState(RobotCapabilities.LED_STATUS.getChannel(),
                                OnOffType.from(1 == Integer.parseInt(stat)));
                    }
                } catch (ClassCastException | IllegalStateException e) {
                    logger.debug("Could not update numeric channel {} with '{}': {}",
                            RobotCapabilities.LED_STATUS.getChannel(), response, e.getMessage());
                }
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
                    logger.trace("ChannelType {} not found (Unexpected). Available types:",
                            capability.getChannelType());
                    for (ChannelType ct : channelTypeRegistry.getChannelTypes()) {
                        logger.trace("Available channelType: '{}' '{}' '{}'", ct.getUID(), ct.toString(),
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
        // B01 protocol responses arrive on dps.10001
        if (dpsJson.has("10001")) {
            return dpsJson.get("10001");
        }
        return null;
    }

    /**
     * Dispatches a normalised (V1-shaped) JSON response string to the appropriate handleGet* method
     * based on the request correlation tracker.
     */
    private void dispatchNormalisedResponse(String jsonString) {
        if (jsonString.endsWith("\"result\":[\"ok\"]}") || jsonString.endsWith("\"result\":[]}")) {
            return;
        }

        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
        if (obj.has("id") && obj.has("result")) {
            int messageId = obj.get("id").getAsInt();
            String methodName = requestCorrelationTracker.findMethodByRequestId(messageId);
            if (methodName == null) {
                logger.trace("Received response for unknown or already handled message ID: {}", messageId);
                return;
            }

            switch (methodName) {
                case "getStatus", COMMAND_GET_STATUS -> handleGetStatus(jsonString);
                case "getConsumable", COMMAND_GET_CONSUMABLE -> handleGetConsumables(jsonString);
                case "getRoomMapping", COMMAND_GET_ROOM_MAPPING -> handleGetRoomMapping(jsonString);
                case "getNetworkInfo", COMMAND_GET_NETWORK_INFO -> handleGetNetworkInfo(jsonString);
                case "getCleanRecord", COMMAND_GET_CLEAN_RECORD -> handleGetCleanRecord(jsonString);
                case "getCleanSummary", COMMAND_GET_CLEAN_SUMMARY -> handleGetCleanSummary(jsonString);
                case "getDndTimer", COMMAND_GET_DND_TIMER -> handleGetDndTimer(jsonString);
                case "getSegmentStatus", COMMAND_GET_SEGMENT_STATUS -> handleGetSegmentStatus(jsonString);
                case "getMapStatus", COMMAND_GET_MAP_STATUS -> handleGetMapStatus(jsonString);
                case "getLedStatus", COMMAND_GET_LED_STATUS -> handleGetLedStatus(jsonString);
                case "getCarpetMode", COMMAND_GET_CARPET_MODE -> handleGetCarpetMode(jsonString);
                case "getFwFeatures", COMMAND_GET_FW_FEATURES -> handleGetFwFeatures(jsonString);
                case "getMultiMapsList", COMMAND_GET_MULTI_MAP_LIST -> handleGetMultiMapsList(jsonString);
                case "getCustomizeCleanMode", COMMAND_GET_CUSTOMIZE_CLEAN_MODE ->
                    handleGetCustomizeCleanMode(jsonString);
                default -> logger.debug("No handler for method: {}", methodName);
            }
            requestCorrelationTracker.removeByRequestId(messageId);
        }
    }

    /**
     * Sends a Q10 native DP command by writing raw data-points directly to the device,
     * bypassing the RPC method/msgId envelope used by V1 and Q7.
     *
     * @param dps the data-point map to publish, e.g. {@code {"201": {"cmd":1}}}
     */
    private void sendQ10DpCommand(Map<String, Object> dps) {
        RoborockAccountHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            logger.debug("Q10 DP command skipped: no bridge handler");
            return;
        }
        localBridge.sendB01DpCommand(config.duid, localKey, dps);
    }

    /**
     * Handles unsolicited flat DP pushes from B01 devices.
     *
     * <p>
     * For Q7, only DPs 121 (state) and 122 (battery) are expected at the root.
     * For Q10, a richer set of DPs is pushed (121–127, 136–139, 141, 142) alongside
     * a nested {@code 101} object containing map, carpet, network, and settings data.
     *
     * <p>
     * Each DP is checked independently — a single push frame can carry any
     * combination of them simultaneously.
     *
     * Handles unsolicited flat DP pushes from B01 devices.
     *
     * <p>
     * Top-level DP mappings for Q10 (from Q10ShadowDataService.applyQ10ShadowDpPayload):
     * 
     * <pre>
     *   121 = status (vacuum state ID)
     *   122 = battery
     *   123 = fan_power
     *   124 = water_box_mode
     *   125 = main_brush_life   (consumable remaining life)
     *   126 = side_brush_life   (consumable remaining life)
     *   127 = filter_life       (consumable remaining life)
     *   136 = clean_times       (number of cleans completed)
     *   137 = clean_mode        (current cleaning mode)
     *   138 = clean_task_type   (type of cleaning task)
     *   139 = back_type         (return-to-dock trigger type)
     *   141 = cleaning_progress (0–100%)
     *   142 = fleeing_goods     (obstacle avoidance active flag)
     * </pre>
     * 
     */
    private void handleFlatDpPush(JsonObject dpsRoot) {
        // DP 121 — vacuum state ID
        if (dpsRoot.has("121")) {
            int stateInt = dpsRoot.get("121").getAsInt();
            // On the Q10, status updates are provided automatically, so just update the channel.
            if (q10) {
                updateState(CHANNEL_STATE_ID, new DecimalType(stateInt));
            } else {
                updateStateIdAndRequestStatusIfChanged(stateInt, true);
            }
        }

        // DP 122 — battery %
        if (dpsRoot.has("122")) {
            updateState(CHANNEL_BATTERY, new DecimalType(dpsRoot.get("122").getAsInt()));
        }

        // DP 123 — fan/suction power
        if (dpsRoot.has("123")) {
            int fanPower = dpsRoot.get("123").getAsInt();
            updateState(CHANNEL_FAN_POWER, new DecimalType(fanPower));
            updateState(CHANNEL_FAN_CONTROL, new DecimalType(FanModeType.getType(fanPower).getId()));
        }

        // DP 124 — water level
        if (dpsRoot.has("124") && deviceCapabilities.containsKey(RobotCapabilities.WATERBOX_MODE)) {
            updateState(RobotCapabilities.WATERBOX_MODE.getChannel(), new DecimalType(dpsRoot.get("124").getAsInt()));
        }

        // DP 125 — main brush use time in s
        if (dpsRoot.has("125")) {
            int brushSecs = 3600 * dpsRoot.get("125").getAsInt();
            updateState(CHANNEL_CONSUMABLE_MAIN_TIME, new QuantityType<>(
                    ConsumablesType.remainingHours(brushSecs, ConsumablesType.MAIN_BRUSH), Units.HOUR));
            updateState(CHANNEL_CONSUMABLE_MAIN_PERC,
                    new DecimalType(ConsumablesType.remainingPercent(brushSecs, ConsumablesType.MAIN_BRUSH)));
        }

        // DP 126 — side brush use time in s
        if (dpsRoot.has("126")) {
            int sideBrushSecs = 3600 * dpsRoot.get("126").getAsInt();
            updateState(CHANNEL_CONSUMABLE_SIDE_TIME, new QuantityType<>(
                    ConsumablesType.remainingHours(sideBrushSecs, ConsumablesType.SIDE_BRUSH), Units.HOUR));
            updateState(CHANNEL_CONSUMABLE_SIDE_PERC,
                    new DecimalType(ConsumablesType.remainingPercent(sideBrushSecs, ConsumablesType.SIDE_BRUSH)));
        }

        // DP 127 — filter use time in s
        if (dpsRoot.has("127")) {
            int filterSecs = 3600 * dpsRoot.get("127").getAsInt();
            updateState(CHANNEL_CONSUMABLE_FILTER_TIME,
                    new QuantityType<>(ConsumablesType.remainingHours(filterSecs, ConsumablesType.FILTER), Units.HOUR));
            updateState(CHANNEL_CONSUMABLE_FILTER_PERC,
                    new DecimalType(ConsumablesType.remainingPercent(filterSecs, ConsumablesType.FILTER)));
        }

        // DP 136 — clean_times: number of cleaning passes completed in this task
        if (dpsRoot.has("136")) {
            logger.debug("Q10 dp136 clean_times: {}", dpsRoot.get("136").getAsInt());
        }

        // DP 137 — clean_mode: current cleaning mode integer
        if (dpsRoot.has("137")) {
            logger.debug("Q10 dp137 clean_mode: {}", dpsRoot.get("137").getAsInt());
        }

        // DP 138 — clean_task_type: type of cleaning task currently running
        if (dpsRoot.has("138")) {
            logger.debug("Q10 dp138 clean_task_type: {}", dpsRoot.get("138").getAsInt());
        }

        // DP 139 — back_type: what triggered the return-to-dock (e.g. finished, low battery)
        if (dpsRoot.has("139")) {
            logger.debug("Q10 dp139 back_type: {}", dpsRoot.get("139").getAsInt());
        }

        // DP 141 — cleaning_progress: 0–100% completion of current task
        if (dpsRoot.has("141") && deviceCapabilities.containsKey(RobotCapabilities.CLEAN_PERCENT)) {
            updateState(CHANNEL_CLEAN_PERCENT, new DecimalType(dpsRoot.get("141").getAsInt()));
        }

        // DP 142 — fleeing_goods: obstacle avoidance active (0=inactive, non-zero=active)
        if (dpsRoot.has("142")) {
            logger.debug("Q10 dp142 fleeing_goods: {}", dpsRoot.get("142").getAsInt());
        }

        // DP 101 — nested object containing map, carpet, network, settings, and live metrics
        JsonElement dp101Element = dpsRoot.get("101");
        if (dp101Element != null && dp101Element.isJsonObject()) {
            handleQ10Dp101(dp101Element.getAsJsonObject());
        }
    }

    /**
     * Handles the Q10's nested {@code dps.101} object, which carries live metrics,
     * map metadata, carpet zones, network info, consumables, and settings sub-keys.
     *
     * <p>
     * Sub-key mappings (from Q10ShadowDataService.applyQ10ShadowDpPayload commonMap):
     * *
     * 
     * <pre>
     * 6   = clean_time       7   = clean_area       25  = quiet_is_open
     * 26  = volume           29  = total_clean_area  30  = total_clean_count
     * 31  = total_clean_time 32  = local timer blob  33  = not-disturb data
     * 36  = voice_language   37  = dust_switch       40  = mop_state
     * 45  = auto_boost       47  = child_lock        50  = dust_setting
     * 51  = map_save_switch  52  = clean record list 53  = recent_clean_record
     * 55  = command ack      57  = virtual walls     59  = customized cleaning flag
     * 60  = multi_map_switch 61  = map list         65  = carpet zones
     * 67  = sensor_dirty_time 76  = carpet_clean_type 78 = clean_path_preference
     * 79  = time zone        80  = custom setting mode 81  = network info
     * 83  = robot_type       86  = laser obstacle   87  = cleaning_progress
     * 88  = ground_clean     90  = fault code       91  = current room IDs
     * 92  = disturb settings 93  = timer_type       96  = add_clean_state
     * 98  = easycard points  100 = threshold points 103 = cliff points
     * 104 = breakpoint_clean 105 = valley_point_charging  106 = valley charging time
     * 112 = ultra_high_suction_mode 113 = deep_mop_preference 207 = user_plan
     * </pre>
     */
    private void handleQ10Dp101(JsonObject dp101) {
        // Sub-key 6 — clean_time: current session clean time in seconds
        if (dp101.has("6")) {
            updateState(CHANNEL_CLEAN_TIME,
                    new QuantityType<>(TimeUnit.SECONDS.toMinutes(dp101.get("6").getAsLong()), Units.MINUTE));
        }

        // Sub-key 7 — clean_area: current session clean area in cm²; convert to m²
        if (dp101.has("7")) {
            long areaMm2 = dp101.get("7").getAsLong() * 10000L;
            updateState(CHANNEL_CLEAN_AREA, new QuantityType<>(areaMm2 / 1000000D, SIUnits.SQUARE_METRE));
        }

        // Sub-key 25 — quiet_is_open: Quiet Is Open (DND Mode)
        if (dp101.has("25")) {
            updateState(CHANNEL_DND_ENABLED, OnOffType.from(1 == dp101.get("25").getAsInt()));
        }

        // Sub-key 29 — total_clean_area: lifetime total clean area in m²
        if (dp101.has("29")) {
            long totalArea = dp101.get("29").getAsLong();
            updateState(CHANNEL_HISTORY_TOTALAREA, new QuantityType<>(totalArea, SIUnits.SQUARE_METRE));
        }

        // Sub-key 30 — total_clean_count: lifetime total clean cycles
        if (dp101.has("30")) {
            updateState(CHANNEL_HISTORY_COUNT, new DecimalType(dp101.get("30").getAsLong()));
        }

        // Sub-key 31 — total_clean_time: lifetime total clean time in minutes
        if (dp101.has("31")) {
            updateState(CHANNEL_HISTORY_TOTALTIME, new QuantityType<>(dp101.get("31").getAsLong(), Units.MINUTE));
        }

        // Sub-key 67 — sensor_dirty_time: sensor consumable seconds used
        if (dp101.has("67")) {
            int sensorSecs = 3600 * dp101.get("67").getAsInt();
            updateState(CHANNEL_CONSUMABLE_SENSOR_TIME,
                    new QuantityType<>(ConsumablesType.remainingHours(sensorSecs, ConsumablesType.SENSOR), Units.HOUR));
            updateState(CHANNEL_CONSUMABLE_SENSOR_PERC,
                    new DecimalType(ConsumablesType.remainingPercent(sensorSecs, ConsumablesType.SENSOR)));
        }

        // Sub-key 87 — cleaning_progress: 0–100% completion of current task
        if (dp101.has("87") && deviceCapabilities.containsKey(RobotCapabilities.CLEAN_PERCENT)) {
            updateState(CHANNEL_CLEAN_PERCENT, new DecimalType(dp101.get("87").getAsInt()));
        }

        // Sub-key 90 — fault: current error/fault code
        if (dp101.has("90")) {
            int errorCode = dp101.get("90").getAsInt();
            updateState(CHANNEL_ERROR_CODE, new StringType(VacuumErrorType.getType(errorCode).getDescription()));
            updateState(CHANNEL_ERROR_ID, new DecimalType(errorCode));
        }

        // Sub-key 81 — network info: {wifiName, ipAdress, mac, signal}
        // Note: Q10 uses "wifiName"/"ipAdress" (with typo) rather than "ssid"/"ip"
        if (dp101.has("81") && dp101.get("81").isJsonObject()) {
            JsonObject net = dp101.get("81").getAsJsonObject();
            if (net.has("wifiName"))
                updateState(CHANNEL_SSID, new StringType(net.get("wifiName").getAsString()));
            if (net.has("mac"))
                updateState(CHANNEL_BSSID, new StringType(net.get("mac").getAsString()));
            if (net.has("signal"))
                updateState(CHANNEL_RSSI, new DecimalType(net.get("signal").getAsInt()));
            if (net.has("ipAdress") && localIP.isEmpty()) {
                localIP = net.get("ipAdress").getAsString();
                refreshTransportContext();
            }
        }

        // Sub-key 61 — map list: {data:[...]}
        if (dp101.has("61") && dp101.get("61").isJsonObject()) {
            JsonObject mapList = dp101.get("61").getAsJsonObject();
            if (mapList.has("data") && mapList.get("data").isJsonArray()) {
                logger.debug("Q10 dp101.61 map list push: {} maps", mapList.get("data").getAsJsonArray().size());
                // TODO: trigger map list refresh if multi-map capability is present
            }
        }

        // Sub-key 65 — carpet zones: {data:[...]}
        if (dp101.has("65") && dp101.get("65").isJsonObject()) {
            JsonObject carpets = dp101.get("65").getAsJsonObject();
            if (carpets.has("data") && carpets.get("data").isJsonArray()) {
                logger.debug("Q10 dp101.65 carpet zone push: {} zones", carpets.get("data").getAsJsonArray().size());
            }
        }

        // Sub-key 52 — clean record list: {op:"list", data:[...]} or {op:"select", result:1}
        if (dp101.has("52") && dp101.get("52").isJsonObject()) {
            JsonObject dp52 = dp101.get("52").getAsJsonObject();
            String op = dp52.has("op") ? dp52.get("op").getAsString() : "";
            if ("list".equals(op) && dp52.has("data") && dp52.get("data").isJsonArray()) {
                logger.debug("Q10 dp101.52 clean record list push: {} records",
                        dp52.get("data").getAsJsonArray().size());
                // TODO: parse and apply cleaning history records
            }
        }

        // Sub-key 55 — command ack: base64 [cmd_byte, result_byte]; result=0x00 means success
        if (dp101.has("55")) {
            try {
                byte[] ackBytes = java.util.Base64.getDecoder().decode(dp101.get("55").getAsString());
                if (ackBytes.length >= 2) {
                    logger.debug("Q10 dp101.55 command ack: cmd={} result={}", ackBytes[0] & 0xFF, ackBytes[1] & 0xFF);
                }
            } catch (IllegalArgumentException e) {
                logger.trace("Q10 dp101.55 ack decode failed: {}", e.getMessage());
            }
        }

        // Sub-keys logged at trace — known but no channel mapping yet
        if (dp101.has("26"))
            logger.trace("Q10 dp101.26 volume: {}", dp101.get("26"));
        if (dp101.has("32")) {
            try {
                byte[] b = java.util.Base64.getDecoder().decode(dp101.get("32").getAsString());
                int timerCount = b.length >= 2 ? b[1] & 0xFF : 0;
                logger.debug("Q10 dp101.32 TIMER: version={} timerCount={} (full schedule has {} bytes)",
                        b.length >= 1 ? b[0] & 0xFF : 0, timerCount, b.length);
            } catch (IllegalArgumentException e) {
                logger.trace("Q10 dp101.32 TIMER decode failed: {}", e.getMessage());
            }
        }
        if (dp101.has("33"))
            logger.trace("Q10 dp101.33 dnd_data_blob: {}", dp101.get("33"));
        if (dp101.has("36"))
            logger.trace("Q10 dp101.36 voice_language: {}", dp101.get("36"));
        if (dp101.has("37"))
            logger.trace("Q10 dp101.37 dust_switch: {}", dp101.get("37"));
        if (dp101.has("40"))
            logger.trace("Q10 dp101.40 mop_state: {}", dp101.get("40"));
        if (dp101.has("45"))
            logger.trace("Q10 dp101.45 carpet_boost: {}", dp101.get("45"));
        if (dp101.has("47"))
            logger.trace("Q10 dp101.47 child_lock: {}", dp101.get("47"));
        if (dp101.has("50"))
            logger.trace("Q10 dp101.50 dust_setting: {}", dp101.get("50"));
        if (dp101.has("51"))
            logger.trace("Q10 dp101.51 map_save_switch: {}", dp101.get("51"));
        if (dp101.has("53"))
            logger.trace("Q10 dp101.53 recent_clean_record: {}", dp101.get("53"));
        if (dp101.has("57"))
            logger.trace("Q10 dp101.57 virtual_walls_blob: {}", dp101.get("57"));
        if (dp101.has("59"))
            logger.trace("Q10 dp101.59 customized_clean_flag: {}", dp101.get("59"));
        if (dp101.has("60"))
            logger.trace("Q10 dp101.60 multi_map_switch: {}", dp101.get("60"));
        if (dp101.has("76"))
            logger.trace("Q10 dp101.76 carpet_clean_type: {}", dp101.get("76"));
        if (dp101.has("79"))
            logger.trace("Q10 dp101.79 time_zone: {}", dp101.get("79"));
        if (dp101.has("80"))
            logger.trace("Q10 dp101.80 custom_setting_mode: {}", dp101.get("80"));
        if (dp101.has("83"))
            logger.trace("Q10 dp101.83 robot_type: {}", dp101.get("83"));
        if (dp101.has("86"))
            logger.trace("Q10 dp101.86 line_laser_obstacle_avoidance: {}", dp101.get("86"));
        if (dp101.has("88"))
            logger.trace("Q10 dp101.88 ground_clean: {}", dp101.get("88"));
        if (dp101.has("92"))
            logger.trace("Q10 dp101.92 disturb_settings: {}", dp101.get("92"));
        if (dp101.has("93"))
            logger.trace("Q10 dp101.93 timer_type: {}", dp101.get("93"));
        if (dp101.has("96"))
            logger.trace("Q10 dp101.96 add_clean_state: {}", dp101.get("96"));
        if (dp101.has("98"))
            logger.trace("Q10 dp101.98 easycard_points: {}", dp101.get("98"));
        if (dp101.has("100"))
            logger.trace("Q10 dp101.100 threshold_points: {}", dp101.get("100"));
        if (dp101.has("103"))
            logger.trace("Q10 dp101.103 cliff_points: {}", dp101.get("103"));
        if (dp101.has("104"))
            logger.trace("Q10 dp101.104 breakpoint_clean: {}", dp101.get("104"));
        if (dp101.has("105"))
            logger.trace("Q10 dp101.105 valley_point_charging: {}", dp101.get("105"));
        if (dp101.has("106"))
            logger.trace("Q10 dp101.106 valley_charging_time: {}", dp101.get("106"));
        if (dp101.has("108"))
            logger.trace("Q10 dp101.108 voice_version: {}", dp101.get("108"));
        if (dp101.has("109"))
            logger.trace("Q10 dp101.109 robot_country_code: {}", dp101.get("109"));
        if (dp101.has("112"))
            logger.trace("Q10 dp101.112 ultra_high_suction_mode: {}", dp101.get("112"));
        if (dp101.has("113"))
            logger.trace("Q10 dp101.113 deep_mop_preference: {}", dp101.get("113"));
        if (dp101.has("207"))
            logger.trace("Q10 dp101.207 user_plan: {}", dp101.get("207"));

        // All remaining sub-keys logged at trace for identification from captures
        Set<String> handledKeys = Set.of("6", "7", "25", "26", "29", "30", "31", "32", "33", "36", "37", "40", "45",
                "47", "50", "51", "52", "53", "55", "57", "59", "60", "61", "65", "67", "76", "78", "79", "80", "81",
                "83", "86", "87", "88", "90", "92", "93", "96", "98", "100", "103", "104", "105", "106", "108", "109",
                "112", "113", "207");
        for (Map.Entry<String, JsonElement> entry : dp101.entrySet()) {
            if (!handledKeys.contains(entry.getKey())) {
                logger.trace("Q10 dp101 unhandled sub-key {}: {}", entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Converts a B01 response envelope into the V1 shape expected by all handleGet* methods,
     * so no downstream handler needs to be aware of the protocol difference.
     *
     * <p>
     * B01 shape: {@code {"msgId":"12345", "code":0, "method":"prop.get", "data":{...}}}
     * <p>
     * V1 shape varies by method — see inline comments for each case.
     *
     * @return normalised V1-shaped JSON string, or {@code null} if the response is an error or unparseable
     */
    @Nullable
    private String normaliseB01Response(String jsonString) {
        try {
            JsonObject b01 = JsonParser.parseString(jsonString).getAsJsonObject();

            // Error response — log and discard
            if (b01.has("code") && b01.get("code").getAsInt() != 0) {
                logger.debug("B01 error response (code={}): {}", b01.get("code").getAsInt(), jsonString);
                return null;
            }

            // msgId is a string in B01 echoing back the int id we sent in the request
            int id = 0;
            if (b01.has("msgId")) {
                try {
                    id = (int) Long.parseLong(b01.get("msgId").getAsString());
                } catch (NumberFormatException e) {
                    logger.debug("Could not parse B01 msgId as int: {}", b01.get("msgId"));
                }
            }

            String method = b01.has("method") ? b01.get("method").getAsString() : "";
            JsonObject data = b01.has("data") && b01.get("data").isJsonObject() ? b01.get("data").getAsJsonObject()
                    : new JsonObject();

            // prop.post is a device-initiated shadow push — handled via handleFlatDpPush, not here.
            if ("prop.post".equals(method)) {
                logger.debug("B01 prop.post received — skipping RPC normalisation path");
                return null;
            }

            JsonObject v1 = new JsonObject();
            v1.addProperty("id", id);

            switch (method) {
                case "prop.get" -> {
                    if (data.has("main_brush")) {
                        renameField(data, "main_brush", "main_brush_work_time");
                        renameField(data, "side_brush", "side_brush_work_time");
                        renameField(data, "hypa", "filter_work_time");
                        renameField(data, "main_sensor", "sensor_dirty_time");
                        JsonArray consumableResult = new JsonArray();
                        consumableResult.add(data);
                        v1.add("result", consumableResult);
                    } else {
                        renameField(data, "status", "state");
                        renameField(data, "wind", "fan_power");
                        renameField(data, "water", "water_box_mode");
                        if (data.has("sweep_type") && !data.has("mode")) {
                            renameField(data, "sweep_type", "mop_mode");
                        } else {
                            renameField(data, "mode", "mop_mode");
                        }
                        renameField(data, "quantity", "battery");
                        renameField(data, "fault", "error_code");
                        renameField(data, "tank_state", "water_box_status");
                        renameField(data, "charge_state", "charge_status");
                        renameField(data, "repeat_state", "repeat");
                        renameField(data, "dust_action", "auto_dust_collection");
                        renameField(data, "cleaning_time", "clean_time");
                        if (data.has("cleaning_area")) {
                            data.addProperty("clean_area", data.get("cleaning_area").getAsLong() * 10000L);
                            data.remove("cleaning_area");
                        }
                        renameField(data, "multi_floor", "switch_map_mode");
                        renameField(data, "clean_path_preference", "corner_clean_mode");
                        JsonArray statusResult = new JsonArray();
                        statusResult.add(data);
                        v1.add("result", statusResult);
                    }
                }

                case "service.get_consumable" -> {
                    renameField(data, "main_brush", "main_brush_work_time");
                    renameField(data, "side_brush", "side_brush_work_time");
                    renameField(data, "filter", "filter_work_time");
                    renameField(data, "sensor", "sensor_dirty_time");
                    JsonArray consumableResult = new JsonArray();
                    consumableResult.add(data);
                    v1.add("result", consumableResult);
                }

                case "service.get_record_list" -> {
                    JsonObject summaryResult = new JsonObject();
                    summaryResult.addProperty("clean_time",
                            data.has("total_time") ? data.get("total_time").getAsLong() : 0);
                    summaryResult.addProperty("clean_area",
                            data.has("total_area") ? data.get("total_area").getAsLong() * 10000L : 0);
                    summaryResult.addProperty("clean_count",
                            data.has("total_count") ? data.get("total_count").getAsLong() : 0);
                    v1.add("result", summaryResult);

                    if (data.has("record_list") && data.get("record_list").isJsonArray()) {
                        JsonArray recordList = data.get("record_list").getAsJsonArray();
                        if (!recordList.isEmpty()) {
                            JsonElement lastEntry = recordList.asList().getLast();
                            if (lastEntry.isJsonObject() && lastEntry.getAsJsonObject().has("detail")) {
                                try {
                                    JsonObject detail = JsonParser
                                            .parseString(lastEntry.getAsJsonObject().get("detail").getAsString())
                                            .getAsJsonObject();
                                    JsonObject rec = new JsonObject();
                                    long startTime = detail.has("record_start_time")
                                            ? detail.get("record_start_time").getAsLong()
                                            : 0;
                                    long useTime = detail.has("record_use_time")
                                            ? detail.get("record_use_time").getAsLong()
                                            : 0;
                                    rec.addProperty("begin", startTime);
                                    rec.addProperty("end", startTime + useTime);
                                    rec.addProperty("duration", useTime);
                                    rec.addProperty("cleaned_area",
                                            detail.has("record_clean_area")
                                                    ? detail.get("record_clean_area").getAsLong() * 10000L
                                                    : 0);
                                    rec.addProperty("error",
                                            detail.has("record_faultcode") ? detail.get("record_faultcode").getAsInt()
                                                    : 0);
                                    rec.addProperty("complete",
                                            detail.has("record_task_status")
                                                    ? detail.get("record_task_status").getAsInt()
                                                    : 0);
                                    rec.addProperty("finish_reason",
                                            detail.has("record_task_status")
                                                    ? detail.get("record_task_status").getAsInt()
                                                    : 0);
                                    JsonArray recResult = new JsonArray();
                                    recResult.add(rec);
                                    JsonObject cleanRecordV1 = new JsonObject();
                                    cleanRecordV1.addProperty("id", id);
                                    cleanRecordV1.add("result", recResult);
                                    handleGetCleanRecord(gson.toJson(cleanRecordV1));
                                } catch (Exception e) {
                                    logger.debug("Failed to synthesise B01 clean record: {}", e.getMessage());
                                }
                            }
                        }
                    }
                }

                case "prop.set", "service.set_room_clean", "service.start_recharge", "service.find_device",
                        "service.reset_consumable", "201", "202", "204", "206" -> {
                    JsonArray ack = new JsonArray();
                    ack.add("ok");
                    v1.add("result", ack);
                }

                default -> {
                    logger.debug("B01 normalise: unhandled method '{}', wrapping data as-is", method);
                    JsonArray defaultResult = new JsonArray();
                    defaultResult.add(data);
                    v1.add("result", defaultResult);
                }
            }
            return gson.toJson(v1);
        } catch (Exception e) {
            logger.debug("Failed to normalise B01 response: {}", e.getMessage());
            return null;
        }
    }

    private void renameField(JsonObject obj, String from, String to) {
        if (obj.has(from)) {
            obj.add(to, obj.get(from));
            obj.remove(from);
        }
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
