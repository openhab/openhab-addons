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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
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
import org.openhab.binding.xiaomivacuum.XiaomiVacuumBindingConfiguration;
import org.openhab.binding.xiaomivacuum.XiaomiVacuumBindingConstants;
import org.openhab.binding.xiaomivacuum.internal.Message;
import org.openhab.binding.xiaomivacuum.internal.RoboCommunication;
import org.openhab.binding.xiaomivacuum.internal.RoboCryptoException;
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
    private byte[] token;

    private final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(5);
    private ExpiringCache<String> status;
    private ExpiringCache<String> consumables;
    private ExpiringCache<String> dnd;
    private ExpiringCache<String> history;

    private RoboCommunication roboCom;

    public XiaomiVacuumHandler(Thing thing) {
        super(thing);
        parser = new JsonParser();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (roboCom == null) {
            logger.debug("Vacuum {} not online. Command {} ignored", getThing().getUID(), command.toString());
            return;
        }
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_VACUUM)) {
            if (command instanceof OnOffType && command == OnOffType.ON) {
                sendCommand(VacuumCommand.START_VACUUM);
            } else if (channelUID.getId().equals(CHANNEL_VACUUM)) {
                if (command instanceof OnOffType && command == OnOffType.OFF) {
                    sendCommand(VacuumCommand.STOP_VACUUM);
                }
            }
        }
        if (channelUID.getId().equals(CHANNEL_SPOT)) {
            if (command instanceof OnOffType && command == OnOffType.ON) {
                sendCommand(VacuumCommand.START_SPOT);
            } else if (channelUID.getId().equals(CHANNEL_VACUUM)) {
                if (command instanceof OnOffType && command == OnOffType.OFF) {
                    sendCommand(VacuumCommand.STOP_VACUUM);
                }
            }
        }
        if (channelUID.getId().equals(CHANNEL_PAUSE) && command instanceof OnOffType && command == OnOffType.ON) {
            sendCommand(VacuumCommand.PAUSE);
        }
        if (channelUID.getId().equals(CHANNEL_RETURN) && command instanceof OnOffType && command == OnOffType.ON) {
            sendCommand(VacuumCommand.STOP_VACUUM);
            sendCommand(VacuumCommand.CHARGE);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Xiaomi Robot Vacuum handler '{}'", getThing().getUID());

        XiaomiVacuumBindingConfiguration configuration = getConfigAs(XiaomiVacuumBindingConfiguration.class);
        boolean tokenFailed = false;
        String tokenSting = configuration.token;
        switch (tokenSting.length()) {
            case 32:
                if (tokenSting.equals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")) {
                    tokenFailed = true;
                } else {
                    token = Utils.hexStringToByteArray(tokenSting);
                }
                break;
            case 16:
                token = tokenSting.getBytes();
                break;
            default:
                tokenFailed = true;
        }
        if (tokenFailed) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Token required. Configure token");
            return;
        }

        scheduler.schedule(this::updateConnection, 0, TimeUnit.SECONDS);
        int pollingPeriod = configuration.refreshInterval;
        pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 5, pollingPeriod, TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing XiaomiVacuum handler '{}'", getThing().getUID());
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        roboCom = null;
    }

    String sendCommand(VacuumCommand command) {
        try {
            return roboCom.sendCommand(command);
        } catch (RoboCryptoException | IOException e) {
            disconnected(e.getMessage());
        }
        return null;
    }

    private JsonObject getResult(String res) {
        JsonObject vacuumStatus = (JsonObject) parser.parse(res);
        JsonObject result = vacuumStatus.getAsJsonArray("result").get(0).getAsJsonObject();
        logger.debug("Response ID:     '{}'", vacuumStatus.get("id").getAsString());
        logger.debug("Response Result: '{}'", result);
        return result;
    }

    private boolean updateVacuumStatus() {
        JsonObject statusData = getResult(status.getValue());
        if (statusData == null) {
            disconnected("No valid status response");
            return false;
        }
        updateState(CHANNEL_BATTERY, new DecimalType(statusData.get("battery").getAsBigDecimal()));
        updateState(CHANNEL_CLEAN_AREA, new DecimalType(statusData.get("clean_area").getAsDouble() / 1000000.0));
        updateState(CHANNEL_CLEAN_TIME, new DecimalType(
                statusData.get("clean_time").getAsBigDecimal().divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
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
        return true;
    }

    private boolean updateConsumables() {
        JsonObject consumablesData = getResult(consumables.getValue());
        if (consumablesData == null) {
            disconnected("No valid consumables response");
            return false;
        }
        updateState(CHANNEL_CONSUMABLE_MAIN, new DecimalType(consumablesData.get("main_brush_work_time")
                .getAsBigDecimal().divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
        updateState(CHANNEL_CONSUMABLE_SIDE, new DecimalType(consumablesData.get("side_brush_work_time")
                .getAsBigDecimal().divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
        updateState(CHANNEL_CONSUMABLE_FILTER, new DecimalType(consumablesData.get("filter_work_time").getAsBigDecimal()
                .divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
        updateState(CHANNEL_CONSUMABLE_SENSOR, new DecimalType(consumablesData.get("sensor_dirty_time")
                .getAsBigDecimal().divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN)));
        return true;
    }

    private boolean updateDnD() {
        JsonObject dndData = getResult(dnd.getValue());
        if (dndData == null) {
            disconnected("No valid Do not Disturb response");
            return false;
        }
        logger.debug("Do not disturb data: {}", dndData.toString());
        return true;
    }

    private boolean cleanHistory() {
        logger.debug("Cleaning history data: {}", getResult(history.getValue()));
        //        JsonObject historyData = getResult(history.getValue());
        //        if (historyData == null) {
        //            disconnected("No valid Clean History response");
        //            return false;
        //        }
        //        logger.debug("Cleaning history data: {}", historyData.toString());
        return true;
    }

    private synchronized void updateData() {
        logger.debug("Update vacuum status'{}'", getThing().getUID());
        if (!hasConnection()) {
            return;
        }
        if (updateVacuumStatus() && updateConsumables() && updateDnD() && cleanHistory()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void disconnected(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
        roboCom = null;
    }

    private boolean hasConnection() {
        if (roboCom != null) {
            return true;
        }
        return updateConnection();
    }

    private boolean updateConnection() {
        this.roboCom = getConnection();
        if (roboCom == null) {
            return false;
        }
        status = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            return sendCommand(VacuumCommand.GET_STATUS);
        });
        consumables = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            return sendCommand(VacuumCommand.CONSUMABLES_GET);
        });
        dnd = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            return sendCommand(VacuumCommand.DND_GET);
        });
        history = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            return sendCommand(VacuumCommand.CLEAN_SUMMARY_GET);
        });

        updateStatus(ThingStatus.ONLINE);
        return true;
    }

    private RoboCommunication getConnection() {
        if (roboCom != null) {
            return roboCom;
        }
        XiaomiVacuumBindingConfiguration configuration = getConfigAs(XiaomiVacuumBindingConfiguration.class);
        String serial = configuration.serial;
        try {
            if (serial != null && serial.length() == 8) {
                logger.debug("Using vacuum serial {}", serial);
                roboCom = new RoboCommunication(configuration.host, token, Utils.hexStringToByteArray(serial));
                return roboCom;
            } else {
                logger.debug("Getting vacuum serial");
                byte[] response = RoboCommunication.comms(XiaomiVacuumBindingConstants.DISCOVER_STRING,
                        configuration.host);
                Message roboResponse = new Message(response);
                updateProperty(Thing.PROPERTY_SERIAL_NUMBER, Utils.getSpacedHex(roboResponse.getSerialByte()));
                Configuration config = editConfiguration();
                config.put(PROPERTY_SERIAL, Utils.getHex(roboResponse.getSerialByte()));
                updateConfiguration(config);
                roboCom = new RoboCommunication(configuration.host, token, roboResponse.getSerialByte());
                return roboCom;
            }
        } catch (IOException e) {
            disconnected(e.getMessage());
            return null;
        }
    }
}
