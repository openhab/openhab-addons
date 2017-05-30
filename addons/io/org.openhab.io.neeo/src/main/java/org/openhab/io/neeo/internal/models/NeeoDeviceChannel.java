/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * The model representing a Neeo Channel (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoDeviceChannel {

    /** The channel kind */
    private final NeeoDeviceChannelKind kind;

    /** The item name */
    private final String itemName;

    /** The channel number */
    private final int channelNbr;

    /** The capability type */
    private final NeeoCapabilityType type;

    /** The label */
    private final String label;

    /** The action/text value */
    private final String value; // could be either a format (text label) or a value to send (button)

    /** The device channel range */
    private final NeeoDeviceChannelRange range;

    /**
     * Create a new channel based on the parms
     *
     * @param kind the non-null kind of channel
     * @param itemName the non-empty item name
     * @param channelNbr the channel number (must be >= 0)
     * @param type the non-null type
     * @param label the non-empty label
     * @param value the possibly null, possibly empty value
     * @param range the possibly null range
     */
    public NeeoDeviceChannel(NeeoDeviceChannelKind kind, String itemName, int channelNbr, NeeoCapabilityType type,
            String label, String value, NeeoDeviceChannelRange range) {
        Objects.requireNonNull(kind, "kind cannot be null");
        NeeoUtil.requireNotEmpty(itemName, "itemName is required");
        Objects.requireNonNull(type, "type is required");
        if (channelNbr < 0) {
            throw new IllegalArgumentException("channelNbr must be >= 0");
        }

        this.kind = kind;
        this.itemName = itemName;
        this.channelNbr = channelNbr;
        this.type = type;
        this.label = StringUtils.isEmpty(label) ? "N/A" : label;
        this.value = value;
        this.range = range;
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
        return itemName + (channelNbr > 1 ? ("-" + channelNbr) : "");
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
    public String getValue() {
        return value;
    }

    /**
     * Gets the device channel range. If the range is null, the {@link NeeoDeviceChannelRange#DEFAULT} will be returned
     *
     * @return the possibly null {@link NeeoDeviceChannelRange}
     */
    public NeeoDeviceChannelRange getRange() {
        return range == null ? NeeoDeviceChannelRange.DEFAULT : range;
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
                + type + ", label=" + label + ", value=" + value + ", range=" + range + "]";
    }
}
