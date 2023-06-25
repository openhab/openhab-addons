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
package org.openhab.binding.lgthinq.internal.type;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;
import static org.openhab.core.thing.DefaultSystemChannelTypeProvider.SYSTEM_POWER;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.internal.model.*;
import org.openhab.core.config.core.*;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.*;
import org.openhab.core.thing.type.*;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingModelTypeUtils} class.
 *
 * @author Nemer Daud - Initial contribution
 */
// @Component
public class ThingModelTypeUtils {
    private static final Logger logger = LoggerFactory.getLogger(ThingModelTypeUtils.class);

    private ThinqThingTypeProvider thingTypeProvider;
    private ThinqChannelTypeProvider channelTypeProvider;
    private ThinqChannelGroupTypeProvider channelGroupTypeProvider;
    private ThinqConfigDescriptionProvider configDescriptionProvider;
    private LGThinQStateDescriptionProvider stateDescriptionProvider;

    @Reference
    public void setThingTypeProvider(ThinqThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = thingTypeProvider;
    }

    @Reference
    public void setChannelTypeProvider(ThinqChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    @Reference
    public void setChannelGroupTypeProvider(ThinqChannelGroupTypeProvider channelGroupTypeProvider) {
        this.channelGroupTypeProvider = channelGroupTypeProvider;
    }

    @Reference
    public void setConfigDescriptionProvider(ThinqConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = configDescriptionProvider;
    }

    @Reference
    public void setStateDescriptionProvider(LGThinQStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    protected ChannelTypeUID createDynTypeChannel(final String channelTypeId, final String channelLabel,
            final String itemType, final Boolean readOnly) {
        final StateDescriptionFragmentBuilder sdb = StateDescriptionFragmentBuilder.create();
        final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelTypeId + "-Type");
        String normLabel = channelLabel.replace(" ", "");
        final ChannelType channelType = ChannelTypeBuilder.state(channelTypeUID, normLabel, itemType)
                .withStateDescriptionFragment(sdb.withReadOnly(readOnly).build())
                .withConfigDescriptionURI(URI.create(String.format("channel-type:lgthinq:%s-type", normLabel))).build();
        channelTypeProvider.addChannelType(channelType);
        return channelTypeUID;
    }

    @NonNull
    private URI getConfigDescriptionURI(ThinqDevice device) {
        try {
            return new URI(String.format("%s:%s", "thing-type", UidUtils.generateThingTypeUID(device)));
        } catch (URISyntaxException ex) {
            String msg = String.format("Can't create configDescriptionURI for device type %s", device.getType());
            throw new IllegalStateException(msg, ex);
        }
    }

    protected ChannelGroupTypeUID createAndRegistryGroupTypeChannel(final ThinqChannelGroup channelGroup,
            final List<ChannelDefinition> channelDefinitions) {
        ChannelGroupTypeUID groupTypeUID = UidUtils.generateChannelGroupTypeUID(channelGroup);
        ChannelGroupType groupType = channelGroupTypeProvider.getChannelGroupType(groupTypeUID, Locale.getDefault());
        if (groupType == null) {

            groupType = ChannelGroupTypeBuilder.instance(groupTypeUID, channelGroup.getLabel())
                    .withChannelDefinitions(channelDefinitions).build();
            channelGroupTypeProvider.addChannelGroupType(groupType);
        }

        return groupTypeUID;
    }

    public void generate(ThinqDevice device) {
        if (thingTypeProvider != null) {
            ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(device);
            ThingType thingType = thingTypeProvider.getThingType(thingTypeUID, Locale.getDefault());
            if (thingType == null) {
                HashMap<ThinqChannelGroup, List<ChannelDefinition>> groupChannelsMap = new HashMap<>();
                logger.debug("Generating ThingType for device '{}' with {} channels", device.getType(),
                        device.getChannels().size());
                device.getChannels().forEach(c -> {
                    // Only generate Channel that is not dynamic. Dyn channel will be created depending on the
                    // thing handler decision.
                    if (!c.isDynamic()) {
                        // generate channel
                        ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(c);
                        ChannelType channelType = channelTypeProvider.getChannelType(channelTypeUID,
                                Locale.getDefault());
                        if (channelType == null) {
                            channelType = createChannelType(c, channelTypeUID);
                            channelTypeProvider.addChannelType(channelType);
                        }

                        ChannelDefinition channelDef = new ChannelDefinitionBuilder(c.getName(), channelType.getUID())
                                .build();
                        groupChannelsMap.computeIfAbsent(c.getChannelGroup(), k -> new ArrayList<>()).add(channelDef);
                    }
                });
                groupChannelsMap.forEach(this::createAndRegistryGroupTypeChannel);
            }
            thingType = createThingType(device, channelGroupTypeProvider.internalGroupTypes());
            thingTypeProvider.addThingType(thingType);
        }
    }

    private ThingType createThingType(ThinqDevice device, List<ChannelGroupType> groupTypes) {
        String label = device.getLabel();
        String description = device.getDescription();

        List<String> supportedBridgeTypeUids = List.of(THING_TYPE_BRIDGE.toString());
        ThingTypeUID thingTypeUID = UidUtils.generateThingTypeUID(device);

        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, PROPERTY_VENDOR_NAME);
        properties.put(Thing.PROPERTY_MODEL_ID, device.getType());

        URI configDescriptionURI = getConfigDescriptionURI(device);
        if (configDescriptionProvider.getConfigDescription(configDescriptionURI, Locale.getDefault()) == null) {
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

    public void generateConfigDescription(ThinqDevice device, URI configDescriptionURI) {
        List<ConfigDescriptionParameter> params = new ArrayList<>();
        List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        for (DeviceParameter param : device.getConfigParameter()) {
            String groupName = null;
            if (param.getGroup() != null) {
                groupName = param.getGroup().getGroupName();
                groups.add(ConfigDescriptionParameterGroupBuilder.create(groupName)
                        .withLabel(param.getGroup().getGroupLabel()).build());
            }
            ConfigDescriptionParameterBuilder builder = ConfigDescriptionParameterBuilder.create(param.getName(),
                    param.getType());
            builder.withLabel(param.getLabel());
            builder.withDefault(param.getDefaultValue());
            builder.withDescription(param.getDescription());
            builder.withReadOnly(param.isReadOnly());
            if (param.getOptions() != null)
                builder.withOptions(param.getOptions());

            builder.withGroupName(groupName);
            params.add(builder.build());
        }
        configDescriptionProvider.addConfigDescription(ConfigDescriptionBuilder.create(configDescriptionURI)
                .withParameters(params).withParameterGroups(groups).build());
    }

    private ChannelType createChannelType(ThinqChannel channel, ChannelTypeUID channelTypeUID) {
        /*
         * <channel id="power" typeId="system.power"/>
         * <channel id="state" typeId="washer-state"/>
         * <channel id="course" typeId="washer-course"/>
         * <channel id="temperature-level" typeId="washer-temp-level"/>
         * <channel id="door-lock" typeId="washer-door-lock"/>
         * <channel id="remain-time" typeId="washerdryer-remain-time"/>
         * <channel id="rinse" typeId="washer-rinse"/>
         * <channel id="spin" typeId="washer-spin"/>
         * <channel id="delay-time" typeId="washerdryer-delay-time"/>
         */
        DataType dataType = channel.getType();
        if (dataType.getName().equals("system.power")) {
            return SYSTEM_POWER;
        } else {
            String itemType = dataType.getName();
            StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();
            if (channel.getUnitDisplayPattern() != null) {
                stateFragment.withPattern(Objects.requireNonNull(channel.getUnitDisplayPattern()));
            }
            stateFragment.withReadOnly(channel.isReadOnly());

            if (dataType.isNumeric()) {
                final BigDecimal min, max;
                if (CoreItemFactory.DIMMER.equals(itemType) || CoreItemFactory.ROLLERSHUTTER.equals(itemType)) {
                    // those types use PercentTypeConverter, so set up min and max as percent values
                    min = BigDecimal.ZERO;
                    max = new BigDecimal(100);
                    stateFragment.withMinimum(min).withMaximum(max);
                }
            } else if (dataType.isEnum() && dataType.getOptions() != null) {
                stateFragment.withOptions(Objects.requireNonNull(dataType.getOptions()));
            }

            String label = channel.getLabel();
            URI configUriDescriptor;
            try {
                configUriDescriptor = new URI(CONFIG_DESCRIPTION_URI_CHANNEL);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(
                        "Error creating URI configuration for a Thinq channel. It's most likely a bug.", e);
            }
            final ChannelTypeBuilder channelTypeBuilder;
            channelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                    .withStateDescriptionFragment(stateFragment.build()).isAdvanced(channel.isAdvanced())
                    .withDescription(channel.getDescription()).withConfigDescriptionURI(configUriDescriptor);
            String category = discoverCategory(channel);
            if (category != null) {
                channelTypeBuilder.withCategory(category);
            }
            return channelTypeBuilder.build();
        }
    }

    @Nullable
    private String discoverCategory(ThinqChannel c) {
        switch (c.getType().getName()) {
            case "washer-temp-level":
                return "Temperature";
            case "washerdryer-delay-time":
            case "washerdryer-remain-time":
                return "Time";
            default:
                return null;
        }
    }
}
