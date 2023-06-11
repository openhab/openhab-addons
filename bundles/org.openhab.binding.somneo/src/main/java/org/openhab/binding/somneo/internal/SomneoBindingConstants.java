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
package org.openhab.binding.somneo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SomneoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class SomneoBindingConstants {

    public static final String BINDING_ID = "somneo";

    // List of all Thing properties
    public static final String PROPERTY_VENDOR_NAME = "Philips";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HF367X = new ThingTypeUID(BINDING_ID, "hf367x");

    // List of all Channel ids
    public static final String CHANNEL_AUDIO_AUX = "audio#aux";
    public static final String CHANNEL_AUDIO_FREQUENCY = "audio#frequency";
    public static final String CHANNEL_AUDIO_PRESET = "audio#preset";
    public static final String CHANNEL_AUDIO_RADIO = "audio#radio";
    public static final String CHANNEL_AUDIO_VOLUME = "audio#volume";
    public static final String CHANNEL_LIGHT_MAIN = "light#main";
    public static final String CHANNEL_LIGHT_NIGHT = "light#night";
    public static final String CHANNEL_RELAX_BREATHING_RATE = "relax#breathingRate";
    public static final String CHANNEL_RELAX_DURATION = "relax#duration";
    public static final String CHANNEL_RELAX_GUIDANCE_TYPE = "relax#guidanceType";
    public static final String CHANNEL_RELAX_LIGHT_INTENSITY = "relax#lightIntensity";
    public static final String CHANNEL_RELAX_REMAINING_TIME = "relax#remainingTime";
    public static final String CHANNEL_RELAX_SWITCH = "relax#switch";
    public static final String CHANNEL_RELAX_VOLUME = "relax#volume";
    public static final String CHANNEL_SENSOR_ILLUMINANCE = "sensor#illuminance";
    public static final String CHANNEL_SENSOR_HUMIDITY = "sensor#humidity";
    public static final String CHANNEL_SENSOR_NOISE = "sensor#noise";
    public static final String CHANNEL_SENSOR_TEMPERATURE = "sensor#temperature";
    public static final String CHANNEL_SUNSET_AMBIENT_NOISE = "sunset#ambientNoise";
    public static final String CHANNEL_SUNSET_COLOR_SCHEMA = "sunset#colorSchema";
    public static final String CHANNEL_SUNSET_DURATION = "sunset#duration";
    public static final String CHANNEL_SUNSET_LIGHT_INTENSITY = "sunset#lightIntensity";
    public static final String CHANNEL_SUNSET_REMAINING_TIME = "sunset#remainingTime";
    public static final String CHANNEL_SUNSET_SWITCH = "sunset#switch";
    public static final String CHANNEL_SUNSET_VOLUME = "sunset#volume";

    // List of all Web Service Endpoints
    public static final String AUDIO_ENDPOINT = "/1/wuply";
    public static final String DEVICE_ENDPOINT = "/1/device";
    public static final String FIRMWARE_ENDPOINT = "/0/firmware";
    public static final String LIGHT_ENDPOINT = "/1/wulgt";
    public static final String PRESET_ENDPOINT = "/1/wufmp/00";
    public static final String RADIO_ENDPOINT = "/1/wufmr";
    public static final String RELAX_ENDPOINT = "/1/wurlx";
    public static final String TIMER_ENDPOINT = "/1/wutmr";
    public static final String SENSORS_ENDPOINT = "/1/wusrd";
    public static final String SUNSET_ENDPOINT = "/1/wudsk";
    public static final String WIFI_ENDPOINT = "/0/wifi";
}
