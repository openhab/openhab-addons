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
package org.openhab.binding.km200.internal.handler;

import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.km200.internal.KM200ChannelTypeProvider;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.openhab.binding.km200.internal.KM200ThingType;
import org.openhab.binding.km200.internal.KM200Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KM200ThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200ThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KM200ThingHandler.class);
    private static URI configDescriptionUriChannel;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream
                    .of(THING_TYPE_DHW_CIRCUIT, THING_TYPE_HEATING_CIRCUIT, THING_TYPE_SOLAR_CIRCUIT,
                            THING_TYPE_HEAT_SOURCE, THING_TYPE_SYSTEM_APPLIANCE, THING_TYPE_SYSTEM_HOLIDAYMODES,
                            THING_TYPE_SYSTEM_SENSOR, THING_TYPE_GATEWAY, THING_TYPE_NOTIFICATION, THING_TYPE_SYSTEM)
                    .collect(Collectors.toSet()));

    private KM200ChannelTypeProvider channelTypeProvider;

    public KM200ThingHandler(Thing thing, KM200ChannelTypeProvider channelTypeProvider) {
        super(thing);
        this.channelTypeProvider = channelTypeProvider;
        try {
            configDescriptionUriChannel = new URI(CONFIG_DESCRIPTION_URI_CHANNEL);
        } catch (URISyntaxException ex) {
            logger.warn("Can't create ConfigDescription URI '{}', ConfigDescription for channels not avilable!",
                    CONFIG_DESCRIPTION_URI_CHANNEL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            return;
        }
        KM200GatewayHandler gateway = (KM200GatewayHandler) bridge.getHandler();
        if (gateway == null) {
            return;
        }
        Channel channel = getThing().getChannel(channelUID.getId());

        if (command instanceof DateTimeType || command instanceof DecimalType || command instanceof StringType
                || command instanceof OnOffType) {
            gateway.prepareMessage(this.getThing(), channel, command);
        } else if (command instanceof RefreshType) {
            gateway.refreshChannel(channel);
        } else {
            logger.warn("Unsupported Command: {} Class: {}", command.toFullString(), command.getClass());
        }
    }

    /**
     * Choose a tag for a channel
     */
    Set<String> checkTag(String unitOfMeasure, boolean readOnly) {
        Set<String> tags = new HashSet<String>();
        if (unitOfMeasure.indexOf("°C") == 0 || unitOfMeasure.indexOf("K") == 0) {
            if (readOnly) {
                tags.add("CurrentTemperature");
            } else {
                tags.add("TargetTemperature");
            }
        }
        return tags;
    }

    /**
     * Choose a category for a channel
     */
    String checkCategory(String unitOfMeasure, String topCategory, boolean readOnly) {
        String category = null;
        if (unitOfMeasure.indexOf("°C") == 0 || unitOfMeasure.indexOf("K") == 0) {
            if (unitOfMeasure.indexOf("°C") == 0 || unitOfMeasure.indexOf("K") == 0) {
                if (readOnly) {
                    category = "Temperature";
                } else {
                    category = "Heating";
                }
            }
        } else if (unitOfMeasure.indexOf("kW") == 0 || unitOfMeasure.indexOf("kWh") == 0) {
            category = "Energy";
        } else if (unitOfMeasure.indexOf("l/min") == 0 || unitOfMeasure.indexOf("l/h") == 0) {
            category = "Flow";
        } else if (unitOfMeasure.indexOf("Pascal") == 0 || unitOfMeasure.indexOf("bar") == 0) {
            category = "Pressure";
        } else if (unitOfMeasure.indexOf("rpm") == 0) {
            category = "Flow";
        } else if (unitOfMeasure.indexOf("mins") == 0 || unitOfMeasure.indexOf("minutes") == 0) {
            category = "Time";
        } else if (unitOfMeasure.indexOf("kg/l") == 0) {
            category = "Oil";
        } else if (unitOfMeasure.indexOf("%%") == 0) {
            category = "Number";
        } else {
            category = topCategory;
        }
        return category;
    }

    /**
     * Creates a new channel
     */
    Channel createChannel(ChannelTypeUID channelTypeUID, ChannelUID channelUID, String root, String type,
            String currentPathName, String description, String label, boolean addProperties, boolean switchProgram,
            StateDescription state, String unitOfMeasure) {
        Channel newChannel = null;
        Map<String, String> chProperties = new HashMap<>();
        String itemType = "";
        String category = null;
        if ("Number".equals(type)) {
            itemType = "NumberType";
            category = "Number";
        } else if ("String".equals(type)) {
            itemType = "StringType";
            category = "Text";
        }
        ChannelType channelType = new ChannelType(channelTypeUID, false, itemType, ChannelKind.STATE, label,
                description, checkCategory(unitOfMeasure, category, state.isReadOnly()),
                checkTag(unitOfMeasure, state.isReadOnly()), state, null, configDescriptionUriChannel);
        channelTypeProvider.addChannelType(channelType);

        chProperties.put("root", KM200Utils.translatesPathToName(root));
        if (switchProgram) {
            chProperties.put(SWITCH_PROGRAM_CURRENT_PATH_NAME, currentPathName);
        }
        if (addProperties) {
            newChannel = ChannelBuilder.create(channelUID, type).withType(channelTypeUID).withDescription(description)
                    .withLabel(label).withKind(ChannelKind.STATE).withProperties(chProperties).build();
        } else {
            newChannel = ChannelBuilder.create(channelUID, type).withType(channelTypeUID).withDescription(description)
                    .withLabel(label).withKind(ChannelKind.STATE).build();
        }
        return newChannel;
    }

    @Override
    public void initialize() {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            logger.debug("Bridge not existing");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        logger.debug("initialize, Bridge: {}", bridge);
        KM200GatewayHandler gateway = (KM200GatewayHandler) bridge.getHandler();
        if (gateway == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            logger.debug("Gateway not existing: {}", bridge);
            return;
        }
        String service = KM200Utils.translatesNameToPath(thing.getProperties().get("root"));
        synchronized (gateway.getDevice()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING);
            if (!gateway.getDevice().getInited()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                logger.debug("Bridge: not initialized: {}", bridge);
                return;
            }
            List<Channel> subChannels = new ArrayList<Channel>();
            if (gateway.getDevice().containsService(service)) {
                KM200ServiceObject serObj = gateway.getDevice().getServiceObject(service);
                addChannels(serObj, thing, subChannels, "");
            } else if (service.contains(SWITCH_PROGRAM_PATH_NAME)) {
                String currentPathName = thing.getProperties().get(SWITCH_PROGRAM_CURRENT_PATH_NAME);
                StateDescription state = new StateDescription(null, null, null, "%s", false,
                        KM200SwitchProgramServiceHandler.daysList);
                Channel newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + "weekday"),
                        new ChannelUID(thing.getUID(), "weekday"), service + "/" + "weekday", "String", currentPathName,
                        "Current selected weekday for cycle selection", "Weekday", true, true, state, "");
                subChannels.add(newChannel);

                state = new StateDescription(BigDecimal.valueOf(0), null, BigDecimal.valueOf(1), "%d", true, null);
                newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + "nbrCycles"),
                        new ChannelUID(thing.getUID(), "nbrCycles"), service + "/" + "nbrCycles", "Number",
                        currentPathName, "Number of switching cycles", "Number", true, true, state, "");
                subChannels.add(newChannel);

                state = new StateDescription(BigDecimal.valueOf(0), null, BigDecimal.valueOf(1), "%d", false, null);
                newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + "cycle"),
                        new ChannelUID(thing.getUID(), "cycle"), service + "/" + "cycle", "Number", currentPathName,
                        "Current selected cycle", "Cycle", true, true, state, "");
                subChannels.add(newChannel);

                state = new StateDescription(BigDecimal.valueOf(0), null, BigDecimal.valueOf(1), "%d minutes", false,
                        null);
                String posName = thing.getProperties().get(SWITCH_PROGRAM_POSITIVE);
                newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + posName),
                        new ChannelUID(thing.getUID(), posName), service + "/" + posName, "Number", currentPathName,
                        "Positive switch of the cycle, like 'Day' 'On'", posName, true, true, state, "minutes");
                subChannels.add(newChannel);

                String negName = thing.getProperties().get(SWITCH_PROGRAM_NEGATIVE);
                newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + negName),
                        new ChannelUID(thing.getUID(), negName), service + "/" + negName, "Number", currentPathName,
                        "Negative switch of the cycle, like 'Night' 'Off'", negName, true, true, state, "minutes");
                subChannels.add(newChannel);
            }
            ThingBuilder thingBuilder = editThing();
            List<Channel> actChannels = thing.getChannels();
            for (Channel channel : actChannels) {
                thingBuilder.withoutChannel(channel.getUID());
            }
            thingBuilder.withChannels(subChannels);
            updateThing(thingBuilder.build());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        channelTypeProvider.removeChannelTypesForThing(getThing().getUID());
    }

    /**
     * Checks whether a channel is linked to an item
     */
    public boolean checkLinked(Channel channel) {
        return isLinked(channel.getUID().getId());
    }

    /**
     * Search for services and add them to a list
     */
    private void addChannels(KM200ServiceObject serObj, Thing thing, List<Channel> subChannels, String subNameAddon) {
        String service = serObj.getFullServiceName();
        Set<String> subKeys = serObj.serviceTreeMap.keySet();
        List<String> asProperties = null;
        /* Some defines for dummy values, we will ignore such services */
        final BigDecimal maxInt16AsFloat = new BigDecimal(+3276.8).setScale(6, RoundingMode.HALF_UP);
        final BigDecimal minInt16AsFloat = new BigDecimal(-3276.8).setScale(6, RoundingMode.HALF_UP);
        final BigDecimal maxInt16AsInt = new BigDecimal(3200).setScale(4, RoundingMode.HALF_UP);
        for (KM200ThingType tType : KM200ThingType.values()) {
            String root = tType.getRootPath();
            if (root.compareTo(service) == 0) {
                asProperties = tType.asBridgeProperties();
            }
        }
        for (String subKey : subKeys) {
            if (asProperties != null) {
                if (asProperties.contains(subKey)) {
                    continue;
                }
            }
            Map<String, String> properties = new HashMap<>(1);
            String root = service + "/" + subKey;
            properties.put("root", KM200Utils.translatesPathToName(root));
            String subKeyType = serObj.serviceTreeMap.get(subKey).getServiceType();
            boolean readOnly;
            String unitOfMeasure = "";
            StateDescription state = null;
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(
                    thing.getUID().getAsString() + ":" + subNameAddon + subKey);
            Channel newChannel = null;
            ChannelUID channelUID = new ChannelUID(thing.getUID(), subNameAddon + subKey);
            if (serObj.serviceTreeMap.get(subKey).getWriteable() > 0) {
                readOnly = false;
            } else {
                readOnly = true;
            }
            if ("temperatures".compareTo(thing.getUID().getId()) == 0) {
                unitOfMeasure = "°C";
            }
            logger.debug("Create things: {} id: {} channel: {}", thing.getUID(), subKey, thing.getUID().getId());
            switch (subKeyType) {
                case "stringValue":
                    /* Creating an new channel type with capabilities from service */
                    List<@NonNull StateOption> options = null;
                    if (serObj.serviceTreeMap.get(subKey).getValueParameter() != null) {
                        options = new ArrayList<StateOption>();
                        // The type is definitely correct here
                        @SuppressWarnings("unchecked")
                        List<String> subValParas = (List<String>) serObj.serviceTreeMap.get(subKey).getValueParameter();
                        for (String para : subValParas) {
                            StateOption stateOption = new StateOption(para, para);
                            options.add(stateOption);
                        }
                    }
                    state = new StateDescription(null, null, null, "%s", readOnly, options);
                    newChannel = createChannel(channelTypeUID, channelUID, root, "String", null, subKey, subKey, true,
                            false, state, unitOfMeasure);
                    break;

                case "floatValue":
                    /*
                     * Check whether the value is a NaN. Usually all floats are BigDecimal. If it's a double then it's
                     * Double.NaN. In this case we are ignoring this channel.
                     */
                    BigDecimal minVal = null;
                    BigDecimal maxVal = null;
                    BigDecimal step = null;
                    BigDecimal val = null;
                    Object tmpVal = serObj.serviceTreeMap.get(subKey).getValue();
                    if (tmpVal instanceof Double) {
                        continue;
                    }
                    /* Check whether the value is a dummy (e.g. not connected sensor) */
                    val = (BigDecimal) serObj.serviceTreeMap.get(subKey).getValue();
                    if (val != null) {
                        if (val.setScale(6, RoundingMode.HALF_UP).equals(maxInt16AsFloat)
                                || val.setScale(6, RoundingMode.HALF_UP).equals(minInt16AsFloat)
                                || val.setScale(4, RoundingMode.HALF_UP).equals(maxInt16AsInt)) {
                            continue;
                        }
                    }
                    /* Check the capabilities of this service */
                    if (serObj.serviceTreeMap.get(subKey).getValueParameter() != null) {
                        /* Creating an new channel type with capabilities from service */
                        // The type is definitely correct here
                        @SuppressWarnings("unchecked")
                        List<Object> subValParas = (List<Object>) serObj.serviceTreeMap.get(subKey).getValueParameter();
                        minVal = (BigDecimal) subValParas.get(0);
                        maxVal = (BigDecimal) subValParas.get(1);
                        if (subValParas.size() > 2) {
                            unitOfMeasure = (String) subValParas.get(2);
                            if ("C".compareTo(unitOfMeasure) == 0) {
                                unitOfMeasure = "°C";
                            }
                        }
                        step = BigDecimal.valueOf(0.5);
                    }
                    state = new StateDescription(minVal, maxVal, step, "%.1f " + unitOfMeasure, readOnly, null);
                    newChannel = createChannel(channelTypeUID, channelUID, root, "Number", null, subKey, subKey, true,
                            false, state, unitOfMeasure);
                    break;

                case "refEnum":
                    /* Check whether the sub service should be ignored */
                    boolean ignoreIt = false;
                    for (KM200ThingType tType : KM200ThingType.values()) {
                        if (tType.getThingTypeUID().equals(thing.getThingTypeUID())) {
                            for (String ignore : tType.ignoreSubService()) {
                                if (ignore.equals(subKey)) {
                                    ignoreIt = true;
                                }
                            }
                        }
                    }
                    if (ignoreIt) {
                        continue;
                    }
                    /* Search for new services in sub path */
                    addChannels(serObj.serviceTreeMap.get(subKey), thing, subChannels, subKey + "_");
                    break;

                case "errorList":
                    if ("nbrErrors".equals(subKey) || "error".equals(subKey)) {
                        state = new StateDescription(null, null, null, "%.0f ", readOnly, null);
                        newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + subKey),
                                channelUID, root, "Number", null, subKey, subKey, true, false, state, unitOfMeasure);
                    } else if ("errorString".equals(subKey)) {
                        state = new StateDescription(null, null, null, "%s", readOnly, null);
                        newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + subKey),
                                channelUID, root, "String", null, "Error message", "Text", true, false, state,
                                unitOfMeasure);
                    }
                    break;
            }
            if (newChannel != null && state != null) {
                subChannels.add(newChannel);
            }
        }
    }
}
