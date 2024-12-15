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
package org.openhab.binding.smartthings.internal.type;

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
import org.openhab.binding.smartthings.internal.dto.SmartthingsCapabilitie;
import org.openhab.binding.smartthings.internal.dto.SmartthingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartthingsProperty;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeChannelDefinitions;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeChannelDefinitions.SmartthingsBridgeChannelDef;
import org.openhab.binding.smartthings.internal.handler.SmartthingsCloudBridgeHandler;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class SmartthingsTypeRegistryImpl implements SmartthingsTypeRegistry {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsTypeRegistryImpl.class);

    private @Nullable SmartthingsThingTypeProvider thingTypeProvider;
    private @Nullable SmartthingsChannelTypeProvider channelTypeProvider;
    private @Nullable SmartthingsChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SmartthingsConfigDescriptionProvider configDescriptionProvider;
    private @Nullable SmartthingsCloudBridgeHandler bridgeHandler;

    private Hashtable<String, SmartthingsCapabilitie> capabilitiesDict = new Hashtable<String, SmartthingsCapabilitie>();

    public SmartthingsTypeRegistryImpl() {
    }

    @Override
    public void registerCapabilities(SmartthingsCapabilitie capa) {
        capabilitiesDict.put(capa.id, capa);
        createChannelDefinition(capa);
    }

    @Override
    @Nullable
    public SmartthingsCapabilitie getCapabilities(String capaKey) {
        if (capabilitiesDict.containsKey(capaKey)) {
            return capabilitiesDict.get(capaKey);
        }

        return null;
    }

    @Override
    public void setCloudBridgeHandler(SmartthingsCloudBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    public void createChannelDefinition(SmartthingsCapabilitie capa) {
        SmartthingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;

        for (String key : capa.attributes.keySet()) {
            SmartthingsAttribute attr = capa.attributes.get(key);

            if (key.indexOf("Range") > 0) {
                continue;
            }

            logger.info("capa: {} <> {}", capa.id, key);

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
                String tpSmart = prop.type;
                String channelTp = "NA";

                /*
                 * SmartthingsProperty unit = null;
                 * if (attr.schema.properties.containsKey("unit")) {
                 * unit = attr.schema.properties.get("unit");
                 * }
                 */

                SmartthingsBridgeChannelDef channelDef = SmartthingsBridgeChannelDefinitions.getChannelDefs(key);
                if (channelDef != null) {
                    channelTp = channelDef.tp;
                } else {
                    if ("integer".equals(tpSmart)) {
                        channelTp = "Number";
                    } else if ("string".equals(tpSmart)) {
                        channelTp = "String";
                    } else if ("object".equals(tpSmart)) {
                        if (prop.title != null && "JsonObject".equals(prop.title)) {
                            channelTp = "String";
                        } else {
                            channelTp = "";
                        }
                    } else if ("array".equals(tpSmart)) {
                        channelTp = "";
                    } else if ("string".equals(tpSmart)) {
                        channelTp = "String";
                    } else if ("number".equals(tpSmart)) {
                        channelTp = "Number";
                    } else if ("boolean".equals(tpSmart)) {
                        channelTp = "Contact";
                    } else {
                        logger.info("need review");
                    }
                }

                String label = capa.name;
                String category = capa.id;
                String channelName = (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(key), '-'))
                        .toLowerCase();

                Boolean display = false;

                if (display) {
                    logger.info("<channel-type id=\"{}\">", channelName);
                    logger.info("  <item-type>{}</item-type>", channelTp);
                    logger.info("  <label>{}</label>", label);
                    logger.info("  <category>{}</category>", category);

                    if (prop.enumeration != null && channelDef == null) {
                        logger.info("  <state>");
                        logger.info("    <options>");
                        for (String opt : prop.enumeration) {
                            String optValue = opt;
                            String optName = StringUtils.capitalize(StringUtils
                                    .join(StringUtils.splitByCharacterTypeCamelCase(opt), StringUtils.SPACE));
                            logger.info("      <option value=\"{}\">{}</option>", optValue, optName);
                        }
                        logger.info("    </options>");
                        logger.info("  </state>");
                    }
                    logger.info("</channel-type>");
                    logger.info("");
                }

                if ("".equals(channelTp)) {
                    continue;
                }

                ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelName);
                ChannelType channelType = null;
                if (lcChannelTypeProvider != null) {
                    channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
                    if (channelType == null) {
                        channelType = createChannelType(capa, channelName, category, label, channelTp, channelTypeUID);
                        lcChannelTypeProvider.addChannelType(channelType);
                    }
                }
            }

        }
    }

    private ChannelType createChannelType(SmartthingsCapabilitie capa, String channelName, String category,
            String description, String channelTp, ChannelTypeUID channelTypeUID) {
        ChannelType channelType;

        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();

        final StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder
                .state(channelTypeUID, channelName, channelTp).withStateDescriptionFragment(stateFragment.build());

        Boolean isAdvanced = false;
        if (capa.id.contains("ocf")) {
            isAdvanced = true;
        }

        channelType = channelTypeBuilder.isAdvanced(isAdvanced).withDescription(description).withCategory(category)
                .build();

        return channelType;
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub
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
                List<ChannelDefinition> channelDefinitions = new ArrayList<>();

                if (device.components == null || device.components.length == 0) {
                    return;
                }

                for (SmartthingsComponent component : device.components) {
                    String compId = component.id;

                    if (component.capabilities == null || component.capabilities.length == 0) {
                        continue;
                    }

                    for (SmartthingsCapabilitie cap : component.capabilities) {
                        String capId = cap.id;

                        capId = capId.replace('.', '_');

                        SmartthingsCapabilitie capa = null;

                        if (capabilitiesDict.containsKey(capId)) {
                            capa = capabilitiesDict.get(capId);
                        } else {
                            SmartthingsApi api = this.bridgeHandler.getSmartthingsApi();
                            try {
                                capa = api.getCapabilitie(cap.id, "1");
                                registerCapabilities(capa);
                            } catch (SmartthingsException ex) {

                            }
                        }

                        logger.info("capa: {}", cap.id);
                        for (String key : capa.attributes.keySet()) {
                            if (key.indexOf("Range") >= 0) {
                                continue;
                            }
                            SmartthingsAttribute attr = capa.attributes.get(key);
                            addChannel(deviceType, groupTypes, channelDefinitions, component, capa, key, attr);
                        }
                    }
                }

                tt = createThingType(deviceType, deviceId, "", groupTypes);
                lcThingTypeProvider.addThingType(tt);
            }
        }
    }

    private void addChannel(String deviceType, List<ChannelGroupType> groupTypes,
            List<ChannelDefinition> channelDefinitions, SmartthingsComponent component, SmartthingsCapabilitie capa,
            String attrKey, @Nullable SmartthingsAttribute attr) {
        Map<String, String> props = new Hashtable<String, String>();
        SmartthingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;
        SmartthingsChannelGroupTypeProvider lcChannelGroupTypeProvider = channelGroupTypeProvider;

        String channelName = (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(attrKey), '-')).toLowerCase();

        ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelName);

        ChannelType channelType = null;
        if (lcChannelTypeProvider != null) {
            channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
        }

        props.put("component", component.id);
        props.put("capability", capa.id);
        props.put("attribute", attrKey);

        // capa.commands
        if (channelType != null) {
            ChannelDefinition channelDef = new ChannelDefinitionBuilder(channelName, channelType.getUID())
                    .withLabel(StringUtils.capitalize(channelName)).withDescription("description").withProperties(props)
                    .build();

            Optional<ChannelDefinition> previous = channelDefinitions.stream()
                    .filter(x -> x.getId().equals(channelName)).findFirst();
            if (previous.isEmpty()) {
                channelDefinitions.add(channelDef);
            }
        }
        // generate group
        String groupdId = deviceType + "_" + component.id;
        ChannelGroupTypeUID groupTypeUID = UidUtils.generateChannelGroupTypeUID(groupdId);
        ChannelGroupType groupType = null;

        if (lcChannelGroupTypeProvider != null) {
            groupType = lcChannelGroupTypeProvider.getInternalChannelGroupType(groupTypeUID);

            if (groupType == null) {
                String groupLabel = StringUtils.capitalize(deviceType + " " + component.id);
                groupType = ChannelGroupTypeBuilder.instance(groupTypeUID, groupLabel)
                        .withChannelDefinitions(channelDefinitions).withCategory("")
                        .withDescription(StringUtils.capitalize(deviceType)).build();
                lcChannelGroupTypeProvider.addChannelGroupType(groupType);
                groupTypes.add(groupType);
            }
        }
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

    @Override
    @Nullable
    public SmartthingsChannelTypeProvider getSmartthingsChannelTypeProvider() {
        return this.channelTypeProvider;
    }
}
