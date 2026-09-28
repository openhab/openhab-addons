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
public class DeviceConfigDTO {
    public double ch_min_set;
    public double ch_max_set;
    public double dhw_min_set;
    public double dhw_max_set;
    public double ch_temp_max;
    public double ch_vacation_temp;
    public long start_vacation;
    public long ch_mode_vacation;
    public long ch_mode_extend;
    public int frost_prot_enabled;
    public double frost_prot_temp_outs;
    public double frost_prot_temp_room;
    public int summer_eco_mode;
    public double summer_eco_temp;
    public int dhw_legion_enabled;
    public int dhw_legion_day;
    public int dhw_legion_time;
    public int disp_brightness;
    public int language;
    public int temp_unit;
    public int pressure_unit;
    public int time_format;
    public int time_zone;
    public double room_temp_offs;
    public double outs_temp_offs;
    public double wd_k_factor;
    public double wd_exponent;
    public double wd_temp_offs;
    public int wdr_temps_influence;
    public double climate_zone;
    public int privacy_mode;
    public String boiler_id = "";
    public String installer_id = "";
    public int boiler_det_type;
    public int dhw_boiler_cap;
    public int max_preheat;
    public int ch_building_size;
    public int ch_heating_type;
    public int ch_isolation;
    public double mu;
    public int shower_time_mode;
    public int comfort_settings;
    public String report_url = "";
    public String download_url = "";
    public String support_contact = "";
}
