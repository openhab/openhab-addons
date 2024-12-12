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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.SmartthingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCapabilitie;
import org.openhab.binding.smartthings.internal.dto.SmartthingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartthingsProperty;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeChannelDefinitions;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeChannelDefinitions.SmartthingsBridgeChannelDef;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
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

    private static final Logger logger = LoggerFactory.getLogger(SmartthingsTypeRegistryImpl.class);

    private static final String TEMPLATE_PATH = "json/";
    private @Nullable SmartthingsThingTypeProvider thingTypeProvider;
    private @Nullable SmartthingsChannelTypeProvider channelTypeProvider;
    private @Nullable SmartthingsChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SmartthingsConfigDescriptionProvider configDescriptionProvider;
    private @NonNullByDefault({}) BundleContext bundleContext;

    private Dictionary<String, SmartthingsCapabilitie> capabilitiesDict = new Hashtable<String, SmartthingsCapabilitie>();

    public SmartthingsTypeRegistryImpl() {
        this.bundleContext = FrameworkUtil.getBundle(SmartthingsTypeRegistryImpl.class).getBundleContext();
        logger.info("SmartthingsTypeRegistryImpl()");
    }

    @Override
    public void RegisterCapabilities(SmartthingsCapabilitie capa) {
        if (capa.status.equals("deprecated")) {
            return;
        }
        if (capa.id.indexOf("switch") < 0) {
            // return;
        }

        capabilitiesDict.put(capa.id, capa);
        CreateChannelDefinition(capa);
    }

    public void CreateChannelDefinition(SmartthingsCapabilitie capa) {
        SmartthingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;
        SmartthingsChannelGroupTypeProvider lcChannelGroupTypeProvider = channelGroupTypeProvider;

        for (String key : capa.attributes.keySet()) {
            SmartthingsAttribute attr = capa.attributes.get(key);

            if (key.indexOf("Range") > 0) {
                continue;
            }

            logger.info("capa:" + capa.id + " <> " + key);

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
            String tpSmart = prop.type;
            String channelTp = "NA";

            SmartthingsProperty unit = null;
            if (attr.schema.properties.containsKey("unit")) {
                unit = attr.schema.properties.get("unit");
            }

            SmartthingsBridgeChannelDef channelDef = SmartthingsBridgeChannelDefinitions.getChannelDefs(key);
            if (channelDef != null) {
                channelTp = channelDef.tp;
            } else {
                if (tpSmart.equals("integer")) {
                    channelTp = "Number";
                } else if (tpSmart.equals("string")) {
                    channelTp = "String";
                } else if (tpSmart.equals("object")) {
                    if (prop.title != null && prop.title.equals("JsonObject")) {
                        channelTp = "String";
                    } else {
                        channelTp = "";
                    }
                } else if (tpSmart.equals("array")) {
                    channelTp = "";
                } else if (tpSmart.equals("string")) {
                    channelTp = "String";
                } else if (tpSmart.equals("number")) {
                    channelTp = "Number";
                } else if (tpSmart.equals("boolean")) {
                    channelTp = "String";
                } else {
                    logger.info("need review");
                }
            }

            String label = capa.name;
            String category = capa.id;
            String channelName = (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(key), '-') + "-channel")
                    .toLowerCase();

            Boolean display = false;

            if (display) {
                logger.info("<channel-type id=\"" + channelName + "\">");
                logger.info("  <item-type>" + channelTp + "</item-type>");
                logger.info("  <label>" + label + "</label>");
                logger.info("  <category>" + category + "</category>");

                if (prop.enumeration != null && channelDef == null) {
                    logger.info("  <state>");
                    logger.info("    <options>");
                    for (String opt : prop.enumeration) {
                        String optValue = opt;
                        String optName = StringUtils.capitalize(
                                StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(opt), StringUtils.SPACE));
                        logger.info("      <option value=\"" + optValue + "\">" + optName + "</option>");
                    }
                    logger.info("    </options>");
                    logger.info("  </state>");
                }
                logger.info("</channel-type>");
                logger.info("");
            }

            if (channelTp == null || channelTp.equals("")) {
                continue;
            }

            ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelName);
            ChannelType channelType = null;
            if (lcChannelTypeProvider != null) {
                channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
                if (channelType == null) {
                    channelType = createChannelType(channelName, category, label, channelTp, channelTypeUID);
                    lcChannelTypeProvider.addChannelType(channelType);
                }
            }

        }
    }

    private ChannelType createChannelType(String channelName, String category, String description, String channelTp,
            ChannelTypeUID channelTypeUID) {
        ChannelType channelType;

        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();

        final StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder
                .state(channelTypeUID, channelName, channelTp).withStateDescriptionFragment(stateFragment.build());

        channelType = channelTypeBuilder.isAdvanced(false).withDescription(description).withCategory(category).build();

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

    /**
     * Reads a template from file and returns the content as String.
     *
     * @param templateName name of the template file to read
     * @return The content of the template file
     * @throws IOException thrown when an HTML template could not be read
     */
    private String readTemplate(String templateName) throws IOException {
        final URL index = bundleContext.getBundle().getEntry(templateName);

        if (index == null) {
            throw new FileNotFoundException(
                    String.format("Cannot find '{}' - failed to initialize Smartthings servlet", templateName));
        } else {
            try (InputStream inputStream = index.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    public void Register(String deviceType, SmartthingsDevice device) {
        try {
            generateThingsType(device.deviceId, device.label, deviceType, device);
        } catch (Exception ex) {
            logger.info("wrong:" + ex.toString());
        }
    }

    private void generateThingsType(String deviceId, String deviceLabel, String deviceType, SmartthingsDevice device) {

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

                if (device.components == null || device.components.length == 0) {
                    return;
                }

                for (SmartthingsComponent component : device.components) {
                    String compId = component.id;
                    String compLabel = component.label;

                    if (!compId.equals("main")) {
                        continue;
                    }

                    if (component.capabilities == null || component.capabilities.length == 0) {
                        continue;
                    }

                    for (SmartthingsCapabilitie cap : component.capabilities) {
                        String capId = cap.id;
                        String capVersion = cap.version;

                        capId = capId.replace('.', '_');

                        SmartthingsCapabilitie capa = capabilitiesDict.get(capId);

                        if (capa != null) {
                            logger.info("capa:" + cap.id);
                            for (String key : capa.attributes.keySet()) {
                                if (key.indexOf("range") >= 0) {
                                    continue;
                                }
                                SmartthingsAttribute attr = capa.attributes.get(key);
                                addChannel(deviceType, groupTypes, channelDefinitions, capa, key, attr);
                            }
                        } else {
                            logger.info("capa null");
                        }

                    }
                }

                tt = createThingType(deviceType, deviceId, "", groupTypes);
                lcThingTypeProvider.addThingType(tt);
            }
        }
    }

    private void addChannel(String deviceType, List<ChannelGroupType> groupTypes,
            List<ChannelDefinition> channelDefinitions, SmartthingsCapabilitie capa, String key,
            @Nullable SmartthingsAttribute attr) {

        Map<String, String> props = new Hashtable<String, String>();
        SmartthingsChannelTypeProvider lcChannelTypeProvider = channelTypeProvider;
        SmartthingsChannelGroupTypeProvider lcChannelGroupTypeProvider = channelGroupTypeProvider;

        String channelName = (StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(key), '-') + "-channel")
                .toLowerCase();

        ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(channelName);

        ChannelType channelType = null;
        if (lcChannelTypeProvider != null) {
            channelType = lcChannelTypeProvider.getInternalChannelType(channelTypeUID);
        }

        if (channelType != null) {
            ChannelDefinition channelDef = new ChannelDefinitionBuilder(channelName, channelType.getUID())
                    .withLabel(channelName).withDescription("description").withProperties(props).build();
            channelDefinitions.add(channelDef);
        }
        // generate group
        String groupdId = deviceType + "_default";
        ChannelGroupTypeUID groupTypeUID = UidUtils.generateChannelGroupTypeUID(groupdId);
        ChannelGroupType groupType = null;

        if (lcChannelGroupTypeProvider != null) {
            groupType = lcChannelGroupTypeProvider.getInternalChannelGroupType(groupTypeUID);

            if (groupType == null) {
                String groupLabel = groupdId + "Label";
                groupType = ChannelGroupTypeBuilder.instance(groupTypeUID, groupLabel)
                        .withChannelDefinitions(channelDefinitions).withCategory("").withDescription(groupdId + "Desc")
                        .build();
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
}
