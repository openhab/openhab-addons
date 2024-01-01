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
package org.openhab.binding.melcloud.internal.api.json;

import java.security.Permissions;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * The {@link Device} is responsible of JSON data For MELCloud API
 * Device Structure.
 * Generated with jsonschema2pojo
 *
 * @author Luca Calcaterra - Initial contribution
 */

public class Device {

    @Expose
    private Integer deviceID;

    @Expose
    private String deviceName;

    @Expose
    private Integer buildingID;

    @Expose
    private Object buildingName;

    @Expose
    private Object floorID;

    @Expose
    private Object floorName;

    @Expose
    private Object areaID;

    @Expose
    private Object areaName;

    @Expose
    private Integer imageID;

    @Expose
    private String installationDate;

    @Expose
    private Object lastServiceDate;

    @Expose
    private List<Preset> presets = null;

    @Expose
    private Object ownerID;

    @Expose
    private Object ownerName;

    @Expose
    private Object ownerEmail;

    @Expose
    private Integer accessLevel;

    @Expose
    private Boolean directAccess;

    @Expose
    private String endDate;

    @Expose
    private Object zone1Name;

    @Expose
    private Object zone2Name;

    @Expose
    private Integer minTemperature;

    @Expose
    private Integer maxTemperature;

    @Expose
    private Boolean hideVaneControls;

    @Expose
    private Boolean hideDryModeControl;

    @Expose
    private Boolean hideRoomTemperature;

    @Expose
    private Boolean hideSupplyTemperature;

    @Expose
    private Boolean hideOutdoorTemperature;

    @Expose
    private Object buildingCountry;

    @Expose
    private Object ownerCountry;

    @Expose
    private Integer adaptorType;

    @Expose
    private Integer type;

    @Expose
    private String macAddress;

    @Expose
    private String serialNumber;

    @Expose
    private DeviceProps device;

    @Expose
    private Integer diagnosticMode;

    @Expose
    private Object diagnosticEndDate;

    @Expose
    private Integer location;

    @Expose
    private Object detectedCountry;

    @Expose
    private Integer registrations;

    @Expose
    private Object localIPAddress;

    @Expose
    private Integer timeZone;

    @Expose
    private Object registReason;

    @Expose
    private Integer expectedCommand;

    private Integer registRetry;

    @Expose
    private String dateCreated;

    @Expose
    private Object firmwareDeployment;

    @Expose
    private Boolean firmwareUpdateAborted;

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
