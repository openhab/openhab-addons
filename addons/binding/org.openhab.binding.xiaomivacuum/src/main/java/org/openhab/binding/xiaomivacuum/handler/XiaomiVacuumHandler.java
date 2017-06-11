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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.xiaomivacuum.internal.RoboCommunication;
import org.openhab.binding.xiaomivacuum.internal.StatusType;
import org.openhab.binding.xiaomivacuum.internal.Utils;
import org.openhab.binding.xiaomivacuum.internal.VacuumCommand;
import org.openhab.binding.xiaomivacuum.internal.VacuumErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link XiaomiVacuumHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class XiaomiVacuumHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(XiaomiVacuumHandler.class);

    private ScheduledFuture<?> pollingJob;

    private JsonParser parser;
    private String ip;
    private byte[] token;

    private final int CACHE_EXPIRY = 5 * 1000; // 5s
    private ExpiringCache<String> status;

    private ExpiringCache<String> consumables;

    private RoboCommunication roboCom;

    public XiaomiVacuumHandler(Thing thing) {
        super(thing);
        parser = new JsonParser();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_VACUUM)) {
            if (command instanceof OnOffType && command == OnOffType.ON) {
                roboCom.sendCommand(VacuumCommand.START_VACUUM);
            } else if (channelUID.getId().equals(CHANNEL_VACUUM)) {
                if (command instanceof OnOffType && command == OnOffType.OFF) {
                    roboCom.sendCommand(VacuumCommand.STOP_VACUUM);
                }
            }
        }
        if (channelUID.getId().equals(CHANNEL_SPOT)) {
            if (command instanceof OnOffType && command == OnOffType.ON) {
                roboCom.sendCommand(VacuumCommand.START_SPOT);
            } else if (channelUID.getId().equals(CHANNEL_VACUUM)) {
                if (command instanceof OnOffType && command == OnOffType.OFF) {
                    roboCom.sendCommand(VacuumCommand.STOP_VACUUM);
                }
            }
        }
        if (channelUID.getId().equals(CHANNEL_PAUSE) && command instanceof OnOffType && command == OnOffType.ON) {
            roboCom.sendCommand(VacuumCommand.PAUSE);
        }
        if (channelUID.getId().equals(CHANNEL_RETURN) && command instanceof OnOffType && command == OnOffType.ON) {
            roboCom.sendCommand(VacuumCommand.STOP_VACUUM);
            roboCom.sendCommand(VacuumCommand.CHARGE);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Xiaomi Robot Vacuum handler '{}'", getThing().getUID());

        Object param;

        param = getConfig().get(PROPERTY_HOST_IP);
        if (param instanceof String) {
            ip = ((String) param);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing IP");
        }

        param = getConfig().get(PROPERTY_TOKEN);
        if (param instanceof String) {
            String tokenSting = (String) param;
            if (tokenSting.length() == 32) {
                token = Utils.hexStringToByteArray(tokenSting);
            } else if (tokenSting.length() == 16) {
                token = tokenSting.getBytes();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "token length error token");
            }
            logger.debug("Initializing Xiaomi Robot Vacuum token  '{}'", param);

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "token error");
        }

        int pollingPeriod = 30;
        param = getConfig().get(PROPERTY_REFRESH_INTERVAL);
        if (param instanceof BigDecimal) {
            pollingPeriod = ((BigDecimal) param).intValue();
        }

        try {
            roboCom = new RoboCommunication(ip, token);
            updateProperty(Thing.PROPERTY_SERIAL_NUMBER, Utils.getHex(roboCom.getSerial()));
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
        updateProperty(Thing.PROPERTY_VENDOR, "Xiaomi");
        updateProperty(Thing.PROPERTY_MODEL_ID, "rockrobo");

        status = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            return roboCom.sendCommand(VacuumCommand.GET_STATUS);
        });
        consumables = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            return roboCom.sendCommand(VacuumCommand.CONSUMABLES_GET);
        });
        pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, pollingPeriod, TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing XiaomiVacuum handler '{}'", getThing().getUID());
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private JsonObject getResult(String res) {
        logger.debug("RAW vacuum response'{}'", res);
        JsonObject vacuumStatus = (JsonObject) parser.parse(res);
        JsonObject result = vacuumStatus.getAsJsonArray("result").get(0).getAsJsonObject();
        logger.debug("Response ID:     '{}'", vacuumStatus.get("id").getAsString());
        logger.debug("Response Result: '{}'", result);
        return result;
    }

    private String updateVacuumStatus() {
        String err = null;
        try {
            JsonObject statusData = getResult(status.getValue());
            if (statusData == null) {
                err = "no response";
                return err;
            }
            updateState(CHANNEL_BATTERY, new DecimalType(statusData.get("battery").getAsBigDecimal()));
            updateState(CHANNEL_CLEAN_AREA, new DecimalType(statusData.get("clean_area").getAsDouble() / 1000000.0));
            updateState(CHANNEL_CLEAN_TIME_, new DecimalType(statusData.get("clean_time").getAsBigDecimal()
                    .divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
            updateState(CHANNEL_DND_ENABLED, new DecimalType(statusData.get("dnd_enabled").getAsBigDecimal()));
            updateState(CHANNEL_ERROR_CODE,
                    new StringType(VacuumErrorType.getType(statusData.get("error_code").getAsInt()).getDescription()));
            updateState(CHANNEL_FAN_POWER, new DecimalType(statusData.get("fan_power").getAsBigDecimal()));
            updateState(CHANNEL_IN_CLEANING, new DecimalType(statusData.get("in_cleaning").getAsBigDecimal()));
            updateState(CHANNEL_MAP_PRESENT, new DecimalType(statusData.get("map_present").getAsBigDecimal()));
            updateState(CHANNEL_MSG_SEQ, new DecimalType(statusData.get("msg_seq").getAsBigDecimal()));
            updateState(CHANNEL_MSG_VER, new DecimalType(statusData.get("msg_ver").getAsBigDecimal()));
            StatusType state = StatusType.getType(statusData.get("state").getAsInt());
            updateState(CHANNEL_STATE, new StringType(state.getDescription()));
            if (state.equals(StatusType.CLEANING)) {
                updateState(CHANNEL_VACUUM, OnOffType.ON);
            } else {
                updateState(CHANNEL_VACUUM, OnOffType.OFF);
            }
            if (state.equals(StatusType.RETURNING)) {
                updateState(CHANNEL_RETURN, OnOffType.ON);
            } else {
                updateState(CHANNEL_RETURN, OnOffType.OFF);
            }
            if (state.equals(StatusType.SPOTCLEAN)) {
                updateState(CHANNEL_SPOT, OnOffType.ON);
            } else {
                updateState(CHANNEL_SPOT, OnOffType.OFF);
            }
            if (state.equals(StatusType.PAUSED)) {
                updateState(CHANNEL_PAUSE, OnOffType.ON);
            } else {
                updateState(CHANNEL_PAUSE, OnOffType.OFF);
            }
        } catch (Exception e) {
            err = "Failed to process vacuum data";
            logger.debug(err, e);
            return err;
        }

        return err;
    }

    private String updateConsumables() {
        String err = null;
        try {
            JsonObject statusData = getResult(consumables.getValue());
            if (statusData == null) {
                err = "no response";
                return err;
            }
            logger.debug("consumable {}", statusData);
            updateState(CHANNEL_CONSUMABLE_MAIN, new DecimalType(statusData.get("main_brush_work_time")
                    .getAsBigDecimal().divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
            updateState(CHANNEL_CONSUMABLE_SIDE, new DecimalType(statusData.get("side_brush_work_time")
                    .getAsBigDecimal().divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
            updateState(CHANNEL_CONSUMABLE_FILTER, new DecimalType(statusData.get("filter_work_time").getAsBigDecimal()
                    .divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
            updateState(CHANNEL_CONSUMABLE_SENSOR, new DecimalType(statusData.get("sensor_dirty_time").getAsBigDecimal()
                    .divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
        } catch (Exception e) {
            err = "Failed to process vacuum data";
            logger.debug(err, e);
            return err;
        }

        return err;
    }

    private synchronized void updateData() {
        logger.debug("Update vacuum status'{}'", getThing().getUID());

        String res = updateVacuumStatus();
        res += updateConsumables();
        if (res != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, res);
        }

        if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
