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
package org.openhab.binding.daikin.internal.api.airbase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container class for enums related to Daikin Airbase A/C systems
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 * @author Paul Smedley <paul@smedley.id.au> - Mods for Daikin Airbase Units
 *
 */
public class AirbaseEnums {
    public enum AirbaseMode {
        UNKNOWN(-1),
        FAN(0),
        HEAT(1),
        COLD(2),
        DRY(7),
        AUTO(3);

        private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseMode.class);
        private final int value;

        AirbaseMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static AirbaseMode fromValue(int value) {
            for (AirbaseMode m : AirbaseMode.values()) {
                if (m.getValue() == value) {
                    return m;
                }
            }

            LOGGER.debug("Unexpected Mode value of \"{}\"", value);

            // Default to auto
            return AUTO;
        }
    }

    public enum AirbaseFanSpeed {
        AUTO("A"),
        LEVEL_1("1"),
        LEVEL_2("3"),
        LEVEL_3("5");

        private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseFanSpeed.class);
        private final String value;

        AirbaseFanSpeed(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static AirbaseFanSpeed fromValue(String value) {
            for (AirbaseFanSpeed m : AirbaseFanSpeed.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }

            LOGGER.debug("Unexpected FanSpeed value of \"{}\"", value);

            // Default to auto
            return AUTO;
        }
    }

    public enum AirbaseFanMovement {
        UNKNOWN(-1),
        STOPPED(0),
        VERTICAL(1),
        HORIZONTAL(2),
        VERTICAL_AND_HORIZONTAL(3);

        private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseFanMovement.class);
        private final int value;

        AirbaseFanMovement(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static AirbaseFanMovement fromValue(int value) {
            for (AirbaseFanMovement m : AirbaseFanMovement.values()) {
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
