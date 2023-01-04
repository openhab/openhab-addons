/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.type;

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.*;
import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameterGroup;
import org.openhab.core.config.core.ConfigDescriptionParameterGroupBuilder;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
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
import org.openhab.core.types.EventDescription;
import org.openhab.core.types.EventOption;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates ThingTypes based on metadata from a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component
public class HomematicTypeGeneratorImpl implements HomematicTypeGenerator {
    private final Logger logger = LoggerFactory.getLogger(HomematicTypeGeneratorImpl.class);
    private static URI configDescriptionUriChannel;

    private HomematicThingTypeProvider thingTypeProvider;
    private HomematicChannelTypeProvider channelTypeProvider;
    private HomematicChannelGroupTypeProvider channelGroupTypeProvider;
    private HomematicConfigDescriptionProvider configDescriptionProvider;
    private final Map<String, Set<String>> firmwaresByType = new HashMap<>();

    private static final String[] IGNORE_DATAPOINT_NAMES = new String[] { DATAPOINT_NAME_AES_KEY,
            VIRTUAL_DATAPOINT_NAME_RELOAD_FROM_GATEWAY };

    public HomematicTypeGeneratorImpl() {
        try {
            configDescriptionUriChannel = new URI(CONFIG_DESCRIPTION_URI_CHANNEL);
        } catch (Exception ex) {
            logger.warn("Can't create ConfigDescription URI '{}', ConfigDescription for channels not avilable!",
                    CONFIG_DESCRIPTION_URI_CHANNEL);
        }
    }

    @Reference
    protected void setThingTypeProvider(HomematicThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = thingTypeProvider;
    }

    protected void unsetThingTypeProvider(HomematicThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = null;
    }

    @Reference
    protected void setChannelTypeProvider(HomematicChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    protected void unsetChannelTypeProvider(HomematicChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

    @Reference
    protected void setChannelGroupTypeProvider(HomematicChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = channelGroupTypeProvider;
    }

    protected void unsetChannelGroupTypeProvider(HomematicChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = null;
    }

    @Reference
    protected void setConfigDescriptionProvider(HomematicConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = configDescriptionProvider;
    }

    protected void unsetConfigDescriptionProvider(HomematicConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = null;
    }

    @Override
    @Activate
    public void initialize() {
        MetadataUtils.initialize();
    }

    @Override
    public void generate(HmDevice device) {
        if (thingTypeProvider != null) {
            ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(device);
            ThingType tt = thingTypeProvider.getInternalThingType(thingTypeUID);

            if (tt == null || device.isGatewayExtras()) {
                logger.debug("Generating ThingType for device '{}' with {} datapoints", device.getType(),
                        device.getDatapointCount());

                List<ChannelGroupType> groupTypes = new ArrayList<>();
                for (HmChannel channel : device.getChannels()) {
                    List<ChannelDefinition> channelDefinitions = new ArrayList<>();
                    // Omit thing channel definitions for reconfigurable channels;
                    // those will be populated dynamically during thing initialization
                    if (!channel.isReconfigurable()) {
                        // generate channel
                        for (HmDatapoint dp : channel.getDatapoints()) {
                            if (!isIgnoredDatapoint(dp) && dp.getParamsetType() == HmParamsetType.VALUES) {
                                ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(dp);
                                ChannelType channelType = channelTypeProvider.getInternalChannelType(channelTypeUID);
                                if (channelType == null) {
                                    channelType = createChannelType(dp, channelTypeUID);
                                    channelTypeProvider.addChannelType(channelType);
                                }

                                ChannelDefinition channelDef = new ChannelDefinitionBuilder(dp.getName(),
                                        channelType.getUID()).build();
                                channelDefinitions.add(channelDef);
                            }
                        }
                    }

                    // generate group
                    ChannelGroupTypeUID groupTypeUID = UidUtils.generateChannelGroupTypeUID(channel);
                    ChannelGroupType groupType = channelGroupTypeProvider.getInternalChannelGroupType(groupTypeUID);
                    if (groupType == null || device.isGatewayExtras()) {
                        String groupLabel = String.format("%s", channel.getType() == null ? null
                                : MiscUtils.capitalize(channel.getType().replace("_", " ")));
                        groupType = ChannelGroupTypeBuilder.instance(groupTypeUID, groupLabel)
                                .withChannelDefinitions(channelDefinitions).build();
                        channelGroupTypeProvider.addChannelGroupType(groupType);
                        groupTypes.add(groupType);
                    }

                }
                tt = createThingType(device, groupTypes);
                thingTypeProvider.addThingType(tt);
            }
            addFirmware(device);
        }
    }

    @Override
    public void validateFirmwares() {
        for (String deviceType : firmwaresByType.keySet()) {
            Set<String> firmwares = firmwaresByType.get(deviceType);
            if (firmwares.size() > 1) {
                logger.info(
                        "Multiple firmware versions for device type '{}' found ({}). "
                                + "Make sure, all devices of the same type have the same firmware version, "
                                + "otherwise you MAY have channel and/or datapoint errors in the logfile",
                        deviceType, String.join(", ", firmwares));
            }
        }
    }

    /**
     * Adds the firmware version for validation.
     */
    private void addFirmware(HmDevice device) {
        if (!"?".equals(device.getFirmware()) && !DEVICE_TYPE_VIRTUAL.equals(device.getType())
                && !DEVICE_TYPE_VIRTUAL_WIRED.equals(device.getType())) {
            Set<String> firmwares = firmwaresByType.get(device.getType());
            if (firmwares == null) {
                firmwares = new HashSet<>();
                firmwaresByType.put(device.getType(), firmwares);
            }
            firmwares.add(device.getFirmware());
        }
    }

    /**
     * Creates the ThingType for the given device.
     */
    private ThingType createThingType(HmDevice device, List<ChannelGroupType> groupTypes) {
        String label = MetadataUtils.getDeviceName(device);
        String description = String.format("%s (%s)", label, device.getType());

        List<String> supportedBridgeTypeUids = new ArrayList<>();
        supportedBridgeTypeUids.add(THING_TYPE_BRIDGE.toString());
        ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(device);

        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, PROPERTY_VENDOR_NAME);
        properties.put(Thing.PROPERTY_MODEL_ID, device.getType());

        URI configDescriptionURI = getConfigDescriptionURI(device);
        if (configDescriptionProvider.getInternalConfigDescription(configDescriptionURI) == null) {
            generateConfigDescription(device, configDescriptionURI);
        }

        List<ChannelGroupDefinition> groupDefinitions = new ArrayList<>();
        for (ChannelGroupType groupType : groupTypes) {
            int usPos = groupType.getUID().getId().lastIndexOf("_");
            String id = usPos == -1 ? groupType.getUID().getId() : groupType.getUID().getId().substring(usPos + 1);
            groupDefinitions.add(new ChannelGroupDefinition(id, groupType.getUID()));
        }

        return ThingTypeBuilder.instance(thingTypeUID, label).withSupportedBridgeTypeUIDs(supportedBridgeTypeUids)
                .withDescription(description).withChannelGroupDefinitions(groupDefinitions).withProperties(properties)
                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).withConfigDescriptionURI(configDescriptionURI)
                .build();
    }

