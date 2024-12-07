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
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class SmartthingsTypeRegistryImpl implements SmartthingsTypeRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SmartthingsTypeRegistryImpl.class);

    private final @NotNull Gson gson;

    private static final String TEMPLATE_PATH = "json/";
    private @Nullable SmartthingsThingTypeProvider thingTypeProvider;
    private @Nullable SmartthingsChannelTypeProvider channelTypeProvider;
    private @Nullable SmartthingsChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SmartthingsConfigDescriptionProvider configDescriptionProvider;
    private @NonNullByDefault({}) BundleContext bundleContext;

    private Dictionary<String, SmartthingsJSonCapabilities> capabilitiesDict = new Hashtable<String, SmartthingsJSonCapabilities>();

    public SmartthingsTypeRegistryImpl() {
        this.bundleContext = FrameworkUtil.getBundle(SmartthingsTypeRegistryImpl.class).getBundleContext();
        logger.info("SmartthingsTypeRegistryImpl()");
        GsonBuilder builder = new GsonBuilder();

        Type smartthingsAttributesListType = new TypeToken<List<SmartthingsJSonAttributes>>() {
        }.getType();

        Type smartthingsCommandsListType = new TypeToken<List<SmartthingsJSonCommands>>() {
        }.getType();

        Type smartthingsPropertiesListType = new TypeToken<List<SmartthingsJSonProperties>>() {
        }.getType();

        gson = builder.setPrettyPrinting()
                .registerTypeAdapter(smartthingsAttributesListType, new SmartthingsAttributesDeserializer())
                .registerTypeAdapter(smartthingsCommandsListType, new SmartthingsCommandsDeserializer())
                .registerTypeAdapter(smartthingsPropertiesListType, new SmartthingPropertiesDeserializer()).create();

        loadCapabilitiesDefs();
        logger.info("pa");
    }

    class SmartthingsAttributesDeserializer implements JsonDeserializer<List<SmartthingsJSonAttributes>> {
        @Override
        public @Nullable List<SmartthingsJSonAttributes> deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            // TODO Auto-generated method stub

            JsonObject obj = (JsonObject) json;
            Set<String> keys = obj.keySet();
            List<SmartthingsJSonAttributes> result = new ArrayList<SmartthingsJSonAttributes>();
            for (String key : keys) {
                JsonObject obj1 = (JsonObject) obj.get(key);
                SmartthingsJSonAttributes attr = gson.fromJson(obj1, SmartthingsJSonAttributes.class);
                attr.setName(key);
                result.add(attr);
            }

            return result;
        }
    }

    class SmartthingsCommandsDeserializer implements JsonDeserializer<List<SmartthingsJSonCommands>> {
        @Override
        public @Nullable List<SmartthingsJSonCommands> deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            // TODO Auto-generated method stub

            List<SmartthingsJSonCommands> result = new ArrayList<SmartthingsJSonCommands>();

            if (json.isJsonObject()) {
                JsonObject obj = (JsonObject) json;
                Set<String> keys = obj.keySet();
                for (String key : keys) {
                    JsonObject obj1 = (JsonObject) obj.get(key);
                    SmartthingsJSonCommands commands = gson.fromJson(obj1, SmartthingsJSonCommands.class);
                    commands.setName(key);
                    result.add(commands);
                }
            } else if (json.isJsonArray()) {

            }

            return result;
        }
    }

    class SmartthingPropertiesDeserializer implements JsonDeserializer<List<SmartthingsJSonProperties>> {
        @Override
        public @Nullable List<SmartthingsJSonProperties> deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            // TODO Auto-generated method stub

            JsonObject obj = (JsonObject) json;
            Set<String> keys = obj.keySet();
            List<SmartthingsJSonProperties> result = new ArrayList<SmartthingsJSonProperties>();
            for (String key : keys) {
                JsonObject obj1 = (JsonObject) obj.get(key);
                SmartthingsJSonProperties props = gson.fromJson(obj1, SmartthingsJSonProperties.class);
                props.setName(key);
                result.add(props);
            }

            return result;
        }
    }

    public void loadCapabilitiesDefs() {
        Enumeration<String> entries = bundleContext.getBundle().getEntryPaths("json/");
        while (entries.hasMoreElements()) {
            String entry = entries.nextElement();

            boolean shouldLoad = false;
            if (entry.indexOf("colorControl") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("colorTemperature") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("switch") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("switchLevel") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("powerMeter") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("switch") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("energyMeter") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("powerConsumptionReport") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("battery") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("waterSensor") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("doorControl") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("thermostatHeatingSetpoint") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("execute") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("ovenOperationalState") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("ovenSetpoint") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("ovenMode") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("ovenOperatingState") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("temperatureMeasurement") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("healthCheck") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("refresh") >= 0) {
                shouldLoad = true;
            } else if (entry.indexOf("firmwareUpdate") >= 0) {
                shouldLoad = true;
            }

            if (entry.indexOf("Presentation") >= 0) {
                shouldLoad = false;
            }

            if (shouldLoad) {
                loadCapabilityDef(entry);
            }
        }
    }

    public void loadCapabilityDef(String path) {
        try {
            logger.info("loading capa: {}", path);
            String template = readTemplate(path);

            SmartthingsJSonCapabilities resultObj = getGson().fromJson(template, SmartthingsJSonCapabilities.class);
            capabilitiesDict.put(resultObj.id, resultObj);

        } catch (Exception ex) {
            logger.info("error loading capa: {} <> {}", path, ex.toString());
            logger.info("");
        }
    }

    public Gson getGson() {
        return gson;
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
    public void Register(SmartthingsDeviceData deviceData, JsonObject devObj) {
        generateThingsType(deviceData.id, deviceData.label, deviceData.deviceType, deviceData.description, devObj);
    }

    private void generateThingsType(String deviceId, String deviceLabel, String deviceType, String deviceDescription,
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

                            SmartthingsJSonCapabilities capa = capabilitiesDict.get(capId);

                            addChannel(deviceType, groupTypes, channelDefinitions, capId + "channel", capId);

                            logger.info("");
                        }
                    }

                }

                tt = createThingType(deviceType, deviceId, deviceDescription, groupTypes);
                lcThingTypeProvider.addThingType(tt);
            }
        }
    }

    private void addChannel(String deviceType, List<ChannelGroupType> groupTypes,
            List<ChannelDefinition> channelDefinitions, String channelTp, String channel) {
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
