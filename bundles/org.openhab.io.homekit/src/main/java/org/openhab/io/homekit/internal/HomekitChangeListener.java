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

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemRegistryChangeListener;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.io.homekit.internal.accessories.HomekitAccessoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.server.impl.HomekitRoot;

/**
 * Listens for changes to the item and metadata registry. When changes are detected, check
 * for HomeKit tags and, if present, add the items to the HomekitAccessoryRegistry.
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class HomekitChangeListener implements ItemRegistryChangeListener {
    private final Logger logger = LoggerFactory.getLogger(HomekitChangeListener.class);
    private final static String REVISION_CONFIG = "revision";
    private final static String ACCESSORY_COUNT = "accessory_count";
    private final ItemRegistry itemRegistry;
    private final HomekitAccessoryRegistry accessoryRegistry = new HomekitAccessoryRegistry();
    private final MetadataRegistry metadataRegistry;
    private final Storage<String> storage;
    private final RegistryChangeListener<Metadata> metadataChangeListener;
    private HomekitAccessoryUpdater updater = new HomekitAccessoryUpdater();
    private HomekitSettings settings;
    private int lastAccessoryCount;

    private final Set<String> pendingUpdates = new HashSet<>();

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
            StorageService storageService) {
        this.itemRegistry = itemRegistry;
        this.settings = settings;
        this.metadataRegistry = metadataRegistry;
        storage = storageService.getStorage(HomekitAuthInfoImpl.STORAGE_KEY);
        this.applyUpdatesDebouncer = new Debouncer("update-homekit-devices", scheduler, Duration.ofMillis(1000),
                Clock.systemUTC(), this::applyUpdates);
        metadataChangeListener = new RegistryChangeListener<Metadata>() {
            @Override
            public void added(final Metadata metadata) {
                try {
                    markDirty(itemRegistry.getItem(metadata.getUID().getItemName()));
                } catch (ItemNotFoundException e) {
                    logger.debug("Could not found item for metadata {}", metadata);
                }
            }

            @Override
            public void removed(final Metadata metadata) {
                try {
                    markDirty(itemRegistry.getItem(metadata.getUID().getItemName()));
                } catch (ItemNotFoundException e) {
                    logger.debug("Could not found item for metadata {}", metadata);
                }
            }

            @Override
            public void updated(final Metadata metadata, final Metadata e1) {
                try {
                    markDirty(itemRegistry.getItem(metadata.getUID().getItemName()));
                    if (!metadata.getUID().getItemName().equals(e1.getUID().getItemName())) {
                        markDirty(itemRegistry.getItem(e1.getUID().getItemName()));
                    }
                } catch (ItemNotFoundException e) {
                    logger.debug("Could not found item for metadata {}", metadata);
                }
            }
        };
        itemRegistry.addRegistryChangeListener(this);
        metadataRegistry.addRegistryChangeListener(metadataChangeListener);
        itemRegistry.getItems().forEach(this::createRootAccessories);
        initialiseRevision();
        logger.info("Created {} HomeKit items.", accessoryRegistry.getAllAccessories().size());
    }

    private void initialiseRevision() {
        int revision;
        try {
            String revisionString = storage.get(REVISION_CONFIG);
            if (revisionString == null) {
                throw new NumberFormatException();
            }
            revision = Integer.parseInt(revisionString);
        } catch (NumberFormatException e) {
            revision = 1;
            storage.put(REVISION_CONFIG, "" + revision);
        }
        try {
            String accessoryCountString = storage.get(ACCESSORY_COUNT);
            if (accessoryCountString == null) {
                throw new NumberFormatException();
            }
            lastAccessoryCount = Integer.parseInt(accessoryCountString);
        } catch (NumberFormatException e) {
            lastAccessoryCount = 0;
            storage.put(ACCESSORY_COUNT, "" + accessoryRegistry.getAllAccessories().size());
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
        logger.trace("Mark dirty item {}", item.getName());
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

    public void makeNewConfigurationRevision() {
        final int newRevision = accessoryRegistry.makeNewConfigurationRevision();
        lastAccessoryCount = accessoryRegistry.getAllAccessories().size();
        logger.trace("make new configuration revision. new revision number {}, number of accessories {}", newRevision,
                lastAccessoryCount);
        storage.put(REVISION_CONFIG, "" + newRevision);
        storage.put(ACCESSORY_COUNT, "" + lastAccessoryCount);
    }

    private synchronized void applyUpdates() {
        logger.trace("apply updates");
        for (final String name : pendingUpdates) {
            accessoryRegistry.remove(name);
            logger.trace(" add items {}", name);
            getItemOptional(name).ifPresent(this::createRootAccessories);
        }
        if (!pendingUpdates.isEmpty()) {
            makeNewConfigurationRevision();
            pendingUpdates.clear();
        }
    }

    @Override
    public void updated(Item oldElement, Item element) {
        markDirty(oldElement);
        markDirty(element);
    }

    public int getLastAccessoryCount() {
        return lastAccessoryCount;
    }

    public synchronized void clearAccessories() {
        accessoryRegistry.clear();
    }

    public synchronized void setBridge(HomekitRoot bridge) {
        accessoryRegistry.setBridge(bridge);
    }

    public synchronized void unsetBridge() {
        applyUpdatesDebouncer.stop();
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
        this.metadataRegistry.removeRegistryChangeListener(metadataChangeListener);
    }

    public Map<String, HomekitAccessory> getAccessories() {
        return this.accessoryRegistry.getAllAccessories();
    }

    public int getConfigurationRevision() {
        return this.accessoryRegistry.getConfigurationRevision();
    }

    /**
     * creates one or more HomeKit items for given openhab item.
     * one OpenHAB item can linked to several HomeKit accessories or characteristics.
     * OpenHAB Item is a good candidate for homeKit accessory IF
     * - it has HomeKit accessory types, i.e. HomeKit accessory tag AND
     * - has no group with HomeKit tag, i.e. single line accessory ODER
     * - has groups with HomeKit tag, but all groups are with baseItem, e.g. Group:Switch,
     * so that the groups already complete accessory and group members can be a standalone HomeKit accessory.
     * In contrast, items which are part of groups without BaseItem are additional HomeKit characteristics of the
     * accessory defined by that group and dont need to be created as RootAccessory here.
     *
     * Examples:
     * // Single Line HomeKit Accessory
     * Switch light "Light" {homekit="Lighting"}
     *
     * // One HomeKit accessory defined using group
     * Group gLight "Light Group" {homekit="Lighting"}
     * Switch light "Light" (gLight) {homekit="Lighting.OnState"}
     *
     * // 2 HomeKit accessories: one is switch attached to group, another one a single switch
     * Group:Switch:OR(ON,OFF) gLight "Light Group " {homekit="Lighting"}
     * Switch light "Light" (gLight) {homekit="Lighting.OnState"}
     *
     * @param item openHAB item
     */
    private void createRootAccessories(Item item) {
        final List<Entry<HomekitAccessoryType, HomekitCharacteristicType>> accessoryTypes = HomekitAccessoryFactory
                .getAccessoryTypes(item, metadataRegistry);
        final List<GroupItem> groups = HomekitAccessoryFactory.getAccessoryGroups(item, itemRegistry, metadataRegistry);
        if (!accessoryTypes.isEmpty()
                && (groups.isEmpty() || groups.stream().noneMatch(g -> g.getBaseItem() == null))) {
            logger.trace("Item {} is a HomeKit accessory of types {}", item.getName(), accessoryTypes);
            final HomekitOHItemProxy itemProxy = new HomekitOHItemProxy(item);
            accessoryTypes.forEach(rootAccessory -> createRootAccessory(new HomekitTaggedItem(itemProxy,
                    rootAccessory.getKey(), HomekitAccessoryFactory.getItemConfiguration(item, metadataRegistry))));
        }
    }

    private void createRootAccessory(HomekitTaggedItem taggedItem) {
        try {
            accessoryRegistry.addRootAccessory(taggedItem.getName(),
                    HomekitAccessoryFactory.create(taggedItem, metadataRegistry, updater, settings));
        } catch (HomekitException e) {
            logger.warn("Could not add device {}: {}", taggedItem.getItem().getUID(), e.getMessage());
        }
    }
}
