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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link TouchWandUnitData} implements unit property.
 *
 * @author Roie Geron - Initial contribution
 */
public abstract class TouchWandUnitData {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("nodeId")
    @Expose
    private Integer nodeId;
    @SerializedName("epId")
    @Expose
    private Integer epId;
    @SerializedName("icon")
    @Expose
    private Object icon;
    @SerializedName("connectivity")
    @Expose
    private String connectivity;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("isFavorite")
    @Expose
    private Object isFavorite;
    @SerializedName("errorCode")
    @Expose
    private Object errorCode;
    @SerializedName("hasPowerMeter")
    @Expose
    private Object hasPowerMeter;
    @SerializedName("hasBattery")
    @Expose
    private Object hasBattery;
    @SerializedName("config")
    @Expose
    private Object config;
    @SerializedName("association")
    @Expose
    private Object association;
    @SerializedName("customOp")
    @Expose
    private Object customOp;
    @SerializedName("isHidden")
    @Expose
    private Object isHidden;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;
    @SerializedName("updatedAt")
    @Expose
    private String updatedAt;
    @SerializedName("roomId")
    @Expose
    private Object roomId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getEpId() {
        return epId;
    }

    public void setEpId(Integer epId) {
        this.epId = epId;
    }

    public Object getIcon() {
        return icon;
    }

    public void setIcon(Object icon) {
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

    public Object getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Object isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Object getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Object errorCode) {
        this.errorCode = errorCode;
    }

    public Object getHasPowerMeter() {
        return hasPowerMeter;
    }

    public void setHasPowerMeter(Object hasPowerMeter) {
        this.hasPowerMeter = hasPowerMeter;
    }

    public Object getHasBattery() {
        return hasBattery;
    }

    public void setHasBattery(Object hasBattery) {
        this.hasBattery = hasBattery;
    }

    public Object getConfig() {
        return config;
    }

    public void setConfig(Object config) {
        this.config = config;
    }

    public Object getAssociation() {
        return association;
    }

    public void setAssociation(Object association) {
        this.association = association;
    }

    public Object getCustomOp() {
        return customOp;
    }

    public void setCustomOp(Object customOp) {
        this.customOp = customOp;
    }

    public Object getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Object isHidden) {
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

    public Object getRoomId() {
        return roomId;
    }

    public void setRoomId(Object roomId) {
        this.roomId = roomId;
    }

    public abstract Object getCurrStatus();
}
