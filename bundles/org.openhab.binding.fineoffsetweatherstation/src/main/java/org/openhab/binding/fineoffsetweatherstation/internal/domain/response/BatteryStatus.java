/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.fineoffsetweatherstation.internal.domain.response;

import static org.openhab.binding.fineoffsetweatherstation.internal.Utils.toUInt8;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The status of the sensors' battery.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class BatteryStatus {

    public enum Type {
        /**
         * 1: BATT low, 0: normal
         */
        LOW_HIGH,

        /**
         * level0~5，<=1 for BATT low
         */
        LEVEL,

        /**
         * level0~6，<=1 for BATT low, 6 = dc power supply
         */
        LEVEL_OR_DC,

        /**
         * val * 0.1v
         */
        VOLTAGE_BROAD_STEPS,

        /**
         * val*0.02V if v<=1.2V BATT low
         */
        VOLTAGE_FINE_STEPS
    }

    private @Nullable Integer level;
    private @Nullable Double voltage;
    private final boolean low;
    private boolean dc;

    public BatteryStatus(Type type, byte data) {
        int value = toUInt8(data);
        double voltage;
        switch (type) {
            case LOW_HIGH:
                low = value == 1;
                break;
            case LEVEL:
                level = value;
                low = value <= 1;
                break;
            case LEVEL_OR_DC:
                dc = value == 6;
                level = value;
                low = value <= 1;
                break;
            case VOLTAGE_BROAD_STEPS:
                this.voltage = voltage = value * 0.1;
                low = voltage <= 1.2;
                break;
            case VOLTAGE_FINE_STEPS:
                this.voltage = voltage = value * 0.02;
                low = voltage <= 1.2;
                break;
            default:
                throw new IllegalArgumentException("Unsupported type " + type);
        }
    }

    /**
     * @return level 0 - 5 or null f not available
     */
    public @Nullable Integer getLevel() {
        return level;
    }

    /**
     * @return voltage of the battery or null if not available
     */
    public @Nullable Double getVoltage() {
        return voltage;
    }

    /**
     * @return true, if the battery is low
     */
    public boolean isLow() {
        return low;
    }

    /**
     * @return true, if device is DC connected
     */
    public boolean isDc() {
        return dc;
    }

    public @Nullable Integer getPercentage() {
        if (dc) {
            return 100;
        }
        Integer currentLevel = level;
        if (currentLevel != null) {
            return (currentLevel * 100 / 5);
        }
        return null;
    }

    @Override
    public String toString() {
        String status = low ? "LOW" : "OK";
        if (dc) {
            return "DC connected";
        }
        if (voltage != null) {
            return "Battery " + voltage + " V " + status;
        }
        if (level != null) {
            return "Battery " + level + "/ 5" + " " + status;
        }
        return "Battery " + status;
    }
}
