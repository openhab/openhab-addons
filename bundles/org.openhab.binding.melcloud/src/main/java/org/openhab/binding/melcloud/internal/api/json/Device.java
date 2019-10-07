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
package org.openhab.binding.melcloud.internal.api.json;

import java.security.Permissions;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link Device} is responsible of JSON data For MELCloud API
 * Device Structure.
 * Generated with jsonschema2pojo
 *
 * @author Luca Calcaterra - Initial contribution
 */

public class Device {

    @SerializedName("DeviceID")
    @Expose
    private Integer deviceID;
    @SerializedName("DeviceName")
    @Expose
    private String deviceName;
    @SerializedName("BuildingID")
    @Expose
    private Integer buildingID;
    @SerializedName("BuildingName")
    @Expose
    private Object buildingName;
    @SerializedName("FloorID")
    @Expose
    private Object floorID;
    @SerializedName("FloorName")
    @Expose
    private Object floorName;
    @SerializedName("AreaID")
    @Expose
    private Object areaID;
    @SerializedName("AreaName")
    @Expose
    private Object areaName;
    @SerializedName("ImageID")
    @Expose
    private Integer imageID;
    @SerializedName("InstallationDate")
    @Expose
    private String installationDate;
    @SerializedName("LastServiceDate")
    @Expose
    private Object lastServiceDate;
    @SerializedName("Presets")
    @Expose
    private List<Preset> presets = null;
    @SerializedName("OwnerID")
    @Expose
    private Object ownerID;
    @SerializedName("OwnerName")
    @Expose
    private Object ownerName;
    @SerializedName("OwnerEmail")
    @Expose
    private Object ownerEmail;
    @SerializedName("AccessLevel")
    @Expose
    private Integer accessLevel;
    @SerializedName("DirectAccess")
    @Expose
    private Boolean directAccess;
    @SerializedName("EndDate")
    @Expose
    private String endDate;
    @SerializedName("Zone1Name")
    @Expose
    private Object zone1Name;
    @SerializedName("Zone2Name")
    @Expose
    private Object zone2Name;
    @SerializedName("MinTemperature")
    @Expose
    private Integer minTemperature;
    @SerializedName("MaxTemperature")
    @Expose
    private Integer maxTemperature;
    @SerializedName("HideVaneControls")
    @Expose
    private Boolean hideVaneControls;
    @SerializedName("HideDryModeControl")
    @Expose
    private Boolean hideDryModeControl;
    @SerializedName("HideRoomTemperature")
    @Expose
    private Boolean hideRoomTemperature;
    @SerializedName("HideSupplyTemperature")
    @Expose
    private Boolean hideSupplyTemperature;
    @SerializedName("HideOutdoorTemperature")
    @Expose
    private Boolean hideOutdoorTemperature;
    @SerializedName("BuildingCountry")
    @Expose
    private Object buildingCountry;
    @SerializedName("OwnerCountry")
    @Expose
    private Object ownerCountry;
    @SerializedName("AdaptorType")
    @Expose
    private Integer adaptorType;
    @SerializedName("Type")
    @Expose
    private Integer type;
    @SerializedName("MacAddress")
    @Expose
    private String macAddress;
    @SerializedName("SerialNumber")
    @Expose
    private String serialNumber;
    @SerializedName("Device")
    @Expose
    private DeviceProps device;
    @SerializedName("DiagnosticMode")
    @Expose
    private Integer diagnosticMode;
    @SerializedName("DiagnosticEndDate")
    @Expose
    private Object diagnosticEndDate;
    @SerializedName("Location")
    @Expose
    private Integer location;
    @SerializedName("DetectedCountry")
    @Expose
    private Object detectedCountry;
    @SerializedName("Registrations")
    @Expose
    private Integer registrations;
    @SerializedName("LocalIPAddress")
    @Expose
    private Object localIPAddress;
    @SerializedName("TimeZone")
    @Expose
    private Integer timeZone;
    @SerializedName("RegistReason")
    @Expose
    private Object registReason;
    @SerializedName("ExpectedCommand")
    @Expose
    private Integer expectedCommand;
    @SerializedName("RegistRetry")
    @Expose
    private Integer registRetry;
    @SerializedName("DateCreated")
    @Expose
    private String dateCreated;
    @SerializedName("FirmwareDeployment")
    @Expose
    private Object firmwareDeployment;
    @SerializedName("FirmwareUpdateAborted")
    @Expose
    private Boolean firmwareUpdateAborted;
    @SerializedName("Permissions")
    @Expose
    private Permissions permissions;

