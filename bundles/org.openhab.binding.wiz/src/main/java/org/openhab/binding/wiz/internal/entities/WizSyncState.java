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
package org.openhab.binding.wiz.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wiz.internal.enums.WizColorMode;
import org.openhab.binding.wiz.internal.utils.WizColorConverter;
import org.openhab.core.library.types.HSBType;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents the "params" of the current state of a WiZ bulb.
 * These are retruned as the "params" in getPilot, sync, and heartbeat packets
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizSyncState {
    // The MAC address the response is coming from
    @Expose
    public String mac = "";

    // The current color mode of the bulb
    // We will assume by default that it's a single color bulb
    @Expose(serialize = false, deserialize = false)
    public WizColorMode colorMode = WizColorMode.SingleColorMode;
    @Expose(serialize = false, deserialize = false)
    private WizColorConverter colorConverter = new WizColorConverter();

    /*
     * Extra Information only in 'hb' params
     */
    // Not sure exactly what this means, seems to be a boolean
    // I believe the bulb communicates with the WiZ servers via MQTT
    @Expose
    public int mqttCd;

    /*
     * Bulb state information - not all fields are populated
     */

    // The bulb's WiFi signal strength
    @Expose
    public int rssi;
    // The overall state of the bulb - on/off
    @Expose
    public boolean state;
    // The numeric identifier for a preset lighting mode
    @Expose
    public int sceneId;
    // Unknown - not seen by SRGD
    @Expose
    public boolean play;
    // The speed of color changes in dynamic lighting modes
    @Expose
    public int speed;
    // Strength of the red channel (0-255)
    @Expose
    public int r;
    // Strength of the green channel (0-255)
    @Expose
    public int g;
    // Strength of the blue channel (0-255)
    @Expose
    public int b;
    // Intensity of the cool white channel (0-255)
    @Expose
    public int c;
    // Intensity of the warm white channel (0-255)
    @Expose
    public int w;
    // Dimming percent (10-100)
    @Expose
    public int dimming;
    // Color temperature - sent in place of r/g/b/c/w
    // If temperatures are sent, color LED's are not in use
    @Expose
    public int temp;
    // Indicates if the light mode is applied following a pre-set "rhythm"
    @Expose
    public int schdPsetId;

    @Expose
    public int fanState;
    @Expose
    public int fanSpeed;
    @Expose
    public int fanMode;
    @Expose
    public int fanRevrs;

    public WizColorMode getColorMode() {
        if (r != 0 || g != 0 || b != 0) {
            return WizColorMode.RGBMode;
        } else if (temp != 0) {
            return WizColorMode.CTMode;
        } else {
            return WizColorMode.SingleColorMode;
        }
    }

    public HSBType getHSBColor() {
        if (getColorMode() == WizColorMode.RGBMode) {
            HSBType newColor = colorConverter.rgbwDimmingToHSB(r, g, b, w, dimming);
            // NOTE: The WiZ bulbs do not use the cool white LED's in full color mode.
            return newColor;
        } else {
            // If a rgb color isn't returned, simply call it simply white.
            // Do not attempt any conversions given a color temperature.
            return HSBType.WHITE;
        }
    }

    public void setHSBColor(HSBType hsb) {
        this.dimming = hsb.getBrightness().intValue();
        int rgbw[] = colorConverter.hsbToRgbw(hsb);
        this.r = rgbw[0];
        this.g = rgbw[1];
        this.b = rgbw[2];
        this.w = rgbw[3];
        this.c = 0;
    }

    public int getTemperature() {
        return temp;
    }

    public void setTemperature(int temp) {
        this.temp = temp;
    }

    public int getDimming() {
        return this.dimming;
    }
}
