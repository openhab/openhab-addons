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
package org.openhab.binding.nest.internal.sdm;

import static java.util.Map.entry;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nest.internal.sdm.dto.SDMDeviceType;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SDMBindingConstants} class defines common constants, which are used for the SDM implementation in the
 * binding.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMBindingConstants {

    private static final String BINDING_ID = "nest";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "sdm_account");
    public static final ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID(BINDING_ID, "sdm_camera");
    public static final ThingTypeUID THING_TYPE_DISPLAY = new ThingTypeUID(BINDING_ID, "sdm_display");
    public static final ThingTypeUID THING_TYPE_DOORBELL = new ThingTypeUID(BINDING_ID, "sdm_doorbell");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "sdm_thermostat");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_CAMERA,
            THING_TYPE_DISPLAY, THING_TYPE_DOORBELL, THING_TYPE_THERMOSTAT);

    // Maps SDM device types to Thing Types UIDs
    public static final Map<SDMDeviceType, ThingTypeUID> SDM_THING_TYPE_MAPPING = Map.ofEntries(
            entry(SDMDeviceType.CAMERA, THING_TYPE_CAMERA), //
            entry(SDMDeviceType.DISPLAY, THING_TYPE_DISPLAY), //
            entry(SDMDeviceType.DOORBELL, THING_TYPE_DOORBELL), //
            entry(SDMDeviceType.THERMOSTAT, THING_TYPE_THERMOSTAT));

    // List of all Channel ids
    public static final String CHANNEL_CHIME_EVENT_IMAGE = "chime_event#image";
    public static final String CHANNEL_CHIME_EVENT_TIMESTAMP = "chime_event#timestamp";
    public static final String CHANNEL_LIVE_STREAM_URL = "live_stream#url";
    public static final String CHANNEL_LIVE_STREAM_CURRENT_TOKEN = "live_stream#current_token";
    public static final String CHANNEL_LIVE_STREAM_EXPIRATION_TIMESTAMP = "live_stream#expiration_timestamp";
    public static final String CHANNEL_LIVE_STREAM_EXTENSION_TOKEN = "live_stream#extension_token";
    public static final String CHANNEL_MOTION_EVENT_IMAGE = "motion_event#image";
    public static final String CHANNEL_MOTION_EVENT_TIMESTAMP = "motion_event#timestamp";
    public static final String CHANNEL_PERSON_EVENT_IMAGE = "person_event#image";
    public static final String CHANNEL_PERSON_EVENT_TIMESTAMP = "person_event#timestamp";
    public static final String CHANNEL_SOUND_EVENT_IMAGE = "sound_event#image";
    public static final String CHANNEL_SOUND_EVENT_TIMESTAMP = "sound_event#timestamp";

    public static final String CHANNEL_AMBIENT_HUMIDITY = "ambient_humidity";
    public static final String CHANNEL_AMBIENT_TEMPERATURE = "ambient_temperature";
    public static final String CHANNEL_CURRENT_ECO_MODE = "current_eco_mode";
    public static final String CHANNEL_CURRENT_MODE = "current_mode";
    public static final String CHANNEL_FAN_TIMER_MODE = "fan_timer_mode";
    public static final String CHANNEL_FAN_TIMER_TIMEOUT = "fan_timer_timeout";
    public static final String CHANNEL_HVAC_STATUS = "hvac_status";
    public static final String CHANNEL_MAXIMUM_TEMPERATURE = "maximum_temperature";
    public static final String CHANNEL_MINIMUM_TEMPERATURE = "minimum_temperature";
    public static final String CHANNEL_TARGET_TEMPERATURE = "target_temperature";

    // List of all configuration property IDs
    public static final String CONFIG_PROPERTY_FAN_TIMER_DURATION = "fanTimerDuration";
    public static final String CONFIG_PROPERTY_IMAGE_HEIGHT = "imageHeight";
    public static final String CONFIG_PROPERTY_IMAGE_WIDTH = "imageWidth";

    // List of all property IDs
    public static final String PROPERTY_AUDIO_CODECS = "audioCodecs";
    public static final String PROPERTY_CUSTOM_NAME = "customName";
    public static final String PROPERTY_MAX_IMAGE_RESOLUTION = "maxImageResolution";
    public static final String PROPERTY_MAX_VIDEO_RESOLUTION = "maxVideoResolution";
    public static final String PROPERTY_SUPPORTED_PROTOCOLS = "supportedProtocols";
    public static final String PROPERTY_ROOM = "room";
    public static final String PROPERTY_TEMPERATURE_SCALE = "temperatureScale";
    public static final String PROPERTY_VIDEO_CODECS = "videoCodecs";
}
