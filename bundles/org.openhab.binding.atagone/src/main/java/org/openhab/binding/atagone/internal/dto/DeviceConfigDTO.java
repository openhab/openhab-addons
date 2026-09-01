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
    /** Minimum CH setpoint (°C). */
    public double ch_min_set;
    /** Maximum CH setpoint (°C). */
    public double ch_max_set;
    /** Minimum DHW setpoint (°C). */
    public double dhw_min_set;
    /** Maximum DHW setpoint (°C). */
    public double dhw_max_set;

    /** CH temperature setpoint during vacation (°C). */
    public double ch_vacation_temp;
    /** Vacation period start (ATAG epoch: seconds since 2000-01-01 UTC). */
    public long start_vacation;
    /** Default vacation mode duration (seconds). */
    public long ch_mode_vacation;
    /** Default extend mode duration (seconds). */
    public long ch_mode_extend;

    /** Frost protection mode: 0=off, 1=outdoor sensor only, 2=indoor sensor only, 3=both. */
    public int frost_prot_enabled;
    /** Frost protection room temperature threshold (°C). */
    public double frost_prot_temp_room;
    /** Summer eco mode enabled (1=on). */
    public int summer_eco_mode;

    /** Legionella protection enabled (1=on). */
    public int dhw_legion_enabled;
    /** Legionella cycle day of week (1=Monday … 7=Sunday). */
    public int dhw_legion_day;

    /** Display brightness (0–100). */
    public int disp_brightness;

    /** Boiler identifier string. */
    public String boiler_id = "";
}
