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
package org.openhab.binding.tradfri.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;

/**
 * The {@link TradfriColor} is used for conversion between color formats.
 * Use the static constructors {@link #TradfriColor(Integer, Integer, Integer)} and
 * {@link #TradfriColor(HSBType)} for construction.
 *
 * @author Holger Reichert - Initial contribution
 * @author Stefan Triller - Use conversions from HSBType
 *
 */
@NonNullByDefault
public class TradfriColor {

    // Tradfri uses the CIE color space (see https://en.wikipedia.org/wiki/CIE_1931_color_space),
    // which uses x,y-coordinates.
    // Its own app comes with 3 predefined color temperature settings (0,1,2), which have those values:
    private static final double[] PRESET_X = new double[] { 24933.0, 30138.0, 33137.0 };
    private static final double[] PRESET_Y = new double[] { 24691.0, 26909.0, 27211.0 };

    /**
     * CIE XY color values in the tradfri range 0 to 65535.
     * May be <code>null</code> if the calculation method does not support this color range.
     */
    public Integer xyX, xyY;

    /**
     * Brightness level in the tradfri range 0 to 254.
     * May be <code>null</code> if the calculation method does not support this color range.
     */
    public @Nullable Integer brightness;

    /**
     * Construct from CIE XY values in the tradfri range.
     *
     * @param xyX x value 0 to 65535
     * @param xyY y value 0 to 65535
     * @param brightness brightness from 0 to 254
     */
    public TradfriColor(Integer xyX, Integer xyY, @Nullable Integer brightness) {
        this.xyX = xyX;
        this.xyY = xyY;
        if (brightness != null) {
            if (brightness > 254) {
                this.brightness = 254;
            } else {
                this.brightness = brightness;
            }
        }
    }

    /**
     * Construct from HSBType
     *
     * @param hsb HSBType from the framework
     */
    public TradfriColor(HSBType hsb) {
        PercentType[] xyArray = hsb.toXY();
        this.xyX = normalize(xyArray[0].doubleValue() / 100.0);
        this.xyY = normalize(xyArray[1].doubleValue() / 100.0);
        this.brightness = (int) (hsb.getBrightness().floatValue() * 2.54);
    }

    /**
     * Obtain the TradfriColor (x/y) as HSBType
     *
     * @return HSBType representing the x/y Tradfri color
     */
    public HSBType getHSB() {
        float x = unnormalize(xyX);
        float y = unnormalize(xyY);

        HSBType converted = HSBType.fromXY(x, y);

        final Integer brightness = this.brightness;
        if (brightness == null) {
            throw new IllegalStateException("cannot convert to HSB with brightness=null");
        }
        return new HSBType(converted.getHue(), converted.getSaturation(), xyBrightnessToPercentType(brightness));
    }

    /**
     * Construct from color temperature in percent.
     * 0 (coldest) to 100 (warmest).
     * Note: The resulting {@link TradfriColor} has only the {@link TradfriColor#xyX X} and {@link TradfriColor#xyY y}
     * values set!
     *
     * @param percentType the color temperature in percent
     */
    public TradfriColor(PercentType percentType) {
        double percent = percentType.doubleValue();

        int x, y;
        if (percent < 50.0) {
            // we calculate a value that is between preset 0 and 1
            double p = percent / 50.0;
            x = (int) Math.round(PRESET_X[0] + p * (PRESET_X[1] - PRESET_X[0]));
            y = (int) Math.round(PRESET_Y[0] + p * (PRESET_Y[1] - PRESET_Y[0]));
        } else {
            // we calculate a value that is between preset 1 and 2
            double p = (percent - 50) / 50.0;
            x = (int) Math.round(PRESET_X[1] + p * (PRESET_X[2] - PRESET_X[1]));
            y = (int) Math.round(PRESET_Y[1] + p * (PRESET_Y[2] - PRESET_Y[1]));
        }

        this.xyX = x;
        this.xyY = y;
    }

    /**
     * Normalize value to the tradfri range.
     *
     * @param value double in the range 0.0 to 1.0
     * @return normalized value in the range 0 to 65535
     */
    private int normalize(double value) {
        return (int) (value * 65535 + 0.5);
    }

    /**
     * Reverse-normalize value from the tradfri range.
     *
     * @param value integer in the range 0 to 65535
     * @return unnormalized value in the range 0.0 to 1.0
     */
    private float unnormalize(int value) {
        return (value / 65535.0f);
    }

    /**
     * Calculate the color temperature from given x and y values.
     *
     * @return {@link PercentType} with color temperature (0 = coolest, 100 = warmest)
     */
    public PercentType getColorTemperature() {
        double x = xyX;
        double y = xyY;
        double value = 0.0;
        if ((x > PRESET_X[1] && y > PRESET_Y[1]) && (x <= PRESET_X[2] && y <= PRESET_Y[2])) {
            // is it between preset 1 and 2?
            value = (x - PRESET_X[1]) / (PRESET_X[2] - PRESET_X[1]) / 2.0 + 0.5;
        } else if ((x >= PRESET_X[0] && y >= PRESET_Y[0]) && (x <= (PRESET_X[1] + 2.0) && y <= PRESET_Y[1])) {
            // is it between preset 0 and 1?
            // hint: in the above line we calculate 2.0 to PRESET_X[1] because
            // some bulbs send slighty higher x values for this preset (maybe rounding errors?)
            value = (x - PRESET_X[0]) / (PRESET_X[1] - PRESET_X[0]) / 2.0;
        } else if (x < PRESET_X[0]) {
            // cooler than coolest preset (full color bulbs)
            value = 0.0;
        } else if (x > PRESET_X[2]) {
            // warmer than warmest preset (full color bulbs)
            value = 1.0;
        }
        return new PercentType((int) Math.round(value * 100.0));
    }

    /**
     * Converts the xyBrightness value to PercentType
     *
     * @param xyBrightness xy brightness level 0 to 254
     * @return {@link PercentType} with brightness level (0 = light is off, 1 = lowest, 100 = highest)
     */
    public static PercentType xyBrightnessToPercentType(int xyBrightness) {
        if (xyBrightness > 254) {
            return PercentType.HUNDRED;
        } else if (xyBrightness < 0) {
            return PercentType.ZERO;
        }
        return new PercentType((int) Math.ceil(xyBrightness / 2.54));
    }
}
