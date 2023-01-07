/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
