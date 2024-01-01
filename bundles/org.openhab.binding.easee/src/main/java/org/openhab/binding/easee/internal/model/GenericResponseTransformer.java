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
package org.openhab.binding.easee.internal.model;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import javax.measure.MetricPrefix;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easee.internal.Utils;
import org.openhab.binding.easee.internal.handler.ChannelProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final CustomResponseTransformer customResponseTransformer;

    public GenericResponseTransformer(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
        this.customResponseTransformer = new CustomResponseTransformer(channelProvider);
    }

    public Map<Channel, State> transform(JsonObject jsonData, String group) {
        Map<Channel, State> result = new HashMap<>(20);

        for (String channelId : jsonData.keySet()) {
            String value = Utils.getAsString(jsonData, channelId);

            Channel channel = channelProvider.getChannel(group, channelId);
            if (channel == null) {
                // As we have a generic response mapper it ould happen that a subset of key/values in the response
                // cannot be mapped to openhab channels.
                logger.debug("Channel not found: {}#{}", group, channelId);
            } else {
                logger.debug("mapping value '{}' to channel {}", value, channel.getUID().getId());
                String channelType = channel.getAcceptedItemType();

                if (value == null || channelType == null) {
                    result.put(channel, UnDefType.NULL);
                } else {
                    try {
                        String channelTypeId = Utils.getChannelTypeId(channel);
                        switch (channelType) {
                            case CHANNEL_TYPE_SWITCH:
                                result.put(channel, OnOffType.from(Boolean.parseBoolean(value)));
                                break;
                            case CHANNEL_TYPE_VOLT:
                                result.put(channel, new QuantityType<>(Double.parseDouble(value), Units.VOLT));
                                break;
                            case CHANNEL_TYPE_AMPERE:
                                result.put(channel, new QuantityType<>(Double.parseDouble(value), Units.AMPERE));
                                break;
                            case CHANNEL_TYPE_KWH:
                                result.put(channel, new QuantityType<>(Double.parseDouble(value),
                                        MetricPrefix.KILO(Units.WATT_HOUR)));
                                break;
                            case CHANNEL_TYPE_POWER:
                                if (channelTypeId.equals(CHANNEL_TYPENAME_RSSI)) {
                                    // explicit type long is needed in case of integer/long values otherwise automatic
                                    // transformation to a decimal type is applied.
                                    result.put(channel,
                                            new QuantityType<>(Long.parseLong(value), Units.DECIBEL_MILLIWATTS));
                                } else {
                                    result.put(channel, new QuantityType<>(Double.parseDouble(value),
                                            MetricPrefix.KILO(Units.WATT)));
                                }
                                break;
                            case CHANNEL_TYPE_DATE:
                                result.put(channel, new DateTimeType(Utils.parseDate(value)));
                                break;
                            case CHANNEL_TYPE_STRING:
                                result.put(channel, new StringType(value));
                                break;
                            case CHANNEL_TYPE_NUMBER:
                                if (channelTypeId.contains(CHANNEL_TYPENAME_INTEGER)) {
                                    // explicit type long is needed in case of integer/long values otherwise automatic
                                    // transformation to a decimal type is applied.
                                    result.put(channel, new DecimalType(Long.parseLong(value)));
                                } else {
                                    result.put(channel, new DecimalType(Double.parseDouble(value)));
                                }
                                break;
                            default:
                                logger.warn("no mapping implemented for channel type '{}'", channelType);
                        }

                        // call the custom handler to handle specific / composite channels which do not map 1:1 to JSON
                        // fields.
                        result.putAll(customResponseTransformer.transform(channel, value, jsonData));

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
