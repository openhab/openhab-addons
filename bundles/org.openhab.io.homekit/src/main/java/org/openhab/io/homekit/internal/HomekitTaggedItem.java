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
package org.openhab.io.homekit.internal;

import static org.openhab.io.homekit.internal.HomekitAccessoryType.DUMMY;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps an Item with data derived from supported tags defined.
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class HomekitTaggedItem {
    private final Logger logger = LoggerFactory.getLogger(HomekitTaggedItem.class);

    private static final Map<Integer, String> CREATED_ACCESSORY_IDS = new ConcurrentHashMap<>();
    /**
     * The type of HomekitDevice we've decided this was. If the item is question is the member of a group which is a
     * HomekitDevice, then this is null.
     */
    private final Item item;
    private final HomekitAccessoryType homekitAccessoryType;
    private @Nullable HomekitCharacteristicType homekitCharacteristicType;
    private @Nullable Map<String, Object> configuration;
    private @Nullable GroupItem parentGroupItem;
    private final int id;

    public HomekitTaggedItem(Item item, HomekitAccessoryType homekitAccessoryType,
            @Nullable Map<String, Object> configuration) {
        this.item = item;
        this.parentGroupItem = null;
        this.configuration = configuration;
        this.homekitAccessoryType = homekitAccessoryType;
        this.homekitCharacteristicType = HomekitCharacteristicType.EMPTY;
        if (homekitAccessoryType != DUMMY) {
            this.id = calculateId(item);
        } else {
            this.id = 0;
        }
    }

    public HomekitTaggedItem(Item item, HomekitAccessoryType homekitAccessoryType,
            @Nullable HomekitCharacteristicType homekitCharacteristicType,
            @Nullable Map<String, Object> configuration) {
        this(item, homekitAccessoryType, configuration);
        this.homekitCharacteristicType = homekitCharacteristicType;
    }

    public HomekitTaggedItem(Item item, HomekitAccessoryType homekitAccessoryType,
            @Nullable HomekitCharacteristicType homekitCharacteristicType, @Nullable GroupItem parentGroup,
            @Nullable Map<String, Object> configuration) {
        this(item, homekitAccessoryType, homekitCharacteristicType, configuration);
        this.parentGroupItem = parentGroup;
    }

    public boolean isGroup() {
        return (isAccessory() && (this.item instanceof GroupItem));
    }

    public HomekitAccessoryType getAccessoryType() {
        return homekitAccessoryType;
    }

    public @Nullable HomekitCharacteristicType getCharacteristicType() {
        return homekitCharacteristicType;
    }

    public @Nullable Map<String, Object> getConfiguration() {
        return configuration;
    }

    /**
     * Returns whether or not this item refers to an item that fully specifies a HomeKit accessory. Mutually
     * exclusive
     * to isCharacteristic(). Primary devices must belong to a root accessory group.
     */
    public boolean isAccessory() {
        return homekitAccessoryType != DUMMY;
    }

    /**
     * Returns whether or not this item is in a group that specifies a HomeKit accessory. It is not possible to be a
     * characteristic and an accessory. Further, all characteristics belong to a
     * root deviceGroup.
     */
    public boolean isCharacteristic() {
        return homekitCharacteristicType != null && homekitCharacteristicType != HomekitCharacteristicType.EMPTY;
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
    public @Nullable GroupItem getRootDeviceGroupItem() {
        return parentGroupItem;
    }

    /**
     * Returns whether or not this item belongs to a HomeKit accessory group.
     *
     * Characteristic devices must belong to a HomeKit accessory group.
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
                        "Could not create HomeKit accessory {} because its hash conflicts with {}. This is a 1:1,000,000 chance occurrence. Change one of the names and consider playing the lottery. See https://github.com/openhab/openhab-addons/issues/257#issuecomment-125886562",
                        item.getName(), CREATED_ACCESSORY_IDS.get(id));
                return 0;
            }
        } else {
            CREATED_ACCESSORY_IDS.put(id, item.getName());
        }
        return id;
    }

    public String toString() {
        return "Item:" + item + "  HomeKit type:" + homekitAccessoryType + " HomeKit characteristic:"
                + homekitCharacteristicType;
    }
}
