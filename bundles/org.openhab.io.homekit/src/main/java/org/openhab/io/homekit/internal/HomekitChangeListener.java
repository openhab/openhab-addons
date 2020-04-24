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

import io.github.hapjava.HomekitRoot;

/**
 * Listens for changes to the item registry. When changes are detected, check
 * for HomeKit tags and, if present, add the items to the HomekitAccessoryRegistry.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HomekitChangeListener implements ItemRegistryChangeListener {
    private final Logger logger = LoggerFactory.getLogger(HomekitChangeListener.class);
    private final ItemRegistry itemRegistry;
    private final HomekitAccessoryRegistry accessoryRegistry = new HomekitAccessoryRegistry();
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

    HomekitChangeListener(ItemRegistry itemRegistry, HomekitSettings settings) {
        this.itemRegistry = itemRegistry;
        this.settings = settings;
        this.applyUpdatesDebouncer = new Debouncer("update-homekit-devices", scheduler, Duration.ofMillis(1000),
                Clock.systemUTC(), this::applyUpdates);

        itemRegistry.addRegistryChangeListener(this);
        itemRegistry.getAll().stream().map(item -> new HomekitTaggedItem(item, itemRegistry))
                .filter(taggedItem -> taggedItem.isAccessory())
                .filter(taggedItem -> !taggedItem.isMemberOfAccessoryGroup())
                .forEach(rootTaggedItem -> createRootAccessory(rootTaggedItem));
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

    private void createRootAccessory(HomekitTaggedItem taggedItem) {
        try {
            if (taggedItem.isMemberOfAccessoryGroup()) {
                logger.warn("Bug! Cannot add {} as a root accessory if it is a member of a group! ",
                        taggedItem.getItem().getUID());
                return;
            }
            logger.debug("Adding HomeKit device {}", taggedItem.getItem().getUID());
            accessoryRegistry.addRootAccessory(taggedItem.getName(),
                    HomekitAccessoryFactory.create(taggedItem, itemRegistry, updater, settings));
            logger.debug("Added HomeKit device {}", taggedItem.getItem().getUID());
        } catch (HomekitException | IncompleteAccessoryException e) {
            logger.warn("Could not add device {}: {}", taggedItem.getItem().getUID(), e.getMessage());
        }
    }
}
