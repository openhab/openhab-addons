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
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
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
import org.openhab.binding.miio.internal.robot.StatusType;
import org.openhab.binding.miio.internal.robot.VacuumErrorType;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final SimpleDateFormat DATEFORMATTER = new SimpleDateFormat("yyyyMMdd-HHss");
    private static final String MAP_PATH = ConfigConstants.getUserDataFolder() + File.separator + BINDING_ID
            + File.separator;
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

    public MiIoVacuumHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService,
            CloudConnector cloudConnector) {
        super(thing, miIoDatabaseWatchService);
        this.cloudConnector = cloudConnector;
        mapChannelUid = new ChannelUID(thing.getUID(), CHANNEL_VACUUM_MAP);
        initializeData();
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
                } else {
                    sendCommand(MiIoCommand.STOP_VACUUM);
                    sendCommand(MiIoCommand.CHARGE);
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
                sendCommand(MiIoCommand.CHARGE);
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

    private boolean updateVacuumStatus(JsonObject statusData) {
        updateState(CHANNEL_BATTERY, new DecimalType(statusData.get("battery").getAsBigDecimal()));
        updateState(CHANNEL_CLEAN_AREA, new DecimalType(statusData.get("clean_area").getAsDouble() / 1000000.0));
        updateState(CHANNEL_CLEAN_TIME,
                new DecimalType(TimeUnit.SECONDS.toMinutes(statusData.get("clean_time").getAsLong())));
        updateState(CHANNEL_DND_ENABLED, new DecimalType(statusData.get("dnd_enabled").getAsBigDecimal()));
        updateState(CHANNEL_ERROR_CODE,
                new StringType(VacuumErrorType.getType(statusData.get("error_code").getAsInt()).getDescription()));
        updateState(CHANNEL_ERROR_ID, new DecimalType(statusData.get("error_code").getAsInt()));
        int fanLevel = statusData.get("fan_power").getAsInt();
        updateState(CHANNEL_FAN_POWER, new DecimalType(fanLevel));
        updateState(CHANNEL_FAN_CONTROL, new DecimalType(FanModeType.getType(fanLevel).getId()));
        updateState(CHANNEL_IN_CLEANING, new DecimalType(statusData.get("in_cleaning").getAsInt()));
        updateState(CHANNEL_MAP_PRESENT, new DecimalType(statusData.get("map_present").getAsBigDecimal()));
        StatusType state = StatusType.getType(statusData.get("state").getAsInt());
        updateState(CHANNEL_STATE, new StringType(state.getDescription()));
        stateId = statusData.get("state").getAsInt();
        updateState(CHANNEL_STATE_ID, new DecimalType(stateId));
        State vacuum = OnOffType.OFF;
        String control;
        switch (state) {
            case CLEANING:
                control = "vacuum";
                vacuum = OnOffType.ON;
                break;
            case CHARGING:
                control = "dock";
                break;
            case CHARGING_ERROR:
                control = "dock";
                break;
            case DOCKING:
                control = "dock";
                break;
            case FULL:
                control = "dock";
                break;
            case IDLE:
                control = "pause";
                break;
            case PAUSED:
                control = "pause";
                break;
            case RETURNING:
                control = "dock";
                break;
            case SLEEPING:
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
    protected boolean initializeData() {
        updateState(CHANNEL_CONSUMABLE_RESET, new StringType("none"));
        this.miioCom = getConnection();
        return true;
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
                    updateVacuumStatus(response.getResult().getAsJsonArray().get(0).getAsJsonObject());
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
                        final String mapPath = MAP_PATH + map + DATEFORMATTER.format(new Date()) + ".rrmap";
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
