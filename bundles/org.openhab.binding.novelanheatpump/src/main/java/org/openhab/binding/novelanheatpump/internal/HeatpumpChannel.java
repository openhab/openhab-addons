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
package org.openhab.binding.novelanheatpump.internal;

import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;

/**
 * Represents all valid commands which could be processed by this binding
 *
 * @author Jan-Philipp Bolle
 * @author Stefan Giehl -- Adjustments for OpenHab3
 * @since 1.0.0
 */
public enum HeatpumpChannel {

    // in german Außentemperatur
    CHANNEL_TEMPERATURE_OUTSIDE("temperature_outside", NumberItem.class, null),

    // in german Außentemperatur
    CHANNEL_TEMPERATURE_OUTSIDE_AVG("temperature_outside_avg", NumberItem.class, null),

    // in german Rücklauf
    CHANNEL_TEMPERATURE_RETURN("temperature_return", NumberItem.class, null),

    // in german Rücklauf Soll
    CHANNEL_TEMPERATURE_REFERENCE_RETURN("temperature_reference_return", NumberItem.class, null),

    // in german Vorlauf
    CHANNEL_TEMPERATURE_SUPPLAY("temperature_supplay", NumberItem.class, null),

    // in german Brauchwasser Soll
    CHANNEL_TEMPERATURE_SERVICEWATER_REFERENCE("temperature_servicewater_reference", NumberItem.class, null),

    // in german Brauchwasser Ist
    CHANNEL_TEMPERATURE_SERVICEWATER("temperature_servicewater", NumberItem.class, null),

    CHANNEL_HEATPUMP_STATE("state", StringItem.class, null),

    CHANNEL_HEATPUMP_SIMPLE_STATE("simple_state", StringItem.class, null),

    CHANNEL_HEATPUMP_SIMPLE_STATE_NUM("simple_state_num", NumberItem.class, null),

    CHANNEL_HEATPUMP_SWITCHOFF_REASON_0("switchoff_reason_0", NumberItem.class, null),

    CHANNEL_HEATPUMP_SWITCHOFF_CODE_0("switchoff_code_0", NumberItem.class, null),

    CHANNEL_HEATPUMP_EXTENDED_STATE("extended_state", StringItem.class, null),

    CHANNEL_HEATPUMP_SOLAR_COLLECTOR("temperature_solar_collector", NumberItem.class, null),

    // in german Temperatur Heissgas
    CHANNEL_TEMPERATURE_HOT_GAS("temperature_hot_gas", NumberItem.class, null),

    // in german Sondentemperatur WP Eingang
    CHANNEL_TEMPERATURE_PROBE_IN("temperature_probe_in", NumberItem.class, null),

    // in german Sondentemperatur WP Ausgang
    CHANNEL_TEMPERATURE_PROBE_OUT("temperature_probe_out", NumberItem.class, null),

    // in german Vorlauftemperatur MK1 IST
    CHANNEL_TEMPERATURE_MK1("temperature_mk1", NumberItem.class, null),

    // in german Vorlauftemperatur MK1 SOLL
    CHANNEL_TEMPERATURE_MK1_REFERENCE("temperature_mk1_reference", NumberItem.class, null),

    // in german Vorlauftemperatur MK1 IST
    CHANNEL_TEMPERATURE_MK2("temperature_mk2", NumberItem.class, null),

    // in german Vorlauftemperatur MK1 SOLL
    CHANNEL_TEMPERATURE_MK2_REFERENCE("temperature_mk2_reference", NumberItem.class, null),

    // in german Temperatur externe Energiequelle
    CHANNEL_TEMPERATURE_EXTERNAL_SOURCE("temperature_external_source", NumberItem.class, null),

    // in german Betriebsstunden Verdichter1
    CHANNEL_HOURS_COMPRESSOR1("hours_compressor1", StringItem.class, null),

    // in german Impulse (Starts) Verdichter 1
    CHANNEL_STARTS_COMPRESSOR1("starts_compressor1", NumberItem.class, null),

    // in german Betriebsstunden Verdichter2
    CHANNEL_HOURS_COMPRESSOR2("hours_compressor2", StringItem.class, null),

    // in german Impulse (Starts) Verdichter 2
    CHANNEL_STARTS_COMPRESSOR2("starts_compressor2", NumberItem.class, null),

