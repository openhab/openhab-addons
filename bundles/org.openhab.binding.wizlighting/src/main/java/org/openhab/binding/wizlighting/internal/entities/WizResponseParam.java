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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.wizlighting.internal.enums.*;

/**
 * This POJO represents the "params" of one WiZ Lighting Response "params" are
 * returned for sync and heartbeat packets
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizResponseParam implements Param {

    // The current color mode of the bulb
    // We will assume by default that it's a single color bulb
    public WizLightingColorMode colorMode = WizLightingColorMode.SetColor;

    /*
     * Bulb metadata in the param of all incoming messages from the bulb
     */
    // The MAC address of the bulb
    public @Nullable String mac;
    // The source of a command
    // Possibilites: "udp" (in response to UDP command) "hb" (regular heartbeat)
    public @Nullable String src;

    /*
     * Extra Information only in 'hb' params
     */
    // Not sure exactly what this means, seems to be a boolean
    // I believe the bulb communicates with the WiZ servers via MQTT
    public int mqttCd;

    /*
     * Extra Information only in 'firstBeat' params
     */
    // Firmware version of the bulb
    public @Nullable String fwVersion;
    // Home ID of the bulb
    public int homeId;

    /*
     * If the response is the result from a comman, a success field is returned
     */
    // Whether the change was successful
    public boolean success = false;

    /*
     * Bulb state information - not all fields are populated
     */

    // The bulb's WiFi signal strength
    public int rssi;
    // The overall state of the bulb - on/off
    public boolean state;
    // The numeric identifier for a preset lighting mode
    public int sceneId;
    // Unknown - not seen by SRGD
    public boolean play;
    // The speed of color changes in dynamic lighting modes
    public int speed;
    // Strength of the red channel (0-255)
    public int r;
    // Strength of the green channel (0-255)
    public int g;
    // Strength of the blue channel (0-255)
    public int b;
    // Intensity of the cool whilte LED's (0-255)
    public int c;
    // Intensity of the warm whilte LED's (0-255)
    public int w;
    // Dimming percent (10-100)
    public int dimming;
    // Color temperature - sent in place of r/g/b/c/w
    // If temperatures are sent, color LED's are not in use
    public int temp;
    // Indicates if the light mode is applied following a pre-set "rhythm"
    public int schdPsetId;

    public WizLightingColorMode getColorMode() {
        if (r != 0 || g != 0 || b != 0) {
            return WizLightingColorMode.RGBMode;
        } else if (temp != 0) {
            return WizLightingColorMode.CTMode;
        } else {
            return WizLightingColorMode.SetColor;
        }
    }

    public HSBType getHSBColor() {
        if (getColorMode() == WizLightingColorMode.RGBMode) {
            HSBType newColor = HSBType.fromRGB(r, g, b);
            return newColor;
        } else {
            // If a rgb color isn't returned, simply call it simply white.
            // Do not attempt any conversions given a color temperature.
            return new HSBType("WHITE");
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
}
