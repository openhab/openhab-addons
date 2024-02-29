/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.octoprint.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OctoPrintBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim-Niclas Ruppert - Initial contribution
 */
@NonNullByDefault
public class OctoPrintBindingConstants {

    private static final String BINDING_ID = "octoprint";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OCTOPRINT = new ThingTypeUID(BINDING_ID, "octoprint");

    // List of all Channel ids
    public static final String SERVER_VERSION = "server_version";
    public static final String SERVER_SAFE_MODE_STATE = "server_safe_mode_state";
    public static final String SERVER_CONNECTION_STATE = "server_connection_state";
    public static final String PRINT_JOB_STATE = "print_job_state";
    public static final String PRINT_JOB_FILE_NAME = "print_job_file_name";
    public static final String PRINT_JOB_FILE_ORIGIN = "print_job_file_origin";
    public static final String PRINT_JOB_FILE_SIZE = "print_job_file_size";
    public static final String PRINT_JOB_FILE_DATE = "print_job_file_date";
    public static final String PRINT_JOB_START = "print_job_start";
    public static final String PRINT_JOB_CANCEL = "print_job_cancel";
    public static final String PRINT_JOB_PAUSE = "print_job_pause";
    public static final String PRINT_JOB_RESTART = "print_job_restart";
    public static final String PRINT_JOB_ESTIMATED_PRINT_TIME = "print_job_estimated_print_time";
    public static final String PRINT_JOB_PROGRESS = "print_job_progress";
    public static final String PRINT_JOB_CURRENT_PRINT_TIME = "print_job_current_print_time";
    public static final String PRINT_JOB_ESTIMATED_PRINT_TIME_LEFT = "print_job_estimated_print_time_left";
    public static final String PRINTER_STATE = "printer_state";
    public static final String PRINTER_HOMING = "printer_homing";
    public static final String PRINTER_TOOL_TEMP_ACTUAL = "printer_tool_temp_actual";
    public static final String PRINTER_TOOL_TEMP_TARGET = "printer_tool_temp_target";
    public static final String PRINTER_BED_TEMP_ACTUAL = "printer_bed_temp_actual";
    public static final String PRINTER_BED_TEMP_TARGET = "printer_bed_temp_target";
    public static final String PRINTER_CHAMBER_TEMP_ACTUAL = "printer_chamber_temp_actual";
    public static final String PRINTER_CHAMBER_TEMP_TARGET = "printer_chamber_temp_target";
    public static final String PRINTER_SD_STATE = "print_sd_state";
}
