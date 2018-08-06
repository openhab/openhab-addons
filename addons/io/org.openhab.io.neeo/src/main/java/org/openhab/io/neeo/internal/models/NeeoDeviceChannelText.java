/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing a Neeo text Channel (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceChannelText extends NeeoDeviceChannel {

    /** Whether the text label is visible or not */
    private final boolean labelVisible;

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
     * @param labelVisible true if the text has a visible label
     */
    public NeeoDeviceChannelText(NeeoDeviceChannelKind kind, String itemName, int channelNbr, NeeoCapabilityType type,
            ItemSubType subType, String label, @Nullable String value, @Nullable NeeoDeviceChannelRange range,
            boolean labelVisible) {
        super(kind, itemName, channelNbr, type, subType, label, value, range);

        this.labelVisible = labelVisible;
    }

    /**
     * Whether the text label is visible or not
     *
     * @return true if visible, false otherwise
     */
    public boolean isLabelVisible() {
        return labelVisible;
    }

    @Override
    public String toString() {
        return "NeeoDeviceChannelText [kind=" + getKind() + ", itemName=" + getItemName() + ", channelNbr="
                + getChannelNbr() + ", type=" + getType() + ", subType=" + getSubType() + ", label=" + getLabel()
                + ", value=" + getValue() + ", range=" + getRange() + ", labelVisible=" + labelVisible + "]";
    }
}
