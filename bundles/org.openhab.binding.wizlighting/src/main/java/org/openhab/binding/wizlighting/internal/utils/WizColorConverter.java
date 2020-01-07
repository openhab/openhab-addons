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
     * Totally made this up. It's based on the commonly referenced SaikoLED blog, but that doesn't include a separate dimming channel.  I couldn't find any satisfactory conversions that actually
     * included the channels the way that the WiZ bulbs seem to.
     *
     * @param hsbColor the {@link HSBType}.
     *
     * @return an interger array of the color components
     */
    public int[] hsbToRgbw(HSBType hsb) {
        logger.trace("Converting hue and saturation to RGBW.  Incoming: {}", hsb);
        int red, green, blue, white;

        double hue = hsb.getHue().doubleValue();
        double cosHue ;
        double cos60MinusHue;

        if (hue < 120) {  // red to green
            cosHue = Math.cos(Math.toRadians(hue));
            cos60MinusHue = Math.cos(Math.toRadians(60 - hue));
            red = (int) (255 * (1 + cosHue / cos60MinusHue));
            green = (int) (255 * (1 + (1 - cosHue / cos60MinusHue)));
            blue = 0;
        } else if (hue < 240) {  // green to blue
            hue =hue - 120;
            cosHue = Math.cos(Math.toRadians(hue));
            cos60MinusHue = Math.cos(Math.toRadians(60 - hue));
            green = (int) (255 * (1 + cosHue / cos60MinusHue));
            blue = (int) (255 * (1 + (1 - cosHue / cos60MinusHue)));
            red = 0;
        } else {  // blue to red
            hue =hue - 240;
            cosHue = Math.cos(Math.toRadians(hue));
            cos60MinusHue = Math.cos(Math.toRadians(60 - hue));
            blue = (int) (255 * (1 + cosHue / cos60MinusHue));
            red = (int) (255 * (1 + (1 - cosHue / cos60MinusHue)));
            green = 0;
        }

        // Convert the "PercentType" to a percent
        double saturationPercent = (hsb.getSaturation().doubleValue() / 100);

        // Calculate the white intensity from saturation and adjust down the other colors
        if (saturationPercent < 0.5) {
            // At less than 50% saturation, maximize white and lower the other intensities by 2x of the saturation percent. (2x to give us full range between 0-50%)
            white = 255;
            red = (int) (red * (2*saturationPercent));
            green = (int) (green * (2*saturationPercent));
            blue = (int) (blue * (2*saturationPercent));
        } else {
            // At >50% saturation, colors are at full and increase saturation by decreasing the white intensity.
            white = (int) (255 *2*(1 - saturationPercent));
        }

        // Note: We're keeping the brightness in a totally separate channel
        logger.trace("Outgoing R: {} G: {} B: {} W: {}", red, green, blue, white);
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
     * Totally made this up.
     *
     * @return a {@link ColorRequestParam} with the color components
     */
    public HSBType rgbwDimmingToHSB(int red, int green, int blue, int white, int dimming) {
        logger.trace("Converting RGB to HS. Incoming R: {} G: {} B: {} W: {}", red, green, blue, white);
        // Can get hue from the ratios of the colors.
        // The calculated *hue* component of the HSB should be correct regardless of the
        // state of the white lights because it's strictly based on the ratio of the colors
        DecimalType hue = HSBType.fromRGB(red, green, blue).getHue();
        double saturationPercent;
        if (white < 255) {
            saturationPercent = (int) (1 - (white / (255 * 2)));
        } else {
            saturationPercent = Math.max(red, Math.max(green, blue)) / (255*2);
        }
        return new HSBType(hue, new PercentType((int) saturationPercent*100), new PercentType(dimming));
    }
}
