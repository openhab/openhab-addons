/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.automation.pwm.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants for the PWM automation module.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class PWMConstants {
    public static final String AUTOMATION_NAME = "pwm";

    public static final String CONFIG_DUTY_CYCLE_ITEM = "dutycycleItem";
    public static final String CONFIG_PERIOD = "interval";
    public static final String CONFIG_MIN_DUTYCYCLE = "minDutycycle";
    public static final String CONFIG_MAX_DUTYCYCLE = "maxDutycycle";
    public static final String CONFIG_COMMAND_ITEM = "command";
    public static final String CONFIG_DEAD_MAN_SWITCH = "deadManSwitch";
    public static final String CONFIG_OUTPUT_ITEM = "outputItem";
    public static final String INPUT = "input";
    public static final String OUTPUT = "command";
}
