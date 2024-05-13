/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

    // List of all Channel ids

    public static final String CHANNEL_HWT_POWER = "basic#power";
    public static final String CHANNEL_HWT_ERRORCODE = "basic#errorcode";
    public static final String CHANNEL_HWT_IS_HOLIDAY_MODE_ACTIVE = "basic#isholidaymodeactive";
    public static final String CHANNEL_HWT_IS_IN_ERROR_STATE = "basic#isinerrorstate";
    public static final String CHANNEL_HWT_IS_IN_WARNING_STATE = "basic#isinwarningstate";
    public static final String CHANNEL_HWT_IS_IN_INSTALLER_STATE = "basic#isininstallerstate";
    public static final String CHANNEL_HWT_IS_IN_EMERGENCY_STATE = "basic#isinemergencystate";
    public static final String CHANNEL_HWT_POWERFUL_MODE = "basic#powerfulmode";
    public static final String CHANNEL_HWT_HEATUP_MODE = "basic#heatupmode";
    public static final String CHANNEL_HWT_TANK_TEMPERATURE = "basic#tanktemperature";
    public static final String CHANNEL_HWT_SETPOINT_MODE = "basic#setpointmode";
    public static final String CHANNEL_HWT_SETTEMP = "basic#settemp";
    public static final String CHANNEL_HWT_SETTEMP_MIN = "basic#settempmin";
    public static final String CHANNEL_HWT_SETTEMP_MAX = "basic#settempmax";
    public static final String CHANNEL_HWT_SETTEMP_STEP = "basic#settempstep";
    public static final String PROPERTY_HWT_NAME = "name";
}
