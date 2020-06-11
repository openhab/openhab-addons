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

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.HomekitAccessory;
import io.github.hapjava.HomekitRoot;

/**
 * Stores the created HomekitAccessories. GroupedAccessories are also held here
 * in a pre-created pending state until all required characteristics are found.
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitAccessoryRegistry {

    private @Nullable HomekitRoot bridge;
    private final Map<String, HomekitAccessory> createdAccessories = new HashMap<>();
    private final Set<Integer> createdIds = new HashSet<>();

    private final Logger logger = LoggerFactory.getLogger(HomekitAccessoryRegistry.class);

    public synchronized void remove(String itemName) {
        if (createdAccessories.containsKey(itemName)) {
            HomekitAccessory accessory = createdAccessories.remove(itemName);
            logger.debug("Removed accessory {} for taggedItem {}", accessory.getId(), itemName);
            if (bridge != null) {
                bridge.removeAccessory(accessory);
            } else {
                logger.warn("trying to remove {} but bridge is null", accessory);
            }
        }
    }

    public synchronized void clear() {
        Iterator<Entry<String, HomekitAccessory>> iter = createdAccessories.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, HomekitAccessory> entry = iter.next();
            if (bridge != null) {
                bridge.removeAccessory(entry.getValue());
            } else {
                logger.warn("trying to clear {} but bridge is null", entry);
            }
            iter.remove();
        }
        createdIds.clear();
    }

    public synchronized void setBridge(HomekitRoot bridge) {
        this.bridge = bridge;
        createdAccessories.values().forEach(accessory -> bridge.addAccessory(accessory));
    }

    public synchronized void unsetBridge() {
        final HomekitRoot oldBridge = bridge;
        if (oldBridge != null) {
            createdAccessories.values().forEach(accessory -> oldBridge.removeAccessory(accessory));
        }
        bridge = null;
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
