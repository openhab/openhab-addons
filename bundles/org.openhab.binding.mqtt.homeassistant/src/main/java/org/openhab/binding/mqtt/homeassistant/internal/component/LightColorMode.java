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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The types of color modes a JSONSchemaLight can support.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class LightColorMode {
    public static final String COLOR_MODE_ONOFF = "onoff";
    public static final String COLOR_MODE_BRIGHTNESS = "brightness";
    public static final String COLOR_MODE_COLOR_TEMP = "color_temp";
    public static final String COLOR_MODE_HS = "hs";
    public static final String COLOR_MODE_XY = "xy";
    public static final String COLOR_MODE_RGB = "rgb";
    public static final String COLOR_MODE_RGBW = "rgbw";
    public static final String COLOR_MODE_RGBWW = "rgbww";
    public static final String COLOR_MODE_WHITE = "white";

    public static final List<String> WITH_RGB = List.of(COLOR_MODE_RGB, COLOR_MODE_RGBW, COLOR_MODE_RGBWW);
    public static final List<String> WITH_COLOR_CHANNEL = List.of(COLOR_MODE_HS, COLOR_MODE_RGB, COLOR_MODE_RGBW,
            COLOR_MODE_RGBWW, COLOR_MODE_XY);

    /**
     * Determines if the list of supported modes includes any that should generate an openHAB Color channel
     */
    public static boolean hasColorChannel(Collection<String> supportedColorModes) {
        return WITH_COLOR_CHANNEL.stream().anyMatch(cm -> supportedColorModes.contains(cm));
    }

    /**
     * Determins if the list of supported modes includes any that have RGB components
     */
    public static boolean hasRGB(Collection<String> supportedColorModes) {
        return WITH_RGB.stream().anyMatch(cm -> supportedColorModes.contains(cm));
    }
}
