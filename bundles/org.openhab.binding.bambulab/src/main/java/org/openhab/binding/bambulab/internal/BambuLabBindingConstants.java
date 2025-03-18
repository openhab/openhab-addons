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
    public enum Channel {
        CHANNEL_NOZZLE_TEMPERATURE("nozzle-temperature"),
        CHANNEL_NOZZLE_TARGET_TEMPERATURE("nozzle-target-temperature"),
        CHANNEL_BED_TEMPERATURE("bed-temperature"),
        CHANNEL_BED_TARGET_TEMPERATURE("bed-target-temperature"),
        CHANNEL_CHAMBER_TEMPERATURE("chamber-temperature"),
        CHANNEL_MC_PRINT_STAGE("mc-print-stage"),
        CHANNEL_MC_PERCENT("mc-percent"),
        CHANNEL_MC_REMAINING_TIME("mc-remaining-time"),
        CHANNEL_WIFI_SIGNAL("wifi-signal"),
        CHANNEL_BED_TYPE("bed-type"),
        CHANNEL_GCODE_FILE("gcode-file"),
        CHANNEL_GCODE_STATE("gcode-state"),
        CHANNEL_REASON("reason"),
        CHANNEL_RESULT("result"),
        CHANNEL_GCODE_FILE_PREPARE_PERCENT("gcode-file-prepare-percent"),
        CHANNEL_BIG_FAN_1_SPEED("big-fan1-speed"),
        CHANNEL_BIG_FAN_2_SPEED("big-fan2-speed"),
        CHANNEL_HEAT_BREAK_FAN_SPEED("heat-break-fan-speed"),
        CHANNEL_LAYER_NUM("layer-num"),
        CHANNEL_SPEED_LEVEL("speed-level"),
        CHANNEL_TIME_LAPS("time-laps"),
        CHANNEL_USE_AMS("use-ams"),
        CHANNEL_VIBRATION_CALIBRATION("vibration-calibration"),
        CHANNEL_LED_CHAMBER_LIGHT("led-chamber", true),
        CHANNEL_LED_WORK_LIGHT("led-work", true);

        private final String name;
        private final boolean supportCommand;

        Channel(String name, boolean supportCommand) {
            this.name = name;
            this.supportCommand = supportCommand;
        }

        private Channel(String name) {
            this(name, false);
        }

        public String getName() {
            return name;
        }

        public boolean isSupportCommand() {
            return supportCommand;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
