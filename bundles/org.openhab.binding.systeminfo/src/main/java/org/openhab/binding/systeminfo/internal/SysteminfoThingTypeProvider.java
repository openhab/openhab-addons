/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.systeminfo.internal;

import static org.openhab.binding.systeminfo.internal.SysteminfoBindingConstants.*;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeRegistry;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended channels can be auto discovered and added to newly created groups in the {@link SysteminfoHandler}. The
 * thing needs to be updated to add the groups. The `SysteminfoThingTypeProvider` OSGi service gives access to the
 * `ThingTypeRegistry` and serves the updated `ThingType`.
 *
 * @author Mark Herwege - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { SysteminfoThingTypeProvider.class, ThingTypeProvider.class })
public class SysteminfoThingTypeProvider extends AbstractStorageBasedTypeProvider {
    private final Logger logger = LoggerFactory.getLogger(SysteminfoThingTypeProvider.class);

    private final ThingTypeRegistry thingTypeRegistry;
    private final ChannelGroupTypeRegistry channelGroupTypeRegistry;
    private final ChannelTypeRegistry channelTypeRegistry;

    private final Map<ThingUID, Map<String, Configuration>> thingChannelsConfig = new HashMap<>();

    @Activate
    public SysteminfoThingTypeProvider(@Reference ThingTypeRegistry thingTypeRegistry,
            @Reference ChannelGroupTypeRegistry channelGroupTypeRegistry,
            @Reference ChannelTypeRegistry channelTypeRegistry, @Reference StorageService storageService) {
        super(storageService);
        this.thingTypeRegistry = thingTypeRegistry;
        this.channelGroupTypeRegistry = channelGroupTypeRegistry;
        this.channelTypeRegistry = channelTypeRegistry;
    }

    /**
     * Create thing type with the provided typeUID and add it to the thing type registry.
     *
     * @param typeUID
     * @return false if base type UID `systeminfo:computer` cannot be found in the thingTypeRegistry
     */
    public boolean createThingType(ThingTypeUID typeUID) {
        logger.trace("Creating thing type {}", typeUID);
        return updateThingType(typeUID, getChannelGroupDefinitions(typeUID));
    }

    /**
     * Update `ThingType`with `typeUID`, replacing the channel group definitions with `groupDefs`.
     *
     * @param typeUID
     * @param groupDefs
     * @return false if `typeUID` or its base type UID `systeminfo:computer` cannot be found in the thingTypeRegistry
     */
    public boolean updateThingType(ThingTypeUID typeUID, List<ChannelGroupDefinition> groupDefs) {
        ThingType baseType = thingTypeRegistry.getThingType(typeUID);
        if (baseType == null) {
            baseType = thingTypeRegistry.getThingType(THING_TYPE_COMPUTER);
            if (baseType == null) {
                logger.warn("Could not find base thing type in registry.");
                return false;
            }
        }
        ThingTypeBuilder builder = createThingTypeBuilder(typeUID, baseType.getUID());
        if (builder != null) {
            logger.trace("Adding channel group definitions to thing type");
            ThingType type = builder.withChannelGroupDefinitions(groupDefs).build();

            putThingType(type);
            return true;
        } else {
            logger.debug("Error adding channel groups");
            return false;
        }
    }

    /**
     * Return a {@link ThingTypeBuilder} that can create an exact copy of the `ThingType` with `baseTypeUID`.
     * Further build steps can be performed on the returned object before recreating the `ThingType` from the builder.
     *
     * @param newTypeUID
     * @param baseTypeUID
     * @return the ThingTypeBuilder, null if `baseTypeUID` cannot be found in the thingTypeRegistry
     */
    private @Nullable ThingTypeBuilder createThingTypeBuilder(ThingTypeUID newTypeUID, ThingTypeUID baseTypeUID) {
        ThingType type = thingTypeRegistry.getThingType(baseTypeUID);

        if (type == null) {
            return null;
        }

        ThingTypeBuilder result = ThingTypeBuilder.instance(newTypeUID, type.getLabel())
                .withChannelGroupDefinitions(type.getChannelGroupDefinitions())
                .withChannelDefinitions(type.getChannelDefinitions())
                .withExtensibleChannelTypeIds(type.getExtensibleChannelTypeIds())
                .withSupportedBridgeTypeUIDs(type.getSupportedBridgeTypeUIDs()).withProperties(type.getProperties())
                .isListed(false);

        String representationProperty = type.getRepresentationProperty();
        if (representationProperty != null) {
            result = result.withRepresentationProperty(representationProperty);
        }
        URI configDescriptionURI = type.getConfigDescriptionURI();
        if (configDescriptionURI != null) {
            result = result.withConfigDescriptionURI(configDescriptionURI);
        }
        String category = type.getCategory();
        if (category != null) {
            result = result.withCategory(category);
        }
        String description = type.getDescription();
        if (description != null) {
            result = result.withDescription(description);
        }

        return result;
    }

