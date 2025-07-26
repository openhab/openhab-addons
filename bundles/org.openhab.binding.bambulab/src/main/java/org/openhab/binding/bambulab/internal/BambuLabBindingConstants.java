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

import static java.util.Arrays.stream;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
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

    // misc consts
    public static final String NO_CAMERA_CERT = "none";

    // List of all Thing Type UIDs
    public static final ThingTypeUID PRINTER_THING_TYPE = new ThingTypeUID(BINDING_ID, "printer");
    public static final ThingTypeUID AMS_THING_TYPE = new ThingTypeUID(BINDING_ID, "ams-device");

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public enum PrinterChannel {
        CHANNEL_COMMAND("command", true),
        CHANNEL_NOZZLE_TEMPERATURE("nozzle-temperature"),
        CHANNEL_NOZZLE_TARGET_TEMPERATURE("nozzle-target-temperature"),
        CHANNEL_BED_TEMPERATURE("bed-temperature"),
        CHANNEL_BED_TARGET_TEMPERATURE("bed-target-temperature"),
        CHANNEL_CHAMBER_TEMPERATURE("chamber-temperature"),
        CHANNEL_MC_PRINT_STAGE("mc-print-stage"),
        CHANNEL_MC_PERCENT("mc-percent"),
        CHANNEL_MC_REMAINING_TIME("mc-remaining-time"),
        CHANNEL_END_DATE("end-date"),
        CHANNEL_WIFI_SIGNAL("wifi-signal"),
        CHANNEL_BED_TYPE("bed-type"),
        CHANNEL_GCODE_FILE("gcode-file", true),
        CHANNEL_GCODE_STATE("gcode-state"),
        CHANNEL_REASON("reason"),
        CHANNEL_RESULT("result"),
        CHANNEL_GCODE_FILE_PREPARE_PERCENT("gcode-file-prepare-percent"),
        CHANNEL_BIG_FAN_1_SPEED("big-fan1-speed"),
        CHANNEL_BIG_FAN_2_SPEED("big-fan2-speed"),
        CHANNEL_HEAT_BREAK_FAN_SPEED("heat-break-fan-speed"),
        CHANNEL_LAYER_NUM("layer-num"),
        CHANNEL_SPEED_LEVEL("speed-level", true),
        CHANNEL_TIME_LAPS("time-laps"),
        CHANNEL_USE_AMS("use-ams"),
        CHANNEL_VIBRATION_CALIBRATION("vibration-calibration"),
        CHANNEL_CAMERA_RECORD("camera-record", true),
        CHANNEL_CAMERA_IMAGE("camera-image"),
        // vtray
        CHANNEL_VTRAY_TRAY_TYPE("vtray-tray-type"),
        CHANNEL_VTRAY_TRAY_COLOR("vtray-tray-color"),
        CHANNEL_VTRAY_NOZZLE_TEMPERATURE_MAX("vtray-nozzle-temperature-max"),
        CHANNEL_VTRAY_NOZZLE_TEMPERATURE_MIN("vtray-nozzle-temperature-min"),
        CHANNEL_VTRAY_REMAIN("vtray-remain"),
        CHANNEL_VTRAY_K("vtray-k"),
        CHANNEL_VTRAY_N("vtray-n"),
        CHANNEL_VTRAY_TAG_UUID("vtray-tag-uuid"),
        CHANNEL_VTRAY_TRAY_ID_NAME("vtray-tray-id-name"),
        CHANNEL_VTRAY_TRAY_INFO_IDX("vtray-tray-info-idx"),
        CHANNEL_VTRAY_TRAY_SUB_BRANDS("vtray-tray-sub-brands"),
        CHANNEL_VTRAY_TRAY_WEIGHT("vtray-tray-weight"),
        CHANNEL_VTRAY_TRAY_DIAMETER("vtray-tray-diameter"),
        CHANNEL_VTRAY_TRAY_TEMPERATURE("vtray-tray-temperature"),
        CHANNEL_VTRAY_TRAY_TIME("vtray-tray-time"),
        CHANNEL_VTRAY_BED_TEMPERATURE_TYPE("vtray-bed-temp-type"),
        CHANNEL_VTRAY_BED_TEMPERATURE("vtray-bed-temperature"),
        // AMS generic
        CHANNEL_AMS_TRAY_NOW("ams-tray-now"),
        CHANNEL_AMS_TRAY_PREVIOUS("ams-tray-previous"),
        // leds
        CHANNEL_LED_CHAMBER_LIGHT("led-chamber", true),
        CHANNEL_LED_WORK_LIGHT("led-work", true);

        private final String name;
        private final boolean supportCommand;

        PrinterChannel(String name, boolean supportCommand) {
            this.name = name;
            this.supportCommand = supportCommand;
        }

        private PrinterChannel(String name) {
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

        public boolean is(ChannelUID channelUID) {
            return name.equals(channelUID.getId());
        }

        public static Optional<PrinterChannel> findChannel(ChannelUID channel) {
            return stream(values())//
                    .filter(c -> c.is(channel))//
                    .findAny();
        }
    }

    public enum AmsChannel {
        CHANNEL_TRAY_TYPE("ams-tray-type", "tray_type"),
        CHANNEL_TRAY_COLOR("ams-tray-color", "tray_color"),
        CHANNEL_NOZZLE_TEMPERATURE_MAX("ams-nozzle-temperature-max", "nozzle_temp_max"),
        CHANNEL_NOZZLE_TEMPERATURE_MIN("ams-nozzle-temperature-min", "nozzle_temp_min"),
        CHANNEL_REMAIN("ams-remain", "remain"),
        CHANNEL_K("ams-k", "k"),
        CHANNEL_N("ams-n", "n"),
        CHANNEL_TAG_UUID("ams-tag-uuid", "tag_uuid"),
        CHANNEL_TRAY_ID_NAME("ams-tray-id-name", "tray_id_name"),
        CHANNEL_TRAY_INFO_IDX("ams-tray-info-idx", "tray_info_idx"),
        CHANNEL_TRAY_SUB_BRANDS("ams-tray-sub-brands", "tray_sub_brands"),
        CHANNEL_TRAY_WEIGHT("ams-tray-weight", "tray_weight"),
        CHANNEL_TRAY_DIAMETER("ams-tray-diameter", "tray_diameter"),
        CHANNEL_TRAY_TEMPERATURE("ams-tray-temperature", "tray_temp"),
        CHANNEL_TRAY_TIME("ams-tray-time", "tray_time"),
        CHANNEL_BED_TEMPERATURE_TYPE("ams-bed-temp-type", "bed_temp_type"),
        CHANNEL_BED_TEMPERATURE("ams-bed-temperature", "bed_temp"),
        CHANNEL_CTYPE("ams-ctype", "ctype");

        public static final int MIN_AMS = 1;
        /**
         * According to Bambu Lab documentation, you can attach up to 4 AMS
         */
        public static final int MAX_AMS = 4;
        private final String group;
        private final String jsonKey;

        AmsChannel(String group, String jsonKey) {
            this.group = group;
            this.jsonKey = jsonKey;
        }

        public String getJsonKey() {
            return jsonKey;
        }

        public String findType(TrayId trayId) {
            return "ams-tray-%s#%s".formatted(trayId.getIdx(), group);
        }

        public static Optional<AmsChannel> findAmsChannel(ChannelUID channel) {
            return Optional.of(channel)//
                    .map(ChannelUID::getGroupId)//
                    .flatMap(group -> stream(values()).filter(c -> c.group.equals(group)).findAny());
        }

        public static enum TrayId {
            TRAY_1(1),
            TRAY_2(2),
            TRAY_3(3),
            TRAY_4(4);

            /**
             * Each AMS device has 4 trays
             */
            public static final int MAX_AMS_TRAYS = values().length;

            private final int idx;

            TrayId(int idx) {
                this.idx = idx;
            }

            public int getIdx() {
                return idx;
            }

            public static Optional<TrayId> parseFromApi(int idx) {
                // tray ID in api starts from 0 and for channels it starts for 1
                return switch (idx) {
                    case 0 -> Optional.of(TRAY_1);
                    case 1 -> Optional.of(TRAY_2);
                    case 2 -> Optional.of(TRAY_3);
                    case 3 -> Optional.of(TRAY_4);
                    default -> Optional.empty();
                };
            }
        }

        public static enum TrayType {
            PLA,
            PETG,
            ABS,
            TPU,
            ASA,
            PA,
            PC,
            PVA,
            HIPS;

            public static Optional<TrayType> findTrayType(String name) {
                return stream(values())//
                        .filter(t -> t.name().equalsIgnoreCase(name))//
                        .findAny();
            }
        }
    }
}