    // Temperatur_TRL_ext
    CHANNEL_TEMPERATURE_OUT_EXTERNAL("temperature_out_external", NumberItem.class, null),

    // in german Betriebsstunden ZWE1
    CHANNEL_HOURS_ZWE1("hours_zwe1", StringItem.class, null),

    // in german Betriebsstunden ZWE1
    CHANNEL_HOURS_ZWE2("hours_zwe2", StringItem.class, null),

    // in german Betriebsstunden ZWE1
    CHANNEL_HOURS_ZWE3("hours_zwe3", StringItem.class, null),

    // in german Betriebsstunden Wärmepumpe
    CHANNEL_HOURS_HETPUMP("hours_heatpump", StringItem.class, null),

    // in german Betriebsstunden Heizung
    CHANNEL_HOURS_HEATING("hours_heating", StringItem.class, null),

    // in german Betriebsstunden Brauchwasser
    CHANNEL_HOURS_WARMWATER("hours_warmwater", StringItem.class, null),

    // in german Betriebsstunden Brauchwasser
    CHANNEL_HOURS_COOLING("hours_cooling", StringItem.class, null),

    // in german Waermemenge Heizung
    CHANNEL_THERMALENERGY_HEATING("thermalenergy_heating", NumberItem.class, null),

    // in german Waermemenge Brauchwasser
    CHANNEL_THERMALENERGY_WARMWATER("thermalenergy_warmwater", NumberItem.class, null),

    // in german Waermemenge Schwimmbad
    CHANNEL_THERMALENERGY_POOL("thermalenergy_pool", NumberItem.class, null),

    // in german Waermemenge gesamt seit Reset
    CHANNEL_THERMALENERGY_TOTAL("thermalenergy_total", NumberItem.class, null),

    // in german Massentrom
    CHANNEL_MASSFLOW("massflow", NumberItem.class, null),

    CHANNEL_HEATPUMP_SOLAR_STORAGE("temperature_solar_storage", NumberItem.class, null),

    // in german Heizung Betriebsart
    CHANNEL_HEATING_OPERATION_MODE("heating_operation_mode", NumberItem.class, 3),

    // in german Heizung Temperatur (Parallelverschiebung)
    CHANNEL_HEATING_TEMPERATURE("heating_temperature", NumberItem.class, 1),

    // in german Warmwasser Betriebsart
    CHANNEL_WARMWATER_OPERATION_MODE("warmwater_operation_mode", NumberItem.class, 4),

    // in german Warmwasser Temperatur
    CHANNEL_WARMWATER_TEMPERATURE("warmwater_temperature", NumberItem.class, 2),

    // in german Comfort Kühlung Betriebsart
    CHANNEL_COOLING_OPERATION_MODE("cooling_operation_mode", NumberItem.class, 100),

    // in german Comfort Kühlung AT-Freigabe
    CHANNEL_COOLING_RELEASE_TEMPERATURE("cooling_release_temperature", NumberItem.class, 110),

    // in german Solltemp MK1
    CHANNEL_COOLING_INLET_TEMP("cooling_inlet_temperature", NumberItem.class, 132),

    // in german AT-Überschreitung
    CHANNEL_COOLING_START_AFTER_HOURS("cooling_start_hours", NumberItem.class, 850),

    // in german AT-Unterschreitung
    CHANNEL_COOLING_STOP_AFTER_HOURS("cooling_stop_hours", NumberItem.class, 851),

    // in german AV (Abtauventil)
    CHANNEL_OUTPUT_AV("output_av", SwitchItem.class, null),

    // in german BUP (Brauchwasserpumpe/Umstellventil)
    CHANNEL_OUTPUT_BUP("output_bup", SwitchItem.class, null),

    // in german HUP (Heizungsumwälzpumpe)
    CHANNEL_OUTPUT_HUP("output_hup", SwitchItem.class, null),

    // in german MA1 (Mischkreis 1 auf)
    CHANNEL_OUTPUT_MA1("output_ma1", SwitchItem.class, null),

    // in german MZ1 (Mischkreis 1 zu)
    CHANNEL_OUTPUT_MZ1("output_mz1", SwitchItem.class, null),

    // in german VEN (Ventilation/Lüftung)
    CHANNEL_OUTPUT_VEN("output_ven", SwitchItem.class, null),

    // in german VBO (Solepumpe/Ventilator)
    CHANNEL_OUTPUT_VBO("output_vbo", SwitchItem.class, null),

