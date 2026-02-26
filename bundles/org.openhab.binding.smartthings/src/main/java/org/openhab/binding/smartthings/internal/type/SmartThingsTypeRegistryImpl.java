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
package org.openhab.binding.smartthings.internal.type;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.dto.SmartThingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.dto.SmartThingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartThingsProperty;
import org.openhab.binding.smartthings.internal.dto.SmartThingsSchema;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeChannelDefinitions;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeChannelDefinitions.ChannelProperty;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsCloudBridgeHandler;
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
public class SmartThingsTypeRegistryImpl implements SmartThingsTypeRegistry {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsTypeRegistryImpl.class);

    private HashMap<String, SemanticTag> sementicTags = new HashMap<String, SemanticTag>();
    private @Nullable SmartThingsThingTypeProvider thingTypeProvider;
    private @Nullable SmartThingsChannelTypeProvider channelTypeProvider;
    private @Nullable SmartThingsChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SmartThingsConfigDescriptionProvider configDescriptionProvider;
    private @Nullable SmartThingsCloudBridgeHandler bridgeHandler;
    private Gson gson = new Gson();

    private HashMap<String, SmartThingsCapability> capabilitiesDict = new HashMap<String, SmartThingsCapability>();

    public SmartThingsTypeRegistryImpl() {
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
    public void registerCapability(SmartThingsCapability capa) {
        capabilitiesDict.put(capa.id, capa);
        createChannelTypes(capa);
    }

    @Override
    @Nullable
    public SmartThingsCapability getCapability(String capaKey) {
        if (capabilitiesDict.containsKey(capaKey)) {
            return capabilitiesDict.get(capaKey);
        }

        return null;
    }

    @Override
    public void setCloudBridgeHandler(SmartThingsCloudBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    public String getChannelType(String smartThingsType, SmartThingsCapability capa,
            @Nullable ChannelProperty channelProp) {
        String channelType = SmartThingsBridgeChannelDefinitions.getChannelType(smartThingsType);
        String unit = null;
        String result = "";

        if (channelProp != null) {
            unit = channelProp.getUoM();

            if (channelProp.getChannelType() != null) {
                channelType = channelProp.getChannelType();
            }
        }

        if (channelType == null) {
            logger.debug("need review");
            return result;
        }

        result = channelType;

        if (unit != null) {
            result = result + ":" + unit;
        }

        return result;
    }

    public void createChannelTypes(SmartThingsCapability capa) {
        logger.trace("createChannelTypes: capa:{} / {}", capa.id, capa.version);

        for (String key : capa.attributes.keySet()) {
            SmartThingsAttribute attr = capa.attributes.get(key);

            logger.trace("createChannelTypes: key {}", key);

            try {
                if (key.indexOf("Range") > 0) {
                    continue;
                }

                if (attr == null) {
                    continue;
                }
                if (attr.schema == null) {
                    logger.debug("no schema");
                }
                if (attr.schema.properties == null) {
                    logger.debug("no properties");
                }
                if (!attr.schema.properties.containsKey("value")) {
                    logger.debug("no value");
                }

                SmartThingsProperty prop = attr.schema.properties.get("value");

                if (prop != null) {
                    if (prop.type.equals("object")) {
                        if (prop.properties != null) {
                            for (Map.Entry<String, SmartThingsProperty> subEntry : prop.properties.entrySet()) {
                                generateChannelTypeForProp(capa, key, subEntry.getKey(), attr, subEntry.getValue());
                            }
                        }
                    } else {
                        if (prop.oneOf != null) {
                            for (SmartThingsProperty subProp : prop.oneOf) {
                                generateChannelTypeForProp(capa, key, "", attr, subProp);
                            }
                        } else {
                            generateChannelTypeForProp(capa, key, "", attr, prop);
                        }
                    }
                }
            } catch (Exception ex) {
                logger.warn("Unable to register ChannelTypes for capability '{}'", key, ex);
            }
        }
    }

    private String getChannelTypeName(SmartThingsCapability capa, String key, String subKey) {
        if ("".equals(subKey)) {
            return capa.id.replace(".", "_") + "_"
                    + (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(key), '-')).toLowerCase(Locale.ROOT);

        } else {
            return capa.id.replace(".", "_") + "_"
                    + (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(key), '-')).toLowerCase(Locale.ROOT)
                    + "_" + (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(subKey), '-'))
                            .toLowerCase(Locale.ROOT);
        }
    }

    private void generateChannelTypeForProp(SmartThingsCapability capa, String key, String subKey,
            SmartThingsAttribute attr, SmartThingsProperty prop) {
        SmartThingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;
        String smartThingsType = prop.type;
        String channelTypeId = "NA";

        SmartThingsProperty unit = null;
        if (attr.schema.properties.containsKey("unit")) {
            unit = attr.schema.properties.get("unit");
        }

        ChannelProperty channelProp = SmartThingsBridgeChannelDefinitions.getChannelProperty(capa.id + "#" + key);

        channelTypeId = getChannelType(smartThingsType, capa, channelProp);

        if ("".equals(channelTypeId)) {
            logger.info("need review");
        }
        String label = capa.name;

        String channelTypeName = getChannelTypeName(capa, key, subKey);

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

        if ("".equals(channelTypeId)) {
            return;
        }

        logger.trace("createChannelTypes: channelTypeName {}", channelTypeName);

        ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelTypeName);
        ChannelType channelType = null;
        if (lcChannelTypeProvider != null) {
            channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
            if (channelType == null) {
                channelType = createChannelType(capa, unit, channelTypeName, "", label, channelTypeId, channelTypeUID,
                        options, channelProp);
                lcChannelTypeProvider.addChannelType(channelType);
            }
        }
    }

    private ChannelType createChannelType(SmartThingsCapability capa, @Nullable SmartThingsProperty unit,
            String channelName, String category, String description, String openhabChannelType,
            ChannelTypeUID channelTypeUID, List<StateOption> options, @Nullable ChannelProperty channelProperty) {
        ChannelType channelType;

        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();

        if (!options.isEmpty()) {
            stateFragment = stateFragment.withOptions(options);
        }

        if (unit != null) {
            stateFragment = stateFragment.withPattern("%d " + unit.defaultObj);
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
    protected void setThingTypeProvider(SmartThingsThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = thingTypeProvider;
    }

    protected void unsetThingTypeProvider(SmartThingsThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = null;
    }

    @Reference
    protected void setChannelTypeProvider(SmartThingsChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    protected void unsetChannelTypeProvider(SmartThingsChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

    //
    @Reference
    protected void setChannelGroupTypeProvider(SmartThingsChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = channelGroupTypeProvider;
    }

    protected void unsetChannelGroupTypeProvider(SmartThingsChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = null;
    }

    @Reference
    protected void setConfigDescriptionProvider(SmartThingsConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = configDescriptionProvider;
    }

    protected void unsetConfigDescriptionProvider(SmartThingsConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = null;
    }

    @Override
    public void register(String deviceType, SmartThingsDevice device) {
        try {
            logger.trace("registerDeviceType: {} {}", deviceType, gson.toJson(device));
            generateThingsType(device.deviceId, device.label, deviceType, device);
        } catch (Exception ex) {
            logger.info("wrong: {}", ex.toString());
        }
    }

    private void generateThingsType(String deviceId, String deviceLabel, String deviceType, SmartThingsDevice device) {
        SmartThingsThingTypeProvider lcThingTypeProvider = thingTypeProvider;

        logger.trace("generateThingsType: {} {}", deviceType, deviceId);

        if (lcThingTypeProvider != null) {
            ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(deviceType);
            ThingType tt = null;

            tt = lcThingTypeProvider.getInternalThingType(thingTypeUID);

            if (tt == null) {
                List<ChannelGroupType> groupTypes = new ArrayList<>();

                if (device.components == null || device.components.length == 0) {
                    return;
                }

                for (SmartThingsComponent component : device.components) {
                    if (component.capabilities == null || component.capabilities.length == 0) {
                        continue;
                    }

                    for (SmartThingsCapability cap : component.capabilities) {
                        String capId = cap.id;

                        capId = capId.replace('.', '_');

                        SmartThingsCapability capa = null;

                        if (capabilitiesDict.containsKey(capId)) {
                            capa = capabilitiesDict.get(capId);
                        } else {
                            SmartThingsBridgeHandler bridgeHandler = this.bridgeHandler;
                            if (bridgeHandler != null) {
                                SmartThingsApi api = bridgeHandler.getSmartThingsApi();
                                try {
                                    logger.trace("Need capability not registered in cache: id:{} version:{}", cap.id,
                                            cap.version);
                                    capa = api.getCapability(cap.id, cap.version, null);

                                    logger.trace("capa is: {}", gson.toJson(capa));
                                    registerCapability(capa);
                                } catch (SmartThingsException ex) {
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

    public static String getChannelName(String propKey) {
        return (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(propKey), '-')).toLowerCase(Locale.ROOT);
    }

    private void addChannels(String deviceType, List<ChannelGroupType> groupTypes, SmartThingsComponent component,
            SmartThingsCapability capa) {
        List<ChannelDefinition> channelDefinitions = new ArrayList<>();
        SmartThingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;
        SmartThingsChannelGroupTypeProvider lcChannelGroupTypeProvider = channelGroupTypeProvider;

        String namespace = "";
        String capaKey = capa.id;
        if (capa.id.contains(".")) {
            String[] idComponents = capa.id.split("\\.");
            namespace = idComponents[0];
            capaKey = idComponents[1];
        }

        String componentId = UidUtils.sanetizeId(component.id);

        for (String attrKey : capa.attributes.keySet()) {
            if (attrKey.indexOf("Range") >= 0) {
                continue;
            }

            SmartThingsAttribute attr = capa.attributes.get(attrKey);
            SmartThingsSchema schema = attr.schema;
            Hashtable<String, SmartThingsProperty> propsMap = schema.properties;
            SmartThingsProperty prop = propsMap.get("value");
            String propType = prop.type;

            if (propType.equals("object")) {
                Hashtable<String, SmartThingsProperty> subPropList = prop.properties;

                if (subPropList != null) {
                    for (String subPropKey : subPropList.keySet()) {

                        SmartThingsProperty subProp = subPropList.get(subPropKey);
                        logger.info("");

                        Map<String, String> props = new Hashtable<String, String>();

                        String channelName = getChannelName(subPropKey);
                        String channelTypeName = getChannelTypeName(capa, attrKey, subPropKey);

                        final String fChannelName = channelName;

                        logger.trace("addChannels: channelTypeName: {}", channelTypeName);
                        ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelTypeName);

                        ChannelType channelType = null;
                        if (lcChannelTypeProvider != null) {
                            channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
                        }

                        if (channelType == null) {
                            logger.warn("Can't find channelType for {}", channelTypeUID);
                        }

                        props.put(SmartThingsBindingConstants.COMPONENT, componentId);
                        props.put(SmartThingsBindingConstants.CAPABILITY, capa.id);
                        props.put(SmartThingsBindingConstants.ATTRIBUTE, attrKey);

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
                }

            } else {

                Map<String, String> props = new Hashtable<String, String>();

                String channelName = getChannelName(attrKey);
                String channelTypeName = capa.id.replace(".", "_") + "_" + channelName;

                final String fChannelName = channelName;

                logger.trace("addChannels: channelTypeName: {}", channelTypeName);
                ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelTypeName);

                ChannelType channelType = null;
                if (lcChannelTypeProvider != null) {
                    channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
                }

                if (channelType == null) {
                    logger.warn("Can't find channelType for {}", channelTypeUID);
                }

                props.put(SmartThingsBindingConstants.COMPONENT, componentId);
                props.put(SmartThingsBindingConstants.CAPABILITY, capa.id);
                props.put(SmartThingsBindingConstants.ATTRIBUTE, attrKey);

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
        }

        // generate group
        String groupId = deviceType + "_" + componentId + "_";

        if (!"".equals(namespace)) {
            groupId = groupId + namespace + "_";
        }
        groupId = groupId + capaKey;

        logger.trace("addChannels: groupId:{}", groupId);
        ChannelGroupTypeUID groupTypeUID = UidUtils.generateChannelGroupTypeUID(groupId);
        ChannelGroupType groupType = null;

        if (lcChannelGroupTypeProvider != null) {
            groupType = lcChannelGroupTypeProvider.getInternalChannelGroupType(groupTypeUID);

            if (groupType == null) {
                String groupLabel = StringUtils.capitalize(componentId + " " + namespace + " " + capaKey);
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
        SmartThingsConfigDescriptionProvider lcConfigDescriptionProvider = configDescriptionProvider;
        String name = device;

        logger.trace("createThingType: device:{} {}", device, label);

        List<String> supportedBridgeTypeUids = new ArrayList<>();
        supportedBridgeTypeUids.add(SmartThingsBindingConstants.THING_TYPE_SMARTTHINGSCLOUD.toString());

        logger.trace("GenerateThingTypeUID: device:{}", device);
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
        builder = builder.withCategory(SmartThingsBindingConstants.CATEGORY_THING_SMARTTHINGS);
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
        logger.trace("getConfigDescriptionURI: device: {}", device);
        ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(device);

        return URI.create((String.format("%s:%s", SmartThingsBindingConstants.CONFIG_DESCRIPTION_URI_THING_PREFIX,
                thingTypeUID)));
    }

    private void generateConfigDescription(String device, List<ChannelGroupType> groupTypes, URI configDescriptionURI) {
        SmartThingsConfigDescriptionProvider lcConfigDescriptionProvider = configDescriptionProvider;
        List<ConfigDescriptionParameter> parms = new ArrayList<>();
        List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        if (lcConfigDescriptionProvider != null) {
            lcConfigDescriptionProvider.addConfigDescription(ConfigDescriptionBuilder.create(configDescriptionURI)
                    .withParameters(parms).withParameterGroups(groups).build());
        }
    }

    @Override
    @Nullable
    public SmartThingsChannelTypeProvider getSmartThingsChannelTypeProvider() {
        return this.channelTypeProvider;
    }
}
