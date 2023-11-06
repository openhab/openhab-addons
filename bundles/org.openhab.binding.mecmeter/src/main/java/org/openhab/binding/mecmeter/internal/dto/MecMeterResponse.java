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
package org.openhab.binding.mecmeter.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MecMeterResponse} is responsible for storing
 * the "data" node of the JSON response
 *
 * @author Florian Pazour - Initial contribution
 * @author Klaus Berger - Initial contribution
 * @author Kai Kreuzer - Refactoring for openHAB 3
 */
public class MecMeterResponse {
    /* General */
    @SerializedName("F")
    private float frequency;
    @SerializedName("T")
    private float temperature;
    @SerializedName("TIME")
    private long operationalTime;

    /* Voltage */
    @SerializedName("VA")
    private float voltagePhase1;
    @SerializedName("VB")
    private float voltagePhase2;
    @SerializedName("VC")
    private float voltagePhase3;
    @SerializedName("VCB")
    private float voltagePhase3ToPhase2;
    @SerializedName("VBA")
    private float voltagePhase2ToPhase1;
    @SerializedName("VAC")
    private float voltagePhase1ToPhase3;
    @SerializedName("VPT")
    private float averageVoltagePhaseToPhase;
    @SerializedName("VT")
    private float averageVoltageNeutralToPhase;

    /* Current */
    @SerializedName("IA")
    private float currentPhase1;
    @SerializedName("IB")
    private float currentPhase2;
    @SerializedName("IC")
    private float currentPhase3;
    @SerializedName("IN")
    private float currentSum;

    /* Angles */
    @SerializedName("IAA")
    private float phaseAngleCurrentToVoltagePhase1;
    @SerializedName("IAB")
    private float phaseAngleCurrentToVoltagePhase2;
    @SerializedName("IAC")
    private float phaseAngleCurrentToVoltagePhase3;
    @SerializedName("UAA")
    private float phaseAnglePhase1To3;
    @SerializedName("UAB")
    private float phaseAnglePhase2To3;

    /* Power */
    @SerializedName("PA")
    private float activePowerPhase1;
    @SerializedName("PB")
    private float activePowerPhase2;
    @SerializedName("PC")
    private float activePowerPhase3;
    @SerializedName("PT")
    private float activePowerSum;

    @SerializedName("PAF")
    private float activeFundamentalPowerPhase1;
    @SerializedName("PBF")
    private float activeFundamentalPowerPhase2;
    @SerializedName("PCF")
    private float activeFundamentalPowerPhase3;
    @SerializedName("PTF")
    private float activeFundamentalPowerSum;

    @SerializedName("PFA")
    private float powerFactorPhase1;
    @SerializedName("PFB")
    private float powerFactorPhase2;
    @SerializedName("PFC")
    private float powerFactorPhase3;
    @SerializedName("PFT")
    private float powerFactorSum;

    @SerializedName("PAH")
    private float activeHarmonicPowerPhase1;
    @SerializedName("PBH")
    private float activeHarmonicPowerPhase2;
    @SerializedName("PCH")
    private float activeHarmonicPowerPhase3;
    @SerializedName("PTH")
    private float activeHarmonicPowerSum;

    @SerializedName("QA")
    private float reactivePowerPhase1;
    @SerializedName("QB")
    private float reactivePowerPhase2;
    @SerializedName("QC")
    private float reactivePowerPhase3;
    @SerializedName("QT")
    private float reactivePowerSum;

    @SerializedName("SA")
    private float apparentPowerPhase1;
    @SerializedName("SB")
    private float apparentPowerPhase2;
    @SerializedName("SC")
    private float apparentPowerPhase3;
    @SerializedName("ST")
    private float apparentPowerSum;

    /* Forward Energy */
    @SerializedName("EFAA")
    private float forwardActiveEnergyPhase1;
    @SerializedName("EFAB")
    private float forwardActiveEnergyPhase2;
    @SerializedName("EFAC")
    private float forwardActiveEnergyPhase3;
    @SerializedName("EFAT")
    private float forwardActiveEnergySum;

