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
package org.openhab.binding.daikinairbase.internal.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container class for enums related to Daikin Airbase A/C systems
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 * @author Paul Smedley <paul@smedley.id.au> - Mods for Daikin Airbase Units
 *
 */
public class Enums {
    public enum Mode {
        UNKNOWN(-1),
        FAN(0),
        HEAT(1),
        COLD(2),
        DRY(7),
        AUTO(3);

        private static Logger LOGGER = LoggerFactory.getLogger(Mode.class);
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
        LEVEL_1("1"),
        LEVEL_2("3"),
        LEVEL_3("5");

        private static Logger LOGGER = LoggerFactory.getLogger(FanSpeed.class);
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

        private static Logger LOGGER = LoggerFactory.getLogger(FanMovement.class);
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
