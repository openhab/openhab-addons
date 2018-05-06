/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.io.homekit.internal.accessories.GroupedAccessory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAccessory;
import com.beowulfe.hap.HomekitRoot;

/**
 * Stores the created HomekitAccessories. GroupedAccessories are also held here
 * in a pre-created pending state until all required characteristics are found.
 *
 * @author Andy Lintner
 */
class HomekitAccessoryRegistry {

    private HomekitRoot bridge;
    private final List<HomekitAccessory> createdAccessories = new LinkedList<>();
    private final Set<Integer> createdIds = new HashSet<>();
    private final Map<String, GroupedAccessory> pendingGroupedAccessories = new HashMap<>();
    private final List<HomekitTaggedItem> pendingCharacteristics = new LinkedList<>();

    private final Logger logger = LoggerFactory.getLogger(HomekitAccessoryRegistry.class);

    public void remove(HomekitTaggedItem taggedItem) {
        Iterator<HomekitAccessory> i = createdAccessories.iterator();
        while (i.hasNext()) {
            HomekitAccessory accessory = i.next();
            if (accessory.getId() == taggedItem.getId()) {
                logger.debug("Removed accessory {}", accessory.getId());
                bridge.removeAccessory(accessory);
                i.remove();
            }
        }
    }

    public synchronized void clear() {
        while (!createdAccessories.isEmpty()) {
            bridge.removeAccessory(createdAccessories.remove(0));
        }
        createdIds.clear();
    }

    public synchronized void setBridge(HomekitRoot bridge) {
        this.bridge = bridge;
        createdAccessories.forEach(accessory -> bridge.addAccessory(accessory));
    }

    public synchronized void addRootDevice(HomekitAccessory accessory) {
        if (accessory instanceof GroupedAccessory) {
            GroupedAccessory groupedAccessory = (GroupedAccessory) accessory;
            pendingGroupedAccessories.put(groupedAccessory.getGroupName(), groupedAccessory);
            for (HomekitTaggedItem characteristic : pendingCharacteristics) {
                if (characteristic.getItem().getGroupNames().contains(groupedAccessory.getGroupName())) {
                    addCharacteristicToGroup(groupedAccessory.getGroupName(), characteristic);
                }
            }
        } else {
            doAddDevice(accessory);
        }
    }

    public synchronized void addCharacteristic(HomekitTaggedItem item) {
        for (String group : item.getItem().getGroupNames()) {
            if (pendingGroupedAccessories.containsKey(group)) {
                addCharacteristicToGroup(group, item);
                logger.debug("Added {} to {}", item.getItem().getName(), group);
                return;
            }
        }
        pendingCharacteristics.add(item);
        logger.debug("Stored {} until group is ready", item.getItem().getName());
    }

    private void addCharacteristicToGroup(String group, HomekitTaggedItem item) {
        GroupedAccessory accessory = pendingGroupedAccessories.get(group);
        accessory.addCharacteristic(item);
        if (accessory.isComplete()) {
            pendingGroupedAccessories.remove(group);
            doAddDevice(accessory);
        }
    }

    private void doAddDevice(HomekitAccessory accessory) {
        createdAccessories.add(accessory);
        createdIds.add(accessory.getId());
        if (bridge != null) {
            bridge.addAccessory(accessory);
        }
        logger.debug("Added accessory {}", accessory.getId());
    }

}
