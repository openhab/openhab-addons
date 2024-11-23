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
package org.openhab.binding.bigassfan.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PercentType;

/**
 * The {@link BigAssFanConverter} is responsible for converting between
 * Dimmer values and values used for fan speed, light brightness, and
 * light color temperature.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class BigAssFanConverter {
    /*
     * Conversion factor for fan range (0-7) to dimmer range (0-100).
     */
    private static final double SPEED_CONVERSION_FACTOR = 14.2857;

    /*
     * Conversion factor for light range (0-16) to dimmer range (0-100).
     */
    private static final double BRIGHTNESS_CONVERSION_FACTOR = 6.25;

    /*
     * Conversion factor for hue range (2200-5000) to dimmer range (0-100).
     */
    private static final double HUE_CONVERSION_FACTOR = 28.0;

    /*
     * Dimmer item will produce PercentType value, which is 0-100
     * Convert that value to what the fan expects, which is 0-7
     */
    public static String percentToSpeed(PercentType command) {
        return String.valueOf((int) Math.round(command.doubleValue() / SPEED_CONVERSION_FACTOR));
    }

    /*
     * Fan will supply fan speed value in range of 0-7
     * Convert that value to a PercentType in range 0-100, which is what Dimmer item expects
     */
    public static PercentType speedToPercent(String speed) {
        return new PercentType((int) Math.round(Integer.parseInt(speed) * SPEED_CONVERSION_FACTOR));
    }

    /*
     * Dimmer item will produce PercentType value, which is 0-100
     * Convert that value to what the light expects, which is 0-16
     */
    public static String percentToLevel(PercentType command) {
        return String.valueOf((int) Math.round(command.doubleValue() / BRIGHTNESS_CONVERSION_FACTOR));
    }

    /*
     * Light will supply brightness value in range of 0-16
     * Convert that value to a PercentType in range 0-100, which is what Dimmer item expects
     */
    public static PercentType levelToPercent(String level) {
        return new PercentType((int) Math.round(Integer.parseInt(level) * BRIGHTNESS_CONVERSION_FACTOR));
    }

    /*
     * Dimmer item will produce PercentType value, which is 0-100
     * Convert that value to what the light expects, which is 2200-5000
     */
    public static String percentToHue(PercentType command) {
        return String.valueOf(2200 + (int) Math.round(command.doubleValue() * HUE_CONVERSION_FACTOR));
    }

    /*
     * Light will supply hue value in range of 2200-5000
     * Convert that value to a PercentType in range 0-100, which is what Dimmer item expects
     */
    public static PercentType hueToPercent(String hue) {
        return new PercentType((int) Math.round((Integer.parseInt(hue) - 2200) / HUE_CONVERSION_FACTOR));
    }
}
