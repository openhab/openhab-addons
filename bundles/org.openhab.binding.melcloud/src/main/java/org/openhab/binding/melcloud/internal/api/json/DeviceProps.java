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
package org.openhab.binding.melcloud.internal.api.json;

import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * The {@link DeviceProps} is responsible of JSON data For MELCloud API
 * Device Properties.
 * Generated with jsonschema2pojo
 *
 * @author Luca Calcaterra - Initial contribution
 */
public class DeviceProps {

    @Expose
    private List<Object> listHistory24Formatters = null;

    @Expose
    private Integer deviceType;

    @Expose
    private Boolean canCool;

    @Expose
    private Boolean canHeat;

    @Expose
    private Boolean canDry;

    @Expose
    private Boolean hasAutomaticFanSpeed;

    @Expose
    private Boolean airDirectionFunction;

    @Expose
    private Boolean swingFunction;

    @Expose
    private Integer numberOfFanSpeeds;

    @Expose
    private Boolean useTemperatureA;

    @Expose
    private Integer temperatureIncrementOverride;

    @Expose
    private Double temperatureIncrement;

    @Expose
    private Double minTempCoolDry;

    @Expose
    private Double maxTempCoolDry;

    @Expose
    private Double minTempHeat;

    @Expose
    private Double maxTempHeat;

    @Expose
    private Double minTempAutomatic;

    @Expose
    private Double maxTempAutomatic;

    @Expose
    private Boolean legacyDevice;

    @Expose
    private Boolean unitSupportsStandbyMode;

    @Expose
    private Boolean modelIsAirCurtain;

    @Expose
    private Boolean modelSupportsFanSpeed;

    @Expose
    private Boolean modelSupportsAuto;

    @Expose
    private Boolean modelSupportsHeat;

    @Expose
    private Boolean modelSupportsDry;

    @Expose
    private Boolean modelSupportsVaneVertical;

    @Expose
    private Boolean modelSupportsVaneHorizontal;

    @Expose
    private Boolean modelSupportsStandbyMode;

    @Expose
    private Boolean modelSupportsEnergyReporting;

    @Expose
    private Boolean power;

    @Expose
    private Double roomTemperature;

    @Expose
    private Double setTemperature;

    @Expose
    private Integer actualFanSpeed;

    @Expose
    private Integer fanSpeed;

    @Expose
    private Boolean automaticFanSpeed;

    @Expose
    private Integer vaneVerticalDirection;

    @Expose
    private Boolean vaneVerticalSwing;

    @Expose
    private Integer vaneHorizontalDirection;

    @Expose
    private Boolean vaneHorizontalSwing;

    @Expose
    private Integer operationMode;

    @Expose
    private Integer effectiveFlags;

    @Expose
    private Integer lastEffectiveFlags;

    @Expose
    private Boolean inStandbyMode;

    @Expose
    private Double defaultCoolingSetTemperature;

    @Expose
    private Double defaultHeatingSetTemperature;

    @Expose
    private Integer roomTemperatureLabel;

    @Expose
    private Boolean hasEnergyConsumedMeter;

    @Expose
    private Integer currentEnergyConsumed;

    @Expose
    private Integer currentEnergyMode;

    @Expose
    private Boolean coolingDisabled;

    @Expose
    private Integer minPcycle;

    @Expose
    private Integer maxPcycle;

    @Expose
    private Integer effectivePCycle;

    @Expose
    private Integer maxOutdoorUnits;

    @Expose
    private Integer maxIndoorUnits;

    @Expose
    private Integer maxTemperatureControlUnits;

    @Expose
    private Integer deviceID;

    @Expose
    private String macAddress;

    @Expose
    private String serialNumber;

    @Expose
    private Integer timeZoneID;

    @Expose
    private Integer diagnosticMode;

    @Expose
    private Object diagnosticEndDate;

    @Expose
    private Integer expectedCommand;

    @Expose
    private Object owner;

    @Expose
    private Object detectedCountry;

