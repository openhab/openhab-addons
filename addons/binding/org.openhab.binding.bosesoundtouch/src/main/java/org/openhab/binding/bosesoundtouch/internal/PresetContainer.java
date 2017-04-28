/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.openhab.binding.bosesoundtouch.internal.exceptions.ContentItemNotPresetableException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoPresetFoundException;

/**
 * The {@link PresetContainer} class manages a PresetContainer which contains all additional Presets
 *
 * @author Thomas Traunbauer
 */
public class PresetContainer {
    private HashMap<Integer, ContentItem> mapOfPresets;
    private File presetFile;

    /**
     * Creates a new instance of this class
     *
     * @throws IOException if PresetFile could not be read
     */
    public PresetContainer(File presetFile) throws IOException {
        this.presetFile = presetFile;
        this.mapOfPresets = new HashMap<Integer, ContentItem>();
        readFromFile(presetFile);
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
     * Adds a ContentItem as Preset, with presetID. Note that a eventually existing id in preset will be overwritten by
     * presetID
     *
     * @param presetID
     * @param preset
     *
     * @throws ContentItemNotPresetableException if ContentItem is not presetable
     * @throws IOException if Presets could not be saved to file
     */
    public void put(int presetID, ContentItem preset) throws ContentItemNotPresetableException, IOException {
        preset.setPresetID(presetID);
        if (preset.isPresetable()) {
            mapOfPresets.put(presetID, preset);
            writeToFile(presetFile);
        } else {
            throw new ContentItemNotPresetableException();
        }
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

    private void writeToFile(File presetFile) throws IOException {
        if (presetFile.exists()) {
            presetFile.delete();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(presetFile));
        Collection<ContentItem> colletionOfPresets = getAllPresets();
        ArrayList<ContentItem> listOfPresets = new ArrayList<ContentItem>();
        listOfPresets.addAll(colletionOfPresets);
        // Only openhab Presets got saved
        for (int i = 6; i < listOfPresets.size(); i++) {
            ContentItem currentItem = listOfPresets.get(i);
            writer.write(currentItem.stringToSave());
            writer.newLine();
        }
        writer.close();
    }

    private void readFromFile(File presetFile) throws IOException {
        if (!presetFile.exists()) {
            throw new IOException("Could not load save PRESETS");
        }
        if (presetFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(presetFile));
                String line = reader.readLine();
                while (line != null) {
                    ContentItem item = new ContentItem();
                    item.createFormString(line);
                    try {
                        put(item.getPresetID(), item);
                    } catch (ContentItemNotPresetableException e) {
                    }
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                throw new IOException("Could not load save PRESETS");
            }
        }
    }
}
