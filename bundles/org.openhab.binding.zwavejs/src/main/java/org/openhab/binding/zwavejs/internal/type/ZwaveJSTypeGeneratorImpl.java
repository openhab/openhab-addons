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
package org.openhab.binding.zwavejs.internal.type;

import static org.openhab.binding.zwavejs.internal.BindingConstants.CONFIGURATION_COMMAND_CLASSES;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.binding.zwavejs.internal.conversion.ChannelMetadata;
import org.openhab.binding.zwavejs.internal.conversion.ConfigMetadata;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragment;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates openHAB entities (Channel, ChannelType and ConfigDescription) based on the Z-Wave JS data.
 *
 * @see ChannelType
 * @see ChannelTypeBuilder
 * @see ChannelTypeUID
 * @see StateChannelTypeBuilder
 * @see ThingRegistry
 * @see ZwaveJSChannelTypeProvider
 * @see ZwaveJSConfigDescriptionProvider
 *
 * @author Leo Siepel - Initial contribution
 */
@Component
@NonNullByDefault
public class ZwaveJSTypeGeneratorImpl implements ZwaveJSTypeGenerator {

    private static final Object CHANNEL_TYPE_VERSION = "3"; // when static configuration is changed, the version must be
                                                            // changed as well to force a new channel type generation
    private final Logger logger = LoggerFactory.getLogger(ZwaveJSTypeGeneratorImpl.class);

    private final ThingRegistry thingRegistry;
    private final ZwaveJSChannelTypeProvider channelTypeProvider;
    private final ZwaveJSConfigDescriptionProvider configDescriptionProvider;

    @Activate
    public ZwaveJSTypeGeneratorImpl(@Reference ZwaveJSChannelTypeProvider channelTypeProvider,
            @Reference ZwaveJSConfigDescriptionProvider configDescriptionProvider,
            @Reference ThingRegistry thingRegistry) {
        this.channelTypeProvider = channelTypeProvider;
        this.configDescriptionProvider = configDescriptionProvider;
        this.thingRegistry = thingRegistry;
    }

    /*
     * Retrieves a Thing by its UID.
     *
     * @param thingUID the UID of the Thing
     * 
     * @return the Thing, or null if not found
     */
    public @Nullable Thing getThing(ThingUID thingUID) {
        return thingRegistry.get(thingUID);
    }

    /*
     * Generates a ZwaveJSTypeGeneratorResult for the given ThingUID and Node.
     *
     * @param thingUID the ThingUID of the device
     * 
     * @param node the Node containing the values to be processed
     * 
     * @param configurationAsChannels flag indicating whether to treat configuration as channels
     * 
     * @return a ZwaveJSTypeGeneratorResult containing the generated channels and configuration descriptions
     */
    @Override
    public ZwaveJSTypeGeneratorResult generate(ThingUID thingUID, Node node, boolean configurationAsChannels) {
        ZwaveJSTypeGeneratorResult result = new ZwaveJSTypeGeneratorResult();
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<>();
        URI uri = Objects.requireNonNull(getConfigDescriptionURI(thingUID, node));
        for (Value value : node.values) {
            if (!configurationAsChannels && CONFIGURATION_COMMAND_CLASSES.contains(value.commandClassName)) {
                ConfigMetadata metadata = new ConfigMetadata(node.nodeId, value);
                configDescriptions.add(createConfigDescription(metadata));
                if (!result.values.containsKey(metadata.id) && value.value != null) {
                    result.values.put(metadata.id, value.value);
                }
            }
            ChannelMetadata metadata = new ChannelMetadata(node.nodeId, value);
            if (configurationAsChannels || !CONFIGURATION_COMMAND_CLASSES.contains(value.commandClassName)) {
                result.channels = createChannel(thingUID, result.channels, metadata, configDescriptionProvider);
                if (!metadata.isIgnoredCommandClass(value.commandClassName) && !result.values.containsKey(metadata.id)
                        && value.value != null) {
                    result.values.put(metadata.id, value.value);
                }
            }
        }
        logger.debug("Node {}. Generated {} channels and {} configDescriptions with URI {}", node.nodeId,
                result.channels.size(), configDescriptions.size(), uri);

        configDescriptionProvider
                .addConfigDescription(ConfigDescriptionBuilder.create(uri).withParameters(configDescriptions).build());

        return result;
    }

    private ConfigDescriptionParameter createConfigDescription(ConfigMetadata details) {
        logger.trace("Node {}. createConfigDescriptions with Id: {}", details.nodeId, details.id);

        ConfigDescriptionParameterBuilder parameterBuilder = ConfigDescriptionParameterBuilder
                .create(details.id, details.configType) //
                .withRequired(details.state != null) //
                .withLabel(details.label) //
                .withVerify(details.state != null) //
                .withUnit(null) //
                .withDescription(details.description);

        if (details.unitSymbol != null) {
            parameterBuilder.withUnit(details.unitSymbol);
        }
        Map<String, String> optionList = details.optionList;
        if (optionList != null) {
            List<ParameterOption> options = new ArrayList<>();
            optionList.forEach((k, v) -> options.add(new ParameterOption(k, v)));
            parameterBuilder.withLimitToOptions(true);
            parameterBuilder.withMultiple(false);
            parameterBuilder.withContext("item");
            parameterBuilder.withOptions(options);
        }

        return parameterBuilder.build();
    }