    @SerializedName("EFAF")
    private float forwardActiveFundamentalEnergyPhase1;
    @SerializedName("EFBF")
    private float forwardActiveFundamentalEnergyPhase2;
    @SerializedName("EFCF")
    private float forwardActiveFundamentalEnergyPhase3;
    @SerializedName("EFTF")
    private float forwardActiveFundamentalEnergySum;

    @SerializedName("EFAH")
    private float forwardActiveHarmonicEnergyPhase1;
    @SerializedName("EFBH")
    private float forwardActiveHarmonicEnergyPhase2;
    @SerializedName("EFCH")
    private float forwardActiveHarmonicEnergyPhase3;
    @SerializedName("EFTH")
    private float forwardActiveHarmonicEnergySum;

    @SerializedName("EFRA")
    private float forwardReactiveEnergyPhase1;
    @SerializedName("EFRB")
    private float forwardReactiveEnergyPhase2;
    @SerializedName("EFRC")
    private float forwardReactiveEnergyPhase3;
    @SerializedName("EFRT")
    private float forwardReactiveEnergySum;

    /* Reverse Energy */
    @SerializedName("ERAA")
    private float reverseActiveEnergyPhase1;
    @SerializedName("ERAB")
    private float reverseActiveEnergyPhase2;
    @SerializedName("ERAC")
    private float reverseActiveEnergyPhase3;
    @SerializedName("ERAT")
    private float reverseActiveEnergySum;

    @SerializedName("ERAF")
    private float reverseActiveFundamentalEnergyPhase1;
    @SerializedName("ERBF")
    private float reverseActiveFundamentalEnergyPhase2;
    @SerializedName("ERCF")
    private float reverseActiveFundamentalEnergyPhase3;
    @SerializedName("ERTF")
    private float reverseActiveFundamentalEnergySum;

    @SerializedName("ERAH")
    private float reverseActiveHarmonicEnergyPhase1;
    @SerializedName("ERBH")
    private float reverseActiveHarmonicEnergyPhase2;
    @SerializedName("ERCH")
    private float reverseActiveHarmonicEnergyPhase3;
    @SerializedName("ERTH")
    private float reverseActiveHarmonicEnergySum;

    @SerializedName("ERRA")
    private float reverseReactiveEnergyPhase1;
    @SerializedName("ERRB")
    private float reverseReactiveEnergyPhase2;
    @SerializedName("ERRC")
    private float reverseReactiveEnergyPhase3;
    @SerializedName("ERRT")
    private float reverseReactiveEnergySum;

    /* apparent Energy */
    @SerializedName("ESA")
    private float apparentEnergyConsumptionPhase1;
    @SerializedName("ESB")
    private float apparentEnergyConsumptionPhase2;
    @SerializedName("ESC")
    private float apparentEnergyConsumptionPhase3;
    @SerializedName("EST")
    private float apparentEnergyConsumptionSum;

    /* Constants */
    private static final int KILO = 1000;

    /* Getters and Setters */
    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public long getOperationalTime() {
        return operationalTime;
    }

    public void setOperationalTime(long operationalTime) {
        this.operationalTime = operationalTime;
    }

    public float getVoltagePhase1() {
        return voltagePhase1;
    }

    public void setVoltagePhase1(float voltagePhase1) {
        this.voltagePhase1 = voltagePhase1;
    }

    public float getVoltagePhase2() {
        return voltagePhase2;
    }

    public void setVoltagePhase2(float voltagePhase2) {
        this.voltagePhase2 = voltagePhase2;
    }

    public float getVoltagePhase3() {
        return voltagePhase3;
    }

    public void setVoltagePhase3(float voltagePhase3) {
        this.voltagePhase3 = voltagePhase3;
    }

    public float getVoltagePhase3ToPhase2() {
        return voltagePhase3ToPhase2;
    }

    public void setVoltagePhase3ToPhase2(float voltagePhase3ToPhase2) {
        this.voltagePhase3ToPhase2 = voltagePhase3ToPhase2;
    }

    public float getVoltagePhase2ToPhase1() {
        return voltagePhase2ToPhase1;
    }

    public void setVoltagePhase2ToPhase1(float voltagePhase2ToPhase1) {
        this.voltagePhase2ToPhase1 = voltagePhase2ToPhase1;
    }

