package org.openhab.binding.tidal.internal.api.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class Link<T extends BaseEntry> {
    private ArrayList<T> data;
    private String href;
    private String self;

    public List<T> getData() {
        return data;
    }

    public void resolveDeps(Hashtable<String, T> dict) {
        if (data == null) {
            return;
        }

        ArrayList<T> originalList = new ArrayList<>(data);

        for (T entry : originalList) {
            String entryId = entry.getId();

            if (dict.containsKey(entryId)) {
                BaseEntry newEntry = dict.get(entryId);
                data.removeIf(entryToRemove -> entryToRemove.getId().equals(entryId));
                data.add((T) newEntry);

                if (newEntry.getRelationShip() != null) {
                    newEntry.getRelationShip().resolveDeps((Hashtable<String, BaseEntry>) dict);
                }
            }
        }
    }

    public String getHref() {
        return href;
    }

    public String getSelf() {
        return self;
    }
}
