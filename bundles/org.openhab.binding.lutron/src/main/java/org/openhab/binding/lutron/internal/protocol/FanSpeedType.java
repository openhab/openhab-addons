/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Defines Lutron fan controller speed settings
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public enum FanSpeedType {
    @SerializedName("High")
    HIGH(100, "High"),
    @SerializedName("MediumHigh")
    MEDIUMHIGH(75, "MediumHigh"),
    @SerializedName("Medium")
    MEDIUM(50, "Medium"),
    @SerializedName("Low")
    LOW(25, "Low"),
    @SerializedName("Off")
    OFF(0, "Off");

    /** Fan speed expressed as a percentage **/
    private final int speed;

    /** Fan speed expressed as a String (used by LEAP) **/
    private final String leapValue;

    FanSpeedType(int speed, String leapValue) {
        this.speed = speed;
        this.leapValue = leapValue;
    }

    public int speed() {
        return speed;
    }

    public String leapValue() {
        return leapValue;
    }

    @Override
    public String toString() {
        return leapValue;
    }

    public static FanSpeedType toFanSpeedType(int percentage) {
        if (percentage == OFF.speed) {
            return FanSpeedType.OFF;
        } else if (percentage > OFF.speed && percentage <= LOW.speed) {
            return FanSpeedType.LOW;
        } else if (percentage > LOW.speed && percentage <= MEDIUM.speed) {
            return FanSpeedType.MEDIUM;
        } else if (percentage > MEDIUM.speed && percentage <= MEDIUMHIGH.speed) {
            return FanSpeedType.MEDIUMHIGH;
        } else {
            return FanSpeedType.HIGH;
        }
    }

    public static FanSpeedType toFanSpeedType(String speedString) {
        for (FanSpeedType enumValue : FanSpeedType.values()) {
            if (enumValue.leapValue.equalsIgnoreCase(speedString)) {
                return enumValue;
            }
        }
        return OFF;
    }
}
