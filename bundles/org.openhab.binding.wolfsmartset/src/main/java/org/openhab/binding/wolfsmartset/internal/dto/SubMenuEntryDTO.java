/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wolfsmartset.internal.dto;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * generated with https://www.jsonschema2pojo.org/
 * 
 * @author Bo Biene - Initial contribution
 */
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
