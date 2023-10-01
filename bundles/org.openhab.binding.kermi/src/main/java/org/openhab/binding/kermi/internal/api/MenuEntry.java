package org.openhab.binding.kermi.internal.api;

import com.google.gson.annotations.SerializedName;

public class MenuEntry {

    @SerializedName("MenuEntryId")
    private String menuEntryId;

    @SerializedName("ParentMenuEntryId")
    private String parentMenuEntryId;

    public String getMenuEntryId() {
        return menuEntryId;
    }

    public void setMenuEntryId(String menuEntryId) {
        this.menuEntryId = menuEntryId;
    }

    public String getParentMenuEntryId() {
        return parentMenuEntryId;
    }

    public void setParentMenuEntryId(String parentMenuEntryId) {
        this.parentMenuEntryId = parentMenuEntryId;
    }

}
