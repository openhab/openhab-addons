/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.handler;

import static org.openhab.binding.xiaomivacuum.XiaomiVacuumBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.xiaomivacuum.internal.robot.ConsumablesType;
import org.openhab.binding.xiaomivacuum.internal.robot.FanModeType;
import org.openhab.binding.xiaomivacuum.internal.robot.StatusType;
import org.openhab.binding.xiaomivacuum.internal.robot.VacuumCommand;
import org.openhab.binding.xiaomivacuum.internal.robot.VacuumErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link XiaomiVacuumHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class XiaomiVacuumHandler extends XiaomiMiIoHandler {
    private final Logger logger = LoggerFactory.getLogger(XiaomiVacuumHandler.class);

    private ExpiringCache<String> status;
    private ExpiringCache<String> consumables;
    private ExpiringCache<String> dnd;
    private ExpiringCache<String> history;

    public XiaomiVacuumHandler(Thing thing) {
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
        if (channelUID.getId().equals(CHANNEL_CONTROL)) {
            if (command.toString().equals("vacuum")) {
                sendCommand(VacuumCommand.START_VACUUM);
            } else if (command.toString().equals("spot")) {
                sendCommand(VacuumCommand.START_SPOT);
            } else if (command.toString().equals("pause")) {
                sendCommand(VacuumCommand.PAUSE);
            } else if (command.toString().equals("dock")) {
                sendCommand(VacuumCommand.STOP_VACUUM);
                sendCommand(VacuumCommand.CHARGE);
            } else {
                logger.info("Command {} not recognised", command.toString());
            }
            status.invalidateValue();
            updateVacuumStatus();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_FAN_POWER)) {
            sendCommand(VacuumCommand.SET_MODE, command.toString());
            status.invalidateValue();
            updateVacuumStatus();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_FAN_CONTROL)) {
            if (Integer.valueOf(command.toString()) > 0) {
                sendCommand(VacuumCommand.SET_MODE, command.toString());
            }
            status.invalidateValue();
            updateVacuumStatus();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_COMMAND)) {
            updateState(CHANNEL_COMMAND, new StringType(sendCommand(command.toString())));
        }
    }

    private boolean updateVacuumStatus() {
        JsonObject statusData = getJsonResultHelper(status.getValue());
        if (statusData == null) {
            disconnectedNoResponse();
            return false;
        }
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
        String control;
        switch (state) {
            case CLEANING:
                control = "vacuum";
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
        return true;
    }

    private boolean updateConsumables() {
        JsonObject consumablesData = getJsonResultHelper(consumables.getValue());
        if (consumablesData == null) {
            return false;
        }
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

    private boolean updateDnD() {
        JsonObject dndData = getJsonResultHelper(dnd.getValue());
        if (dndData == null) {
            return false;
        }
        logger.debug("Do not disturb data: {}", dndData.toString());
        updateState(CHANNEL_DND_FUNCTION, new DecimalType(dndData.get("enabled").getAsBigDecimal()));
        updateState(CHANNEL_DND_START, new StringType(String.format("%02d:%02d", dndData.get("start_hour").getAsInt(),
                dndData.get("start_minute").getAsInt())));
        updateState(CHANNEL_DND_END, new StringType(
                String.format("%02d:%02d", dndData.get("end_hour").getAsInt(), dndData.get("end_minute").getAsInt())));
        return true;
    }

    private boolean updateHistory() {
        String historyString = history.getValue();
        if (historyString == null) {
            return false;
        }
        JsonArray historyData = ((JsonObject) parser.parse(historyString)).getAsJsonArray("result");
        if (historyData == null) {
            return false;
        }
        logger.trace("Cleaning history data: {},{}", historyData.toString());
        updateState(CHANNEL_HISTORY_TOTALTIME,
                new DecimalType(TimeUnit.SECONDS.toMinutes(historyData.get(0).getAsLong())));
        updateState(CHANNEL_HISTORY_TOTALAREA, new DecimalType(historyData.get(1).getAsDouble() / 1000000D));
        updateState(CHANNEL_HISTORY_COUNT, new DecimalType(historyData.get(2).toString()));
        return true;
    }

    @Override
    protected synchronized void updateData() {
        logger.debug("Update vacuum status '{}'", getThing().getUID().toString());
        if (!hasConnection()) {
            return;
        }
        try {
            if ((0 + (updateNetwork() ? 1 : 0) + (updateVacuumStatus() ? 1 : 0) + (updateConsumables() ? 1 : 0)
                    + (updateDnD() ? 1 : 0) + (updateHistory() ? 1 : 0)) > 0) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                disconnectedNoResponse();
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}': ", getThing().getUID().toString(), e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected boolean initializeData() {
        this.roboCom = getConnection();
        if (roboCom != null) {
            updateStatus(ThingStatus.ONLINE);
        }
        status = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            try {
                return sendCommand(VacuumCommand.GET_STATUS);
            } catch (Exception e) {
                logger.debug("Error during status refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        consumables = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            try {
                return sendCommand(VacuumCommand.CONSUMABLES_GET);
            } catch (Exception e) {
                logger.debug("Error during consumables refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        dnd = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            try {
                return sendCommand(VacuumCommand.DND_GET);
            } catch (Exception e) {
                logger.debug("Error during dnd refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        history = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            try {
                return sendCommand(VacuumCommand.CLEAN_SUMMARY_GET);
            } catch (Exception e) {
                logger.debug("Error during cleaning data refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        initalizeNetworkCache();
        return true;
    }
}
