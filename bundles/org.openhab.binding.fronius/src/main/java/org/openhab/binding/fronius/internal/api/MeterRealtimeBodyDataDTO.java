/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MeterRealtimeBodyData} is responsible for storing
 * the "data" node of the JSON response
 * 
 * The main SerializedName values use Smart Meter 63A names
 * The first SerializedName alternate names use Smart Meter 65A names
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class MeterRealtimeBodyDataDTO {
    @SerializedName(value = "Current_AC_Phase_1", alternate = { "ACBRIDGE_CURRENT_ACTIVE_MEAN_01_F32" })
    private double currentACPhase1;
    @SerializedName(value = "Current_AC_Phase_2", alternate = { "ACBRIDGE_CURRENT_ACTIVE_MEAN_02_F32" })
    private double currentACPhase2;
    @SerializedName(value = "Current_AC_Phase_3", alternate = { "ACBRIDGE_CURRENT_ACTIVE_MEAN_03_F32" })
    private double currentACPhase3;
    @SerializedName("Details")
    private MeterRealtimeDetailsDTO details;
    @SerializedName(value = "Enable", alternate = { "COMPONENTS_MODE_ENABLE_U16" })
    private int enable;
    @SerializedName(value = "EnergyReactive_VArAC_Sum_Consumed", alternate = {
            "SMARTMETER_ENERGYREACTIVE_CONSUMED_SUM_F64" })
    private double energyReactiveVArACSumConsumed;
    @SerializedName(value = "EnergyReactive_VArAC_Sum_Produced", alternate = {
            "SMARTMETER_ENERGYREACTIVE_PRODUCED_SUM_F64" })
    private double energyReactiveVArACSumProduced;
    @SerializedName(value = "EnergyReal_WAC_Minus_Absolute", alternate = {
            "SMARTMETER_ENERGYACTIVE_ABSOLUT_MINUS_F64" })
    private double energyRealWACMinusAbsolute;
    @SerializedName(value = "EnergyReal_WAC_Plus_Absolute", alternate = { "SMARTMETER_ENERGYACTIVE_ABSOLUT_PLUS_F64" })
    private double energyRealWACPlusAbsolute;
    @SerializedName(value = "EnergyReal_WAC_Sum_Consumed", alternate = { "SMARTMETER_ENERGYACTIVE_CONSUMED_SUM_F64" })
    private double energyRealWACSumConsumed;
    @SerializedName(value = "EnergyReal_WAC_Sum_Produced", alternate = { "SMARTMETER_ENERGYACTIVE_PRODUCED_SUM_F64" })
    private double energyRealWACSumProduced;
    @SerializedName(value = "Frequency_Phase_Average", alternate = { "GRID_FREQUENCY_MEAN_F32" })
    private double frequencyPhaseAverage;
    @SerializedName(value = "Meter_Location_Current", alternate = { "SMARTMETER_VALUE_LOCATION_U16" })
    private int meterLocationCurrent;
    @SerializedName(value = "PowerApparent_S_Phase_1", alternate = { "SMARTMETER_POWERAPPARENT_01_F64" })
    private double powerApparentSPhase1;
    @SerializedName(value = "PowerApparent_S_Phase_2", alternate = { "SMARTMETER_POWERAPPARENT_02_F64" })
    private double powerApparentSPhase2;
    @SerializedName(value = "PowerApparent_S_Phase_3", alternate = { "SMARTMETER_POWERAPPARENT_03_F64" })
    private double powerApparentSPhase3;
    @SerializedName(value = "PowerApparent_S_Sum", alternate = { "SMARTMETER_POWERAPPARENT_MEAN_SUM_F64" })
    private double powerApparentSSum;
    @SerializedName(value = "PowerFactor_Phase_1", alternate = { "SMARTMETER_FACTOR_POWER_01_F64" })
    private double powerFactorPhase1;
    @SerializedName(value = "PowerFactor_Phase_2", alternate = { "SMARTMETER_FACTOR_POWER_02_F64" })
    private double powerFactorPhase2;
    @SerializedName(value = "PowerFactor_Phase_3", alternate = { "SMARTMETER_FACTOR_POWER_03_F64" })
    private double powerFactorPhase3;
    @SerializedName(value = "PowerFactor_Sum", alternate = { "SMARTMETER_FACTOR_POWER_SUM_F64" })
    private double powerFactorSum;
    @SerializedName(value = "PowerReactive_Q_Phase_1", alternate = { "SMARTMETER_POWERREACTIVE_01_F64" })
    private double powerReactiveQPhase1;
    @SerializedName(value = "PowerReactive_Q_Phase_2", alternate = { "SMARTMETER_POWERREACTIVE_02_F64" })
    private double powerReactiveQPhase2;
    @SerializedName(value = "PowerReactive_Q_Phase_3", alternate = { "SMARTMETER_POWERREACTIVE_03_F64" })
    private double powerReactiveQPhase3;
    @SerializedName(value = "PowerReactive_Q_Sum", alternate = { "SMARTMETER_POWERREACTIVE_MEAN_SUM_F64" })
    private double powerReactiveQSum;
    @SerializedName(value = "PowerReal_P_Phase_1", alternate = { "SMARTMETER_POWERACTIVE_01_F64" })
    private double powerRealPPhase1;
    @SerializedName(value = "PowerReal_P_Phase_2", alternate = { "SMARTMETER_POWERACTIVE_02_F64" })
    private double powerRealPPhase2;
    @SerializedName(value = "PowerReal_P_Phase_3", alternate = { "SMARTMETER_POWERACTIVE_03_F64" })
    private double powerRealPPhase3;
    @SerializedName(value = "PowerReal_P_Sum", alternate = { "SMARTMETER_POWERACTIVE_MEAN_SUM_F64" })
    private double powerRealPSum;
    @SerializedName("TimeStamp")
    private int timeStamp;
    @SerializedName(value = "Visible", alternate = { "COMPONENTS_MODE_VISIBLE_U16" })
    private int visible;
    @SerializedName(value = "Voltage_AC_PhaseToPhase_12", alternate = { "ACBRIDGE_VOLTAGE_MEAN_12_F32" })
    private double voltageACPhaseToPhase12;
    @SerializedName(value = "Voltage_AC_PhaseToPhase_23", alternate = { "ACBRIDGE_VOLTAGE_MEAN_23_F32" })
    private double voltageACPhaseToPhase23;
    @SerializedName(value = "Voltage_AC_PhaseToPhase_31", alternate = { "ACBRIDGE_VOLTAGE_MEAN_31_F32" })
    private double voltageACPhaseToPhase31;
    @SerializedName(value = "Voltage_AC_Phase_1", alternate = { "SMARTMETER_VOLTAGE_01_F64" })
    private double voltageACPhase1;
    @SerializedName(value = "Voltage_AC_Phase_2", alternate = { "SMARTMETER_VOLTAGE_02_F64" })
    private double voltageACPhase2;
    @SerializedName(value = "Voltage_AC_Phase_3", alternate = { "SMARTMETER_VOLTAGE_03_F64" })
    private double voltageACPhase3;

    public double getCurrentACPhase1() {
        return currentACPhase1;
    }

    public void setCurrentACPhase1(double currentACPhase1) {
        this.currentACPhase1 = currentACPhase1;
    }

    public double getCurrentACPhase2() {
        return currentACPhase2;
    }

    public void setCurrentACPhase2(double currentACPhase2) {
        this.currentACPhase2 = currentACPhase2;
    }

    public double getCurrentACPhase3() {
        return currentACPhase3;
    }

    public void setCurrentACPhase3(double currentACPhase3) {
        this.currentACPhase3 = currentACPhase3;
    }

    public MeterRealtimeDetailsDTO getDetails() {
        if (details == null) {
            details = new MeterRealtimeDetailsDTO();
        }
        return details;
    }

    public void setDetails(MeterRealtimeDetailsDTO details) {
        this.details = details;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public double getEnergyReactiveVArACSumConsumed() {
        return energyReactiveVArACSumConsumed;
    }

    public void setEnergyReactiveVArACSumConsumed(double energyReactiveVArACSumConsumed) {
        this.energyReactiveVArACSumConsumed = energyReactiveVArACSumConsumed;
    }

    public double getEnergyReactiveVArACSumProduced() {
        return energyReactiveVArACSumProduced;
    }

    public void setEnergyReactiveVArACSumProduced(double energyReactiveVArACSumProduced) {
        this.energyReactiveVArACSumProduced = energyReactiveVArACSumProduced;
    }

    public double getEnergyRealWACMinusAbsolute() {
        return energyRealWACMinusAbsolute;
    }

    public void setEnergyRealWACMinusAbsolute(double energyRealWACMinusAbsolute) {
        this.energyRealWACMinusAbsolute = energyRealWACMinusAbsolute;
    }

    public double getEnergyRealWACPlusAbsolute() {
        return energyRealWACPlusAbsolute;
    }

    public void setEnergyRealWACPlusAbsolute(double energyRealWACPlusAbsolute) {
        this.energyRealWACPlusAbsolute = energyRealWACPlusAbsolute;
    }

    public double getEnergyRealWACSumConsumed() {
        return energyRealWACSumConsumed;
    }

    public void setEnergyRealWACSumConsumed(double energyRealWACSumConsumed) {
        this.energyRealWACSumConsumed = energyRealWACSumConsumed;
    }

    public double getEnergyRealWACSumProduced() {
        return energyRealWACSumProduced;
    }

    public void setEnergyRealWACSumProduced(double energyRealWACSumProduced) {
        this.energyRealWACSumProduced = energyRealWACSumProduced;
    }

    public double getFrequencyPhaseAverage() {
        return frequencyPhaseAverage;
    }

    public void setFrequencyPhaseAverage(double frequencyPhaseAverage) {
        this.frequencyPhaseAverage = frequencyPhaseAverage;
    }

    public int getMeterLocationCurrent() {
        return meterLocationCurrent;
    }

    public void setMeterLocationCurrent(int meterLocationCurrent) {
        this.meterLocationCurrent = meterLocationCurrent;
    }

    public double getPowerApparentSPhase1() {
        return powerApparentSPhase1;
    }

    public void setPowerApparentSPhase1(double powerApparentSPhase1) {
        this.powerApparentSPhase1 = powerApparentSPhase1;
    }

    public double getPowerApparentSPhase2() {
        return powerApparentSPhase2;
    }

    public void setPowerApparentSPhase2(double powerApparentSPhase2) {
        this.powerApparentSPhase2 = powerApparentSPhase2;
    }

    public double getPowerApparentSPhase3() {
        return powerApparentSPhase3;
    }

    public void setPowerApparentSPhase3(double powerApparentSPhase3) {
        this.powerApparentSPhase3 = powerApparentSPhase3;
    }

    public double getPowerApparentSSum() {
        return powerApparentSSum;
    }

    public void setPowerApparentSSum(double powerApparentSSum) {
        this.powerApparentSSum = powerApparentSSum;
    }

    public double getPowerFactorPhase1() {
        return powerFactorPhase1;
    }

    public void setPowerFactorPhase1(double powerFactorPhase1) {
        this.powerFactorPhase1 = powerFactorPhase1;
    }

    public double getPowerFactorPhase2() {
        return powerFactorPhase2;
    }

    public void setPowerFactorPhase2(double powerFactorPhase2) {
        this.powerFactorPhase2 = powerFactorPhase2;
    }

    public double getPowerFactorPhase3() {
        return powerFactorPhase3;
    }

    public void setPowerFactorPhase3(double powerFactorPhase3) {
        this.powerFactorPhase3 = powerFactorPhase3;
    }

    public double getPowerFactorSum() {
        return powerFactorSum;
    }

    public void setPowerFactorSum(double powerFactorSum) {
        this.powerFactorSum = powerFactorSum;
    }

    public double getPowerReactiveQPhase1() {
        return powerReactiveQPhase1;
    }

    public void setPowerReactiveQPhase1(double powerReactiveQPhase1) {
        this.powerReactiveQPhase1 = powerReactiveQPhase1;
    }

    public double getPowerReactiveQPhase2() {
        return powerReactiveQPhase2;
    }

    public void setPowerReactiveQPhase2(double powerReactiveQPhase2) {
        this.powerReactiveQPhase2 = powerReactiveQPhase2;
    }

    public double getPowerReactiveQPhase3() {
        return powerReactiveQPhase3;
    }

    public void setPowerReactiveQPhase3(double powerReactiveQPhase3) {
        this.powerReactiveQPhase3 = powerReactiveQPhase3;
    }

    public double getPowerReactiveQSum() {
        return powerReactiveQSum;
    }

    public void setPowerReactiveQSum(double powerReactiveQSum) {
        this.powerReactiveQSum = powerReactiveQSum;
    }

    public double getPowerRealPPhase1() {
        return powerRealPPhase1;
    }

    public void setPowerRealPPhase1(double powerRealPPhase1) {
        this.powerRealPPhase1 = powerRealPPhase1;
    }

    public double getPowerRealPPhase2() {
        return powerRealPPhase2;
    }

    public void setPowerRealPPhase2(double powerRealPPhase2) {
        this.powerRealPPhase2 = powerRealPPhase2;
    }

    public double getPowerRealPPhase3() {
        return powerRealPPhase3;
    }

    public void setPowerRealPPhase3(double powerRealPPhase3) {
        this.powerRealPPhase3 = powerRealPPhase3;
    }

    public double getPowerRealPSum() {
        return powerRealPSum;
    }

    public void setPowerRealPSum(double powerRealPSum) {
        this.powerRealPSum = powerRealPSum;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public double getVoltageACPhaseToPhase12() {
        return voltageACPhaseToPhase12;
    }

    public void setVoltageACPhaseToPhase12(double voltageACPhaseToPhase12) {
        this.voltageACPhaseToPhase12 = voltageACPhaseToPhase12;
    }

    public double getVoltageACPhaseToPhase23() {
        return voltageACPhaseToPhase23;
    }

    public void setVoltageACPhaseToPhase23(double voltageACPhaseToPhase23) {
        this.voltageACPhaseToPhase23 = voltageACPhaseToPhase23;
    }

    public double getVoltageACPhaseToPhase31() {
        return voltageACPhaseToPhase31;
    }

    public void setVoltageACPhaseToPhase31(double voltageACPhaseToPhase31) {
        this.voltageACPhaseToPhase31 = voltageACPhaseToPhase31;
    }

    public double getVoltageACPhase1() {
        return voltageACPhase1;
    }

    public void setVoltageACPhase1(double voltageACPhase1) {
        this.voltageACPhase1 = voltageACPhase1;
    }

    public double getVoltageACPhase2() {
        return voltageACPhase2;
    }

    public void setVoltageACPhase2(double voltageACPhase2) {
        this.voltageACPhase2 = voltageACPhase2;
    }

    public double getVoltageACPhase3() {
        return voltageACPhase3;
    }

    public void setVoltageACPhase3(double voltageACPhase3) {
        this.voltageACPhase3 = voltageACPhase3;
    }
}
