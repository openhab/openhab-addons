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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class contains the enums for the various modes supported by the BLE thermostat
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class MadokaProperties {

    public enum FanSpeed {
        MAX(5),
        MEDIUM(3),
        LOW(1),
        AUTO(0);

        private int v;

        FanSpeed(int v) {
            this.v = v;
        }

        public static FanSpeed valueOf(int v) {
            if (v == 5) {
                return MAX;
            } else if (v >= 2 && v <= 4) {
                return MEDIUM;
            } else if (v == 1) {
                return LOW;
            } else {
                return AUTO;
            }
        }

        public int value() {
            return v;
        }
    }

    public enum OperationMode {
        FAN(0),
        DRY(1),
        AUTO(2),
        COOL(3),
        HEAT(4),
        VENTILATION(5);

        private int v;

        OperationMode(int v) {
            this.v = v;
        }

        public static OperationMode valueOf(int v) {
            for (OperationMode m : values()) {
                if (m.v == v) {
                    return m;
                }
            }
            // Should never happen
            return HEAT;
        }

        public int value() {
            return v;
        }
    }
}
