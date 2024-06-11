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

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.*;

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
            add(new ConfigurationChannel(CHANNEL_COUNTRY_CODE, CHANNEL_COUNTRY_CODE, "String"));
            add(new ConfigurationChannel(CHANNEL_PM_STANDARD, CHANNEL_PM_STANDARD, "String"));
            add(new ConfigurationChannel(CHANNEL_ABC_DAYS, CHANNEL_ABC_DAYS, "Number"));
            add(new ConfigurationChannel(CHANNEL_TVOC_LEARNING_OFFSET, CHANNEL_TVOC_LEARNING_OFFSET, "Number"));
            add(new ConfigurationChannel(CHANNEL_NOX_LEARNING_OFFSET, CHANNEL_NOX_LEARNING_OFFSET, "Number"));
            add(new ConfigurationChannel(CHANNEL_MQTT_BROKER_URL, CHANNEL_MQTT_BROKER_URL, "String"));
            add(new ConfigurationChannel(CHANNEL_TEMPERATURE_UNIT, CHANNEL_TEMPERATURE_UNIT, "String"));
            add(new ConfigurationChannel(CHANNEL_CONFIGURATION_CONTROL, CHANNEL_CONFIGURATION_CONTROL, "String"));
            add(new ConfigurationChannel(CHANNEL_POST_TO_CLOUD, CHANNEL_POST_TO_CLOUD, "Switch"));
            add(new ConfigurationChannel(CHANNEL_LED_BAR_BRIGHTNESS, CHANNEL_LED_BAR_BRIGHTNESS,
                    "Number:Dimensionless"));
            add(new ConfigurationChannel(CHANNEL_DISPLAY_BRIGHTNESS, CHANNEL_DISPLAY_BRIGHTNESS,
                    "Number:Dimensionless"));
            add(new ConfigurationChannel(CHANNEL_MODEL, CHANNEL_MODEL, "String"));
            add(new ConfigurationChannel(CHANNEL_LED_BAR_TEST, CHANNEL_LED_BAR_TEST, "String"));
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
