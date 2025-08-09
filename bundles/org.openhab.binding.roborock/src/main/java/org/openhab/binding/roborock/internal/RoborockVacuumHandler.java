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
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.openhab.core.thing.ThingStatusInfo;
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
import com.google.gson.JsonSyntaxException;

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
    private final SchedulerTask pollTask;
    private String token = "";
    private Rooms[] homeRooms = new Rooms[0];
    private String rrHomeId = "";
    private String localKey = "";
    private final byte[] nonce = new byte[16];
    private boolean hasChannelStructure;
    private ConcurrentHashMap<RobotCapabilities, Boolean> deviceCapabilities = new ConcurrentHashMap<>();
    private ChannelTypeRegistry channelTypeRegistry;
    private long lastSuccessfulPollTimestamp;
    private boolean supportsRoutines = true;
    private final Gson gson = new Gson();
    private String lastHistoryID = "";

    private final Map<String, Integer> outstandingRequests = new ConcurrentHashMap<>();

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
        pollTask = new SchedulerTask(scheduler, logger, "Poll", this::pollData);
        new java.security.SecureRandom().nextBytes(nonce);
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
                    sendCommand(COMMAND_APP_CHARGE);
                } else {
                    logger.info("Command {} not recognised", command.toString());
                }
                return;
            }

            if (channelUID.getId().equals(CHANNEL_ROUTINE)) {
                setRoutineViaBridge(command.toString());
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/offline.conf-error.no-bridge");
            return;
        }
        bridgeHandler = accountHandler;
        hasChannelStructure = false;

        initTask.setNamePrefix(getThing().getUID().getId());
        pollTask.setNamePrefix(getThing().getUID().getId());
        initTask.schedule(5);
        updateStatus(ThingStatus.UNKNOWN);
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
        if (token.isEmpty()) {
            token = getTokenFromBridge();
            if (!token.isEmpty()) {
                if (rrHomeId.isEmpty()) {
                    Home home = bridgeHandler.getHomeDetail();
                    if (home != null) {
                        rrHomeId = Integer.toString(home.data.rrHomeId);
                    }
                }
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "@text/offline.conf-error.no-token");
            }
        }

        scheduleNextPoll(-1);
        updateStatus(ThingStatus.ONLINE);
    }

    private synchronized void teardown(boolean scheduleReconnection) {
        pollTask.cancel();
        initTask.cancel();

        if (scheduleReconnection) {
            initTask.schedule(30);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        teardown(false);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {}", bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initTask.submit();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            teardown(false);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void pollData() {
        logger.debug("Running pollData for: {}", getThing().getUID().getId());
        if (!rrHomeId.isEmpty()) {
            HomeData homeData = bridgeHandler.getHomeData(rrHomeId);
            if ((homeData != null && (homeData.result != null))) {
                for (int i = 0; i < homeData.result.devices.length; i++) {
                    if (getThing().getUID().getId().equals(homeData.result.devices[i].duid)) {
                        if (localKey.isEmpty()) {
                            localKey = homeData.result.devices[i].localKey;
                        }
                        updateState(CHANNEL_ERROR_ID,
                                new DecimalType(homeData.result.devices[i].deviceStatus.errorCode));
                        updateState(CHANNEL_STATE_ID,
                                new DecimalType(homeData.result.devices[i].deviceStatus.vacuumState));
                        updateState(CHANNEL_BATTERY, new DecimalType(homeData.result.devices[i].deviceStatus.battery));
                        updateState(CHANNEL_FAN_POWER,
                                new DecimalType(homeData.result.devices[i].deviceStatus.fanPower));
                        updateState(CHANNEL_MOP_DRYING,
                                new DecimalType(homeData.result.devices[i].deviceStatus.dryingStatus));
                        updateState(CHANNEL_CONSUMABLE_MAIN_PERC,
                                new DecimalType(homeData.result.devices[i].deviceStatus.mainBrushWorkTime));
                        updateState(CHANNEL_CONSUMABLE_SIDE_PERC,
                                new DecimalType(homeData.result.devices[i].deviceStatus.sideBrushWorkTime));
                        updateState(CHANNEL_CONSUMABLE_FILTER_PERC,
                                new DecimalType(homeData.result.devices[i].deviceStatus.filterWorkTime));

                        homeRooms = homeData.result.rooms;
                        if (homeData.result.devices[i].online) {
                            sendAllMqttCommands();
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "@text/offline.comm-error.vac-offline");
                        }
                    }
                }
            }
        }

        if (supportsRoutines) {
            String routinesResponse = bridgeHandler.getRoutines(getThing().getUID().getId());
            if (!routinesResponse.isEmpty()
                    && JsonParser.parseString(routinesResponse).getAsJsonObject().get("result").isJsonArray()
                    && JsonParser.parseString(routinesResponse).getAsJsonObject().get("result").getAsJsonArray()
                            .size() > 0
                    && JsonParser.parseString(routinesResponse).getAsJsonObject().get("result").getAsJsonArray().get(0)
                            .isJsonObject()) {
                JsonArray routinesArray = JsonParser.parseString(routinesResponse).getAsJsonObject().get("result")
                        .getAsJsonArray();
                Map<String, Object> routines = new HashMap<>();
                for (int i = 0; i < routinesArray.size(); ++i) {
                    JsonObject routinesJsonObject = routinesArray.get(i).getAsJsonObject();
                    routines.put(routinesJsonObject.get("id").getAsString(),
                            routinesJsonObject.get("name").getAsString());
                }
                updateState(CHANNEL_ROUTINES, new StringType(gson.toJson(routines)));
            } else {
                logger.debug("Routines not supported for device {}", getThing().getUID().getId());
                supportsRoutines = false;
            }
        }

        lastSuccessfulPollTimestamp = System.currentTimeMillis();
        scheduleNextPoll(-1);

        updateStatus(ThingStatus.ONLINE);
    }

    private void sendAllMqttCommands() {
        try {
            outstandingRequests.put("getStatus", sendCommand(COMMAND_GET_STATUS));
            outstandingRequests.put("getConsumable", sendCommand(COMMAND_GET_CONSUMABLE));
            outstandingRequests.put("getNetworkInfo", sendCommand(COMMAND_GET_NETWORK_INFO));
            outstandingRequests.put("getCleanSummary", sendCommand(COMMAND_GET_CLEAN_SUMMARY));
            outstandingRequests.put("getDndTimer", sendCommand(COMMAND_GET_DND_TIMER));
            outstandingRequests.put("getRoomMapping", sendCommand(COMMAND_GET_ROOM_MAPPING));
            outstandingRequests.put("getSegmentStatus", sendCommand(COMMAND_GET_SEGMENT_STATUS));
            outstandingRequests.put("getMapStatus", sendCommand(COMMAND_GET_MAP_STATUS));
            outstandingRequests.put("getLedStatus", sendCommand(COMMAND_GET_LED_STATUS));
            outstandingRequests.put("getCarpetMode", sendCommand(COMMAND_GET_CARPET_MODE));
            outstandingRequests.put("getFwFeatures", sendCommand(COMMAND_GET_FW_FEATURES));
            outstandingRequests.put("getMultiMapsList", sendCommand(COMMAND_GET_MULTI_MAP_LIST));
            outstandingRequests.put("getCustomizeCleanMode", sendCommand(COMMAND_GET_CUSTOMIZE_CLEAN_MODE));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Failed to send MQTT commands due to unsupported encoding: {}", e.getMessage());
        }
    }

    public void handleMessage(byte[] payload) {
        logger.debug("Received MQTT message for: {}", getThing().getUID().getId());
        try {
            String response = ProtocolUtils.handleMessage(payload, localKey, nonce);
            if (response.isEmpty()) {
                logger.debug("MQTT message processed - invalid message format received");
                return;
            }

            logger.trace("Received MQTT message: {}", response);

            if (JsonParser.parseString(response).isJsonObject()
                    && JsonParser.parseString(response).getAsJsonObject().get("dps").isJsonObject()
                    && JsonParser.parseString(response).getAsJsonObject().get("dps").getAsJsonObject().has("102")) {
                String jsonString = JsonParser.parseString(response).getAsJsonObject().get("dps").getAsJsonObject()
                        .get("102").getAsString();
                if (!jsonString.endsWith("\"result\":[\"ok\"]}") && !jsonString.endsWith("\"result\":[]}")
                        && JsonParser.parseString(jsonString).getAsJsonObject().has("id")
                        && JsonParser.parseString(jsonString).getAsJsonObject().has("result")) {
                    int messageId = JsonParser.parseString(jsonString).getAsJsonObject().get("id").getAsInt();
                    String methodName = outstandingRequests.entrySet().stream()
                            .filter(entry -> entry.getValue() == messageId).map(Map.Entry::getKey).findFirst()
                            .orElse(null);

                    if (methodName == null) {
                        logger.trace("Received response {} for unknown or already handled message ID: {}", response,
                                messageId);
                        return;
                    }

                    logger.debug("Received {} response for ID {}, parsing it.", methodName, messageId);
                    switch (methodName) {
                        case "getStatus":
                            handleGetStatus(jsonString);
                            break;
                        case "getConsumable":
                            handleGetConsumables(jsonString);
                            break;
                        case "getRoomMapping":
                            handleGetRoomMapping(jsonString);
                            break;
                        case "getNetworkInfo":
                            handleGetNetworkInfo(jsonString);
                            break;
                        case "getCleanRecord":
                            handleGetCleanRecord(jsonString);
                            break;
                        case "getCleanSummary":
                            handleGetCleanSummary(jsonString);
                            break;
                        case "getDndTimer":
                            handleGetDndTimer(jsonString);
                            break;
                        case "getSegmentStatus":
                            handleGetSegmentStatus(jsonString);
                            break;
                        case "getMapStatus":
                            handleGetMapStatus(jsonString);
                            break;
                        case "getLedStatus":
                            handleGetLedStatus(jsonString);
                            break;
                        case "getCarpetMode":
                            handleGetCarpetMode(jsonString);
                            break;
                        case "getFwFeatures":
                            handleGetFwFeatures(jsonString);
                            break;
                        case "getMultiMapsList":
                            handleGetMultiMapsList(jsonString);
                            break;
                        case "getCustomizeCleanMode":
                            handleGetCustomizeCleanMode(jsonString);
                            break;
                        case "getMap":
                            handleGetMap(jsonString);
                            break;
                        default:
                            logger.debug("No handler for method: {}", methodName);
                            break;
                    }
                    outstandingRequests.remove(methodName);
                }
            } else {
                // handle live updates ie any one (or more of values of dps)
                // "dps":{"121":8,"122":100,"123":104,"124":203,"125":75,"126":63,"127":50,"128":0,"133":1,"134":0}
                if (JsonParser.parseString(response).isJsonObject()
                        && JsonParser.parseString(response).getAsJsonObject().get("dps").isJsonObject()) {
                    JsonObject dpsJsonObject = JsonParser.parseString(response).getAsJsonObject().get("dps")
                            .getAsJsonObject();
                    if (dpsJsonObject.has("121")) {
                        int stateInt = dpsJsonObject.get("121").getAsInt();
                        StatusType state = StatusType.getType(stateInt);
                        updateState(CHANNEL_STATE, new StringType(state.getDescription()));
                        updateState(CHANNEL_STATE_ID, new DecimalType(stateInt));
                    } else if (dpsJsonObject.has("122")) {
                        int battery = dpsJsonObject.get("122").getAsInt();
                        updateState(CHANNEL_BATTERY, new DecimalType(battery));
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            // Occasionally get non-JSON returned from the Roborock MQTT server
            logger.debug("Invalid JSON response", e);
        }
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
        logger.trace("handleGetConsumable - response {}", response);
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
        logger.trace("handleGetNetworkInfo - response {}", response);
        GetNetworkInfo getNetworkInfo = gson.fromJson(response, GetNetworkInfo.class);
        if (getNetworkInfo != null && getNetworkInfo.result != null) {
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
                        outstandingRequests.put("getCleanRecord",
                                sendCommand(COMMAND_GET_CLEAN_RECORD, "[" + lastClean + "]"));
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
                            outstandingRequests.put("getCleanRecord",
                                    sendCommand(COMMAND_GET_CLEAN_RECORD, "[" + lastClean + "]"));
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
        if (getDndTimer != null) {
            updateState(CHANNEL_DND_FUNCTION, new DecimalType(getDndTimer.result[0].enabled));
            updateState(CHANNEL_DND_START, new StringType(
                    String.format("%02d:%02d", getDndTimer.result[0].startHour, getDndTimer.result[0].startMinute)));
            updateState(CHANNEL_DND_END, new StringType(
                    String.format("%02d:%02d", getDndTimer.result[0].endHour, getDndTimer.result[0].endMinute)));
        }
    }

    private void handleGetSegmentStatus(String response) {
        logger.trace("handleGetSegmentStatus, response = {}", response);
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray() && JsonParser
                .parseString(response).getAsJsonObject().get("result").getAsJsonArray().get(0).isJsonPrimitive()) {
            JsonArray getSegmentStatus = JsonParser.parseString(response).getAsJsonObject().get("result")
                    .getAsJsonArray();
            String stat = getSegmentStatus.get(0).getAsString();
            if (!"OK".equals(stat)) {
                updateState(RobotCapabilities.SEGMENT_STATUS.getChannel(), new DecimalType(Integer.parseInt(stat)));
            }
        }
    }

    private void handleGetMapStatus(String response) {
        logger.trace("handleGetMapStatus, response = {}", response);
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray() && JsonParser
                .parseString(response).getAsJsonObject().get("result").getAsJsonArray().get(0).isJsonPrimitive()) {
            JsonArray getSegmentStatus = JsonParser.parseString(response).getAsJsonObject().get("result")
                    .getAsJsonArray();
            try {
                String stat = getSegmentStatus.get(0).getAsString();
                if (!"OK".equals(stat)) {
                    updateState(RobotCapabilities.MAP_STATUS.getChannel(), new DecimalType(Integer.parseInt(stat)));
                }
            } catch (ClassCastException | IllegalStateException e) {
                logger.debug("Could not update numeric channel {} with '{}': {}",
                        RobotCapabilities.MAP_STATUS.getChannel(), response, e.getMessage());
            }
        }
    }

    private void handleGetLedStatus(String response) {
        logger.trace("handleGetLedStatus, response = {}", response);
        if (JsonParser.parseString(response).getAsJsonObject().get("result").isJsonArray() && JsonParser
                .parseString(response).getAsJsonObject().get("result").getAsJsonArray().get(0).isJsonPrimitive()) {
            JsonArray getSegmentStatus = JsonParser.parseString(response).getAsJsonObject().get("result")
                    .getAsJsonArray();
            try {
                String stat = getSegmentStatus.get(0).getAsString();
                if (!"OK".equals(stat)) {
                    updateState(RobotCapabilities.LED_STATUS.getChannel(), new DecimalType(Integer.parseInt(stat)));
                }
            } catch (ClassCastException | IllegalStateException e) {
                logger.debug("Could not update numeric channel {} with '{}': {}",
                        RobotCapabilities.MAP_STATUS.getChannel(), response, e.getMessage());
            }
        }
    }

    private void handleGetCarpetMode(String response) {
        logger.trace("handleGetCarpetMode, response = {}", response);
        try {
            JsonArray getCarpetMode = JsonParser.parseString(response).getAsJsonObject().get("result").getAsJsonArray();
            updateState(RobotCapabilities.CARPET_MODE.getChannel(),
                    new StringType(getCarpetMode.get(0).getAsJsonObject().toString()));
        } catch (ClassCastException | IllegalStateException e) {
            logger.debug("Could not update numeric channel {} with '{}': {}", RobotCapabilities.MAP_STATUS.getChannel(),
                    response, e.getMessage());
        }
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

    private int sendCommand(String method) throws UnsupportedEncodingException {
        return sendCommand(method, "[]");
    }

    private int sendCommand(String method, String params) throws UnsupportedEncodingException {
        RoborockAccountHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            return 0;
        }
        try {
            return localBridge.sendCommand(method, params, getThing().getUID().getId(), localKey, nonce);
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            return 0;
        }
    }
}
