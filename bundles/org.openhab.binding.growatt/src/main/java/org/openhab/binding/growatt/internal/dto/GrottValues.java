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
package org.openhab.binding.growatt.internal.dto;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.growatt.internal.GrowattBindingConstants;
import org.openhab.binding.growatt.internal.GrowattBindingConstants.UoM;
import org.openhab.core.library.types.QuantityType;

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

    // solar generation
    public @Nullable @SerializedName(value = "pvpowerin") Integer pv_power_in;
    public @Nullable @SerializedName(value = "pvpowerout") Integer pv_power_out;

    // electric data for strings #1 and #2
    public @Nullable @SerializedName(value = "pv1voltage", alternate = { "vpv1" }) Integer pv1_potential;
    public @Nullable @SerializedName(value = "pv1current", alternate = { "buck1curr" }) Integer pv1_current;
    public @Nullable @SerializedName(value = "pv1watt", alternate = { "ppv1" }) Integer pv1_power;

    public @Nullable @SerializedName(value = "pv2voltage", alternate = { "vpv2" }) Integer pv2_potential;
    public @Nullable @SerializedName(value = "pv2current", alternate = { "buck2curr" }) Integer pv2_current;
    public @Nullable @SerializedName(value = "pv2watt", alternate = { "ppv2" }) Integer pv2_power;

    // grid electric data (1-phase resp. 3-phase)
    public @Nullable @SerializedName(value = "pvfrequentie", alternate = { "line_freq" }) Integer grid_frequency;

    public @Nullable @SerializedName(value = "pvgridvoltage", alternate = { "grid_volt" }) Integer grid_potential;
    public @Nullable @SerializedName(value = "pvgridvoltage2") Integer grid_potential_s;
    public @Nullable @SerializedName(value = "pvgridvoltage3") Integer grid_potential_t;
    public @Nullable @SerializedName(value = "Vac_RS") Integer grid_potential_rs;
    public @Nullable @SerializedName(value = "Vac_ST") Integer grid_potential_st;
    public @Nullable @SerializedName(value = "Vac_TR") Integer grid_potential_tr;

    // solar power to grid
    public @Nullable @SerializedName(value = "pvgridcurrent", alternate = { "OP_Curr" }) Integer grid_current;
    public @Nullable @SerializedName(value = "pvgridcurrent2") Integer grid_current_s;
    public @Nullable @SerializedName(value = "pvgridcurrent3") Integer grid_current_t;

    public @Nullable @SerializedName(value = "pvgridpower", alternate = { "op_watt" }) Integer grid_power;
    public @Nullable @SerializedName(value = "pvgridpower2") Integer grid_power_s;
    public @Nullable @SerializedName(value = "pvgridpower3") Integer grid_power_t;

    public @Nullable @SerializedName(value = "op_va") Integer grid_va;

    // grid power to battery
    public @Nullable @SerializedName(value = "ACCharCurr") Integer grid_charge_current;
    public @Nullable @SerializedName(value = "acchr_watt") Integer grid_charge_power;
    public @Nullable @SerializedName(value = "acchar_VA") Integer grid_charge_va;

    // grid power from battery
    // TODO current ??
    public @Nullable @SerializedName(value = "ACDischarWatt") Integer grid_discharge_power;
    public @Nullable @SerializedName(value = "ACDischarVA") Integer grid_discharge_va;

    // battery discharge / charge power
    // TODO current ??
    public @Nullable @SerializedName(value = "p1charge1", alternate = { "BatWatt" }) Integer battery_charge_power;
    public @Nullable @SerializedName(value = "pdischarge1", alternate = { "BatDischarWatt" }) Integer battery_discharge_power;
    public @Nullable @SerializedName(value = "BatDischarVA") Integer battery_discharge_va;

    // power to grid
    public @Nullable @SerializedName(value = "pacttogridtotal") Integer to_grid_power;
    public @Nullable @SerializedName(value = "pacttogridr") Integer to_grid_power_r;
    public @Nullable @SerializedName(value = "pacttogrids") Integer to_grid_power_s;
    public @Nullable @SerializedName(value = "pacttogridt") Integer to_grid_power_t;

    // power to user
    public @Nullable @SerializedName(value = "pacttousertotal") Integer to_user_power;
    public @Nullable @SerializedName(value = "pacttouserr") Integer to_user_power_r;
    public @Nullable @SerializedName(value = "pacttousers") Integer to_user_power_s;
    public @Nullable @SerializedName(value = "pacttousert") Integer to_user_power_t;

    // power to local
    public @Nullable @SerializedName(value = "plocalloadtotal") Integer to_local_power;
    public @Nullable @SerializedName(value = "plocalloadr") Integer to_local_power_r;
    public @Nullable @SerializedName(value = "plocalloads") Integer to_local_power_s;
    public @Nullable @SerializedName(value = "plocalloadt") Integer to_local_power_t;

    // pv energy
    public @Nullable @SerializedName(value = "pvenergytoday") Integer pv_energy_today;
    public @Nullable @SerializedName(value = "pvenergytotal") Integer pv_energy_total;

    // energy taken from solar
    public @Nullable @SerializedName(value = "epvtoday") Integer pv_grid_energy_today;
    public @Nullable @SerializedName(value = "epv1today", alternate = { "epv1tod" }) Integer pv1_grid_energy_today;
    public @Nullable @SerializedName(value = "epv2today", alternate = { "epv2tod" }) Integer pv2_grid_energy_today;

    public @Nullable @SerializedName(value = "epvtotal") Integer pv_grid_energy_total;
    public @Nullable @SerializedName(value = "epv1total", alternate = { "epv1tot" }) Integer pv1_grid_energy_total;
    public @Nullable @SerializedName(value = "epv2total", alternate = { "epv2tot" }) Integer pv2_grid_energy_total;

    // energy supplied to grid
    public @Nullable @SerializedName(value = "etogrid_tod", alternate = { "eactoday" }) Integer to_grid_energy_today;
    public @Nullable @SerializedName(value = "etogrid_tot", alternate = { "eactotal" }) Integer to_grid_energy_total;

    // energy supplied to user
    public @Nullable @SerializedName(value = "etouser_tod") Integer to_user_energy_today;
    public @Nullable @SerializedName(value = "etouser_tot") Integer to_user_energy_total;

    // energy supplied to local load
    public @Nullable @SerializedName(value = "elocalload_tod") Integer to_local_energy_today;
    public @Nullable @SerializedName(value = "elocalloadr_tot") Integer to_local_energy_total;

    // energy taken from grid to charge
    public @Nullable @SerializedName(value = "eacharge_today", alternate = { "eacCharToday" }) Integer grid_charge_energy_today;
    public @Nullable @SerializedName(value = "eacharge_total", alternate = { "eacCharTotal" }) Integer grid_charge_energy_total;

    // energy supplied to grid from discharge of battery
    public @Nullable @SerializedName(value = "edischarge1_tod", alternate = { "eacDischarToday" }) Integer grid_discharge_energy_today;
    public @Nullable @SerializedName(value = "edischarge1_tot", alternate = { "eacDischarTotal" }) Integer grid_discharge_energy_total;

    // energy taken from battery
    public @Nullable @SerializedName(value = "ebatDischarToday") Integer battery_discharge_energy_today;
    public @Nullable @SerializedName(value = "ebatDischarTotal") Integer battery_discharge_energy_total;

    // inverter up time
    public @Nullable  @SerializedName(value = "totworktime") Integer total_work_time;

    // bus voltages
    public @Nullable @SerializedName(value = "pbusvolt", alternate = { "bus_volt" }) Integer p_bus_potential;
    public @Nullable @SerializedName(value = "nbusvolt") Integer n_bus_potential;
    public @Nullable @SerializedName(value = "spbusvolt") Integer sp_bus_potential;

    // temperatures
    public @Nullable @SerializedName(value = "pvtemperature", alternate = { "dcdctemp", "buck1_ntc" }) Integer pv_temperature;
    public @Nullable @SerializedName(value = "pvipmtemperature", alternate = { "invtemp" }) Integer pv_ipm_temperature;
    public @Nullable @SerializedName(value = "pvboosttemp", alternate = { "pvboottemperature" }) Integer pv_boost_temperature;
    public @Nullable @SerializedName(value = "temp4") Integer temperature_4;
    public @Nullable @SerializedName(value = "buck2_ntc") Integer pv2_temperature;

    // battery data
    public @Nullable @SerializedName(value = "batteryType") Integer battery_type;
    public @Nullable @SerializedName(value = "batttemp") Integer battery_temperature;
    public @Nullable @SerializedName(value = "vbat", alternate = { "uwBatVolt_DSP", "bat_Volt" }) Integer battery_potential;
    public @Nullable @SerializedName(value = "bat_dsp")  Integer battery_display;
    public @Nullable @SerializedName(value = "SOC", alternate = { "batterySOC" })  Integer battery_soc;

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
    public @Nullable @SerializedName(value = "uwsysworkmode")  Integer system_work_mode;
    public @Nullable @SerializedName(value = "spdspstatus")  Integer sp_display_status;
    public @Nullable @SerializedName(value = "constantPowerOK")  Integer constant_power_ok;

    // rac ??
    public @Nullable @SerializedName(value = "rac")  Integer rac;
    public @Nullable @SerializedName(value = "eractoday")  Integer erac_today;
    public @Nullable @SerializedName(value = "eractotal")  Integer erac_total;

    // duplicates ??
    public @Nullable @SerializedName(value = "outputvolt") Integer output_potential;
    public @Nullable @SerializedName(value = "outputfreq") Integer output_frequency;
    public @Nullable @SerializedName(value = "loadpercent") Integer load_percent;
    public @Nullable @SerializedName(value = "Inv_Curr") Integer inverter_current;
    public @Nullable @SerializedName(value = "AC_InWatt") Integer grid_input_power;
    public @Nullable @SerializedName(value = "AC_InVA") Integer grid_input_va;

    // @formatter:on

    /**
     * Return the valid values from this DTO in a map between channel id and respective QuantityType states.
     *
     * @return a map of channel ids and respective QuantityType state values.
     * @throws NoSuchFieldException should not occur since we specifically tested this in JUnit tests.
     * @throws SecurityException should not occur since all fields are public.
     * @throws IllegalAccessException should not occur since all fields are public.
     * @throws IllegalArgumentException should not occur since we are specifically working with this class.
     */
    public Map<String, QuantityType<?>> getChannelStates()
            throws NoSuchFieldException, SecurityException, IllegalAccessException, IllegalArgumentException {
        Map<String, QuantityType<?>> map = new HashMap<>();
        for (Entry<String, UoM> entry : GrowattBindingConstants.CHANNEL_ID_UOM_MAP.entrySet()) {
            String channelId = entry.getKey();
            UoM uom = entry.getValue();
            Field field = getClass().getField(getFieldName(channelId));
            Object obj = field.get(this);
            if (obj instanceof Integer) {
                map.put(channelId, QuantityType.valueOf(((Integer) obj).doubleValue() / uom.divisor, uom.units));
            }
        }
        return map;
    }
}
