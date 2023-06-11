/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 *
 * @author Daniel Weber - Initial contribution
 *         This class holds information for creating a channel of an EnOcean thing like acceptedItemType and
 *         channelTypeUID
 */
public class EnOceanChannelDescription {
    public final ChannelTypeUID channelTypeUID;
    public final String acceptedItemType;
    @NonNull
    public final String label;
    public final boolean isStateChannel;
    public final boolean autoCreate;

    /**
     * Ctor for an EnOceanChannelDescription
     *
     * @param channelTypeUID ChannelTypeUID of channel
     * @param acceptedItemType AcceptedItemType of channel like Switch, Dimmer or null if we accept everything
     */
    public EnOceanChannelDescription(ChannelTypeUID channelTypeUID, String itemType) {
        this(channelTypeUID, itemType, "", true, true);
    }

    /**
     * Ctor for an EnOceanChannelDescription with detailed information
     *
     * @param channelTypeUID ChannelTypeUID of channel
     * @param acceptedItemType ItemType of channel like Switch, Dimmer
     * @param label of created channel
     * @param isStateChannel otherwise created channel is a trigger channel
     * @param autoCreate create channel during thing initialization, otherwise channel is created
     *            manually/predefined
     */
    public EnOceanChannelDescription(ChannelTypeUID channelTypeUID, String itemType, String label,
            boolean isStateChannel, boolean autoCreate) {
        this.channelTypeUID = channelTypeUID;
        this.acceptedItemType = itemType;
        if (label != null) {
            this.label = label;
        } else {
            this.label = "";
        }

        this.isStateChannel = isStateChannel;
        this.autoCreate = autoCreate;
    }
}
