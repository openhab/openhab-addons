/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.touchwand.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link TouchWandUnitData} implements unit property.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public abstract class TouchWandUnitData {

    @SerializedName("id")
    @Expose
    private Integer id = 0;
    @SerializedName("name")
    @Expose
    private String name = "";
    @SerializedName("type")
    @Expose
    private String type = "";
    @SerializedName("nodeId")
    @Expose
    private Integer nodeId = 0;
    @SerializedName("epId")
    @Expose
    private Integer epId = 0;
    @SerializedName("icon")
    @Expose
    private String icon = "";
    @SerializedName("connectivity")
    @Expose
    private String connectivity = "";
    @SerializedName("status")
    @Expose
    private String status = "";
    @SerializedName("isFavorite")
    @Expose
    private Integer isFavorite = 0;
    @SerializedName("errorCode")
    @Expose
    private Integer errorCode = 0;
    @SerializedName("hasPowerMeter")
    @Expose
    private boolean hasPowerMeter;
    @SerializedName("hasBattery")
    @Expose
    private boolean hasBattery;
    @SerializedName("config")
    @Expose
    private String config = "";
    @SerializedName("association")
    @Expose
    private String association = "";
    @SerializedName("customOp")
    @Expose
    private String customOp = "";
    @SerializedName("isHidden")
    @Expose
    private boolean isHidden = false;
    @SerializedName("createdAt")
    @Expose
    private String createdAt = "";
    @SerializedName("updatedAt")
    @Expose
    private String updatedAt = "";
    @SerializedName("roomId")
    @Expose
    private Integer roomId = 0;

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getEpId() {
        return epId;
    }

    public void setEpId(int epId) {
        this.epId = epId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(String connectivity) {
        this.connectivity = connectivity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Integer isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public boolean getHasPowerMeter() {
        return hasPowerMeter;
    }

    public void setHasPowerMeter(boolean hasPowerMeter) {
        this.hasPowerMeter = hasPowerMeter;
    }

    public boolean getHasBattery() {
        return hasBattery;
    }

    public void setHasBattery(boolean hasBattery) {
        this.hasBattery = hasBattery;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getAssociation() {
        return association;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public String getCustomOp() {
        return customOp;
    }

    public void setCustomOp(String customOp) {
        this.customOp = customOp;
    }

    public boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public abstract Integer getCurrStatus();
}
