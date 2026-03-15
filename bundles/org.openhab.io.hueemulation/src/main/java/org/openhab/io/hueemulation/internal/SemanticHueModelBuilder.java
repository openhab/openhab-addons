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
package org.openhab.io.hueemulation.internal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.SemanticTags;
import org.openhab.core.semantics.SemanticsPredicates;
import org.openhab.core.semantics.Tag;
import org.openhab.core.semantics.model.DefaultSemanticTags;
import org.openhab.core.semantics.model.DefaultSemanticTags.Equipment;
import org.openhab.core.semantics.model.DefaultSemanticTags.Location;
import org.openhab.core.semantics.model.DefaultSemanticTags.Point;
import org.openhab.core.semantics.model.DefaultSemanticTags.Property;
import org.openhab.io.hueemulation.internal.dto.HueGroupEntry;
import org.openhab.io.hueemulation.internal.dto.HueLightEntry;

/**
 * Builds a Hue-compatible model based on openHAB's semantic model.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SemanticHueModelBuilder {

    private static final Map<SemanticTag, String> LOCATION_TO_ROOM_CLASS = Map.ofEntries( //
            Map.entry(Location.LIVING_ROOM, "Living room"), //
            Map.entry(Location.KITCHEN, "Kitchen"), //
            Map.entry(Location.DINING_ROOM, "Dining"), //
            Map.entry(Location.BEDROOM, "Bedroom"), //
            Map.entry(Location.BATHROOM, "Bathroom"), //
            Map.entry(Location.OFFICE, "Office"), //
            Map.entry(Location.CORRIDOR, "Hallway"), //
            Map.entry(Location.GARAGE, "Garage"), //
            Map.entry(Location.TERRACE, "Terrace"), //
            Map.entry(Location.GARDEN, "Garden"), //
            Map.entry(Location.DRIVEWAY, "Driveway"), //
            Map.entry(Location.CARPORT, "Carport"), //
            Map.entry(Location.ATTIC, "Attic"), //
            Map.entry(Location.GUEST_ROOM, "Guest room"), //
            Map.entry(Location.LAUNDRY_ROOM, "Laundry room"), //
            Map.entry(Location.PORCH, "Porch")); //

    private final ItemRegistry itemRegistry;
    private final ConfigStore configStore;

    private final List<HueLightEntry> lights = new ArrayList<>();
    private final List<HueGroupEntry> groups = new ArrayList<>();

    public SemanticHueModelBuilder(ItemRegistry itemRegistry, ConfigStore configStore) {
        this.itemRegistry = itemRegistry;
        this.configStore = configStore;
    }

    public void build() {
        Set<GroupItem> lightSources = itemRegistry.getItems().stream().filter(GroupItem.class::isInstance)
                .map(GroupItem.class::cast).filter(e -> isSemanticItem(e, Equipment.LIGHT_SOURCE))
                .collect(Collectors.toSet());

        Map<GroupItem, List<HueLightEntry>> lightSourceToLights = new HashMap<>();

        for (GroupItem lightSource : lightSources) {
            List<HueLightEntry> lightEntries = createLights(lightSource);
            if (!lightEntries.isEmpty()) {
                lights.addAll(lightEntries);
                lightSourceToLights.put(lightSource, lightEntries);
            }
        }

        Map<GroupItem, List<HueLightEntry>> locationToLights = new HashMap<>();

        for (Map.Entry<GroupItem, List<HueLightEntry>> entry : lightSourceToLights.entrySet()) {
            GroupItem lightSource = entry.getKey();
            GroupItem location = getSemanticGroupItem(lightSource, DefaultSemanticTags.LOCATION);
            if (location == null) {
                continue;
            }

            Objects.requireNonNull(locationToLights.computeIfAbsent(location, l -> new ArrayList<>()))
                    .addAll(entry.getValue());
        }

        for (Map.Entry<GroupItem, List<HueLightEntry>> entry : locationToLights.entrySet()) {
            GroupItem location = entry.getKey();
            List<HueLightEntry> locationLights = entry.getValue();

            DeviceType groupType = determineGroupDeviceType(locationLights);

            HueGroupEntry group = createHueGroup(location, groupType);
            group.lights = locationLights.stream().map(light -> configStore.mapItemUIDtoHueID(light.item)).toList();

            groups.add(group);
        }
    }

    public List<HueLightEntry> getLights() {
        return lights;
    }

    public List<HueGroupEntry> getGroups() {
        return groups;
    }

    private HueGroupEntry createHueGroup(GroupItem location, DeviceType deviceType) {
        String name = location.getLabel();
        if (name == null) {
            name = location.getName();
        }

        HueGroupEntry groupEntry = new HueGroupEntry(name, location, deviceType);
        groupEntry.type = HueGroupEntry.TypeEnum.Room.name();
        groupEntry.roomclass = getRoomClass(location);
        return groupEntry;
    }

    private List<HueLightEntry> createLights(GroupItem lightSource) {
        List<HueLightEntry> lightEntries = new ArrayList<>();

        for (Item member : lightSource.getMembers()) {
            if (!(member instanceof GenericItem genericItem)) {
                continue;
            }
            DeviceType targetType = determineTargetType(configStore, member);

            if (targetType == null) {
                continue;
            }

            String hueID = configStore.mapItemUIDtoHueID(member);
            String name = lightSource.getLabel();

            lightEntries.add(new HueLightEntry(genericItem, configStore.getHueUniqueId(hueID), targetType,
                    name != null ? name : ""));
        }

        return lightEntries;
    }

    private String getRoomClass(GroupItem location) {
        for (Map.Entry<SemanticTag, String> entry : LOCATION_TO_ROOM_CLASS.entrySet()) {
            Class<? extends Tag> tagClass = SemanticTags.getById(entry.getKey().getUID());
            if (tagClass != null && SemanticsPredicates.isA(tagClass).test(location)) {
                return entry.getValue();
            }
        }
        return HueGroupEntry.DEFAULT_ROOM_CLASS;
    }

    private static boolean isSemanticItem(Item item, SemanticTag tag) {
        Class<? extends Tag> semanticTag = SemanticTags.getById(tag.getUID());

        return semanticTag != null && SemanticsPredicates.isA(semanticTag).test(item);
    }

    private @Nullable GroupItem getSemanticGroupItem(Item item, SemanticTag tag) {
        return SemanticUtils.getSemanticGroupItem(itemRegistry, item, tag);
    }

    private @Nullable DeviceType determineTargetType(ConfigStore cs, Item item) {
        Set<String> tags = item.getTags();

        // The user wants this item to be not exposed
        if (cs.ignoreItemsFilter.stream().anyMatch(tags::contains)) {
            return null;
        }

        DeviceType deviceType = null;

        switch (item.getType()) {
            case CoreItemFactory.COLOR:
                if (item.hasTag(Point.CONTROL.getName()) && item.hasTag(Property.COLOR.getName())) {
                    deviceType = DeviceType.ColorType;
                    break;
                }
                // Fall through to Dimmer-level behavior

            case CoreItemFactory.DIMMER:
                if (item.hasTag(Point.CONTROL.getName()) && item.hasTag(Property.BRIGHTNESS.getName())) {
                    deviceType = DeviceType.WhiteTemperatureType;
                    break;
                }
                // Fall through to Switch-level behavior

            case CoreItemFactory.SWITCH:
                if (item.hasTag(Point.SWITCH.getName()) && item.hasTag(Property.POWER.getName())) {
                    deviceType = DeviceType.SwitchType;
                    break;
                }
                break;
        }

        if (deviceType == null) {
            return null;
        }

        return hasSemanticGroupItem(item, Equipment.LIGHT_SOURCE) ? deviceType : null;
    }

    private boolean hasSemanticGroupItem(Item item, SemanticTag tag) {
        Class<? extends Tag> semanticRootTag = SemanticTags.getById(tag.getUID());

        return semanticRootTag != null && item.getGroupNames().stream().map(itemRegistry::get).filter(Objects::nonNull)
                .filter(GroupItem.class::isInstance).map(GroupItem.class::cast)
                .anyMatch(SemanticsPredicates.isA(semanticRootTag));
    }

    private static DeviceType determineGroupDeviceType(List<HueLightEntry> lights) {
        return Objects.requireNonNull(lights.stream().map(light -> light.deviceType)
                .min(Comparator.comparingInt(SemanticHueModelBuilder::deviceTypeRank)).orElse(DeviceType.SwitchType));
    }

    private static int deviceTypeRank(DeviceType type) {
        return switch (type) {
            case SwitchType -> 0;
            case WhiteType -> 1;
            case WhiteTemperatureType -> 2;
            case ColorType -> 3;
        };
    }
}
