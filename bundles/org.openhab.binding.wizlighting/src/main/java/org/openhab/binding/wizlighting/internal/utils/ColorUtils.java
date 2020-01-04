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
package org.openhab.binding.wizlighting.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.openhab.binding.wizlighting.internal.entities.ColorRequestParam;

/**
 * Utilities for converting colors and color temperures
 *
 * The full color WiZ bulbs can produce colors and various temperatures of
 * "whites" by mixing any of the available LEDs: RGBWwarm = RGBWWCwarm = Red, Green,
 * Blue, Warm White, Cool White. When operating in full color mode, the warm
 * whites are used to increase saturation (RGBW style). Temperatures of white
 * can also be called directly as K instead of mixing cw/ww (c/w) The colors and
 * temperatures need to be converted to the HSBType/PercentType supported by
 * OpenHAB.
 *
 * Code for converting from HSI (HSB) to RGBW is adapted from: https://
 * blog.saikoled.com/post/44677718712/how-to-convert-from-hsi-to-rgb-white
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */

@NonNullByDefault
public class ColorUtils {

    /**
     * Converts an {@link HSBType} to Red, Green, Blue, and White (RGBW) components
     *
     * @param hsbColor the {@link HSBType}.
     *
     * @return a {@link ColorRequestParam} with the color components
     */
    public static ColorRequestParam HSI2RGBW(HSBType hsbColor) {
        // Get the components of the color
        // Hue is already clamped to [0, 360]
        double hueDegrees = hsbColor.getHue().doubleValue();
        // Saturation and intensity are already clamped to [0, 1]
        double saturation = hsbColor.getSaturation().doubleValue() / 100.0d;
        double intensity = hsbColor.getBrightness().doubleValue() / 100.0d;

        int red, green, blue, warm, cool;
        double cosHue, cos60DFromHue;

        // Convert hue to radians.
        double hueRadians = Math.PI * hueDegrees / 180.0d;
        // Calculate the role saturation and intensity play in the color space
        double siFactor = saturation * 255 * intensity / 3;
        // Calculate the white intensity from saturation and intensity
        warm = (int) (255 * (1 - saturation) * intensity);
        cool = 0;

        if (hueRadians < (2 / 3) * Math.PI) { // hues less than 120° - Red to Green
            // get the cosine of the hue and of the hue shifted by 60°
            // We divided the hue space into 60° chunks (360/3!)
            cosHue = Math.cos(hueRadians);
            cos60DFromHue = Math.cos((1 / 3) * Math.PI - hueRadians);
            // Split the hue between the red and green channels, turn off blue
            red = (int) (siFactor * (1 + cosHue / cos60DFromHue));
            green = (int) (siFactor * (1 + (1 - cosHue / cos60DFromHue)));
            blue = 0;
        } else if (hueRadians < (4 / 3) * Math.PI) { // hues between 120° and 240° - Green to Blue
            hueRadians = hueRadians - (2 / 3) * Math.PI; // subtract 120° from the hue
            // get the adjusted cosine of the hue and of the hue shifted by 60°
            cosHue = Math.cos(hueRadians);
            cos60DFromHue = Math.cos((1 / 3) * Math.PI - hueRadians);
            // Split the hue between the green and blue channels, turn off red
            green = (int) (siFactor * (1 + cosHue / cos60DFromHue));
            blue = (int) (siFactor * (1 + (1 - cosHue / cos60DFromHue)));
            red = 0;
        } else { // hues between 240° and 360° - Blue back to Red
            hueRadians = hueRadians - (4 / 3) * Math.PI; // subtract 240° from the hue
            // get the adjusted cosine of the hue and of the hue shifted by 60°
            cosHue = Math.cos(hueRadians);
            cos60DFromHue = Math.cos((1 / 3) * Math.PI - hueRadians);
            // Split the hue between the blue and red channels, turn off green
            blue = (int) (siFactor * (1 + cosHue / cos60DFromHue));
            red = (int) (siFactor * (1 + (1 - cosHue / cos60DFromHue)));
            green = 0;
        }

        return new ColorRequestParam(red, green, blue, warm, cool);
    }

    // TODO:  Go back from RGBW to HSB
}
