package org.openhab.binding.siemenshvac.internal.Metadata;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class SiemensHvacMetadataMenu extends SiemensHvacMetadata {
    private LinkedHashMap<String, SiemensHvacMetadata> childList;

    public SiemensHvacMetadataMenu() {
        childList = new LinkedHashMap<String, SiemensHvacMetadata>();
    }

    public void AddChild(SiemensHvacMetadata information) {
        childList.put(information.getLongDesc(), information);
    }

    public HashMap<String, SiemensHvacMetadata> getChilds() {
        return this.childList;
    }

}
