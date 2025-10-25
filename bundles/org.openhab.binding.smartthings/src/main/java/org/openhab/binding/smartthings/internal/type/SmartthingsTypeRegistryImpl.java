/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.dto.SmartthingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCapability;
import org.openhab.binding.smartthings.internal.dto.SmartthingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartthingsProperty;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeChannelDefinitions;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeChannelDefinitions.ChannelProperty;
import org.openhab.binding.smartthings.internal.handler.SmartthingsCloudBridgeHandler;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterGroup;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Equipment;
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
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class SmartthingsTypeRegistryImpl implements SmartthingsTypeRegistry {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsTypeRegistryImpl.class);

    private Hashtable<String, SemanticTag> sementicTags = new Hashtable<String, SemanticTag>();
    private @Nullable SmartthingsThingTypeProvider thingTypeProvider;
    private @Nullable SmartthingsChannelTypeProvider channelTypeProvider;
    private @Nullable SmartthingsChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SmartthingsConfigDescriptionProvider configDescriptionProvider;
    private @Nullable SmartthingsCloudBridgeHandler bridgeHandler;
    private Gson gson = new Gson();

    private Hashtable<String, SmartthingsCapability> capabilitiesDict = new Hashtable<String, SmartthingsCapability>();

    public SmartthingsTypeRegistryImpl() {
        initSemanticTags();
    }

    public void initSemanticTags() {
        sementicTags.put("light", Equipment.LIGHTBULB);
        sementicTags.put("motionsensor", Equipment.MOTION_DETECTOR);
        sementicTags.put("oven", Equipment.OVEN);
        sementicTags.put("dishwasher", Equipment.DISHWASHER);
        sementicTags.put("smartplug", Equipment.POWER_OUTLET);
        sementicTags.put("leaksensor", Equipment.LEAK_SENSOR);

        // @todo: review this one
        sementicTags.put("hub", Equipment.NETWORK_APPLIANCE);
        sementicTags.put("networking", Equipment.NETWORK_APPLIANCE);
    }

    @Override
    public void registerCapability(SmartthingsCapability capa) {
        capabilitiesDict.put(capa.id, capa);
        createChannelTypes(capa);
    }

    @Override
    @Nullable
    public SmartthingsCapability getCapability(String capaKey) {
        if (capabilitiesDict.containsKey(capaKey)) {
            return capabilitiesDict.get(capaKey);
        }

        return null;
    }

    @Override
    public void setCloudBridgeHandler(SmartthingsCloudBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    public String getOpenhabChannelType(String smartThingsType, SmartthingsCapability capa, String key,
            @Nullable ChannelProperty channelProp) {
        String openhabChannelType = SmartthingsBridgeChannelDefinitions.getChannelType(smartThingsType);
        String openhabUoM = null;
        String result = "";

        if (channelProp != null) {
            openhabUoM = channelProp.getUoM();

            if (channelProp.getOpenhabChannelType() != null) {
                openhabChannelType = channelProp.getOpenhabChannelType();
            }
        }

        if (openhabChannelType == null) {
            logger.info("need review");
            return result;
        }

        result = openhabChannelType;

        if (openhabUoM != null) {
            result = result + ":" + openhabUoM;
        }

        return result;
    }

    public void createChannelTypes(SmartthingsCapability capa) {
        SmartthingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;

        for (String key : capa.attributes.keySet()) {
            SmartthingsAttribute attr = capa.attributes.get(key);

            if (key.indexOf("Range") > 0) {
                continue;
            }

            // logger.info("capa: {} <> {}", capa.id, key);

            if (attr == null) {
                continue;
            }
            if (attr.schema == null) {
                logger.info("no schema");
            }
            if (attr.schema.properties == null) {
                logger.info("no properties");
            }
            if (!attr.schema.properties.containsKey("value")) {
                logger.info("no value");
            }

            SmartthingsProperty prop = attr.schema.properties.get("value");

            if (prop != null) {
                String smartThingsType = prop.type;
                String openHabChannelType = "NA";

                SmartthingsProperty unit = null;
                if (attr.schema.properties.containsKey("unit")) {
                    unit = attr.schema.properties.get("unit");
                }

                ChannelProperty channelProp = SmartthingsBridgeChannelDefinitions
                        .getChannelProperty(capa.id + "#" + key);

                openHabChannelType = getOpenhabChannelType(smartThingsType, capa, key, channelProp);

                if ("".equals(openHabChannelType)) {
                    logger.info("need review");
                }
                String label = capa.name;

                String channelTypeName = capa.id.replace(".", "_") + "_"
                        + (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(key), '-')).toLowerCase();

                List<StateOption> options = new ArrayList<StateOption>();

                if (prop.enumeration != null) {
                    for (String opt : prop.enumeration) {
                        String optValue = opt;
                        String optName = StringUtils.capitalize(
                                StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(opt), StringUtils.SPACE));

                        StateOption option = new StateOption(optValue, optName);
                        options.add(option);
                    }
                }

                if ("".equals(openHabChannelType)) {
                    continue;
                }

                ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelTypeName);
                ChannelType channelType = null;
                if (lcChannelTypeProvider != null) {
                    channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
                    if (channelType == null) {
                        channelType = createChannelType(capa, unit, channelTypeName, "", label, openHabChannelType,
                                channelTypeUID, options, channelProp);
                        lcChannelTypeProvider.addChannelType(channelType);
                    }
                }
            }

        }
    }

    private ChannelType createChannelType(SmartthingsCapability capa, @Nullable SmartthingsProperty unit,
            String channelName, String category, String description, String openhabChannelType,
            ChannelTypeUID channelTypeUID, List<StateOption> options, @Nullable ChannelProperty channelProperty) {
        ChannelType channelType;

        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();

        if (!options.isEmpty()) {
            stateFragment = stateFragment.withOptions(options);
        }

        if (unit != null) {
            stateFragment = stateFragment.withPattern("%d " + unit.defaultUnit);
            if (unit.minimum != 0) {
                stateFragment = stateFragment.withMinimum(new BigDecimal(unit.minimum));
            }
            if (unit.maximum != 0) {
                stateFragment = stateFragment.withMaximum(new BigDecimal(unit.maximum));
            }
        }

        if (channelName.contains("hue")) {
            stateFragment = stateFragment.withMaximum(new BigDecimal(360));
        }

        StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder
                .state(channelTypeUID, channelName, openhabChannelType)
                .withStateDescriptionFragment(stateFragment.build());

        Boolean isAdvanced = false;
        if (capa.id.contains("ocf")) {
            isAdvanced = true;
        }
        channelTypeBuilder = channelTypeBuilder.isAdvanced(isAdvanced);
        channelTypeBuilder = channelTypeBuilder.withDescription(description);
        channelTypeBuilder = channelTypeBuilder.withCategory(category);

        if (channelProperty != null) {
            if (channelProperty.getSemanticPoint() != null) {
                channelTypeBuilder.withTags(channelProperty.getSemanticPoint());
            }
            if (channelProperty.getSemanticProperty() != null) {
                channelTypeBuilder.withTags(channelProperty.getSemanticProperty());
            }
        }

        channelType = channelTypeBuilder.build();

        return channelType;
    }

    @Override
    public void initialize() {
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

    @Override
    public void register(String deviceType, SmartthingsDevice device) {
        try {
            logger.trace("registerDeviceType: {} {}", deviceType, gson.toJson(device));
            generateThingsType(device.deviceId, device.label, deviceType, device);
        } catch (Exception ex) {
            logger.info("wrong: {}", ex.toString());
        }
    }

    private void generateThingsType(String deviceId, String deviceLabel, String deviceType, SmartthingsDevice device) {
        SmartthingsThingTypeProvider lcThingTypeProvider = thingTypeProvider;

        if (lcThingTypeProvider != null) {
            ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(deviceType);
            ThingType tt = null;

            tt = lcThingTypeProvider.getInternalThingType(thingTypeUID);

            if (tt == null) {
                List<ChannelGroupType> groupTypes = new ArrayList<>();

                if (device.components == null || device.components.length == 0) {
                    return;
                }

                for (SmartthingsComponent component : device.components) {
                    if (component.capabilities == null || component.capabilities.length == 0) {
                        continue;
                    }

                    for (SmartthingsCapability cap : component.capabilities) {
                        String capId = cap.id;

                        capId = capId.replace('.', '_');

                        SmartthingsCapability capa = null;

                        if (capabilitiesDict.containsKey(capId)) {
                            capa = capabilitiesDict.get(capId);
                        } else {
                            if (bridgeHandler != null) {
                                SmartthingsApi api = bridgeHandler.getSmartthingsApi();
                                try {
                                    logger.trace("Need capability not registered in cache: id:{} version:{}", cap.id,
                                            cap.version);
                                    capa = api.getCapability(cap.id, cap.version, null);
                                    if (capa != null) {
                                        logger.trace("capa is: {}", gson.toJson(capa));
                                    }
                                    registerCapability(capa);
                                } catch (SmartthingsException ex) {
                                    logger.error("Exception during capa reading:{}", ex.toString(), ex);

                                }
                            }
                        }

                        logger.trace("capa: {}", cap.id);
                        if (capa != null) {
                            addChannels(deviceType, groupTypes, component, capa);
                        }
                    }
                }

                tt = createThingType(deviceType, deviceId, groupTypes);
                lcThingTypeProvider.addThingType(tt);
            }
        }
    }

    private void addChannels(String deviceType, List<ChannelGroupType> groupTypes, SmartthingsComponent component,
            SmartthingsCapability capa) {
        List<ChannelDefinition> channelDefinitions = new ArrayList<>();
        SmartthingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;
        SmartthingsChannelGroupTypeProvider lcChannelGroupTypeProvider = channelGroupTypeProvider;

        String namespace = "";
        String capaKey = capa.id;
        if (capa.id.contains(".")) {
            String[] idComponents = capa.id.split("\\.");
            namespace = idComponents[0];
            capaKey = idComponents[1];
        }

        for (String attrKey : capa.attributes.keySet()) {
            if (attrKey.indexOf("Range") >= 0) {
                continue;
            }
            Map<String, String> props = new Hashtable<String, String>();

            String channelName = (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(attrKey), '-'))
                    .toLowerCase();
            String channelTypeName = capa.id.replace(".", "_") + "_" + channelName;

            final String fChannelName = channelName;

            ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelTypeName);

            ChannelType channelType = null;
            if (lcChannelTypeProvider != null) {
                channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
            }

            if (channelType == null) {
                logger.warn("Can't find channelType for {}", channelTypeUID);
            }

            props.put(SmartthingsBindingConstants.COMPONENT, component.id);
            props.put(SmartthingsBindingConstants.CAPABILITY, capa.id);
            props.put(SmartthingsBindingConstants.ATTRIBUTE, attrKey);

            ChannelDefinition channelDef = null;

            // capa.commands
            if (channelType != null) {
                channelDef = new ChannelDefinitionBuilder(channelName, channelType.getUID())
                        .withLabel(StringUtils.capitalize(channelName)).withProperties(props).build();

                Optional<ChannelDefinition> previous = channelDefinitions.stream()
                        .filter(x -> x.getId().equals(fChannelName)).findFirst();
                if (previous.isEmpty()) {
                    channelDefinitions.add(channelDef);
                }
            }
        }

        // generate group
        String groupId = deviceType + "_" + component.id + "_";

        if (!"".equals(namespace)) {
            groupId = groupId + namespace + "_";
        }
        groupId = groupId + capaKey;

        ChannelGroupTypeUID groupTypeUID = UidUtils.generateChannelGroupTypeUID(groupId);
        ChannelGroupType groupType = null;

        if (lcChannelGroupTypeProvider != null) {
            groupType = lcChannelGroupTypeProvider.getInternalChannelGroupType(groupTypeUID);

            if (groupType == null) {
                String groupLabel = StringUtils.capitalize(component.id + " " + namespace + " " + capaKey);
                groupType = ChannelGroupTypeBuilder.instance(groupTypeUID, groupLabel)
                        .withChannelDefinitions(channelDefinitions).withCategory("").build();
                lcChannelGroupTypeProvider.addChannelGroupType(groupType);
                groupTypes.add(groupType);
            }
        }
    }

    /**
     * Creates the ThingType for the given device.
     */
    private ThingType createThingType(String device, String label, List<ChannelGroupType> groupTypes) {
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

        ThingTypeBuilder builder = ThingTypeBuilder.instance(thingTypeUID, name);

        builder = builder.withSupportedBridgeTypeUIDs(supportedBridgeTypeUids);
        builder = builder.withLabel(label);
        builder = builder.withRepresentationProperty(Thing.PROPERTY_MODEL_ID);
        builder = builder.withConfigDescriptionURI(configDescriptionURI);
        builder = builder.withCategory(SmartthingsBindingConstants.CATEGORY_THING_SMARTTHINGS);
        builder = builder.withChannelGroupDefinitions(groupDefinitions);
        builder = builder.withProperties(properties);

        SemanticTag semanticTag = getThingSemanticType(device);
        if (semanticTag != null) {
            builder = builder.withSemanticEquipmentTag(semanticTag);
        }

        return builder.build();
    }

    public @Nullable SemanticTag getThingSemanticType(String deviceType) {
        if (sementicTags.containsKey(deviceType)) {
            return sementicTags.get(deviceType);
        } else {
            logger.info("@need review, missing semanticTag for deviceType: {}", deviceType);
        }
        return null;
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

    @Override
    @Nullable
    public SmartthingsChannelTypeProvider getSmartthingsChannelTypeProvider() {
        return this.channelTypeProvider;
    }
}
