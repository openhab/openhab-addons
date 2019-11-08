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
package org.openhab.binding.nibeuplink.internal.model;

import static org.openhab.binding.nibeuplink.internal.NibeUplinkBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.nibeuplink.internal.handler.ChannelProvider;
import org.openhab.binding.nibeuplink.internal.handler.ChannelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * transforms the http response into the openhab datamodel (instances of State)
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class DataResponseTransformer {
    private final Logger logger = LoggerFactory.getLogger(DataResponseTransformer.class);

    private static final double UNSCALED = 1;
    private static final double DIV_10 = 0.1;
    private static final double DIV_100 = 0.01;

    private final ChannelProvider channelProvider;

    public DataResponseTransformer(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public Map<Channel, State> transform(DataResponse response) {
        Map<String, Long> source = response.getValues();
        Map<Channel, State> result = new HashMap<>(source.size());

        for (String channelId : source.keySet()) {
            Long value = source.get(channelId);

            Channel channel = channelProvider.getSpecificChannel(channelId);
            if (channel == null) {
                // This should not happen but we want to get informed about it
                logger.warn("Channel not found: {}", channelId);
            } else {
                ChannelTypeUID typeUID = channel.getChannelTypeUID();
                String type = typeUID == null ? null : typeUID.getId();
                type = type == null ? "null" : type;

                switch (type) {
                    case CHANNEL_TYPE_TEMPERATURE:
                    case CHANNEL_TYPE_START_COOLING_RW:
                        putQuantityType(result, channel, value, DIV_10, SIUnits.CELSIUS);
                        break;
                    case CHANNEL_TYPE_ENERGY:
                        putQuantityType(result, channel, value, DIV_10, MetricPrefix.KILO(SmartHomeUnits.WATT_HOUR));
                        break;
                    case CHANNEL_TYPE_POWER:
                        putQuantityType(result, channel, value, DIV_100, MetricPrefix.KILO(SmartHomeUnits.WATT));
                        break;
                    case CHANNEL_TYPE_SWITCH_RW:
                    case CHANNEL_TYPE_SWITCH:
                        putOnOffType(result, channel, value);
                        break;
                    case CHANNEL_TYPE_ELECTRIC_CURRENT:
                        putQuantityType(result, channel, value, DIV_10, SmartHomeUnits.AMPERE);
                        break;
                    case CHANNEL_TYPE_TIME_UNSCALED:
                        putQuantityType(result, channel, value, UNSCALED, SmartHomeUnits.HOUR);
                        break;
                    case CHANNEL_TYPE_TIME_SCALE10:
                        putQuantityType(result, channel, value, DIV_10, SmartHomeUnits.HOUR);
                        break;
                    case CHANNEL_TYPE_FREQUENCY_UNSCALED:
                        putQuantityType(result, channel, value, UNSCALED, SmartHomeUnits.HERTZ);
                        break;
                    case CHANNEL_TYPE_FREQUENCY_SCALE10:
                        putQuantityType(result, channel, value, DIV_10, SmartHomeUnits.HERTZ);
                        break;
                    case CHANNEL_TYPE_FLOW:
                        putQuantityType(result, channel, value, DIV_10,
                                SmartHomeUnits.LITRE.divide(SmartHomeUnits.MINUTE));
                        break;
                    case CHANNEL_TYPE_SPEED:
                        putQuantityType(result, channel, value, UNSCALED, SmartHomeUnits.PERCENT);
                        break;
                    case CHANNEL_TYPE_NUMBER_SCALE100:
                        putDecimalType(result, channel, value, DIV_100);
                    case CHANNEL_TYPE_NUMBER_SCALE10:
                    case CHANNEL_TYPE_DEGREE_MINUTES_RW:
                        putDecimalType(result, channel, value, DIV_10);
                        break;
                    case CHANNEL_TYPE_NUMBER_UNSCALED:
                    case CHANNEL_TYPE_DEFROSTING_STATE:
                    case CHANNEL_TYPE_HPAC_STATE:
                    case CHANNEL_TYPE_HW_LUX_RW:
                    case CHANNEL_TYPE_HW_MODE_RW:
                    case CHANNEL_TYPE_FAN_SPEED_RW:
                        putDecimalType(result, channel, value, UNSCALED);
                        break;
                    default:
                        logger.warn("could not handle unknown type {}, channel {}, value {}", type,
                                channel.getUID().getAsString(), value);
                }
            }
        }
        return result;
    }

    private final void putQuantityType(Map<Channel, State> targetMap, Channel channel, long value, double factor,
            Unit<?> unit) {
        // make sure that values are stored as long if no factor is to be applied
        State val = factor == UNSCALED ? new QuantityType<>(value, unit) : new QuantityType<>(value * factor, unit);
        targetMap.put(channel, val);
        logger.debug("Channel {} transformed to QuantityType ({}*{} {}) -> {}", channel.getUID().getId(), value, factor,
                unit.toString(), val.toString());
    }

    private final void putOnOffType(Map<Channel, State> targetMap, Channel channel, long value) {
        State val = ChannelUtil.mapValue(channel, value);
        targetMap.put(channel, val);
        logger.debug("Channel {} transformed to OnOffType ({}) -> {}", channel.getUID().getId(), value, val.toString());
    }

    private final void putDecimalType(Map<Channel, State> targetMap, Channel channel, long value, double factor) {
        // make sure that values are stored as long if no factor is to be applied
        State val = factor == UNSCALED ? new DecimalType(value) : new DecimalType(value * factor);
        targetMap.put(channel, val);
        logger.debug("Channel {} transformed to DecimalType ({}*{}) -> {}", channel.getUID().getId(), value, factor,
                val.toString());
    }

}