    /**
     * Return List of {@link ChannelGroupDefinition} for `ThingType` with `typeUID`. If the `ThingType` does not exist
     * in the thingTypeRegistry yet, retrieve list of `ChannelGroupDefinition` for base type systeminfo:computer.
     *
     * @param typeUID UID for ThingType
     * @return list of channel group definitions, empty list if no channel group definitions
     */
    public List<ChannelGroupDefinition> getChannelGroupDefinitions(ThingTypeUID typeUID) {
        ThingType type = thingTypeRegistry.getThingType(typeUID);
        if (type == null) {
            type = thingTypeRegistry.getThingType(THING_TYPE_COMPUTER);
        }
        if (type != null) {
            return type.getChannelGroupDefinitions();
        } else {
            logger.debug("Cannot retrieve channel group definitions, no base thing type found");
            return Collections.emptyList();
        }
    }

    /**
     * Create a new channel group definition with index appended to id and label.
     *
     * @param channelGroupID id of channel group without index
     * @param channelGroupTypeID id ChannelGroupType for new channel group definition
     * @param i index
     * @return channel group definition, null if provided channelGroupTypeID cannot be found in ChannelGroupTypeRegistry
     */
    public @Nullable ChannelGroupDefinition createChannelGroupDefinitionWithIndex(String channelGroupID,
            String channelGroupTypeID, int i) {
        ChannelGroupTypeUID channelGroupTypeUID = new ChannelGroupTypeUID(BINDING_ID, channelGroupTypeID);
        ChannelGroupType channelGroupType = channelGroupTypeRegistry.getChannelGroupType(channelGroupTypeUID);
        if (channelGroupType == null) {
            logger.debug("Cannot create channel group definition, group type {} invalid", channelGroupTypeID);
            return null;
        }
        String index = String.valueOf(i);
        return new ChannelGroupDefinition(channelGroupID + index, channelGroupTypeUID,
                channelGroupType.getLabel() + " " + index, channelGroupType.getDescription());
    }

    /**
     * Create a new channel with index appended to id and label of an existing channel.
     *
     * @param thing containing the existing channel
     * @param channelID id of channel without index
     * @param i index
     * @return channel, null if provided channelID does not match a channel, or no type can be retrieved for the
     *         provided channel
     */
    public @Nullable Channel createChannelWithIndex(Thing thing, String channelID, int i) {
        Channel baseChannel = thing.getChannel(channelID);
        if (baseChannel == null) {
            logger.debug("Cannot create channel, ID {} invalid", channelID);
            return null;
        }
        ChannelTypeUID channelTypeUID = baseChannel.getChannelTypeUID();
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);
        if (channelType == null) {
            logger.debug("Cannot create channel, type {} invalid",
                    channelTypeUID != null ? channelTypeUID.getId() : "null");
            return null;
        }
        ThingUID thingUID = thing.getUID();
        String index = String.valueOf(i);
        ChannelUID channelUID = new ChannelUID(thingUID, channelID + index);
        ChannelBuilder builder = ChannelBuilder.create(channelUID).withType(channelTypeUID)
                .withConfiguration(baseChannel.getConfiguration());
        builder.withLabel(channelType.getLabel() + " " + index);
        String description = channelType.getDescription();
        if (description != null) {
            builder.withDescription(description);
        }
        return builder.build();
    }

    /**
     * Store the channel configurations for a thing, to be able to restore them later when the thing handler for the
     * same thing gets recreated with a new thing type. This is necessary because the
     * {@link BaseThingHandler##changeThingType()} method reverts channel configurations to their defaults.
     *
     * @param thing
     */
    public void storeChannelsConfig(Thing thing) {
        Map<String, Configuration> channelsConfig = thing.getChannels().stream()
                .collect(Collectors.toMap(c -> c.getUID().getId(), c -> c.getConfiguration()));
        thingChannelsConfig.put(thing.getUID(), channelsConfig);
    }

    /**
     * Restore previous channel configurations of matching channels when the thing handler gets recreated with a new
     * thing type. Return an empty map if no channel configurations where stored. Before returning previous channel
     * configurations, clear the store, so they can only be retrieved ones, immediately after a thing type change. See
     * also {@link #storeChannelsConfig(Thing)}.
     *
     * @param UID
     * @return Map of ChannelId and Configuration for the channel
     */
    public Map<String, Configuration> restoreChannelsConfig(ThingUID UID) {
        Map<String, Configuration> configs = thingChannelsConfig.remove(UID);
        return configs != null ? configs : Collections.emptyMap();
    }
}
