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
package org.openhab.binding.loxone.internal.types;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openhab.core.library.types.HSBType;

/**
 * Temperature HSB Type which acceptss a color in the form brigthness,temperature (Kelvin)
 *
 * @author Michael Mattan - initial contribution
 *
 */
public class LxTemperatureHSBType extends HSBType {

    private static final long serialVersionUID = -2821122730407485795L;

    public static HSBType fromBrightnessTemperature(String value) {
        List<String> constituents = Arrays.stream(value.split(",")).map(in -> in.trim()).collect(Collectors.toList());

        if (constituents.size() == 2) {
            int brightness = constrain(Integer.valueOf(constituents.get(0)), 0, 100);
            int temperature = constrain(Integer.valueOf(constituents.get(1)), 0, 65500);

            int red = map(brightness, 0, 100, 0, calculateRed(temperature));
            int green = map(brightness, 0, 100, 0, calculateGreen(temperature));
            int blue = map(brightness, 0, 100, 0, calculateBlue(temperature));

            return HSBType.fromRGB(red, green, blue);
        } else {
            throw new IllegalArgumentException(value + " is not a valid TemperatureHSBType syntax");
        }
    }

    /**
     * Re-maps a number from one range to another. That is, a value of fromLow would get mapped to toLow, a value of
     * fromHigh to toHigh, values in-between to values in-between, etc.
     *
     * @param x the number to map
     * @param fromLow the lower bound of the value's current range
     * @param fromHigh the upper bound of the value's current range
     * @param toLow the lower bound of the value's target range
     * @param toHigh the upper bound of the value's target range
     * @return the mapped value
     */
    private static int map(int x, int fromLow, int fromHigh, int toLow, int toHigh) {
        return (x - fromLow) * (toHigh - toLow) / (fromHigh - fromLow) + toLow;
    }

    /**
     * calculates the red value based on the Kelvin temperature
     *
     * @param temp the Kelvin temperature
     * @return the red value
     */
    private static int calculateRed(int temp) {
        int red = 255;
        int temperature = temp / 100;

        if (temperature > 66) {
            red = temperature - 60;
            red = ((Long) Math.round(329.698727466 * Math.pow(red, -0.1332047592))).intValue();
        }

        return constrain(red, 0, 255);
    }

    /**
     * calculates the green value based on the Kelvin temperature
     *
     * @param temp green Kelvin temperature
     * @return the red value
     */
    private static int calculateGreen(int temp) {
        int green;
        int temperature = temp / 100;

        if (temperature <= 66) {
            green = temperature;
            green = ((Long) Math.round((99.4708025861 * Math.log(green)) - 161.1195681661)).intValue();
        } else {
            green = temperature - 60;
            green = ((Long) Math.round(288.1221695283 * Math.pow(green, -0.0755148492))).intValue();
        }

        return constrain(green, 0, 255);
    }

    /**
     * calculates the blue value based on the Kelvin temperature
     *
     * @param temp the Kelvin temperature
     * @return the blue value
     */
    private static int calculateBlue(int temp) {
        int blue = 255;
        int temperature = temp / 100;

        if (temperature < 65) {
            if (temperature <= 19) {
                blue = 0;
            } else {
                blue = temperature - 10;
                blue = ((Long) Math.round((138.5177312231 * Math.log(blue)) - 305.0447927307)).intValue();
            }
        }

        return constrain(blue, 0, 255);
    }

    /**
     * Constrains a number to be within a range.
     *
     * @param x the number to constrain
     * @param min the minimum value
     * @param max the maximum value
     * @return the constrained value
     */
    private static int constrain(int x, int min, int max) {
        if (x >= min && x <= max) {
            return x;
        } else if (x < min) {
            return min;
        } else {
            return max;
        }
    }
}
