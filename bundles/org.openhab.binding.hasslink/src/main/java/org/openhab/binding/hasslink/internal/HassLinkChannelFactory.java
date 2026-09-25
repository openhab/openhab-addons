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
package org.openhab.binding.hasslink.internal;

import static org.openhab.binding.hasslink.internal.HassLinkBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityId;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.binding.hasslink.internal.util.EntityUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single source of truth for deducing channel IDs and constructing openHAB Channel objects.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HassLinkChannelFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HassLinkChannelFactory.class);

    public static final String EVENT_CHANNEL_TYPE_ID = "event";

    public static final String CONFIG_AUTO_CREATED = "autoCreated";
    public static final String CONFIG_ENTITY_ID = "entityId";
    public static final String CONFIG_ATTRIBUTE = "attribute";
    public static final String CONFIG_AUTOUPDATE = "autoupdate";

    public static List<Channel> buildChannels(List<EntityState> entityStates, ThingUID thingUID,
            @Nullable String thingLabel, HassLinkBridgeHandler bridgeHandler) {

        // Sort entity states by objectId (exclude the domain prefix)
        List<EntityState> sortedEntityStates = entityStates.stream() //
                .sorted(Comparator.comparing(EntityState::getObjectId, String.CASE_INSENSITIVE_ORDER)).toList();

        CommonPrefix commonPrefix = CommonPrefix.from(sortedEntityStates, thingLabel);
        List<Channel> channels = new ArrayList<>();
        Set<String> knownChannelIds = new HashSet<>();
        EntityContext context = new EntityContext(bridgeHandler, attr -> true, null);

        for (EntityState entity : sortedEntityStates) {
            buildChannelsForEntity(entity, thingUID, commonPrefix, bridgeHandler, context, channels, knownChannelIds);
        }

        return disambiguateDuplicateLabels(channels);
    }

    private static void buildChannelsForEntity(EntityState entity, ThingUID thingUID, CommonPrefix commonPrefix,
            HassLinkBridgeHandler bridgeHandler, EntityContext context, List<Channel> channels,
            Set<String> knownChannelIds) {

        String baseChannelId = computeBaseChannelId(entity.entityId(), commonPrefix);

        EntityType entityType = bridgeHandler.getEntityType(entity);
        List<ChannelSpec> channelSpecs = entityType.getChannelSpecs(entity, context);

        for (ChannelSpec spec : channelSpecs) {
            Channel channel = buildSingleChannel(spec, thingUID, entity, entityType, baseChannelId, commonPrefix,
                    knownChannelIds);
            if (channel != null) {
                channels.add(channel);
            }
        }
    }

    private static @Nullable Channel buildSingleChannel(ChannelSpec spec, ThingUID thingUID, EntityState entity,
            EntityType entityType, String baseChannelId, CommonPrefix commonPrefix, Set<String> knownChannelIds) {

        String attribute = spec.attribute();
        boolean isPrimaryChannel = EntityType.isPrimary(attribute);
        String channelId = isPrimaryChannel ? baseChannelId
                : baseChannelId + ChannelUID.CHANNEL_GROUP_SEPARATOR + attribute;

        if (knownChannelIds.contains(channelId)) {
            LOGGER.warn("Duplicate channel ID '{}' for entity '{}', skipping channel creation.", channelId,
                    entity.entityId());
            return null;
        }

        Configuration config = new Configuration();
        config.put(CONFIG_ENTITY_ID, entity.entityId());
        config.put(CONFIG_AUTO_CREATED, true);
        if (!isPrimaryChannel) {
            config.put(CONFIG_ATTRIBUTE, attribute);
        }

        String channelLabel = inferChannelLabel(entity, entityType, attribute, spec.label(), commonPrefix);

        boolean isTrigger = spec.kind() == ChannelKind.TRIGGER;
        ChannelTypeUID channelTypeUID = isTrigger ? new ChannelTypeUID(BINDING_ID, EVENT_CHANNEL_TYPE_ID)
                : new ChannelTypeUID(BINDING_ID, spec.itemType().channelTypeId());

        String acceptedItemType = isTrigger ? null : spec.itemType().name();

        ChannelBuilder channelBuilder = ChannelBuilder.create(new ChannelUID(thingUID, channelId), acceptedItemType)
                .withType(channelTypeUID) //
                .withKind(spec.kind()) //
                .withLabel(channelLabel) //
                .withAutoUpdatePolicy(spec.autoUpdatePolicy()) //
                .withConfiguration(config);

        String description = spec.description();
        if (description != null && !description.isBlank()) {
            channelBuilder.withDescription(description);
        }

        if (!spec.defaultTags().isEmpty()) {
            channelBuilder.withDefaultTags(spec.defaultTags());
        }

        Channel channel = channelBuilder.build();
        knownChannelIds.add(channelId);

        return channel;
    }

    private static String inferChannelLabel(EntityState entity, EntityType entityType, @Nullable String attribute,
            @Nullable String customLabel, CommonPrefix commonPrefix) {

        EntityId entityId = EntityUtils.parseEntityId(entity.entityId());
        if (entityId == null) {
            return "";
        }

        String domainLabel = capitalize(entityId.domain().replace("_", " "));
        String friendlyName = entity.getAttributeAsString("friendly_name");
        String rawName = (friendlyName != null && !friendlyName.isBlank()) ? friendlyName
                : entityId.objectId().replace("_", " ");

        String strippedObjId = capitalize(stripPrefixes(rawName, commonPrefix.candidates()));

        // 1. Build Base Part
        String basePart;
        if (strippedObjId.isBlank() || strippedObjId.equalsIgnoreCase(domainLabel)) {
            basePart = domainLabel;
        } else if (strippedObjId.toLowerCase().endsWith(domainLabel.toLowerCase())) {
            basePart = strippedObjId;
        } else if (entityType.appendsDomainToLabel()) {
            // Appends domain for control entities, images, actuators, etc. (e.g. "Force Refresh Button", "Camera
            // Switch")
            basePart = strippedObjId + " " + domainLabel;
        } else {
            // Telemetry/Sensors/Setpoints keep just the property name (e.g. "Current Layer", "Printing Speed")
            basePart = strippedObjId;
        }

        // 2. Build Attribute Part
        boolean isPrimary = EntityType.isPrimary(attribute);
        if (isPrimary) {
            return basePart;
        }

        String attrLabel = (customLabel != null && !customLabel.isBlank()) ? customLabel
                : formatAttributeLabel(attribute != null ? attribute : "");

        if (attrLabel.isBlank()) {
            return basePart;
        }

        // Avoid repetitions (e.g. "Camera" + "Camera Control" -> "Camera Control")
        if (attrLabel.toLowerCase().startsWith(basePart.toLowerCase())) {
            return capitalize(attrLabel);
        }
        if (basePart.toLowerCase().endsWith(attrLabel.toLowerCase())) {
            return basePart;
        }

        return basePart + " " + capitalize(attrLabel);
    }

    private static String formatAttributeLabel(String attribute) {
        return attribute.replaceAll("[_#]+", " ").replaceAll("\\s+", " ").trim();
    }

    private static List<Channel> disambiguateDuplicateLabels(List<Channel> channels) {
        Map<String, List<Channel>> labelGroups = channels.stream().collect(Collectors.groupingBy(c -> {
            String label = c.getLabel();
            return label != null ? label : "";
        }, LinkedHashMap::new, Collectors.toList()));

        List<Channel> result = new ArrayList<>();

        for (Map.Entry<String, List<Channel>> entry : labelGroups.entrySet()) {
            List<Channel> group = entry.getValue();
            if (group.size() == 1) {
                result.add(group.get(0));
                continue;
            }

            String baseLabel = entry.getKey();

            // Count primary state channels in this colliding group
            long primaryCount = group.stream().filter(c -> {
                Object attrConfig = c.getConfiguration().get(CONFIG_ATTRIBUTE);
                String attribute = attrConfig != null ? String.valueOf(attrConfig) : null;
                return EntityType.isPrimary(attribute);
            }).count();

            for (Channel channel : group) {
                Object attrConfig = channel.getConfiguration().get(CONFIG_ATTRIBUTE);
                String attribute = attrConfig != null ? String.valueOf(attrConfig) : null;
                boolean isPrimary = EntityType.isPrimary(attribute);

                String newLabel;
                if (!isPrimary && attribute != null) {
                    // Attribute channel: append formatted attribute name
                    newLabel = baseLabel + " (" + capitalize(formatAttributeLabel(attribute)) + ")";
                } else if (primaryCount > 1) {
                    // Multiple primary channels collided across entities: append domain qualifier
                    String entityIdStr = (String) channel.getConfiguration().get(CONFIG_ENTITY_ID);
                    EntityId entityId = entityIdStr != null ? EntityUtils.parseEntityId(entityIdStr) : null;
                    String qualifier = (entityId != null) ? capitalize(entityId.domain()) : "Channel";
                    newLabel = baseLabel + " (" + qualifier + ")";
                } else {
                    // Single primary channel colliding with its own attribute(s): keep clean base label
                    newLabel = baseLabel;
                }

                ChannelBuilder builder = ChannelBuilder.create(channel.getUID(), channel.getAcceptedItemType())
                        .withType(channel.getChannelTypeUID()).withKind(channel.getKind()).withLabel(newLabel)
                        .withConfiguration(channel.getConfiguration());

                String description = channel.getDescription();
                if (description != null && !description.isBlank()) {
                    builder.withDescription(description);
                }

                if (!channel.getDefaultTags().isEmpty()) {
                    builder.withDefaultTags(channel.getDefaultTags());
                }

                result.add(builder.build());
            }
        }

        return result;
    }

    private static String computeBaseChannelId(String rawEntityId, CommonPrefix commonPrefix) {
        EntityId entityId = EntityUtils.parseEntityId(rawEntityId);
        if (entityId == null) {
            return "";
        }

        String rawChannelId = stripPrefixes(entityId.objectId(), commonPrefix.candidates());
        if (rawChannelId.isBlank()) {
            rawChannelId = entityId.objectId();
        }
        String sanitizedChannelId = EntityUtils.sanitize(rawChannelId);

        return entityId.domain() + "-" + sanitizedChannelId;
    }

    private static String stripPrefixes(String text, List<String> candidatePrefixes) {
        String current = text.trim();

        List<String> sorted = candidatePrefixes.stream().filter(p -> !p.isBlank())
                .sorted(Comparator.comparingInt(String::length).reversed()).toList();

        for (String prefix : sorted) {
            current = stripSinglePrefix(current, prefix);
        }

        return current;
    }

    private static String stripSinglePrefix(String text, String prefix) {
        String[] words = prefix.trim().split("[\\s_]+");
        if (words.length == 0 || words[0].isBlank()) {
            return text;
        }

        String prefixPattern = String.join("[\\s_]+", Arrays.stream(words).map(Pattern::quote).toList());
        String regex = "(?i)^" + prefixPattern + "[\\s_#-]*";

        return text.replaceAll(regex, "").trim();
    }

    private static String capitalize(String text) {
        String cap = StringUtils.capitalizeByWhitespace(text);
        return cap != null ? cap : text;
    }

    private record CommonPrefix(String objectIdPrefix, String friendlyNamePrefix, List<String> candidates) {
        static CommonPrefix from(List<EntityState> entityStates, @Nullable String thingLabel) {
            List<String> objectIds = entityStates.stream().map(EntityState::getObjectId).toList();
            List<String> friendlyNames = entityStates.stream().map(e -> e.getAttributeAsString("friendly_name"))
                    .filter(Objects::nonNull).toList();

            String objectIdPrefix = EntityUtils.calculateCommonPrefix(objectIds, '_');
            String friendlyNamePrefix = findFriendlyNamePrefix(friendlyNames);

            List<String> candidates = new ArrayList<>();
            if (thingLabel != null && !thingLabel.isBlank()) {
                candidates.add(thingLabel.trim());
            }
            if (!friendlyNamePrefix.isBlank()) {
                candidates.add(friendlyNamePrefix.trim());
            }
            if (!objectIdPrefix.isBlank()) {
                candidates.add(objectIdPrefix.replace("_", " ").trim());
            }

            return new CommonPrefix(objectIdPrefix, friendlyNamePrefix, candidates.stream().distinct().toList());
        }

        private static String findFriendlyNamePrefix(List<String> friendlyNames) {
            if (friendlyNames.isEmpty()) {
                return "";
            }
            String strict = EntityUtils.calculateCommonPrefix(friendlyNames, ' ');
            if (!strict.isBlank()) {
                return strict;
            }
            Map<String, Integer> counts = new HashMap<>();
            for (String name : friendlyNames) {
                String[] words = name.trim().split("\\s+");
                if (words.length >= 2) {
                    String prefix = words[0] + " " + words[1];
                    counts.put(prefix, counts.getOrDefault(prefix, 0) + 1);
                }
            }
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > friendlyNames.size() / 2) {
                    return entry.getKey();
                }
            }
            return "";
        }
    }
}
