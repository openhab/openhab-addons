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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * The model representing a Neeo Channel (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceChannel {

    /** The channel kind */
    private final NeeoDeviceChannelKind kind;

    /** The item name */
    private final String itemName;

    /** The channel number */
    private final int channelNbr;

    /** The item subtype id (the subtype allows multiple channels for the same itemName/channelNbr) */
    private final ItemSubType subType;

    /** The capability type */
    private final NeeoCapabilityType type;

    /** The label */
    private final String label;

    /** The action/text value */
    private final @Nullable String value; // could be either a format (text label) or a value to send (button)

    /** The device channel range */
    private final NeeoDeviceChannelRange range;

    /**
     * Create a new channel based on the parms
     *
     * @param kind the non-null kind of channel
     * @param itemName the non-empty item name
     * @param channelNbr the channel number (must be >= 0)
     * @param type the non-null type
     * @param subType the non-null subtype
     * @param label the non-empty label
     * @param value the possibly null, possibly empty value
     * @param range the possibly null range
     */
    public NeeoDeviceChannel(NeeoDeviceChannelKind kind, String itemName, int channelNbr, NeeoCapabilityType type,
            ItemSubType subType, String label, @Nullable String value, @Nullable NeeoDeviceChannelRange range) {
        Objects.requireNonNull(kind, "kind cannot be null");
        NeeoUtil.requireNotEmpty(itemName, "itemName is required");
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(subType, "subType is required");
        if (channelNbr < 0) {
            throw new IllegalArgumentException("channelNbr must be >= 0");
        }

        this.kind = kind;
        this.itemName = itemName;
        this.channelNbr = channelNbr;
        this.type = type;
        this.subType = subType;
        this.label = label;
        this.value = value;
        this.range = range == null ? NeeoDeviceChannelRange.DEFAULT : range;
    }

    /**
     * Create a list of {@link NeeoDeviceChannel} from the given channel, capability type, sub type and labels
     *
     * @param channel a non-null channel
     * @param capabilityType a non-null capability type
     * @param subType a non-null sub type
     * @param existingLabels a non-null, possibly empty set of existing labels
     * @return a non-null, possibly empty list of device channels
     */
    public static List<NeeoDeviceChannel> from(Channel channel, NeeoCapabilityType capabilityType, ItemSubType subType,
            Set<String> existingLabels) {
        Objects.requireNonNull(channel);
        Objects.requireNonNull(capabilityType);
        Objects.requireNonNull(subType);
        Objects.requireNonNull(existingLabels);

        final ChannelUID uid = channel.getUID();
        return Arrays.asList(new NeeoDeviceChannel(NeeoDeviceChannelKind.get(channel.getKind()), uid.getId(), 1,
                capabilityType, subType, NeeoUtil.getUniqueLabel(existingLabels, uid.getIdWithoutGroup()), "", null));
    }

    /**
     * Create a list of {@link NeeoDeviceChannel} from the given item, channel, channel type, capability type and labels
     *
     * @param item a non-null item
     * @param channel a possibly null channel
     * @param channelType a possibly null channel type
     * @param capabilityType a non-null capability type
     * @param existingLabels a non-null, possibly empty set of existing labels
     * @return a non-null, possibly empty list of device channels
     */
    public static List<NeeoDeviceChannel> from(Item item, @Nullable Channel channel, @Nullable ChannelType channelType,
            NeeoCapabilityType capabilityType, Set<String> existingLabels) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(capabilityType);
        Objects.requireNonNull(existingLabels);

        if (item.getAcceptedDataTypes().contains(HSBType.class)) {
            return Arrays.asList(
                    new NeeoDeviceChannel(
                            NeeoDeviceChannelKind.get(channel == null ? ChannelKind.STATE : channel.getKind()),
                            item.getName(), 1, capabilityType, ItemSubType.NONE,
                            NeeoUtil.getUniqueLabel(existingLabels, NeeoUtil.getLabel(item, channelType)),
                            NeeoUtil.getPattern(item, channelType), NeeoDeviceChannelRange.from(item)),
                    new NeeoDeviceChannel(
                            NeeoDeviceChannelKind.get(channel == null ? ChannelKind.STATE : channel.getKind()),
                            item.getName(), 1, capabilityType, ItemSubType.HUE,
                            NeeoUtil.getUniqueLabel(existingLabels, NeeoUtil.getLabel(item, channelType) + " (Hue)"),
                            NeeoUtil.getPattern(item, channelType), NeeoDeviceChannelRange.from(item)),
                    new NeeoDeviceChannel(
                            NeeoDeviceChannelKind.get(channel == null ? ChannelKind.STATE : channel.getKind()),
                            item.getName(), 1, capabilityType, ItemSubType.SATURATION,
                            NeeoUtil.getUniqueLabel(existingLabels,
                                    NeeoUtil.getLabel(item, channelType) + " (Saturation)"),
                            NeeoUtil.getPattern(item, channelType), NeeoDeviceChannelRange.from(item)),
                    new NeeoDeviceChannel(
                            NeeoDeviceChannelKind.get(channel == null ? ChannelKind.STATE : channel.getKind()),
                            item.getName(), 1, capabilityType, ItemSubType.BRIGHTNESS,
                            NeeoUtil.getUniqueLabel(existingLabels,
                                    NeeoUtil.getLabel(item, channelType) + " (Brightness)"),
                            NeeoUtil.getPattern(item, channelType), NeeoDeviceChannelRange.from(item)));
        } else {
            return Arrays.asList(new NeeoDeviceChannel(
                    NeeoDeviceChannelKind.get(channel == null ? ChannelKind.STATE : channel.getKind()), item.getName(),
                    1, capabilityType, ItemSubType.NONE,
                    NeeoUtil.getUniqueLabel(existingLabels, NeeoUtil.getLabel(item, channelType)),
                    NeeoUtil.getPattern(item, channelType), NeeoDeviceChannelRange.from(item)));
        }
    }

    /**
     * Returns the kind of channel
     *
     * @return the non-null kind of channel
     */
    public NeeoDeviceChannelKind getKind() {
        return kind;
    }

    /**
     * Gets the unique item name (which may include the channel number)
     *
     * @return the unique item name
     */
    public String getUniqueItemName() {
        if (isPowerState()) {
            return "powerstate";
        }
        return itemName + "-" + subType + (channelNbr > 1 ? ("-" + channelNbr) : "");
    }

    /**
     * Gets the channel number
     *
     * @return the channel number
     */
    public int getChannelNbr() {
        return channelNbr;
    }

    /**
     * Gets the {@link NeeoCapabilityType}
     *
     * @return the {@link NeeoCapabilityType}
     */
    public NeeoCapabilityType getType() {
        return type;
    }

    /**
     * Gets the sub type
     *
     * @return the sub type
     */
    public ItemSubType getSubType() {
        return subType;
    }

    /**
     * Gets the item name.
     *
     * @return the item name
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        if (isPowerState()) {
            return "powerstate";
        }
        return label;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public @Nullable String getValue() {
        return value;
    }

    /**
     * Gets the device channel range. If the range is null, the {@link NeeoDeviceChannelRange#DEFAULT} will be returned
     *
     * @return the possibly null {@link NeeoDeviceChannelRange}
     */
    public NeeoDeviceChannelRange getRange() {
        return range;
    }

    /**
     * Helper method to determine if the channel is a powerstate channel or not
     *
     * @return true if powerstate, false otherwise
     */
    private boolean isPowerState() {
        return NeeoCapabilityType.SENSOR_POWER.equals(type);
    }

    @Override
    public String toString() {
        return "NeeoDeviceChannel [kind=" + kind + ", itemName=" + itemName + ", channelNbr=" + channelNbr + ", type="
                + type + ", subType=" + subType + ", label=" + label + ", value=" + value + ", range=" + range + "]";
    }
}
