/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.handler;

import static org.openhab.binding.ihc.IhcBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.ihc.ws.datatypes.WSRFDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ChannelUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(IhcHandler.class);

    public static void addChannelsFromProjectFile(Thing thing, Document projectFile, List<Channel> thingChannels) {
        LOGGER.debug("Updating thing channels");

        if (projectFile != null) {
            try {
                NodeList nodes = projectFile.getElementsByTagName("product_dataline");

                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("dataline_input"),
                            "Switch", "inputs#", CHANNEL_TYPE_SWITCH, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("dataline_output"),
                            "Switch", "outputs#", CHANNEL_TYPE_SWITCH, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("airlink_input"), "Switch",
                            "inputs#", CHANNEL_TYPE_SWITCH, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("airlink_output"),
                            "Switch", "outputs#", CHANNEL_TYPE_SWITCH, thingChannels);
                    ChannelUtils.addChannelsFromProjectFile(thing, node.getElementsByTagName("resource_temperature"),
                            "Number", "temperatures#", CHANNEL_TYPE_NUMBER, thingChannels);
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

    public static @NonNull Configuration getChannelParameters(Thing thing, String channelId)
            throws IllegalArgumentException {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            return channel.getConfiguration();
        }
        throw new IllegalArgumentException("Invalid channelId");
    }

    public static Integer getResourceIdFromChannelParameters(Thing thing, String channelId)
            throws IllegalArgumentException {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Object resourceId = channel.getConfiguration().get(PARAM_RESOURCE_ID);
            if (resourceId != null) {
                try {
                    return ((BigDecimal) resourceId).intValue();
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
            return null;
        }
        throw new IllegalArgumentException("Invalid channelId");
    }

    public static Integer getPulseLengthFromChannelParameters(Thing thing, String channelId)
            throws IllegalArgumentException {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Object pulseLength = channel.getConfiguration().get(PARAM_PULSE_LENGTH);
            if (pulseLength != null) {
                try {
                    return ((BigDecimal) pulseLength).intValue();
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
            return null;
        }
        throw new IllegalArgumentException("Invalid channelId");
    }

    public static String getSpecialCommandFromChannelParameters(Thing thing, String channelId)
            throws IllegalArgumentException {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Object test = channel.getConfiguration().get(PARAM_SPECIAL_COMMAND);
            if (test != null) {
                try {
                    return (String) test;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
            return null;
        }
        throw new IllegalArgumentException("Invalid channelId");
    }

    public static Long getSerialNumberFromChannelParameters(Thing thing, String channelId)
            throws IllegalArgumentException {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Object serialNumber = channel.getConfiguration().get(PARAM_SERIAL_NUMBER);
            if (serialNumber != null) {
                try {
                    return ((BigDecimal) serialNumber).longValue();
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
            return null;
        }
        throw new IllegalArgumentException("Invalid channelId");
    }

    public static String getDirectionFromChannelParameters(Thing thing, String channelId)
            throws IllegalArgumentException {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Object direction = channel.getConfiguration().get(PARAM_DIRECTION);
            if (direction != null) {
                try {
                    return (String) direction;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
            return null;
        }
        throw new IllegalArgumentException("Invalid channelId");
    }

    public static boolean isChannelReadOnly(Thing thing, String channelId) throws IllegalArgumentException {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Object direction = channel.getConfiguration().get(PARAM_DIRECTION);
            if (direction != null) {
                if (DIRECTION_READ_ONLY.equals(direction)) {
                    return true;
                }
            }
            return false;
        }
        throw new IllegalArgumentException("Invalid channelId");
    }

    public static boolean isChannelWriteOnly(Thing thing, String channelId) throws IllegalArgumentException {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Object direction = channel.getConfiguration().get(PARAM_DIRECTION);
            if (direction != null) {
                if (DIRECTION_WRITE_ONLY.equals(direction)) {
                    return true;
                }
            }
            return false;
        }
        throw new IllegalArgumentException("Invalid channelId");
    }

    public static String getChannelTypeId(Thing thing, String channelId) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null) {
                return channelTypeUID.getId();
            }
        }
        return null;
    }

    public static Set<Integer> getAllChannelsResourceIds(Thing thing) {
        Set<Integer> resourceIds = new HashSet<>();

        thing.getChannels().forEach(c -> {
            Integer resourceId = ChannelUtils.getResourceIdFromChannelParameters(thing, c.getUID().getId());
            if (resourceId != null) {
                if (resourceId != 0) {
                    resourceIds.add(resourceId);
                } else {
                    String specialCommandsStr = ChannelUtils.getSpecialCommandFromChannelParameters(thing,
                            c.getUID().getId());
                    if (specialCommandsStr != null) {
                        try {
                            List<SpecialCommand> specialCommands = new SpecialCommandParser(specialCommandsStr)
                                    .getAllOutCommands();
                            for (SpecialCommand specialCommand : specialCommands) {
                                resourceId = specialCommand.getResourceId();
                                if (resourceId != 0) {
                                    resourceIds.add(resourceId);
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            LOGGER.warn("Illegal value found from channel '{}' special command '{}': {}", c.getUID(),
                                    specialCommandsStr, e.getMessage());
                        }
                    }
                }
            }
        });

        return resourceIds;
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
