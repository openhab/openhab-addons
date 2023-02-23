/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tplinksmarthome.internal.model.GetRealtime;
import org.openhab.binding.tplinksmarthome.internal.model.GetSysinfo;
import org.openhab.binding.tplinksmarthome.internal.model.GsonUtil;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;
import org.openhab.binding.tplinksmarthome.internal.model.Realtime;
import org.openhab.binding.tplinksmarthome.internal.model.SetBrightness;
import org.openhab.binding.tplinksmarthome.internal.model.SetLedOff;
import org.openhab.binding.tplinksmarthome.internal.model.SetLightState;
import org.openhab.binding.tplinksmarthome.internal.model.SetRelayState;
import org.openhab.binding.tplinksmarthome.internal.model.SetSwitchState;
import org.openhab.binding.tplinksmarthome.internal.model.Sysinfo;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState.LightOnOff;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState.LightStateBrightness;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState.LightStateColor;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightState.LightStateColorTemperature;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightStateResponse;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.Gson;

/**
 * Class to construct the tp-link json commands and convert retrieved results into data objects.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Rewritten to use gson to parse json
 */
@NonNullByDefault
public class Commands {

    private static final String CONTEXT = "{\"context\":{\"child_ids\":[\"%s\"]},";
    private static final String SYSTEM_GET_SYSINFO = "\"system\":{\"get_sysinfo\":{}}";
    private static final String GET_SYSINFO = "{" + SYSTEM_GET_SYSINFO + "}";
    private static final String REALTIME = "\"emeter\":{\"get_realtime\":{}}";
    private static final String GET_REALTIME_AND_SYSINFO = "{" + SYSTEM_GET_SYSINFO + ", " + REALTIME + "}";
    private static final String GET_REALTIME_BULB_AND_SYSINFO = "{" + SYSTEM_GET_SYSINFO
            + ", \"smartlife.iot.common.emeter\":{\"get_realtime\":{}}}";

    private final Gson gson = GsonUtil.createGson();
    private final Gson gsonWithExpose = GsonUtil.createGsonWithExpose();

    /**
     * Returns the json to get the energy and sys info data from the device.
     *
     * @return The json string of the command to send to the device
     */
    public static String getRealtimeAndSysinfo() {
        return GET_REALTIME_AND_SYSINFO;
    }

    /**
     * Returns the json to get the energy and sys info data from the bulb.
     *
     * @return The json string of the command to send to the bulb
     */
    public static String getRealtimeBulbAndSysinfo() {
        return GET_REALTIME_BULB_AND_SYSINFO;
    }

    /**
     * Returns the json to get the energy and sys info data from an outlet device.
     *
     * @param id optional id of the device
     * @return The json string of the command to send to the device
     */
    public static String getRealtimeWithContext(final String id) {
        return String.format(CONTEXT, id) + REALTIME + "}";
    }

    /**
     * Returns the json response of the get_realtime command to the data object.
     *
     * @param realtimeResponse the json string
     * @return The data object containing the energy data from the json string
     */
    @SuppressWarnings("null")
    public Realtime getRealtimeResponse(final String realtimeResponse) {
        final GetRealtime getRealtime = gson.fromJson(realtimeResponse, GetRealtime.class);
        return getRealtime == null ? new Realtime() : getRealtime.getRealtime();
    }

    /**
     * Returns the json to get the sys info data from the device.
     *
     * @return The json string of the command to send to the device
     */
    public static String getSysinfo() {
        return GET_SYSINFO;
    }

    /**
     * Returns the json response of the get_sysinfo command to the data object.
     *
     * @param getSysinfoReponse the json string
     * @return The data object containing the state data from the json string
     */
    @SuppressWarnings("null")
    public Sysinfo getSysinfoReponse(final String getSysinfoReponse) {
        final GetSysinfo getSysinfo = gson.fromJson(getSysinfoReponse, GetSysinfo.class);
        return getSysinfo == null ? new Sysinfo() : getSysinfo.getSysinfo();
    }

