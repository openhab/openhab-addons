/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps an Item with data derived from supported tags defined.
 *
 * @author Andy Lintner
 */
public class HomekitTaggedItem {

    private static final Map<Integer, String> CREATED_ACCESSORY_IDS = new ConcurrentHashMap<>();

    private HomekitDeviceType homekitDeviceType;
    private HomekitCharacteristicType homekitCharacteristicType;
    private final Item item;
    private Logger logger = LoggerFactory.getLogger(HomekitTaggedItem.class);
    private final int id;

    public HomekitTaggedItem(Item item, ItemRegistry itemRegistry) {
        this.item = item;
        for (String tag : item.getTags()) {

            if (item instanceof ColorItem) {
                tag = "Colorful" + tag;
            } else if (item instanceof DimmerItem) {
                tag = "Dimmable" + tag;
            }

            /*
             * Is the item part of a tagged group AND does it have a matching CharacteristicType ?
             * This matches items with tags that require a parent group like the "TargetTemperature" in
             * thermostats
             */
            if (isMemberOfRootGroup(item, itemRegistry)) {
                homekitCharacteristicType = HomekitCharacteristicType.valueOfTag(tag);
            }

            /*
             * If its not a characteristic type for a group item, see if we have a matching device type.
             */
            if (homekitCharacteristicType == null) {
                homekitDeviceType = HomekitDeviceType.valueOfTag(tag);
            }

            if (homekitDeviceType != null || homekitCharacteristicType != null) {
                break;
            }
        }
        if (homekitDeviceType != null) {
            this.id = calculateId(item);
        } else {
            this.id = 0;
        }
    }

    public boolean isTagged() {
        return (homekitDeviceType != null && id != 0) || homekitCharacteristicType != null;
    }

    public HomekitDeviceType getDeviceType() {
        return homekitDeviceType;
    }

    public HomekitCharacteristicType getCharacteristicType() {
        return homekitCharacteristicType;
    }

    public boolean isRootDevice() {
        return homekitDeviceType != null;
    }

    public boolean isCharacteristic() {
        return homekitCharacteristicType != null;
    }

    public Item getItem() {
        return item;
    }

    public int getId() {
        return id;
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
                logger.error(
                        "Could not create homekit accessory {} because its hash conflicts with {}. This is a 1:1,000,000 chance occurrence. Change one of the names and consider playing the lottery. See https://github.com/openhab/openhab2-addons/issues/257#issuecomment-125886562",
                        item.getName(), CREATED_ACCESSORY_IDS.get(id));
                return 0;
            }
        } else {
            CREATED_ACCESSORY_IDS.put(id, item.getName());
        }
        return id;
    }

    private boolean isMemberOfRootGroup(Item item, ItemRegistry itemRegistry) {
        for (String groupName : item.getGroupNames()) {
            Item groupItem = itemRegistry.get(groupName);
            if (groupItem != null) {
                for (String groupTag : groupItem.getTags()) {
                    if (HomekitDeviceType.valueOfTag(groupTag) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
