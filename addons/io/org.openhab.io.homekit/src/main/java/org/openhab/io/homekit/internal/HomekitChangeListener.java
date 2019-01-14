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

import java.util.Collection;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.openhab.io.homekit.internal.accessories.HomekitAccessoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitRoot;

/**
 * Listens for changes to the item registry. When changes are detected, check
 * for Homekit tags and, if present, add the items to the HomekitAccessoryRegistry.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitChangeListener implements ItemRegistryChangeListener {

    private ItemRegistry itemRegistry;
    private HomekitAccessoryUpdater updater = new HomekitAccessoryUpdater();
    private Logger logger = LoggerFactory.getLogger(HomekitChangeListener.class);
    private final HomekitAccessoryRegistry accessoryRegistry = new HomekitAccessoryRegistry();
    private HomekitSettings settings;
    boolean initialized = false;

    @Override
    public synchronized void added(Item item) {
        HomekitTaggedItem taggedItem = new HomekitTaggedItem(item, itemRegistry);
        if (taggedItem.isMemberOfAccessoryGroup()) {
            // Update the root item as the member characteristics are modified.
            updated(taggedItem.getRootDeviceGroupItem(), taggedItem.getRootDeviceGroupItem());
        } else if (taggedItem.isAccessory()) {
            createRootAccessory(taggedItem);
        }
    }

    @Override
    public void allItemsChanged(Collection<String> oldItemNames) {
        clearAccessories();
    }

    @Override
    public synchronized void removed(Item item) {
        HomekitTaggedItem taggedItem = new HomekitTaggedItem(item, itemRegistry);
        if (taggedItem.isMemberOfAccessoryGroup()) {
            // Process the root item as modified.
            updated(taggedItem.getRootDeviceGroupItem(), taggedItem.getRootDeviceGroupItem());
        } else {
            accessoryRegistry.remove(taggedItem);
        }
    }

    @Override
    public void updated(Item oldElement, Item element) {
        removed(oldElement);
        added(element);
    }

    public synchronized void clearAccessories() {
        accessoryRegistry.clear();
    }

    public synchronized void setBridge(HomekitRoot bridge) {
        accessoryRegistry.setBridge(bridge);
    }

    public synchronized void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        maybeInitialize();
    }

    /**
     * Call after itemRegistry and settings are specified to initialize homekit devices
     */
    private void maybeInitialize() {
        if (initialized) {
            return;
        }
        if (this.itemRegistry != null && this.settings != null) {
            initialized = true;
            itemRegistry.addRegistryChangeListener(this);
            itemRegistry.getAll().stream().map(item -> new HomekitTaggedItem(item, itemRegistry))
                    .filter(taggedItem -> taggedItem.isAccessory())
                    .filter(taggedItem -> !taggedItem.isMemberOfAccessoryGroup())
                    .forEach(rootTaggedItem -> createRootAccessory(rootTaggedItem));
        }
    }

    public void setUpdater(HomekitAccessoryUpdater updater) {
        this.updater = updater;
    }

    public void setSettings(HomekitSettings settings) {
        this.settings = settings;
        maybeInitialize();
    }

    public void stop() {
        if (this.itemRegistry != null) {
            this.itemRegistry.removeRegistryChangeListener(this);
        }
    }

    private void createRootAccessory(HomekitTaggedItem taggedItem) {
        try {
            if (taggedItem.isMemberOfAccessoryGroup()) {
                throw new RuntimeException(
                        "Bug! Cannot add as a root accessory if it is a member of a group! " + taggedItem.getName());
            }
            logger.debug("Adding homekit device {}", taggedItem.getItem().getName());
            accessoryRegistry.addRootAccessory(taggedItem.getName(),
                    HomekitAccessoryFactory.create(taggedItem, itemRegistry, updater, settings));
            logger.debug("Added homekit device {}", taggedItem.getItem().getName());
        } catch (Exception e) {
            logger.error("Could not add device: {}", e.getMessage(), e);
        }
    }
}
