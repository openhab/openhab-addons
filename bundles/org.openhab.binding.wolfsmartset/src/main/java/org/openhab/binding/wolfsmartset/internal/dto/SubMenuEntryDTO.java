
package org.openhab.binding.wolfsmartset.internal.dto;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class SubMenuEntryDTO {

    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("SubMenuEntries")
    @Expose
    private List<Object> subMenuEntries = null;
    @SerializedName("ParameterNode")
    @Expose
    private Boolean parameterNode;
    @SerializedName("ImageName")
    @Expose
    private String imageName;
    @SerializedName("TabViews")
    @Expose
    private List<MenuItemTabViewDTO> tabViews = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getSubMenuEntries() {
        return subMenuEntries;
    }

    public void setSubMenuEntries(List<Object> subMenuEntries) {
        this.subMenuEntries = subMenuEntries;
    }

    public Boolean getParameterNode() {
        return parameterNode;
    }

    public void setParameterNode(Boolean parameterNode) {
        this.parameterNode = parameterNode;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public List<MenuItemTabViewDTO> getTabViews() {
        return tabViews;
    }

    public void setTabViews(List<MenuItemTabViewDTO> tabViews) {
        this.tabViews = tabViews;
    }
}
