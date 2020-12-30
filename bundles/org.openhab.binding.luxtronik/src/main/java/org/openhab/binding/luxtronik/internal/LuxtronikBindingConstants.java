/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LuxtronikBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan-Philipp Bolle - Initial contribution
 * @author Hilbrand Bouwkamp - Migrated to openHAB 3
 * @author Christoph Scholz - Finished migration to openHAB 3
 */
@NonNullByDefault
public class LuxtronikBindingConstants {

    private static final String BINDING_ID = "luxtronik";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HEATPUMP = new ThingTypeUID(BINDING_ID, "heatpump");

    public static final Map<String, Integer> INTEGER_VALUES = Map.ofEntries(Map.entry("starts_compressor2", 59),
            Map.entry("massflow", 155), Map.entry("switchoff_error_count", 105), Map.entry("starts_compressor1", 57));

    public static final Map<String, Integer> KILOWATT_HOUR_VALUES = Map.ofEntries(
            Map.entry("thermalenergy_warmwater", 152), Map.entry("thermalenergy_heating", 151),
            Map.entry("thermalenergy_total", 154), Map.entry("thermalenergy_pool", 153));

    public static final Map<String, Integer> ONOFF_VALUES = Map.ofEntries(Map.entry("output_vdh", 182),
            Map.entry("output_vdh2", 216), Map.entry("output_vd12", 215), Map.entry("output_vbo2", 214),
            Map.entry("output_av2", 213), Map.entry("output_frh", 167), Map.entry("output_vsk", 166),
            Map.entry("output_ma2", 55), Map.entry("output_mz2", 54), Map.entry("output_sup", 53),
            Map.entry("output_slp", 52), Map.entry("output_fp2", 51), Map.entry("output_zw3sst", 50),
            Map.entry("output_zw2sst", 49), Map.entry("output_zw1", 48), Map.entry("output_zup", 47),
            Map.entry("output_zip", 46), Map.entry("output_vd2", 45), Map.entry("output_vd1", 44),
            Map.entry("output_vbo", 43), Map.entry("output_ven", 42), Map.entry("output_mz1", 41),
            Map.entry("output_ma1", 40), Map.entry("output_hup", 39), Map.entry("output_bup", 38),
            Map.entry("output_av", 37), Map.entry("output_fp3", 140), Map.entry("output_ma3", 139),
            Map.entry("output_mz3", 138));

    public static final Map<String, Integer> SECOND_VALUES = Map.ofEntries(Map.entry("time_zwe1", 60),
            Map.entry("time_compressor2", 58), Map.entry("time_compressor1", 56), Map.entry("state_time", 120),
            Map.entry("time_cooling", 66), Map.entry("time_warmwater", 65), Map.entry("time_heating", 64),
            Map.entry("time_heatpump", 63), Map.entry("time_zwe3", 62), Map.entry("time_zwe2", 61));

    public static final Map<String, Integer> STRING_VALUES = Map.ofEntries(Map.entry("switchoff_reason_4", 110),
            Map.entry("switchoff_reason_3", 109), Map.entry("switchoff_reason_2", 108),
            Map.entry("switchoff_reason_1", 107), Map.entry("switchoff_reason_0", 106),
            Map.entry("switchoff_error_4", 104), Map.entry("switchoff_error_3", 103),
            Map.entry("switchoff_error_2", 102), Map.entry("switchoff_error_1", 101),
            Map.entry("switchoff_error_0", 100), Map.entry("extended_state", 119), Map.entry("state", 117));

    public static final Map<String, Integer> TEMPERATURE_VALUES = Map.ofEntries(
            Map.entry("temperature_external_source", 28), Map.entry("temperature_solar_storage", 27),
            Map.entry("temperature_solar_collector", 26), Map.entry("temperature_mk2_reference", 25),
            Map.entry("temperature_mk2", 24), Map.entry("temperature_mk1_reference", 22),
            Map.entry("temperature_mk1", 21), Map.entry("temperature_probe_out", 20),
            Map.entry("temperature_probe_in", 19), Map.entry("temperature_servicewater_reference", 18),
            Map.entry("temperature_servicewater", 17), Map.entry("temperature_outside_avg", 16),
            Map.entry("temperature_outside", 15), Map.entry("temperature_hot_gas", 14),
            Map.entry("temperature_out_external", 13), Map.entry("temperature_reference_return", 12),
            Map.entry("temperature_return", 11), Map.entry("temperature_supply", 10));

    public static final Map<String, Integer> TIMESTAMP_VALUES = Map.ofEntries(
            Map.entry("switchoff_reason_timestamp_4", 115), Map.entry("switchoff_error_timestamp_4", 99),
            Map.entry("switchoff_error_timestamp_3", 98), Map.entry("switchoff_error_timestamp_2", 97),
            Map.entry("switchoff_error_timestamp_1", 96), Map.entry("switchoff_error_timestamp_0", 95),
            Map.entry("switchoff_reason_timestamp_3", 114), Map.entry("switchoff_reason_timestamp_2", 113),
            Map.entry("switchoff_reason_timestamp_1", 112), Map.entry("switchoff_reason_timestamp_0", 111));

    public static final Map<String, Integer> TEMPERATURE_PARAMS = Map.ofEntries(
            Map.entry("cooling_release_temperature", 110), Map.entry("cooling_inlet_temperature", 132),
            Map.entry("warmwater_temperature", 2), Map.entry("heating_temperature", 1));

    public static final Map<String, Integer> HOUR_PARAMS = Map.ofEntries(Map.entry("cooling_stop_hours", 851),
            Map.entry("cooling_start_hours", 850));

    public static final Map<String, Integer> STRING_PARAMS = Map.ofEntries(Map.entry("warmwater_operation_mode", 4),
            Map.entry("heating_operation_mode", 3), Map.entry("cooling_operation_mode", 108));
}
