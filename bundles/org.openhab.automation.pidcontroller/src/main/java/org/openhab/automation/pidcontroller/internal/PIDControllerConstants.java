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
package org.openhab.automation.pidcontroller.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Constants for PID controller.
 *
 * @author Fabian Wolter - Initial contribution
 *
 */
@NonNullByDefault
public class PIDControllerConstants {
    public static final String AUTOMATION_NAME = "pidcontroller";
    public static final String CONFIG_INPUT_ITEM = "input";
    public static final String CONFIG_SETPOINT_ITEM = "setpoint";
    public static final String CONFIG_COMMAND_ITEM = "commandItem";
    public static final String CONFIG_LOOP_TIME = "loopTime";
    public static final String CONFIG_KP_GAIN = "kp";
    public static final String CONFIG_KI_GAIN = "ki";
    public static final String CONFIG_KD_GAIN = "kd";
    public static final String CONFIG_KD_TIMECONSTANT = "kdTimeConstant";
    public static final String CONFIG_I_MAX = "integralMaxValue";
    public static final String CONFIG_I_MIN = "integralMinValue";
    public static final String P_INSPECTOR = "pInspector";
    public static final String I_INSPECTOR = "iInspector";
    public static final String D_INSPECTOR = "dInspector";
    public static final String E_INSPECTOR = "eInspector";
    public static final String COMMAND = "command";
}
