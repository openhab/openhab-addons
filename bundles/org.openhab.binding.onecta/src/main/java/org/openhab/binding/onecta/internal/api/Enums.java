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
package org.openhab.binding.onecta.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class Enums {
    public enum OnOff {
        ON("on"),
        OFF("off");

        private static final Logger LOGGER = LoggerFactory.getLogger(OnOff.class);
        private final String value;

        OnOff(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static OnOff fromValue(String value) {
            for (OnOff m : OnOff.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }

            LOGGER.debug("Unexpected OnOff value of \"{}\"", value);

            // Default to off
            return OFF;
        }
    }

    public enum OperationMode {
        UNKNOWN(""),
        AUTO("auto"),
        DEHUMIDIFIER("dry"),
        COLD("cooling"),
        HEAT("heating"),
        FAN("fanOnly");

        private static final Logger LOGGER = LoggerFactory.getLogger(OperationMode.class);
        private final String value;

        OperationMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static OperationMode fromValue(String value) {
            for (OperationMode m : OperationMode.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }

            LOGGER.debug("Unexpected Mode value of \"{}\"", value);

            // Default to auto
            return AUTO;
        }
    }

    public enum HeatupMode {
        UNKNOWN(""),
        REHEATONLY("reheatOnly"),
        SCHEDULEONLY("scheduleOnly"),
        REHEATSCHEDULE("reheatSchedule");

        private static final Logger LOGGER = LoggerFactory.getLogger(HeatupMode.class);
        private final String value;

        HeatupMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static HeatupMode fromValue(String value) {
            for (HeatupMode m : HeatupMode.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }

            LOGGER.debug("Unexpected Mode value of \"{}\"", value);

            // Default to unknown
            return UNKNOWN;
        }
    }

    public enum SetpointMode {
        UNKNOWN(""),
        WEATHERDEPENDENT("weatherDependent"),
        FIXED("fixed");

        private static final Logger LOGGER = LoggerFactory.getLogger(SetpointMode.class);
        private final String value;

        SetpointMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static SetpointMode fromValue(String value) {
            for (SetpointMode m : SetpointMode.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }

            LOGGER.debug("Unexpected Mode value of \"{}\"", value);

            // Default to unknown
            return UNKNOWN;
        }
    }

    public enum ManagementPoint {
        GATEWAY("gateway"),
        CLIMATECONTROL("climateControl"),
        INDOORUNIT("indoorUnit"),
        OUTDOORUNIT("outdoorUnit"),
        WATERTANK("domesticHotWaterTank");

        private final String value;

        ManagementPoint(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum SensorData {
        ROOMTEMP("roomTemperature"),
        ROOMHUMINITY("roomHumidity"),
        OUTDOORTEMP("outdoorTemperature"),
        LEAVINGWATERTEMP("leavingWaterTemperature");

        private final String value;

        SensorData(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum FanSpeed {
        AUTO("auto"),
        SILENCE("quiet"),
        LEVEL_1("fixed_1"),
        LEVEL_2("fixed_2"),
        LEVEL_3("fixed_3"),
        LEVEL_4("fixed_4"),
        LEVEL_5("fixed_5"),
        NOTAVAILABLE("notavailable");

        private static final Logger LOGGER = LoggerFactory.getLogger(FanSpeed.class);
        private final String value;
        private final String mode;
        private final Integer speed;

        FanSpeed(String value) {
            this.value = value;
            this.mode = value.split("_")[0];
            if (value.contains("_")) {
                this.speed = Integer.parseInt(value.split("_")[1]);
            } else {
                this.speed = 0;
            }
        }

        public String getValue() {
            return value;
        }

        public Integer getValueSpeed() {
            return speed;
        }

        public String getValueMode() {
            return mode;
        }

        public static FanSpeed fromValue(String value) {
            for (FanSpeed m : FanSpeed.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected FanSpeed value of \"{}\"", value);

            // Default to auto
            return AUTO;
        }
    }

    public enum FanSpeedMode {
        AUTO("auto"),
        SILENCE("quiet"),
        FIXED("fixed");

        private static final Logger LOGGER = LoggerFactory.getLogger(FanSpeedMode.class);
        private final String value;

        FanSpeedMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static FanSpeedMode fromValue(String value) {
            for (FanSpeedMode m : FanSpeedMode.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected FanSpeedMode value of \"{}\"", value);

            // Default to auto
            return AUTO;
        }
    }

    public enum FanMovementHor {
        STOPPED("stop"),
        SWING("swing"),
        NOTAVAILABLE("notavailable");

        private static final Logger LOGGER = LoggerFactory.getLogger(FanMovementHor.class);
        private final String value;

        FanMovementHor(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static FanMovementHor fromValue(@Nullable String value) {
            for (FanMovementHor m : FanMovementHor.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected FanMovementHor value of \"{}\"", value);

            // Default to notavailable
            return NOTAVAILABLE;
        }
    }

    public enum FanMovementVer {
        STOPPED("stop"),
        SWING("swing"),
        WINDNICE("windNice"),
        NOTAVAILABLE("notavailable");

        private static final Logger LOGGER = LoggerFactory.getLogger(FanMovementVer.class);
        private final String value;

        FanMovementVer(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static FanMovementVer fromValue(@Nullable String value) {
            for (FanMovementVer m : FanMovementVer.values()) {
                if (m.getValue().equalsIgnoreCase(value)) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected FanMovementVer value of \"{}\"", value);

            // Default to notavailable
            return NOTAVAILABLE;
        }
    }

    public enum FanMovement {
        UNKNOWN(-1),
        STOPPED(0),
        VERTICAL(1),
        VERTICAL_EXTRA(4),
        HORIZONTAL(2),
        VERTICAL_AND_HORIZONTAL(3),
        VERTICAL_AND_HORIZONTAL_EXTRA(5);

        private static final Logger LOGGER = LoggerFactory.getLogger(FanMovement.class);
        private final int value;

        FanMovement(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static FanMovement fromValue(int value) {
            for (FanMovement m : FanMovement.values()) {
                if (m.getValue() == value) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected FanMovement value of \"{}\"", value);

            // Default to stopped
            return STOPPED;
        }
    }

    public enum DemandControl {

        OFF("off"),
        AUTO("auto"),
        FIXED("fixed"),
        SCHEDULED("scheduled");

        private static final Logger LOGGER = LoggerFactory.getLogger(DemandControl.class);
        private final String value;

        DemandControl(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static DemandControl fromValue(String value) {
            for (DemandControl m : DemandControl.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected DemandControl value of \"{}\"", value);

            // Default to off
            return OFF;
        }
    }

    public enum AdvancedMode {
        STREAMER("13"),
        ECO("12"),
        POWERFUL("2"),
        POWERFUL_STREAMER("2/13"),
        ECO_STREAMER("12/13"),
        OFF(""),
        UNKNOWN("??");

        private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedMode.class);
        private final String value;

        AdvancedMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public boolean isStreamerActive() {
            return this.equals(STREAMER) || this.equals(POWERFUL_STREAMER) || this.equals(ECO_STREAMER);
        }

        public boolean isUndefined() {
            return this.equals(UNKNOWN);
        }

        public static AdvancedMode fromValue(String value) {
            for (AdvancedMode m : AdvancedMode.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected AdvancedMode value of \"{}\"", value);

            // Default to UNKNOWN
            return UNKNOWN;
        }
    }

    public enum SpecialMode {
        NORMAL("0"),
        POWERFUL("1"),
        ECO("2");

        private final String value;

        SpecialMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static SpecialMode fromAdvancedMode(AdvancedMode advMode) {
            switch (advMode) {
                case POWERFUL:
                case POWERFUL_STREAMER:
                    return SpecialMode.POWERFUL;
                case ECO:
                case ECO_STREAMER:
                    return SpecialMode.ECO;
            }
            // default to normal
            return NORMAL;
        }
    }
}
