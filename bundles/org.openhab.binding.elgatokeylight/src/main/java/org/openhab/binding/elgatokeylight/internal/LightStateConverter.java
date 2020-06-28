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
package org.openhab.binding.elgatokeylight.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.elgatokeylight.internal.ElgatoKeyLight.LightStatus;

/**
 * The {@link LightStateConverter} is responsible for mapping OpenHAB types to
 * Elgato types and vice versa. (Copied and adapted from Hue binding)
 *
 * @author see org.openhab.binding.hue.internal.handler.LightStateConverter
 * @author Gunnar Wagenknecht - adapted
 */
@NonNullByDefault
public class LightStateConverter {

    private static final int MIN_COLOR_TEMPERATURE = 143;
    private static final int MAX_COLOR_TEMPERATURE = 344;
    private static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;

    private static final int DIM_STEPSIZE = 30;

    private static int restrictToBounds(final int percentValue) {
        if (percentValue < 0) {
            return 0;
        } else if (percentValue > 100) {
            return 100;
        }
        return percentValue;
    }

    /**
     * Adjusts the given brightness using the {@link IncreaseDecreaseType} and
     * returns the updated value.
     *
     * @param command           The {@link IncreaseDecreaseType} to be used
     * @param currentBrightness The current brightness
     * @return The adjusted brightness value
     */
    public static int toAdjustedBrightness(final IncreaseDecreaseType command, final int currentBrightness) {
        int newBrightness;
        if (command == IncreaseDecreaseType.DECREASE) {
            newBrightness = Math.max(currentBrightness - DIM_STEPSIZE, 0);
        } else {
            newBrightness = Math.min(currentBrightness + DIM_STEPSIZE, 100);
        }
        return newBrightness;
    }

    /**
     * Adjusts the given color temperature using the {@link IncreaseDecreaseType}
     * and returns the updated value.
     *
     * @param type             The {@link IncreaseDecreaseType} to be used
     * @param currentColorTemp The current color temperature
     * @return The adjusted color temperature value
     */
    public static int toAdjustedColorTemp(final IncreaseDecreaseType type, final int currentColorTemp) {
        int newColorTemp;
        if (type == IncreaseDecreaseType.DECREASE) {
            newColorTemp = Math.max(currentColorTemp - DIM_STEPSIZE, MIN_COLOR_TEMPERATURE);
        } else {
            newColorTemp = Math.min(currentColorTemp + DIM_STEPSIZE, MAX_COLOR_TEMPERATURE);
        }
        return newColorTemp;
    }

    /**
     * Transforms Hue Light {@link State} into {@link PercentType} representing the
     * brightness.
     *
     * @param lightState light state
     * @return percent type representing the brightness
     */
    public static PercentType toBrightnessPercentType(final LightStatus lightState) {
        return new PercentType(restrictToBounds(lightState.brightness));
    }

    /**
     * Transforms the given {@link PercentType} into a light state containing the
     * color temperature represented by {@link PercentType}.
     *
     * @param percentType color temperature represented as {@link PercentType}
     */
    public static int toColorTemperatureLightState(final PercentType percentType) {
        int colorTemperature = MIN_COLOR_TEMPERATURE
                + Math.round((COLOR_TEMPERATURE_RANGE * percentType.floatValue()) / 100);
        return colorTemperature;
    }

    /**
     * Transforms Hue Light {@link State} into {@link PercentType} representing the
     * color temperature.
     *
     * @param lightState light state
     * @return percent type representing the color temperature
     */
    public static PercentType toColorTemperaturePercentType(final LightStatus lightState) {
        int percent = (int) Math
                .round(((lightState.temperature - MIN_COLOR_TEMPERATURE) * 100.0) / COLOR_TEMPERATURE_RANGE);
        return new PercentType(restrictToBounds(percent));
    }

    /**
     * Transforms the given {@link OnOffType} into a light state containing the 'on'
     * value.
     *
     * @param onOffType on or off state
     * @return light state containing the 'on' value
     */
    public static int toOnOffLightState(final OnOffType onOffType) {
        return OnOffType.ON.equals(onOffType) ? 1 : 0;
    }
}
