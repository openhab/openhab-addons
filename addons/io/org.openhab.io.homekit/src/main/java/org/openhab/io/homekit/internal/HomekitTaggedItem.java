/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps an Item with data derived from any homekit: tags defined.
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

    public HomekitTaggedItem(Item item) {
        this.item = item;
        for (String tag : item.getTags()) {
            if (tag.startsWith("homekit:")) {
                String tagValue = tag.substring("homekit:".length()).trim();
                homekitDeviceType = HomekitDeviceType.valueOfTag(tagValue);
                if (homekitDeviceType == null) {
                    homekitCharacteristicType = HomekitCharacteristicType.valueOfTag(tagValue);
                    if (homekitCharacteristicType == null) {
                        logger.error("Unrecognized homekit type: " + tagValue);
                    }
                }
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
                logger.error("Could not create homekit accessory " + item.getName() + " "
                        + "because its hash conflicts with " + CREATED_ACCESSORY_IDS.get(id) + ". "
                        + "This is a 1:1,000,000 chance occurrence. Change one of the names and "
                        + "consider playing the lottery. See "
                        + "https://github.com/openhab/openhab2/issues/257#issuecomment-125886562");
                return 0;
            }
        } else {
            CREATED_ACCESSORY_IDS.put(id, item.getName());
        }
        return id;
    }
}
