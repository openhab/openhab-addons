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
package org.openhab.io.mcp.internal.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.semantics.Property;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.SemanticTags;
import org.openhab.core.semantics.SemanticsPredicates;
import org.openhab.core.semantics.Tag;
import org.openhab.core.semantics.model.DefaultSemanticTags;

/**
 * Builds a hierarchical representation of the openHAB semantic model
 * for consumption by AI agents via MCP.
 *
 * The output structure is: Locations -> Equipment -> Points
 *
 * Includes equipment associated with a location via either Group membership
 * or the {@code semantics.config.hasLocation} metadata.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SemanticModelBuilder {

    private static final String SEMANTICS_NAMESPACE = "semantics";
    private static final String HAS_LOCATION = "hasLocation";

    private final ItemRegistry itemRegistry;
    private final @Nullable MetadataRegistry metadataRegistry;

    public SemanticModelBuilder(ItemRegistry itemRegistry, @Nullable MetadataRegistry metadataRegistry) {
        this.itemRegistry = itemRegistry;
        this.metadataRegistry = metadataRegistry;
    }

    /**
     * Build the full semantic model as a map structure.
     *
     * @param includeUntagged whether to include items not in the semantic model
     * @return map representing the semantic model
     */
    public Map<String, Object> buildModel(boolean includeUntagged) {
        Map<String, Object> model = new LinkedHashMap<>();
        List<Map<String, Object>> locations = new ArrayList<>();

        Set<GroupItem> locationItems = itemRegistry.getItems().stream().filter(GroupItem.class::isInstance)
                .map(GroupItem.class::cast).filter(item -> isSemanticType(item, DefaultSemanticTags.LOCATION))
                .collect(Collectors.toSet());

        // Only emit root locations at the top level — sub-locations will appear nested
        // via buildLocation() recursion, avoiding duplicates.
        List<GroupItem> rootLocations = locationItems.stream()
                .filter(loc -> locationItems.stream()
                        .noneMatch(parent -> !parent.equals(loc) && parent.getMembers().contains(loc)))
                .collect(Collectors.toList());

        // Build lookup: locationName -> items that declared hasLocation metadata for it.
        Map<String, List<Item>> hasLocationItems = buildHasLocationIndex(locationItems);

        Set<String> assignedItems = new HashSet<>();
        for (GroupItem location : rootLocations) {
            Map<String, Object> locationMap = buildLocation(location, hasLocationItems, assignedItems);
            locations.add(locationMap);
        }

        model.put("locations", locations);

        if (includeUntagged) {
            List<Map<String, Object>> unassigned = new ArrayList<>();
            for (Item item : itemRegistry.getItems()) {
                if (item instanceof GroupItem) {
                    continue;
                }
                if (!assignedItems.contains(item.getName())) {
                    unassigned.add(buildItemInfo(item));
                }
            }
            if (!unassigned.isEmpty()) {
                model.put("unassignedItems", unassigned);
            }
        }

        return model;
    }

    private Map<String, Object> buildLocation(GroupItem location, Map<String, List<Item>> hasLocationItems,
            Set<String> assignedItems) {
        Map<String, Object> locationMap = new LinkedHashMap<>();
        locationMap.put("name", location.getName());
        locationMap.put("label", Objects.requireNonNullElse(location.getLabel(), location.getName()));
        locationMap.put("type", getSemanticTypeName(location));
        assignedItems.add(location.getName());

        List<Map<String, Object>> equipment = new ArrayList<>();
        List<Map<String, Object>> subLocations = new ArrayList<>();

        // Direct group-members of this location.
        for (Item member : location.getMembers()) {
            if (assignedItems.contains(member.getName())) {
                continue;
            }
            if (member instanceof GroupItem groupMember) {
                if (isSemanticType(groupMember, DefaultSemanticTags.EQUIPMENT)) {
                    assignedItems.add(groupMember.getName());
                    equipment.add(buildEquipment(groupMember, assignedItems));
                } else if (isSemanticType(groupMember, DefaultSemanticTags.LOCATION)) {
                    subLocations.add(buildLocation(groupMember, hasLocationItems, assignedItems));
                }
            } else if (isSemanticType(member, DefaultSemanticTags.EQUIPMENT)) {
                // Single-point equipment — a non-Group item directly tagged as Equipment.
                assignedItems.add(member.getName());
                equipment.add(buildEquipmentFromSinglePoint(member));
            } else if (isSemanticType(member, DefaultSemanticTags.POINT)) {
                // Loose Point directly under a Location (no Equipment wrapper). Surface
                // it as an implicit equipment so the agent can still see and command it.
                assignedItems.add(member.getName());
                equipment.add(buildEquipmentFromSinglePoint(member));
            }
        }

        // Items that reference this Location via hasLocation metadata (not group membership).
        for (Item item : hasLocationItems.getOrDefault(location.getName(), List.of())) {
            if (assignedItems.contains(item.getName())) {
                continue;
            }
            assignedItems.add(item.getName());
            if (item instanceof GroupItem gi && isSemanticType(gi, DefaultSemanticTags.EQUIPMENT)) {
                equipment.add(buildEquipment(gi, assignedItems));
            } else {
                equipment.add(buildEquipmentFromSinglePoint(item));
            }
        }

        if (!equipment.isEmpty()) {
            locationMap.put("equipment", equipment);
        }
        if (!subLocations.isEmpty()) {
            locationMap.put("subLocations", subLocations);
        }

        return locationMap;
    }

    private Map<String, Object> buildEquipment(GroupItem equipmentGroup, Set<String> assignedItems) {
        Map<String, Object> equipmentMap = new LinkedHashMap<>();
        equipmentMap.put("name", equipmentGroup.getName());
        equipmentMap.put("label", Objects.requireNonNullElse(equipmentGroup.getLabel(), equipmentGroup.getName()));
        equipmentMap.put("type", getSemanticTypeName(equipmentGroup));

        List<Map<String, Object>> points = new ArrayList<>();
        for (Item member : equipmentGroup.getMembers()) {
            if (isSemanticType(member, DefaultSemanticTags.POINT)) {
                assignedItems.add(member.getName());
                points.add(buildPoint(member));
            }
        }

        if (!points.isEmpty()) {
            equipmentMap.put("points", points);
        }

        return equipmentMap;
    }

    /**
     * An Equipment can be expressed as a plain non-Group item tagged directly as
     * Equipment (single-function devices like a lightbulb dimmer). Surface the item
     * itself as both the equipment container and its one implicit point so the agent
     * can command it.
     */
    private Map<String, Object> buildEquipmentFromSinglePoint(Item item) {
        Map<String, Object> equipmentMap = new LinkedHashMap<>();
        equipmentMap.put("name", item.getName());
        equipmentMap.put("label", Objects.requireNonNullElse(item.getLabel(), item.getName()));
        equipmentMap.put("type", getSemanticTypeName(item));
        equipmentMap.put("itemType", item.getType());
        equipmentMap.put("state", ItemStateFormatter.formatState(item.getState()));
        String property = getSemanticPropertyName(item);
        if (!property.isEmpty()) {
            equipmentMap.put("property", property);
        }
        // Also include a single synthetic point so traversal code that expects "points"
        // under every equipment still works.
        Map<String, Object> point = buildPoint(item);
        equipmentMap.put("points", List.of(point));
        return equipmentMap;
    }

    private Map<String, Object> buildPoint(Item item) {
        Map<String, Object> pointMap = new LinkedHashMap<>();
        pointMap.put("itemName", item.getName());
        pointMap.put("label", Objects.requireNonNullElse(item.getLabel(), item.getName()));
        pointMap.put("itemType", item.getType());
        pointMap.put("state", ItemStateFormatter.formatState(item.getState()));

        String pointType = getSemanticTypeName(item);
        pointMap.put("type", pointType);

        String property = getSemanticPropertyName(item);
        if (!property.isEmpty()) {
            pointMap.put("property", property);
        }

        return pointMap;
    }

    private Map<String, Object> buildItemInfo(Item item) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("itemName", item.getName());
        info.put("label", Objects.requireNonNullElse(item.getLabel(), item.getName()));
        info.put("itemType", item.getType());
        info.put("state", ItemStateFormatter.formatState(item.getState()));
        if (!item.getTags().isEmpty()) {
            info.put("tags", item.getTags());
        }
        return info;
    }

    /**
     * Scan all items for a {@code semantics.config.hasLocation} metadata entry and index
     * them by the referenced Location item name. Returns an empty map if no
     * MetadataRegistry is available.
     */
    private Map<String, List<Item>> buildHasLocationIndex(Set<GroupItem> locationItems) {
        Map<String, List<Item>> index = new LinkedHashMap<>();
        MetadataRegistry registry = metadataRegistry;
        if (registry == null) {
            return index;
        }
        Set<String> locationNames = locationItems.stream().map(Item::getName).collect(Collectors.toSet());
        for (Item item : itemRegistry.getItems()) {
            Metadata md = registry.get(new MetadataKey(SEMANTICS_NAMESPACE, item.getName()));
            if (md == null) {
                continue;
            }
            Object locName = md.getConfiguration().get(HAS_LOCATION);
            if (locName instanceof String s && locationNames.contains(s)) {
                index.computeIfAbsent(s, k -> new ArrayList<>()).add(item);
            }
        }
        return index;
    }

    private static boolean isSemanticType(Item item, SemanticTag rootTag) {
        Class<? extends Tag> tagClass = SemanticTags.getById(rootTag.getUID());
        return tagClass != null && SemanticsPredicates.isA(tagClass).test(item);
    }

    private static String getSemanticTypeName(Item item) {
        Class<? extends Tag> semType = SemanticTags.getSemanticType(item);
        if (semType != null) {
            return semType.getSimpleName();
        }
        return "Unknown";
    }

    private static String getSemanticPropertyName(Item item) {
        @Nullable
        Class<? extends Property> prop = SemanticTags.getProperty(item);
        if (prop != null) {
            return prop.getSimpleName();
        }
        return "";
    }
}
