/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container class for enums related to Daikin Airbase A/C systems
 *
 * @author Tim Waterhouse - Initial contribution
 * @author Paul Smedley - Mods for Daikin Airbase Units
 *
 */
@NonNullByDefault
public class AirbaseEnums {
    public enum AirbaseMode {
        COLD(2, "Cooling"),
        HEAT(1, "Heating"),
        FAN(0, "Fan"),
        DRY(7, "Dehumidifier"),
        AUTO(3, "Auto");

        private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseMode.class);
        private final int value;
        private final String label;

        AirbaseMode(int value, String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public static AirbaseMode fromValue(int value) {
            for (AirbaseMode m : AirbaseMode.values()) {
                if (m.getValue() == value) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected Mode value of \"{}\"", value);
            return AUTO;
        }
    }

    public enum AirbaseFanSpeed {
        // level,f_auto,f_airside
        AUTO(0, false, false),
        LEVEL_1(1, false, false),
        LEVEL_2(2, false, false),
        LEVEL_3(3, false, false),
        LEVEL_4(4, false, false),
        LEVEL_5(5, false, false),
        AUTO_LEVEL_1(1, true, false),
        AUTO_LEVEL_2(2, true, false),
        AUTO_LEVEL_3(3, true, false),
        AUTO_LEVEL_4(4, true, false),
        AUTO_LEVEL_5(5, true, false),
        AIRSIDE(1, false, true);

        private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseFanSpeed.class);
        private final int level;
        private final boolean auto;
        private final boolean airside;

        AirbaseFanSpeed(int level, boolean auto, boolean airside) {
            this.level = level;
            this.auto = auto;
            this.airside = airside;
        }

        public int getLevel() {
            return level;
        }

        public boolean getAuto() {
            return auto;
        }

        public boolean getAirside() {
            return airside;
        }

        public String getLabel() {
            if (airside) {
                return "Airside";
            }
            if (level == 0) {
                return "Auto";
            }
            String label = "";
            if (auto) {
                label = "Auto ";
            }
            return label + "Level " + Integer.toString(level);
        }

        public static AirbaseFanSpeed fromValue(int rate, boolean auto, boolean airside) { // convert from f_rate,
                                                                                           // f_auto, f_airside
            if (airside) {
                return AIRSIDE;
            }
            if (rate == 0) {
                return AirbaseFanSpeed.AUTO;
            }
            for (AirbaseFanSpeed m : AirbaseFanSpeed.values()) {
                if (m.getLevel() == rate && m.getAuto() == auto && m.getAirside() == airside) {
                    return m;
                }
            }
            LOGGER.debug("Unexpected FanSpeed value from rate={}, auto={}, airside={}", rate, auto ? 1 : 0,
                    airside ? 1 : 0);
            return LEVEL_1;
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
            return STOPPED;
        }
    }

    public enum AirbaseFeature {
        ZONE("en_zone"),
        FILTER_SIGN("en_filter_sign"),
        TEMP_SETTING("en_temp_setting"),
        FRATE("en_frate"),
        DIR("en_dir"),
        RTEMP_A("en_rtemp_a"),
        SPMODE("en_spmode"),
        MOMPOW("en_mompow"),
        PATROL("en_patrol"),
        AIRSIDE("en_airside"),
        QUICK_TIMER("en_quick_timer"),
        AUTO("en_auto"),
        DRY("en_dry"),
        FRATE_AUTO("en_frate_auto");

        private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseFeature.class);
        private final String value;

        AirbaseFeature(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
