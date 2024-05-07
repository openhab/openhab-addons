/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal.handler;

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
 * public interface of the {@link MyUplinkDynamicThingHandler}. provides some default implementations which can be used
 * by dynamic ThingHandlers.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface MyUplinkDynamicThingHandler extends MyUplinkThingHandler, DynamicChannelProvider {

    void addDynamicChannel(Channel channel);

    @Override
    default Channel getOrCreateChannel(String channelId, JsonObject channelData) {
        Channel result = getChannel(CHANNEL_GROUP_NONE, channelId);
        if (result == null) {

            String label = Utils.getAsString(channelData.getAsJsonObject(), JSON_KEY_CHANNEL_LABEL);
            label = label == null ? "" : label;
            String unit = Utils.getAsString(channelData.getAsJsonObject(), JSON_KEY_CHANNEL_UNIT);
            unit = unit == null ? "" : unit;

            String channelType = switch (unit) {
                case JSON_VAL_UNIT_ENERGY -> CHANNEL_TYPENAME_ENERGY;
                case JSON_VAL_UNIT_PRESSURE -> CHANNEL_TYPENAME_PRESSURE;
                case JSON_VAL_UNIT_PERCENT -> CHANNEL_TYPENAME_PERCENT;
                case JSON_VAL_UNIT_TEMPERATURE -> CHANNEL_TYPENAME_TEMPERATURE;
                case JSON_VAL_UNIT_FREQUENCY -> CHANNEL_TYPENAME_FREQUENCY;
                default -> CHANNEL_TYPENAME_DEFAULT;
            };

            ThingUID thingUID = getThing().getUID();
            ChannelUID channelUID = new ChannelUID(thingUID, channelId);
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelType);
            result = ChannelBuilder.create(channelUID).withLabel(label).withDescription(label).withType(channelTypeUID)
                    .build();

            addDynamicChannel(result);
        }
        return result;
    }
}
