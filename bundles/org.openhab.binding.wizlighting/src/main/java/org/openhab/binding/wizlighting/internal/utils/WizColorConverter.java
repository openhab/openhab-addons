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
     * Totally made this up. I couldn't find any satisfactory conversions that actually
     * included the channels the way that the WiZ bulbs seem to.
     *
     * @param hsbColor the {@link HSBType}.
     *
     * @return an interger array of the color components
     */
    public int[] hsbToRgbw(HSBType hsb) {
        logger.trace("Converting hue and saturation to RGBW.  Incoming: {}", hsb);
        int red, green, blue, white;

        // Get, roughly, the components
        red = hsb.getRed().intValue();
        green = hsb.getGreen().intValue();
        blue = hsb.getBlue().intValue();
        logger.trace("Converting hue and saturation to RGBW.  Incoming: {}", hsb);

        // Convert the "PercentType" to a percent
        double saturationPercent = (hsb.getSaturation().doubleValue() / 100);

        // Calculate the white intensity from saturation and adjust down the other colors
        if (saturationPercent < 0.15) {
            white = 255;
            red = (int) (red * (saturationPercent + 0.5));
            green = (int) (green * (saturationPercent + 0.5));
            blue = (int) (blue * (saturationPercent + 0.5));
        } else if (saturationPercent < 0.5) {
            white = 255;
        } else if (saturationPercent < 0.85) {
            white = (int) (255 * (1 - saturationPercent));
        } else {
            white = 0;
        }

        // Note: We're keeping the brightness as-is
        return new int[] { red, green, blue, white };
    }

    /**
     * Converts Red/Green/Blue/White components to Hue and saturation.
     *
     * @param int R - the value of the red component (0-255)
     * @param int G - the value of the green component (0-255)
     * @param int B - the value of the blue component (0-255)
     * @param int W - the value of the white component (0-255)
     *
     *            Totally made this up.
     *
     * @return a {@link ColorRequestParam} with the color components
     */
    public HSBType rgbwToHSB(int r, int g, int b, int w) {
        logger.trace("Converting RGB to HS. Incoming R: {} G: {} B: {} W: {}", r, g, b, w);
        float desatBump = (255 - w) / 255;
        if (r == 0) {
            g = Math.min((int) (g * desatBump), 255);
            b = Math.min((int) (b * desatBump), 255);
            r = 255 - w;
            return HSBType.fromRGB(r, g, b);
        } else if (b == 0) {
            g = Math.min((int) (g * desatBump), 255);
            r = Math.min((int) (r * desatBump), 255);
            b = 255 - w;
            return HSBType.fromRGB(r, g, b);
        } else if (g == 0) {
            b = Math.min((int) (b * desatBump), 255);
            r = Math.min((int) (r * desatBump), 255);
            g = 255 - w;
            return HSBType.fromRGB(r, g, b);
        } else {
            r = Math.min((int) (r * desatBump), 255);
            g = Math.min((int) (g * desatBump), 255);
            b = Math.min((int) (b * desatBump), 255);
            return HSBType.fromRGB(r, g, b);
        }
    }
}
