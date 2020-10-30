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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.Message;
import org.openhab.binding.miio.internal.MiIoBindingConfiguration;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoCrypto;
import org.openhab.binding.miio.internal.MiIoCryptoException;
import org.openhab.binding.miio.internal.MiIoDevices;
import org.openhab.binding.miio.internal.MiIoInfoDTO;
import org.openhab.binding.miio.internal.MiIoMessageListener;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.Utils;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link MiIoAbstractHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public abstract class MiIoAbstractHandler extends BaseThingHandler implements MiIoMessageListener {
    protected static final int MAX_QUEUE = 5;
    protected static final Gson GSON = new GsonBuilder().create();

    protected @Nullable ScheduledFuture<?> pollingJob;
    protected MiIoDevices miDevice = MiIoDevices.UNKNOWN;
    protected boolean isIdentified;

    protected final JsonParser parser = new JsonParser();
    protected byte[] token = new byte[0];

    protected @Nullable MiIoBindingConfiguration configuration;
    protected @Nullable MiIoAsyncCommunication miioCom;
    protected int lastId;

    protected Map<Integer, String> cmds = new ConcurrentHashMap<>();
    protected Map<String, Object> deviceVariables = new HashMap<>();
    protected final ExpiringCache<String> network = new ExpiringCache<>(CACHE_EXPIRY_NETWORK, () -> {
        int ret = sendCommand(MiIoCommand.MIIO_INFO);
        if (ret != 0) {
            return "id:" + ret;
        }
        return "failed";
    });;
    protected static final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(5);
    protected static final long CACHE_EXPIRY_NETWORK = TimeUnit.SECONDS.toMillis(60);

    private final Logger logger = LoggerFactory.getLogger(MiIoAbstractHandler.class);
    protected MiIoDatabaseWatchService miIoDatabaseWatchService;

    public MiIoAbstractHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService) {
        super(thing);
        this.miIoDatabaseWatchService = miIoDatabaseWatchService;
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    @Override
    public void initialize() {
        logger.debug("Initializing Mi IO device handler '{}' with thingType {}", getThing().getUID(),
                getThing().getThingTypeUID());
        final MiIoBindingConfiguration configuration = getConfigAs(MiIoBindingConfiguration.class);
        this.configuration = configuration;
        if (configuration.host == null || configuration.host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP address required. Configure IP address");
            return;
        }
        if (!tokenCheckPass(configuration.token)) {
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

    private boolean tokenCheckPass(@Nullable String tokenSting) {
        if (tokenSting == null) {
            return false;
        }
        switch (tokenSting.length()) {
            case 16:
                token = tokenSting.getBytes();
                return true;
            case 32:
                if (!IGNORED_TOKENS.contains(tokenSting)) {
                    token = Utils.hexStringToByteArray(tokenSting);
                    return true;
                }
                return false;
            case 96:
                try {
                    token = Utils.hexStringToByteArray(MiIoCrypto.decryptToken(Utils.hexStringToByteArray(tokenSting)));
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
        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
        final @Nullable MiIoAsyncCommunication miioCom = this.miioCom;
        if (miioCom != null) {
            lastId = miioCom.getId();
            miioCom.unregisterListener(this);
            miioCom.close();
            this.miioCom = null;
        }
    }

    protected int sendCommand(MiIoCommand command) {
        return sendCommand(command, "[]");
    }

    protected int sendCommand(MiIoCommand command, String params) {
        try {
            final MiIoAsyncCommunication connection = getConnection();
            return (connection != null) ? connection.queueCommand(command, params) : 0;
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
     * @param commandString command to be executed
     * @return vacuum response
     */
    protected int sendCommand(String commandString) {
        final MiIoAsyncCommunication connection = getConnection();
        try {
            String command = commandString.trim();
            String param = "[]";
            int sb = command.indexOf("[");
            int cb = command.indexOf("{");
            logger.debug("locs {}, {}", sb, cb);
            if (Math.max(sb, cb) > 0) {
                int loc = (Math.min(sb, cb) > 0 ? Math.min(sb, cb) : Math.max(sb, cb));
                param = command.substring(loc).trim();
                command = command.substring(0, loc).trim();
            }
            return (connection != null) ? connection.queueCommand(command, param) : 0;
        } catch (MiIoCryptoException | IOException e) {
            disconnected(e.getMessage());
        }
        return 0;
    }

    protected boolean skipUpdate() {
        final MiIoAsyncCommunication miioCom = this.miioCom;
        if (!hasConnection() || miioCom == null) {
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
        if (miioCom.getQueueLength() > MAX_QUEUE) {
            logger.debug("Skipping periodic update for '{}'. {} elements in queue.", getThing().getUID().toString(),
                    miioCom.getQueueLength());
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
        final MiIoAsyncCommunication miioCom = this.miioCom;
        if (miioCom != null) {
            lastId = miioCom.getId();
            lastId += 10;
        }
    }

    protected synchronized @Nullable MiIoAsyncCommunication getConnection() {
        if (miioCom != null) {
            return miioCom;
        }
        final MiIoBindingConfiguration configuration = getConfigAs(MiIoBindingConfiguration.class);
        if (configuration.host == null || configuration.host.isEmpty()) {
            return null;
        }
        @Nullable
        String deviceId = configuration.deviceId;
        try {
            if (deviceId != null && deviceId.length() == 8 && tokenCheckPass(configuration.token)) {
                logger.debug("Ping Mi device {} at {}", deviceId, configuration.host);
                final MiIoAsyncCommunication miioCom = new MiIoAsyncCommunication(configuration.host, token,
                        Utils.hexStringToByteArray(deviceId), lastId, configuration.timeout);
                Message miIoResponse = miioCom.sendPing(configuration.host);
                if (miIoResponse != null) {
                    logger.debug("Ping response from device {} at {}. Time stamp: {}, OH time {}, delta {}",
                            Utils.getHex(miIoResponse.getDeviceId()), configuration.host, miIoResponse.getTimestamp(),
                            LocalDateTime.now(), miioCom.getTimeDelta());
                    miioCom.registerListener(this);
                    this.miioCom = miioCom;
                    return miioCom;
                } else {
                    miioCom.close();
                }
            } else {
                logger.debug("No device ID defined. Retrieving Mi device ID");
                final MiIoAsyncCommunication miioCom = new MiIoAsyncCommunication(configuration.host, token,
                        new byte[0], lastId, configuration.timeout);
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
                    logger.debug("Using retrieved Mi device ID: {}", deviceId);
                    updateDeviceIdConfig(deviceId);
                    miioCom.registerListener(this);
                    this.miioCom = miioCom;
                    return miioCom;
                } else {
                    miioCom.close();
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

    private void updateDeviceIdConfig(String deviceId) {
        if (!deviceId.isEmpty()) {
            updateProperty(Thing.PROPERTY_SERIAL_NUMBER, deviceId);
            Configuration config = editConfiguration();
            config.put(PROPERTY_DID, deviceId);
            updateConfiguration(config);
        } else {
            logger.debug("Could not update config with device ID: {}", deviceId);
        }
    }

    protected boolean initializeData() {
        this.miioCom = getConnection();
        return true;
    }

    protected void refreshNetwork() {
        network.getValue();
    }

    protected void defineDeviceType(JsonObject miioInfo) {
        updateProperties(miioInfo);
        isIdentified = updateThingType(miioInfo);
    }

    private void updateProperties(JsonObject miioInfo) {
        final MiIoInfoDTO info = GSON.fromJson(miioInfo, MiIoInfoDTO.class);
        Map<String, String> properties = editProperties();
        if (info.model != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, info.model);
        }
        if (info.fwVer != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, info.fwVer);
        }
        if (info.hwVer != null) {
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, info.hwVer);
        }
        if (info.wifiFwVer != null) {
            properties.put("wifiFirmware", info.wifiFwVer);
        }
        if (info.mcuFwVer != null) {
            properties.put("mcuFirmware", info.mcuFwVer);
        }
        deviceVariables.putAll(properties);
        updateProperties(properties);
    }

    protected boolean updateThingType(JsonObject miioInfo) {
        MiIoBindingConfiguration configuration = getConfigAs(MiIoBindingConfiguration.class);
        String model = miioInfo.get("model").getAsString();
        miDevice = MiIoDevices.getType(model);
        if (configuration.model == null || configuration.model.isEmpty()) {
            Configuration config = editConfiguration();
            config.put(PROPERTY_MODEL, model);
            updateConfiguration(config);
            configuration = getConfigAs(MiIoBindingConfiguration.class);
        }
        if (!configuration.model.equals(model)) {
            logger.info("Mi Device model {} has model config: {}. Unexpected unless manual override", model,
                    configuration.model);
        }
        if (miDevice.getThingType().equals(getThing().getThingTypeUID())
                && !(miDevice.getThingType().equals(THING_TYPE_UNSUPPORTED)
                        && miIoDatabaseWatchService.getDatabaseUrl(model) != null)) {
            logger.debug("Mi Device model {} identified as: {}. Matches thingtype {}", model, miDevice.toString(),
                    miDevice.getThingType().toString());
            return true;
        } else {
            if (getThing().getThingTypeUID().equals(THING_TYPE_MIIO)
                    || getThing().getThingTypeUID().equals(THING_TYPE_UNSUPPORTED)) {
                changeType(model);
            } else {
                logger.info(
                        "Mi Device model {} identified as: {}, thingtype {}. Does not matches thingtype {}. Unexpected, unless manual override.",
                        miDevice.toString(), miDevice.getThingType(), getThing().getThingTypeUID().toString(),
                        miDevice.getThingType().toString());
                return true;
            }
        }
        return false;
    }

    /**
     * Changes the {@link org.openhab.core.thing.type.ThingType} to the right type once it is retrieved from
     * the device.
     *
     * @param modelId String with the model id
     */
    private void changeType(final String modelId) {
        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
        scheduler.schedule(() -> {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withLabel(miDevice.getDescription());
            updateThing(thingBuilder.build());
            logger.info("Mi Device model {} identified as: {}. Does not match thingtype {}. Changing thingtype to {}",
                    modelId, miDevice.toString(), getThing().getThingTypeUID().toString(),
                    miDevice.getThingType().toString());
            ThingTypeUID thingTypeUID = MiIoDevices.getType(modelId).getThingType();
            if (thingTypeUID.equals(THING_TYPE_UNSUPPORTED)
                    && miIoDatabaseWatchService.getDatabaseUrl(modelId) != null) {
                thingTypeUID = THING_TYPE_BASIC;
            }
            changeThingType(thingTypeUID, getConfig());
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
            if (MiIoCommand.MIIO_INFO.equals(response.getCommand())) {
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
