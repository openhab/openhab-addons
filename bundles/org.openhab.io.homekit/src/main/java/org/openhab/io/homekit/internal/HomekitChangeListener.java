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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemRegistryChangeListener;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.storage.Storage;
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
    private int instance;

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
            Storage<String> storage, int instance) {
        this.itemRegistry = itemRegistry;
        this.settings = settings;
        this.metadataRegistry = metadataRegistry;
        this.storage = storage;
        this.instance = instance;
        this.applyUpdatesDebouncer = new Debouncer("update-homekit-devices", scheduler, Duration.ofMillis(1000),
                Clock.systemUTC(), this::applyUpdates);
        metadataChangeListener = new RegistryChangeListener<Metadata>() {
            @Override
            public void added(final Metadata metadata) {
                final MetadataKey uid = metadata.getUID();
                if (HomekitAccessoryFactory.METADATA_KEY.equalsIgnoreCase(uid.getNamespace())) {
                    try {
                        markDirty(itemRegistry.getItem(uid.getItemName()));
                    } catch (ItemNotFoundException e) {
                        logger.debug("Could not find item for metadata {}", metadata);
                    }
                }
            }

            @Override
            public void removed(final Metadata metadata) {
                final MetadataKey uid = metadata.getUID();
                if (HomekitAccessoryFactory.METADATA_KEY.equalsIgnoreCase(uid.getNamespace())) {
                    try {
                        markDirty(itemRegistry.getItem(uid.getItemName()));
                    } catch (ItemNotFoundException e) {
                        logger.debug("Could not find item for metadata {}", metadata);
                    }
                }
            }

            @Override
            public void updated(final Metadata oldMetadata, final Metadata newMetadata) {
                final MetadataKey oldUid = oldMetadata.getUID();
                final MetadataKey newUid = newMetadata.getUID();
                if (HomekitAccessoryFactory.METADATA_KEY.equalsIgnoreCase(oldUid.getNamespace())
                        || HomekitAccessoryFactory.METADATA_KEY.equalsIgnoreCase(newUid.getNamespace())) {
                    try {
                        // the item name is same in old and new metadata, so we can take any.
                        markDirty(itemRegistry.getItem(oldUid.getItemName()));
                    } catch (ItemNotFoundException e) {
                        logger.debug("Could not find item for metadata {}", oldMetadata);
                    }
                }
            }
        };
        itemRegistry.addRegistryChangeListener(this);
        metadataRegistry.addRegistryChangeListener(metadataChangeListener);
        itemRegistry.getItems().forEach(this::createRootAccessories);
        initialiseRevision();
        makeNewConfigurationRevision();
        logger.info("Created {} HomeKit items in instance {}.", accessoryRegistry.getAllAccessories().size(), instance);
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

    private boolean hasHomeKitMetadata(Item item) {
        return metadataRegistry.get(new MetadataKey(HomekitAccessoryFactory.METADATA_KEY, item.getUID())) != null;
    }

    @Override
    public synchronized void added(Item item) {
        if (hasHomeKitMetadata(item)) {
            markDirty(item);
        }
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

        /*
         * if metadata of a group item was changed, mark all group member as dirty.
         */
        if (item instanceof GroupItem) {
            ((GroupItem) item).getMembers().forEach(groupMember -> pendingUpdates.add(groupMember.getName()));
        }
        applyUpdatesDebouncer.call();
    }

    @Override
    public synchronized void removed(Item item) {
        if (hasHomeKitMetadata(item)) {
            markDirty(item);
        }
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
        logger.trace("Make new configuration revision. new revision number {}, number of accessories {}", newRevision,
                lastAccessoryCount);
        storage.put(REVISION_CONFIG, "" + newRevision);
        storage.put(ACCESSORY_COUNT, "" + lastAccessoryCount);
    }

    private synchronized void applyUpdates() {
        logger.trace("Apply updates");
        for (final String name : pendingUpdates) {
            accessoryRegistry.remove(name);
            logger.trace(" Add items {}", name);
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
     * select primary accessory type from list of types.
     * selection logic:
     * - if accessory has only one type, it is the primary type
     * - if accessory has no primary type defined per configuration, then the first type on the list is the primary type
     * - if accessory has primary type defined per configuration and this type is on the list of types, then it is the
     * primary
     * - if accessory has primary type defined per configuration and this type is NOT on the list of types, then the
     * first type on the list is the primary type
     *
     * @param item openhab item
     * @param accessoryTypes list of accessory type attached to the item
     * @return primary accessory type
     */
    private HomekitAccessoryType getPrimaryAccessoryType(Item item,
            List<Entry<HomekitAccessoryType, HomekitCharacteristicType>> accessoryTypes,
            @Nullable Map<String, Object> configuration) {
        if (accessoryTypes.size() > 1 && configuration != null) {
            final @Nullable Object value = configuration.get(HomekitTaggedItem.PRIMARY_SERVICE);
            if (value instanceof String) {
                return accessoryTypes.stream()
                        .filter(aType -> ((String) value).equalsIgnoreCase(aType.getKey().getTag())).findAny()
                        .orElse(accessoryTypes.get(0)).getKey();
            }
        }
        // no primary accessory found or there is only one type, so return the first type from the list
        return accessoryTypes.get(0).getKey();
    }

    /**
     * creates one or more HomeKit items for given openhab item.
     * one OpenHAB item can be linked to several HomeKit accessories.
     * OpenHAB item is a good candidate for a HomeKit accessory
     * IF
     * - it has HomeKit accessory types defined using HomeKit accessory metadata
     * - AND is not part of a group with HomeKit metadata
     * e.g.
     * Switch light "Light" {homekit="Lighting"}
     * Group gLight "Light Group" {homekit="Lighting"}
     *
     * OR
     * - it has HomeKit accessory types defined using HomeKit accessory metadata
     * - AND is part of groups with HomeKit metadata, but all groups have baseItem
     * e.g.
     * Group:Switch:OR(ON,OFF) gLight "Light Group " {homekit="Lighting"}
     * Switch light "Light" (gLight) {homekit="Lighting.OnState"}
     *
     *
     * In contrast, items which are part of groups without BaseItem are additional HomeKit characteristics of the
     * accessory defined by that group and don't need to be created as accessory here.
     * e.g.
     * Group gLight "Light Group " {homekit="Lighting"}
     * Switch light "Light" (gLight) {homekit="Lighting.OnState"}
     * is not the root accessory but only a characteristic "OnState"
     *
     * Examples:
     * // Single line HomeKit Accessory
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
        final @Nullable Map<String, Object> itemConfiguration = HomekitAccessoryFactory.getItemConfiguration(item,
                metadataRegistry);
        if (accessoryTypes.isEmpty() || !(groups.isEmpty() || groups.stream().noneMatch(g -> g.getBaseItem() == null))
                || !itemIsForThisBridge(item, itemConfiguration)) {
            return;
        }

        final HomekitAccessoryType primaryAccessoryType = getPrimaryAccessoryType(item, accessoryTypes,
                itemConfiguration);
        logger.trace("Item {} is a HomeKit accessory of types {}. Primary type is {}", item.getName(), accessoryTypes,
                primaryAccessoryType);
        final HomekitOHItemProxy itemProxy = new HomekitOHItemProxy(item);
        final HomekitTaggedItem taggedItem = new HomekitTaggedItem(new HomekitOHItemProxy(item), primaryAccessoryType,
                itemConfiguration);
        try {
            final HomekitAccessory accessory = HomekitAccessoryFactory.create(taggedItem, metadataRegistry, updater,
                    settings);

            accessoryTypes.stream().filter(aType -> !primaryAccessoryType.equals(aType.getKey()))
                    .forEach(additionalAccessoryType -> {
                        final HomekitTaggedItem additionalTaggedItem = new HomekitTaggedItem(itemProxy,
                                additionalAccessoryType.getKey(), itemConfiguration);
                        try {
                            final HomekitAccessory additionalAccessory = HomekitAccessoryFactory
                                    .create(additionalTaggedItem, metadataRegistry, updater, settings);
                            accessory.getServices().add(additionalAccessory.getPrimaryService());
                        } catch (HomekitException e) {
                            logger.warn("Cannot create additional accessory {}", additionalTaggedItem);
                        }
                    });
            accessoryRegistry.addRootAccessory(taggedItem.getName(), accessory);
        } catch (HomekitException e) {
            logger.warn("Cannot create accessory {}", taggedItem);
        }
    }

    private boolean itemIsForThisBridge(Item item, @Nullable Map<String, Object> configuration) {
        // non-tagged accessories belong to the first instance
        if (configuration == null) {
            return (instance == 1);
        }

        final @Nullable Object value = configuration.get(HomekitTaggedItem.INSTANCE);
        if (value == null) {
            return (instance == 1);
        }
        if (value instanceof Number) {
            return (instance == ((Number) value).intValue());
        }
        logger.warn("Unrecognized instance tag {} ({}) for item {}; assigning to default instance.", value,
                value.getClass(), item.getName());
        return (instance == 1);
    }
}
