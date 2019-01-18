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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing a Neeo Directory (list) Channel (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceChannelDirectory extends NeeoDeviceChannel {

    private final NeeoDeviceChannelDirectoryListItem[] listItems;

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
     * @param listItems the non-null, possibly empty array of {@link NeeoDeviceChannelDirectoryListItem}
     */
    public NeeoDeviceChannelDirectory(NeeoDeviceChannelKind kind, String itemName, int channelNbr,
            NeeoCapabilityType type, ItemSubType subType, String label, @Nullable String value,
            @Nullable NeeoDeviceChannelRange range, NeeoDeviceChannelDirectoryListItem[] listItems) {
        super(kind, itemName, channelNbr, type, subType, label, value, range);

        this.listItems = listItems;
    }

    /**
     * A non-null, possibly empty array of {@link NeeoDeviceChannelDirectoryListItem}
     *
     * @return a non-null, possibly empty array of {@link NeeoDeviceChannelDirectoryListItem}
     */
    public NeeoDeviceChannelDirectoryListItem[] getListItems() {
        return listItems;
    }

    @Override
    public String toString() {
        return "NeeoDeviceChannelDirectory [kind=" + getKind() + ", itemName=" + getItemName() + ", channelNbr="
                + getChannelNbr() + ", type=" + getType() + ", subType=" + getSubType() + ", label=" + getLabel()
                + ", value=" + getValue() + ", range=" + getRange() + ", listItems=" + Arrays.toString(listItems) + "]";
    }
}
