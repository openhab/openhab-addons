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
package org.openhab.binding.milight.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.milight.internal.MilightThingState;

/**
 * The {@link LedHandlerInterface} defines the general interface for all Milight bulbs.
 * The different versions might support additional functionality.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface LedHandlerInterface {
    /**
     * Set the color value of this bulb.
     *
     * @param hue A value from 0 to 360
     * @param saturation A saturation value. Can be -1 if not known
     * @param brightness A brightness value. Can be -1 if not known
     * @param state The changed values will be written back to the state
     */
    void setHSB(int hue, int saturation, int brightness, MilightThingState state);

    /**
     * Enable/Disable the bulb.
     *
     * @param on On/Off
     * @param state The changed values will be written back to the state
     */
    void setPower(boolean on, MilightThingState state);

    /**
     * Switches to white mode (disables color leds).
     *
     * @param state The changed values will be written back to the state
     */
    void whiteMode(MilightThingState state);

    /**
     * Switches to night mode (low current for all leds).
     *
     * @param state The changed values will be written back to the state
     */
    void nightMode(MilightThingState state);

    /**
     * Sets the color temperature of the bulb.
     *
     * @param colorTemp Color temperature percentage
     * @param state The changed values will be written back to the state
     */
    void setColorTemperature(int colorTemp, MilightThingState state);

    void changeColorTemperature(int colorTempRelative, MilightThingState state);

    /**
     * Sets the brightness of the bulb.
     *
     * @param value brightness percentage
     * @param state The changed values will be written back to the state
     */
    void setBrightness(int value, MilightThingState state);

    void changeBrightness(int relativeBrightness, MilightThingState state);

    void setSaturation(int value, MilightThingState state);

    void changeSaturation(int relativeSaturation, MilightThingState state);

    void setLedMode(int mode, MilightThingState state);

    void previousAnimationMode(MilightThingState state);

    void nextAnimationMode(MilightThingState state);

    void changeSpeed(int relativeSpeed, MilightThingState state);
}
