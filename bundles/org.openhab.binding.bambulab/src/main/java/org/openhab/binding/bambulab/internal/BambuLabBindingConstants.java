/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
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
        public static final String NOZZLE_TEMPERATURE_CHANNEL = "nozzleTemperature";
        public static final String NOZZLE_TARGET_TEMPERATURE_CHANNEL = "nozzleTargetTemperature";
        public static final String BED_TEMPERATURE_CHANNEL = "bedTemperature";
        public static final String BED_TARGET_TEMPERATURE_CHANNEL = "bedTargetTemperature";
        public static final String CHAMBER_TEMPERATURE_CHANNEL = "chamberTemperature";
        public static final String MC_PRINT_STAGE_CHANNEL = "mcPrintStage";
        public static final String MC_PERCENT_CHANNEL = "mcPercent";
        public static final String MC_REMAINING_TIME_CHANNEL = "mcRemainingTime";
        public static final String WIFI_SIGNAL_CHANNEL = "wifiSignal";

        public static final String BED_TYPE_CHANNEL = "bedType";
        public static final String GCODE_fILE_CHANNEL = "gcodeFile";
        public static final String GCODE_STATE_CHANNEL = "gcodeState";
        public static final String REASON_CHANNEL = "reason";
        public static final String RESULT_CHANNEL = "result";
        public static final String GCODE_FILE_PREPARE_PERCENT_CHANNEL = "gcodeFilePreparePercent";
        public static final String BIG_FAN_1_SPEED_CHANNEL = "bigFan1Speed";
        public static final String BIG_FAN_2_SPEED_CHANNEL = "bigFan2Speed";
        public static final String HEAT_BREAK_FAN_SPEED_CHANNEL = "heatBreakFanSpeed";
        public static final String LAYER_NUM_CHANNEL = "layerNum";
        public static final String SPEED_LEVEL_CHANNEL = "speedLevel";
        public static final String TIMELAPS_CHANNEL = "timeLaps";
        public static final String USE_AMS_CHANNEL = "useAms";
        public static final String VIBRATION_CALIBRATION_CHANNEL     = "vibrationCalibration";
    }
}
