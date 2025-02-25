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
        public static final String COMMAND_CHANNEL = "command";
        public static final String MESSAGE_CHANNEL = "message";
        public static final String SEQUENCE_ID_CHANNEL = "sequenceId";

        public static final String UPGRADE_STATE_CHANNEL = "upgradeState";
        public static final String IPCAM_CHANNEL = "ipcam";
        public static final String XCAM_CHANNEL = "xcam";
        public static final String UPLOAD_CHANNEL = "upload";
        public static final String NETWORK_CHANNEL = "net";
        public static final String IPCAM_DEV_CHANNEL = "ipcamDev";
        public static final String BUILDPLATE_MARKER_DETECTOR_CHANNEL = "buildplateMarkerDetector";
        public static final String UPLOAD_STATUS_CHANNEL = "uploadStatus";
        public static final String UPLOAD_PROGRESS_CHANNEL = "uploadProgress";
        public static final String UPLOAD_MESSAGE_CHANNEL = "uploadMessage";
        public static final String NETWORK_CONFIGURATION_CHANNEL = "netConf";
        public static final String IPCAM_RECORD_CHANNEL = "ipcamRecord";
        public static final String TIMELAPSE_CHANNEL = "timelapse";
        public static final String RESOLUTION_CHANNEL = "resolution";
        public static final String TUTK_SERVER_CHANNEL = "tutkServer";
        public static final String MODE_BITS_CHANNEL = "modeBits";
    }
}
