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
package org.openhab.binding.bosesoundtouch.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.DeletableStorage;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PresetContainer} class manages a PresetContainer which contains all additional Presets
 *
 * @author Thomas Traunbauer - Initial contribution
 * @author Kai Kreuzer - Refactored it to use storage instead of file
 */
@NonNullByDefault
public class PresetContainer {

    private final Logger logger = LoggerFactory.getLogger(PresetContainer.class);

    private final Map<Integer, ContentItem> mapOfPresets = new HashMap<>();
    private Storage<ContentItem> storage;

    /**
     * Creates a new instance of this class
     */
    public PresetContainer(Storage<ContentItem> storage) {
        this.storage = storage;
        readFromStorage();
    }

    /**
     * Returns a Collection of all Presets
     *
     * @param operationModeType
     */
    public Collection<ContentItem> getAllPresets() {
        return mapOfPresets.values();
    }

    /**
     * Adds a ContentItem as Preset, with presetID. Note that an eventually existing id in preset will be overwritten by
     * presetID
     *
     * @param presetID
     * @param preset
     *
     * @throws ContentItemNotPresetableException if ContentItem is not presetable
     */
    public void put(int presetID, ContentItem preset) throws ContentItemNotPresetableException {
        preset.setPresetID(presetID);
        if (preset.isPresetable()) {
            mapOfPresets.put(presetID, preset);
            writeToStorage();
        } else {
            throw new ContentItemNotPresetableException();
        }
    }

    /**
     * Remove the Preset stored under the specified Id
     * 
     * @param presetID
     */
    public void remove(int presetID) {
        mapOfPresets.remove(presetID);
        writeToStorage();
    }

    /**
     * Returns the Preset with presetID
     *
     * @param presetID
     *
     * @throws NoPresetFoundException if Preset could not be found
     */
    public ContentItem get(int presetID) throws NoPresetFoundException {
        ContentItem psFound = mapOfPresets.get(presetID);
        if (psFound != null) {
            return psFound;
        } else {
            throw new NoPresetFoundException();
        }
    }

    /**
     * Deletes all presets from the storage.
     */
    public void clear() {
        if (storage instanceof DeletableStorage) {
            ((DeletableStorage<ContentItem>) storage).delete();
        } else {
            Collection<@NonNull String> keys = storage.getKeys();
            keys.forEach(key -> storage.remove(key));
        }
    }

    private void writeToStorage() {
        Collection<ContentItem> colletionOfPresets = getAllPresets();
        List<ContentItem> listOfPresets = new ArrayList<>();
        listOfPresets.addAll(colletionOfPresets);
        // Only binding presets get saved
        for (Iterator<ContentItem> cii = listOfPresets.iterator(); cii.hasNext();) {
            if (cii.next().getPresetID() <= 6) {
                cii.remove();
            }
        }

        if (!listOfPresets.isEmpty()) {
            listOfPresets.forEach(item -> storage.put(String.valueOf(item.getPresetID()), item));
        }
    }

    private void readFromStorage() {
        Collection<@Nullable ContentItem> items = storage.getValues();
        for (ContentItem item : items) {
            try {
                if (item != null) {
                    put(item.getPresetID(), item);
                }
            } catch (ContentItemNotPresetableException e) {
                logger.debug("Item '{}' is not presetable - ignoring it.", item.getItemName());
            }
        }
    }
}
