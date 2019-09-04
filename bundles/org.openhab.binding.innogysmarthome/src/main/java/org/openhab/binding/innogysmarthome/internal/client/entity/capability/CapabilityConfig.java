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
package org.openhab.binding.innogysmarthome.internal.client.entity.capability;

/**
 * Holds the Capability configuration.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class CapabilityConfig {

    /**
     * Name of the capability
     */
    private String name;

    /**
     * Specifies if the activity logging is enabled
     */
    private Boolean activityLogActive;

    /**
     * Specifies if the activity logging is enabled
     */
    private Integer pushButtons;

    /**
     * The valve index
     */
    private Integer valveIndex;

    /**
     * The valve type
     */
    private String valveType;

    /**
     * The valve control mode: Heating or Cooling
     */
    private String controlMode;

    /**
     * Dimmer: Programmed on the device as maximum/minimum and not used for UI representation
     */
    private Integer technicalMaxValue;

    /**
     * Dimmer: Programmed on the device as maximum/minimum and not used for UI representation
     */
    private Integer technicalMinValue;

    /**
     * Rollershutter: How long it takes for the shutter to open completely when it's completely closed (in tenth of
     * second)
     */
    private Integer timeFullUp;

    /**
     * Rollershutter: How long it takes for the shutter to close completely when it's completely open (in tenth of
     * second)
     */
    private Integer timeFullDown;

    /**
     * Rollershutter: Flag indicating the ISR is in the calibration mode or not.
     */
    private Boolean isCalibrating;

    /**
     * Switchactuator:
     * "different types of current sensing behavior the ISS can have:
     * Enabled - Factory default value, current sensing is enabled; (default)
     * DisabledNormal - Current sensing disabled, uses output 1;
     * DisabledReversed - Current sensing disabled, uses output 2"
     */
    private String sensingBehavior;

    /**
     * Thermostatactuator: The max temperature
     */
    private Double maxTemperature;

    /**
     * Thermostatactuator: The min temperature
     */
    private Double minTemperature;

    /**
     * Thermostatactuator: Indicating whether the device is locked
     */
    private Boolean childLock;

    /**
     * Thermostatactuator: The window open temperature
     */
    private Double windowOpenTemperature;

    /**
     * Thermostatactuator: default PointTemperature
     */
    private String vRCCSetPoint;

    /**
     * Temperaturesensor: Indicating whether the device has freeze protection activated
     */
    private Boolean isFreezeProtectionActivated;

    /**
     * Temperaturesensor: The freeze protection temperature, default 6 °C
     */
    private Double freezeProtection;

    /**
     * Temperaturesensor: default Temperature
     */
    private String vRCCTemperature;

    /**
     * HumiditySensor: Indicating whether the device has mold protection activated
     */
    private Boolean isMoldProtectionActivated;

    /**
     * HumiditySensor: The humidity mold protection
     */
    private Double humidityMoldProtection;

    /**
     * HumiditySensor: default Humidity
     */
    private String vRCCHumidity;

    /**
     * SirenActuator: Alarm Sound Id
     */
    private String alarmSoundId;

    /**
     * SirenActuator: Notification Sound Id
     */
    private String notificationSoundId;

    /**
     * SirenActuator: Feedback Sound Id
     */
    private String feedbackSoundId;

    /**
     * RoomSetPoint/RoomTemperature/RoomHumidity: List of capability ids, which are linked to the VRCC
     */
    private String underlyingCapabilityIds;

    /**
     * WindowsDoorSensor: Time before the changed status is sent after the window/door is opened (in seconds)
     */
    private Integer eventFilterTime;

    /**
     * Medion ThermostatActuator: Specifies the temperature threshold that will denote a window open event. 0 = window
     * reduction disabled 1-12 = 1/12 °C, 2/12 °C,…, 12/12 °C
     */
    private Integer windowOpenThreshold;

    /**
     * Medion ThermostatActuator: Duration in minutes for how long after the threshold was overstepped the valve will be
     * closed (target temperature = OFF). After the set time, the temperature will jump back to the previous set target
     * temperature.
     */
    private Integer windowOpenTimer;

    /**
     * Medion MotionDetectionSensor sensitivityControl
     */
    private Integer sensitivityControl;

    /**
     * Medion WindowDoorShockSensor: shockDetectorThreshold
     */
    private Integer shockDetectorThreshold;

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
     * @return the activityLogActive
     */
    public Boolean getActivityLogActive() {
        return activityLogActive;
    }

    /**
     * @param activityLogActive the activityLogActive to set
     */
    public void setActivityLogActive(Boolean activityLogActive) {
        this.activityLogActive = activityLogActive;
    }

    /**
     * @return the pushButtons
     */
    public Integer getPushButtons() {
        return pushButtons;
    }

    /**
     * @param pushButtons the pushButtons to set
     */
    public void setPushButtons(Integer pushButtons) {
        this.pushButtons = pushButtons;
    }

    /**
     * @return the valveIndex
     */
    public Integer getValveIndex() {
        return valveIndex;
    }

    /**
     * @param valveIndex the valveIndex to set
     */
    public void setValveIndex(Integer valveIndex) {
        this.valveIndex = valveIndex;
    }

    /**
     * @return the valveType
     */
    public String getValveType() {
        return valveType;
    }

    /**
     * @param valveType the valveType to set
     */
    public void setValveType(String valveType) {
        this.valveType = valveType;
    }

    /**
     * @return the controlMode
     */
    public String getControlMode() {
        return controlMode;
    }

    /**
     * @param controlMode the controlMode to set
     */
    public void setControlMode(String controlMode) {
        this.controlMode = controlMode;
    }

    /**
     * @return the technicalMaxValue
     */
    public Integer getTechnicalMaxValue() {
        return technicalMaxValue;
    }

    /**
     * @param technicalMaxValue the technicalMaxValue to set
     */
    public void setTechnicalMaxValue(Integer technicalMaxValue) {
        this.technicalMaxValue = technicalMaxValue;
    }

    /**
     * @return the technicalMinValue
     */
    public Integer getTechnicalMinValue() {
        return technicalMinValue;
    }

    /**
     * @param technicalMinValue the technicalMinValue to set
     */
    public void setTechnicalMinValue(Integer technicalMinValue) {
        this.technicalMinValue = technicalMinValue;
    }

    /**
     * @return the timeFullUp
     */
    public Integer getTimeFullUp() {
        return timeFullUp;
    }

    /**
     * @param timeFullUp the timeFullUp to set
     */
    public void setTimeFullUp(Integer timeFullUp) {
        this.timeFullUp = timeFullUp;
    }

    /**
     * @return the timeFullDown
     */
    public Integer getTimeFullDown() {
        return timeFullDown;
    }

    /**
     * @param timeFullDown the timeFullDown to set
     */
    public void setTimeFullDown(Integer timeFullDown) {
        this.timeFullDown = timeFullDown;
    }

    /**
     * @return the isCalibrating
     */
    public Boolean getIsCalibrating() {
        return isCalibrating;
    }

    /**
     * @param isCalibrating the isCalibrating to set
     */
    public void setIsCalibrating(Boolean isCalibrating) {
        this.isCalibrating = isCalibrating;
    }

    /**
     * @return the sensingBehavior
     */
    public String getSensingBehavior() {
        return sensingBehavior;
    }

    /**
     * @param sensingBehavior the sensingBehavior to set
     */
    public void setSensingBehavior(String sensingBehavior) {
        this.sensingBehavior = sensingBehavior;
    }

    /**
     * @return the maxTemperature
     */
    public Double getMaxTemperature() {
        return maxTemperature;
    }

    /**
     * @param maxTemperature the maxTemperature to set
     */
    public void setMaxTemperature(Double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    /**
     * @return the minTemperature
     */
    public Double getMinTemperature() {
        return minTemperature;
    }

    /**
     * @param minTemperature the minTemperature to set
     */
    public void setMinTemperature(Double minTemperature) {
        this.minTemperature = minTemperature;
    }

    /**
     * @return the childLock
     */
    public Boolean getChildLock() {
        return childLock;
    }

    /**
     * @param childLock the childLock to set
     */
    public void setChildLock(Boolean childLock) {
        this.childLock = childLock;
    }

    /**
     * @return the windowOpenTemperature
     */
    public Double getWindowOpenTemperature() {
        return windowOpenTemperature;
    }

    /**
     * @param windowOpenTemperature the windowOpenTemperature to set
     */
    public void setWindowOpenTemperature(Double windowOpenTemperature) {
        this.windowOpenTemperature = windowOpenTemperature;
    }

    /**
     * @return the vRCCSetPoint
     */
    public String getvRCCSetPoint() {
        return vRCCSetPoint;
    }

    /**
     * @param vRCCSetPoint the vRCCSetPoint to set
     */
    public void setvRCCSetPoint(String vRCCSetPoint) {
        this.vRCCSetPoint = vRCCSetPoint;
    }

    /**
     * @return the isFreezeProtectionActivated
     */
    public Boolean getIsFreezeProtectionActivated() {
        return isFreezeProtectionActivated;
    }

    /**
     * @param isFreezeProtectionActivated the isFreezeProtectionActivated to set
     */
    public void setIsFreezeProtectionActivated(Boolean isFreezeProtectionActivated) {
        this.isFreezeProtectionActivated = isFreezeProtectionActivated;
    }

    /**
     * @return the freezeProtection
     */
    public Double getFreezeProtection() {
        return freezeProtection;
    }

    /**
     * @param freezeProtection the freezeProtection to set
     */
    public void setFreezeProtection(Double freezeProtection) {
        this.freezeProtection = freezeProtection;
    }

    /**
     * @return the vRCCTemperature
     */
    public String getvRCCTemperature() {
        return vRCCTemperature;
    }

    /**
     * @param vRCCTemperature the vRCCTemperature to set
     */
    public void setvRCCTemperature(String vRCCTemperature) {
        this.vRCCTemperature = vRCCTemperature;
    }

    /**
     * @return the isMoldProtectionActivated
     */
    public Boolean getIsMoldProtectionActivated() {
        return isMoldProtectionActivated;
    }

    /**
     * @param isMoldProtectionActivated the isMoldProtectionActivated to set
     */
    public void setIsMoldProtectionActivated(Boolean isMoldProtectionActivated) {
        this.isMoldProtectionActivated = isMoldProtectionActivated;
    }

    /**
     * @return the humidityMoldProtection
     */
    public Double getHumidityMoldProtection() {
        return humidityMoldProtection;
    }

    /**
     * @param humidityMoldProtection the humidityMoldProtection to set
     */
    public void setHumidityMoldProtection(Double humidityMoldProtection) {
        this.humidityMoldProtection = humidityMoldProtection;
    }

    /**
     * @return the vRCCHumidity
     */
    public String getvRCCHumidity() {
        return vRCCHumidity;
    }

    /**
     * @param vRCCHumidity the vRCCHumidity to set
     */
    public void setvRCCHumidity(String vRCCHumidity) {
        this.vRCCHumidity = vRCCHumidity;
    }

    /**
     * @return the alarmSoundId
     */
    public String getAlarmSoundId() {
        return alarmSoundId;
    }

    /**
     * @param alarmSoundId the alarmSoundId to set
     */
    public void setAlarmSoundId(String alarmSoundId) {
        this.alarmSoundId = alarmSoundId;
    }

    /**
     * @return the notificationSoundId
     */
    public String getNotificationSoundId() {
        return notificationSoundId;
    }

    /**
     * @param notificationSoundId the notificationSoundId to set
     */
    public void setNotificationSoundId(String notificationSoundId) {
        this.notificationSoundId = notificationSoundId;
    }

    /**
     * @return the feedbackSoundId
     */
    public String getFeedbackSoundId() {
        return feedbackSoundId;
    }

    /**
     * @param feedbackSoundId the feedbackSoundId to set
     */
    public void setFeedbackSoundId(String feedbackSoundId) {
        this.feedbackSoundId = feedbackSoundId;
    }

    /**
     * @return the underlyingCapabilityIds
     */
    public String getUnderlyingCapabilityIds() {
        return underlyingCapabilityIds;
    }

    /**
     * @param underlyingCapabilityIds the underlyingCapabilityIds to set
     */
    public void setUnderlyingCapabilityIds(String underlyingCapabilityIds) {
        this.underlyingCapabilityIds = underlyingCapabilityIds;
    }

    /**
     * @return the eventFilterTime
     */
    public Integer getEventFilterTime() {
        return eventFilterTime;
    }

    /**
     * @param eventFilterTime the eventFilterTime to set
     */
    public void setEventFilterTime(Integer eventFilterTime) {
        this.eventFilterTime = eventFilterTime;
    }

    /**
     * @return the windowOpenThreshold
     */
    public Integer getWindowOpenThreshold() {
        return windowOpenThreshold;
    }

    /**
     * @param windowOpenThreshold the windowOpenThreshold to set
     */
    public void setWindowOpenThreshold(Integer windowOpenThreshold) {
        this.windowOpenThreshold = windowOpenThreshold;
    }

    /**
     * @return the windowOpenTimer
     */
    public Integer getWindowOpenTimer() {
        return windowOpenTimer;
    }

    /**
     * @param windowOpenTimer the windowOpenTimer to set
     */
    public void setWindowOpenTimer(Integer windowOpenTimer) {
        this.windowOpenTimer = windowOpenTimer;
    }

    /**
     * @return the sensitivityControl
     */
    public Integer getSensitivityControl() {
        return sensitivityControl;
    }

    /**
     * @param sensitivityControl the sensitivityControl to set
     */
    public void setSensitivityControl(Integer sensitivityControl) {
        this.sensitivityControl = sensitivityControl;
    }

    /**
     * @return the shockDetectorThreshold
     */
    public Integer getShockDetectorThreshold() {
        return shockDetectorThreshold;
    }

    /**
     * @param shockDetectorThreshold the shockDetectorThreshold to set
     */
    public void setShockDetectorThreshold(Integer shockDetectorThreshold) {
        this.shockDetectorThreshold = shockDetectorThreshold;
    }

    // /**
    // * @return the protocolId
    // */
    // public String getProtocolId() {
    // return protocolId;
    // }
    //
    // /**
    // * @param protocolId the protocolId to set
    // */
    // public void setProtocolId(String protocolId) {
    // this.protocolId = protocolId;
    // }
    //
    //
    // /**
    // * @return the timeOfAcceptance
    // */
    // public String getTimeOfAcceptance() {
    // return timeOfAcceptance;
    // }
    //
    // /**
    // * @param timeOfAcceptance the timeOfAcceptance to set
    // */
    // public void setTimeOfAcceptance(String timeOfAcceptance) {
    // this.timeOfAcceptance = timeOfAcceptance;
    // }
    //
    // /**
    // * @return the timeOfDiscovery
    // */
    // public String getTimeOfDiscovery() {
    // return timeOfDiscovery;
    // }
    //
    // /**
    // * @param timeOfDiscovery the timeOfDiscovery to set
    // */
    // public void setTimeOfDiscovery(String timeOfDiscovery) {
    // this.timeOfDiscovery = timeOfDiscovery;
    // }
    //
    // /**
    // * @return the hardwareVersion
    // */
    // public String getHardwareVersion() {
    // return hardwareVersion;
    // }
    //
    // /**
    // * @param hardwareVersion the hardwareVersion to set
    // */
    // public void setHardwareVersion(String hardwareVersion) {
    // this.hardwareVersion = hardwareVersion;
    // }
    //
    // /**
    // * @return the softwareVersion
    // */
    // public String getSoftwareVersion() {
    // return softwareVersion;
    // }
    //
    // /**
    // * @param softwareVersion the softwareVersion to set
    // */
    // public void setSoftwareVersion(String softwareVersion) {
    // this.softwareVersion = softwareVersion;
    // }
    //
    // /**
    // * @return the firmwareVersion
    // */
    // public String getFirmwareVersion() {
    // return firmwareVersion;
    // }
    //
    // /**
    // * @param firmwareVersion the firmwareVersion to set
    // */
    // public void setFirmwareVersion(String firmwareVersion) {
    // this.firmwareVersion = firmwareVersion;
    // }
    //
    // /**
    // * @return the hostName
    // */
    // public String getHostName() {
    // return hostName;
    // }
    //
    // /**
    // * @param hostName the hostName to set
    // */
    // public void setHostName(String hostName) {
    // this.hostName = hostName;
    // }
    //
    // /**
    // * @return the activityLogEnabled
    // */
    // public Boolean getActivityLogEnabled() {
    // return activityLogEnabled;
    // }
    //
    // /**
    // * @param activityLogEnabled the activityLogEnabled to set
    // */
    // public void setActivityLogEnabled(Boolean activityLogEnabled) {
    // this.activityLogEnabled = activityLogEnabled;
    // }
    //
    // /**
    // * @return the configurationState
    // */
    // public String getConfigurationState() {
    // return configurationState;
    // }
    //
    // /**
    // * @param configurationState the configurationState to set
    // */
    // public void setConfigurationState(String configurationState) {
    // this.configurationState = configurationState;
    // }
    //
    // /**
    // * @return the geoLocation
    // */
    // public String getGeoLocation() {
    // return geoLocation;
    // }
    //
    // /**
    // * @param geoLocation the geoLocation to set
    // */
    // public void setGeoLocation(String geoLocation) {
    // this.geoLocation = geoLocation;
    // }
    //
    // /**
    // * @return the timeZone
    // */
    // public String getTimeZone() {
    // return timeZone;
    // }
    //
    // /**
    // * @param timeZone the timeZone to set
    // */
    // public void setTimeZone(String timeZone) {
    // this.timeZone = timeZone;
    // }
    //
    // /**
    // * @return the currentUTCOffset
    // */
    // public Integer getCurrentUTCOffset() {
    // return currentUTCOffset;
    // }
    //
    // /**
    // * @param currentUTCOffset the currentUTCOffset to set
    // */
    // public void setCurrentUTCOffset(Integer currentUTCOffset) {
    // this.currentUTCOffset = currentUTCOffset;
    // }
    //
    // /**
    // * @return the iPAddress
    // */
    // public String getIPAddress() {
    // return IPAddress;
    // }
    //
    // /**
    // * @param iPAddress the iPAddress to set
    // */
    // public void setIPAddress(String iPAddress) {
    // IPAddress = iPAddress;
    // }
    //
    // /**
    // * @return the mACAddress
    // */
    // public String getMACAddress() {
    // return MACAddress;
    // }
    //
    // /**
    // * @param mACAddress the mACAddress to set
    // */
    // public void setMACAddress(String mACAddress) {
    // MACAddress = mACAddress;
    // }
    //
    // /**
    // * @return the shcType
    // */
    // public String getShcType() {
    // return shcType;
    // }
    //
    // /**
    // * @param shcType the shcType to set
    // */
    // public void setShcType(String shcType) {
    // this.shcType = shcType;
    // }
    //
    // /**
    // * @return the backendConnectionMonitored
    // */
    // public Boolean getBackendConnectionMonitored() {
    // return backendConnectionMonitored;
    // }
    //
    // /**
    // * @param backendConnectionMonitored the backendConnectionMonitored to set
    // */
    // public void setBackendConnectionMonitored(Boolean backendConnectionMonitored) {
    // this.backendConnectionMonitored = backendConnectionMonitored;
    // }
    //
    // /**
    // * @return the rFCommFailureNotification
    // */
    // public Boolean getRFCommFailureNotification() {
    // return RFCommFailureNotification;
    // }
    //
    // /**
    // * @param rFCommFailureNotification the rFCommFailureNotification to set
    // */
    // public void setRFCommFailureNotification(Boolean rFCommFailureNotification) {
    // RFCommFailureNotification = rFCommFailureNotification;
    // }
    //
    // /**
    // * @return the postCode
    // */
    // public String getPostCode() {
    // return postCode;
    // }
    //
    // /**
    // * @param postCode the postCode to set
    // */
    // public void setPostCode(String postCode) {
    // this.postCode = postCode;
    // }
    //
    // /**
    // * @return the city
    // */
    // public String getCity() {
    // return city;
    // }
    //
    // /**
    // * @param city the city to set
    // */
    // public void setCity(String city) {
    // this.city = city;
    // }
    //
    // /**
    // * @return the street
    // */
    // public String getStreet() {
    // return street;
    // }
    //
    // /**
    // * @param street the street to set
    // */
    // public void setStreet(String street) {
    // this.street = street;
    // }
    //
    // /**
    // * @return the houseNumber
    // */
    // public String getHouseNumber() {
    // return houseNumber;
    // }
    //
    // /**
    // * @param houseNumber the houseNumber to set
    // */
    // public void setHouseNumber(String houseNumber) {
    // this.houseNumber = houseNumber;
    // }
    //
    // /**
    // * @return the country
    // */
    // public String getCountry() {
    // return country;
    // }
    //
    // /**
    // * @param country the country to set
    // */
    // public void setCountry(String country) {
    // this.country = country;
    // }
    //
    // /**
    // * @return the householdType
    // */
    // public String getHouseholdType() {
    // return householdType;
    // }
    //
    // /**
    // * @param householdType the householdType to set
    // */
    // public void setHouseholdType(String householdType) {
    // this.householdType = householdType;
    // }
    //
    // /**
    // * @return the numberOfPersons
    // */
    // public Integer getNumberOfPersons() {
    // return numberOfPersons;
    // }
    //
    // /**
    // * @param numberOfPersons the numberOfPersons to set
    // */
    // public void setNumberOfPersons(Integer numberOfPersons) {
    // this.numberOfPersons = numberOfPersons;
    // }
    //
    // /**
    // * @return the numberOfFloors
    // */
    // public Integer getNumberOfFloors() {
    // return numberOfFloors;
    // }
    //
    // /**
    // * @param numberOfFloors the numberOfFloors to set
    // */
    // public void setNumberOfFloors(Integer numberOfFloors) {
    // this.numberOfFloors = numberOfFloors;
    // }
    //
    // /**
    // * @return the livingArea
    // */
    // public Integer getLivingArea() {
    // return livingArea;
    // }
    //
    // /**
    // * @param livingArea the livingArea to set
    // */
    // public void setLivingArea(Integer livingArea) {
    // this.livingArea = livingArea;
    // }
    //
    // /**
    // * @return the registrationTime
    // */
    // public String getRegistrationTime() {
    // return registrationTime;
    // }
    //
    // /**
    // * @param registrationTime the registrationTime to set
    // */
    // public void setRegistrationTime(String registrationTime) {
    // this.registrationTime = registrationTime;
    // }
    //
    // /**
    // * @return the displayCurrentTemperature
    // */
    // public String getDisplayCurrentTemperature() {
    // return displayCurrentTemperature;
    // }
    //
    // /**
    // * @param displayCurrentTemperature the displayCurrentTemperature to set
    // */
    // public void setDisplayCurrentTemperature(String displayCurrentTemperature) {
    // this.displayCurrentTemperature = displayCurrentTemperature;
    // }
    //
    // /**
    // * @return the underlyingDeviceIds
    // */
    // public String getUnderlyingDeviceIds() {
    // return underlyingDeviceIds;
    // }
    //
    // /**
    // * @param underlyingDeviceIds the underlyingDeviceIds to set
    // */
    // public void setUnderlyingDeviceIds(String underlyingDeviceIds) {
    // this.underlyingDeviceIds = underlyingDeviceIds;
    // }
    //
    // /**
    // * @return the meterId
    // */
    // public String getMeterId() {
    // return meterId;
    // }
    //
    // /**
    // * @param meterId the meterId to set
    // */
    // public void setMeterId(String meterId) {
    // this.meterId = meterId;
    // }
    //
    // /**
    // * @return the meterFirmwareVersion
    // */
    // public String getMeterFirmwareVersion() {
    // return meterFirmwareVersion;
    // }
    //
    // /**
    // * @param meterFirmwareVersion the meterFirmwareVersion to set
    // */
    // public void setMeterFirmwareVersion(String meterFirmwareVersion) {
    // this.meterFirmwareVersion = meterFirmwareVersion;
    // }
    //
    // /**
    // * @return the deviceType
    // */
    // public String getDeviceType() {
    // return deviceType;
    // }
    //
    // /**
    // * @param deviceType the deviceType to set
    // */
    // public void setDeviceType(String deviceType) {
    // this.deviceType = deviceType;
    // }
}