    public float getVoltagePhase1ToPhase3() {
        return voltagePhase1ToPhase3;
    }

    public void setVoltagePhase1ToPhase3(float voltagePhase1ToPhase3) {
        this.voltagePhase1ToPhase3 = voltagePhase1ToPhase3;
    }

    public float getAverageVoltagePhaseToPhase() {
        return averageVoltagePhaseToPhase;
    }

    public void setAverageVoltagePhaseToPhase(float averageVoltagePhaseToPhase) {
        this.averageVoltagePhaseToPhase = averageVoltagePhaseToPhase;
    }

    public float getAverageVoltageNeutralToPhase() {
        return averageVoltageNeutralToPhase;
    }

    public void setAverageVoltageNeutralToPhase(float averageVoltageNeutralToPhase) {
        this.averageVoltageNeutralToPhase = averageVoltageNeutralToPhase;
    }

    public float getCurrentPhase1() {
        return currentPhase1;
    }

    public void setCurrentPhase1(float currentPhase1) {
        this.currentPhase1 = currentPhase1;
    }

    public float getCurrentPhase2() {
        return currentPhase2;
    }

    public void setCurrentPhase2(float currentPhase2) {
        this.currentPhase2 = currentPhase2;
    }

    public float getCurrentPhase3() {
        return currentPhase3;
    }

    public void setCurrentPhase3(float currentPhase3) {
        this.currentPhase3 = currentPhase3;
    }

    public float getCurrentSum() {
        return currentSum;
    }

    public void setCurrentSum(float currentSum) {
        this.currentSum = currentSum;
    }

    public float getPhaseAngleCurrentToVoltagePhase1() {
        return phaseAngleCurrentToVoltagePhase1;
    }

    public void setPhaseAngleCurrentToVoltagePhase1(float phaseAngleCurrentToVoltagePhase1) {
        this.phaseAngleCurrentToVoltagePhase1 = phaseAngleCurrentToVoltagePhase1;
    }

    public float getPhaseAngleCurrentToVoltagePhase2() {
        return phaseAngleCurrentToVoltagePhase2;
    }

    public void setPhaseAngleCurrentToVoltagePhase2(float phaseAngleCurrentToVoltagePhase2) {
        this.phaseAngleCurrentToVoltagePhase2 = phaseAngleCurrentToVoltagePhase2;
    }

    public float getPhaseAngleCurrentToVoltagePhase3() {
        return phaseAngleCurrentToVoltagePhase3;
    }

    public void setPhaseAngleCurrentToVoltagePhase3(float phaseAngleCurrentToVoltagePhase3) {
        this.phaseAngleCurrentToVoltagePhase3 = phaseAngleCurrentToVoltagePhase3;
    }

    public float getPhaseAnglePhase1To3() {
        return phaseAnglePhase1To3;
    }

    public void setPhaseAnglePhase1To3(float phaseAnglePhase1To3) {
        this.phaseAnglePhase1To3 = phaseAnglePhase1To3;
    }

    public float getPhaseAnglePhase2To3() {
        return phaseAnglePhase2To3;
    }

    public void setPhaseAnglePhase2To3(float phaseAnglePhase2To3) {
        this.phaseAnglePhase2To3 = phaseAnglePhase2To3;
    }

    public float getActivePowerPhase1() {
        return activePowerPhase1;
    }

    public void setActivePowerPhase1(float activePowerPhase1) {
        this.activePowerPhase1 = activePowerPhase1;
    }

    public float getActivePowerPhase2() {
        return activePowerPhase2;
    }

    public void setActivePowerPhase2(float activePowerPhase2) {
        this.activePowerPhase2 = activePowerPhase2;
    }

    public float getActivePowerPhase3() {
        return activePowerPhase3;
    }

    public void setActivePowerPhase3(float activePowerPhase3) {
        this.activePowerPhase3 = activePowerPhase3;
    }

    public float getActivePowerSum() {
        return activePowerSum;
    }

    public void setActivePowerSum(float activePowerSum) {
        this.activePowerSum = activePowerSum;
    }

    public float getActiveFundamentalPowerPhase1() {
        return activeFundamentalPowerPhase1;
    }

