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
package org.openhab.binding.neeo.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.openhab.binding.neeo.internal.NeeoConstants;
import org.openhab.binding.neeo.internal.UidUtils;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoMacro;
import org.openhab.binding.neeo.internal.models.NeeoRecipe;
import org.openhab.binding.neeo.internal.models.NeeoRecipes;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.binding.neeo.internal.models.NeeoScenario;
import org.openhab.binding.neeo.internal.models.NeeoScenarios;

/**
 * Utility class for generating channels
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
class ChannelUtils {
    /**
     * Generates a list of {@link Channel} s for a specific device. This implementation will generate a channel for each
     * macro found on the device.
     *
     * @param thingUid a non-null thingUID
     * @param device a non-null device
     * @return a non-null but possibly empty list of {@link Channel} s
     */
    static List<Channel> generateChannels(ThingUID thingUid, NeeoDevice device) {
        Objects.requireNonNull(thingUid, "thingUid cannot be null");
        Objects.requireNonNull(device, "device cannot be null");

        final List<Channel> channels = new ArrayList<>();
        for (NeeoMacro macro : device.getMacros().getMacros()) {
            final String key = macro.getKey();
            if (key != null && StringUtils.isNotEmpty(key)) {
                final String label = StringUtils.isEmpty(macro.getName()) ? macro.getLabel() : macro.getName();
                channels.add(ChannelBuilder
                        .create(UidUtils.createChannelUID(thingUid, NeeoConstants.DEVICE_GROUP_MACROS_ID,
                                NeeoConstants.DEVICE_CHANNEL_STATUS, key), "Switch")
                        .withLabel(label == null || StringUtils.isEmpty(label) ? key : label)
                        .withType(NeeoConstants.DEVICE_MACRO_STATUS_UID).build());
            }
        }
        return channels;
    }

    /**
     * Generates a list of {@link Channel} s for a specific room. This implementation will generate multiple channels
     * for each scenario and recipe in the room (in addition to the current step channel)
     *
     * @param thingUid a non-null thingUID
     * @param room a non-null room
     * @return a non-null but possibly empty list of {@link Channel} s
     */
    static List<Channel> generateChannels(ThingUID thingUid, NeeoRoom room) {
        Objects.requireNonNull(thingUid, "thingUid cannot be null");
        Objects.requireNonNull(room, "room cannot be null");

        final List<Channel> channels = new ArrayList<>();
        channels.addAll(generateStateChannels(thingUid));
        channels.addAll(generateScenarioChannels(thingUid, room.getScenarios()));
        channels.addAll(generateRecipeChannels(thingUid, room.getRecipes()));
        return channels;
    }

    /**
     * Helper method to generate state channels (the current step)
     *
     * @param thingUid a non-null thingUID
     * @return a non-null but possibly empty list of {@link Channel} s
     */
    private static List<Channel> generateStateChannels(ThingUID thingUid) {
        Objects.requireNonNull(thingUid, "thingUid cannot be null");

        final List<Channel> channels = new ArrayList<>();
        channels.add(ChannelBuilder
                .create(UidUtils.createChannelUID(thingUid, NeeoConstants.ROOM_GROUP_STATE_ID,
                        NeeoConstants.ROOM_CHANNEL_CURRENTSTEP), "String")
                .withType(NeeoConstants.ROOM_STATE_CURRENTSTEP_UID).build());
        return channels;
    }

    /**
     * Helper method to generate scenario channels
     *
     * @param thingUid a non-null thingUID
     * @param scenarios the non-null scenarios
     * @return a non-null but possibly empty list of {@link Channel} s
     */
    private static List<Channel> generateScenarioChannels(ThingUID thingUid, NeeoScenarios scenarios) {
        Objects.requireNonNull(thingUid, "thingUid cannot be null");
        Objects.requireNonNull(scenarios, "scenarios cannot be null");

        final List<Channel> channels = new ArrayList<>();
        for (NeeoScenario scenario : scenarios.getScenarios()) {
            final String key = scenario.getKey();
            if (key != null && StringUtils.isNotEmpty(key)) {
                final String scenarioLabel = StringUtils.isEmpty(scenario.getName()) ? null : scenario.getName();
                final String nameLabel = (scenarioLabel == null || StringUtils.isEmpty(scenarioLabel) ? key
                        : scenarioLabel) + " Name";
                final String configuredLabel = (scenarioLabel == null || StringUtils.isEmpty(scenarioLabel) ? key
                        : scenarioLabel) + " Configured";
                final String statusLabel = (scenarioLabel == null || StringUtils.isEmpty(scenarioLabel) ? key
                        : scenarioLabel) + " Status";

                channels.add(ChannelBuilder
                        .create(UidUtils.createChannelUID(thingUid, NeeoConstants.ROOM_GROUP_SCENARIO_ID,
                                NeeoConstants.ROOM_CHANNEL_NAME, key), "String")
                        .withLabel(nameLabel).withType(NeeoConstants.ROOM_SCENARIO_NAME_UID).build());
                channels.add(ChannelBuilder
                        .create(UidUtils.createChannelUID(thingUid, NeeoConstants.ROOM_GROUP_SCENARIO_ID,
                                NeeoConstants.ROOM_CHANNEL_CONFIGURED, key), "Switch")
                        .withLabel(configuredLabel).withType(NeeoConstants.ROOM_SCENARIO_CONFIGURED_UID).build());
                channels.add(ChannelBuilder
                        .create(UidUtils.createChannelUID(thingUid, NeeoConstants.ROOM_GROUP_SCENARIO_ID,
                                NeeoConstants.ROOM_CHANNEL_STATUS, key), "Switch")
                        .withLabel(statusLabel).withType(NeeoConstants.ROOM_SCENARIO_STATUS_UID).build());
            }
        }
        return channels;
    }

    /**
     * Helper method to generate recipe channels
     *
     * @param thingUid a non-null thingUID
     * @param recipes the non-null recipes
     * @return a non-null but possibly empty list of {@link Channel} s
     */
    private static List<Channel> generateRecipeChannels(ThingUID thingUid, NeeoRecipes recipes) {
        Objects.requireNonNull(thingUid, "thingUid cannot be null");
        Objects.requireNonNull(recipes, "recipes cannot be null");

        final List<Channel> channels = new ArrayList<>();
        for (NeeoRecipe recipe : recipes.getRecipes()) {
            final String key = recipe.getKey();
            if (key != null && StringUtils.isNotEmpty(key)) {
                final String recipeLabel = StringUtils.isEmpty(recipe.getName()) ? null : recipe.getName();
                final String nameLabel = (recipeLabel == null || StringUtils.isEmpty(recipeLabel) ? key : recipeLabel)
                        + " Name (" + recipe.getType() + ")";
                final String typeLabel = (recipeLabel == null || StringUtils.isEmpty(recipeLabel) ? key : recipeLabel)
                        + " Type (" + recipe.getType() + ")";
                final String enabledLabel = (recipeLabel == null || StringUtils.isEmpty(recipeLabel) ? key
                        : recipeLabel) + " Enabled (" + recipe.getType() + ")";
                final String statusLabel = (recipeLabel == null || StringUtils.isEmpty(recipeLabel) ? key : recipeLabel)
                        + " Status (" + recipe.getType() + ")";

                channels.add(ChannelBuilder
                        .create(UidUtils.createChannelUID(thingUid, NeeoConstants.ROOM_GROUP_RECIPE_ID,
                                NeeoConstants.ROOM_CHANNEL_NAME, key), "String")
                        .withLabel(nameLabel).withType(NeeoConstants.ROOM_RECIPE_NAME_UID).build());
                channels.add(ChannelBuilder
                        .create(UidUtils.createChannelUID(thingUid, NeeoConstants.ROOM_GROUP_RECIPE_ID,
                                NeeoConstants.ROOM_CHANNEL_TYPE, key), "String")
                        .withLabel(enabledLabel).withType(NeeoConstants.ROOM_RECIPE_TYPE_UID).build());
                channels.add(ChannelBuilder
                        .create(UidUtils.createChannelUID(thingUid, NeeoConstants.ROOM_GROUP_RECIPE_ID,
                                NeeoConstants.ROOM_CHANNEL_ENABLED, key), "Switch")
                        .withLabel(typeLabel).withType(NeeoConstants.ROOM_RECIPE_ENABLED_UID).build());
                channels.add(ChannelBuilder
                        .create(UidUtils.createChannelUID(thingUid, NeeoConstants.ROOM_GROUP_RECIPE_ID,
                                NeeoConstants.ROOM_CHANNEL_STATUS, key), "Switch")
                        .withLabel(statusLabel).withType(NeeoConstants.ROOM_RECIPE_STATUS_UID).build());
            }
        }
        return channels;
    }
}
