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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Gson DTO for the {@code configuration} block in an {@code update_message}.
 * Only non-null fields are serialized by Gson.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class DeviceConfigUpdateDTO {
    /** Vacation start in ATAG epoch (seconds since 2000-01-01 UTC). */
    public @Nullable Long start_vacation;
    public @Nullable Double ch_vacation_temp;
    public @Nullable Integer frost_prot_enabled;
    public @Nullable Double frost_prot_temp_room;
    public @Nullable Integer summer_eco_mode;
    public @Nullable Double summer_eco_temp;
    public @Nullable Integer dhw_legion_enabled;
    public @Nullable Integer disp_brightness;
    public @Nullable Double room_temp_offs;
    public @Nullable Double outs_temp_offs;
    public @Nullable Integer privacy_mode;
    public @Nullable Integer ch_heating_type;
    public @Nullable Integer ch_isolation;
    public @Nullable Integer ch_building_size;
    public @Nullable Integer wdr_temps_influence;
    public @Nullable Double climate_zone;
    public @Nullable Double wd_temp_offs;
    public @Nullable Double frost_prot_temp_outs;
    public @Nullable Integer max_preheat;
    public @Nullable Long ch_mode_vacation;
    public @Nullable Long ch_mode_extend;
    public @Nullable Integer time_zone;
    public @Nullable Integer dhw_legion_day;
    public @Nullable Integer dhw_legion_time;

    /** True if at least one field has been set and this update carries a change to send. */
    public boolean hasChanges() {
        return start_vacation != null || ch_vacation_temp != null || frost_prot_enabled != null
                || frost_prot_temp_room != null || summer_eco_mode != null || summer_eco_temp != null
                || dhw_legion_enabled != null || disp_brightness != null || room_temp_offs != null
                || outs_temp_offs != null || privacy_mode != null || ch_heating_type != null || ch_isolation != null
                || ch_building_size != null || wdr_temps_influence != null || climate_zone != null
                || wd_temp_offs != null || frost_prot_temp_outs != null || max_preheat != null
                || ch_mode_vacation != null || ch_mode_extend != null || time_zone != null || dhw_legion_day != null
                || dhw_legion_time != null;
    }
}
