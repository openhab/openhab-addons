/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.storage.Storage;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDevice;
import org.openhab.io.hueemulation.internal.dto.HueGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to the ItemRegistry for items that fulfill one of these criteria:
 * <ul>
 * <li>Type is any of SWITCH, DIMMER, COLOR, or Group
 * <li>The category is "ColorLight" for coloured lights or "Light" for switchables.
 * <li>The item is tagged, according to what is set with {@link #setFilterTags(Set, Set, Set)}.
 * </ul>
 *
 * <p>
 * A {@link HueDevice} instances is created for each found item.
 * Those are kept in the given {@link HueDataStore}.
 * </p>
 *
 * <p>
 * Implementing scenes should be done here as well.
 * </p>
 *
 * <p>
 * The HUE Rest API requires a unique integer ID for every listed device. A storage service
 * is used to store and load this mapping. A storage is not required for this class to work,
 * but without it the mapping will be temporary only and ids may change on every boot up.
 * </p>
 *
 * <p>
 * </p>
 *
 * @author David Graeff - Initial contribution
 * @author Florian Schmidt - Removed base type restriction from Group items
 */
@NonNullByDefault
public class LightItems implements RegistryChangeListener<Item> {
    private final Logger logger = LoggerFactory.getLogger(LightItems.class);
    private static final String ITEM_TYPE_GROUP = "Group";
    private static final Set<String> ALLOWED_ITEM_TYPES = Stream
            .of(CoreItemFactory.COLOR, CoreItemFactory.DIMMER, CoreItemFactory.SWITCH, ITEM_TYPE_GROUP)
            .collect(Collectors.toSet());

    // deviceMap maps a unique Item id to a Hue numeric id
    final TreeMap<String, Integer> itemUIDtoHueID = new TreeMap<>();
    private final HueDataStore dataStore;
    private Set<String> switchFilter = Collections.emptySet();
    private Set<String> colorFilter = Collections.emptySet();
    private Set<String> whiteFilter = Collections.emptySet();
    private @Nullable Storage<Integer> storage;
    private boolean initDone = false;
    private @NonNullByDefault({}) ItemRegistry itemRegistry;

    public LightItems(HueDataStore ds) {
        dataStore = ds;
    }

    /**
     * Set filter tags. Empty sets are allowed, items will not be filtered by tags but other criteria then.
     *
     * <p>
     * Calling this method will reset the {@link HueDataStore} and parse items from the item registry again.
     * </p>
     *
     * @param switchFilter The switch filter tags
     * @param colorFilter  The color filter tags
     * @param whiteFilter  The white bulb filter tags
     */
    public void setFilterTags(Set<String> switchFilter, Set<String> colorFilter, Set<String> whiteFilter) {
        this.switchFilter = switchFilter;
        this.colorFilter = colorFilter;
        this.whiteFilter = whiteFilter;
        fetchItems();
    }

    /**
     * Sets the item registry. Used to load up items and register to changes
     *
     * @param itemRegistry The item registry
     */
    public void setItemRegistry(@Nullable ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    /**
     * Load the {@link #itemUIDtoHueID} mapping from the given storage.
     * This method can also be called when the storage service changes.
     * A changed storage causes an immediately write request.
     * <p>
     * Because storage services are dynamically bound and may appear late,
     * it may happen that the mapping is only loaded after items have been parsed already.
     * In that case the {@link HueDataStore} is reset and items from the item registry are parsed again.
     * It is important to keep the once exposed ids though.
     * </p>
     *
     * @param storage A storage service
     */
    public void loadMappingFromFile(@Nullable Storage<Integer> storage) {
        boolean storageChanged = this.storage != null && this.storage != storage;
        this.storage = storage;
        if (storage == null) {
            return;
        }

        for (String itemUID : storage.getKeys()) {
            Integer hueID = storage.get(itemUID);
            if (hueID == null) {
                continue;
            }
            itemUIDtoHueID.put(itemUID, hueID);
        }

        if (storageChanged) {
            writeToFile();
        } else if (initDone) { // storage comes late to the game -> reassign all items
            fetchItems();
        }
    }

    /**
     * Registers to the {@link ItemRegistry} and enumerates currently existing items.
     * Call {@link #close(ItemRegistry)} when you are done with this object.
     *
     * Only call this after you have set the filter tags with {@link #setFilterTags(Set, Set, Set)}.
     */
    public synchronized void fetchItems() {
        initDone = false;

        dataStore.resetGroupsAndLights();

        itemRegistry.removeRegistryChangeListener(this);
        itemRegistry.addRegistryChangeListener(this);

        boolean changed = false;
        for (Item item : itemRegistry.getItems()) {
            changed |= addItem(item);
        }
        initDone = true;

        logger.debug("Added items: {}",
                dataStore.lights.values().stream().map(l -> l.name).collect(Collectors.joining(", ")));
        if (changed) {
            writeToFile();
        }
    }

    /**
     * Saves the ID->Item association to the storage.
     */
    private void writeToFile() {
        Storage<Integer> storage = this.storage;
        if (storage == null) {
            return;
        }
        storage.getKeys().forEach(key -> storage.remove(key));
        itemUIDtoHueID.forEach((itemUID, hueID) -> storage.put(itemUID, hueID));
    }

    public void resetStorage() {
        this.storage = null;
    }

    /**
     * Unregisters from the {@link ItemRegistry}.
     */
    public void close() {
        writeToFile();
        itemRegistry.removeRegistryChangeListener(this);
    }

    private @Nullable DeviceType determineTargetType(@Nullable String category, String type, Set<String> tags) {
        // Determine type, heuristically
        DeviceType t = null;

        // First consider the category
        if (category != null) {
            switch (category) {
                case "ColorLight":
                    t = DeviceType.ColorType;
                    break;
                case "Light":
                    t = DeviceType.SwitchType;
            }
        }

        // Then the tags
        if (switchFilter.stream().anyMatch(tags::contains)) {
            t = DeviceType.SwitchType;
        }
        if (whiteFilter.stream().anyMatch(tags::contains)) {
            t = DeviceType.WhiteTemperatureType;
        }
        if (colorFilter.stream().anyMatch(tags::contains)) {
            t = DeviceType.ColorType;
        }

        // Last but not least, the item type
        if (t == null) {
            switch (type) {
                case CoreItemFactory.COLOR:
                    if (colorFilter.size() == 0) {
                        t = DeviceType.ColorType;
                    }
                    break;
                case CoreItemFactory.DIMMER:
                    if (whiteFilter.size() == 0) {
                        t = DeviceType.WhiteTemperatureType;
                    }
                    break;
                case CoreItemFactory.SWITCH:
                    if (switchFilter.size() == 0) {
                        t = DeviceType.SwitchType;
                    }
                    break;
            }
        }
        return t;
    }

    @Override
    public synchronized void added(Item element) {
        addItem(element);
    }

    String getType(Item element) {
        if (element instanceof GroupItem) {
            return ITEM_TYPE_GROUP;
        }
        return element.getType();
    }

    @SuppressWarnings({ "unused", "null" })
    public boolean addItem(Item element) {
        // Only allowed types
        String type = getType(element);

        if (!ALLOWED_ITEM_TYPES.contains(type)) {
            return false;
        }

        DeviceType t = determineTargetType(element.getCategory(), type, element.getTags());
        if (t == null) {
            return false;
        }

        Integer hueID = itemUIDtoHueID.get(element.getUID());

        boolean itemAssociationCreated = false;
        if (hueID == null) {
            hueID = dataStore.generateNextLightHueID();
            itemAssociationCreated = true;
        }

        HueDevice device = new HueDevice(element, dataStore.config.uuid + "-" + hueID.toString(), t);
        device.item = element;
        dataStore.lights.put(hueID, device);
        if (element instanceof GroupItem) {
            GroupItem g = (GroupItem) element;
            g.getMembers();
            HueGroup group = new HueGroup(g.getName(), g, itemUIDtoHueID);
            dataStore.groups.put(hueID, group);
        }
        updateGroup0();
        itemUIDtoHueID.put(element.getUID(), hueID);
        if (initDone) {
            logger.debug("Add item {}", element.getUID());
            if (itemAssociationCreated) {
                writeToFile();
            }
        }
        return itemAssociationCreated;
    }

    /**
     * The HUE API enforces a Group 0 that contains all lights.
     */
    private void updateGroup0() {
        dataStore.groups.get(0).lights = dataStore.lights.keySet().stream().map(v -> String.valueOf(v))
                .collect(Collectors.toList());
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public synchronized void removed(Item element) {
        Integer hueID = itemUIDtoHueID.get(element.getUID());
        if (hueID == null) {
            return;
        }
        logger.debug("Remove item {}", element.getUID());
        dataStore.lights.remove(hueID);
        dataStore.groups.remove(hueID);
        updateGroup0();
        itemUIDtoHueID.remove(element.getUID());
        writeToFile();
    }

    /**
     * The tags might have changed
     */
    @SuppressWarnings({ "null", "unused" })
    @Override
    public synchronized void updated(Item oldElement, Item element) {
        Integer hueID = itemUIDtoHueID.get(element.getUID());
        if (hueID == null) {
            // If the correct tags got added -> use the logic within added()
            added(element);
            return;
        }

        HueGroup hueGroup = dataStore.groups.get(hueID);
        if (hueGroup != null) {
            if (element instanceof GroupItem) {
                hueGroup.updateItem((GroupItem) element);
            } else {
                dataStore.groups.remove(hueID);
            }
        }

        HueDevice hueDevice = dataStore.lights.get(hueID);
        if (hueDevice == null) {
            // If the correct tags got added -> use the logic within added()
            added(element);
            return;
        }

        // Check if type can still be determined (tags and category is still sufficient)
        DeviceType t = determineTargetType(element.getCategory(), getType(element), element.getTags());
        if (t == null) {
            removed(element);
            return;
        }

        hueDevice.updateItem(element);
    }
}
