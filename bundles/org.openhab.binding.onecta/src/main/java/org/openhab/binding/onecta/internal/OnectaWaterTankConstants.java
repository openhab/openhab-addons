/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OnectaWaterTankConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaWaterTankConstants {
    public static final String PROPERTY_HWT_NAME = "name";
    // List of all Channel ids
    public static final String CHANNEL_HWT_POWER = "basic#power";
    public static final String CHANNEL_HWT_ERRORCODE = "basic#error-code";
    public static final String CHANNEL_HWT_IS_HOLIDAY_MODE_ACTIVE = "basic#is-holiday-mode-active";
    public static final String CHANNEL_HWT_IS_IN_ERROR_STATE = "basic#is-in-error-state";
    public static final String CHANNEL_HWT_IS_IN_WARNING_STATE = "basic#is-in-warning-state";
    public static final String CHANNEL_HWT_IS_IN_INSTALLER_STATE = "basic#is-in-installer-state";
    public static final String CHANNEL_HWT_IS_IN_EMERGENCY_STATE = "basic#is-in-emergency-state";
    public static final String CHANNEL_HWT_POWERFUL_MODE = "basic#powerful-mode";
    public static final String CHANNEL_HWT_HEATUP_MODE = "basic#heatup-mode";
    public static final String CHANNEL_HWT_TANK_TEMPERATURE = "basic#tank-temperature";
    public static final String CHANNEL_HWT_OPERATION_MODE = "basic#operation-mode";
    public static final String CHANNEL_HWT_SETPOINT_MODE = "basic#setpoint-mode";
    public static final String CHANNEL_HWT_SETTEMP = "basic#setpoint-temp";
    public static final String CHANNEL_HWT_SETTEMP_MIN = "basic#setpoint-temp-min";
    public static final String CHANNEL_HWT_SETTEMP_MAX = "basic#setpoint-temp-max";
    public static final String CHANNEL_HWT_SETTEMP_STEP = "basic#setpoint-temp-step";
}
