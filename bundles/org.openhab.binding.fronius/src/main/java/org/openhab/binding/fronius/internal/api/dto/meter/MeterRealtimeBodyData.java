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
package org.openhab.binding.fronius.internal.api.dto.meter;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MeterRealtimeBodyData} is responsible for storing
 * the "Data" node of the {@link MeterRealtimeBody}.
 *
 * The main SerializedName values use Smart Meter 63A names
 * The first SerializedName alternate names use Smart Meter 65A names
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class MeterRealtimeBodyData {
    @SerializedName(value = "Current_AC_Phase_1", alternate = { "ACBRIDGE_CURRENT_ACTIVE_MEAN_01_F32" })
    private double currentACPhase1;
    @SerializedName(value = "Current_AC_Phase_2", alternate = { "ACBRIDGE_CURRENT_ACTIVE_MEAN_02_F32" })
    private double currentACPhase2;
    @SerializedName(value = "Current_AC_Phase_3", alternate = { "ACBRIDGE_CURRENT_ACTIVE_MEAN_03_F32" })
    private double currentACPhase3;
    @SerializedName("Details")
    private MeterRealtimeDetails details;
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

    public double getCurrentACPhase2() {
        return currentACPhase2;
    }

    public double getCurrentACPhase3() {
        return currentACPhase3;
    }

    public MeterRealtimeDetails getDetails() {
        if (details == null) {
            details = new MeterRealtimeDetails();
        }
        return details;
    }

    public int getEnable() {
        return enable;
    }

    public double getEnergyReactiveVArACSumConsumed() {
        return energyReactiveVArACSumConsumed;
    }

    public double getEnergyReactiveVArACSumProduced() {
        return energyReactiveVArACSumProduced;
    }

    public double getEnergyRealWACMinusAbsolute() {
        return energyRealWACMinusAbsolute;
    }

    public double getEnergyRealWACPlusAbsolute() {
        return energyRealWACPlusAbsolute;
    }

    public double getEnergyRealWACSumConsumed() {
        return energyRealWACSumConsumed;
    }

    public double getEnergyRealWACSumProduced() {
        return energyRealWACSumProduced;
    }

    public double getFrequencyPhaseAverage() {
        return frequencyPhaseAverage;
    }

    public int getMeterLocationCurrent() {
        return meterLocationCurrent;
    }

    public double getPowerApparentSPhase1() {
        return powerApparentSPhase1;
    }

    public double getPowerApparentSPhase2() {
        return powerApparentSPhase2;
    }

    public double getPowerApparentSPhase3() {
        return powerApparentSPhase3;
    }

    public double getPowerApparentSSum() {
        return powerApparentSSum;
    }

    public double getPowerFactorPhase1() {
        return powerFactorPhase1;
    }

    public double getPowerFactorPhase2() {
        return powerFactorPhase2;
    }

    public double getPowerFactorPhase3() {
        return powerFactorPhase3;
    }

    public double getPowerFactorSum() {
        return powerFactorSum;
    }

    public double getPowerReactiveQPhase1() {
        return powerReactiveQPhase1;
    }

    public double getPowerReactiveQPhase2() {
        return powerReactiveQPhase2;
    }

    public double getPowerReactiveQPhase3() {
        return powerReactiveQPhase3;
    }

    public double getPowerReactiveQSum() {
        return powerReactiveQSum;
    }

    public double getPowerRealPPhase1() {
        return powerRealPPhase1;
    }

    public double getPowerRealPPhase2() {
        return powerRealPPhase2;
    }

    public double getPowerRealPPhase3() {
        return powerRealPPhase3;
    }

    public double getPowerRealPSum() {
        return powerRealPSum;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public int getVisible() {
        return visible;
    }

    public double getVoltageACPhaseToPhase12() {
        return voltageACPhaseToPhase12;
    }

    public double getVoltageACPhaseToPhase23() {
        return voltageACPhaseToPhase23;
    }

    public double getVoltageACPhaseToPhase31() {
        return voltageACPhaseToPhase31;
    }

    public double getVoltageACPhase1() {
        return voltageACPhase1;
    }

    public double getVoltageACPhase2() {
        return voltageACPhase2;
    }

    public double getVoltageACPhase3() {
        return voltageACPhase3;
    }
}
