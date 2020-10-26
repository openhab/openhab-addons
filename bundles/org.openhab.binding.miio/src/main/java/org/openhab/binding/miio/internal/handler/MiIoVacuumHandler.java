/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.handler;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.MiIoBindingConfiguration;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.cloud.CloudUtil;
import org.openhab.binding.miio.internal.cloud.MiCloudException;
import org.openhab.binding.miio.internal.robot.ConsumablesType;
import org.openhab.binding.miio.internal.robot.FanModeType;
import org.openhab.binding.miio.internal.robot.RRMapDraw;
import org.openhab.binding.miio.internal.robot.RobotCababilities;
import org.openhab.binding.miio.internal.robot.StatusDTO;
import org.openhab.binding.miio.internal.robot.StatusType;
import org.openhab.binding.miio.internal.robot.VacuumErrorType;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link MiIoVacuumHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoVacuumHandler extends MiIoAbstractHandler {
    private final Logger logger = LoggerFactory.getLogger(MiIoVacuumHandler.class);
    private static final float MAP_SCALE = 2.0f;
    private static final SimpleDateFormat DATEFORMATTER = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private final ChannelUID mapChannelUid;

    private ExpiringCache<String> status;
    private ExpiringCache<String> consumables;
    private ExpiringCache<String> dnd;
    private ExpiringCache<String> history;
    private int stateId;
    private ExpiringCache<String> map;
    private String lastHistoryId = "";
    private String lastMap = "";
    private CloudConnector cloudConnector;
    private boolean hasChannelStructure;
    private ConcurrentHashMap<RobotCababilities, Boolean> deviceCapabilities = new ConcurrentHashMap<>();
    private ChannelTypeRegistry channelTypeRegistry;

    public MiIoVacuumHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService,
            CloudConnector cloudConnector, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, miIoDatabaseWatchService);
        this.cloudConnector = cloudConnector;
        this.channelTypeRegistry = channelTypeRegistry;
        mapChannelUid = new ChannelUID(thing.getUID(), CHANNEL_VACUUM_MAP);
        status = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.GET_STATUS);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during status refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        consumables = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.CONSUMABLES_GET);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during consumables refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        dnd = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.DND_GET);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during dnd refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        history = new ExpiringCache<>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.CLEAN_SUMMARY_GET);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during cleaning data refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        map = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            try {
                int ret = sendCommand(MiIoCommand.GET_MAP);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during dnd refresh: {}", e.getMessage(), e);
            }
            return null;
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getConnection() == null) {
            logger.debug("Vacuum {} not online. Command {} ignored", getThing().getUID(), command.toString());
            return;
        }
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
            lastMap = "";
            if (channelUID.getId().equals(CHANNEL_VACUUM_MAP)) {
                sendCommand(MiIoCommand.GET_MAP);
            }
            return;
        }
        if (channelUID.getId().equals(CHANNEL_VACUUM)) {
            if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    sendCommand(MiIoCommand.START_VACUUM);
                    forceStatusUpdate();
                    return;
                } else {
                    sendCommand(MiIoCommand.STOP_VACUUM);
                    scheduler.schedule(() -> {
                        sendCommand(MiIoCommand.CHARGE);
                        forceStatusUpdate();
                    }, 2000, TimeUnit.MILLISECONDS);
                    return;
                }
            }
        }
        if (channelUID.getId().equals(CHANNEL_CONTROL)) {
            if (command.toString().equals("vacuum")) {
                sendCommand(MiIoCommand.START_VACUUM);
            } else if (command.toString().equals("spot")) {
                sendCommand(MiIoCommand.START_SPOT);
            } else if (command.toString().equals("pause")) {
                sendCommand(MiIoCommand.PAUSE);
            } else if (command.toString().equals("dock")) {
                sendCommand(MiIoCommand.STOP_VACUUM);
                scheduler.schedule(() -> {
                    sendCommand(MiIoCommand.CHARGE);
                    forceStatusUpdate();
                }, 2000, TimeUnit.MILLISECONDS);
                return;
            } else {
                logger.info("Command {} not recognised", command.toString());
            }
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_FAN_POWER)) {
            sendCommand(MiIoCommand.SET_MODE, "[" + command.toString() + "]");
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(RobotCababilities.WATERBOX_MODE.getChannel())) {
            sendCommand(MiIoCommand.SET_WATERBOX_MODE, "[" + command.toString() + "]");
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(RobotCababilities.SEGMENT_CLEAN.getChannel()) && !command.toString().isEmpty()) {
            sendCommand(MiIoCommand.START_SEGMENT, "[" + command.toString() + "]");
            updateState(RobotCababilities.SEGMENT_CLEAN.getChannel(), UnDefType.UNDEF);
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_FAN_CONTROL)) {
            if (Integer.valueOf(command.toString()) > 0) {
                sendCommand(MiIoCommand.SET_MODE, "[" + command.toString() + "]");
            }
            forceStatusUpdate();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_CONSUMABLE_RESET)) {
            sendCommand(MiIoCommand.CONSUMABLES_RESET, "[" + command.toString() + "]");
            updateState(CHANNEL_CONSUMABLE_RESET, new StringType("none"));
        }
        if (channelUID.getId().equals(CHANNEL_COMMAND)) {
            cmds.put(sendCommand(command.toString()), command.toString());
        }
    }

    private void forceStatusUpdate() {
        status.invalidateValue();
        status.getValue();
    }

    private void safeUpdateState(String channelID, @Nullable Integer state) {
        if (state != null) {
            updateState(channelID, new DecimalType(state));
        } else {
            logger.debug("Channel {} not update. value not available.", channelID);
        }
    }

    private boolean updateVacuumStatus(JsonObject statusData) {
        StatusDTO statusInfo = GSON.fromJson(statusData, StatusDTO.class);
        safeUpdateState(CHANNEL_BATTERY, statusInfo.getBattery());
        if (statusInfo.getCleanArea() != null) {
            updateState(CHANNEL_CLEAN_AREA, new DecimalType(statusInfo.getCleanArea() / 1000000.0));
        }
        if (statusInfo.getCleanTime() != null) {
            updateState(CHANNEL_CLEAN_TIME, new DecimalType(TimeUnit.SECONDS.toMinutes(statusInfo.getCleanTime())));
        }
        safeUpdateState(CHANNEL_DND_ENABLED, statusInfo.getDndEnabled());

        if (statusInfo.getErrorCode() != null) {
            updateState(CHANNEL_ERROR_CODE,
                    new StringType(VacuumErrorType.getType(statusInfo.getErrorCode()).getDescription()));
            safeUpdateState(CHANNEL_ERROR_ID, statusInfo.getErrorCode());
        }

        if (statusInfo.getFanPower() != null) {
            updateState(CHANNEL_FAN_POWER, new DecimalType(statusInfo.getFanPower()));
            updateState(CHANNEL_FAN_CONTROL, new DecimalType(FanModeType.getType(statusInfo.getFanPower()).getId()));
        }
        safeUpdateState(CHANNEL_IN_CLEANING, statusInfo.getInCleaning());
        safeUpdateState(CHANNEL_MAP_PRESENT, statusInfo.getMapPresent());
        if (statusInfo.getState() != null) {
            StatusType state = StatusType.getType(statusInfo.getState());
            updateState(CHANNEL_STATE, new StringType(state.getDescription()));
            updateState(CHANNEL_STATE_ID, new DecimalType(statusInfo.getState()));

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
            if (control.equals("undef")) {
                updateState(CHANNEL_CONTROL, UnDefType.UNDEF);
            } else {
                updateState(CHANNEL_CONTROL, new StringType(control));
            }
            updateState(CHANNEL_VACUUM, vacuum);
        }
        if (deviceCapabilities.containsKey(RobotCababilities.WATERBOX_MODE)) {
            safeUpdateState(RobotCababilities.WATERBOX_MODE.getChannel(), statusInfo.getWaterBoxMode());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.WATERBOX_STATUS)) {
            safeUpdateState(RobotCababilities.WATERBOX_STATUS.getChannel(), statusInfo.getWaterBoxStatus());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.WATERBOX_CARRIAGE)) {
            safeUpdateState(RobotCababilities.WATERBOX_CARRIAGE.getChannel(), statusInfo.getWaterBoxCarriageStatus());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.LOCKSTATUS)) {
            safeUpdateState(RobotCababilities.LOCKSTATUS.getChannel(), statusInfo.getLockStatus());
        }
        if (deviceCapabilities.containsKey(RobotCababilities.MOP_FORBIDDEN)) {
            safeUpdateState(RobotCababilities.MOP_FORBIDDEN.getChannel(), statusInfo.getMopForbiddenEnable());
        }
        return true;
    }

    private boolean updateConsumables(JsonObject consumablesData) {
        int mainBrush = consumablesData.get("main_brush_work_time").getAsInt();
        int sideBrush = consumablesData.get("side_brush_work_time").getAsInt();
        int filter = consumablesData.get("filter_work_time").getAsInt();
        int sensor = consumablesData.get("sensor_dirty_time").getAsInt();
        updateState(CHANNEL_CONSUMABLE_MAIN_TIME,
                new DecimalType(ConsumablesType.remainingHours(mainBrush, ConsumablesType.MAIN_BRUSH)));
        updateState(CHANNEL_CONSUMABLE_MAIN_PERC,
                new DecimalType(ConsumablesType.remainingPercent(mainBrush, ConsumablesType.MAIN_BRUSH)));
        updateState(CHANNEL_CONSUMABLE_SIDE_TIME,
                new DecimalType(ConsumablesType.remainingHours(sideBrush, ConsumablesType.SIDE_BRUSH)));
        updateState(CHANNEL_CONSUMABLE_SIDE_PERC,
                new DecimalType(ConsumablesType.remainingPercent(sideBrush, ConsumablesType.SIDE_BRUSH)));
        updateState(CHANNEL_CONSUMABLE_FILTER_TIME,
                new DecimalType(ConsumablesType.remainingHours(filter, ConsumablesType.FILTER)));
        updateState(CHANNEL_CONSUMABLE_FILTER_PERC,
                new DecimalType(ConsumablesType.remainingPercent(filter, ConsumablesType.FILTER)));
        updateState(CHANNEL_CONSUMABLE_SENSOR_TIME,
                new DecimalType(ConsumablesType.remainingHours(sensor, ConsumablesType.SENSOR)));
        updateState(CHANNEL_CONSUMABLE_SENSOR_PERC,
                new DecimalType(ConsumablesType.remainingPercent(sensor, ConsumablesType.SENSOR)));
        return true;
    }

    private boolean updateDnD(JsonObject dndData) {
        logger.trace("Do not disturb data: {}", dndData.toString());
        updateState(CHANNEL_DND_FUNCTION, new DecimalType(dndData.get("enabled").getAsBigDecimal()));
        updateState(CHANNEL_DND_START, new StringType(String.format("%02d:%02d", dndData.get("start_hour").getAsInt(),
                dndData.get("start_minute").getAsInt())));
        updateState(CHANNEL_DND_END, new StringType(
                String.format("%02d:%02d", dndData.get("end_hour").getAsInt(), dndData.get("end_minute").getAsInt())));
        return true;
    }

    private boolean updateHistory(JsonArray historyData) {
        logger.trace("Cleaning history data: {}", historyData.toString());
        updateState(CHANNEL_HISTORY_TOTALTIME,
                new DecimalType(TimeUnit.SECONDS.toMinutes(historyData.get(0).getAsLong())));
        updateState(CHANNEL_HISTORY_TOTALAREA, new DecimalType(historyData.get(1).getAsDouble() / 1000000D));
        updateState(CHANNEL_HISTORY_COUNT, new DecimalType(historyData.get(2).toString()));
        if (historyData.get(3).getAsJsonArray().size() > 0) {
            String lastClean = historyData.get(3).getAsJsonArray().get(0).getAsString();
            if (!lastClean.equals(lastHistoryId)) {
                lastHistoryId = lastClean;
                sendCommand(MiIoCommand.CLEAN_RECORD_GET, "[" + lastClean + "]");
            }
        }
        return true;
    }

    private void updateHistoryRecord(JsonArray historyData) {
        ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(historyData.get(0).getAsLong()),
                ZoneId.systemDefault());
        ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(historyData.get(1).getAsLong()),
                ZoneId.systemDefault());
        long duration = TimeUnit.SECONDS.toMinutes(historyData.get(2).getAsLong());
        double area = historyData.get(3).getAsDouble() / 1000000D;
        int error = historyData.get(4).getAsInt();
        int finished = historyData.get(5).getAsInt();
        JsonObject historyRecord = new JsonObject();
        historyRecord.addProperty("start", startTime.toString());
        historyRecord.addProperty("end", endTime.toString());
        historyRecord.addProperty("duration", duration);
        historyRecord.addProperty("area", area);
        historyRecord.addProperty("error", error);
        historyRecord.addProperty("finished", finished);
        updateState(CHANNEL_HISTORY_START_TIME, new DateTimeType(startTime));
        updateState(CHANNEL_HISTORY_END_TIME, new DateTimeType(endTime));
        updateState(CHANNEL_HISTORY_DURATION, new DecimalType(duration));
        updateState(CHANNEL_HISTORY_AREA, new DecimalType(area));
        updateState(CHANNEL_HISTORY_ERROR, new DecimalType(error));
        updateState(CHANNEL_HISTORY_FINISH, new DecimalType(finished));
        updateState(CHANNEL_HISTORY_RECORD, new StringType(historyRecord.toString()));
    }

    @Override
    protected boolean skipUpdate() {
        if (!hasConnection()) {
            logger.debug("Skipping periodic update for '{}'. No Connection", getThing().getUID().toString());
            return true;
        }
        if (ThingStatusDetail.CONFIGURATION_ERROR.equals(getThing().getStatusInfo().getStatusDetail())) {
            logger.debug("Skipping periodic update for '{}' UID '{}'. Thing Status", getThing().getUID().toString(),
                    getThing().getStatusInfo().getStatusDetail());
            refreshNetwork();
            return true;
        }
        final MiIoAsyncCommunication mc = miioCom;
        if (mc != null && mc.getQueueLength() > MAX_QUEUE) {
            logger.debug("Skipping periodic update for '{}'. {} elements in queue.", getThing().getUID().toString(),
                    mc.getQueueLength());
            return true;
        }
        return false;
    }

    @Override
    protected synchronized void updateData() {
        if (!hasConnection() || skipUpdate()) {
            return;
        }
        logger.debug("Periodic update for '{}' ({})", getThing().getUID().toString(), getThing().getThingTypeUID());
        try {
            dnd.getValue();
            history.getValue();
            status.getValue();
            refreshNetwork();
            consumables.getValue();
            if (lastMap.isEmpty() || stateId != 8) {
                if (isLinked(mapChannelUid)) {
                    map.getValue();
                }
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}': '{}", getThing().getUID().toString(), e.getLocalizedMessage());
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        hasChannelStructure = false;
    }

    @Override
    protected boolean initializeData() {
        updateState(CHANNEL_CONSUMABLE_RESET, new StringType("none"));
        return super.initializeData();
    }

    @Override
    public void onMessageReceived(MiIoSendCommand response) {
        super.onMessageReceived(response);
        if (response.isError()) {
            return;
        }
        switch (response.getCommand()) {
            case GET_STATUS:
                if (response.getResult().isJsonArray()) {
                    JsonObject statusResponse = response.getResult().getAsJsonArray().get(0).getAsJsonObject();
                    if (!hasChannelStructure) {
                        setCapabilities(statusResponse);
                        createCapabilityChannels();
                    }
                    updateVacuumStatus(statusResponse);
                }
                break;
            case CONSUMABLES_GET:
                if (response.getResult().isJsonArray()) {
                    updateConsumables(response.getResult().getAsJsonArray().get(0).getAsJsonObject());
                }
                break;
            case DND_GET:
                if (response.getResult().isJsonArray()) {
                    updateDnD(response.getResult().getAsJsonArray().get(0).getAsJsonObject());
                }
                break;
            case CLEAN_SUMMARY_GET:
                if (response.getResult().isJsonArray()) {
                    updateHistory(response.getResult().getAsJsonArray());
                }
                break;
            case CLEAN_RECORD_GET:
                if (response.getResult().isJsonArray() && response.getResult().getAsJsonArray().size() > 0
                        && response.getResult().getAsJsonArray().get(0).isJsonArray()) {
                    updateHistoryRecord(response.getResult().getAsJsonArray().get(0).getAsJsonArray());
                } else {
                    logger.debug("Could not extract cleaning history record from: {}", response);
                }
                break;
            case GET_MAP:
                if (response.getResult().isJsonArray()) {
                    String mapresponse = response.getResult().getAsJsonArray().get(0).getAsString();
                    if (!mapresponse.contentEquals("retry") && !mapresponse.contentEquals(lastMap)) {
                        lastMap = mapresponse;
                        scheduler.submit(() -> updateState(CHANNEL_VACUUM_MAP, getMap(mapresponse)));
                    }
                }
                break;
            case UNKNOWN:
                updateState(CHANNEL_COMMAND, new StringType(response.getResponse().toString()));
                break;
            default:
                break;
        }
    }

    private void setCapabilities(JsonObject statusResponse) {
        for (RobotCababilities capability : RobotCababilities.values()) {
            if (statusResponse.has(capability.getStatusFieldName())) {
                deviceCapabilities.putIfAbsent(capability, false);
                logger.debug("Setting additional vacuum {}", capability);
            }
        }
    }

    private void createCapabilityChannels() {
        ThingBuilder thingBuilder = editThing();
        int cnt = 0;

        for (Entry<RobotCababilities, Boolean> robotCapability : deviceCapabilities.entrySet()) {
            RobotCababilities capability = robotCapability.getKey();
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

    private State getMap(String map) {
        final MiIoBindingConfiguration configuration = this.configuration;
        if (configuration != null && cloudConnector.isConnected()) {
            try {
                final @Nullable RawType mapDl = cloudConnector.getMap(map,
                        (configuration.cloudServer != null) ? configuration.cloudServer : "");
                if (mapDl != null) {
                    byte[] mapData = mapDl.getBytes();
                    RRMapDraw rrMap = RRMapDraw.loadImage(new ByteArrayInputStream(mapData));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (logger.isDebugEnabled()) {
                        final String mapPath = BINDING_USERDATA_PATH + File.separator + map
                                + DATEFORMATTER.format(new Date()) + ".rrmap";
                        CloudUtil.writeBytesToFileNio(mapData, mapPath);
                        logger.debug("Mapdata saved to {}", mapPath);
                    }
                    ImageIO.write(rrMap.getImage(MAP_SCALE), "jpg", baos);
                    byte[] byteArray = baos.toByteArray();
                    if (byteArray != null && byteArray.length > 0) {
                        return new RawType(byteArray, "image/jpeg");
                    } else {
                        logger.debug("Mapdata empty removing image");
                        return UnDefType.UNDEF;
                    }
                }
            } catch (MiCloudException e) {
                logger.debug("Error getting data from Xiaomi cloud. Mapdata could not be updated: {}", e.getMessage());
            } catch (IOException e) {
                logger.debug("Mapdata could not be updated: {}", e.getMessage());
            }
        } else {
            logger.debug("Not connected to Xiaomi cloud. Cannot retreive new map: {}", map);
        }
        return UnDefType.UNDEF;
    }
}
