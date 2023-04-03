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
package org.openhab.binding.hue.internal;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HueBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Jochen Hiller - Added OSRAM Classic A60 RGBW
 * @author Markus Mazurczak - Added OSRAM PAR16 50
 * @author Andre Fuechsel - changed to generic thing types
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 */
@NonNullByDefault
public class HueBindingConstants {

    public static final String BINDING_ID = "hue";

    // List all Thing Type UIDs, related to the Hue Binding

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_ON_OFF_LIGHT = new ThingTypeUID(BINDING_ID, "0000");
    public static final ThingTypeUID THING_TYPE_ON_OFF_PLUG = new ThingTypeUID(BINDING_ID, "0010");
    public static final ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "0200");
    public static final ThingTypeUID THING_TYPE_COLOR_TEMPERATURE_LIGHT = new ThingTypeUID(BINDING_ID, "0220");
    public static final ThingTypeUID THING_TYPE_EXTENDED_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "0210");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "0100");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_PLUG = new ThingTypeUID(BINDING_ID, "0110");

    public static final ThingTypeUID THING_TYPE_DIMMER_SWITCH = new ThingTypeUID(BINDING_ID, "0820");
    public static final ThingTypeUID THING_TYPE_TAP_SWITCH = new ThingTypeUID(BINDING_ID, "0830");
    public static final ThingTypeUID THING_TYPE_CLIP_GENERIC_STATUS = new ThingTypeUID(BINDING_ID, "0840");
    public static final ThingTypeUID THING_TYPE_CLIP_GENERIC_FLAG = new ThingTypeUID(BINDING_ID, "0850");
    public static final ThingTypeUID THING_TYPE_PRESENCE_SENSOR = new ThingTypeUID(BINDING_ID, "0107");
    public static final ThingTypeUID THING_TYPE_GEOFENCE_SENSOR = new ThingTypeUID(BINDING_ID, "geofencesensor");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE_SENSOR = new ThingTypeUID(BINDING_ID, "0302");
    public static final ThingTypeUID THING_TYPE_LIGHT_LEVEL_SENSOR = new ThingTypeUID(BINDING_ID, "0106");
    public static final ThingTypeUID THING_TYPE_GROUP = new ThingTypeUID(BINDING_ID, "group");

    public static final Set<ThingTypeUID> V1_THING_TYPE_UIDS = Set.of(THING_TYPE_COLOR_LIGHT,
            THING_TYPE_COLOR_TEMPERATURE_LIGHT, THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_EXTENDED_COLOR_LIGHT,
            THING_TYPE_ON_OFF_LIGHT, THING_TYPE_ON_OFF_PLUG, THING_TYPE_DIMMABLE_PLUG, THING_TYPE_DIMMER_SWITCH,
            THING_TYPE_TAP_SWITCH, THING_TYPE_PRESENCE_SENSOR, THING_TYPE_TEMPERATURE_SENSOR,
            THING_TYPE_LIGHT_LEVEL_SENSOR, THING_TYPE_GROUP);

    // List all channels
    public static final String CHANNEL_COLORTEMPERATURE = "color_temperature";
    public static final String CHANNEL_COLORTEMPERATURE_ABS = "color_temperature_abs";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ALERT = "alert";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_DIMMER_SWITCH = "dimmer_switch";
    public static final String CHANNEL_TAP_SWITCH = "tap_switch";
    public static final String CHANNEL_PRESENCE = "presence";
    public static final String CHANNEL_ENABLED = "enabled";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_LAST_UPDATED = "last_updated";
    public static final String CHANNEL_BATTERY_LEVEL = "battery_level";
    public static final String CHANNEL_BATTERY_LOW = "battery_low";
    public static final String CHANNEL_ILLUMINANCE = "illuminance";
    public static final String CHANNEL_LIGHT_LEVEL = "light_level";
    public static final String CHANNEL_DARK = "dark";
    public static final String CHANNEL_DAYLIGHT = "daylight";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_FLAG = "flag";
    public static final String CHANNEL_SCENE = "scene";

    // List all triggers
    public static final String EVENT_DIMMER_SWITCH = "dimmer_switch_event";
    public static final String EVENT_TAP_SWITCH = "tap_switch_event";

    // Binding configuration properties
    public static final String REMOVAL_GRACE_PERIOD = "removalGracePeriod";

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String USER_NAME = "userName";

    // Thing configuration properties
    public static final String LIGHT_ID = "lightId";
    public static final String SENSOR_ID = "sensorId";
    public static final String PROPERTY_PRODUCT_NAME = "productName";
    public static final String UNIQUE_ID = "uniqueId";
    public static final String FADETIME = "fadetime";
    public static final String GROUP_ID = "groupId";

    // property names for CLIP 2
    public static final String PROPERTY_RESOURCE_ID = "resourceId";
    public static final String PROPERTY_RESOURCE_TYPE = "resourceType";
    public static final String PROPERTY_RESOURCE_NAME = "resourceName";
    public static final String PROPERTY_RESOURCE_ARCHETYPE = "resourceArchetype";
    public static final String PROPERTY_PRODUCT_ARCHETYPE = "productArchetype";
    public static final String PROPERTY_PRODUCT_CERTIFIED = "productCertified";
    public static final String PROPERTY_LEGACY_THING_UID = "legacyThingUID";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_OWNER_TYPE = "ownerType";

    public static final String NORMALIZE_ID_REGEX = "[^a-zA-Z0-9_]";

    //
    public static final String TEXT_OFFLINE_COMMUNICATION_ERROR = "@text/offline.communication-error";
    public static final String TEXT_OFFLINE_CONFIGURATION_ERROR_INVALID_SSL_CERIFICATE = "@text/offline.conf-error-invalid-ssl-certificate";

    // Config status messages
    public static final String IP_ADDRESS_MISSING = "missing-ip-address-configuration";

    // thing types for CLIP 2
    public static final ThingTypeUID THING_TYPE_CLIP2 = new ThingTypeUID(BINDING_ID, "clip2");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");

    // channels for CLIP 2
    public static final String CHANNEL_2_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_2_BATTERY_LOW = "batteryLow";
    public static final String CHANNEL_2_BUTTON_LAST_EVENT = "buttonLastEvent";
    public static final String CHANNEL_2_COLOR_TEMPERATURE = "colorTemperature";
    public static final String CHANNEL_2_COLOR_TEMPERATURE_ABS = "colorTemperatureAbs";
    public static final String CHANNEL_2_LAST_UPDATED = "lastUpdated";
    public static final String CHANNEL_2_LIGHT_LEVEL = "lightLevel";
    public static final String CHANNEL_2_LIGHT_LEVEL_ENABLED = "lightLevelEnabled";
    public static final String CHANNEL_2_MOTION = "motion";
    public static final String CHANNEL_2_MOTION_ENABLED = "motionEnabled";
    public static final String CHANNEL_2_ROTARY_STEPS = "rotarySteps";
    public static final String CHANNEL_2_TEMPERATURE_ENABLED = "temperatureEnabled";
    public static final String CHANNEL_2_ZIGBEE_STATUS = "zigbeeStatus";

    public static final String CLIP2_PROPERTY_SUFFIX = " (api2)";

    /*
     * Map of API v1 channel IDs against API v2 channel IDs where, if the v1 channel exists in the system, then we
     * should try to replicate the channel/item links from the v1 channel into the respective v2 channel.
     */
    public static final Map<String, String> REPLICATE_CHANNEL_ID_MAP = Stream.of(new String[][] { //
            { CHANNEL_BRIGHTNESS, CHANNEL_BRIGHTNESS }, //
            { CHANNEL_COLOR, CHANNEL_COLOR }, //
            { CHANNEL_COLORTEMPERATURE, CHANNEL_2_COLOR_TEMPERATURE }, //
            { CHANNEL_COLORTEMPERATURE_ABS, CHANNEL_2_COLOR_TEMPERATURE_ABS }, //
            { CHANNEL_SWITCH, CHANNEL_SWITCH }, //
            { CHANNEL_DIMMER_SWITCH, CHANNEL_2_BUTTON_LAST_EVENT }, //
            { CHANNEL_LIGHT_LEVEL, CHANNEL_2_LIGHT_LEVEL }, //
            { CHANNEL_PRESENCE, CHANNEL_2_MOTION }, //
            { CHANNEL_TEMPERATURE, CHANNEL_TEMPERATURE }, //
            { CHANNEL_BATTERY_LEVEL, CHANNEL_2_BATTERY_LEVEL }, //
            { CHANNEL_BATTERY_LOW, CHANNEL_2_BATTERY_LOW }, //
            { CHANNEL_LAST_UPDATED, CHANNEL_2_LAST_UPDATED }, //
            { CHANNEL_SCENE, CHANNEL_SCENE }, //
    }).collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));

    public static final String ALL_LIGHTS_KEY = "discovery.group.all_lights.label";
}
