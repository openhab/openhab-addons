/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bondhome.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This POJO represents the Bond Device state
 *
 * The incoming JSON looks like this:
 *
 * { "breeze": [ 1, 0.2, 0.9 ], "brightness": 75, "light": 1, "power": 0,
 * "speed": 2, "timer": 3599 }
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondDeviceState {
    // The current state hash
    @SerializedName("_")
    @Expose(serialize = false, deserialize = true)
    public @Nullable String hash;

    // The device power state 1 = on, 0 = off
    @Expose(serialize = true, deserialize = true)
    public int power;

    // The seconds remaining on timer, or 0 meaning no timer running
    @Expose(serialize = true, deserialize = true)
    public int timer;

    // The fan speed - value from 1 to max_speed. If power=0, speed represents the
    // last speed setting and the speed to which the device resumes when user asks
    // to turn on.
    @Expose(serialize = true, deserialize = true)
    public int speed;

    // The current breeze setting (for a ceiling fan)
    // array of the form[<mode>,<mean>,<var>]:
    // mode: (integer) 0 = breeze mode disabled, 1 = breeze mode enabled
    // mean: (integer) sets the average speed. 0 = minimum average speed (calm), 100 = maximum average speed (storm)
    // var: (integer) sets the variability of the speed. 0 = minimum variation (steady), 100 = maximum variation (gusty)
    @Expose(serialize = true, deserialize = true)
    public int[] breeze = { 0, 50, 50 };

    // The direction of a fan with a reversible motor 1 = forward, -1 = reverse.
    // The forward and reverse modes are sometimes called Summer and Winter, respectively.
    @Expose(serialize = true, deserialize = true)
    public int direction;

    // The fan light state 1 = light on, 0 = light off
    @Expose(serialize = true, deserialize = true)
    public int light;

    // Whether separate up and down lights are enabled, if applicable
    // 1 = enabled, 0 = disabled
    // If both up_light and light are 1, then the up light will be on, and similar for down light.
    @SerializedName("up_light")
    @Expose(serialize = true, deserialize = true)
    public int upLight;

    @SerializedName("down_light")
    @Expose(serialize = true, deserialize = true)
    public int downLight;

    // The brightness of a fan light or lights
    @Expose(serialize = true, deserialize = true)
    public int brightness;

    @Expose(serialize = true, deserialize = true)
    public int upLightBrightness;

    @Expose(serialize = true, deserialize = true)
    public int downLightBrightness;

    // The flame level of a fireplace - value from 1 to 100. If power=0, flame represents the last flame setting and
    // the flame to which the device resumes when user asks to turn on
    @Expose(serialize = true, deserialize = true)
    public int flame;

    // Whether a device is open or closed (for motorized shades and garage doors)
    // 1 = open, 0 = closed
    @Expose(serialize = true, deserialize = true)
    public int open;

    // Fan settings for a fireplace fan
    // fpfan_power: (integer) 1 = on, 0 = off
    // fpfan_speed: (integer) from 1-100
    @SerializedName("fpfan_power")
    @Expose(serialize = true, deserialize = true)
    public int fpfanPower;

    @SerializedName("fpfan_speed")
    @Expose(serialize = true, deserialize = true)
    public int fpfanSpeed;
}
