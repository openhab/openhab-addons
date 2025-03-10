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
package org.openhab.binding.bambulab.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BambuLabBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class BambuLabBindingConstants {

    public static final String BINDING_ID = "bambulab";

    // List of all Thing Type UIDs
    public static final ThingTypeUID PRINTER_THING_TYPE = new ThingTypeUID(BINDING_ID, "printer");

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public interface Channel {
        // PrintDetails
        public static final String CHANNEL_NOZZLE_TEMPERATURE = "nozzle-temperature";
        public static final String CHANNEL_NOZZLE_TARGET_TEMPERATURE = "nozzle-target-temperature";
        public static final String CHANNEL_BED_TEMPERATURE = "bed-temperature";
        public static final String CHANNEL_BED_TARGET_TEMPERATURE = "bed-target-temperature";
        public static final String CHANNEL_CHAMBER_TEMPERATURE = "chamber-temperature";
        public static final String CHANNEL_MC_PRINT_STAGE = "mc-print-stage";
        public static final String CHANNEL_MC_PERCENT = "mc-percent";
        public static final String CHANNEL_MC_REMAINING_TIME = "mc-remaining-time";
        public static final String CHANNEL_WIFI_SIGNAL = "wifi-signal";
        public static final String CHANNEL_BED_TYPE = "bed-type";
        public static final String CHANNEL_GCODE_FILE = "gcode-file";
        public static final String CHANNEL_GCODE_STATE = "gcode-state";
        public static final String CHANNEL_REASON = "reason";
        public static final String CHANNEL_RESULT = "result";
        public static final String CHANNEL_GCODE_FILE_PREPARE_PERCENT = "gcode-file-prepare-percent";
        public static final String CHANNEL_BIG_FAN_1_SPEED = "big-fan1-speed";
        public static final String CHANNEL_BIG_FAN_2_SPEED = "big-fan2-speed";
        public static final String CHANNEL_HEAT_BREAK_FAN_SPEED = "heat-break-fan-speed";
        public static final String CHANNEL_LAYER_NUM = "layer-num";
        public static final String CHANNEL_SPEED_LEVEL = "speed-level";
        public static final String CHANNEL_TIME_LAPS = "time-laps";
        public static final String CHANNEL_USE_AMS = "use-ams";
        public static final String CHANNEL_VIBRATION_CALIBRATION = "vibration-calibration";
        // command channels
        public static final String CHANNEL_LED_CHAMBER_LIGHT = "led-chamber";
        public static final String CHANNEL_LED_WORK_LIGHT = "led-work";
    }
}
