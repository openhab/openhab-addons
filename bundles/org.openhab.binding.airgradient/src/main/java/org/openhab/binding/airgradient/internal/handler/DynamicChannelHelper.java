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
package org.openhab.binding.airgradient.internal.handler;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DynamicChannelHelper} is responsible for creating dynamic configuration channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class DynamicChannelHelper {

    private record ConfigurationChannel(String id, String typeId, String itemType) {
    }

    private final static List<ConfigurationChannel> channels = new ArrayList<ConfigurationChannel>() {
        {
            add(new ConfigurationChannel("country-code", "country-code", "String"));
            add(new ConfigurationChannel("pm-standard", "pm-standard", "String"));
            add(new ConfigurationChannel("abc-days", "abc-days", "Number"));
            add(new ConfigurationChannel("tvoc-learning-offset", "tvoc-learning-offset", "Number"));
            add(new ConfigurationChannel("nox-learning-offset", "nox-learning-offset", "Number"));
            add(new ConfigurationChannel("mqtt-broker-url", "mqtt-broker-url", "String"));
            add(new ConfigurationChannel("temperature-unit", "temperature-unit", "String"));
            add(new ConfigurationChannel("configuration-control", "configuration-control", "String"));
            add(new ConfigurationChannel("post-to-cloud", "post-to-cloud", "Switch"));
            add(new ConfigurationChannel("led-bar-brightness", "led-bar-brightness", "Number:Dimensionless"));
            add(new ConfigurationChannel("display-brightness", "display-brightness", "Number:Dimensionless"));
            add(new ConfigurationChannel("model", "model", "String"));
            add(new ConfigurationChannel("led-bar-test", "led-bar-test", "String"));
        }
    };

    private final static Logger logger = LoggerFactory.getLogger(DynamicChannelHelper.class);

    public static ThingBuilder updateThingWithConfigurationChannels(Thing thing, ThingBuilder builder) {
        for (ConfigurationChannel channel : channels) {
            addLocalConfigurationChannel(thing, builder, channel);
        }

        return builder;
    }

    private static void addLocalConfigurationChannel(Thing originalThing, ThingBuilder builder,
            ConfigurationChannel toAdd) {
        ChannelUID channelId = new ChannelUID(originalThing.getUID(), toAdd.id);
        if (originalThing.getChannel(channelId) == null) {
            logger.debug("Adding dynamic channel {} to {}", toAdd.id, originalThing.getUID());
            ChannelTypeUID typeId = new ChannelTypeUID(BINDING_ID, toAdd.typeId);
            Channel channel = ChannelBuilder.create(channelId, toAdd.itemType).withType(typeId).build();
            builder.withChannel(channel);
        }
    }
}
