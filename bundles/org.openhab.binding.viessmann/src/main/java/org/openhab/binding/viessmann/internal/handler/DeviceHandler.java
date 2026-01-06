/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.viessmann.internal.ViessmannDynamicStateDescriptionProvider;
import org.openhab.binding.viessmann.internal.api.ViessmannCommunicationException;
import org.openhab.binding.viessmann.internal.config.ThingsConfig;
import org.openhab.binding.viessmann.internal.dto.HeatingCircuit;
import org.openhab.binding.viessmann.internal.dto.ThingMessageDTO;
import org.openhab.binding.viessmann.internal.dto.ViessmannMessage;
import org.openhab.binding.viessmann.internal.dto.features.FeatureCommands;
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

    private ThingsConfig config = new ThingsConfig();

    private final Map<String, HeatingCircuit> heatingCircuits = new HashMap<>();

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

        logger.trace("ChannelUID: {} | Properties: {}", ch.getUID(), ch.getProperties());

        String uri = null;
        String param = null;
        boolean initState = false;
        int initStateDelay = 0;
        String[] com = commands.split(",");

        if (command instanceof OnOffType onOff) {
            uri = prop.get(onOff == OnOffType.ON ? "activateUri" : "deactivateUri");
            String feature = prop.get("feature");
            param = "{}";
            if (feature != null && feature.contains("oneTimeCharge")) {
                initState = true;
                initStateDelay = 2;
            }
        } else if (command instanceof DecimalType) {
            logger.trace("Received DecimalType Command for Channel {}", ch.getUID());
            for (String str : com) {
                if (str.contains("setCurve")) {
                    String circuitId = prop.get("circuitId");
                    HeatingCircuit heatingCircuit = heatingCircuits.get(circuitId);
                    if (heatingCircuit != null) {
                        String slope = heatingCircuit.getSlope();
                        String shift = heatingCircuit.getShift();
                        String value = command.toString();
                        if (ch.getUID().toString().contains("shift")) {
                            param = "{\"slope\":" + slope + ", \"shift\":" + value + "}";
                            heatingCircuit.setShift(value);
                        } else if (ch.getUID().toString().contains("slope")) {
                            param = "{\"slope\":" + value + ", \"shift\":" + shift + "}";
                            heatingCircuit.setSlope(value);
                        }
                        if (circuitId != null) {
                            heatingCircuits.put(circuitId, heatingCircuit);
                        }
                    }
                    uri = prop.get(str + "Uri");
                    break;
                }
                if (str.contains("setHysteresis")) {
                    uri = prop.get(str + "Uri");
                    param = "{\"" + prop.get(str + "Params") + "\":" + command.toString() + "}";
                    break;
                }

            }
        } else if (command instanceof QuantityType<?> value) {
            double f = value.doubleValue();
            for (String str : com) {
                if (str.matches(".*(Temperature|setHysteresis|setMin|setMax|temperature).*")) {
                    uri = prop.get(str + "Uri");
                    param = "{\"" + prop.get(str + "Params") + "\":" + f + "}";
                    break;
                }
            }
            logger.trace("Received QuantityType Command for Channel {} Command: {}", ch.getUID(), value.floatValue());
        } else if (command instanceof StringType) {
            String s = command.toString();
            for (String str : com) {
                uri = prop.get(str + "Uri");
                String paramKey = prop.get(str + "Params");
                param = s.startsWith("{") ? "{\"" + paramKey + "\":" + s + "}" : "{\"" + paramKey + "\":\"" + s + "\"}";
                break;
            }
            logger.trace("Received StringType Command for Channel {}", ch.getUID());
        }

        if (uri != null && param != null) {
            BridgeInterface bridgeInterface = getBridgeInterface();
            if (bridgeInterface == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error.bridge-type");
                return;
            }
            try {
                if (!bridgeInterface.setData(uri, param) || initState) {
                    scheduler.schedule(this::initChannelState, initStateDelay, TimeUnit.SECONDS);
                }
            } catch (ViessmannCommunicationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void handleUpdateChannel(ViessmannMessage msg) {
        logger.trace("handleUpdateChannel: {}", msg);
    }

    @Override
    public void handleUpdate(FeatureDataDTO featureDataDTO) {
        updateStatus(ThingStatus.ONLINE);
        ThingMessageDTO msg = new ThingMessageDTO();
        if (featureDataDTO.properties != null) {
            msg.setDeviceId(featureDataDTO.deviceId);
            msg.setFeatureClear(featureDataDTO.feature);
            msg.setFeatureDescription(getFeatureDescription(featureDataDTO.feature));
            FeatureCommands commands = featureDataDTO.commands;
            if (commands != null) {
                msg.setCommands(commands);
            }
            FeatureProperties prop = featureDataDTO.properties;
            List<String> entr = prop.getUsedEntries();
            if (!entr.isEmpty()) {
                for (String entry : entr) {
                    String valueEntry = "";
                    String typeEntry = "";
                    boolean bool = false;
                    String viUnit = "";
                    String unit = null;
                    HeatingCircuit heatingCircuit = new HeatingCircuit();
                    msg.setFeatureName(getFeatureName(featureDataDTO.feature));
                    msg.setSuffix(entry);
                    switch (entry) {
                        case "value":
                            viUnit = prop.value.unit;
                            typeEntry = switch (viUnit) {
                                case "celsius" -> "temperature";
                                case "percent", "kelvin", "liter", "bar" -> viUnit;
                                case "minute" -> "duration";
                                case "revolutionsPerSecond" -> "revolutions-per-second";
                                case "kilowattHour/year" -> "house-heating-load";
                                case "kiloJoule" -> "thermal-energy";
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
                            heatingCircuit.setSlope(prop.slope.value.toString());
                            heatingCircuit.setShift(prop.shift.value.toString());
                            heatingCircuits.put(msg.getCircuitId(), heatingCircuit);
                            break;
                        case "slope":
                            typeEntry = "slope";
                            valueEntry = prop.slope.value.toString();
                            heatingCircuit.setSlope(prop.slope.value.toString());
                            heatingCircuit.setShift(prop.shift.value.toString());
                            heatingCircuits.put(msg.getCircuitId(), heatingCircuit);
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
                        case "unit":
                            typeEntry = prop.unit.type;
                            valueEntry = prop.unit.value;
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
                        default:
                            break;
                    }
                    msg.setType(typeEntry);
                    msg.setValue(valueEntry);
                    msg.setChannelType(typeEntry);

                    if (msg.getDeviceId().contains(config.deviceId) && !"unit".equals(entry)) {
                        if (!"[]".equals(valueEntry)) {
                            logger.trace("Feature: {} Type:{} Entry: {}={}", featureDataDTO.feature, typeEntry, entry,
                                    valueEntry);

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
                                createOrUpdateChannel(msg, false);
                            } else {
                                Map<String, String> properties = channel.getProperties();
                                for (String propKey : PROPERTIES_URIS) {
                                    if (properties.containsKey(propKey)) {
                                        logger.trace("URI from Channel: {}={}", propKey, properties.get(propKey));
                                        Map<String, String> uris = msg.getCommands().getUris();
                                        logger.trace("URI from JSON response: {}={}", propKey, uris.get(propKey));
                                        if (!Objects.equals(properties.get(propKey), uris.get(propKey))) {
                                            logger.trace(
                                                    "The command URI is different. The channel is now being updated.");
                                            createOrUpdateChannel(msg, false);
                                        }
                                    }
                                }
                                String channelDescription = channel.getDescription();
                                if (channelDescription != null) {
                                    if (!channelDescription.equals(msg.getFeatureDescription())) {
                                        logger.trace(
                                                "Channel Description is different. The channel is now being updated.");
                                        createOrUpdateChannel(msg, false);
                                    }
                                }

                                String channelLabel = channel.getLabel();
                                if (channelLabel != null) {
                                    if (!channelLabel.equals(msg.getFeatureName())) {
                                        logger.trace("Channel Label is different. The channel is now being updated.");
                                        createOrUpdateChannel(msg, false);
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
                                    subMsg.setSuffix("produced");
                                    subMsg.setChannelType("boolean-read-only");
                                    createOrUpdateChannel(subMsg, true);
                                    break;
                                case "day":
                                    subMsg.setSuffix("today");
                                    subMsg.setChannelType(subChannelType);
                                    createOrUpdateChannel(subMsg, true);
                                    subMsg.setSuffix("yesterday");
                                    createOrUpdateChannel(subMsg, true);
                                    break;
                                case "week":
                                    subMsg.setSuffix("this-week");
                                    subMsg.setChannelType(subChannelType);
                                    createOrUpdateChannel(subMsg, true);
                                    subMsg.setSuffix("last-week");
                                    createOrUpdateChannel(subMsg, true);
                                    break;
                                case "month":
                                    subMsg.setSuffix("this-month");
                                    subMsg.setChannelType(subChannelType);
                                    createOrUpdateChannel(subMsg, true);
                                    subMsg.setSuffix("last-month");
                                    createOrUpdateChannel(subMsg, true);
                                    break;
                                case "year":
                                    subMsg.setSuffix("this-year");
                                    subMsg.setChannelType(subChannelType);
                                    createOrUpdateChannel(subMsg, true);
                                    subMsg.setSuffix("last-year");
                                    createOrUpdateChannel(subMsg, true);
                                    break;
                                case "active":
                                    if (featureDataDTO.feature.contains("oneTimeCharge")) {
                                        subMsg.setSuffix("status");
                                        subMsg.setChannelType("boolean-read-only");
                                        createOrUpdateChannel(subMsg, true);
                                    }
                                default:
                                    break;
                            }

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
                                case "thermal-energy":
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

                                    String[] parts = msg.getValue().replace("[", "").replace("]", "").replace(" ", "")
                                            .split(",");
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
            }
        }
    }

    /**
     * Creates new channels or updates existing channel for the thing.
     *
     * @param msg contains everything is needed of the channel to be created or updated.
     */
    private void createOrUpdateChannel(ThingMessageDTO msg, boolean isSubChannel) {
        getSafeCallback().ifPresent(cb -> {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), msg.getChannelId());
            ChannelTypeUID typeUID = new ChannelTypeUID(BINDING_ID, convertChannelType(msg));

            Map<String, String> properties = new HashMap<>();
            if (!isSubChannel) {
                properties = buildProperties(msg);
            }

            if (isSubChannel && checkSubChannelLabelAndDescription(msg)) {
                properties.put("feature", msg.getFeatureClear());
            }

            Channel channel = cb.createChannelBuilder(channelUID, typeUID)
                    .withLabel(nvl(msg.getFeatureName(), msg.getChannelId()))
                    .withDescription(nvl(msg.getFeatureDescription(), "")).withType(typeUID).withProperties(properties)
                    .build();

            Thing edited = editThing().withoutChannel(channelUID).withChannel(channel).build();
            updateThing(edited);
            logger.debug("Channel {} created/updated on Thing {}", channelUID, getThing().getUID());
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
        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR_OF_DAY); // Get hour in 24-hour format
        int minute = now.get(Calendar.MINUTE);

        Date currTime = parseTime(hour + ":" + minute);

        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        ScheduleDTO schedule = GSON.fromJson(scheduleJson, ScheduleDTO.class);
        if (schedule == null) {
            logger.warn("Could not create schedule object and determine day.");
            return OnOffType.OFF;
        }
        List<DaySchedule> day = schedule.getMon();
        switch (dayOfWeek) {
            case 2:
                day = schedule.getMon();
                break;
            case 3:
                day = schedule.getTue();
                break;
            case 4:
                day = schedule.getWed();
                break;
            case 5:
                day = schedule.getThu();
                break;
            case 6:
                day = schedule.getFri();
                break;
            case 7:
                day = schedule.getSat();
                break;
            case 1:
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
        SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat);
        try {
            return inputParser.parse(time);
        } catch (java.text.ParseException e) {
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
        prop.put("feature", msg.getFeatureClear());
        prop.put("channelType", convertChannelType(msg));
        FeatureCommands commands = msg.getCommands();
        if (commands != null) {
            List<String> com = commands.getUsedCommands();
            if (!com.isEmpty()) {
                for (String command : com) {
                    prop.put("command", addPropertiesCommands(prop, command));
                    switch (command) {
                        case "setName":
                            prop.put("setNameUri", commands.setName.uri);
                            prop.put("setNameParams", "name");
                            break;
                        case "setCurve":
                            prop.put("circuitId", msg.getCircuitId());
                            prop.put("setCurveUri", commands.setCurve.uri);
                            prop.put("setCurveParams", "slope,shift");
                            break;
                        case "setSchedule":
                            prop.put("setScheduleUri", commands.setSchedule.uri);
                            prop.put("setScheduleParams", "newSchedule");
                            break;
                        case "setMode":
                            prop.put("setModeUri", commands.setMode.uri);
                            prop.put("setModeParams", "mode");
                            break;
                        case "setTemperature":
                            prop.put("setTemperatureUri", commands.setTemperature.uri);
                            prop.put("setTemperatureParams", "targetTemperature");
                            break;
                        case "setTargetTemperature":
                            prop.put("setTargetTemperatureUri", commands.setTargetTemperature.uri);
                            prop.put("command", "setTargetTemperature");
                            prop.put("setTargetTemperatureParams", "temperature");
                            break;
                        case "activate":
                            prop.put("activateUri", commands.activate.uri);
                            prop.put("activateParams", "{}");
                            prop.put("deactivateParams", "{}");
                            break;
                        case "deactivate":
                            prop.put("deactivateUri", commands.deactivate.uri);
                            prop.put("activateParams", "{}");
                            prop.put("deactivateParams", "{}");
                            break;
                        case "changeEndDate":
                            prop.put("changeEndDateUri", commands.changeEndDate.uri);
                            prop.put("command", "changeEndDate,schedule,unschedule");
                            prop.put("changeEndDateParams", "end");
                            prop.put("scheduleParams", "start,end");
                            prop.put("unscheduleParams", "{}");
                            break;
                        case "schedule":
                            prop.put("scheduleUri", commands.schedule.uri);
                            prop.put("scheduleParams", "start,end");
                            break;
                        case "unschedule":
                            prop.put("unscheduleUri", commands.unschedule.uri);
                            prop.put("unscheduleParams", "{}");
                            break;
                        case "setMin":
                            if (msg.getSuffix().contains("min")) {
                                prop.put("setMinUri", commands.setMin.uri);
                                prop.put("command", "setMin");
                                prop.put("setMinParams", "temperature");
                            }
                            break;
                        case "setMax":
                            if (msg.getSuffix().contains("max")) {
                                prop.put("setMaxUri", commands.setMax.uri);
                                prop.put("command", "setMax");
                                prop.put("setMaxParams", "temperature");
                            }
                            break;
                        case "setHysteresis":
                            prop.put("setHysteresisUri", commands.setHysteresis.uri);
                            prop.put("command", "setHysteresis");
                            prop.put("setHysteresisParams", "hysteresis");
                            break;
                        case "setHysteresisSwitchOnValue":
                            if (msg.getSuffix().contains("switchOnValue")) {
                                prop.put("setHysteresisSwitchOnValueUri", commands.setHysteresisSwitchOnValue.uri);
                                prop.put("command", "setHysteresisSwitchOnValue");
                                prop.put("setHysteresisSwitchOnValueParams", "hysteresis");
                            }
                            break;
                        case "setHysteresisSwitchOffValue":
                            if (msg.getSuffix().contains("switchOffValue")) {
                                prop.put("setHysteresisSwitchOffValueUri", commands.setHysteresisSwitchOffValue.uri);
                                prop.put("command", "setHysteresisSwitchOffValue");
                                prop.put("setHysteresisSwitchOffValueParams", "hysteresis");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return prop;
    }

    private String addPropertiesCommands(Map<String, String> properties, String command) {
        String commands = properties.get("command");
        if (commands != null) {
            commands = commands + "," + command;
            return commands;
        }
        return command;
    }

    private String convertChannelType(ThingMessageDTO msg) {
        String channelType = msg.getChannelType();
        FeatureCommands commands = msg.getCommands();
        if (commands != null) {
            List<String> com = commands.getUsedCommands();
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
                        default:
                            break;
                    }
                }
            } else if ("boolean".equals(channelType)) {
                channelType = channelType + "-read-only";
            }
        } else if ("boolean".equals(channelType)) {
            channelType = channelType + "-read-only";
        }
        return channelType.toLowerCase();
    }

    private void setStateDescriptionOptions(ThingMessageDTO msg) {
        if ("set-mode".equals(convertChannelType(msg))) {
            List<String> modes = msg.commands.setMode.params.mode.constraints.enumValue;
            if (modes != null) {
                Locale locale = localeProvider.getLocale();
                List<StateOption> stateOptions = new ArrayList<>();
                for (String command : modes) {
                    String commandLabel = Objects.requireNonNull(
                            i18Provider.getText(bundle, "viessmann.command.label." + command, command, locale));
                    StateOption stateOption = new StateOption(command, commandLabel);
                    stateOptions.add(stateOption);
                }
                ChannelUID channelUID = new ChannelUID(thing.getUID(), msg.getChannelId());
                setChannelStateDescription(channelUID, stateOptions);
            }
        }
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
}
