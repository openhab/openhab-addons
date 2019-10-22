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
package org.openhab.binding.innogysmarthome.internal.client.entity.device;

import java.time.ZonedDateTime;

import org.openhab.binding.innogysmarthome.internal.client.Util;

import com.google.api.client.util.Key;

/**
 * Holds the configuration of the Device.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class DeviceConfig {
    @Key("name")
    private String name;

    @Key("protocolId")
    private String protocolId;

    @Key("timeOfAcceptance")
    private String timeOfAcceptance;

    @Key("timeOfDiscovery")
    private String timeOfDiscovery;

    @Key("hardwareVersion")
    private String hardwareVersion;

    @Key("softwareVersion")
    private String softwareVersion;

    @Key("firmwareVersion")
    private String firmwareVersion;

    @Key("hostName")
    private String hostName;

    @Key("activityLogEnabled")
    private boolean activityLogEnabled;

    @Key("configurationState")
    private String configurationState;

    @Key("IPAddress")
    private String IPAddress;

    @Key("MACAddress")
    private String MACAddress;

    @Key("registrationTime")
    private String registrationTime;

    @Key("timeZone")
    private String timeZone;

    @Key("shcType")
    private String shcType;

    @Key("geoLocation")
    private String geoLocation;

    @Key("currentUTCOffset")
    private Double currentUTCOffset;

    @Key("backendConnectionMonitored")
    private Boolean backendConnectionMonitored;

    @Key("RFCommFailureNotification")
    private Boolean RFCommFailureNotification;

    @Key("displayCurrentTemperature")
    private String displayCurrentTemperature;

    // TODO VRCC
    @Key("underlyingDeviceIds")
    private String underlyingDeviceIds;

    @Key("meterId")
    private String meterId;

    @Key("meterFirmwareVersion")
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
     * Returns the time, when the {@link Device} was added to the SHC configuration.
     *
     * @return
     */
    public ZonedDateTime getTimeOfAcceptance() {
        if (timeOfAcceptance == null) {
            return null;
        }
        return Util.convertZuluTimeStringToDate(timeOfAcceptance);
    }

    /**
     * @param timeOfAcceptance the timeOfAcceptance to set
     */
    public void setTimeOfAcceptance(String timeOfAcceptance) {
        this.timeOfAcceptance = timeOfAcceptance;
    }

    /**
     * Returns the time, when the {@link Device} was discovered by the SHC.
     *
     * @return
     */
    public ZonedDateTime getTimeOfDiscovery() {
        if (timeOfDiscovery == null) {
            return null;
        }
        return Util.convertZuluTimeStringToDate(timeOfDiscovery);
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
        return IPAddress;
    }

    /**
     * @param iPAddress the iPAddress to set
     */
    public void setIPAddress(String iPAddress) {
        IPAddress = iPAddress;
    }

    /**
     * @return the mACAddress
     */
    public String getMACAddress() {
        return MACAddress;
    }

    /**
     * @param mACAddress the mACAddress to set
     */
    public void setMACAddress(String mACAddress) {
        MACAddress = mACAddress;
    }

    /**
     * @return the registrationTime
     */
    public ZonedDateTime getRegistrationTime() {
        if (registrationTime == null) {
            return null;
        }
        return Util.convertZuluTimeStringToDate(registrationTime);
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
        return RFCommFailureNotification;
    }

    /**
     * @param rFCommFailureNotification the rFCommFailureNotification to set
     */
    public void setRFCommFailureNotification(Boolean rFCommFailureNotification) {
        RFCommFailureNotification = rFCommFailureNotification;
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