    public void setActiveFundamentalPowerPhase1(float activeFundamentalPowerPhase1) {
        this.activeFundamentalPowerPhase1 = activeFundamentalPowerPhase1;
    }

    public float getPowerFactorPhase1() {
        return powerFactorPhase1;
    }

    public void setPowerFactorPhase1(float powerFactorPhase1) {
        this.powerFactorPhase1 = powerFactorPhase1;
    }

    public float getPowerFactorPhase2() {
        return powerFactorPhase2;
    }

    public void setPowerFactorPhase2(float powerFactorPhase2) {
        this.powerFactorPhase2 = powerFactorPhase2;
    }

    public float getPowerFactorPhase3() {
        return powerFactorPhase3;
    }

    public void setPowerFactorPhase3(float powerFactorPhase3) {
        this.powerFactorPhase3 = powerFactorPhase3;
    }

    public float getPowerFactorSum() {
        return powerFactorSum;
    }

    public void setPowerFactorSum(float powerFactorSum) {
        this.powerFactorSum = powerFactorSum;
    }

    public float getActiveFundamentalPowerPhase2() {
        return activeFundamentalPowerPhase2;
    }

    public void setActiveFundamentalPowerPhase2(float activeFundamentalPowerPhase2) {
        this.activeFundamentalPowerPhase2 = activeFundamentalPowerPhase2;
    }

    public float getActiveFundamentalPowerPhase3() {
        return activeFundamentalPowerPhase3;
    }

    public void setActiveFundamentalPowerPhase3(float activeFundamentalPowerPhase3) {
        this.activeFundamentalPowerPhase3 = activeFundamentalPowerPhase3;
    }

    public float getActiveFundamentalPowerSum() {
        return activeFundamentalPowerSum;
    }

    public void setActiveFundamentalPowerSum(float activeFundamentalPowerSum) {
        this.activeFundamentalPowerSum = activeFundamentalPowerSum;
    }

    public float getActiveHarmonicPowerPhase1() {
        return activeHarmonicPowerPhase1;
    }

    public void setActiveHarmonicPowerPhase1(float activeHarmonicPowerPhase1) {
        this.activeHarmonicPowerPhase1 = activeHarmonicPowerPhase1;
    }

    public float getActiveHarmonicPowerPhase2() {
        return activeHarmonicPowerPhase2;
    }

    public void setActiveHarmonicPowerPhase2(float activeHarmonicPowerPhase2) {
        this.activeHarmonicPowerPhase2 = activeHarmonicPowerPhase2;
    }

    public float getActiveHarmonicPowerPhase3() {
        return activeHarmonicPowerPhase3;
    }

    public void setActiveHarmonicPowerPhase3(float activeHarmonicPowerPhase3) {
        this.activeHarmonicPowerPhase3 = activeHarmonicPowerPhase3;
    }

    public float getActiveHarmonicPowerSum() {
        return activeHarmonicPowerSum;
    }

    public void setActiveHarmonicPowerSum(float activeHarmonicPowerSum) {
        this.activeHarmonicPowerSum = activeHarmonicPowerSum;
    }

    public float getReactivePowerPhase1() {
        return reactivePowerPhase1;
    }

    public void setReactivePowerPhase1(float reactivePowerPhase1) {
        this.reactivePowerPhase1 = reactivePowerPhase1;
    }

    public float getReactivePowerPhase2() {
        return reactivePowerPhase2;
    }

    public void setReactivePowerPhase2(float reactivePowerPhase2) {
        this.reactivePowerPhase2 = reactivePowerPhase2;
    }

    public float getReactivePowerPhase3() {
        return reactivePowerPhase3;
    }

    public void setReactivePowerPhase3(float reactivePowerPhase3) {
        this.reactivePowerPhase3 = reactivePowerPhase3;
    }

    public float getReactivePowerSum() {
        return reactivePowerSum;
    }

    public void setReactivePowerSum(float reactivePowerSum) {
        this.reactivePowerSum = reactivePowerSum;
    }

    public float getApparentPowerPhase1() {
        return apparentPowerPhase1;
    }

    public void setApparentPowerPhase1(float apparentPowerPhase1) {
        this.apparentPowerPhase1 = apparentPowerPhase1;
    }

