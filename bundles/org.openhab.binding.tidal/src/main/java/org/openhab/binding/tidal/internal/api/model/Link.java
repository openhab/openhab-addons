package org.openhab.binding.tidal.internal.api.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class Link<T extends BaseEntry> {
    private List<T> data;
    private String href;
    private String self;

    public List<T> getData() {
        return data;
    }

    public void resolveDeps(Hashtable<String, BaseEntry> dict) {
        List<T> originalList = (List<T>) ((ArrayList<T>) data).clone();

        for (int idx = 0; idx < originalList.size(); idx++) {
            T entry = originalList.get(idx);
            String entryId = entry.getId();

            if (dict.containsKey(entryId)) {

                data.remove(idx);
                data.add((T) dict.get(entryId));
            }
        }

    }
}
