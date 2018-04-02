/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.type;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.openhab.binding.neeo.NeeoConstants;
import org.openhab.binding.neeo.NeeoUtil;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates ThingTypes based on metadata from the {@link NeeoRoom}
 *
 * @author Tim Roberts - Initial Contribution
 */
@Component(immediate = true)
public class NeeoTypeGeneratorImpl implements NeeoTypeGenerator {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoTypeGeneratorImpl.class);

    /** The thing type provider */
    @NonNullByDefault({})
    private NeeoThingTypeProvider thingTypeProvider;

    /** The config description provider */
    @NonNullByDefault({})
    private NeeoConfigDescriptionProvider configDescriptionProvider;

    /** The channel type provider */
    @NonNullByDefault({})
    private NeeoChannelTypeProvider channelTypeProvider;

    /**
     * There is no initialization needed
     *
     * @see org.openhab.binding.neeo.internal.type.NeeoTypeGenerator#initialize()
     */
    @Activate
    @Override
    public void initialize() {
    }

    /**
     * Sets the thing type provider.
     *
     * @param thingTypeProvider the non-null thing type provider
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, service = NeeoThingTypeProvider.class, name = "ThingTypeProvider", policy = ReferencePolicy.DYNAMIC, unbind = "unsetThingTypeProvider")
    public void setThingTypeProvider(NeeoThingTypeProvider thingTypeProvider) {
        Objects.requireNonNull(thingTypeProvider, "thingTypeProvider cannot be null");

        this.thingTypeProvider = thingTypeProvider;
    }

    /**
     * Unsets thing type provider.
     *
     * @param thingTypeProvider the thing type provider (ignored)
     */
    public void unsetThingTypeProvider(NeeoThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = null;
    }

    /**
     * Sets the channel type provider.
     *
     * @param channelTypeProvider the non-null channel type provider
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, service = NeeoChannelTypeProvider.class, name = "ChannelTypeProvider", policy = ReferencePolicy.DYNAMIC, unbind = "unsetChannelTypeProvider")
    public void setChannelTypeProvider(NeeoChannelTypeProvider channelTypeProvider) {
        Objects.requireNonNull(channelTypeProvider, "channelTypeProvider cannot be null");

        this.channelTypeProvider = channelTypeProvider;
    }

    /**
     * Unsets channel type provider.
     *
     * @param channelTypeProvider the channel type provider (ignored)
     */
    public void unsetChannelTypeProvider(NeeoChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

    /**
     * Sets the config description provider.
     *
     * @param configDescriptionProvider the non-null config description provider
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, service = NeeoConfigDescriptionProvider.class, name = "ConfigDescriptionProvider", policy = ReferencePolicy.DYNAMIC, unbind = "unsetConfigDescriptionProvider")
    public void setConfigDescriptionProvider(NeeoConfigDescriptionProvider configDescriptionProvider) {
        Objects.requireNonNull(configDescriptionProvider, "configDescriptionProvider cannot be null");
        this.configDescriptionProvider = configDescriptionProvider;
    }

    /**
     * Unset config description provider.
     *
     * @param configDescriptionProvider the config description provider (ignored)
     */
    public void unsetConfigDescriptionProvider(NeeoConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = null;
    }

    @Override
    public void generate(String brainId, NeeoRoom room) {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        Objects.requireNonNull(room, "room cannot be null");

        final NeeoThingTypeProvider localThingTypeProvider = thingTypeProvider;
        Objects.requireNonNull(localThingTypeProvider, "thingTypeProvider cannot be null");

        logger.debug("Generating ThingType for room '{}' ({})", room.getName(), room.getKey());
        localThingTypeProvider.addThingType(createThingType(brainId, room));
    }

    /**
     * Creates the ThingType for the given {@link NeeoRoom}.
     *
     * @param brainId the non-null, non-empty brainID
     * @param room the non-null room
     * @return the thing type
     */
    private ThingType createThingType(String brainId, NeeoRoom room) {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        Objects.requireNonNull(room, "room cannot be null");

        final NeeoConfigDescriptionProvider localConfigDescriptionProvider = configDescriptionProvider;
        Objects.requireNonNull(localConfigDescriptionProvider, "configDescriptionProvider cannot be null");

        final String label = "NEEO Room " + room.getName() + " (" + brainId + ")";
        final String description = String.format("%s (%s)", label, room.getKey());

        final List<String> supportedBridgeTypeUids = new ArrayList<>();
        supportedBridgeTypeUids.add(NeeoConstants.BRIDGE_TYPE_BRAIN.toString());

        final ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(room);

        final Map<String, String> properties = new HashMap<>();

        final URI configDescriptionURI = getConfigDescriptionURI(room);
        if (localConfigDescriptionProvider.getConfigDescription(configDescriptionURI, null) == null) {
            generateConfigDescription(room, configDescriptionURI);
        }

        final List<ChannelGroupDefinition> groupDefinitions = MetadataUtils.getGroupDefinitions(room);

        return ThingTypeBuilder.instance(thingTypeUID, label).withSupportedBridgeTypeUIDs(supportedBridgeTypeUids)
                .withDescription(description).withChannelGroupDefinitions(groupDefinitions).withProperties(properties)
                .withConfigDescriptionURI(configDescriptionURI).buildBridge();
    }

    /**
     * Generate config description.
     *
     * @param room the non-null room
     * @param configDescriptionURI the non-null config description URI
     */
    private void generateConfigDescription(NeeoRoom room, URI configDescriptionURI) {
        Objects.requireNonNull(room, "room cannot be null");
        Objects.requireNonNull(configDescriptionURI, "configDescriptionURI cannot be null");

        final NeeoConfigDescriptionProvider localConfigDescriptionProvider = configDescriptionProvider;
        Objects.requireNonNull(localConfigDescriptionProvider, "configDescriptionProvider cannot be null");

        final List<ConfigDescriptionParameter> parms = new ArrayList<>();
        final List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        final ConfigDescriptionParameterBuilder keyParmBuilder = ConfigDescriptionParameterBuilder
                .create(NeeoConstants.CONFIG_ROOMKEY, Type.TEXT).withLabel("Room Key")
                .withDescription("Unique key of the room").withRequired(true);
        parms.add(keyParmBuilder.build());

        final ConfigDescriptionParameterBuilder pollingParmBuilder = ConfigDescriptionParameterBuilder
                .create(NeeoConstants.CONFIG_REFRESH_POLLING, Type.INTEGER).withLabel("Refresh Polling")
                .withDescription("The time (in seconds) to refresh state (<= 0 to disable)").withDefault("120")
                .withAdvanced(true);
        parms.add(pollingParmBuilder.build());

        final ConfigDescriptionParameterBuilder exludeParmBuilder = ConfigDescriptionParameterBuilder
                .create(NeeoConstants.CONFIG_EXCLUDE_THINGS, Type.BOOLEAN).withLabel("Exclude Things")
                .withDescription("Exclude openHAB things (from NEEO transport)").withDefault("true").withAdvanced(true);
        parms.add(exludeParmBuilder.build());

        localConfigDescriptionProvider.addConfigDescription(new ConfigDescription(configDescriptionURI, parms, groups));
    }

    /**
     * Gets the config description URI for the room
     *
     * @param room the non-null room
     * @return the config description URI
     */
    private URI getConfigDescriptionURI(NeeoRoom room) {
        Objects.requireNonNull(room, "room cannot be null");

        try {
            return new URI(String.format("%s:%s", NeeoConstants.CONFIG_DESCRIPTION_URI_ROOM,
                    UidUtils.generateThingTypeUID(room)));
        } catch (URISyntaxException ex) {
            throw new UnsupportedOperationException("Can't create configDescriptionURI for room " + room.getKey());
        }
    }

    @Override
    public void generate(String brainId, ThingTypeUID roomUid, NeeoDevice device) {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be null");
        Objects.requireNonNull(roomUid, "roomUid cannot be null");
        Objects.requireNonNull(device, "device cannot be null");

        final NeeoThingTypeProvider localThingTypeProvider = thingTypeProvider;
        Objects.requireNonNull(localThingTypeProvider, "thingTypeProvider cannot be null");

        logger.debug("Generating ThingType for device '{}' ({})", device.getName(), device.getKey());
        localThingTypeProvider.addThingType(createThingType(brainId, roomUid, device));
    }

    /**
     * Creates the ThingType for the given {@link NeeoDevice}.
     *
     * @param brainId the non-null, non-empty brainID
     * @param roomUid the non-null room Uid
     * @param device the non-null device
     * @return the thing type
     */
    private ThingType createThingType(String brainId, ThingTypeUID roomUid, NeeoDevice device) {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        Objects.requireNonNull(roomUid, "roomUid cannot be null");
        Objects.requireNonNull(device, "device cannot be null");

        final NeeoConfigDescriptionProvider localConfigDescriptionProvider = configDescriptionProvider;
        Objects.requireNonNull(localConfigDescriptionProvider, "configDescriptionProvider cannot be null");

        final NeeoChannelTypeProvider localChannelTypeProvider = channelTypeProvider;
        Objects.requireNonNull(localChannelTypeProvider, "channelTypeProvider cannot be null");

        final String label = "NEEO Device " + device.getName() + " (" + brainId + ")";
        final String description = String.format("%s (%s)", label, device.getKey());

        final List<String> supportedBridgeTypeUids = new ArrayList<>();
        supportedBridgeTypeUids.add(roomUid.toString());

        final ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(device);

        final Map<String, String> properties = new HashMap<>();

        final URI configDescriptionURI = getConfigDescriptionURI(device);
        if (localConfigDescriptionProvider.getConfigDescription(configDescriptionURI, null) == null) {
            generateConfigDescription(device, configDescriptionURI);
        }

        localChannelTypeProvider.addChannelTypes(MetadataUtils.getChannelTypes(device));
        localChannelTypeProvider.addChannelGroupTypes(MetadataUtils.getChannelGroupTypes(device));

        final List<ChannelGroupDefinition> groupDefinitions = MetadataUtils.getGroupDefinitions(device);

        return ThingTypeBuilder.instance(thingTypeUID, label).withSupportedBridgeTypeUIDs(supportedBridgeTypeUids)
                .withDescription(description).withChannelGroupDefinitions(groupDefinitions).withProperties(properties)
                .withConfigDescriptionURI(configDescriptionURI).build();
    }

    /**
     * Generate config description.
     *
     * @param device the non-null device
     * @param configDescriptionURI the non-null config description URI
     */
    private void generateConfigDescription(NeeoDevice device, URI configDescriptionURI) {
        Objects.requireNonNull(device, "device cannot be null");
        Objects.requireNonNull(configDescriptionURI, "configDescriptionURI cannot be null");

        final NeeoConfigDescriptionProvider localConfigDescriptionProvider = configDescriptionProvider;
        Objects.requireNonNull(localConfigDescriptionProvider, "configDescriptionProvider cannot be null");

        final List<ConfigDescriptionParameter> parms = new ArrayList<>();
        final List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        final ConfigDescriptionParameterBuilder keyParmBuilder = ConfigDescriptionParameterBuilder
                .create(NeeoConstants.CONFIG_DEVICEKEY, Type.TEXT).withLabel("Device Key")
                .withDescription("Unique key of the device").withRequired(true);
        parms.add(keyParmBuilder.build());

        localConfigDescriptionProvider.addConfigDescription(new ConfigDescription(configDescriptionURI, parms, groups));
    }

    /**
     * Gets the config description URI for the device
     *
     * @param device the non-null device
     * @return the config description URI
     */
    private URI getConfigDescriptionURI(NeeoDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        try {
            return new URI(String.format("%s:%s", NeeoConstants.CONFIG_DESCRIPTION_URI_DEVICE,
                    UidUtils.generateThingTypeUID(device)));
        } catch (URISyntaxException ex) {
            throw new UnsupportedOperationException(
                    "Can't create configDescriptionURI for device {}" + device.getKey());
        }
    }
}
