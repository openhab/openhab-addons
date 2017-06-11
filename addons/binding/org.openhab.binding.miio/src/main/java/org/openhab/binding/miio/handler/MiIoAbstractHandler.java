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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.miio.MiIoBindingConfiguration;
import org.openhab.binding.miio.internal.Message;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoCrypto;
import org.openhab.binding.miio.internal.MiIoCryptoException;
import org.openhab.binding.miio.internal.MiIoDevices;
import org.openhab.binding.miio.internal.MiIoMessageListener;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.Utils;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link MiIoAbstractHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public abstract class MiIoAbstractHandler extends BaseThingHandler implements MiIoMessageListener {
    protected static final int MAX_QUEUE = 5;

    protected ScheduledFuture<?> pollingJob;
    protected MiIoBindingConfiguration configuration;
    protected MiIoDevices miDevice = MiIoDevices.UNKNOWN;
    protected boolean isIdentified;

    protected JsonParser parser;
    protected byte[] token;

    protected MiIoAsyncCommunication miioCom;
    protected int lastId;

    protected Map<Integer, String> cmds = new ConcurrentHashMap<Integer, String>();
    protected ExpiringCache<String> network;
    protected static final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(5);
    private final Logger logger = LoggerFactory.getLogger(MiIoAbstractHandler.class);

    @NonNullByDefault
    public MiIoAbstractHandler(Thing thing) {
        super(thing);
        parser = new JsonParser();
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    @Override
    public void initialize() {
        logger.debug("Initializing Mi IO device handler '{}' with thingType {}", getThing().getUID(),
                getThing().getThingTypeUID());
        configuration = getConfigAs(MiIoBindingConfiguration.class);
        if (!tolkenCheckPass(configuration.token)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Token required. Configure token");
            return;
        }
        isIdentified = false;
        scheduler.schedule(this::initializeData, 1, TimeUnit.SECONDS);
        int pollingPeriod = configuration.refreshInterval;
        if (pollingPeriod > 0) {
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    updateData();
                } catch (Exception e) {
                    logger.debug("Unexpected error during refresh.", e);
                }
            }, 10, pollingPeriod, TimeUnit.SECONDS);
            logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
        } else {
            logger.debug("Polling job disabled. for '{}'", getThing().getUID());
            scheduler.schedule(this::updateData, 10, TimeUnit.SECONDS);
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    private boolean tolkenCheckPass(String tokenSting) {
        switch (tokenSting.length()) {
            case 16:
                token = tokenSting.getBytes();
                return true;
            case 32:
                if (!IGNORED_TOLKENS.contains(tokenSting)) {
                    token = Utils.hexStringToByteArray(tokenSting);
                    return true;
                }
                return false;
            case 96:
                try {
                    token = Utils
                            .hexStringToByteArray(MiIoCrypto.decryptTolken(Utils.hexStringToByteArray(tokenSting)));
                    logger.debug("IOS token decrypted to {}", Utils.getHex(token));
                } catch (MiIoCryptoException e) {
                    logger.warn("Could not decrypt token {}{}", tokenSting, e.getMessage());
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Xiaomi Mi IO handler '{}'", getThing().getUID());
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (miioCom != null) {
            lastId = miioCom.getId();
            miioCom.unregisterListener(this);
            miioCom.close();
            miioCom = null;
        }
    }

    protected int sendCommand(MiIoCommand command) {
        return sendCommand(command, "[]");
    }

    protected int sendCommand(MiIoCommand command, String params) {
        if (!hasConnection()) {
            return 0;
        }
        try {
            return getConnection().queueCommand(command, params);
        } catch (MiIoCryptoException | IOException e) {
            logger.debug("Command {} for {} failed (type: {}): {}", command.toString(), getThing().getUID(),
                    getThing().getThingTypeUID(), e.getLocalizedMessage());
        }
        return 0;
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
    protected int sendCommand(String commandString) {
        if (!hasConnection()) {
            return 0;
        }
        try {
            String command = commandString.trim();
            String param = "[]";
            int loc = command.indexOf("[");
            loc = (loc > 0 ? loc : command.indexOf("{"));
            if (loc > 0) {
                param = command.substring(loc).trim();
                command = command.substring(0, loc).trim();
            }
            return miioCom.queueCommand(command, param);
        } catch (MiIoCryptoException | IOException e) {
            disconnected(e.getMessage());
        }
        return 0;
    }

    protected boolean skipUpdate() {
        if (!hasConnection()) {
            logger.debug("Skipping periodic update for '{}'. No Connection", getThing().getUID().toString());
            return true;
        }
        if (getThing().getStatusInfo().getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)) {
            logger.debug("Skipping periodic update for '{}'. Thing Status {}", getThing().getUID().toString(),
                    getThing().getStatusInfo().getStatusDetail());
            try {
                miioCom.queueCommand(MiIoCommand.MIIO_INFO);
            } catch (MiIoCryptoException | IOException e) {
                // ignore
            }
            return true;
        }
        if (miioCom.getQueueLenght() > MAX_QUEUE) {
            logger.debug("Skipping periodic update for '{}'. {} elements in queue.", getThing().getUID().toString(),
                    miioCom.getQueueLenght());
            return true;
        }
        return false;
    }

    protected abstract void updateData();

    protected boolean updateNetwork(JsonObject networkData) {
        try {
            updateState(CHANNEL_SSID, new StringType(networkData.getAsJsonObject("ap").get("ssid").getAsString()));
            updateState(CHANNEL_BSSID, new StringType(networkData.getAsJsonObject("ap").get("bssid").getAsString()));
            if (networkData.getAsJsonObject("ap").get("rssi") != null) {
                updateState(CHANNEL_RSSI, new DecimalType(networkData.getAsJsonObject("ap").get("rssi").getAsLong()));
            } else if (networkData.getAsJsonObject("ap").get("wifi_rssi") != null) {
                updateState(CHANNEL_RSSI,
                        new DecimalType(networkData.getAsJsonObject("ap").get("wifi_rssi").getAsLong()));
            } else {
                logger.debug("No RSSI info in response");
            }
            updateState(CHANNEL_LIFE, new DecimalType(networkData.get("life").getAsLong()));
            return true;
        } catch (Exception e) {
            logger.debug("Could not parse network response: {}", networkData, e);
        }
        return false;
    }

    protected boolean hasConnection() {
        return getConnection() != null;
    }

    protected void disconnectedNoResponse() {
        disconnected("No Response from device");
    }

    protected void disconnected(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
        try {
            lastId = miioCom.getId();
            lastId += 10;
        } catch (Exception e) {
            // Ignore
        }
        // miioCom = null;
    }

    protected synchronized MiIoAsyncCommunication getConnection() {
        if (miioCom != null) {
            return miioCom;
        }
        String deviceId = configuration.deviceId;
        try {
            if (deviceId != null && deviceId.length() == 8 && tolkenCheckPass(configuration.token)) {
                logger.debug("Ping Mi IO device {} at {}", deviceId, configuration.host);
                miioCom = new MiIoAsyncCommunication(configuration.host, token, Utils.hexStringToByteArray(deviceId),
                        lastId, configuration.timeout);
                Message miIoResponse = miioCom.sendPing(configuration.host);
                ;
                if (miIoResponse != null) {
                    logger.debug("Ping response from device {} at {}. Time stamp: {}, OH time {}, delta {}",
                            Utils.getHex(miIoResponse.getDeviceId()), configuration.host, miIoResponse.getTimestamp(),
                            LocalDateTime.now(), miioCom.getTimeDelta());
                    miioCom.registerListener(this);
                    return miioCom;
                }
            } else {
                logger.debug("No device ID defined. Retrieving MiIO device ID");
                MiIoAsyncCommunication miioCom = new MiIoAsyncCommunication(configuration.host, token, new byte[0],
                        lastId, configuration.timeout);
                Message miIoResponse = miioCom.sendPing(configuration.host);
                if (miIoResponse != null) {
                    logger.debug("Ping response from device {} at {}. Time stamp: {}, OH time {}, delta {}",
                            Utils.getHex(miIoResponse.getDeviceId()), configuration.host, miIoResponse.getTimestamp(),
                            LocalDateTime.now(), miioCom.getTimeDelta());
                    deviceId = Utils.getHex(miIoResponse.getDeviceId());
                    logger.debug("Ping response from device {} at {}. Time stamp: {}, OH time {}, delta {}", deviceId,
                            configuration.host, miIoResponse.getTimestamp(), LocalDateTime.now(),
                            miioCom.getTimeDelta());
                    miioCom.setDeviceId(miIoResponse.getDeviceId());
                    logger.debug("Using retrieved MiIO device ID: {}", deviceId);
                    updateDeviceIdConfig(deviceId);
                    miioCom.registerListener(this);
                    return miioCom;
                }
            }
            logger.debug("Ping response from device {} at {} FAILED", configuration.deviceId, configuration.host);
            disconnectedNoResponse();
            return null;
        } catch (IOException e) {
            logger.debug("Could not connect to {} at {}", getThing().getUID().toString(), configuration.host);
            disconnected(e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("null")
    private void updateDeviceIdConfig(String deviceId) {
        if (deviceId != null) {
            updateProperty(Thing.PROPERTY_SERIAL_NUMBER, deviceId);
            Configuration config = editConfiguration();
            config.put(PROPERTY_DID, deviceId);
            updateConfiguration(config);
            configuration = getConfigAs(MiIoBindingConfiguration.class);
        } else {
            logger.debug("Could not update config with device ID: {}", deviceId);
        }
    }

    protected boolean initializeData() {
        initalizeNetworkCache();
        this.miioCom = getConnection();
        return true;
    }

    /**
     * Prepares the ExpiringCache for network data
     */
    protected void initalizeNetworkCache() {
        network = new ExpiringCache<String>(CACHE_EXPIRY * 120, () -> {
            try {
                int ret = sendCommand(MiIoCommand.MIIO_INFO);
                if (ret != 0) {
                    return "id:" + ret;
                }
            } catch (Exception e) {
                logger.debug("Error during network status refresh: {}", e.getMessage(), e);
            }
            return null;
        });
    }

    protected void refreshNetwork() {
        if (network == null) {
            initalizeNetworkCache();
        }
        network.getValue();
    }

    protected void defineDeviceType(JsonObject miioInfo) {
        updateProperties(miioInfo);
        isIdentified = updateThingType(miioInfo);
    }

    private void updateProperties(JsonObject miioInfo) {
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_MODEL_ID, miioInfo.get("model").getAsString());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, miioInfo.get("fw_ver").getAsString());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, miioInfo.get("hw_ver").getAsString());
        if (miioInfo.get("wifi_fw_ver") != null) {
            properties.put("wifiFirmware", miioInfo.get("wifi_fw_ver").getAsString());
        }
        if (miioInfo.get("mcu_fw_ver") != null) {
            properties.put("mcuFirmware", miioInfo.get("mcu_fw_ver").getAsString());
        }
        updateProperties(properties);
    }

    protected boolean updateThingType(JsonObject miioInfo) {
        String model = miioInfo.get("model").getAsString();
        miDevice = MiIoDevices.getType(model);
        if (configuration.model == null || configuration.model.isEmpty()) {
            Configuration config = editConfiguration();
            config.put(PROPERTY_MODEL, model);
            updateConfiguration(config);
            configuration = getConfigAs(MiIoBindingConfiguration.class);
        }
        if (!configuration.model.equals(model)) {
            logger.info("Mi IO Device model {} has model config: {}. Unexpected unless manual override", model,
                    configuration.model);
        }
        if (miDevice.getThingType().equals(getThing().getThingTypeUID())) {
            logger.info("Mi IO model {} identified as: {}. Matches thingtype {}", model, miDevice.toString(),
                    miDevice.getThingType().toString());
            return true;
        } else {
            if (getThing().getThingTypeUID().equals(THING_TYPE_MIIO)
                    || getThing().getThingTypeUID().equals(THING_TYPE_UNSUPPORTED)) {
                changeType(model);
            } else {
                logger.warn(
                        "Mi IO Device model {} identified as: {}, thingtype {}. Does not matches thingtype {}. Unexpected, unless unless manual override.",
                        miDevice.toString(), miDevice.getThingType(), getThing().getThingTypeUID().toString(),
                        miDevice.getThingType().toString());
                return true;
            }
        }
        return false;
    }

    /**
     * @param model
     */
    private void changeType(final String modelId) {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        scheduler.schedule(() -> {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withLabel(miDevice.getDescription());
            updateThing(thingBuilder.build());
            logger.info(
                    "Mi IO Device model {} identified as: {}. Does not matches thingtype {}. Changing thingtype to {}",
                    modelId, miDevice.toString(), getThing().getThingTypeUID().toString(),
                    miDevice.getThingType().toString());
            changeThingType(MiIoDevices.getType(modelId).getThingType(), getConfig());
        }, 10, TimeUnit.SECONDS);
    }

    @Override
    public void onStatusUpdated(ThingStatus status, ThingStatusDetail statusDetail) {
        updateStatus(status, statusDetail);
    }

    @Override
    public void onMessageReceived(MiIoSendCommand response) {
        logger.debug("Received response for {} type: {}, result: {}, fullresponse: {}", getThing().getUID().getId(),
                response.getCommand(), response.getResult(), response.getResponse());
        if (response.isError()) {
            logger.debug("Error received: {}", response.getResponse().get("error"));
            if (MiIoCommand.MIIO_INFO.equals(response.getCommand()) && network != null) {
                network.invalidateValue();
            }
            return;
        }
        try {
            switch (response.getCommand()) {
                case MIIO_INFO:
                    if (!isIdentified) {
                        defineDeviceType(response.getResult().getAsJsonObject());
                    }
                    updateNetwork(response.getResult().getAsJsonObject());
                    break;
                default:
                    break;
            }
            if (cmds.containsKey(response.getId())) {
                updateState(CHANNEL_COMMAND, new StringType(response.getResponse().toString()));
                cmds.remove(response.getId());
            }
        } catch (Exception e) {
            logger.debug("Error while handing message {}", response.getResponse(), e);
        }
    }
}
