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

import static org.openhab.io.homekit.internal.HomekitCharacteristicType.EMPTY;

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.homekit.internal.accessories.HomekitAccessoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.server.impl.HomekitRoot;

/**
 * Listens for changes to the item registry. When changes are detected, check
 * for HomeKit tags and, if present, add the items to the HomekitAccessoryRegistry.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitChangeListener implements ItemRegistryChangeListener {
    private final Logger logger = LoggerFactory.getLogger(HomekitChangeListener.class);
    private final static String REVISION_CONFIG = "revision";
    private final ItemRegistry itemRegistry;
    private final HomekitAccessoryRegistry accessoryRegistry = new HomekitAccessoryRegistry();
    private final MetadataRegistry metadataRegistry;
    private final Storage<String> storage;
    private HomekitAccessoryUpdater updater = new HomekitAccessoryUpdater();
    private HomekitSettings settings;

    private Set<String> pendingUpdates = new HashSet<>();

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON);

    /**
     * Rather than reacting to item added/removed/modified changes directly, we mark them as dirty (and the groups to
     * which they belong)
     *
     * We wait for a second to pass until no more items are changed. This allows us to add a group of items all at once,
     * rather than for each update at a time, preventing us from showing an error message with each addition until the
     * group is complete.
     */
    private final Debouncer applyUpdatesDebouncer;

    HomekitChangeListener(ItemRegistry itemRegistry, HomekitSettings settings, MetadataRegistry metadataRegistry,
            final StorageService storageService) {
        this.itemRegistry = itemRegistry;
        this.settings = settings;
        this.metadataRegistry = metadataRegistry;
        storage = storageService.getStorage("homekit");
        initialiseRevision();

        this.applyUpdatesDebouncer = new Debouncer("update-homekit-devices", scheduler, Duration.ofMillis(1000),
                Clock.systemUTC(), this::applyUpdates);

        itemRegistry.addRegistryChangeListener(this);
        itemRegistry.getItems().stream().forEach(this::createRootAccessories);
        logger.info("Created {} HomeKit items.", accessoryRegistry.getAllAccessories().size());
    }

    private void initialiseRevision() {
        int revision;
        try {
            revision = Integer.valueOf(storage.get(REVISION_CONFIG));
        } catch (java.lang.NumberFormatException e) {
            revision = 1;
            storage.put(REVISION_CONFIG, "" + revision);
        }
        accessoryRegistry.setConfigurationRevision(revision);
    }

    @Override
    public synchronized void added(Item item) {
        markDirty(item);
    }

    @Override
    public void allItemsChanged(Collection<String> oldItemNames) {
        clearAccessories();
    }

    /**
     * Mark an item as dirty, plus any accessory groups to which it pertains, so that after a debounce period the
     * accessory update can be applied.
     *
     * @param item The item that has been changed or removed.
     */
    private synchronized void markDirty(Item item) {
        logger.trace("Mark dirty item {}", item.getLabel());
        pendingUpdates.add(item.getName());
        /*
         * If findMyAccessoryGroups fails because the accessory group has already been deleted, then we can count on a
         * later update telling us that the accessory group was removed.
         */
        for (Item accessoryGroup : HomekitAccessoryFactory.getAccessoryGroups(item, itemRegistry, metadataRegistry)) {
            pendingUpdates.add(accessoryGroup.getName());
        }

        applyUpdatesDebouncer.call();
    }

    @Override
    public synchronized void removed(Item item) {
        markDirty(item);
    }

    private Optional<Item> getItemOptional(String name) {
        try {
            return Optional.of(itemRegistry.getItem(name));
        } catch (ItemNotFoundException e) {
            return Optional.empty();
        }
    }

    private synchronized void applyUpdates() {
        logger.trace("apply updates");
        Iterator<String> iter = pendingUpdates.iterator();

        while (iter.hasNext()) {
            String name = iter.next();
            accessoryRegistry.remove(name);
            logger.trace(" add items {}", name);
            getItemOptional(name).ifPresent(this::createRootAccessories);
        }
        if (!pendingUpdates.isEmpty()) {
            storage.put(REVISION_CONFIG, "" + accessoryRegistry.makeNewConfigurationRevision());
            pendingUpdates.clear();
        }
    }

    @Override
    public void updated(Item oldElement, Item element) {
        markDirty(oldElement);
        markDirty(element);
    }

    public synchronized void clearAccessories() {
        accessoryRegistry.clear();
    }

    public synchronized void setBridge(HomekitRoot bridge) {
        accessoryRegistry.setBridge(bridge);
    }

    public synchronized void unsetBridge() {
        accessoryRegistry.unsetBridge();
    }

    public void setUpdater(HomekitAccessoryUpdater updater) {
        this.updater = updater;
    }

    public void updateSettings(HomekitSettings settings) {
        this.settings = settings;
    }

    public void stop() {
        this.itemRegistry.removeRegistryChangeListener(this);
    }

    public Map<String, HomekitAccessory> getAccessories() {
        return this.accessoryRegistry.getAllAccessories();
    }

    /**
     * creates one or more HomeKit items for given openhab item.
     * one openhab item can linked to several HomeKit accessories or characteristics.
     * 
     * @param item
     */
    private void createRootAccessories(Item item) {
        logger.trace("create root accessory {}", item.getLabel());
        final List<Entry<HomekitAccessoryType, HomekitCharacteristicType>> accessoryTypes = HomekitAccessoryFactory
                .getAccessoryTypes(item, metadataRegistry);
        final List<GroupItem> groups = HomekitAccessoryFactory.getAccessoryGroups(item, itemRegistry, metadataRegistry);
        logger.trace("Item {} has groups {}", item.getName(), groups);
        if (!accessoryTypes.isEmpty() && groups.isEmpty()) { // it has homekit accessory type and is not part of bigger
                                                             // homekit group item
            logger.trace("Item {} is a HomeKit accessory of types {}", item.getName(), accessoryTypes);
            accessoryTypes.stream().filter(accessory -> accessory.getValue() == EMPTY) // no characteristic => root
                                                                                       // accessory or group
                    .forEach(rootAccessory -> createRootAccessory(new HomekitTaggedItem(item, rootAccessory.getKey(),
                            HomekitAccessoryFactory.getItemConfiguration(item, metadataRegistry))));
        }
    }

    private void createRootAccessory(HomekitTaggedItem taggedItem) {
        try {
            logger.trace("Adding HomeKit device {}", taggedItem.getItem().getUID());
            accessoryRegistry.addRootAccessory(taggedItem.getName(),
                    HomekitAccessoryFactory.create(taggedItem, metadataRegistry, updater, settings));
        } catch (HomekitException e) {
            logger.warn("Could not add device {}: {}", taggedItem.getItem().getUID(), e.getMessage());
        }
    }
}
