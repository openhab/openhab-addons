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
package org.openhab.binding.mideaac.internal.devices.a1;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link A1StringCommands handles the String Commands for dehumidifiers
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class A1StringCommands {

    /**
     * Operational Modes for Dehumidifier
     */
    public enum A1OperationalMode {
        MANUAL(1),
        CONTINUOUS(2),
        AUTO(3),
        CLOTHES_DRY(4),
        SHOES_DRY(5),
        UNKNOWN(0);

        private final int value;

        private A1OperationalMode(int value) {
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
        public static A1OperationalMode fromId(int id) {
            for (A1OperationalMode type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * Converts byte value to the Fan Speed for Dehumidifier
     * 
     */
    public enum A1FanSpeed {

        AUTO(102),
        HIGH(80),
        MEDIUM(60),
        LOW(40),
        LOWEST(1),
        OFF(127),
        UNKNOWN(0);

        private final int value;

        private A1FanSpeed(int value) {
            this.value = value;
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
         * Returns Fan Speed high, medium, low, etc
         * 
         * @param id integer from byte response
         * @return type
         */
        public static A1FanSpeed fromId(int id) {
            for (A1FanSpeed type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
