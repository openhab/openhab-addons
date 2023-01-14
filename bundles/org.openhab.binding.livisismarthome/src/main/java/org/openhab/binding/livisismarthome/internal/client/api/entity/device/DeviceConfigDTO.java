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
package org.openhab.binding.livisismarthome.internal.client.api.entity.device;

import java.time.ZonedDateTime;

import org.openhab.binding.livisismarthome.internal.client.Util;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the configuration of the Device.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class DeviceConfigDTO {

    private String name;
    private String protocolId;
    private String timeOfAcceptance;
    private String timeOfDiscovery;
    private String hardwareVersion;
    private String softwareVersion;
    private String firmwareVersion;
    private String hostName;
    private boolean activityLogEnabled;
    private String configurationState;
    @SerializedName("IPAddress")
    private String ipAddress;
    @SerializedName("MACAddress")
    private String macAddress;
    private String registrationTime;
    private String timeZone;
    private String shcType;
    private String geoLocation;
    private Double currentUTCOffset;
    private Boolean backendConnectionMonitored;
    @SerializedName("RFCommFailureNotification")
    private Boolean rfCommFailureNotification;
    private String displayCurrentTemperature;
    private String underlyingDeviceIds;
    private String meterId;
    private String meterFirmwareVersion;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the protocolId
     */
    public String getProtocolId() {
        return protocolId;
    }

    /**
     * @param protocolId the protocolId to set
     */
    public void setProtocolId(String protocolId) {
        this.protocolId = protocolId;
    }

    /**
     * Returns the time, when the {@link DeviceDTO} was added to the SHC configuration.
     *
     * @return time of acceptance
     */
    public ZonedDateTime getTimeOfAcceptance() {
        if (timeOfAcceptance == null) {
            return null;
        }
        return Util.timeStringToDate(timeOfAcceptance);
    }

    /**
     * @param timeOfAcceptance the timeOfAcceptance to set
     */
    public void setTimeOfAcceptance(String timeOfAcceptance) {
        this.timeOfAcceptance = timeOfAcceptance;
    }

    /**
     * Returns the time, when the {@link DeviceDTO} was discovered by the SHC.
     *
     * @return time of discovery
     */
    public ZonedDateTime getTimeOfDiscovery() {
        if (timeOfDiscovery == null) {
            return null;
        }
        return Util.timeStringToDate(timeOfDiscovery);
    }

    /**
     * @param timeOfDiscovery the timeOfDiscovery to set
     */
    public void setTimeOfDiscovery(String timeOfDiscovery) {
        this.timeOfDiscovery = timeOfDiscovery;
    }

    /**
     * @return the hardwareVersion
     */
    public String getHardwareVersion() {
        return hardwareVersion;
    }

    /**
     * @param hardwareVersion the hardwareVersion to set
     */
    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    /**
     * @return the softwareVersion
     */
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * @param softwareVersion the softwareVersion to set
     */
    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    /**
     * @return the firmwareVersion
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * @param firmwareVersion the firmwareVersion to set
     */
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return the activityLogEnabled
     */
    public boolean isActivityLogEnabled() {
        return activityLogEnabled;
    }

    /**
     * @param activityLogEnabled the activityLogEnabled to set
     */
    public void setActivityLogEnabled(boolean activityLogEnabled) {
        this.activityLogEnabled = activityLogEnabled;
    }

    /**
     * @return the configurationState
     */
    public String getConfigurationState() {
        return configurationState;
    }

    /**
     * @param configurationState the configurationState to set
     */
    public void setConfigurationState(String configurationState) {
        this.configurationState = configurationState;
    }

    /**
     * @return the iPAddress
     */
    public String getIPAddress() {
        return ipAddress;
    }

    /**
     * @param ipAddress the ipAddress to set
     */
    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return the mACAddress
     */
    public String getMACAddress() {
        return macAddress;
    }

    /**
     * @param mACAddress the mACAddress to set
     */
    public void setMACAddress(String mACAddress) {
        this.macAddress = mACAddress;
    }

    /**
     * @return the registrationTime
     */
    public ZonedDateTime getRegistrationTime() {
        if (registrationTime == null) {
            return null;
        }
        return Util.timeStringToDate(registrationTime);
    }

    /**
     * @param registrationTime the registrationTime to set
     */
    public void setRegistrationTime(String registrationTime) {
        this.registrationTime = registrationTime;
    }

    /**
     * @return the timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the shcType
     */
    public String getShcType() {
        return shcType;
    }

    /**
     * @param shcType the shcType to set
     */
    public void setShcType(String shcType) {
        this.shcType = shcType;
    }

    /**
     * @return the geoLocation
     */
    public String getGeoLocation() {
        return geoLocation;
    }

    /**
     * @param geoLocation the geoLocation to set
     */
    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    /**
     * @return the currentUTCOffset
     */
    public Double getCurrentUTCOffset() {
        return currentUTCOffset;
    }

    /**
     * @param currentUTCOffset the currentUTCOffset to set
     */
    public void setCurrentUTCOffset(Double currentUTCOffset) {
        this.currentUTCOffset = currentUTCOffset;
    }

    /**
     * @return the backendConnectionMonitored
     */
    public Boolean getBackendConnectionMonitored() {
        return backendConnectionMonitored;
    }

    /**
     * @param backendConnectionMonitored the backendConnectionMonitored to set
     */
    public void setBackendConnectionMonitored(Boolean backendConnectionMonitored) {
        this.backendConnectionMonitored = backendConnectionMonitored;
    }

    /**
     * @return the rFCommFailureNotification
     */
    public Boolean getRFCommFailureNotification() {
        return rfCommFailureNotification;
    }

    /**
     * @param rFCommFailureNotification the rFCommFailureNotification to set
     */
    public void setRFCommFailureNotification(Boolean rFCommFailureNotification) {
        rfCommFailureNotification = rFCommFailureNotification;
    }

    /**
     * @return the displayCurrentTemperature
     */
    public String getDisplayCurrentTemperature() {
        return displayCurrentTemperature;
    }

    /**
     * @param displayCurrentTemperature the displayCurrentTemperature to set
     */
    public void setDisplayCurrentTemperature(String displayCurrentTemperature) {
        this.displayCurrentTemperature = displayCurrentTemperature;
    }

    /**
     * @return the underlyingDeviceIds
     */
    public String getUnderlyingDeviceIds() {
        return underlyingDeviceIds;
    }

    /**
     * @param underlyingDeviceIds the underlyingDeviceIds to set
     */
    public void setUnderlyingDeviceIds(String underlyingDeviceIds) {
        this.underlyingDeviceIds = underlyingDeviceIds;
    }

    /**
     * @return the meterId
     */
    public String getMeterId() {
        return meterId;
    }

    /**
     * @param meterId the meterId to set
     */
    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    /**
     * @return the meterFirmwareVersion
     */
    public String getMeterFirmwareVersion() {
        return meterFirmwareVersion;
    }

    /**
     * @param meterFirmwareVersion the meterFirmwareVersion to set
     */
    public void setMeterFirmwareVersion(String meterFirmwareVersion) {
        this.meterFirmwareVersion = meterFirmwareVersion;
    }
}
