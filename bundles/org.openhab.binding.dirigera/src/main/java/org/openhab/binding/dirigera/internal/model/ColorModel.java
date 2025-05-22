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
package org.openhab.binding.dirigera.internal.model;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ColorModel} converts colors according to DIRIGERA values. openHAB ColorUtil conversion uses XY
 * transformations which visually are not matching e.g. for kelvin2HSB values.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ColorModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorModel.class);

    private static final TreeMap<Integer, Integer> MAPPING_RGB_TEMPERETATURE = new TreeMap<>();
    private static final TreeMap<Integer, Integer> MAPPING_TEMPERETATURE_RGB = new TreeMap<>();
    private static final int MAX_HUE = 360;
    private static final int MAX_SAT = 100;

    /**
     * Simulate color-temperature if color light doesn't have the "canReceive" "colorTemperature" capability
     * https://www.npmjs.com/package/color-temperature?activeTab=code
     *
     * @param kelvin
     * @return color temperature as HSBType
     */
    private static int[] kelvin2RGB(long kelvin) {
        double temperature = kelvin / 100.0;
        double red;
        double green;
        double blue;
        /* Calculate red */
        if (temperature <= 66.0) {
            red = 255;
        } else {
            red = temperature - 60.0;
            red = 329.698727446 * Math.pow(red, -0.1332047592);
            if (red < 0) {
                red = 0;
            }
            if (red > 255) {
                red = 255;
            }
        }
        /* Calculate green */
        if (temperature <= 66.0) {
            green = temperature;
            green = 99.4708025861 * Math.log(green) - 161.1195681661;
            if (green < 0) {
                green = 0;
            }
            if (green > 255) {
                green = 255;
            }
        } else {
            green = temperature - 60.0;
            green = 288.1221695283 * Math.pow(green, -0.0755148492);
            if (green < 0) {
                green = 0;
            }
            if (green > 255) {
                green = 255;
            }
        }
        /* Calculate blue */
        if (temperature >= 66.0) {
            blue = 255;
        } else {
            if (temperature <= 19.0) {
                blue = 0;
            } else {
                blue = temperature - 10;
                blue = 138.5177312231 * Math.log(blue) - 305.0447927307;
                if (blue < 0) {
                    blue = 0;
                }
                if (blue > 255) {
                    blue = 255;
                }
            }
        }
        return new int[] { (int) Math.round(red), (int) Math.round(green), (int) Math.round(blue) };
    }

    private static void init() {
        if (MAPPING_RGB_TEMPERETATURE.isEmpty()) {
            for (int i = 1000; i < 10001; i = i + 10) {
                int rgbEncoding = encodeRGBValue(kelvin2RGB(i));
                MAPPING_RGB_TEMPERETATURE.put(rgbEncoding, i);
                MAPPING_TEMPERETATURE_RGB.put(i, rgbEncoding);
            }
        }
    }

    private static int encodeRGBValue(int[] rgb) {
        return rgb[0] * 1000000 + rgb[1] * 1000 + rgb[2];
    }

    private static int[] decodeRGBValue(int encoded) {
        int part = encoded;
        int red = part / 1000000;
        part -= red * 1000000;
        int green = part / 1000;
        part -= green * 1000;
        int blue = part;
        return new int[] { red, green, blue };
    }

    public static HSBType kelvin2Hsb(long kelvin) {
        init();
        Entry<Integer, Integer> entry = MAPPING_TEMPERETATURE_RGB.ceilingEntry((int) kelvin);
        if (entry == null) {
            entry = MAPPING_TEMPERETATURE_RGB.floorEntry((int) kelvin);
            if (entry == null) {
                // this path cannot be entered if tables isn't empty which is prevent by init call
                LOGGER.warn("DIRIGERA COLOR_MODEL no rgb mapping found for {}", kelvin);
                return new HSBType();
            }
        }
        int encoded = entry.getValue();
        int[] rgb = decodeRGBValue(encoded);
        return ColorUtil.rgbToHsb(rgb);
    }

    public static long hsb2Kelvin(HSBType hsb) {
        init();
        HSBType compare = new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.HUNDRED);
        int rgb[] = ColorUtil.hsbToRgb(compare);
        int key = encodeRGBValue(rgb);
        Entry<Integer, Integer> entry = MAPPING_RGB_TEMPERETATURE.ceilingEntry(key);
        if (entry == null) {
            entry = MAPPING_RGB_TEMPERETATURE.floorEntry(key);
            if (entry == null) {
                // this path cannot be entered if tables isn't empty which is prevent by init call
                LOGGER.warn("DIRIGERA COLOR_MODEL no kelvin mapping found for {}", compare);
                return -1;
            }
        }
        return entry.getValue();
    }

    public static boolean closeTo(HSBType refHSB, HSBType compareHSB, double percent) {
        double hueDistance = Math.abs(refHSB.getHue().doubleValue() - compareHSB.getHue().doubleValue());
        double saturationDistance = Math
                .abs(refHSB.getSaturation().doubleValue() - compareHSB.getSaturation().doubleValue());
        return ((hueDistance < (MAX_HUE * percent) || hueDistance > (MAX_HUE - (MAX_HUE * percent)))
                && saturationDistance < (MAX_SAT * percent));
    }
}
