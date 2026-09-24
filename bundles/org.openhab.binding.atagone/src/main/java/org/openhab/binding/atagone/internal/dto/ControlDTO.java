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
public class ControlDTO {
    public int ch_status;
    public int ch_control_mode;
    public int ch_mode;
    public long ch_mode_duration;
    public double ch_mode_temp;
    public double dhw_temp_setp;
    public int dhw_status;
    public int dhw_mode;
    public double dhw_mode_temp;
    public double weather_temp;
    public int weather_status;
    public long vacation_duration;
    public long extend_duration;
    public long fireplace_duration;
}
