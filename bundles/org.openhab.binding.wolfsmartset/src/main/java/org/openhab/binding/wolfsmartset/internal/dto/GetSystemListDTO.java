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

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * generated with https://www.jsonschema2pojo.org/
 * 
 * @author Bo Biene - Initial contribution
 */
@Generated("jsonschema2pojo")
public class GetSystemListDTO {

    @SerializedName("Id")
    @Expose
    private Integer id;
    @SerializedName("GatewayId")
    @Expose
    private Integer gatewayId;
    @SerializedName("IsForeignSystem")
    @Expose
    private Boolean isForeignSystem;
    @SerializedName("AccessLevel")
    @Expose
    private Integer accessLevel;
    @SerializedName("GatewayUsername")
    @Expose
    private String gatewayUsername;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("SystemShares")
    @Expose
    private List<Object> systemShares = null;
    @SerializedName("GatewaySoftwareVersion")
    @Expose
    private String gatewaySoftwareVersion;
    @SerializedName("UserNameOwner")
    @Expose
    private String userNameOwner;
    @SerializedName("SystemShareId")
    @Expose
    private Integer systemShareId;
    @SerializedName("OperatorName")
    @Expose
    private String operatorName;
    @SerializedName("Location")
    @Expose
    private String location;
    @SerializedName("InstallationDate")
    @Expose
    private String installationDate;
    @SerializedName("ImageId")
    @Expose
    private Integer imageId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(Integer gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Boolean getIsForeignSystem() {
        return isForeignSystem;
    }

    public void setIsForeignSystem(Boolean isForeignSystem) {
        this.isForeignSystem = isForeignSystem;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getGatewayUsername() {
        return gatewayUsername;
    }

    public void setGatewayUsername(String gatewayUsername) {
        this.gatewayUsername = gatewayUsername;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getSystemShares() {
        return systemShares;
    }

    public void setSystemShares(List<Object> systemShares) {
        this.systemShares = systemShares;
    }

    public String getGatewaySoftwareVersion() {
        return gatewaySoftwareVersion;
    }

    public void setGatewaySoftwareVersion(String gatewaySoftwareVersion) {
        this.gatewaySoftwareVersion = gatewaySoftwareVersion;
    }

    public String getUserNameOwner() {
        return userNameOwner;
    }

    public void setUserNameOwner(String userNameOwner) {
        this.userNameOwner = userNameOwner;
    }

    public @Nullable Integer getSystemShareId() {
        return systemShareId;
    }

    public void setSystemShareId(Integer systemShareId) {
        this.systemShareId = systemShareId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(String installationDate) {
        this.installationDate = installationDate;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }
}