    @Expose
    private Integer adaptorType;

    @Expose
    private Object firmwareDeployment;

    @Expose
    private Boolean firmwareUpdateAborted;

    @Expose
    private Integer wifiSignalStrength;

    @Expose
    private String wifiAdapterStatus;

    @Expose
    private String position;

    @Expose
    private Integer pCycle;

    @Expose
    private Integer recordNumMax;

    @Expose
    private String lastTimeStamp;

    @Expose
    private Integer errorCode;

    @Expose
    private Boolean hasError;

    @Expose
    private String lastReset;

    @Expose
    private Integer flashWrites;

    @Expose
    private Object scene;

    @Expose
    private Object sSLExpirationDate;

    @Expose
    private Object sPTimeout;

    @Expose
    private Object passcode;

    @Expose
    private Boolean serverCommunicationDisabled;

    @Expose
    private Integer consecutiveUploadErrors;

    @Expose
    private Object doNotRespondAfter;

    @Expose
    private Integer ownerRoleAccessLevel;

    @Expose
    private Integer ownerCountry;

    @Expose
    private Object rate1StartTime;

    @Expose
    private Object rate2StartTime;

    @Expose
    private Integer protocolVersion;

    @Expose
    private Integer unitVersion;

    @Expose
    private Integer firmwareAppVersion;

    @Expose
    private Integer firmwareWebVersion;

    @Expose
    private Integer firmwareWlanVersion;

    @Expose
    private Boolean hasErrorMessages;

    @Expose
    private Boolean hasZone2;

    @Expose
    private Boolean offline;

    @Expose
    private List<Object> units = null;

    public List<Object> getListHistory24Formatters() {
        return listHistory24Formatters;
    }

