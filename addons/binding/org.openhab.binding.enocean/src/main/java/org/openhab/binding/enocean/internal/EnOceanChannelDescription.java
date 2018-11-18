/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 *
 * @author Daniel Weber - Initial contribution
 *         This class holds information for creating a channel of an EnOcean thing like itemType and channelTypeUID
 */
public class EnOceanChannelDescription {
    public final ChannelTypeUID channelTypeUID;
    public final String itemType;
    @NonNull
    public final String label;
    public final boolean isStateChannel;

    /**
     * Ctor for an EnOceanChannelDescription
     * 
     * @param channelTypeUID ChannelTypeUID of channel
     * @param itemType       ItemType of channel like Switch, Dimmer
     */
    public EnOceanChannelDescription(ChannelTypeUID channelTypeUID, String itemType) {
        this(channelTypeUID, itemType, "", true);
    }

    /**
     * Ctor for an EnOceanChannelDescription with detailed information
     * 
     * @param channelTypeUID ChannelTypeUID of channel
     * @param itemType       ItemType of channel like Switch, Dimmer
     * @param label          of created channel
     * @param isStateChannel otherwise created channel is a trigger channel
     */
    public EnOceanChannelDescription(ChannelTypeUID channelTypeUID, String itemType, String label,
            boolean isStateChannel) {
        this.channelTypeUID = channelTypeUID;
        this.itemType = itemType;
        if (label != null) {
            this.label = label;
        } else {
            this.label = "";
        }

        this.isStateChannel = isStateChannel;
    }
}
