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

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * generated with https://www.jsonschema2pojo.org/
 * 
 * @author Bo Biene - Initial contribution
 */
@Generated("jsonschema2pojo")
public class GetSystemStateListDTO {

    @SerializedName("SystemId")
    @Expose
    private Integer systemId;
    @SerializedName("GatewayState")
    @Expose
    private GatewayStateDTO gatewayState;
    @SerializedName("AccessLevel")
    @Expose
    private Integer accessLevel;
    @SerializedName("IsSystemShareDeleted")
    @Expose
    private Boolean isSystemShareDeleted;
    @SerializedName("IsSystemShareRejected")
    @Expose
    private Boolean isSystemShareRejected;
    @SerializedName("IsSystemDeleted")
    @Expose
    private Boolean isSystemDeleted;
    @SerializedName("FirstActiveAlertCode")
    @Expose
    private Integer firstActiveAlertCode;

    public Integer getSystemId() {
        return systemId;
    }

    public void setSystemId(Integer systemId) {
        this.systemId = systemId;
    }

    public GatewayStateDTO getGatewayState() {
        return gatewayState;
    }

    public void setGatewayState(GatewayStateDTO gatewayState) {
        this.gatewayState = gatewayState;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Boolean getIsSystemShareDeleted() {
        return isSystemShareDeleted;
    }

    public void setIsSystemShareDeleted(Boolean isSystemShareDeleted) {
        this.isSystemShareDeleted = isSystemShareDeleted;
    }

    public Boolean getIsSystemShareRejected() {
        return isSystemShareRejected;
    }

    public void setIsSystemShareRejected(Boolean isSystemShareRejected) {
        this.isSystemShareRejected = isSystemShareRejected;
    }

    public Boolean getIsSystemDeleted() {
        return isSystemDeleted;
    }

    public void setIsSystemDeleted(Boolean isSystemDeleted) {
        this.isSystemDeleted = isSystemDeleted;
    }

    public Integer getFirstActiveAlertCode() {
        return firstActiveAlertCode;
    }

    public void setFirstActiveAlertCode(Integer firstActiveAlertCode) {
        this.firstActiveAlertCode = firstActiveAlertCode;
    }
}
