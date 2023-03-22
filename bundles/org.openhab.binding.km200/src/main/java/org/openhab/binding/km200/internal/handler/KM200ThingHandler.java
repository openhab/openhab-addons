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
package org.openhab.binding.km200.internal.handler;

import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.km200.internal.KM200ChannelTypeProvider;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.openhab.binding.km200.internal.KM200ThingType;
import org.openhab.binding.km200.internal.KM200Utils;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KM200ThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public class KM200ThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KM200ThingHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(THING_TYPE_DHW_CIRCUIT, THING_TYPE_HEATING_CIRCUIT, THING_TYPE_SOLAR_CIRCUIT, THING_TYPE_HEAT_SOURCE,
                    THING_TYPE_SYSTEM_APPLIANCE, THING_TYPE_SYSTEM_HOLIDAYMODES, THING_TYPE_SYSTEM_SENSOR,
                    THING_TYPE_GATEWAY, THING_TYPE_NOTIFICATION, THING_TYPE_SYSTEM, THING_TYPE_SYSTEMSTATES)
            .collect(Collectors.toSet()));

    private final KM200ChannelTypeProvider channelTypeProvider;

    public KM200ThingHandler(Thing thing, KM200ChannelTypeProvider channelTypeProvider) {
        super(thing);
        this.channelTypeProvider = channelTypeProvider;
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
        if (null != channel) {
            if (command instanceof DateTimeType || command instanceof DecimalType || command instanceof StringType
                    || command instanceof OnOffType) {
                gateway.prepareMessage(this.getThing(), channel, command);
            } else if (command instanceof RefreshType) {
                gateway.refreshChannel(channel);
            } else {
                logger.warn("Unsupported Command: {} Class: {}", command.toFullString(), command.getClass());
            }
        }
    }

    /**
     * Choose a category for a channel
     */
    String checkCategory(String unitOfMeasure, String topCategory, @Nullable Boolean readOnly) {
        String category = null;
        if (unitOfMeasure.indexOf("°C") == 0 || unitOfMeasure.indexOf("K") == 0) {
            if (null == readOnly) {
                category = topCategory;
            } else {
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
    @Nullable
    Channel createChannel(ChannelTypeUID channelTypeUID, ChannelUID channelUID, String root, String type,
            @Nullable String currentPathName, String description, String label, boolean addProperties,
            boolean switchProgram, StateDescriptionFragment state, String unitOfMeasure) {
        Channel newChannel = null;
        ChannelType channelType = null;
        Map<String, String> chProperties = new HashMap<>();
        String category = null;
        if (CoreItemFactory.NUMBER.equals(type)) {
            category = "Number";
        } else if (CoreItemFactory.STRING.equals(type)) {
            category = "Text";
        } else {
            logger.info("Channeltype {} not supported", type);
            return null;
        }
        channelType = ChannelTypeBuilder.state(channelTypeUID, label, type) //
                .withDescription(description) //
                .withCategory(checkCategory(unitOfMeasure, category, state.isReadOnly())) //
                .withStateDescriptionFragment(state).build();

        channelTypeProvider.addChannelType(channelType);

        chProperties.put("root", KM200Utils.translatesPathToName(root));
        if (null != currentPathName && switchProgram) {
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
        if (service == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "root property missing");
            return;
        }
        synchronized (gateway.getDevice()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING);
            if (!gateway.getDevice().getInited()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                logger.debug("Bridge: not initialized: {}", bridge);
                return;
            }
            List<Channel> subChannels = new ArrayList<>();
            if (gateway.getDevice().containsService(service)) {
                KM200ServiceObject serObj = gateway.getDevice().getServiceObject(service);
                if (null != serObj) {
                    addChannels(serObj, thing, subChannels, "");
                }
            } else if (service.contains(SWITCH_PROGRAM_PATH_NAME)) {
                String currentPathName = thing.getProperties().get(SWITCH_PROGRAM_CURRENT_PATH_NAME);
                StateDescriptionFragment state = StateDescriptionFragmentBuilder.create().withPattern("%s")
                        .withOptions(KM200SwitchProgramServiceHandler.daysList).build();
                Channel newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + "weekday"),
                        new ChannelUID(thing.getUID(), "weekday"), service + "/" + "weekday", CoreItemFactory.STRING,
                        currentPathName, "Current selected weekday for cycle selection", "Weekday", true, true, state,
                        "");
                if (null == newChannel) {
                    logger.warn("Creation of the channel {} was not possible", thing.getUID());
                } else {
                    subChannels.add(newChannel);
                }

                state = StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.ZERO).withStep(BigDecimal.ONE)
                        .withPattern("%d").withReadOnly(true).build();
                newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + "nbrCycles"),
                        new ChannelUID(thing.getUID(), "nbrCycles"), service + "/" + "nbrCycles",
                        CoreItemFactory.NUMBER, currentPathName, "Number of switching cycles", "Number", true, true,
                        state, "");
                if (null == newChannel) {
                    logger.warn("Creation of the channel {} was not possible", thing.getUID());
                } else {
                    subChannels.add(newChannel);
                }

                state = StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.ZERO).withStep(BigDecimal.ONE)
                        .withPattern("%d").build();
                newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + "cycle"),
                        new ChannelUID(thing.getUID(), "cycle"), service + "/" + "cycle", CoreItemFactory.NUMBER,
                        currentPathName, "Current selected cycle", "Cycle", true, true, state, "");
                if (null == newChannel) {
                    logger.warn("Creation of the channel {} was not possible", thing.getUID());
                } else {
                    subChannels.add(newChannel);
                }

                state = StateDescriptionFragmentBuilder.create().withMinimum(BigDecimal.ZERO).withStep(BigDecimal.ONE)
                        .withPattern("%d minutes").build();
                String posName = thing.getProperties().get(SWITCH_PROGRAM_POSITIVE);
                if (posName == null) {
                    newChannel = null;
                } else {
                    newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + posName),
                            new ChannelUID(thing.getUID(), posName), service + "/" + posName, CoreItemFactory.NUMBER,
                            currentPathName, "Positive switch of the cycle, like 'Day' 'On'", posName, true, true,
                            state, "minutes");
                }
                if (null == newChannel) {
                    logger.warn("Creation of the channel {} was not possible", thing.getUID());
                } else {
                    subChannels.add(newChannel);
                }

                String negName = thing.getProperties().get(SWITCH_PROGRAM_NEGATIVE);
                if (negName == null) {
                    newChannel = null;
                } else {
                    newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + negName),
                            new ChannelUID(thing.getUID(), negName), service + "/" + negName, CoreItemFactory.NUMBER,
                            currentPathName, "Negative switch of the cycle, like 'Night' 'Off'", negName, true, true,
                            state, "minutes");
                }
                if (null == newChannel) {
                    logger.warn("Creation of the channel {} was not possible", thing.getUID());
                } else {
                    subChannels.add(newChannel);
                }
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
            StateDescriptionFragment state = null;
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
            logger.trace("Create things: {} id: {} channel: {}", thing.getUID(), subKey, thing.getUID().getId());
            switch (subKeyType) {
                case DATA_TYPE_STRING_VALUE:
                    /* Creating a new channel type with capabilities from service */
                    List<StateOption> options = null;
                    if (serObj.serviceTreeMap.get(subKey).getValueParameter() != null) {
                        options = new ArrayList<>();
                        // The type is definitely correct here
                        @SuppressWarnings("unchecked")
                        List<String> subValParas = (List<String>) serObj.serviceTreeMap.get(subKey).getValueParameter();
                        if (null != subValParas) {
                            for (String para : subValParas) {
                                StateOption stateOption = new StateOption(para, para);
                                options.add(stateOption);
                            }
                        }
                    }
                    StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create().withPattern("%s")
                            .withReadOnly(readOnly);
                    if (options != null && !options.isEmpty()) {
                        builder.withOptions(options);
                    }
                    state = builder.build();
                    newChannel = createChannel(channelTypeUID, channelUID, root, CoreItemFactory.STRING, null, subKey,
                            subKey, true, false, state, unitOfMeasure);
                    break;
                case DATA_TYPE_FLOAT_VALUE:
                    /*
                     * Check whether the value is a NaN. Usually all floats are BigDecimal. If it's a double then it's
                     * Double.NaN. In this case we are ignoring this channel.
                     */
                    BigDecimal minVal = null;
                    BigDecimal maxVal = null;
                    BigDecimal step = null;
                    final BigDecimal val;
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
                        /* Creating a new channel type with capabilities from service */
                        // The type is definitely correct here
                        @SuppressWarnings("unchecked")
                        List<Object> subValParas = (List<Object>) serObj.serviceTreeMap.get(subKey).getValueParameter();
                        if (null != subValParas) {
                            minVal = (BigDecimal) subValParas.get(0);
                            maxVal = (BigDecimal) subValParas.get(1);
                            if (subValParas.size() > 2) {
                                unitOfMeasure = (String) subValParas.get(2);
                                if ("C".equals(unitOfMeasure)) {
                                    unitOfMeasure = "°C";
                                }
                            }
                            step = BigDecimal.valueOf(0.5);
                        }
                    }
                    builder = StateDescriptionFragmentBuilder.create().withPattern("%.1f " + unitOfMeasure)
                            .withReadOnly(readOnly);
                    if (minVal != null) {
                        builder.withMinimum(minVal);
                    }
                    if (maxVal != null) {
                        builder.withMaximum(maxVal);
                    }
                    if (step != null) {
                        builder.withStep(step);
                    }
                    state = builder.build();
                    newChannel = createChannel(channelTypeUID, channelUID, root, CoreItemFactory.NUMBER, null, subKey,
                            subKey, true, false, state, unitOfMeasure);
                    break;
                case DATA_TYPE_REF_ENUM:
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
                    KM200ServiceObject obj = serObj.serviceTreeMap.get(subKey);
                    if (obj != null) {
                        addChannels(obj, thing, subChannels, subKey + "_");
                    }
                    break;
                case DATA_TYPE_ERROR_LIST:
                    if ("nbrErrors".equals(subKey) || "error".equals(subKey)) {
                        state = StateDescriptionFragmentBuilder.create().withPattern("%.0f").withReadOnly(readOnly)
                                .build();
                        newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + subKey),
                                channelUID, root, CoreItemFactory.NUMBER, null, subKey, subKey, true, false, state,
                                unitOfMeasure);
                    } else if ("errorString".equals(subKey)) {
                        state = StateDescriptionFragmentBuilder.create().withPattern("%s").withReadOnly(readOnly)
                                .build();
                        newChannel = createChannel(new ChannelTypeUID(thing.getUID().getAsString() + ":" + subKey),
                                channelUID, root, CoreItemFactory.STRING, null, "Error message", "Text", true, false,
                                state, unitOfMeasure);
                    }
                    break;
            }
            if (newChannel != null && state != null) {
                subChannels.add(newChannel);
            }
        }
    }
}
