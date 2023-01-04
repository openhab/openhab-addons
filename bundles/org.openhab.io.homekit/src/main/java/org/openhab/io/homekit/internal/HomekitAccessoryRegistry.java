/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.server.impl.HomekitRoot;

/**
 * Stores the created HomekitAccessories. GroupedAccessories are also held here
 * in a pre-created pending state until all required characteristics are found.
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitAccessoryRegistry {
    private @Nullable HomekitRoot bridge;
    private final Map<String, HomekitAccessory> createdAccessories = new HashMap<>();
    private int configurationRevision = 1;
    private final Logger logger = LoggerFactory.getLogger(HomekitAccessoryRegistry.class);

    public void setConfigurationRevision(int revision) {
        configurationRevision = revision;
    }

    public int getConfigurationRevision() {
        return configurationRevision;
    }

    public int makeNewConfigurationRevision() {
        configurationRevision = (configurationRevision + 1) % 65535;
        final HomekitRoot bridge = this.bridge;
        try {
            if (bridge != null) {
                bridge.setConfigurationIndex(configurationRevision);
            }
        } catch (IOException e) {
            logger.warn("Could not update configuration revision number", e);
        }
        return configurationRevision;
    }

    public synchronized void remove(String itemName) {
        if (createdAccessories.containsKey(itemName)) {
            HomekitAccessory accessory = createdAccessories.remove(itemName);
            logger.trace("Removed accessory {} for taggedItem {}", accessory, itemName);
            final HomekitRoot bridge = this.bridge;
            if (bridge != null) {
                bridge.removeAccessory(accessory);
            } else {
                logger.warn("trying to remove {} but bridge is null", accessory);
            }
        }
    }

    public synchronized void clear() {
        final HomekitRoot bridge = this.bridge;
        if (bridge != null) {
            createdAccessories.values().forEach(bridge::removeAccessory);
        } else {
            logger.warn("trying to clear accessories but bridge is null");
        }
    }

    public synchronized void setBridge(HomekitRoot bridge) {
        this.bridge = bridge;
        createdAccessories.values().forEach(bridge::addAccessory);
    }

    public synchronized void unsetBridge() {
        bridge = null;
    }

    public synchronized HomekitRoot getBridge() {
        return bridge;
    }

    public synchronized void addRootAccessory(String itemName, HomekitAccessory accessory) {
        createdAccessories.put(itemName, accessory);
        final HomekitRoot bridge = this.bridge;
        if (bridge != null) {
            bridge.addAccessory(accessory);
        }
    }

    public Map<String, HomekitAccessory> getAllAccessories() {
        return this.createdAccessories;
    }
}
