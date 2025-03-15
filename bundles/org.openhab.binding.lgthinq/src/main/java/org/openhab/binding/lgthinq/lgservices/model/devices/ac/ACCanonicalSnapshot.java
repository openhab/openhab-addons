/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.model.AbstractSnapshotDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link ACCanonicalSnapshot}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@JsonIgnoreProperties(ignoreUnknown = true)
public class ACCanonicalSnapshot extends AbstractSnapshotDefinition {

    // ============ FOR HEAT PUMP ONLY ===============
    private double hpWaterTempCoolMin;
    private double hpWaterTempCoolMax;
    private double hpWaterTempHeatMin;
    private double hpWaterTempHeatMax;
    private double hpAirTempCoolMin;
    private double hpAirTempCoolMax;
    private double hpAirTempHeatMin;
    private double hpAirTempHeatMax;
    private double hpAirWaterTempSwitch = -1;
    // ===============================================

    private int airWindStrength;
    private double targetTemperature;
    private double currentTemperature;
    private double airCleanMode;
    private double coolJetMode;
    private double autoDryMode;
    private double energySavingMode;
    private double stepUpDownMode;
    private double stepLeftRightMode;

    private int operationMode;
    @Nullable
    private Integer operation;
    @JsonIgnore
    private boolean online;

    private double energyConsumption;

    @JsonIgnore
    public DevicePowerState getPowerStatus() {
        return DevicePowerState.statusOf(operation);
    }

    @JsonIgnore
    public void setPowerStatus(DevicePowerState value) {
        operation = (int) value.getValue();
    }

    @JsonIgnore
    public ACFanSpeed getAcFanSpeed() {
        return ACFanSpeed.statusOf(airWindStrength);
    }

    @JsonProperty("airState.windStrength")
    @JsonAlias("WindStrength")
    public Integer getAirWindStrength() {
        return airWindStrength;
    }

    public void setAirWindStrength(Integer airWindStrength) {
        this.airWindStrength = airWindStrength;
    }

    @JsonProperty("airState.wMode.jet")
    @JsonAlias("Jet")
    public Double getCoolJetMode() {
        return coolJetMode;
    }

    public void setCoolJetMode(Double coolJetMode) {
        this.coolJetMode = coolJetMode;
    }

    @JsonProperty("airState.wMode.airClean")
    @JsonAlias("AirClean")
    public Double getAirCleanMode() {
        return airCleanMode;
    }

    public void setAirCleanMode(double airCleanMode) {
        this.airCleanMode = airCleanMode;
    }

    @JsonProperty("airState.miscFuncState.autoDry")
    @JsonAlias("AutoDry")
    public Double getAutoDryMode() {
        return autoDryMode;
    }

    public void setAutoDryMode(double autoDryMode) {
        this.autoDryMode = autoDryMode;
    }

    @JsonProperty("airState.powerSave.basic")
    @JsonAlias("PowerSave")
    public Double getEnergySavingMode() {
        return energySavingMode;
    }

    public void setEnergySavingMode(double energySavingMode) {
        this.energySavingMode = energySavingMode;
    }

    @JsonProperty("airState.energy.onCurrent")
    public double getEnergyConsumption() {
        return energyConsumption;
    }

