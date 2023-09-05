/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.MiIoBindingConfiguration;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoQuantiyTypes;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.Utils;
import org.openhab.binding.miio.internal.basic.ActionConditions;
import org.openhab.binding.miio.internal.basic.BasicChannelTypeProvider;
import org.openhab.binding.miio.internal.basic.CommandParameterType;
import org.openhab.binding.miio.internal.basic.Conversions;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.openhab.binding.miio.internal.basic.MiIoDeviceActionCondition;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.transport.MiIoAsyncCommunication;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
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
import com.google.gson.JsonParser;
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
    protected final Logger logger = LoggerFactory.getLogger(MiIoBasicHandler.class);
    protected boolean hasChannelStructure;

    protected final ExpiringCache<Boolean> updateDataCache = new ExpiringCache<>(CACHE_EXPIRY, () -> {
        miIoScheduler.schedule(this::updateData, 0, TimeUnit.SECONDS);
        return true;
    });

    protected List<MiIoBasicChannel> refreshList = new ArrayList<>();
    protected Map<String, MiIoBasicChannel> refreshListCustomCommands = new HashMap<>();

    protected @Nullable MiIoBasicDevice miioDevice;
    protected Map<ChannelUID, MiIoBasicChannel> actions = new HashMap<>();
    protected ChannelTypeRegistry channelTypeRegistry;
    protected BasicChannelTypeProvider basicChannelTypeProvider;
    private Map<String, Integer> customRefreshInterval = new HashMap<>();

    public MiIoBasicHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService,
            CloudConnector cloudConnector, ChannelTypeRegistry channelTypeRegistry,
            BasicChannelTypeProvider basicChannelTypeProvider, TranslationProvider i18nProvider,
            LocaleProvider localeProvider) {
        super(thing, miIoDatabaseWatchService, cloudConnector, i18nProvider, localeProvider);
        this.channelTypeRegistry = channelTypeRegistry;
        this.basicChannelTypeProvider = basicChannelTypeProvider;
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
    public void handleCommand(ChannelUID channelUID, Command receivedCommand) {
        Command command = receivedCommand;
        deviceVariables.put(TIMESTAMP, Instant.now().getEpochSecond());
        if (command == RefreshType.REFRESH) {
            if (updateDataCache.isExpired()) {
                logger.debug("Refreshing {}", channelUID);
                updateDataCache.getValue();
            } else {
                logger.debug("Refresh {} skipped. Already refreshing", channelUID);
            }
            return;
        }
        if (handleCommandsChannels(channelUID, command)) {
            forceStatusUpdate();
            return;
        }
        logger.debug("Locating action for {} channel '{}': '{}'", getThing().getUID(), channelUID.getId(), command);
        if (!actions.isEmpty()) {
            final MiIoBasicChannel miIoBasicChannel = actions.get(channelUID);
            if (miIoBasicChannel != null) {
                int valuePos = 0;
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
                    if (command instanceof QuantityType) {
                        QuantityType<?> qtc = null;
                        try {
                            if (!miIoBasicChannel.getUnit().isBlank()) {
                                Unit<?> unit = MiIoQuantiyTypes.get(miIoBasicChannel.getUnit());
                                if (unit != null) {
                                    qtc = ((QuantityType<?>) command).toUnit(unit);
                                }
                            }
                        } catch (MeasurementParseException e) {
                            // swallow
                        }
                        if (qtc != null) {
                            command = new DecimalType(qtc.toBigDecimal());
                        } else {
                            logger.debug("Could not convert QuantityType to '{}'", miIoBasicChannel.getUnit());
                            command = new DecimalType(((QuantityType<?>) command).toBigDecimal());
                        }
                    }
                    if (paramType == CommandParameterType.OPENCLOSE) {
                        if (command instanceof OpenClosedType) {
                            value = new JsonPrimitive(command == OpenClosedType.OPEN ? "open" : "close");
                        } else {
                            value = new JsonPrimitive(("ON".contentEquals(command.toString().toUpperCase())
                                    || "1".contentEquals(command.toString())) ? "open" : "close");
                        }
                    }
                    if (paramType == CommandParameterType.OPENCLOSENUMBER) {
                        if (command instanceof OpenClosedType) {
                            value = new JsonPrimitive(command == OpenClosedType.OPEN ? 1 : 0);
                        } else {
                            value = new JsonPrimitive(("ON".contentEquals(command.toString().toUpperCase())
                                    || "1".contentEquals(command.toString())) ? 1 : 0);
                        }
                    }
                    if (paramType == CommandParameterType.OPENCLOSESWITCH) {
                        if (command instanceof OpenClosedType) {
                            value = new JsonPrimitive(command == OpenClosedType.OPEN ? "on" : "off");
                        } else {
                            value = new JsonPrimitive(("ON".contentEquals(command.toString().toUpperCase())
                                    || "1".contentEquals(command.toString())) ? "on" : "off");
                        }
                    }
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
                        } else if (paramType == CommandParameterType.ONOFFNUMBER) {
                            value = new JsonPrimitive(command == OnOffType.ON ? 1 : 0);
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
                        value = parameters.deepCopy();
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
                    if (action.isMiOtAction() && parameters.size() > 0 && parameters.get(0).isJsonObject()) {
                        // hack as unlike any other commands miot actions parameters appear to be send as a json object
                        // instead of a json array
                        cmd = cmd + parameters.get(0).getAsJsonObject().toString();
                    } else {
                        cmd = cmd + parameters.toString();
                    }
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
                    for (Entry<ChannelUID, MiIoBasicChannel> a : actions.entrySet()) {
                        logger.trace("Available entries: {} : {}", a.getKey(), a.getValue().getFriendlyName());
                    }
                }
            }
            forceStatusUpdate();
        } else {
            logger.debug("Actions not loaded yet, or none available");
        }
    }

    protected void forceStatusUpdate() {
        updateDataCache.invalidateValue();
        miIoScheduler.schedule(() -> {
            updateData();
        }, 3000, TimeUnit.MILLISECONDS);
    }

    protected @Nullable JsonElement miotTransform(MiIoBasicChannel miIoBasicChannel, @Nullable JsonElement value) {
        JsonObject json = new JsonObject();
        json.addProperty("did", miIoBasicChannel.getChannel());
        json.addProperty("siid", miIoBasicChannel.getSiid());
        json.addProperty("piid", miIoBasicChannel.getPiid());
        json.add("value", value);
        return json;
    }

    protected @Nullable JsonElement miotActionTransform(MiIoDeviceAction action, MiIoBasicChannel miIoBasicChannel,
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
                sendCommand(MiIoCommand.MIIO_INFO);
            }
            final MiIoBasicDevice midevice = miioDevice;
            if (midevice != null) {
                deviceVariables.put(TIMESTAMP, Instant.now().getEpochSecond());
                refreshProperties(midevice, "");
                refreshCustomProperties(midevice, false);
                refreshNetwork();
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}': ", getThing().getUID().toString(), e);
        }
    }

    private boolean customRefreshIntervalCheck(MiIoBasicChannel miChannel) {
        if (miChannel.getRefreshInterval() > 1) {
            int iteration = customRefreshInterval.getOrDefault(miChannel.getChannel(), 0);
            if (iteration < 1) {
                customRefreshInterval.put(miChannel.getChannel(), miChannel.getRefreshInterval() - 1);
            } else {
                logger.debug("Skip refresh of channel {} for {}. Next refresh in {} cycles.", miChannel.getChannel(),
                        getThing().getUID(), iteration);
                customRefreshInterval.put(miChannel.getChannel(), iteration - 1);
                return true;
            }
        }
        return false;
    }

    private boolean linkedChannelCheck(MiIoBasicChannel miChannel) {
        if (!isLinked(miChannel.getChannel())) {
            logger.debug("Skip refresh of channel {} for {} as it is not linked", miChannel.getChannel(),
                    getThing().getUID());
            return false;
        }
        return true;
    }

    protected void refreshCustomProperties(MiIoBasicDevice midevice, boolean cloudOnly) {
        logger.debug("Custom Refresh for device '{}': {} channels ", getThing().getUID(),
                refreshListCustomCommands.size());
        for (MiIoBasicChannel miChannel : refreshListCustomCommands.values()) {
            if (customRefreshIntervalCheck(miChannel) || !linkedChannelCheck(miChannel)) {
                continue;
            }
            final JsonElement para = miChannel.getCustomRefreshParameters();
            String cmd = miChannel.getChannelCustomRefreshCommand() + (para != null ? para.toString() : "");
            if (!cmd.startsWith("/") && !cloudOnly) {
                cmds.put(sendCommand(cmd), miChannel.getChannel());
            } else {
                if (cloudServer.isBlank()) {
                    logger.debug("Cloudserver empty. Skipping refresh for {} channel '{}'", getThing().getUID(),
                            miChannel.getChannel());
                } else {
                    cmds.put(sendCommand(cmd, cloudServer), miChannel.getChannel());
                }
            }
        }
    }

    protected boolean refreshProperties(MiIoBasicDevice device, String childId) {
        String command = device.getDevice().getPropertyMethod();
        int maxProperties = device.getDevice().getMaxProperties();
        JsonArray getPropString = new JsonArray();
        if (!childId.isBlank()) {
            getPropString.add(childId);
            maxProperties++;
        }
        for (MiIoBasicChannel miChannel : refreshList) {
            if (customRefreshIntervalCheck(miChannel) || !linkedChannelCheck(miChannel)) {
                continue;
            }
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
                if (!childId.isBlank()) {
                    getPropString.add(childId);
                }
            }
        }
        if (getPropString.size() > (childId.isBlank() ? 0 : 1)) {
            sendRefreshProperties(command, getPropString);
        }
        return true;
    }

    protected void sendRefreshProperties(String command, JsonArray getPropString) {
        JsonArray para = getPropString;
        if (MiIoCommand.GET_DEVICE_PROPERTY_EXP.getCommand().contentEquals(command)) {
            logger.debug("This seems a subdevice propery refresh for {}... ({} {})", getThing().getUID(), command,
                    getPropString.toString());
            para = new JsonArray();
            para.add(getPropString);
        }
        sendCommand(command, para.toString(), getCloudServer());
    }

    /**
     * Checks if the channel structure has been build already based on the model data. If not build it.
     */
    protected void checkChannelStructure() {
        final MiIoBindingConfiguration configuration = this.configuration;
        if (configuration == null) {
            return;
        }
        if (!hasChannelStructure) {
            if (configuration.model.isEmpty()) {
                logger.debug("Model needs to be determined");
                isIdentified = false;
            } else {
                hasChannelStructure = buildChannelStructure(configuration.model);
            }
        }
        if (hasChannelStructure) {
            refreshList = new ArrayList<>();
            refreshListCustomCommands = new HashMap<>();
            final MiIoBasicDevice miioDevice = this.miioDevice;
            if (miioDevice != null) {
                for (MiIoBasicChannel miChannel : miioDevice.getDevice().getChannels()) {
                    if (miChannel.getRefresh()) {
                        if (miChannel.getChannelCustomRefreshCommand().isBlank()) {
                            refreshList.add(miChannel);
                        } else {
                            String cm = miChannel.getChannelCustomRefreshCommand();
                            refreshListCustomCommands.put(cm.trim(), miChannel);
                        }
                    }
                }
            }
        }
    }

    protected boolean buildChannelStructure(String deviceName) {
        logger.debug("Building Channel Structure for {} - Model: {}", getThing().getUID().toString(), deviceName);
        URL fn = miIoDatabaseWatchService.getDatabaseUrl(deviceName);
        if (fn == null) {
            logger.warn("Database entry for model '{}' cannot be found.", deviceName);
            return false;
        }
        try {
            JsonObject deviceMapping = Utils.convertFileToJSON(fn);
            logger.debug("Using device database: {} for device {}", fn.getFile(), deviceName);
            String key = fn.getFile().replaceFirst("/database/", "").split("json")[0];
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
                for (Channel cn : getThing().getChannels()) {
                    logger.trace("Channel '{}' for thing {} already exist... removing", cn.getUID(),
                            getThing().getUID());
                    if (!PERSISTENT_CHANNELS.contains(cn.getUID().getId().toString())) {
                        thingBuilder.withoutChannels(cn);
                    }
                }
                for (MiIoBasicChannel miChannel : device.getDevice().getChannels()) {
                    logger.debug("properties {}", miChannel);
                    if (!miChannel.getType().isEmpty()) {
                        basicChannelTypeProvider.addChannelType(miChannel, deviceName);
                        ChannelUID channelUID = addChannel(thingBuilder, miChannel, deviceName, key);
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

    protected @Nullable ChannelUID addChannel(ThingBuilder thingBuilder, MiIoBasicChannel miChannel, String model,
            String key) {
        String channel = miChannel.getChannel();
        String dataType = miChannel.getType();
        if (channel.isEmpty() || dataType.isEmpty()) {
            logger.info("Channel '{}', UID '{}' cannot be added incorrectly configured database. ", channel,
                    getThing().getUID());
            return null;
        }
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channel);
        String label = getLocalText(I18N_CHANNEL_PREFIX + key + channel, miChannel.getFriendlyName());
        ChannelBuilder newChannel = ChannelBuilder.create(channelUID, dataType).withLabel(label);
        boolean useGeneratedChannelType = false;
        if (!miChannel.getChannelType().isBlank()) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(miChannel.getChannelType());
            if (channelTypeRegistry.getChannelType(channelTypeUID) != null) {
                newChannel = newChannel.withType(channelTypeUID);
                final LinkedHashSet<String> tags = miChannel.getTags();
                if (tags != null && !tags.isEmpty()) {
                    newChannel.withDefaultTags(tags);
                }
            } else {
                logger.debug("ChannelType '{}' is not available. Check the Json file for {}", channelTypeUID, model);
                useGeneratedChannelType = true;
            }
        } else {
            useGeneratedChannelType = true;
        }
        if (useGeneratedChannelType) {
            newChannel = newChannel
                    .withType(new ChannelTypeUID(BINDING_ID, model.toUpperCase().replace(".", "_") + "_" + channel));
            final Set<String> tags = miChannel.getTags();
            if (tags != null && !tags.isEmpty()) {
                newChannel.withDefaultTags(tags);
            }
        }
        thingBuilder.withChannel(newChannel.build());
        return channelUID;
    }

    protected @Nullable MiIoBasicChannel getChannel(String parameter) {
        for (MiIoBasicChannel refreshEntry : refreshList) {
            if (refreshEntry.getProperty().equals(parameter)) {
                return refreshEntry;
            }
        }
        logger.trace("Did not find channel for {} in {}", parameter, refreshList);
        return null;
    }

    private @Nullable MiIoBasicChannel getCustomRefreshChannel(String channelName) {
        for (MiIoBasicChannel refreshEntry : refreshListCustomCommands.values()) {
            if (refreshEntry.getChannel().equals(channelName)) {
                return refreshEntry;
            }
        }
        logger.trace("Did not find channel for {} in {}", channelName, refreshList);
        return null;
    }

    protected void updatePropsFromJsonArray(MiIoSendCommand response) {
        boolean isSubdeviceUpdate = false;
        JsonArray res = response.getResult().getAsJsonArray();
        JsonArray para = JsonParser.parseString(response.getCommandString()).getAsJsonObject().get("params")
                .getAsJsonArray();
        if (para.get(0).isJsonArray()) {
            isSubdeviceUpdate = true;
            para = para.get(0).getAsJsonArray();
            para.remove(0);
            if (res.get(0).isJsonArray()) {
                res = res.get(0).getAsJsonArray();
            }
        }
        if (res.size() != para.size()) {
            logger.debug("Unexpected size different{}. Request size {},  response size {}. (Req: {}, Resp:{})",
                    isSubdeviceUpdate ? " for childdevice refresh" : "", para.size(), res.size(), para, res);
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

    protected void updatePropsFromJsonObject(MiIoSendCommand response) {
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

    protected void updateChannel(@Nullable MiIoBasicChannel basicChannel, String param, JsonElement value) {
        JsonElement val = value;
        deviceVariables.put(param, val);
        if (basicChannel == null) {
            logger.debug("Channel not found for {}", param);
            return;
        }
        final String transformation = basicChannel.getTransformation();
        if (transformation != null) {
            JsonElement transformed = Conversions.execute(transformation, val, deviceVariables);
            logger.debug("Transformed with '{}': {} {} -> {} ", transformation, basicChannel.getFriendlyName(), val,
                    transformed);
            val = transformed;
        }
        try {
            String[] chType = basicChannel.getType().toLowerCase().split(":");
            switch (chType[0]) {
                case "number":
                    quantityTypeUpdate(basicChannel, val, chType.length > 1 ? chType[1] : "");
                    break;
                case "dimmer":
                    updateState(basicChannel.getChannel(), new PercentType(val.getAsBigDecimal()));
                    break;
                case "string":
                    if (val.isJsonPrimitive()) {
                        updateState(basicChannel.getChannel(), new StringType(val.getAsString()));
                    } else {
                        updateState(basicChannel.getChannel(), new StringType(val.toString()));
                    }
                    break;
                case "switch":
                    if (val.getAsJsonPrimitive().isNumber()) {
                        updateState(basicChannel.getChannel(), val.getAsInt() > 0 ? OnOffType.ON : OnOffType.OFF);
                    } else {
                        String strVal = val.getAsString().toLowerCase();
                        updateState(basicChannel.getChannel(),
                                "on".equals(strVal) || "true".equals(strVal) || "1".equals(strVal) ? OnOffType.ON
                                        : OnOffType.OFF);
                    }
                    break;
                case "contact":
                    if (val.getAsJsonPrimitive().isNumber()) {
                        updateState(basicChannel.getChannel(),
                                val.getAsInt() > 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                    } else {
                        String strVal = val.getAsString().toLowerCase();
                        updateState(basicChannel.getChannel(),
                                "open".equals(strVal) || "on".equals(strVal) || "true".equals(strVal)
                                        || "1".equals(strVal) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                    }
                    break;
                case "color":
                    if (val.isJsonPrimitive()
                            && (val.getAsJsonPrimitive().isNumber() || val.getAsString().matches("^[0-9]+$"))) {
                        Color rgb = new Color(val.getAsInt());
                        HSBType hsb = HSBType.fromRGB(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
                        updateState(basicChannel.getChannel(), hsb);
                    } else {
                        try {
                            HSBType hsb = HSBType.valueOf(val.getAsString().replace("[", "").replace("]", ""));
                            updateState(basicChannel.getChannel(), hsb);
                        } catch (IllegalArgumentException e) {
                            logger.debug("Failed updating channel '{}'. Could not convert '{}' to color",
                                    basicChannel.getChannel(), val.getAsString());
                        }
                    }
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

    protected void quantityTypeUpdate(MiIoBasicChannel basicChannel, JsonElement val, String type) {
        if (!basicChannel.getUnit().isBlank()) {
            Unit<?> unit = MiIoQuantiyTypes.get(basicChannel.getUnit());
            if (unit != null) {
                logger.debug("'{}' channel '{}' has unit '{}' with symbol '{}'.", getThing().getUID(),
                        basicChannel.getChannel(), basicChannel.getUnit(), unit);
                updateState(basicChannel.getChannel(), new QuantityType<>(val.getAsBigDecimal(), unit));
            } else {
                logger.debug(
                        "Unit '{}' used by '{}' channel '{}' is not found in conversion table... Trying anyway to submit as the update.",
                        basicChannel.getUnit(), getThing().getUID(), basicChannel.getChannel());
                updateState(basicChannel.getChannel(),
                        new QuantityType<>(val.getAsBigDecimal().toPlainString() + " " + basicChannel.getUnit()));
            }
            return;
        }
        // if no unit is provided or unit not found use default units, these units have so far been seen for miio
        // devices
        switch (type.toLowerCase()) {
            case "temperature":
                updateState(basicChannel.getChannel(), new QuantityType<>(val.getAsBigDecimal(), SIUnits.CELSIUS));
                break;
            case "electriccurrent":
                updateState(basicChannel.getChannel(), new QuantityType<>(val.getAsBigDecimal(), Units.AMPERE));
                break;
            case "energy":
                updateState(basicChannel.getChannel(), new QuantityType<>(val.getAsBigDecimal(), Units.WATT));
                break;
            case "time":
                updateState(basicChannel.getChannel(), new QuantityType<>(val.getAsBigDecimal(), Units.HOUR));
                break;
            default:
                updateState(basicChannel.getChannel(), new DecimalType(val.getAsBigDecimal()));
        }
    }

    @Override
    public void onMessageReceived(MiIoSendCommand response) {
        super.onMessageReceived(response);
        if (response.isError() || (!response.getSender().isBlank()
                && !response.getSender().contentEquals(getThing().getUID().getAsString()))) {
            logger.trace("Device {} is not processing command {} as no match. Sender id:'{}'", getThing().getUID(),
                    response.getId(), response.getSender());
            return;
        }
        try {
            switch (response.getCommand()) {
                case MIIO_INFO:
                    break;
                case GET_DEVICE_PROPERTY_EXP:
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
                    String channel = cmds.get(response.getId());
                    if (channel != null) {
                        logger.debug("Processing custom refresh command response for '{}' - {}", response.getMethod(),
                                response.getResult());
                        final MiIoBasicChannel ch = getCustomRefreshChannel(channel);
                        if (ch != null) {
                            if (response.getResult().isJsonArray()) {
                                JsonArray cmdResponse = response.getResult().getAsJsonArray();
                                final String transformation = ch.getTransformation();
                                if (transformation == null || transformation.isBlank()) {
                                    JsonElement response0 = cmdResponse.get(0);
                                    updateChannel(ch, ch.getChannel(), response0.isJsonPrimitive() ? response0
                                            : new JsonPrimitive(response0.toString()));
                                } else {
                                    updateChannel(ch, ch.getChannel(), cmdResponse);
                                }
                            } else {
                                updateChannel(ch, ch.getChannel(), new JsonPrimitive(response.getResult().toString()));
                            }
                        }
                        cmds.remove(response.getId());
                    } else {
                        logger.debug("Could not identify channel for {}. Device {} has {} commands in queue.",
                                response.getMethod(), getThing().getUID(), cmds.size());
                    }
                    break;
            }
        } catch (Exception e) {
            logger.debug("Error while handing message {}", response.getResponse(), e);
        }
    }
}
