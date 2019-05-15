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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAccessory;
import com.beowulfe.hap.HomekitRoot;

/**
 * Stores the created HomekitAccessories. GroupedAccessories are also held here
 * in a pre-created pending state until all required characteristics are found.
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitAccessoryRegistry {

    private HomekitRoot bridge;
    private final Map<String, HomekitAccessory> createdAccessories = new HashMap<>();
    private final Set<Integer> createdIds = new HashSet<>();

    private final Logger logger = LoggerFactory.getLogger(HomekitAccessoryRegistry.class);

    public synchronized void remove(String itemName) {
        if (createdAccessories.containsKey(itemName)) {
            HomekitAccessory accessory = createdAccessories.remove(itemName);
            logger.debug("Removed accessory {} for taggedItem {}", accessory.getId(), itemName);
            bridge.removeAccessory(accessory);
        }
    }

    public synchronized void clear() {
        Iterator<Entry<String, HomekitAccessory>> iter = createdAccessories.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, HomekitAccessory> entry = iter.next();
            bridge.removeAccessory(entry.getValue());
            iter.remove();
        }
        createdIds.clear();
    }

    public synchronized void setBridge(HomekitRoot bridge) {
        this.bridge = bridge;
        createdAccessories.values().forEach(accessory -> bridge.addAccessory(accessory));
    }

    public synchronized void addRootAccessory(String itemName, HomekitAccessory accessory) {
        createdAccessories.put(itemName, accessory);
        createdIds.add(accessory.getId());
        if (bridge != null) {
            bridge.addAccessory(accessory);
        }
        logger.debug("Added accessory {}", accessory.getId());
    }
}
