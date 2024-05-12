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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
public class GenericResponseTransformer {
    private final Logger logger = LoggerFactory.getLogger(GenericResponseTransformer.class);
    private final ChannelProvider channelProvider;
    private final @Nullable DynamicChannelProvider dynamicChannelProvider;
    // TODO: private final CustomResponseTransformer customResponseTransformer;

    public GenericResponseTransformer(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
        this.dynamicChannelProvider = channelProvider instanceof DynamicChannelProvider
                ? (DynamicChannelProvider) channelProvider
                : null;
        // TODO: this.customResponseTransformer = new CustomResponseTransformer(channelProvider);
    }

    public Map<Channel, State> transform(JsonObject jsonData, String group) {
        Map<Channel, State> result = new HashMap<>(20);

        for (JsonElement channelData : Utils.getAsJsonArray(jsonData, JSON_KEY_ROOT_DATA)) {

            logger.debug("received channel data: {}", channelData.toString());

            var value = Utils.getAsString(channelData.getAsJsonObject(), JSON_KEY_CHANNEL_VALUE);
            var channelId = Utils.getAsString(channelData.getAsJsonObject(), JSON_KEY_CHANNEL_ID);
            channelId = channelId == null ? "" : channelId;

            Channel channel;
            if (dynamicChannelProvider != null) {
                channel = dynamicChannelProvider.getOrCreateChannel(channelId, channelData.getAsJsonObject());
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
                        var newState = switch (ChannelType.fromTypeName(channelTypeId)) {
                            case ENERGY ->
                                new QuantityType<>(Double.parseDouble(value), MetricPrefix.KILO(Units.WATT_HOUR));
                            case PRESSURE -> new QuantityType<>(Double.parseDouble(value), Units.BAR);
                            case PERCENT -> new QuantityType<>(Double.parseDouble(value), Units.PERCENT);
                            case TEMPERATURE -> new QuantityType<>(Double.parseDouble(value), SIUnits.CELSIUS);
                            case FREQUENCY -> new QuantityType<>(Double.parseDouble(value), Units.HERTZ);
                            case FLOW ->
                                new QuantityType<>(Double.parseDouble(value), Units.LITRE.divide(Units.MINUTE));
                            case ELECTRIC_CURRENT -> new QuantityType<>(Double.parseDouble(value), Units.AMPERE);
                            case TIME -> new QuantityType<>(Double.parseDouble(value), Units.HOUR);
                            case INTEGER -> new DecimalType(Double.valueOf(value).longValue());
                            case DOUBLE -> new DecimalType(Double.parseDouble(value));
                            // case CHANNEL_TYPE_SWITCH -> OnOffType.from(Boolean.parseBoolean(value));
                            default -> UnDefType.NULL;
                        };

                        if (newState == UnDefType.NULL) {
                            logger.warn("no mapping implemented for channel type '{}'", channelTypeId);
                        } else {
                            result.put(channel, newState);
                        }
                        // switch (channelType) {
                        // case CHANNEL_TYPE_SWITCH:
                        // result.put(channel, OnOffType.from(Boolean.parseBoolean(value)));
                        // break;
                        // case CHANNEL_TYPE_VOLT:
                        // result.put(channel, new QuantityType<>(Double.parseDouble(value), Units.VOLT));
                        // break;
                        // case CHANNEL_TYPE_AMPERE:
                        // result.put(channel, new QuantityType<>(Double.parseDouble(value), Units.AMPERE));
                        // break;
                        // case CHANNEL_TYPE_KWH:
                        // result.put(channel, new QuantityType<>(Double.parseDouble(value),
                        // MetricPrefix.KILO(Units.WATT_HOUR)));
                        // break;
                        // case CHANNEL_TYPE_POWER:
                        // result.put(channel,
                        // new QuantityType<>(Double.parseDouble(value), MetricPrefix.KILO(Units.WATT)));
                        // break;
                        // case CHANNEL_TYPE_DATE:
                        // result.put(channel, new DateTimeType(Utils.parseDate(value)));
                        // break;
                        // case CHANNEL_TYPE_STRING:
                        // result.put(channel, new StringType(value));
                        // break;
                        // case CHANNEL_TYPE_NUMBER:
                        // if (channelTypeId.contains(CHANNEL_TYPENAME_INTEGER)) {
                        // // explicit type long is needed in case of integer/long values otherwise automatic
                        // // transformation to a decimal type is applied.
                        // result.put(channel, new DecimalType(Long.parseLong(value)));
                        // } else {
                        // result.put(channel, new DecimalType(Double.parseDouble(value)));
                        // }
                        // break;
                        // default:
                        // logger.warn("no mapping implemented for channel type '{}'", channelType);
                        // }

                        // // call the custom handler to handle specific / composite channels which do not map 1:1 to
                        // JSON
                        // // fields.
                        // // TODO: result.putAll(customResponseTransformer.transform(channel, value, jsonData));

                    } catch (NumberFormatException | DateTimeParseException ex) {
                        logger.warn("caught exception while parsing data for channel {} (value '{}'). Exception: {}",
                                channel.getUID().getId(), value, ex.getMessage());
                    }
                }
            }
        }
        return result;
    }
}
