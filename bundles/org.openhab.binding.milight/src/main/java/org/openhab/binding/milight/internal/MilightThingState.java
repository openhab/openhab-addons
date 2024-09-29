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
package org.openhab.binding.milight.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains a led bulb state including the HSB value, white color temperature and animation values.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MilightThingState {
    public int animationMode;
    public int colorTemperature; // only for led bulbs which include white leds applicable
    public int brightness;
    public int hue360; // only for rgb(w) leds applicable (v3+)
    public int saturation; // only for rgbww leds applicable (v6+)

    public MilightThingState() {
        reset();
    }

    public void reset() {
        animationMode = 0;
        colorTemperature = 100; // only for led bulbs which include white leds applicable
        brightness = 100;
        hue360 = 180; // only for rgb(w) leds applicable (v3+)
        saturation = 100; // only for rgbww leds applicable (v6+)
    }
}
