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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The types of color modes a JSONSchemaLight can support.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public enum LightColorMode {
    @SerializedName("onoff")
    COLOR_MODE_ONOFF,
    @SerializedName("brightness")
    COLOR_MODE_BRIGHTNESS,
    @SerializedName("color_temp")
    COLOR_MODE_COLOR_TEMP,
    @SerializedName("hs")
    COLOR_MODE_HS,
    @SerializedName("xy")
    COLOR_MODE_XY,
    @SerializedName("rgb")
    COLOR_MODE_RGB,
    @SerializedName("rgbw")
    COLOR_MODE_RGBW,
    @SerializedName("rgbww")
    COLOR_MODE_RGBWW,
    @SerializedName("white")
    COLOR_MODE_WHITE;

    public static final List<LightColorMode> WITH_RGB = List.of(COLOR_MODE_RGB, COLOR_MODE_RGBW, COLOR_MODE_RGBWW);
    public static final List<LightColorMode> WITH_COLOR_CHANNEL = List.of(COLOR_MODE_HS, COLOR_MODE_RGB,
            COLOR_MODE_RGBW, COLOR_MODE_RGBWW, COLOR_MODE_XY);

    /**
     * Determines if the list of supported modes includes any that should generate an openHAB Color channel
     */
    public static boolean hasColorChannel(List<LightColorMode> supportedColorModes) {
        return WITH_COLOR_CHANNEL.stream().anyMatch(cm -> supportedColorModes.contains(cm));
    }

    /**
     * Determins if the list of supported modes includes any that have RGB components
     */
    public static boolean hasRGB(List<LightColorMode> supportedColorModes) {
        return WITH_RGB.stream().anyMatch(cm -> supportedColorModes.contains(cm));
    }

    public String serializedName() {
        try {
            return LightColorMode.class.getDeclaredField(toString()).getAnnotation(SerializedName.class).value();
        } catch (NoSuchFieldException e) {
            // can't happen
            throw new IllegalStateException(e);
        }
    }
}
