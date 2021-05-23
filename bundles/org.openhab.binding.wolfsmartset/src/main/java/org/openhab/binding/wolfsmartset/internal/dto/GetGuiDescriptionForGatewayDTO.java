
package org.openhab.binding.wolfsmartset.internal.dto;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class GetGuiDescriptionForGatewayDTO {

    @SerializedName("MenuItems")
    @Expose
    private List<MenuItemDTO> menuItems = null;
    @SerializedName("DynFaultMessageDevices")
    @Expose
    private List<Object> dynFaultMessageDevices = null;
    @SerializedName("SystemHasWRSClassicDevices")
    @Expose
    private Boolean systemHasWRSClassicDevices;

    public List<MenuItemDTO> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemDTO> menuItems) {
        this.menuItems = menuItems;
    }

    public List<Object> getDynFaultMessageDevices() {
        return dynFaultMessageDevices;
    }

    public void setDynFaultMessageDevices(List<Object> dynFaultMessageDevices) {
        this.dynFaultMessageDevices = dynFaultMessageDevices;
    }

    public Boolean getSystemHasWRSClassicDevices() {
        return systemHasWRSClassicDevices;
    }

    public void setSystemHasWRSClassicDevices(Boolean systemHasWRSClassicDevices) {
        this.systemHasWRSClassicDevices = systemHasWRSClassicDevices;
    }
}
