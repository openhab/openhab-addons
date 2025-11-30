/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.devices.ac;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ACStringCommands handles the String Commands for ACs
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ACStringCommands {

    /**
     * Operational Modes for AC Device
     */
    public enum OperationalMode {
        AUTO(1),
        COOL(2),
        DRY(3),
        HEAT(4),
        FAN_ONLY(5),
        UNKNOWN(0);

        private final int value;

        private OperationalMode(int value) {
            this.value = value;
        }

        /**
         * Gets Operational Mode value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Provides Operational Mode Common name
         * 
         * @param id integer from byte response
         * @return type
         */
        public static OperationalMode fromId(int id) {
            for (OperationalMode type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * Converts byte value to the Swing Mode label by version
     * for AC devices.
     * Two versions of V3, Supported Swing or Non-Supported (4)
     * V2 set without leading 3, but reports with it (1)
     */
    public enum SwingMode {
        OFF3(0x30, 3),
        OFF4(0x00, 3),
        VERTICAL3(0x3C, 3),
        VERTICAL4(0xC, 3),
        HORIZONTAL3(0x33, 3),
        HORIZONTAL4(0x3, 3),
        BOTH3(0x3F, 3),
        BOTH4(0xF, 3),
        OFF2(0, 2),
        VERTICAL2(0xC, 2),
        VERTICAL1(0x3C, 2),
        HORIZONTAL2(0x3, 2),
        HORIZONTAL1(0x33, 2),
        BOTH2(0xF, 2),
        BOTH1(0x3F, 2),

        UNKNOWN(0xFF, 0);

        private final int value;
        private final int version;

        private SwingMode(int value, int version) {
            this.value = value;
            this.version = version;
        }

        /**
         * Gets Swing Mode value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Gets device version for swing mode
         * 
         * @return version
         */
        public int getVersion() {
            return version;
        }

        /**
         * Gets Swing mode in common language horiontal, vertical, off, etc.
         * 
         * @param id integer from byte response
         * @param version device version
         * @return type
         */
        public static SwingMode fromId(int id, int version) {
            for (SwingMode type : values()) {
                if (type.getId() == id && type.getVersion() == version) {
                    return type;
                }
            }
            return UNKNOWN;
        }

        @Override
        public String toString() {
            // Drops the trailing 1 (V2 report) 2, 3 or 4 (nonsupported V3) from the swing mode
            return super.toString().replace("1", "").replace("2", "").replace("3", "").replace("4", "");
        }
    }

    /**
     * Converts byte value to the Fan Speed label by version.
     * Some devices do not support all speeds
     */
    public enum FanSpeed {
        AUTO2(102, 2),
        FULL2(100, 2),
        HIGH2(80, 2),
        MEDIUM2(50, 2),
        LOW2(30, 2),
        SILENT2(20, 2),
        UNKNOWN2(0, 2),

        AUTO3(102, 3),
        FULL3(0, 3),
        HIGH4(100, 3),
        HIGH3(80, 3),
        MEDIUM3(60, 3),
        LOW3(40, 3),
        SILENT3(30, 3),
        UNKNOWN3(0, 3),

        UNKNOWN(0, 0);

        private final int value;

        private final int version;

        private FanSpeed(int value, int version) {
            this.value = value;
            this.version = version;
        }

        /**
         * Gets Fan Speed value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Gets device version for Fan Speed
         * 
         * @return version
         */
        public int getVersion() {
            return version;
        }

        /**
         * Returns Fan Speed high, medium, low, etc
         * 
         * @param id integer from byte response
         * @param version version
         * @return type
         */
        public static FanSpeed fromId(int id, int version) {
            for (FanSpeed type : values()) {
                if (type.getId() == id && type.getVersion() == version) {
                    return type;
                }
            }
            return UNKNOWN;
        }

        @Override
        public String toString() {
            // Drops the trailing 2 or 3 from the fan speed
            return super.toString().replace("2", "").replace("3", "").replace("4", "");
        }
    }
}
