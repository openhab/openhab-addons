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

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceProps} is responsible of JSON data For MELCloud API
 * Device Properties.
 * Generated with jsonschema2pojo
 *
 * @author LucaCalcaterra - Initial contribution
 */
public class DeviceProps {

    @SerializedName("ListHistory24Formatters")
    @Expose
    private List<Object> listHistory24Formatters = null;
    @SerializedName("DeviceType")
    @Expose
    private Integer deviceType;
    @SerializedName("CanCool")
    @Expose
    private Boolean canCool;
    @SerializedName("CanHeat")
    @Expose
    private Boolean canHeat;
    @SerializedName("CanDry")
    @Expose
    private Boolean canDry;
    @SerializedName("HasAutomaticFanSpeed")
    @Expose
    private Boolean hasAutomaticFanSpeed;
    @SerializedName("AirDirectionFunction")
    @Expose
    private Boolean airDirectionFunction;
    @SerializedName("SwingFunction")
    @Expose
    private Boolean swingFunction;
    @SerializedName("NumberOfFanSpeeds")
    @Expose
    private Integer numberOfFanSpeeds;
    @SerializedName("UseTemperatureA")
    @Expose
    private Boolean useTemperatureA;
    @SerializedName("TemperatureIncrementOverride")
    @Expose
    private Integer temperatureIncrementOverride;
    @SerializedName("TemperatureIncrement")
    @Expose
    private Double temperatureIncrement;
    @SerializedName("MinTempCoolDry")
    @Expose
    private Double minTempCoolDry;
    @SerializedName("MaxTempCoolDry")
    @Expose
    private Double maxTempCoolDry;
    @SerializedName("MinTempHeat")
    @Expose
    private Double minTempHeat;
    @SerializedName("MaxTempHeat")
    @Expose
    private Double maxTempHeat;
    @SerializedName("MinTempAutomatic")
    @Expose
    private Double minTempAutomatic;
    @SerializedName("MaxTempAutomatic")
    @Expose
    private Double maxTempAutomatic;
    @SerializedName("LegacyDevice")
    @Expose
    private Boolean legacyDevice;
    @SerializedName("UnitSupportsStandbyMode")
    @Expose
    private Boolean unitSupportsStandbyMode;
    @SerializedName("ModelIsAirCurtain")
    @Expose
    private Boolean modelIsAirCurtain;
    @SerializedName("ModelSupportsFanSpeed")
    @Expose
    private Boolean modelSupportsFanSpeed;
    @SerializedName("ModelSupportsAuto")
    @Expose
    private Boolean modelSupportsAuto;
    @SerializedName("ModelSupportsHeat")
    @Expose
    private Boolean modelSupportsHeat;
    @SerializedName("ModelSupportsDry")
    @Expose
    private Boolean modelSupportsDry;
    @SerializedName("ModelSupportsVaneVertical")
    @Expose
    private Boolean modelSupportsVaneVertical;
    @SerializedName("ModelSupportsVaneHorizontal")
    @Expose
    private Boolean modelSupportsVaneHorizontal;
    @SerializedName("ModelSupportsStandbyMode")
    @Expose
    private Boolean modelSupportsStandbyMode;
    @SerializedName("ModelSupportsEnergyReporting")
    @Expose
    private Boolean modelSupportsEnergyReporting;
    @SerializedName("Power")
    @Expose
    private Boolean power;
    @SerializedName("RoomTemperature")
    @Expose
    private Double roomTemperature;
    @SerializedName("SetTemperature")
    @Expose
    private Double setTemperature;
    @SerializedName("ActualFanSpeed")
    @Expose
    private Integer actualFanSpeed;
    @SerializedName("FanSpeed")
    @Expose
    private Integer fanSpeed;
    @SerializedName("AutomaticFanSpeed")
    @Expose
    private Boolean automaticFanSpeed;
    @SerializedName("VaneVerticalDirection")
    @Expose
    private Integer vaneVerticalDirection;
    @SerializedName("VaneVerticalSwing")
    @Expose
    private Boolean vaneVerticalSwing;
    @SerializedName("VaneHorizontalDirection")
    @Expose
    private Integer vaneHorizontalDirection;
    @SerializedName("VaneHorizontalSwing")
    @Expose
    private Boolean vaneHorizontalSwing;
    @SerializedName("OperationMode")
    @Expose
    private Integer operationMode;
    @SerializedName("EffectiveFlags")
    @Expose
    private Integer effectiveFlags;
    @SerializedName("LastEffectiveFlags")
    @Expose
    private Integer lastEffectiveFlags;
    @SerializedName("InStandbyMode")
    @Expose
    private Boolean inStandbyMode;
    @SerializedName("DefaultCoolingSetTemperature")
    @Expose
    private Double defaultCoolingSetTemperature;
    @SerializedName("DefaultHeatingSetTemperature")
    @Expose
    private Double defaultHeatingSetTemperature;
    @SerializedName("RoomTemperatureLabel")
    @Expose
    private Integer roomTemperatureLabel;
    @SerializedName("HasEnergyConsumedMeter")
    @Expose
    private Boolean hasEnergyConsumedMeter;
    @SerializedName("CurrentEnergyConsumed")
    @Expose
    private Integer currentEnergyConsumed;
    @SerializedName("CurrentEnergyMode")
    @Expose
    private Integer currentEnergyMode;
    @SerializedName("CoolingDisabled")
    @Expose
    private Boolean coolingDisabled;
    @SerializedName("MinPcycle")
    @Expose
    private Integer minPcycle;
    @SerializedName("MaxPcycle")
    @Expose
    private Integer maxPcycle;
    @SerializedName("EffectivePCycle")
    @Expose
    private Integer effectivePCycle;
    @SerializedName("MaxOutdoorUnits")
    @Expose
    private Integer maxOutdoorUnits;
    @SerializedName("MaxIndoorUnits")
    @Expose
    private Integer maxIndoorUnits;
    @SerializedName("MaxTemperatureControlUnits")
    @Expose
    private Integer maxTemperatureControlUnits;
    @SerializedName("DeviceID")
    @Expose
    private Integer deviceID;
    @SerializedName("MacAddress")
    @Expose
    private String macAddress;
    @SerializedName("SerialNumber")
    @Expose
    private String serialNumber;
    @SerializedName("TimeZoneID")
    @Expose
    private Integer timeZoneID;
    @SerializedName("DiagnosticMode")
    @Expose
    private Integer diagnosticMode;
    @SerializedName("DiagnosticEndDate")
    @Expose
    private Object diagnosticEndDate;
    @SerializedName("ExpectedCommand")
    @Expose
    private Integer expectedCommand;
    @SerializedName("Owner")
    @Expose
    private Object owner;
    @SerializedName("DetectedCountry")
    @Expose
    private Object detectedCountry;
    @SerializedName("AdaptorType")
    @Expose
    private Integer adaptorType;
    @SerializedName("FirmwareDeployment")
    @Expose
    private Object firmwareDeployment;
    @SerializedName("FirmwareUpdateAborted")
    @Expose
    private Boolean firmwareUpdateAborted;
    @SerializedName("WifiSignalStrength")
    @Expose
    private Integer wifiSignalStrength;
    @SerializedName("WifiAdapterStatus")
    @Expose
    private String wifiAdapterStatus;
    @SerializedName("Position")
    @Expose
    private String position;
    @SerializedName("PCycle")
    @Expose
    private Integer pCycle;
    @SerializedName("RecordNumMax")
    @Expose
    private Integer recordNumMax;
    @SerializedName("LastTimeStamp")
    @Expose
    private String lastTimeStamp;
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("HasError")
    @Expose
    private Boolean hasError;
    @SerializedName("LastReset")
    @Expose
    private String lastReset;
    @SerializedName("FlashWrites")
    @Expose
    private Integer flashWrites;
    @SerializedName("Scene")
    @Expose
    private Object scene;
    @SerializedName("SSLExpirationDate")
    @Expose
    private Object sSLExpirationDate;
    @SerializedName("SPTimeout")
    @Expose
    private Object sPTimeout;
    @SerializedName("Passcode")
    @Expose
    private Object passcode;
    @SerializedName("ServerCommunicationDisabled")
    @Expose
    private Boolean serverCommunicationDisabled;
    @SerializedName("ConsecutiveUploadErrors")
    @Expose
    private Integer consecutiveUploadErrors;
    @SerializedName("DoNotRespondAfter")
    @Expose
    private Object doNotRespondAfter;
    @SerializedName("OwnerRoleAccessLevel")
    @Expose
    private Integer ownerRoleAccessLevel;
    @SerializedName("OwnerCountry")
    @Expose
    private Integer ownerCountry;
    @SerializedName("Rate1StartTime")
    @Expose
    private Object rate1StartTime;
    @SerializedName("Rate2StartTime")
    @Expose
    private Object rate2StartTime;
    @SerializedName("ProtocolVersion")
    @Expose
    private Integer protocolVersion;
    @SerializedName("UnitVersion")
    @Expose
    private Integer unitVersion;
    @SerializedName("FirmwareAppVersion")
    @Expose
    private Integer firmwareAppVersion;
    @SerializedName("FirmwareWebVersion")
    @Expose
    private Integer firmwareWebVersion;
    @SerializedName("FirmwareWlanVersion")
    @Expose
    private Integer firmwareWlanVersion;
    @SerializedName("HasErrorMessages")
    @Expose
    private Boolean hasErrorMessages;
    @SerializedName("HasZone2")
    @Expose
    private Boolean hasZone2;
    @SerializedName("Offline")
    @Expose
    private Boolean offline;
    @SerializedName("Units")
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