    // in german VD1 (Verdichter 1)
    CHANNEL_OUTPUT_VD1("output_vd1", SwitchItem.class, null),

    // in german VD2 (Verdichter 2)
    CHANNEL_OUTPUT_VD2("output_vd2", SwitchItem.class, null),

    // in german ZIP (Zirkulationspumpe)
    CHANNEL_OUTPUT_ZIP("output_zip", SwitchItem.class, null),

    // in german ZUP (Zusatzumwälzpumpe)
    CHANNEL_OUTPUT_ZUP("output_zup", SwitchItem.class, null),

    // in german ZW1 (Steuersignal Zusatzheizung v. Heizung)
    CHANNEL_OUTPUT_ZW1("output_zw1", SwitchItem.class, null),

    // in german ZW2 (Steuersignal Zusatzheizung/Störsignal)
    CHANNEL_OUTPUT_ZW2SST("output_zw2sst", SwitchItem.class, null),

    // in german ZW3 (Zusatzheizung 3)
    CHANNEL_OUTPUT_ZW3SST("output_zw3sst", SwitchItem.class, null),

    // in german FP2 (Pumpe Mischkreis 2)
    CHANNEL_OUTPUT_FP2("output_fp2", SwitchItem.class, null),

    // in german SLP (Solarladepumpe)
    CHANNEL_OUTPUT_SLP("output_slp", SwitchItem.class, null),

    // in german SUP (Schwimmbadpumpe)
    CHANNEL_OUTPUT_SUP("output_sup", SwitchItem.class, null),

    // in german MA2 (Mischkreis 2 auf)
    CHANNEL_OUTPUT_MA2("output_ma2", SwitchItem.class, null),

    // in german MZ2 (Mischkreis 2 zu)
    CHANNEL_OUTPUT_MZ2("output_mz2", SwitchItem.class, null),

    // in german MA3 (Mischkreis 3 auf)
    CHANNEL_OUTPUT_MA3("output_ma3", SwitchItem.class, null),

    // in german MZ3 (Mischkreis 3 zu)
    CHANNEL_OUTPUT_MZ3("output_mz3", SwitchItem.class, null),

    // in german FP3 (Pumpe Mischkreis 3)
    CHANNEL_OUTPUT_FP3("output_fp3", SwitchItem.class, null),

    // in german VSK
    CHANNEL_OUTPUT_VSK("output_vsk", SwitchItem.class, null),

    // in german FRH
    CHANNEL_OUTPUT_FRH("output_frh", SwitchItem.class, null),

    // in german VDH (Verdichterheizung)
    CHANNEL_OUTPUT_VDH("output_vdh", SwitchItem.class, null),

    // in german AV2 (Abtauventil 2)
    CHANNEL_OUTPUT_AV2("output_av2", SwitchItem.class, null),

    // in german VBO2 (Solepumpe/Ventilator)
    CHANNEL_OUTPUT_VBO2("output_vbo2", SwitchItem.class, null),

    // in german VD12 (Verdichter 1/2)
    CHANNEL_OUTPUT_VD12("output_vd12", SwitchItem.class, null),

    // in german VDH2 (Verdichterheizung 2)
    CHANNEL_OUTPUT_VDH2("output_vdh2", SwitchItem.class, null);

    /** Represents the heatpump command as it will be used in *.items configuration */
    String command;
    Class<? extends Item> itemClass;
    Integer writeParam;

    private HeatpumpChannel(String command, Class<? extends Item> itemClass, Integer writeParam) {
        this.command = command;
        this.itemClass = itemClass;
        this.writeParam = writeParam;
    }

    public String getCommand() {
        return command;
    }

    public Class<? extends Item> getItemClass() {
        return itemClass;
    }

    public Boolean isWritable() {
        return writeParam != null;
    }

    public Integer getWriteParameter() {
        return writeParam;
    }

    public static HeatpumpChannel fromString(String heatpumpCommand) {

        if ("".equals(heatpumpCommand)) {
            return null;
        }
        for (HeatpumpChannel c : HeatpumpChannel.values()) {

            if (c.getCommand().equals(heatpumpCommand)) {
                return c;
            }
        }

        throw new IllegalArgumentException("cannot find novelanHeatpumpCommand for '" + heatpumpCommand + "'");
    }

    @Override
    public String toString() {
        return getCommand();
    }
}
