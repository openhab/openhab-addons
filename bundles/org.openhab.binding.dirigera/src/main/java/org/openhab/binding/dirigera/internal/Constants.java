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
package org.openhab.binding.dirigera.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Constants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Constants {
    public static final String BINDING_ID = "dirigera";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "color-light");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE_LIGHT = new ThingTypeUID(BINDING_ID, "temperature-light");
    public static final ThingTypeUID THING_TYPE_MOTION_SENSOR = new ThingTypeUID(BINDING_ID, "motion-sensor");
    public static final ThingTypeUID THING_TYPE_LIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "light-sensor");
    public static final ThingTypeUID THING_TYPE_MOTION_LIGHT_SENSOR = new ThingTypeUID(BINDING_ID,
            "motion-light-sensor");
    public static final ThingTypeUID THING_TYPE_CONTACT_SENSOR = new ThingTypeUID(BINDING_ID, "contact-sensor");
    public static final ThingTypeUID THING_TYPE_PLUG = new ThingTypeUID(BINDING_ID, "plug");
    public static final ThingTypeUID THING_TYPE_SMART_PLUG = new ThingTypeUID(BINDING_ID, "smart-plug");
    public static final ThingTypeUID THING_TYPE_SPEAKER = new ThingTypeUID(BINDING_ID, "speaker");
    public static final ThingTypeUID THING_TYPE_SCENE = new ThingTypeUID(BINDING_ID, "scene");
    public static final ThingTypeUID THING_TYPE_REPEATER = new ThingTypeUID(BINDING_ID, "repeater");
    public static final ThingTypeUID THING_TYPE_LIGHT_CONTROLLER = new ThingTypeUID(BINDING_ID, "light-controller");
    public static final ThingTypeUID THING_TYPE_AIR_QUALITY = new ThingTypeUID(BINDING_ID, "air-quality");
    public static final ThingTypeUID THING_TYPE_WATER_SENSOR = new ThingTypeUID(BINDING_ID, "water-sensor");
    public static final ThingTypeUID THING_TYPE_UNKNNOWN = new ThingTypeUID(BINDING_ID, "unkown");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GATEWAY,
            THING_TYPE_COLOR_LIGHT, THING_TYPE_TEMPERATURE_LIGHT, THING_TYPE_MOTION_SENSOR, THING_TYPE_CONTACT_SENSOR,
            THING_TYPE_SMART_PLUG, THING_TYPE_SPEAKER, THING_TYPE_SCENE, THING_TYPE_REPEATER,
            THING_TYPE_LIGHT_CONTROLLER, THING_TYPE_MOTION_LIGHT_SENSOR, THING_TYPE_PLUG, THING_TYPE_AIR_QUALITY,
            THING_TYPE_WATER_SENSOR);

    public static final Set<ThingTypeUID> IGNORE_THING_TYPES_UIDS = Set.of(THING_TYPE_LIGHT_SENSOR);

    public static final List<String> THING_PROPERTIES = List.of("model", "manufacturer", "firmwareVersion",
            "hardwareVersion", "serialNumber", "productCode");

    public static final String WS_URL = "wss://%s:8443/v1";
    public static final String BASE_URL = "https://%s:8443/v1";
    public static final String OAUTH_URL = BASE_URL + "/oauth/authorize";
    public static final String TOKEN_URL = BASE_URL + "/oauth/token";
    public static final String HOME_URL = BASE_URL + "/home";
    public static final String DEVICE_URL = BASE_URL + "/devices/%s";
    public static final String SCENE_URL = BASE_URL + "/scenes/%s";

    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_DEVICES = "devices";
    public static final String PROPERTY_SCENES = "scenes";
    public static final String PROPERTY_DEVICE_ID = "id";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_TOKEN = "token";
    public static final String PROPERTY_ATTRIBUTES = "attributes";
    public static final String PROPERTY_HTTP_ERROR_STATUS = "http-error-status";
    public static final String PROPERTY_OTA_STATUS = "otaStatus";
    public static final String PROPERTY_OTA_STATE = "otaState";
    public static final String PROPERTY_OTA_PROGRESS = "otaProgress";
    public static final String PROPERTY_BATTERY_PERCENTAGE = "batteryPercentage";
    public static final String PROPERTY_PERMIT_JOIN = "permittingJoin";

    public static final String PROPERTY_EMPTY = "";

    public static final String ATTRIBUTE_COLOR_MODE = "colorMode";

    public static final String DEVICE_TYPE_GATEWAY = "gateway";
    public static final String DEVICE_TYPE_LIGHT = "light";
    public static final String DEVICE_TYPE_MOTION_SENSOR = "motionSensor";
    public static final String DEVICE_TYPE_LIGHT_SENSOR = "lightSensor";
    public static final String DEVICE_TYPE_CONTACT_SENSOR = "openCloseSensor";
    public static final String DEVICE_TYPE_OUTLET = "outlet";
    public static final String DEVICE_TYPE_SPEAKER = "speaker";
    public static final String DEVICE_TYPE_REPEATER = "repeater";
    public static final String DEVICE_TYPE_LIGHT_CONTROLLER = "lightController";
    public static final String DEVICE_TYPE_ENVIRONMENT_SENSOR = "environmentSensor";
    public static final String DEVICE_TYPE_WATER_SENSOR = "waterSensor";
    public static final String TYPE_USER_SCENE = "userScene";

    // Gateway channels
    public static final String CHANNEL_STATISTICS = "statistics";

    // Generic channels
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_OTA_STATUS = "ota-status";
    public static final String CHANNEL_OTA_STATE = "ota-state";
    public static final String CHANNEL_OTA_PROGRESS = "ota-progress";

    // Gateway channels
    public static final String CHANNEL_SUNRISE = "sunrise";
    public static final String CHANNEL_SUNSET = "sunset";
    public static final String CHANNEL_PAIRING = "pairing";

    // Light channels
    public static final String CHANNEL_LIGHT_HSB = "hsb";
    public static final String CHANNEL_LIGHT_BRIGHTNESS = "brightness";
    public static final String CHANNEL_LIGHT_TEMPERATURE = "temperature";

    // Sensor channels
    public static final String CHANNEL_DETECTION = "detection";
    public static final String CHANNEL_ILLUMINANCE = "illuminance";

    // Plug channels
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_CURRENT = "ampere";
    public static final String CHANNEL_POTENTIAL = "voltage";
    public static final String CHANNEL_CHILD_LOCK = "child-lock";
    public static final String CHANNEL_DISABLE_STATUS_LIGHT = "disable-light";

    // Speaker channels
    public static final String CHANNEL_PLAYER = "player";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_TRACK = "track";
    public static final String CHANNEL_PLAY_MODES = "modes";
    public static final String CHANNEL_SHUFFLE = "shuffle";
    public static final String CHANNEL_REPEAT = "repeat";
    public static final String CHANNEL_CROSSFADE = "crossfade";
    public static final String CHANNEL_IMAGE = "image";

    // Scene channels
    public static final String CHANNEL_TRIGGER = "trigger";
    public static final String CHANNEL_LAST_TRIGGER = "last-trigger";

    // Air quality channels
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_PARTICULATE_MATTER = "particulate-matter";
    public static final String CHANNEL_VOC_INDEX = "voc-index";

    // Websocket update types
    public static final String EVENT_TYPE_DEVICE_DISCOVERED = "deviceDiscovered";
    public static final String EVENT_TYPE_DEVICE_ADDED = "deviceAdded";
    public static final String EVENT_TYPE_DEVICE_CHANGE = "deviceStateChanged";
    public static final String EVENT_TYPE_DEVICE_REMOVED = "deviceRemoved";

    public static final String EVENT_TYPE_SCENE_CREATED = "sceneCreated";
    public static final String EVENT_TYPE_SCENE_UPDATE = "sceneUpdated";
    public static final String EVENT_TYPE_SCENE_DELETED = "sceneDeleted";

    /**
     * Maps connecting device attributes to channel ids
     */

    // Mappings for ota
    public static final Map<String, Integer> OTA_STATUS_MAP = Map.of("upToDate", 0, "updateAvailable", 1);
    public static final Map<String, Integer> OTA_STATE_MAP = Map.of("readyToCheck", 0, "checkInProgress", 1,
            "readyToDownload", 2, "downloadInProgress", 3);

    // Ikea property to openHAB channel mappings
    public static final Map<String, String> COLOR_LIGHT_MAP = Map.of("isOn", CHANNEL_STATE, "lightLevel",
            CHANNEL_LIGHT_HSB, "colorHue", CHANNEL_LIGHT_HSB, "colorSaturation", CHANNEL_LIGHT_HSB, "colorTemperature",
            "color-temperature", PROPERTY_OTA_STATUS, CHANNEL_OTA_STATUS, PROPERTY_OTA_STATE, CHANNEL_OTA_STATE,
            PROPERTY_OTA_PROGRESS, CHANNEL_OTA_PROGRESS);
    public static final Map<String, String> TEMPERATURE_LIGHT_MAP = Map.of("isOn", CHANNEL_STATE, "lightLevel",
            CHANNEL_LIGHT_BRIGHTNESS, "colorTemperature", CHANNEL_LIGHT_TEMPERATURE, PROPERTY_OTA_STATUS,
            CHANNEL_OTA_STATUS, PROPERTY_OTA_STATE, CHANNEL_OTA_STATE, PROPERTY_OTA_PROGRESS, CHANNEL_OTA_PROGRESS);

    public static final Map<String, String> SPEAKER_MAP = Map.of("playback", CHANNEL_PLAYER, "volume", CHANNEL_VOLUME,
            "isMuted", CHANNEL_MUTE, "playbackAudio", CHANNEL_TRACK, "playbackModes", CHANNEL_PLAY_MODES,
            PROPERTY_OTA_STATUS, CHANNEL_OTA_STATUS, PROPERTY_OTA_STATE, CHANNEL_OTA_STATE, PROPERTY_OTA_PROGRESS,
            CHANNEL_OTA_PROGRESS);

    public static final Map<String, String> SMART_PLUG_MAP = Map.of("isOn", CHANNEL_STATE, "currentActivePower",
            CHANNEL_POWER, "currentVoltage", CHANNEL_POTENTIAL, "currentAmps", CHANNEL_CURRENT, "statusLight",
            CHANNEL_DISABLE_STATUS_LIGHT, "childLock", CHANNEL_CHILD_LOCK, PROPERTY_OTA_STATUS, CHANNEL_OTA_STATUS,
            PROPERTY_OTA_STATE, CHANNEL_OTA_STATE, PROPERTY_OTA_PROGRESS, CHANNEL_OTA_PROGRESS);
    public static final Map<String, String> PLUG_MAP = Map.of("isOn", CHANNEL_STATE, "statusLight",
            CHANNEL_DISABLE_STATUS_LIGHT, "childLock", CHANNEL_CHILD_LOCK, PROPERTY_OTA_STATUS, CHANNEL_OTA_STATUS,
            PROPERTY_OTA_STATE, CHANNEL_OTA_STATE, PROPERTY_OTA_PROGRESS, CHANNEL_OTA_PROGRESS);

    public static final Map<String, String> LIGHT_SENSOR_MAP = Map.of("illuminance", CHANNEL_ILLUMINANCE);
    public static final Map<String, String> MOTION_SENSOR_MAP = Map.of("batteryPercentage", CHANNEL_BATTERY_LEVEL,
            "isDetected", CHANNEL_DETECTION);
    public static final Map<String, String> MOTION_LIGHT_SENSOR_MAP = Map.of("batteryPercentage", CHANNEL_BATTERY_LEVEL,
            "isDetected", CHANNEL_DETECTION, "illuminance", CHANNEL_ILLUMINANCE);

    public static final Map<String, String> CONTACT_SENSOR_MAP = Map.of("batteryPercentage", CHANNEL_BATTERY_LEVEL,
            "isOpen", CHANNEL_STATE);
    public static final Map<String, String> SCENE_MAP = Map.of("lastTriggered", CHANNEL_TRIGGER);
    public static final Map<String, String> REPEATER_MAP = Map.of(PROPERTY_OTA_STATUS, CHANNEL_OTA_STATUS,
            PROPERTY_OTA_STATE, CHANNEL_OTA_STATE, PROPERTY_OTA_PROGRESS, CHANNEL_OTA_PROGRESS);

    public static final Map<String, String> LIGHT_CONTROLLER_MAP = Map.of("batteryPercentage", CHANNEL_BATTERY_LEVEL,
            PROPERTY_OTA_STATUS, CHANNEL_OTA_STATUS, PROPERTY_OTA_STATE, CHANNEL_OTA_STATE, PROPERTY_OTA_PROGRESS,
            CHANNEL_OTA_PROGRESS);
    public static final Map<String, String> AIR_QUALITY_MAP = Map.of("currentTemperature", CHANNEL_TEMPERATURE,
            "currentRH", CHANNEL_HUMIDITY, "currentPM25", CHANNEL_PARTICULATE_MATTER, "vocIndex", CHANNEL_VOC_INDEX,
            PROPERTY_OTA_STATUS, CHANNEL_OTA_STATUS, PROPERTY_OTA_STATE, CHANNEL_OTA_STATE, PROPERTY_OTA_PROGRESS,
            CHANNEL_OTA_PROGRESS);
    public static final Map<String, String> WATER_SENSOR_MAP = Map.of("batteryPercentage", CHANNEL_BATTERY_LEVEL,
            "waterLeakDetected", CHANNEL_DETECTION, PROPERTY_OTA_STATUS, CHANNEL_OTA_STATUS, PROPERTY_OTA_STATE,
            CHANNEL_OTA_STATE, PROPERTY_OTA_PROGRESS, CHANNEL_OTA_PROGRESS);
}