    /**
     * Creates the ChannelType for the given datapoint.
     */
    public static ChannelType createChannelType(HmDatapoint dp, ChannelTypeUID channelTypeUID) {
        ChannelType channelType;
        if (dp.getName().equals(DATAPOINT_NAME_LOWBAT) || dp.getName().equals(DATAPOINT_NAME_LOWBAT_IP)) {
            channelType = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_LOW_BATTERY;
        } else if (dp.getName().equals(VIRTUAL_DATAPOINT_NAME_SIGNAL_STRENGTH)) {
            channelType = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_SIGNAL_STRENGTH;
        } else if (dp.getName().equals(VIRTUAL_DATAPOINT_NAME_BUTTON)) {
            channelType = DefaultSystemChannelTypeProvider.SYSTEM_BUTTON;
        } else {
            String itemType = MetadataUtils.getItemType(dp);
            StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create()
                    .withPattern(MetadataUtils.getStatePattern(dp)).withReadOnly(dp.isReadOnly());

            if (dp.isNumberType()) {
                final BigDecimal min, max;
                if (ITEM_TYPE_DIMMER.equals(itemType) || ITEM_TYPE_ROLLERSHUTTER.equals(itemType)) {
                    // those types use PercentTypeConverter, so set up min and max as percent values
                    min = MetadataUtils.createBigDecimal(0);
                    max = MetadataUtils.createBigDecimal(100);
                } else {
                    min = MetadataUtils.createBigDecimal(dp.getMinValue());
                    max = MetadataUtils.createBigDecimal(dp.getMaxValue());
                }
                stateFragment.withMinimum(min).withMaximum(max);
            } else if (dp.isEnumType()) {
                List<StateOption> options = MetadataUtils.generateOptions(dp,
                        (value, description) -> new StateOption(value, description));
                if (options != null) {
                    stateFragment.withOptions(options);
                }
            }

            String label = MetadataUtils.getLabel(dp);
            final ChannelTypeBuilder channelTypeBuilder;
            if (dp.isTrigger()) {
                EventDescription eventDescription = new EventDescription(
                        MetadataUtils.generateOptions(dp, (value, description) -> new EventOption(value, description)));
                channelTypeBuilder = ChannelTypeBuilder.trigger(channelTypeUID, label)
                        .withEventDescription(eventDescription);
            } else {
                channelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                        .withStateDescriptionFragment(stateFragment.build());
            }
            channelType = channelTypeBuilder.isAdvanced(!MetadataUtils.isStandard(dp))
                    .withDescription(MetadataUtils.getDatapointDescription(dp))
                    .withCategory(MetadataUtils.getCategory(dp, itemType))
                    .withConfigDescriptionURI(configDescriptionUriChannel).build();
        }
        return channelType;
    }

