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
package org.openhab.binding.insteon.internal.device;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RampRate} represents a ramp rate for Insteon dimmer products
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum RampRate {
    MIN_9(0x00, 540),
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
    SLOW(0x19, 6.5),
    SEC_4_5(0x1A, 4.5),
    MEDIUM(0x1B, 2),
    DEFAULT(0x1C, 0.5),
    FAST(0x1D, 0.3),
    SEC_0_2(0x1E, 0.2),
    INSTANT(0x1F, 0.1);

    private static final List<String> SUPPORTED_FEATURE_TYPES = List.of(FEATURE_TYPE_GENERIC_DIMMER);

    private static final Map<Integer, RampRate> VALUE_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(rate -> rate.value, Function.identity()));

    private final int value;
    private final double time;

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

    @Override
    public String toString() {
        double time = getTimeInSeconds();
        String unit = "s";
        if (time >= 60) {
            time /= 60;
            unit = "min";
        }
        return new DecimalFormat("0.#").format(time) + unit;
    }

    /**
     * Factory method for determining if a given feature type supports ramp rate
     *
     * @param featureType the feature type
     * @return true if supported
     */
    public static boolean supportsFeatureType(String featureType) {
        return SUPPORTED_FEATURE_TYPES.contains(featureType);
    }

    /**
     * Factory method for getting a RampRate from a ramp rate value
     *
     * @param value the ramp rate value
     * @return the ramp rate
     */
    public static RampRate valueOf(int value) {
        return VALUE_MAP.getOrDefault(value, RampRate.DEFAULT);
    }

    /**
     * Factory method for getting a RampRate from the closest ramp time
     *
     * @param time the ramp time
     * @return the ramp rate
     */
    public static RampRate fromTime(double time) {
        return VALUE_MAP.values().stream().min(Comparator.comparingDouble(rate -> Math.abs(rate.time - time))).get();
    }

    /**
     * Factory method for getting a RampRate from a ramp rate string
     *
     * @param string the ramp rate string
     * @return the ramp rate
     */
    public static @Nullable RampRate fromString(String string) {
        try {
            return fromTime(Double.parseDouble(string));
        } catch (NumberFormatException e) {
            return VALUE_MAP.values().stream().filter(rate -> rate.toString().equals(string)).findAny().orElse(null);
        }
    }
}
