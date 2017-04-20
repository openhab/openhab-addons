/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.items;

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

    public PresetContainer() {
        mapOfPresets = new HashMap<Integer, ContentItem>();
    }

    public Collection<ContentItem> values() {
        return mapOfPresets.values();
    }

    public void put(int presetID, ContentItem preset) throws ContentItemNotPresetableException {
        preset.setPresetID(presetID);
        if (preset.isPresetable()) {
            mapOfPresets.put(presetID, preset);
        } else {
            throw new ContentItemNotPresetableException();
        }
    }

    public void put(ContentItem preset) throws ContentItemNotPresetableException {
        put(preset.getPresetID(), preset);
    }

    public ContentItem get(int presetID) throws NoPresetFoundException {
        ContentItem psFound = mapOfPresets.get(presetID);
        if (psFound != null) {
            return psFound;
        } else {
            throw new NoPresetFoundException();
        }
    }

    public ContentItem getNext(ContentItem currentContentItem) throws NoPresetFoundException {
        ContentItem psFound = null;
        Collection<ContentItem> listOfPresets = values();
        for (ContentItem ps : listOfPresets) {
            if (ps.getLocation().equals(currentContentItem.getLocation())) {
                psFound = ps;
            }
        }

        if (psFound != null) {
            psFound = mapOfPresets.get(psFound.getPresetID() + 1);
        } else {
            throw new NoPresetFoundException();
        }

        if (psFound != null) {
            return psFound;
        } else {
            throw new NoPresetFoundException();
        }
    }

    public ContentItem getPrev(ContentItem currentContentItem) throws NoPresetFoundException {
        ContentItem psFound = null;
        Collection<ContentItem> listOfPresets = values();
        for (ContentItem ps : listOfPresets) {
            if (ps.getLocation().equals(currentContentItem.getLocation())) {
                psFound = ps;
            }
        }

        if (psFound != null) {
            psFound = mapOfPresets.get(psFound.getPresetID() - 1);
        } else {
            throw new NoPresetFoundException();
        }

        if (psFound != null) {
            return psFound;
        } else {
            throw new NoPresetFoundException();
        }
    }

    public void writeToFile(File presetFile) throws IOException {
        if (presetFile.exists()) {
            presetFile.delete();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(presetFile));
        Collection<ContentItem> colletionOfPresets = values();
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

    public void readFromFile(File presetFile) throws IOException {
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
                        put(item);
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
