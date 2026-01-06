/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal.handler;

import static org.openhab.binding.viessmann.internal.ViessmannBindingConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.viessmann.internal.ViessmannDynamicStateDescriptionProvider;
import org.openhab.binding.viessmann.internal.api.ViessmannCommunicationException;
import org.openhab.binding.viessmann.internal.config.ThingsConfig;
import org.openhab.binding.viessmann.internal.dto.ThingMessageDTO;
import org.openhab.binding.viessmann.internal.dto.ViessmannMessage;
import org.openhab.binding.viessmann.internal.dto.device.StoredChannelValues;
import org.openhab.binding.viessmann.internal.dto.features.FeatureCommand;
import org.openhab.binding.viessmann.internal.dto.features.FeatureCommandParams;
import org.openhab.binding.viessmann.internal.dto.features.FeatureDataDTO;
import org.openhab.binding.viessmann.internal.dto.features.FeatureProperties;
import org.openhab.binding.viessmann.internal.dto.schedule.DaySchedule;
import org.openhab.binding.viessmann.internal.dto.schedule.ScheduleDTO;
import org.openhab.binding.viessmann.internal.interfaces.BridgeInterface;
import org.openhab.binding.viessmann.internal.util.ViessmannUtil;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.items.ManagedItemProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link DeviceHandler} is responsible for handling DeviceHandler
 *
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
@Component(service = DeviceHandler.class)
public class DeviceHandler extends ViessmannThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private static final Set<String> BASE_ALLOWED_SUFFIXES = Set.of("active");

    private ThingsConfig config = new ThingsConfig();

    private final StoredChannelValues storedChannelValues = new StoredChannelValues();

    private final ItemChannelLinkRegistry linkRegistry;
    private final ManagedItemProvider managedItemProvider;

    private final TranslationProvider i18Provider;
    private final LocaleProvider localeProvider;

    private final Bundle bundle;

    public DeviceHandler(Thing thing, ViessmannDynamicStateDescriptionProvider stateDescriptionProvider,
            ItemChannelLinkRegistry linkRegistry, ManagedItemProvider managedItemProvider,
            TranslationProvider i18Provider, LocaleProvider localeProvider) {
        super(thing, stateDescriptionProvider);
        this.linkRegistry = linkRegistry;
        this.managedItemProvider = managedItemProvider;
        this.i18Provider = i18Provider;
        this.localeProvider = localeProvider;
        bundle = FrameworkUtil.getBundle(getClass());
    }

    @Override
    public void initialize() {
        ThingsConfig config = getConfigAs(ThingsConfig.class);
        this.config = config;
        if (config.deviceId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.device-id");
            return;
        }
        updateProperty(PROPERTY_ID, config.deviceId); // set representation property used by discovery

        int thingTypeVersion = getThingTypeVersion();
        if (thingTypeVersion < 4) {
            migrateChannelIds();
        }

        initDeviceState();
    }

    private void migrateChannelIds() {
        List<Channel> oldChannels = thing.getChannels();
        List<Channel> newChannels = new ArrayList<>(oldChannels.size());

        Map<ChannelUID, ChannelUID> renameMap = new LinkedHashMap<>();

        for (Channel channel : oldChannels) {
            String oldId = channel.getUID().getId();
            String newId = ViessmannUtil.camelToHyphen(oldId);
            boolean updateChannelType = false;
            String channelLabel = channel.getLabel();
            String oldChannelType = ViessmannUtil
                    .camelToHyphen(Objects.requireNonNull(channel.getChannelTypeUID()).toString());
            oldChannelType = oldChannelType.replace(BINDING_ID + ":", "");
            String newChannelType = oldChannelType;
            if ("type-energy".equals(oldChannelType) || "energy".equals(oldChannelType)) {
                logger.trace("Migrate channelType");
                if (newId.contains("gas")) {
                    newChannelType = "gas-energy";
                }
                if (newId.contains("power")) {
                    newChannelType = "power-energy";
                }
                updateChannelType = true;
            }

            if ("type-volume".equals(oldChannelType) || "volume".equals(oldChannelType)) {
                if (newId.contains("gas")) {
                    newChannelType = "gas-volume";
                    updateChannelType = true;
                }
            }

            if ("type-minute".equals(oldChannelType) || "minute".equals(oldChannelType)
                    || "hours".equals(oldChannelType) || "type-hours".equals(oldChannelType)) {
                newChannelType = "duration";
                updateChannelType = true;
            }

            if ("type-settemperature".equals(oldChannelType) || "settemperature".equals(oldChannelType)) {
                newChannelType = "set-temperature";
                updateChannelType = true;
            }

            if (oldChannelType.contains("type-")) {
                newChannelType = newChannelType.replace("type-", "");
                updateChannelType = true;
            }

            if ("slope".equals(oldId) && oldChannelType.contains("decimal")) {
                newChannelType = "slope";
                updateChannelType = true;
            }

            if ("shift".equals(oldId) && oldChannelType.contains("number")) {
                newChannelType = "shift";
                updateChannelType = true;
            }

            if (!newId.equals(oldId) || updateChannelType) {
                logger.info("Migrating channel '{}' -> '{}' | channel-type  '{}' -> '{}'", oldId, newId, oldChannelType,
                        newChannelType);

                ChannelUID oldUid = channel.getUID();
                ChannelUID newUid = new ChannelUID(thing.getUID(), newId);
                String channelDescription = channel.getDescription();
                if (channelDescription == null) {
                    channelDescription = "";
                }

                ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, newChannelType);
                if (channelLabel != null) {
                    Channel newChannel = ChannelBuilder.create(newUid, channel.getAcceptedItemType())
                            .withLabel(channelLabel).withDescription(channelDescription).withType(channelTypeUID)
                            .withProperties(channel.getProperties()).build();

                    newChannels.add(newChannel);
                    renameMap.put(oldUid, newUid);
                }
            }
        }

        if (renameMap.isEmpty()) {
            return;
        }

        updateThing(editThing().withChannels(newChannels).build());

        for (Map.Entry<ChannelUID, ChannelUID> e : renameMap.entrySet()) {
            ChannelUID oldUid = e.getKey();
            ChannelUID newUid = e.getValue();

            Collection<ItemChannelLink> links = new ArrayList<>(linkRegistry.getLinks(oldUid));

            for (ItemChannelLink link : links) {
                String item = link.getItemName();
                if (isManagedItem(item)) {
                    try {
                        linkRegistry.remove(link.getUID());
                    } catch (Exception ex) {
                        logger.warn("Could not remove old link {} -> {}: {}", item, oldUid, ex.getMessage());
                    }

                    linkRegistry.add(new ItemChannelLink(item, newUid));
                    logger.info("Re-linked item '{}' from '{}' to '{}'", item, oldUid.getId(), newUid.getId());
                }
            }
        }
    }

    private @Nullable BridgeInterface getBridgeInterface() {
        if (getBridge() instanceof Bridge bridge) {
            if (bridge.getHandler() instanceof BridgeInterface bridgeInterface) {
                return bridgeInterface;
            }
        }
        return null;
    }

    @Override
    public void initChannelState() {
        BridgeInterface bridgeInterface = getBridgeInterface();
        if (bridgeInterface == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.bridge-type");
            return;
        }
        bridgeInterface.setConfigInstallationGatewayIdToDevice(this);
        bridgeInterface.updateFeaturesOfDevice(this);
    }

    public String getDeviceId() {
        return config.deviceId;
    }

    public void setConfigInstallationGatewayId(String installationId, String gatewaySerial) {
        this.updateProperty(INSTALLATION_ID, installationId);
        this.updateProperty(GATEWAY_SERIAL, gatewaySerial);
    }

    public String getInstallationId() {
        return Objects.requireNonNull(thing.getProperties().get(INSTALLATION_ID), "Installation ID is missing!");
    }

    public String getGatewaySerial() {
        return Objects.requireNonNull(thing.getProperties().get(GATEWAY_SERIAL), "Gateway serial is missing!");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel ch = thing.getChannel(channelUID.getId());
        if (ch == null) {
            logger.warn("Invalid or missing ChannelUID: {}", channelUID);
            return;
        }

        Map<String, String> prop = ch.getProperties();
        String commands = prop.get("command");
        if (commands == null) {
            return;
        }

        String channelId = ch.getUID().getId();
        String[] parts = channelId.split("#", 2);
        channelId = parts[0];
        String suffix = parts.length > 1 ? parts[1] : "";

        String[] params = prop.getOrDefault("params", "{}").split(",");
        String[] com = commands.split(",");

        logger.trace("ChannelUID: {} | Properties: {} | Params {}", ch.getUID(), prop, params);

        if (command instanceof OnOffType onOff) {
            handleOnOff(channelUID, channelId, suffix, prop, params, com, onOff);
        } else if (command instanceof DecimalType dec) {
            handleNumeric(channelUID, channelId, suffix, prop, params, com, dec.doubleValue());
        } else if (command instanceof QuantityType<?> qty) {
            handleNumeric(channelUID, channelId, suffix, prop, params, com, qty.doubleValue());
        } else if (command instanceof StringType str) {
            handleString(channelUID, channelId, suffix, prop, com, str);
        }
    }

    private void handleOnOff(ChannelUID channelUID, String channelId, String suffix, Map<String, String> prop,
            String[] params, String[] com, OnOffType onOff) {
        logger.trace("Received OnOff Command for Channel {} value={}", channelUID, onOff);
        if (!checkCommandType(prop, "boolean") && !"boolean".equals(prop.get("channelType"))) {
            logger.warn("OnOffType Command not executable for Channel: {}", channelUID);
            return;
        }

        boolean isOn = onOff == OnOffType.ON;
        String feature = prop.get("feature");

        UriParam up = resolveOnOffUri(prop, channelId, params, com, isOn);
        if (up == null) {
            logger.warn("Channel {} misconfigured (no URI)", channelUID);
            return;
        }
        sendChannelCommand(up.uri(), up.param(), feature != null && feature.contains("oneTimeCharge") ? 2 : 0,
                feature != null && feature.contains("oneTimeCharge"));
    }

    private void handleNumeric(ChannelUID channelUID, String channelId, String suffix, Map<String, String> prop,
            String[] params, String[] com, double value) {
        logger.trace("Received Numeric Command for Channel {} value={}", channelUID, value);
        if (!checkCommandType(prop, "number")) {
            logger.warn("Numeric Command not executable for Channel: {}", channelUID);
            return;
        }

        UriParam up = resolveNumericCommand(prop, channelId, suffix, params, com, value);
        if (up == null) {
            logger.trace("No matching numeric command for Channel {}", channelUID);
            return;
        }
        sendChannelCommand(up.uri(), up.param(), 0, false);
    }

    private void handleString(ChannelUID channelUID, String channelId, String suffix, Map<String, String> prop,
            String[] com, StringType command) {
        logger.trace("Received StringType Command for Channel {} value={}", channelUID, command);
        if (!checkCommandType(prop, "string")) {
            logger.warn("StringType Command not executable for Channel: {}", channelUID);
            return;
        }

        String value = command.toString();
        String lcSuffix = suffix.toLowerCase(Locale.ROOT);

        String uri = null;
        String paramsDef = null;

        for (String c : com) {
            String p = prop.get(c + "Params");
            if ((p != null && p.contains(lcSuffix)) || c.toLowerCase(Locale.ROOT).contains(lcSuffix)) {
                uri = prop.get(c + "Uri");
                paramsDef = p;
                break;
            }
        }

        if (uri == null || paramsDef == null) {
            logger.trace("No matching StringType command for Channel {}", channelUID);
            return;
        }

        JsonObject json = new JsonObject();
        String[] params = paramsDef.split(",");

        for (String p : params) {
            String v = storedChannelValues.getProperty(channelId + "#" + p);
            if (p.equals(suffix) || v == null) {
                json.addProperty(p, value);
            } else {
                json.addProperty(p, v);
            }
        }
        sendChannelCommand(uri, json.toString(), 0, false);
    }

    private void sendChannelCommand(String uri, String param, int delaySeconds, boolean reloadFeatures) {
        BridgeInterface bridge = getBridgeInterface();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.bridge-type");
            return;
        }

        try {
            if (!bridge.setData(uri, param) || reloadFeatures) {
                scheduler.schedule(this::initChannelState, delaySeconds, TimeUnit.SECONDS);
            }
        } catch (ViessmannCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private @Nullable UriParam resolveOnOffUri(Map<String, String> prop, String channelId, String[] params,
            String[] com, boolean isOn) {
        // setActive / setEnabled (highest priority)
        for (String base : List.of("setActive", "setEnabled")) {
            if (prop.containsKey(base + "Uri")) {
                String key = require(prop, base + "Params");
                return new UriParam(require(prop, base + "Uri"), "{\"" + key + "\":" + isOn + "}");
            }
        }

        // multi-param JSON
        if (isOn && params.length > 1) {
            JsonObject json = new JsonObject();
            for (String p : params) {
                String value = storedChannelValues.getProperty(channelId + "#" + p);
                if (value != null) {
                    if ("number".equals(prop.get(p + "Type"))) {
                        json.addProperty(p, Double.parseDouble(value));
                    } else {
                        json.addProperty(p, value);
                    }
                }
            }

            String uri = prop.containsKey("scheduleUri") ? prop.get("scheduleUri") : prop.get("activateUri");
            if (uri == null) {
                return null;
            }
            return new UriParam(uri, json.toString());
        }

        // Simple activate / deactivate
        String uri = prop.get(isOn ? "activateUri" : "deactivateUri");
        return uri != null ? new UriParam(uri, "{}") : null;
    }

    private @Nullable UriParam resolveNumericCommand(Map<String, String> prop, String channelId, String suffix,
            String[] params, String[] com, double value) {
        String lcSuffix = Objects.requireNonNull(ViessmannUtil.hyphenToCamel(suffix, false)).toLowerCase(Locale.ROOT);
        String uri = null;
        String paramsDef = null;
        String cmd = null;
        JsonObject json = new JsonObject();

        for (String c : com) {
            cmd = c;

            paramsDef = prop.get(cmd + "Params");

            if ((paramsDef != null && paramsDef.toLowerCase(Locale.ROOT).contains(lcSuffix))
                    || cmd.toLowerCase(Locale.ROOT).contains(lcSuffix) || isQuantityCommand(cmd)) {
                uri = prop.get(cmd + "Uri");

                if ((paramsDef != null
                        && paramsDef.matches(".*(Temperature|setHysteresis|setMin|setMax|temperature).*"))) {
                    json.addProperty(paramsDef, value);
                    if (uri == null) {
                        return null;
                    }
                    return new UriParam(uri, json.toString());
                }
                break;
            }
        }

        if (uri == null || cmd == null) {
            return null;
        }

        for (String p : params) {
            String v = storedChannelValues.getProperty(channelId + "#" + p);
            String type = prop.get(p + "Type");

            if (v == null || p.equals(suffix)) {
                v = Double.toString(value);
            }

            if ("number".equals(type)) {
                json.addProperty(p, Double.parseDouble(v));
            } else {
                json.addProperty(p, v);
            }

            if (cmd.toLowerCase(Locale.ROOT).contains(lcSuffix) || isQuantityCommand(cmd)) {
                break;
            }
        }
        return new UriParam(uri, json.toString());
    }

    @Override
    public void handleUpdateChannel(ViessmannMessage msg) {
        logger.trace("handleUpdateChannel: {}", msg);
    }

    @Override
    public void handleUpdate(FeatureDataDTO featureDataDTO) {
        updateStatus(ThingStatus.ONLINE);
        if (featureDataDTO.properties == null) {
            return;
        }
        ThingMessageDTO msg = new ThingMessageDTO();
        msg.setDeviceId(featureDataDTO.deviceId);
        msg.setFeatureClear(featureDataDTO.feature);
        msg.setFeatureDescription(getFeatureDescription(featureDataDTO.feature));

        Map<String, FeatureCommand> commands = featureDataDTO.commands;
        if (commands != null) {
            msg.setCommands(commands);
        }

        FeatureProperties prop = featureDataDTO.properties;
        List<String> entr = prop.getUsedEntries();
        if (entr.isEmpty()) {
            return;
        }

        for (String entry : entr) {
            if ("unit".equals(entry)) {
                continue;
            }

            String valueEntry = "";
            String typeEntry = "";
            boolean bool = false;
            String viUnit = "";
            String unit = null;
            msg.setFeatureName(getFeatureName(featureDataDTO.feature));
            msg.setSuffix(entry);
            switch (entry) {
                case "value":
                    viUnit = prop.value.unit;
                    typeEntry = switch (viUnit) {
                        case "celsius" -> "temperature";
                        case "percent", "kelvin", "liter", "bar" -> viUnit;
                        case "minute" -> "duration-minute";
                        case "revolutionsPerSecond" -> "revolutions-per-second";
                        case "kilowattHour/year" -> "house-heating-load";
                        case "kiloJoule" -> "thermal-energy";
                        case "cubicMeter/hour" -> "cubic-meter-per-hour";
                        case null, default -> prop.value.type;
                    };
                    if ("liter/hour".equals(viUnit)) {
                        valueEntry = String.valueOf(Double.parseDouble(prop.value.value) / 60);
                        viUnit = "liter/minute";
                        typeEntry = "liter-per-minute";
                    } else {
                        valueEntry = prop.value.value;
                    }
                    if ("kilowatt".equals(viUnit)) {
                        valueEntry = String.valueOf(Double.parseDouble(prop.value.value) * 1000);
                        viUnit = "watt";
                        typeEntry = "power";
                    }
                    break;
                case "status":
                    typeEntry = prop.status.type;
                    valueEntry = prop.status.value;
                    if ("off".equals(valueEntry)) {
                        typeEntry = "boolean";
                    } else if ("on".equals(valueEntry)) {
                        typeEntry = "boolean";
                        bool = true;
                    }
                    viUnit = "";
                    break;
                case "active":
                    typeEntry = prop.active.type;
                    valueEntry = prop.active.value ? "true" : "false";
                    bool = prop.active.value;
                    break;
                case "name":
                    typeEntry = prop.name.type;
                    valueEntry = prop.name.value;
                    break;
                case "shift":
                    typeEntry = "shift";
                    valueEntry = prop.shift.value.toString();
                    break;
                case "slope":
                    typeEntry = "slope";
                    valueEntry = prop.slope.value.toString();
                    break;
                case "entries":
                    msg.setSuffix("schedule");
                    typeEntry = prop.entries.type;
                    valueEntry = new Gson().toJson(prop.entries.value);
                    break;
                case "overlapAllowed":
                    typeEntry = prop.overlapAllowed.type;
                    valueEntry = prop.overlapAllowed.value ? "true" : "false";
                    bool = prop.overlapAllowed.value;
                    break;
                case "temperature":
                    valueEntry = prop.temperature.value.toString();
                    typeEntry = "temperature";
                    viUnit = prop.temperature.unit;
                    break;
                case "start":
                    typeEntry = prop.start.type;
                    valueEntry = prop.start.value;
                    break;
                case "begin":
                    typeEntry = prop.begin.type;
                    valueEntry = prop.begin.value;
                    break;
                case "end":
                    typeEntry = prop.end.type;
                    valueEntry = prop.end.value;
                    break;
                case "top":
                    typeEntry = prop.top.type;
                    valueEntry = prop.top.value.toString();
                    break;
                case "middle":
                    typeEntry = prop.middle.type;
                    valueEntry = prop.middle.value.toString();
                    break;
                case "bottom":
                    typeEntry = prop.bottom.type;
                    valueEntry = prop.bottom.value.toString();
                    break;
                case "day":
                    // returns array as string
                    typeEntry = prop.day.type;
                    valueEntry = prop.day.value.toString();
                    if (featureDataDTO.feature.contains("gas")) {
                        viUnit = "gas-" + prop.day.unit;
                    } else if (featureDataDTO.feature.contains("power")) {
                        viUnit = "power-" + prop.day.unit;
                    } else if (featureDataDTO.feature.contains(".heat.")) {
                        viUnit = "heat-" + prop.day.unit;
                    } else {
                        viUnit = prop.day.unit;
                    }
                    break;
                case "week":
                    // returns array as string
                    typeEntry = prop.week.type;
                    valueEntry = prop.week.value.toString();
                    if (featureDataDTO.feature.contains("gas")) {
                        viUnit = "gas-" + prop.week.unit;
                    } else if (featureDataDTO.feature.contains("power")) {
                        viUnit = "power-" + prop.week.unit;
                    } else if (featureDataDTO.feature.contains(".heat.")) {
                        viUnit = "heat-" + prop.week.unit;
                    } else {
                        viUnit = prop.week.unit;
                    }
                    break;
                case "month":
                    // returns array as string
                    typeEntry = prop.month.type;
                    valueEntry = prop.month.value.toString();
                    if (featureDataDTO.feature.contains("gas")) {
                        viUnit = "gas-" + prop.month.unit;
                    } else if (featureDataDTO.feature.contains("power")) {
                        viUnit = "power-" + prop.month.unit;
                    } else if (featureDataDTO.feature.contains(".heat.")) {
                        viUnit = "heat-" + prop.month.unit;
                    } else {
                        viUnit = prop.month.unit;
                    }
                    break;
                case "year":
                    // returns array as string
                    typeEntry = prop.year.type;
                    valueEntry = prop.year.value.toString();
                    if (featureDataDTO.feature.contains("gas")) {
                        viUnit = "gas-" + prop.year.unit;
                    } else if (featureDataDTO.feature.contains("power")) {
                        viUnit = "power-" + prop.year.unit;
                    } else if (featureDataDTO.feature.contains(".heat.")) {
                        viUnit = "heat-" + prop.year.unit;
                    } else {
                        viUnit = prop.year.unit;
                    }
                    break;
                case "starts":
                    typeEntry = prop.starts.type;
                    valueEntry = prop.starts.value.toString();
                    viUnit = prop.starts.unit;
                    break;
                case "hours":
                    typeEntry = "duration";
                    valueEntry = prop.hours.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassOne":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassOne.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassTwo":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassTwo.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassThree":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassThree.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassFour":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassFour.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassFive":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassFive.value.toString();
                    viUnit = "hour";
                    break;
                case "min":
                    typeEntry = prop.min.type;
                    valueEntry = prop.min.value.toString();
                    viUnit = prop.min.unit;
                    break;
                case "max":
                    typeEntry = prop.max.type;
                    valueEntry = prop.max.value.toString();
                    viUnit = prop.max.unit;
                    break;
                case "phase":
                    typeEntry = prop.phase.type;
                    valueEntry = prop.phase.value;
                    viUnit = "";
                    break;
                case "switchOnValue":
                    typeEntry = prop.switchOnValue.type;
                    valueEntry = prop.switchOnValue.value;
                    viUnit = prop.switchOffValue.unit;
                    break;
                case "switchOffValue":
                    typeEntry = prop.switchOffValue.type;
                    valueEntry = prop.switchOffValue.value;
                    viUnit = prop.switchOffValue.unit;
                    break;
                case "enabled":
                    typeEntry = prop.enabled.type;
                    JsonElement v = prop.enabled.value;
                    if ("array".equals(typeEntry)) {
                        valueEntry = v.toString();
                    }
                    if ("boolean".equals(typeEntry)) {
                        valueEntry = v.getAsBoolean() ? "true" : "false";
                        bool = v.getAsBoolean();
                    }
                    break;
                default:
                    break;
            }
            msg.setType(typeEntry);
            msg.setValue(valueEntry);
            msg.setChannelType(typeEntry);

            if ("[]".equals(valueEntry)) {
                return;
            }

            if (msg.getDeviceId().contains(config.deviceId)) {
                logger.trace("Feature: {} Type:{} Entry: {}={}", featureDataDTO.feature, typeEntry, entry, valueEntry);

                String subChannelType = "";

                if (viUnit != null) {
                    if (!viUnit.isEmpty()) {
                        msg.setUnit(viUnit);
                        unit = UNIT_MAP.get(viUnit);
                        subChannelType = SUB_CHANNEL_TYPE_MAP.getOrDefault(viUnit, "");
                        if (unit == null) {
                            logger.warn(
                                    "Unknown unit. Could not parse unit: {} of Feature: {} - Please open an issue on GitHub.",
                                    viUnit, featureDataDTO.feature);
                            return;
                        }
                    }
                }

                Channel channel = thing.getChannel(msg.getChannelId());
                if (channel == null) {
                    logger.trace("Channel does not exist -> Channel is being created");
                    createOrUpdateChannel(msg);
                } else {
                    updateChannelIfPropertiesChanged(channel, msg);

                    String channelDescription = channel.getDescription();
                    if (channelDescription != null) {
                        if (!channelDescription.equals(msg.getFeatureDescription())) {
                            logger.trace("Channel Description is different. The channel is now being updated.");
                            createOrUpdateChannel(msg);
                        }
                    }

                    String channelLabel = channel.getLabel();
                    if (channelLabel != null) {
                        if (!channelLabel.equals(msg.getFeatureName())) {
                            logger.trace("Channel Label is different. The channel is now being updated.");
                            createOrUpdateChannel(msg);
                        }
                    }
                }
                setStateDescriptionOptions(msg);

                ThingMessageDTO subMsg = new ThingMessageDTO();
                subMsg.setDeviceId(featureDataDTO.deviceId);
                subMsg.setFeatureClear(featureDataDTO.feature);
                subMsg.setFeatureDescription(getFeatureDescription(featureDataDTO.feature));
                subMsg.setFeatureName(getFeatureName(featureDataDTO.feature));
                subMsg.setType(typeEntry);
                subMsg.setValue(valueEntry);
                switch (entry) {
                    case "entries":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("produced");
                        subMsg.setChannelType("boolean-read-only");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "day":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("today");
                        subMsg.setChannelType(subChannelType);
                        checkIfSubChannelExists(subMsg);
                        subMsg.setSuffix("yesterday");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "week":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("this-week");
                        subMsg.setChannelType(subChannelType);
                        checkIfSubChannelExists(subMsg);
                        subMsg.setSuffix("last-week");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "month":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("this-month");
                        subMsg.setChannelType(subChannelType);
                        checkIfSubChannelExists(subMsg);
                        subMsg.setSuffix("last-month");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "year":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("this-year");
                        subMsg.setChannelType(subChannelType);
                        checkIfSubChannelExists(subMsg);
                        subMsg.setSuffix("last-year");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "active":
                        if (featureDataDTO.feature.contains("oneTimeCharge")) {
                            subMsg.setIsSubChannel(true);
                            subMsg.setSuffix("status");
                            subMsg.setChannelType("boolean-read-only");
                            checkIfSubChannelExists(subMsg);
                        }
                        break;
                    default:
                        break;
                }

                storedChannelValues.putProperty(msg.getChannelId(), msg.getValue());

                switch (typeEntry) {
                    case "decimal":
                    case "number":
                    case "temperature":
                    case "percent":
                    case "minute":
                    case "hours":
                    case "kelvin":
                    case "liter":
                    case "liter-per-minute":
                    case "bar":
                    case "power":
                    case "revolutions-per-second":
                    case "slope":
                    case "shift":
                    case "duration":
                    case "duration-minute":
                    case "thermal-energy":
                    case "cubic-meter-per-hour":
                        updateChannelState(msg.getChannelId(), msg.getValue(), unit);
                        break;
                    case "boolean":
                        OnOffType state = bool ? OnOffType.ON : OnOffType.OFF;
                        updateState(msg.getChannelId(), state);
                        if (featureDataDTO.feature.contains("oneTimeCharge")) {
                            updateState(subMsg.getChannelId(), state);
                        }
                        break;
                    case "house-heating-load":
                        updateState(msg.getChannelId(), new DecimalType(msg.getValue()));
                        break;
                    case "string":
                    case "array":
                        updateState(msg.getChannelId(), StringType.valueOf(msg.getValue()));
                        String[] parts = msg.getValue().replace("[", "").replace("]", "").replace(" ", "").split(",");
                        if (parts.length > 1) {
                            switch (entry) {
                                case "day":
                                    subMsg.setSuffix("today");
                                    updateChannelState(subMsg.getChannelId(), parts[0], unit);
                                    subMsg.setSuffix("yesterday");
                                    updateChannelState(subMsg.getChannelId(), parts[1], unit);
                                    break;
                                case "week":
                                    subMsg.setSuffix("this-week");
                                    updateChannelState(subMsg.getChannelId(), parts[0], unit);
                                    subMsg.setSuffix("last-week");
                                    updateChannelState(subMsg.getChannelId(), parts[1], unit);
                                    break;
                                case "month":
                                    subMsg.setSuffix("this-month");
                                    updateChannelState(subMsg.getChannelId(), parts[0], unit);
                                    subMsg.setSuffix("last-month");
                                    updateChannelState(subMsg.getChannelId(), parts[1], unit);
                                    break;
                                case "year":
                                    subMsg.setSuffix("this-year");
                                    updateChannelState(subMsg.getChannelId(), parts[0], unit);
                                    subMsg.setSuffix("last-year");
                                    updateChannelState(subMsg.getChannelId(), parts[1], unit);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    case "Schedule":
                        updateState(msg.getChannelId(), StringType.valueOf(msg.getValue()));
                        String channelId = msg.getChannelId().replace("#schedule", "#produced");
                        updateState(channelId, parseSchedule(msg.getValue()));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Creates new channels or updates existing channel for the thing.
     *
     * @param msg contains everything is needed of the channel to be created or updated.
     */
    private void createOrUpdateChannel(ThingMessageDTO msg) {
        getSafeCallback().ifPresent(cb -> {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), msg.getChannelId());
            ChannelTypeUID typeUID = new ChannelTypeUID(BINDING_ID, convertChannelType(msg));

            Map<String, String> properties = new HashMap<>();
            if (!msg.isSubChannel) {
                properties = buildProperties(msg);
            }

            if (msg.isSubChannel && checkSubChannelLabelAndDescription(msg)) {
                properties.put("feature", msg.getFeatureClear());
            }

            Channel channel = cb.createChannelBuilder(channelUID, typeUID)
                    .withLabel(nvl(msg.getFeatureName(), msg.getChannelId()))
                    .withDescription(nvl(msg.getFeatureDescription(), "")).withType(typeUID).withProperties(properties)
                    .build();

            Thing edited = editThing().withoutChannel(channelUID).withChannel(channel).build();
            updateThing(edited);
            logger.debug("{} {} created/updated on Thing {}", msg.isSubChannel ? "Sub-Channel" : "Channel", channelUID,
                    getThing().getUID());
        });
    }

    private Optional<ThingHandlerCallback> getSafeCallback() {
        ThingHandlerCallback cb = getCallback();
        if (cb == null) {
            logger.warn("ThingHandlerCallback is null for Thing {} (Thing not initialized?)", getThing().getUID());
            return Optional.empty();
        }
        return Optional.of(cb);
    }

    private static String nvl(@Nullable String a, String fallback) {
        return a != null ? a : fallback;
    }

    private boolean checkSubChannelLabelAndDescription(ThingMessageDTO msg) {
        boolean updateChannel = false;

        Channel channel = thing.getChannel(msg.getChannelId());

        if (channel != null) {
            String channelDescription = channel.getDescription();
            String channelLabel = channel.getLabel();

            if (channelDescription != null) {
                if (!channelDescription.equals(msg.getFeatureDescription())) {
                    logger.trace("Sub-Channel Description is different. The channel is now being updated.");
                    updateChannel = true;
                }
            }

            if (channelLabel != null) {
                if (!channelLabel.equals(msg.getFeatureName())) {
                    logger.trace("Sub-Channel Label is different. The channel is now being updated.");
                    updateChannel = true;
                }
            }
        } else {
            updateChannel = true;
        }
        return updateChannel;
    }

    private String getFeatureName(String feature) {
        Locale locale = localeProvider.getLocale();
        Pattern pattern = Pattern.compile("(\\.[0-3])");
        Matcher matcher = pattern.matcher(feature);
        if (matcher.find()) {
            String circuit = matcher.group(0);
            feature = matcher.replaceAll(".N");
            String featureName = Objects
                    .requireNonNull(i18Provider.getText(bundle, "viessmann.feature.name." + feature, feature, locale));
            return featureName + " (Circuit: " + circuit.replace(".", "") + ")";
        }
        return Objects
                .requireNonNull(i18Provider.getText(bundle, "viessmann.feature.name." + feature, feature, locale));
    }

    private @Nullable String getFeatureDescription(String feature) {
        Locale locale = localeProvider.getLocale();
        feature = feature.replaceAll("\\.[0-3]", ".N");
        return Objects
                .requireNonNull(i18Provider.getText(bundle, "viessmann.feature.description." + feature, "", locale));
    }

    private OnOffType parseSchedule(String scheduleJson) {
        ZonedDateTime now = ZonedDateTime.now(ViessmannUtil.getOpenHABZoneId());

        int hour = now.getHour();
        int minute = now.getMinute();
        int dayOfWeek = now.getDayOfWeek().getValue();

        Date currTime = parseTime(hour + ":" + minute);

        ScheduleDTO schedule = GSON.fromJson(scheduleJson, ScheduleDTO.class);
        if (schedule == null) {
            logger.warn("Could not create schedule object and determine day.");
            return OnOffType.OFF;
        }
        List<DaySchedule> day = schedule.getMon();
        switch (dayOfWeek) {
            case 1:
                day = schedule.getMon();
                break;
            case 2:
                day = schedule.getTue();
                break;
            case 3:
                day = schedule.getWed();
                break;
            case 4:
                day = schedule.getThu();
                break;
            case 5:
                day = schedule.getFri();
                break;
            case 6:
                day = schedule.getSat();
                break;
            case 7:
                day = schedule.getSun();
                break;
            default:
                break;
        }
        for (DaySchedule daySchedule : day) {
            Date startTime = parseTime(daySchedule.getStart());
            Date endTime = parseTime(daySchedule.getEnd());

            if (startTime.before(currTime) && endTime.after(currTime)) {
                return OnOffType.ON;
            }
        }
        return OnOffType.OFF;
    }

    private Date parseTime(String time) {
        final String inputFormat = "HH:mm";
        SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.ROOT);
        try {
            return inputParser.parse(time);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    private void updateChannelState(String channelId, String stateAsString, @Nullable String unit) {
        if (unit != null) {
            updateState(channelId, new QuantityType<>(stateAsString + " " + unit));
        } else {
            DecimalType s = DecimalType.valueOf(stateAsString);
            updateState(channelId, s);
        }
    }

    private Map<String, String> buildProperties(ThingMessageDTO msg) {
        Map<String, String> prop = new HashMap<>();

        String suffix = Objects.requireNonNullElse(msg.getSuffix(), "");
        String lcSuffix = suffix.toLowerCase(Locale.ROOT);

        prop.put("suffix", suffix);
        prop.put("feature", msg.getFeatureClear());
        prop.put("channelType", convertChannelType(msg));
        prop.put("circuitId", msg.getCircuitId());

        Map<String, FeatureCommand> commands = msg.getCommands();
        if (commands == null || commands.isEmpty()) {
            return prop;
        }

        for (String command : msg.getAllCommands()) {
            String lcCommand = command.toLowerCase(Locale.ROOT);

            // empty suffix → only one command allowed
            if (lcSuffix.isBlank() && prop.containsKey("command")) {
                continue;
            }

            FeatureCommand fc = commands.get(command);
            if (fc == null || fc.isDeprecated) {
                continue;
            }

            List<String> allParams = fc.getAllParams();
            Set<String> allowedSuffixes = new HashSet<>(BASE_ALLOWED_SUFFIXES);
            allowedSuffixes.addAll(allParams);

            if (!lcSuffix.isBlank() && !lcCommand.contains(lcSuffix) && !allowedSuffixes.contains(lcSuffix)) {
                continue;
            }

            for (String param : allParams) {
                prop.put(param + "Params", addProperties(prop, param + "Params", param));
                prop.put("params", addProperties(prop, "params", param));
                prop.put(fc.name + "Params", addProperties(prop, fc.name + "Params", param));

                FeatureCommandParams fcp = fc.params.get(param);
                if (fcp == null) {
                    continue;
                }

                Map<String, Object> constraints = fcp.getConstraints();
                prop.put(param + "RegEx", (String) constraints.getOrDefault("regEx", ""));
                prop.put(param + "Type", fcp.getType());
            }

            prop.put("command", addProperties(prop, "command", command));
            prop.put(fc.name + "Uri", fc.uri);
        }
        return prop;
    }

    private String addProperties(Map<String, String> properties, String key, String value) {
        String p = properties.get(key);
        if (p != null) {
            if (!p.contains(value)) {
                p = p + "," + value;
            }
            return p;
        }
        return value;
    }

    private String convertChannelType(ThingMessageDTO msg) {
        String channelType = msg.getChannelType();

        List<String> com = msg.getAllCommands();
        if (com == null) {
            return channelType.toLowerCase(Locale.ROOT);
        }
        if (!com.isEmpty() && !"boolean-read-only".equals(channelType)) {
            for (String command : com) {
                switch (command) {
                    case "setTemperature":
                    case "setTargetTemperature":
                        if (!"boolean".equals(channelType)) {
                            channelType = "set-temperature";
                        }
                        break;
                    case "setMin":
                        if (msg.getSuffix().contains("min")) {
                            channelType = "set-min";
                        }
                        break;
                    case "setMax":
                        if (msg.getSuffix().contains("max")) {
                            channelType = "set-max";
                        }
                        break;
                    case "setHysteresis", "setHysteresisSwitchOnValue":
                        channelType = "set-target-hysteresis";
                        break;
                    case "setHysteresisSwitchOffValue":
                        if (msg.getSuffix().contains("switchOffValue")) {
                            channelType = "set-hysteresis-off";
                        }
                        break;
                    case "setMode":
                        channelType = "set-mode";
                        break;
                    case "setSchedule":
                    case "setName":
                        if (msg.getSuffix().contains("active")) {
                            channelType = "boolean-read-only";
                        }
                        break;
                    case "active":
                        channelType = "boolean";
                        break;
                    default:
                        break;
                }
            }
        } else if ("boolean".equals(channelType)) {
            channelType = channelType + "-read-only";
        }
        return channelType.toLowerCase(Locale.ROOT);
    }

    private void setStateDescriptionOptions(ThingMessageDTO msg) {
        Locale locale = localeProvider.getLocale();
        List<StateOption> stateOptions = new ArrayList<>();

        Map<String, FeatureCommand> commands = msg.getCommands();
        if (commands == null || commands.isEmpty()) {
            return;
        }
        commands.forEach((name, command) -> {
            ArrayList<String> p = command.getAllParams();
            p.forEach(param -> {
                FeatureCommandParams fcp = command.params.get(param);
                Map<String, Object> constraints = fcp.getConstraints();
                if (constraints.containsKey("enumValue")) {
                    List<String> modes = Optional.ofNullable(command.params.get(param))
                            .map(FeatureCommandParams::getConstraints).map(c -> c.get("enumValue"))
                            .filter(List.class::isInstance).map(v -> (List<?>) v)
                            .map(l -> l.stream().filter(String.class::isInstance).map(String.class::cast).toList())
                            .orElse(List.of());

                    for (String cmd : modes) {
                        String commandLabel = Objects.requireNonNull(
                                i18Provider.getText(bundle, "viessmann.command.label." + cmd, cmd, locale));
                        StateOption stateOption = new StateOption(cmd, commandLabel);
                        stateOptions.add(stateOption);
                    }
                    ChannelUID channelUID = new ChannelUID(thing.getUID(), msg.getChannelId());
                    setChannelStateDescription(channelUID, stateOptions);
                }
            });
        });
    }

    private int getThingTypeVersion() {
        String versionString = thing.getProperties().get("thingTypeVersion");
        if (versionString == null) {
            logger.debug("Thing property 'thingTypeVersion' not set, assuming 0");
            return 0;
        }

        try {
            return Integer.parseInt(versionString);
        } catch (NumberFormatException e) {
            logger.debug("Invalid thingTypeVersion '{}', assuming 0", versionString);
            return 0;
        }
    }

    private boolean isManagedItem(String itemName) {
        boolean isManaged = managedItemProvider.getAll().stream().anyMatch(item -> item.getName().equals(itemName));

        if (isManaged) {
            return true; // item comes from UI / JSONDB
        }

        // fallback: treat as file-based
        logger.debug("Item '{}' not found in managed provider – treating as file-based.", itemName);
        return false;
    }

    private String require(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(key + " missing");
        }
        return value;
    }

    private void updateChannelIfPropertiesChanged(Channel channel, ThingMessageDTO msg) {
        Map<String, String> oldProperties = channel.getProperties();
        Map<String, String> newProperties = buildProperties(msg);

        if (!Objects.equals(oldProperties, newProperties)) {
            createOrUpdateChannel(msg);
            logger.debug("Channel {} properties updated: {}", channel.getUID(), newProperties);
        }
    }

    private void checkIfSubChannelExists(ThingMessageDTO msg) {
        Channel channel = thing.getChannel(msg.getChannelId());
        if (channel == null) {
            logger.trace("Sub-Channel does not exist -> Channel is being created");
            createOrUpdateChannel(msg);
        }
    }

    private boolean checkCommandType(Map<String, String> prop, String type) {
        String[] params = new String[] { "{}" };
        if (prop.containsKey("params")) {
            params = prop.get("params").split(",");
        }

        for (String p : params) {
            String propType = prop.get(p + "Type");
            if (propType == null) {
                return false;
            }
            if (propType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private record UriParam(String uri, String param) {
    }

    private boolean isQuantityCommand(String command) {
        return command.matches(".*(Temperature|temperature|setHysteresis|setMin|setMax).*");
    }
}