    /**
     * Returns the json for the set_relay_state command to switch on or off.
     *
     * @param onOff the switch state to set
     * @param childId optional child id if multiple children are supported by a single device
     * @return The json string of the command to send to the device
     */
    public String setRelayState(final OnOffType onOff, @Nullable final String childId) {
        final SetRelayState relayState = new SetRelayState();
        relayState.setRelayState(onOff);
        if (childId != null) {
            relayState.setChildId(childId);
        }
        return gsonWithExpose.toJson(relayState);
    }

    /**
     * Returns the json response of the set_relay_state command to the data object.
     *
     * @param relayStateResponse the json string
     * @return The data object containing the state data from the json string
     */
    public @Nullable SetRelayState setRelayStateResponse(final String relayStateResponse) {
        return gsonWithExpose.fromJson(relayStateResponse, SetRelayState.class);
    }

    /**
     * Returns the json for the set_switch_state command to switch a dimmer on or off.
     *
     * @param onOff the switch state to set
     * @return The json string of the command to send to the device
     */
    public String setSwitchState(final OnOffType onOff) {
        final SetSwitchState switchState = new SetSwitchState();
        switchState.setSwitchState(onOff);
        return gsonWithExpose.toJson(switchState);
    }

    /**
     * Returns the json response of the set_switch_state command to the data object.
     *
     * @param switchStateResponse the json string
     * @return The data object containing the state data from the json string
     */
    public @Nullable SetSwitchState setSwitchStateResponse(final String switchStateResponse) {
        return gsonWithExpose.fromJson(switchStateResponse, SetSwitchState.class);
    }

    /**
     * Returns the json for the set_brightness command to set the brightness value.
     *
     * @param brightness the brightness value to set
     * @return The json string of the command to send to the device
     */
    public String setDimmerBrightness(final int brightness) {
        final SetBrightness setBrightness = new SetBrightness();
        setBrightness.setBrightness(brightness);
        return gsonWithExpose.toJson(setBrightness);
    }

    /**
     * Returns the json response of the set_brightness command to the data object.
     *
     * @param dimmerBrightnessResponse the json string
     * @return The data object containing the state data from the json string
     */
    public @Nullable HasErrorResponse setDimmerBrightnessResponse(final String dimmerBrightnessResponse) {
        return gsonWithExpose.fromJson(dimmerBrightnessResponse, SetBrightness.class);
    }

    /**
     * Returns the json for the set_led_off command to switch the led of the device on or off.
     *
     * @param onOff the led state to set
     * @param childId optional child id if multiple children are supported by a single device
     * @return The json string of the command to send to the device
     */
    public String setLedOn(final OnOffType onOff, @Nullable final String childId) {
        final SetLedOff sLOff = new SetLedOff();
        sLOff.setLed(onOff);
        if (childId != null) {
            sLOff.setChildId(childId);
        }
        return gsonWithExpose.toJson(sLOff);
    }

    /**
     * Returns the json response for the set_led_off command to the data object.
     *
     * @param setLedOnResponse the json string
     * @return The data object containing the data from the json string
     */
    public @Nullable SetLedOff setLedOnResponse(final String setLedOnResponse) {
        return gsonWithExpose.fromJson(setLedOnResponse, SetLedOff.class);
    }

