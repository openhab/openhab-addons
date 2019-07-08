/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.iaqualink.internal.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Device refers to a iAqualink Pool Controller.
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
public class Device {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("serial_number")
    @Expose
    private String serialNumber;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("device_type")
    @Expose
    private String deviceType;
    @SerializedName("owner_id")
    @Expose
    private Object ownerId;
    @SerializedName("updating")
    @Expose
    private Boolean updating;
    @SerializedName("firmware_version")
    @Expose
    private Object firmwareVersion;
    @SerializedName("target_firmware_version")
    @Expose
    private Object targetFirmwareVersion;
    @SerializedName("update_firmware_start_at")
    @Expose
    private Object updateFirmwareStartAt;
    @SerializedName("last_activity_at")
    @Expose
    private Object lastActivityAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Object getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Object ownerId) {
        this.ownerId = ownerId;
    }

    public Boolean getUpdating() {
        return updating;
    }

    public void setUpdating(Boolean updating) {
        this.updating = updating;
    }

    public Object getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(Object firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Object getTargetFirmwareVersion() {
        return targetFirmwareVersion;
    }

    public void setTargetFirmwareVersion(Object targetFirmwareVersion) {
        this.targetFirmwareVersion = targetFirmwareVersion;
    }

    public Object getUpdateFirmwareStartAt() {
        return updateFirmwareStartAt;
    }

    public void setUpdateFirmwareStartAt(Object updateFirmwareStartAt) {
        this.updateFirmwareStartAt = updateFirmwareStartAt;
    }

    public Object getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Object lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

}