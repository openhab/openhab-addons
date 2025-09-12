/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.wiz.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.util.ColorUtil;

/**
 * Utilities for converting colors and color temperatures
 *
 * The full color WiZ bulbs can produce colors and various temperatures of
 * "whites" by mixing any of the available LEDs: RGBWwarm = RGBWWCwarm = Red,
 * Green, Blue, Warm White, Cool White. When operating in full color mode, the
 * warm whites are used to increase saturation (RGBW style). Temperatures of
 * white can also be called directly as K instead of mixing cw/ww (c/w) The
 * colors and temperatures need to be converted to the HSBType/PercentType
 * supported by openHAB.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public class WizColorConverter {
    /**
     * Converts an {@link DecimalType} hue and a {@link PercentType} saturation to
     * red, green, blue, and white (RGBW) components. Because the WiZ bulbs keep
     * dimming in a separate channel, we only take account hue and saturation for
     * the color channels. In RGB mode the WiZ bulbs only use the warm
     * white channel, the cool white channel is ignored. I have also found that adding
     * the warm white channel washes out the color too much to be useful. Leaving
     * warm white set to 0 seems to give the best color representation across the full
     * saturation range.
     *
     * NOTE: This method must be kept in sync with rgbwDimmingToHSB in order for the
     * color representation to remain accurate between conversions in both directions.
     *
     * @param hsbColor the {@link HSBType}.
     *
     * @return an interger array of the color components
     */
    public int[] hsbToRgbw(HSBType hsb) {
        // Only use RGB, ignore white
        HSBType hsbFull = new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.HUNDRED);
        PercentType[] rgbPercent = ColorUtil.hsbToRgbPercent(hsbFull);
        int red = (int) (rgbPercent[0].doubleValue());
        int green = (int) (rgbPercent[1].doubleValue());
        int blue = (int) (rgbPercent[2].doubleValue());
        int white = 0;
        return new int[] { red, green, blue, white };
    }

    /**
     * Converts Red/Green/Blue/White components to Hue and saturation.
     *
     * @param int red - the value of the red component (0-255)
     * @param int green - the value of the green component (0-255)
     * @param int blue - the value of the blue component (0-255)
     * @param int white - the value of the white component (0-255)
     * @param int dimming - the brightness of the bulb, independent of the RGB color (0-100)
     *
     * NOTE: This method must be kept in sync with hsbToRgbw in order for the
     * color representation to remain accurate between conversions in both directions.
     *
     * @return a {@link HSBType} with the color components
     */
    public HSBType rgbwDimmingToHSB(int red, int green, int blue, int white, int dimming) {
        // Only use RGB for saturation
        double r = red / 255.0;
        double g = green / 255.0;
        double b = blue / 255.0;
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double saturation = (max == 0) ? 0 : (max - min) / max;
        int saturationPercent = (int) (saturation * 100.0);
        DecimalType hue = HSBType.fromRGB(red, green, blue).getHue();
        HSBType out = new HSBType(hue, new PercentType(saturationPercent), new PercentType(dimming));
        return out;
    }
}
