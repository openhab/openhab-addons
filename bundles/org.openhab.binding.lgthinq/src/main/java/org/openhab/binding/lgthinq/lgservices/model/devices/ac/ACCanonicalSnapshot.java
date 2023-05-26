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

    private int airWindStrength;

    private double targetTemperature;

    private double currentTemperature;

    private boolean coolJetModeOn;

    private double airCleanMode;
    private double coolJetMode;
    private double autoDryMode;
    private double energySavingMode;

    private int operationMode;
    @Nullable
    private Integer operation;
    @JsonIgnore
    private boolean online;

    private double energyConsumption;

    @JsonIgnore
    public DevicePowerState getPowerStatus() {
        return operation == null ? DevicePowerState.DV_POWER_UNK : DevicePowerState.statusOf(operation);
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

    @JsonProperty("airState.wMode.jet")
    @JsonAlias("Jet")
    public Double getCoolJetMode() {
        return coolJetMode;
    }

    @JsonProperty("airState.wMode.airClean")
    @JsonAlias("AirClean")
    public Double getAirCleanMode() {
        return airCleanMode;
    }

    @JsonProperty("airState.miscFuncState.autoDry")
    @JsonAlias("AutoDry")
    public Double getAutoDryMode() {
        return autoDryMode;
    }

    @JsonProperty("airState.powerSave.basic")
    @JsonAlias("PowerSave")
    public Double getEnergySavingMode() {
        return energySavingMode;
    }

    public void setAirCleanMode(double airCleanMode) {
        this.airCleanMode = airCleanMode;
    }

    public void setAutoDryMode(double autoDryMode) {
        this.autoDryMode = autoDryMode;
    }

    public void setEnergySavingMode(double energySavingMode) {
        this.energySavingMode = energySavingMode;
    }

    public void setCoolJetMode(Double coolJetMode) {
        this.coolJetMode = coolJetMode;
    }

    public void setAirWindStrength(Integer airWindStrength) {
        this.airWindStrength = airWindStrength;
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

    @JsonIgnore
    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return "ACSnapShot{" + "airWindStrength=" + airWindStrength + ", targetTemperature=" + targetTemperature
                + ", currentTemperature=" + currentTemperature + ", operationMode=" + operationMode + ", operation="
                + operation + ", acPowerStatus=" + getPowerStatus() + ", acFanSpeed=" + getAcFanSpeed() + ", acOpMode="
                + ", online=" + isOnline() + " }";
    }
}