    /**
     * Returns the json for the transition_light_state command to switch a bulb on or off.
     *
     * @param onOff the switch state to set
     * @param transitionPeriod the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setTransitionLightState(final OnOffType onOff, final int transitionPeriod) {
        return setTransitionLightState(new LightOnOff(), onOff, transitionPeriod);
    }

    /**
     * Returns the json for the set_light_State command to set the brightness.
     *
     * @param brightness the brightness value
     * @param transitionPeriod the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setTransitionLightStateBrightness(final int brightness, final int transitionPeriod) {
        final LightStateBrightness lightState = new LightStateBrightness();
        lightState.setBrightness(brightness);
        return setTransitionLightState(lightState, OnOffType.from(brightness != 0), transitionPeriod);
    }

    /**
     * Returns the json for the set_light_State command to set the color.
     *
     * @param hsb the color to set
     * @param transitionPeriod the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setTransitionLightStateColor(final HSBType hsb, final int transitionPeriod) {
        final LightStateColor lightState = new LightStateColor();
        final int brightness = hsb.getBrightness().intValue();
        lightState.setBrightness(brightness);
        lightState.setHue(hsb.getHue().intValue());
        lightState.setSaturation(hsb.getSaturation().intValue());
        return setTransitionLightState(lightState, OnOffType.from(brightness != 0), transitionPeriod);
    }

    /**
     * Returns the json for the set_light_State command to set the color temperature.
     *
     * @param colorTemperature the color temperature to set
     * @param transitionPeriod the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setColorTemperature(final int colorTemperature, final int transitionPeriod) {
        final LightStateColorTemperature lightState = new LightStateColorTemperature();
        lightState.setColorTemperature(colorTemperature);
        return setTransitionLightState(lightState, OnOffType.ON, transitionPeriod);
    }

    private String setTransitionLightState(final LightOnOff lightOnOff, final OnOffType onOff,
            final int transitionPeriod) {
        final TransitionLightState transitionLightState = new TransitionLightState();
        transitionLightState.setLightState(lightOnOff);
        lightOnOff.setOnOff(onOff);
        lightOnOff.setTransitionPeriod(transitionPeriod);
        return gson.toJson(transitionLightState);
    }

    /**
     * Returns the json response for the set_light_state command.
     *
     * @param response the json string
     * @return The data object containing the state data from the json string
     */
    public @Nullable TransitionLightStateResponse setTransitionLightStateResponse(final String response) {
        return gson.fromJson(response, TransitionLightStateResponse.class);
    }

    // ---------------------------------------------------------------

    /**
     * Returns the json for the set_light_state command to switch a light strip on or off.
     *
     * @param onOff the switch state to set
     * @param transition the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setLightStripState(final OnOffType onOff, final int transition) {
        return setLightStripState(new SetLightState.LightOnOff(), onOff, transition);
    }

    /**
     * Returns the json for the set_light_State command to set the brightness.
     *
     * @param brightness the brightness value
     * @param transition the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setLightStripBrightness(final int brightness, final int transition) {
        final SetLightState.Brightness lightState = new SetLightState.Brightness();
        lightState.setBrightness(brightness);
        return setLightStripState(lightState, OnOffType.from(brightness != 0), transition);
    }

    /**
     * Returns the json for the set_light_State command to set the color.
     *
     * @param hsb the color to set
     * @param transition the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setLightStripColor(final HSBType hsb, final int transition) {
        final SetLightState.Color lightState = new SetLightState.Color();
        final int brightness = hsb.getBrightness().intValue();
        lightState.setHue(hsb.getHue().intValue());
        lightState.setSaturation(hsb.getSaturation().intValue());
        lightState.setBrightness(brightness);
        return setLightStripState(lightState, OnOffType.from(brightness != 0), transition);
    }

    /**
     * Returns the json for the set_light_State command to set the color temperature.
     *
     * @param colorTemperature the color temperature to set
     * @param transition the transition period for the action to take place
     * @return The json string of the command to send to the device
     */
    public String setLightStripColorTemperature(final int colorTemperature, final int transition) {
        final SetLightState.ColorTemperature lightState = new SetLightState.ColorTemperature();
        lightState.setColorTemp(colorTemperature);
        return setLightStripState(lightState, OnOffType.ON, transition);
    }

    private String setLightStripState(final SetLightState.LightOnOff lightOnOff, final OnOffType onOff,
            final int transition) {
        final SetLightState setLightState = new SetLightState();
        setLightState.setContext(new SetLightState.Context());
        setLightState.setLightState(lightOnOff);
        lightOnOff.setOnOff(onOff);
        lightOnOff.setTransition(transition);
        return gsonWithExpose.toJson(setLightState);
    }

    /**
     * Returns the json response for the set_light_state command.
     *
     * @param response the json string
     * @return The data object containing the state data from the json string
     */
    public @Nullable SetLightState setLightStripStateResponse(final String response) {
        return gsonWithExpose.fromJson(response, SetLightState.class);
    }
}
