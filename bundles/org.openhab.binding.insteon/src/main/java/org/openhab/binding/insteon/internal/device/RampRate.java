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
package org.openhab.binding.insteon.internal.device;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents ramp rate for Insteon dimmer products
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum RampRate {
    MIN_8(0x01, 480),
    MIN_7(0x02, 420),
    MIN_6(0x03, 360),
    MIN_5(0x04, 300),
    MIN_4_5(0x05, 270),
    MIN_4(0x06, 240),
    MIN_3_5(0x07, 210),
    MIN_3(0x08, 180),
    MIN_2_5(0x09, 150),
    MIN_2(0x0A, 120),
    MIN_1_5(0x0B, 90),
    MIN_1(0x0C, 60),
    SEC_47(0x0D, 47),
    SEC_43(0x0E, 43),
    SEC_38_5(0x0F, 38.5),
    SEC_34(0x10, 34),
    SEC_32(0x11, 32),
    SEC_30(0x12, 30),
    SEC_28(0x13, 28),
    SEC_26(0x14, 26),
    SEC_23_5(0x15, 23.5),
    SEC_21_5(0x16, 21.5),
    SEC_19(0x17, 19),
    SEC_8_5(0x18, 8.5),
    SEC_6_5(0x19, 6.5),
    SEC_4_5(0x1A, 4.5),
    SEC_2(0x1B, 2),
    DEFAULT(0x1C, 0.5),
    SEC_0_3(0x1D, 0.3),
    SEC_0_2(0x1E, 0.2),
    INSTANT(0x1F, 0.1);

    private static Map<Integer, RampRate> map = new HashMap<>();
    static {
        for (RampRate rate : RampRate.values()) {
            map.put(rate.value, rate);
        }
    }

    private int value;
    private double time;

    private RampRate(int value, double time) {
        this.value = value;
        this.time = time;
    }

    public int getValue() {
        return value;
    }

    public double getTimeInSeconds() {
        return time;
    }

    public long getTimeInMilliseconds() {
        return (long) (time * 1000);
    }

    /**
     * Factory method for getting a RampRate from the ramp rate value
     *
     * @param hex the ramp rate value
     * @return the ramp rate
     */
    public static RampRate valueOf(int value) {
        return map.getOrDefault(value, RampRate.DEFAULT);
    }

    /**
     * Factory method for getting a RampRate from the closest ramp time
     *
     * @param time the ramp time
     * @return the ramp rate
     */
    public static RampRate fromTime(double time) {
        return map.values().stream().min(Comparator.comparingDouble(rate -> Math.abs(rate.getTimeInSeconds() - time)))
                .orElse(RampRate.DEFAULT);
    }
}