    public float getApparentPowerPhase2() {
        return apparentPowerPhase2;
    }

    public void setApparentPowerPhase2(float apparentPowerPhase2) {
        this.apparentPowerPhase2 = apparentPowerPhase2;
    }

    public float getApparentPowerPhase3() {
        return apparentPowerPhase3;
    }

    public void setApparentPowerPhase3(float apparentPowerPhase3) {
        this.apparentPowerPhase3 = apparentPowerPhase3;
    }

    public float getApparentPowerSum() {
        return apparentPowerSum;
    }

    public void setApparentPowerSum(float apparentPowerSum) {
        this.apparentPowerSum = apparentPowerSum;
    }

    public float getForwardActiveEnergyPhase1() {
        return forwardActiveEnergyPhase1 / KILO;
    }

    public void setForwardActiveEnergyPhase1(float forwardActiveEnergyPhase1) {
        this.forwardActiveEnergyPhase1 = forwardActiveEnergyPhase1;
    }

    public float getForwardActiveEnergyPhase2() {
        return forwardActiveEnergyPhase2 / KILO;
    }

    public void setForwardActiveEnergyPhase2(float forwardActiveEnergyPhase2) {
        this.forwardActiveEnergyPhase2 = forwardActiveEnergyPhase2;
    }

    public float getForwardActiveEnergyPhase3() {
        return forwardActiveEnergyPhase3 / KILO;
    }

    public void setForwardActiveEnergyPhase3(float forwardActiveEnergyPhase3) {
        this.forwardActiveEnergyPhase3 = forwardActiveEnergyPhase3;
    }

    public float getForwardActiveEnergySum() {
        return forwardActiveEnergySum / KILO;
    }

    public void setForwardActiveEnergySum(float forwardActiveEnergySum) {
        this.forwardActiveEnergySum = forwardActiveEnergySum;
    }

    public float getForwardActiveFundamentalEnergyPhase1() {
        return forwardActiveFundamentalEnergyPhase1 / KILO;
    }

    public void setForwardActiveFundamentalEnergyPhase1(float forwardActiveFundamentalEnergyPhase1) {
        this.forwardActiveFundamentalEnergyPhase1 = forwardActiveFundamentalEnergyPhase1;
    }

    public float getForwardActiveFundamentalEnergyPhase2() {
        return forwardActiveFundamentalEnergyPhase2 / KILO;
    }

    public void setForwardActiveFundamentalEnergyPhase2(float forwardActiveFundamentalEnergyPhase2) {
        this.forwardActiveFundamentalEnergyPhase2 = forwardActiveFundamentalEnergyPhase2;
    }

    public float getForwardActiveFundamentalEnergyPhase3() {
        return forwardActiveFundamentalEnergyPhase3 / KILO;
    }

    public void setForwardActiveFundamentalEnergyPhase3(float forwardActiveFundamentalEnergyPhase3) {
        this.forwardActiveFundamentalEnergyPhase3 = forwardActiveFundamentalEnergyPhase3;
    }

    public float getForwardActiveFundamentalEnergySum() {
        return forwardActiveFundamentalEnergySum / KILO;
    }

    public void setForwardActiveFundamentalEnergySum(float forwardActiveFundamentalEnergySum) {
        this.forwardActiveFundamentalEnergySum = forwardActiveFundamentalEnergySum;
    }

    public float getForwardActiveHarmonicEnergyPhase1() {
        return forwardActiveHarmonicEnergyPhase1 / KILO;
    }

    public void setForwardActiveHarmonicEnergyPhase1(float forwardActiveHarmonicEnergyPhase1) {
        this.forwardActiveHarmonicEnergyPhase1 = forwardActiveHarmonicEnergyPhase1;
    }

    public float getForwardActiveHarmonicEnergyPhase2() {
        return forwardActiveHarmonicEnergyPhase2 / KILO;
    }

    public void setForwardActiveHarmonicEnergyPhase2(float forwardActiveHarmonicEnergyPhase2) {
        this.forwardActiveHarmonicEnergyPhase2 = forwardActiveHarmonicEnergyPhase2;
    }

    public float getForwardActiveHarmonicEnergyPhase3() {
        return forwardActiveHarmonicEnergyPhase3 / KILO;
    }