    public void setListHistory24Formatters(List<Object> listHistory24Formatters) {
        this.listHistory24Formatters = listHistory24Formatters;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Boolean getCanCool() {
        return canCool;
    }

    public void setCanCool(Boolean canCool) {
        this.canCool = canCool;
    }

    public Boolean getCanHeat() {
        return canHeat;
    }

    public void setCanHeat(Boolean canHeat) {
        this.canHeat = canHeat;
    }

    public Boolean getCanDry() {
        return canDry;
    }

    public void setCanDry(Boolean canDry) {
        this.canDry = canDry;
    }

    public Boolean getHasAutomaticFanSpeed() {
        return hasAutomaticFanSpeed;
    }

    public void setHasAutomaticFanSpeed(Boolean hasAutomaticFanSpeed) {
        this.hasAutomaticFanSpeed = hasAutomaticFanSpeed;
    }

    public Boolean getAirDirectionFunction() {
        return airDirectionFunction;
    }

    public void setAirDirectionFunction(Boolean airDirectionFunction) {
        this.airDirectionFunction = airDirectionFunction;
    }

    public Boolean getSwingFunction() {
        return swingFunction;
    }

    public void setSwingFunction(Boolean swingFunction) {
        this.swingFunction = swingFunction;
    }

    public Integer getNumberOfFanSpeeds() {
        return numberOfFanSpeeds;
    }

    public void setNumberOfFanSpeeds(Integer numberOfFanSpeeds) {
        this.numberOfFanSpeeds = numberOfFanSpeeds;
    }

    public Boolean getUseTemperatureA() {
        return useTemperatureA;
    }

    public void setUseTemperatureA(Boolean useTemperatureA) {
        this.useTemperatureA = useTemperatureA;
    }

    public Integer getTemperatureIncrementOverride() {
        return temperatureIncrementOverride;
    }

    public void setTemperatureIncrementOverride(Integer temperatureIncrementOverride) {
        this.temperatureIncrementOverride = temperatureIncrementOverride;
    }

    public Double getTemperatureIncrement() {
        return temperatureIncrement;
    }

    public void setTemperatureIncrement(Double temperatureIncrement) {
        this.temperatureIncrement = temperatureIncrement;
    }

    public Double getMinTempCoolDry() {
        return minTempCoolDry;
    }

    public void setMinTempCoolDry(Double minTempCoolDry) {
        this.minTempCoolDry = minTempCoolDry;
    }

    public Double getMaxTempCoolDry() {
        return maxTempCoolDry;
    }

    public void setMaxTempCoolDry(Double maxTempCoolDry) {
        this.maxTempCoolDry = maxTempCoolDry;
    }

    public Double getMinTempHeat() {
        return minTempHeat;
    }

    public void setMinTempHeat(Double minTempHeat) {
        this.minTempHeat = minTempHeat;
    }

    public Double getMaxTempHeat() {
        return maxTempHeat;
    }

    public void setMaxTempHeat(Double maxTempHeat) {
        this.maxTempHeat = maxTempHeat;
    }

    public Double getMinTempAutomatic() {
        return minTempAutomatic;
    }

    public void setMinTempAutomatic(Double minTempAutomatic) {
        this.minTempAutomatic = minTempAutomatic;
    }

    public Double getMaxTempAutomatic() {
        return maxTempAutomatic;
    }

    public void setMaxTempAutomatic(Double maxTempAutomatic) {
        this.maxTempAutomatic = maxTempAutomatic;
    }

    public Boolean getLegacyDevice() {
        return legacyDevice;
    }

    public void setLegacyDevice(Boolean legacyDevice) {
        this.legacyDevice = legacyDevice;
    }

    public Boolean getUnitSupportsStandbyMode() {
        return unitSupportsStandbyMode;
    }

    public void setUnitSupportsStandbyMode(Boolean unitSupportsStandbyMode) {
        this.unitSupportsStandbyMode = unitSupportsStandbyMode;
    }

    public Boolean getModelIsAirCurtain() {
        return modelIsAirCurtain;
    }

    public void setModelIsAirCurtain(Boolean modelIsAirCurtain) {
        this.modelIsAirCurtain = modelIsAirCurtain;
    }

    public Boolean getModelSupportsFanSpeed() {
        return modelSupportsFanSpeed;
    }

    public void setModelSupportsFanSpeed(Boolean modelSupportsFanSpeed) {
        this.modelSupportsFanSpeed = modelSupportsFanSpeed;
    }

    public Boolean getModelSupportsAuto() {
        return modelSupportsAuto;
    }

    public void setModelSupportsAuto(Boolean modelSupportsAuto) {
        this.modelSupportsAuto = modelSupportsAuto;
    }

    public Boolean getModelSupportsHeat() {
        return modelSupportsHeat;
    }

    public void setModelSupportsHeat(Boolean modelSupportsHeat) {
        this.modelSupportsHeat = modelSupportsHeat;
    }

    public Boolean getModelSupportsDry() {
        return modelSupportsDry;
    }

    public void setModelSupportsDry(Boolean modelSupportsDry) {
        this.modelSupportsDry = modelSupportsDry;
    }

    public Boolean getModelSupportsVaneVertical() {
        return modelSupportsVaneVertical;
    }

    public void setModelSupportsVaneVertical(Boolean modelSupportsVaneVertical) {
        this.modelSupportsVaneVertical = modelSupportsVaneVertical;
    }

    public Boolean getModelSupportsVaneHorizontal() {
        return modelSupportsVaneHorizontal;
    }

    public void setModelSupportsVaneHorizontal(Boolean modelSupportsVaneHorizontal) {
        this.modelSupportsVaneHorizontal = modelSupportsVaneHorizontal;
    }

    public Boolean getModelSupportsStandbyMode() {
        return modelSupportsStandbyMode;
    }

    public void setModelSupportsStandbyMode(Boolean modelSupportsStandbyMode) {
        this.modelSupportsStandbyMode = modelSupportsStandbyMode;
    }

    public Boolean getModelSupportsEnergyReporting() {
        return modelSupportsEnergyReporting;
    }

    public void setModelSupportsEnergyReporting(Boolean modelSupportsEnergyReporting) {
        this.modelSupportsEnergyReporting = modelSupportsEnergyReporting;
    }

    public Boolean getPower() {
        return power;
    }

    public void setPower(Boolean power) {
        this.power = power;
    }

    public Double getRoomTemperature() {
        return roomTemperature;
    }

    public void setRoomTemperature(Double roomTemperature) {
        this.roomTemperature = roomTemperature;
    }

    public Double getSetTemperature() {
        return setTemperature;
    }

    public void setSetTemperature(Double setTemperature) {
        this.setTemperature = setTemperature;
    }

    public Integer getActualFanSpeed() {
        return actualFanSpeed;
    }

    public void setActualFanSpeed(Integer actualFanSpeed) {
        this.actualFanSpeed = actualFanSpeed;
    }

    public Integer getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(Integer fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public Boolean getAutomaticFanSpeed() {
        return automaticFanSpeed;
    }

    public void setAutomaticFanSpeed(Boolean automaticFanSpeed) {
        this.automaticFanSpeed = automaticFanSpeed;
    }

    public Integer getVaneVerticalDirection() {
        return vaneVerticalDirection;
    }

    public void setVaneVerticalDirection(Integer vaneVerticalDirection) {
        this.vaneVerticalDirection = vaneVerticalDirection;
    }

    public Boolean getVaneVerticalSwing() {
        return vaneVerticalSwing;
    }

    public void setVaneVerticalSwing(Boolean vaneVerticalSwing) {
        this.vaneVerticalSwing = vaneVerticalSwing;
    }

    public Integer getVaneHorizontalDirection() {
        return vaneHorizontalDirection;
    }

    public void setVaneHorizontalDirection(Integer vaneHorizontalDirection) {
        this.vaneHorizontalDirection = vaneHorizontalDirection;
    }

    public Boolean getVaneHorizontalSwing() {
        return vaneHorizontalSwing;
    }

    public void setVaneHorizontalSwing(Boolean vaneHorizontalSwing) {
        this.vaneHorizontalSwing = vaneHorizontalSwing;
    }

    public Integer getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(Integer operationMode) {
        this.operationMode = operationMode;
    }

    public Integer getEffectiveFlags() {
        return effectiveFlags;
    }

    public void setEffectiveFlags(Integer effectiveFlags) {
        this.effectiveFlags = effectiveFlags;
    }

    public Integer getLastEffectiveFlags() {
        return lastEffectiveFlags;
    }

    public void setLastEffectiveFlags(Integer lastEffectiveFlags) {
        this.lastEffectiveFlags = lastEffectiveFlags;
    }

    public Boolean getInStandbyMode() {
        return inStandbyMode;
    }

    public void setInStandbyMode(Boolean inStandbyMode) {
        this.inStandbyMode = inStandbyMode;
    }

    public Double getDefaultCoolingSetTemperature() {
        return defaultCoolingSetTemperature;
    }

    public void setDefaultCoolingSetTemperature(Double defaultCoolingSetTemperature) {
        this.defaultCoolingSetTemperature = defaultCoolingSetTemperature;
    }

    public Double getDefaultHeatingSetTemperature() {
        return defaultHeatingSetTemperature;
    }

    public void setDefaultHeatingSetTemperature(Double defaultHeatingSetTemperature) {
        this.defaultHeatingSetTemperature = defaultHeatingSetTemperature;
    }

    public Integer getRoomTemperatureLabel() {
        return roomTemperatureLabel;
    }

    public void setRoomTemperatureLabel(Integer roomTemperatureLabel) {
        this.roomTemperatureLabel = roomTemperatureLabel;
    }

    public Boolean getHasEnergyConsumedMeter() {
        return hasEnergyConsumedMeter;
    }

    public void setHasEnergyConsumedMeter(Boolean hasEnergyConsumedMeter) {
        this.hasEnergyConsumedMeter = hasEnergyConsumedMeter;
    }

    public Integer getCurrentEnergyConsumed() {
        return currentEnergyConsumed;
    }

    public void setCurrentEnergyConsumed(Integer currentEnergyConsumed) {
        this.currentEnergyConsumed = currentEnergyConsumed;
    }

    public Integer getCurrentEnergyMode() {
        return currentEnergyMode;
    }

    public void setCurrentEnergyMode(Integer currentEnergyMode) {
        this.currentEnergyMode = currentEnergyMode;
    }

    public Boolean getCoolingDisabled() {
        return coolingDisabled;
    }

    public void setCoolingDisabled(Boolean coolingDisabled) {
        this.coolingDisabled = coolingDisabled;
    }

    public Integer getMinPcycle() {
        return minPcycle;
    }

    public void setMinPcycle(Integer minPcycle) {
        this.minPcycle = minPcycle;
    }

    public Integer getMaxPcycle() {
        return maxPcycle;
    }

    public void setMaxPcycle(Integer maxPcycle) {
        this.maxPcycle = maxPcycle;
    }

    public Integer getEffectivePCycle() {
        return effectivePCycle;
    }

    public void setEffectivePCycle(Integer effectivePCycle) {
        this.effectivePCycle = effectivePCycle;
    }

    public Integer getMaxOutdoorUnits() {
        return maxOutdoorUnits;
    }

    public void setMaxOutdoorUnits(Integer maxOutdoorUnits) {
        this.maxOutdoorUnits = maxOutdoorUnits;
    }

    public Integer getMaxIndoorUnits() {
        return maxIndoorUnits;
    }

    public void setMaxIndoorUnits(Integer maxIndoorUnits) {
        this.maxIndoorUnits = maxIndoorUnits;
    }

    public Integer getMaxTemperatureControlUnits() {
        return maxTemperatureControlUnits;
    }

    public void setMaxTemperatureControlUnits(Integer maxTemperatureControlUnits) {
        this.maxTemperatureControlUnits = maxTemperatureControlUnits;
    }

    public Integer getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(Integer deviceID) {
        this.deviceID = deviceID;
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

    public Integer getTimeZoneID() {
        return timeZoneID;
    }

    public void setTimeZoneID(Integer timeZoneID) {
        this.timeZoneID = timeZoneID;
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

    public Integer getExpectedCommand() {
        return expectedCommand;
    }

    public void setExpectedCommand(Integer expectedCommand) {
        this.expectedCommand = expectedCommand;
    }

    public Object getOwner() {
        return owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }

    public Object getDetectedCountry() {
        return detectedCountry;
    }

    public void setDetectedCountry(Object detectedCountry) {
        this.detectedCountry = detectedCountry;
    }

    public Integer getAdaptorType() {
        return adaptorType;
    }

    public void setAdaptorType(Integer adaptorType) {
        this.adaptorType = adaptorType;
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

    public Integer getWifiSignalStrength() {
        return wifiSignalStrength;
    }

    public void setWifiSignalStrength(Integer wifiSignalStrength) {
        this.wifiSignalStrength = wifiSignalStrength;
    }

    public String getWifiAdapterStatus() {
        return wifiAdapterStatus;
    }

    public void setWifiAdapterStatus(String wifiAdapterStatus) {
        this.wifiAdapterStatus = wifiAdapterStatus;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getPCycle() {
        return pCycle;
    }

    public void setPCycle(Integer pCycle) {
        this.pCycle = pCycle;
    }

    public Integer getRecordNumMax() {
        return recordNumMax;
    }

    public void setRecordNumMax(Integer recordNumMax) {
        this.recordNumMax = recordNumMax;
    }

    public String getLastTimeStamp() {
        return lastTimeStamp;
    }

    public void setLastTimeStamp(String lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public String getLastReset() {
        return lastReset;
    }

    public void setLastReset(String lastReset) {
        this.lastReset = lastReset;
    }

    public Integer getFlashWrites() {
        return flashWrites;
    }

    public void setFlashWrites(Integer flashWrites) {
        this.flashWrites = flashWrites;
    }

    public Object getScene() {
        return scene;
    }

    public void setScene(Object scene) {
        this.scene = scene;
    }

    public Object getSSLExpirationDate() {
        return sSLExpirationDate;
    }

    public void setSSLExpirationDate(Object sSLExpirationDate) {
        this.sSLExpirationDate = sSLExpirationDate;
    }

    public Object getSPTimeout() {
        return sPTimeout;
    }

    public void setSPTimeout(Object sPTimeout) {
        this.sPTimeout = sPTimeout;
    }

    public Object getPasscode() {
        return passcode;
    }

    public void setPasscode(Object passcode) {
        this.passcode = passcode;
    }

    public Boolean getServerCommunicationDisabled() {
        return serverCommunicationDisabled;
    }

    public void setServerCommunicationDisabled(Boolean serverCommunicationDisabled) {
        this.serverCommunicationDisabled = serverCommunicationDisabled;
    }

    public Integer getConsecutiveUploadErrors() {
        return consecutiveUploadErrors;
    }

    public void setConsecutiveUploadErrors(Integer consecutiveUploadErrors) {
        this.consecutiveUploadErrors = consecutiveUploadErrors;
    }

    public Object getDoNotRespondAfter() {
        return doNotRespondAfter;
    }

    public void setDoNotRespondAfter(Object doNotRespondAfter) {
        this.doNotRespondAfter = doNotRespondAfter;
    }

    public Integer getOwnerRoleAccessLevel() {
        return ownerRoleAccessLevel;
    }

    public void setOwnerRoleAccessLevel(Integer ownerRoleAccessLevel) {
        this.ownerRoleAccessLevel = ownerRoleAccessLevel;
    }

    public Integer getOwnerCountry() {
        return ownerCountry;
    }

    public void setOwnerCountry(Integer ownerCountry) {
        this.ownerCountry = ownerCountry;
    }

    public Object getRate1StartTime() {
        return rate1StartTime;
    }

    public void setRate1StartTime(Object rate1StartTime) {
        this.rate1StartTime = rate1StartTime;
    }

    public Object getRate2StartTime() {
        return rate2StartTime;
    }

    public void setRate2StartTime(Object rate2StartTime) {
        this.rate2StartTime = rate2StartTime;
    }

    public Integer getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(Integer protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public Integer getUnitVersion() {
        return unitVersion;
    }

    public void setUnitVersion(Integer unitVersion) {
        this.unitVersion = unitVersion;
    }

    public Integer getFirmwareAppVersion() {
        return firmwareAppVersion;
    }

    public void setFirmwareAppVersion(Integer firmwareAppVersion) {
        this.firmwareAppVersion = firmwareAppVersion;
    }

    public Integer getFirmwareWebVersion() {
        return firmwareWebVersion;
    }

    public void setFirmwareWebVersion(Integer firmwareWebVersion) {
        this.firmwareWebVersion = firmwareWebVersion;
    }

    public Integer getFirmwareWlanVersion() {
        return firmwareWlanVersion;
    }

    public void setFirmwareWlanVersion(Integer firmwareWlanVersion) {
        this.firmwareWlanVersion = firmwareWlanVersion;
    }

    public Boolean getHasErrorMessages() {
        return hasErrorMessages;
    }

    public void setHasErrorMessages(Boolean hasErrorMessages) {
        this.hasErrorMessages = hasErrorMessages;
    }

    public Boolean getHasZone2() {
        return hasZone2;
    }

    public void setHasZone2(Boolean hasZone2) {
        this.hasZone2 = hasZone2;
    }

    public Boolean getOffline() {
        return offline;
    }

    public void setOffline(Boolean offline) {
        this.offline = offline;
    }

    public List<Object> getUnits() {
        return units;
    }

    public void setUnits(List<Object> units) {
        this.units = units;
    }
}
