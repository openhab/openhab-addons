/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nibeuplink.handler.ChannelProvider;
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

    private final ChannelProvider channelProvider;

    public DataResponseTransformer(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public Map<Channel, State> transform(DataResponse response) {
        Map<String, String> source = response.getValues();
        Map<Channel, State> result = new HashMap<>(source.size());

        for (String channelId : source.keySet()) {
            String valueAsString = source.get(channelId);

            Channel channel = channelProvider.getSpecificChannel(channelId);
            if (channel == null) {
                // This should not happen but we want to get informed about it
                logger.warn("Channel not found: {}", channelId);
            } else {
                try {
                    double value = Double.parseDouble(valueAsString);
                    Unit<?> unit = channel.getUnit();

                    if (unit == null) {
                        logger.debug("Channel {} transformed to NumberType ({})", channel.getFQName(), value);
                        result.put(channel, new DecimalType(value));
                    } else {
                        logger.debug("Channel {} transformed to QuantityType ({} {})", channel.getFQName(), value,
                                unit.toString());
                        result.put(channel, new QuantityType<>(value, unit));
                    }
                } catch (NumberFormatException ex) {
                    logger.info("Could not parse value '{}' as double. Channel: {}", valueAsString,
                            channel.getFQName());
                    result.put(channel, UnDefType.UNDEF);
                }
            }
        }
        return result;
    }

}