    public void setEnergyConsumption(double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    @JsonProperty("airState.tempState.target")
    @JsonAlias("TempCfg")
    public Double getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(Double targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    @JsonProperty("airState.tempState.current")
    @JsonAlias("TempCur")
    public Double getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(Double currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    @JsonProperty("airState.opMode")
    @JsonAlias("OpMode")
    public Integer getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(Integer operationMode) {
        this.operationMode = operationMode;
    }

    @Nullable
    @JsonProperty("airState.operation")
    @JsonAlias("Operation")
    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    @JsonProperty("airState.wDir.vStep")
    @JsonAlias("WDirVStep")
    public double getStepUpDownMode() {
        return stepUpDownMode;
    }

    public void setStepUpDownMode(double stepUpDownMode) {
        this.stepUpDownMode = stepUpDownMode;
    }

    @JsonProperty("airState.wDir.hStep")
    @JsonAlias("WDirHStep")
    public double getStepLeftRightMode() {
        return stepLeftRightMode;
    }

    public void setStepLeftRightMode(double stepLeftRightMode) {
        this.stepLeftRightMode = stepLeftRightMode;
    }

    @JsonIgnore
    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    // ==================== For HP only
    @JsonProperty("airState.tempState.waterTempCoolMin")
    public double getHpWaterTempCoolMin() {
        return hpWaterTempCoolMin;
    }

    public void setHpWaterTempCoolMin(double hpWaterTempCoolMin) {
        this.hpWaterTempCoolMin = hpWaterTempCoolMin;
    }

    @JsonProperty("airState.tempState.waterTempCoolMax")
    public double getHpWaterTempCoolMax() {
        return hpWaterTempCoolMax;
    }

    public void setHpWaterTempCoolMax(double hpWaterTempCoolMax) {
        this.hpWaterTempCoolMax = hpWaterTempCoolMax;
    }

    @JsonProperty("airState.tempState.waterTempHeatMin")
    public double getHpWaterTempHeatMin() {
        return hpWaterTempHeatMin;
    }

    public void setHpWaterTempHeatMin(double hpWaterTempHeatMin) {
        this.hpWaterTempHeatMin = hpWaterTempHeatMin;
    }

    @JsonProperty("airState.tempState.waterTempHeatMax")
    public double getHpWaterTempHeatMax() {
        return hpWaterTempHeatMax;
    }

    public void setHpWaterTempHeatMax(double hpWaterTempHeatMax) {
        this.hpWaterTempHeatMax = hpWaterTempHeatMax;
    }

    @JsonProperty("airState.tempState.airTempCoolMin")
    public double getHpAirTempCoolMin() {
        return hpAirTempCoolMin;
    }

    public void setHpAirTempCoolMin(double hpAirTempCoolMin) {
        this.hpAirTempCoolMin = hpAirTempCoolMin;
    }

    @JsonProperty("airState.tempState.airTempCoolMax")
    public double getHpAirTempCoolMax() {
        return hpAirTempCoolMax;
    }

    public void setHpAirTempCoolMax(double hpAirTempCoolMax) {
        this.hpAirTempCoolMax = hpAirTempCoolMax;
    }

    @JsonProperty("airState.tempState.airTempHeatMin")
    public double getHpAirTempHeatMin() {
        return hpAirTempHeatMin;
    }

    public void setHpAirTempHeatMin(double hpAirTempHeatMin) {
        this.hpAirTempHeatMin = hpAirTempHeatMin;
    }

    @JsonProperty("airState.tempState.airTempHeatMax")
    public double getHpAirTempHeatMax() {
        return hpAirTempHeatMax;
    }

    public void setHpAirTempHeatMax(double hpAirTempHeatMax) {
        this.hpAirTempHeatMax = hpAirTempHeatMax;
    }

    @JsonProperty("airState.miscFuncState.awhpTempSwitch")
    public double getHpAirWaterTempSwitch() {
        return hpAirWaterTempSwitch;
    }

    public void setHpAirWaterTempSwitch(double hpAirWaterTempSwitch) {
        this.hpAirWaterTempSwitch = hpAirWaterTempSwitch;
    }
    // ===================================

    @Override
    public String toString() {
        return "ACSnapShot{" + "airWindStrength=" + airWindStrength + ", targetTemperature=" + targetTemperature
                + ", currentTemperature=" + currentTemperature + ", operationMode=" + operationMode + ", operation="
                + operation + ", acPowerStatus=" + getPowerStatus() + ", acFanSpeed=" + getAcFanSpeed() + ", acOpMode="
                + ", online=" + isOnline() + " }";
    }
}
