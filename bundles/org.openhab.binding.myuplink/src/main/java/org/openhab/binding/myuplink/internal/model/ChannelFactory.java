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

import java.util.HashMap;

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
        ChannelBuilder builder = ChannelBuilder.create(channelUID).withLabel(label).withDescription(label)
                .withType(channelTypeUID).withAcceptedItemType(channelType.getAcceptedType());

        if (writable) {
            var props = new HashMap<String, String>();
            props.put(PARAMETER_NAME_VALIDATION_REGEXP, DEFAULT_VALIDATION_EXPRESSION);
            builder.withProperties(props);
        }

        return builder.build();
    }

    /**
     * internal method to dertermine the enum type.
     *
     * @param enumValues enum data from myuplink API
     * @param writable flag to determine writable capability
     * @return
     */
    private static ChannelType determineEnumType(JsonArray enumValues, boolean writable) {
        boolean containsOffAt0 = false;
        boolean containsOnAt1 = false;
        boolean containsOffAt10 = false;
        boolean containsOffAt20 = false;
        boolean containsHotWaterAt20 = false;
        boolean containsHeatingAt30 = false;
        boolean containsPoolAt40 = false;
        boolean containsStartsAt40 = false;
        boolean containsRunsAt60 = false;

        for (var element : enumValues) {
            var enumText = Utils.getAsString(element.getAsJsonObject(), JSON_ENUM_KEY_TEXT);
            var enumOrdinal = Utils.getAsString(element.getAsJsonObject(), JSON_KEY_CHANNEL_VALUE, GENERIC_NO_VAL);

            switch (enumText.toLowerCase()) {
                case JSON_ENUM_VAL_OFF -> {
                    containsOffAt0 = enumOrdinal.equals(JSON_ENUM_ORD_0);
                    containsOffAt10 = enumOrdinal.equals(JSON_ENUM_ORD_10);
                    containsOffAt20 = enumOrdinal.equals(JSON_ENUM_ORD_20);
                }
                case JSON_ENUM_VAL_ON -> containsOnAt1 = enumOrdinal.equals(JSON_ENUM_ORD_1);
                case JSON_ENUM_VAL_HOT_WATER -> containsHotWaterAt20 = enumOrdinal.equals(JSON_ENUM_ORD_20);
                case JSON_ENUM_VAL_HEATING -> containsHeatingAt30 = enumOrdinal.equals(JSON_ENUM_ORD_30);
                case JSON_ENUM_VAL_POOL -> containsPoolAt40 = enumOrdinal.equals(JSON_ENUM_ORD_40);
                case JSON_ENUM_VAL_STARTS -> containsStartsAt40 = enumOrdinal.equals(JSON_ENUM_ORD_40);
                case JSON_ENUM_VAL_RUNS -> containsRunsAt60 = enumOrdinal.equals(JSON_ENUM_ORD_60);
            }
        }

        if (enumValues.size() == 2 && containsOffAt0 && containsOnAt1) {
            if (writable) {
                return ChannelType.RW_SWITCH;
            } else {
                return ChannelType.SWITCH;
            }
        } else if (enumValues.size() == 7 && containsOffAt10 && containsHotWaterAt20 && containsHeatingAt30
                && containsPoolAt40) {
            return ChannelType.PRIORITY;

        } else if (enumValues.size() == 4 && containsOffAt20 && containsStartsAt40 && containsRunsAt60) {
            return ChannelType.COMPRESSOR_STATUS;
        }

        LOGGER.info("could identify enum type with values: {}", enumValues.toString());
        return ChannelType.DOUBLE;
    }
}
