/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import static org.openhab.core.library.CoreItemFactory.NUMBER;
import static org.openhab.core.library.CoreItemFactory.STRING;
import static org.openhab.core.library.CoreItemFactory.SWITCH;

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
    private static final String NUMBER_DIMENSIONLESS = NUMBER + ":Dimensionless";

    private record ConfigurationChannel(String id, String typeId, String itemType) {
    }

    private static final List<ConfigurationChannel> CHANNELS = List.of(
            new ConfigurationChannel(CHANNEL_COUNTRY_CODE, CHANNEL_COUNTRY_CODE, STRING),
            new ConfigurationChannel(CHANNEL_PM_STANDARD, CHANNEL_PM_STANDARD, STRING),
            new ConfigurationChannel(CHANNEL_ABC_DAYS, CHANNEL_ABC_DAYS, NUMBER),
            new ConfigurationChannel(CHANNEL_TVOC_LEARNING_OFFSET, CHANNEL_TVOC_LEARNING_OFFSET, NUMBER),
            new ConfigurationChannel(CHANNEL_NOX_LEARNING_OFFSET, CHANNEL_NOX_LEARNING_OFFSET, NUMBER),
            new ConfigurationChannel(CHANNEL_MQTT_BROKER_URL, CHANNEL_MQTT_BROKER_URL, STRING),
            new ConfigurationChannel(CHANNEL_TEMPERATURE_UNIT, CHANNEL_TEMPERATURE_UNIT, STRING),
            new ConfigurationChannel(CHANNEL_CONFIGURATION_CONTROL, CHANNEL_CONFIGURATION_CONTROL, STRING),
            new ConfigurationChannel(CHANNEL_POST_TO_CLOUD, CHANNEL_POST_TO_CLOUD, SWITCH),
            new ConfigurationChannel(CHANNEL_LED_BAR_BRIGHTNESS, CHANNEL_LED_BAR_BRIGHTNESS, NUMBER_DIMENSIONLESS),
            new ConfigurationChannel(CHANNEL_DISPLAY_BRIGHTNESS, CHANNEL_DISPLAY_BRIGHTNESS, NUMBER_DIMENSIONLESS),
            new ConfigurationChannel(CHANNEL_MODEL, CHANNEL_MODEL, STRING),
            new ConfigurationChannel(CHANNEL_LED_BAR_TEST, CHANNEL_LED_BAR_TEST, STRING));

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicChannelHelper.class);

    public static ThingBuilder updateThingWithConfigurationChannels(Thing thing, ThingBuilder builder) {
        for (ConfigurationChannel channel : CHANNELS) {
            addLocalConfigurationChannel(thing, builder, channel);
        }

        return builder;
    }

    private static void addLocalConfigurationChannel(Thing originalThing, ThingBuilder builder,
            ConfigurationChannel toAdd) {
        ChannelUID channelId = new ChannelUID(originalThing.getUID(), toAdd.id);
        if (originalThing.getChannel(channelId) == null) {
            LOGGER.debug("Adding dynamic channel {} to {}", toAdd.id, originalThing.getUID());
            ChannelTypeUID typeId = new ChannelTypeUID(BINDING_ID, toAdd.typeId);
            Channel channel = ChannelBuilder.create(channelId, toAdd.itemType).withType(typeId).build();
            builder.withChannel(channel);
        }
    }
}
