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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DecimalType;
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
import org.openhab.binding.xiaomivacuum.internal.Utils;
import org.openhab.binding.xiaomivacuum.internal.robot.VacuumCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link XiaomiMiioHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class XiaomiMiioHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(XiaomiMiioHandler.class);

    protected ScheduledFuture<?> pollingJob;

    protected JsonParser parser;
    protected byte[] token;

    protected RoboCommunication roboCom;
    protected int lastId;

    protected ExpiringCache<String> network;

    protected final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(5);

    public XiaomiMiioHandler(Thing thing) {
        super(thing);
        parser = new JsonParser();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getConnection() == null) {
            logger.debug("MiIO device {} not online. Command {} ignored", getThing().getUID(), command.toString());
            return;
        }
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_COMMAND)) {
            updateState(CHANNEL_COMMAND, new StringType(sendCommand(command.toString())));
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MiIO device handler '{}'", getThing().getUID());
        XiaomiVacuumBindingConfiguration configuration = getConfigAs(XiaomiVacuumBindingConfiguration.class);
        if (!tolkenCheckPass(configuration.token)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Token required. Configure token");
            return;
        }
        scheduler.schedule(this::initializeData, 0, TimeUnit.SECONDS);
        scheduler.schedule(this::getDeviceType, 0, TimeUnit.SECONDS);
        int pollingPeriod = configuration.refreshInterval;
        pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 1, pollingPeriod, TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
    }

    private boolean tolkenCheckPass(String tokenSting) {
        boolean tokenPassed = true;
        switch (tokenSting.length()) {
            case 32:
                if (tokenSting.equals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")
                        || tokenSting.equals("00000000000000000000000000000000")) {
                    tokenPassed = false;
                } else {
                    token = Utils.hexStringToByteArray(tokenSting);
                }
                break;
            case 16:
                token = tokenSting.getBytes();
                break;
            default:
                tokenPassed = false;
        }
        return tokenPassed;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Xiaomi MiIO handler '{}'", getThing().getUID());
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (roboCom != null) {
            lastId = roboCom.getId();
            roboCom.close();
            roboCom = null;
        }
    }

    protected String sendCommand(VacuumCommand command) {
        return sendCommand(command, "");
    }

    protected String sendCommand(VacuumCommand command, String params) {
        try {
            return roboCom.sendCommand(command, params);
        } catch (RoboCryptoException | IOException e) {
            disconnected(e.getMessage());
        }
        return null;
    }

    /**
     * This is used to execute arbitrary commands by sending to the commands channel. Command parameters to be added
     * between
     * [] brackets. This to allow for unimplemented commands to be executed (e.g. get detailed historical cleaning
     * records)
     *
     * @param command to be executed
     * @return vacuum response
     */
    protected String sendCommand(String command) {
        try {
            command = command.trim();
            String param = "";
            int loc = command.indexOf("[");
            if (loc > 0) {
                param = command.substring(loc + 1, command.length() - 1).trim();
                command = command.substring(0, loc).trim();
            }
            return roboCom.sendCommand(command, param);
        } catch (RoboCryptoException | IOException e) {
            disconnected(e.getMessage());
        }
        return null;
    }

    protected synchronized void updateData() {
        logger.debug("Update connection '{}'", getThing().getUID().toString());
        if (!hasConnection()) {
            return;
        }
        try {
            if (updateNetwork()) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}'", getThing().getUID().toString(), e);
        }
    }

    protected boolean updateNetwork() {
        JsonObject networkData = getJsonResultHelper(network.getValue());
        if (networkData == null) {
            disconnected("No valid Network response");
            return false;
        }
        updateState(CHANNEL_SSID, new StringType(networkData.getAsJsonObject("ap").get("ssid").getAsString()));
        updateState(CHANNEL_BSSID, new StringType(networkData.getAsJsonObject("ap").get("bssid").getAsString()));
        updateState(CHANNEL_RSSI, new DecimalType(networkData.getAsJsonObject("ap").get("rssi").getAsLong()));
        updateState(CHANNEL_LIFE, new DecimalType(networkData.get("life").getAsLong()));
        return true;
    }

    protected boolean hasConnection() {
        if (roboCom != null) {
            return true;
        }
        return initializeData();
    }

    protected void disconnected(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
        try {
            lastId = roboCom.getId();
            lastId += 10;
        } catch (Exception e) {
            // Ignore
        }
        roboCom = null;
    }

    protected synchronized RoboCommunication getConnection() {
        if (roboCom != null) {
            return roboCom;
        }
        XiaomiVacuumBindingConfiguration configuration = getConfigAs(XiaomiVacuumBindingConfiguration.class);
        String deviceId = configuration.deviceId;

        try {
            if (deviceId != null && deviceId.length() == 8 && tolkenCheckPass(configuration.token)) {
                logger.debug("Ping MiIO device {} at {}", deviceId, configuration.host);
                roboCom = new RoboCommunication(configuration.host, token, Utils.hexStringToByteArray(deviceId),
                        lastId);
                byte[] response = roboCom.comms(XiaomiVacuumBindingConstants.DISCOVER_STRING, configuration.host);
                if (response.length > 0) {
                    Message roboResponse = new Message(response);
                    logger.debug("Ping response from device {} at {}. Time stamp: {}, OH time {}, delta {}",
                            Utils.getHex(roboResponse.getDeviceId()), configuration.host, roboResponse.getTimestamp(),
                            LocalDateTime.now(), LocalDateTime.now().compareTo(roboResponse.getTimestamp()));
                    return roboCom;
                }
            } else {
                logger.debug("No device ID defined. Retrieving MiIO device ID");
                RoboCommunication idCom = new RoboCommunication(configuration.host, token, new byte[0], lastId);
                byte[] response = idCom.comms(XiaomiVacuumBindingConstants.DISCOVER_STRING, configuration.host);
                Message roboResponse = new Message(response);
                updateProperty(Thing.PROPERTY_SERIAL_NUMBER, Utils.getSpacedHex(roboResponse.getDeviceId()));
                Configuration config = editConfiguration();
                config.put(PROPERTY_DID, Utils.getHex(roboResponse.getDeviceId()));
                updateConfiguration(config);
                logger.debug("Using retrieved MiIO device ID {}", Utils.getHex(roboResponse.getDeviceId()));
                lastId = idCom.getId();
                idCom.close();
                if (tolkenCheckPass(configuration.token)) {
                    roboCom = new RoboCommunication(configuration.host, token, roboResponse.getDeviceId(), lastId);
                    return roboCom;
                }
            }
            logger.debug("Ping response from device {} at {} FAILED", configuration.deviceId, configuration.host);
            return null;
        } catch (IOException e) {
            logger.debug("Could not connect to {} at {}", getThing().getUID().toString(), configuration.host);
            disconnected(e.getMessage());
            return null;
        }
    }

    protected boolean initializeData() {
        this.roboCom = getConnection();
        if (roboCom == null) {
            updateStatus(ThingStatus.OFFLINE);
            return false;
        }
        network = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            try {
                return sendCommand(VacuumCommand.MIIO_INFO);
            } catch (Exception e) {
                logger.debug("Error during network status refresh: {}", e.getMessage(), e);
            }
            return null;
        });
        updateStatus(ThingStatus.ONLINE);
        return true;
    }

    private boolean getDeviceType() {
        roboCom = getConnection();
        String miIoData = sendCommand(VacuumCommand.MIIO_INFO);
        logger.debug("MiIO Device Data {}", miIoData);
        JsonObject result = ((JsonObject) parser.parse(miIoData)).getAsJsonObject("result").getAsJsonObject();
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_MODEL_ID, result.get("model").getAsString());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, result.get("fw_ver").getAsString());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, result.get("hw_ver").getAsString());
        updateProperties(properties);
        return true;
    }

    protected JsonObject getJsonResultHelper(String res) {
        try {
            JsonObject result;
            JsonObject vacuumResponse = (JsonObject) parser.parse(res);
            if (vacuumResponse.get("result").getClass().isAssignableFrom(JsonArray.class)) {
                result = vacuumResponse.getAsJsonArray("result").get(0).getAsJsonObject();
            } else {
                result = vacuumResponse.getAsJsonObject("result");
            }
            logger.debug("Response ID:     '{}'", vacuumResponse.get("id").getAsString());
            logger.debug("Response Result: '{}'", result);
            return result;
        } catch (JsonSyntaxException e) {
            logger.debug("Could not parse result from response: '{}'", res);
        } catch (NullPointerException e) {
            logger.debug("Empty response received.");
        }
        return null;
    }
}
