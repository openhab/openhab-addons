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
