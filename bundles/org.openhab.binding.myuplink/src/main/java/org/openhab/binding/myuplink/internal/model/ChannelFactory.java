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
package org.openhab.binding.myuplink.internal.model;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

import com.google.gson.JsonObject;

/**
 * Factory that contains logic to create dynamic channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class ChannelFactory {

    public static Channel createChannel(ThingUID thingUID, String channelId, JsonObject channelData) {
        var label = Utils.getAsString(channelData, JSON_KEY_CHANNEL_LABEL);
        label = label == null ? "" : label;
        var unit = Utils.getAsString(channelData, JSON_KEY_CHANNEL_UNIT);
        unit = unit == null ? "" : unit;
        var strVal = Utils.getAsString(channelData, JSON_KEY_CHANNEL_STR_VAL);
        strVal = strVal == null ? "" : strVal;

        ChannelType channelType = ChannelType.fromJsonData(unit, strVal);

        ChannelUID channelUID = new ChannelUID(thingUID, channelId);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelType.getTypeName());
        Channel result = ChannelBuilder.create(channelUID).withLabel(label).withDescription(label)
                .withType(channelTypeUID).withAcceptedItemType(channelType.getAcceptedType()).build();

        return result;
    }
}
