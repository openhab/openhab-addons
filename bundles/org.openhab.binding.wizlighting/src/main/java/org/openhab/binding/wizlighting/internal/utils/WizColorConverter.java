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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for converting colors and color temperures
 *
 * The full color WiZ bulbs can produce colors and various temperatures of
 * "whites" by mixing any of the available LEDs: RGBWwarm = RGBWWCwarm = Red,
 * Green, Blue, Warm White, Cool White. When operating in full color mode, the
 * warm whites are used to increase saturation (RGBW style). Temperatures of
 * white can also be called directly as K instead of mixing cw/ww (c/w) The
 * colors and temperatures need to be converted to the HSBType/PercentType
 * supported by OpenHAB.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */

@NonNullByDefault
public class WizColorConverter {

    private final Logger logger = LoggerFactory.getLogger(WizColorConverter.class);

    /**
     * Converts an {@link DecimalType} hue and a {@link PercentType} saturation to
     * red, green, blue, and white (RGBW) components. Because the WiZ bulbs keep
     * dimming in a separate channel, we only take account hue and saturation for
     * the color channels. When creating colors, the WiZ bulbs only use the warm
     * white channel, the cool white channel is ignored.
     *
     * Taken from Tasmota HsToRGB
     *
     * @param hsbColor the {@link HSBType}.
     *
     * @return an interger array of the color components
     */
    public int[] hsbToRgbw(HSBType hsb) {
        logger.debug("Converting hue and saturation to RGBW.  Incoming: {}", hsb);
        double redD = 255.;
        double greenD = 255.;
        double blueD = 255.;
        int red = 255;
        int green = 255;
        int blue = 255;
        int white = 255;

        // Since we're going to use the white lights to control saturation, recalculate what
        // the HSBvalue would be if the color was at full brightness and saturation
        Double hue = hsb.getHue().doubleValue();
        Double sat_255 = ((hsb.getSaturation().doubleValue()) / 100) * 255;

        if (sat_255 > 0) {
            int i = (int) (hue / 60); // color quadrant 0..2555
            double f = hue % 60; // 0..59
            double q = 255 - ((f / 60) * sat_255);
            // double p = 255 - sat_255;
            // double t = 255 - (((60 - f) / 60) * sat_255);
            double p = 0;
            double t = 255 - (((60 - f) / 60) * 255);

            switch (i) {
                case 0:
                    // redD = 255;
                    greenD = t;
                    blueD = p;
                    break;
                case 1:
                    redD = q;
                    // greenD = 255;
                    blueD = p;
                    break;
                case 2:
                    redD = p;
                    // greenD = 255;
                    blueD = t;
                    break;
                case 3:
                    redD = p;
                    greenD = q;
                    // blueD = 255;
                    break;
                case 4:
                    redD = t;
                    greenD = p;
                    // blueD = 255;
                    break;
                default:
                    // redD = 255;
                    greenD = p;
                    blueD = q;
                    break;
            }
        }

        logger.debug("Colors from hue, assuming full brightness R: {} G: {} B: {}", redD, greenD, blueD);

        // Convert the "PercentType" to a percent
        double saturationPercent = (hsb.getSaturation().doubleValue() / 100);

        // Calculate the white intensity from saturation and adjust down the other colors
        // This is approximately what the WiZ app does. Personally, I think it undersaturates everything
        if (saturationPercent < 0.5) {
            // At less than 50% saturation, maximize white and lower the other intensities by 2x of the saturation
            // percent. (2x to give us full range between 0-50%)
            // white = 255;
            // ^^ WiZ does this.. I think it's very undersaturated that way
            white = 255 / 2; // Divide by two to not undersaturate
            red = (int) (redD * (2 * saturationPercent));
            green = (int) (greenD * (2 * saturationPercent));
            blue = (int) (blueD * (2 * saturationPercent));
        } else {
            // At >50% saturation, colors are at full and increase saturation by decreasing the white intensity.
            // white = (int) (255 * 2 * (1 - saturationPercent));
            // ^^ WiZ does this.. I think it's very undersaturated that way
            white = (int) ((255 / 2) * 2 * (1 - saturationPercent));
            red = (int) redD;
            green = (int) greenD;
            blue = (int) blueD;
        }

        // Note: We're keeping the brightness in a totally separate channel
        logger.debug("Outgoing colors, adjusted for saturation R: {} G: {} B: {} W: {}", red, green, blue, white);
        return new int[] { red, green, blue, white };
    }

    /**
     * Converts Red/Green/Blue/White components to Hue and saturation.
     *
     * @param int red - the value of the red component (0-255)
     * @param int green - the value of the green component (0-255)
     * @param int blue - the value of the blue component (0-255)
     * @param int white - the value of the white component (0-255)
     *
     *            Totally made this up.
     *
     * @return a {@link ColorRequestParam} with the color components
     */
    public HSBType rgbwDimmingToHSB(int red, int green, int blue, int white, int dimming) {
        logger.debug("Converting RGB to HS. Incoming R: {} G: {} B: {} W: {}", red, green, blue, white);
        // Can get hue from the ratios of the colors.
        // The calculated *hue* component of the HSB should be correct regardless of the
        // state of the white lights because it's strictly based on the ratio of the colors
        DecimalType hue = HSBType.fromRGB(red, green, blue).getHue();
        double saturationPercent;
        if (white < 255) {
            saturationPercent = (int) (1 - (white / (255 * 2)));
        } else {
            saturationPercent = Math.max(red, Math.max(green, blue)) / (255 * 2);
        }
        HSBType out = new HSBType(hue, new PercentType((int) saturationPercent * 100), new PercentType(dimming));
        logger.debug("Outgoing HSB: {}", out);
        return out;
    }
}
