/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.sunspec.internal.block;

/**
 * Model for SunSpec compatible inverter data
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
public class InverterModelBlock extends AbstractSunSpecMessageBlock {

    /**
     * Type of inverter (single phase, split phase, three phase)
     */
    private int phaseConfiguration;

    /**
     * Length of the block in 16bit words
     */
    private int length;

    /**
     * AC Total Current value
     */
    private Integer acCurrentTotal;

    /**
     * AC Phase A Current value
     */
    private Integer acCurrentPhaseA;

    /**
     * AC Phase B Current value
     */
    private Integer acCurrentPhaseB;

    /**
     * AC Phase C Current value
     */
    private Integer acCurrentPhaseC;

    /**
     * AC Current scale factor
     */
    private Short acCurrentSF;

    /**
     * AC Voltage Phase AB value
     */
    private Integer acVoltageAB;

    /**
     * AC Voltage Phase BC value
     */
    private Integer acVoltageBC;

    /**
     * AC Voltage Phase CA value
     */
    private Integer acVoltageCA;

    /**
     * AC Voltage Phase A to N value
     */
    private Integer acVoltageAtoN;

    /**
     * AC Voltage Phase B to N value
     */
    private Integer acVoltageBtoN;

    /**
     * AC Voltage Phase C to N value
     */
    private Integer acVoltageCtoN;

    /**
     * AC Voltage scale factor
     */
    private Short acVoltageSF;

    /**
     * AC Power value
     */
    private Short acPower;

    /**
     * AC Power scale factor
     */
    private Short acPowerSF;

    /**
     * AC Frequency value
     */
    private Integer acFrequency;

    /**
     * AC Frequency scale factor
     */
    private Short acFrequencySF;

    /**
     * Apparent power
     */
    private Short acApparentPower;

    /**
     * Apparent power scale factor
     */
    private Short acApparentPowerSF;

    /**
     * Reactive power
     */
    private Short acReactivePower;

    /**
     * Reactive power scale factor
     */
    private Short acReactivePowerSF;

    /**
     * Power factor
     */
    private Short acPowerFactor;

    /**
     * Power factor scale factor
     */
    private Short acPowerFactorSF;

    /**
     * AC Lifetime Energy production
     */
    private Long acEnergyLifetime;

    /**
     * AC Lifetime Energy scale factor
     */
    private Short acEnergyLifetimeSF;

    /**
     * DC Current value
     */
    private Integer dcCurrent;

    /**
     * DC Current scale factor
     */
    private Short dcCurrentSF;

    /**
     * DC Voltage value
     */
    private Integer dcVoltage;

    /**
     * DC Voltage scale factor
     */
    private Short dcVoltageSF;

    /**
     * DC Power value
     */
    private Short dcPower;

    /**
     * DC Power scale factor
     */
    private Short dcPowerSF;

    /**
     * Cabinet temperature
     */
    private Short temperatureCabinet;

    /**
     * Heat sink temperature
     */
    private Short temperatureHeatsink;

    /**
     * Transformer temperature
     */
    private Short temperatureTransformer;

    /**
     * Other temperature
     */
    private Short temperatureOther;

    /**
     * Heat sink temperature scale factor
     */
    private Short temperatureSF;

    /**
     * Current operating state
     */
    private Integer status;

    /**
     * Vendor defined operating state or error code
     */
    private Integer statusVendor;

    public int getPhaseConfiguration() {
        return phaseConfiguration;
    }

    public void setPhaseConfiguration(int phaseConfiguration) {
        this.phaseConfiguration = phaseConfiguration;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Integer getAcCurrentTotal() {
        return acCurrentTotal;
    }

    public void setAcCurrentTotal(Integer acCurrentTotal) {
        this.acCurrentTotal = acCurrentTotal;
    }

    public Integer getAcCurrentPhaseA() {
        return acCurrentPhaseA;
    }

    public void setAcCurrentPhaseA(Integer acCurrentPhaseA) {
        this.acCurrentPhaseA = acCurrentPhaseA;
    }

    public Integer getAcCurrentPhaseB() {
        return acCurrentPhaseB;
    }

    public void setAcCurrentPhaseB(Integer acCurrentPhaseB) {
        this.acCurrentPhaseB = acCurrentPhaseB;
    }

    public Integer getAcCurrentPhaseC() {
        return acCurrentPhaseC;
    }

    public void setAcCurrentPhaseC(Integer acCurrentPhaseC) {
        this.acCurrentPhaseC = acCurrentPhaseC;
    }

    public Short getAcCurrentSF() {
        return acCurrentSF;
    }

    public void setAcCurrentSF(Short acCurrentSF) {
        this.acCurrentSF = acCurrentSF;
    }

    public Integer getAcVoltageAB() {
        return acVoltageAB;
    }

    public void setAcVoltageAB(Integer acVoltageAB) {
        this.acVoltageAB = acVoltageAB;
    }

    public Integer getAcVoltageBC() {
        return acVoltageBC;
    }

    public void setAcVoltageBC(Integer acVoltageBC) {
        this.acVoltageBC = acVoltageBC;
    }

    public Integer getAcVoltageCA() {
        return acVoltageCA;
    }

    public void setAcVoltageCA(Integer acVoltageCA) {
        this.acVoltageCA = acVoltageCA;
    }

    public Integer getAcVoltageAtoN() {
        return acVoltageAtoN;
    }

    public void setAcVoltageAtoN(Integer acVoltageAtoN) {
        this.acVoltageAtoN = acVoltageAtoN;
    }

    public Integer getAcVoltageBtoN() {
        return acVoltageBtoN;
    }

    public void setAcVoltageBtoN(Integer acVoltageBtoN) {
        this.acVoltageBtoN = acVoltageBtoN;
    }

    public Integer getAcVoltageCtoN() {
        return acVoltageCtoN;
    }

    public void setAcVoltageCtoN(Integer acVoltageCtoN) {
        this.acVoltageCtoN = acVoltageCtoN;
    }

    public Short getAcVoltageSF() {
        return acVoltageSF;
    }

    public void setAcVoltageSF(Short acVoltageSF) {
        this.acVoltageSF = acVoltageSF;
    }

    public Short getAcPower() {
        return acPower;
    }

    public void setAcPower(Short acPower) {
        this.acPower = acPower;
    }

    public Short getAcPowerSF() {
        return acPowerSF;
    }

    public void setAcPowerSF(Short acPowerSF) {
        this.acPowerSF = acPowerSF;
    }

    public Integer getAcFrequency() {
        return acFrequency;
    }

    public void setAcFrequency(Integer acFrequency) {
        this.acFrequency = acFrequency;
    }

    public Short getAcFrequencySF() {
        return acFrequencySF;
    }

    public void setAcFrequencySF(Short acFrequencySF) {
        this.acFrequencySF = acFrequencySF;
    }

    public Short getAcApparentPower() {
        return acApparentPower;
    }

    public void setAcApparentPower(Short acApparentPower) {
        this.acApparentPower = acApparentPower;
    }

    public Short getAcApparentPowerSF() {
        return acApparentPowerSF;
    }

    public void setAcApparentPowerSF(Short acApparentPowerSF) {
        this.acApparentPowerSF = acApparentPowerSF;
    }

    public Short getAcReactivePower() {
        return acReactivePower;
    }

    public void setAcReactivePower(Short acReactivePower) {
        this.acReactivePower = acReactivePower;
    }

    public Short getAcReactivePowerSF() {
        return acReactivePowerSF;
    }

    public void setAcReactivePowerSF(Short acReactivePowerSF) {
        this.acReactivePowerSF = acReactivePowerSF;
    }

    public Short getAcPowerFactor() {
        return acPowerFactor;
    }

    public void setAcPowerFactor(Short powerFactor) {
        this.acPowerFactor = powerFactor;
    }

    public Short getAcPowerFactorSF() {
        return acPowerFactorSF;
    }

    public void setAcPowerFactorSF(Short powerFactorSF) {
        this.acPowerFactorSF = powerFactorSF;
    }

    public Long getAcEnergyLifetime() {
        return acEnergyLifetime;
    }

    public void setAcEnergyLifetime(Long acEnergyLifetime) {
        this.acEnergyLifetime = acEnergyLifetime;
    }

    public Short getAcEnergyLifetimeSF() {
        return acEnergyLifetimeSF;
    }

    public void setAcEnergyLifetimeSF(Short acEnergyLifetimeSF) {
        this.acEnergyLifetimeSF = acEnergyLifetimeSF;
    }

    public Integer getDcCurrent() {
        return dcCurrent;
    }

    public void setDcCurrent(Integer dcCurrent) {
        this.dcCurrent = dcCurrent;
    }

    public Short getDcCurrentSF() {
        return dcCurrentSF;
    }

    public void setDcCurrentSF(Short dcCurrentSF) {
        this.dcCurrentSF = dcCurrentSF;
    }

    public Integer getDcVoltage() {
        return dcVoltage;
    }

    public void setDcVoltage(Integer dcVoltage) {
        this.dcVoltage = dcVoltage;
    }

    public Short getDcVoltageSF() {
        return dcVoltageSF;
    }

    public void setDcVoltageSF(Short dcVoltageSF) {
        this.dcVoltageSF = dcVoltageSF;
    }

    public Short getDcPower() {
        return dcPower;
    }

    public void setDcPower(Short dcPower) {
        this.dcPower = dcPower;
    }

    public Short getDcPowerSF() {
        return dcPowerSF;
    }

    public void setDcPowerSF(Short dcPowerSF) {
        this.dcPowerSF = dcPowerSF;
    }

    public Short getTemperatureCabinet() {
        return temperatureCabinet;
    }

    public void setTemperatureCabinet(Short temperature) {
        this.temperatureCabinet = temperature;
    }

    public Short getTemperatureHeatsink() {
        return temperatureHeatsink;
    }

    public void setTemperatureHeatsink(Short temperature) {
        this.temperatureHeatsink = temperature;
    }

    public Short getTemperatureTransformer() {
        return temperatureTransformer;
    }

    public void setTemperatureTransformer(Short temperature) {
        this.temperatureTransformer = temperature;
    }

    public Short getTemperatureOther() {
        return temperatureOther;
    }

    public void setTemperatureOther(Short temperature) {
        this.temperatureOther = temperature;
    }

    public Short getTemperatureSF() {
        return temperatureSF;
    }

    public void setTemperatureSF(Short temperatureSF) {
        this.temperatureSF = temperatureSF;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatusVendor() {
        return statusVendor;
    }

    public void setStatusVendor(Integer statusVendor) {
        this.statusVendor = statusVendor;
    }

}
