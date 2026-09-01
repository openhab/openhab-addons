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
 * Gson DTO for the {@code control} block in a {@code retrieve_reply}.
 * <p>
 * CH mode values: 1=manual, 2=auto, 3=holiday/vacation, 4=extend, 5=fireplace.
 * CH control mode: 0=room (room-sensor setpoint control), 1=weather (weather-compensated heating
 * curve, no room setpoint) — independent of ch_mode.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class ControlDTO {
    /** Room vs. weather-compensated control (0=room, 1=weather) — independent of ch_mode. */
    public int ch_control_mode;
    /** Current preset mode (1=manual, 2=auto, 3=vacation, 4=extend, 5=fireplace, 6=standby). */
    public int ch_mode;
    /** Remaining duration of the active preset (seconds). */
    public long ch_mode_duration;
    /** Temperature setpoint for the active preset (°C). */
    public double ch_mode_temp;
    /** DHW temperature setpoint (°C). */
    public double dhw_temp_setp;
    /** DHW operating mode. */
    public int dhw_mode;
    /** Current weather temperature (°C) — reported in the control block, not report. */
    public double weather_temp;
    /** Weather status code (firmware-defined integer). */
    public int weather_status;
    /** Vacation period duration (seconds). */
    public long vacation_duration;
    /** Extend mode duration (seconds). */
    public long extend_duration;
    /** Fireplace mode duration (seconds). */
    public long fireplace_duration;
}
