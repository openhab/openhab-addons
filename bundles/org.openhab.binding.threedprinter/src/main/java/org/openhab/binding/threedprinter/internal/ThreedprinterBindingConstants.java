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
package org.openhab.binding.threedprinter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ThreedprinterBindingConstants} class defines all constants used across the binding.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class ThreedprinterBindingConstants {

    public static final String BINDING_ID = "threedprinter";

    // Thing types
    public static final ThingTypeUID THING_TYPE_PRUSAPRINTER = new ThingTypeUID(BINDING_ID, "prusaprinter");
    public static final ThingTypeUID THING_TYPE_KLIPPER = new ThingTypeUID(BINDING_ID, "klipper");
    public static final ThingTypeUID THING_TYPE_OCTOPRINT = new ThingTypeUID(BINDING_ID, "octoprint");

    // Channels
    public static final String CHANNEL_PRINTER_STATE = "printer-state";
    public static final String CHANNEL_JOB_NAME = "job-name";
    public static final String CHANNEL_JOB_PROGRESS = "job-progress";
    public static final String CHANNEL_TIME_ELAPSED = "time-elapsed";
    public static final String CHANNEL_TIME_REMAINING = "time-remaining";
    public static final String CHANNEL_NOZZLE_TEMPERATURE = "nozzle-temperature";
    public static final String CHANNEL_NOZZLE_TEMPERATURE_SETPOINT = "nozzle-temperature-setpoint";
    public static final String CHANNEL_BED_TEMPERATURE = "bed-temperature";
    public static final String CHANNEL_BED_TEMPERATURE_SETPOINT = "bed-temperature-setpoint";
    public static final String CHANNEL_PRINT_SPEED = "print-speed";
    public static final String CHANNEL_FAN_SPEED = "fan-speed";
    public static final String CHANNEL_PAUSE_RESUME = "pause-resume";
    public static final String CHANNEL_CANCEL = "cancel";
    public static final String CHANNEL_JOB_PREVIEW = "job-preview";

    // Printer state values
    public static final String STATE_IDLE = "IDLE";
    public static final String STATE_PRINTING = "PRINTING";
    public static final String STATE_PAUSED = "PAUSED";
    public static final String STATE_FINISHED = "FINISHED";
    public static final String STATE_ERROR = "ERROR";
    public static final String STATE_BUSY = "BUSY";
}
