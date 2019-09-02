/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container class for enums related to Daikin A/C systems
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 *
 */
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
}
