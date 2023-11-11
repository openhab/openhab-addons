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
package org.openhab.binding.kermi.internal.api;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marco Descher - Initial contribution
 */
public class DeviceInfo {

    @SerializedName("DeviceId")
    private String deviceId;

    @SerializedName("Protocol")
    private int protocol;

    @SerializedName("PortAddress")
    private String portAddress;

    @SerializedName("SoftwareVersion")
    private String softwareVersion;

    @SerializedName("Address")
    private String address;

    @SerializedName("HomeServerSenderAddress")
    private String homeServerAddress;

    @SerializedName("Name")
    private String name;

    @SerializedName("Description")
    private String description;

    @SerializedName("Serial")
    private String serial;

    @SerializedName("ParentMenuEntryId")
    private String parentMenuEntryId;

    @SerializedName("ParentDeviceId")
    private String parentDeviceId;

    @SerializedName("DeviceType")
    private String deviceType;

    @SerializedName("DeviceOptions")
    private List<DeviceOption> deviceOptions;

    @SerializedName("VisualizationDatapoints")
    private List<VisualizationDatapoint> visualizationDatapoints;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public String getPortAddress() {
        return portAddress;
    }

    public void setPortAddress(String portAddress) {
        this.portAddress = portAddress;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHomeServerAddress() {
        return homeServerAddress;
    }

    public void setHomeServerAddress(String homeServerAddress) {
        this.homeServerAddress = homeServerAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getParentMenuEntryId() {
        return parentMenuEntryId;
    }

    public void setParentMenuEntryId(String parentMenuEntryId) {
        this.parentMenuEntryId = parentMenuEntryId;
    }

    public String getParentDeviceId() {
        return parentDeviceId;
    }

    public void setParentDeviceId(String parentDeviceId) {
        this.parentDeviceId = parentDeviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<DeviceOption> getDeviceOptions() {
        return deviceOptions;
    }

    public void setDeviceOptions(List<DeviceOption> deviceOptions) {
        this.deviceOptions = deviceOptions;
    }

    public List<VisualizationDatapoint> getVisualizationDatapoints() {
        return visualizationDatapoints;
    }

    public void setVisualizationDatapoints(List<VisualizationDatapoint> visualizationDatapoints) {
        this.visualizationDatapoints = visualizationDatapoints;
    }
}
