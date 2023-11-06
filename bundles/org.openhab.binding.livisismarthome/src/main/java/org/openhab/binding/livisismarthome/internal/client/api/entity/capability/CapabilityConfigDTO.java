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
package org.openhab.binding.livisismarthome.internal.client.api.entity.capability;

/**
 * Holds the Capability configuration.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class CapabilityConfigDTO {

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
    public void setName(final String name) {
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
    public void setActivityLogActive(final Boolean activityLogActive) {
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
    public void setPushButtons(final Integer pushButtons) {
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
    public void setValveIndex(final Integer valveIndex) {
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
    public void setValveType(final String valveType) {
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
    public void setControlMode(final String controlMode) {
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
    public void setTechnicalMaxValue(final Integer technicalMaxValue) {
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
    public void setTechnicalMinValue(final Integer technicalMinValue) {
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
    public void setTimeFullUp(final Integer timeFullUp) {
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
    public void setTimeFullDown(final Integer timeFullDown) {
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
    public void setIsCalibrating(final Boolean isCalibrating) {
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
    public void setSensingBehavior(final String sensingBehavior) {
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
    public void setMaxTemperature(final Double maxTemperature) {
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
    public void setMinTemperature(final Double minTemperature) {
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
    public void setChildLock(final Boolean childLock) {
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
    public void setWindowOpenTemperature(final Double windowOpenTemperature) {
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
    public void setvRCCSetPoint(final String vRCCSetPoint) {
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
    public void setIsFreezeProtectionActivated(final Boolean isFreezeProtectionActivated) {
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
    public void setFreezeProtection(final Double freezeProtection) {
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
    public void setvRCCTemperature(final String vRCCTemperature) {
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
    public void setIsMoldProtectionActivated(final Boolean isMoldProtectionActivated) {
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
    public void setHumidityMoldProtection(final Double humidityMoldProtection) {
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
    public void setvRCCHumidity(final String vRCCHumidity) {
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
    public void setAlarmSoundId(final String alarmSoundId) {
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
    public void setNotificationSoundId(final String notificationSoundId) {
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
    public void setFeedbackSoundId(final String feedbackSoundId) {
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
    public void setUnderlyingCapabilityIds(final String underlyingCapabilityIds) {
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
    public void setEventFilterTime(final Integer eventFilterTime) {
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
    public void setWindowOpenThreshold(final Integer windowOpenThreshold) {
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
    public void setWindowOpenTimer(final Integer windowOpenTimer) {
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
    public void setSensitivityControl(final Integer sensitivityControl) {
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
    public void setShockDetectorThreshold(final Integer shockDetectorThreshold) {
        this.shockDetectorThreshold = shockDetectorThreshold;
    }
}
