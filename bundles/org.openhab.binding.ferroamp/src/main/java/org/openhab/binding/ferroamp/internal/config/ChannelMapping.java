/**
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
package org.openhab.binding.ferroamp.internal.config;

import static org.openhab.binding.ferroamp.internal.FerroampBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;

/**
 * The {@link ChannelMapping} class defines methods, that set the channel configuration for the binding.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class ChannelMapping {

    public String id = "";
    public Unit<?> unit = Units.ONE;
    public String jsonPath = "";

    public ChannelMapping(String id, Unit<?> unit, String jsonPath) {
        this.id = id;
        this.unit = unit;
        this.jsonPath = jsonPath;
    }

    private static ChannelMapping cc(String id, Unit<?> unit, String jsonPath) {
        return new ChannelMapping(id, unit, jsonPath);
    }

    public static List<ChannelMapping> getChannelConfigurationEhub() {
        final List<ChannelMapping> list = new ArrayList<>();
        list.add(cc(CHANNEL_GRID_FREQUENCY, Units.HERTZ, "gridfreq"));
        list.add(cc(CHANNEL_ACE_CURRENT_L1, Units.AMPERE, "iace.L1"));
        list.add(cc(CHANNEL_ACE_CURRENT_L2, Units.AMPERE, "iace.L2"));
        list.add(cc(CHANNEL_ACE_CURRENT_L3, Units.AMPERE, "iace.L3"));
        list.add(cc(CHANNEL_GRID_VOLTAGE_L1, Units.VOLT, "ul.L1"));
        list.add(cc(CHANNEL_GRID_VOLTAGE_L2, Units.VOLT, "ul.L2"));
        list.add(cc(CHANNEL_GRID_VOLTAGE_L3, Units.VOLT, "ul.L3"));
        list.add(cc(CHANNEL_INVERTER_RMS_CURRENT_L1, Units.AMPERE, "il.L1"));
        list.add(cc(CHANNEL_INVERTER_RMS_CURRENT_L2, Units.AMPERE, "il.L2"));
        list.add(cc(CHANNEL_INVERTER_RMS_CURRENT_L3, Units.AMPERE, "il.L3"));
        list.add(cc(CHANNEL_INVERTER_REACTIVE_CURRENT_L1, Units.AMPERE, "ild.L1"));
        list.add(cc(CHANNEL_INVERTER_REACTIVE_CURRENT_L2, Units.AMPERE, "ild.L2"));
        list.add(cc(CHANNEL_INVERTER_REACTIVE_CURRENT_L3, Units.AMPERE, "ild.L3"));
        list.add(cc(CHANNEL_INVERTER_ACTIVE_CURRENT_L1, Units.AMPERE, "ilq.L1"));
        list.add(cc(CHANNEL_INVERTER_ACTIVE_CURRENT_L2, Units.AMPERE, "ilq.L2"));
        list.add(cc(CHANNEL_INVERTER_ACTIVE_CURRENT_L3, Units.AMPERE, "ilq.L3"));
        list.add(cc(CHANNEL_GRID_CURRENT_L1, Units.AMPERE, "iext.L1"));
        list.add(cc(CHANNEL_GRID_CURRENT_L2, Units.AMPERE, "iext.L2"));
        list.add(cc(CHANNEL_GRID_CURRENT_L3, Units.AMPERE, "iext.L3"));
        list.add(cc(CHANNEL_GRID_REACTIVE_CURRENT_L1, Units.AMPERE, "iextd.L1"));
        list.add(cc(CHANNEL_GRID_REACTIVE_CURRENT_L2, Units.AMPERE, "iextd.L2"));
        list.add(cc(CHANNEL_GRID_REACTIVE_CURRENT_L3, Units.AMPERE, "iextd.L3"));
        list.add(cc(CHANNEL_GRID_ACTIVE_CURRENT_L1, Units.AMPERE, "iextq.L1"));
        list.add(cc(CHANNEL_GRID_ACTIVE_CURRENT_L2, Units.AMPERE, "iextq.L2"));
        list.add(cc(CHANNEL_GRID_ACTIVE_CURRENT_L3, Units.AMPERE, "iextq.L3"));
        list.add(cc(CHANNEL_INVERTER_LOAD_REACTIVE_CURRENT_L1, Units.AMPERE, "iloadd.L1"));
        list.add(cc(CHANNEL_INVERTER_LOAD_REACTIVE_CURRENT_L2, Units.AMPERE, "iloadd.L2"));
        list.add(cc(CHANNEL_INVERTER_LOAD_REACTIVE_CURRENT_L3, Units.AMPERE, "iloadd.L3"));
        list.add(cc(CHANNEL_INVERTER_LOAD_ACTIVE_CURRENT_L1, Units.AMPERE, "iloadq.L1"));
        list.add(cc(CHANNEL_INVERTER_LOAD_ACTIVE_CURRENT_L2, Units.AMPERE, "iloadq.L2"));
        list.add(cc(CHANNEL_INVERTER_LOAD_ACTIVE_CURRENT_L3, Units.AMPERE, "iloadq.L3"));
        list.add(cc(CHANNEL_APPARENT_POWER, Units.WATT, "sext"));
        list.add(cc(CHANNEL_GRID_POWER_ACTIVE_L1, Units.AMPERE, "pext.L1"));
        list.add(cc(CHANNEL_GRID_POWER_ACTIVE_L2, Units.AMPERE, "pext.L2"));
        list.add(cc(CHANNEL_GRID_POWER_ACTIVE_L3, Units.AMPERE, "pext.L3"));
        list.add(cc(CHANNEL_GRID_POWER_REACTIVE_L1, Units.WATT, "pextreactive.L1"));
        list.add(cc(CHANNEL_GRID_POWER_REACTIVE_L2, Units.WATT, "pextreactive.L2"));
        list.add(cc(CHANNEL_GRID_POWER_REACTIVE_L3, Units.WATT, "pextreactive.L3"));
        list.add(cc(CHANNEL_INVERTER_POWER_ACTIVE_L1, Units.VOLT, "pinv.L1"));
        list.add(cc(CHANNEL_INVERTER_POWER_ACTIVE_L2, Units.VOLT, "pinv.L2"));
        list.add(cc(CHANNEL_INVERTER_POWER_ACTIVE_L3, Units.VOLT, "pinv.L3"));
        list.add(cc(CHANNEL_INVERTER_POWER_REACTIVE_L1, Units.VOLT, "pinvreactive.L1"));
        list.add(cc(CHANNEL_INVERTER_POWER_REACTIVE_L2, Units.VOLT, "pinvreactive.L2"));
        list.add(cc(CHANNEL_INVERTER_POWER_REACTIVE_L3, Units.VOLT, "pinvreactive.L3"));
        list.add(cc(CHANNEL_CONSUMPTION_POWER_L1, Units.WATT, "pload.L1"));
        list.add(cc(CHANNEL_CONSUMPTION_POWER_L2, Units.WATT, "pload.L2"));
        list.add(cc(CHANNEL_CONSUMPTION_POWER_L3, Units.WATT, "pload.L3"));
        list.add(cc(CHANNEL_CONSUMPTION_POWER_REACTIVE_L1, Units.WATT, "ploadreactive.L1"));
        list.add(cc(CHANNEL_CONSUMPTION_POWER_REACTIVE_L2, Units.WATT, "ploadreactive.L2"));
        list.add(cc(CHANNEL_CONSUMPTION_POWER_REACTIVE_L3, Units.WATT, "ploadreactive.L3"));
        list.add(cc(CHANNEL_SOLAR_PV, Units.WATT, "ppv"));
        list.add(cc(CHANNEL_POSITIVE_DC_LINK_VOLTAGE, Units.VOLT, "udc.pos"));
        list.add(cc(CHANNEL_NEGATIVE_DC_LINK_VOLTAGE, Units.VOLT, "udc.neg"));
        list.add(cc(CHANNEL_GRID_ENERGY_PRODUCED_L1, Units.WATT, "wextprodq.L1")); // unit multiplyer
        list.add(cc(CHANNEL_GRID_ENERGY_PRODUCED_L2, Units.WATT, "wextprodq.L2")); // unit multiplyer
        list.add(cc(CHANNEL_GRID_ENERGY_PRODUCED_L3, Units.WATT, "wextprodq.L3")); // unit multiplyer
        list.add(cc(CHANNEL_GRID_ENERGY_CONSUMED_L1, Units.WATT, "wextconsq.L1")); // unit multiplyer
        list.add(cc(CHANNEL_GRID_ENERGY_CONSUMED_L2, Units.WATT, "wextconsq.L2")); // unit multiplyer
        list.add(cc(CHANNEL_GRID_ENERGY_CONSUMED_L3, Units.WATT, "wextconsq.L3")); // unit multiplyer
        list.add(cc(CHANNEL_INVERTER_ENERGY_PRODUCED_L1, Units.WATT, "winvprodq.L1")); // unit multiplyer
        list.add(cc(CHANNEL_INVERTER_ENERGY_PRODUCED_L2, Units.WATT, "winvprodq.L2")); // unit multiplyer
        list.add(cc(CHANNEL_INVERTER_ENERGY_PRODUCED_L3, Units.WATT, "winvprodq.L3")); // unit multiplyer
        list.add(cc(CHANNEL_INVERTER_ENERGY_CONSUMED_L1, Units.WATT, "winvconsq.L1")); // unit multiplyer
        list.add(cc(CHANNEL_INVERTER_ENERGY_CONSUMED_L2, Units.WATT, "winvconsq.L2")); // unit multiplyer
        list.add(cc(CHANNEL_INVERTER_ENERGY_CONSUMED_L3, Units.WATT, "winvconsq.L3")); // unit multiplyer
        list.add(cc(CHANNEL_LOAD_ENERGY_PRODUCED_L1, Units.WATT, "wloadprodq.L1")); // unit multiplyer
        list.add(cc(CHANNEL_LOAD_ENERGY_PRODUCED_L2, Units.WATT, "wloadprodq.L2")); // unit multiplyer
        list.add(cc(CHANNEL_LOAD_ENERGY_PRODUCED_L3, Units.WATT, "wloadprodq.L3")); // unit multiplyer
        list.add(cc(CHANNEL_LOAD_ENERGY_CONSUMED_L1, Units.WATT, "wloadconsq.L1")); // unit multiplyer
        list.add(cc(CHANNEL_LOAD_ENERGY_CONSUMED_L2, Units.WATT, "wloadconsq.L2")); // unit multiplyer
        list.add(cc(CHANNEL_LOAD_ENERGY_CONSUMED_L3, Units.WATT, "wloadconsq.L3")); // unit multiplyer
        list.add(cc(CHANNEL_GRID_ENERGY_PRODUCED_TOTAL, Units.WATT, "wextprodq_3p")); // unit multiplyer
        list.add(cc(CHANNEL_GRID_ENERGY_CONSUMED_TOTAL, Units.WATT, "wextconsq_3p")); // unit multiplyer
        list.add(cc(CHANNEL_INVERTER_ENERGY_PRODUCED_TOTAL, Units.WATT, "winvprodq_3p")); // unit multiplyer
        list.add(cc(CHANNEL_INVERTER_ENERGY_CONSUMED_TOTAL, Units.WATT, "winvconsq_3p")); // unit multiplyer
        list.add(cc(CHANNEL_LOAD_ENERGY_PRODUCED_TOTAL, Units.WATT, "wloadprodq_3p")); // unit multiplyer
        list.add(cc(CHANNEL_LOAD_ENERGY_CONSUMED_TOTAL, Units.WATT, "wloadprodq_3p")); // unit multiplyer
        list.add(cc(CHANNEL_TOTAL_SOLAR_ENERGY, Units.WATT, "wpv"));
        list.add(cc(CHANNEL_STATE, Units.ONE, "state"));
        list.add(cc(CHANNEL_TIMESTAMP, Units.ONE, "ts"));
        list.add(cc(CHANNEL_BATTERY_ENERGY_PRODUCED, Units.WATT, "wbatprod"));
        list.add(cc(CHANNEL_BATTERY_ENERGY_CONSUMED, Units.WATT, "wpbatcons"));
        list.add(cc(CHANNEL_SOC, Units.PERCENT, "soc"));
        list.add(cc(CHANNEL_SOH, Units.PERCENT, "soh"));
        list.add(cc(CHANNEL_POWER_BATTERY, Units.WATT, "pbat"));
        list.add(cc(CHANNEL_TOTAL_CAPACITY_BATTERIES, Units.WATT_HOUR, "ratedcap"));
        return list;
    }

    public static List<ChannelMapping> getSSOMapping() {
        final List<ChannelMapping> list = new ArrayList<>();
        list.add(cc(CHANNEL_SSO_ID, Units.ONE, "id"));
        list.add(cc(CHANNEL_SSO_PV_VOLTAGE, Units.VOLT, "upv"));
        list.add(cc(CHANNEL_SSO_PV_CURRENT, Units.AMPERE, "ipv"));
        list.add(cc(CHANNEL_SSO_TOTAL_SOLAR_ENERGY, Units.WATT, "wpv"));
        list.add(cc(CHANNEL_SSO_RELAY_STATUS, Units.ONE, "relaystatus"));
        list.add(cc(CHANNEL_SSO_TEMPERATURE, Units.ONE, "temp"));
        list.add(cc(CHANNEL_SSO_FAULT_CODE, Units.ONE, "faultcode"));
        list.add(cc(CHANNEL_SSO_DC_LINK_VOLTAGE, Units.VOLT, "udc"));
        list.add(cc(CHANNEL_SSO_TIMESTAMP, Units.ONE, "ts"));
        return list;
    }

    public static List<ChannelMapping> getESOMapping() {
        final List<ChannelMapping> list = new ArrayList<>();
        list.add(cc(CHANNEL_ESO_ID, Units.ONE, "id"));
        list.add(cc(CHANNEL_ESO_VOLTAGE_BATTERY, Units.VOLT, "ubat"));
        list.add(cc(CHANNEL_ESO_CURRENT_BATTERY, Units.AMPERE, "ibat"));
        list.add(cc(CHANNEL_ESO_BATTERY_ENERGY_PRODUCED, Units.WATT, "wbatprod"));
        list.add(cc(CHANNEL_ESO_BATTERY_ENERGY_CONSUMED, Units.WATT, "wbatcons"));
        list.add(cc(CHANNEL_ESO_SOC, Units.PERCENT, "soc"));
        list.add(cc(CHANNEL_ESO_RELAY_STATUS, Units.ONE, "relaystatus"));
        list.add(cc(CHANNEL_ESO_TEMPERATURE, Units.ONE, "temp"));
        list.add(cc(CHANNEL_ESO_FAULT_CODE, Units.ONE, "faultcode"));
        list.add(cc(CHANNEL_ESO_DC_LINK_VOLTAGE, Units.VOLT, "udc"));
        list.add(cc(CHANNEL_ESO_TIMESTAMP, Units.ONE, "ts"));
        return list;
    }

    public static List<ChannelMapping> getESMMapping() {
        final List<ChannelMapping> list = new ArrayList<>();
        list.add(cc(CHANNEL_ESM_ID, Units.ONE, "id"));
        list.add(cc(CHANNEL_ESM_SOH, Units.PERCENT, "soh"));
        list.add(cc(CHANNEL_ESM_SOC, Units.PERCENT, "soc"));
        list.add(cc(CHANNEL_ESM_TOTAL_CAPACITY, Units.WATT_HOUR, "ratedcapacity")); // unit multiplyer
        list.add(cc(CHANNEL_ESM_POWER_BATTERY, Units.WATT, "ratedpower")); // unit multiplyer
        list.add(cc(CHANNEL_ESM_STATUS, Units.ONE, "status"));
        list.add(cc(CHANNEL_ESM_TIMESTAMP, Units.ONE, "ts"));
        return list;
    }
}