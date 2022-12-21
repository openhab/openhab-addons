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

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This enum represents the possible device actions
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public enum BondDeviceAction {

    // State Variables
    // power: (integer) 1 = on, 0 = off
    // Actions
    @SerializedName("TurnOn")
    TURN_ON("TurnOn", CHANNEL_GROUP_COMMON, CHANNEL_POWER),
    // ^^ Turn device power on.
    @SerializedName("TurnOff")
    TURN_OFF("TurnOff", CHANNEL_GROUP_COMMON, CHANNEL_POWER),
    // ^^ Turn device power off.
    @SerializedName("TogglePower")
    TOGGLE_POWER("TogglePower", CHANNEL_GROUP_COMMON, CHANNEL_POWER),
    // ^^ Change device power from on to off, or off to on.

    // State Variables
    // timer: (integer) seconds remaining on timer, or 0 meaning no timer running
    // Actions
    @SerializedName("SetTimer")
    SET_TIMER("SetTimer", CHANNEL_GROUP_COMMON, CHANNEL_FAN_TIMER),
    // ^^ Start timer for s seconds. If power if off, device is implicitly turned
    // on. If argument is zero, the timer is
    // canceled without turning off the device.

    // Properties
    // max_speed: (integer) highest speed available
    // State Variables
    // speed: (integer) value from 1 to max_speed. If power=0, speed represents the
    // last speed setting and the speed to
    // which the device resumes when user asks to turn on.
    // Actions
    @SerializedName("SetSpeed")
    SET_SPEED("SetSpeed", CHANNEL_GROUP_FAN, CHANNEL_FAN_SPEED),
    // ^^ Set speed and turn on. If speed>max_speed, max_speed is assumed. If the
    // fan is off, implicitly turn on the
    // power. Setting speed to zero or a negative value is ignored.
    @SerializedName("IncreaseSpeed")
    INCREASE_SPEED("IncreaseSpeed", CHANNEL_GROUP_FAN, CHANNEL_FAN_SPEED),
    // ^^ Increase speed of fan by specified number of speeds. If the fan is off,
    // implicitly turn on the power.
    @SerializedName("DecreaseSpeed")
    DECREASE_SPEED("DecreaseSpeed", CHANNEL_GROUP_FAN, CHANNEL_FAN_SPEED),
    // ^^ Decrease fan speed by specified number of speeds. If attempting to
    // decrease fan speed below 1, the fan will
    // remain at speed 1. That is, power will not be implicitly turned off. If the
    // power is already off, DecreaseSpeed
    // is ignored.

    // State Variables
    // breeze: (array) array of the form [ <mode>, <mean>, <var> ]:
    // mode: (integer) 0 = breeze mode disabled, 1 = breeze mode enabled
    // mean: (integer) sets the average speed. 0 = minimum average speed (calm), 100
    // = maximum average speed (storm)
    // var: (integer) sets the variability of the speed. 0 = minimum variation
    // (steady), 100 = maximum variation (gusty)
    // Actions
    @SerializedName("BreezeOn")
    BREEZE_ON("BreezeOn", CHANNEL_GROUP_FAN, CHANNEL_FAN_BREEZE_STATE),
    // ^^ Enable breeze with remembered parameters. Defaults to [50,50].
    @SerializedName("BreezeOff")
    BREEZE_OFF("BreezeOff", CHANNEL_GROUP_FAN, CHANNEL_FAN_BREEZE_STATE),
    // ^^ Stop breeze. Fan remains on at current speed.
    @SerializedName("SetBreeze")
    SET_BREEZE("SetBreeze", CHANNEL_GROUP_FAN, CHANNEL_FAN_BREEZE_MEAN),
    // ^^ Enable breeze with specified parameters (same as breeze state variable).
    // Example SetBreeze([1, 20, 90]).

    // State Variables
    // direction: (integer) 1 = forward, -1 = reverse.
    // The forward and reverse modes are sometimes called Summer and Winter,
    // respectively.
    // Actions
    @SerializedName("SetDirection")
    SET_DIRECTION("SetDirection", CHANNEL_GROUP_FAN, CHANNEL_FAN_DIRECTION),
    // ^^ Control forward and reverse.
    @SerializedName("ToggleDirection")
    TOGGLE_DIRECTION("ToggleDirection", CHANNEL_GROUP_FAN, CHANNEL_FAN_DIRECTION),
    // ^^ Reverse the direction of the fan.

    // State Variables
    // light: (integer) 1 = light on, 0 = light off
    // Actions
    @SerializedName("TurnLightOn")
    TURN_LIGHT_ON("TurnLightOn", CHANNEL_GROUP_LIGHT, CHANNEL_LIGHT_POWER),
    // ^^ Turn light on.
    @SerializedName("TurnLightOff")
    TURN_LIGHT_OFF("TurnLightOff", CHANNEL_GROUP_LIGHT, CHANNEL_LIGHT_POWER),
    // ^^ Turn off light.
    @SerializedName("ToggleLight")
    TOGGLE_LIGHT("ToggleLight", CHANNEL_GROUP_LIGHT, CHANNEL_LIGHT_POWER),
    // ^^ Change light from on to off, or off to on.

    // State Variables
    // up_light: (integer) 1 = up light enabled, 0 = up light disabled
    // down_light: (integer) 1 = down light enabled, 0 = down light disabled
    // If both up_light and light are 1, then the up light will be on, and similar
    // for down light.
    // Note that both up_light and down_light may not be simultaneously zero, so
    // that the device is always ready to
    // respond to a TurnLightOn request.
    // Actions
    @SerializedName("TurnUpLightOn")
    TURN_UP_LIGHT_ON("TurnUpLightOn", CHANNEL_GROUP_UP_LIGHT, CHANNEL_UP_LIGHT_ENABLE),
    // ^^ Turn up light on.
    @SerializedName("TurnDownLightOn")
    TURN_DOWN_LIGHT_ON("TurnDownLightOn", CHANNEL_GROUP_DOWN_LIGHT, CHANNEL_DOWN_LIGHT_ENABLE),
    // ^^ Turn down light on.
    @SerializedName("TurnUpLightOff")
    TURN_UP_LIGHT_OFF("TurnUpLightOff", CHANNEL_GROUP_UP_LIGHT, CHANNEL_UP_LIGHT_POWER),
    // ^^ Turn off up light.
    @SerializedName("TurnDownLightOff")
    TURN_DOWN_LIGHT_OFF("TurnDownLightOff", CHANNEL_GROUP_DOWN_LIGHT, CHANNEL_DOWN_LIGHT_POWER),
    // ^^ Turn off down light.
    @SerializedName("ToggleUpLight")
    TOGGLE_UP_LIGHT("ToggleUpLight", CHANNEL_GROUP_UP_LIGHT, CHANNEL_UP_LIGHT_POWER),
    // ^^ Change up light from on to off, or off to on.
    @SerializedName("ToggleDownLight")
    TOGGLE_DOWN_LIGHT("ToggleDownLight", CHANNEL_GROUP_DOWN_LIGHT, CHANNEL_DOWN_LIGHT_POWER),
    // ^^ Change down light from on to off, or off to on.

    // State Variables
    // brightness: (integer) percentage value of brightness, 1-100. If light=0,
    // brightness represents the last
    // brightness setting and the brightness to resume when user turns on light. If
    // fan has no dimmer or a non-stateful
    // dimmer, brightness is always 100.
    // Actions
    @SerializedName("SetBrightness")
    SET_BRIGHTNESS("SetBrightness", CHANNEL_GROUP_LIGHT, CHANNEL_LIGHT_BRIGHTNESS),
    // ^^ Set the brightness of the light to specified percentage. Value of 0 is
    // ignored, use TurnLightOff instead.
    @SerializedName("IncreaseBrightness")
    INCREASE_BRIGHTNESS("IncreaseBrightness", CHANNEL_GROUP_LIGHT, CHANNEL_LIGHT_BRIGHTNESS),
    // will be turned on at (0 + amount).
    DECREASE_BRIGHTNESS("DecreaseBrightness", CHANNEL_GROUP_LIGHT, CHANNEL_LIGHT_BRIGHTNESS),
    // ^^ Decrease light brightness by specified percentage. If attempting to
    // decrease brightness below 1%, light will
    // remain at 1%. Use TurnLightOff to turn off the light. If the light is off,
    // the light will remain off but the
    // remembered brightness will be decreased.

    // State Variables
    // up_light_brightness: (integer) percentage value of up light brightness,
    // 1-100.
    // down_light_brightness: (integer) percentage value of down light brightness,
    // 1-100.
    // Actions
    @SerializedName("SetUpLightBrightness")
    SET_UP_LIGHT_BRIGHTNESS("SetUpLightBrightness", CHANNEL_GROUP_UP_LIGHT, CHANNEL_UP_LIGHT_BRIGHTNESS),
    // ^^ Similar to SetBrightness but only for the up light.
    @SerializedName("SetDownLightBrightness")
    SET_DOWN_LIGHT_BRIGHTNESS("SetDownLightBrightness", CHANNEL_GROUP_DOWN_LIGHT, CHANNEL_DOWN_LIGHT_BRIGHTNESS),
    // ^^ Similar to SetBrightness but only for the down light.
    @SerializedName("IncreaseUpLightBrightness")
    INCREASE_UP_LIGHT_BRIGHTNESS("IncreaseUpLightBrightness", CHANNEL_GROUP_UP_LIGHT, CHANNEL_UP_LIGHT_BRIGHTNESS),
    // ^^ Similar to IncreaseBrightness but only for the up light.
    @SerializedName("InreaseDownLightBrightness")
    INCREASE_DOWN_LIGHT_BRIGHTNESS("IncreaseDownLightBrightness", CHANNEL_GROUP_DOWN_LIGHT,
            CHANNEL_DOWN_LIGHT_BRIGHTNESS),
    // ^^ Similar to IncreaseBrightness but only for the down light.
    @SerializedName("DecreaseUpLightBrightness")
    DECREASE_UP_LIGHT_BRIGHTNESS("DecreaseUpLightBrightness", CHANNEL_GROUP_UP_LIGHT, CHANNEL_UP_LIGHT_BRIGHTNESS),
    // ^^ Similar to DecreaseBrightness but only for the up light.
    @SerializedName("DecreaseDownLightBrightness")
    DECREASE_DOWN_LIGHT_BRIGHTNESS("DecreaseDownLightBrightness", CHANNEL_GROUP_DOWN_LIGHT,
            CHANNEL_DOWN_LIGHT_BRIGHTNESS),
    // ^^ Similar to DecreaseBrightness but only for the down light.

    // State Variables
    // flame: (integer) value from 1 to 100. If power=0, flame represents the last
    // flame setting and the flame to which
    // the device resumes when user asks to turn on.
    // Actions
    @SerializedName("SetFlame")
    SET_FLAME("SetFlame", CHANNEL_GROUP_FIREPLACE, CHANNEL_FLAME),
    // ^^ Set flame and turn on. If flame>100, 100 is assumed. If the fireplace is
    // off, implicitly turn on the power.
    // Setting flame to zero or a negative value is ignored.
    @SerializedName("IncreaseFlame")
    INCREASE_FLAME("IncreaseFlame", CHANNEL_GROUP_FIREPLACE, CHANNEL_FLAME),
    // ^^ Increase flame level of fireplace by specified number of flames. If the
    // fireplace is off, implicitly turn on
    // the power.
    @SerializedName("DecreaseFlame")
    DECREASE_FLAME("DecreaseFlame", CHANNEL_GROUP_FIREPLACE, CHANNEL_FLAME),
    // ^^ Decrease flame level by specified number of flames. If attempting to
    // decrease fireplace flame below 1, the
    // fireplace will remain at flame 1. That is, power will not be implicitly
    // turned off. If the power is already off,
    // DecreaseFlame is ignored.

    // State Variables
    // fpfan_power: (integer) 1 = on, 0 = off
    // fpfan_speed: (integer) from 1-100
    // Actions
    @SerializedName("TurnFpFanOff")
    TURN_FP_FAN_OFF("TurnFpFanOff", CHANNEL_GROUP_FAN, CHANNEL_FAN_SPEED),
    // ^^ Turn the fireplace fan off
    @SerializedName("TurnFpFanOn")
    TURN_FP_FAN_ON("TurnFpFanOn", CHANNEL_GROUP_FAN, CHANNEL_FAN_POWER),
    // ^^ Turn the fireplace fan on, restoring the previous speed
    @SerializedName("SetFpFan")
    SET_FP_FAN("SetFpFan", CHANNEL_GROUP_FAN, CHANNEL_FAN_SPEED),
    // ^^ Sets the speed of the fireplace fan

    // State Variables
    // open: (integer) 1 = open, 0 = closed
    // Actions
    @SerializedName("Open")
    OPEN("Open", CHANNEL_GROUP_SHADES, CHANNEL_ROLLERSHUTTER),
    // ^^ Open the device.
    @SerializedName("Close")
    CLOSE("Close", CHANNEL_GROUP_SHADES, CHANNEL_ROLLERSHUTTER),
    // ^^ Close the device.
    @SerializedName("ToggleOpen")
    TOGGLE_OPEN("ToggleOpen", CHANNEL_GROUP_SHADES, CHANNEL_ROLLERSHUTTER),
    // ^^ Close the device if it's open, open it if it's closed
    @SerializedName("Preset")
    PRESET("Preset", CHANNEL_GROUP_COMMON, CHANNEL_COMMAND),
    // ^^ Sets a shade to a preset level

    // Other actions
    @SerializedName("Stop")
    STOP("Stop", CHANNEL_GROUP_COMMON, CHANNEL_COMMAND),
    // ^^ This action tells the Bond to stop any in-progress transmission and empty
    // its transmission queue.
    @SerializedName("Hold")
    HOLD("Hold", CHANNEL_GROUP_SHADES, CHANNEL_COMMAND),
    // ^^ Can be used when a signal is required to tell a device to stop moving or
    // the like, since Stop is a special
    // "stop transmitting" action
    @SerializedName("Pair")
    PAIR("Pair", CHANNEL_GROUP_COMMON, null),
    // ^^ Used in devices that need to be paired with a receiver.
    @SerializedName("StartDimmer")
    START_DIMMER("StartDimmer", CHANNEL_GROUP_COMMON, CHANNEL_COMMAND),
    // ^^ Start dimming. The Bond should time out its transmission after 30 seconds,
    // or when the Stop action is called.
    @SerializedName("StartUpLightDimmer")
    START_UP_LIGHT_DIMMER("StartUpLightDimmer", CHANNEL_GROUP_COMMON, CHANNEL_COMMAND),
    // ^^ Use this and the StartDownLightDimmer instead of StartDimmer if your
    // device has two dimmable lights.
    @SerializedName("StartDownLightDimmer")
    START_DOWN_LIGHT_DIMMER("StartDownLightDimmer", CHANNEL_GROUP_COMMON, CHANNEL_COMMAND),
    // ^^ The counterpart to StartUpLightDimmer
    @SerializedName("StartIncreasingBrightness")
    START_INCREASING_BRIGHTNESS("StartIncreasingBrightness", CHANNEL_GROUP_COMMON, CHANNEL_COMMAND),
    @SerializedName("StartDecreasingBrightness")
    START_DECREASING_BRIGHTNESS("StartDecreasingBrightness", CHANNEL_GROUP_COMMON, CHANNEL_COMMAND),

    // More actions
    @SerializedName("OEMRandom")
    OEM_RANDOM("OEMRandom", CHANNEL_GROUP_COMMON, null),
    @SerializedName("OEMTimer")
    OEM_TIMER("OEMTimer", CHANNEL_GROUP_COMMON, null),
    @SerializedName("Unknown")
    UNKNOWN("Unknown", CHANNEL_GROUP_COMMON, null);

    private String actionId;
    private String channelGroupTypeId;
    private @Nullable String channelTypeId;

    private BondDeviceAction(final String actionId, String channelGroupTypeId, @Nullable String channelTypeId) {
        this.actionId = actionId;
        this.channelGroupTypeId = channelGroupTypeId;
        this.channelTypeId = channelTypeId;
    }

    /**
     * @return the actionId
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * @return the channelGroupTypeId
     */
    public String getChannelGroupTypeId() {
        return channelGroupTypeId;
    }

    /**
     * @return the channelTypeId
     */
    public @Nullable String getChannelTypeId() {
        return channelTypeId;
    }
}
