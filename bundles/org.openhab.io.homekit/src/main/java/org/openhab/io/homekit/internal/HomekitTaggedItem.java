/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.homekit.internal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps an Item with data derived from supported tags defined.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitTaggedItem {
    class BadItemConfigurationException extends Exception {
        private static final long serialVersionUID = 2199765638404197193L;

        public BadItemConfigurationException(String reason) {
            super(reason);
        }
    }

    private static final Map<Integer, String> CREATED_ACCESSORY_IDS = new ConcurrentHashMap<>();

    /**
     * The type of HomekitDevice we've decided this was. If the item is question is the member of a group which is a
     * HomekitDevice, then this is null.
     */
    private HomekitAccessoryType homekitAccessoryType;
    private HomekitCharacteristicType homekitCharacteristicType;
    private final Item item;
    private Logger logger = LoggerFactory.getLogger(HomekitTaggedItem.class);
    private final int id;
    private GroupItem parentGroupItem;

    private static Stream<String> getPrefixedTags(Item item) {
        Stream<String> prefixedTags = item.getTags().stream();
        if (item instanceof ColorItem) {
            prefixedTags = prefixedTags.map(tag -> "Colorful" + tag);
        } else if (item instanceof DimmerItem) {
            prefixedTags = prefixedTags.map(tag -> "Dimmable" + tag);
        }
        return prefixedTags;
    }

    public static HomekitAccessoryType findAccessoryType(Item item) {
        return getPrefixedTags(item).map(tag -> HomekitAccessoryType.valueOfTag(tag)).filter(Objects::nonNull)
                .findFirst().orElseGet(() -> null);
    }

    public static HomekitCharacteristicType findCharacteristicType(Item item) {
        return getPrefixedTags(item).map(tag -> HomekitCharacteristicType.valueOfTag(tag)).filter(Objects::nonNull)
                .findFirst().orElseGet(() -> null);
    }

    public HomekitTaggedItem(Item item, ItemRegistry itemRegistry) {
        this.item = item;

        try {
            homekitAccessoryType = findAccessoryType(item);
            homekitCharacteristicType = findCharacteristicType(item);
            if (homekitAccessoryType != null && homekitCharacteristicType != null) {
                throw new BadItemConfigurationException(
                        "Items cannot be tagged as both a characteristic and an accessory type");
            }
            List<GroupItem> matchingGroupItems = findMyAccessoryGroups(item, itemRegistry);

            switch (matchingGroupItems.size()) {
                case 0: // Does not belong to a accessory group
                    if (homekitCharacteristicType != null) {
                        throw new BadItemConfigurationException(
                                "Item is tagged as a characteristic, but does not belong to a root accessory group");
                    }

                    parentGroupItem = null;
                    break;
                case 1: // Belongs to exactly one accessory group
                    if (item instanceof GroupItem) {
                        throw new BadItemConfigurationException("Nested Accessory Groups are not supported");
                    }

                    parentGroupItem = matchingGroupItems.get(0);
                    break;
                default: // Belongs to more than one accessory group
                    throw new BadItemConfigurationException(
                            "Item belongs to multiple Groups which are tagged as Homekit devices.");
            }

        } catch (BadItemConfigurationException e) {
            logger.warn("Item {} was misconfigured: {}. Excluding item from homekit.", item.getName(), e.getMessage());
            homekitAccessoryType = null;
            homekitCharacteristicType = null;
            parentGroupItem = null;
        }
        if (homekitAccessoryType != null) {
            this.id = calculateId(item);
        } else {
            this.id = 0;
        }
    }

    public boolean isTagged() {
        return (homekitAccessoryType != null && id != 0) || homekitCharacteristicType != null;
    }

    public boolean isGroup() {
        return (isAccessory() && (this.item instanceof GroupItem));
    }

    public HomekitAccessoryType getAccessoryType() {
        return homekitAccessoryType;
    }

    public HomekitCharacteristicType getCharacteristicType() {
        return homekitCharacteristicType;
    }

    /**
     * Returns whether or not this item refers to an item that fully specifies a Homekit accessory. Mutually
     * exclusive
     * to isCharacteristic(). Primary devices must belong to a root accessory group.
     */
    public boolean isAccessory() {
        return homekitAccessoryType != null;
    }

    /**
     * Returns whether or not this item is in a group that specifies a Homekit accessory. It is not possible to be a
     * characteristic and an accessory. Further, all characteristics belong to a
     * root deviceGroup.
     */
    public boolean isCharacteristic() {
        return homekitCharacteristicType != null;
    }

    public Item getItem() {
        return item;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return item.getName();
    }

    /**
     * Returns the RootDevice GroupItem to which this item belongs.
     * Returns null if not in a group.
     */
    public GroupItem getRootDeviceGroupItem() {
        return parentGroupItem;
    }

    /**
     * Returns whether or not this item belongs to a Homekit accessory group.
     *
     * Characteristic devices must belong to a Homekit accessory group.
     */
    public boolean isMemberOfAccessoryGroup() {
        return parentGroupItem != null;
    }

    private int calculateId(Item item) {
        int id = new HashCodeBuilder().append(item.getName()).hashCode();
        if (id < 0) {
            id += Integer.MAX_VALUE;
        }
        if (id < 2) {
            id = 2; // 0 and 1 are reserved
        }
        if (CREATED_ACCESSORY_IDS.containsKey(id)) {
            if (!CREATED_ACCESSORY_IDS.get(id).equals(item.getName())) {
                logger.warn(
                        "Could not create homekit accessory {} because its hash conflicts with {}. This is a 1:1,000,000 chance occurrence. Change one of the names and consider playing the lottery. See https://github.com/openhab/openhab2-addons/issues/257#issuecomment-125886562",
                        item.getName(), CREATED_ACCESSORY_IDS.get(id));
                return 0;
            }
        } else {
            CREATED_ACCESSORY_IDS.put(id, item.getName());
        }
        return id;
    }

    static List<GroupItem> findMyAccessoryGroups(Item item, ItemRegistry itemRegistry) {
        return item.getGroupNames().stream().flatMap(name -> {
            Item groupItem = itemRegistry.get(name);
            if ((groupItem != null) && (groupItem instanceof GroupItem)) {
                return Stream.of((GroupItem) groupItem);
            } else {
                return Stream.empty();
            }
        }).filter(groupItem -> {
            return groupItem.getTags().stream().filter(gt -> HomekitAccessoryType.valueOfTag(gt) != null).count() > 0;
        }).collect(Collectors.toList());
    }
}
