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
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airgradient.internal.model.LocalConfiguration;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DynamicChannelHelper} is responsible for creating dynamic channels for supported values.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Leo Siepel - Reorganized code and added dynamic measurement channels
 */
@NonNullByDefault
public class DynamicChannelHelper {
    private static final String NUMBER_DIMENSIONLESS = NUMBER + ":Dimensionless";
    private static final String NUMBER_DENSITY = NUMBER + ":Density";
    private static final String NUMBER_TEMPERATURE = NUMBER + ":Temperature";

    private record DynamicChannel<T> (String id, ChannelTypeUID channelTypeUID, String itemType,
            Predicate<T> isSupported) {
        private DynamicChannel(String id, String typeId, String itemType, Predicate<T> isSupported) {
            this(id, new ChannelTypeUID(BINDING_ID, typeId), itemType, isSupported);
        }
    }

    private static final List<DynamicChannel<LocalConfiguration>> CONFIGURATION_CHANNELS = List.of(
            new DynamicChannel<>(CHANNEL_COUNTRY_CODE, CHANNEL_COUNTRY_CODE, STRING,
                    (config) -> config.country != null),
            new DynamicChannel<>(CHANNEL_PM_STANDARD, CHANNEL_PM_STANDARD, STRING,
                    (config) -> config.pmStandard != null),
            new DynamicChannel<>(CHANNEL_ABC_DAYS, CHANNEL_ABC_DAYS, NUMBER, (config) -> config.abcDays != null),
            new DynamicChannel<>(CHANNEL_TVOC_LEARNING_OFFSET, CHANNEL_TVOC_LEARNING_OFFSET, NUMBER,
                    (config) -> config.tvocLearningOffset != null),
            new DynamicChannel<>(CHANNEL_NOX_LEARNING_OFFSET, CHANNEL_NOX_LEARNING_OFFSET, NUMBER,
                    (config) -> config.noxLearningOffset != null),
            new DynamicChannel<>(CHANNEL_MQTT_BROKER_URL, CHANNEL_MQTT_BROKER_URL, STRING,
                    (config) -> config.mqttBrokerUrl != null),
            new DynamicChannel<>(CHANNEL_TEMPERATURE_UNIT, CHANNEL_TEMPERATURE_UNIT, STRING,
                    (config) -> config.temperatureUnit != null),
            new DynamicChannel<>(CHANNEL_CONFIGURATION_CONTROL, CHANNEL_CONFIGURATION_CONTROL, STRING,
                    (config) -> config.configurationControl != null),
            new DynamicChannel<>(CHANNEL_POST_TO_CLOUD, CHANNEL_POST_TO_CLOUD, SWITCH,
                    (config) -> config.postDataToAirGradient != null),
            new DynamicChannel<>(CHANNEL_LED_BAR_BRIGHTNESS, CHANNEL_LED_BAR_BRIGHTNESS, NUMBER_DIMENSIONLESS,
                    (config) -> config.ledBarBrightness != null),
            new DynamicChannel<>(CHANNEL_DISPLAY_BRIGHTNESS, CHANNEL_DISPLAY_BRIGHTNESS, NUMBER_DIMENSIONLESS,
                    (config) -> config.displayBrightness != null),
            new DynamicChannel<>(CHANNEL_MODEL, CHANNEL_MODEL, STRING, (config) -> config.model != null),
            new DynamicChannel<>(CHANNEL_LED_BAR_TEST, CHANNEL_LED_BAR_TEST, STRING, (config) -> true));

