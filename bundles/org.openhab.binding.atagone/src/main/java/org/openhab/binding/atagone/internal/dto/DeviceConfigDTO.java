/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atagone.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Gson DTO for the {@code configuration} block in a {@code retrieve_reply}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class DeviceConfigDTO {
    // ── Temperature setpoint limits ───────────────────────────────────────────
    /** Minimum CH setpoint (°C). */
    public double ch_min_set;
    /** Maximum CH setpoint (°C). */
    public double ch_max_set;
    /** Minimum DHW setpoint (°C). */
    public double dhw_min_set;
    /** Maximum DHW setpoint (°C). */
    public double dhw_max_set;
    /** Maximum boiler temperature for CH (°C). */
    public double ch_temp_max;

    // ── Vacation / mode defaults ──────────────────────────────────────────────
    /** CH temperature setpoint during vacation (°C). */
    public double ch_vacation_temp;
    /** Vacation period start (ATAG epoch: seconds since 2000-01-01 UTC). */
    public long start_vacation;
    /** Default vacation mode duration (seconds). */
    public long ch_mode_vacation;
    /** Default extend mode duration (seconds). */
    public long ch_mode_extend;

    // ── Frost / summer protection ─────────────────────────────────────────────
    /**
     * Frost protection mode: 0=off, 1=outdoor sensor only, 2=indoor sensor only,
     * 3=indoor + outdoor sensors.
     */
    public int frost_prot_enabled;
    /** Frost protection outdoor temperature threshold (°C). */
    public double frost_prot_temp_outs;
    /** Frost protection room temperature threshold (°C). */
    public double frost_prot_temp_room;
    /** Summer eco mode enabled (1=on). */
    public int summer_eco_mode;
    /** Summer eco mode activation temperature (°C). */
    public double summer_eco_temp;

    // ── Legionella protection ────────────────────────────────────────────────
    /** Legionella protection enabled (1=on). */
    public int dhw_legion_enabled;
    /** Legionella cycle day of week (1=Monday … 7=Sunday). */
    public int dhw_legion_day;
    /** Legionella cycle start time (minutes since midnight). */
    public int dhw_legion_time;

    // ── Display / UI settings ────────────────────────────────────────────────
    /** Display brightness (0–100). */
    public int disp_brightness;
    /**
     * Display language code (device-defined integer; not mapped to locale strings
     * by the binding).
     */
    public int language;
    /** Temperature unit: 0=°C, 1=°F. */
    public int temp_unit;
    /** Pressure unit: 0=bar, 1=psi. */
    public int pressure_unit;
    /** Clock format: 0=24h, 1=12h. */
    public int time_format;
    /** Time zone offset (device-defined). */
    public int time_zone;

    // ── Temperature calibration offsets ──────────────────────────────────────
    /** Room temperature offset calibration (°C). */
    public double room_temp_offs;
    /** Outside temperature offset calibration (°C). */
    public double outs_temp_offs;

    // ── Weather-dependent regulation (WDR) ───────────────────────────────────
    /** WDR k-factor (also in report.details). */
    public double wd_k_factor;
    /** WDR exponent (also in report.details). */
    public double wd_exponent;
    /** WDR temperature offset (°C). */
    public double wd_temp_offs;
    /**
     * WDR room temperature influence:
     * 0=off, 1=less, 2=average, 3=more, 4=room_regulation.
     */
    public int wdr_temps_influence;
    /** Climate zone (WDR parameter). */
    public double climate_zone;

    // ── Boiler / installer configuration ─────────────────────────────────────
    /** Privacy mode (1=on — disables cloud reporting). */
    public int privacy_mode;
    /** Boiler identifier string. */
    public String boiler_id = "";
    /** Installer identifier string. */
    public String installer_id = "";
    /** Boiler detection type (firmware-internal). */
    public int boiler_det_type;
    /** DHW boiler capacity (kW). */
    public int dhw_boiler_cap;
    /** Maximum pre-heat time (minutes). */
    public int max_preheat;

    // ── Building characteristics ──────────────────────────────────────────────
    /**
     * Building size: 1=small, 2=medium, 3=large.
     * Used by WDR algorithm.
     */
    public int ch_building_size;
    /**
     * Heating system type: 1=air_heating, 2=convector, 3=radiator,
     * 4=radiator_underfloor, 5=underfloor, 6=underfloor_radiator.
     */
    public int ch_heating_type;
    /** Insulation quality: 1=poor, 2=average, 3=good. */
    public int ch_isolation;
    /** Regulation mu parameter (also in report.details). */
    public double mu;

    // ── Shower / comfort settings ─────────────────────────────────────────────
    /** Shower time mode (device-defined). */
    public int shower_time_mode;
    /** Comfort settings bitmask (device-defined). */
    public int comfort_settings;

    // ── Service / cloud URLs (read-only, do not write) ───────────────────────
    /** Cloud report upload URL (read-only). */
    public String report_url = "";
    /**
     * Firmware download URL — the firmware version string is embedded in the
     * path (read-only).
     */
    public String download_url = "";
    /** Support contact URL or phone (read-only). */
    public String support_contact = "";
}
