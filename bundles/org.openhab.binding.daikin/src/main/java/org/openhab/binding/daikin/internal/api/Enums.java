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
package org.openhab.binding.daikin.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container class for enums related to Daikin A/C systems
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 * @author Lukas Agethen - Add special modes
 *
 */
@NonNullByDefault
public class Enums {
    public enum Mode {
        UNKNOWN(-1),
        AUTO(0),
        DEHUMIDIFIER(2),
        COLD(3),
        HEAT(4),
        FAN(6);

        private static final Logger LOGGER = LoggerFactory.getLogger(Mode.class);
        private final int value;

        Mode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Mode fromValue(int value) {
            for (Mode m : Mode.values()) {
                if (m.getValue() == value) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected Mode value of \"{}\"", value);

            // Default to auto
            return AUTO;
        }
    }

    public enum FanSpeed {
        AUTO("A"),
        SILENCE("B"),
        LEVEL_1("3"),
        LEVEL_2("4"),
        LEVEL_3("5"),
        LEVEL_4("6"),
        LEVEL_5("7");

        private static final Logger LOGGER = LoggerFactory.getLogger(FanSpeed.class);
        private final String value;

        FanSpeed(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
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

    public enum FanMovement {
        UNKNOWN(-1),
        STOPPED(0),
        VERTICAL(1),
        HORIZONTAL(2),
        VERTICAL_AND_HORIZONTAL(3);

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

    public enum HomekitMode {
        AUTO("auto"),
        COOL("cool"),
        HEAT("heat"),
        OFF("off");

        private static final Logger LOGGER = LoggerFactory.getLogger(HomekitMode.class);
        private final String value;

        HomekitMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum SpecialMode {
        STREAMER("13"),
        ECO("12"),
        POWERFUL("2"),
        POWERFUL_STREAMER("2/13"),
        ECO_STREAMER("12/13"),
        OFF(""),
        UNKNOWN("??");

        private static final Logger LOGGER = LoggerFactory.getLogger(SpecialMode.class);
        private final String value;

        SpecialMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public boolean isPowerfulActive() {
            return this.equals(POWERFUL) || this.equals(POWERFUL_STREAMER);
        }

        public boolean isUndefined() {
            return this.equals(UNKNOWN);
        }

        public static SpecialMode fromValue(String value) {
            for (SpecialMode m : SpecialMode.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected SpecialMode value of \"{}\"", value);

            // Default to UNKNOWN
            return UNKNOWN;
        }
    }

    public enum SpecialModeKind {
        UNKNOWN(-1),
        STREAMER(0),
        POWERFUL(1),
        ECO(2);

        private static final Logger LOGGER = LoggerFactory.getLogger(SpecialModeKind.class);
        private final int value;

        SpecialModeKind(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SpecialModeKind fromValue(int value) {
            for (SpecialModeKind m : SpecialModeKind.values()) {
                if (m.getValue() == value) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected SpecialModeKind value of \"{}\"", value);

            // Default to UNKNOWN
            return UNKNOWN;
        }
    }
}
