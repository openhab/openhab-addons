/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.handler;

import static org.openhab.binding.miio.MiIoBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.robot.ConsumablesType;
import org.openhab.binding.miio.internal.robot.FanModeType;
import org.openhab.binding.miio.internal.robot.StatusType;
import org.openhab.binding.miio.internal.robot.VacuumErrorType;
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
public class MiIoVacuumHandler extends MiIoAbstractHandler {
    private final Logger logger = LoggerFactory.getLogger(MiIoVacuumHandler.class);

    private ExpiringCache<String> status;
    private ExpiringCache<String> consumables;
    private ExpiringCache<String> dnd;
    private ExpiringCache<String> history;
    private String lastHistoryId;

    @NonNullByDefault
    public MiIoVacuumHandler(Thing thing) {
        super(thing);
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
            status.invalidateValue();
            status.getValue();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_FAN_POWER)) {
            sendCommand(MiIoCommand.SET_MODE, "[" + command.toString() + "]");
            status.invalidateValue();
            status.getValue();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_FAN_CONTROL)) {
            if (Integer.valueOf(command.toString()) > 0) {
                sendCommand(MiIoCommand.SET_MODE, "[" + command.toString() + "]");
            }
            status.invalidateValue();
            status.getValue();
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

    private boolean updateVacuumStatus(JsonObject statusData) {
        updateState(CHANNEL_BATTERY, new DecimalType(statusData.get("battery").getAsBigDecimal()));
        updateState(CHANNEL_CLEAN_AREA, new DecimalType(statusData.get("clean_area").getAsDouble() / 1000000.0));
        updateState(CHANNEL_CLEAN_TIME,
                new DecimalType(TimeUnit.SECONDS.toMinutes(statusData.get("clean_time").getAsLong())));
        updateState(CHANNEL_DND_ENABLED, new DecimalType(statusData.get("dnd_enabled").getAsBigDecimal()));
        updateState(CHANNEL_ERROR_CODE,
                new StringType(VacuumErrorType.getType(statusData.get("error_code").getAsInt()).getDescription()));
        int fanLevel = statusData.get("fan_power").getAsInt();
        FanModeType fanpower = FanModeType.getType(fanLevel);
        updateState(CHANNEL_FAN_POWER, new DecimalType(fanLevel));
        updateState(CHANNEL_FAN_CONTROL, new DecimalType(fanpower.getId()));
        updateState(CHANNEL_IN_CLEANING, new DecimalType(statusData.get("in_cleaning").getAsBigDecimal()));
        updateState(CHANNEL_MAP_PRESENT, new DecimalType(statusData.get("map_present").getAsBigDecimal()));
        StatusType state = StatusType.getType(statusData.get("state").getAsInt());
        updateState(CHANNEL_STATE, new StringType(state.getDescription()));
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
        if (miioCom.getQueueLenght() > MAX_QUEUE) {
            logger.debug("Skipping periodic update for '{}'. {} elements in queue.", getThing().getUID().toString(),
                    miioCom.getQueueLenght());
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
        } catch (Exception e) {
            logger.debug("Error while updating '{}': '{}", getThing().getUID().toString(), e.getLocalizedMessage());
        }
    }

    @Override
    protected boolean initializeData() {
        initalizeNetworkCache();
        status = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
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
        consumables = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
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
        dnd = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
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
        history = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
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
            case UNKNOWN:
                updateState(CHANNEL_COMMAND, new StringType(response.getResponse().toString()));
                break;
            default:
                break;
        }
    }

}
