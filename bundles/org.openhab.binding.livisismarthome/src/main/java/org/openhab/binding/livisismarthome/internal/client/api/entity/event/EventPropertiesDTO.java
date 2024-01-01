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
package org.openhab.binding.livisismarthome.internal.client.api.entity.event;

import com.google.gson.annotations.SerializedName;

/**
 * @author Oliver Kuhl - Initial contribution
 */
public class EventPropertiesDTO {

    /** SHC Properties **/
    private Integer configVersion;
    private Boolean isConnected;

    /** Writable capability properties **/
    private Integer dimLevel;
    private Boolean onState;
    private String operationMode;
    private String operationStatus;
    private Double pointTemperature;
    private Integer shutterLevel;
    private Boolean value;

    /** readable capability properties **/
    private Double absoluteEnergyConsumption;
    private Double energyConsumptionDayEuro;
    private Double energyConsumptionDayKWh;
    private Double energyConsumptionMonthEuro;
    private Double energyConsumptionMonthKWh;
    private Double energyPerDayInEuro;
    private Double energyPerDayInKWh;
    private Double energyPerMonthInEuro;
    private Double energyPerMonthInKWh;
    private Boolean frostWarning;
    private Double humidity;
    private Boolean isReachable;
    private Boolean isOpen;
    private Boolean isSmokeAlarm;
    @SerializedName("type")
    private String keyPressType;
    @SerializedName("index")
    private Integer keyPressButtonIndex;
    private Integer keyPressCounter;
    @SerializedName("lastPressedButtonIndex")
    private Integer lastKeyPressButtonIndex;
    private Integer lastKeyPressCounter;
    private Double luminance;
    private Boolean moldWarning;
    private Integer motionDetectedCount;
    private Double powerConsumptionWatt;
    private Double powerInWatt;
    private Double temperature;
    private Double totalEnergy;
    private Boolean windowReductionActive;
    private Double cpuUsage;
    private Double diskUsage;
    private Double memoryUsage;
    @SerializedName("CPULoad")
    private Double cpuLoad;
    private Double memoryLoad;
    @SerializedName("OSState")
    private String osState;

    /**
     * @return the configurationVersion
     */
    public Integer getConfigVersion() {
        return configVersion;
    }

    /**
     * @param configVersion the configurationVersion to set
     */
    public void setConfigVersion(final Integer configVersion) {
        this.configVersion = configVersion;
    }

    /**
     * @return the isConnected
     */
    public Boolean getIsConnected() {
        return isConnected;
    }

    /**
     * @param isConnected the isConnected to set
     */
    public void setIsConnected(final Boolean isConnected) {
        this.isConnected = isConnected;
    }

    /**
     * @return the dimLevel
     */
    public Integer getDimLevel() {
        return dimLevel;
    }

    /**
     * @param dimLevel the dimLevel to set
     */
    public void setDimLevel(final Integer dimLevel) {
        this.dimLevel = dimLevel;
    }

    /**
     * @return the onState
     */
    public Boolean getOnState() {
        return onState;
    }

    /**
     * @param onState the onState to set
     */
    public void setOnState(final Boolean onState) {
        this.onState = onState;
    }

    /**
     * @return the operationMode
     */
    public String getOperationMode() {
        return operationMode;
    }

    /**
     * @param operationMode the operationMode to set
     */
    public void setOperationMode(final String operationMode) {
        this.operationMode = operationMode;
    }

    /**
     * @return the operationStatus
     */
    public String getOperationStatus() {
        return operationStatus;
    }

    /**
     * @param operationStatus the operationStatus to set
     */
    public void setOperationStatus(final String operationStatus) {
        this.operationStatus = operationStatus;
    }

    /**
     * @return the pointTemperature
     */
    public Double getPointTemperature() {
        return pointTemperature;
    }

    /**
     * @param pointTemperature the pointTemperature to set
     */
    public void setPointTemperature(final Double pointTemperature) {
        this.pointTemperature = pointTemperature;
    }

    /**
     * @return the shutterLevel
     */
    public Integer getShutterLevel() {
        return shutterLevel;
    }

    /**
     * @param shutterLevel the shutterLevel to set
     */
    public void setShutterLevel(final Integer shutterLevel) {
        this.shutterLevel = shutterLevel;
    }

    /**
     * @return the value
     */
    public Boolean getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(final Boolean value) {
        this.value = value;
    }

    /**
     * @return the absoluteEnergyConsumption
     */
    public Double getAbsoluteEnergyConsumption() {
        return absoluteEnergyConsumption;
    }

    /**
     * @param absoluteEnergyConsumption the absoluteEnergyConsumption to set
     */
    public void setAbsoluteEnergyConsumption(final Double absoluteEnergyConsumption) {
        this.absoluteEnergyConsumption = absoluteEnergyConsumption;
    }

