/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.miio.internal.MiIoBindingConstants.CHANNEL_COMMAND;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.miio.internal.MiIoBindingConstants;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoCryptoException;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.Utils;
import org.openhab.binding.miio.internal.basic.CommandParameterType;
import org.openhab.binding.miio.internal.basic.Conversions;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MiIoBasicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoBasicHandler extends MiIoAbstractHandler {
    private static final int MAX_PROPERTIES = 5;
    private final Logger logger = LoggerFactory.getLogger(MiIoBasicHandler.class);
    private boolean hasChannelStructure;

    private final ExpiringCache<Boolean> updateDataCache = new ExpiringCache<>(CACHE_EXPIRY, () -> {
        scheduler.schedule(this::updateData, 0, TimeUnit.SECONDS);
        return true;
    });

    List<MiIoBasicChannel> refreshList = new ArrayList<MiIoBasicChannel>();

    MiIoBasicDevice miioDevice;
    private Map<String, MiIoDeviceAction> actions;

    @NonNullByDefault
    public MiIoBasicHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        hasChannelStructure = false;
        isIdentified = false;
        refreshList = new ArrayList<MiIoBasicChannel>();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Xiaomi Mi IO Basic handler '{}'", getThing().getUID());
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (miioCom != null) {
            lastId = miioCom.getId();
            miioCom.close();
            miioCom = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            if (updateDataCache.isExpired()) {
                logger.debug("Refreshing {}", channelUID);
                updateDataCache.getValue();
            } else {
                logger.debug("Refresh {} skipped. Already refreshing", channelUID);
            }
            return;
        }
        if (channelUID.getId().equals(CHANNEL_COMMAND)) {
            cmds.put(sendCommand(command.toString()), command.toString());
        }
        logger.debug("Locating action for channel {}: {}", channelUID.getId(), command);
        if (actions != null) {
            if (actions.containsKey(channelUID.getId())) {
                String preCommandPara1 = actions.get(channelUID.getId()).getPreCommandParameter1();
                preCommandPara1 = ((preCommandPara1 != null && !preCommandPara1.isEmpty()) ? preCommandPara1 + ","
                        : "");
                String para1 = actions.get(channelUID.getId()).getParameter1();
                String para2 = actions.get(channelUID.getId()).getParameter2();
                String para3 = actions.get(channelUID.getId()).getParameter3();
                String para = "" + (para1 != null ? "," + para1 : "") + (para2 != null ? "," + para2 : "")
                        + (para3 != null ? "," + para3 : "");
                String cmd = actions.get(channelUID.getId()).getCommand();
                CommandParameterType paramType = actions.get(channelUID.getId()).getparameterType();
                if (paramType == CommandParameterType.EMPTY) {
                    cmd = cmd + "[]";
                } else if (paramType == CommandParameterType.NONE) {
                    logger.trace("NONE command type");
                } else if (paramType == CommandParameterType.COLOR) {
                    if (command instanceof HSBType) {
                        HSBType hsb = (HSBType) command;
                        Color color = Color.getHSBColor(hsb.getHue().floatValue() / 360,
                                hsb.getSaturation().floatValue() / 100, hsb.getBrightness().floatValue() / 100);
                        cmd = cmd + "[" + preCommandPara1
                                + ((color.getRed() * 65536) + (color.getGreen() * 256) + color.getBlue()) + para + "]";
                    } else if (command instanceof DecimalType) {
                        // actually brightness is being set instead of a color
                        cmd = "set_bright" + "[" + command.toString().toLowerCase() + "]";
                    } else {
                        logger.debug("Unsupported command for COLOR: {}", command);
                    }

                } else if (command instanceof OnOffType) {
                    if (paramType == CommandParameterType.ONOFF) {
                        cmd = cmd + "[" + preCommandPara1 + "\"" + command.toString().toLowerCase() + "\"" + para + "]";
                    } else if (paramType == CommandParameterType.ONOFFPARA) {
                        cmd = cmd.replace("*", command.toString().toLowerCase()) + "[]";
                    } else {
                        cmd = cmd + "[]";
                    }
                } else if (command instanceof StringType) {
                    if (paramType == CommandParameterType.STRING) {
                        cmd = cmd + "[" + preCommandPara1 + "\"" + command.toString() + "\"" + para + "]";
                    } else if (paramType == CommandParameterType.CUSTOMSTRING) {
                        cmd = cmd + "[" + preCommandPara1 + "\"" + command.toString() + para + "]";
                    }
                } else if (command instanceof DecimalType) {
                    cmd = cmd + "[" + preCommandPara1 + command.toString().toLowerCase() + para + "]";
                }
                logger.debug("Sending command {}", cmd);
                sendCommand(cmd);
            } else {
                logger.debug("Channel Id {} not in mapping.", channelUID.getId());
                for (String a : actions.keySet()) {
                    logger.trace("Available entries: {} : {}", a, actions.get(a).getCommand());
                }
            }
            updateDataCache.invalidateValue();
            updateData();
        } else

        {
            logger.debug("Actions not loaded yet");
        }
    }

    @Override
    protected synchronized void updateData() {
        logger.debug("Periodic update for '{}' ({})", getThing().getUID().toString(), getThing().getThingTypeUID());
        try {
            if (!hasConnection() || skipUpdate()) {
                return;
            }
            try {
                miioCom.startReceiver();
                miioCom.sendPing(configuration.host);
            } catch (Exception e) {
                // ignore
            }
            checkChannelStructure();
            if (!isIdentified) {
                miioCom.queueCommand(MiIoCommand.MIIO_INFO);
            }
            if (miioDevice != null) {
                refreshProperties(miioDevice);
                refreshNetwork();
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}': ", getThing().getUID().toString(), e);
        }
    }

    private boolean refreshProperties(MiIoBasicDevice device) {
        JsonArray getPropString = new JsonArray();
        for (MiIoBasicChannel miChannel : refreshList) {
            getPropString.add(miChannel.getProperty());
            if (getPropString.size() >= MAX_PROPERTIES) {
                sendRefreshProperties(getPropString);
                getPropString = new JsonArray();
            }
        }
        sendRefreshProperties(getPropString);
        return true;
    }

    private void sendRefreshProperties(JsonArray getPropString) {
        try {
            miioCom.queueCommand(MiIoCommand.GET_PROPERTY, getPropString.toString());
        } catch (MiIoCryptoException | IOException e) {
            logger.debug("Send refresh failed {}", e.getMessage(), e);
        }
    }

    @Override
    protected boolean initializeData() {
        miioCom = new MiIoAsyncCommunication(configuration.host, token,
                Utils.hexStringToByteArray(configuration.deviceId), lastId, configuration.timeout);
        miioCom.registerListener(this);
        try {
            miioCom.sendPing(configuration.host);
        } catch (Exception e) {
            logger.debug("ping {} failed", configuration.host);
        }
        return true;
    }

    /**
     * Checks if the channel structure has been build already based on the model data. If not build it.
     */
    private void checkChannelStructure() {
        if (!hasChannelStructure) {
            if (configuration.model == null || configuration.model.isEmpty()) {
                logger.debug("Model needs to be determined");
            } else {
                hasChannelStructure = buildChannelStructure(configuration.model);
            }
        }
        if (hasChannelStructure) {
            refreshList = new ArrayList<MiIoBasicChannel>();
            for (MiIoBasicChannel miChannel : miioDevice.getDevice().getChannels()) {
                if (miChannel.getRefresh()) {
                    refreshList.add(miChannel);
                }

            }

        }
    }

    private URL findDatabaseEntry(String deviceName) {
        URL fn;
        try {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            fn = bundle.getEntry(MiIoBindingConstants.DATABASE_PATH + deviceName + ".json");
            if (fn != null) {
                logger.trace("bundle: {}, {}", bundle, fn.getFile());
                return fn;
            }
            for (URL db : Collections.list(bundle.findEntries(MiIoBindingConstants.DATABASE_PATH, "*.json", false))) {
                try {
                    JsonObject deviceMapping = Utils.convertFileToJSON(db);
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    MiIoBasicDevice devdb = gson.fromJson(deviceMapping, MiIoBasicDevice.class);
                    for (String id : devdb.getDevice().getId()) {
                        if (deviceName.equals(id)) {
                            return db;
                        }
                    }
                } catch (Exception e) {
                    // not relevant
                    logger.debug("Error while searching for {} in database '{}': {}", deviceName, db, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.debug("Error while searching for {} in database: {}", deviceName, e.getMessage());
        }
        return null;
    }

    private boolean buildChannelStructure(String deviceName) {
        logger.debug("Building Channel Structure for {} - Model: {}", getThing().getUID().toString(), deviceName);
        URL fn = findDatabaseEntry(deviceName);
        if (fn == null) {
            logger.warn("Database entry for model '{}' cannot be found.", deviceName);
            return false;
        }
        try {
            JsonObject deviceMapping = Utils.convertFileToJSON(fn);
            logger.debug("Using device database: {} for device {}", fn.getFile(), deviceName);
            Gson gson = new GsonBuilder().serializeNulls().create();
            miioDevice = gson.fromJson(deviceMapping, MiIoBasicDevice.class);

            for (Channel ch : getThing().getChannels()) {
                logger.debug("Current thing channels {}, type: {}", ch.getUID(), ch.getChannelTypeUID());
            }
            ThingBuilder thingBuilder = editThing();
            int channelsAdded = 0;

            // make a map of the actions
            actions = new HashMap<String, MiIoDeviceAction>();

            for (MiIoBasicChannel miChannel : miioDevice.getDevice().getChannels()) {
                logger.debug("properties {}", miChannel);
                for (MiIoDeviceAction action : miChannel.getActions()) {
                    actions.put(miChannel.getChannel(), action);
                }
                if (miChannel.getType() != null) {
                    channelsAdded += addChannel(thingBuilder, miChannel.getChannel(), miChannel.getChannelType(),
                            miChannel.getType(), miChannel.getFriendlyName()) ? 1 : 0;
                }
            }
            // only update if channels were added/removed
            if (channelsAdded > 0) {
                logger.debug("Current thing channels added: {}", channelsAdded);
                updateThing(thingBuilder.build());
            }
            return true;
        } catch (JsonIOException e) {
            logger.warn("Error reading database Json", e);
        } catch (JsonSyntaxException e) {
            logger.warn("Error reading database Json", e);
        } catch (IOException e) {
            logger.warn("Error reading database file", e);
        } catch (Exception e) {
            logger.warn("Error creating channel structure", e);
        }
        return false;
    }

    private boolean addChannel(ThingBuilder thingBuilder, String channel, String channelType, String datatype,
            String friendlyName) {
        if (channel == null || channel.isEmpty() || datatype == null || datatype.isEmpty()) {
            logger.info("Channel '{}', UID '{}' cannot be added incorrectly configured database. ", channel,
                    getThing().getUID());
            return false;
        }
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channel);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(channelType);

        // TODO: Need to understand if this harms anything. If yes, channel only to be added when not there already.
        // current way allows to have no issues when channels are changing.
        if (getThing().getChannel(channel) != null) {
            logger.info("Channel '{}' for thing {} already exist... removing", channel, getThing().getUID());
            thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), channel));
        }

        Channel newChannel = ChannelBuilder.create(channelUID, datatype).withType(channelTypeUID)
                .withLabel(friendlyName).build();
        thingBuilder.withChannel(newChannel);
        return true;
    }

    private MiIoBasicChannel getChannel(String parameter) {
        for (MiIoBasicChannel refreshEntry : refreshList) {
            if (refreshEntry.getProperty().equals(parameter)) {
                return refreshEntry;
            }
        }
        logger.trace("Did not find channel for {} in {}", parameter, refreshList);
        return null;
    }

    void updateProperties(MiIoSendCommand response) {
        JsonArray res = response.getResult().getAsJsonArray();
        JsonArray para = parser.parse(response.getCommandString()).getAsJsonObject().get("params").getAsJsonArray();
        if (res.size() != para.size()) {
            logger.debug("Unexpected size different. Request size {},  response size {}. (Req: {}, Resp:{})",
                    para.size(), res.size(), para.toString(), res.toString());
        }
        for (int i = 0; i < para.size(); i++) {
            JsonElement val = res.get(i);
            if (val.isJsonNull()) {
                logger.debug("Property '{}' returned null (is it supported?).", para.get(i).getAsString());
                continue;
            }
            MiIoBasicChannel basicChannel = getChannel(para.get(i).getAsString());
            if (basicChannel != null) {
                if (basicChannel.getTransfortmation() != null) {
                    JsonElement transformed = Conversions.execute(basicChannel.getTransfortmation(), val);
                    logger.debug("Transformed with '{}': {} {} -> {} ", basicChannel.getTransfortmation(),
                            basicChannel.getFriendlyName(), val, transformed);
                    val = transformed;
                }
                try {
                    if (basicChannel.getType().equals("Number")) {
                        updateState(basicChannel.getChannel(), new DecimalType(val.getAsBigDecimal()));
                    }
                    if (basicChannel.getType().equals("String")) {
                        updateState(basicChannel.getChannel(), new StringType(val.getAsString()));
                    }
                    if (basicChannel.getType().equals("Switch")) {
                        updateState(basicChannel.getChannel(),
                                val.getAsString().toLowerCase().equals("on")
                                        || val.getAsString().toLowerCase().equals("true") ? OnOffType.ON
                                                : OnOffType.OFF);
                    }
                    if (basicChannel.getType().equals("Color")) {
                        Color rgb = new Color(val.getAsInt());
                        HSBType hsb = HSBType.fromRGB(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
                        updateState(basicChannel.getChannel(), hsb);
                    }
                } catch (Exception e) {
                    logger.debug("Error updating {} property {} with '{}' : {}", getThing().getUID().getAsString(),
                            basicChannel.getChannel(), val.getAsString(), e.getMessage());
                    logger.trace("Property update error detail:", e);
                }
            } else {
                logger.debug("Channel not found for {}", para.get(i).getAsString());
            }
        }
    }

    @Override
    public void onMessageReceived(MiIoSendCommand response) {
        super.onMessageReceived(response);
        if (response.isError()) {
            return;
        }
        try {
            switch (response.getCommand()) {
                case MIIO_INFO:
                    break;
                case GET_PROPERTY:
                    if (response.getResult().isJsonArray()) {
                        updateProperties(response);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.debug("Error while handing message {}", response.getResponse(), e);
        }
    }
}
