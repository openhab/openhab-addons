/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * This POJO represents the "params" of one WiZ Lighting Response
 * "params" are returned for sync and heartbeat packets
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class SyncResponseParam implements Param {
    // The MAC address of the bulb
    public @Nullable String mac;
    // The bulb's WiFi signal strength
    public int rssi;
    // The source of a command
    // Possibilites: "udp" (in response to UDP command) "hb" (regular heartbeat)
    public @Nullable String src;
    // Not sure, seems to be a boolean
    public int mqttCd;
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
    // Firmware version of the bulb
    public @Nullable String fwVersion;

    public PercentType getTemperatureColor() {
        return new PercentType((temp - 2200) / (6500 - 2200) * 100);
    }
}