    /**
     * @return the energyConsumptionDayEuro
     */
    public Double getEnergyConsumptionDayEuro() {
        return energyConsumptionDayEuro;
    }

    /**
     * @param energyConsumptionDayEuro the energyConsumptionDayEuro to set
     */
    public void setEnergyConsumptionDayEuro(final Double energyConsumptionDayEuro) {
        this.energyConsumptionDayEuro = energyConsumptionDayEuro;
    }

    /**
     * @return the energyConsumptionDayKWh
     */
    public Double getEnergyConsumptionDayKWh() {
        return energyConsumptionDayKWh;
    }

    /**
     * @param energyConsumptionDayKWh the energyConsumptionDayKWh to set
     */
    public void setEnergyConsumptionDayKWh(final Double energyConsumptionDayKWh) {
        this.energyConsumptionDayKWh = energyConsumptionDayKWh;
    }

    /**
     * @return the energyConsumptionMonthEuro
     */
    public Double getEnergyConsumptionMonthEuro() {
        return energyConsumptionMonthEuro;
    }

    /**
     * @param energyConsumptionMonthEuro the energyConsumptionMonthEuro to set
     */
    public void setEnergyConsumptionMonthEuro(final Double energyConsumptionMonthEuro) {
        this.energyConsumptionMonthEuro = energyConsumptionMonthEuro;
    }

    /**
     * @return the energyConsumptionMonthKWh
     */
    public Double getEnergyConsumptionMonthKWh() {
        return energyConsumptionMonthKWh;
    }

    /**
     * @param energyConsumptionMonthKWh the energyConsumptionMonthKWh to set
     */
    public void setEnergyConsumptionMonthKWh(final Double energyConsumptionMonthKWh) {
        this.energyConsumptionMonthKWh = energyConsumptionMonthKWh;
    }

    /**
     * @return the energyPerDayInEuro
     */
    public Double getEnergyPerDayInEuro() {
        return energyPerDayInEuro;
    }

    /**
     * @param energyPerDayInEuro the energyPerDayInEuro to set
     */
    public void setEnergyPerDayInEuro(final Double energyPerDayInEuro) {
        this.energyPerDayInEuro = energyPerDayInEuro;
    }

    /**
     * @return the energyPerDayInKWh
     */
    public Double getEnergyPerDayInKWh() {
        return energyPerDayInKWh;
    }

    /**
     * @param energyPerDayInKWh the energyPerDayInKWh to set
     */
    public void setEnergyPerDayInKWh(final Double energyPerDayInKWh) {
        this.energyPerDayInKWh = energyPerDayInKWh;
    }

    /**
     * @return the energyPerMonthInEuro
     */
    public Double getEnergyPerMonthInEuro() {
        return energyPerMonthInEuro;
    }

    /**
     * @param energyPerMonthInEuro the energyPerMonthInEuro to set
     */
    public void setEnergyPerMonthInEuro(final Double energyPerMonthInEuro) {
        this.energyPerMonthInEuro = energyPerMonthInEuro;
    }

    /**
     * @return the energyPerMonthInKWh
     */
    public Double getEnergyPerMonthInKWh() {
        return energyPerMonthInKWh;
    }

    /**
     * @param energyPerMonthInKWh the energyPerMonthInKWh to set
     */
    public void setEnergyPerMonthInKWh(final Double energyPerMonthInKWh) {
        this.energyPerMonthInKWh = energyPerMonthInKWh;
    }

    /**
     * @return the frostWarning
     */
    public Boolean getFrostWarning() {
        return frostWarning;
    }

    /**
     * @param frostWarning the frostWarning to set
     */
    public void setFrostWarning(final Boolean frostWarning) {
        this.frostWarning = frostWarning;
    }

    /**
     * @return the humidity
     */
    public Double getHumidity() {
        return humidity;
    }

    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(final Double humidity) {
        this.humidity = humidity;
    }

    /**
     * @return if the device is reachable
     */
    public Boolean getReachable() {
        return isReachable;
    }

    /**
     * @param reachable if the device is reachable
     */
    public void setReachable(Boolean reachable) {
        isReachable = reachable;
    }

    /**
     * @return the isOpen
     */
    public Boolean getIsOpen() {
        return isOpen;
    }

