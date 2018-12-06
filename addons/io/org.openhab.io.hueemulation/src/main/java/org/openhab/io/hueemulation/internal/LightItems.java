/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDevice;
import org.openhab.io.hueemulation.internal.dto.HueGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to the ItemRegistry for items that fulfill the criteria
 * (type is any of SWITCH, DIMMER, COLOR and it is tagged or has a category)
 * and creates {@link HueDevice} instances for every found item.
 *
 * <p>
 * The {@link HueDevice} instances are kept in the given {@link HueDataStore}.
 * </p>
 *
 * <p>
 * Implementing groups or scenes should be done here as well by filtering for GroupItems.
 * At the moment only the artificial Group 0 is provided.
 * </p>
 *
 * The HUE Rest API requires a unique integer ID for every listed device.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class LightItems implements RegistryChangeListener<Item> {
    private final Logger logger = LoggerFactory.getLogger(LightItems.class);
    private static final Set<String> ALLOWED_ITEM_TYPES = Stream
            .of(CoreItemFactory.COLOR, CoreItemFactory.DIMMER, CoreItemFactory.SWITCH).collect(Collectors.toSet());

    // deviceMap maps a unique Item id to a Hue numeric id
    private final TreeMap<String, Integer> itemUIDtoHueID = new TreeMap<>();
    private final HueDataStore dataStore;
    private Set<String> switchFilter = Collections.emptySet();
    private Set<String> colorFilter = Collections.emptySet();
    private Set<String> whiteFilter = Collections.emptySet();
    private @Nullable Storage<Integer> storage;

    public LightItems(HueDataStore ds) {
        dataStore = ds;
    }

    /**
     * Set filter tags. Empty sets are allowed, items will not be filtered then.
     *
     * @param switchFilter The switch filter tags
     * @param colorFilter The color filter tags
     * @param whiteFilter The white bulb filter tags
     */
    public void setFilterTags(Set<String> switchFilter, Set<String> colorFilter, Set<String> whiteFilter) {
        this.switchFilter = switchFilter;
        this.colorFilter = colorFilter;
        this.whiteFilter = whiteFilter;
    }

    /**
     * Load the {@link #itemUIDtoHueID} mapping.
     */
    public void loadMappingFromFile(Storage<Integer> storage) {
        boolean storageChanged = this.storage != null && this.storage != storage;
        this.storage = storage;
        for (String itemUID : storage.getKeys()) {
            Integer hueID = storage.get(itemUID);
            if (hueID == null) {
                continue;
            }
            itemUIDtoHueID.put(itemUID, hueID);
        }
        if (storageChanged) {
            writeToFile();
        }
    }

    /**
     * Registers to the {@link ItemRegistry} and enumerates currently existing items.
     * Call {@link #close(ItemRegistry)} when you are done with this object.
     *
     * Only call this after you have set the filter tags with {@link #setFilterTags(Set, Set, Set)}.
     *
     * @param itemRegistry The item registry
     */
    public void fetchItemsAndWatchRegistry(ItemRegistry itemRegistry) {
        itemRegistry.addRegistryChangeListener(this);

        for (Item item : itemRegistry.getItems()) {
            added(item);
        }
    }

    private int generateNextHueID() {
        return dataStore.lights.size() == 0 ? 1 : new Integer(dataStore.lights.lastKey().intValue() + 1);
    }

    /**
     * Saves the ID->Item association to the storage.
     */
    private void writeToFile() {
        Storage<Integer> storage = this.storage;
        if (storage == null) {
            return;
        }
        itemUIDtoHueID.forEach((itemUID, hueID) -> storage.put(itemUID, hueID));
    }

    public void resetStorage() {
        this.storage = null;
    }

    /**
     * Unregisters from the {@link ItemRegistry}.
     */
    public void close(ItemRegistry itemRegistry) {
        writeToFile();
        itemRegistry.removeRegistryChangeListener(this);
    }

    private @Nullable DeviceType determineTargetType(Item element) {
        // Determine type, heuristically
        DeviceType t = null;

        // No read only states
        StateDescription stateDescription = element.getStateDescription();
        if (stateDescription != null && stateDescription.isReadOnly()) {
            return t;
        }

        // First consider the category
        String category = element.getCategory();
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
        if (switchFilter.stream().anyMatch(element.getTags()::contains)) {
            t = DeviceType.SwitchType;
        }
        if (whiteFilter.stream().anyMatch(element.getTags()::contains)) {
            t = DeviceType.WhiteTemperatureType;
        }
        if (colorFilter.stream().anyMatch(element.getTags()::contains)) {
            t = DeviceType.ColorType;
        }

        // Last but not least, the item type
        if (t == null) {
            switch (element.getType()) {
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

    @SuppressWarnings({ "unused", "null" })
    @Override
    public void added(Item element) {
        // Only allowed types
        if (!ALLOWED_ITEM_TYPES.contains(element.getType())) {
            return;
        }

        DeviceType t = determineTargetType(element);
        if (t == null) {
            return;
        }

        logger.debug("Add item {}", element.getUID());

        Integer hueID = itemUIDtoHueID.get(element.getUID());

        boolean itemAssociationCreated = false;
        if (hueID == null) {
            hueID = generateNextHueID();
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
        if (itemAssociationCreated) {
            writeToFile();
        }
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
    public void removed(Item element) {
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
    public void updated(Item oldElement, Item element) {
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
        DeviceType t = determineTargetType(element);
        if (t == null) {
            removed(element);
            return;
        }

        hueDevice.updateItem(element);
    }
}