    public void setForwardActiveHarmonicEnergyPhase3(float forwardActiveHarmonicEnergyPhase3) {
        this.forwardActiveHarmonicEnergyPhase3 = forwardActiveHarmonicEnergyPhase3;
    }

    public float getForwardActiveHarmonicEnergySum() {
        return forwardActiveHarmonicEnergySum / KILO;
    }

    public void setForwardActiveHarmonicEnergySum(float forwardActiveHarmonicEnergySum) {
        this.forwardActiveHarmonicEnergySum = forwardActiveHarmonicEnergySum;
    }

    public float getForwardReactiveEnergyPhase1() {
        return forwardReactiveEnergyPhase1;
    }

    public void setForwardReactiveEnergyPhase1(float forwardReactiveEnergyPhase1) {
        this.forwardReactiveEnergyPhase1 = forwardReactiveEnergyPhase1;
    }

    public float getForwardReactiveEnergyPhase2() {
        return forwardReactiveEnergyPhase2;
    }

    public void setForwardReactiveEnergyPhase2(float forwardReactiveEnergyPhase2) {
        this.forwardReactiveEnergyPhase2 = forwardReactiveEnergyPhase2;
    }

    public float getForwardReactiveEnergyPhase3() {
        return forwardReactiveEnergyPhase3;
    }

    public void setForwardReactiveEnergyPhase3(float forwardReactiveEnergyPhase3) {
        this.forwardReactiveEnergyPhase3 = forwardReactiveEnergyPhase3;
    }

    public float getForwardReactiveEnergySum() {
        return forwardReactiveEnergySum;
    }

    public void setForwardReactiveEnergySum(float forwardReactiveEnergySum) {
        this.forwardReactiveEnergySum = forwardReactiveEnergySum;
    }

    public float getReverseActiveEnergyPhase1() {
        return reverseActiveEnergyPhase1 / KILO;
    }

    public void setReverseActiveEnergyPhase1(float reverseActiveEnergyPhase1) {
        this.reverseActiveEnergyPhase1 = reverseActiveEnergyPhase1;
    }

    public float getReverseActiveEnergyPhase2() {
        return reverseActiveEnergyPhase2 / KILO;
    }

    public void setReverseActiveEnergyPhase2(float reverseActiveEnergyPhase2) {
        this.reverseActiveEnergyPhase2 = reverseActiveEnergyPhase2;
    }

    public float getReverseActiveEnergyPhase3() {
        return reverseActiveEnergyPhase3 / KILO;
    }

    public void setReverseActiveEnergyPhase3(float reverseActiveEnergyPhase3) {
        this.reverseActiveEnergyPhase3 = reverseActiveEnergyPhase3;
    }

    public float getReverseActiveEnergySum() {
        return reverseActiveEnergySum / KILO;
    }

    public void setReverseActiveEnergySum(float reverseActiveEnergySum) {
        this.reverseActiveEnergySum = reverseActiveEnergySum;
    }

    public float getReverseActiveFundamentalEnergyPhase1() {
        return reverseActiveFundamentalEnergyPhase1 / KILO;
    }

    public void setReverseActiveFundamentalEnergyPhase1(float reverseActiveFundamentalEnergyPhase1) {
        this.reverseActiveFundamentalEnergyPhase1 = reverseActiveFundamentalEnergyPhase1;
    }

    public float getReverseActiveFundamentalEnergyPhase2() {
        return reverseActiveFundamentalEnergyPhase2 / KILO;
    }

    public void setReverseActiveFundamentalEnergyPhase2(float reverseActiveFundamentalEnergyPhase2) {
        this.reverseActiveFundamentalEnergyPhase2 = reverseActiveFundamentalEnergyPhase2;
    }

    public float getReverseActiveFundamentalEnergyPhase3() {
        return reverseActiveFundamentalEnergyPhase3 / KILO;
    }

    public void setReverseActiveFundamentalEnergyPhase3(float reverseActiveFundamentalEnergyPhase3) {
        this.reverseActiveFundamentalEnergyPhase3 = reverseActiveFundamentalEnergyPhase3;
    }

    public float getReverseActiveFundamentalEnergySum() {
        return reverseActiveFundamentalEnergySum / KILO;
    }

