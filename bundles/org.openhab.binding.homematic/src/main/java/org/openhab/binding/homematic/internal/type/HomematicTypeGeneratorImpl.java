/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.smarthome.config.core.ConfigDescriptionBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelDefinitionBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.EventOption;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.type.MetadataUtils.OptionsBuilder;
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
@Component(immediate = true)
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
                        String groupLabel = String.format("%s",
                                WordUtils.capitalizeFully(StringUtils.replace(channel.getType(), "_", " ")));
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
                        deviceType, StringUtils.join(firmwares, ", "));
            }
        }
    }

    /**
     * Adds the firmware version for validation.
     */
    private void addFirmware(HmDevice device) {
        if (!StringUtils.equals(device.getFirmware(), "?") && !DEVICE_TYPE_VIRTUAL.equals(device.getType())
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
            String id = StringUtils.substringAfterLast(groupType.getUID().getId(), "_");
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
    private ChannelType createChannelType(HmDatapoint dp, ChannelTypeUID channelTypeUID) {
        ChannelType channelType;
        if (dp.getName().equals(DATAPOINT_NAME_LOWBAT) || dp.getName().equals(DATAPOINT_NAME_LOWBAT_IP)) {
            channelType = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_LOW_BATTERY;
        } else if (dp.getName().equals(VIRTUAL_DATAPOINT_NAME_SIGNAL_STRENGTH)) {
            channelType = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_SIGNAL_STRENGTH;
        } else if (dp.getName().equals(VIRTUAL_DATAPOINT_NAME_BUTTON)) {
            channelType = DefaultSystemChannelTypeProvider.SYSTEM_BUTTON;
        } else {
            String itemType = MetadataUtils.getItemType(dp);
            String category = MetadataUtils.getCategory(dp, itemType);
            String label = MetadataUtils.getLabel(dp);
            String description = MetadataUtils.getDatapointDescription(dp);

            List<StateOption> options = null;
            if (dp.isEnumType()) {
                options = MetadataUtils.generateOptions(dp, new OptionsBuilder<StateOption>() {
                    @Override
                    public StateOption createOption(String value, String description) {
                        return new StateOption(value, description);
                    }
                });
            }

            StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();
            if (dp.isNumberType()) {
                BigDecimal min = MetadataUtils.createBigDecimal(dp.getMinValue());
                BigDecimal max = MetadataUtils.createBigDecimal(dp.getMaxValue());

                BigDecimal step = MetadataUtils.createBigDecimal(dp.getStep());
                if (step == null) {
                    step = MetadataUtils.createBigDecimal(dp.isFloatType() ? new Float(0.1) : new Long(1L));
                }
                stateFragment.withMinimum(min).withMaximum(max).withStep(step)
                        .withPattern(MetadataUtils.getStatePattern(dp)).withReadOnly(dp.isReadOnly());
            } else {
                stateFragment.withPattern(MetadataUtils.getStatePattern(dp)).withReadOnly(dp.isReadOnly());
            }
            if (options != null) {
                stateFragment.withOptions(options);
            }

            ChannelTypeBuilder channelTypeBuilder;
            EventDescription eventDescription = null;
            if (dp.isTrigger()) {
                eventDescription = new EventDescription(
                        MetadataUtils.generateOptions(dp, new OptionsBuilder<EventOption>() {
                            @Override
                            public EventOption createOption(String value, String description) {
                                return new EventOption(value, description);
                            }
                        }));
                channelTypeBuilder = ChannelTypeBuilder.trigger(channelTypeUID, label)
                        .withEventDescription(eventDescription);
            } else {
                channelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                        .withStateDescription(stateFragment.build().toStateDescription());
            }
            channelType = channelTypeBuilder.isAdvanced(!MetadataUtils.isStandard(dp)).withDescription(description)
                    .withCategory(category).withConfigDescriptionURI(configDescriptionUriChannel).build();
        }
        return channelType;
    }

    private void generateConfigDescription(HmDevice device, URI configDescriptionURI) {
        List<ConfigDescriptionParameter> parms = new ArrayList<>();
        List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        for (HmChannel channel : device.getChannels()) {
            String groupName = "HMG_" + channel.getNumber();
            String groupLabel = MetadataUtils.getDescription("CHANNEL_NAME") + " " + channel.getNumber();
            groups.add(new ConfigDescriptionParameterGroup(groupName, null, false, groupLabel, null));

            for (HmDatapoint dp : channel.getDatapoints()) {
                if (dp.getParamsetType() == HmParamsetType.MASTER) {
                    ConfigDescriptionParameterBuilder builder = ConfigDescriptionParameterBuilder.create(
                            MetadataUtils.getParameterName(dp), MetadataUtils.getConfigDescriptionParameterType(dp));

                    builder.withLabel(MetadataUtils.getLabel(dp));
                    builder.withDefault(ObjectUtils.toString(dp.getDefaultValue()));
                    builder.withDescription(MetadataUtils.getDatapointDescription(dp));

                    if (dp.isEnumType()) {
                        builder.withLimitToOptions(dp.isEnumType());
                        List<ParameterOption> options = MetadataUtils.generateOptions(dp,
                                new OptionsBuilder<ParameterOption>() {
                                    @Override
                                    public ParameterOption createOption(String value, String description) {
                                        return new ParameterOption(value, description);
                                    }
                                });
                        builder.withOptions(options);
                    }

                    if (dp.isNumberType()) {
                        builder.withMinimum(MetadataUtils.createBigDecimal(dp.getMinValue()));
                        builder.withMaximum(MetadataUtils.createBigDecimal(dp.getMaxValue()));
                        builder.withStepSize(
                                MetadataUtils.createBigDecimal(dp.isFloatType() ? new Float(0.1) : new Long(1L)));
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
        return StringUtils.indexOfAny(dp.getName(), IGNORE_DATAPOINT_NAMES) != -1;
    }
}
