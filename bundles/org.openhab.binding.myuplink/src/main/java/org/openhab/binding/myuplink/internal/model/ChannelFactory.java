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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Factory that contains logic to create dynamic channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class ChannelFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelFactory.class);

    public static Channel createChannel(ThingUID thingUID, String channelId, JsonObject channelData) {
        var label = Utils.getAsString(channelData, JSON_KEY_CHANNEL_LABEL, GENERIC_NO_VAL);
        var unit = Utils.getAsString(channelData, JSON_KEY_CHANNEL_UNIT, GENERIC_NO_VAL);
        var strVal = Utils.getAsString(channelData, JSON_KEY_CHANNEL_STR_VAL, GENERIC_NO_VAL);
        var writable = Utils.getAsBool(channelData, JSON_KEY_CHANNEL_WRITABLE, Boolean.FALSE);
        var enumValues = Utils.getAsJsonArray(channelData, JSON_KEY_CHANNEL_ENUM_VALUES);

        ChannelType channelType = null;
        if (enumValues.isEmpty()) {
            channelType = ChannelType.fromJsonData(unit, writable);
        } else {
            channelType = determineEnumType(enumValues, writable);
        }

        if (channelType == null) {
            if (strVal.contains(JSON_VAL_DECIMAL_SEPARATOR)) {
                channelType = ChannelType.DOUBLE;
            } else {
                channelType = ChannelType.INTEGER;
            }
        }

        ChannelUID channelUID = new ChannelUID(thingUID, channelId);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelType.getTypeName());
        Channel result = ChannelBuilder.create(channelUID).withLabel(label).withDescription(label)
                .withType(channelTypeUID).withAcceptedItemType(channelType.getAcceptedType()).build();

        return result;
    }

    private static ChannelType determineEnumType(JsonArray enumValues, boolean writable) {
        boolean containsOffAt0 = false;
        boolean containsOnAt1 = false;

        for (var element : enumValues) {
            var enumText = Utils.getAsString(element.getAsJsonObject(), JSON_ENUM_KEY_TEXT);
            var enumOrdinal = Utils.getAsString(element.getAsJsonObject(), JSON_KEY_CHANNEL_VALUE, GENERIC_NO_VAL);

            switch (enumText.toLowerCase()) {
                case JSON_ENUM_VAL_OFF -> containsOffAt0 = enumOrdinal.equals(JSON_ENUM_ORD_0);
                case JSON_ENUM_VAL_ON -> containsOnAt1 = enumOrdinal.equals(JSON_ENUM_ORD_1);
            }
        }

        if (enumValues.size() == 2 && containsOnAt1 && containsOffAt0) {
            if (writable) {
                return ChannelType.RW_SWITCH;
            } else {
                return ChannelType.SWITCH;
            }
        }
        LOGGER.info("could identify enum type with values: {}", enumValues.toString());
        return ChannelType.DOUBLE;
    }
}
