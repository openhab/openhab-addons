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
package org.openhab.binding.wizlighting.internal.entities;

import static org.openhab.binding.wizlighting.internal.WizLightingBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.wizlighting.internal.enums.*;
import org.openhab.binding.wizlighting.internal.utils.WizColorConverter;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents the "params" of one WiZ Lighting Response "params" are
 * returned for sync and heartbeat packets
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class SyncResponseParam {
    // The MAC address the response is coming from
    @Expose(serialize = true, deserialize = true)
    public String mac = "bulbMacAddress";

    // The current color mode of the bulb
    // We will assume by default that it's a single color bulb
    @Expose(serialize = false, deserialize = false)
    public WizLightingColorMode colorMode = WizLightingColorMode.SingleColorMode;
    @Expose(serialize = false, deserialize = false)
    private WizColorConverter colorConverter = new WizColorConverter();

    /*
     * Extra Information only in 'hb' params
     */
    // Not sure exactly what this means, seems to be a boolean
    // I believe the bulb communicates with the WiZ servers via MQTT
    @Expose(serialize = true, deserialize = true)
    public int mqttCd;

    /*
     * Bulb state information - not all fields are populated
     */

    // The bulb's WiFi signal strength
    @Expose(serialize = true, deserialize = true)
    public int rssi;
    // The overall state of the bulb - on/off
    @Expose(serialize = true, deserialize = true)
    public boolean state;
    // The numeric identifier for a preset lighting mode
    @Expose(serialize = true, deserialize = true)
    public int sceneId;
    // Unknown - not seen by SRGD
    @Expose(serialize = true, deserialize = true)
    public boolean play;
    // The speed of color changes in dynamic lighting modes
    @Expose(serialize = true, deserialize = true)
    public int speed;
    // Strength of the red channel (0-255)
    @Expose(serialize = true, deserialize = true)
    public int r;
    // Strength of the green channel (0-255)
    @Expose(serialize = true, deserialize = true)
    public int g;
    // Strength of the blue channel (0-255)
    @Expose(serialize = true, deserialize = true)
    public int b;
    // Intensity of the cool white channel (0-255)
    @Expose(serialize = true, deserialize = true)
    public int c;
    // Intensity of the warm white channel (0-255)
    @Expose(serialize = true, deserialize = true)
    public int w;
    // Dimming percent (10-100)
    @Expose(serialize = true, deserialize = true)
    public int dimming;
    // Color temperature - sent in place of r/g/b/c/w
    // If temperatures are sent, color LED's are not in use
    @Expose(serialize = true, deserialize = true)
    public int temp;
    // Indicates if the light mode is applied following a pre-set "rhythm"
    @Expose(serialize = true, deserialize = true)
    public int schdPsetId;

    public WizLightingColorMode getColorMode() {
        if (r != 0 || g != 0 || b != 0) {
            return WizLightingColorMode.RGBMode;
        } else if (temp != 0) {
            return WizLightingColorMode.CTMode;
        } else {
            return WizLightingColorMode.SingleColorMode;
        }
    }

    public HSBType getHSBColor() {
        if (getColorMode() == WizLightingColorMode.RGBMode) {
            HSBType newColor = colorConverter.rgbwDimmingToHSB(r, g, b, w, dimming);
            // NOTE: The WiZ bulbs do not use the cool white LED's in full color mode.
            return newColor;
        } else {
            // If a rgb color isn't returned, simply call it simply white.
            // Do not attempt any conversions given a color temperature.
            return HSBType.WHITE;
        }
    }

    public PercentType getTemperaturePercent() {
        if (getColorMode() == WizLightingColorMode.CTMode) {
            return new PercentType((temp - MIN_COLOR_TEMPERATURE) / COLOR_TEMPERATURE_RANGE * 100);
        } else {
            // If a color temperature isn't returned, just set it to 50%.
            // Don't attempt any fancy conversions.
            return new PercentType(50);
        }
    }

    public int getDimming() {
        return this.dimming;
    }
}
