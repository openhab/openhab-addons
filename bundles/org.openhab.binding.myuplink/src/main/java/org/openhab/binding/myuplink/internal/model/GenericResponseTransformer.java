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

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.binding.myuplink.internal.handler.ChannelProvider;
import org.openhab.binding.myuplink.internal.handler.DynamicChannelProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * transforms the http response into the openhab datamodel (instances of State)
 * this is a generic trnasformer which tries to map json fields 1:1 to channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GenericResponseTransformer implements ResponseTransformer {
    private final Logger logger = LoggerFactory.getLogger(GenericResponseTransformer.class);
    private final ChannelProvider channelProvider;
    private final @Nullable DynamicChannelProvider dynamicChannelProvider;

    public GenericResponseTransformer(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
        this.dynamicChannelProvider = channelProvider instanceof DynamicChannelProvider
                ? (DynamicChannelProvider) channelProvider
                : null;
    }

    public Map<Channel, State> transform(JsonObject jsonData, String group) {
        Map<Channel, State> result = new HashMap<>(20);

        for (JsonElement channelData : Utils.getAsJsonArray(jsonData, JSON_KEY_ROOT_DATA)) {

            logger.debug("received channel data: {}", channelData.toString());

            var value = Utils.getAsBigDecimal(channelData.getAsJsonObject(), JSON_KEY_CHANNEL_VALUE);
            var unit = UnitUtils.parseUnit(
                    Utils.fixUnit(Utils.getAsString(channelData.getAsJsonObject(), JSON_KEY_CHANNEL_UNIT, "")));
            var channelId = Utils.getAsString(channelData.getAsJsonObject(), JSON_KEY_CHANNEL_ID, GENERIC_NO_VAL);

            Channel channel;
            var dcp = dynamicChannelProvider;
            if (dcp != null) {
                channel = getOrCreateChannel(dcp, channelId, channelData.getAsJsonObject());
            } else {
                channel = channelProvider.getChannel(group, channelId);
            }

            if (channel == null) {
                logger.debug("Channel not found: {}#{}, dynamic channels not support by thing.", group, channelId);
            } else {
                logger.debug("mapping value '{}' to channel {}", value, channel.getUID().getId());

                if (value == null) {
                    result.put(channel, UnDefType.NULL);
                } else {
                    try {
                        var channelTypeId = Utils.getChannelTypeId(channel);
                        State newState;
                        if (channelTypeId.equals(CHANNEL_TYPE_RW_SWITCH)) {
                            newState = convertToOnOffType(value.stripTrailingZeros().toString());
                        } else if (channelTypeId.equals(CHANNEL_TYPE_RW_COMMAND)) {
                            newState = new StringType(value.toString());
                        } else if (unit != null) {
                            newState = new QuantityType<>(value, unit);
                        } else {
                            newState = new DecimalType(value.stripTrailingZeros());
                        }

                        if (newState == UnDefType.NULL) {
                            logger.warn("no mapping implemented for channel type '{}'", channelTypeId);
                        } else {
                            result.put(channel, newState);
                        }
                    } catch (NumberFormatException | DateTimeParseException ex) {
                        logger.warn("caught exception while parsing data for channel {} (value '{}'). Exception: {}",
                                channel.getUID().getId(), value, ex.getMessage());
                    }
                }
            }
        }
        return result;
    }

    private Channel getOrCreateChannel(DynamicChannelProvider dcp, String channelId, JsonObject channelData) {
        var result = channelProvider.getChannel(EMPTY, channelId);
        if (result == null) {
            result = dcp.getChannelFactory().createChannel(dcp.getThingUid(), channelData);
            dcp.registerChannel(result);

        }
        return result;
    }

    private OnOffType convertToOnOffType(String value) {
        return switch (value) {
            case "1" -> OnOffType.ON;
            case "1.0" -> OnOffType.ON;
            default -> OnOffType.OFF;
        };
    }
}