    private Map<String, Channel> createChannel(ThingUID thingUID, Map<String, Channel> channels,
            ChannelMetadata details, ZwaveJSConfigDescriptionProvider configDescriptionProvider) {
        if (details.isIgnoredCommandClass(details.commandClassName)) {
            return channels;
        }
        logger.trace("Node {} building channel with Id: {}", details.nodeId, details.id);
        logger.trace(" >> {}", details);

        ChannelUID channelUID = new ChannelUID(thingUID, details.id);

        Configuration newChannelConfiguration = new Configuration();
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_INCOMING_UNIT, details.unitSymbol);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_ITEM_TYPE, details.itemType);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_COMMANDCLASS_ID, details.commandClassId);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_COMMANDCLASS_NAME, details.commandClassName);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_ENDPOINT, details.endpoint);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_FACTOR, details.factor);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_INVERTED, false);

        if (details.writable) {
            newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY,
                    String.valueOf(details.writeProperty));
        }

        @Nullable
        Channel existingChannel = channels.get(channelUID.getId());
        if (existingChannel != null) {
            Configuration existingChannelConfiguration = existingChannel.getConfiguration();
            if (ChannelMetadata.isSameReadWriteChannel(existingChannelConfiguration, newChannelConfiguration)) {
                ChannelTypeUID newChannelTypeUID = generateChannelTypeUID(details);
                ChannelBuilder builder = ChannelBuilder.create(existingChannel)
                        .withConfiguration(details.writable ? newChannelConfiguration : existingChannelConfiguration);

                if (!newChannelTypeUID.equals(existingChannel.getChannelTypeUID())) {
                    ChannelType newChannelType = getOrGenerate(newChannelTypeUID, details);
                    if (newChannelType != null) {
                        builder.withAcceptedItemType(newChannelType.getItemType()).withType(newChannelType.getUID());
                    }
                }

                channels.put(details.id, builder.build());

                logger.debug("Node {}. Channel {} existing channel updated", details.nodeId, details.id);
                return channels;
            } else {
                logger.debug("Node {}. Channel {} exists: ignored", details.nodeId, details.id);
                return channels;
            }
        }

        ChannelTypeUID channelTypeUID = generateChannelTypeUID(details);
        ChannelType channelType = getOrGenerate(channelTypeUID, details);
        if (channelType == null) {
            return channels;
        }

        ChannelBuilder builder = ChannelBuilder.create(channelUID, details.itemType).withLabel(details.label)
                .withConfiguration(newChannelConfiguration).withType(channelType.getUID());

        channels.put(details.id, builder.build());
        return channels;
    }

    private @Nullable ChannelType getOrGenerate(ChannelTypeUID channelTypeUID, ChannelMetadata details) {
        ChannelType channelType = channelTypeProvider.getChannelType(channelTypeUID, null);
        if (channelType == null) {
            channelType = generateChannelType(details);
            if (channelType != null) {
                channelTypeProvider.addChannelType(channelType);
            }
        }
        if (channelType == null) {
            logger.warn("Node {} Channel {}, ChannelType could not be found or generated, please report, this is a bug",
                    details.nodeId, details.id);
            return null;
        }
        return channelType;
    }

    private ChannelTypeUID generateChannelTypeUID(ChannelMetadata details) {
        StringBuilder parts = new StringBuilder();
        parts.append(CHANNEL_TYPE_VERSION);
        parts.append(details.itemType);
        parts.append(details.unitSymbol);
        parts.append(details.writable);
        parts.append(details.isAdvanced);
        StateDescriptionFragment statePattern = details.statePattern;
        if (statePattern != null) {
            parts.append(statePattern.hashCode());
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] array = messageDigest.digest(parts.toString().getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                stringBuilder.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return new ChannelTypeUID(BindingConstants.BINDING_ID, stringBuilder.toString());
        } catch (NoSuchAlgorithmException e) {
            logger.warn("NoSuchAlgorithmException error when calculating MD5 hash");
        }
        return new ChannelTypeUID(BindingConstants.BINDING_ID, "unknown");
    }

    private @Nullable ChannelType generateChannelType(ChannelMetadata details) {
        final ChannelTypeUID channelTypeUID = generateChannelTypeUID(details);
        return generateChannelType(channelTypeUID, details);
    }

    private ChannelType generateChannelType(ChannelTypeUID channelTypeUID, ChannelMetadata details) {
        StateChannelTypeBuilder builder = ChannelTypeBuilder.state(channelTypeUID, details.label, details.itemType);

        if (details.description != null) {
            builder.withDescription(Objects.requireNonNull(details.description));
        }

        if (details.statePattern != null) {
            builder.withStateDescriptionFragment(details.statePattern);
        }

        if (details.unitSymbol != null) {
            builder.withUnitHint(details.unitSymbol);
        }

        if (details.isInvertible()) {
            builder.withConfigDescriptionURI(URI.create("channel-type:zwavejs:invertible-channel"));
        } else {
            builder.withConfigDescriptionURI(URI.create("channel-type:zwavejs:base-channel"));
        }

        return builder.isAdvanced(details.isAdvanced).build();
    }

    private @Nullable URI getConfigDescriptionURI(ThingUID thingUID, Node node) {
        Thing thing = getThing(thingUID);
        if (thing == null) {
            logger.debug("Thing '{}'' not found in registry for getConfigDescriptionURI", thingUID);
            return null;
        }
        ThingUID bridgeUID = thing.getBridgeUID();
        if (bridgeUID == null) {
            logger.debug("No bridgeUID found for Thing '{}'' in getConfigDescriptionURI", thingUID);
            return null;
        }

        try {
            return new URI(String.format("thing:%s:node:%s:node%s", BindingConstants.BINDING_ID, bridgeUID.getId(),
                    node.nodeId));
        } catch (URISyntaxException ex) {
            logger.warn("Can't create configDescriptionURI for node {}", node.nodeId);
            return null;
        }
    }
}