    /**
     * @param isOpen the isOpen to set
     */
    public void setIsOpen(final Boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * @return the isSmokeAlarm
     */
    public Boolean getIsSmokeAlarm() {
        return isSmokeAlarm;
    }

    /**
     * @param isSmokeAlarm the isSmokeAlarm to set
     */
    public void setIsSmokeAlarm(final Boolean isSmokeAlarm) {
        this.isSmokeAlarm = isSmokeAlarm;
    }

    public String getKeyPressType() {
        return keyPressType;
    }

    public void setKeyPressType(final String keyPressType) {
        this.keyPressType = keyPressType;
    }

    public Integer getKeyPressButtonIndex() {
        return keyPressButtonIndex;
    }

    public void setKeyPressButtonIndex(final Integer keyPressButtonIndex) {
        this.keyPressButtonIndex = keyPressButtonIndex;
    }

    public Integer getKeyPressCounter() {
        return keyPressCounter;
    }

    public void setKeyPressCounter(final Integer keyPressCounter) {
        this.keyPressCounter = keyPressCounter;
    }

    public Integer getLastKeyPressButtonIndex() {
        return lastKeyPressButtonIndex;
    }

    public void setLastKeyPressButtonIndex(final Integer lastKeyPressButtonIndex) {
        this.lastKeyPressButtonIndex = lastKeyPressButtonIndex;
    }

    public Integer getLastKeyPressCounter() {
        return lastKeyPressCounter;
    }

    public void setLastKeyPressCounter(final Integer lastKeyPressCounter) {
        this.lastKeyPressCounter = lastKeyPressCounter;
    }

    /**
     * @return the luminance
     */
    public Double getLuminance() {
        return luminance;
    }

    /**
     * @param luminance the luminance to set
     */
    public void setLuminance(final Double luminance) {
        this.luminance = luminance;
    }

    /**
     * @return the moldWarning
     */
    public Boolean getMoldWarning() {
        return moldWarning;
    }

    /**
     * @param moldWarning the moldWarning to set
     */
    public void setMoldWarning(final Boolean moldWarning) {
        this.moldWarning = moldWarning;
    }

    /**
     * @return the motionDetectedCount
     */
    public Integer getMotionDetectedCount() {
        return motionDetectedCount;
    }

    /**
     * @param motionDetectedCount the motionDetectedCount to set
     */
    public void setMotionDetectedCount(final Integer motionDetectedCount) {
        this.motionDetectedCount = motionDetectedCount;
    }

    /**
     * @return the powerConsumptionWatt
     */
    public Double getPowerConsumptionWatt() {
        return powerConsumptionWatt;
    }

    /**
     * @param powerConsumptionWatt the powerConsumptionWatt to set
     */
    public void setPowerConsumptionWatt(final Double powerConsumptionWatt) {
        this.powerConsumptionWatt = powerConsumptionWatt;
    }

    /**
     * @return the powerInWatt
     */
    public Double getPowerInWatt() {
        return powerInWatt;
    }

    /**
     * @param powerInWatt the powerInWatt to set
     */
    public void setPowerInWatt(final Double powerInWatt) {
        this.powerInWatt = powerInWatt;
    }

    /**
     * @return the temperature
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final Double temperature) {
        this.temperature = temperature;
    }

    /**
     * @return the totalEnergy
     */
    public Double getTotalEnergy() {
        return totalEnergy;
    }

    /**
     * @param totalEnergy the totalEnergy to set
     */
    public void setTotalEnergy(final Double totalEnergy) {
        this.totalEnergy = totalEnergy;
    }

    /**
     * @return the windowReductionActive
     */
    public Boolean getWindowReductionActive() {
        return windowReductionActive;
    }

    /**
     * @param windowReductionActive the windowReductionActive to set
     */
    public void setWindowReductionActive(final Boolean windowReductionActive) {
        this.windowReductionActive = windowReductionActive;
    }

    /**
     * @param cpuUsage the cpuUsage to set
     */
    public void setCpuUsage(final Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * @return the cpuUsage
     */
    public Double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * @param diskUsage the diskUsage to set
     */
    public void setDiskUsage(final Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    /**
     * @return the diskUsage
     */
    public Double getDiskUsage() {
        return diskUsage;
    }

    /**
     * @param memoryUsage the memoryUsage to set
     */
    public void setMemoryUsage(final Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    /**
     * @return the memoryUsage
     */
    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public Double getCPULoad() {
        return cpuLoad;
    }

    public void setCPULoad(Double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public Double getCpuUsage(boolean isSHCClassic) {
        if (isSHCClassic) {
            return getCPULoad();
        }
        return getCpuUsage();
    }

    public Double getMemoryLoad() {
        return memoryLoad;
    }

    public void setMemoryLoad(Double memoryLoad) {
        this.memoryLoad = memoryLoad;
    }

    /**
     * @return the memoryUsage
     */
    public Double getMemoryUsage(boolean isSHCClassic) {
        if (isSHCClassic) {
            return getMemoryLoad();
        }
        return getMemoryUsage();
    }

    public String getOSState() {
        return osState;
    }

    public void setOSState(String osState) {
        this.osState = osState;
    }

    /**
     * @return the operationStatus
     */
    public String getOperationStatus(boolean isSHCClassic) {
        if (isSHCClassic) {
            return getOSState();
        }
        return getOperationStatus();
    }
}
