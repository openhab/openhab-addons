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
package org.openhab.binding.dirigera.internal.handler.light;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;

/**
 * The {@link TempToRgb} is holding all information to execute a new light command
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TempToRgb {

    /**
     * https://www.npmjs.com/package/color-temperature?activeTab=code
     *
     * @param kelvin
     * @return
     */
    public static HSBType getTemperatureOpt1(long kelvin) {
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
        return HSBType.fromRGB((int) Math.round(red), (int) Math.round(green), (int) Math.round(blue));
    }
}