    private void generateConfigDescription(HmDevice device, URI configDescriptionURI) {
        List<ConfigDescriptionParameter> parms = new ArrayList<>();
        List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        for (HmChannel channel : device.getChannels()) {
            String groupName = "HMG_" + channel.getNumber();
            String groupLabel = MetadataUtils.getDescription("CHANNEL_NAME") + " " + channel.getNumber();
            groups.add(ConfigDescriptionParameterGroupBuilder.create(groupName).withLabel(groupLabel).build());

            for (HmDatapoint dp : channel.getDatapoints()) {
                if (dp.getParamsetType() == HmParamsetType.MASTER) {
                    String defaultValueString = Objects.toString(dp.getDefaultValue(), "");
                    ConfigDescriptionParameterBuilder builder = ConfigDescriptionParameterBuilder.create(
                            MetadataUtils.getParameterName(dp), MetadataUtils.getConfigDescriptionParameterType(dp));

                    builder.withLabel(MetadataUtils.getLabel(dp));
                    builder.withDefault(defaultValueString);
                    builder.withDescription(MetadataUtils.getDatapointDescription(dp));
                    if (dp.isEnumType()) {
                        builder.withLimitToOptions(dp.isEnumType());
                        List<ParameterOption> options = MetadataUtils.generateOptions(dp,
                                (value, description) -> new ParameterOption(value, description));
                        builder.withOptions(options);
                        if (dp.isEnumType()) {
                            logger.trace("Checking if default option {} is valid",
                                    Objects.toString(dp.getDefaultValue(), ""));
                            boolean needsChange = options.stream()
                                    .noneMatch(opt -> opt.getValue().equals(defaultValueString));
                            if (needsChange) {
                                String defStr = Objects.toString(dp.getDefaultValue(), "0");
                                int offset = defStr != null ? Integer.valueOf(defStr) : 0;
                                if (offset >= 0 && offset < options.size()) {
                                    ParameterOption defaultOption = options.get(offset);
                                    logger.trace("Changing default option to {} (offset {})", defaultOption, offset);
                                    builder.withDefault(defaultOption.getValue());
                                } else if (options.size() > 0) {
                                    ParameterOption defaultOption = options.get(0);
                                    logger.trace("Changing default option to {} (first value)", defaultOption);
                                    builder.withDefault(defaultOption.getValue());
                                }
                            }
                        }
                    }

                    if (dp.isNumberType()) {
                        Number defaultValue = (Number) dp.getDefaultValue();
                        Number maxValue = dp.getMaxValue();
                        Number minValue = dp.getMinValue();
                        if (defaultValue != null) {
                            // some datapoints can have a default value that is greater than the maximum value
                            if (maxValue != null && defaultValue.doubleValue() > maxValue.doubleValue()) {
                                maxValue = defaultValue;
                            }
                            // ... and there are also default values less than the minimum value
                            if (minValue != null && defaultValue.doubleValue() < minValue.doubleValue()) {
                                minValue = defaultValue;
                            }
                        }
                        builder.withMinimum(MetadataUtils.createBigDecimal(minValue));
                        builder.withMaximum(MetadataUtils.createBigDecimal(maxValue));
                        builder.withUnitLabel(MetadataUtils.getUnit(dp));
                    }

                    builder.withGroupName(groupName);
                    parms.add(builder.build());
                }
            }
        }
        configDescriptionProvider.addConfigDescription(ConfigDescriptionBuilder.create(configDescriptionURI)
                .withParameters(parms).withParameterGroups(groups).build());
    }

    private URI getConfigDescriptionURI(HmDevice device) {
        try {
            return new URI(
                    String.format("%s:%s", CONFIG_DESCRIPTION_URI_THING_PREFIX, UidUtils.generateThingTypeUID(device)));
        } catch (URISyntaxException ex) {
            logger.warn("Can't create configDescriptionURI for device type {}", device.getType());
            return null;
        }
    }

    /**
     * Returns true, if the given datapoint can be ignored for metadata generation.
     */
    public static boolean isIgnoredDatapoint(HmDatapoint dp) {
        for (String testValue : IGNORE_DATAPOINT_NAMES) {
            if (dp.getName().indexOf(testValue) > -1) {
                return true;
            }
        }
        return false;
    }
}