    public Integer getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(Integer deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getBuildingID() {
        return buildingID;
    }

    public void setBuildingID(Integer buildingID) {
        this.buildingID = buildingID;
    }

    public Object getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(Object buildingName) {
        this.buildingName = buildingName;
    }

    public Object getFloorID() {
        return floorID;
    }

    public void setFloorID(Object floorID) {
        this.floorID = floorID;
    }

    public Object getFloorName() {
        return floorName;
    }

    public void setFloorName(Object floorName) {
        this.floorName = floorName;
    }

    public Object getAreaID() {
        return areaID;
    }

    public void setAreaID(Object areaID) {
        this.areaID = areaID;
    }

    public Object getAreaName() {
        return areaName;
    }

    public void setAreaName(Object areaName) {
        this.areaName = areaName;
    }

    public Integer getImageID() {
        return imageID;
    }

    public void setImageID(Integer imageID) {
        this.imageID = imageID;
    }

    public String getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(String installationDate) {
        this.installationDate = installationDate;
    }

    public Object getLastServiceDate() {
        return lastServiceDate;
    }

    public void setLastServiceDate(Object lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
    }

    public List<Preset> getPresets() {
        return presets;
    }

    public void setPresets(List<Preset> presets) {
        this.presets = presets;
    }

    public Object getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(Object ownerID) {
        this.ownerID = ownerID;
    }

    public Object getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(Object ownerName) {
        this.ownerName = ownerName;
    }

    public Object getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(Object ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Boolean getDirectAccess() {
        return directAccess;
    }

    public void setDirectAccess(Boolean directAccess) {
        this.directAccess = directAccess;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Object getZone1Name() {
        return zone1Name;
    }

    public void setZone1Name(Object zone1Name) {
        this.zone1Name = zone1Name;
    }

    public Object getZone2Name() {
        return zone2Name;
    }

    public void setZone2Name(Object zone2Name) {
        this.zone2Name = zone2Name;
    }

    public Integer getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(Integer minTemperature) {
        this.minTemperature = minTemperature;
    }

    public Integer getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(Integer maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public Boolean getHideVaneControls() {
        return hideVaneControls;
    }

    public void setHideVaneControls(Boolean hideVaneControls) {
        this.hideVaneControls = hideVaneControls;
    }

    public Boolean getHideDryModeControl() {
        return hideDryModeControl;
    }

    public void setHideDryModeControl(Boolean hideDryModeControl) {
        this.hideDryModeControl = hideDryModeControl;
    }

    public Boolean getHideRoomTemperature() {
        return hideRoomTemperature;
    }

    public void setHideRoomTemperature(Boolean hideRoomTemperature) {
        this.hideRoomTemperature = hideRoomTemperature;
    }

    public Boolean getHideSupplyTemperature() {
        return hideSupplyTemperature;
    }

    public void setHideSupplyTemperature(Boolean hideSupplyTemperature) {
        this.hideSupplyTemperature = hideSupplyTemperature;
    }

    public Boolean getHideOutdoorTemperature() {
        return hideOutdoorTemperature;
    }

    public void setHideOutdoorTemperature(Boolean hideOutdoorTemperature) {
        this.hideOutdoorTemperature = hideOutdoorTemperature;
    }

    public Object getBuildingCountry() {
        return buildingCountry;
    }

    public void setBuildingCountry(Object buildingCountry) {
        this.buildingCountry = buildingCountry;
    }

    public Object getOwnerCountry() {
        return ownerCountry;
    }

    public void setOwnerCountry(Object ownerCountry) {
        this.ownerCountry = ownerCountry;
    }

    public Integer getAdaptorType() {
        return adaptorType;
    }

    public void setAdaptorType(Integer adaptorType) {
        this.adaptorType = adaptorType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public DeviceProps getDeviceProps() {
        return device;
    }

    public void setDeviceProps(DeviceProps device) {
        this.device = device;
    }

    public Integer getDiagnosticMode() {
        return diagnosticMode;
    }

    public void setDiagnosticMode(Integer diagnosticMode) {
        this.diagnosticMode = diagnosticMode;
    }

    public Object getDiagnosticEndDate() {
        return diagnosticEndDate;
    }

    public void setDiagnosticEndDate(Object diagnosticEndDate) {
        this.diagnosticEndDate = diagnosticEndDate;
    }

    public Integer getLocation() {
        return location;
    }

    public void setLocation(Integer location) {
        this.location = location;
    }

    public Object getDetectedCountry() {
        return detectedCountry;
    }

    public void setDetectedCountry(Object detectedCountry) {
        this.detectedCountry = detectedCountry;
    }

    public Integer getRegistrations() {
        return registrations;
    }

    public void setRegistrations(Integer registrations) {
        this.registrations = registrations;
    }

    public Object getLocalIPAddress() {
        return localIPAddress;
    }

    public void setLocalIPAddress(Object localIPAddress) {
        this.localIPAddress = localIPAddress;
    }

    public Integer getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(Integer timeZone) {
        this.timeZone = timeZone;
    }

    public Object getRegistReason() {
        return registReason;
    }

    public void setRegistReason(Object registReason) {
        this.registReason = registReason;
    }

    public Integer getExpectedCommand() {
        return expectedCommand;
    }

    public void setExpectedCommand(Integer expectedCommand) {
        this.expectedCommand = expectedCommand;
    }

    public Integer getRegistRetry() {
        return registRetry;
    }

    public void setRegistRetry(Integer registRetry) {
        this.registRetry = registRetry;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Object getFirmwareDeployment() {
        return firmwareDeployment;
    }

    public void setFirmwareDeployment(Object firmwareDeployment) {
        this.firmwareDeployment = firmwareDeployment;
    }

    public Boolean getFirmwareUpdateAborted() {
        return firmwareUpdateAborted;
    }

    public void setFirmwareUpdateAborted(Boolean firmwareUpdateAborted) {
        this.firmwareUpdateAborted = firmwareUpdateAborted;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

}
