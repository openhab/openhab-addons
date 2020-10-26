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

import static org.openhab.binding.miio.internal.MiIoBindingConstants.CHANNEL_COMMAND;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.MiIoBindingConfiguration;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoCryptoException;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.Utils;
import org.openhab.binding.miio.internal.basic.ActionConditions;
import org.openhab.binding.miio.internal.basic.CommandParameterType;
import org.openhab.binding.miio.internal.basic.Conversions;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.openhab.binding.miio.internal.basic.MiIoDeviceActionCondition;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MiIoBasicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoBasicHandler extends MiIoAbstractHandler {
    private final Logger logger = LoggerFactory.getLogger(MiIoBasicHandler.class);
    private boolean hasChannelStructure;

    private final ExpiringCache<Boolean> updateDataCache = new ExpiringCache<>(CACHE_EXPIRY, () -> {
        scheduler.schedule(this::updateData, 0, TimeUnit.SECONDS);
        return true;
    });

    List<MiIoBasicChannel> refreshList = new ArrayList<>();
    private Map<String, MiIoBasicChannel> refreshListCustomCommands = new HashMap<>();

    private @Nullable MiIoBasicDevice miioDevice;
    private Map<ChannelUID, MiIoBasicChannel> actions = new HashMap<>();
    private ChannelTypeRegistry channelTypeRegistry;

    public MiIoBasicHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService,
            ChannelTypeRegistry channelTypeRegistry) {
        super(thing, miIoDatabaseWatchService);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public void initialize() {
        super.initialize();
        hasChannelStructure = false;
        isIdentified = false;
        refreshList = new ArrayList<>();
        refreshListCustomCommands = new HashMap<>();
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
            return;
        }
        logger.debug("Locating action for channel '{}': '{}'", channelUID.getId(), command);
        if (!actions.isEmpty()) {
            if (actions.containsKey(channelUID)) {
                int valuePos = 0;
                MiIoBasicChannel miIoBasicChannel = actions.get(channelUID);
                for (MiIoDeviceAction action : miIoBasicChannel.getActions()) {
                    @Nullable
                    JsonElement value = null;
                    JsonArray parameters = action.getParameters().deepCopy();
                    for (int i = 0; i < action.getParameters().size(); i++) {
                        JsonElement p = action.getParameters().get(i);
                        if (p.isJsonPrimitive() && p.getAsString().toLowerCase().contains("$value$")) {
                            valuePos = i;
                            break;
                        }
                    }
                    String cmd = action.getCommand();
                    CommandParameterType paramType = action.getparameterType();
                    if (paramType == CommandParameterType.COLOR) {
                        if (command instanceof HSBType) {
                            HSBType hsb = (HSBType) command;
                            Color color = Color.getHSBColor(hsb.getHue().floatValue() / 360,
                                    hsb.getSaturation().floatValue() / 100, hsb.getBrightness().floatValue() / 100);
                            value = new JsonPrimitive(
                                    (color.getRed() << 16) + (color.getGreen() << 8) + color.getBlue());
                        } else if (command instanceof DecimalType) {
                            // actually brightness is being set instead of a color
                            value = new JsonPrimitive(((DecimalType) command).toBigDecimal());
                        } else if (command instanceof OnOffType) {
                            value = new JsonPrimitive(command == OnOffType.ON ? 100 : 0);
                        } else {
                            logger.debug("Unsupported command for COLOR: {}", command);
                        }
                    } else if (command instanceof OnOffType) {
                        if (paramType == CommandParameterType.ONOFF) {
                            value = new JsonPrimitive(command == OnOffType.ON ? "on" : "off");
                        } else if (paramType == CommandParameterType.ONOFFPARA) {
                            cmd = cmd.replace("*", command == OnOffType.ON ? "on" : "off");
                            value = new JsonArray();
                        } else if (paramType == CommandParameterType.ONOFFBOOL) {
                            boolean boolCommand = command == OnOffType.ON;
                            value = new JsonPrimitive(boolCommand);
                        } else if (paramType == CommandParameterType.ONOFFBOOLSTRING) {
                            value = new JsonPrimitive(command == OnOffType.ON ? "true" : "false");
                        }
                    } else if (command instanceof DecimalType) {
                        value = new JsonPrimitive(((DecimalType) command).toBigDecimal());
                    } else if (command instanceof StringType) {
                        if (paramType == CommandParameterType.STRING) {
                            value = new JsonPrimitive(command.toString().toLowerCase());
                        } else if (paramType == CommandParameterType.CUSTOMSTRING) {
                            value = new JsonPrimitive(parameters.get(valuePos).getAsString().replace("$value",
                                    command.toString().toLowerCase()));
                        }
                    } else {
                        value = new JsonPrimitive(command.toString().toLowerCase());
                    }
                    if (paramType == CommandParameterType.EMPTY) {
                        value = new JsonArray();
                    }
                    final MiIoDeviceActionCondition miIoDeviceActionCondition = action.getCondition();
                    if (miIoDeviceActionCondition != null) {
                        value = ActionConditions.executeAction(miIoDeviceActionCondition, deviceVariables, value,
                                command);
                    }
                    // Check for miot channel
                    if (value != null) {
                        if (action.isMiOtAction()) {
                            value = miotActionTransform(action, miIoBasicChannel, value);
                        } else if (miIoBasicChannel.isMiOt()) {
                            value = miotTransform(miIoBasicChannel, value);
                        }
                    }
                    if (paramType != CommandParameterType.NONE && paramType != CommandParameterType.ONOFFPARA
                            && value != null) {
                        if (parameters.size() > 0) {
                            parameters.set(valuePos, value);
                        } else {
                            parameters.add(value);
                        }
                    }
                    cmd = cmd + parameters.toString();
                    if (value != null) {
                        logger.debug("Sending command {}", cmd);
                        sendCommand(cmd);
                    } else {
                        if (miIoDeviceActionCondition != null) {
                            logger.debug("Conditional command {} not send, condition '{}' not met", cmd,
                                    miIoDeviceActionCondition.getName());
                        } else {
                            logger.debug("Command not send. Value null");
                        }
                    }
                }
            } else {
                logger.debug("Channel Id {} not in mapping.", channelUID.getId());
                if (logger.isTraceEnabled()) {
                    for (ChannelUID a : actions.keySet()) {
                        logger.trace("Available entries: {} : {}", a, actions.get(a).getFriendlyName());
                    }
                }
            }
            updateDataCache.invalidateValue();
            scheduler.schedule(() -> {
                updateData();
            }, 3000, TimeUnit.MILLISECONDS);
        } else {
            logger.debug("Actions not loaded yet");
        }
    }

    private @Nullable JsonElement miotTransform(MiIoBasicChannel miIoBasicChannel, @Nullable JsonElement value) {
        JsonObject json = new JsonObject();
        json.addProperty("did", miIoBasicChannel.getChannel());
        json.addProperty("siid", miIoBasicChannel.getSiid());
        json.addProperty("piid", miIoBasicChannel.getPiid());
        json.add("value", value);
        return json;
    }

    private @Nullable JsonElement miotActionTransform(MiIoDeviceAction action, MiIoBasicChannel miIoBasicChannel,
            @Nullable JsonElement value) {
        JsonObject json = new JsonObject();
        json.addProperty("did", miIoBasicChannel.getChannel());
        json.addProperty("siid", action.getSiid());
        json.addProperty("aiid", action.getAiid());
        if (value != null) {
            json.add("in", value);
        }
        return json;
    }

    @Override
    protected synchronized void updateData() {
        logger.debug("Periodic update for '{}' ({})", getThing().getUID().toString(), getThing().getThingTypeUID());
        final MiIoAsyncCommunication miioCom = getConnection();
        try {
            if (!hasConnection() || skipUpdate() || miioCom == null) {
                return;
            }
            checkChannelStructure();
            if (!isIdentified) {
                miioCom.queueCommand(MiIoCommand.MIIO_INFO);
            }
            final MiIoBasicDevice midevice = miioDevice;
            if (midevice != null) {
                refreshProperties(midevice);
                refreshCustomProperties(midevice);
                refreshNetwork();
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}': ", getThing().getUID().toString(), e);
        }
    }

    private void refreshCustomProperties(MiIoBasicDevice midevice) {
        for (MiIoBasicChannel miChannel : refreshListCustomCommands.values()) {
            sendCommand(miChannel.getChannelCustomRefreshCommand());
        }
    }

    private boolean refreshProperties(MiIoBasicDevice device) {
        MiIoCommand command = MiIoCommand.getCommand(device.getDevice().getPropertyMethod());
        int maxProperties = device.getDevice().getMaxProperties();
        JsonArray getPropString = new JsonArray();
        for (MiIoBasicChannel miChannel : refreshList) {
            JsonElement property;
            if (miChannel.isMiOt()) {
                JsonObject json = new JsonObject();
                json.addProperty("did", miChannel.getProperty());
                json.addProperty("siid", miChannel.getSiid());
                json.addProperty("piid", miChannel.getPiid());
                property = json;
            } else {
                property = new JsonPrimitive(miChannel.getProperty());
            }
            getPropString.add(property);
            if (getPropString.size() >= maxProperties) {
                sendRefreshProperties(command, getPropString);
                getPropString = new JsonArray();
            }
        }
        if (getPropString.size() > 0) {
            sendRefreshProperties(command, getPropString);
        }
        return true;
    }

    private void sendRefreshProperties(MiIoCommand command, JsonArray getPropString) {
        try {
            final MiIoAsyncCommunication miioCom = this.miioCom;
            if (miioCom != null) {
                miioCom.queueCommand(command, getPropString.toString());
            }
        } catch (MiIoCryptoException | IOException e) {
            logger.debug("Send refresh failed {}", e.getMessage(), e);
        }
    }

    /**
     * Checks if the channel structure has been build already based on the model data. If not build it.
     */
    private void checkChannelStructure() {
        final MiIoBindingConfiguration configuration = this.configuration;
        if (configuration == null) {
            return;
        }
        if (!hasChannelStructure) {
            if (configuration.model == null || configuration.model.isEmpty()) {
                logger.debug("Model needs to be determined");
                isIdentified = false;
            } else {
                hasChannelStructure = buildChannelStructure(configuration.model);
            }
        }
        if (hasChannelStructure) {
            refreshList = new ArrayList<>();
            final MiIoBasicDevice miioDevice = this.miioDevice;
            if (miioDevice != null) {
                for (MiIoBasicChannel miChannel : miioDevice.getDevice().getChannels()) {
                    if (miChannel.getRefresh()) {
                        if (miChannel.getChannelCustomRefreshCommand().isBlank()) {
                            refreshList.add(miChannel);
                        } else {
                            String i = miChannel.getChannelCustomRefreshCommand().split("\\[")[0];
                            refreshListCustomCommands.put(i.trim(), miChannel);
                        }
                    }
                }
            }

        }
    }

    private boolean buildChannelStructure(String deviceName) {
        logger.debug("Building Channel Structure for {} - Model: {}", getThing().getUID().toString(), deviceName);
        URL fn = miIoDatabaseWatchService.getDatabaseUrl(deviceName);
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
            actions = new HashMap<>();
            final MiIoBasicDevice device = this.miioDevice;
            if (device != null) {
                for (MiIoBasicChannel miChannel : device.getDevice().getChannels()) {
                    logger.debug("properties {}", miChannel);
                    if (!miChannel.getType().isEmpty()) {
                        ChannelUID channelUID = addChannel(thingBuilder, miChannel.getChannel(),
                                miChannel.getChannelType(), miChannel.getType(), miChannel.getFriendlyName());
                        if (channelUID != null) {
                            actions.put(channelUID, miChannel);
                            channelsAdded++;
                        } else {
                            logger.debug("Channel for {} ({}) not loaded", miChannel.getChannel(),
                                    miChannel.getFriendlyName());
                        }
                    } else {
                        logger.debug("Channel {} ({}), not loaded, missing type", miChannel.getChannel(),
                                miChannel.getFriendlyName());
                    }
                }
            }
            // only update if channels were added/removed
            if (channelsAdded > 0) {
                logger.debug("Current thing channels added: {}", channelsAdded);
                updateThing(thingBuilder.build());
            }
            return true;
        } catch (JsonIOException | JsonSyntaxException e) {
            logger.warn("Error parsing database Json", e);
        } catch (IOException e) {
            logger.warn("Error reading database file", e);
        } catch (Exception e) {
            logger.warn("Error creating channel structure", e);
        }
        return false;
    }

    private @Nullable ChannelUID addChannel(ThingBuilder thingBuilder, @Nullable String channel, String channelType,
            @Nullable String datatype, String friendlyName) {
        if (channel == null || channel.isEmpty() || datatype == null || datatype.isEmpty()) {
            logger.info("Channel '{}', UID '{}' cannot be added incorrectly configured database. ", channel,
                    getThing().getUID());
            return null;
        }
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channel);

        // TODO: Need to understand if this harms anything. If yes, channel only to be added when not there already.
        // current way allows to have no issues when channels are changing.
        if (getThing().getChannel(channel) != null) {
            logger.info("Channel '{}' for thing {} already exist... removing", channel, getThing().getUID());
            thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), channel));
        }
        ChannelBuilder newChannel = ChannelBuilder.create(channelUID, datatype).withLabel(friendlyName);
        if (!channelType.isBlank()) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(channelType);
            if (channelTypeRegistry.getChannelType(channelTypeUID) != null) {
                newChannel = newChannel.withType(channelTypeUID);
            } else {
                logger.debug("ChannelType '{}' is not available. Check the Json file for {}", channelTypeUID,
                        getThing().getUID());
            }
        }
        thingBuilder.withChannel(newChannel.build());
        return channelUID;
    }

    private @Nullable MiIoBasicChannel getChannel(String parameter) {
        for (MiIoBasicChannel refreshEntry : refreshList) {
            if (refreshEntry.getProperty().equals(parameter)) {
                return refreshEntry;
            }
        }
        logger.trace("Did not find channel for {} in {}", parameter, refreshList);
        return null;
    }

    private void updatePropsFromJsonArray(MiIoSendCommand response) {
        JsonArray res = response.getResult().getAsJsonArray();
        JsonArray para = parser.parse(response.getCommandString()).getAsJsonObject().get("params").getAsJsonArray();
        if (res.size() != para.size()) {
            logger.debug("Unexpected size different. Request size {},  response size {}. (Req: {}, Resp:{})",
                    para.size(), res.size(), para, res);
            return;
        }
        for (int i = 0; i < para.size(); i++) {
            // This is a miot parameter
            String param;
            final JsonElement paraElement = para.get(i);
            if (paraElement.isJsonObject()) { // miot channel
                param = paraElement.getAsJsonObject().get("did").getAsString();
            } else {
                param = paraElement.getAsString();
            }
            JsonElement val = res.get(i);
            if (val.isJsonNull()) {
                logger.debug("Property '{}' returned null (is it supported?).", param);
                continue;
            } else if (val.isJsonObject()) { // miot channel
                val = val.getAsJsonObject().get("value");
            }
            MiIoBasicChannel basicChannel = getChannel(param);
            updateChannel(basicChannel, param, val);
        }
    }

    private void updatePropsFromJsonObject(MiIoSendCommand response) {
        JsonObject res = response.getResult().getAsJsonObject();
        for (Object k : res.keySet()) {
            String param = (String) k;
            JsonElement val = res.get(param);
            if (val.isJsonNull()) {
                logger.debug("Property '{}' returned null (is it supported?).", param);
                continue;
            }
            MiIoBasicChannel basicChannel = getChannel(param);
            updateChannel(basicChannel, param, val);
        }
    }

    private void updateChannel(@Nullable MiIoBasicChannel basicChannel, String param, JsonElement value) {
        JsonElement val = value;
        if (basicChannel == null) {
            logger.debug("Channel not found for {}", param);
            return;
        }
        final String transformation = basicChannel.getTransfortmation();
        if (transformation != null) {
            JsonElement transformed = Conversions.execute(transformation, val);
            logger.debug("Transformed with '{}': {} {} -> {} ", transformation, basicChannel.getFriendlyName(), val,
                    transformed);
            val = transformed;
        }
        try {
            switch (basicChannel.getType().toLowerCase()) {
                case "number":
                    updateState(basicChannel.getChannel(), new DecimalType(val.getAsBigDecimal()));
                    break;
                case "dimmer":
                    updateState(basicChannel.getChannel(), new PercentType(val.getAsBigDecimal()));
                    break;
                case "string":
                    updateState(basicChannel.getChannel(), new StringType(val.getAsString()));
                    break;
                case "switch":
                    updateState(basicChannel.getChannel(), val.getAsString().toLowerCase().equals("on")
                            || val.getAsString().toLowerCase().equals("true") ? OnOffType.ON : OnOffType.OFF);
                    break;
                case "color":
                    Color rgb = new Color(val.getAsInt());
                    HSBType hsb = HSBType.fromRGB(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
                    updateState(basicChannel.getChannel(), hsb);
                    break;
                default:
                    logger.debug("No update logic for channeltype '{}' ", basicChannel.getType());
            }
        } catch (Exception e) {
            logger.debug("Error updating {} property {} with '{}' : {}: {}", getThing().getUID(),
                    basicChannel.getChannel(), val, e.getClass().getCanonicalName(), e.getMessage());
            logger.trace("Property update error detail:", e);
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
                case GET_VALUE:
                case GET_PROPERTIES:
                case GET_PROPERTY:
                    if (response.getResult().isJsonArray()) {
                        updatePropsFromJsonArray(response);
                    } else if (response.getResult().isJsonObject()) {
                        updatePropsFromJsonObject(response);
                    }
                    break;
                default:
                    if (refreshListCustomCommands.containsKey(response.getMethod())) {
                        logger.debug("Processing custom refresh command response for !{}", response.getMethod());
                        MiIoBasicChannel ch = refreshListCustomCommands.get(response.getMethod());
                        if (response.getResult().isJsonArray()) {
                            JsonArray cmdResponse = response.getResult().getAsJsonArray();
                            final String transformation = ch.getTransfortmation();
                            if (transformation == null || transformation.isBlank()) {
                                updateChannel(ch, ch.getChannel(),
                                        cmdResponse.get(0).isJsonPrimitive() ? cmdResponse.get(0)
                                                : new JsonPrimitive(cmdResponse.get(0).toString()));
                            } else {
                                updateChannel(ch, ch.getChannel(), cmdResponse);
                            }
                        } else {
                            updateChannel(ch, ch.getChannel(), new JsonPrimitive(response.getResult().toString()));
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            logger.debug("Error while handing message {}", response.getResponse(), e);
        }
    }
}
