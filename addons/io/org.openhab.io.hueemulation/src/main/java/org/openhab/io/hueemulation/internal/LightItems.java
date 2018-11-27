/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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
 * A file in userdata/hueemulation/itemUIDtoHueID is used to keep track of the used integer ids.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class LightItems implements RegistryChangeListener<Item> {
    private final Logger logger = LoggerFactory.getLogger(LightItems.class);
    private static final File ITEM_FILE = new File(
            ConfigConstants.getUserDataFolder() + File.separator + "hueemulation" + File.separator + "itemUIDtoHueID");
    private static final Set<String> ALLOWED_ITEM_TYPES = Stream
            .of(CoreItemFactory.COLOR, CoreItemFactory.DIMMER, CoreItemFactory.SWITCH).collect(Collectors.toSet());

    // deviceMap maps a unique Item id to a Hue numeric id
    private final TreeMap<String, Integer> itemUIDtoHueID = new TreeMap<>();
    private final HueDataStore dataStore;
    private Set<String> switchFilter = Collections.emptySet();
    private Set<String> colorFilter = Collections.emptySet();
    private Set<String> whiteFilter = Collections.emptySet();
    private final Gson gson;

    public LightItems(HueDataStore ds, Gson gson) {
        dataStore = ds;
        this.gson = gson;
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
     * Load the userdata/hueemulation/itemUIDtoHueID file into {@link #itemUIDtoHueID}.
     */
    public void loadMappingFromFile() {
        // load item list from disk
        if (ITEM_FILE.exists()) {
            try (JsonReader reader = new JsonReader(new FileReader(ITEM_FILE))) {
                Map<String, Integer> tmpMap;
                tmpMap = gson.fromJson(reader, new TypeToken<Map<String, Integer>>() {
                }.getType());
                if (tmpMap != null) {
                    itemUIDtoHueID.putAll(tmpMap);
                }
            } catch (IOException e) {
                logger.warn("File {} error", ITEM_FILE, e);
            }
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
     * Saves the ID->Item association to file.
     */
    private void writeToFile() {
        try (JsonWriter writer = new JsonWriter(new FileWriter(ITEM_FILE))) {
            gson.toJson(itemUIDtoHueID, new TypeToken<Map<String, Integer>>() {
            }.getType(), writer);
        } catch (IOException e) {
            logger.error("Could not persist item cache", e);
        }
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

        Integer hueID = itemUIDtoHueID.get(element.getUID());

        boolean itemAssociationCreated = false;
        if (hueID == null) {
            hueID = generateNextHueID();
            itemAssociationCreated = true;
        }

        try {
            HueDevice device = new HueDevice(element, UDN.getUDN() + "-" + hueID.toString(), t);
            device.item = element;
            dataStore.lights.put(hueID, device);
            updateGroup0();
            itemUIDtoHueID.put(element.getUID(), hueID);
            if (itemAssociationCreated) {
                writeToFile();
            }
        } catch (IOException e) {
            logger.warn("IO failed", e);
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
        dataStore.lights.remove(hueID);
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
