/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal;

import static org.openhab.binding.ihc.internal.IhcBindingConstants.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.ihc.internal.config.ChannelParams;
import org.openhab.binding.ihc.internal.handler.IhcHandler;
import org.openhab.binding.ihc.internal.ws.datatypes.WSRFDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Generic methods related to openHAB channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ChannelUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(IhcHandler.class);

    public static Set<Integer> getAllChannelsResourceIds(Thing thing) {
        Set<Integer> resourceIds = new HashSet<>();

        thing.getChannels().forEach(c -> {
            ChannelParams params = new ChannelParams(c);
            if (params.getResourceId() != null && params.getResourceId() != 0) {
                resourceIds.add(params.getResourceId());
            }
        });

        return resourceIds;
    }

    public static Set<Integer> getAllTriggerChannelsResourceIds(Thing thing) {
        Set<Integer> resourceIds = new HashSet<>();

        thing.getChannels().forEach(c -> {
            ChannelParams params = new ChannelParams(c);
            if (params.getChannelTypeId() != null) {
                switch (params.getChannelTypeId()) {
                    case CHANNEL_TYPE_PUSH_BUTTON_TRIGGER:
                        if (params.getResourceId() != null && params.getResourceId() != 0) {
                            resourceIds.add(params.getResourceId());
                        }
                        break;
                }
            }
        });
        return resourceIds;
    }

    public static void addChannelsFromProjectFile(Thing thing, Document projectFile, List<Channel> thingChannels) {
        LOGGER.debug("Updating thing channels");

        if (projectFile != null) {
            try {
                NodeList nodes = projectFile.getElementsByTagName("product_dataline");

                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("dataline_input"),
                            "Switch", "input", CHANNEL_TYPE_SWITCH, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("dataline_output"),
                            "Switch", "output", CHANNEL_TYPE_SWITCH, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("resource_temperature"),
                            "Number", "temperature", CHANNEL_TYPE_NUMBER, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("resource_humidity_level"),
                            "Number", "humidity", CHANNEL_TYPE_NUMBER, thingChannels);
                }
            } catch (Exception e) {
                LOGGER.warn("Error occured when adding channels, reason: {}", e.getMessage(), e);
            }

            try {
                NodeList nodes = projectFile.getElementsByTagName("product_airlink");

                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("airlink_input"), "Switch",
                            "input", CHANNEL_TYPE_SWITCH, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("airlink_output"),
                            "Switch", "output", CHANNEL_TYPE_SWITCH, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("airlink_relay"), "Switch",
                            "output", CHANNEL_TYPE_SWITCH, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("airlink_dimming"),
                            "Dimmer", "output", CHANNEL_TYPE_SWITCH, thingChannels);
                }
            } catch (Exception e) {
                LOGGER.warn("Error occured when adding channels, reason: {}", e.getMessage(), e);
            }
        } else {
            LOGGER.warn("Project file data doesn't exist, can't automatically create channels!");
        }
    }

    public static void addControllerChannels(Thing thing, List<Channel> thingChannels) {
        if (thing != null && thingChannels != null) {
            Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_STATE), "String")
                    .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_CONTROLLER_STATE)).build();
            addOrUpdateChannel(channel, thingChannels);

            channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_SW_VERSION), "String")
                    .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_CONTROLLER_SW_VER)).build();
            addOrUpdateChannel(channel, thingChannels);

            channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_HW_VERSION), "String")
                    .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_CONTROLLER_HW_VER)).build();
            addOrUpdateChannel(channel, thingChannels);

            channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_UPTIME), "Number")
                    .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_CONTROLLER_UPTIME)).build();
            addOrUpdateChannel(channel, thingChannels);

            channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), CHANNEL_CONTROLLER_TIME), "DateTime")
                    .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_CONTROLLER_TIME)).build();
            addOrUpdateChannel(channel, thingChannels);
        }
    }

    public static void addRFDeviceChannels(Thing thing, List<WSRFDevice> devs, List<Channel> thingChannels) {
        if (thing != null && devs != null && thingChannels != null) {
            devs.forEach(d -> {
                String serialNumberHex = Long.toHexString(d.getSerialNumber());
                Configuration configuration = new Configuration();
                configuration.put("serialNumber", new Long(d.getSerialNumber()));

                // low battery
                String channelId = String.format("%s-lowBattery", serialNumberHex);
                String label = String.format("Low Battery - %s", serialNumberHex);

                Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), channelId), "Switch")
                        .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_RF_LOW_BATTERY))
                        .withConfiguration(configuration).withLabel(label).build();
                addOrUpdateChannel(channel, thingChannels);

                // signal level
                channelId = String.format("%s-signalStrength", serialNumberHex);
                label = String.format("Signal Strength - %s", serialNumberHex);

                channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), channelId), "String")
                        .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_RF_SIGNAL_STRENGTH))
                        .withConfiguration(configuration).withLabel(label).build();
                addOrUpdateChannel(channel, thingChannels);
            });
        }
    }

    public static void addChannelsFromProjectFile(Thing thing, NodeList nodes, String acceptedItemType, String group,
            String channelType, List<Channel> thingChannels) {
        if (thing != null && nodes != null && thingChannels != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                Element parent = (Element) nodes.item(i).getParentNode();
                Element parentParent = (Element) nodes.item(i).getParentNode().getParentNode();

                String parentName = parent.getAttribute("name");
                String parentPosition = parent.getAttribute("position");
                String parentParentName = parentParent.getAttribute("name");

                String resourceName = element.getAttribute("name");
                int resourceId = Integer.parseInt(element.getAttribute("id").replace("_0x", ""), 16);

                String description = createDescription(parentName, parentPosition, parentParentName, resourceName);
                ChannelUID channelUID = new ChannelUID(thing.getUID(), group + resourceId);
                ChannelTypeUID type = new ChannelTypeUID(BINDING_ID, channelType);
                Configuration configuration = new Configuration();
                configuration.put(PARAM_RESOURCE_ID, new Integer(resourceId));

                Channel channel = ChannelBuilder.create(channelUID, acceptedItemType).withConfiguration(configuration)
                        .withLabel(description).withType(type).build();
                addOrUpdateChannel(channel, thingChannels);
            }
        }
    }

    private static String createDescription(String parentName, String parentPosition, String parentParentName,
            String resourceName) {
        String description = "";
        if (StringUtils.isNotEmpty(parentParentName)) {
            description = parentParentName;
        }
        if (StringUtils.isNotEmpty(parentPosition)) {
            description += String.format(" - %s", parentPosition);
        }
        if (StringUtils.isNotEmpty(parentName)) {
            description += String.format(" - %s", parentName);
        }
        if (StringUtils.isNotEmpty(resourceName)) {
            description += String.format(" - %s", resourceName);
        }
        return description;
    }

    private static void addOrUpdateChannel(Channel newChannel, List<Channel> thingChannels) {
        removeChannelByUID(thingChannels, newChannel.getUID());
        thingChannels.add(newChannel);
    }

    private static void removeChannelByUID(List<Channel> thingChannels, ChannelUID channelUIDtoRemove) {
        Predicate<Channel> channelPredicate = c -> c.getUID().getId().equals(channelUIDtoRemove.getId());
        thingChannels.removeIf(channelPredicate);
    }
}
