/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.luxtronik.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LuxtronikBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan-Philipp Bolle - Initial contribution
 * @author Hilbrand Bouwkamp - Migrated to openHAB 3
 */
@NonNullByDefault
public class LuxtronikBindingConstants {

    private static final String BINDING_ID = "luxtronik";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HEATPUMP = new ThingTypeUID(BINDING_ID, "heatpump");

    // List of all Channel ids// in german Außentemperatur
    public static final String CHANNEL_TEMPERATURE_OUTSIDE = "temperature_outside";
    // in german Außentemperatur
    public static final String CHANNEL_TEMPERATURE_OUTSIDE_AVG = "temperature_outside_avg";
    // in german Rücklauf
    public static final String CHANNEL_TEMPERATURE_RETURN = "temperature_return";
    // in german Rücklauf Soll
    public static final String CHANNEL_TEMPERATURE_REFERENCE_RETURN = "temperature_reference_return";
    // in german Vorlauf
    public static final String CHANNEL_TEMPERATURE_SUPPLAY = "temperature_supplay";
    // in german Brauchwasser Soll
    public static final String CHANNEL_TEMPERATURE_SERVICEWATER_REFERENCE = "temperature_servicewater_reference";
    // in german Brauchwasser Ist
    public static final String CHANNEL_TEMPERATURE_SERVICEWATER = "temperature_servicewater";
    public static final String CHANNEL_HEATPUMP_STATE = "state";
    public static final String CHANNEL_HEATPUMP_SIMPLE_STATE = "simple_state";
    public static final String CHANNEL_HEATPUMP_SIMPLE_STATE_NUM = "simple_state_num";
    public static final String CHANNEL_HEATPUMP_SWITCHOFF_REASON_0 = "switchoff_reason_0";
    public static final String CHANNEL_HEATPUMP_SWITCHOFF_CODE_0 = "switchoff_code_0";
    public static final String CHANNEL_HEATPUMP_EXTENDED_STATE = "extended_state";
    public static final String CHANNEL_HEATPUMP_SOLAR_COLLECTOR = "temperature_solar_collector";
    // in german Temperatur Heissgas
    public static final String CHANNEL_TEMPERATURE_HOT_GAS = "temperature_hot_gas";
    // in german Sondentemperatur WP Eingang
    public static final String CHANNEL_TEMPERATURE_PROBE_IN = "temperature_probe_in";
    // in german Sondentemperatur WP Ausgang
    public static final String CHANNEL_TEMPERATURE_PROBE_OUT = "temperature_probe_out";
    // in german Vorlauftemperatur MK1 IST
    public static final String CHANNEL_TEMPERATURE_MK1 = "temperature_mk1";
    // in german Vorlauftemperatur MK1 SOLL
    public static final String CHANNEL_TEMPERATURE_MK1_REFERENCE = "temperature_mk1_reference";
    // in german Vorlauftemperatur MK1 IST
    public static final String CHANNEL_TEMPERATURE_MK2 = "temperature_mk2";
    // in german Vorlauftemperatur MK1 SOLL
    public static final String CHANNEL_TEMPERATURE_MK2_REFERENCE = "temperature_mk2_reference";
    // in german Temperatur externe Energiequelle
    public static final String CHANNEL_TEMPERATURE_EXTERNAL_SOURCE = "temperature_external_source";
    // in german Betriebsstunden Verdichter1
    public static final String CHANNEL_HOURS_COMPRESSOR1 = "hours_compressor1";
    // in german Impulse (Starts) Verdichter 1
    public static final String CHANNEL_STARTS_COMPRESSOR1 = "starts_compressor1";
    // in german Betriebsstunden Verdichter2
    public static final String CHANNEL_HOURS_COMPRESSOR2 = "hours_compressor2";
    // in german Impulse (Starts) Verdichter 2
    public static final String CHANNEL_STARTS_COMPRESSOR2 = "starts_compressor2";
    // Temperatur_TRL_ext
    public static final String CHANNEL_TEMPERATURE_OUT_EXTERNAL = "temperature_out_external";
    // in german Betriebsstunden ZWE1
    public static final String CHANNEL_HOURS_ZWE1 = "hours_zwe1";
    // in german Betriebsstunden ZWE1
    public static final String CHANNEL_HOURS_ZWE2 = "hours_zwe2";
    // in german Betriebsstunden ZWE1
    public static final String CHANNEL_HOURS_ZWE3 = "hours_zwe3";
    // in german Betriebsstunden Wärmepumpe
    public static final String CHANNEL_HOURS_HETPUMP = "hours_heatpump";
    // in german Betriebsstunden Heizung
    public static final String CHANNEL_HOURS_HEATING = "hours_heating";
    // in german Betriebsstunden Brauchwasser
    public static final String CHANNEL_HOURS_WARMWATER = "hours_warmwater";
    // in german Betriebsstunden Brauchwasser
    public static final String CHANNEL_HOURS_COOLING = "hours_cooling";
    // in german Waermemenge Heizung
    public static final String CHANNEL_THERMALENERGY_HEATING = "thermalenergy_heating";
    // in german Waermemenge Brauchwasser
    public static final String CHANNEL_THERMALENERGY_WARMWATER = "thermalenergy_warmwater";
    // in german Waermemenge Schwimmbad
    public static final String CHANNEL_THERMALENERGY_POOL = "thermalenergy_pool";
    // in german Waermemenge gesamt seit Reset
    public static final String CHANNEL_THERMALENERGY_TOTAL = "thermalenergy_total";
    // in german Massentrom
    public static final String CHANNEL_MASSFLOW = "massflow";
    public static final String CHANNEL_HEATPUMP_SOLAR_STORAGE = "temperature_solar_storage";
    // in german Heizung Betriebsart
    public static final String CHANNEL_HEATING_OPERATION_MODE = "heating_operation_mode";
    // in german Heizung Temperatur (Parallelverschiebung)
    public static final String CHANNEL_HEATING_TEMPERATURE = "heating_temperature";
    // in german Warmwasser Betriebsart
    public static final String CHANNEL_WARMWATER_OPERATION_MODE = "warmwater_operation_mode";
    // in german Warmwasser Temperatur
    public static final String CHANNEL_WARMWATER_TEMPERATURE = "warmwater_temperature";
    // in german Comfort Kühlung Betriebsart
    public static final String CHANNEL_COOLING_OPERATION_MODE = "cooling_operation_mode";
    // in german Comfort Kühlung AT-Freigabe
    public static final String CHANNEL_COOLING_RELEASE_TEMPERATURE = "cooling_release_temperature";
    // in german Solltemp MK1
    public static final String CHANNEL_COOLING_INLET_TEMP = "cooling_inlet_temperature";
    // in german AT-Überschreitung
    public static final String CHANNEL_COOLING_START_AFTER_HOURS = "cooling_start_hours";
    // in german AT-Unterschreitung
    public static final String CHANNEL_COOLING_STOP_AFTER_HOURS = "cooling_stop_hours";
    // in german AV (Abtauventil)
    public static final String CHANNEL_OUTPUT_AV = "output_av";
    // in german BUP (Brauchwasserpumpe/Umstellventil)
    public static final String CHANNEL_OUTPUT_BUP = "output_bup";
    // in german HUP (Heizungsumwälzpumpe)
    public static final String CHANNEL_OUTPUT_HUP = "output_hup";
    // in german MA1 (Mischkreis 1 auf)
    public static final String CHANNEL_OUTPUT_MA1 = "output_ma1";
    // in german MZ1 (Mischkreis 1 zu)
    public static final String CHANNEL_OUTPUT_MZ1 = "output_mz1";
    // in german VEN (Ventilation/Lüftung)
    public static final String CHANNEL_OUTPUT_VEN = "output_ven";
    // in german VBO (Solepumpe/Ventilator)
    public static final String CHANNEL_OUTPUT_VBO = "output_vbo";
    // in german VD1 (Verdichter 1)
    public static final String CHANNEL_OUTPUT_VD1 = "output_vd1";
    // in german VD2 (Verdichter 2)
    public static final String CHANNEL_OUTPUT_VD2 = "output_vd2";
    // in german ZIP (Zirkulationspumpe)
    public static final String CHANNEL_OUTPUT_ZIP = "output_zip";
    // in german ZUP (Zusatzumwälzpumpe)
    public static final String CHANNEL_OUTPUT_ZUP = "output_zup";
    // in german ZW1 (Steuersignal Zusatzheizung v. Heizung)
    public static final String CHANNEL_OUTPUT_ZW1 = "output_zw1";
    // in german ZW2 (Steuersignal Zusatzheizung/Störsignal)
    public static final String CHANNEL_OUTPUT_ZW2SST = "output_zw2sst";
    // in german ZW3 (Zusatzheizung 3)
    public static final String CHANNEL_OUTPUT_ZW3SST = "output_zw3sst";
    // in german FP2 (Pumpe Mischkreis 2)
    public static final String CHANNEL_OUTPUT_FP2 = "output_fp2";
    // in german SLP (Solarladepumpe)
    public static final String CHANNEL_OUTPUT_SLP = "output_slp";
    // in german SUP (Schwimmbadpumpe)
    public static final String CHANNEL_OUTPUT_SUP = "output_sup";
    // in german MA2 (Mischkreis 2 auf)
    public static final String CHANNEL_OUTPUT_MA2 = "output_ma2";
    // in german MZ2 (Mischkreis 2 zu)
    public static final String CHANNEL_OUTPUT_MZ2 = "output_mz2";
    // in german MA3 (Mischkreis 3 auf)
    public static final String CHANNEL_OUTPUT_MA3 = "output_ma3";
    // in german MZ3 (Mischkreis 3 zu)
    public static final String CHANNEL_OUTPUT_MZ3 = "output_mz3";
    // in german FP3 (Pumpe Mischkreis 3)
    public static final String CHANNEL_OUTPUT_FP3 = "output_fp3";
    // in german VSK
    public static final String CHANNEL_OUTPUT_VSK = "output_vsk";
    // in german FRH
    public static final String CHANNEL_OUTPUT_FRH = "output_frh";
    // in german VDH (Verdichterheizung)
    public static final String CHANNEL_OUTPUT_VDH = "output_vdh";
    // in german AV2 (Abtauventil 2)
    public static final String CHANNEL_OUTPUT_AV2 = "output_av2";
    // in german VBO2 (Solepumpe/Ventilator)
    public static final String CHANNEL_OUTPUT_VBO2 = "output_vbo2";
    // in german VD12 (Verdichter 1/2)
    public static final String CHANNEL_OUTPUT_VD12 = "output_vd12";
    // in german VDH2 (Verdichterheizung 2)
    public static final String CHANNEL_OUTPUT_VDH2 = "output_vdh2";
}
