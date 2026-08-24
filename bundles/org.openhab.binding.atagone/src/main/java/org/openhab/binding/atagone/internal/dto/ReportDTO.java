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
 * Gson DTO for the {@code report} block in a {@code retrieve_reply}.
 * <p>
 * Boiler status bitmask ({@code boiler_status}) bit positions:
 * 0x004 = CH active, 0x008 = burner commanded on, 0x010 = DHW active, 0x100 = flame sensor.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class ReportDTO {
    /** Report timestamp (ATAG epoch: seconds since 2000-01-01 UTC). */
    public long report_time;
    /** Total burner run hours. */
    public double burning_hours;
    /** Active device error codes (comma-separated string or empty). */
    public String device_errors = "";
    /** Active boiler error codes (comma-separated string or empty). */
    public String boiler_errors = "";
    /** Current room temperature (°C). */
    public double room_temp;
    /** Outside temperature sensor (°C) — may be negative; signed arithmetic required. */
    public double outside_temp;
    /** Debug outside temperature reading (°C). */
    public double dbg_outside_temp;
    /** PCB (circuit board) temperature (°C). */
    public double pcb_temp;
    /** Active CH setpoint sent to boiler (°C). */
    public double ch_setpoint;
    /** DHW water temperature (°C). */
    public double dhw_water_temp;
    /** CH circuit water temperature (°C). */
    public double ch_water_temp;
    /** DHW circuit water pressure (bar). */
    public double dhw_water_pres;
    /** CH circuit water pressure (bar). */
    public double ch_water_pres;
    /** CH circuit return temperature (°C). */
    public double ch_return_temp;
    /**
     * Boiler status bitmask.
     * 0x004=CH active, 0x008=burner commanded on, 0x010=DHW active, 0x100=flame sensor.
     */
    public int boiler_status;
    /** Boiler configuration bitmask (firmware-internal). */
    public int boiler_config;
    /** Estimated seconds until room reaches target temperature. */
    public int ch_time_to_temp;
    /** Temperature shown on the thermostat display (°C). */
    public double shown_set_temp;
    /** Instantaneous power consumption (W). */
    public int power_cons;
    /** Average outside temperature (°C). */
    public double tout_avg;
    /** WiFi signal strength (device-relative units). */
    public int rssi;
    /** Supply current (device-internal units). */
    public int current;
    /** Supply voltage (device-internal units, likely mV). */
    public int voltage;
    /** Charge status (firmware-internal). */
    public int charge_status;
    /** Burner start count since last reset. */
    public int lmuc_burner_starts;
    /** DHW flow rate (L/min). */
    public double dhw_flow_rate;
    /** Controller reset count. */
    public int resets;
    /** Memory allocation indicator (firmware-internal). */
    public int memory_allocation;
    /** Extended boiler diagnostics (regulation internals). */
    public ReportDetailsDTO details;
}
