/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.openhab.binding.neeo.NeeoConstants;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoMacro;
import org.openhab.binding.neeo.internal.models.NeeoRecipe;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.binding.neeo.internal.models.NeeoScenario;

/**
 * Helper methods for generating the openHAB metadata.
 *
 * @author Tim Roberts - Initial Contribution
 */

class MetadataUtils {
    /**
     * Gets the group definitions from a {@link NeeoRoom}
     *
     * @param room the non-null room
     * @return the possibly empty, not-null list of {@link ChannelGroupDefinition}
     */
    static List<ChannelGroupDefinition> getGroupDefinitions(NeeoRoom room) {
        Objects.requireNonNull(room, "room cannot be null");

        final List<ChannelGroupDefinition> groupDefinitions = new ArrayList<>();

        groupDefinitions.add(new ChannelGroupDefinition(NeeoConstants.ROOM_CHANNEL_GROUP_STATEID,
                new ChannelGroupTypeUID(NeeoConstants.BINDING_ID, NeeoConstants.ROOM_CHANNEL_GROUP_STATE),
                "Current State", "The state information for " + room.getName()));

        for (NeeoScenario scenario : room.getScenarios().getScenarios()) {
            groupDefinitions
                    .add(new ChannelGroupDefinition(UidUtils.generateGroupId(scenario),
                            new ChannelGroupTypeUID(NeeoConstants.BINDING_ID,
                                    NeeoConstants.ROOM_CHANNEL_GROUP_SCENARIO),
                            "Scenario " + scenario.getName(),
                            "Scenario for " + scenario.getName() + " (" + scenario.getKey() + ")"));
        }

        for (NeeoRecipe recipe : room.getRecipes().getRecipes()) {
            // if (recipe.isHiddenRecipe()) {
            // continue;
            // }

            groupDefinitions.add(new ChannelGroupDefinition(UidUtils.generateGroupId(recipe),
                    new ChannelGroupTypeUID(NeeoConstants.BINDING_ID, NeeoConstants.ROOM_CHANNEL_GROUP_RECIPE),
                    "Recipe " + recipe.getName() + " (" + recipe.getType() + ")",
                    "Recipe for " + recipe.getName() + " (" + recipe.getKey() + ")"));
        }
        return groupDefinitions;
    }

    /**
     * Gets a list of {@link ChannelType} for the {@link NeeoDevice}. This method will create a {@link ChannelType} for
     * each {@link NeeoMacro} found in the device
     *
     * @param device a non-null {@link NeeoDevice}
     * @return a non-null, possibly empty list of {@link ChannelType}
     */
    static List<ChannelType> getChannelTypes(NeeoDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        final List<ChannelType> channelTypes = new ArrayList<>();
        for (NeeoMacro macro : device.getMacros().getMacros()) {
            final ChannelType channelType = new ChannelType(UidUtils.createChannelType(macro), false, "Switch",
                    macro.getName(), "Send ON to trigger the macro", null, null, null, null);

            channelTypes.add(channelType);
        }

        return channelTypes;
    }

    /**
     * Gets a list of {@link ChannelGroupType} for the {@link NeeoDevice}. This method will create return exactly one
     * {@link ChannelGroupType} representing the macros
     *
     * @param device a non-null {@link NeeoDevice}
     * @return a non-null, possibly empty list of {@link ChannelGroupType}
     */
    static List<ChannelGroupType> getChannelGroupTypes(NeeoDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        final List<ChannelGroupType> channelGroupTypes = new ArrayList<>();

        final ChannelGroupTypeUID macrosGroupTypeUID = UidUtils.createMacroChannelGroupType();

        final List<ChannelDefinition> channelDefinitions = new ArrayList<>();
        for (NeeoMacro macro : device.getMacros().getMacros()) {
            channelDefinitions.add(new ChannelDefinition(macro.getKey(), UidUtils.createChannelType(macro)));
        }

        final ChannelGroupType macrosGroupType = new ChannelGroupType(macrosGroupTypeUID, false, "Macros",
                "Macros for " + device.getName(), null, channelDefinitions);

        channelGroupTypes.add(macrosGroupType);

        return channelGroupTypes;
    }

    /**
     * Gets the channel definitions from a {@link NeeoDevice}
     *
     * @param device the non-null device
     * @return the possibly empty, not-null list of {@link ChannelDefinition}
     */
    static List<ChannelGroupDefinition> getGroupDefinitions(NeeoDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        final List<ChannelGroupDefinition> groupChannelDefinitions = new ArrayList<>();
        groupChannelDefinitions.add(new ChannelGroupDefinition(NeeoConstants.DEVICE_GROUP_MACROSID,
                new ChannelGroupTypeUID(NeeoConstants.BINDING_ID, NeeoConstants.DEVICE_GROUP_MACROS), "Macros",
                "Macros for " + device.getName()));

        return groupChannelDefinitions;
    }
}
