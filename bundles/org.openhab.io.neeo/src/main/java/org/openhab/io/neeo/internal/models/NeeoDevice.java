/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.models;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The model representing a NEEO device
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDevice {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDevice.class);

    /** The thing uid */
    private final NeeoThingUID uid;

    /** The device type */
    private final NeeoDeviceType type;

    /** The manufacturer */
    private final String manufacturer;

    /** The name */
    private final String name;

    /** The channels */
    private final List<NeeoDeviceChannel> channels = new ArrayList<>();

    /** The device capabilities ('alwaysOn', etc) */
    private final List<String> deviceCapabilities = new ArrayList<>();

    /** The device timings */
    private final NeeoDeviceTiming timing;

    /**
     * The specific name for the device, if null - NEEO will default it based on the type (ie "ACCESSORY", etc)
     */

    private final @Nullable String specificName;

    /**
     * The icon name to assign. If null, NEEO will default it to a standard icon based on the type
     */
    private final @Nullable String iconName;

    /**
     * The driver version for the device
     */
    private final int driverVersion;

    /**
     * Creates the device from the given parameters
     *
     * @param thing the non-null thing
     * @param channels the non-null, possibly empty channels
     * @param type the device type
     * @param timing the possibly null device timings
     */
    public NeeoDevice(Thing thing, List<NeeoDeviceChannel> channels, NeeoDeviceType type,
            @Nullable NeeoDeviceTiming timing) {
        this(new NeeoThingUID(thing.getUID()), 0, type, "openHAB", thing.getLabel(), channels, timing, null, null,
                null);
    }

    /**
     * Creates the device from the given parameters
     *
     * @param uid the non-null uid
     * @param driverVersion the driver version for the device
     * @param type the non-null device type
     * @param manufacturer the non-empty manufacturer
     * @param name the non-empty name
     * @param channels the non-null, possibly empty list of channels
     * @param deviceTiming a possibly null device timings
     * @param deviceCapabilities a possibly null, possibly empty list of device capabilities
     * @param specificName a possibly null, possibly empty specific name
     * @param iconName a possibly null, possibly empty custom icon name
     */
    public NeeoDevice(NeeoThingUID uid, int driverVersion, NeeoDeviceType type, String manufacturer,
            @Nullable String name, List<NeeoDeviceChannel> channels, @Nullable NeeoDeviceTiming deviceTiming,
            @Nullable List<String> deviceCapabilities, @Nullable String specificName, @Nullable String iconName) {
        Objects.requireNonNull(uid, "UID is required");
        Objects.requireNonNull(type, "type is required");
        NeeoUtil.requireNotEmpty(manufacturer, "manufacturer is required");
        Objects.requireNonNull(channels, "channels is required");

        String powerSensorItem = null;
        final Set<String> uniqueLabels = new HashSet<>();
        final Map<Entry<String, ItemSubType>, Set<Integer>> uniqueIds = new HashMap<>();

        for (NeeoDeviceChannel channel : channels) {
            final String itemName = channel.getItemName();
            final Entry<String, ItemSubType> key = new AbstractMap.SimpleEntry<>(channel.getItemName(),
                    channel.getSubType());

            if (!uniqueIds.containsKey(key)) {
                uniqueIds.put(key, new HashSet<>());
            }
            final Set<Integer> ids = uniqueIds.get(key);

            final Integer channelNbr = channel.getChannelNbr();
            if (ids.contains(channelNbr)) {
                throw new IllegalArgumentException(
                        "Channel '" + itemName + "' didn't have a unique channel number: " + channelNbr);
            }
            ids.add(channelNbr);

            if (channel.getType() != NeeoCapabilityType.EXCLUDE && channel.getKind() == NeeoDeviceChannelKind.ITEM) {
                final String label = channel.getLabel();
                if (uniqueLabels.contains(label)) {
                    throw new IllegalArgumentException(
                            "Channel '" + itemName + "' didn't have a unique label: " + label);
                }
                uniqueLabels.add(label);
            }

            if (channel.getType() == NeeoCapabilityType.SENSOR_POWER) {
                if (powerSensorItem != null) {
                    throw new IllegalArgumentException(
                            "Only 1 channel should be defined as a NEEO POWER type.  Channels: " + itemName + " and "
                                    + powerSensorItem + " were power types");
                }
                powerSensorItem = itemName;
            }
        }

        for (Entry<Entry<String, ItemSubType>, Set<Integer>> entry : uniqueIds.entrySet()) {
            if (!entry.getValue().contains(1)) {
                throw new IllegalArgumentException(
                        "Channel " + entry.getKey() + " doesn't have the original channel nbr (1)");
            }
        }

        this.uid = uid;
        this.driverVersion = driverVersion;
        this.type = type;
        this.manufacturer = manufacturer;
        this.name = name == null || name.isEmpty() ? "(N/A)" : name;
        this.specificName = specificName;
        this.iconName = iconName;
        this.channels.addAll(channels);
        this.timing = deviceTiming == null ? new NeeoDeviceTiming() : deviceTiming;
        if (deviceCapabilities != null) {
            this.deviceCapabilities.addAll(deviceCapabilities);
        }
    }

    /**
     * Gets the {@link NeeoThingUID}
     *
     * @return the {@link NeeoThingUID}
     */
    public NeeoThingUID getUid() {
        return uid;
    }

    /**
     * Gets the device type
     *
     * @return the device type
     */
    public NeeoDeviceType getType() {
        return type;
    }

    /**
     * Gets the manufacturer
     *
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Gets the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the specific name assigned to the device
     *
     * @return a possibly null, possibly empty specific name
     */
    public @Nullable String getSpecificName() {
        return specificName;
    }

    /**
     * Returns the custom icon assigned to the device
     *
     * @return a possibly null, possibly empty icon name
     */
    public @Nullable String getIconName() {
        return iconName;
    }

    /**
     * Returns the driver version for the device
     *
     * @return a non-null driver version
     */
    public int getDriverVersion() {
        return driverVersion;
    }

    /**
     * Gets the channels (this is a disconnected array)
     *
     * @return the channels
     */
    public List<NeeoDeviceChannel> getChannels() {
        return Collections.unmodifiableList(channels);
    }

    /**
     * Gets the device timing or null if using the default values
     *
     * @return a possibly null {@link NeeoDeviceTiming}
     */
    public @Nullable NeeoDeviceTiming getDeviceTiming() {
        if (supportTiming(this)) {
            return timing;
        }
        return null;
    }

    /**
     * Gets the device capabilities
     *
     * @return a possibly empty list of device capabilities
     */
    public List<String> getDeviceCapabilities() {
        return Collections.unmodifiableList(deviceCapabilities);
    }

    /**
     * Returns all exposed channels
     *
     * @return a non-null array of {@link NeeoDeviceChannel}
     */
    public NeeoDeviceChannel[] getExposedChannels() {
        final List<NeeoDeviceChannel> exposedChannels = new ArrayList<>(channels.size());
        for (NeeoDeviceChannel channel : channels) {
            if (channel.getType() != NeeoCapabilityType.EXCLUDE && !channel.getType().toString().isEmpty()) {
                exposedChannels.add(channel);
            }
        }
        return exposedChannels.toArray(new NeeoDeviceChannel[0]);
    }

    /**
     * Determines if the given itemname has been exposed to the NEEO brain or not
     *
     * @param itemName a non-null, non-empty item name
     * @return true if exposed, false otherwise
     */
    public boolean isExposed(String itemName) {
        NeeoUtil.requireNotEmpty(itemName, "itemName cannot be empty");
        logger.trace("isExposed: {}", itemName);

        for (NeeoDeviceChannel channel : channels) {

            final boolean notExcluded = channel.getType() != NeeoCapabilityType.EXCLUDE;
            final boolean notEmpty = !channel.getType().toString().isEmpty();
            final boolean isItemMatch = itemName.equalsIgnoreCase(channel.getItemName());

            logger.trace("isExposed(channel): {} --- notExcluded({}) -- notEmpty({}) -- isItemMatch({}) -- {}",
                    itemName, notExcluded, notEmpty, isItemMatch, channel);
            if (notExcluded && notEmpty && isItemMatch) {
                return true;
            }
        }

        logger.trace("isExposed (FALSE): {}", itemName);
        return false;
    }

    /**
     * Gets the channel for the given item name (and channel number)
     *
     * @param itemName the non-empty item name
     * @param subType the non-null sub type
     * @param channelNbr the channel nbr
     * @return the channel or null if none found
     */
    public @Nullable NeeoDeviceChannel getChannel(String itemName, ItemSubType subType, int channelNbr) {
        NeeoUtil.requireNotEmpty(itemName, "itemName cannot be empty");
        Objects.requireNonNull(subType, "subType cannot be null");

        for (NeeoDeviceChannel channel : channels) {
            if (itemName.equalsIgnoreCase(channel.getItemName()) && channel.getSubType() == subType
                    && channel.getChannelNbr() == channelNbr) {
                return channel;
            }
        }
        return null;
    }

    /**
     * Merges the latest {@link Thing} into this device to create a new {@link NeeoDevice} that includes any changes
     * from when it was defined to now (like channels being added or removed) or a new label
     *
     * @param context the non-null service context
     * @return the new {@link NeeoDevice} or null if the {@link Thing} doesn't exist anymore
     */
    public @Nullable NeeoDevice merge(ServiceContext context) {
        Objects.requireNonNull(context, "context cannot be null");

        if (NeeoConstants.NEEOIO_BINDING_ID.equals(uid.getBindingId())) {
            return this;
        }

        final Thing thing = context.getThingRegistry().get(uid.asThingUID());
        if (thing == null) {
            return null;
        }

        final boolean exposeAll = context.isExposeAllThings();

        final Set<String> itemNames = new HashSet<>();
        final Set<String> existinglabels = new HashSet<>();
        final List<NeeoDeviceChannel> newChannels = new ArrayList<>();

        // Drop (by not adding them to the set/list) any channels that don't exist anymore
        channels.stream().filter(c -> {
            if (c.getKind() == NeeoDeviceChannelKind.TRIGGER) {
                return true;
            }
            try {
                context.getItemRegistry().getItem(c.getItemName());
                return true;
            } catch (ItemNotFoundException e) {
                return false;
            }
        }).forEach(c -> {
            itemNames.add(c.getItemName());
            newChannels.add(c);
            existinglabels.add(c.getLabel());
        });

        // Add any new channels
        for (Channel channel : thing.getChannels()) {
            final ChannelType channelType = context.getChannelTypeRegistry()
                    .getChannelType(channel.getChannelTypeUID());

            final Set<Item> linkedItems = context.getItemChannelLinkRegistry().getLinkedItems(uid);
            if (linkedItems != null) {
                for (Item item : linkedItems) {
                    if (!itemNames.contains(item.getName())) {
                        newChannels.addAll(NeeoDeviceChannel.from(item, channel, channelType,
                                exposeAll ? NeeoCapabilityType.guessType(channelType) : NeeoCapabilityType.EXCLUDE,
                                existinglabels));
                    }

                }
            }
        }

        // TODO (increase driver version?)
        return new NeeoDevice(uid, driverVersion, type, manufacturer, thing.getLabel(), newChannels, timing,
                deviceCapabilities, specificName, iconName);
    }

    /**
     * Helper method to determine if the device supports timing or not
     *
     * @param device a non-null device to check
     * @return true if it support timing, false otherwise
     */
    private static boolean supportTiming(NeeoDevice device) {
        Objects.requireNonNull(device, "device must not be null");
        return !NeeoDeviceType.EXCLUDE.equals(device.type) && !NeeoDeviceType.ACCESSOIRE.equals(device.type)
                && !NeeoDeviceType.LIGHT.equals(device.type);
    }
}