    private static final List<DynamicChannel<Measure>> MEASUREMENT_CHANNELS = List.of(
            new DynamicChannel<>(CHANNEL_PM01_STANDARD, "pm1", NUMBER_DENSITY,
                    (measure) -> measure.pm01Standard != null),
            new DynamicChannel<>(CHANNEL_PM02_STANDARD, "pm2", NUMBER_DENSITY,
                    (measure) -> measure.pm02Standard != null),
            new DynamicChannel<>(CHANNEL_PM10_STANDARD, "pm10", NUMBER_DENSITY,
                    (measure) -> measure.pm10Standard != null),
            new DynamicChannel<>(CHANNEL_PM005_COUNT, "particle-count", NUMBER_DIMENSIONLESS,
                    (measure) -> measure.pm005Count != null),
            new DynamicChannel<>(CHANNEL_PM01_COUNT, "particle-count", NUMBER_DIMENSIONLESS,
                    (measure) -> measure.pm01Count != null),
            new DynamicChannel<>(CHANNEL_PM02_COUNT, "particle-count", NUMBER_DIMENSIONLESS,
                    (measure) -> measure.pm02Count != null),
            new DynamicChannel<>(CHANNEL_PM50_COUNT, "particle-count", NUMBER_DIMENSIONLESS,
                    (measure) -> measure.pm50Count != null),
            new DynamicChannel<>(CHANNEL_PM10_COUNT, "particle-count", NUMBER_DIMENSIONLESS,
                    (measure) -> measure.pm10Count != null),
            new DynamicChannel<>(CHANNEL_PM02_COMPENSATED, "pm2", NUMBER_DENSITY,
                    (measure) -> measure.pm02Compensated != null),
            new DynamicChannel<>(CHANNEL_ATMP_COMPENSATED,
                    DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE, NUMBER_TEMPERATURE,
                    (measure) -> measure.atmpCompensated != null),
            new DynamicChannel<>(CHANNEL_RHUM_COMPENSATED,
                    DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_ATMOSPHERIC_HUMIDITY, NUMBER_DIMENSIONLESS,
                    (measure) -> measure.rhumCompensated != null),
            new DynamicChannel<>(CHANNEL_TVOC_INDEX, "tvoc", NUMBER_DIMENSIONLESS,
                    (measure) -> measure.tvocIndex != null),
            new DynamicChannel<>(CHANNEL_TVOC_RAW, "tvoc", NUMBER_DIMENSIONLESS, (measure) -> measure.tvocRaw != null),
            new DynamicChannel<>(CHANNEL_NOX_INDEX, "nox", NUMBER_DIMENSIONLESS, (measure) -> measure.noxIndex != null),
            new DynamicChannel<>(CHANNEL_NOX_RAW, "nox", NUMBER_DIMENSIONLESS, (measure) -> measure.noxRaw != null));

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicChannelHelper.class);

    public static @Nullable String getDynamicChannelCapabilitySignature(String firmwareVersion,
            @Nullable String model) {
        if (firmwareVersion.isEmpty() && (model == null || model.isEmpty())) {
            return null;
        }

        return firmwareVersion + "|" + (model == null ? "" : model);
    }

    public static @Nullable ThingBuilder updateThingWithMeasurementChannels(Thing thing, @Nullable ThingBuilder builder,
            Supplier<ThingBuilder> builderSupplier, Measure measure) {
        return updateThingWithChannels(thing, builder, builderSupplier, MEASUREMENT_CHANNELS, measure);
    }

    public static @Nullable ThingBuilder updateThingWithConfigurationChannels(Thing thing,
            @Nullable ThingBuilder builder, Supplier<ThingBuilder> builderSupplier, LocalConfiguration configuration) {
        return updateThingWithChannels(thing, builder, builderSupplier, CONFIGURATION_CHANNELS, configuration);
    }

    private static <T> @Nullable ThingBuilder updateThingWithChannels(Thing thing, @Nullable ThingBuilder builder,
            Supplier<ThingBuilder> builderSupplier, List<DynamicChannel<T>> channels, T currentData) {
        ThingBuilder currentBuilder = builder;
        for (DynamicChannel<T> channel : channels) {
            currentBuilder = addDynamicChannel(thing, currentBuilder, builderSupplier, channel, currentData);
        }

        return currentBuilder;
    }

    private static <T> @Nullable ThingBuilder addDynamicChannel(Thing originalThing, @Nullable ThingBuilder builder,
            Supplier<ThingBuilder> builderSupplier, DynamicChannel<T> toAdd, T currentData) {
        if (!toAdd.isSupported().test(currentData)) {
            return builder;
        }

        ChannelUID channelId = new ChannelUID(originalThing.getUID(), toAdd.id);
        if (originalThing.getChannel(channelId) == null) {
            LOGGER.debug("Adding dynamic channel {} to {}", toAdd.id, originalThing.getUID());
            Channel channel = ChannelBuilder.create(channelId, toAdd.itemType).withType(toAdd.channelTypeUID).build();
            ThingBuilder currentBuilder = builder;
            if (currentBuilder == null) {
                currentBuilder = builderSupplier.get();
            }
            currentBuilder.withChannel(channel);
            return currentBuilder;
        }

        return builder;
    }
}
