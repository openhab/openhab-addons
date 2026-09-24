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

/**
 * @author Florian Lettner - Initial contribution
 */
public class ReportDTO {
    public long report_time;
    public double burning_hours;
    public String device_errors = "";
    public String boiler_errors = "";
    public double room_temp;
    public double outside_temp;
    public double dbg_outside_temp;
    public double pcb_temp;
    public double ch_setpoint;
    public double dhw_water_temp;
    public double ch_water_temp;
    public double dhw_water_pres;
    public double ch_water_pres;
    public double ch_return_temp;
    public int boiler_status;
    public int boiler_config;
    public int ch_time_to_temp;
    public double shown_set_temp;
    public int power_cons;
    public double tout_avg;
    public int rssi;
    public int current;
    public int voltage;
    public int charge_status;
    public int lmuc_burner_starts;
    public double dhw_flow_rate;
    public int resets;
    public int memory_allocation;
    public ReportDetailsDTO details;
}
