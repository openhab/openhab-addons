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
package org.openhab.binding.smartthings.internal.type;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDeviceData;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterGroup;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class SmartthingsTypeRegistryImpl implements SmartthingsTypeRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SmartthingsTypeRegistryImpl.class);

    private @Nullable SmartthingsThingTypeProvider thingTypeProvider;
    private @Nullable SmartthingsChannelTypeProvider channelTypeProvider;
    private @Nullable SmartthingsChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SmartthingsConfigDescriptionProvider configDescriptionProvider;

    public SmartthingsTypeRegistryImpl() {
    }

    @Reference
    protected void setThingTypeProvider(SmartthingsThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = thingTypeProvider;
    }

    protected void unsetThingTypeProvider(SmartthingsThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = null;
    }

    @Reference
    protected void setChannelTypeProvider(SmartthingsChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    protected void unsetChannelTypeProvider(SmartthingsChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

    //
    @Reference
    protected void setChannelGroupTypeProvider(SmartthingsChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = channelGroupTypeProvider;
    }

    protected void unsetChannelGroupTypeProvider(SmartthingsChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = null;
    }

    @Reference
    protected void setConfigDescriptionProvider(SmartthingsConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = configDescriptionProvider;
    }

    protected void unsetConfigDescriptionProvider(SmartthingsConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = null;
    }

    /**
     * Initializes the type generator.
     */
    @Override
    @Activate
    public void initialize() {
        // generateThingsType("accelerationSensor", "Acceleration Sensor", "Acceleration Sensor");
        // generateThingsType("light", "Light", "Light");
    }

    @Override
    public void Register(SmartthingsDeviceData deviceData, JsonObject devObj) {
        generateThingsType(deviceData.id, deviceData.deviceType, deviceData.description, devObj);

    }

    private void generateThingsType(String deviceLabel, String deviceType, String deviceDescription,
            JsonObject devObj) {

        SmartthingsThingTypeProvider lcThingTypeProvider = thingTypeProvider;
        SmartthingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;
        SmartthingsChannelGroupTypeProvider lcChannelGroupTypeProvider = channelGroupTypeProvider;

        if (lcThingTypeProvider != null) {
            ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(deviceType);
            ThingType tt = null;

            tt = lcThingTypeProvider.getInternalThingType(thingTypeUID);

            if (tt == null) {
                List<ChannelGroupType> groupTypes = new ArrayList<>();
                List<ChannelDefinition> channelDefinitions = new ArrayList<>();

                JsonElement components = devObj.get("components");
                if (components == null || !components.isJsonArray()) {
                    return;
                }
                JsonArray componentsArray = (JsonArray) components;

                for (JsonElement elm : componentsArray) {
                    JsonObject component = (JsonObject) elm;
                    String compId = component.get("id").getAsString();
                    String compLabel = component.get("label").getAsString();

                    if (!compId.equals("main")) {
                        continue;
                    }

                    JsonElement capabilitites = component.get("capabilities");
                    if (capabilitites != null && capabilitites.isJsonArray()) {
                        JsonArray capabilititesArray = (JsonArray) capabilitites;
                        for (JsonElement elmCap : capabilititesArray) {
                            JsonObject elmCapObj = (JsonObject) elmCap;
                            String capId = elmCapObj.get("id").getAsString();
                            String capVersion = elmCapObj.get("version").getAsString();

                            capId = capId.replace('.', '_');

                            addChannel(groupTypes, channelDefinitions, capId + "channel", capId);

                            logger.info("");
                        }
                    }

                }

                tt = createThingType(deviceType, deviceLabel, deviceDescription, groupTypes);
                lcThingTypeProvider.addThingType(tt);
            }
        }
    }

    private void addChannel(List<ChannelGroupType> groupTypes, List<ChannelDefinition> channelDefinitions,
            String channelTp, String channel) {
        SmartthingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;
        SmartthingsChannelGroupTypeProvider lcChannelGroupTypeProvider = channelGroupTypeProvider;

        ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelTp);
        ChannelType channelType = null;
        if (lcChannelTypeProvider != null) {
            channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
            if (channelType == null) {
                channelType = createChannelType(channelTp, channelTypeUID);
                lcChannelTypeProvider.addChannelType(channelType);
            }
        }

        Map<String, String> props = new Hashtable<String, String>();

        if (channelType != null) {
            ChannelDefinition channelDef = new ChannelDefinitionBuilder(channel, channelType.getUID())
                    .withLabel(channel).withDescription("description").withProperties(props).build();
            channelDefinitions.add(channelDef);
        }

        // generate group
        ChannelGroupTypeUID groupTypeUID = UidUtils.generateChannelGroupTypeUID("default");
        ChannelGroupType groupType = null;

        if (lcChannelGroupTypeProvider != null) {
            groupType = lcChannelGroupTypeProvider.getInternalChannelGroupType(groupTypeUID);

            if (groupType == null) {
                String groupLabel = "default";
                groupType = ChannelGroupTypeBuilder.instance(groupTypeUID, groupLabel)
                        .withChannelDefinitions(channelDefinitions).withCategory("").withDescription("default").build();
                lcChannelGroupTypeProvider.addChannelGroupType(groupType);
                groupTypes.add(groupType);
            }
        }
    }

    private ChannelType createChannelType(String itemType, ChannelTypeUID channelTypeUID) {
        ChannelType channelType;
        String label = itemType;

        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();

        final StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                .withStateDescriptionFragment(stateFragment.build());

        channelType = channelTypeBuilder.isAdvanced(false).withDescription("description").withCategory("category")
                .build();

        return channelType;

    }

    /**
     * Creates the ThingType for the given device.
     */
    private ThingType createThingType(String device, String label, String description,
            List<ChannelGroupType> groupTypes) {
        SmartthingsConfigDescriptionProvider lcConfigDescriptionProvider = configDescriptionProvider;
        String name = device;

        List<String> supportedBridgeTypeUids = new ArrayList<>();
        supportedBridgeTypeUids.add(SmartthingsBindingConstants.THING_TYPE_SMARTTHINGSCLOUD.toString());

        ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(device);

        Map<String, String> properties = new HashMap<>();

        URI configDescriptionURI = getConfigDescriptionURI(device);
        if (lcConfigDescriptionProvider != null
                && lcConfigDescriptionProvider.getInternalConfigDescription(configDescriptionURI) == null) {
            generateConfigDescription(device, groupTypes, configDescriptionURI);
        }

        List<ChannelGroupDefinition> groupDefinitions = new ArrayList<>();
        for (ChannelGroupType groupType : groupTypes) {
            String id = groupType.getUID().getId();
            groupDefinitions.add(new ChannelGroupDefinition(id, groupType.getUID()));
        }

        return ThingTypeBuilder.instance(thingTypeUID, name).withSupportedBridgeTypeUIDs(supportedBridgeTypeUids)
                .withLabel(label).withDescription(description).withRepresentationProperty(Thing.PROPERTY_MODEL_ID)
                .withConfigDescriptionURI(configDescriptionURI)
                .withCategory(SmartthingsBindingConstants.CATEGORY_THING_HVAC)
                .withChannelGroupDefinitions(groupDefinitions).withProperties(properties).build();
    }

    private URI getConfigDescriptionURI(String device) {
        return URI.create((String.format("%s:%s", SmartthingsBindingConstants.CONFIG_DESCRIPTION_URI_THING_PREFIX,
                UidUtils.generateThingTypeUID(device))));
    }

    private void generateConfigDescription(String device, List<ChannelGroupType> groupTypes, URI configDescriptionURI) {
        SmartthingsConfigDescriptionProvider lcConfigDescriptionProvider = configDescriptionProvider;
        List<ConfigDescriptionParameter> parms = new ArrayList<>();
        List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        if (lcConfigDescriptionProvider != null) {
            lcConfigDescriptionProvider.addConfigDescription(ConfigDescriptionBuilder.create(configDescriptionURI)
                    .withParameters(parms).withParameterGroups(groups).build());
        }
    }

}
