/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.handler;

import static org.openhab.binding.km200.KM200BindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.km200.KM200ThingTypes;
import org.openhab.binding.km200.internal.KM200CommObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link KM200ThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200ThingHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(KM200ThingHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_DHW_CIRCUIT,
            THING_TYPE_HEATING_CIRCUIT, THING_TYPE_SOLAR_CIRCUIT, THING_TYPE_HEAT_SOURCE, THING_TYPE_SYSTEM_APPLIANCE,
            THING_TYPE_SYSTEM_SENSOR, THING_TYPE_GATEWAY, THING_TYPE_NOTIFICATION, THING_TYPE_SYSTEM);

    Boolean isInited = false;

    public KM200ThingHandler(Thing thing) {
        super(thing);
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        KM200GatewayHandler gateway = (KM200GatewayHandler) this.getBridge().getHandler();
        Channel channel = getThing().getChannel(channelUID.getId());
        Class<?> commandType = command.getClass();

        if (commandType.isAssignableFrom(DateTimeType.class) || commandType.isAssignableFrom(DecimalType.class)
                || commandType.isAssignableFrom(StringType.class) || commandType.isAssignableFrom(OnOffType.class)) {
            gateway.prepareMessage(this.getThing(), channel, command);

        } else if (commandType.isAssignableFrom(RefreshType.class)) {
            gateway.refreshChannel(channel);
        } else {
            logger.warn("Unsupported Command: {} Class: {}", command.toFullString(), command.getClass());
        }
    }

    @Override
    public void initialize() {
        if (isInited) {
            return;
        }
        logger.debug("initialize, Bridge: {}", this.getBridge());
        KM200GatewayHandler gateway = (KM200GatewayHandler) this.getBridge().getHandler();
        String service = KM200GatewayHandler.translatesNameToPath(thing.getProperties().get("root"));
        synchronized (gateway.device) {
            if (!gateway.device.getInited()) {
                logger.debug("Bridge: not inited");
                isInited = false;
                updateStatus(ThingStatus.OFFLINE);
                return;
            }
            List<Channel> subChannels = new ArrayList<Channel>();
            if (gateway.device.containsService(service)) {
                KM200CommObject serObj = gateway.device.getServiceObject(service);
                addChannels(serObj, thing, subChannels, "");
            } else if (service.contains(SWITCH_PROGRAM_PATH_NAME + "/" + SWITCH_PROGRAM_REPLACEMENT)) {
                Channel newChannel = null;
                String currentPathName = thing.getProperties().get(SWITCH_PROGRAM_CURRENT_PATH_NAME);
                String posName = thing.getProperties().get(SWITCH_PROGRAM_POSITIVE);
                String negName = thing.getProperties().get(SWITCH_PROGRAM_NEGATIVE);

                String subKey = "weekday";
                String root = service + "/" + subKey;
                Map<String, String> propertiesWeekday = new HashMap<>(2);
                propertiesWeekday.put(SWITCH_PROGRAM_CURRENT_PATH_NAME, currentPathName);
                propertiesWeekday.put("root", KM200GatewayHandler.translatesPathToName(root));

                newChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), subKey), "String")
                        .withType(new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_STRING_VALUE))
                        .withDescription("Current selected weekday for cycle selection").withLabel("Weekday")
                        .withKind(ChannelKind.STATE).withProperties(propertiesWeekday).build();
                subChannels.add(newChannel);
                subKey = "nbrCycles";
                root = service + "/" + subKey;
                Map<String, String> propertiesNumber = new HashMap<>(2);
                propertiesNumber.put(SWITCH_PROGRAM_CURRENT_PATH_NAME, currentPathName);
                propertiesNumber.put("root", KM200GatewayHandler.translatesPathToName(root));
                newChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), subKey), "Number")
                        .withType(new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_FLOAT_VALUE))
                        .withDescription("Number of switching cycles").withLabel("Number").withKind(ChannelKind.STATE)
                        .withProperties(propertiesNumber).build();
                subChannels.add(newChannel);
                subKey = "cycle";
                root = service + "/" + subKey;
                Map<String, String> propertiesCycle = new HashMap<>(2);
                propertiesCycle.put(SWITCH_PROGRAM_CURRENT_PATH_NAME, currentPathName);
                propertiesCycle.put("root", KM200GatewayHandler.translatesPathToName(root));
                newChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), subKey), "Number")
                        .withType(new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_FLOAT_VALUE))
                        .withDescription("Current selected cycle").withLabel("Cycle").withKind(ChannelKind.STATE)
                        .withProperties(propertiesCycle).build();
                subChannels.add(newChannel);
                subKey = posName;
                root = service + "/" + subKey;
                Map<String, String> propertiesPos = new HashMap<>(2);
                propertiesPos.put(SWITCH_PROGRAM_CURRENT_PATH_NAME, currentPathName);
                propertiesPos.put("root", KM200GatewayHandler.translatesPathToName(root));
                newChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), subKey), "Number")
                        .withType(new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_FLOAT_VALUE))
                        .withDescription("Positive switch of the cycle, like 'Day' 'On'").withLabel(posName)
                        .withKind(ChannelKind.STATE).withProperties(propertiesPos).build();
                subChannels.add(newChannel);
                subKey = negName;
                root = service + "/" + subKey;
                Map<String, String> propertiesNeg = new HashMap<>(2);
                propertiesNeg.put(SWITCH_PROGRAM_CURRENT_PATH_NAME, currentPathName);
                propertiesNeg.put("root", KM200GatewayHandler.translatesPathToName(root));
                newChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), subKey), "Number")
                        .withType(new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_FLOAT_VALUE))
                        .withDescription("Negative switch of the cycle, like 'Night' 'Off'").withLabel(negName)
                        .withKind(ChannelKind.STATE).withProperties(propertiesNeg).build();
                subChannels.add(newChannel);

            }
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(subChannels);
            updateThing(thingBuilder.build());
        }
        isInited = true;
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleRemoval() {
        logger.debug("Handle removed");
        this.updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            initialize();
        }
    }

    /**
     * Checks whether a channel is linked to an item
     *
     * @param channel
     */
    public Boolean checkLinked(Channel channel) {
        return isLinked(channel.getUID().getId());
    }

    /**
     * Search for services and add them to a list
     *
     * @param serObj
     * @param subChannels
     * @param subNameAddon
     */
    private void addChannels(KM200CommObject serObj, Thing thing, List<Channel> subChannels, String subNameAddon) {
        String service = serObj.getFullServiceName();
        Set<String> subKeys = serObj.serviceTreeMap.keySet();
        /* Some defines for dummy values, we will ignore such services */
        final BigDecimal maxInt16AsFloat = new BigDecimal(+3276.8).setScale(6, RoundingMode.HALF_UP);
        final BigDecimal minInt16AsFloat = new BigDecimal(-3276.8).setScale(6, RoundingMode.HALF_UP);
        final BigDecimal maxInt16AsInt = new BigDecimal(3200).setScale(4, RoundingMode.HALF_UP);

        for (String subKey : subKeys) {
            Map<String, String> properties = new HashMap<>(1);
            String root = service + "/" + subKey;
            properties.put("root", KM200GatewayHandler.translatesPathToName(root));
            String subKeyType = serObj.serviceTreeMap.get(subKey).getServiceType();
            logger.debug("Create things: {} channel: {}", thing.getUID(), subKey);
            if (subKeyType.equals("stringValue")) {
                /* We cannot add this limits to the channels yet, only on xml files possible */
                if (serObj.serviceTreeMap.get(subKey).getValueParameter() != null) {
                    @SuppressWarnings("unchecked")
                    List<String> subValParas = (List<String>) serObj.serviceTreeMap.get(subKey).getValueParameter();
                    if (serObj.serviceTreeMap.get(subKey).getWriteable() > 0) {
                        Map<String, String> switchProperties = new HashMap<>(1);
                        switchProperties.put("root", KM200GatewayHandler.translatesPathToName(root));
                        Channel newChannel = ChannelBuilder
                                .create(new ChannelUID(thing.getUID(), subNameAddon + subKey + "Switch"), "Switch")
                                .withType(
                                        new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_SWITCH_STRING_VALUE))
                                .withDescription(subKey).withLabel(subKey + " Switch").withKind(ChannelKind.STATE)
                                .withProperties(switchProperties).build();
                        subChannels.add(newChannel);
                    }
                }
                Channel newChannel = ChannelBuilder
                        .create(new ChannelUID(thing.getUID(), subNameAddon + subKey), "String")
                        .withType(new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_STRING_VALUE))
                        .withDescription(subKey).withLabel(subKey).withKind(ChannelKind.STATE)
                        .withProperties(properties).build();
                subChannels.add(newChannel);

            } else if (subKeyType.equals("floatValue")) {
                /* Check whether the value is a dummy (e.g. not connected sensor) */
                BigDecimal val = (BigDecimal) serObj.serviceTreeMap.get(subKey).getValue();
                if (val != null) {
                    if (val.setScale(6, RoundingMode.HALF_UP).equals(maxInt16AsFloat)
                            || val.setScale(6, RoundingMode.HALF_UP).equals(minInt16AsFloat)
                            || val.setScale(4, RoundingMode.HALF_UP).equals(maxInt16AsInt)) {
                        continue;
                    }
                }
                /* Check the capabilities of this service */
                if (serObj.serviceTreeMap.get(subKey).getValueParameter() != null) {
                    /* We cannot add this limits to the channels yet, only on xml files possible */
                    @SuppressWarnings("unchecked")
                    List<BigDecimal> subValParas = (List<BigDecimal>) serObj.serviceTreeMap.get(subKey)
                            .getValueParameter();
                    BigDecimal minVal = subValParas.get(0);
                    BigDecimal maxVal = subValParas.get(1);

                }
                Channel newChannel = ChannelBuilder
                        .create(new ChannelUID(thing.getUID(), subNameAddon + subKey), "Number")
                        .withType(new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_FLOAT_VALUE))
                        .withDescription(subKey).withLabel(subKey).withKind(ChannelKind.STATE)
                        .withProperties(properties).build();
                subChannels.add(newChannel);
            } else if (subKeyType.equals("refEnum")) {
                /* Check whether the subelement should be ignored */
                Boolean ignoreIt = false;
                for (KM200ThingTypes tType : KM200ThingTypes.values()) {
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
                /* Search for new services in subpath */
                addChannels(serObj.serviceTreeMap.get(subKey), thing, subChannels, subKey + "#");
            } else if (subKeyType.equals("errorList")) {
                Channel newChannel = null;
                switch (subKey) {
                    case "nbrErrors":
                    case "error":
                        newChannel = ChannelBuilder
                                .create(new ChannelUID(thing.getUID(), subNameAddon + subKey), "Number")
                                .withType(new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_FLOAT_VALUE))
                                .withDescription(subKey).withLabel(subKey).withKind(ChannelKind.STATE)
                                .withProperties(properties).build();
                        break;
                    case "errorString":
                        newChannel = ChannelBuilder
                                .create(new ChannelUID(thing.getUID(), subNameAddon + subKey), "String")
                                .withType(new ChannelTypeUID(thing.getUID().getBindingId(), CHANNEL_STRING_VALUE))
                                .withDescription("Error message").withLabel("Text").withKind(ChannelKind.STATE)
                                .withProperties(properties).build();
                        break;
                }
                subChannels.add(newChannel);
            }
        }
    }
}
