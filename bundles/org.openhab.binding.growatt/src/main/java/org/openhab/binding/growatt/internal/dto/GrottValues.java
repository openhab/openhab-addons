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
package org.openhab.binding.growatt.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link GrottValues} is a DTO containing inverter value fields received from the Grott application.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrottValues {

    /**
     * Convert Java field name to openHAB channel id
     */
    public static String getChannelId(String fieldName) {
        return fieldName.replace("_", "-");
    }

    /**
     * Convert openHAB channel id to Java field name
     */
    public static String getFieldName(String channelId) {
        return channelId.replace("-", "_");
    }

    // @formatter:off

    // inverter state
    public @Nullable @SerializedName(value = "pvstatus") Integer system_status;

    // solar AC and DC generation
    public @Nullable @SerializedName(value = "pvpowerin") Integer pv_power; // from DC solar
    public @Nullable @SerializedName(value = "pvpowerout") Integer inverter_power; // to AC mains

    // DC electric data for strings #1 and #2
    public @Nullable @SerializedName(value = "pv1voltage", alternate = { "vpv1" }) Integer pv1_voltage;
    public @Nullable @SerializedName(value = "pv1current", alternate = { "buck1curr" }) Integer pv1_current;
    public @Nullable @SerializedName(value = "pv1watt", alternate = { "ppv1" }) Integer pv1_power;

    public @Nullable @SerializedName(value = "pv2voltage", alternate = { "vpv2" }) Integer pv2_voltage;
    public @Nullable @SerializedName(value = "pv2current", alternate = { "buck2curr" }) Integer pv2_current;
    public @Nullable @SerializedName(value = "pv2watt", alternate = { "ppv2" }) Integer pv2_power;

    // AC mains electric data (1-phase resp. 3-phase)
    public @Nullable @SerializedName(value = "pvfrequentie", alternate = { "line_freq", "outputfreq" }) Integer grid_frequency;
    public @Nullable @SerializedName(value = "pvgridvoltage", alternate = { "grid_volt", "outputvolt", "voltage_l1" }) Integer grid_voltage_r;
    public @Nullable @SerializedName(value = "pvgridvoltage2", alternate = { "voltage_l2" }) Integer grid_voltage_s;
    public @Nullable @SerializedName(value = "pvgridvoltage3", alternate = { "voltage_l3" }) Integer grid_voltage_t;
    public @Nullable @SerializedName(value = "Vac_RS", alternate = { "vacrs", "L1-2_voltage" }) Integer grid_voltage_rs;
    public @Nullable @SerializedName(value = "Vac_ST", alternate = { "vacst", "L2-3_voltage" }) Integer grid_voltage_st;
    public @Nullable @SerializedName(value = "Vac_TR", alternate = { "vactr", "L3-1_voltage" }) Integer grid_voltage_tr;

    // solar AC mains power
    public @Nullable @SerializedName(value = "pvgridcurrent", alternate = { "OP_Curr", "Inv_Curr", "Current_l1" }) Integer inverter_current_r;
    public @Nullable @SerializedName(value = "pvgridcurrent2", alternate = { "Current_l2" }) Integer inverter_current_s;
    public @Nullable @SerializedName(value = "pvgridcurrent3", alternate = { "Current_l3" }) Integer inverter_current_t;

    public @Nullable @SerializedName(value = "pvgridpower", alternate = { "op_watt", "AC_InWatt" }) Integer inverter_power_r;
    public @Nullable @SerializedName(value = "pvgridpower2") Integer inverter_power_s;
    public @Nullable @SerializedName(value = "pvgridpower3") Integer inverter_power_t;

    // apparent power VA
    public @Nullable @SerializedName(value = "op_va", alternate = { "AC_InVA" }) Integer inverter_va;

    // battery discharge / charge power
    public @Nullable @SerializedName(value = "p1charge1", alternate = { "acchr_watt", "BatWatt", "bdc1_pchr" }) Integer charge_power;
    public @Nullable @SerializedName(value = "pdischarge1", alternate = { "ACDischarWatt", "BatDischarWatt", "bdc1_pdischr" }) Integer discharge_power;

    // miscellaneous battery
    public @Nullable @SerializedName(value = "ACCharCurr") Integer charge_current;
    public @Nullable @SerializedName(value = "ACDischarVA", alternate = { "BatDischarVA", "acchar_VA" }) Integer discharge_va;

    // power exported to utility company
    public @Nullable @SerializedName(value = "pactogridtot", alternate = { "ptogridtotal" }) Integer export_power;
    public @Nullable @SerializedName(value = "pactogridr") Integer export_power_r;
    public @Nullable @SerializedName(value = "pactogrids") Integer export_power_s;
    public @Nullable @SerializedName(value = "pactogridt") Integer export_power_t;

    // power imported from utility company
    public @Nullable @SerializedName(value = "pactousertot", alternate = { "ptousertotal", "pos_rev_act_power" }) Integer import_power;
    public @Nullable @SerializedName(value = "pactouserr", alternate = { "act_power_l1" }) Integer import_power_r;
    public @Nullable @SerializedName(value = "pactousers", alternate = { "act_power_l2" }) Integer import_power_s;
    public @Nullable @SerializedName(value = "pactousert", alternate = { "act_power_l3" }) Integer import_power_t;

    // power delivered to internal load
    public @Nullable @SerializedName(value = "plocaloadtot", alternate = { "ptoloadtotal" }) Integer load_power;
    public @Nullable @SerializedName(value = "plocaloadr") Integer load_power_r;
    public @Nullable @SerializedName(value = "plocaloads") Integer load_power_s;
    public @Nullable @SerializedName(value = "plocaloadt") Integer load_power_t;

    // inverter AC energy
    public @Nullable @SerializedName(value = "eactoday", alternate = { "pvenergytoday" }) Integer inverter_energy_today;
    public @Nullable @SerializedName(value = "eactotal", alternate = { "pvenergytotal" }) Integer inverter_energy_total;

    // solar DC pv energy
    public @Nullable @SerializedName(value = "epvtoday") Integer pv_energy_today;
    public @Nullable @SerializedName(value = "epv1today", alternate = { "epv1tod" }) Integer pv1_energy_today;
    public @Nullable @SerializedName(value = "epv2today", alternate = { "epv2tod" }) Integer pv2_energy_today;

    public @Nullable @SerializedName(value = "epvtotal") Integer pv_energy_total;
    public @Nullable @SerializedName(value = "epv1total", alternate = { "epv1tot" }) Integer pv1_energy_total;
    public @Nullable @SerializedName(value = "epv2total", alternate = { "epv2tot" }) Integer pv2_energy_total;

    // energy exported to utility company
    public @Nullable @SerializedName(value = "etogrid_tod", alternate = { "etogridtoday" }) Integer export_energy_today;
    public @Nullable @SerializedName(value = "etogrid_tot", alternate = { "etogridtotal", "rev_act_energy" }) Integer export_energy_total;

    // energy imported from utility company
    public @Nullable @SerializedName(value = "etouser_tod", alternate = { "etousertoday" }) Integer import_energy_today;
    public @Nullable @SerializedName(value = "etouser_tot", alternate = { "etousertotal", "pos_act_energy" }) Integer import_energy_total;

    // energy supplied to local load
    public @Nullable @SerializedName(value = "elocalload_tod", alternate = { "eloadtoday" }) Integer load_energy_today;
    public @Nullable @SerializedName(value = "elocalload_tot", alternate = { "eloadtotal" }) Integer load_energy_total;

    // charging energy from import
    public @Nullable @SerializedName(value = "eacharge_today", alternate = { "eacCharToday", "eacchrtoday" }) Integer import_charge_energy_today;
    public @Nullable @SerializedName(value = "eacharge_total", alternate = { "eacCharTotal", "eacchrtotal" }) Integer import_charge_energy_total;

    // charging energy from solar
    public @Nullable @SerializedName(value = "eharge1_tod", alternate = { "echrtoday" }) Integer inverter_charge_energy_today;
    public @Nullable @SerializedName(value = "eharge1_tot", alternate = { "echrtotal" }) Integer inverter_charge_energy_total;

    // discharging energy
    public @Nullable @SerializedName(value = "edischarge1_tod", alternate = { "eacDischarToday", "ebatDischarToday", "edischrtoday" }) Integer discharge_energy_today;
    public @Nullable @SerializedName(value = "edischarge1_tot", alternate = { "eacDischarTotal", "ebatDischarTotal", "edischrtotal" }) Integer discharge_energy_total;

    // inverter up time
    public @Nullable @SerializedName(value = "totworktime") Integer total_work_time;

    // bus voltages
    public @Nullable @SerializedName(value = "pbusvolt", alternate = { "bus_volt", "pbusvoltage" }) Integer p_bus_voltage;
    public @Nullable @SerializedName(value = "nbusvolt", alternate = { "nbusvoltage" }) Integer n_bus_voltage;
    public @Nullable @SerializedName(value = "spbusvolt") Integer sp_bus_voltage;

    // temperatures
    public @Nullable @SerializedName(value = "pvtemperature", alternate = { "dcdctemp", "buck1_ntc" }) Integer pv_temperature;
    public @Nullable @SerializedName(value = "pvipmtemperature", alternate = { "invtemp" }) Integer pv_ipm_temperature;
    public @Nullable @SerializedName(value = "pvboosttemp", alternate = { "pvboottemperature", "temp3" }) Integer pv_boost_temperature;
    public @Nullable @SerializedName(value = "temp4") Integer temperature_4;
    public @Nullable @SerializedName(value = "buck2_ntc", alternate = { "temp5" }) Integer pv2_temperature;

    // battery data
    public @Nullable @SerializedName(value = "batterytype") Integer battery_type;
    public @Nullable @SerializedName(value = "batttemp", alternate = { "bdc1_tempa" }) Integer battery_temperature;
    public @Nullable @SerializedName(value = "vbat", alternate = { "uwBatVolt_DSP", "bat_Volt", "bms_batteryvolt" }) Integer battery_voltage;
    public @Nullable @SerializedName(value = "bat_dsp") Integer battery_display;
    public @Nullable @SerializedName(value = "SOC", alternate = { "batterySOC", "bms_soc" }) Integer battery_soc;

    // fault codes
    public @Nullable @SerializedName(value = "systemfaultword0", alternate = { "isof", "faultBit" }) Integer system_fault_0;
    public @Nullable @SerializedName(value = "systemfaultword1", alternate = { "gfcif", "faultValue" }) Integer system_fault_1;
    public @Nullable @SerializedName(value = "systemfaultword2", alternate = { "dcif", "warningBit" }) Integer system_fault_2;
    public @Nullable @SerializedName(value = "systemfaultword3", alternate = { "vpvfault", "warningValue" }) Integer system_fault_3;
    public @Nullable @SerializedName(value = "systemfaultword4", alternate = { "vacfault" }) Integer system_fault_4;
    public @Nullable @SerializedName(value = "systemfaultword5", alternate = { "facfault" }) Integer system_fault_5;
    public @Nullable @SerializedName(value = "systemfaultword6", alternate = { "tempfault" }) Integer system_fault_6;
    public @Nullable @SerializedName(value = "systemfaultword7", alternate = { "faultcode" }) Integer system_fault_7;

    // miscellaneous
    public @Nullable @SerializedName(value = "uwsysworkmode") Integer system_work_mode;
    public @Nullable @SerializedName(value = "spdspstatus") Integer sp_display_status;
    public @Nullable @SerializedName(value = "constantPowerOK") Integer constant_power_ok;
    public @Nullable @SerializedName(value = "loadpercent") Integer load_percent;

    // reactive 'power' resp. 'energy'
    public @Nullable @SerializedName(value = "rac", alternate = { "react_power" }) Integer rac;
    public @Nullable @SerializedName(value = "eractoday", alternate = { "react_energy_kvar" }) Integer erac_today;
    public @Nullable @SerializedName(value = "eractotal") Integer erac_total;

    // @formatter:on
}
