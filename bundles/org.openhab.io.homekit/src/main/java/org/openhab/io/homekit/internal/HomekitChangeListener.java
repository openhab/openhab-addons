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

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.openhab.io.homekit.internal.accessories.HomekitAccessoryFactory;
import org.openhab.io.homekit.internal.accessories.IncompleteAccessoryException;
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
    private boolean initialized = false;
    private Set<String> pendingUpdates = new HashSet<String>();

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

    HomekitChangeListener() {
        this.applyUpdatesDebouncer = new Debouncer("update-homekit-devices", scheduler, Duration.ofMillis(1000),
                Clock.systemUTC(), this::applyUpdates);
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
        pendingUpdates.add(item.getName());
        /*
         * If findMyAccessoryGroups fails because the accessory group has already been deleted, then we can count on a
         * later update telling us that the accessory group was removed.
         */
        for (Item accessoryGroup : HomekitTaggedItem.findMyAccessoryGroups(item, itemRegistry)) {
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
        Iterator<String> iter = pendingUpdates.iterator();

        while (iter.hasNext()) {
            String name = iter.next();
            accessoryRegistry.remove(name);

            getItemOptional(name).map(i -> new HomekitTaggedItem(i, itemRegistry))
                    .filter(i -> i.isAccessory() && !i.isMemberOfAccessoryGroup())
                    .ifPresent(rootItem -> createRootAccessory(rootItem));
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
                logger.warn("Bug! Cannot add {} as a root accessory if it is a member of a group! ",
                        taggedItem.getName());
                return;
            }
            logger.debug("Adding homekit device {}", taggedItem.getItem().getName());
            accessoryRegistry.addRootAccessory(taggedItem.getName(),
                    HomekitAccessoryFactory.create(taggedItem, itemRegistry, updater, settings));
            logger.debug("Added homekit device {}", taggedItem.getItem().getName());
        } catch (HomekitException | IncompleteAccessoryException e) {
            logger.warn("Could not add device: {}", e.getMessage(), e);
        }
    }
}