    public void setReverseActiveFundamentalEnergySum(float reverseActiveFundamentalEnergySum) {
        this.reverseActiveFundamentalEnergySum = reverseActiveFundamentalEnergySum;
    }

    public float getReverseActiveHarmonicEnergyPhase1() {
        return reverseActiveHarmonicEnergyPhase1 / KILO;
    }

    public void setReverseActiveHarmonicEnergyPhase1(float reverseActiveHarmonicEnergyPhase1) {
        this.reverseActiveHarmonicEnergyPhase1 = reverseActiveHarmonicEnergyPhase1;
    }

    public float getReverseActiveHarmonicEnergyPhase2() {
        return reverseActiveHarmonicEnergyPhase2 / KILO;
    }

    public void setReverseActiveHarmonicEnergyPhase2(float reverseActiveHarmonicEnergyPhase2) {
        this.reverseActiveHarmonicEnergyPhase2 = reverseActiveHarmonicEnergyPhase2;
    }

    public float getReverseActiveHarmonicEnergyPhase3() {
        return reverseActiveHarmonicEnergyPhase3 / KILO;
    }

    public void setReverseActiveHarmonicEnergyPhase3(float reverseActiveHarmonicEnergyPhase3) {
        this.reverseActiveHarmonicEnergyPhase3 = reverseActiveHarmonicEnergyPhase3;
    }

    public float getReverseActiveHarmonicEnergySum() {
        return reverseActiveHarmonicEnergySum / KILO;
    }

    public void setReverseActiveHarmonicEnergySum(float reverseActiveHarmonicEnergySum) {
        this.reverseActiveHarmonicEnergySum = reverseActiveHarmonicEnergySum;
    }

    public float getReverseReactiveEnergyPhase1() {
        return reverseReactiveEnergyPhase1;
    }

    public void setReverseReactiveEnergyPhase1(float reverseReactiveEnergyPhase1) {
        this.reverseReactiveEnergyPhase1 = reverseReactiveEnergyPhase1;
    }

    public float getReverseReactiveEnergyPhase2() {
        return reverseReactiveEnergyPhase2;
    }

    public void setReverseReactiveEnergyPhase2(float reverseReactiveEnergyPhase2) {
        this.reverseReactiveEnergyPhase2 = reverseReactiveEnergyPhase2;
    }

    public float getReverseReactiveEnergyPhase3() {
        return reverseReactiveEnergyPhase3;
    }

    public void setReverseReactiveEnergyPhase3(float reverseReactiveEnergyPhase3) {
        this.reverseReactiveEnergyPhase3 = reverseReactiveEnergyPhase3;
    }

    public float getReverseReactiveEnergySum() {
        return reverseReactiveEnergySum;
    }

    public void setReverseReactiveEnergySum(float reverseReactiveEnergySum) {
        this.reverseReactiveEnergySum = reverseReactiveEnergySum;
    }

    public float getApparentEnergyConsumptionPhase1() {
        return apparentEnergyConsumptionPhase1;
    }

    public void setApparentEnergyConsumptionPhase1(float apparentEnergyConsumptionPhase1) {
        this.apparentEnergyConsumptionPhase1 = apparentEnergyConsumptionPhase1;
    }

    public float getApparentEnergyConsumptionPhase2() {
        return apparentEnergyConsumptionPhase2;
    }

    public void setApparentEnergyConsumptionPhase2(float apparentEnergyConsumptionPhase2) {
        this.apparentEnergyConsumptionPhase2 = apparentEnergyConsumptionPhase2;
    }

    public float getApparentEnergyConsumptionPhase3() {
        return apparentEnergyConsumptionPhase3;
    }

    public void setApparentEnergyConsumptionPhase3(float apparentEnergyConsumptionPhase3) {
        this.apparentEnergyConsumptionPhase3 = apparentEnergyConsumptionPhase3;
    }

    public float getApparentEnergyConsumptionSum() {
        return apparentEnergyConsumptionSum;
    }

    public void setApparentEnergyConsumptionSum(float apparentEnergyConsumptionSum) {
        this.apparentEnergyConsumptionSum = apparentEnergyConsumptionSum;
    }
}
