/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.siemenshvac.internal.Metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacCallback;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnectorImpl;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelGroupTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacConfigDescriptionProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacThingTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.UidUtils;
import org.openhab.core.OpenHAB;
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
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component(immediate = true)
@NonNullByDefault
public class SiemensHvacMetadataRegistryImpl implements SiemensHvacMetadataRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SiemensHvacMetadataRegistryImpl.class);

    // private Map<ThingTypeUID, ThingType> thingTypesByUID = new HashMap<>();
    // protected List<HomematicThingTypeExcluder> homematicThingTypeExcluders = new CopyOnWriteArrayList<>();

    // A map contains data point config read from Api and/or WebPages
    private @Nullable Map<String, SiemensHvacMetadata> dptMap = null;
    private @Nullable SiemensHvacMetadata root = null;
    private @Nullable ArrayList<SiemensHvacMetadataDevice> devices = null;

    private boolean interrupted = false;

    private static final String JSON_DIR = OpenHAB.getUserDataFolder() + File.separatorChar + "jsondb";

    private @Nullable static URI configDescriptionUriChannel;

    private @Nullable SiemensHvacThingTypeProvider thingTypeProvider;
    private @Nullable SiemensHvacChannelTypeProvider channelTypeProvider;
    private @Nullable SiemensHvacChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SiemensHvacConfigDescriptionProvider configDescriptionProvider;
    private @Nullable SiemensHvacConnector hvacConnector;

    public SiemensHvacMetadataRegistryImpl() {
    }

    @Reference
    protected void setSiemensHvacConnector(SiemensHvacConnector hvacConnector) {
        this.hvacConnector = hvacConnector;
    }

    protected void unsetSiemensHvacConnector(SiemensHvacConnector hvacConnector) {
        this.hvacConnector = null;
    }

    @Reference
    protected void setThingTypeProvider(SiemensHvacThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = thingTypeProvider;
    }

    protected void unsetThingTypeProvider(SiemensHvacThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = null;
    }

    @Reference
    protected void setChannelTypeProvider(SiemensHvacChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    protected void unsetChannelTypeProvider(SiemensHvacChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

    //
    @Reference
    protected void setChannelGroupTypeProvider(SiemensHvacChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = channelGroupTypeProvider;
    }

    protected void unsetChannelGroupTypeProvider(SiemensHvacChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = null;
    }

    @Reference
    protected void setConfigDescriptionProvider(SiemensHvacConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = configDescriptionProvider;
    }

    protected void unsetConfigDescriptionProvider(SiemensHvacConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = null;
    }

    @Override
    public @Nullable SiemensHvacConnector getSiemensHvacConnector() {
        return this.hvacConnector;
    }

    @Override
    public @Nullable SiemensHvacChannelTypeProvider getChannelTypeProvider() {
        return this.channelTypeProvider;
    }

    @Override
    public @Nullable ArrayList<SiemensHvacMetadataDevice> getDevices() {
        return devices;
    }

    /**
     * Initializes the type generator.
     */
    @Override
    @Activate
    public void initialize() {
    }

    public void InitDptMap(@Nullable SiemensHvacMetadata node) {

        if (node.getClass() == SiemensHvacMetadataMenu.class) {
            SiemensHvacMetadataMenu mInformation = (SiemensHvacMetadataMenu) node;

            for (SiemensHvacMetadata child : mInformation.getChilds().values()) {
                InitDptMap(child);
            }
        }

        if (node != null) {
            if (node.getLongDesc() != null) {
                dptMap.put("byName" + node.getLongDesc(), node);
            }
            if (node.getShortDesc() != null) {
                dptMap.put("byName" + node.getShortDesc(), node);
            }
        }

        dptMap.put("byId" + node.getId(), node);
        dptMap.put("bySubId" + node.getSubId(), node);
        if (node.getClass() == SiemensHvacMetadataDataPoint.class) {
            SiemensHvacMetadataDataPoint dpi = (SiemensHvacMetadataDataPoint) node;
            dptMap.put("byDptId" + dpi.getDptId(), node);
        }
    }

    public void ResolveDetails() {
        for (String key : dptMap.keySet()) {
            if (key.indexOf("byId") < 0) {
                continue;
            }

            SiemensHvacMetadata node = dptMap.get(key);
            if (node.getClass() == SiemensHvacMetadataDataPoint.class) {
                SiemensHvacMetadataDataPoint dpi = (SiemensHvacMetadataDataPoint) node;
                if (!dpi.getDetailsResolved()) {
                    resolveDptDetails(dpi);
                }
            }
        }
    }

    public int UnresolveCount() {
        int count = 0;
        for (String key : dptMap.keySet()) {
            if (key.indexOf("byId") < 0) {
                continue;
            }

            SiemensHvacMetadata node = dptMap.get(key);
            if (node.getClass() == SiemensHvacMetadataDataPoint.class) {
                SiemensHvacMetadataDataPoint dpi = (SiemensHvacMetadataDataPoint) node;
                if (!dpi.getDetailsResolved()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public @Nullable SiemensHvacMetadataMenu getRoot() {
        return (SiemensHvacMetadataMenu) root;
    }

    @Override
    public void ReadMeta() {
        if (root == null) {
            logger.debug("siemensHvac:InitDptMap():begin");

            LoadMetaDataFromCache();

            ReadDeviceList();

            if (root == null) {
                root = new SiemensHvacMetadataMenu();
                ReadMetaData(root, -1);
                hvacConnector.WaitAllPendingRequest();
            }

            dptMap = new Hashtable<String, SiemensHvacMetadata>();
            InitDptMap(root);

            int unresolveCount = Integer.MAX_VALUE;

            while (unresolveCount > 0) {
                ResolveDetails();
                hvacConnector.WaitAllPendingRequest();
                unresolveCount = UnresolveCount();

            }

            SaveMetaDataToCache();

            SiemensHvacMetadataMenu rootMenu = getRoot();
            for (SiemensHvacMetadataDevice device : devices) {
                if (device.getType().indexOf("OZW672") >= 0) {
                    continue;
                }

                generateThingsType(device);
            }

            logger.debug("siemensHvac:InitDptMap():end");
        }

    }

    private void generateThingsType(SiemensHvacMetadataDevice device) {
        logger.debug("Generate thing types for device : " + device.getName() + "/" + device.getSerialNr());
        if (thingTypeProvider != null) {
            ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(device);
            ThingType tt = thingTypeProvider.getInternalThingType(thingTypeUID);

            if (tt == null) {

                List<ChannelGroupType> groupTypes = new ArrayList<>();

                int treeId = device.getTreeId();
                if (dptMap.containsKey("byId" + treeId)) {
                    SiemensHvacMetadataMenu menu = (SiemensHvacMetadataMenu) dptMap.get("byId" + treeId);

                    for (SiemensHvacMetadata child : menu.getChilds().values()) {

                        if (child instanceof SiemensHvacMetadataMenu) {
                            SiemensHvacMetadataMenu subMenu = (SiemensHvacMetadataMenu) child;

                            List<ChannelDefinition> channelDefinitions = new ArrayList<>();

                            for (SiemensHvacMetadata childDt : subMenu.getChilds().values()) {

                                if (childDt instanceof SiemensHvacMetadataDataPoint) {
                                    SiemensHvacMetadataDataPoint dataPoint = (SiemensHvacMetadataDataPoint) childDt;

                                    if (dataPoint.getDptType() == null) {
                                        continue;
                                    }

                                    ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(dataPoint);

                                    ChannelType channelType = channelTypeProvider
                                            .getInternalChannelType(channelTypeUID);
                                    if (channelType == null) {
                                        channelType = createChannelType(dataPoint, channelTypeUID);
                                        channelTypeProvider.addChannelType(channelType);
                                    }

                                    SiemensHvacMetadataDataPoint dpt = ((SiemensHvacMetadataDataPoint) childDt);

                                    Map<String, String> props = new Hashtable<String, String>();
                                    props.put("dptId", "" + dpt.getDptId());
                                    props.put("id", "" + dpt.getId());
                                    props.put("subId", "" + dpt.getSubId());
                                    props.put("groupdId", "" + dpt.getGroupId());

                                    String id = dataPoint.getId() + "_" + UidUtils.sanetizeId(dataPoint.getShortDesc());
                                    ChannelDefinition channelDef = new ChannelDefinitionBuilder(id,
                                            channelType.getUID()).withLabel(dataPoint.getShortDesc())
                                                    .withDescription(dataPoint.getLongDesc()).withProperties(props)
                                                    .build();

                                    channelDefinitions.add(channelDef);
                                }
                            }

                            // generate group
                            ChannelGroupTypeUID groupTypeUID = UidUtils.generateChannelGroupTypeUID(subMenu);
                            ChannelGroupType groupType = channelGroupTypeProvider
                                    .getInternalChannelGroupType(groupTypeUID);

                            if (groupType == null) {
                                String groupLabel = subMenu.getShortDesc();
                                groupType = ChannelGroupTypeBuilder.instance(groupTypeUID, groupLabel)
                                        .withChannelDefinitions(channelDefinitions).withCategory("")
                                        .withDescription(menu.getLongDesc()).build();
                                channelGroupTypeProvider.addChannelGroupType(groupType);
                                groupTypes.add(groupType);
                            }

                        }
                    }

                }

                tt = createThingType(device, groupTypes);
                thingTypeProvider.addThingType(tt);
            }
        }
    }

    private ChannelType createChannelType(SiemensHvacMetadataDataPoint dpt, ChannelTypeUID channelTypeUID) {
        ChannelType channelType;

        String itemType = getItemType(dpt);
        String category = getCategory(dpt);
        String label = dpt.getShortDesc();
        String description = dpt.getLongDesc();

        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();

        List<StateOption> options = new ArrayList<StateOption>();
        if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_ENUM)) {
            for (SiemensHvacMetadataPointChild opt : dpt.getChild()) {
                StateOption stOpt = new StateOption(opt.getValue(), opt.getText());
                options.add(stOpt);
            }
        }

        if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_NUMERIC)) {
            BigDecimal min = new BigDecimal(dpt.getMin());
            BigDecimal max = new BigDecimal(dpt.getMax());
            BigDecimal step = new BigDecimal(dpt.getResolution());

            stateFragment.withMinimum(min).withMaximum(max).withStep(step).withReadOnly(false);
        } else {
            stateFragment.withPattern(getStatePattern(dpt)).withReadOnly(dpt.getWriteAccess() == false);
        }

        if (options != null && !options.isEmpty()) {
            stateFragment.withOptions(options);
        }

        boolean isAdvanced = false;
        if (label.contains("_Y")) {
            isAdvanced = true;
        }
        if (label.contains("_K")) {
            isAdvanced = true;
        }
        if (label.contains("Histo")) {
            isAdvanced = true;
        }
        if (label.contains(" QX")) {
            isAdvanced = true;
        }

        ChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                .withStateDescriptionFragment(stateFragment.build());

        channelType = channelTypeBuilder.isAdvanced(isAdvanced).withDescription(description).withCategory(category)
                .build();

        return channelType;

    }

    /**
     * Creates the ThingType for the given device.
     */
    private ThingType createThingType(SiemensHvacMetadataDevice device, List<ChannelGroupType> groupTypes) {
        String name = device.getName();
        String description = device.getName();

        List<String> supportedBridgeTypeUids = new ArrayList<>();
        supportedBridgeTypeUids.add(SiemensHvacBindingConstants.THING_TYPE_OZW672.toString());
        ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(device);

        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, SiemensHvacBindingConstants.PROPERTY_VENDOR_NAME);
        properties.put(Thing.PROPERTY_MODEL_ID, device.getType());

        URI configDescriptionURI = getConfigDescriptionURI(device);
        if (configDescriptionProvider.getInternalConfigDescription(configDescriptionURI) == null) {
            generateConfigDescription(device, groupTypes, configDescriptionURI);
        }

        List<ChannelGroupDefinition> groupDefinitions = new ArrayList<>();
        for (ChannelGroupType groupType : groupTypes) {
            String id = groupType.getUID().getId();
            groupDefinitions.add(new ChannelGroupDefinition(id, groupType.getUID()));
        }

        return ThingTypeBuilder.instance(thingTypeUID, name).withSupportedBridgeTypeUIDs(supportedBridgeTypeUids)
                .withDescription(description).withChannelGroupDefinitions(groupDefinitions).withProperties(properties)
                .withRepresentationProperty(Thing.PROPERTY_MODEL_ID).withConfigDescriptionURI(configDescriptionURI)
                .withCategory(SiemensHvacBindingConstants.CATEGORY_THING_HVAC).build();
    }

    private URI getConfigDescriptionURI(SiemensHvacMetadataDevice device) {
        try {
            return new URI(String.format("%s:%s", SiemensHvacBindingConstants.CONFIG_DESCRIPTION_URI_THING_PREFIX,
                    UidUtils.generateThingTypeUID(device)));
        } catch (URISyntaxException ex) {
            logger.warn("Can't create configDescriptionURI for device type {}", device.getName());
            throw new RuntimeException("can't construct URI");
        }
    }

    private void generateConfigDescription(SiemensHvacMetadataDevice device, List<ChannelGroupType> groupTypes,
            URI configDescriptionURI) {
        List<ConfigDescriptionParameter> parms = new ArrayList<>();
        List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        configDescriptionProvider.addConfigDescription(ConfigDescriptionBuilder.create(configDescriptionURI)
                .withParameters(parms).withParameterGroups(groups).build());
    }

    public static String getItemType(SiemensHvacMetadataDataPoint dpt) {
        if (dpt.getDptType() != null) {
            if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_STRING)) {
                return SiemensHvacBindingConstants.ITEM_TYPE_STRING;
            } else if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_NUMERIC)) {
                return SiemensHvacBindingConstants.ITEM_TYPE_NUMBER;
            } else if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_ENUM)) {
                return SiemensHvacBindingConstants.ITEM_TYPE_NUMBER;
            } else if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_DATE)) {
                return SiemensHvacBindingConstants.ITEM_TYPE_DATETIME;
            } else if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_TIME)) {
                return SiemensHvacBindingConstants.ITEM_TYPE_DATETIME;
            } else if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_RADIO)) {
                return SiemensHvacBindingConstants.ITEM_TYPE_CONTACT;
            } else if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_SCHEDULER)) {
                return SiemensHvacBindingConstants.ITEM_TYPE_DATETIME;
            } else if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_CALENDAR)) {
                return SiemensHvacBindingConstants.ITEM_TYPE_DATETIME;
            } else {
                logger.debug("unknow type in getItemType()");

            }
        }

        return "";

    }

    /**
     * Determines the category for the given Datapoint.
     */
    public static String getCategory(SiemensHvacMetadataDataPoint dp) {
        String dpDialog = dp.getDialogType();
        int dpCatId = dp.getCatId();
        String dpType = dp.getDptType();
        String dptUnit = dp.getDptUnit();

        if (dptUnit != null && dptUnit.contains("°C")) {
            return SiemensHvacBindingConstants.CATEGORY_CHANNEL_PROPS_TEMP;
        } else if (dpType != null) {
            if (dpType.contains("DateTime")) {
                return SiemensHvacBindingConstants.CATEGORY_CHANNEL_PROPS_TIME;
            } else if (dpType.contains("TimeOfDay")) {
                return SiemensHvacBindingConstants.CATEGORY_CHANNEL_PROPS_TIME;
            } else if (dpType.contains("Enumeration")) {
                return SiemensHvacBindingConstants.CATEGORY_CHANNEL_WIDGETS_SWITCH;
            } else if (dpType.contains("RadioButton")) {
                return SiemensHvacBindingConstants.CATEGORY_CHANNEL_WIDGETS_SWITCH;
            } else if (dpType.contains("Numeric")) {
                return SiemensHvacBindingConstants.CATEGORY_CHANNEL_WIDGETS_NUMBER;
            } else {
                return SiemensHvacBindingConstants.CATEGORY_CHANNEL_CONTROL_HEATING;
            }
        }

        return SiemensHvacBindingConstants.CATEGORY_CHANNEL_CONTROL_HEATING;
        // String channelType = StringUtils.defaultString(dp.getChannel().getType());

        // Number
        // Text
        // Switch
        // Group
        // Sun
        // Moon
        // Humidity
        // Energy
        // Water
        // Pressure
        // QualityOfService
        // Flow
        // Heating
        // Alarm
        // Vacation

    }

    /**
     * Returns the state pattern metadata string with unit for the given Datapoint.
     */
    public static String getStatePattern(SiemensHvacMetadataDataPoint dpt) {
        String unit = dpt.getDptUnit();

        if ("%".equals(unit)) {
            return "%d %%";
        }

        if (unit != null && unit != "") {
            if (dpt.getDptType().equals(SiemensHvacBindingConstants.DPT_TYPE_NUMERIC)) {
                return String.format("%s %s", "%d", "%unit%");
            }
        }

        return "";
    }

    public void ReadDeviceList() {
        try {
            devices = new ArrayList<SiemensHvacMetadataDevice>();
            String request = "api/devicelist/list.json?";

            JsonObject response = hvacConnector.DoRequest(request, null);
            JsonArray devicesList = response.getAsJsonArray("Devices");

            for (JsonElement device : devicesList) {

                JsonObject obj = (JsonObject) device;
                String Name = "";
                String Addr = "";
                String Type = "";
                String SerialNr = "";
                String TreeDate = "";
                String TreeTime = "";
                boolean TreeGenerated = false;

                if (obj.has("Name")) {
                    Name = obj.get("Name").getAsString();
                }

                if (obj.has("Addr")) {
                    Addr = obj.get("Addr").getAsString();
                }

                if (obj.has("Type")) {
                    Type = obj.get("Type").getAsString();
                }

                if (obj.has("SerialNr")) {
                    SerialNr = obj.get("SerialNr").getAsString();
                }

                if (obj.has("TreeDate")) {
                    TreeDate = obj.get("TreeDate").getAsString();
                }

                if (obj.has("TreeTime")) {
                    TreeTime = obj.get("TreeTime").getAsString();
                }

                if (obj.has("TreeGenerated")) {
                    TreeGenerated = obj.get("TreeGenerated").getAsBoolean();
                }

                SiemensHvacMetadataDevice deviceObj = new SiemensHvacMetadataDevice();
                deviceObj.setName(Name);
                deviceObj.setAddr(Addr);
                deviceObj.setSerialNr(SerialNr);
                deviceObj.setType(Type);
                deviceObj.setTreeDate(TreeDate);
                deviceObj.setTreeTime(TreeTime);
                deviceObj.setTreeGenerated(TreeGenerated);

                String request2 = "api/menutree/device_root.json?TreeName=Web&SerialNumber=" + SerialNr;
                JsonObject response2 = hvacConnector.DoRequest(request2, null);

                if (response2.has("TreeItem")) {
                    JsonObject tree = response2.getAsJsonObject("TreeItem");
                    if (tree.has("Id")) {
                        int treeId = tree.get("Id").getAsInt();
                        deviceObj.setTreeId(treeId);
                    }
                }

                devices.add(deviceObj);
            }

        } catch (Exception e) {
            logger.error("siemensHvac:ResolveDpt:Error during dp reading: " + e.getLocalizedMessage());
            // Reset sessionId so we redone _auth on error
        }

    }

    public void ReadMetaData(SiemensHvacMetadata parent, int id) {
        try {
            String request = "api/menutree/list.json?";
            if (id != -1) {
                request = request + "&Id=" + id;
            }

            hvacConnector.DoRequest(request, new SiemensHvacCallback() {

                @Override
                public void execute(URI uri, int status, @Nullable Object response) {
                    if (response instanceof JsonObject) {
                        DecodeMetaDataResult((JsonObject) response, parent, id);
                    }
                }
            });

        } catch (Exception e) {
            logger.error("siemensHvac:ResolveDpt:Error during dp reading: " + id + " ; " + e.getLocalizedMessage());
            // Reset sessionId so we redone _auth on error
        }

    }

    private static int nbDpt = 0;

    public void DecodeMetaDataResult(JsonObject resultObj, SiemensHvacMetadata parent, int id) {
        if (resultObj.has("MenuItems")) {
            if (parent != null) {
                logger.debug("Decode menuItem for :" + parent.getShortDesc());
            }
            SiemensHvacMetadata childNode;
            JsonArray menuItems = resultObj.getAsJsonArray("MenuItems");

            for (JsonElement child : menuItems) {
                JsonObject menuItem = child.getAsJsonObject();

                childNode = new SiemensHvacMetadataMenu();
                childNode.setParent(parent);

                int itemId = -1;
                if (menuItem.has("Id")) {
                    itemId = menuItem.get("Id").getAsInt();
                }

                childNode.setId(itemId);

                if (menuItem.has("Text")) {
                    JsonObject descObj = menuItem.getAsJsonObject("Text");

                    int catId = -1;
                    int groupId = -1;
                    int subItemId = -1;
                    String longDesc = "";
                    String shortDesc = "";

                    if (descObj.has("CatId")) {
                        catId = descObj.get("CatId").getAsInt();
                    }
                    if (descObj.has("GroupId")) {
                        groupId = descObj.get("GroupId").getAsInt();
                    }
                    if (descObj.has("Id")) {
                        subItemId = descObj.get("Id").getAsInt();
                    }
                    if (descObj.has("Long")) {
                        longDesc = descObj.get("Long").getAsString();
                    }
                    if (descObj.has("Short")) {
                        shortDesc = descObj.get("Short").getAsString();
                    }

                    childNode.setSubId(subItemId);
                    childNode.setCatId(catId);
                    childNode.setGroupId(groupId);
                    childNode.setShortDesc(shortDesc);
                    childNode.setLongDesc(longDesc);
                    ((SiemensHvacMetadataMenu) parent).AddChild(childNode);

                    // logger.debug(String.format("siemensHvac:ResolveDpt():findMenuItem: %d, %s, %s, %s, %s", itemId,
                    // subItemId, groupId, catId, longDesc));

                    boolean shouldReadSub = false;

                    shouldReadSub = shouldReadSub || (itemId == 931);
                    shouldReadSub = shouldReadSub || (itemId == 932);
                    // shouldReadSub = shouldReadSub || (itemId == 936);
                    // shouldReadSub = shouldReadSub || (itemId == 947);
                    // shouldReadSub = shouldReadSub || (itemId == 956);
                    // shouldReadSub = shouldReadSub || (itemId == 965);
                    // shouldReadSub = shouldReadSub || (itemId == 974);
                    // shouldReadSub = shouldReadSub || (itemId == 992);
                    // shouldReadSub = shouldReadSub || (itemId == 1017);
                    // shouldReadSub = shouldReadSub || (itemId == 1030);
                    // shouldReadSub = shouldReadSub || (itemId == 1036);
                    // shouldReadSub = shouldReadSub || (itemId == 1040);
                    // shouldReadSub = shouldReadSub || (itemId == 1046);
                    // shouldReadSub = shouldReadSub || (itemId == 1070);
                    // shouldReadSub = shouldReadSub || (itemId == 1099);
                    // shouldReadSub = shouldReadSub || (itemId == 1260);
                    // shouldReadSub = shouldReadSub || (itemId == 1279);
                    // shouldReadSub = shouldReadSub || (itemId == 1300);
                    // shouldReadSub = shouldReadSub || (itemId == 1328);
                    // shouldReadSub = shouldReadSub || (itemId == 1489);
                    // shouldReadSub = shouldReadSub || (itemId == 1505);
                    // shouldReadSub = shouldReadSub || (itemId == 1558);

                    shouldReadSub = shouldReadSub || true;

                    if (shouldReadSub) {
                        ReadMetaData(childNode, itemId);
                    }

                }

            }
        }
        if (resultObj.has("DatapointItems"))

        {
            if (parent != null) {
                logger.debug("Decode dp for :" + parent.getShortDesc());
            }

            SiemensHvacMetadata childNode;
            JsonArray dptItems = resultObj.getAsJsonArray("DatapointItems");

            Map<String, SiemensHvacMetadataDataPoint> idMap = new Hashtable<String, SiemensHvacMetadataDataPoint>();

            for (JsonElement child : dptItems) {
                JsonObject dptItem = child.getAsJsonObject();

                nbDpt++;

                childNode = new SiemensHvacMetadataDataPoint();
                childNode.setParent(parent);

                int nodeId = -1;
                int dpSubKey = -1;
                boolean hasWriteAccess = false;
                String address = "";

                if (dptItem.has("Id")) {
                    nodeId = dptItem.get("Id").getAsInt();
                }
                if (dptItem.has("Address")) {
                    address = dptItem.get("Address").getAsString();
                }
                if (dptItem.has("DpSubKey")) {
                    dpSubKey = dptItem.get("DpSubKey").getAsInt();
                }
                if (dptItem.has("WriteAccess")) {
                    hasWriteAccess = dptItem.get("WriteAccess").getAsBoolean();
                }

                SiemensHvacMetadataDataPoint dptChild = (SiemensHvacMetadataDataPoint) childNode;

                dptChild.setId(nodeId);
                dptChild.setAddress(address);
                dptChild.setDptSubKey(dpSubKey);
                dptChild.setWriteAccess(hasWriteAccess);

                idMap.put("" + nodeId, dptChild);

                if (dptItem.has("Text")) {
                    JsonObject descObj = dptItem.getAsJsonObject("Text");

                    int catId = -1;
                    int groupId = -1;
                    int subItemId = -1;
                    String longDesc = "";
                    String shortDesc = "";

                    if (descObj.has("CatId")) {
                        catId = descObj.get("CatId").getAsInt();
                    }
                    if (descObj.has("GroupId")) {
                        groupId = descObj.get("GroupId").getAsInt();
                    }
                    if (descObj.has("Id")) {
                        subItemId = descObj.get("Id").getAsInt();
                    }
                    if (descObj.has("Long")) {
                        longDesc = descObj.get("Long").getAsString();
                    }
                    if (descObj.has("Short")) {
                        shortDesc = descObj.get("Short").getAsString();
                    }

                    childNode.setSubId(subItemId);
                    childNode.setCatId(catId);
                    childNode.setGroupId(groupId);
                    childNode.setShortDesc(shortDesc);
                    childNode.setLongDesc(longDesc);

                    // logger.debug(String.format("siemensHvac:ResolveDpt():findDpItem: %d, %s, %s, %s, %s %s", dptId,
                    // catId, groupId, subItemId, shortDesc, longDesc));
                }

                ((SiemensHvacMetadataMenu) parent).AddChild(childNode);

            }

            String request2 = "main.app?section=popcard&idtype=4";
            if (id != -1) {
                request2 = request2 + "&id=" + id;
            }

            hvacConnector.DoRequest(request2, new SiemensHvacCallback() {

                @Override
                public void execute(URI uri, int status, @Nullable Object response) {

                    String st = (String) response;
                    st = st.replace("\n", "");

                    Pattern pattern = Pattern
                            .compile("td class=\\\"dp_linenumber\\\".*?>(.*?)<\\/td>.+?(?=id)id=\"dp(.+?)\"");
                    Matcher matcher = pattern.matcher(st);

                    while (matcher.find()) {
                        String all = matcher.group(0);
                        String id = matcher.group(2);
                        String dptId = matcher.group(1);

                        if ((!StringUtils.isEmpty(id)) && (!StringUtils.isEmpty(dptId))) {

                            if (idMap.containsKey(id)) {
                                SiemensHvacMetadataDataPoint child = idMap.get(id);
                                child.setDptId(dptId);
                            }

                        }
                    }
                }
            });

        }
        if (resultObj.has("WidgetItems")) {
            // JSONArray wgItems = (JSONArray) result.get("WidgetItems");
        }

    }

    @Override
    public @Nullable SiemensHvacMetadata getDptMap(String key) {

        if (dptMap.containsKey("byMenu" + key)) {
            return dptMap.get("byMenu" + key);
        }
        if (dptMap.containsKey("byName" + key)) {
            return dptMap.get("byName" + key);
        }
        if (dptMap.containsKey("byDptId" + key)) {
            return dptMap.get("byDptId" + key);
        }

        return null;

    }

    public void LoadMetaDataFromCache() {
        File file = null;

        try {
            file = new File(JSON_DIR + File.separator + "siemens.json");

            if (!file.exists()) {
                return;
            }

            FileInputStream is = new FileInputStream(file);
            String js = IOUtils.toString(is);

            root = SiemensHvacConnectorImpl.getGsonWithAdapter().fromJson(js, SiemensHvacMetadataMenu.class);
        } catch (IOException ioe) {
            logger.error("Couldn't write WithingsAccount to file '{}'.", file.getAbsolutePath());

        }

    }

    public void SaveMetaDataToCache() {
        File file = null;

        try {
            file = new File(JSON_DIR + File.separator + "siemens.json");

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream os = new FileOutputStream(file);

            String js = SiemensHvacConnectorImpl.getGsonWithAdapter().toJson(root);

            IOUtils.write(js, os);
            IOUtils.closeQuietly(os);

        } catch (IOException ioe) {
            logger.error("Couldn't write WithingsAccount to file '{}'.", file.getAbsolutePath());

        }
    }

    public void resolveDptDetails(SiemensHvacMetadataDataPoint dpt) {
        if (dpt.getDetailsResolved()) {
            return;
        }

        String request = "api/menutree/datapoint_desc.json?Id=" + dpt.getId();
        hvacConnector.DoRequest(request, new SiemensHvacCallback() {

            @Override
            public void execute(URI uri, int status, @Nullable Object response) {
                if (response instanceof JsonObject) {
                    dpt.resolveDptDetails((JsonObject) response);
                } else {
                    logger.debug("errror");
                }
            }
        });
    }

}
