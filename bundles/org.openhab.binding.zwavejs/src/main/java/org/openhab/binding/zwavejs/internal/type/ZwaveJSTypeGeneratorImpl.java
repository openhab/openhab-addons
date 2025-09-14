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

import static org.openhab.binding.zwavejs.internal.BindingConstants.*;
import static org.openhab.binding.zwavejs.internal.CommandClassConstants.COMMAND_CLASS_ALARM;
import static org.openhab.binding.zwavejs.internal.CommandClassConstants.COMMAND_CLASS_DOOR_LOCK;
import static org.openhab.binding.zwavejs.internal.CommandClassConstants.COMMAND_CLASS_SWITCH_COLOR;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.api.dto.Metadata;
import org.openhab.binding.zwavejs.internal.api.dto.MetadataType;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.binding.zwavejs.internal.config.ZwaveJSChannelConfiguration;
import org.openhab.binding.zwavejs.internal.conversion.ChannelMetadata;
import org.openhab.binding.zwavejs.internal.conversion.ConfigMetadata;
import org.openhab.binding.zwavejs.internal.type.capabilities.ColorCapability;
import org.openhab.binding.zwavejs.internal.type.capabilities.RollerShutterCapability;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Point;
import org.openhab.core.semantics.model.DefaultSemanticTags.Property;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
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
    private static final Object CHANNEL_TYPE_VERSION = "6"; // when static configuration is changed, the version must be
                                                            // changed as well to force new channel type generation
    private static final Map<String, SemanticTag> ITEM_TYPES_TO_PROPERTY_TAGS = new HashMap<>();
    static {
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:ElectricCurrent", Property.CURRENT);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:ElectricPotential", Property.VOLTAGE);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Energy", Property.ENERGY);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Frequency", Property.FREQUENCY);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Illuminance", Property.ILLUMINANCE);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Power", Property.POWER);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Pressure", Property.PRESSURE);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Speed", Property.SPEED);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Temperature", Property.TEMPERATURE);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Time", Property.DURATION);
    }

    private static final List<String> ROLLER_SHUTTER_KEYWORDS = List.of("shutter", "blind", "curtain", "shade",
            "awning", "venetian", "drape", "roller", "screen", "covering", "rts");

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

    /**
     * Retrieves a Thing by its UID.
     *
     * @param thingUID the UID of the Thing
     * @return the Thing, or {@code null} if not found
     */
    public @Nullable Thing getThing(ThingUID thingUID) {
        return thingRegistry.get(thingUID);
    }

    /**
     * Generates a ZwaveJSTypeGeneratorResult for the given ThingUID and Node.
     *
     * @param thingUID the ThingUID of the device
     * @param node the Node containing the values to be processed
     * @param configurationAsChannels flag indicating whether to treat configuration as channels
     * @return a ZwaveJSTypeGeneratorResult containing the generated channels and configuration descriptions
     */
    @Override
    public ZwaveJSTypeGeneratorResult generate(ThingUID thingUID, Node node, boolean configurationAsChannels) {
        ZwaveJSTypeGeneratorResult result = new ZwaveJSTypeGeneratorResult();
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<>();
        URI uri = Objects.requireNonNull(getConfigDescriptionURI(thingUID, node));

        for (Value value : node.values) {
            if (!configurationAsChannels && CONFIGURATION_COMMAND_CLASSES.contains(value.commandClass)) {
                ConfigMetadata metadata = new ConfigMetadata(node.nodeId, value);
                configDescriptions.add(createConfigDescription(metadata));
                if (!result.values.containsKey(metadata.id) && value.value != null) {
                    result.values.put(metadata.id, value.value);
                }
            }
            ChannelMetadata metadata = new ChannelMetadata(node.nodeId, value);
            if (configurationAsChannels || !CONFIGURATION_COMMAND_CLASSES.contains(value.commandClass)) {
                result.channels = createChannel(thingUID, result, metadata, configDescriptionProvider);
                if (!metadata.isIgnoredCommandClass(value.commandClassName) && !result.values.containsKey(metadata.id)
                        && value.value != null) {
                    result.values.put(metadata.id, value.value);
                }
            }
        }

        // Skip adding RollerShutter channels for devices with color capabilities.
        // This prevents creating unnecessary RollerShutter channels for each color.
        if (result.colorCapabilities.isEmpty()) {
            // Map roller shutter capabilities based on node label and description
            mapRollerShutterCapabilities(result, node.label,
                    node.deviceConfig != null ? node.deviceConfig.description : null);

            // Mark roller shutter-related channels as advanced
            result.rollerShutterCapabilities.values().forEach(cap -> {
                result.channels.computeIfPresent(cap.dimmerChannel.getId(), (id, channel) -> markAdvanced(channel));
                result.channels.computeIfPresent(cap.upChannel.getId(), (id, channel) -> markAdvanced(channel));
                result.channels.computeIfPresent(cap.downChannel.getId(), (id, channel) -> markAdvanced(channel));
            });

            // Add roller shutter channels to the result
            addRollerShutterChannels(thingUID, node, result);
        }

        // cross link the ColorCapability dimmer channels to Dimmer type channels withing the same endpoint
        mapDimmerChannelsToColorCapabilities(result);

        // add a color temperature channel if necessary
        addColorTemperatureChannel(thingUID, node, result);

        // add raw notification channel if necessary
        addRawNotificationChannel(thingUID, node, result);

        logger.debug("Node {}. Generated {} channels and {} configDescriptions with URI {}", node.nodeId,
                result.channels.size(), configDescriptions.size(), uri);

        configDescriptionProvider
                .addConfigDescription(ConfigDescriptionBuilder.create(uri).withParameters(configDescriptions).build());

        return result;
    }

    private void addRawNotificationChannel(ThingUID thingUID, Node node, ZwaveJSTypeGeneratorResult result) {
        // loop all channels to find endpoints with a notification CC or a door lock CC
        Map<Integer, List<Integer>> grouped = result.channels.values().stream()
                .map(channel -> channel.getConfiguration().as(ZwaveJSChannelConfiguration.class))
                .filter(config -> COMMAND_CLASS_ALARM == config.commandClassId
                        || COMMAND_CLASS_DOOR_LOCK == config.commandClassId)
                .collect(Collectors.groupingBy(config -> config.endpoint,
                        Collectors.mapping(config -> config.commandClassId, Collectors.toList())));

        // 2. Find endpoints with both Notification and Door Lock CCs
        List<Integer> endpoints = grouped.entrySet().stream()
                .filter(entry -> entry.getValue().contains(COMMAND_CLASS_ALARM)
                        && entry.getValue().contains(COMMAND_CLASS_DOOR_LOCK))
                .map(Map.Entry::getKey).toList();

        // 2. Extract channel creation logic to a helper
        for (Integer endpoint : endpoints) {
            createRawNotificationChannel(thingUID, node, result, endpoint);
        }
    }

    private void createRawNotificationChannel(ThingUID thingUID, Node node, ZwaveJSTypeGeneratorResult result,
            int endpoint) {
        Value value = new Value();
        value.endpoint = endpoint;
        value.commandClass = COMMAND_CLASS_ALARM;
        value.commandClassName = VIRTUAL_COMMAND_CLASS_NOTIFICATION;
        value.propertyKey = VIRTUAL_NOTIFICATION_PROPERTY;
        value.property = VIRTUAL_NOTIFICATION_PROPERTY;
        value.metadata = new Metadata();
        value.metadata.type = MetadataType.STRING;
        value.metadata.writeable = false;
        value.metadata.label = "Raw Notification";
        value.metadata.description = "Notification channel that updates on alarm events";
        value.metadata.readable = true;

        ChannelMetadata details = new ChannelMetadata(node.nodeId, value);
        if (!result.channels.containsKey(details.id)) {
            logger.trace("Node {} building channel with Id: {}", details.nodeId, details.id);
            logger.trace(" >> {}", details);

            ChannelTypeUID channelTypeUID = generateChannelTypeUID(details);
            ChannelType type = getOrGenerate(channelTypeUID, details);
            if (type == null) {
                return;
            }
            Configuration config = buildChannelConfiguration(details);

            Channel channel = ChannelBuilder.create(new ChannelUID(thingUID, details.id), CoreItemFactory.STRING)
                    .withType(type.getUID()) //
                    .withDefaultTags(type.getTags()) //
                    .withKind(type.getKind()) //
                    .withLabel(details.label) //
                    .withDescription(Objects.requireNonNull(details.description)) //
                    .withAutoUpdatePolicy(type.getAutoUpdatePolicy()) //
                    .withConfiguration(config) //
                    .build();

            result.channels.put(details.id, channel);
        }
    }

    private ConfigDescriptionParameter createConfigDescription(ConfigMetadata details) {
        logger.trace("Node {}. createConfigDescriptions with Id: {}", details.nodeId, details.id);

        ConfigDescriptionParameterBuilder parameterBuilder = ConfigDescriptionParameterBuilder
                .create(details.id, details.configType) //
                .withRequired(false) //
                .withLabel(details.label) //
                .withVerify(false) //
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

    private Channel markAdvanced(Channel channel) {
        ChannelType originalChannelType = Objects.requireNonNull(
                channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()), null),
                "Original ChannelType must not be null");

        ChannelTypeUID advancedChannelTypeUID = new ChannelTypeUID(originalChannelType.getUID().getBindingId(),
                originalChannelType.getUID().getId() + "_advanced");

        ChannelType channelType = channelTypeProvider.getChannelType(advancedChannelTypeUID, null);
        if (channelType == null) {
            StateChannelTypeBuilder builder = ChannelTypeBuilder.state(advancedChannelTypeUID,
                    originalChannelType.getLabel(),
                    Objects.requireNonNull(originalChannelType.getItemType(), "ItemType must not be null"));
            if (originalChannelType.getDescription() instanceof String description) {
                builder.withDescription(description);
            }
            if (originalChannelType.getState() instanceof StateDescription stateDescription) {
                builder.withStateDescriptionFragment(StateDescriptionFragmentBuilder.create(stateDescription).build());
            }
            if (originalChannelType.getUnitHint() != null) {
                builder.withUnitHint(originalChannelType.getUnitHint());
            }
            if (originalChannelType.getConfigDescriptionURI() instanceof URI uri) {
                builder.withConfigDescriptionURI(uri);
            }
            builder.withTags(originalChannelType.getTags());
            channelType = builder.isAdvanced(true).build();

            channelTypeProvider.addChannelType(channelType);
        }
        return ChannelBuilder.create(channel).withType(advancedChannelTypeUID).build();
    }

    private Map<String, Channel> createChannel(ThingUID thingUID, ZwaveJSTypeGeneratorResult result,
            ChannelMetadata details, ZwaveJSConfigDescriptionProvider configDescriptionProvider) {
        if (details.isIgnoredCommandClass(details.commandClassName)) {
            logger.trace("Node {}. Ignoring channel with Id: {} (ignored command class)", details.nodeId, details.id);
            return result.channels;
        }
        logger.trace("Node {}. building channel with Id: {}", details.nodeId, details.id);
        logger.trace(" >> {}", details);

        ChannelUID channelUID = new ChannelUID(thingUID, details.id);
        Configuration channelConfiguration = buildChannelConfiguration(details);

        // Try to reuse or update an existing channel
        Channel existingChannel = result.channels.get(channelUID.getId());
        ChannelTypeUID channelTypeUID = generateChannelTypeUID(details);
        ChannelType channelType = getOrGenerate(channelTypeUID, details);

        String label = details.label;
        String itemType = details.itemType;
        if (existingChannel != null) {
            // Update configuration and label if needed
            Configuration existingConfig = existingChannel.getConfiguration();
            updateReadWriteProperties(existingConfig, details);

            if (details.writable) {
                label = existingChannel.getLabel() != null ? existingChannel.getLabel() : label;
            } else {
                // If the channel is not writable, we keep the existing item type
                itemType = existingChannel.getAcceptedItemType();
            }
            // If the channel type UID has changed and the channel is not writable, keep the old type UID
            if (!channelTypeUID.equals(existingChannel.getChannelTypeUID()) && !details.writable) {
                channelTypeUID = existingChannel.getChannelTypeUID();
            }
            channelConfiguration = existingConfig;
            logger.debug("Node {}. Channel {}: existing channel updated", details.nodeId, details.id);
        }

        if (label == null || label.isBlank()) {
            label = "Unknown Channel";
        }

        if (channelType == null) {
            logger.warn("Node {} Channel {}, ChannelType could not be found or generated, please report, this is a bug",
                    details.nodeId, details.id);
            return result.channels;
        }

        // Build the new or updated channel
        ChannelBuilder builder = ChannelBuilder.create(channelUID, itemType).withLabel(label)
                .withConfiguration(channelConfiguration).withType(channelTypeUID);

        if (details.writable) {
            builder.withAcceptedItemType(channelType.getItemType());
        }

        result.channels.put(details.id, builder.build());

        // if necessary add or update the entry in our ZwaveJSTypeGeneratorResult's map of ColorCapabilities
        updateColorCapabilities(thingUID, details, result);

        return result.channels;
    }

    /**
     * Builds the Configuration object for a channel based on ChannelMetadata.
     *
     * @param details the channel metadata
     * @return the Configuration object for the channel
     */
    private Configuration buildChannelConfiguration(ChannelMetadata details) {
        Configuration config = new Configuration();
        config.put(BindingConstants.CONFIG_CHANNEL_INCOMING_UNIT, details.unitSymbol);
        config.put(BindingConstants.CONFIG_CHANNEL_COMMANDCLASS_ID, details.commandClassId);
        config.put(BindingConstants.CONFIG_CHANNEL_COMMANDCLASS_NAME, details.commandClassName);
        config.put(BindingConstants.CONFIG_CHANNEL_ENDPOINT, details.endpoint);
        if (details.propertyKey instanceof Number propertyInteger) {
            config.put(BindingConstants.CONFIG_CHANNEL_PROPERTY_KEY_INT, propertyInteger);
        } else if (details.propertyKey instanceof String propertyString) {
            config.put(BindingConstants.CONFIG_CHANNEL_PROPERTY_KEY_STR, propertyString);
        }
        config.put(BindingConstants.CONFIG_CHANNEL_FACTOR, details.factor);
        config.put(BindingConstants.CONFIG_CHANNEL_INVERTED, false);

        updateReadWriteProperties(config, details);
        return config;
    }

    /**
     * Updates the read/write properties of an existing channel configuration.
     *
     * @param config the Configuration object to update
     * @param details the channel metadata containing the new properties
     */
    private void updateReadWriteProperties(Configuration config, ChannelMetadata details) {
        if (details.writeProperty instanceof Number writePropertyInteger) {
            config.put(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_INT, writePropertyInteger);
        } else if (details.writeProperty instanceof String writePropertyString) {
            config.put(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_STR, writePropertyString);
        }
        if (details.readProperty != null) {
            config.put(BindingConstants.CONFIG_CHANNEL_READ_PROPERTY, String.valueOf(details.readProperty));
        }
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
        parts.append(details.isInvertible());
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

        // Length check makes sure that it is a quantity type, e.g. `Number:xyz`
        if (details.unitSymbol != null && details.itemType.length() > CoreItemFactory.NUMBER.length() + 1) {
            builder.withUnitHint(details.unitSymbol);
        }

        if (details.itemType.equals(CoreItemFactory.ROLLERSHUTTER)) {
            builder.withConfigDescriptionURI(URI.create("channel-type:zwavejs:rollershutter-channel"));
        } else if (details.isInvertible()) {
            builder.withConfigDescriptionURI(URI.create("channel-type:zwavejs:invertible-channel"));
        } else {
            builder.withConfigDescriptionURI(URI.create("channel-type:zwavejs:base-channel"));
        }
        builder = setSemanticTags(builder, details);
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

    /**
     * Matches the given list of keywords against the label and description of the provided
     * {@link ChannelMetadata}. This method delegates the matching logic to the overloaded
     * {@link #match(List, String, String)} method.
     *
     * @param keyWords the list of keywords to match
     * @param metadata the channel metadata containing the label and description to match against
     * @return {@code true} if the keywords match the label or description, {@code false} otherwise
     */
    private static boolean match(List<String> keyWords, ChannelMetadata metadata) {
        return match(keyWords, metadata.label, metadata.description);
    }

    /**
     * Checks if any of the given keywords match the provided label or description.
     * A match is determined if:
     * - The keyword is contained within the label or description (case-insensitive).
     * - The label or description ends with the keyword (ignoring trailing whitespace).
     * - The label or description starts with the keyword (ignoring leading whitespace).
     *
     * @param keyWords A list of keywords to search for.
     * @param label The label to check against the keywords.
     * @param description An optional description to check against the keywords. Can be null.
     * @return {@code true} if any keyword matches the label or description, {@code false} otherwise.
     */
    private static boolean match(List<String> keyWords, @Nullable String label, @Nullable String description) {
        if ((label == null || label.isBlank()) && (description == null || description.isBlank())
                || keyWords.isEmpty()) {
            return false;
        }
        String labelLower = label != null ? label.toLowerCase() : "";
        String descLower = description != null ? description.toLowerCase() : "";
        return keyWords.stream().map(String::toLowerCase)
                .anyMatch(kw -> labelLower.contains(kw) || descLower.contains(kw)
                        || labelLower.endsWith(kw.stripTrailing()) || descLower.endsWith(kw.stripTrailing())
                        || labelLower.startsWith(kw.stripLeading()) || descLower.startsWith(kw.stripLeading()));
    }

    /**
     * Sets the channel's Point and Property tags.
     *
     * @param builder the StateChannelTypeBuilder to update
     * @param details the channel metadata
     * @return the updated StateChannelTypeBuilder
     */
    private StateChannelTypeBuilder setSemanticTags(StateChannelTypeBuilder builder, ChannelMetadata details) {
        SemanticTag point = null;
        SemanticTag property = null;
        switch (details.itemType) {
            case CoreItemFactory.COLOR:
                point = details.writable ? Point.CONTROL : Point.STATUS;
                property = Property.COLOR;
            case CoreItemFactory.DIMMER:
                point = Point.CONTROL;
                break;
            case CoreItemFactory.STRING:
                point = details.writable ? Point.CONTROL : Point.STATUS;
                break;
            case CoreItemFactory.SWITCH:
                point = details.writable ? Point.SWITCH
                        : match(List.of("alarm", "error", "warning", "fault"), details) ? Point.ALARM : Point.STATUS;
                break;
            case CoreItemFactory.CONTACT:
                point = match(List.of("alarm", "error", "warning", "fault"), details) ? Point.ALARM : Point.STATUS;
                property = Property.OPEN_STATE;
                break;
            case CoreItemFactory.DATETIME:
                point = details.writable ? Point.CONTROL : Point.STATUS;
                property = Property.TIMESTAMP;
                break;
            case CoreItemFactory.LOCATION:
                point = details.writable ? Point.CONTROL : Point.STATUS;
                property = Property.GEO_LOCATION;
                break;
            case CoreItemFactory.PLAYER:
                point = Point.CONTROL;
                property = Property.MEDIA_CONTROL;
                break;
            case CoreItemFactory.ROLLERSHUTTER:
                point = Point.CONTROL;
                property = Property.OPEN_LEVEL;
                break;
            case CoreItemFactory.NUMBER:
                point = details.writable ? Point.CONTROL : Point.MEASUREMENT;
                break;
            default:
                // this handles the case of Number:Dimension
                if (details.itemType.startsWith(CoreItemFactory.NUMBER)) {
                    point = details.writable ? Point.SETPOINT : Point.MEASUREMENT;
                    property = ITEM_TYPES_TO_PROPERTY_TAGS.get(details.itemType);
                }
                break;
        }

        // estimate property tag if missing
        if (point != null && property == null) {
            if (Point.SWITCH == point && match(List.of("mode ", "auto", "manual", "enable", "disable", "lock", " day ",
                    "night", "standby", "lock", "away"), details)) {
                property = Property.MODE;
            } else if (Point.STATUS == point && match(List.of("mode ", "auto", "manual", "enable", "disable", "lock",
                    " day ", "night", "standby", "lock", "away"), details)) {
                property = Property.MODE;
            } else if (match(List.of("air"), details) && match(List.of("flow"), details)) {
                property = Property.AIRFLOW;
            } else if (match(List.of("air"), details) && match(List.of("quality"), details)) {
                property = Property.AIR_QUALITY;
            } else if (match(List.of(" nox ", "no2", "no₂"), details)) {
                property = Property.AIR_QUALITY;
            } else if (match(List.of("aqi"), details)) {
                property = Property.AQI;
            } else if (match(List.of("bright", "dimm"), details)) {
                property = Property.BRIGHTNESS;
            } else if (match(List.of(" co ", "(co)", "monoxide"), details)) {
                property = Property.CO;
            } else if (match(List.of("co2", "co₂", "dioxide"), details)) {
                property = Property.CO2;
            } else if (match(List.of("color", "colour"), details) && match(List.of("temp"), details)) {
                property = Property.COLOR_TEMPERATURE;
            } else if (match(List.of("color", "colour"), details)) {
                property = Property.COLOR;
            } else if (match(List.of("kelvin", "mirek", "mired"), details)) {
                property = Property.COLOR_TEMPERATURE;
            } else if (match(List.of("duration"), details)) {
                property = Property.DURATION;
            } else if (match(List.of("energy"), details)) {
                property = Property.ENERGY;
            } else if (match(List.of("fan"), details)) {
                property = Property.SPEED;
            } else if (match(List.of("freq"), details)) {
                property = Property.FREQUENCY;
            } else if (match(List.of("gas"), details)) {
                property = Property.GAS;
            } else if (match(List.of("location"), details)) {
                property = Property.GEO_LOCATION;
            } else if (match(List.of("heat"), details)) {
                property = Property.HEATING;
            } else if (match(List.of("conditioning", "cooling", " ac "), details)) {
                property = Property.AIRCONDITIONING;
            } else if (match(List.of("humidity"), details) && !match(List.of("soil"), details)) {
                property = Property.HUMIDITY;
            } else if (match(List.of("light"), details) && match(List.of("level"), details)) {
                property = Property.ILLUMINANCE;
            } else if (match(List.of("illuminance", " lux "), details)) {
                property = Property.ILLUMINANCE;
            } else if (match(List.of("battery"), details) && match(List.of("low", "alarm"), details)) {
                property = Property.LOW_BATTERY;
            } else if (Point.ALARM == point && match(List.of("battery"), details)) {
                property = Property.LOW_BATTERY;
            } else if (match(List.of("battery"), details)) {
                property = Property.ENERGY;
            } else if (match(List.of("media", "player", "television", " tv ", "receiver"), details)) {
                property = Property.INFO;
            } else if (match(List.of("soil"), details) && match(List.of("humidity"), details)) {
                property = Property.MOISTURE;
            } else if (match(List.of("moisture"), details)) {
                property = Property.MOISTURE;
            } else if (match(List.of("motion", " pir "), details)) {
                property = Property.MOTION;
            } else if (match(List.of("noise"), details)) {
                property = Property.NOISE;
            } else if (match(List.of(" oil "), details)) {
                property = Property.OIL;
            } else if (match(List.of("open", "close", " shut "), details)
                    && (match(List.of("state", "status", "level", "position"), details))) {
                property = Property.OPEN_LEVEL;
            } else if (match(List.of("open", "close", " shut "), details)) {
                property = Property.OPENING;
            } else if (match(List.of("ozone", "o3", "o₃"), details)) {
                property = Property.OZONE;
            } else if (match(List.of("partic", " pm ", "dust"), details)) {
                property = Property.PARTICULATE_MATTER;
            } else if (match(List.of("pollen"), details)) {
                property = Property.POLLEN;
            } else if (match(List.of("position"), details)) {
                property = Property.POSITION;
            } else if (match(List.of("wall"), details) && !match(List.of("socket", "outlet", "plug "), details)) {
                property = Property.LIGHT;
            } else if (match(List.of("light", "luminaire", "lamp", "bulb", " led "), details)) {
                property = Property.LIGHT;
            } else if (match(List.of("power", "on-off", " off ", "on/off", "relay", "outlet", "plug "), details)) {
                property = Property.POWER;
            } else if (match(List.of("presence", " occup"), details)) {
                property = Property.PRESENCE;
            } else if (match(List.of("pressure"), details)) {
                property = Property.PRESSURE;
            } else if (match(List.of("quality "), details)) {
                property = Property.QUALITY_OF_SERVICE;
            } else if (match(List.of("radon"), details)) {
                property = Property.RADON;
            } else if (match(List.of(" rain", "precip"), details)) {
                property = Property.RAIN;
            } else if (match(List.of("rssi"), details)) {
                property = Property.RSSI;
            } else if (match(List.of("signal"), details) && match(List.of("strength"), details)) {
                property = Property.SIGNAL_STRENGTH;
            } else if (match(List.of(" rf ", " dbw "), details)) {
                property = Property.SIGNAL_STRENGTH;
            } else if (match(List.of("smoke", "fire"), details)) {
                property = Property.SMOKE;
            } else if (match(List.of("sound", "audio", "noise", "decibel", " db ", "mute", " dba "), details)) {
                property = Property.SOUND_VOLUME;
            } else if (match(List.of("speed", "velocity"), details)) {
                property = Property.SPEED;
            } else if (match(List.of("tamper"), details)) {
                property = Property.TAMPERED;
            } else if (match(List.of("tilt", "vane", "slat"), details)) {
                property = Property.TILT;
            } else if (match(List.of("stamp "), details)) {
                property = Property.TIMESTAMP;
            } else if (match(List.of("violet", " uv "), details)) {
                property = Property.ULTRAVIOLET;
            } else if (match(List.of("ventilat", " fan "), details)) {
                property = Property.VENTILATION;
            } else if (match(List.of("vibration"), details)) {
                property = Property.VIBRATION;
            } else if (match(List.of("volatile", " voc "), details)) {
                property = Property.VOC;
            } else if (match(List.of("water", "leak", "flood"), details)) {
                property = Property.WATER;
            } else if (match(List.of("wind"), details)) {
                property = Property.WIND;
            } else if (match(List.of("level"), details)) {
                property = Property.LEVEL;
            } else if (Point.CONTROL == point && match(List.of("application", " app "), details)) {
                property = Property.APP;
            } else if (Point.CONTROL == point && match(List.of("channel"), details)) {
                property = Property.CHANNEL;
            }
        }

        if (point != null) {
            if (property == null) {
                builder.withTags(point);
            } else {
                builder.withTags(point, property);
            }
        }
        return builder;
    }

    private void addRollerShutterChannels(ThingUID thingUID, Node node, ZwaveJSTypeGeneratorResult result) {
        result.rollerShutterCapabilities.forEach((endPoint, rollerShutterCapability) -> {
            Value value = new Value();
            // populate minimum required fields; the system channel type provides the rest
            value.endpoint = endPoint;
            value.commandClass = -1;
            value.commandClassName = VIRTUAL_COMMAND_CLASS_ROLLERSHUTTER;
            value.propertyKey = VIRTUAL_ROLLERSHUTTER_PROPERTY;
            value.property = VIRTUAL_ROLLERSHUTTER_PROPERTY;
            value.metadata = new Metadata();
            value.metadata.type = MetadataType.NUMBER;
            value.metadata.writeable = true;
            value.metadata.label = "Roller Shutter";
            value.metadata.description = "Roller Shutter that accepts UP/DOWN/STOP and NUMBER commands";
            value.metadata.readable = true;
            value.value = 0;

            ChannelMetadata details = new ChannelMetadata(node.nodeId, value);

            logger.trace("Node {} building channel with Id: {}", details.nodeId, details.id);
            logger.trace(" >> {}", details);

            ChannelTypeUID channelTypeUID = generateChannelTypeUID(details);
            ChannelType type = getOrGenerate(channelTypeUID, details);
            if (type == null) {
                return;
            }
            Configuration config = buildChannelConfiguration(details);

            Channel channel = ChannelBuilder.create(new ChannelUID(thingUID, details.id), CoreItemFactory.ROLLERSHUTTER)
                    .withType(type.getUID()) //
                    .withDefaultTags(type.getTags()) //
                    .withKind(type.getKind()) //
                    .withLabel(type.getLabel()) //
                    .withDescription(Objects.requireNonNull(type.getDescription())) //
                    .withAutoUpdatePolicy(type.getAutoUpdatePolicy()) //
                    .withConfiguration(config) //
                    .build();

            result.channels.put(details.id, channel);
            Object dimmerValue = result.values.get(rollerShutterCapability.dimmerChannel.getId());
            if (dimmerValue != null) {
                result.values.put(details.id, dimmerValue);
            }
        });
    }

    /**
     * Iterates over the {@link ZwaveJSTypeGeneratorResult}'s map of {@link ColorCapability} to find endpoints which
     * support color temperature, and if found, adds a respective color temperature channel to the
     * {@link ZwaveJSTypeGeneratorResult}'s map of {@link Channel}.
     *
     * @param thingUID the ThingUID
     * @param node the Node
     * @param result the ZwaveJSTypeGeneratorResult that provides the inputs and receives the results
     */
    private void addColorTemperatureChannel(ThingUID thingUID, Node node, ZwaveJSTypeGeneratorResult result) {
        result.colorCapabilities.forEach((endPoint, colorCapability) -> {
            if (colorCapability.colorTempChannel != null
                    || (colorCapability.coldWhiteChannel == null && colorCapability.warmWhiteChannel == null)) {
                return;
            }

            Value value = new Value();
            // populate minimum required fields; the system channel type provides the rest
            value.endpoint = endPoint;
            value.commandClass = COMMAND_CLASS_SWITCH_COLOR;
            value.commandClassName = COLOR_TEMP_CHANNEL_COMMAND_CLASS_NAME;
            value.propertyName = COLOR_TEMP_CHANNEL_PROPERTY_NAME;
            value.metadata = new Metadata();
            value.metadata.type = MetadataType.NUMBER;
            value.metadata.writeable = true;
            value.metadata.readable = true;
            value.value = 0;

            ChannelMetadata details = new ChannelMetadata(node.nodeId, value);
            logger.trace("Node {} building channel with Id: {}", details.nodeId, details.id);
            logger.trace(" >> {}", details);

            ChannelType type = DefaultSystemChannelTypeProvider.SYSTEM_COLOR_TEMPERATURE;
            Configuration config = buildChannelConfiguration(details);

            Channel channel = ChannelBuilder.create(new ChannelUID(thingUID, details.id), type.getItemType())
                    .withType(type.getUID()) //
                    .withDefaultTags(type.getTags()) //
                    .withKind(type.getKind()) //
                    .withLabel(type.getLabel()) //
                    .withDescription(Objects.requireNonNull(type.getDescription())) //
                    .withAutoUpdatePolicy(type.getAutoUpdatePolicy()) //
                    .withConfiguration(config) //
                    .build();

            result.channels.put(details.id, channel);
            colorCapability.colorTempChannel = channel.getUID();
        });
    }

    /**
     * Iterates over the {@link ZwaveJSTypeGeneratorResult}'s map of {@link Channel} to detect RollerShutter
     * capabilities.
     * This method identifies channels of type "Dimmer" and associates them with corresponding "Switch" channels
     * (e.g., "up", "down", "open", "close", "on", "off") within the same endpoint to define roller shutter
     * capabilities.
     *
     * @param result The {@link ZwaveJSTypeGeneratorResult} containing the channels to be processed.
     * @param nodeLabel The label of the node, used for matching keywords related to roller shutters.
     * @param nodeDescription An optional description of the node, used for additional keyword matching.
     */
    private void mapRollerShutterCapabilities(ZwaveJSTypeGeneratorResult result, String nodeLabel,
            @Nullable String nodeDescription) {
        // Categorize channels by endpoint and type
        result.channels.values().stream().filter(c -> CoreItemFactory.DIMMER.equals(c.getAcceptedItemType())) //
                .forEach(channel -> {
                    ZwaveJSChannelConfiguration config = channel.getConfiguration()
                            .as(ZwaveJSChannelConfiguration.class);
                    int endpoint = config.endpoint;
                    // create single array as mutable container for the lambda
                    ChannelUID[] upChannel = new ChannelUID[1];
                    ChannelUID[] downChannel = new ChannelUID[1];
                    // get other channels of type switch within the same endpoint
                    result.channels.values().stream()
                            .filter(otherChannel -> CoreItemFactory.SWITCH.equals(otherChannel.getAcceptedItemType())) //
                            .filter(otherChannel -> {
                                ZwaveJSChannelConfiguration configSwitch = otherChannel.getConfiguration()
                                        .as(ZwaveJSChannelConfiguration.class);
                                return configSwitch.endpoint == endpoint;
                            }).forEach(otherChannel -> {
                                if (upChannel[0] != null && downChannel[0] != null) {
                                    return;
                                }
                                String channelId = otherChannel.getUID().getId().toLowerCase();
                                if ((channelId.contains("-up") || channelId.contains("-open")
                                        || channelId.contains("-on"))
                                        && match(ROLLER_SHUTTER_KEYWORDS, nodeLabel, nodeDescription)) {
                                    upChannel[0] = otherChannel.getUID();
                                } else if ((channelId.contains("-down") || channelId.contains("-close")
                                        || channelId.contains("-off"))
                                        && match(ROLLER_SHUTTER_KEYWORDS, nodeLabel, nodeDescription)) {
                                    downChannel[0] = otherChannel.getUID();
                                }
                            });
                    if (upChannel[0] != null && downChannel[0] != null) {
                        RollerShutterCapability capability = new RollerShutterCapability(endpoint, channel.getUID(),
                                upChannel[0], downChannel[0]);
                        result.rollerShutterCapabilities.put(endpoint, capability);
                    }
                });
    }

    /**
     * Iterates over the {@link ZwaveJSTypeGeneratorResult}'s map of {@link Channel} to find ones with the accepted Item
     * type 'Dimmer' and, if such a channel's endpoint has an entry in the {@link ZwaveJSTypeGeneratorResult}'s map of
     * {@link ColorCapability}, adds that channel to the respective ColorCapability's set of dimmer channels.
     *
     * @param result ZwaveJSTypeGeneratorResult that provides the inputs and receives the results
     */
    private void mapDimmerChannelsToColorCapabilities(ZwaveJSTypeGeneratorResult result) {
        if (!result.colorCapabilities.isEmpty()) {
            result.channels.values().stream() //
                    .filter(c -> CoreItemFactory.DIMMER.equals(c.getAcceptedItemType())) //
                    .forEach(c -> {
                        ZwaveJSChannelConfiguration config = c.getConfiguration().as(ZwaveJSChannelConfiguration.class);
                        if (result.colorCapabilities.get(config.endpoint) instanceof ColorCapability colorCapability) {
                            colorCapability.dimmerChannel = c.getUID();
                        }
                    });
        }
    }

    /**
     * Parses the {@link ChannelMetadata} to determine the {@link ColorCapability}, if any, and updates the given
     * {@link ZwaveJSTypeGeneratorResult}'s colorCapabilities map accordingly.
     *
     * @param thingUID the ThingUID
     * @param details the channel creation metadata
     * @param result the ZwaveJSTypeGeneratorResult that receives the results
     */
    private void updateColorCapabilities(ThingUID thingUID, ChannelMetadata details,
            ZwaveJSTypeGeneratorResult result) {
        if (details.commandClassId != COMMAND_CLASS_SWITCH_COLOR) {
            return;
        }

        boolean isColor = (details.value instanceof Map map && map.containsKey(GREEN)) //
                || details.id.contains(HEX);

        int propertyKey = details.propertyKey instanceof Number n ? n.intValue()
                : details.propertyKey instanceof String s ? Integer.valueOf(s) : -1;

        boolean isColdWhite = propertyKey == COLD_PROPERTY_KEY;
        boolean isWarmWhite = propertyKey == WARM_PROPERTY_KEY;

        if (isColor || isColdWhite || isWarmWhite) {
            ColorCapability colorCapability = result.colorCapabilities.getOrDefault(details.endpoint,
                    new ColorCapability());
            if (isColor) {
                colorCapability.colorChannels.add(new ChannelUID(thingUID, details.id));
            }
            if (isColdWhite) {
                colorCapability.coldWhiteChannel = new ChannelUID(thingUID, details.id);
            }
            if (isWarmWhite) {
                colorCapability.warmWhiteChannel = new ChannelUID(thingUID, details.id);
            }
            result.colorCapabilities.put(details.endpoint, colorCapability);
        }
    }
}
